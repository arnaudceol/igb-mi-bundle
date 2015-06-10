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
package it.iit.genomics.cru.structures.bridges.eppic;

import it.iit.genomics.cru.bridges.dsysmap.model.DSysMapResult;
import it.iit.genomics.cru.structures.bridges.eppic.client.EppicJaxbClient;

import it.iit.genomics.cru.structures.bridges.eppic.model.EppicAnalysis;
import it.iit.genomics.cru.structures.bridges.eppic.model.EppicAnalysisList;
import it.iit.genomics.cru.structures.bridges.eppic.model.Interface;
import static it.iit.genomics.cru.structures.bridges.eppic.model.Interface.EPPIC_CLASSIFICATION_BIO;
import it.iit.genomics.cru.structures.bridges.eppic.model.InterfaceCluster;
import it.iit.genomics.cru.structures.bridges.eppic.model.Residue;
import it.iit.genomics.cru.structures.model.MoleculeEntry;
import it.iit.genomics.cru.structures.business.StructureMapper;
import it.iit.genomics.cru.structures.model.AAPosition;
import it.iit.genomics.cru.structures.model.AAPositionManager;
import it.iit.genomics.cru.structures.model.ChainMapping;
import it.iit.genomics.cru.structures.model.InteractionStructure;
import it.iit.genomics.cru.structures.sources.StructureSource;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Arnaud Ceol
 */
public class EPPICStructureMapper extends StructureMapper {

    private static final Logger logger = LoggerFactory.getLogger(EPPICStructureMapper.class);

    /**
     * key: uniprotAc + mutations
     */
    private final HashMap<String, DSysMapResult> mutation2dsysmap = new HashMap<>();

    private final EppicJaxbClient wsClient;

    /**
     *
     * @param structureSource
     * @param aaPositionManager
     * @param eppicLocalPath
     */
    public EPPICStructureMapper(StructureSource structureSource,
            AAPositionManager aaPositionManager, String eppicLocalPath) {
        super(structureSource, aaPositionManager);

        File theDir = new File(eppicLocalPath);  // Defining Directory/Folder Name  
        if (false == eppicLocalPath.endsWith(File.pathSeparator)) {
            eppicLocalPath += File.pathSeparator;
        }

        try {
            if (!theDir.exists()) {  // Checks that Directory/Folder Doesn't Exists!  
                boolean result = theDir.mkdir();
                if (result) {
                    logger.info("New Folder created: " + eppicLocalPath);
                }
            } else {
                logger.info("EPPIC Folder: " + eppicLocalPath);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

        this.wsClient = new EppicJaxbClient(eppicLocalPath);

    }

    @Override
    public void searchInterfaces(MappingType type, MoleculeEntry proteinA, MoleculeEntry proteinB, InteractionStructure miStructure, Collection<AAPosition> residuesA) throws Exception {

        if (residuesA.isEmpty()) {
            return;
        }
        // Get residues at interface
        String structureID = miStructure.getStructureID();

        Collection<String> contacts = new HashSet<>();

        if (false == proteinB.isProtein()) {
            logger.error("strange: protB is not a protein");
            return;
        } else if (false == proteinA.isProtein()) {
            logger.error("strange: protA is not a protein");
            return;
        } else {
            EppicAnalysisList list = wsClient.retrievePDB(structureID);

            for (EppicAnalysis analysis : list.getEppicAnalysis()) {
                for (InterfaceCluster cluster : analysis.getInterfaceClusters()) {

                    for (Interface eppicInterface : cluster.getInterfaces()) {

                        if (eppicInterface.getChain1().equals(eppicInterface.getChain2())) {
                            continue;
                        }

                        boolean hasProteinA = false;
                        boolean hasProteinB = false;

                        for (ChainMapping chain : proteinA.getChains(miStructure.getStructureID())) {
                            if (chain.getChain().equals(eppicInterface.getChain1())) {
                                hasProteinA = true;
                            }
                            if (chain.getChain().equals(eppicInterface.getChain2())) {
                                hasProteinB = true;
                            }
                        }

                        for (ChainMapping chain : proteinB.getChains(miStructure.getStructureID())) {
                            if (chain.getChain().equals(eppicInterface.getChain1())) {
                                hasProteinA = true;
                            }
                            if (chain.getChain().equals(eppicInterface.getChain2())) {
                                hasProteinB = true;
                            }
                        }

                        if (false == hasProteinA || false == hasProteinB) {
                            continue;
                        }

                        if (EPPIC_CLASSIFICATION_BIO.equals(eppicInterface.getEppicClassification())) {
                            for (Residue residue : eppicInterface.getResidues()) {
                                if (residue.isCoreEvolutionaryGeometry()) {
                                    String chain = residue.getSide() == 1 ? eppicInterface.getChain1() : eppicInterface.getChain2();
                                    contacts.add(residue.getPdbResidueNumber() + ":" + chain);
                                }
                            }
                        }
                    }
                }
            }

        }
        if (contacts.isEmpty()) {
            return;
        }

        for (AAPosition search : residuesA) {
            for (String pdbResidue : search.getPositions(structureID)) {
                if (contacts.contains(pdbResidue)) {
                    interactionStructuresQueryResiduesAtInterfaces.addResidue(
                            structureID, pdbResidue);
                    if (MappingType.INTERACTION_PROTEINA.equals(type)) {
                        interfaceAAPositionsA.add(search);
                    } else if (MappingType.INTERACTION_PROTEINB.equals(type)) {
                        interfaceAAPositionsB.add(search);
                    } else {
                        logger.error("strange: mapping type = {0}", type);
                    }
                    search.addInterfaceStructurePosition(structureID, pdbResidue, proteinB);
                }
            }
        }

    }
}
