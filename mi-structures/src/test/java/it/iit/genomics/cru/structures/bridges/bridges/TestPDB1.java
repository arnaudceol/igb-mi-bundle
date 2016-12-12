/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.iit.genomics.cru.structures.bridges.bridges;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.template.PairwiseSequenceAligner;
import org.biojava.nbio.core.alignment.matrices.SimpleSubstitutionMatrix;
import org.biojava.nbio.core.alignment.template.Profile;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author aceol
 */
public class TestPDB1 {


    public TestPDB1() {
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

    @Test
    public void testAlignment() throws CompoundNotFoundException  {
        //P31751
             ProteinSequence s1 = new ProteinSequence("YAAAYA");
        ProteinSequence s2 = new ProteinSequence("YAAAA");

        
 //       SmithWaterman aligner = new SmithWaterman(s1, s2, new SimpleGapPenalty(), new SimpleSubstitutionMatrix<AminoAcidCompound>());
        PairwiseSequenceAligner<ProteinSequence, AminoAcidCompound> aligner = Alignments
                .getPairwiseAligner(s1, s2, PairwiseSequenceAlignerType.GLOBAL,
                        new SimpleGapPenalty(),
                        SimpleSubstitutionMatrix.getBlosum62());

//        aligner.setQuery(s1);
//        aligner.setTarget(s2);
        aligner.getPair();
        System.out.println(aligner.getComputationTime());
        Profile profile = aligner.getProfile();
        
        int startAAPosition = profile.getAlignedSequence(s2).getAlignmentIndexAt(1);

        String uniprotChainSequence = profile.getAlignedSequence(s1)
                .getSequenceAsString();

        // The alignment is not necessarily perfect: correct the starting group.
        int startAlignmentPosition =  profile.getAlignedSequence(s1).getAlignmentIndexAt(1) -1;
        //.getPair().getAlignedSequences()
          //     .get(0).getSequenceIndexAt(1) - 1;

    }
}
