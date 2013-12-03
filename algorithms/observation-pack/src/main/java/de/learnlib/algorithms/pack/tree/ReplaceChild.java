package de.learnlib.algorithms.pack.tree;

/**
 * Interface used by parent nodes in the tree that allow replacing a child node.
 * 
 * @author falk
 * 
 */
interface ReplaceChild<I, O> {

    /**
     * replaces a child node by another one.
     * 
     * @param oldChild
     * @param newChild 
     */
    void replaceChild(LeafNode<I, O > oldChild, InnerNode<I, O> newChild);
    
}
