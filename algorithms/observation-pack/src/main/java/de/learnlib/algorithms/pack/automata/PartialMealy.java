package de.learnlib.algorithms.pack.automata;

import de.learnlib.logging.LearnLogger;
import java.util.ArrayList;
import java.util.List;
import net.automatalib.automata.dot.DOTHelperMealy;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.MealyTransition;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Implementation of Partial Mealy Machine. Words without runs produce special 
 * error outputs.
 * 
 * @author falk
 * 
 * @param <I> input class
 * @param <O> output class
 */
public class PartialMealy<I, O> extends 
        AbstractPartialMutableAutomaton<MealyState<I, O>, I, MealyTransition<MealyState<I, O>, O>, Void, O> implements 
        MealyMachine<MealyState<I, O>, I, MealyTransition<MealyState<I, O>, O>, O> {

    private static final LearnLogger logger = 
            LearnLogger.getLogger(AbstractPartialMutableAutomaton.class);
            
    private final O errorOut;
    
    public PartialMealy(Alphabet<I> inputs, O errorOut) {
        super(inputs);
        this.errorOut = errorOut;
    }
    
    @Override
    protected MealyState<I, O> createState(int id, Word<I> as) {
        return new MealyState<>(id, as);
    }

    public void setOutput(Word<I> word, O out) {
        Word<I> prefix = word.prefix(-1);
        I input = word.lastSymbol();
        MealyState<I, O> state = trace(prefix);
        
        logger.logEvent("Set output to [" + out+ "]" + " at [" + 
                state.getId() + "] for input [" + input + "]");
        
        state.setOutput(input, out);
    }

    @Override
    public Void getStateProperty(MealyState<I, O> state) {
        return null;
    }

    @Override
    public O getOutput(MealyState<I, O> state, I input) {
        return state.getOutput(input);
    }

    @Override
    public void trace(Iterable<I> input, List<O> output) {
        trace(getInitialState(), input, output);
    }

    @Override
    public void trace(MealyState<I, O> state, Iterable<I> input, List<O> output) {
        MealyState<I, O> cur = state;
        for (I i : input) {
            O out = errorOut;
            if (cur != null) {
                O tmp = cur.getOutput(i);
                if (tmp != null) 
                    out = tmp;
                
                cur = cur.getSuccessor(i);
            }
            output.add(out);
        }
    }

    @Override
    public Word<O> computeOutput(Iterable<I> input) {
        Word<I> eps = Word.epsilon();
        return computeSuffixOutput(eps, input);
    }

    @Override
    public Word<O> computeSuffixOutput(Iterable<I> prefix, Iterable<I> suffix) {
        MealyState<I, O> state = trace(prefix);                
        List<O> output = new ArrayList<>();
        trace(state, suffix, output);
        return Word.fromList(output);
    }

    @Override
    public O getTransitionProperty(MealyTransition<MealyState<I, O>, O> transition) {
        return transition.getOutput();
    }

    @Override
    public O getTransitionOutput(MealyTransition<MealyState<I, O>, O> transition) {
        return getTransitionProperty(transition);
    }

    @Override
    public MealyTransition<MealyState<I, O>, O> getTransition(MealyState<I, O> state, I input) {
        return new MealyTransition<>(state.getSuccessor(input), state.getOutput(input));
    }

    @Override
    public MealyState<I, O> getSuccessor(MealyTransition<MealyState<I, O>, O> transition) {
        return transition.getSuccessor();
    }

    @Override
    public GraphDOTHelper<MealyState<I, O>, TransitionEdge<I, MealyTransition<MealyState<I, O>, O>>> getDOTHelper() {
        return new DOTHelperMealy<>(this);
    }    
}
