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
package it.iit.genomics.cru.structures.model;

import java.util.Objects;

/**
 *
 * @author aceol
 */
/* We consider two chains as interacting if they have at least five residue-residue con- tacts, including
     (i) covalent interactions (disulfide bridges),
          defined as two sulfur atoms of a pair of cysteines at a distance ≤2.56 Å (two times the covalent radius of sulfur plus 0.5 Å); 
     (ii) hydrogen bonds, defined as all atom pairs N-O and O-N at a distance ≤3.5 Å; 
     (iii) salt bridges, defined as all atom pairs N-O and O-N at a distance ≤5.5 Å; and 
     (iv) van der Waals interactions, defined as all pairs of carbon atoms at a distance ≤5.0 Å. 
          Any pair of atoms at a distance less than the sum of the two covalent radii plus 0.5 Å that are not forming a disulfide bridge are considered clashes, 
          and are not counted.
 */
public class Contact {
    private final int seqNumA;

    /**
     *
     * @return
     */
    public int getSeqNumA() {
        return seqNumA;
    }

    /**
     *
     * @return
     */
    public int getSeqNumB() {
        return seqNumB;
    }
    private final int seqNumB;

    /**
     *
     */
    public enum ContactType {

        /**
         *
         */
        DISULFITE_BRIDGE,

        /**
         *
         */
        HYDROGEN_BOND,

        /**
         *
         */
        SALT_BRIDGE,

        /**
         *
         */
        VAN_DER_WAALS,

        /**
         *
         */
        OTHER
    };

    /**
     *
     */
    protected String chainA;

    /**
     *
     */
    protected String chainB;

    /**
     *
     */
    protected ContactType type;

    /**
     *
     */
    protected double distance;

    /**
     *
     */
    protected String atomA;

    /**
     *
     */
    protected String atomB;

    /**
     *
     */
    protected String residueA;

    /**
     *
     */
    protected String residueB;

    /**
     *
     * @param chainA
     * @param chainB
     * @param type
     * @param distance
     * @param atomA
     * @param atomB
     * @param residueA
     * @param residueB
     * @param seqNumA
     * @param seqNumB
     */
    public Contact(String chainA, String chainB, ContactType type,
            double distance, String atomA, String atomB, String residueA,
            String residueB, int seqNumA, int seqNumB) {
        super();
        this.chainA = chainA;
        this.chainB = chainB;
        this.type = type;
        this.distance = distance;
        this.atomA = atomA;
        this.atomB = atomB;
        this.residueA = residueA;
        this.residueB = residueB;
        this.seqNumA = seqNumA;
        this.seqNumB = seqNumB;
    }

    /**
     *
     * @return
     */
    public String getChainA() {
        return chainA;
    }

    /**
     *
     * @return
     */
    public String getChainB() {
        return chainB;
    }

    /**
     *
     * @return
     */
    public ContactType getType() {
        return type;
    }

    /**
     *
     * @return
     */
    public double getDistance() {
        return distance;
    }

    /**
     *
     * @return
     */
    public String getAtomA() {
        return atomA;
    }

    /**
     *
     * @return
     */
    public String getAtomB() {
        return atomB;
    }

    @Override
    public String toString() {
        return chainA + " " + chainB + " " + Math.round(distance * 100) / 100 + " " + type + " " + atomA + " " + atomB + " " + residueA + " " + residueB;
    }

    /**
     *
     * @return
     */
    public String getResidueA() {
        return residueA;
    }

    /**
     *
     * @return
     */
    public String getResidueB() {
        return residueB;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.chainA);
        hash = 61 * hash + Objects.hashCode(this.chainB);
        hash = 61 * hash + Objects.hashCode(this.type);
        hash = 61 * hash + Objects.hashCode(this.atomA);
        hash = 61 * hash + Objects.hashCode(this.atomB);
        hash = 61 * hash + Objects.hashCode(this.residueA);
        hash = 61 * hash + Objects.hashCode(this.residueB);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Contact other = (Contact) obj;
        if (!Objects.equals(this.chainA, other.chainA)) {
            return false;
        }
        if (!Objects.equals(this.chainB, other.chainB)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.atomA, other.atomA)) {
            return false;
        }
        if (!Objects.equals(this.atomB, other.atomB)) {
            return false;
        }
        if (!Objects.equals(this.residueA, other.residueA)) {
            return false;
        }
        if (!Objects.equals(this.residueB, other.residueB)) {
            return false;
        }
        return true;
    }

    
    
    
}
