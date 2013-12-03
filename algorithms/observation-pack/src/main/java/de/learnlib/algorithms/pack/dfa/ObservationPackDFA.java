package de.learnlib.algorithms.pack.dfa;

import de.learnlib.algorithms.pack.automata.api.InputGenerator;
import de.learnlib.algorithms.pack.generic.ObservationPack;
import de.learnlib.api.MembershipOracle.DFAMembershipOracle;
import de.learnlib.logging.LearnLogger;
import de.learnlib.oracles.DefaultQuery;
import java.util.Collections;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 *
 * @author falk
 * @param <I>
 */
public class ObservationPackDFA<I> extends ObservationPack<DFA<?, I>, I, Boolean> {
 
    private static final LearnLogger logger = 
            LearnLogger.getLogger(ObservationPackDFA.class);
      
    public ObservationPackDFA(DFAMembershipOracle<I> oracle, Alphabet<I> inputs,
            InputGenerator<I> inputGenerator) {
        
        super(oracle, new DFAHypothesis<>(inputs), inputGenerator);       
    }
    
    @Override
    public void startLearning() {
        logger.logPhase("Start Learning");        
        this.tree.initWithEmptySuffixAsRoot();
        Word<I> eps = Word.epsilon();
        this.newPrefixes.add(eps);
        close();
    }   
    
    @Override
    protected void exploreState(Word<I> accessSequence) {
        this.hypothesis.createState(accessSequence);
        // TODO: maybe we can use some kind of cache to save this query?
        DefaultQuery<I, Boolean> query = new DefaultQuery<>(accessSequence);
        this.oracle.processQueries(Collections.singletonList(query));
        this.hypothesis.setOutput(accessSequence, query.getOutput());        
        for (Word<I> word : inputGenerator.generateExtensions(accessSequence)) {            
            this.newPrefixes.add(word);
        }
    }    
}
