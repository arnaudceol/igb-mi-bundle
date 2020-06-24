package it.iit.genomics.cru.structures.alignment;
// source: http://www.itu.dk/people/sestoft/bsa/Match2.java
// Implementation of some algorithms for pairwise alignment from
// Durbin et al: Biological Sequence Analysis, CUP 1998, chapter 2.
// Peter Sestoft, sestoft@itu.dk 1999-09-25, 2003-04-20 version 1.4
// Reference:  http://www.itu.dk/people/sestoft/bsa.html
// License: Anybody can use this code for any purpose, including
// teaching, research, and commercial purposes, provided proper
// reference is made to its origin.  Neither the author nor the Royal
// Veterinary and Agricultural University, Copenhagen, Denmark, can
// take any responsibility for the consequences of using this code.
// Compile with:
//      javac SmithWatermanItuDK.java
// Run with:
//      java SmithWatermanItuDK HEAGAWGHEE PAWHEAE
//  Class hierarchies
//  -----------------
//  Align                   general pairwise alignment
//     AlignSimple          alignment with simple gap costs
//        NW                global alignment with simple gap costs
//        SW                local alignment with simple gap costs
//        RM                repeated matches with simple gap costs
//        OM                overlap matches with simple gap costs 
//     AlignAffine          alignment with affine gap costs (FSA model)
//        NWAffine          global alignment with affine gap costs
//     AlignSmart           alignment using smart linear-space algorithm
//        NWSmart           global alignment using linear space
//        SWSmart           local alignment using linear space
//     AlignSmartAffine     alignment w affine gap costs in linear space
//        SWSmartAffine     local alignment w affine gap costs in linear space
//  Traceback               traceback pointers
//     Traceback2           traceback for simple gap costs
//     Traceback3           traceback for affine gap costs
//  Substitution            substitution matrices with fast lookup
//     Blosum50             the BLOSUM50 substitution matrix
//  Output                  general text output
//     SystemOut            output to the console (in the application)
//     TextAreaOut          output to a TextArea (in the applet)
// Notational conventions: 
//   i in {0..n} indexes columns and sequence seq1
//   j in {0..m} indexes rows    and sequence seq2
//   k in {0..2} indexes states (in affine alignment)
// The class of substitution (scoring) matrices

abstract class Substitution {

    public int[][] score;

    void buildscore(String residues, int[][] residuescores) {
        // Allow lowercase and uppercase residues (ASCII code <= 127):
        score = new int[127][127];
        for (int i = 0; i < residues.length(); i++) {
            char res1 = residues.charAt(i);
            for (int j = 0; j <= i; j++) {
                char res2 = residues.charAt(j);
                score[res1][res2] = score[res2][res1]
                        = score[res1][res2 + 32] = score[res2 + 32][res1]
                        = score[res1 + 32][res2] = score[res2][res1 + 32]
                        = score[res1 + 32][res2 + 32] = score[res2 + 32][res1 + 32]
                        = residuescores[i][j];
            }
        }
    }

    abstract public String getResidues();
}

// The BLOSUM50 substitution matrix for amino acids (Durbin et al, p 16)
class Blosum50 extends Substitution {

    private final String residues = "ARNDCQEGHILKMFPSTWYV";

    @Override
    public String getResidues() {
        return residues;
    }

    private final int[][] residuescores
            = /* A  R  N  D  C  Q  E  G  H  I  L  K  M  F  P  S  T  W  Y  V */ { /* A */{5},
                /* R */ {-2, 7},
                /* N */ {-1, -1, 7},
                /* D */ {-2, -2, 2, 8},
                /* C */ {-1, -4, -2, -4, 13},
                /* Q */ {-1, 1, 0, 0, -3, 7},
                /* E */ {-1, 0, 0, 2, -3, 2, 6},
                /* G */ {0, -3, 0, -1, -3, -2, -3, 8},
                /* H */ {-2, 0, 1, -1, -3, 1, 0, -2, 10},
                /* I */ {-1, -4, -3, -4, -2, -3, -4, -4, -4, 5},
                /* L */ {-2, -3, -4, -4, -2, -2, -3, -4, -3, 2, 5},
                /* K */ {-1, 3, 0, -1, -3, 2, 1, -2, 0, -3, -3, 6},
                /* M */ {-1, -2, -2, -4, -2, 0, -2, -3, -1, 2, 3, -2, 7},
                /* F */ {-3, -3, -4, -5, -2, -4, -3, -4, -1, 0, 1, -4, 0, 8},
                /* P */ {-1, -3, -2, -1, -4, -1, -1, -2, -2, -3, -4, -1, -3, -4, 10},
                /* S */ {1, -1, 1, 0, -1, 0, -1, 0, -1, -3, -3, 0, -2, -3, -1, 5},
                /* T */ {0, -1, 0, -1, -1, -1, -1, -2, -2, -1, -1, -1, -1, -2, -1, 2, 5},
                /* W */ {-3, -3, -4, -5, -5, -1, -3, -3, -3, -3, -2, -3, -1, 1, -4, -4, -3, 15},
                /* Y */ {-2, -1, -2, -3, -3, -1, -2, -3, 2, -1, -1, -2, 0, 4, -3, -2, -2, 2, 8},
                /* V */ {0, -3, -3, -4, -1, -3, -3, -4, -4, 4, 1, -3, 1, -1, -3, -2, 0, -3, -1, 5}
            /* A  R  N  D  C  Q  E  G  H  I  L  K  M  F  P  S  T  W  Y  V */
            };

    public Blosum50() {
        buildscore(residues, residuescores);
    }
}

// Pairwise sequence alignment 
abstract class Align {

    Substitution sub;             // substitution matrix
    int d;                        // gap cost
    String seq1, seq2;            // the sequences
    int n, m;                     // their lengths
    Traceback B0;                 // the starting point of the traceback

    final static int NegInf = Integer.MIN_VALUE / 2; // negative infinity

    public Align(Substitution sub, int d, String seq1, String seq2) {
        this.sub = sub;
        this.seq1 = strip(seq1);
        this.seq2 = strip(seq2);
        this.d = d;
        this.n = this.seq1.length();
        this.m = this.seq2.length();
    }

    public final String strip(String s) {
        boolean[] valid = new boolean[127];
        String residues = sub.getResidues();
        for (int i = 0; i < residues.length(); i++) {
            char c = residues.charAt(i);
            if (c < 96) {
                valid[c] = valid[c + 32] = true;
            } else {
                valid[c - 32] = valid[c] = true;
            }
        }
        StringBuilder res = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            if (valid[s.charAt(i)]) {
                res.append(s.charAt(i));
            }
        }
        return res.toString();
    }

    // Return two-element array containing an alignment with maximal score
    protected int startA;
    protected int endA;
    protected int startB;
    protected int endB;

    protected String alignedA;
    protected String alignedB;
    protected String globalAlignedA;
    protected String globalAlignedB;

    protected void doMatch() {
        StringBuilder res1 = new StringBuilder();
        StringBuilder res2 = new StringBuilder();
        Traceback tb = B0;
        int i = tb.i, j = tb.j;
        endA = i;
        endB = j;

        while ((tb = next(tb)) != null) {
            if (i == tb.i) {
                res1.append('-');
            } else {
                res1.append(seq1.charAt(i - 1));
            }
            if (j == tb.j) {
                res2.append('-');
            } else {
                res2.append(seq2.charAt(j - 1));
            }
            i = tb.i;
            j = tb.j;
        }

        startA = i;
        startB = j;
        alignedA = res1.reverse().toString();
        alignedB = res2.reverse().toString();

        globalAlignedA = alignedA;
        if (getStartAlignmentA() > 0) {
            globalAlignedA = seq1.substring(0, getStartAlignmentA()) + globalAlignedA;
        }
        for (int k = 0; k < getStartAlignmentB(); k++) {
            globalAlignedA = "-" + globalAlignedA;
        }

        globalAlignedB = alignedB;

        for (int k = 0; k < getStartAlignmentA(); k++) {
            globalAlignedB = "-" + globalAlignedB;
        }
        if (getStartAlignmentB() > 0) {
            try {
                globalAlignedB = seq2.substring(0, getStartAlignmentB()) + globalAlignedB;
            } catch (Exception e) {
                System.out.println("Exception !!!!!!!!!!");
                System.out.println(seq1 + "\n" + seq2);
                System.out.println(getStartAlignmentA() + " " + getAlignmentSeqA());
                System.out.println(getStartAlignmentB() + " " + getAlignmentSeqB());

            }
        }

    }

    public String getAlignmentSeqA() {
        return alignedA;
    }

    /**
     * Add '-' at to start the string if necessary
     *
     * @return
     */
    public String getGlobalAlignmentSeqA() {

        return globalAlignedA;
    }

    /**
     * Add '-' at to start the string if necessary
     *
     * @return
     */
    public String getGlobalAlignmentSeqB() {

        return globalAlignedB;
    }

    public String getAlignmentSeqB() {
        return alignedB;
    }

    public int getStartAlignmentA() {
        return startA;
    }

    public int getStartAlignmentB() {
        return startB;
    }

    public String fmtscore(int val) {
        if (val < NegInf / 2) {
            return "-Inf";
        } else {
            return Integer.toString(val);
        }
    }

    // Get the next state in the traceback
    public Traceback next(Traceback tb) {
        return tb;
    }                // dummy implementation for the `smart' algs.

    // Return the score of the best alignment
    public abstract int getScore();

    // Print the matrix (matrices) used to compute the alignment
    public abstract void printf(Output out);

    // Auxiliary functions
    static int max(int x1, int x2) {
        return (x1 > x2 ? x1 : x2);
    }

    static int max(int x1, int x2, int x3) {
        return max(x1, max(x2, x3));
    }

    static int max(int x1, int x2, int x3, int x4) {
        return max(max(x1, x2), max(x3, x4));
    }

    static String padLeft(String s, int width) {
        int filler = width - s.length();
        if (filler > 0) {           // and therefore width > 0
            StringBuilder res = new StringBuilder(width);
            for (int i = 0; i < filler; i++) {
                res.append(' ');
            }
            return res.append(s).toString();
        } else {
            return s;
        }
    }
}

// Alignment with simple gap costs
abstract class AlignSimple extends Align {

    protected int[][] F;                    // the matrix used to compute the alignment
    protected Traceback2[][] B;             // the traceback matrix

    public AlignSimple(Substitution sub, int d, String seq1, String seq2) {
        super(sub, d, seq1, seq2);
        F = new int[n + 1][m + 1];
        B = new Traceback2[n + 1][m + 1];
    }

    @Override
    public Traceback next(Traceback tb) {
        Traceback2 tb2 = (Traceback2) tb;
        return B[tb2.i][tb2.j];
    }

    @Override
    public int getScore() {
        return F[B0.i][B0.j];
    }

    @Override
    public void printf(Output out) {
        for (int j = 0; j <= m; j++) {
            for (int[] F1 : F) {
                out.print(padLeft(fmtscore(F1[j]), 5));
            }
            out.println();
        }
    }
}

// Traceback objects
abstract class Traceback {

    int i, j;                     // absolute coordinates
}

// Traceback2 objects for simple gap costs
class Traceback2 extends Traceback {

    public Traceback2(int i, int j) {
        this.i = i;
        this.j = j;
    }
}

// Auxiliary classes for output
abstract class Output {

    public abstract void print(String s);

    public abstract void println(String s);

    public abstract void println();
}

// Global alignment with the Needleman-Wunsch algorithm (simple gap costs)
class NW extends AlignSimple {

    public NW(Substitution sub, int d, String sq1, String sq2) {
        super(sub, d, sq1, sq2);
        int n = this.n, m = this.m;
        int[][] score = sub.score;
        for (int i = 1; i <= n; i++) {
            F[i][0] = -d * i;
            B[i][0] = new Traceback2(i - 1, 0);
        }
        for (int j = 1; j <= m; j++) {
            F[0][j] = -d * j;
            B[0][j] = new Traceback2(0, j - 1);
        }
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int s = score[seq1.charAt(i - 1)][seq2.charAt(j - 1)];
                int val = max(F[i - 1][j - 1] + s, F[i - 1][j] - d, F[i][j - 1] - d);
                F[i][j] = val;
                if (val == F[i - 1][j - 1] + s) {
                    B[i][j] = new Traceback2(i - 1, j - 1);
                } else if (val == F[i - 1][j] - d) {
                    B[i][j] = new Traceback2(i - 1, j);
                } else if (val == F[i][j - 1] - d) {
                    B[i][j] = new Traceback2(i, j - 1);
                } else {
                    throw new Error("NW 1");
                }
            }
        }
        B0 = new Traceback2(n, m);
    }
}

// Local alignment with the Smith-Waterman algorithm (simple gap costs)

/**
 *
 * @author Arnaud Ceol
 */
public class SmithWatermanItuDK extends AlignSimple {

    /**
     *
     * @param sq1
     * @param sq2
     */
    public SmithWatermanItuDK( String sq1, String sq2) {
        super(new Blosum50(), 8, sq1, sq2);
        int n = this.n, m = this.m;
        int[][] score = sub.score;
        int maxi = n, maxj = m;
        int maxval = NegInf;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int s = score[seq1.charAt(i - 1)][seq2.charAt(j - 1)];
                int val = max(0, F[i - 1][j - 1] + s, F[i - 1][j] - d, F[i][j - 1] - d);
                F[i][j] = val;
                if (val == 0) {
                    B[i][j] = null;
                } else if (val == F[i - 1][j - 1] + s) {
                    B[i][j] = new Traceback2(i - 1, j - 1);
                } else if (val == F[i - 1][j] - d) {
                    B[i][j] = new Traceback2(i - 1, j);
                } else if (val == F[i][j - 1] - d) {
                    B[i][j] = new Traceback2(i, j - 1);
                } else {
                    throw new Error("SW 1");
                }
                if (val > maxval) {
                    maxval = val;
                    maxi = i;
                    maxj = j;
                }
            }
        }
        B0 = new Traceback2(maxi, maxj);

        doMatch();
    }
    
    /**
     *
     * @param args
     */
    public static void main(String[] args){
           
//                  String query = "MVRSRQMCNTNMSVPTDGAVTTSQIPASEQETLVRPKPLLLKLLKSVGAQKDTYTMKEVLFYLGQYIMTKRLYDEKQQHIVYCSNDLLGDLFGVPSFSVKEHRKIYTMIYRNLVVVNQQESSDSGTSVSENRCHLEGGSDQKDLVQELQEEKPSSSHLVSRPSTSSRRRAISETEENSDELSGERQRKRHKSDSISLSFDESLALCVIREICCERSSSSESTGTPSNPDLDAGVSEHSGDWLDQDSVSDQFSVEFEVESLDSEDYSLSEEGQELSDEDDEVYQVTVYQAGESDTDSFEEDPEISLADYWKCTSCNEMNPPLPSHCNRCWALRENWLPEDKGKDKGEISEKAKLENSTQAEEGFDVPDCKKTIVNDSRESCVEENDDKITQASQSQESEDYSQPSTSSSIIYSSQEDVKEFEREETQDKEESVESSLPLNAIEPCVICQGRPKNGCIVHGKTGHLMACFTCAKKLKKRNKPCPVCRQPIQMIVLTYFP";
//          String target = "TSWRSEATFQFTVERFSRLSESVLSPPCFVRNLPWKIMVMPRFKSVGFFLQCNAESDSTSWSCHAQAVLKIINYRDDEKSFSRRISHLFFHKENDWGFSNFMAWSEVTDPEKGFIDDDKVTFEVFVQADLDAGVSE";
              String query = "YTTGCD";
           String target =  "TTGACD";
        SmithWatermanItuDK sm = new SmithWatermanItuDK(query, target);
       
        System.out.println("A: "+sm.getAlignmentSeqA());
        System.out.println("B: "+sm.getAlignmentSeqB());
        System.out.println("A: "+sm.getGlobalAlignmentSeqA());
        System.out.println("B: "+sm.getGlobalAlignmentSeqB());
        System.out.println("A: "+sm.getStartAlignmentA());
        System.out.println("B: "+sm.getStartAlignmentB());
    }

}
