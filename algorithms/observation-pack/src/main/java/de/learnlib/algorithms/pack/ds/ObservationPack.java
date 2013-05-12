/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.algorithms.pack.ds;

import de.learnlib.algorithms.pack.SelectiveExploration;
import de.learnlib.algorithms.pack.ds.ClassificationTree.Leaf;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.logging.LearnLogger;
import de.learnlib.oracles.DefaultQuery;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import net.automatalib.words.Word;

/**
 *
 * @author falkhowar
 */
public class ObservationPack<A, I, O> implements LearningAlgorithm<A, I, O>, MembershipOracle<I, O> {

    private static LearnLogger logger = LearnLogger.getLogger(ObservationPack.class.getName());

    boolean useEpsilon() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static class SuffixCheck<I, O> {
        private final PrincipalRow<I, O> row;
        private final int suffix;
        SuffixCheck(PrincipalRow<I, O> _row, int _suffix) {
            this.row = _row;
            this.suffix = _suffix;
        }
    }
        
    private final MembershipOracle oracle;

    private Collection<? extends I> inputs;

    private ClassificationTree<I, O> ctree;

    private final boolean useEpsilon;
    
    private final boolean useSuffixesGlobally;
    
    private final boolean checkSemanticSuffixClosedness;
    
    private final boolean doSelectiveExploration;
    
    private LinkedHashSet<Word<I>> suffixes = new LinkedHashSet<>();

    private final Queue<SuffixCheck> openChecks = new ArrayDeque<>();
    
    private SelectiveExploration selector = null;

    
    private Query<I, O> counterexample = null;
    
    private List<OrdinaryRow<I, O>> newRows = new ArrayList<>();

    public ObservationPack(MembershipOracle oracle, Collection<? extends I> inputs) {
        this.oracle = oracle;
        this.inputs = inputs;
        this.ctree = new ClassificationTree();
    }

    @Override
    public void startLearning() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, O> ceQuery) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public A getHypothesisModel() {
	throw new UnsupportedOperationException("Not supported yet.");
    }
        
    boolean isSelectiveExploration() {
	return doSelectiveExploration;
    }
    
//    SelectiveExploration<I> getSelectiveExploration() {
//	return null;
//    }
    
    List<I> getInputs() {
	return null;
    }
    
    void addWord(Word<I> prefix, I action) {
//        for (I a : pack.getInputs()) {   
//            
//            
//            OrdinaryRow<I, O> continuation = new OrdinaryRow<>(this.accessSequence, a);
//            if (!this.pack.hasSelectiveExploration()
//                    || this.pack.getSelectiveExploration().explore(continuation)) {
//                pack.addWord(continuation);
//            }
//	}        
    }
    

    @Override
    public void processQueries(Collection<? extends Query<I, O>> queries) {
        this.oracle.processQueries(queries);
    }


    
//
//
//        Symbol out = WordUtil.lastSymbol(membershipQuery(new WordImpl()));
//
//        Component c = new Component( new WordImpl(),
//                out,
//                new LinkedHashMap<Word, Word>());
//
//        c.setPack(this);
//
//        this.ctree = new ClassificationTree(c, this, fsa, changeListener);
//        this.ctree.getHypothesis().getStart().setOutput(out);
//
//        if (this.dInit == null)
//        {
//            this.dInit = new ArrayList<Word>();
//            for (Symbol s : this.inputs.getSymbolList())
//                this.dInit.add(WordUtil.toWord(s));
//        }
//
//        if (batchMode)
//        {
//            ctree.batchAddWords(newWords);
//            newWords.clear();
//        }
//
//        String dset = "";
//        for (Word w : this.dInit)
//        {
//            c.addSuffix(w);
//            dset += "[" + w + "]\n";
//        }
//
//        this.globalSuffixes = new LinkedHashSet<Word>();
//        for (Word w : this.dInit)
//            this.globalSuffixes.add(w);
//
//        if (this.checkSemanticSuffixClosedness)
//            this.openChecks = new LinkedList<SuffixTriple>();
//        
//        this.initialized = true;
//
//        logger.logMultiline("Packs initialized","" +
//                "Use Global Suffix Set: " + useSuffixesGlobally + "\n" +
//                "Use Full Traces Opt.:" + fullTraces + "\n" +
//                "Batch-mode: " + batchMode + "\n" +
//                "\n Initial D:\n" + dset +
//                "", LogLevel.DEBUG);
//    }

//    /**
//     * add a new prefix to the observations
//     * adding arbitrary words to the pack
//     * may break the algorithm:
//     * thus it will be checked that prefix of
//     * w is access sequence
//     *
//     * @param w
//     * @throws LearningException
//     */
//    public void addPrefix(Word w) throws LearningException
//    {
//        logger.log(LogLevel.DEBUG,
//                "Adding prefix [" + w + "] to the pack.");
//
//        // test if prefix is access sequence already
//        Word prefix = WordUtil.prefix(w, w.size()-1);
//        if (!ctree.getAccessors().containsKey(prefix)) {
//            throw new PreconditionException(
//                    "Failed: prefix [" + prefix + "] of"
//                    + "[" + w +"] has to be an access sequence.");
//        }
//
//        addWord(w);
//    }

//    void addWord(Word<I> w) {
//        this.newWords.add(w);
//    }

    void addComponent(Component<I, O> c) {
        if (this.useSuffixesGlobally) {
            // FIXME: determine difference in suffixes?
            c.addSuffixes(this.suffixes);
        }
        
        c.start();
    }
    
    void splitComponent(Component<I, O> oldC, Collection<Component<I, O>> newC) {
        this.ctree.splitLeaf(oldC, newC);
        for (Component<I, O> c : newC) {
            addComponent(c);
        }
    }


    private void learn()
    {
        boolean closed = true;
        ArrayList<Component<I, O>> unclosed = new ArrayList<>();
        do { // semantic suffix closedness loop            
            do { // counterexample loop            
                logger.logPhase("Closing Table");
                do { // closing table loop
                    closed = true;
                    unclosed.clear();
                    for (Leaf<I, O> l : ctree.getLeaves()) {
                        Component<I, O> c = l.getComponent();
                        if (!c.isClosed()) {
                            unclosed.add(c);
                            closed = false;
                        }
                    }
                    // perform membership queries
                    this.complete(unclosed);
                    // close components 
                    for (Component c : unclosed) {
                        c.close();
                    }
                    // sink new words to the pack
                    if (!newRows.isEmpty()) {
                        ctree.addRows(newRows);
                        newRows.clear();
                    }
                } 
                while (!closed);
                
            } 
            while(processCounterexample());
        } 
        while(!checkSemanticSuffixClosedness());
    }


    private void complete(Collection<Component<I, O>> unclosed) {        
        List<Query<I, O>> queries = new ArrayList<>();
        for (Component<I, O> c : unclosed) {
            c.getOpenQueries(queries);
        }
        this.oracle.processQueries(queries);
    }
    
    void addSuffixCheck(final PrincipalRow<I, O> row, final int column) {        
        if (checkSemanticSuffixClosedness) {
            this.openChecks.offer(new SuffixCheck(row, column));
        }
    }
        
    
    private boolean check(SuffixCheck c) {
//        // fast check
//        Word w = WordUtil.concat(t.c.getAccessSequence(),t.suffix);
//
//        // dfa special case
//        if (w.size() < 1) {
//            openChecks.poll();            
//            return true; 
//        }
//        
//        Symbol o = WordUtil.lastSymbol(getResult().getTraceOutput(w));
//                        
//        if ( t.out.equals(o) ) {
//            openChecks.poll();
//            return true;
//        }
//        
////        System.out.println(":::  split triple " + t.c.getAccessSequence()+
////                " . " + t.suffix + "  :::  " + t.out + " ! " + o);
//
//        // something is wrong ...
//        addCounterExample(w, membershipQuery(w));
        return false;
    }
    
    
    private boolean checkSemanticSuffixClosedness() {
         if (!checkSemanticSuffixClosedness) {
            return true;
         }

        logger.logPhase("Checking semantic suffix closedness");
        
        while (!openChecks.isEmpty()) {            
            if (!check(openChecks.poll())) {
                return false;
            }
        }        
        return true;        
    }

    private boolean processCounterexample() {
//        if (this.splitter == null)
//        {
//            this.splitter = new RivestStyleSplitterCreator();
//            this.splitter.setOracle(this.oracle);
//        }
//
//        this.splitter.setOracle(oracle);
//        
//        if (counterexample == null)
//            return false;
//
//        logger.logPhase("Analyzing Counterexample", LogLevel.INFO);
//
//        if (counteroutput.equals(
//                WordUtil.suffix(
//                    getResult().getTraceOutput(counterexample),
//                    counteroutput.size())))
//        {
//            logger.log(LogLevel.INFO, "[" + counterexample + "] is not a counterexample");
//
//            counterexample = null;
//            return false;
//        }
//
//        Collection<Word> suffixes = splitter.createSplitters(
//                counterexample, counteroutput, getResult());
//
//        Collection<State> states = splitter.applyToStates();
//
//        if (states.size() < 1 || suffixes.size() < 1)
//        {
//            counterexample = null;
//            return false;
//        }
//
//        if (!useSuffixesGlobally)
//        {
//            for (State s : states)
//                for (Word w : suffixes)
//                    ctree.getComponents().get(s).addSuffix(w);
//        }
//        // table algorithm?
//        else
//        {
//            for (Component c : ctree.getComponents().values())
//                for (Word w : suffixes)
//                    c.addSuffix(w);
//
//                for (Word w : suffixes)
//                {
//                    this.globalSuffixes.add(w);
//                    logger.log(LogLevel.DEBUG, "Adding [" + w + "] " +
//                            "to global suffix set");
//                }
//        }
//
        return true;
    }

//    public void addSuffixes(Collection<Word<I>> suffixes) {
//        for (Word<I>)
//        if (this.globalSuffixes.contains(suffix))
//            return;
//
//        this.globalSuffixes.add(suffix);
//        logger.log(LogLevel.DEBUG, "Adding [" + suffix + "] " +
//                            "to global suffix set");
//
//        for (Component c : ctree.getComponents().values())
//            c.addSuffix(suffix);
//    }
//    
//    public void addSuffixLocally(Word suffix, Word as)
//    {
//        State s = ctree.getHypothesis().getTraceState(as, as.size());
//        Component c = ctree.getComponents().get(s);
//        
//        logger.log(LogLevel.DEBUG, "Trying to add [" + suffix + "] " +
//                            "to suffix set of [" + c.getAccessSequence() + "]");
//                
//        c.addSuffix(suffix);
//    }



//    /**
//     * will add a letter to the input alphabet and
//     * will extend the set of long prefixes (one-letter extensions)
//     * accordingly
//     * 
//     * @param letter to be added
//     * @return new size of the input alphabet
//     */
//    @Override
//    public int addLetter(Symbol letter)
//    {
//        if (!initialized) 
//          throw new UnsupportedOperationException("Not initialized yet.");
//
//        // really a new symbol?
//        if (this.inputs.getIndexForSymbol(letter) >= 0)
//            return this.inputs.size();
//            
//        // should do the trick also in the hypothesis
//        // as we share alphabets
//        this.inputs.addSymbol(letter);
//
//        // extend long prefixes
//        Collection<Word> prefixes = new ArrayList<Word>(ctree.getAccessors().keySet());
//        for (Word s : prefixes)
//        {
//            try {
//                addWord(WordUtil.concat(s, letter));
//            } catch (LearningException ex) {
//                Logger.getLogger(ObservationPack.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return this.inputs.size();
//    }


    @Override
    public String toString() {
        return this.ctree.toString();
    }

    
}
