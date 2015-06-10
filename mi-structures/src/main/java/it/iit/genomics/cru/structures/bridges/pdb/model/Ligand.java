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
package it.iit.genomics.cru.structures.bridges.pdb.model;

/**
 *
 * @author Arnaud Ceol
 */
public class Ligand {

    /**
     *
     */
    public final static String LIGAND_TYPE_NON_POLYMER = "non-polymer";    
    
    String structureId;

    String chemicalId;

    String type;

    double molecularWeight;

    String chemicalName;

    String formula;

    String inChIKey;

    String inChI;

    String smiles;

    /**
     *
     * @return
     */
    public boolean isNonPolymer() {
        return LIGAND_TYPE_NON_POLYMER.equals(type);
    }
    
    /**
     *
     * @return
     */
    public String getStructureId() {
        return structureId;
    }

    /**
     *
     * @param structureId
     */
    public void setStructureId(String structureId) {
        this.structureId = structureId;
    }

    /**
     *
     * @return
     */
    public String getChemicalId() {
        return chemicalId;
    }

    /**
     *
     * @param chemicalId
     */
    public void setChemicalId(String chemicalId) {
        this.chemicalId = chemicalId;
    }

    /**
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     */
    public double getMolecularWeight() {
        return molecularWeight;
    }

    /**
     *
     * @param molecularWeight
     */
    public void setMolecularWeight(double molecularWeight) {
        this.molecularWeight = molecularWeight;
    }

    /**
     *
     * @return
     */
    public String getChemicalName() {
        return chemicalName;
    }

    /**
     *
     * @param chemicalName
     */
    public void setChemicalName(String chemicalName) {
        this.chemicalName = chemicalName;
    }

    /**
     *
     * @return
     */
    public String getFormula() {
        return formula;
    }

    /**
     *
     * @param formula
     */
    public void setFormula(String formula) {
        this.formula = formula;
    }

    /**
     *
     * @return
     */
    public String getInChIKey() {
        return inChIKey;
    }

    /**
     *
     * @param inChIKey
     */
    public void setInChIKey(String inChIKey) {
        this.inChIKey = inChIKey;
    }

    /**
     *
     * @return
     */
    public String getInChI() {
        return inChI;
    }

    /**
     *
     * @param inChi
     */
    public void setInChI(String inChi) {
        this.inChI = inChi;
    }

    /**
     *
     * @return
     */
    public String getSmiles() {
        return smiles;
    }

    /**
     *
     * @param smiles
     */
    public void setSmiles(String smiles) {
        this.smiles = smiles;
    }

}
