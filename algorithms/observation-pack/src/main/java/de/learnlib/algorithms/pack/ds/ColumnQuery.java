/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.algorithms.pack.ds;

import de.learnlib.api.Query;
import net.automatalib.words.Word;

/**
 *
 * @author falkhowar
 */
public class ColumnQuery<I, O> extends Query<I, O> {

    final Row<I, O> row;
        
    private final Word<I> suffix;
    
    private final int column;
    
    private final ObservationPack pack;

    ColumnQuery(final Row<I, O> _row, final Word<I> _suffix, final int _column, ObservationPack<?, I, O> _pack) {
	this.row = _row;
	this.suffix = _suffix;
	this.column = _column;
        this.pack = _pack;
    }
    
    @Override
    public Word<I> getPrefix() {
	return row.getPrefix();
    }

    @Override
    public Word<I> getSuffix() {
	return this.suffix;
    }

    @Override
    public void answer(O output) {
	this.row.setColumnOuput(column, output, pack);
    }
}
