/* 
 * Copyright 2015 Fondazione Istituto Italiano di Tecnologia.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.iit.genomics.cru.structures.bridges.psicquic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author aceol
 */
public class InteractionManager {

    /**
     *
     */
    protected HashMap<String, Interaction> interactions = new HashMap<>();

    /**
     *
     */
    protected HashSet<String> interactors = new HashSet<>();
    
    /**
     *
     * @param xref1
     * @param xref2
     * @return
     */
    public Interaction getOrCreateInteraction(String xref1, String xref2) {
        String key = xref1.compareTo(xref2) <= 0 ? xref1 + "#" + xref2 : xref2 + "#" + xref1;

        if (false == interactions.containsKey(key)) {
            interactions.put(key, new Interaction(xref1, xref2));
            interactors.add(xref1);
            interactors.add(xref2);
        }

        return interactions.get(key);
    }

    /**
     *
     * @param interaction
     */
    public void merge(Interaction interaction) {
        if (false == interactions.containsKey(interaction.getKey())) {
            interactions.put(interaction.getKey(), interaction);                
            interactors.add(interaction.getAc1());
            interactors.add(interaction.getAc2());
        } else {
            Interaction interactionToUpdate = interactions.get(interaction.getKey());
            interactionToUpdate.addBibRefs(interaction.getBibRefs());
            interactionToUpdate.addMethods(interaction.getMethods());
            interactionToUpdate.addTypes(interaction.getInteractionTypes());
        }
    }

    /**
     *
     * @return
     */
    public Collection<String> getInteractors() {
        return this.interactors;
    }
    
}
