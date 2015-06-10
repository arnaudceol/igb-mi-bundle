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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Arnaud Ceol
 */
@XmlRootElement(name = "structureId")
@XmlAccessorType(XmlAccessType.FIELD)
public class Polymer {

    @XmlAttribute
    int entityNr;

    @XmlAttribute
    int length;

    @XmlAttribute
    String type;

    @XmlAttribute
    double weight;

    @XmlElement(name = "chain")
    List<Chain> chains = new ArrayList<>();

    @XmlElement(name = "Taxonomy")
    Taxonomy taxonomy;

    @XmlElement(name = "macroMolecule")
    MacroMolecule macromolecule;

    @XmlElement(name = "polymerDescription")
    PolymerDescription polymerDescription;

    /**
     *
     * @return
     */
    public List<Chain> getChains() {
        return chains;
    }

    /**
     *
     * @param chains
     */
    public void setChains(List<Chain> chains) {
        this.chains = chains;
    }

    /**
     *
     * @return
     */
    public Taxonomy getTaxonomy() {
        return taxonomy;
    }

    /**
     *
     * @param taxonomy
     */
    public void setTaxonomy(Taxonomy taxonomy) {
        this.taxonomy = taxonomy;
    }

    /**
     *
     * @return
     */
    public MacroMolecule getMacromolecule() {
        return macromolecule;
    }

    /**
     *
     * @param macromolecule
     */
    public void setMacromolecule(MacroMolecule macromolecule) {
        this.macromolecule = macromolecule;
    }

    /**
     *
     * @return
     */
    public PolymerDescription getPolymerDescription() {
        return polymerDescription;
    }

    /**
     *
     * @param polymerDescription
     */
    public void setPolymerDescription(PolymerDescription polymerDescription) {
        this.polymerDescription = polymerDescription;
    }

    /**
     *
     * @return
     */
    public int getEntityNr() {
        return entityNr;
    }

    /**
     *
     * @param entityNr
     */
    public void setEntityNr(int entityNr) {
        this.entityNr = entityNr;
    }

    /**
     *
     * @return
     */
    public int getLength() {
        return length;
    }

    /**
     *
     * @param length
     */
    public void setLength(int length) {
        this.length = length;
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
    public double getWeight() {
        return weight;
    }

    /**
     *
     * @param weight
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

}
