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
package it.iit.genomics.cru.structures.bridges.dsysmap;

import it.iit.genomics.cru.bridges.dsysmap.local.DSysMapLocalRepository;
import it.iit.genomics.cru.bridges.dsysmap.model.DSysMapResult;
import it.iit.genomics.cru.bridges.dsysmap.ws.DSysMapWSClient;
import it.iit.genomics.cru.structures.model.MoleculeEntry;
import it.iit.genomics.cru.structures.business.StructureMapper;
import it.iit.genomics.cru.structures.model.AAPosition;
import it.iit.genomics.cru.structures.model.AAPositionManager;
import it.iit.genomics.cru.structures.model.InteractionStructure;
import it.iit.genomics.cru.structures.sources.StructureSource;
import java.util.Collection;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Arnaud Ceol
 */
public class DSysMapStructureMapper extends StructureMapper {

    private static final Logger logger = LoggerFactory.getLogger(DSysMapStructureMapper.class);

    private final DSysMapLocalRepository repo;

    /**
     * key: uniprotAc + mutations
     */
    private final HashMap<String, DSysMapResult> mutation2dsysmap = new HashMap<>();

    private final DSysMapWSClient wsClient;

    /**
     *
     * @param structureSource
     * @param aaPositionManager
     * @param repo
     * @param searchOnline
     */
    public DSysMapStructureMapper(StructureSource structureSource,
            AAPositionManager aaPositionManager, DSysMapLocalRepository repo, boolean searchOnline) {
        super(structureSource, aaPositionManager);

        logger.info("Init DSysMap");

        this.repo = repo;
        if (searchOnline) {
            this.wsClient = new DSysMapWSClient();
        } else {
            this.wsClient = null;
        }
    }

    @Override
    public void searchInterfaces(MappingType type, MoleculeEntry proteinA, MoleculeEntry proteinB, InteractionStructure miStructure, Collection<AAPosition> residuesA) throws Exception {

        if (residuesA.isEmpty()) {
            return;
        }
        // Get residues at interface
        String structureID = miStructure.getStructureID();

        // logger.log(Level.INFO, "DSysMap structure ID: {0} {1} {2}", new Object[]{proteinA.getUniprotAc(), proteinB.getUniprotAc(), miStructure.getStructureID()});
        Collection<String> contacts;

        if (false == proteinB.isProtein()) {
            logger.error("strange: protB is not a protein");
            return;
        } else if (false == proteinA.isProtein()) {
            logger.error("strange: protA is not a protein");
            return;
        } else {
            contacts = repo.getInterfaceResidues(proteinA.getUniprotAc(), proteinB.getUniprotAc(), miStructure.getStructureID());
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
