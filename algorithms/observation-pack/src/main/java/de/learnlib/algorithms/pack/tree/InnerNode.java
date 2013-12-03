package de.learnlib.algorithms.pack.tree;

import de.learnlib.api.MembershipOracle;
import de.learnlib.logging.LearnLogger;
import de.learnlib.oracles.DefaultQuery;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import net.automatalib.words.Word;

/**
 * Implementation of inner nodes. Inner nodes are used to sift prefixes
 * into the tree. They are labeled with suffixes used to classify prefixes.
 * 
 * @author falk
 * 
 */
final class InnerNode<I, O> extends Node<I, O> implements ReplaceChild<I, O> {

    private static final LearnLogger logger
            = LearnLogger.getLogger(InnerNode.class);

    private final Word<I> suffix;

    private final Map<O, Node> children;

    public InnerNode(Word<I> suffix, Map<O, Node> children, ClassificationTree<I, O> tree) {
        super(tree);
        this.suffix = suffix;
        this.children = children;
    }

    @Override
    void sift(Word<I> prefix, MembershipOracle<I, O> oracle) {
        DefaultQuery<I, O> query = new DefaultQuery<>(prefix, this.suffix);
        oracle.processQueries(Collections.singleton(query));
        O out = query.getOutput();

        logger.logQuery("Sift [" + query.getPrefix()
                + "] at [" + query.getSuffix() + "] to [" + out + "]");

        Node n = this.children.get(out);
        if (n != null) {
            n.sift(prefix, oracle);
        } else {
            LeafNode leaf = new LeafNode(prefix, this.tree, this);
            this.children.put(out, leaf);
            this.tree.newLeaf(leaf);

            assert this.children.containsValue(leaf);
        }

    }

    @Override
    public void replaceChild(LeafNode<I, O> oldChild, InnerNode<I, O> newChild) {

        logger.logEvent("Replacing leaf [" + oldChild.getAccessSequence() + "] by inner node "
                + "[" + newChild.suffix + "] at inner node [" + suffix + "]");

        O key = null;
        for (Entry<O, Node> e : this.children.entrySet()) {
            if (e.getValue() == oldChild) {
                key = e.getKey();
                break;
            }
        }

        assert key != null;
        this.children.put(key, newChild);
    }

}
