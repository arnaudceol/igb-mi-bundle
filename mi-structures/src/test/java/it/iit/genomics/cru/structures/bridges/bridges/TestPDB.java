/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.iit.genomics.cru.structures.bridges.bridges;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;

import it.iit.genomics.cru.structures.bridges.commons.BridgesRemoteAccessException;
import it.iit.genomics.cru.structures.bridges.pdb.PDBWSClient;
import it.iit.genomics.cru.structures.bridges.pdb.model.Chain;
import it.iit.genomics.cru.structures.bridges.pdb.model.Ligand;
import it.iit.genomics.cru.structures.bridges.pdb.model.MoleculeDescription;
import it.iit.genomics.cru.structures.bridges.pdb.model.Polymer;
import it.iit.genomics.cru.structures.bridges.pdb.model.StructureID;
import it.iit.genomics.cru.structures.bridges.uniprot.UniprotkbUtils;
import it.iit.genomics.cru.structures.business.StructureMapper;
import it.iit.genomics.cru.structures.model.AAPosition;
import it.iit.genomics.cru.structures.model.AAPositionManager;
import it.iit.genomics.cru.structures.model.ChainMapping;
import it.iit.genomics.cru.structures.model.InteractionStructure;
import it.iit.genomics.cru.structures.model.MoleculeEntry;
import it.iit.genomics.cru.structures.sources.PDBStructureSource;
import it.iit.genomics.cru.structures.sources.StructureSource;

/**
 *
 * @author aceol
 */
public class TestPDB {

    private static final Logger logger = LoggerFactory.getLogger(TestPDB.class);

    public TestPDB() {
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

    
    public void testAcessibility() throws IOException, Exception {
        //P31751
        ArrayList<String> acs = new ArrayList<>();
        acs.add("P69905");
        Collection<MoleculeEntry> entries
                = UniprotkbUtils.getInstance("9606").getUniprotEntriesFromUniprotAccessions(acs).values();

        StructureSource source = new PDBStructureSource("/usr/local/data/pdb/");

        HashMap<String, MoleculeEntry> targetUniprotEntries = new HashMap<>();
        HashMultimap<String, String> interactors = HashMultimap.create();

        PDBWSClient client = new PDBWSClient();

        MoleculeEntry protA = entries.iterator().next();

        // Do only 10 by 10
        List<String> pdbs = new ArrayList<>();
        pdbs.addAll(protA.getPdbs());

        while (false == pdbs.isEmpty()) {
            List<String> subset = pdbs.subList(0, Math.min(10, pdbs.size()));
            pdbs = pdbs.subList(Math.min(10, pdbs.size()), pdbs.size());
            MoleculeDescription molDesc = client.getDescription(subset);

            if (molDesc != null) {
                for (StructureID structureId : molDesc.getStructureId()) {
                    // for (String pdb : entry.getPdbs()) {
                    // MoleculeDescription molecule =
                    // client.getDescription(pdb);

                    for (Polymer polymer : structureId
                            .getPolymers()) {
                        if (polymer.getPolymerDescription() == null) {
                            logger.error( "No description for {0}", structureId.getId());
                        }
                        if (null != polymer.getType()) {
                            switch (polymer.getType()) {
                                case "protein":
                                    if (null != polymer.getMacromolecule()) {
                                        interactors.put(polymer.getMacromolecule().getAccession().get(0),
                                                "association (from PDB structure)");
                                    }
//                         source.addInteractionStructure(structureId.getId(), protA.getUniprotAc(), rnaEntry.getUniprotAc(), 
//                        protA.getChains(structureId.getId()), rnaEntry.getChains(structureId.getId()));
                                    break;
                                case "dna":
                                    interactors.put(polymer.getPolymerDescription()
                                            .getDescription(),
                                            "DNA association (from PDB structure)");
                                    MoleculeEntry dnaEntry = new MoleculeEntry(
                                            polymer.getPolymerDescription()
                                            .getDescription());
                                    dnaEntry.setSequence("");
                                    dnaEntry.setTaxid("DNA");
                                    for (Chain chain : polymer.getChains()) {
                                        ChainMapping chainMapping = new ChainMapping(
                                                structureId.getId(), chain.getId(),
                                                0, 0);
                                        dnaEntry.addChain(structureId.getId(),
                                                chainMapping, "unspecified");
                                        dnaEntry.addGeneName("DNA");
                                    }
                                    targetUniprotEntries.put(polymer
                                            .getPolymerDescription()
                                            .getDescription(), dnaEntry);
                                    source.addInteractionStructure(structureId.getId(), protA.getUniprotAc(), dnaEntry.getUniprotAc(),
                                            protA.getChains(structureId.getId()), dnaEntry.getChains(structureId.getId()));
                                    System.out.println("Add DNA: " + structureId.getId() + " " +  polymer.getPolymerDescription()
                                            .getDescription() + " " + dnaEntry.getUniprotAc());
                                    break;
                                case "rna":
                                    interactors.put(polymer.getPolymerDescription()
                                            .getDescription(),
                                            "RNA association (from PDB structure)");
                                    MoleculeEntry rnaEntry = new MoleculeEntry(
                                            polymer.getPolymerDescription()
                                            .getDescription());
                                    rnaEntry.setSequence("");
                                    rnaEntry.setTaxid("RNA");
                                    for (Chain chain : polymer.getChains()) {
                                        ChainMapping chainMapping = new ChainMapping(
                                                structureId.getId(), chain.getId(),
                                                0, 0);
                                        rnaEntry.addChain(structureId.getId(),
                                                chainMapping, "unspecified");
                                        rnaEntry.addGeneName("RNA");
                                    }
                                    targetUniprotEntries.put(polymer
                                            .getPolymerDescription()
                                            .getDescription(), rnaEntry);

                                    source.addInteractionStructure(structureId.getId(), protA.getUniprotAc(), rnaEntry.getUniprotAc(),
                                            protA.getChains(structureId.getId()), rnaEntry.getChains(structureId.getId()));
                                    break;
                            }
                        }
                    }
                }
            }

            for (Ligand ligand : client.getLigands(subset)) {
                if ("CARBON MONOXIDE".equals(ligand.getChemicalName()) || ligand.getChemicalName().endsWith("MOLECULE")
                        || ligand.getChemicalName().endsWith(" ION")) {
                    continue;
                }
                System.out.println(ligand.getChemicalName());
                interactors.put(ligand.getChemicalName(),
                        "ligand association (from PDB structure)");
                MoleculeEntry rnaEntry = new MoleculeEntry(
                        ligand.getChemicalName());
                rnaEntry.setSequence("");
                rnaEntry.setTaxid("ligand");
                ChainMapping chainMapping = new ChainMapping(ligand.getStructureId(),
                        "ligand", 0, 0);
                rnaEntry.addChain(ligand.getStructureId(), chainMapping, "unspecified");
                rnaEntry.addGeneName(ligand.getChemicalId());
                source.addInteractionStructure(ligand.getStructureId(), protA.getUniprotAc(), rnaEntry.getUniprotAc(),
                        protA.getChains(ligand.getStructureId()), rnaEntry.getChains(ligand.getStructureId()));
                targetUniprotEntries.put(ligand.getChemicalName(),
                        rnaEntry);
            }
        }

        StructureMapper mapper = new StructureMapper(source, AAPositionManager.getAAPositionManager(this.getClass().getName()));
        for (MoleculeEntry protB : targetUniprotEntries.values()) {
            for (InteractionStructure structure : source.getInteractionStructures(protA.getUniprotAc(), protB.getUniprotAc())) {
                ArrayList<AAPosition> aaPosition = new ArrayList<>();
                aaPosition.add(new AAPosition(86, 86, "1"));
                aaPosition.add(new AAPosition(90, 90, "2"));
                mapper.searchInterfaces(StructureMapper.MappingType.INTERACTION_PROTEINA, protA, protB, structure, aaPosition);
            }

        }

    }

    @Test
    public void testPDB() throws BridgesRemoteAccessException {
        //P31751
        ArrayList<String> acs = new ArrayList<>();
        acs.add("Q01196");
        Collection<MoleculeEntry> entries
                = UniprotkbUtils.getInstance("9606").getUniprotEntriesFromUniprotAccessions(acs).values();

        StructureSource source = new PDBStructureSource("/usr/local/data/pdb/");

        for (MoleculeEntry entry : entries) {

            PDBWSClient client = new PDBWSClient();
            MoleculeDescription molDesc = client.getDescription(entry.getPdbs());

            if (molDesc != null) {
                for (StructureID structureId : molDesc.getStructureId()) {
                    // for (String pdb : entry.getPdbs()) {
                    // MoleculeDescription molecule =
                    // client.getDescription(pdb);

                    for (Polymer polymer : structureId
                            .getPolymers()) {
                        if (polymer.getPolymerDescription() == null) {
                            logger.error( "No description for {0}", structureId.getId());
                        }
                        if (null != polymer.getType()) {
                            switch (polymer.getType()) {
                                case "protein":
                                    if (null != polymer.getMacromolecule()) {
                                        logger.info("{0}" + ": "
                                                + "association (from PDB structure)",
                                                polymer
                                                .getMacromolecule()
                                                .getAccession()
                                                .get(0));
                                    }
                                    break;
                                case "dna":
                                    logger.info("{0}" + ": "
                                            + "DNA association (from PDB structure)", polymer.getPolymerDescription()
                                            .getDescription());
                                    MoleculeEntry dnaEntry = new MoleculeEntry(
                                            polymer.getPolymerDescription()
                                            .getDescription());
                                    dnaEntry.setSequence("");
                                    dnaEntry.setTaxid("DNA");
                                    for (Chain chain : polymer.getChains()) {
                                        ChainMapping chainMapping = new ChainMapping(
                                                structureId.getId(), chain.getId(),
                                                0, 0);
                                        dnaEntry.addChain(structureId.getId(),
                                                chainMapping, "unspecified");
                                        dnaEntry.addGeneName("DNA");
                                    }
                                    logger.info("{0}: {1}", new Object[]{polymer
                                        .getPolymerDescription()
                                        .getDescription(), dnaEntry});
                                    break;
                            }
                        }
                    }
                }

            }
        }
    }
}
