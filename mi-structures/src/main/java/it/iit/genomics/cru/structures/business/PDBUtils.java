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

import it.iit.genomics.cru.structures.model.ChainMapping;
import it.iit.genomics.cru.structures.model.InteractionStructure;
import it.iit.genomics.cru.structures.model.MoleculeEntry;
import it.iit.genomics.cru.structures.sources.StructureSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author aceol
 */
public class PDBUtils {
    
    private final static Logger logger = LoggerFactory.getLogger(PDBUtils.class.getName());

    /**
     *
     * @param entry1
     * @param entry2
     * @param structureSource
     * @return
     */
    public static Set<InteractionStructure> getStructures(MoleculeEntry entry1, MoleculeEntry entry2,
            StructureSource structureSource) {
        
        
        String proteinAc1 = entry1.getUniprotAc();
        String proteinAc2 = entry2.getUniprotAc();
        
         // Get structures if not already done
        if (false == structureSource.hasInteraction(proteinAc1, proteinAc2)) {

            ArrayList<String> pdbs = new ArrayList<>();
            pdbs.addAll(entry1.getPdbs());
            pdbs.retainAll(entry2.getPdbs());

            for (String pdb : pdbs) {
                if (pdb.length() > 4) {
                    // this is an Interactome3D id.
                    continue;
                }
                try {
                    Collection<ChainMapping> chainsA = entry1
                            .getChains(pdb);
                    Collection<ChainMapping> chainsB = entry2
                            .getChains(pdb);

                    structureSource.addInteractionStructure(pdb, proteinAc1,
                            proteinAc2, chainsA, chainsB);
                } catch (IOException e) {
                   logger.error( "Cannot get PDB structures ", e);
                }

            }
        }

        return structureSource.getInteractionStructures(
                proteinAc1, proteinAc2);

    
    }
    
}
