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
package it.iit.genomics.cru.structures.bridges.eppic.model;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Arnaud Ceol
 */

@XmlRootElement(name = "interface")
@XmlAccessorType(XmlAccessType.FIELD)
public class Interface {
    
    /**
     *
     */
    public final static String EPPIC_CLASSIFICATION_BIO = "bio";
    
    /**
     *
     */
    public final static String METHOD_EPPIC_CORE_SURFACE = "eppic-cs";

    /**
     *
     */
    public final static String METHOD_EPPIC_CORE_RIM = "eppic-cr";

    /**
     *
     */
    public final static String METHOD_EPPIC_GEOMETRY = "eppic-gm";

    /**
     *
     */
    public final static String METHOD_EPPIC = "eppic";
    
    
    @XmlElement
    String chain1;
    
    @XmlElement
    String chain2;
    
    @XmlElementWrapper(name = "interfaceScores")
    @XmlElement(name = "interfaceScore")
    Collection<InterfaceScore> interfaceScores = new ArrayList<>();

    @XmlElementWrapper(name = "residues")
    @XmlElement(name = "residue")
    Collection<Residue> residues = new ArrayList<>();

    /**
     *
     * @return
     */
    public String getEppicGmClassification() {
        for (InterfaceScore score : this.getInterfaceScores()) {
            if (METHOD_EPPIC_GEOMETRY.equals(score.getMethod())) {
                return score.getCallName();
            }
        }
        return null;
    }
    
    /**
     *
     * @return
     */
    public String getEppicCrClassification() {
        for (InterfaceScore score : this.getInterfaceScores()) {
            if (METHOD_EPPIC_CORE_RIM.equals(score.getMethod())) {
                return score.getCallName();
            }
        }
        return null;
    }
    
    /**
     *
     * @return
     */
    public String getEppicCsClassification() {
        for (InterfaceScore score : this.getInterfaceScores()) {
            if (METHOD_EPPIC_CORE_SURFACE.equals(score.getMethod())) {
                return score.getCallName();
            }
        }
        return null;
    }
    
    /**
     *
     * @return
     */
    public String getEppicClassification() {
        for (InterfaceScore score : this.getInterfaceScores()) {
            if (METHOD_EPPIC.equals(score.getMethod())) {
                return score.getCallName();
            }
        }
        return null;
    }
    
    /**
     *
     * @return
     */
    public String getChain1() {
        return chain1;
    }

    /**
     *
     * @param chain1
     */
    public void setChain1(String chain1) {
        this.chain1 = chain1;
    }

    /**
     *
     * @return
     */
    public String getChain2() {
        return chain2;
    }

    /**
     *
     * @param chain2
     */
    public void setChain2(String chain2) {
        this.chain2 = chain2;
    }

    /**
     *
     * @return
     */
    public Collection<InterfaceScore> getInterfaceScores() {
        return interfaceScores;
    }

    /**
     *
     * @param interfaceScores
     */
    public void setInterfaceScores(Collection<InterfaceScore> interfaceScores) {
        this.interfaceScores = interfaceScores;
    }

    /**
     *
     * @return
     */
    public Collection<Residue> getResidues() {
        return residues;
    }

    /**
     *
     * @param residues
     */
    public void setResidues(Collection<Residue> residues) {
        this.residues = residues;
    }

    /**
     *
     * @return
     */
    public String hasContact() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
