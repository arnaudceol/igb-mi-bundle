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

import it.iit.genomics.cru.structures.sources.StructureManager.StructureSourceType;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Arnaud Ceol
 */
public class ProteinStructure extends StructureModel {

    /**
     *
     */
    protected ArrayList<ChainMapping> chains = new ArrayList<>();

    /**
     *
     */
    protected String proteinAc;

    /**
     *
     * @param sourceType
     * @param structureID
     * @param proteinAc
     */
    public ProteinStructure(StructureSourceType sourceType, String structureID, String proteinAc) {
        this.sourceType = sourceType;
        this.structureID = structureID;
        this.proteinAc = proteinAc;
    }

    /**
     *
     * @param sourceType
     * @param structureID
     * @param proteinAc
     * @param chain
     * @param start
     * @param end
     */
    public ProteinStructure(StructureSourceType sourceType, String structureID, String proteinAc,
            String chain, int start, int end) {
        this.sourceType = sourceType;
        this.structureID = structureID;
        this.proteinAc = proteinAc;
        chains.add(new ChainMapping(structureID, chain, start, end));
    }

    /**
     *
     * @param sourceType
     * @param structureID
     * @param proteinAc
     * @param chain
     */
    public ProteinStructure(StructureSourceType sourceType, String structureID, String proteinAc,
            ChainMapping chain) {
        this.sourceType = sourceType;
        this.structureID = structureID;
        this.proteinAc = proteinAc;
        chains.add(chain);
    }

    /**
     *
     * @param sourceType
     * @param structureID
     * @param proteinAc
     * @param chains
     */
    public ProteinStructure(StructureSourceType sourceType, String structureID, String proteinAc,
            Collection<ChainMapping> chains) {
        this.sourceType = sourceType;
        this.structureID = structureID;
        this.proteinAc = proteinAc;

        this.chains.addAll(chains);
    }

    /**
     *
     * @param proteinAc
     * @return
     */
    @Override
    public ArrayList<ChainMapping> getChains(String proteinAc) {
        if (proteinAc.equals(this.proteinAc)) {
            return chains;
        }
        return null;
    }

    /**
     *
     * @param chain
     */
    public void addChain(ChainMapping chain) {
        chains.add(chain);
    }
}
