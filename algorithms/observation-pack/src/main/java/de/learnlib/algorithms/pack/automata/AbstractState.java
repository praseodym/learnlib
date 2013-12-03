package de.learnlib.algorithms.pack.automata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.automatalib.words.Word;

/**
 * Abstract state implementation. Provides IDs and storage of access sequences.
 * 
 * @author falk
 * 
 * @param <I> input class
 */
abstract class AbstractState<I> {

    private final int id;
    
    private final Map<I, AbstractState<I>> succ = new HashMap<>();
        
    private final Word<I> accessor;

    protected AbstractState(int id, Word<I> accessor) {
        this.id = id;
        this.accessor = accessor;
    }    
    
    AbstractState<I> getSuccessor(I input) {
        return succ.get(input);
    }
    
    void setSuccessor(I i, AbstractState<I> s) {
        succ.put(i, s);
    }

    Word<I> getAccessor() {
        return accessor;
    }
    
    int getId() {
        return this.id;
    }
    
    Set<I> getInputs() {
        return this.succ.keySet();
    }       

    @Override
    public String toString() {
        return "" + getId();
    }
    

}
