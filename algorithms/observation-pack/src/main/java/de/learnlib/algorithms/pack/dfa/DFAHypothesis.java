package de.learnlib.algorithms.pack.dfa;

import de.learnlib.algorithms.pack.automata.PartialDFA;
import de.learnlib.algorithms.pack.generic.Hypothesis;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 *
 * @author falk
 * @param <I>
 */
public class DFAHypothesis<I> implements Hypothesis<DFA<?, I>, I, Boolean> {

    private final PartialDFA<I> model;

    public DFAHypothesis(Alphabet<I> inputs) {
        this.model = new PartialDFA<>(inputs); 
    }
        
    @Override
    public void createState(Word<I> accessSequence) {
        model.addState(accessSequence);
    }

    @Override
    public void createTransition(Word<I> transition, Word<I> destination) {
        model.addTransition(transition, destination);
    }

    @Override
    public Word<I> transformAccessSequence(Word<I> word) {
        return model.transformAccessSequence(word);
    }

    @Override
    public boolean isAccessSequence(Word<I> word) {
        return model.isAccessSequence(word);
    }

    @Override
    public Boolean computeSuffixOutput(Iterable<I> prefix, Iterable<I> suffix) {
        return model.computeSuffixOutput(prefix, suffix);
    }

    @Override
    public Boolean computeOutput(Iterable<I> input) {
        return model.computeOutput(input);
    }

    @Override
    public DFA<?, I> getModel() {
        return model;
    }

    @Override
    public void setOutput(Word<I> prefix, Boolean out) {
        model.setOutput(prefix, out);
    }
   
}
