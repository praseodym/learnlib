package de.learnlib.algorithms.pack.tree;

import de.learnlib.api.MembershipOracle;
import de.learnlib.logging.LearnLogger;
import de.learnlib.oracles.DefaultQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.automatalib.words.Word;

/**
 * Implementation of a leaf. Leaves store prefixes and correspond to states.
 * 
 * @author falk
 */
final class LeafNode<I, O> extends Node<I, O> {
        
    private static final LearnLogger logger = 
            LearnLogger.getLogger(LeafNode.class);
            
    private final Word<I> accessSequence;

    private final List<Word<I>> transitions;
    
    private ReplaceChild<I, O> parent;
    
    LeafNode(Word<I> accessSequence, ClassificationTree<I, O> tree, 
            ReplaceChild<I, O> parent) {
        super(tree);
        this.accessSequence = accessSequence;
        this.transitions = new ArrayList<>();
        this.parent = parent;
    }
    
   
    @Override
    void sift(Word<I> prefix, MembershipOracle<I, O> oracle) {
        logger.logEvent("Adding new prefix [" + prefix + "] to leaf [" + this.getAccessSequence() + "]");
        this.transitions.add(prefix);
        this.tree.newPrefixAtLeaf(prefix, this);
    }
    
    void split(Word<I> suffix, MembershipOracle<I, O> oracle) {
        logger.logEvent("Split leaf [" + accessSequence + "] by suffix [" + suffix + "]");
        
        List<Word<I>> prefixes = new ArrayList<>(this.transitions);
        this.transitions.clear();
        
        DefaultQuery<I, O> query = 
                new DefaultQuery<>(this.accessSequence, suffix);
        oracle.processQueries(Collections.singletonList(query));
        O refOut = query.getOutput();

        logger.logEvent("Making leaf [" + accessSequence + "] the [" + 
                refOut + "] successor at [" + suffix + "]");
        
        Map<O, LeafNode> children = new HashMap<>();
        children.put(refOut, this);
        InnerNode inner = new InnerNode(suffix, children, this.tree);
        this.parent.replaceChild(this, inner);
        this.parent = inner;
                
        for (Word<I> prefix : prefixes) {
            inner.sift(prefix, oracle);
        }
        
        assert !this.transitions.equals(prefixes);
    }

    public Word<I> getAccessSequence() {
        return accessSequence;
    }
    

}
