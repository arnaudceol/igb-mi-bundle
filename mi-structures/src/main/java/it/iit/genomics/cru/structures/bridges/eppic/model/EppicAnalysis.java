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

@XmlRootElement(name = "eppicAnalysis")
@XmlAccessorType(XmlAccessType.FIELD)
public class EppicAnalysis {
    
    @XmlElement
    String pdbCode;
    
    
    @XmlElementWrapper(name = "interfaceClusters")
    @XmlElement(name = "interfaceCluster")
    Collection<InterfaceCluster> interfaceClusters = new ArrayList<>();

    /**
     *
     * @return
     */
    public String getPdbCode() {
        return pdbCode;
    }

    /**
     *
     * @param pdbCode
     */
    public void setPdbCode(String pdbCode) {
        this.pdbCode = pdbCode;
    }

    /**
     *
     * @return
     */
    public Collection<InterfaceCluster> getInterfaceClusters() {
        return interfaceClusters;
    }

    /**
     *
     * @param interfaceClusters
     */
    public void setInterfaceClusters(Collection<InterfaceCluster> interfaceClusters) {
        this.interfaceClusters = interfaceClusters;
    }
    
    
}
