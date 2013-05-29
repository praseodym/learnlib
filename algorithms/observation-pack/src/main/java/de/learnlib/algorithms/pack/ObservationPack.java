/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.algorithms.pack;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import java.util.Collection;

/**
 *
 * @author falk
 */
public class ObservationPack<I, O> implements MembershipOracle<I, O> {

    @Override
    public void processQueries(Collection<? extends Query<I, O>> queries) {
    }
  
    void addComponents(Collection<Component<I,O>> components) {
    }
    
}
