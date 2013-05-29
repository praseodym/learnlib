/* Copyright (C) 2013 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.algorithms.pack;

import de.learnlib.api.Query;
import de.learnlib.logging.LearnLogger;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 *
 * @author falkhowar
 */
class Component<I, O> {

    /**
     * buckets for sorting words by outputs. Adding is synchronized so that
     * multiple queries can run concurrently.
     * 
     * @param <I>
     * @param <O> 
     */
    public static class Buckets<I, O> {

        // sorting buckets
        private final Map<O, List<Word<I>>> buckets = new LinkedHashMap<>();
        
        synchronized void add(Word<I> prefix, O output) {
            List<Word<I>> list = buckets.get(output);
            if (list == null) {
                list = new ArrayList<>();
                buckets.put(output, list);
            }
            list.add(prefix);
        }
    }
    
    /**
     * query used when sorting words into buckets.
     * 
     * @param <I>
     * @param <O> 
     */
    public static class BucketQuery<I, O> extends Query<I, O> {

        private final Buckets<I, O> buckets;
        private final Word<I> prefix;        
        private final Word<I> suffix;

        public BucketQuery(Buckets<I, O> buckets, Word<I> prefix, Word<I> suffix) {
            this.buckets = buckets;
            this.prefix = prefix;
            this.suffix = suffix;
        }
        
        @Override
        public Word<I> getPrefix() {
            return this.prefix;
        }

        @Override
        public Word<I> getSuffix() {
            return this.suffix;
        }

        @Override
        public void answer(O output) {
            buckets.add(prefix, output);
        }
    
    
    }
    
    /**
     * logger
     */
    private static LearnLogger logger = LearnLogger.getLogger(Component.class.getName());
    
    /**
     * access sequence for this component
     */
    private Word<I> accessSequence;
    
    /**
     * row for access sequence
     */
    private PrincipalRow principal;
    
    /**
     * lists of open queries per suffix
     */
    private HashMap<Integer, Collection<Word<I>>> openWords = new HashMap<>();
    
    /**
     * outputs (is a linked hash map because it is used for iteration some times)
     */
    private LinkedHashMap<Word<I>, O> outputs = new LinkedHashMap<>();
    
    /**
     * unknown outputs for access sequence and suffixes
     */
    private Collection<Integer> asUnknown;
    
    /**
     * all queries done?
     */
    private boolean closed = false;
    
    /**
     * initialized?
     */
    private boolean started = false;

    /**
     * completed suffixes
     */
    private int complete = 0;
        
    /**
     * ref to pack
     */
    private ObservationPack<I, O> pack;

    Component(Word accessSequence, O o, LinkedHashMap<Word<I>, O> suffixTraceMap) {

        // create a hard copy of the original word
        WordBuilder<I> wb = new WordBuilder<>(accessSequence);
        this.accessSequence = wb.toWord();

        // initalize principle row and open queries
        this.principal = new PrincipalRow();
        this.asUnknown = PrincipalRow.initialize(this.principal, suffixTraceMap);

        // store initial output
        this.outputs.put(accessSequence, o);
    }

    /**
     * adds a word to the component
     *
     * @param w
     * @param matchingSuffixes
     */
    void addWord(Word<I> w, O o, Collection<Word<I>> matchingSuffixes) {
        if (outputs.containsKey(w)) {
            return;
        }

        for (Integer i : this.principal.getMissingIds(matchingSuffixes)) {
            Collection<Word<I>> list = openWords.get(i);
            if (list == null) {
                list = new ArrayList<>();
                openWords.put(i, list);
            }

            list.add(w);
            complete = java.lang.Math.min(complete, i);
        }
        outputs.put(w, o);
        closed = false;
    }
    
    void close() {
        
        // start component if not already done so
        if (!started) {
            start();
        }

        // collect queries for principal rows
        List<Query<I, O>> queries = new ArrayList<>();
        getPrincipalQueries(queries);
        
        // iterate over open suffixes
        while (complete < getPrincipalRow().size()) {

            Collection<Word<I>> words = openWords.get(complete);
            
            // skip id?
            if (words == null) {
                complete++;
                continue;
            }

            // sort words into buckets
            Word<I> suffix = principal.getSuffix(complete);
            Buckets<I, O> buckets = new Buckets<>();            
            for (Word<I> w : words) {
                queries.add(new BucketQuery<>(buckets, w, suffix));
            }
            pack.processQueries(queries);
            
            O refOut = principal.getOuput(complete);

            if (buckets.buckets.size() > 1 || !buckets.buckets.containsKey(refOut)) {
                // split component ...
                complete++;
                //pack.addComponent(splitComponent(complete - 1, w, trace));
                pack.addComponents(splitComponent(buckets, complete, refOut));
                return;
            }
            
//            ArrayList<Word> remove = new ArrayList<Word>();
//            for (Word w : new ArrayList<Word>(words)) {
//                // perform mq
//                Word trace = pack.membershipQuery(w, suffix);
//                remove.add(w);
//
//                // fine, proceed ...
//                if (trace.equals(testTrace)) {
//                    continue;
//                }
//
//                // split component ...
//                words.removeAll(remove);
//                complete++;
//                for (Component c : splitComponent(complete - 1, w, trace)) {
//                    pack.addComponent(c);
//                }
//
//                return;
//            }
            queries.clear();
            openWords.remove(complete);
            complete++;
        }
        
        // only principal queries to ask ...
        if (!queries.isEmpty()) {
            pack.processQueries(queries);
        }
        
        // component is closed for now 
        closed = true;
    }

    /**
     * 
     */
    void start() {
        if (started) {
            return;
        }

        // TODO: what about suffix triples?

//        for (Element e : principal.getPrint()) {
//            if (e.getTrace() != null) {
//                pack.addSuffixTriple(this, e.getSuffix(), e.getTrace());
//            }
//        }

        for (O s : pack.getInputs().getOList()) {
            if (!this.pack.hasSelectiveExploration()
                    || this.pack.getSelectiveExploration().
                    explore(accessSequence, s)) {
                Word continuation = null;
                if (cWordLevel > 0) {
                    continuation = new CWord(accessSequence, s);
                } else {
                    continuation = WordUtil.concat(accessSequence, s);
                }

                pack.addWord(continuation);
            }
        }

        started = true;
    }

    private Collection<Component> splitComponent(Buckets<I, O> buckets, int suffixId, O refOut) {
        Collection<Component> components = new ArrayList<>();
        for (Entry<O, List<Word<I>>> e : buckets.buckets.entrySet()) {
            // skip group that remains part of this component
            if (e.getKey().equals(refOut)) {
                continue;
            }
            
            // new access sequence
            Word<I> as = e.getValue().remove(0);
            
            
            Component<I, O> nc = new Component<>(as, e.getKey(), suffixTraceMap);            
            components.add(nc);
            
            // transfer other words in bucket
            for (Word<I> w : e.getValue()) {
                nc.addWord(w, this.getOutput(w), suff);
            }
        }
        return components;
    }
    
    
    private Collection<Component> splitComponent(int index, Word newAS, Word newTrace) throws LearningException {
        LinkedHashMap<Word, Component> other = new LinkedHashMap<Word, Component>();
        Word refTrace = getPrincipalRow().getElementById(index).getTrace();
        Word suffix = getPrincipalRow().getElementById(index).getSuffix();
        LinkedHashSet<Word> remove = new LinkedHashSet<Word>();

        logger.logEvent("Splitting Component:\n"
                + "    Suffix: [" + suffix + "]\n"
                + "    New access sequence: [" + newAS + "]\n"
                + "    New trace: [" + newTrace + "]\n"
                + "    Old accesss sequence: [" + accessSequence + "]\n"
                + "    Old trace: [" + refTrace + "]");

        LinkedList<Word> words = new LinkedList<Word>(outputs.keySet());
        words.remove(accessSequence);
        words.remove(newAS);
        words.push(newAS);

        for (Word w : words) {
//            if (w.equals(accessSequence))
//                continue;

            Word trace = null;
            if (w.equals(newAS)) {
                trace = newTrace;
            } else if (openWords.get(index).contains(w)) {
                trace = pack.membershipQuery(w, suffix);
                openWords.get(index).remove(w);
            } else {
                trace = refTrace;
            }

//            System.out.println("--" + w + " : " + trace);

            // no new trace -> skip!
            if (trace.equals(refTrace)) {
                continue;
            }

            if (other.containsKey(trace)) {
                other.get(trace).addWord(w, outputs.get(w),
                        getPrincipalRow().getSuffixesDone(getMissingIDs(w)));

                remove.add(w);
            } else {
                LinkedHashMap<Word, Word> suffixTraceMap = new LinkedHashMap<Word, Word>();
                for (int i = 0; i < getPrincipalRow().size(); i++) {
                    Word s = getPrincipalRow().getElementById(i).getSuffix();
                    Word t = null;

                    if (i == index) {
                        t = trace;
                    } else if (!openWords.containsKey(i) || !openWords.get(i).contains(w)) {
                        t = getPrincipalRow().getElementById(i).getTrace();
                    }

                    suffixTraceMap.put(s, t);
                }
//                System.out.println("New component: " + w);

                Component nc = new Component(w, outputs.get(w), suffixTraceMap);
                other.put(trace, nc);

                remove.add(w);
            }
        }

        for (Word r : remove) {
            removeWord(r);
            outputs.remove(r);
        }

        return other.values();
    }

    /**
     * complete principal row
     */
    private void getPrincipalQueries(Collection<Query<I, O>> queries) {
        
        for (Integer c : asUnknown) {        
            queries.add(this.principal.createQuery(c, this.accessSequence));        
        }
        
        // TODO: what about these suffix triples???
        // pack.addSuffixTriple(this, suffix, trace);
        
        asUnknown.clear();
    }

    /**
     * add a single suffix to the component
     * 
     * @param w
     */
    void addSuffix(Word<I> w) {
        if (getPrincipalRow().containsSuffix(w)) {
            return;
        }

        // add suffix to principal row
        int idx = getPrincipalRow().addSuffix(w, null);
        
        // mark cell as unknown
        asUnknown.add(idx);

        // add queries to worklist
        ArrayList<Word<I>> open = new ArrayList<>(outputs.keySet());
        open.remove(accessSequence);
        openWords.put(idx, open);

        // mark component as unclosed
        closed = false;
    }

    /**
     * @return the accessSequence
     */
    Word getAccessSequence() {
        return accessSequence;
    }

    /**
     * @return all prefixes of this component
     */
    Collection<Word<I>> getAllWords() {
        return new ArrayList<>(outputs.keySet());
    }

    /**
     * @return the closed
     */
    boolean isClosed() {
        return closed;
    }

    /**
     * @param pack the pack to set
     */
    void setPack(ObservationPack pack) {
        this.pack = pack;
    }

    /**
     * @return the principal
     */
    PrincipalRow getPrincipalRow() {
        return principal;
    }

    O getOutput(Word w) {
        return outputs.get(w);
    }

    Collection<Integer> getMissingIDs(Word w) {
        ArrayList<Integer> ret = new ArrayList<>();
        for (Entry<Integer, Collection<Word<I>>> e : openWords.entrySet()) {
            if (e.getValue().contains(w)) {
                ret.add(e.getKey());
            }
        }
        return ret;
    }

    private void removeWord(Word w) {
        for (Collection<Word<I>> c : openWords.values()) {
            c.remove(w);
        }
    }
    
    String toHtml() {
        StringBuilder ret = new StringBuilder();
        ret.append("<table border=\"0\" cellspacing=\"1\" cellpadding=\"1\" bgcolor=\"#000000\">");

        // header
        ret.append("<tr><td bgcolor=\"#ffffff\"></td>");
        for (int i = 0; i < getPrincipalRow().size(); i++) {
            ret.append("<td  bgcolor=\"#ffffff\"><b>").append(
                    getPrincipalRow().getElementById(i).getSuffix()).append("</b></td>");
        }

        ret.append("</tr>");

        Word notAsked = new WordImpl();
        notAsked.addO(new OImpl("?"));

        Word blocked = new WordImpl();
        blocked.addO(new OImpl("!"));

        // row for access sequence
        ret.append("<tr><td bgcolor=\"#afafaf\">").append(accessSequence).append("</td>");
        for (int i = 0; i < getPrincipalRow().size(); i++) {
            Word trace = notAsked;
            if (!asUnknown.contains(i)) {
                trace = getPrincipalRow().getElementById(i).getTrace();
            }

            ret.append("<td bgcolor=\"#afafaf\">").append(trace).append("</td>");
        }
        ret.append("</tr>");

        // rows for the rest
        Collection<Word> words = getAllWords();
        words.remove(accessSequence);
        for (Word w : words) {
            ret.append("<tr><td bgcolor=\"#ffffff\">").append(w).append("</td>");
            for (int i = 0; i < getPrincipalRow().size(); i++) {
                Word trace = notAsked;
                if (this.unclean == i
                        && (!openWords.containsKey(i) || !openWords.get(i).contains(w))) {
                    trace = blocked;
                } else if (!asUnknown.contains(i)
                        && (!openWords.containsKey(i) || !openWords.get(i).contains(w))) {
                    trace = getPrincipalRow().getElementById(i).getTrace();
                }

                ret.append("<td bgcolor=\"#ffffff\">").append(trace).append("</td>");
            }
            ret.append("</tr>");
        }

        // end ...
        ret.append("</table>");

        return ret.toString();
    }
}
