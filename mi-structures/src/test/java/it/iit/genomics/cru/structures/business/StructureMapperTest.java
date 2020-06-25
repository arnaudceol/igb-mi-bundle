/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.iit.genomics.cru.structures.business;

import it.iit.genomics.cru.structures.model.AAPosition;
import it.iit.genomics.cru.structures.model.AAPositionManager;
import it.iit.genomics.cru.structures.model.InteractionStructure;
import it.iit.genomics.cru.structures.model.MoleculeEntry;
import it.iit.genomics.cru.structures.model.StructureException;
import it.iit.genomics.cru.structures.sources.StructureManager;
import it.iit.genomics.cru.structures.sources.StructureSource;
import java.util.HashSet;
import org.junit.Test;

/**
 *
 * @author arnaudceol
 */
public class StructureMapperTest {
    
  
    /**
     * Test of getInterfaceAAPositionsB method, of class StructureMapper.
     */
    @Test
    public void testGetPDBPositions() throws StructureException {
        
        StructureSource structureSource = new MockStructureSource();
        AAPositionManager aaPositionManager = AAPositionManager.getAAPositionManager("test");

        String structureId = "P11310-P13804-EXP-2a1t.pdb1-D-0-R-0";
        
        StructureMapper instance = new StructureMapper(structureSource, aaPositionManager);
        
        MoleculeEntry protein = new MoleculeEntry("P13804");
        
//        I3DInteractionStructure s = new I3DInteractionStructure();
//        s.setFilename(structureId + ".pdb");
//        s.setUniprotAc1("P11310");
//        s.setUniprotAc2("P13804");
//        s.setChainId1("A");
//        s.setChainId2("B");
       InteractionStructure s =new 
        InteractionStructure(StructureManager.StructureSourceType.INTERACTOME3D, structureId, "P11310", "P13804", "A", 35, 421, "B", 18, 333) ;
        HashSet<AAPosition> expResult = new HashSet<>();
       
        
        
        instance.searchStructureResidues(StructureMapper.MappingType.INTERACTION_PROTEINA, protein, s, expResult);
       // assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
       // fail("The test case is a prototype.");
    }
    
}
