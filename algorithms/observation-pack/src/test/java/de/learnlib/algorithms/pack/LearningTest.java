package de.learnlib.algorithms.pack;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.words.Alphabet;

import org.testng.Assert;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.util.graphs.dot.GraphDOT;

public class LearningTest {

    public static <I, O, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>> void testLearnModel(
            UniversalDeterministicAutomaton<?, I, ?, ?, ?> target,
            Alphabet<I> alphabet,
            LearningAlgorithm<M, I, O> learner,
            MembershipOracle<I, O> oracle,
            EquivalenceOracle<? super M, I, O> eqOracle) {

        int maxRounds = target.size();
        learner.startLearning();

        while (maxRounds-- > 0) {
            M hyp = learner.getHypothesisModel();
            
            try {
                GraphDOT.write( (DOTPlottableAutomaton<?, I, ?>) hyp, System.out);
            } catch (IOException ex) {
                Logger.getLogger(LearningTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            DefaultQuery<I, O> ce = eqOracle.findCounterExample(hyp, alphabet);
            if (ce == null) {
                break;
            }
            Assert.assertNotEquals(maxRounds, 0);
            learner.refineHypothesis(ce);
        }

        M hyp = learner.getHypothesisModel();
        Assert.assertEquals(hyp.size(), target.size());
    }

}
