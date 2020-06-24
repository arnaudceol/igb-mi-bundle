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
package it.iit.genomics.cru.structures.business;

import it.iit.genomics.cru.bridges.interactome3d.model.I3DInteractionStructure;
import it.iit.genomics.cru.bridges.interactome3d.ws.Interactome3DClient;
import it.iit.genomics.cru.bridges.interactome3d.ws.Interactome3DException;
import it.iit.genomics.cru.structures.model.ChainMapping;
import it.iit.genomics.cru.structures.model.InteractionStructure;
import it.iit.genomics.cru.structures.model.MoleculeEntry;
import it.iit.genomics.cru.structures.sources.StructureSource;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author aceol
 */
public class Interactome3DUtils {

    /**
     *
     */
    public final static int maxInteractome3DStructures = 10;
    
    private final static Logger logger = LoggerFactory.getLogger(Interactome3DUtils.class);

    /**
     *
     * @param entry1
     * @param entry2
     * @param client
     * @param structureSource
     * @return
     */
    public static Set<InteractionStructure> getStructures(MoleculeEntry entry1, MoleculeEntry entry2,
            Interactome3DClient client, StructureSource structureSource) {

        String proteinAc1 = entry1.getUniprotAc();
        String proteinAc2 = entry2.getUniprotAc();
        // System.out.println("Search " + proteinAc1 + "-" + proteinAc2);
        if (false == structureSource.hasInteraction(proteinAc1, proteinAc2)) {
            // Structures from i3D
            Collection<I3DInteractionStructure> i3dStructures;
            try {
                i3dStructures = client.getInteractionStructures(proteinAc1,
                        proteinAc2);

                int numInteractome3DStructures = 0;

                for (I3DInteractionStructure i3dStructure : i3dStructures) {

                    numInteractome3DStructures++;
                    // Limit the number of structures to download
                    if (numInteractome3DStructures > maxInteractome3DStructures) {
                        break;
                    }

                    String structureID = i3dStructure.getFilename().substring(
                            0, i3dStructure.getFilename().lastIndexOf("."));

                    try {

                        ChainMapping chainA;
                        ChainMapping chainB;

                        // Warning, A and B can be inverted in the structure
                        if (proteinAc1.equals(i3dStructure.getUniprotAc1())) {
                            chainA = new ChainMapping(structureID, "A",
                                    i3dStructure.getStart1(),
                                    i3dStructure.getEnd1());
                            chainB = new ChainMapping(structureID, "B",
                                    i3dStructure.getStart2(),
                                    i3dStructure.getEnd2());
                        } else {
                            chainA = new ChainMapping(structureID, "B",
                                    i3dStructure.getStart2(),
                                    i3dStructure.getEnd2());
                            chainB = new ChainMapping(structureID, "A",
                                    i3dStructure.getStart1(),
                                    i3dStructure.getEnd1());
                        }

                        chainA.setSequence(entry1.getSequence(null).getSequence());
                        chainB.setSequence(entry2.getSequence(null).getSequence());

                        entry1.addChain(structureID, chainA, null);
                        entry2.addChain(structureID, chainB, null);

                        structureSource.addInteractionStructure(structureID,
                                proteinAc1, proteinAc2, chainA, chainB);

                    } catch (IOException e) {
                        logger.error( "Exception in I3D: " + e.getMessage(), e);
                    }
                }

            } catch (Interactome3DException e1) {
                logger.error("Problem to retrieve data from Interactome3D");
            }
        }

        return structureSource
                .getInteractionStructures(proteinAc1, proteinAc2);

    }

}
