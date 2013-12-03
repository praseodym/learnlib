package de.learnlib.algorithms.pack.automata;

import java.util.HashMap;
import java.util.Map;
import net.automatalib.words.Word;

/**
 * State implementation for Mealy Machines. Provides output function.
 * 
 * @author falk
 * 
 * @param <I> input class
 * @param <O> output class
 */
public class MealyState<I, O> extends AbstractState<I> {
    
    private final Map<I, O> output = new HashMap<>();

    public MealyState(int id, Word<I> accessor) {
        super(id, accessor);
    }     
    
    void setOutput(I i, O o) {
        this.output.put(i, o);
    }
    
    O getOutput(I i) {
        return this.output.get(i);
    }
    
    @Override
    MealyState<I, O> getSuccessor(I input) {
        return (MealyState<I, O>) super.getSuccessor(input);
    }    
}
