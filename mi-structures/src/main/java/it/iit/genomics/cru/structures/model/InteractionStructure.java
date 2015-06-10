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
public class InteractionStructure extends StructureModel {

    /**
     *
     */
    protected ArrayList<ChainMapping> chainsA = new ArrayList<>();

    /**
     *
     */
    protected ArrayList<ChainMapping> chainsB = new ArrayList<>();

    /**
     *
     */
    protected String proteinA;

    /**
     *
     */
    protected String proteinB;

    /**
     *
     * @param sourceType
     * @param structureID
     * @param proteinA
     * @param proteinB
     */
    public InteractionStructure(StructureSourceType sourceType, String structureID, String proteinA, String proteinB) {
        this.sourceType = sourceType;
        this.structureID = structureID;
        this.proteinA = proteinA;
        this.proteinB = proteinB;
    }

    /**
     *
     * @param sourceType
     * @param structureID
     * @param proteinA
     * @param proteinB
     * @param chainA
     * @param startA
     * @param endA
     * @param chainB
     * @param startB
     * @param endB
     */
    public InteractionStructure(StructureSourceType sourceType, String structureID, String proteinA, String proteinB, String chainA, int startA, int endA, String chainB, int startB, int endB) {
        this.sourceType = sourceType;
        this.structureID = structureID;
        this.proteinA = proteinA;
        this.proteinB = proteinB;

        this.chainsA.add(new ChainMapping(structureID, chainA, startA, endA));
        this.chainsB.add(new ChainMapping(structureID, chainB, startB, endB));
    }

    /**
     *
     * @param sourceType
     * @param structureID
     * @param proteinA
     * @param proteinB
     * @param chainA
     * @param chainB
     */
    public InteractionStructure(StructureSourceType sourceType, String structureID, String proteinA, String proteinB, ChainMapping chainA, ChainMapping chainB) {
        this.sourceType = sourceType;
        this.structureID = structureID;
        this.proteinA = proteinA;
        this.proteinB = proteinB;

        this.chainsA.add(chainA);
        this.chainsB.add(chainB);
    }

    /**
     *
     * @param sourceType
     * @param structureID
     * @param proteinA
     * @param proteinB
     * @param chainsA
     * @param chainsB
     */
    public InteractionStructure(StructureSourceType sourceType, String structureID, String proteinA, String proteinB, Collection<ChainMapping> chainsA, Collection<ChainMapping> chainsB) {
        this.sourceType = sourceType;
        this.structureID = structureID;
        this.proteinA = proteinA;
        this.proteinB = proteinB;

        this.chainsA.addAll(chainsA);
        this.chainsB.addAll(chainsB);
    }

    /**
     *
     * @param proteinAc
     * @return
     */
    @Override
    public ArrayList<ChainMapping> getChains(String proteinAc) {
        if (proteinAc.equals(proteinA)) {
            return chainsA;
        } else if (proteinAc.equals(proteinB)) {
            return chainsB;
        }
        return null;
    }

    /**
     *
     * @param proteinAc
     * @param chain
     */
    public void addChain(String proteinAc, ChainMapping chain) {
        getChains(proteinAc).add(chain);
    }

}
