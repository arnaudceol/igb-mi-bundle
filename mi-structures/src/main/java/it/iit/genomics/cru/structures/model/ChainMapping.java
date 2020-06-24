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

/**
 *
 * @author Arnaud Ceol
 *
 * Mapping of the chain in a structure to the protein sequence.
 *
 */
public class ChainMapping {

    /**
     *
     */
    protected String structureID;

    /**
     *
     */
    protected String proteinAc;

    /**
     *
     */
    protected String chain;

    /**
     *
     */
    protected int start;

    /**
     *
     */
    protected int end;

    /**
     *
     */
    protected String sequence;

    /**
     *
     * @return
     */
    public String getSequence() {
        return sequence;
    }

    /**
     *
     * @return
     */
    public String getChain() {
        return chain;
    }

    /**
     *
     * @return
     */
    public int getStart() {
        return start;
    }

    /**
     *
     * @return
     */
    public int getEnd() {
        return end;
    }

    /**
     *
     * @param structureID
     * @param chain
     * @param start
     * @param end
     */
    public ChainMapping(String structureID, String chain, int start, int end) {
        super();
        this.structureID = structureID;
        this.chain = chain;
        this.start = start;
        this.end = end;
    }

    /**
     *
     * @param proteinSequence
     */
    public void setSequence(String proteinSequence) {
        // pdb start at 1 and include the end
        try {
            this.sequence = proteinSequence.substring(start - 1, end - 1);
        } catch (StringIndexOutOfBoundsException e) {
            int s = start - 1;
            int en = end - 1;
            this.sequence = "";
        }
    }

    /**
     *
     * @return
     */
    public String getStructureID() {
        return structureID;
    }

    /**
     *
     * @return
     */
    public String getProteinAc() {
        return proteinAc;
    }

    /**
     *
     * @param proteinAc
     */
    public void setProteinAc(String proteinAc) {
        this.proteinAc = proteinAc;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChainMapping other = (ChainMapping) obj;
        if (chain == null) {
            if (other.chain != null) {
                return false;
            }
        } else if (!chain.equals(other.chain)) {
            return false;
        }
        if (end != other.end) {
            return false;
        }
        if (start != other.start) {
            return false;
        }
        if (structureID == null) {
            if (other.structureID != null) {
                return false;
            }
        } else if (!structureID.equals(other.structureID)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((chain == null) ? 0 : chain.hashCode());
        result = prime * result + end;
        result = prime * result + start;
        result = prime * result
                + ((structureID == null) ? 0 : structureID.hashCode());
        return result;
    }

}
