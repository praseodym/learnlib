
package de.learnlib.algorithms.pack;

import net.automatalib.words.Word;

/**
 * Selective exploration can be provided to the learning algorithm with
 * two consequences.
 * 1) before adding a new word to the observations, the learning algorithm
 * will invoke the explore method. Only if true is returned the word will
 * actually be added.
 *
 *  @param <I> input symbol class
 *
 * @author falkhowar
 */
public interface SelectiveExploration<I, O> {

    /**
     * tests if the word w should be used as a prefix in the observations.
     *
     * @param w tested word
     * @return true if word should be used, false otherwise
     */
    O explore(Word<I> w);
}
