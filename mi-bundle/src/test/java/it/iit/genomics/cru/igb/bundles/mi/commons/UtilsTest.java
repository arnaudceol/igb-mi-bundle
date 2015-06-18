/**
 * *****************************************************************************
 * Copyright 2014 Fondazione Istituto Italiano di Tecnologia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *****************************************************************************
 */
package it.iit.genomics.cru.igb.bundles.mi.commons;

import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.symmetry.impl.SimpleMutableSeqSymmetry;
import it.iit.genomics.cru.bridges.ensembl.EnsemblClient;
import it.iit.genomics.cru.bridges.ensembl.EnsemblClientManager;
import it.iit.genomics.cru.bridges.ensembl.model.Exon;
import it.iit.genomics.cru.bridges.ensembl.model.Gene;
import it.iit.genomics.cru.structures.model.MoleculeEntry;
import it.iit.genomics.cru.structures.bridges.uniprot.UniprotkbUtils;
import it.iit.genomics.cru.structures.model.MIExon;

import java.util.ArrayList;
import java.util.Collection;


public class UtilsTest {

    
    public void testGetAASequence() {

        try {
            int[] positions = {20971132, 20971019, 20975547, 20966445,
                20972133, 20971042, 20971144, 20964597, 20975602, 20975527,
                20975070};
            String chromosome = "1";

            EnsemblClient ensembl = EnsemblClientManager.getInstance()
                    .getClient("Homo sapiens");

            ArrayList<String> uniprotAcs = new ArrayList<String>();
            uniprotAcs.add("Q9BXM7");
            MoleculeEntry protein = UniprotkbUtils.getInstance("9606")
                    .getUniprotEntriesFromUniprotAccessions(uniprotAcs)
                    .values().iterator().next();

            for (int position : positions) {
                Collection<Gene> genes = ensembl.getGenesByPosition(chromosome,
                        position, position + 1);

				// String dnaSequence = "GA AAA AGT AAC TTT TTT TATG";
				// String aaSequence = Utils.getAAPositions(exon, symmetry,
                // proteinSequence);
                // Get exons
                for (Gene gene : genes) {
                    String sequence = ensembl.getSequence(gene
                            .getEnsemblGeneID());

                    Collection<Exon> exons = ensembl.getExons(gene
                            .getEnsemblGeneID());
//					System.out.println("gene: " + gene.getEnsemblGeneID()
//							+ ", " + sequence.length());
//					System.out.println("> " + sequence.substring(0, 30) + "..."
//							+ sequence.substring(sequence.length() - 30));
//					System.out.println("< "
//							+ new StringBuffer(sequence.substring(0, 30))
//									.reverse().toString()
//							+ "..."
//							+ new StringBuffer(sequence.substring(sequence
//									.length() - 30)).reverse().toString());

                    for (Exon exon : exons) {

                        // AGCCCCGCAGAGGA
                        int relativeMin;
                        int relativeMax;

                        if (gene.isReverseStrand()) {
                            // int length = gene.getMax() - gene.getMin();
                            relativeMin = gene.getMax() - exon.getMax();
                            relativeMax = gene.getMax() - exon.getMin();
                        } else {
                            relativeMin = exon.getMin() - gene.getMin();
                            relativeMax = exon.getMax() - gene.getMin();
                        }

						// if (gene.isReverseStrand()) {
                        // String exonSequence = sequence.substring(relativeMin,
                        // relativeMax);
                        // // System.out.println("rteverse: " +exonSequence );
                        // DNASequence c = new DNASequence(exonSequence);
                        // exon.setSequence(c.getComplement().getSequenceAsString());
                        // } else {
                        exon.setSequence(sequence.substring(relativeMin,
                                relativeMax));
						// }

						// System.out.println("exon: " + exon.getSequence());
                        SeqSpan span = new SimpleSeqSpan(position,
                                position + 1, null);
                        SimpleMutableSeqSymmetry symmetry = new SimpleMutableSeqSymmetry();
                        symmetry.addSpan(span);

                        int symMin = span.getMin();
                        int symMax = span.getMax();

                        if ((exon.getMin() >= symMin && exon.getMin() <= symMax)
                                || (exon.getMax() >= symMin && exon.getMax() <= symMax)
                                || (symMin >= exon.getMin() && symMin <= exon
                                .getMax())
                                || (symMax >= exon.getMin() && symMax <= exon
                                .getMin())) {
//							System.out
//									.println("gene: " + gene.getMin() + "-"
//											+ gene.getMax() + ", exon: "
//											+ exon.getMin() + "-"
//											+ exon.getMax() + " ("
//											+ exon.getSequence().length()
//											+ "), " + relativeMin + " - "
//											+ relativeMax);
//							System.out.println("exon: " + exon.getSequence());

                            MIExon miExon = new MIExon(exon.getStart(),
                                    exon.getEnd());
                            miExon.setSequence(exon.getSequence());

//							for (AAPosition aa : AAPositionManager.getAAPositionManager("test").getAAPositions(chromosome, miExon,
//									symmetry.getSpan(0).getMin(), symmetry.getSpan(0).getMax(), protein.getSequence(),
//									gene.isReverseStrand())) {
////								System.out.println(position + ": AA: "
////										+ aa.getSequence(protein.getSequence())
////										+ ": " + aa.getStart() + " - "
////										+ aa.getEnd());
//								// int start = 1094;
//								// System.out.println("AA: " +
//								// protein.getSequence().substring(start -1,
//								// start+2)+ ": "+ start+ " - "
//								// + start);
//
//							}
                        }
                    }

                }
            }
			//
            // // reverse
            //
            // dnaSequence = "acttcg";
            //
            // aaSequence = Utils.getAASequence(dnaSequence, true);
            //
            // Assert.assertEquals(aaSequence, "TVDGPRE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
