//package de.learnlib.eqtests.basic;
//
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.List;
//
//import de.learnlib.api.EquivalenceOracle;
//import de.learnlib.oracles.DefaultQuery;
//
//import net.automatalib.automata.UniversalDeterministicAutomaton;
//import net.automatalib.automata.concepts.SuffixOutput;
//
//public class FakeRandomEQOracle<I, O> implements EquivalenceOracle<UniversalDeterministicAutomaton<?, I, ?, ?, ?>, I, O> {
//	
//	private final UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference;
//	private final SuffixOutput<I,O> output;
//
//	public <R extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & SuffixOutput<I, O>>
//	FakeRandomEQOracle(R reference) {
//		this.reference = reference;
//		this.output = reference;
//	}
//
//	@Override
//	public DefaultQuery<I, O> findCounterExample(
//			UniversalDeterministicAutomaton<?, I, ?, ?, ?> hypothesis,
//			Collection<? extends I> inputs) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	
//	private static final class SeparatorNode<I> {
//		private final List<SeparatorEdge<I>> outgoing;
//		private final List<SeparatorEdge<I>> incoming;
//		
//		private final List<SeparatorEdge<I>> updated;
//		
//		
//		private boolean needsUpdate;
//		
//		public SeparatorNode() {
//			
//		}
//		
//		public boolean update() {
//			int updMin = Integer.MAX_VALUE;
//			int updMax = 0;
//			for(SeparatorEdge<I> edge : updated) {
//				
//			}
//		}
//	}
//	
//	private static final class SeparatorEdge<I> {
//		private final boolean separator;
//		private final I input;
//		private final SeparatorNode<I> source;
//		private final SeparatorNode<I> target;
//		
//		private int minDistance = Integer.MAX_VALUE;
//		private int maxDistance = 0;
//		
//		public boolean isSeparator() {
//			return separator;
//		}
//		
//		public I getInput() {
//			return input;
//		}
//		
//		public SeparatorNode<I> getSource() {
//			return source;
//		}
//		
//		public SeparatorNode<I> getTarget() {
//			return target;
//		}
//	}
//	
//	public static <I> SeparatorNode<I> buildSeparatorGraph(
//			UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton1,
//			UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton2,
//			Collection<? extends I> inputs) {
//		
//	}
//	
//	private static final class Record<S1,S2,I> {
//		public final SeparatorNode<I> sepNode;
//		public final S1 s1;
//		public final S2 s2;
//		public final Iterator<? extends I> inputsIt;
//		
//		public Record(SeparatorNode<I> sepNode, S1 s1, S2 s2, Iterator<? extends I> inputsIt) {
//			this.sepNode = sepNode;
//			this.s1 = s1;
//			this.s2 = s2;
//			this.inputsIt = inputsIt;
//		}
//	}
//	
//	private static <S1,S2,I,T1,T2> doBuildSeparatorGraph(
//			UniversalDeterministicAutomaton<S1, I, T1, ?, ?> automaton1,
//			UniversalDeterministicAutomaton<S2, I, T2, ?, ?> automaton2,
//			Collection<? extends I> inputs) {
//		
//	}
//
//}
