package de.learnlib.algorithms.pack.ds;

import de.learnlib.api.Query;
import net.automatalib.words.Word;

/**
 *
 * @author falkhowar
 */
abstract class Row<I, O> extends Word<I> {

    private Component<I, O> component;
       
    /**
     * return the word from the union of U and UA labeling
     * this row.
     * 
     * @return row label of this row 
     */
    abstract Word<I> getRowLabel();

    /**
     * add HTML for this row to the passed string builder.
     * 
     * @param ret string builder 
     */
    abstract void toHtml(StringBuilder ret);
    
    /**
     * 
     * @return 
     */
    abstract O getOutput();
    
    
    //abstract private void setColumnOuput(final int column, final O output, final ObservationPack<?, I ,O> pack);

}
