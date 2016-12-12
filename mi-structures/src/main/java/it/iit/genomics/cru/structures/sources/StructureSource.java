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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;

import it.iit.genomics.cru.structures.business.Accessibility;
import it.iit.genomics.cru.structures.model.ChainMapping;
import it.iit.genomics.cru.structures.model.InteractionStructure;
import it.iit.genomics.cru.structures.model.ProteinStructure;
import it.iit.genomics.cru.structures.model.StructureException;
import it.iit.genomics.cru.structures.sources.StructureManager.StructureSourceType;


/**
 * @author Arnaud Ceol
 *
 * Manage information about structures (both PDB and Interactome3D) in order not
 * to download the same structure twice.
 *
 */
public abstract class StructureSource {

    /**
     *
     */
    protected static final Logger logger = LoggerFactory.getLogger(StructureSource.class);

    /**
     *
     */
    protected HashMultimap<String, ProteinStructure> proteinStructures = HashMultimap.create();

    /**
     *
     */
    protected HashMultimap<String, InteractionStructure> interactionStructures = HashMultimap.create();

    /**
     *
     */
    protected ArrayList<String> downloadedStructures = new ArrayList<>();

    /**
     *
     */
    protected PDBFileReader pdbFileReader;

    /**
     * Local copy of the repository
     */
    // protected String localPath2 = null;
    /**
     * Directory where the structures downloaded are chached.
     */
    protected String cacheDir = null;

    /**
     *
     * @return
     */
    public String getCacheDir() {
        return cacheDir;
    }

    /**
     *
     * @param cacheDir
     */
    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
        getStructuresFromCache();
    }

    /**
     *
     */
    protected String URL = null;

    /**
     *
     */
    protected StructureSourceType sourceType = null;

    /**
     * Key: structureID, value: list of residues (format pos:Chain, e.g. 125:A)
     * and the chains they are in contact with
     */
    protected HashMap<String, HashMultimap<String, String>> interfaces = new HashMap<>();

    /**
     * Key: structureID, value: list of pairs of chains and the residues (format
     * pos:Chain, e.g. 125:A)
     */
    protected HashMap<String, HashMap<String, Collection<String>>> interfacesByChains = new HashMap<>();

    /**
     * List of interfaces that have already been computed: key: structureID,
     * value: chainA#chainB
     */
    protected HashMultimap<String, String> interfacesComputed = HashMultimap.create();

    /**
     * List of structure for which no file was found, to be ignored.
     */
    protected ArrayList<String> blackListedStructured = new ArrayList<>();

    /**
     *
     * @param sourceType
     */
    protected StructureSource(StructureSourceType sourceType) {
        this.sourceType = sourceType;
        pdbFileReader = new PDBFileReader();
        pdbFileReader.setAutoFetch(true);
    }

    /**
     *
     * @param structureId
     * @return
     */
    public boolean isBlackListed(String structureId) {
        return blackListedStructured.contains(structureId);
    }

    /**
     *
     * @param structureId
     */
    protected void addToBlackList(String structureId) {
        blackListedStructured.add(structureId);
    }

    /**
     *
     * @param structureId
     * @param chainA
     * @param chainB
     * @return
     */
    public Collection<String> getResiduesAtInterface(String structureId,
            String chainA, String chainB) {

        HashSet<String> residues = new HashSet<>();
        String key = getChainPairKey(chainA, chainB);

        /**
         * TODO: sounds strange, check it
         */
        if (false == interfacesComputed.containsKey(structureId)
                || false == interfacesComputed.get(structureId).contains(key)) {

            // Residues between any chain
            HashMultimap<String, String> allResidues = interfaces.get(structureId);

            if (allResidues != null && false == allResidues.keySet().isEmpty()) {
                for (String residue : allResidues.keySet()) {
                    String chain = residue.split(":")[1];
                    for (String contactChain : allResidues.get(residue)) {
                        if ((chainA.equals(chain) && chainB
                                .equals(contactChain))
                                || (chainB.equals(chain) && chainA
                                .equals(contactChain))) {
                            residues.add(residue);
                        }
                    }
                }
            }
        }

        return residues;

    }

    /**
     * Return all residues from a list of chains in contact with residues in
     * chains from a second list
     *
     * @param structureId
     * @param chainsA
     * @param chainsB
     * @return
     */
    public Collection<String> getContacts(String structureId,
            Collection<String> chainsA, Collection<String> chainsB) {

        ArrayList<String[]> chainPairsToCompute = new ArrayList<>();

        // Which ones should be computed?
        for (String chainA : chainsA) {

            for (String chainB : chainsB) {

                if (chainA.equals(chainB)) {
                    continue;
                }

                String key = getChainPairKey(chainA, chainB);

                /**
                 * TODO: sounds strange, check it
                 */
                if (false == interfacesComputed.containsKey(structureId)
                        || false == interfacesComputed.get(structureId)
                        .contains(key)) {
                    String[] pair = {chainA, chainB};
                    chainPairsToCompute.add(pair);
                }
            }
        }

        if (chainPairsToCompute.size() > 0) {
            getInterfaces(structureId, chainPairsToCompute);
        }

        HashSet<String> residues = new HashSet<>();

        // Residues between any chain
        HashMultimap<String, String> allResidues = interfaces.get(structureId);

        if (allResidues != null && false == allResidues.keySet().isEmpty()) {
            for (String residue : allResidues.keySet()) {
                String chain = residue.split(":")[1];
                for (String contactChain : allResidues.get(residue)) {
                    if ((chainsA.contains(chain) && chainsB
                            .contains(contactChain))
                            || (chainsB.contains(chain) && chainsA
                            .contains(contactChain))) {

                        residues.add(residue);
                    }
                }
            }
        }

        return residues;
    }

    /**
     *
     * @param structureId
     * @param chainsA
     * @param ligandId
     * @return
     */
    public Collection<String> getLigandContacts(String structureId,
            Collection<String> chainsA, String ligandId) {

        ArrayList<String[]> chainPairsToCompute = new ArrayList<>();

        // Which ones should be computed?
        for (String chainA : chainsA) {

            String key = getChainPairKey(chainA, ligandId);

            if (false == interfacesComputed.containsKey(structureId)
                    || false == interfacesComputed.get(structureId)
                    .contains(key)) {
                String[] pair = {chainA, ligandId};
                chainPairsToCompute.add(pair);
            }

        }

        if (chainPairsToCompute.size() > 0) {
            getLigandInterfaces(structureId, chainPairsToCompute);
        }

        HashSet<String> residues = new HashSet<>();

        // Residues between any chain
        HashMultimap<String, String> allResidues = interfaces.get(structureId);

        if (allResidues != null && false == allResidues.keySet().isEmpty()) {
            for (String residue : allResidues.keySet()) {
                String chain = residue.split(":")[1];
                for (String contactChain : allResidues.get(residue)) {
                    if (chainsA.contains(chain) && ligandId.equals(contactChain)) {
                        residues.add(residue);
                    }
                }
            }
        }

        return residues;
    }

    /**
     *
     * @param structureID
     * @param proteinAc
     * @param chain
     * @throws IOException
     */
    public void addProteinStructure(String structureID, String proteinAc,
            ChainMapping chain) throws IOException {

        proteinStructures.put(proteinAc, new ProteinStructure(sourceType,
                structureID, proteinAc, chain));
    }

    /**
     *
     * @param structureID
     * @param proteinAc
     * @param chains
     * @throws IOException
     */
    public void addProteinStructure(String structureID, String proteinAc,
            Collection<ChainMapping> chains) throws IOException {

        proteinStructures.put(proteinAc, new ProteinStructure(sourceType,
                structureID, proteinAc, chains));
    }

    /**
     *
     * @param structureID
     * @param proteinAc1
     * @param proteinAc2
     * @param chainA
     * @param chainB
     * @throws IOException
     */
    public void addInteractionStructure(String structureID, String proteinAc1,
            String proteinAc2, ChainMapping chainA, ChainMapping chainB)
            throws IOException {
        interactionStructures.put(getInteractionKey(proteinAc1, proteinAc2),
                new InteractionStructure(sourceType, structureID, proteinAc1,
                        proteinAc2, chainA, chainB));
    }

    /**
     *
     * @param structureID
     * @param proteinAc1
     * @param proteinAc2
     * @param chainsA
     * @param chainsB
     * @throws IOException
     */
    public void addInteractionStructure(String structureID, String proteinAc1,
            String proteinAc2, Collection<ChainMapping> chainsA,
            Collection<ChainMapping> chainsB) throws IOException {

        InteractionStructure interactionStructure = new InteractionStructure(
                sourceType, structureID, proteinAc1, proteinAc2, chainsA,
                chainsB);

        interactionStructures.put(getInteractionKey(proteinAc1, proteinAc2),
                interactionStructure);
    }

    /**
     *
     * @param proteinAc1
     * @param proteinAc2
     * @return
     */
    public Set<InteractionStructure> getInteractionStructures(
            String proteinAc1, String proteinAc2) {

        String key = getInteractionKey(proteinAc1, proteinAc2);

        if (interactionStructures.containsKey(key)) {
            return interactionStructures.get(key);
        }

        return Collections.emptySet();
    }

    /**
     *
     * @param proteinAc
     * @return
     */
    public Set<ProteinStructure> getProteinStructures(String proteinAc) {
        if (proteinStructures.containsKey(proteinAc)) {
            return proteinStructures.get(proteinAc);
        }

        return Collections.emptySet();
    }

    /**
     *
     * @param proteinAc
     * @return
     */
    public boolean hasProtein(String proteinAc) {
        return proteinStructures.containsKey(proteinAc);
    }

    /**
     *
     * @param proteinAc1
     * @param proteinAc2
     * @return
     */
    public boolean hasInteraction(String proteinAc1, String proteinAc2) {
        return interactionStructures.containsKey(getInteractionKey(proteinAc1,
                proteinAc2));
    }

    private static String getInteractionKey(String proteinAc1, String proteinAc2) {
        if (proteinAc1.compareTo(proteinAc2) <= 0) {
            return proteinAc1 + "#" + proteinAc2;
        } else {
            return proteinAc2 + "#" + proteinAc1;
        }
    }

    private void getInterfaces(String structureId, List<String[]> chainPairs) {

        try {
            Structure structure = getStructure(structureId);
            
            if (structure == null) {
                logger.error( "No structure for {0}", structureId);
            }
            
            HashMultimap<String, String> accessibilities = Accessibility
                    .getFasterContactChains(structure,
                            chainPairs);

            addInterfaces(structureId, accessibilities);

            for (String[] pair : chainPairs) {
                interfacesComputed.put(structureId,
                        getChainPairKey(pair[0], pair[1]));
//				
//				interfacesComputed.add(structureId,
//						getChainPairKey(pair[1], pair[0]));
            }

        } catch (StructureException | NumberFormatException | IOException e) {
            logger.error( "Cannot extract interfaces from " + structureId, e);
        }
    }

    private void getLigandInterfaces(String structureId, List<String[]> chainPairs) {

        try {

            HashMultimap<String, String> accessibilities = Accessibility
                    .getFasterContactHETATMS(getStructure(structureId),
                            chainPairs);
            addInterfaces(structureId, accessibilities);

            for (String[] pair : chainPairs) {
                interfacesComputed.put(structureId,
                        getChainPairKey(pair[0], pair[1]));
            }

        } catch (StructureException | NumberFormatException | IOException e) {
            logger.error( "Cannot extract ligand interfaces from " + structureId, e);
        }
    }

    private void addInterfaces(String structureId,
            HashMultimap<String, String> accessibilities) {

        if (false == interfaces.containsKey(structureId)) {
            interfaces.put(structureId, accessibilities);
        } else {
            interfaces.get(structureId).putAll(accessibilities);
        }

        for (String position : accessibilities.keySet()) {
            for (String contactChain : accessibilities.get(position)) {
                // format of a position: residueNumber:chain
                String chainA = position.split(":")[1];
                String pair = getChainPairKey(chainA, contactChain);

                if (false == interfacesByChains.containsKey(structureId)) {
                    interfacesByChains.put(structureId,
                            new HashMap<String, Collection<String>>());
                }

                if (false == interfacesByChains.get(structureId).containsKey(
                        pair)) {
                    interfacesByChains.get(structureId).put(pair,
                            new HashSet<String>());
                }

                interfacesByChains.get(structureId).get(pair).add(position);
            }
        }

    }

    /**
     * Order is important!!
     *
     * @param chainA
     * @param chainB
     * @return
     */
    private static String getChainPairKey(String chainA, String chainB) {
        // if (chainA.compareTo(chainB) < 0) {
        return chainA + "#" + chainB;
        // } else {
        // return chainB + "#" + chainA;
        // }
    }

    private void getStructuresFromCache() {
        downloadedStructures.clear();

        if (cacheDir == null) {
            return;
        }

        File directory = new File(cacheDir);
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                if (file.getName().endsWith(".ent.gz")) {
                    String structureID = file.getName().replace(".ent.gz", "");
                    downloadedStructures.add(structureID);
                }
            }

        }
    }

    /**
     *
     * @param structureID
     * @return
     */
    protected abstract String getFileName(String structureID);

    /**
     *
     * @param structureID
     * @return
     * @throws StructureException
     */
    public abstract Structure getStructure(String structureID) throws StructureException;

}
