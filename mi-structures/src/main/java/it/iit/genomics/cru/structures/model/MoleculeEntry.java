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

import it.iit.genomics.cru.structures.model.sequence.UniprotSequence;
import it.iit.genomics.cru.utils.maps.MapOfMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Arnaud Ceol
 *
 * Model for a Protein. This class allow to store all information about a
 * protein requiered for the MI Bundle.
 *
 */
public class MoleculeEntry implements Comparable<MoleculeEntry> {

    private final static  Logger logger = LoggerFactory.getLogger(MoleculeEntry.class.getName());
    
    /**
     *
     */
    public final static String TAXID_UNSPECIFIED = "-3";

    /**
     *
     */
    public final static String TAXID_DNA = "DNA";

    /**
     *
     */
    public final static String TAXID_RNA = "RNA";

    /**
     *
     */
    public final static String TAXID_LIGAND = "LIGAND";

    /**
     *
     */
    public final static String TAXID_MODIFICATION = "MODIFICATION";

    /**
     *
     */
    public final static String DATASET_SWISSPROT = "Swiss-Prot";

    /**
     *
     */
    public final static String DATASET_TREMBL = "TrEMBL";
    
    /**
     *
     */
    protected final String uniprotAc;

    /**
     *
     */
    protected String dataset;
    
    /**
     *
     */
    protected String taxid = TAXID_UNSPECIFIED;

    /**
     *
     */
    protected String organism;

    /**
     *
     */
    protected Collection<String> geneNames;

    /**
     *
     */
    protected Collection<String> refseqs;

    /**
     *
     */
    protected Collection<String> ensemblGenes;

    /**
     *
     */
    protected Collection<ModifiedResidue> modifications;

    /**
     *
     */
    protected MapOfMap<String, ChainMapping> chains;

    /**
     *
     */
    protected Collection<String> diseases;

    /**
     *
     */
    protected UniprotSequence mainSequence;

    /**
     *
     */
    protected HashMap<String, String> pdbMethods = new HashMap<>();

    /**
     *
     */
    protected String mainIsoform;

    /**
     *
     * @return
     */
    public String getMainIsoform() {
        return mainIsoform;
    }

    /**
     *
     * @param mainIsoform
     */
    public void setMainIsoform(String mainIsoform) {
        this.mainIsoform = mainIsoform;
        this.mainSequence = varSpliceToSequence.get(mainIsoform);
//        logger.info("Main sequence: " + mainIsoform + " : " + mainSequence.getSequence());
        if (mainSequence == null) {
            logger.error( "Main sequence is null: {0}", mainIsoform);
        }
    }
    
    /**
     *
     */
    protected HashMap<String, String> xrefToVarSplice = new HashMap<>();

    /**
     *
     */
    protected HashMap<String, UniprotSequence> varSpliceToSequence = new HashMap<>();

    /**
     *
     * @return
     */
    public String getUniprotAc() {
        return uniprotAc;
    }

    /**
     *
     * @return
     */
    public String getDataset() {
        return dataset;
    }

    /**
     *
     * @param dataset
     */
    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    /**
     *
     * @return
     */
    public String getTaxid() {
        return taxid;
    }

    /**
     *
     * @param taxid
     */
    public void setTaxid(String taxid) {
        this.taxid = taxid;
    }

    /**
     *
     * @return
     */
    public String getOrganism() {
        if (null == organism) {
            return taxid;
        }
        return organism;
    }

    /**
     *
     * @param organism
     */
    public void setOrganism(String organism) {
        this.organism = organism;
    }

    /**
     * Retrun the first available gene name
     *
     * @return
     */
    public String getGeneName() {
        if (geneNames.isEmpty()) {
            return null;
        }
        return geneNames.iterator().next();
    }

    /**
     *
     * @return
     */
    public Collection<String> getGeneNames() {
        return geneNames;
    }

    /**
     *
     * @return
     */
    public Collection<String> getRefseqs() {
        return refseqs;
    }

    /**
     *
     * @return
     */
    public Collection<String> getEnsemblGenes() {
        return ensemblGenes;
    }

    /**
     *
     * @return
     */
    public Collection<String> getPdbs() {

        // Some non PDB chains may have been added, filter them out
        HashSet<String> pdbIds = new HashSet<>();
        for (String id : pdbMethods.keySet()) {
            if (id.length() == 4) {
                pdbIds.add(id);
            }
        }
        return pdbIds;
    }

    /**
     *
     * @param pdbId
     * @return
     */
    public String getPdbMethod(String pdbId) {
        return pdbMethods.get(pdbId);
    }

    /**
     *
     * @param pdb
     * @return
     */
    public Collection<ChainMapping> getChains(String pdb) {
        if (chains.containsKey(pdb)) {
            return chains.get(pdb);
        }
        return Collections.emptyList();
    }

    /**
     *
     * @return
     */
    public Collection<String> getDiseases() {
        return diseases;
    }

    /**
     *
     * @param pdb
     * @return
     */
    public Collection<String> getChainNames(String pdb) {
        if (chains.containsKey(pdb)) {
            HashSet<String> chainNames = new HashSet<>();
            for (ChainMapping chain : chains.get(pdb)) {
                chainNames.add(chain.getChain());
            }
            return chainNames;
        }
        return Collections.emptyList();
    }

    /**
     * There may be more than one sequence (one for each splice variant)
     *
     * @param xref
     * @return
     */
    public UniprotSequence getSequence(String xref) {
   //     logger.info("Get sequence for : " + xref + ", " + xrefToVarSplice.get(xref));
        
        if (xref != null && xrefToVarSplice.containsKey(xref) ) {
            String varSplice = xrefToVarSplice.get(xref);
            if (varSpliceToSequence.containsKey(varSplice)) {
                return varSpliceToSequence.get(varSplice);
            }
        } 

        return mainSequence;
    }

    /**
     * There may be more than one sequence (one for each splice variant)
     *
     * @param xref
     * @return
     */
    public String getVarSpliceAC(String xref) {
        if (xref != null && xrefToVarSplice.containsKey(xref)) {
            return xrefToVarSplice.get(xref);
        }
        //logger.log(Level.WARNING, "No varsplice for xref: {0}, returning main isoform: {1}", new Object[]{xref, getUniprotAc()});
        return null;
    }
    
    /**
     *
     * @param xref
     * @param varSplice
     */
    public void addXrefToVarSplice(String xref, String varSplice) {
        xrefToVarSplice.put(xref, varSplice);
    }

    /**
     *
     * @param varSplice
     * @param sequence
     */
    public void addSequence(String varSplice, String sequence) {
        varSpliceToSequence.put(varSplice, new UniprotSequence(sequence));
    }

    /**
     *
     * @param geneName
     */
    public void addGeneName(String geneName) {
        this.geneNames.add(geneName);
    }

    /**
     *
     * @param refseq
     */
    public void addRefseq(String refseq) {
        this.refseqs.add(refseq);
    }

    /**
     *
     * @param ensemblGeneId
     */
    public void addEnsemblGene(String ensemblGeneId) {
        this.ensemblGenes.add(ensemblGeneId);
    }

    /**
     *
     * @param disease
     */
    public void addDisease(String disease) {
        this.diseases.add(disease);
    }

    /**
     *
     * @param structureId
     * @param method
     */
    public void addPDB(String structureId, String method) {
        pdbMethods.put(structureId, method);
    }

    /**
     *
     * @param structureId
     * @param chain
     * @param method
     */
    public void addChain(String structureId, ChainMapping chain, String method) {
        chains.add(structureId, chain);
        addPDB(structureId, method);
    }

    /**
     *
     * @param uniprotAc
     * @param taxid
     * @param geneNames
     * @param refseqs
     * @param pdbChains
     */
    public MoleculeEntry(String uniprotAc, String taxid,
            Collection<String> geneNames, Collection<String> refseqs,
            MapOfMap<String, ChainMapping> pdbChains) {
        super();
        this.uniprotAc = uniprotAc;
        this.taxid = taxid;
        this.geneNames = geneNames;
        this.refseqs = refseqs;
        this.chains = pdbChains;
    }

    /**
     *
     * @param uniprotAc
     */
    public MoleculeEntry(String uniprotAc) {
        super();
        this.uniprotAc = uniprotAc;

        // It has to be an List: the order is important
        this.geneNames = new ArrayList<>();
        this.refseqs = new ArrayList<>();
        this.ensemblGenes = new ArrayList<>();
        this.modifications = new ArrayList<>();
        this.diseases = new ArrayList<>();
        this.chains = new MapOfMap<>();
    }

    /**
     *
     * @param sequence
     */
    public void setSequence(String sequence) {
        this.mainSequence = new UniprotSequence(sequence);
        /**
         * TODO: Check if I should remove this, i.e. I don't need the mapping
         * anymore
         */
        for (String pdb : chains.keySet()) {
            for (ChainMapping mapping : chains.get(pdb)) {
                mapping.setSequence(sequence);
            }
        }
    }

    /**
     *
     * @return
     */
    public Collection<ModifiedResidue> getModifications() {
        return modifications;
    }

    @Override
    public String toString() {
        return "ProteinEntry[uniprotAc=" + uniprotAc + ", taxid=" + taxid
                + ", geneNames=" + StringUtils.join(geneNames, ";")
                + ", ensembl=" + StringUtils.join(ensemblGenes, ";")
                + ", refseq=" + StringUtils.join(refseqs, ";") + ", pdb="
                + StringUtils.join(chains.keySet(), ";")
                + ", sequenceLength=" + mainSequence.getSequence().length();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MoleculeEntry other = (MoleculeEntry) obj;
        if (!Objects.equals(this.uniprotAc, other.uniprotAc)) {
            return false;
        }
        if (!Objects.equals(this.taxid, other.taxid)) {
            return false;
        }
        if (!Objects.equals(this.organism, other.organism)) {
            return false;
        }
        if (!Objects.equals(this.geneNames, other.geneNames)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.uniprotAc);
        hash = 71 * hash + Objects.hashCode(this.taxid);
        hash = 71 * hash + Objects.hashCode(this.organism);
        hash = 71 * hash + Objects.hashCode(this.geneNames);
        return hash;
    }

    @Override
    public int compareTo(MoleculeEntry o) {
        if (null != getUniprotAc() && null != o.getUniprotAc() && false == getUniprotAc().equals(o.getUniprotAc())) {
            return getUniprotAc().compareTo(o.getUniprotAc());
        }

        return toString().compareTo(o.toString());
    }

    /**
     *
     * @return
     */
    public boolean isProtein() {
        switch (getTaxid()) {
            case TAXID_LIGAND:
                return false;
            case TAXID_DNA:
                return false;
            case TAXID_RNA:
                return false;
            case TAXID_MODIFICATION:
                return false;
            default:
                return true;
        }
    }

    /**
     *
     * @return
     */
    public boolean isLigand() {
        return TAXID_LIGAND.equals(getTaxid());
    }

    /**
     *
     * @return
     */
    public boolean isSwissprot() {
        return DATASET_SWISSPROT.equals(dataset);
    }


}
