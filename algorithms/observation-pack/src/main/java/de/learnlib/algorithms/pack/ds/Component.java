package de.learnlib.algorithms.pack.ds;

import de.learnlib.algorithms.pack.ds.OrdinaryRow.RowKey;
import de.learnlib.api.Query;
import de.learnlib.logging.LearnLogger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import net.automatalib.words.Word;

/**
 * A component is a bunch of rows with the same behavior on 
 * a shared set of suffixes.
 *
 * @param<I> input symbol class of learning algorithm
 * @param<O> output class of learning algorithm
 * 
 * @author falkhowar
 */
class Component<I, O> {

    /**
     * logger
     */
    private final static LearnLogger logger = LearnLogger.getLogger(Component.class.getName());

    /**
     * suffixes for this component
     */
    private final ArrayList<Word<I>> suffixes;

    /**
     * outputs for this components
     */
    private final ArrayList<O> outputs;
        
    /** 
     * access sequence for this component
     */
    private final Row<I, O> principal;

    /**
     * number of suffixes completed
     */
    private int complete;

    /**
     * ordinary rows
     */
    private final List<Row<I, O>> rows;
    
    /**
     * flag indicating if the component 
     */
    private boolean closed;

    /**
     * link to observation pack
     */
    private final ObservationPack<?, I, O> pack;

//    
//    Component(Word<I> accessSequence, LinkedHashMap<Word<I>, O> suffixTraceMap, ObservationPack<?, I, O> _pack) {
//        this.principal = new PrincipalRow(principal, suffixTraceMap);
//        this.rows = new ArrayList<>();
//        this.pack = _pack;
//        this.closed = false;
//    }
//    
//    private Component(PrincipalRow<I,O> _principal, List<OrdinaryRow<I, O>> _rows, ObservationPack<?, I, O> _pack) {
//        this.principal = _principal;
//        this.rows = _rows;
//        this.pack = _pack;
//        this.closed = true;
//    }

    /**
     * adds a row to the component.
     *
     * @param row the row to be added
     * @param matchingSuffixes the suffixes that have been tested already
     */
    void addRow(OrdinaryRow<I, O> row, Set<Word<I>> matchingSuffixes) {
        SortedMap<Integer, O> completed = this.principal.getSuffixIds(matchingSuffixes);
        row.setDifferences(completed);
        closed = false;
    }


    void complete(List<Query<I, O>> queries) {
    
    }
    
    void getOpenQueries(Collection<Query<I, O>> queries) {
        this.principal.prepareForQueries(queries, this.pack);
        for (OrdinaryRow<I,O> row : this.rows) {
            row.prepareForQueries(this.principal, queries);
        }
    }


    void close() {

        this.closed = true;
        
        // analyze query results
        Map<RowKey<O>, List<OrdinaryRow<I, O>>> split = new HashMap<>();
        List<OrdinaryRow<I, O>> remove = new ArrayList<>();
        for (OrdinaryRow<I,O> row : this.rows) {
            RowKey<O> key = row.updateAfterQueries(this.principal);
            if (key == null) {
                continue;
            }
            
            List<OrdinaryRow<I, O>> list = split.get(key);
            if (list == null) {
                list = new ArrayList<>();
                split.put(key, list);
            }
            
            list.add(row);
            remove.add(row);
        }
        
        // no splitting necessary?
        if (remove.isEmpty()) {
            return;
        }

        // split component
        this.rows.removeAll(remove);
        
        for (Entry<RowKey<O>, List<OrdinaryRow<I, O>>> e : split.entrySet()) {        
            OrdinaryRow<I, O> ord0 = e.getValue().remove(0);
            PrincipalRow<I, O> newAs = new PrincipalRow(ord0, this.principal, e.getKey());            
            Component<I, O> c = new Component(newAs, e.getValue(), this.pack);
            pack.addComponent(c);
        }                
    }
   
    void start()
    {
        for (I a : pack.getInputs()) {   
            pack.addWord(this.principal.getPrefix(), a);
        }
    }

    /**
     *
     * @param suffixes 
     */
    void addSuffixes(Collection<Word<I>> suffixes)
    {
        this.principal.addSuffixes(suffixes);
        closed = false;
    }

//    /**
//     * @return the accessSequence
//     */
//    Word getAccessSequence() {
//        return accessSequence;
//    }

//    /**
//     * @return the allWords
//     */
//    Collection<Word> getAllWords()
//    {
//        Collection<Word> ret = new ArrayList<Word>(outputs.keySet());
//        return ret;
//    }

    /**
     * @return the closed
     */
    boolean isClosed() {
        return closed;
    }

//    /**
//     * @param pack the pack to set
//     */
//    void setPack(ObservationPack pack) {
//        this.pack = pack;
//    }

    /**
     * principal row of this component.
     * 
     * @return principal row
     */
    PrincipalRow<I, O> getPrincipalRow() {
        return this.principal;
    }
    
//
//    O getOutput(Word w)
//    {
//        return outputs.get(w);
//    }
//
//    Collection<Integer> getMissingIDs(Word w)
//    {
//        ArrayList<Integer> ret = new ArrayList<Integer>();
//        for (Entry<Integer,Collection<Word>> e : openWords.entrySet())
//            if (e.getValue().contains(w))
//                ret.add(e.getKey());
//
//        return ret;
//    }

    void toHtml(StringBuilder ret) {
        ret.append("<table border=\"0\" cellspacing=\"1\" cellpadding=\"1\" bgcolor=\"#000000\">");
        this.principal.toHtml(ret);
        for (OrdinaryRow<I,O> row : this.rows) {
            row.toHtml(ret, principal);
        }        
        ret.append("</table>");
    }


}
