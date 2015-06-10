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
package it.iit.genomics.cru.structures.sources;

import it.iit.genomics.cru.structures.sources.StructureManager.StructureSourceType;
import java.io.File;
import java.io.IOException;

import org.biojava.nbio.structure.Structure;



/**
 * @author Arnaud Ceol
 *
 * Manage information about structures (both PDB and Interactome3D) in order not
 * to download the same structure twice.
 *
 */
public class USERStructureSource extends StructureSource {

    /**
     * Directory where the structures downloaded are chached.
     */
    protected String localPath = null;

    /**
     *
     * @param localPath
     */
    public USERStructureSource(String localPath) {
        super(StructureSourceType.USER);
        this.localPath = localPath;
    }

    /**
     *
     * @param structureID
     * @return
     */
    @Override
    public Structure getStructure(String structureID) {

        String outputFileName = getFileName(structureID);

        File outputfile = new File(outputFileName);

        try {
            Structure pdbStructure = pdbFileReader.getStructure(outputfile);
            return pdbStructure;
        } catch (IOException e) {
            logger.error("Cannot get PDB for " + structureID, e);
            return null;
        }
    }

    /**
     *
     * @param structureID
     * @return
     */
    @Override
    protected String getFileName(String structureID) {
        return String.format("%s/%s.pdb", localPath, structureID);
    }

}
