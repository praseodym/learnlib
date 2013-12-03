package de.learnlib.algorithms.pack.tree;

import de.learnlib.algorithms.pack.automata.api.TreeEventListener;
import de.learnlib.api.MembershipOracle;
import de.learnlib.logging.LearnLogger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import net.automatalib.words.Word;

/**
 * Tree for organizing and classify prefixes by suffixes.
 * 
 * @author falk
 * 
 * @param <I> input class
 * @param <O> output class
 */
public final class ClassificationTree<I, O> {

    private static final LearnLogger logger = 
            LearnLogger.getLogger(ClassificationTree.class);
        
    private final MembershipOracle<I, O> oracle;
    
    private final List<TreeEventListener<I, O>> listeners;

    private Node root;
    
    private final ReplaceChild<I, O> rootMonitor = new ReplaceChild<I, O>() {

        @Override
        public void replaceChild(LeafNode<I, O> oldChild, InnerNode<I, O> newChild) {
            root = newChild;
        }
    };
    
    private final LinkedHashMap<Word<I>, LeafNode> leaves = 
            new LinkedHashMap<>();
    
    public ClassificationTree(MembershipOracle<I, O> oracle, 
            List<TreeEventListener<I, O>> listeners) {
        this.oracle = oracle;
        this.listeners = listeners;
    }
    
    public void initWithLeafAsRoot() {
        Word<I> eps = Word.epsilon();
        this.root = new LeafNode(eps, this, rootMonitor);
        newLeaf( (LeafNode) this.root);        
    }
    
    public void initWithEmptySuffixAsRoot() {
        Word<I> eps = Word.epsilon();
        this.root = new InnerNode(eps, new HashMap<>(), this);
    }
           
    public void addPrefix(Word<I> prefix) {
        logger.logEvent("Adding new prefix [" + prefix + "] to tree ");
        root.sift(prefix, this.oracle);
    }
    
    public void splitLeaf(Word<I> prefix, Word<I> suffix) {
        LeafNode split = leaves.get(prefix);
        assert split != null;
        split.split(suffix, this.oracle);
    }
    
    public void addListener(TreeEventListener<I,O> listener) {
        this.listeners.add(this.listeners.size() - 1, listener);
    }
    
    void newLeaf(LeafNode leaf) {
        logger.logEvent("Creating new leaf for [" + leaf.getAccessSequence() + "]");                    
        this.leaves.put(leaf.getAccessSequence(), leaf);
        for (TreeEventListener<I, O> l : listeners) {
            l.newState(leaf.getAccessSequence());
        }
    }
    
    void newPrefixAtLeaf(Word<I> prefix, LeafNode leaf) {
        for (TreeEventListener<I, O> l : listeners) {
            l.newTransition(prefix, leaf.getAccessSequence());
        }        
    }       
    
}
