package de.learnlib.algorithms.pack.automata.api;

import net.automatalib.words.Word;

/**
 * Listener for events issued by the discrimination tree.
 * 
 * @author falk
 * 
 * @param <I> input class
 * @param <O> output class
 */
public interface TreeEventListener<I, O> {
    
    /**
     * callback for new states.
     * 
     * @param as 
     */
    public void newState(Word<I> as);
    
    /**
     * callback for new transitions.
     * 
     * @param trans
     * @param dest 
     */
    public void newTransition(Word<I> trans, Word<I> dest);
    
}
