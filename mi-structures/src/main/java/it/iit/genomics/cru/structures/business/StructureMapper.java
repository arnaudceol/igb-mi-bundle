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

import it.iit.genomics.cru.structures.alignment.SmithWaterman;
import it.iit.genomics.cru.structures.model.MoleculeEntry;
import it.iit.genomics.cru.structures.model.AAPosition;
import it.iit.genomics.cru.structures.model.AAPositionManager;
import it.iit.genomics.cru.structures.model.ChainMapping;
import it.iit.genomics.cru.structures.model.InteractionStructure;
import it.iit.genomics.cru.structures.model.StructureException;
import it.iit.genomics.cru.structures.model.StructureModel;
import it.iit.genomics.cru.structures.model.position.UniprotPosition;
import it.iit.genomics.cru.structures.sources.StructureSource;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.google.common.collect.ArrayListMultimap;

import org.apache.commons.lang.ArrayUtils;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.GroupType;
import org.biojava.nbio.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Arnaud Ceol
 */
public class StructureMapper {

    static final Logger logger = LoggerFactory.getLogger(StructureMapper.class.getName());

    /**
     *
     */
    public enum MappingType {

        /**
         *
         */
        INTERACTION_PROTEINA,
        /**
         *
         */
        INTERACTION_PROTEINB //, PROTEINA, PROTEINB
    };

    /**
     *
     */
    public class StructureResidueContainer {

        private boolean hasResidues = false;

        private final ArrayList<String> structuresIds = new ArrayList<>();

        /**
         * key = structureId + "#" +chainId
         */
        private final ArrayListMultimap<String, String> chain2residues = ArrayListMultimap.create();

        /**
         *
         * @param structureId
         * @param chainId
         * @param pdbPosition
         */
        public void addResidue(String structureId, String chainId,
                int pdbPosition) {
            addResidue(structureId, Integer.toString(pdbPosition) + ":"
                    + chainId);
        }

        /**
         *
         * @param structureId
         * @param pdbResidue
         */
        public void addResidue(String structureId, String pdbResidue) {
            if (false == structuresIds.contains(structureId)) {
                structuresIds.add(structureId);
            }

            chain2residues.put(structureId, pdbResidue);

            hasResidues = true;
        }

        /**
         *
         * @return
         */
        public ArrayList<String> getStructuresIds() {
            return structuresIds;
        }

        /**
         *
         * @param structureId
         * @return
         */
        public Collection<String> getResidues(String structureId) {
            return chain2residues.get(structureId);
        }

        /**
         *
         * @return
         */
        public boolean hasResidues() {
            return hasResidues;
        }

    }

    /**
     *
     */
    protected StructureSource structureSource;

    /**
     *
     */
    protected AAPositionManager aaPositionManager;

    /**
     * For each structure, list of query residues -> pdb to chain to residues
     */
    protected final StructureResidueContainer interactionStructuresQueryResidues;

    /**
     * For each structure, list of query residues that lies at an interface ->
     * pdb to chain to residues
     */
    protected final StructureResidueContainer interactionStructuresQueryResiduesAtInterfaces;

    /**
     * For each structure, list of query residues -> pdb to chain to residues
     */
    protected final StructureResidueContainer interactionStructuresQueryResiduesA;

    /**
     *
     */
    protected final StructureResidueContainer interactionStructuresQueryResiduesB;

    /**
     *
     */
    protected final HashSet<AAPosition> interfaceAAPositionsA = new HashSet<>();

    /**
     *
     */
    protected final HashSet<AAPosition> interfaceAAPositionsB = new HashSet<>();

    /**
     *
     * @param structureSource
     * @param aaPositionManager
     */
    public StructureMapper(StructureSource structureSource,
            AAPositionManager aaPositionManager) {

        interactionStructuresQueryResidues = new StructureResidueContainer();
        interactionStructuresQueryResiduesAtInterfaces = new StructureResidueContainer();

        interactionStructuresQueryResiduesA = new StructureResidueContainer();
        interactionStructuresQueryResiduesB = new StructureResidueContainer();

        this.structureSource = structureSource;
        this.aaPositionManager = aaPositionManager;
    }

    /**
     *
     * @param type
     * @param protein
     * @param miStructure
     * @param residues
     * @throws StructureException
     */
    public void searchStructureResidues(MappingType type, MoleculeEntry protein,
            StructureModel miStructure, Collection<AAPosition> residues) throws StructureException {
        //logger.log(Level.INFO, "Get structure residues: {0} {1} {2}", new Object[]{protein.getGeneName(), miStructure.getStructureID(), residues.size()});
        String structureID = miStructure.getStructureID();

        // StructureResidueContainer src = null;
        if (residues == null || residues.isEmpty()) {
            return;
        }

        // First check residue
        // boolean hasResidues = false;
        Structure structure = structureSource.getStructure(miStructure
                .getStructureID());

        if (structure == null) {
            logger.warn("No structure for {0}", structureID);
            return;
        }

        for (AAPosition search : residues) {

//                logger.log(Level.INFO, "Search: {0} {1}", new Object[]{structureID, search.toString()});
            if (search.notOnStructure(structureID)) {
//                logger.log(Level.WARNING, "Not on structure: {0} {1}", new Object[]{structureID, search.toString()});
                continue;
            }

            boolean isOnStructure = false;

            // Get residues on structure
            for (ChainMapping structureMapping : miStructure.getChains(protein
                    .getUniprotAc())) {

//                logger.log(Level.INFO, "Search: {0} ", new Object[]{structureMapping.getChain() + ":" + structureMapping.getStart()});
                /**
                 * TODO: this is not the right place to do that
                 */
                structureMapping.setProteinAc(protein.getUniprotAc());

                if (structureMapping.getStart() > structureMapping.getEnd()) {
                    logger.error("Bad mapping: {0} > {1}", new Object[]{structureMapping.getStart(), structureMapping.getEnd()});
                }

                Chain pdbChain;
                try {
                    pdbChain = structure.getChainByPDB(structureMapping
                            .getChain());
                } catch (org.biojava.nbio.structure.StructureException ex) {
                    logger.error("Cannot get chain " + structureMapping.getChain() + " from structure " + structureID, ex);
                    continue;
                }

                if (pdbChain.getAtomSequence().length() == 0) {
                    logger.error("No atoms for chain: {0} > {1}", new Object[]{pdbChain.getChainID(), structureID});
                    continue;
                }

                /**
                 * The chain in the PDB file contains both atoms and hetams, we
                 * should consider it when Coloring atoms
                 */
                /**
                 * TODO: Simpler test?
                 */
                if ((structureMapping.getStart() <= search.getStart()
                        && structureMapping.getEnd() >= search.getEnd())
                        || (structureMapping.getStart() >= search.getStart()
                        && structureMapping.getEnd() <= search.getEnd())
                        || (structureMapping.getStart() <= search.getEnd()
                        && structureMapping.getStart() >= search.getStart())
                        || (structureMapping.getEnd() <= search.getEnd()
                        && structureMapping.getEnd() >= search.getStart())
                        || (search.getStart() <= structureMapping.getEnd()
                        && search.getStart() >= structureMapping.getStart())
                        || (search.getEnd() <= structureMapping.getEnd()
                        && search.getEnd() >= structureMapping.getStart())) {
                    // get chain
                    int[] pdbPositions = getPdbPositions(structureMapping.getStructureID(), pdbChain, search,
                            protein);

                    for (int pdbPosition : pdbPositions) {
                        search.addStructurePosition(structureID,
                                pdbPosition + ":" + pdbChain.getChainID());

                        search.addStructure2ProteinPosition(
                                structureID, pdbPosition,
                                pdbChain.getChainID(), structureMapping);

                        isOnStructure = true;
                    }
                }

            }

            if (false == isOnStructure) {
                search.addNotOnStructure(structureID);
            }

            if (false == search.getPositions(structureID).isEmpty()) {
                // Add residue
                for (String pdbPosition : search
                        .getPositions(structureID)) {

                    switch (type) {
                        case INTERACTION_PROTEINA:
                            interactionStructuresQueryResidues.addResidue(
                                    structureID, pdbPosition);
                            interactionStructuresQueryResiduesA.addResidue(
                                    structureID, pdbPosition);
                            break;
                        case INTERACTION_PROTEINB:
                            interactionStructuresQueryResidues.addResidue(
                                    structureID, pdbPosition);
                            interactionStructuresQueryResiduesB.addResidue(
                                    structureID, pdbPosition);
                            break;
                        default:
                            logger.warn("Strange type: {0}", type);
                            return;
                    }

                }
            }
        }

    }

    /**
     * Mapping type is either PROTEINA or PROTEINB
     *
     * @param type
     * @param proteinA
     * @param proteinB
     * @param miStructure
     * @param residuesA
     * @throws Exception
     */
    public void searchInterfaces(MappingType type, MoleculeEntry proteinA,
            MoleculeEntry proteinB, InteractionStructure miStructure,
            Collection<AAPosition> residuesA) throws Exception {

        if (residuesA.isEmpty()) {
            return;
        }

        // Get residues at interface
        String structureID = miStructure.getStructureID();

        Collection<String> contacts;

        if (proteinB.isLigand()) {
            contacts = structureSource
                    .getLigandContacts(structureID,
                            proteinA.getChainNames(structureID),
                            proteinB.getGeneName());
        } else if (proteinA.isLigand()) {
            contacts = null;
            logger.error("strange: protA is a ligand");
        } else {
            contacts = structureSource.getContacts(structureID,
                    proteinA.getChainNames(structureID),
                    proteinB.getChainNames(structureID));
        }

        if (contacts == null || contacts.isEmpty()) {
            return;
        }

        for (AAPosition search : residuesA) {

            for (String pdbResidue : search.getPositions(structureID)) {
                if (contacts.contains(pdbResidue)) {
                    interactionStructuresQueryResiduesAtInterfaces.addResidue(
                            structureID, pdbResidue);
                    if (MappingType.INTERACTION_PROTEINA.equals(type)) {
                        interfaceAAPositionsA.add(search);
                        // interfaceRegionsA.add(search.getDescription());
                        // interfaceProteinResiduesA.add(search.getUniprotAAPosition(proteinA.getSequence()));
                    } else if (MappingType.INTERACTION_PROTEINB.equals(type)) {
                        interfaceAAPositionsB.add(search);
                        // interfaceRegionsB.add(search.getDescription());
                        // interfaceProteinResiduesB.add(search.getUniprotAAPosition(proteinA.getSequence()));
                    } else {
                        logger.warn("strange: mapping type = {0}", type);
                        // strange
                    }
                    search.addInterfaceStructurePosition(structureID, pdbResidue, proteinB);
                }
            }
        }

    }

    private int[] getPdbPositions(String structureId, Chain chain, AAPosition search, MoleculeEntry protein) {

        ArrayList<Integer> positions = new ArrayList<>();

        // Align the region to the chain
        SmithWaterman nw;
        nw = new SmithWaterman(search.getGene().getUniprotSequence().getSequence(), chain.getAtomSequence());

        String regionAlignedSequence = nw.getAlignmentSeqA();
        String chainAlignedSequence = nw.getAlignmentSeqB();

        int groupPosition = 0;

        // go to the first AA of the group, skiping non AA
        for (int chainAAindex = 0; chainAAindex < nw.getStartAlignmentB(); chainAAindex++) {

            Group group = chain.getAtomGroup(groupPosition);

            if (false == GroupType.AMINOACID.equals(group.getType())) {
                chainAAindex++;
            }
            groupPosition++;
        }

        int uniprotCursor = nw.getStartAlignmentA();

        for (int aaPosition = 0; aaPosition < regionAlignedSequence.length(); aaPosition++) {

            char uniprotAA = regionAlignedSequence.charAt(aaPosition);
            char pdbAA = chainAlignedSequence.charAt(aaPosition);

            if ('-' == pdbAA) {
                uniprotCursor++;
                continue;
            }

            Group group = chain.getAtomGroup(groupPosition);

            groupPosition++;

            if (GroupType.AMINOACID.equals(group.getType())) {

                Integer pos = group.getResidueNumber().getSeqNum();

                if ('-' == uniprotAA) {
                    logger.warn("Position not aligned to uniprot: {0} {1} {2}", new Object[]{structureId, chain.getChainID(), pos});
                } else if (uniprotCursor >= search.getStart() - 1 && uniprotCursor <= search.getEnd() - 1) {

                    // Add residue
                    positions.add(pos);

                    int uniprotPos = uniprotCursor + 1;

                    search.addPdbToProtein(structureId, pos, chain.getChainID(), new UniprotPosition(uniprotPos));
                }

            }

            if ('-' != uniprotAA) {
                uniprotCursor++;
            }

        }

        return ArrayUtils.toPrimitive(positions.toArray(new Integer[positions
                .size()]));

    }

    /**
     *
     * @return
     */
    public Collection<String> getStructuresWithQueryResidues() {
        return interactionStructuresQueryResidues.getStructuresIds();
    }

    /**
     *
     * @param pdbID
     * @return
     */
    public Collection<String> getStructuresResidues(String pdbID) {
        return interactionStructuresQueryResidues.getResidues(pdbID);
    }

    /**
     *
     * @return
     */
    public Collection<String> getStructuresQueryResiduesAtInterfaces() {
        return interactionStructuresQueryResiduesAtInterfaces
                .getStructuresIds();
    }

    /**
     *
     * @param pdbID
     * @return
     */
    public Collection<String> getStructuresResiduesAtInterfaces(String pdbID) {
        return interactionStructuresQueryResiduesAtInterfaces
                .getResidues(pdbID);
    }

    /**
     *
     * @return
     */
    public boolean hasResiduesOnStructure() {
        return interactionStructuresQueryResidues.hasResidues();
    }

    /**
     *
     * @return
     */
    public boolean hasInterfaceOnStructure() {
        return interactionStructuresQueryResiduesAtInterfaces.hasResidues();
    }

    /**
     *
     * @param structureSource
     */
    public void setStructureSource(StructureSource structureSource) {
        this.structureSource = structureSource;
    }

    /**
     *
     * @param aaPositionManager
     */
    public void setAaPositionManager(AAPositionManager aaPositionManager) {
        this.aaPositionManager = aaPositionManager;
    }

    /**
     *
     * @return
     */
    public boolean proteinAHasResiduesOnStructure() {
        return interactionStructuresQueryResiduesA.hasResidues();
    }

    /**
     *
     * @return
     */
    public boolean proteinAHasInterfaceOnStructure() {
        return false == interfaceAAPositionsA.isEmpty();
    }

    /**
     *
     * @return
     */
    public boolean proteinBHasInterfaceOnStructure() {
        return false == interfaceAAPositionsB.isEmpty();
    }

    /**
     *
     * @return
     */
    public boolean proteinBHasResiduesOnStructure() {
        return this.interactionStructuresQueryResiduesB.hasResidues();
    }

    /**
     *
     * @return
     */
    public Collection<String> getStructuresWithQueryResiduesA() {
        return interactionStructuresQueryResiduesA.getStructuresIds();
    }

    /**
     *
     * @param pdbID
     * @return
     */
    public Collection<String> getStructuresResiduesA(String pdbID) {
        return interactionStructuresQueryResiduesA.getResidues(pdbID);
    }

    /**
     *
     * @return
     */
    public Collection<String> getStructuresWithQueryResiduesB() {
        return interactionStructuresQueryResiduesB.getStructuresIds();
    }

    /**
     *
     * @param pdbID
     * @return
     */
    public Collection<String> getStructuresResiduesB(String pdbID) {
        return interactionStructuresQueryResiduesB.getResidues(pdbID);
    }

    /**
     *
     * @return
     */
    public HashSet<AAPosition> getInterfaceAAPositionsA() {
        return interfaceAAPositionsA;
    }

    /**
     *
     * @return
     */
    public HashSet<AAPosition> getInterfaceAAPositionsB() {
        return interfaceAAPositionsB;
    }

}
