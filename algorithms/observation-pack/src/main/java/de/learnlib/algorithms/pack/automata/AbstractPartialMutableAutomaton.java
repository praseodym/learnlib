package de.learnlib.algorithms.pack.automata;

import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.logging.LearnLogger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.commons.util.mappings.MapMapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Abstract basis for implementation of partial mutable automata used
 * by Observation Packs.
 * 
 * @author falk
 * 
 * @param <S> state class
 * @param <I> input class
 * @param <T> transition class
 * @param <SP> state predicate
 * @param <TP> transition predicate
 */
abstract class AbstractPartialMutableAutomaton<S extends AbstractState<I>, I, T, SP, TP> 
        implements AccessSequenceTransformer<I>, StateIDs<S>,
                UniversalDeterministicAutomaton<S, I, T, SP, TP>,
                DOTPlottableAutomaton<S, I, T> {
       
    private static final LearnLogger logger = 
            LearnLogger.getLogger(AbstractPartialMutableAutomaton.class);
                    
    private final List<S> ids = new ArrayList<>();
   
    private final Alphabet<I> inputs;
    
    private S initial;
                       
    protected AbstractPartialMutableAutomaton(Alphabet<I> inputs) {
        this.inputs = inputs;
    }
    
    protected abstract S createState(int id, Word<I> accessor);
    
    public final void addTransition(Word<I> trans, Word<I> dest) {
        S dst = trace(dest);        
        addTransition(trans, dst);
    }
    
    private void addTransition(Word<I> trans, S dst) {
        Word<I> as = trans.prefix(-1);
        I input = trans.lastSymbol();
        S src = trace(as);
        
        assert src != null;
        assert dst != null;
        
        logger.logEvent("Creating transition from [" + src.getId() + 
                "] to [" + dst.getId() + "] for [" + input + "]");
        
        src.setSuccessor(input, dst);
    }
    
    public final void addState(Word<I> accessor) {
        S state = createState(ids.size(), accessor);
        ids.add(state);

        logger.logEvent("Creating state no [" + state.getId() + 
                "] for [" + accessor + "]");
        
        if (accessor.length() < 1) {
            initial = state;
        } else {
            addTransition(accessor, state);
        }
    }       
    
    @Override
    public final Word<I> transformAccessSequence(Word<I> word) {
        S dest = trace(word);
        return dest.getAccessor();
    }

    @Override
    public final boolean isAccessSequence(Word<I> word) {
        return (word.equals(transformAccessSequence(word)));
    }      
    
    @Override
    public Alphabet<I> getInputAlphabet() {
        return inputs;
    }    
    
    protected final S trace(Iterable<I> word) {
        return trace(initial, word);
    }

    protected final S trace(S init, Iterable<I> word) {
        S cur = init;
        for (I i : word) {
            if (cur == null) {
                break;
            }
            cur = getSuccessor(cur, i);
        }
        return cur;
    }
    
    protected Word<I> concatenate(Iterable<I> prefix, Iterable<I> suffix) {
        Word<I> input = Word.epsilon();
        for (I i : prefix) {
            input = input.append(i);
        }
        for (I i : suffix) {
            input = input.append(i);
        }    
        return input;
    }
   
    
    protected Word<I> getAccessSequence(S state) {
        return state.getAccessor();
    }
    
    public final void setSuccessor(DFAState<I> state, I input, DFAState<I> to) {
        state.setSuccessor(input, to);
    }
    
    @Override
    public final Collection<T> getTransitions(S s, I i) {
        T trans = getTransition(s, i);
        return Collections.singletonList(trans);
    }

    @Override
    public final Set<S> getInitialStates() {
        return Collections.singleton(initial);
    }

    @Override
    public final Set<S> getSuccessors(S s, Iterable<I> itrbl) {
        Set<S> set = new HashSet<>();
        for (I i : itrbl) {
            set.addAll(getSuccessors(s, i));
        }
        return set;
    }

    @Override
    public final Set<S> getSuccessors(Collection<S> clctn, Iterable<I> itrbl) {
        Set<S> set = new HashSet<>();
        for (S s : clctn) {
            set.addAll(getSuccessors(s, itrbl));
        }
        return set;
    }

    @Override
    public final Collection<S> getStates() {
        return ids;
    }

    @Override
    public final int size() {
        return ids.size();
    }

    @Override
    public final Iterator<S> iterator() {
        return ids.iterator();
    }

    @Override
    public final S getInitialState() {
        return initial;
    }
   
    @Override
    public final Set<S> getSuccessors(S state, I input) {
        return (Set<S>) Collections.singleton(state.getSuccessor(input));
    }

    @Override
    public final S getSuccessor(S state, I input) {
        return (S) state.getSuccessor(input);
    }
    
    @Override
    public StateIDs<S> stateIDs() {
        return this;
    }
    
    @Override
    public int getStateId(S state) {
        return state.getId();
    }

    @Override
    public S getState(int id) {
        return this.ids.get(id);
    }
       
    @Override
    public <V> MutableMapping<S, V> createStaticStateMapping() {
        return new MapMapping<>(new HashMap<S,V>());
    }

    @Override
    public <V> MutableMapping<S, V> createDynamicStateMapping() {
        return new MapMapping<>(new HashMap<S,V>());
    }    

    @Override
    public Set<S> getStates(Iterable<I> input) {
        return Collections.singleton(getState(input));
    }

    @Override
    public S getSuccessor(S state, Iterable<I> input) {
        return trace(state, input);
    }
    
    @Override
    public S getState(Iterable<I> input) {
        return trace(input); 
    }    
    
    @Override
    public DeterministicTransitionSystem<? extends Set<S>, I, ? extends Collection<T>> powersetView() {
        throw new UnsupportedOperationException("Not supported."); 
    }    
}
