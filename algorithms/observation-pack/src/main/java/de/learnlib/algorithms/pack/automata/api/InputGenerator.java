package de.learnlib.algorithms.pack.automata.api;

import java.util.Collection;
import net.automatalib.words.Word;

/**
 * An input generator is used by ObservationPack to determine which words
 * to explore. It is called whenever a new state is to be expanded.
 * 
 * @author falk
 * 
 * @param <I> input class
 */
public interface InputGenerator<I> {

    /**
     * Called by ObservationPack to get one-letter extensions to 
     * explore from a new state.
     * 
     * @param access sequence of expanded state 
     * 
     * @return One-letter continuations that shall be explored
     */
    public Collection<Word<I>> generateExtensions(Word<I> prefix);
             
}
