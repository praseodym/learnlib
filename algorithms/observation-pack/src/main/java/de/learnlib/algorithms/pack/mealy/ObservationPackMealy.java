package de.learnlib.algorithms.pack.mealy;

import de.learnlib.algorithms.pack.automata.api.InputGenerator;
import de.learnlib.algorithms.pack.generic.ObservationPack;
import de.learnlib.api.MembershipOracle;
import de.learnlib.logging.LearnLogger;
import de.learnlib.oracles.DefaultQuery;
import java.util.Collections;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 *
 * @author falk
 * @param <I>
 * @param <O>
 */
public class ObservationPackMealy<I, O> extends 
        ObservationPack<MealyMachine<?, I, ?, O>, I, O> {

    private static final LearnLogger logger = 
            LearnLogger.getLogger(ObservationPackMealy.class);
      
    public ObservationPackMealy(MembershipOracle<I, O> oracle, Alphabet<I> inputs,
            InputGenerator<I> inputGenerator, O errorOut) {
        
        super(oracle, new MealyHypothesis<>(errorOut, inputs), inputGenerator);
    }
    
    @Override
    public void startLearning() {
        logger.logPhase("Start Learning");        
        this.tree.initWithLeafAsRoot();
        close();
    }
    
    @Override
    protected void exploreState(Word<I> accessSequence) {
        this.hypothesis.createState(accessSequence);
        for (Word<I> word : inputGenerator.generateExtensions(accessSequence)) {            
            DefaultQuery<I, O> query = new DefaultQuery<>(word);
            this.oracle.processQueries(Collections.singletonList(query));
            this.hypothesis.setOutput(word, query.getOutput());
            this.newPrefixes.add(word);
        }
    }
       
}
