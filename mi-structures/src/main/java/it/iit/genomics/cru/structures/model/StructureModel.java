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

import java.util.ArrayList;

import it.iit.genomics.cru.structures.sources.StructureManager.StructureSourceType;

/**
 *
 * @author Arnaud Ceol
 */
public abstract class StructureModel {

    /**
     *
     */
    protected StructureSourceType sourceType = null;

    /**
     *
     */
    protected String structureID;

    /**
     *
     * @return
     */
    public StructureSourceType getSourceType() {
        return sourceType;
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
     * @param proteinAc
     * @return
     */
    public abstract ArrayList<ChainMapping> getChains(String proteinAc);
}
