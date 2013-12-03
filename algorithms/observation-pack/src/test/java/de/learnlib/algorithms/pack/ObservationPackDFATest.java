package de.learnlib.algorithms.pack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;

import org.testng.annotations.Test;

import static de.learnlib.algorithms.pack.LearningTest.testLearnModel;
import de.learnlib.algorithms.pack.automata.api.InputGenerator;
import de.learnlib.algorithms.pack.dfa.ObservationPackDFA;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle.DFAMembershipOracle;
import de.learnlib.eqtests.basic.SimulatorEQOracle;
import de.learnlib.eqtests.basic.WMethodEQOracle;
import de.learnlib.eqtests.basic.WpMethodEQOracle;
import de.learnlib.examples.dfa.ExamplePaulAndMary;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SimulatorOracle.DFASimulatorOracle;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Test
public class ObservationPackDFATest extends LearningTest {

    @Test
    public void testObservationPackDFA() {

        DFA<?, Symbol> targetDFA = ExamplePaulAndMary.getInstance();
        final Alphabet<Symbol> alphabet = ExamplePaulAndMary.getInputAlphabet();
        final DFAMembershipOracle<Symbol> dfaOracle = new DFASimulatorOracle<>(targetDFA);

        List<EquivalenceOracle<? super DFA<?, Symbol>, Symbol, Boolean>> eqOracles
                = new ArrayList<>();

        eqOracles.add(new SimulatorEQOracle<>(targetDFA));
        eqOracles.add(new WMethodEQOracle<>(3, dfaOracle));
        eqOracles.add(new WpMethodEQOracle<>(3, dfaOracle));

        InputGenerator<Symbol> gen
                = new InputGenerator<Symbol>() {

                    @Override
                    public Collection<Word<Symbol>> generateExtensions(Word<Symbol> prefix) {
                        Collection<Word<Symbol>> ret = new ArrayList<>();
                        for (Symbol i : alphabet) {
                            DefaultQuery<Symbol, Boolean> q = new DefaultQuery<>(prefix, Word.fromLetter(i));
                            //dfaOracle.processQueries(Collections.singletonList(q));
                            ret.add(q.getInput());                          
                        }
                        return ret;
                    }

                };

        for (EquivalenceOracle<? super DFA<?, Symbol>, Symbol, Boolean> eqOracle : eqOracles) {
            LearningAlgorithm<DFA<?, Symbol>, Symbol, Boolean> learner
                    = new ObservationPackDFA<>(dfaOracle, alphabet, gen);

            testLearnModel(targetDFA, alphabet, learner, dfaOracle, eqOracle);
        }
    }
}
