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
package it.iit.genomics.cru.structures.alignment;


import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.SubstitutionMatrixHelper;
import org.biojava.nbio.alignment.template.GapPenalty;
import org.biojava.nbio.alignment.template.PairwiseSequenceAligner;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Arnaud Ceol
 */
public class SmithWatermanBioJava {

    
    static final Logger logger = LoggerFactory.getLogger(SmithWatermanBioJava.class);
    
    /**
     *
     */
    protected int startAlignmentA;

    /**
     *
     */
    protected int startAlignmentB;

    /**
     *
     */
    protected String alignmentSeqA;

    /**
     *
     */
    protected String alignmentSeqB;

    /**
     *
     * @param query
     * @param target
     */
    public SmithWatermanBioJava(String query, String target) {

        try {
            ProteinSequence querySequence = new ProteinSequence(query);

            ProteinSequence targetSequence = new ProteinSequence(target);

            GapPenalty g = new SimpleGapPenalty();
//        g.setOpenPenalty(1);
//        g.setExtensionPenalty(1);

            PairwiseSequenceAligner<ProteinSequence, AminoAcidCompound> profile = Alignments
                    .getPairwiseAligner(querySequence, targetSequence, Alignments.PairwiseSequenceAlignerType.LOCAL,
                            g,
                            SubstitutionMatrixHelper.getBlosum50());

            startAlignmentA = profile.getPair().getQuery()
                    .getSequenceIndexAt(1) - 1;

            startAlignmentB = profile.getPair().getTarget()
                    .getSequenceIndexAt(1) - 1;

            alignmentSeqA = "";
//        profile.getPair().getAlignedSequence(1)
//                .getSequenceAsString();

            alignmentSeqB = "";
//        profile.getPair().getAlignedSequence(2)
//                .getSequenceAsString();

            int queryIdx = 0;
            int targetIdx = 0;

            while (queryIdx < startAlignmentA) {
                alignmentSeqA += query.charAt(queryIdx);
                alignmentSeqB += "-";
                queryIdx++;
            }

            while (targetIdx < startAlignmentB) {
                alignmentSeqA =  alignmentSeqA + "-";
                alignmentSeqB += target.charAt(targetIdx);
                targetIdx++;
            }
            
            
            String alignmentA = profile.getProfile().getAlignedSequence(querySequence).getSequenceAsString();

            for (int i = 0; i < alignmentA.length(); i++) {
                char c = alignmentA.charAt(i);
                if (c != '-') {
                    queryIdx++;
                }
                alignmentSeqA += alignmentA.charAt(i);
            }

//        alignmentSeqA +=  profile.getProfile().getAlignedSequence(querySequence).getSequenceAsString();
//        queryIdx += alignmentSeqA.length();


            String alignmentB = profile.getProfile().getAlignedSequence(targetSequence).getSequenceAsString();

            for (int i = 0; i < alignmentB.length(); i++) {
                char c = alignmentB.charAt(i);
                if (c != '-') {
                    targetIdx++;
                }
                alignmentSeqB += alignmentB.charAt(i);
            }

//        alignmentSeqB +=  profile.getProfile().getAlignedSequence(targetSequence).getSequenceAsString();
            while (queryIdx < query.length()) {
                alignmentSeqA += query.charAt(queryIdx);
                alignmentSeqB += "-";
                queryIdx++;
            }

            while (targetIdx < target.length()) {
                alignmentSeqA += "-";
                alignmentSeqB += target.charAt(targetIdx);
                targetIdx++;
            }

//        SequencePair<S, C> s  = profile.getPair();
//        
            System.out.println("A: " + alignmentA);
            System.out.println("B: " + alignmentB);
            System.out.println("A: " + getAlignmentSeqA());
            System.out.println("B: " + getAlignmentSeqB());
            System.out.println("symilartity: " + profile.getSimilarity());
            System.out.println("score: " + profile.getScore());
//         System.out.println("score: " + profile.getPair()..getNumIdenticals());

        } catch (CompoundNotFoundException ex) {
            logger.error( null, ex);
        }
    }

    /**
     *
     * @return
     */
    public int getStartAlignmentA() {
        return startAlignmentA;
    }

    /**
     *
     * @return
     */
    public int getStartAlignmentB() {
        return startAlignmentB;
    }

    /**
     *
     * @return
     */
    public String getAlignmentSeqA() {
        return alignmentSeqA;
    }

    /**
     *
     * @return
     */
    public String getAlignmentSeqB() {
        return alignmentSeqB;
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
          String query = "MVRSRQMCNTNMSVPTDGAVTTSQIPASEQETLVRPKPLLLKLLKSVGAQKDTYTMKEVLFYLGQYIMTKRLYDEKQQHIVYCSNDLLGDLFGVPSFSVKEHRKIYTMIYRNLVVVNQQESSDSGTSVSENRCHLEGGSDQKDLVQELQEEKPSSSHLVSRPSTSSRRRAISETEENSDELSGERQRKRHKSDSISLSFDESLALCVIREICCERSSSSESTGTPSNPDLDAGVSEHSGDWLDQDSVSDQFSVEFEVESLDSEDYSLSEEGQELSDEDDEVYQVTVYQAGESDTDSFEEDPEISLADYWKCTSCNEMNPPLPSHCNRCWALRENWLPEDKGKDKGEISEKAKLENSTQAEEGFDVPDCKKTIVNDSRESCVEENDDKITQASQSQESEDYSQPSTSSSIIYSSQEDVKEFEREETQDKEESVESSLPLNAIEPCVICQGRPKNGCIVHGKTGHLMACFTCAKKLKKRNKPCPVCRQPIQMIVLTYFP";
          String target = "TSWRSEATFQFTVERFSRLSESVLSPPCFVRNLPWKIMVMPRFKSVGFFLQCNAESDSTSWSCHAQAVLKIINYRDDEKSFSRRISHLFFHKENDWGFSNFMAWSEVTDPEKGFIDDDKVTFEVFVQADLDAGVSE";
//        String query = "YTTGCD";
//        String target = "TTGACD";
//        SmithWaterman sm = new SmithWaterman( "YTTGCD", "TTGACD");
        SmithWatermanBioJava sm = new SmithWatermanBioJava(query, target);

        System.out.println(".A: " + sm.getAlignmentSeqA());
        System.out.println(".B: " + sm.getAlignmentSeqB());
        System.out.println("A: " + sm.getStartAlignmentA());
        System.out.println("B: " + sm.getStartAlignmentB());

    }

}
