/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.algorithms.pack.ds;

import net.automatalib.words.Word;

/**
 *
 * @author falkhowar
 */
public class ColumnZeroQuery<I, O> extends ColumnQuery<I, O> {

    ColumnZeroQuery(final OrdinaryRow<I, O> _row) {
	super(_row, Word.fromLetter(_row.getAction()), 0, null);
    }
    
    @Override
    public Word<I> getPrefix() {
	return ((OrdinaryRow<I, O>)row).getAccessSequence();
    }

}
