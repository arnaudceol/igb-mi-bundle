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

import it.iit.genomics.cru.structures.model.StructureException;
import java.io.IOException;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.PDBFileReader;

/**
 * Simply read a file and build the structure
 * @author Arnaud Ceol
 */
public class FileStructureSource  extends StructureSource{
    
    /**
     *
     */
    public FileStructureSource() {
        super(StructureManager.StructureSourceType.PDB);
    }

    /**
     *
     * @param structureID
     * @return
     */
    @Override
    protected String getFileName(String structureID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param fileName
     * @return
     * @throws StructureException
     */
    @Override
    public Structure getStructure(String fileName) throws StructureException {
        PDBFileReader pdb = new PDBFileReader();
        try {
            return pdb.getStructure(fileName);
        } catch (IOException ex) {
            logger.error( null, ex);
            return null;
        }
        
    }
    
}
