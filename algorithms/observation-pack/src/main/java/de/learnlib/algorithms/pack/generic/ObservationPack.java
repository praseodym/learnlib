package de.learnlib.algorithms.pack.generic;

import de.learnlib.algorithms.pack.automata.api.InputGenerator;
import de.learnlib.algorithms.pack.tree.ClassificationTree;
import de.learnlib.algorithms.pack.automata.api.TreeEventListener;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.logging.LearnLogger;
import de.learnlib.oracles.DefaultQuery;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import net.automatalib.words.Word;

/**
 *
 * @author falk
 * @param <A>
 * @param <I>
 * @param <O>
 */
public abstract class ObservationPack<A, I, O> implements LearningAlgorithm<A, I, O>{

    private static final LearnLogger logger = 
            LearnLogger.getLogger(ObservationPack.class);
    
    protected final MembershipOracle<I, O> oracle;
    
    protected final InputGenerator<I> inputGenerator;

    protected final ClassificationTree<I, O> tree;
    
    protected final Hypothesis<A, I, O> hypothesis;
    
    private final Queue<DefaultQuery<I, O>> counterexamples = new LinkedList<>();
    
    protected final Queue<Word<I>> newPrefixes = new LinkedList<>();
    
    private final TreeEventListener<I, O> listener = new TreeEventListener<I, O>() {

        @Override
        public void newState(Word<I> as) {
            exploreState(as);
        }

        @Override
        public void newTransition(Word<I> trans, Word<I> dest) {
            logger.logEvent("New Transition [" + trans + "] to [" + dest + "]");
            hypothesis.createTransition(trans, dest);
        }
    };

    protected ObservationPack(MembershipOracle<I, O> oracle, 
            Hypothesis<A, I, O> hypothesis, InputGenerator<I> inputGenerator) {
        this.oracle = oracle;
        this.hypothesis = hypothesis;
        this.inputGenerator = inputGenerator;        
        
        this.tree = new ClassificationTree<>(
                this.oracle, Collections.singletonList(this.listener));        
    }
    
    @Override
    public boolean refineHypothesis(DefaultQuery<I, O> dq) {  
        this.counterexamples.add(dq);
        return refine();
    }
    
    private boolean refine() {
        boolean refined = true;
        while (!this.counterexamples.isEmpty()) {
            DefaultQuery<I, O> ceQuery = this.counterexamples.peek();
            O hypOut = this.hypothesis.computeOutput(ceQuery.getInput());
            if (hypOut.equals(ceQuery.getOutput())) {
                this.counterexamples.poll();
                logger.logEvent("Word [" + ceQuery.getInput()+ 
                        "] is no longer a counterexample");
                continue;
            }
            
            logger.logPhase("Refine Hypothesis with counterexample [" + 
                    ceQuery.getInput() + "]");                

            int idx = LocalSuffixFinders.findRivestSchapire(
                    ceQuery, this.hypothesis, this.hypothesis, this.oracle);
            
            assert idx >= 0;
            refined = true;
            
            Word<I> ce = ceQuery.getInput();
            Word<I> prefix = ce.prefix(idx);
            Word<I> suffix = ce.subWord(idx);
            Word<I> as = this.hypothesis.transformAccessSequence(prefix);

            logger.logEvent("CE split index: [" + idx + "]");                
            logger.logEvent("Prefix: [" + prefix + "]");                
            logger.logEvent("Access: [" + as + "]");                
            logger.logEvent("Suffix: [" + suffix + "]");                
                        
            this.tree.splitLeaf(as, suffix);
            close();
        }
        return refined;
    }

    protected void close() {
        while (!this.newPrefixes.isEmpty()) {
            this.tree.addPrefix(this.newPrefixes.poll());
        }
    }
    
    @Override
    public A getHypothesisModel() {
        return hypothesis.getModel();
    }
    
    protected abstract void exploreState(Word<I> accessSequence);
    
}
