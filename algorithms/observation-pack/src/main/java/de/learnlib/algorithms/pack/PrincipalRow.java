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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.automatalib.words.Word;

/**
 *
 *
 * @author falkhowar
 */
class PrincipalRow<I, O> {

    
    public static class PrincipalQuery<I, O> extends Query<I, O> {

        private final Cell<I, O> cell;
        
        private final Word<I> prefix;

        private PrincipalQuery(Cell<I, O> cell, Word<I> prefix) {
            this.cell = cell;
            this.prefix = prefix;
        }
        
        @Override
        public Word<I> getPrefix() {
            return this.prefix;
        }

        @Override
        public Word<I> getSuffix() {
            return this.cell.suffix;
        }

        @Override
        public void answer(O output) {
            this.cell.trace = output;
        }   
    }
    
    
    
    /*
     * cell of row 
     */
    private static class Cell<I, O> {

        private final Word<I> suffix;
        private O trace;

        Cell(Word<I> suffix, O trace) {
            this.suffix = suffix;
            this.trace = trace;
        }

        /**
         * @return the suffix
         */
        public Word<I> getSuffix() {
            return suffix;
        }

        /**
         * @return the trace
         */
        public O getTrace() {
            return trace;
        }
    }

    /**
     * maps suffixes to cell ids
     */
    private HashMap<Word<I>, Integer> ids;
    
    /**
     * list of cells
     */
    private ArrayList<Cell<I, O>> cells;

    PrincipalRow() {
        this.ids = new HashMap<>();
        this.cells = new ArrayList<>();
    }

    /**
     * add a single suffix and trace to the row
     * 
     * @param suffix
     * @param trace
     * 
     * @return the index of the new cell
     */
    int addSuffix(Word<I> suffix, O trace) {
        Cell e = new Cell(suffix, trace);
        int idx = this.cells.size();
        this.ids.put(suffix, idx);
        this.cells.add(e);
        return idx;
    }

    PrincipalQuery<I, O> createQuery(int id, Word<I> prefix) {
        return new PrincipalQuery<>(this.cells.get(id), prefix);
    }
    
//    /**
//     * add a trace for suffix by id
//     * 
//     * @param id
//     * @param trace 
//     */
//    void addTraceById(int id, O trace) {
//        cells.get(id).trace = trace;
//    }

    boolean containsSuffix(Word<I> w) {
        return ids.containsKey(w);
    }

//    int getIdForSuffix(Word<I> w) {
//        return ids.get(w);
//    }
    
    

    Word<I> getSuffix(int id) {
        return cells.get(id).suffix;
    }
    
    O getOuput(int id) {
        return cells.get(id).trace;
    }
    

//
//    Cell getCellBySuffix(Word w) {
//        Integer i = ids.get(w);
//        if (i == null) {
//            return null;
//        }
//
//        return cells.get(i);
//    }


    /**
     * @return size of principal row 
     */
    int size() {
        return this.cells.size();
    }

    Collection<Integer> getMissingIds(Collection<Word<I>> done) {
        Collection<Integer> missing = new ArrayList<>();
        for (Entry<Word<I>, Integer> e : this.ids.entrySet()) {
            if (!done.remove(e.getKey())) {
                missing.add(e.getValue());
            }
        }        
        return missing;
    }

//    Collection<Word> getSuffixesDone(Collection<Integer> missingIds) {
//        Collection<Word> done = new ArrayList<>();
//
//        for (int i = 0; i < cells.size(); i++) {
//            // contained in missingIds -> do not copy
//            if (missingIds.contains(i)) {
//                continue;
//            }
//
//            done.add(cells.get(i).getSuffix());
//
//        }
//
//        return done;
//    }

//    /**
//     * @return the cells
//     */
//    ArrayList<Cell> getPrint() {
//        return cells;
//    }
    
    static <I, O> List<Integer> initialize(PrincipalRow<I, O> row, Map<Word<I>, O> suffixTraceMap) {
        ArrayList<Integer> missing = new ArrayList<>();
        for (Word<I> w : suffixTraceMap.keySet()) {
            int idx = row.addSuffix(w, suffixTraceMap.get(w));
            if (suffixTraceMap.get(w) == null) {
                missing.add(idx);
            }
        }
        return missing;
    }
}
