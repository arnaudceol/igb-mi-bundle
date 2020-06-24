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
package it.iit.genomics.cru.igb.bundles.mi.business.genes;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.SupportsCdsSpan;
import com.affymetrix.genometry.parsers.CytobandParser;
import com.affymetrix.genometry.span.SeqSpanComparator;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.symmetry.SymWithProps;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.TypeContainerAnnot;
import it.iit.genomics.cru.igb.bundles.commons.business.IGBLogger;
import it.iit.genomics.cru.structures.model.MIExon;
import it.iit.genomics.cru.structures.model.MIGene;
import it.iit.genomics.cru.structures.model.sequence.TranscriptSequence;
import it.iit.genomics.cru.utils.maps.MapOfMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.exceptions.TranslationException;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.RNASequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava.nbio.core.sequence.compound.DNACompoundSet;
import org.biojava.nbio.core.sequence.compound.RNACompoundSet;
import org.biojava.nbio.core.sequence.io.IUPACParser;
import org.biojava.nbio.core.sequence.io.ProteinSequenceCreator;
import org.biojava.nbio.core.sequence.io.RNASequenceCreator;
import org.biojava.nbio.core.sequence.template.CompoundSet;
import org.biojava.nbio.core.sequence.transcription.DNAToRNATranslator;
import org.biojava.nbio.core.sequence.transcription.Frame;
import org.biojava.nbio.core.sequence.transcription.RNAToAminoAcidTranslator;
import org.biojava.nbio.core.sequence.transcription.Table;
import org.lorainelab.igb.services.IgbService;

/**
 * @author Arnaud Ceol
 *
 * Get information about genes and exons from IGB QuickLoad.
 *
 */
public class IGBQuickLoadGeneManager extends GeneManager {

    private final IGBLogger igbLogger;

    private static final HashMap<String, IGBQuickLoadGeneManager> instances = new HashMap<>();

    protected IgbService igbService;

    private final static DNACompoundSet dna = DNACompoundSet.getDNACompoundSet();
    private final static RNACompoundSet rna = RNACompoundSet.getRNACompoundSet();
    private final static AminoAcidCompoundSet aa = AminoAcidCompoundSet
            .getAminoAcidCompoundSet();

    private final static DNAToRNATranslator dnaTranslator = new DNAToRNATranslator(
            new RNASequenceCreator(rna), dna, rna, false);

    private final static Table table = IUPACParser.getInstance().getTable(new Integer(1));

    private final static CompoundSet<Table.Codon> codons = table.getCodonCompoundSet(rna, aa);
   

    private final static RNAToAminoAcidTranslator rnaTranslator = new RNAToAminoAcidTranslator(
            new ProteinSequenceCreator(aa), rna, codons, aa, table, false,
            false, false,false,false);

    protected IGBQuickLoadGeneManager(IgbService igbService, String species) {
        this.igbService = igbService;
        igbLogger = IGBLogger.getMainInstance();
    }

    public static IGBQuickLoadGeneManager getFirstInstance() {
        return instances.values().iterator().next();
    }

    public static IGBQuickLoadGeneManager getInstance(IgbService igbService, String species) {
        if (false == instances.containsKey(species)) {
            IGBQuickLoadGeneManager instance = new IGBQuickLoadGeneManager(igbService, species);
            instances.put(species, instance);
        }

        return instances.get(species);
    }

    MapOfMap<String, MIGene> genesByGeneId = new MapOfMap<>();
    MapOfMap<String, MIGene> genesBySymId = new MapOfMap<>();

    @Override
    public Collection<MIGene> getByID(String geneId) {

        if (genesByGeneId.containsKey(geneId)) {
            return genesByGeneId.get(geneId);
        }

        ArrayList<MIGene> genes = new ArrayList<>();

        Set<SeqSymmetry> symmetries = new HashSet<>();
        Pattern pattern = Pattern.compile(geneId);

        
        for (BioSeq seq : GenometryModel.getInstance().getSelectedGenomeVersion().getSeqList()) {
             seq.search(symmetries, pattern, -1);
        }
//        GenometryModel.getInstance().
//                .getSelectedSeqGroup().search(symmetries, pattern, -1);

        for (SeqSymmetry symmetry : symmetries) {
            /**
             * TODO: ensure it's the good one
             */
            if (genesBySymId.containsKey(symmetry.getID())) {
                genes.addAll(genesBySymId.get(symmetry.getID()));
            } else {

                BioSeq seq = symmetry.getSpan(0).getBioSeq();

                MIGene gene = spanToMIGene(symmetry.getID(), symmetry.getID(), symmetry.getSpan(seq));
                genes.add(gene);
                genesBySymId.add(symmetry.getID(), gene);

                getExons(seq, symmetry, gene);

            }
        }
        genesByGeneId.addAll(geneId, genes);

        return genes;
    }

    @Override
    public Collection<MIGene> getByPosition(String chromosome, int start,
            int end) {
        igbLogger.severe("not supported: getByPosition");
        return null;
    }

    /**
     * TODO: can I avoid to do it automatically?
     *
     * @param chromosomeSeq
     * @param geneSym
     * @param gene
     */
    private void getExons(BioSeq chromosomeSeq, SeqSymmetry geneSym, MIGene gene) {

        SeqSpan geneSpan;

        ArrayList<SeqSpan> exonSpans = new ArrayList<>();

        if ((geneSym instanceof SupportsCdsSpan) && ((SupportsCdsSpan) geneSym).hasCdsSpan()) {
            geneSpan = ((SupportsCdsSpan) geneSym).getCdsSpan();
        } else {
            geneSpan = geneSym.getSpan(chromosomeSeq);
        }

        int numChildren = geneSym.getChildCount();

        if (numChildren == 0) {
            // full length
            exonSpans.add(new SimpleSeqSpan(geneSpan.getStart(), geneSpan.getEnd(), chromosomeSeq));
        }

        for (int i = 0; i < numChildren; i++) {
            SeqSymmetry child = geneSym.getChild(i);

            SeqSpan exonSpan = child.getSpan(chromosomeSeq);

            // Skip if non coding 
            // remember that spans are 0-based exclusive while migene are 0-based inclusive
            if (exonSpan.getMin() > geneSpan.getMax()
                    || exonSpan.getMax() <= geneSpan.getMin()) {
                continue;
            }

            // 0-based exclusive
            int min = Math.max(exonSpan.getMin(), geneSpan.getMin());
            int max = Math.min(exonSpan.getMax(), geneSpan.getMax());

            if (exonSpan.isForward()) {
                exonSpans.add(new SimpleSeqSpan(min, max, chromosomeSeq));
            } else {
                exonSpans.add(new SimpleSeqSpan(max, min, chromosomeSeq));
            }
        }

        Collections.sort(exonSpans, new SeqSpanComparator());

        int start = 0;

        for (SeqSpan span : exonSpans) {
            int exonStart = span.isForward() ? span.getStart() : span.getStart() - 1;
            int exonEnd = span.isForward() ? span.getEnd() - 1 : span.getEnd();

            MIExon exon = new MIExon(exonStart, exonEnd);
            // how many bases started the current AA in previous exon 
            // and as a consequence how many bases in the current exon participate
            // to the previous base
            // if previous = 0 (AA finsiched) -> current =  0
            // if previous = 1 -> current =  2
            // if previous = 2 -> current =  3
            exon.setNumberOfOverlappingBases((3 - start % 3) % 3);

            int elength = span.isForward() ? span.getEnd() - span.getStart() : span.getStart() - span.getEnd();

            int protStart = (int) Math.floor(start / 3) + 1;
            int protEnd = (int) Math.ceil(((double) (start + elength)) / 3);
            exon.setProteinStart(protStart);
            exon.setProteinEnd(protEnd);

            gene.getExons().add(exon);

            start += span.getLength();
        }

        gene.setCodingStart(gene.getExons().get(0).getStart());
        gene.setCodingEnd(gene.getExons().get(gene.getExons().size() - 1).getEnd());

    }

    /**
     * TODO: can I avoid to do it automatically?
     *
     * @param chromosomeSeq
     * @param gene
     */
    public void loadTranscriptSequence(BioSeq chromosomeSeq, MIGene gene) {

        String transcriptSeq = "";

        // Create the transcript sequence
        for (MIExon exon : gene.getExons()) {
            // get residues automatically do reverse complement if necessary

            int exonStart = gene.isForward() ? exon.getStart() : exon.getStart() + 1;
            int exonEnd = gene.isForward() ? exon.getEnd() + 1 : exon.getEnd();

            igbService.loadResidues(new SimpleSeqSpan(exonStart, exonEnd, chromosomeSeq), true);
            String exonSeq = chromosomeSeq.getResidues(exonStart, exonEnd);
            transcriptSeq += exonSeq;
            exon.setSequence(exonSeq);
        }

        try {

            DNASequence dnaSeq = new DNASequence(transcriptSeq);

            RNASequence rnaSequence = (RNASequence) dnaTranslator.createSequence(
                    dnaSeq, Frame.ONE);
            if (rnaSequence == null) {
                igbLogger.getLogger().severe("RNA Sequence null, transcription error: " +  gene.getID());
                return;
            }

            ProteinSequence exonProteinSequence = ((ProteinSequence) rnaTranslator
                    .createSequence(rnaSequence));
            String exonProteinSequenceAsString = exonProteinSequence.getSequenceAsString();

            gene.setTranscriptSequence(new TranscriptSequence(exonProteinSequenceAsString));
        } catch (NullPointerException | CompoundNotFoundException | TranslationException te) {
            igbLogger.getLogger().warning("Translation error: "+ transcriptSeq);
        }

    }

    @Override
    public void loadExons(MIGene gene) {
        // Done by default	        
    }

    /**
     * SeqSpans are 0-based exclusive, MIGenes are 0-based inclusive
     *
     * @param id
     * @param name
     * @param span
     */
    private MIGene spanToMIGene(String id, String name, SeqSpan span) {
        int start;
        int end;

        if (span.isForward()) {
            start = span.getStart();
            end = span.getEnd() - 1;
        } else {
            start = span.getStart() - 1;
            end = span.getEnd();
        }

        return new MIGene(id, name, span.getBioSeq().getId(), start, end);
    }

    public MapOfMap<SeqSymmetry, MIGene> getBySymList(BioSeq chromosomeSeq, ArrayList<SeqSymmetry> syms) {

        MapOfMap<SeqSymmetry, MIGene> genes = new MapOfMap<>();

        Pattern CYTOBAND_TIER_REGEX = Pattern.compile(".*"
                + CytobandParser.CYTOBAND_TIER_NAME);

        SeqSymmetry chromosomeSym = null;

        for (int a = 0; a < chromosomeSeq.getAnnotationCount(); a++) {
            SeqSymmetry annotSym = chromosomeSeq.getAnnotation(a);
            if (annotSym instanceof TypeContainerAnnot) {
                TypeContainerAnnot tca = (TypeContainerAnnot) annotSym;
                if (false == CYTOBAND_TIER_REGEX.matcher(tca.getType())
                        .matches()) {
                    chromosomeSym = annotSym;
                    break;
                }
            }
        }

        if (chromosomeSym == null) {
            igbLogger.warning(
                    "No chromosome found for " + chromosomeSeq.getId());
            return genes;
        }

        int countChildren = chromosomeSym.getChildCount();

        for (int j = 0; j < countChildren; j++) {
            SymWithProps geneSym = (SymWithProps) chromosomeSym.getChild(j);

            SeqSpan geneSeqSpan = geneSym.getSpan(0);

            for (int idxSym = 0; idxSym < syms.size(); idxSym++) {

                int min = syms.get(idxSym).getSpan(0).getMin();
                int max = syms.get(idxSym).getSpan(0).getMax();

                if (min >= geneSeqSpan.getMax()) {
                    // remove  all the syms up to this one excluded
                    if (idxSym > 0) {
                        ArrayList<SeqSymmetry> newList = new ArrayList<>();
                        newList.addAll(syms.subList(idxSym, syms.size()));
                        syms = newList;
                    }
                    break;
                }

                if ((geneSeqSpan.getMin() >= min && geneSeqSpan.getMin() < max)
                        || (geneSeqSpan.getMax() > min && geneSeqSpan.getMax() <= max)
                        || (min >= geneSeqSpan.getMin() && min < geneSeqSpan
                        .getMax())
                        || (max > geneSeqSpan.getMin() && max <= geneSeqSpan
                        .getMax())) {

                    MIGene gene = spanToMIGene(geneSym.getID(), geneSym.getID(), geneSym.getSpan(chromosomeSeq));

                    genes.add(syms.get(idxSym), gene);

                    genesBySymId.add(geneSym.getID(), gene);

                    getExons(chromosomeSeq, geneSym, gene);
                }

            }

        }

        return genes;
    }

}
