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
package it.iit.genomics.cru.structures.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;

import it.iit.genomics.cru.structures.model.position.UniprotPosition;

/**
 *
 * @author Arnaud Ceol
 *
 * Mapping between the genomic region and the amino acid position of a protein
 * sequence.
 *
 */
public class AAPosition {

    static final Logger logger = LoggerFactory.getLogger(AAPosition.class.getName());

    // Starts from 1
    /**
     *
     */
    protected final int uniprotStart;

    // Starts from 1, inclusive
    /**
     *
     */
    protected final int uniprotEnd;

    // associated symmetry, e.g. genomic region
    // private SeqSymmetry symmetry;
    /**
     *
     */
    protected final String description;

    //protected String chromosome;
    /**
     *
     */
    protected int genomeMin;

    /**
     *
     */
    protected int genomeMax;
    //protected boolean reverse;

    /**
     *
     */
    protected MIGene gene;

    // positions in structures: key = structureID, values = positions
    // (position:chainID)
    /**
     *
     */
    protected final HashMultimap<String, String> structurePositions = HashMultimap.create();

    /**
     *
     */
    protected final HashMultimap<String, String> structureInterfaces = HashMultimap.create();

    /**
     *
     */
    protected final HashMultimap<MoleculeEntry, String> partnerInterfaces = HashMultimap.create();

    /**
     *
     */
    protected final ArrayList<String> notOnStructure = new ArrayList<>();

    /**
     * Key: structureId:pos:chain, values: associated AA positions
     */
    private final HashMultimap<String, ChainMapping> structurePosition2AAPositions = HashMultimap.create();

    /**
     *
     * @param start
     * @param end
     * @param description
     */
    public AAPosition(int start, int end, String description) {
        this.uniprotStart = start;
        this.uniprotEnd = end;
//        /** TODO: warning, I should change it, or even remove the constructor */
//        this.transcriptAAStart = start;
//        this.transcriptAAEnd = end;
        this.description = description;
    }

    /**
     *
     * @param uniprotStart
     * @param uniprotEnd
     * @param genomeMin
     * @param genomeMax
     * @param gene
     */
    public AAPosition(int uniprotStart, int uniprotEnd, int genomeMin, int genomeMax, MIGene gene) { //String chromosome, int genomeMin, int genomeMax, boolean reverse) {
        this.uniprotStart = uniprotStart;
        this.uniprotEnd = uniprotEnd;
        this.genomeMin = genomeMin;
        this.genomeMax = genomeMax;
        this.description = gene.getChromosomeName() + ":" + genomeMin + "-" + genomeMax;
        this.gene = gene;
    }

//    public void addUniprotToTranscriptAA(HashMap<Integer, Integer> uniprot2transcriptAA) {
//        this.uniprot2transcriptAA.putAll(uniprot2transcriptAA);
//    }
    /**
     *
     * @return
     */
    public int getStart() {
        return uniprotStart;
    }

    /**
     *
     * @return
     */
    public int getEnd() {
        return uniprotEnd;
    }

    /**
     *
     * @return
     */
    public MIGene getGene() {
        return gene;
    }

    /**
     *
     * @return
     */
    public String getSequence() {
        return getSequence(getStart(), getEnd(), gene.getUniprotSequence().getSequence());
    }

    private String getSequence(int start, int end, String proteinSequence) {
        // String index starts at 0 while sequence index starts at 1.
        try {
            return proteinSequence.substring(start - 1, end);
        } catch (Exception e) {
            logger.error(
                    "Cannot get AA sequence: " + this.toString()
                    + ", protein length: " + proteinSequence.length()
                    + " Symmetry: " + description, e);
            return null;
        }
    }

    /**
     *
     * @param structureID
     * @param position
     */
    public void addStructurePosition(String structureID, String position) {
        structurePositions.put(structureID, position);
    }

    /**
     *
     * @return
     */
    public Collection<String> getStructureIDs() {
        return structurePositions.keySet();
    }

    /**
     *
     * @param structureID
     * @return
     */
    public Collection<String> getPositions(String structureID) {
        if (structurePositions.get(structureID) == null) {
            return Collections.emptyList();
        }
        return structurePositions.get(structureID);
    }

    /**
     *
     * @param structureID
     * @return
     */
    public Collection<String> getInterfacePositions(String structureID) {
        if (structureInterfaces.get(structureID) == null) {
            return Collections.emptyList();
        }
        return structureInterfaces.get(structureID);
    }

    /**
     *
     * @param partner
     * @return
     */
    public Collection<String> getInterfacePartnerPositions(MoleculeEntry partner) {
        if (partnerInterfaces.get(partner) == null) {
            return Collections.emptyList();
        }
        return partnerInterfaces.get(partner);
    }

    /**
     *
     * @param partner
     * @return
     */
    public Collection<String> getInterfacePartnerAAPositions(MoleculeEntry partner) {
        if (partnerInterfaces.get(partner) == null) {
            return Collections.emptyList();
        }

        HashSet<String> positions = new HashSet<>();

        for (String pdbPos : getInterfacePartnerPositions(partner)) {
            UniprotPosition pos = pdbPos2uniprotPositions.get(pdbPos);
            if (pos == null) {
                System.out.println("####    no pos for " + pdbPos);
            }
//            Integer pos = uniprot2transcriptAA.getKey(transcriptAA);
//            if (pos== null) {
//                System.out.println("####    no pos for " + pdbPos + ", " +transcriptAA);
//            }
//            System.out.println(this.getGene().getID() + ": " + gene.getProteins().iterator().next().getSequence(gene.getID()));
            try {
                positions.add(gene.getProtein().getSequence(gene.getID()).getSequence().substring(pos.getPosition() - 1, pos.getPosition()) + pos.getPosition());
            } catch (StringIndexOutOfBoundsException e) {

                logger.error("Out of bounds: {0}  {1}: {2}", new Object[]{pos, this.getGene().getID(), gene.getProtein().getSequence(gene.getID())});

                return Collections.emptyList();
            }
        }

        return positions;
    }

    /**
     *
     * @param structureID
     * @param position
     * @param partner
     */
    public void addInterfaceStructurePosition(String structureID, String position, MoleculeEntry partner) {
        structureInterfaces.put(structureID, position);
        partnerInterfaces.put(partner, structureID + ":" + position);
    }

    /**
     *
     * @return
     */
    public Collection<String> getInterfaceStructureIDs() {
        return structureInterfaces.keySet();
    }

    /**
     *
     * @param structureID
     * @return
     */
    public Collection<String> getInterfaceStructurePositions(String structureID) {
        if (structureInterfaces.get(structureID) == null) {
            return Collections.emptyList();
        }
        return structureInterfaces.get(structureID);
    }

    @Override
    public String toString() {
        return "AAPosition[" + "start=" + uniprotStart + ", end=" + uniprotEnd + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + uniprotEnd;
        result = prime * result + uniprotStart;
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AAPosition other = (AAPosition) obj;
        if (uniprotEnd != other.uniprotEnd) {
            return false;
        }
        if (uniprotStart != other.uniprotStart) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        return true;
    }

    /**
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param structureID
     */
    public void addNotOnStructure(String structureID) {
        notOnStructure.add(structureID);
    }

    /**
     *
     * @param structureID
     * @return
     */
    public boolean notOnStructure(String structureID) {
        return notOnStructure.contains(structureID);
    }

    /**
     *
     * @return
     */
    public String getProteinAAPosition() {
        String position = getSequence();

        if (uniprotStart == uniprotEnd) {
            position += uniprotStart;
        } else {
            position += uniprotStart + "-" + uniprotEnd;
        }

        return position;
    }

    /**
     *
     * @return
     */
    public int getGenomeMin() {
        return genomeMin;
    }

    /**
     *
     * @return
     */
    public int getGenomeMax() {
        return genomeMax;
    }

    /**
     *
     * @param structureID
     * @param position
     * @param chain
     * @param aa
     */
    public void addStructure2ProteinPosition(String structureID, int position, String chain, ChainMapping aa) {
        structurePosition2AAPositions.put(structureID + ":" + position + ":" + chain, aa);
    }

    // chain mapping:   ----------S-----------E--------
    // AA Position      -------------S-----E-----------
    // protein          S-----------------------------E
    // PDB position     ---------------S-E-------------
    /**
     *
     * @return
     */
    public Collection<String> getGenomicPositions() {

        HashSet<String> genomePositions = new HashSet<>();

        for (String structureId : getInterfaceStructureIDs()) {

            for (String pdbPosition : getInterfaceStructurePositions(structureId)) {

                String StructurePdbPosition = structureId + ":" + pdbPosition;

                if (false == structurePosition2AAPositions.containsKey(StructurePdbPosition)) {
                    continue;
                }

                genomePositions.addAll(getGenomicPositions(StructurePdbPosition));
            }
        }

        return genomePositions;
    }

    /**
     *
     * @param partner
     * @return
     */
    public Collection<String> getGenomicPositions(MoleculeEntry partner) {

        HashSet<String> genomePositions = new HashSet<>();

        for (String structurePdbPosition : getInterfacePartnerPositions(partner)) {
            genomePositions.addAll(getGenomicPositions(structurePdbPosition));
        }

        return genomePositions;
    }

    private Collection<String> getGenomicPositions(String structurePdbPosition) {

        UniprotPosition uniprotPosition = pdbPos2uniprotPositions.get(structurePdbPosition);
        if (uniprotPosition == null) {
            return Collections.emptyList();
        }
        return getGenomicPositions(uniprotPosition);
    }

    /**
     *
     * @param uniprotPosition
     * @return
     */
    public Collection<String> getGenomicPositions(UniprotPosition uniprotPosition) {
        return gene.getGenomePositions(uniprotPosition);
    }

    /**
     * 1-based
     *
     * @param structureId
     * @param pdbPosition
     * @param chain
     * @param uniprotPos
     */
    public void addPdbToProtein(String structureId, int pdbPosition, String chain, UniprotPosition uniprotPos) {
        String pdbKey = structureId + ":" + pdbPosition + ":" + chain;
        pdbPos2uniprotPositions.put(pdbKey, uniprotPos);
    }

    private final HashMap<String, UniprotPosition> pdbPos2uniprotPositions = new HashMap<>();

}
