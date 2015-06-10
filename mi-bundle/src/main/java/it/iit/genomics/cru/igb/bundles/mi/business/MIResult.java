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
package it.iit.genomics.cru.igb.bundles.mi.business;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.parsers.TrackLineParser;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.TypeContainerAnnot;
import it.iit.genomics.cru.bridges.interactome3d.local.Interactome3DLocalRepository;
import it.iit.genomics.cru.igb.bundles.commons.business.IGBLogger;
import it.iit.genomics.cru.bridges.interactome3d.local.I3DDownload;
import it.iit.genomics.cru.igb.bundles.mi.commons.IDManager;
import it.iit.genomics.cru.igb.bundles.mi.commons.MIBundleConfiguration;
import it.iit.genomics.cru.igb.bundles.mi.model.MISymContainer;
import it.iit.genomics.cru.igb.bundles.mi.model.MISymManager;
import it.iit.genomics.cru.igb.bundles.mi.query.MIQuery;
import it.iit.genomics.cru.structures.model.MoleculeEntry;
import it.iit.genomics.cru.structures.bridges.userData.UserStructuresManager;
import it.iit.genomics.cru.structures.business.StructureMapper;
import it.iit.genomics.cru.structures.business.StructureMapper.MappingType;
import it.iit.genomics.cru.structures.model.AAPosition;
import it.iit.genomics.cru.structures.model.AAPositionManager;
import it.iit.genomics.cru.structures.model.InteractionStructure;
import it.iit.genomics.cru.structures.model.ProteinStructure;
import it.iit.genomics.cru.structures.sources.StructureManager;
import it.iit.genomics.cru.structures.sources.StructureManager.StructureSourceType;
import it.iit.genomics.cru.structures.sources.StructureSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

import it.iit.genomics.cru.bridges.dsysmap.local.DSysMapDownload;
import it.iit.genomics.cru.bridges.dsysmap.local.DSysMapLocalRepository;
import it.iit.genomics.cru.igb.bundles.mi.genometry.MIGeneSymmetry;
import it.iit.genomics.cru.structures.bridges.dsysmap.DSysMapStructureMapper;
import it.iit.genomics.cru.structures.bridges.eppic.EPPICStructureMapper;
import it.iit.genomics.cru.structures.bridges.psicquic.Interaction;

import it.iit.genomics.cru.structures.business.Interactome3DUtils;
import it.iit.genomics.cru.structures.business.PDBUtils;
import it.iit.genomics.cru.structures.model.MIGene;
import it.iit.genomics.cru.structures.model.ModifiedResidue;
import it.iit.genomics.cru.structures.model.Range;
import it.iit.genomics.cru.structures.model.position.UniprotPosition;
import java.awt.Color;
import java.io.File;

/**
 * @author Arnaud Ceol
 *
 * Structure for a result, e.g. an interaction. This class stores information
 * about the interaction and do the mapping of residues to the structures.
 *
 */
public class MIResult {

    private final IGBLogger igbLogger;

    private final static String featureName = "mi";

    private boolean homodimer = false;

    private String ID;

    private String queryID;

    private String queryTaxid;

    private String trackId;

    private String psicquicUrl;

    private MISymContainer container1;

    private MISymContainer container2;

    private Collection<SeqSymmetry> symmetries1;

    private Collection<SeqSymmetry> symmetries2;

    private Interaction interaction;

    private Collection<InteractionStructure> structuresInteraction;

    private Collection<ProteinStructure> structuresProteinA;

    private Collection<ProteinStructure> structuresProteinB;

    private Collection<AAPosition> queryResiduesA = new HashSet<>();

    private Collection<AAPosition> queryResiduesB = new HashSet<>();

    private StructureMapper structureMapper;

    public final static int maxInteractome3DStructures = 1000;

    private StructureSource structureSource;

    //private GeneManager geneManager;
    private boolean hasTrack = false;

    public MIResult(String trackGroup, MISymContainer container1,
            MISymContainer container2, Interaction interaction, MIQuery query,
            MISymManager symManager) {

        super();

        this.ID = IDManager.getInstance().getMiID();

        igbLogger = IGBLogger.getInstance(ID);

        this.trackId = trackGroup + "-" + container1.getEntry().getGeneName() + "-" + container2.getEntry().getGeneName();

        //this.geneManager = geneManager;
        this.queryID = query.getLabel();

        this.queryTaxid = query.getTaxid();

        // Save the provider we did the search with
        this.psicquicUrl = query.getPsiquicServer();

        this.container1 = container1;
        this.container2 = container2;

        this.interaction = interaction;

        this.symmetries1 = new HashSet<>();
        this.symmetries1.add(container1.getResultSym());

        this.symmetries2 = new HashSet<>();
        this.symmetries2.add(container2.getResultSym());

        this.structuresInteraction = new HashSet<>();
        this.structuresProteinA = new HashSet<>();
        this.structuresProteinB = new HashSet<>();

        /**
         * TODO: use a list of species to avoid queries that we know won't
         * return anything in Interactome3d
         */
        String proteinAc1 = container1.getEntry().getUniprotAc();
        String proteinAc2 = container2.getEntry().getUniprotAc();

        if (proteinAc1.equals(proteinAc2)) {
            homodimer = true;
        }

        initAndGetStructures(query);
        if (symManager.hasQueryResidues(container1.getEntry())) {
            queryResiduesA.addAll(symManager.getQueryResidues(container1
                    .getEntry()));
        }

        if (symManager.hasQueryResidues(container2.getEntry())) {
            queryResiduesB.addAll(symManager.getQueryResidues(container2
                    .getEntry()));
        }

        // We have the structures, get the residues and interfaces
        for (InteractionStructure interactionStructure : structuresInteraction) {
            try {
                structureMapper.searchStructureResidues(
                        MappingType.INTERACTION_PROTEINA, container1.getEntry(),
                        interactionStructure, queryResiduesA);

                structureMapper.searchInterfaces(MappingType.INTERACTION_PROTEINA, container1.getEntry(),
                        container2.getEntry(), interactionStructure,
                        queryResiduesA);

                if (false == homodimer && container2.getEntry().isProtein()) {
                    structureMapper.searchStructureResidues(
                            MappingType.INTERACTION_PROTEINB, container2.getEntry(),
                            interactionStructure, queryResiduesB);

                    structureMapper.searchInterfaces(MappingType.INTERACTION_PROTEINB, container2.getEntry(),
                            container1.getEntry(), interactionStructure,
                            queryResiduesB);
                }
            } catch (Exception e) {
                igbLogger.getLogger().error("Cannot get PDB structures ", e);
            }
        }

        // get modified residues
        merger = new RangeMerger();
        mergerA = new RangeMerger();
        mergerB = new RangeMerger();
        // Get pdbResidues
        for (AAPosition aa : structureMapper.getInterfaceAAPositionsA()) {
            //  igbLogger.info("Check residue " + aa);

            for (String pos : aa.getGenomicPositions(container2.getEntry())) {
                String[] posSplit = pos.split(":");
                String seq = posSplit[0];
                Range range = new Range(Integer.parseInt(posSplit[1].split("-")[0]), Integer.parseInt(posSplit[1].split("-")[1]));
                merger.addRange(seq, range);
                mergerA.addRange(seq, range);
            }
        }
        for (AAPosition aa : structureMapper.getInterfaceAAPositionsB()) {
            for (String pos : aa.getGenomicPositions(container1.getEntry())) {
                String[] posSplit = pos.split(":");
                String seq = posSplit[0];
                Range range = new Range(Integer.parseInt(posSplit[1].split("-")[0]), Integer.parseInt(posSplit[1].split("-")[1]));
                merger.addRange(seq, range);
                mergerB.addRange(seq, range);
            }
        }

        /**
         * Do I need to do it also for residuesB? No: modification is only
         * molecule B, i.e. the partner of molecule A
         */
        for (AAPosition aa : getResiduesA()) {
            if (MoleculeEntry.TAXID_MODIFICATION.equals(container2.getEntry().getTaxid())) {
                for (ModifiedResidue mod : container1.getEntry().getModifications()) {

                    if (mod.getDescription().equals(container2.getEntry().getGeneName())) {
                        for (UniprotPosition position : mod.getPositions()) {
                            if (aa.getStart() <= position.getPosition() && aa.getEnd() >= position.getPosition()) {
                                for (String pos : aa.getGenomicPositions(position)) {
                                    String[] posSplit = pos.split(":");
                                    String seq = posSplit[0];
                                    Range range = new Range(Integer.parseInt(posSplit[1].split("-")[0]), Integer.parseInt(posSplit[1].split("-")[1]));
                                    merger.addRange(seq, range);
                                    mergerA.addRange(seq, range);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * Init structureSource, structureManager, and retrieve all structures for
     * the interaction and proteins.
     *
     * @param query
     * @param proteinAc1
     * @param proteinAc2
     */
    private void initAndGetStructures(MIQuery query) {
        // Get Structure repository
        // User dir
        if (query.getUserStructuresPath() != null
                && query.searchUserStructures()) {

            structureSource = StructureManager.getInstance()
                    .getStructureSource(StructureSourceType.USER,
                            query.getUserStructuresPath(),
                            MIBundleConfiguration.getInstance().getCachePath());

            Interactome3DLocalRepository userStructures = UserStructuresManager
                    .getInstance().getUserRepository(
                            query.getUserStructuresPath());
            igbLogger.info("Get I3D structures from user source");

            structuresInteraction.addAll(Interactome3DUtils.getStructures(container1.getEntry(), container2.getEntry(), userStructures, structureSource));
        } // Interactome3D
        else if (false == MIBundleConfiguration.getInstance()
                .isDisabledInteractome3D() && (query.searchInteractome3D() || query.searchDSysMap())) {
            structureSource = StructureManager.getInstance()
                    .getStructureSource(StructureSourceType.INTERACTOME3D,
                            null,
                            MIBundleConfiguration.getInstance().getCachePath());

              I3DDownload download = new I3DDownload(MIBundleConfiguration.getInstance().getCachePath());

            Interactome3DLocalRepository client = UserStructuresManager
                    .getInstance().getUserRepository(
                            download.getI3DdatPath(query.getTaxid()));
            structuresInteraction.addAll(Interactome3DUtils.getStructures(container1.getEntry(), container2.getEntry(), client, structureSource));
        } // Structures from PDB
        else if (query.searchPDBLocal()) {
            structureSource = StructureManager.getInstance()
                    .getStructureSource(StructureSourceType.PDB,
                            query.getPdbMirrorPath(),
                            MIBundleConfiguration.getInstance().getCachePath());
            structuresInteraction.addAll(PDBUtils.getStructures(container1.getEntry(), container2.getEntry(), structureSource));
        } // Structures from PDB
        else if (query.searchPDB() || query.searchEPPIC()) {
            structureSource = StructureManager.getInstance()
                    .getStructureSource(StructureSourceType.PDB,
                            query.getPdbURL(),
                            MIBundleConfiguration.getInstance().getCachePath());
            structuresInteraction.addAll(PDBUtils.getStructures(container1.getEntry(), container2.getEntry(), structureSource));
        }

        if (query.searchDSysMap()) {
            DSysMapDownload download = new DSysMapDownload(MIBundleConfiguration.getInstance().getCachePath());

            if (false == download.isDatDownloaded()) {
                download.downloadDat();
            }

            DSysMapLocalRepository repo = DSysMapRepositoryManager.getInstance().getRepository(download.getDSysMapDatPath());

            structureMapper = new DSysMapStructureMapper(structureSource,
                    AAPositionManager.getAAPositionManager(queryID), repo, false);
        } else if (query.searchEPPIC()) {
            structureMapper = new EPPICStructureMapper(structureSource,
                    AAPositionManager.getAAPositionManager(queryID),
                    MIBundleConfiguration.getInstance().getCachePath() + File.separator + "EPPIC");
        } else {
            structureMapper = new StructureMapper(structureSource,
                    AAPositionManager.getAAPositionManager(queryID));
        }
    }

    private float score = -1;

    public float getScore() {

        if (score > -1) {
            return score;
        }

        HashSet<AAPosition> residuesOnProteins = new HashSet<>();
        HashSet<AAPosition> residuesOnContacts = new HashSet<>();

        residuesOnProteins.addAll(queryResiduesA);
        residuesOnProteins.addAll(queryResiduesB);

        residuesOnContacts.addAll(structureMapper.getInterfaceAAPositionsA());
        residuesOnContacts.addAll(structureMapper.getInterfaceAAPositionsB());

        if (residuesOnProteins.isEmpty()) {
            return 0;
        }

        float residueScore = ((float) residuesOnContacts.size())
                / residuesOnProteins.size();

        this.score = residueScore;

        return residueScore;
    }

    public String toTab() {

        String uniprotA = this.container1.getEntry().getUniprotAc();
        String geneA = this.container1.getEntry().getGeneName();

        String uniprotB = this.container2.getEntry().getUniprotAc();
        String geneB = this.container2.getEntry().getGeneName();

        HashSet<String> selectedSymsAtInterfaceA = new HashSet<>();
        HashSet<String> selectedProteinResiduesAtInterfaceA = new HashSet<>();

        HashSet<String> selectedSymsAtInterfaceB = new HashSet<>();
        HashSet<String> selectedProteinResiduesAtInterfaceB = new HashSet<>();

        for (String sequence : mergerA.getSequences()) {
            for (Range range : mergerA.getRanges(sequence)) {
                selectedSymsAtInterfaceA.add(sequence + ":" + range.getMin() + "-" + range.getMax());
                for (MIGene gene : container1.getMiGenes()) {
                    int lastAA = -1;
                    for (int i = range.getMin(); i < range.getMax(); i++) {
                        UniprotPosition aa = gene.getUniprotAAPosition(i);
                        if (aa == null) {
                            continue;
                        }

                        if (aa.getPosition() > lastAA) {
                            try {
                                selectedProteinResiduesAtInterfaceA.add(gene.getID() + "," + container1.getEntry().getVarSpliceAC(gene.getID()) + ":" + container1.getEntry().getSequence(gene.getID()).getSequence().substring(aa.getPosition() - 1, aa.getPosition()) + aa.getPosition());
                            } catch (Exception e) {
                                igbLogger.getLogger().error("Pb when assigning AA : {0}, {1}, {2}, {3}", new Object[]{aa, gene.getID(), container1.getEntry().getVarSpliceAC(gene.getID()), container1.getEntry().getSequence(gene.getID())});
                            }
                        }
                    }
                }
            }
        }

        if (container2.getEntry().isProtein()) {
            for (String sequence : mergerB.getSequences()) {
                for (Range range : mergerB.getRanges(sequence)) {
                    selectedSymsAtInterfaceB.add(sequence + ":" + range.getMin() + "-" + range.getMax());
                    for (MIGene gene : container2.getMiGenes()) {
                        int lastAA = -1;
                        for (int i = range.getMin(); i < range.getMax(); i++) {
                            UniprotPosition aa = gene.getUniprotAAPosition(i);
                            if (aa == null) {
                                continue;
                            }
                            if (aa.getPosition() > lastAA) {
                                try {
                                    selectedProteinResiduesAtInterfaceB.add(gene.getID() + "," + container2.getEntry().getVarSpliceAC(gene.getID()) + ":" + gene.getUniprotSequence().getSequence().substring(aa.getPosition() - 1, aa.getPosition()) + aa.getPosition());
                                } catch (Exception e) {
                                    System.err.println(e);
                                }
                            }
                        }
                    }
                }
            }
        }

        return uniprotA + "\t" + geneA + "\t"
                + "\t" + uniprotB + "\t" + geneB
                + "\t"
                + StringUtils.join(selectedSymsAtInterfaceA, "|") + "\t"
                + StringUtils.join(selectedSymsAtInterfaceB, "|") + "\t"
                + StringUtils.join(selectedProteinResiduesAtInterfaceA, "|") + "\t"
                + StringUtils.join(selectedProteinResiduesAtInterfaceB, "|");
    }

    public String toXgmml() {

        int idA = this.container1.getId();
        int idB = this.container2.getId();

        String type;
        String graphics = "";

        if (hasInterfaceOnStructure()) {
            type = "interface";
        } else if (false == this.structuresInteraction.isEmpty()) {
            type = "wt-structure";
        } else {
            type = "wo-structure";
        }

        String strutureType = "none";

        for (InteractionStructure structure : structuresInteraction) {
            if (structure.getStructureID().length() == 4) {
                // PDB
                strutureType = "experimental";
                break;
            } else if (structure.getStructureID().contains("-EXP-")) {
                // Interactome3D experimental
                strutureType = "experimental";
                break;
            } else if (structure.getStructureID().contains("-MDL-")
                    || structure.getStructureID().contains("-MDD-")) {
                // Interactome3D experimental
                strutureType = "model";
                break;
            }
        }

        String structureTypeAttr = "<att name=\"structuretype\" type=\"string\" value=\""
                + strutureType + "\"/>";

        String edges = "";

        if (false == structureMapper.getInterfaceAAPositionsA().isEmpty()
                || structureMapper.getInterfaceAAPositionsB().isEmpty()) {
            edges += "<edge source=\"" + idA + "\" target=\"" + idB
                    + "\" label=\"" + idA + "-" + idB + "\">" + graphics
                    + "<att name=\"positions\" type=\"float\" value=\""
                    + structureMapper.getInterfaceAAPositionsA().size()
                    + "\"/><att type=\"string\" name=\"interaction\" value=\""
                    + type + "\"/>" + structureTypeAttr + "</edge>\n";
        }

        if (false == structureMapper.getInterfaceAAPositionsB().isEmpty()) {
            edges += "<edge source=\"" + idB + "\" target=\"" + idA
                    + "\" label=\"" + idB + "-" + idA + "\">" + graphics
                    + "<att name=\"positions\" type=\"float\" value=\""
                    + structureMapper.getInterfaceAAPositionsB().size()
                    + "\"/><att type=\"string\" name=\"interaction\" value=\""
                    + type + "\"/>" + structureTypeAttr + "</edge>\n";
        }

        return edges;

    }

    private static String getPositionList(Collection<SeqSymmetry> symmetries) {
        HashSet<String> positions = new HashSet<>();
        for (SeqSymmetry sym : symmetries) {

            if (sym == null) {
                continue;
            }

            if (sym.getSpanCount() == 0) {
                continue;
            }
            SeqSpan span = sym.getSpan(0);

            positions.add(span.getBioSeq().getID() + ":" + span.getStart()
                    + "-" + span.getEnd());
        }

        return StringUtils.join(positions, ";");
    }

    private String getPositionList(Collection<AAPosition> aaPositions,
            MoleculeEntry protein) {
        HashSet<String> positions = new HashSet<>();
        for (AAPosition pos : aaPositions) {
             String seq = pos.getSequence();
            if (seq == null) {
                igbLogger.severe(
                        "No sequence for residue " + protein.getUniprotAc()
                        + ", " + pos.getDescription());
            }

            if (pos.getStart() < pos.getEnd()) {
                positions.add(seq
                        + pos.getStart() + "-" + pos.getEnd());
            } else {
                positions.add(seq
                        + pos.getStart());
            }
        }

        return StringUtils.join(positions, ";");
    }

    public Collection<AAPosition> getResiduesA() {
        return queryResiduesA;
    }

    public Collection<AAPosition> getResiduesB() {
        if (homodimer) {
            return queryResiduesA;
        }
        return queryResiduesB;
    }

    public StructureSource getStructureSource() {
        return structureSource;
    }

    public boolean hasResiduesOnStructureA() {
        return structureMapper.proteinAHasResiduesOnStructure(); // hasInterfaceWithQueryResiduesB;
    }

    public Collection<String> getResiduesOnStructureA(String structureId) {
        return structureMapper.getStructuresResiduesA(structureId); // hasInterfaceWithQueryResiduesB;
    }

    public boolean hasInterfaceOnStructureA() {
        return structureMapper.proteinAHasInterfaceOnStructure(); // hasInterfaceWithQueryResiduesB;
    }

    public boolean hasInterfaceOnStructureB() {
        if (homodimer) {
            return structureMapper.proteinAHasInterfaceOnStructure();
        }
        return structureMapper.proteinBHasInterfaceOnStructure(); // hasInterfaceWithQueryResiduesB;
    }

    public boolean hasResiduesOnStructureB() {
        if (homodimer) {
            return structureMapper.proteinAHasResiduesOnStructure(); // hasInterfaceWithQueryResiduesB;
        }
        return structureMapper.proteinBHasResiduesOnStructure(); // hasInterfaceWithQueryResiduesB;
    }

    public Collection<String> getResiduesOnStructureB(String structureId) {
        if (homodimer) {
            return structureMapper.getStructuresResiduesA(structureId); // hasInterfaceWithQueryResiduesB;
        }
        return structureMapper.getStructuresResiduesB(structureId); // hasInterfaceWithQueryResiduesB;
    }

    public boolean hasResiduesOnStructure() {
        return structureMapper.hasResiduesOnStructure();
    }

    public boolean hasInterfaceOnStructure() {
        return structureMapper.hasInterfaceOnStructure();
    }

    public String getID() {
        return ID;
    }

    public String getQueryTaxid() {
        return queryTaxid;
    }

    public String getTrackId() {
        return trackId;
    }

    public Collection<SeqSymmetry> getSymmetries1() {
        return symmetries1;
    }

    public MoleculeEntry getInteractor1() {
        return container1.getEntry();
    }

    public Collection<ProteinStructure> getStructures1() {
        return structuresProteinA;
    }

    public String getChromosome1() {
        return container1.getChromosomeName();
    }

    public Collection<SeqSymmetry> getSymmetries2() {
        return symmetries2;
    }

    public MoleculeEntry getInteractor2() {
        return container2.getEntry();
    }

    public Collection<ProteinStructure> getStructures2() {
        return structuresProteinB;
    }

    public String getChromosome2() {
        return container2.getChromosomeName();
    }

    public Collection<InteractionStructure> getInteractionStructures() {
        return structuresInteraction;
    }

    public Collection<String> getStructuresWithQueryResiduesAtInterface() {
        return structureMapper.getStructuresQueryResiduesAtInterfaces();
    }

    public Collection<String> getStructuresWithQueryResidues() {
        return structureMapper.getStructuresWithQueryResidues();
    }

    public Collection<String> getStructuresResiduesAtInterfaces(String pdbId) {
        if (structureMapper.getStructuresQueryResiduesAtInterfaces().contains(
                pdbId)) {
            return structureMapper.getStructuresResiduesAtInterfaces(pdbId);
        } else {
            return Collections.emptyList();
        }
    }

    public Collection<String> getStructuresResidues(String pdbId) {
        if (structureMapper.getStructuresWithQueryResidues().contains(pdbId)) {
            return structureMapper.getStructuresResidues(pdbId);
        } else {
            return Collections.emptyList();
        }
    }

    public String getPsicquicUrl() {
        return psicquicUrl;
    }

    public String getContainerId1() {
        return "" + container1.getId();
    }

    public String getContainerId2() {
        return "" + container2.getId();
    }

    public static final String HTML_SCORE_0 = "<font color=\"#D3D3D3\">***</font>";
    public static final String HTML_SCORE_1 = "<font color=\"black\">*</font><font color=\"#D3D3D3\">**</font>";
    public static final String HTML_SCORE_2 = "<font color=\"black\">**</font><font color=\"#D3D3D3\">*</font>";
    public static final String HTML_SCORE_3 = "<font color=\"black\">***</font>";



    public String getDiseasesHtml() {
        Collection<String> diseasesA = container1.getEntry().getDiseases();
        Collection<String> diseasesB = container2.getEntry().getDiseases();
        ArrayList<String> commonDiseases = new ArrayList<>();
        ArrayList<String> uniqueDiseasesA = new ArrayList<>();
        ArrayList<String> uniqueDiseasesB = new ArrayList<>();

        commonDiseases.addAll(diseasesA);
        commonDiseases.retainAll(diseasesB);

        uniqueDiseasesA.addAll(diseasesA);
        uniqueDiseasesA.removeAll(uniqueDiseasesB);

        uniqueDiseasesB.addAll(diseasesB);
        uniqueDiseasesB.removeAll(uniqueDiseasesA);

        return "<html><font color=\"orange\">"
                + StringUtils.join(commonDiseases, ", ") + "</font> "
                + "<font color=\"green\">"
                + StringUtils.join(uniqueDiseasesA, ", ") + "</font> "
                + "<font color=\"blue\">"
                + StringUtils.join(uniqueDiseasesB, ", ") + "</font></html>";

    }

    public StructureSummary getStructureSummary() {
        return new StructureSummary(
                false == structuresProteinA.isEmpty(),
                hasResiduesOnStructureA(),
                hasInterfaceOnStructureA(),
                false == structuresProteinB.isEmpty(),
                hasResiduesOnStructureB(),
                hasInterfaceOnStructureB(),
                false == getInteractionStructures().isEmpty(),
                hasResiduesOnStructure(),
                hasInterfaceOnStructure());
    }

    public class StructureSummary implements Comparable<StructureSummary> {

        private final boolean hasStructureA;
        private final boolean hasResidueA;
        private final boolean hasInterfaceA;

        private final boolean hasStructureB;
        private final boolean hasResidueB;
        private final boolean hasInterfaceB;

        private final boolean hasStructure;
        private final boolean hasResidue;
        private final boolean hasInterface;

        public StructureSummary(boolean hasStructureA, boolean hasResidueA, boolean hasInterfaceA,
                boolean hasStructureB, boolean hasResidueB, boolean hasInterfaceB,
                boolean hasStructure, boolean hasResidue, boolean hasInterface) {
            super();
            this.hasStructureA = hasStructureA;
            this.hasResidueA = hasResidueA;
            this.hasInterfaceA = hasInterfaceA;
            this.hasStructureB = hasStructureB;
            this.hasResidueB = hasResidueB;
            this.hasInterfaceB = hasInterfaceB;
            this.hasStructure = hasStructure;
            this.hasResidue = hasResidue;
            this.hasInterface = hasInterface;
        }

        public boolean hasStructureA() {
            return hasStructureA;
        }

        public boolean hasResidueA() {
            return hasResidueA;
        }

        public boolean hasStructureB() {
            return hasStructureB;
        }

        public boolean hasResidueB() {
            return hasResidueB;
        }

        public boolean hasStructure() {
            return hasStructure;
        }

        public boolean hasResidue() {
            return hasResidue;
        }

        public boolean hasInterface() {
            return hasInterface;
        }

        private int getScore() {
            if (hasInterface) {
                return 7;
            }
            if (hasResidue) {
                return 6;
            }
            if (hasStructure) {
                return 5;
            }
            if (hasResidueA) {
                return 4;
            }
            if (hasStructureA) {
                return 3;
            }
            if (hasResidueB) {
                return 2;
            }
            if (hasStructureB) {
                return 1;
            }
            return 0;
        }

        @Override
        public int compareTo(StructureSummary o) {
            return this.getScore() - ((StructureSummary) o).getScore();
        }

        public boolean hasInterfaceA() {
            return hasInterfaceA;
        }

        public boolean hasInterfaceB() {
            return hasInterfaceB;
        }

    }

    public boolean isHomodimer() {
        return homodimer;
    }

    private final RangeMerger merger;

    private final RangeMerger mergerA;

    private final RangeMerger mergerB;

    public RangeMerger getRangeMerger() {
        return this.merger;
    }

    public TypeContainerAnnot createTrack() {

        // create track       
        TypeContainerAnnot interactorTrack = new TypeContainerAnnot(
                getTrackId());
        interactorTrack.setID(getTrackId());
        interactorTrack.setProperty(TrackLineParser.ITEM_RGB, Color.PINK);

        BioSeq aseq = GenometryModel.getInstance().getSelectedSeq();

        ArrayList<MISymContainer> resultSyms = new ArrayList<>();
        resultSyms.add(container1);
        if (false == this.isHomodimer()) {
            resultSyms.add(container2);
        }

        for (MISymContainer container : resultSyms) {
            SeqSymmetry symFound = container.getResultSym();

            if (symFound.getSpanCount() == 0) {
                continue;
            }

            BioSeq sequence = symFound.getSpan(0).getBioSeq();

            if (null == sequence.getAnnotation(trackId) && false == sequence.equals(aseq)) {
                sequence.addAnnotation(interactorTrack);
            }

            if (null == symFound.getSpan(sequence)) {
                continue;
            }

            if (symFound.getSpanCount() == 0
                    || symFound.getSpan(0) == null) {
                continue;
            }
            SeqSpan span = symFound.getSpan(sequence);

            ArrayList<Integer> emins = new ArrayList<>();
            ArrayList<Integer> emaxs = new ArrayList<>();

            if (merger.getRanges(sequence.getID()) != null) {
                for (Range range : merger.getRanges(sequence.getID())) {
                    if (range.getMin() >= span.getMin() && range.getMax() < span.getMax()) {
                        emins.add(range.getMin());
                        // from 0-based inclusive to 0-based exclusive
                        emaxs.add(range.getMax() + 1);
                    }
                }
            }

            int[] eminsA = new int[emaxs.size()];
            int[] emaxsA = new int[emaxs.size()];

            for (int i = 0; i < emaxs.size(); i++) {
                eminsA[i] = emins.get(i);
                emaxsA[i] = emaxs.get(i);
            }

            MIGeneSymmetry geneSym
                    = new MIGeneSymmetry(symFound.getID(), symFound.getID(), symFound.getID(),
                            sequence, span.isForward(), span.getMin(), span.getMax(),
                            span.getMin(), span.getMax(), eminsA, emaxsA, container.getMiGenes());

            if (eminsA.length > 0) {
                geneSym.setProperty(TrackLineParser.ITEM_RGB, Color.RED);
            } else {
                geneSym.setProperty(TrackLineParser.ITEM_RGB, Color.BLACK);
            }

            interactorTrack.addChild(geneSym);
        }

        this.hasTrack = true;

        return interactorTrack;

    }

    public HashSet<AAPosition> getInterfaceAAPositionsA() {
        return structureMapper.getInterfaceAAPositionsA();
    }

    public HashSet<AAPosition> getInterfaceAAPositionsB() {
        return structureMapper.getInterfaceAAPositionsB();
    }

    public boolean hasTrack() {
        return hasTrack;
    }

    public Interaction getInteraction() {
        return interaction;
    }

}
