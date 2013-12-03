package de.learnlib.algorithms.pack;

import de.learnlib.algorithms.pack.automata.api.InputGenerator;
import de.learnlib.algorithms.pack.mealy.ObservationPackMealy;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import org.testng.annotations.Test;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.eqtests.basic.SimulatorEQOracle;
import de.learnlib.eqtests.basic.mealy.SymbolEQOracleWrapper;
import de.learnlib.examples.mealy.ExampleStack;
import de.learnlib.mealy.MealyUtil;
import de.learnlib.oracles.SimulatorOracle.MealySimulatorOracle;
import java.util.ArrayList;
import java.util.Collection;

@Test
public class ObservationPackMealyTest extends LearningTest {

    @Test
    public void testObservationPackMealy() {

        MealyMachine<?, ExampleStack.Input, ?, ExampleStack.Output> mealy = ExampleStack.getInstance();
        final Alphabet<ExampleStack.Input> alphabet = ExampleStack.getInputAlphabet();
        final MealyMembershipOracle<ExampleStack.Input, ExampleStack.Output> back
                = new MealySimulatorOracle<>(mealy);
        
        final MembershipOracle<ExampleStack.Input, ExampleStack.Output> oracle
                = MealyUtil.wrapWordOracle(back);

        EquivalenceOracle<? super MealyMachine<?, ExampleStack.Input, ?, ExampleStack.Output>, ExampleStack.Input, Word<ExampleStack.Output>> mealyEqOracle
                = new SimulatorEQOracle<>(mealy);

        EquivalenceOracle<? super MealyMachine<?, ExampleStack.Input, ?, ExampleStack.Output>, ExampleStack.Input, ExampleStack.Output> mealySymEqOracle
                = new SymbolEQOracleWrapper<>(mealyEqOracle);

        InputGenerator<ExampleStack.Input> gen = 
                new InputGenerator<ExampleStack.Input>() {

            @Override
            public Collection<Word<ExampleStack.Input>> generateExtensions(Word<ExampleStack.Input> prefix) {
                Collection<Word<ExampleStack.Input>> ret = new ArrayList<>();
                for (ExampleStack.Input i : alphabet) {
                    ret.add(prefix.append(i));
                }
                return ret;
            }

        };

        LearningAlgorithm<MealyMachine<?, ExampleStack.Input, ?, ExampleStack.Output>, ExampleStack.Input, ExampleStack.Output> learner
                = new ObservationPackMealy<>(oracle, alphabet, gen, null);

        testLearnModel(mealy, alphabet, learner, oracle, mealySymEqOracle);

    }
}
