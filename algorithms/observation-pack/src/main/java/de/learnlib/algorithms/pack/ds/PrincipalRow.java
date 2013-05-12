/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.algorithms.pack.ds;

import de.learnlib.algorithms.pack.ds.OrdinaryRow.RowKey;
import de.learnlib.api.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import net.automatalib.words.Word;

/**
 *
 * @author falkhowar
 */
class PrincipalRow<I, O> extends Row<I, O> {

    private final Component<I, O> component;
    
    private final ArrayList<O> outputs;
        
    private final Word<I> prefix;

    private int complete;

//    PrincipalRow(Word<I> _prefix, O _out) {
//	this.prefix = _prefix;
//	this.complete = 0;
//	this.suffixes = new ArrayList<>();
//	this.outputs = new ArrayList<>();
//    }

    PrincipalRow(Word<I> _prefix, LinkedHashMap<Word<I>, O> _soMap) {
	this.prefix = _prefix;
	this.complete = 0;
	this.suffixes = new ArrayList<>(_soMap.size());
	this.outputs = new ArrayList<>(_soMap.size());
        for (Entry<Word<I>, O> e : _soMap.entrySet()) {
            assert(e.getValue() != null);
            this.suffixes.add(e.getKey());                
            this.outputs.add(e.getValue());
        }
    }

    PrincipalRow(OrdinaryRow<I, O> row, PrincipalRow<I, O> orig, RowKey<O> mod) {
        //this.prefix = ;
        throw new IllegalStateException("not implemented yet");
    }
    
    @Override
    public I getSymbol(int i) {
	return prefix.getSymbol(i);
    }

    @Override
    public int length() {
	return prefix.length();
    }    
    
    @Override
    Word<I> getPrefix() {
	return this.prefix;
    }
    
    @Override
    void setColumnOuput(final int column, final O output, ObservationPack<?, I, O> pack) {
	this.outputs.set(column, output);
        pack.addSuffixCheck(this, column);
    }

    Word<I> getColumnSuffix(final int column) {
	return this.suffixes.get(column);
    }

    O getColumnOutput(final int column) {
	return this.outputs.get(column);
    } 
    
    void prepareForQueries(final Collection<Query<I, O>> queries, ObservationPack<?, I, O> pack) {
	for (int i = complete; i < suffixes.size(); i++) {
            if (this.outputs.get(i) == null) {
                queries.add(new ColumnQuery(this, this.suffixes.get(i), i, pack));
            }
	}
    }

    void updateAfterQueries() {
	this.complete = this.suffixes.size();
    }
    
    int completed() {
	return this.complete;
    }
    
    int columns() {
	return this.suffixes.size();
    }
    
    SortedMap<Integer, O> getSuffixIds(Set<Word<I>> suffixes) {
        SortedMap<Integer, O> ret = new TreeMap<>();
        for (int i = 0; i < this.suffixes.size(); i++) {
            if (suffixes.remove(this.suffixes.get(i))) {
                ret.put(i, this.outputs.get(i));
            }
        }
        return ret;
    }
    
    /**
     * adds suffixes to the row.
     * it is assumed that only suffixes are added that
     * are not part of the row already. 
     * THUS, THIS IS NOT CHECKED HERE!
     * 
     * @param _suffixes the suffixes to add
     */
    void addSuffixes(Collection<Word<I>> _suffixes) {        
        for (Word<I> s : _suffixes) {
            this.suffixes.add(s);
            this.outputs.add(null);
        }
    }
    
    void toHtml(StringBuilder ret) {
        ret.append("<tr><td bgcolor=\"#ffffff\"></td>");
        // suffixes
        for (Word<I> s : this.suffixes) {
            ret.append("<td bgcolor=\"#ffffff\"><b>").append(s).append("</b></td>");
        }
        ret.append("</tr>");
        // outputs
        ret.append("<tr><td>").append(this.prefix).append("</td>");        
        for (O o : this.outputs) {
            ret.append("<td>").append(o).append("</td>");
        }
        ret.append("</tr>");        
    }
    
}
