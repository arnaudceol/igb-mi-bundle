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
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author aceol
 */
public class Interaction implements Comparable {

    /**
     *
     */
    public final static String INTERACTION_TYPE_PDB = "association (PDB)";

    /**
     *
     */
    public final static String INTERACTION_TYPE_I3D = "direct interaction (Interactome3D)";
    
    private static final Logger logger = LoggerFactory.getLogger(Interaction.class);
    
//    public enum InteractionTypeClassification {
//        PHYSICAL, ASSOCIATION, ENZYMATIC, OTHER, UNSPECIFIED
//    }

    /**
     *
     */
        protected String ac1;

    /**
     *
     */
    protected String ac2;

    /**
     *
     */
    protected HashSet<String> interactionTypes;

    /**
     *
     */
    protected HashSet<String> bibRefs;

    /**
     *
     */
    protected HashSet<String> methods;

    /**
     *
     */
    protected boolean isPhysical = false;

    /**
     *
     */
    protected boolean isUnspecified = false;

    /**
     *
     */
    protected boolean isAssociation = false;

    /**
     *
     */
    protected boolean isEnzymatic = false;

    /**
     *
     */
    protected boolean isOther = false;

    /**
     *
     * @param ac1
     * @param ac2
     */
    public Interaction(String ac1, String ac2) {
        this.ac1 = ac1.compareTo(ac2) <= 0 ? ac1 : ac2;
        this.ac2 = ac1.compareTo(ac2) > 0 ? ac1 : ac2;
        interactionTypes = new HashSet<>();
        bibRefs = new HashSet<>();
        methods = new HashSet<>();
    }

    /**
     *
     * @param interactionType
     */
    public void addType(String interactionType) {
        interactionTypes.add(interactionType);

        if (interactionType.toLowerCase().contains("reaction")) {
            this.isEnzymatic = true;
        } else if (interactionType.toLowerCase().contains("genetic") || interactionType.toLowerCase().contains("localization")) {
            this.isOther = true;
        } else if (interactionType.toLowerCase().contains("direct interaction") || interactionType.toLowerCase().equals("physical association")) {
            this.isPhysical = true;
        } else if (interactionType.toLowerCase().equals("association")) {
            this.isAssociation = true;
        } else {
            this.isOther = true;
        }

    }
    
    /**
     *
     * @param interactionTypes
     */
    public void addTypes(Collection<String>  interactionTypes) {
         for (String type: interactionTypes) {
             addType(type);
         }
     }
    
    /**
     *
     * @return
     */
    public boolean isHomodimer() {
        return ac1.equals(ac2);
    }
     
    /**
     *
     * @param bibRef
     */
    public void addBibRef(String bibRef) {
        bibRefs.add(bibRef);
    }

    /**
     *
     * @param bibRefs
     */
    public void addBibRefs(Collection<String> bibRefs) {
        this.bibRefs.addAll(bibRefs);
    }
    
    /**
     *
     * @param method
     */
    public void addMethod(String method) {
        methods.add(method);
    }
    
    /**
     *
     * @param methods
     */
    public void addMethods(Collection<String> methods) {
        this.methods.addAll(methods);
    }
    

    /**
     * Ignore unspecified terms
     * @return 
     */
    private int getNumSpecified(Collection<String> list) {
        int num = list.size();
        if (list.contains(PsicquicUtils.UNSPECIFIED_TERM)) {
            num--;
        }
        return num;        
    }

    
    /**
     * Simple score: - two publications and two methods = 2 - two publications
     * or two methods = 1 - other = 0
     *
     * @return
     */
    public int getScore() {
        int score = 0;

        
        if (getNumSpecified(getBibRefs()) > 0) {
            score++;
        }
        
        if (getNumSpecified(getBibRefs()) > 1) {
            score++;
        }

        if (getNumSpecified(getMethods()) > 1) {
            score++;
        }

        return score;
    }

    /**
     *
     * @return
     */
    public String getAc1() {
        return ac1;
    }

    /**
     *
     * @return
     */
    public String getAc2() {
        return ac2;
    }

    /**
     *
     * @return
     */
    public Collection<String> getInteractionTypes() {
        return interactionTypes;
    }

    /**
     *
     * @return
     */
    public Collection<String> getBibRefs() {
        return bibRefs;
    }

    /**
     *
     * @return
     */
    public Collection<String> getMethods() {
        return methods;
    }

    /**
     *
     * @return
     */
    public boolean isPhysical() {
        return isPhysical;
    }

    /**
     *
     * @return
     */
    public boolean isUnspecified() {
        return isUnspecified;
    }

    /**
     *
     * @return
     */
    public boolean isAssociation() {
        return isAssociation;
    }

    /**
     *
     * @return
     */
    public boolean isEnzymatic() {
        return isEnzymatic;
    }

    /**
     *
     * @return
     */
    public boolean isOther() {
        return isOther;
    }
    
    /**
     *
     * @return
     */
    public String getKey() {
        return ac1 + "#" + ac2;
    }

    @Override
    public int compareTo(Object o) {
                
                Interaction o2 = (Interaction) o;
                Interaction o1 = this;
                
                int score1 = o1.getScore();
                int score2 = o2.getScore();

                if (score1 != score2) {
                    return Integer.compare(score1, score2);
                }

                int evidenceScore1 = 0;
                if (o1.getInteractionTypes().contains(INTERACTION_TYPE_I3D) || o1.getInteractionTypes().contains(INTERACTION_TYPE_PDB)) {
                    evidenceScore1 = 5;
                } else if (o1.isPhysical()) {
                    evidenceScore1 = 4;
                } else if (o1.isAssociation()) {
                    evidenceScore1 = 3;
                } else if (o1.isEnzymatic()) {
                    evidenceScore1 = 2;
                } else if (o1.isOther()) {
                    evidenceScore1 = 1;
                } else if (o1.isUnspecified()) {
                    evidenceScore1 = 0;
                }

                int evidenceScore2 = 0;
                if (o2.getInteractionTypes().contains(INTERACTION_TYPE_I3D) || o2.getInteractionTypes().contains(INTERACTION_TYPE_PDB)) {
                    evidenceScore2 = 5;
                } else if (o2.isPhysical()) {
                    evidenceScore2 = 4;
                } else if (o2.isAssociation()) {
                    evidenceScore2 = 3;
                } else if (o2.isEnzymatic()) {
                    evidenceScore2 = 2;
                } else if (o2.isOther()) {
                    evidenceScore2 = 1;
                } else if (o2.isUnspecified()) {
                    evidenceScore2 = 0;
                }

                return Integer.compare(evidenceScore1, evidenceScore2);
           
        
     
    }
    
}
