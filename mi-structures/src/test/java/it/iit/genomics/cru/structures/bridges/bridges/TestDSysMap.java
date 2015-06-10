/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.iit.genomics.cru.structures.bridges.bridges;

import it.iit.genomics.cru.bridges.dsysmap.local.DSysMapDownload;
import it.iit.genomics.cru.bridges.dsysmap.local.DSysMapLocalRepository;
import it.iit.genomics.cru.bridges.interactome3d.model.I3DInteractionStructure;
import it.iit.genomics.cru.bridges.interactome3d.ws.Interactome3DClient;
import it.iit.genomics.cru.bridges.interactome3d.ws.Interactome3DException;
import it.iit.genomics.cru.bridges.interactome3d.ws.Interactome3DWSClient;
import it.iit.genomics.cru.structures.model.MoleculeEntry;
import it.iit.genomics.cru.structures.bridges.uniprot.UniprotkbUtils;
import it.iit.genomics.cru.structures.bridges.dsysmap.DSysMapStructureMapper;
import it.iit.genomics.cru.structures.business.StructureMapper;
import it.iit.genomics.cru.structures.model.AAPosition;
import it.iit.genomics.cru.structures.model.AAPositionManager;
import it.iit.genomics.cru.structures.model.ChainMapping;
import it.iit.genomics.cru.structures.model.InteractionStructure;
import it.iit.genomics.cru.structures.model.MIGene;
import it.iit.genomics.cru.structures.sources.I3DStructureSource;
import it.iit.genomics.cru.structures.sources.StructureManager;
import it.iit.genomics.cru.structures.sources.StructureManager.StructureSourceType;
import it.iit.genomics.cru.structures.sources.StructureSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author aceol
 */
public class TestDSysMap {

    private static final Logger logger = LoggerFactory.getLogger(TestDSysMap.class);

    public TestDSysMap() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    
    public void testDSysMap() throws IOException, Exception {

        I3DStructureSource source = (I3DStructureSource) StructureManager.getInstance().getStructureSource(StructureSourceType.INTERACTOME3D, "/usr/local/data/interactome3d_human", null);

//        I3DDownload download = new I3DDownload("/usr/local/data/");
//
//        Interactome3DLocalRepository client = UserStructuresManager
//                    .getInstance().getUserRepository(
//                            download.getI3DdatPath("9606"));
//        
        AAPositionManager aa = AAPositionManager.getAAPositionManager("test");
        
        DSysMapDownload dw = new DSysMapDownload(System.getProperty("java.io.tmpdir") );
        if (false == dw.isDatDownloaded()) {
            dw.downloadDat();
        }
        
        DSysMapLocalRepository repo = new DSysMapLocalRepository(dw.getDSysMapDatPath());

        DSysMapStructureMapper mapper = new DSysMapStructureMapper(source,
                aa, repo, false);

        ArrayList<String> acs = new ArrayList<>();
        acs.add("P84022"); //P84022:T261I
        acs.add("Q13485");
        HashMap<String, MoleculeEntry> entries
                = UniprotkbUtils.getInstance("9606").getUniprotEntriesFromUniprotAccessions(acs);

//        HashMap<String, MoleculeEntry> targetUniprotEntries = new HashMap<>();
        //      MapOfMap<String, String> interactors = new MapOfMap<>();
        //  PDBWSClient client = new PDBWSClient();
        // mutations.add("P13647:K404K");
//        aa.
        //searchInterfaces(StructureMapper.MappingType type,
        //   MoleculeEntry proteinA, MoleculeEntry proteinB, InteractionStructure miStructure, Collection<AAPosition> residuesA) throws Exception {
        ArrayList<AAPosition> aas = new ArrayList<>();
        aas.add(aa.getAAPosition(279, 279, 0,0, new MIGene(null, "SMAD3", "chr0",0,0)));
        aas.add(aa.getAAPosition(261, 261, 0,0,new MIGene(null, "SMAD3", "chr0",0,0)));
        aas.add(aa.getAAPosition(287, 287, 0,0,new MIGene(null, "SMAD3", "chr0",0,0)));
//R279|T261|R287
        MoleculeEntry proteinA = entries.get("P84022");
        MoleculeEntry proteinB = entries.get("Q13485");
        
        
  //      Collection<InteractionStructure> interactionStructures   = Interactome3DUtils.getStructures(proteinA,proteinB, client, source);

        String structureId = "P84022-Q13485-EXP-1u7f.pdb1-A-0-B-0";
        Interactome3DWSClient i3d = new Interactome3DWSClient(); //"/usr/local/data/interactome3d_2014_06_human/");
        //i3d.getInteractionStructures(proteinA.getUniprotAc(), proteinB.getUniprotAc());

        for (InteractionStructure i :  getI3DStructures(proteinA, proteinB,
                i3d, source)) {
            System.out.println("Struct: " + i.getStructureID());
            mapper.searchStructureResidues(StructureMapper.MappingType.INTERACTION_PROTEINA, proteinA, i, aas);
        }
//        getI3DStructures(proteinA, proteinB,
//                i3d, source);
        Set<InteractionStructure> ss = source.getInteractionStructures(proteinA.getUniprotAc(), proteinB.getUniprotAc());
        System.out.println("structures: " + ss.size());
        
//        InteractionStructure st = new InteractionStructure(
//                StructureSourceType.INTERACTOME3D, 
//                structureId, proteinA.getUniprotAc(), proteinB.getUniprotAc(), proteinA.getChains(structureId),proteinB.getChains(structureId)) ;
        //       System.out.println("Seq: " + proteinA.getSequence().length() +" " + proteinA.getSequence());// InteractionStructure s = source.getStructure("Q01196-Q13951-EXP-1e50.pdb2-C-0-D-0");

//        Collection<InteractionStructure> ss = source.getInteractionStructures("Q01196", "Q13951");
        for (InteractionStructure s : ss) {
            System.out.println("Structure: " + s.getStructureID());
//            if (s.getStructureID().equals(structureId)) {
            
                mapper.searchInterfaces(StructureMapper.MappingType.INTERACTION_PROTEINA, proteinA, proteinB, s, aas);
                mapper.searchInterfaces(StructureMapper.MappingType.INTERACTION_PROTEINB, proteinB, proteinA, s, aas);
//            }
        }
        for (AAPosition a : mapper.getInterfaceAAPositionsA()) {
            System.out.println("-A- " + a.getStart());
        }
        
        for (AAPosition a : mapper.getInterfaceAAPositionsB()) {
            System.out.println("-B- " + a.getStart());
        }

    }

    private Set<InteractionStructure> getI3DStructures(MoleculeEntry protein1, MoleculeEntry protein2,
            Interactome3DClient client, StructureSource structureSource) {

        String proteinAc1 = protein1.getUniprotAc();
        String proteinAc2 = protein2.getUniprotAc();

        if (false == structureSource.hasInteraction(proteinAc2, proteinAc2)) {

            // Structures from i3D
            Collection<I3DInteractionStructure> i3dStructures;
            try {
                i3dStructures = client.getInteractionStructures(proteinAc1,
                        proteinAc2);

                for (I3DInteractionStructure i3dStructure : i3dStructures) {

                    // Limit the number of structures to download
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

                        chainA.setSequence(protein1.getSequence(null).getSequence());
                        chainB.setSequence(protein2.getSequence(null).getSequence());

                        protein1.addChain(structureID, chainA,
                                null);
                        protein2.addChain(structureID, chainB,
                                null);

                        structureSource.addInteractionStructure(structureID,
                                proteinAc1, proteinAc2, chainA, chainB);
                        logger.info( "Add to source {0} {1} {2} {3} {4}",  new Object[]{structureID ,  proteinAc1, proteinAc2, chainA.getChain(), chainB.getChain()});
                    } catch (IOException e) {
                        logger.error(
                                "Exception in MIWorker", e.getMessage());

                    }
                    System.out.println("chains: " + structureID + " " + StringUtils.join(protein2.getChainNames(structureID), ","));
                }
            } catch (Interactome3DException e1) {
                logger.error(
                        "Problem to retrieve data from Interactome3D", e1);
            }
        }

        return structureSource
                .getInteractionStructures(proteinAc1, proteinAc2);

    }

}
