/* Copyright (C) 2013-2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.parallelism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Throwables;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.settings.LearnLibSettings;


/**
 * A membership oracle that statically distributes a set of queries among several threads.
 * <p>
 * An incoming set of queries is divided into a given number of batches, such that the sizes of
 * all batches differ by at most one. This keeps the required synchronization effort low, but
 * if some batches are "harder" (for whatever reason) than others, the load can be very unbalanced.  
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> output domain type
 */
@ParametersAreNonnullByDefault
public class StaticParallelOracle<I, D> implements ParallelOracle<I, D> {
	
	static {
		LearnLibSettings settings = LearnLibSettings.getInstance();
		
		DEFAULT_MIN_BATCH_SIZE = settings.getInt("parallel.static.min_batch_size", 10);
		
		int numCores = Runtime.getRuntime().availableProcessors();
		DEFAULT_NUM_INSTANCES = settings.getInt("parallel.static.num_instances", numCores);
		
		DEFAULT_POOL_POLICY = settings.getEnumValue("parallel.static.pool_policy", PoolPolicy.class, PoolPolicy.CACHED);
	}
	
	public static final int DEFAULT_MIN_BATCH_SIZE;
	public static final int DEFAULT_NUM_INSTANCES;
	public static final PoolPolicy DEFAULT_POOL_POLICY;
	
	@Nonnegative
	private final int minBatchSize;
	@Nonnull
	private final MembershipOracle<I, D>[] oracles;
	@Nonnull
	private final ExecutorService executor;

	@SuppressWarnings("unchecked")
	public StaticParallelOracle(Collection<? extends MembershipOracle<I,D>> oracles,
			@Nonnegative int minBatchSize,
			PoolPolicy policy) {
		
		this.oracles = oracles.toArray(new MembershipOracle[oracles.size()]);
		
		switch(policy) {
		case FIXED:
			this.executor = Executors.newFixedThreadPool(this.oracles.length - 1);
			break;
		case CACHED:
			this.executor = Executors.newCachedThreadPool();
			break;
		default:
			throw new IllegalArgumentException("Illegal pool policy: " + policy);
		}
		this.minBatchSize = minBatchSize;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.MembershipOracle#processQueries(java.util.Collection)
	 */
	@Override
	public void processQueries(Collection<? extends Query<I, D>> queries) {
		int num = queries.size();
		if(num <= 0)
			return;
		
		int numBatches = (num - minBatchSize)/minBatchSize + 1;
		if(numBatches > oracles.length)
			numBatches = oracles.length;
		
		// Calculate the number of full and non-full batches. The difference in size
		// will never exceed one (cf. pidgeonhole principle)
		int fullBatchSize = (num - 1)/numBatches + 1;
		int nonFullBatches = fullBatchSize*numBatches - num;
		
		// One batch is always executed in the local thread. This saves the thread creation
		// overhead for the common case where the batch size is quite small.
		int externalBatches = numBatches - 1;
		
		if(externalBatches == 0) {
			processQueriesLocally(queries);
			return;
		}
		
		List<Future<?>> futures = new ArrayList<>(externalBatches);
		
		Iterator<? extends Query<I,D>> queryIt = queries.iterator();
		
		// Start the threads for the external batches
		for(int i = 0; i < externalBatches; i++) {
			int bs = fullBatchSize;
			if(i < nonFullBatches)
				bs--;
			List<Query<I,D>> batch = new ArrayList<>(bs);
			for(int j = 0; j < bs; j++) {
				batch.add(queryIt.next());
			}
			
			Runnable job = new StaticQueriesJob<>(batch, oracles[i + 1]);
			Future<?> future = executor.submit(job);
			futures.add(future);
		}
		
		
		// Finally, prepare and process the batch for the oracle executed in this thread.
		List<Query<I,D>> localBatch = new ArrayList<>(fullBatchSize);
		for(int j = 0; j < fullBatchSize; j++) {
			localBatch.add(queryIt.next());
		}
		
		processQueriesLocally(localBatch);
		
		try {
			for(Future<?> f : futures) {
				f.get();
			}
		}
		catch(ExecutionException ex) {
			Throwables.propagateIfPossible(ex.getCause());
			throw new AssertionError("Runnable must not throw checked exceptions", ex);
		}
		catch(InterruptedException ex) {
			Thread.interrupted();
			throw new ParallelOracleInterruptedException(ex);
		}
	}
	
	private void processQueriesLocally(Collection<? extends Query<I,D>> localBatch) {
		oracles[0].processQueries(localBatch);
	}
	
	@Override
	public void shutdown() {
		executor.shutdown();
	}
	
	@Override
	public void shutdownNow() {
		executor.shutdownNow();
	}

}
