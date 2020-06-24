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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Arnaud Ceol
 */
@XmlRootElement(name = "structureId")
@XmlAccessorType(XmlAccessType.FIELD)
public class StructureID {

    @XmlAttribute(name = "id")
    String id;

    @XmlElement(name = "polymer")
    private List<Polymer> polymers = new ArrayList<Polymer>();

    @XmlElementWrapper(name = "ligandInfo")
    @XmlElement(name = "ligand")
    private List<Ligand> ligands = new ArrayList<Ligand>();

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public List<Ligand> getLigands() {
        return ligands;
    }

    /**
     *
     * @param ligands
     */
    public void setLigands(List<Ligand> ligands) {
        this.ligands = ligands;
    }

    /**
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public List<Polymer> getPolymers() {
        return polymers;
    }

    /**
     *
     * @param polymers
     */
    public void setPolymers(List<Polymer> polymers) {
        this.polymers = polymers;
    }

}
