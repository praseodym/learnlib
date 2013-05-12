/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.algorithms.pack.ds;

import de.learnlib.api.Query;
import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import net.automatalib.words.Word;

/**
 *
 * @author falkhowar
 */
class OrdinaryRow<I, O> extends Row<I, O> {

    
    
    
    
    
    
    
    static class RowKey<O> {
	private final int[] ids;
	private final O[] outputs;
	public RowKey(SortedMap<Integer, O> in) {
	    this.ids = new int[in.size()];
	    //@SuppressWarnings({"unchecked"})
	    this.outputs = (O[]) new Object[in.size()];
	    int i = 0;
	    for (Map.Entry<Integer, O> e : in.entrySet()) {
		this.ids[i] = e.getKey();
		this.outputs[i] = e.getValue();
	    }
	}

	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
		return false;
	    }
	    if (getClass() != obj.getClass()) {
		return false;
	    }
	    final RowKey<O> other = (RowKey<O>) obj;
	    if (!Arrays.equals(this.ids, other.ids)) {
		return false;
	    }
	    if (!Arrays.deepEquals(this.outputs, other.outputs)) {
		return false;
	    }
	    return true;
	}
    
	@Override
	public int hashCode() {
	    int hash = 7;
	    hash = 89 * hash + Arrays.hashCode(this.ids);
	    hash = 89 * hash + Arrays.deepHashCode(this.outputs);
	    return hash;
	}
	
    }
    
    private Word<I> prefix;
    
    private I action;

    private SortedMap<Integer, O> differences; 

    OrdinaryRow(Word<I> prefix, I action) {
        this.prefix = prefix;
        this.action = action;
    }
        
    @Override
    public I getSymbol(int i) {
	if (i == prefix.length()) {
	    return action;
	}
	return prefix.getSymbol(i);
    }

    @Override
    public int length() {
	return prefix.length() + 1;
    }

    @Override
    Word<I> getPrefix() {
	return this;
    }
    
    Word<I> getAccessSequence() {
        return this.prefix;
    }
    
    I getAction() {
        return this.action;
    }

    @Override
    synchronized void setColumnOuput(int column, O output, ObservationPack<?, I, O> pack) {
	this.differences.put(column, output);
    }
    
    void prepareForQueries(PrincipalRow<I, O> principal, Collection<Query<I, O>> queries) {
	if (this.differences != null) {
	    // newly added row
	    for (Integer i : this.differences.keySet()) {
		queries.add(new ColumnQuery(this, principal.getColumnSuffix(i), i, null));
	    }	    
	} else {
	    for (int i = principal.completed(); i < principal.columns(); i++) {
		queries.add(new ColumnQuery(this, principal.getColumnSuffix(i), i, null));
	    }
	}
	this.differences = new TreeMap<>();
    }
    
    RowKey updateAfterQueries(PrincipalRow<I, O> principal) {
	List<Integer> remove = new ArrayList<>();
	for (Entry<Integer, O> e : this.differences.entrySet()) {
	    if (principal.getColumnOutput(e.getKey()).equals(e.getValue())) {
		remove.add(e.getKey());
	    }
	}
	for (Integer i : remove) {
	    this.differences.remove(i);
	}
	if (this.differences.isEmpty()) {
	    this.differences = null;
	    return null;
	}
	return new RowKey(this.differences);
    }

    void setDifferences(SortedMap<Integer, O> _differences) {
        this.differences = _differences;
    }
    
    void toHtml(StringBuilder ret, PrincipalRow<I, O> principal) {
        ret.append("<tr><td>").append(this).append("</td>");
        ret.append("<td colspan=\"").append(principal.columns()).append("\"></td></tr>");        
    }    
}
