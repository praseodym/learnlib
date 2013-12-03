package de.learnlib.algorithms.pack.tree;

import de.learnlib.api.MembershipOracle;
import net.automatalib.words.Word;

/**
 * Abstract implementation of a node for the classification tree. 
 * 
 * @author falk
 */
abstract class Node<I, O> {
    
    protected final ClassificationTree<I, O> tree;

    Node(ClassificationTree<I, O> tree) {
        this.tree = tree;
    }
        
    abstract void sift(Word<I> prefix, MembershipOracle<I, O> oracle);
    
}
