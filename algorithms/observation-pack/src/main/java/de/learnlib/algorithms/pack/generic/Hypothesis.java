package de.learnlib.algorithms.pack.generic;

import de.learnlib.api.AccessSequenceTransformer;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Word;

/**
 *
 * @author falk
 * @param <A>
 * @param <I>
 * @param <O>
 */
public interface Hypothesis<A, I, O> extends 
        AccessSequenceTransformer<I>, SuffixOutput<I, O> {
    
    public void createState(Word<I> accessSequence);
        
    public void createTransition(Word<I> transition, Word<I> destination);
    
    public A getModel();
    
    public void setOutput(Word<I> prefix, O out);
}
