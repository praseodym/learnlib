package de.learnlib.algorithms.pack.ds;

import net.automatalib.words.Word;

/**
 *
 * @author fh
 */
public interface HypothesisChangeListener<I, SP, TP> 
{

    public void newState(Word<I> s, SP sp);
    
    public void newTransition(Word<I> src, I in, TP tp, Word<I> dst);
    
}
