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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Arnaud Ceol
 */

@XmlRootElement(name = "residue")
@XmlAccessorType(XmlAccessType.FIELD)
public class Residue {
    @XmlElement
    int residueNumber;
    
    @XmlElement
    int pdbResidueNumber;
    
    @XmlElement
    double asa;
    
    @XmlElement
    double bsa;
    
    @XmlElement
    double bsaPercentage;
    
    @XmlElement
    int side;
    
    @XmlElement
    double entropyScore;

    /**
     *
     * @return
     */
    public boolean isCoreGeometry() {
        return this.getBsaPercentage() > 0.95;
    }
    
    /**
     *
     * @return
     */
    public boolean isCoreEvolutionaryGeometry() {
        return this.getBsaPercentage() > 0.70;
    }

    /**
     *
     * @return
     */
    public int getResidueNumber() {
        return residueNumber;
    }

    /**
     *
     * @param residueNumber
     */
    public void setResidueNumber(int residueNumber) {
        this.residueNumber = residueNumber;
    }
    
    /**
     *
     * @return
     */
    public int getPdbResidueNumber() {
        return pdbResidueNumber;
    }

    /**
     *
     * @param pdbResidueNumber
     */
    public void setPdbResidueNumber(int pdbResidueNumber) {
        this.pdbResidueNumber = pdbResidueNumber;
    }

    /**
     *
     * @return
     */
    public double getAsa() {
        return asa;
    }

    /**
     *
     * @param asa
     */
    public void setAsa(double asa) {
        this.asa = asa;
    }

    /**
     *
     * @return
     */
    public double getBsa() {
        return bsa;
    }

    /**
     *
     * @param bsa
     */
    public void setBsa(double bsa) {
        this.bsa = bsa;
    }

    /**
     *
     * @return
     */
    public double getBsaPercentage() {
        return bsaPercentage;
    }

    /**
     *
     * @param bsaPercentage
     */
    public void setBsaPercentage(double bsaPercentage) {
        this.bsaPercentage = bsaPercentage;
    }

    /**
     *
     * @return
     */
    public int getSide() {
        return side;
    }

    /**
     *
     * @param side
     */
    public void setSide(int side) {
        this.side = side;
    }

    /**
     *
     * @return
     */
    public double getEntropyScore() {
        return entropyScore;
    }

    /**
     *
     * @param entropyScore
     */
    public void setEntropyScore(double entropyScore) {
        this.entropyScore = entropyScore;
    }
    
    
    
    
}
