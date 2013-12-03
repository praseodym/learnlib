package de.learnlib.algorithms.pack.automata;

import net.automatalib.words.Word;

/**
 * State implementation for DFAs. Provides acceptance information.
 * 
 * @author falk
 * 
 * @param <I> input class
 */
public class DFAState<I> extends AbstractState<I> {

    private boolean accepting;
    
    public DFAState(int id, Word<I> accessor) {
        super(id, accessor);
    }      
    @Override
    DFAState<I> getSuccessor(I input) {
        return (DFAState<I>) super.getSuccessor(input);
    }

    boolean isAccepting() {
        return accepting;
    }

    void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }
        
}
