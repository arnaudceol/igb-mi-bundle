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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import it.iit.genomics.cru.structures.model.position.TranscriptPosition;
import it.iit.genomics.cru.structures.model.position.UniprotPosition;
import it.iit.genomics.cru.structures.model.sequence.TranscriptSequence;
import it.iit.genomics.cru.structures.model.sequence.UniprotSequence;

/**
 * 0-based inclusive. It was originally exclusive, but it make it difficult to
 * browse in two directions
 *
 * @author aceol
 */
public class MIGene {
	
    static AtomicInteger nextId = new AtomicInteger();
    private final int uniqueID;

    /**
     *
     * @return
     */
    public int getUniqueID() {
        return uniqueID;
    }

    /**
     * AA positions are 1-based inclusive
     */
    protected BiMap<UniprotPosition, TranscriptPosition> uniprot2transcriptAA = HashBiMap.create();

    /**
     *
     * @param uniprot2transcriptAA
     */
    public void addUniprotToTranscriptAA(HashMap<UniprotPosition, TranscriptPosition> uniprot2transcriptAA) {
        this.uniprot2transcriptAA.putAll(uniprot2transcriptAA);
    }

    /**
     *
     * @param proteinPosition
     * @return
     */
    public TranscriptPosition getTranscriptAA(UniprotPosition proteinPosition) {
        return uniprot2transcriptAA.get(proteinPosition);
    }

    private static final Logger logger = LoggerFactory.getLogger(MIGene.class.getName());

    private final String id;

    private final String name;

    private final String chromosomeName;

    private final int start;

    private final int end;

    private final boolean forward;

    private final HashSet<String> uniprotAcs = new HashSet<>();

//    private final HashSet<MoleculeEntry> proteins = new HashSet<>();
    private MoleculeEntry protein = null;

    // Order is important!
    private final ArrayList<MIExon> exons = new ArrayList<>();

    private int codingStart;

    /**
     * 0-based inclusive
     *
     * @param codingStart
     * @return
     */
    public void setCodingStart(int codingStart) {
        this.codingStart = codingStart;
    }

    /**
     * 0-based inclusive
     *
     * @param codingEnd
     */
    public void setCodingEnd(int codingEnd) {
        this.codingEnd = codingEnd;
    }

    private int codingEnd;

    /**
     * 0-based inclusive
     *
     * @return
     */
    public int getCodingStart() {
        return codingStart;
    }

    /**
     * 0-based inclusive
     *
     * @return
     */
    public int getCodingEnd() {
        return codingEnd;
    }

    /**
     * Translated from the transcript
     */
    private TranscriptSequence transcriptSequence;

    /**
     *
     * @return
     */
    public TranscriptSequence getTranscriptSequence() {
        return transcriptSequence;
    }

    /**
     *
     * @param transcriptSequence
     */
    public void setTranscriptSequence(TranscriptSequence transcriptSequence) {
        this.transcriptSequence = transcriptSequence;
    }

    /**
     * Uniprot sequence of this transcript, automatically loaded from the
     * uniprot entry
     */
    private UniprotSequence uniprotSequence;

    /**
     *
     * @return
     */
    public UniprotSequence getUniprotSequence() {
        return uniprotSequence;
    }

    /**
     *
     * @param id
     * @param name
     * @param chromosomeName
     * @param start
     * @param end
     * @param isForward
     */
    public MIGene(String id, String name, String chromosomeName,
            int start, int end, boolean isForward) {
        super();
        uniqueID = nextId.incrementAndGet();
        this.id = id;
        this.name = name;
        this.chromosomeName = chromosomeName;
        this.start = start;
        this.end = end;
        this.forward = isForward;
    }

    /**
     *
     * @param id
     * @param name
     * @param chromosomeName
     * @param start
     * @param end
     */
    public MIGene(String id, String name, String chromosomeName,
            int start, int end) {
        super();

        uniqueID = nextId.incrementAndGet();
        this.id = id;
        this.name = name;
        this.chromosomeName = chromosomeName;
        this.start = start;
        this.end = end;
        this.forward = start <= end;
    }

    /**
     *
     * @return
     */
    public String getID() {
        return id;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public String getChromosomeName() {
        return chromosomeName;
    }

    /**
     *
     * @return
     */
    public int getStart() {
        return start;
    }

    /**
     *
     * @return
     */
    public int getEnd() {
        return end;
    }

    /**
     *
     * @return
     */
    public int getMin() {
        if (start < end) {
            return start;
        }

        return end;
    }

    /**
     *
     * @return
     */
    public int getMax() {
        if (start > end) {
            return start;
        }

        return end;
    }

    /**
     *
     * @return
     */
    public boolean isForward() {
        return forward;
    }

    /**
     * Exons are ordered, following the gene strand
     *
     * @return
     */
    public List<MIExon> getExons() {
        return exons;
    }

    /**
     *
     * @return
     */
    public HashSet<String> getUniprotAcs() {
        return uniprotAcs;
    }

//    public Iterable<MoleculeEntry> getProteins() {
//        return proteins;
//    }
    /**
     *
     * @return
     */
    public MoleculeEntry getProtein() {
        return protein;
    }

    /**
     *
     * @param protein
     */
    public void setProtein(MoleculeEntry protein) {
        if (this.protein != null) {
//            logger.log(Level.WARNING, "Gene {0} is already associated to protein {1}. Skip assignment to protein {2}", new Object[]{this.getID(), this.protein.getUniprotAc(), protein.getUniprotAc()});
        } else {
            this.protein = protein;
            this.uniprotSequence = protein.getSequence(this.getID());
        }
    }

    /**
     *
     * @param genomicPosition
     * @return
     */
    public UniprotPosition getUniprotAAPosition(int genomicPosition) {

        TranscriptPosition transcriptAAPosition = getTranscriptAAPosition(genomicPosition);
        if (transcriptAAPosition != null) {
            return uniprot2transcriptAA.inverse().get(transcriptAAPosition);
        }

        return null;
    }

    /**
     *
     * @param genomicPosition
     * @return
     */
    public TranscriptPosition getTranscriptAAPosition(int genomicPosition) {

        for (MIExon exon : exons) {
            if (exon.getMin() <= genomicPosition && genomicPosition <= exon.getMax()) {

                // 1-based
                int proteinPos = exon.getProteinStart();

                if (isForward()) {
                    int exonCursor = exon.getStart();
                    if (exon.getNumberOfOverlappingBases() > 0 && genomicPosition - exon.getMin() <= exon.getNumberOfOverlappingBases()) {
                        return new TranscriptPosition(proteinPos);
                    } else {
                        if (exon.getNumberOfOverlappingBases() > 0) {
                            exonCursor += exon.getNumberOfOverlappingBases();
                            proteinPos++;
                        }
                    }
                    proteinPos += Math.floor(((double) genomicPosition - exonCursor) / 3);
                    return new TranscriptPosition(proteinPos);
                } else {
                    int exonCursor = exon.getStart();

                    if (exon.getNumberOfOverlappingBases() > 0 && exon.getMax() - genomicPosition <= exon.getNumberOfOverlappingBases()) {
                        return new TranscriptPosition(proteinPos);
                    } else {
                        if (exon.getNumberOfOverlappingBases() > 0) {
                            exonCursor -= exon.getNumberOfOverlappingBases();
                            proteinPos++;
                        }
                    }
                    int prev = proteinPos;
                    proteinPos += Math.floor(((double) exonCursor - genomicPosition) / 3);

                    double u = ((double) exonCursor - genomicPosition) / 3;

                    return new TranscriptPosition(proteinPos);
                }
            }
        }
        return null;
    }

    /**
     *
     * @param position
     * @return
     */
    public Collection<String> getGenomePositions(UniprotPosition position) {
        TranscriptPosition transcriptAAPosition = getTranscriptAA(position);
        if (transcriptAAPosition == null) {
            return new ArrayList<>();
        }
        return getGenomePositions(transcriptAAPosition);
    }

    /**
     * Associate a position in the translated sequence to the genome. The full
     * length is always 3, but it may overlap more than one genomics position
     * (e.g. posEnd and posStart of two exons) Protein position is 1 based.
     *
     * @param transcriptAAPosition
     * @return
     */
    public Collection<String> getGenomePositions(TranscriptPosition transcriptAAPosition) {
        ArrayList<String> positions = new ArrayList<>();

        if (transcriptAAPosition == null) {
            logger.error("Null transcript position: {0}, it may be due to a network problem when downloading residues.", this.getName());
            return positions;
        }

        int baseStart = (transcriptAAPosition.getPosition() - 1) * 3;

        int curPos = 0;

        int curPositionLength = 0;

        for (MIExon exon : exons) {
            if (curPos + exon.getLength() - 1 < baseStart) {
                curPos += exon.getLength();
            } else {

                if (isForward()) {
                    // If we are not in the middle of the 3 residues encoding 
                    // the amino acid, skip the first residues
                    int frameShift = 0;

                    int posStart = exon.getStart() + frameShift + (baseStart - curPos);

                    int posEnd = Math.min(posStart + (2 - curPositionLength), exon.getEnd());

                    positions.add(this.getChromosomeName() + ":" + posStart + "-" + posEnd);

                    curPositionLength += posEnd - posStart + 1;
                    if (curPositionLength == 3) {
                        break;
                    }
                    if (curPositionLength > 3) {
                        logger.warn("Pos: {0}:{1}-{2}", new Object[]{this.getChromosomeName(), posStart, posEnd});
                        logger.warn("Pos length = {0} > 3", curPositionLength);
                        break;
                    }

                } else {
                    // If we are not in the middle of the 3 residues encoding 
                    // the amino acid, skip the first residues
                    int frameShift = 0;

                    int posStart = exon.getMax() - frameShift - (baseStart - curPos);

                    int posEnd = Math.max(posStart - (2 - curPositionLength), exon.getEnd());
                    positions.add(this.getChromosomeName() + ":" + posStart + "-" + posEnd);
                    curPositionLength += posStart - posEnd + 1;

                    if (curPositionLength == 3) {
                        break;
                    }
                    if (curPositionLength > 3) {
                        logger.warn("Pos: {0}:{1}-{2}", new Object[]{this.getChromosomeName(), posStart, posEnd});
                        logger.warn("Pos length = {0} > 3", curPositionLength);
                        break;
                    }

                }

            }
        }

        return positions;
    }

    @Override
    public String toString() {
        return "Gene[ID=" + id
                + ", name=" + name
                + ", chromosomeName=" + chromosomeName
                + ", start=" + start
                + ", end=" + end
                + ", reverse=" + forward;
    }

}
