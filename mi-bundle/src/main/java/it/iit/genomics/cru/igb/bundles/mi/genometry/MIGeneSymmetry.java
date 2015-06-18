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
package it.iit.genomics.cru.igb.bundles.mi.genometry;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.MutableSeqSpan;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.SupportsCdsSpan;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.symmetry.SupportsGeneName;
import com.affymetrix.genometry.symmetry.SymSpanWithCds;
import com.affymetrix.genometry.symmetry.SymWithProps;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import it.iit.genomics.cru.structures.model.MIGene;
import it.iit.genomics.cru.structures.model.position.UniprotPosition;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import java.util.Map;

/**
 *
 * @author aceol
 */
public class MIGeneSymmetry implements SeqSpan, SupportsCdsSpan, SymSpanWithCds, SymWithProps, SupportsGeneName {

    String geneName;
    String name;
    int txMin;
    int txMax;
    int cdsMin;
    int cdsMax;
    boolean forward;
    int[] emins;
    int[] emaxs;
    BioSeq seq;
    String type;
    Map<String, Object> props;

    Collection<MIGene> genes;

    public MIGeneSymmetry(String type, String geneName, String name,
            BioSeq seq, boolean forward, int txMin, int txMax,
            int cdsMin, int cdsMax, int[] emins, int[] emaxs, Collection<MIGene> genes
    ) {
        this.type = type;
        this.geneName = geneName;
        this.name = name;
        this.seq = seq;
        this.forward = forward;
        this.txMin = txMin;
        this.txMax = txMax;
        this.cdsMin = cdsMin;
        this.cdsMax = cdsMax;
        this.emins = emins;
        this.emaxs = emaxs;
        this.genes = genes;
    }

    @Override
    public String getGeneName() {
        return null;
    }

    public String getName() {
        return null;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean isCdsStartStopSame() {
        return cdsMin == cdsMax;
    }

    public boolean hasCdsSpan() {
        return (cdsMin >= 0 && cdsMax >= 0);
    }

    public SeqSpan getCdsSpan() {
        if (!hasCdsSpan()) {
            return null;
        }
        if (forward) {
            return new SimpleSeqSpan(cdsMin, cdsMax, seq);
        } else {
            return new SimpleSeqSpan(cdsMax, cdsMin, seq);
        }
    }

    /**
     * SeqSymmetry implementation.
     */
    public String getID() {
        return name;
    }

    public SeqSpan getSpan(BioSeq bs) {
        if (bs.equals(this.seq)) {
            return this;
        } else {
            return null;
        }
    }

    public SeqSpan getSpan(int index) {
        if (index == 0) {
            return this;
        } else {
            return this.getChild(index).getSpan(this.getBioSeq());
        }
    }

    public boolean getSpan(BioSeq bs, MutableSeqSpan span) {
        if (bs.equals(this.seq)) {
            if (forward) {
                span.set(txMin, txMax, seq);
            } else {
                span.set(txMax, txMin, seq);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean getSpan(int index, MutableSeqSpan span) {
        if (index == 0) {
            if (forward) {
                span.set(txMin, txMax, seq);
            } else {
                span.set(txMax, txMin, seq);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getSpanCount() {
        return 1;
    }

    @Override
    public BioSeq getSpanSeq(int index) {
        if (index == 0) {
            return seq;
        } else {
            return null;
        }
    }

    @Override
    public int getChildCount() {
        return emins.length;
    }

    @Override
    public SeqSymmetry getChild(int index) {

        int start = isForward() ? emins[index] : emaxs[index];
        int end = isForward() ? emaxs[index] : emins[index];

        Map<String, Object> residueProps = new HashMap<>();

        int i = 1;

        String residues = "";

        for (MIGene gene : genes) {
            UniprotPosition position = gene.getUniprotAAPosition(emins[index]);
            if (position != null) {
                char residue = gene.getUniprotSequence().getSequence().charAt(position.getPosition() -1);
                residueProps.put("uniprot-pos-" + i, gene.getProtein().getVarSpliceAC(gene.getID()) + ":" + residue + position);
                if ("".equals(residues)) {
                    residues = String.valueOf(residue);
                }
                i++;
            }            
        }
        
        if (emaxs[index] - emins[index] == 2) {
            residues+= "  ";
        } else if  (emaxs[index] - emins[index] == 1) {
            residues+= " ";
        } 

        MIResidue s = new MIResidue(residues);

        for (String property : residueProps.keySet()) {
            s.setProperty(property, residueProps.get(property));
        }

        SimpleSeqSpan span = new SimpleSeqSpan(start, end, seq);

        s.addSpan(span);

        return s;
    }

    //  SeqSpan implementation
    @Override
    public int getStart() {
        return (forward ? txMin : txMax);
    }

    @Override
    public int getEnd() {
        return (forward ? txMax : txMin);
    }

    @Override
    public int getMin() {
        return txMin;
    }

    @Override
    public int getMax() {
        return txMax;
    }

    @Override
    public int getLength() {
        return (txMax - txMin);
    }

    @Override
    public boolean isForward() {
        return forward;
    }

    @Override
    public BioSeq getBioSeq() {
        return seq;
    }

    @Override
    public double getStartDouble() {
        return getStart();
    }

    @Override
    public double getEndDouble() {
        return getEnd();
    }

    @Override
    public double getMaxDouble() {
        return getMax();
    }

    @Override
    public double getMinDouble() {
        return getMin();
    }

    @Override
    public double getLengthDouble() {
        return getLength();
    }

    @Override
    public boolean isIntegral() {
        return true;
    }

    @Override
    public Map<String, Object> getProperties() {
        return cloneProperties();
    }

    @Override
    public Map<String, Object> cloneProperties() {
        HashMap<String, Object> tprops = new HashMap<>();
        tprops.put("id", name);
        tprops.put("type", type);
        tprops.put("gene name", geneName);
        tprops.put("seq id", seq.getId());
        tprops.put("forward", forward);
        if (props != null) {
            tprops.putAll(props);
        }
        return tprops;
    }

    @Override
    public Object getProperty(String key) {
        // test for standard gene sym  props
        if (key.equals("id")) {
            return name;
        } else if (key.equals("type")) {
            return getType();
        } else if (key.equals("gene name") || key.equals("gene_name")) {
            return geneName;
        } else if (key.equals("seq id")) {
            return seq.getId();
        } else if (key.equals("forward")) {
            return forward;
        } else if (key.equals("cds min")) {
            return cdsMin;
        } else if (key.equals("cds max")) {
            return cdsMax;
        } else if (props != null) {
            return props.get(key);
        } else {
            return null;
        }
    }

    @Override
    public boolean setProperty(String name, Object val) {
        if (props == null) {
            props = new Hashtable<>();
        }
        props.put(name, val);
        return true;
    }

}
