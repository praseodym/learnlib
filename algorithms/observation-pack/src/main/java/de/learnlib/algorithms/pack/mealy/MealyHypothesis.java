package de.learnlib.algorithms.pack.mealy;

import de.learnlib.algorithms.pack.automata.PartialMealy;
import de.learnlib.algorithms.pack.generic.Hypothesis;
import de.learnlib.api.MembershipOracle;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 *
 * @author falk
 */
class MealyHypothesis<I, O> implements Hypothesis<MealyMachine<?, I, ?, O>, I, O> {

    private final PartialMealy<I, O> model;
    
    public MealyHypothesis(O errorOut, Alphabet<I> inputs) {
        model = new PartialMealy<>(inputs, errorOut);
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
    public O computeSuffixOutput(Iterable<I> prefix, Iterable<I> suffix) {
        return model.computeSuffixOutput(prefix, suffix).lastSymbol();
    }

    @Override
    public O computeOutput(Iterable<I> input) {
        return model.computeOutput(input).lastSymbol();
    }

    @Override
    public MealyMachine<?, I, ?, O> getModel() {
        return model;
    }

    @Override
    public void setOutput(Word<I> prefix, O out) {
        model.setOutput(prefix, out);
    }
    
}
