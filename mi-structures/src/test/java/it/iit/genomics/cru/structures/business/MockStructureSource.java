/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.iit.genomics.cru.structures.business;

import it.iit.genomics.cru.structures.model.StructureException;
import it.iit.genomics.cru.structures.sources.I3DStructureSource;
import it.iit.genomics.cru.structures.sources.StructureManager;
import it.iit.genomics.cru.structures.sources.StructureSource;
import java.io.IOException;
import org.biojava.nbio.structure.Structure;
import org.slf4j.LoggerFactory;

/**
 *
 * @author arnaudceol
 */
public class MockStructureSource extends StructureSource {

    String path = "/Users/arnaudceol/Dropbox/tmp/";
    
    public MockStructureSource() {
        super(StructureManager.StructureSourceType.PDB);
    }

            
    @Override
    protected String getFileName(String structureID) {
        return path + structureID + ".pdb";
    }

    @Override
    public Structure getStructure(String structureID) throws StructureException {
 
        String outputFileName = getFileName(structureID);

        Structure pdbStructure;
        try {
            pdbStructure = pdbFileReader.getStructure(outputFileName);
        } catch (IOException ex) {
            LoggerFactory.getLogger(I3DStructureSource.class).error(null, ex);
            throw new StructureException(ex);
        }

        return pdbStructure;

    }
    
}
