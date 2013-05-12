package de.learnlib.algorithms.pack.ds;

import de.learnlib.logging.LearnLogger;
import de.learnlib.oracles.DefaultQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;


import java.util.ArrayDeque;
import java.util.List;
import java.util.Map.Entry;
import net.automatalib.words.Word;


class ClassificationTree<I, O> {

    static LearnLogger logger = LearnLogger.getLogger(ClassificationTree.class.getName());

    static abstract class Node<I, O> {
    }

    static final class InnerNode<I, O> extends Node<I, O> {
        private final Word<I> suffix;
        private final LinkedHashMap<O, Node<I, O>> children = new LinkedHashMap<>();

	private InnerNode(Word<I> suffix) {
            this.suffix = suffix;
        }
    }

    static final class Leaf<I, O> extends Node<I, O> {
        private final Component<I, O> component;
        private final Node<I, O> parent;

        private Leaf(Component<I, O> component, Node<I, O> parent) {
            this.component = component;
            this.parent = parent;
        }
        
        Component<I, O> getComponent() {
            return component;
        }
    }

    private ObservationPack<?, I, O> pack;

    private Node<I, O> root;

    private LinkedHashMap<Word<I>, Leaf<I, O>> components;

    private List<HypothesisChangeListener> changeListener = new ArrayList<>(); 

//    ClassificationTree(Component<I, O> c, ObservationPack pack, A, 
//            HypothesisCshangeListener changeListener) {
//        this.changeListener = changeListener;
//        this.pack= pack;
//        if (fsa) {
//            hypothesis = new AnnotatedFSA(pack.getInputs());
//        } else {
//            hypothesis = new AnnotatedAutomaton(pack.getInputs());
//        }
//        components = new LinkedHashMap<State, Component>();
//        accessors  = new LinkedHashMap<Word, State>();
//
//        // init
//        components.put(hypothesis.getStart(), c);
//        ((Annotated)hypothesis).setAccessSequence(
//                hypothesis.getStart(),c.getAccessSequence());
//
//        accessors.put(c.getAccessSequence(), hypothesis.getStart());
//        root = new Leaf();
//        ((Leaf)root).component = c;
//        
//        if (changeListener != null)
//            changeListener.initialState(hypothesis.getStart());
//    }

    void addRows(Collection<OrdinaryRow<I, O>> rows) {        
        HashMap<OrdinaryRow<I, O>, InnerNode<I, O>> positions = new HashMap<>();
        HashMap<OrdinaryRow<I, O>, LinkedHashMap<Word<I>, O>> suffixTraceMaps = new HashMap<>();
        for (OrdinaryRow<I, O> r : rows) {
            positions.put(r, (InnerNode) root);
            suffixTraceMaps.put(r, new LinkedHashMap());
        }

        // record extra output for transitions
        if (!pack.useEpsilon()) {
            List<ColumnZeroQuery<I, O>> queries = new ArrayList<>();
            for (OrdinaryRow<I, O> r : rows) {
                queries.add(new ColumnZeroQuery(r));            
            }
            pack.processQueries(queries);
        }
        
        // sift into tree
        while (!positions.isEmpty()) {
            // prepare queries
            List<DefaultQuery<I, O>> queries = new ArrayList<>();
            for (Entry<OrdinaryRow<I, O>, InnerNode<I, O>> e : positions.entrySet()) {
                queries.add(new DefaultQuery(e.getKey(), e.getValue().suffix));
            }                
            // perform queries
            pack.processQueries(queries);            
            // analyze results
            List<OrdinaryRow<I, O>> remove = new ArrayList<>();
            for (DefaultQuery<I, O> query : queries) {
                OrdinaryRow<I, O> row = (OrdinaryRow) query.getPrefix();
                InnerNode<I, O> n = positions.get(row);
                
                LinkedHashMap<Word<I>, O> suffixMap = suffixTraceMaps.get(row);
                suffixMap.put(query.getSuffix(), query.getOutput());
                Node<I, O> next = n.children.get(query.getOutput());

                // still in the tree
                if (next instanceof InnerNode) {
                    positions.put(row, (InnerNode) next);
                    continue;
                }
                
                // new leaf?
                if (next == null) {
                    Component c = new Component(row, suffixMap, this.pack);
                    this.pack.addComponent(c);
                    remove.add(row);                
                }
                                
                // existing leaf
                Leaf<I, O> l = (Leaf) next;
                l.component.addRow(row, suffixMap.keySet());
                setTransition(row, suffixMap.get( (Word<I>) Word.epsilon()), l.component);
                remove.add(row);
            }

            for (OrdinaryRow<I, O> row : remove) {
                positions.remove(row);
            }                
        }
    }


//    void addWord(Word w)
//    {
//        Symbol o = WordUtil.lastSymbol(pack.membershipQuery(w));
//
//        LinkedHashMap<Word,Word> suffixTraceMap = new LinkedHashMap<Word, Word>();
//        Node act = root;
//        while (act instanceof InnerNode)
//        {
//            InnerNode n = (InnerNode)act;
//            Word trace = pack.membershipQuery(w, n.suffix);
//            act = n.children.get(trace);
//            suffixTraceMap.put(n.suffix, trace);
//        }
//
//        // come to a leaf?
//        if (act instanceof Leaf)
//        {
//            Leaf l = (Leaf)act;
//            l.component.addWord(w, o, suffixTraceMap.keySet());
//            setTransition(w, o, l.component);
//            return;
//        }
//
//        // come to an end?
//        Component c = new Component(w, o, suffixTraceMap);
//
//        pack.addComponent(c);
//    }


    void splitLeaf(Component<I, O> c, Collection<Component<I, O>> newC) {
//        logger.logEvent("New state: " + c.getPrincipalRow().getPrefix());
//        this.components.put(c.getPrincipalRow().getPrefix(), c);

//        for (HchangeListener != null) {
//            changeListener.newState(s);
//        }
//        
//        for (Word w : c.getRows()) {
//            setTransition(w, c.getOutput(w), c);
//        }
//        // what about principal row?

        InnerNode<I, O> prev = null;
        Node<I, O> act = root;
        O out = null;

        int column = pack.useEpsilon() ? 1 : 0;
        while (act instanceof InnerNode) {
            InnerNode<I, O> n = (InnerNode)act;
            out = c.getPrincipalRow().getColumnOutput(column);
            assert n.suffix.equals(c.getPrincipalRow().getColumnSuffix(column));
            column++;
            prev = n;
            act = n.children.get(out);
        }

        // root is leaf
        if (out == null) {
            return;
        }
        
        // come to an end -> add leaf?
        if (act == null) {
            Leaf l = new Leaf();
            l.component = c;
            prev.children.put(out, l);                       
            return;
        }

        // come to a leaf -> replace by inner node ...        
        Leaf<I, O> oldLeaf = (Leaf)act;
        Leaf<I, O> newLeaf = new Leaf<>();
        newLeaf.component = c;

        for (int i = 0; i < oldLeaf.component.getPrincipalRow().columns(); i++)
        {
            O oOld = oldLeaf.component.getPrincipalRow().getColumnOutput(i);
            O oNew = newLeaf.component.getPrincipalRow().getColumnOutput(i);
            Word<I> suffix = oldLeaf.component.getPrincipalRow().getColumnSuffix(i);
            assert suffix.equals(newLeaf.component.getPrincipalRow().getColumnSuffix(i));
            if (oOld.equals(oNew)) {
                continue;
            }
            
            InnerNode newInner = new InnerNode(suffix);
            newInner.children.put(oOld, oldLeaf);
            newInner.children.put(oNew, newLeaf);

            prev.children.remove(out);
            prev.children.put(out, newInner);
            return;
        }
        assert false;        
    }

    
    private void addState(Component<I, O> comp) {
    
    }

    private void setTransition(Row<I, O> row, O out, Component to) {
        if (row.getPrefix().size() < 1) {
            return;
        }

        if (o == null)
            return;

        if (dest.equals(src.getTransitionState(in)))
            return;
        
        if (changeListener != null) {
            changeListener.newTransition(src, in, o, dest);
        }
    }


//    /**
//     * @return the hypothesis
//     */
//    Automaton getHypothesis() {
//        return hypothesis;
//    }

    /**
     * @return the components
     */
    Collection<Leaf<I, O>> getLeaves() {
        return components.values();
    }

    /**
     * @return the accessors
     */
    LinkedHashMap<Word,State> getAccessors() {
        return accessors;
    }

    @Override
    public String toString()
    {
        StringBuilder ret = new StringBuilder();
        ret.append(
                "digraph final {\n" +
                "    node [shape=record];\n"+
                "    rankdir=LR;\n\n");

        // ctree nodes & edges
        ArrayDeque<Node> nodes = new ArrayDeque<>();
        nodes.offer(root);

        String edges = "";
        while (!nodes.isEmpty())
        {
            Node n = nodes.poll();
            if (n instanceof Leaf)
                continue;

            InnerNode in = (InnerNode) n;

            ret.append("n_").append(in.hashCode()).append(" [label=\"{").append(in.suffix).append("|{");

            for (Word<I> w : in.children.keySet())
            {
                Node child = in.children.get(w);

                ret.append("<").append(w.hashCode()).append("> ").append(w).append("|");

                if (child instanceof Leaf)
                {
                    edges += "n_" + in.hashCode() + ":" + w.hashCode() + " -> " +
                            "c_" + ((Leaf)child).component.hashCode() + ":c;\n";
                }
                else
                {
                    edges += "n_" + in.hashCode() + ":" + w.hashCode() + " -> " +
                            "n_" + ((InnerNode)child).hashCode() + ";\n";

                    nodes.offer(child);
                }
            }

            ret.append("}}\"];\n");
        }

        ret.append(
                "subgraph cluster_0 {\n" +
                "    label =\"Observation Table\";\n" +
                "    rank = same;\n\n");

        // components
        for (Component c : components.values())
        {

            ret.append("c_").append(c.hashCode()).append(" [label=\"{{<c> [").append(c.getAccessSequence()).append("]");

            // aapend line for as
            ret.append("|").append(c.getAccessSequence());

            Collection<Word> words = c.getAllWords();
            words.remove(c.getAccessSequence());

            for (Word w : words)
                ret.append("|").append(w);
                
            ret.append("}");
            
            for (int i = 0; i<c.getFingerprint().size();i++)
            {
                ret.append("|{").append(c.getFingerprint().getElementById(i).getSuffix());

                Word trace = c.getFingerprint().getElementById(i).getTrace();
                if (trace == null)
                    trace = new WordImpl();

                // append comlumn for as
                ret.append("|").append(trace);

                for (Word w : words)
                {
                    ret.append("|");
                    if (!c.getMissingIDs(w).contains(i))
                        ret.append(trace);
                }

                ret.append("}");
            }            
            ret.append("}\"];\n");                
        }

        ret.append("}\n");
        ret.append(edges);
        ret.append("}\n");
        return ret.toString();
    }


    void setHypothesisChangeListener(HypothesisChangeListener hcl) {
        this.changeListener = hcl;
    }

}
