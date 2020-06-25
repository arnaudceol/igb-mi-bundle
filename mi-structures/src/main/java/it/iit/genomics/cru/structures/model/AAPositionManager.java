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

import it.iit.genomics.cru.structures.alignment.SmithWaterman;
import it.iit.genomics.cru.structures.model.position.TranscriptPosition;
import it.iit.genomics.cru.structures.model.position.UniprotPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Arnaud Ceol
 *
 * Calculate and manage the mappings between genomic region of a SeqSymmetry and
 * amino acid position of protein sequences. A different manager is assigned to
 * each new Query.
 *
 */
public class AAPositionManager {

    private static final Logger logger = LoggerFactory.getLogger(AAPositionManager.class);

    private static final boolean debug = false;

    private final HashMap<String, AAPosition> aaPositions = new HashMap<>();

    private static final HashMap<String, AAPositionManager> managers = new HashMap<>();

    private AAPositionManager() {

    }

    /**
     *
     * @param queryId
     * @return
     */
    public static AAPositionManager getAAPositionManager(String queryId) {
        if (false == managers.containsKey(queryId)) {
            managers.put(queryId, new AAPositionManager());
        }

        return managers.get(queryId);
    }

    /**
     *
     * @param queryId
     */
    public static void removeManager(String queryId) {
        managers.remove(queryId);
    }

    /**
     *
     * @param uniprotStart
     * @param uniprotEnd
     * @param genomeMin
     * @param genomeMax
     * @param gene
     * @return
     */
        public AAPosition getAAPosition(int uniprotStart, int uniprotEnd, int genomeMin, int genomeMax, MIGene gene) { //String chromosome, int genomeMin, int genomeMax, boolean reverse) {
        String key = uniprotStart + "#" + uniprotEnd + "#" + gene.toString(); //chromosome + "#" + genomeMin + "#" + genomeMax;
        if (false == aaPositions.containsKey(key)) {
            AAPosition position = new AAPosition(uniprotStart, uniprotEnd, genomeMin, genomeMax, gene); //chromosome, genomeMin, genomeMax, reverse);//chromosome + ":" +  genomeMin+ "-" +  genomeMax
            aaPositions.put(key, position);
        }

        return aaPositions.get(key);
    }

    /**
     * Map the translated sequence to the protein sequence in uniprot. The
     * result AA positions are 1-based inclusive.
     *
     * @param gene
     * @param minPos 0-based inclusve
     * @param maxPos 0-based inclusve
     * @return
     */
    public Collection<AAPosition> getAAPositions(MIGene gene, int minPos, int maxPos) {
        ArrayList<AAPosition> aaPositionsLocal = new ArrayList<>();

        Range targetRange = new Range(minPos, maxPos);
        Range geneRange;

        if (gene.isForward()) {
            geneRange = new Range(gene.getCodingStart(), gene.getCodingEnd());
        } else {
            geneRange = new Range(gene.getCodingEnd(), gene.getCodingStart());
        }

        if (false == targetRange.intersects(geneRange)) {
            return aaPositionsLocal;
        }

        /* 1-based inclusive */
        if (minPos < Math.min(gene.getCodingStart(), gene.getCodingEnd())) {
            minPos = Math.min(gene.getCodingStart(), gene.getCodingEnd());
        }

        if (maxPos > Math.max(gene.getCodingStart(), gene.getCodingEnd())) {
            maxPos = Math.max(gene.getCodingStart(), gene.getCodingEnd());
        }
        
        // Should be 1-based
        // warning, genomic ranges are 1-based exclusive: do not translate the max
        TranscriptPosition ts = gene.getTranscriptAAPosition(gene.isForward() ? minPos : maxPos);
        TranscriptPosition te = gene.getTranscriptAAPosition(gene.isForward() ? maxPos : minPos);

        if (ts == null) {
            if (gene.isForward()) {
                // first exon start > minPos
                for (MIExon exon : gene.getExons()) {
                    if (minPos <= exon.getStart()) {
                        minPos = exon.getStart();
                        ts = new TranscriptPosition(exon.getProteinStart());
                        //            logger.info("--- TS from " + minPos + " to " + ts);
                        break;
                    }
                }
            } else {
                // first exon start > minPos
                for (MIExon exon : gene.getExons()) {
                    if (maxPos >= exon.getEnd()) {
                        maxPos = exon.getEnd();
                        ts = new TranscriptPosition(exon.getProteinStart());
                        break;
                    }
                }
            }
        }

        if (te == null) {
            if (gene.isForward()) {
                int lastIndexGene = -1;
                int lastIndexAA = -1;
                for (MIExon exon : gene.getExons()) {
                    if (maxPos >= exon.getStart()) {
                        maxPos = lastIndexGene;
                        te = new TranscriptPosition(lastIndexAA);
                        break;
                    }
                    lastIndexGene = exon.getEnd();
                    lastIndexAA = exon.getProteinEnd();
                }
            } else {
                for (MIExon exon : gene.getExons()) {
                    if (minPos < exon.getEnd()) {
                        minPos = exon.getEnd();
                        te = new TranscriptPosition(exon.getProteinEnd());
                        break;
                    }
                }
            }
        }

        if (ts == null || te == null) {
            logger.error( "Null transcript position: {0}/{1}, {2} {3}-{4}", new Object[]{ts, te, gene.getID(), minPos, maxPos});
            return aaPositionsLocal;
        }

        // 1-based to 0-based
        int regionTranscriptStart = ts.getPosition() - 1;
        int regionTranscriptEnd = te.getPosition() - 1;

        if (null == gene.getTranscriptSequence()) {
            logger.error("No transcript sequence for " + gene.getID());
            return aaPositionsLocal;
        }
        String transcriptSequence = gene.getTranscriptSequence().getSequence();
        if (transcriptSequence.startsWith("*")) {
            transcriptSequence = transcriptSequence.substring(1);
            if (regionTranscriptStart == 0) {
                regionTranscriptStart++;
            }
        }

        if (transcriptSequence.endsWith("*")) {
            transcriptSequence = transcriptSequence.substring(0, transcriptSequence.length());
            // It should not change anything for the end index, unless we were looking for the last character
            if (regionTranscriptEnd == transcriptSequence.length() - 1) {
                regionTranscriptEnd--;
            }
        }
        try {

            SmithWaterman nw = new SmithWaterman(transcriptSequence, gene.getUniprotSequence().getSequence());
            if (regionTranscriptStart < 0 || regionTranscriptEnd < 0) {
                return aaPositionsLocal;
            }

            int transcriptCursor = nw.getStartAlignmentA();
            int proteinCursor = nw.getStartAlignmentB();

            Integer startAA = null;
            Integer endAA = null;

            if (regionTranscriptEnd < transcriptCursor) {
                return aaPositionsLocal;
            }

            HashMap<UniprotPosition, TranscriptPosition> uniprot2transcriptAA = new HashMap<>();
            for (int i = 0; i < nw.getAlignmentSeqA().length(); i++) {
                char transcriptAA = nw.getAlignmentSeqA().charAt(i);
                char proteinAA = nw.getAlignmentSeqB().charAt(i);
                if (transcriptCursor >= regionTranscriptStart && startAA == null) {
                    startAA = proteinCursor + 1;
                    if (proteinAA == '-') {
                        logger.warn("Strange, AA aligned to '-'");
                    }
                }

                if (transcriptCursor == regionTranscriptEnd && endAA == null) {
                    endAA = proteinCursor + 1;
                    if (proteinAA == '-') {
                        logger.warn("Strange, AA aligned to '-'");
                    }
                }

                if (proteinAA != '-' && transcriptAA != '-') {
                    uniprot2transcriptAA.put(new UniprotPosition(proteinCursor + 1), new TranscriptPosition(transcriptCursor + 1));
                }

                if (endAA != null) {
                    break;
                }
                if ('-' != transcriptAA) {
                    transcriptCursor++;
                }

                if ('-' != proteinAA) {
                    proteinCursor++;
                }

            }

            /**
             * TODO: Any reason why it should not be rejected? It is out the
             * loop
             */
            if (transcriptCursor == regionTranscriptStart && startAA == null) {
                startAA = proteinCursor + 1;
            }

            if (transcriptCursor == regionTranscriptEnd) {
                endAA = proteinCursor + 1;
            }

            if (startAA != null) {

                /**
                 * TODO: Any reason why it should not be rejected?
                 */
                if (endAA == null) {
                    endAA = proteinCursor + 1;
                }

                /**
                 * TODO: add description
                 */
                AAPosition position
                        = getAAPosition(startAA, endAA, minPos, maxPos, gene);
                gene.addUniprotToTranscriptAA(uniprot2transcriptAA);
                aaPositionsLocal.add(position);
            }
        } catch (Exception e) {
            logger.error( gene.getID() + ", " + gene.getProtein() + ", " + transcriptSequence + ", " + gene.getUniprotSequence(), e);
        }
        return aaPositionsLocal;
    }

    /**
     *
     * @param chromosome
     * @param start
     * @param end
     * @param id
     * @return
     */
    public static String getSymmetrySummary(String chromosome, int start, int end, String id) {
        String summary;

        if (start != end) {
            summary = chromosome + ":" + start + "-" + end;
        } else {
            summary = chromosome + ":" + start;
        }

        if (id != null) {
            summary += " (" + id + ")";
        }

        return summary;

    }

}
