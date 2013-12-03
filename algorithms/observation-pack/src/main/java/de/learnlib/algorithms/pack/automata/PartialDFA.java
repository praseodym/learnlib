package de.learnlib.algorithms.pack.automata;

import de.learnlib.logging.LearnLogger;
import net.automatalib.automata.dot.DOTHelperFSA;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;


/**
 * Implementation of Partial DFA. Words without runs are not accepted. 
 * For all other words acceptance depends on the state reached.
 * 
 * @author falk
 * 
 * @param <I> input class
 */
public class PartialDFA<I> extends 
        AbstractPartialMutableAutomaton<DFAState<I>, I, DFAState<I>, Boolean, Void> implements DFA<DFAState<I>, I> {

    private static final LearnLogger logger = 
            LearnLogger.getLogger(PartialDFA.class);
 
    public PartialDFA(Alphabet<I> inputs) {
        super(inputs);
    }
    
    public void setOutput(Word<I> word, Boolean out) {
        DFAState<I> state = trace(word);
        state.setAccepting(out);
        logger.logEvent("Set output to [" + out+ "]" + " at [" + 
                state.getId() + "]");
             
    }
    
    @Override
    protected DFAState<I> createState(int id, Word<I> as) {
        return new DFAState<>(id, as);
    }

    @Override
    public DFAState<I> getTransition(DFAState<I> state, I input) {
        return state.getSuccessor(input);
    }

    @Override
    public Boolean getStateProperty(DFAState<I> state) {
        return state != null && state.isAccepting();
    }

    @Override
    public Void getTransitionProperty(DFAState<I> transition) {
        return null;
    }

    @Override
    public boolean isAccepting(DFAState<I> state) {
        return getStateProperty(state);
    }

    @Override
    public boolean accepts(Iterable<I> input) {
        DFAState<I> state = trace(input);
        return isAccepting(state);
    }

    @Override
    public Boolean computeSuffixOutput(Iterable<I> prefix, Iterable<I> suffix) {
        return accepts(concatenate(prefix, suffix));
    }

    @Override
    public Boolean computeOutput(Iterable<I> input) {
        return accepts(input);
    }

    @Override
    public DFAState<I> getSuccessor(DFAState<I> transition) {
        return transition;
    }

    @Override
    public GraphDOTHelper<DFAState<I>, TransitionEdge<I, DFAState<I>>> getDOTHelper() {
        return new DOTHelperFSA<>(this);
    }

}