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
package it.iit.genomics.cru.igb.bundles.mi.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.google.common.collect.HashMultimap;

import it.iit.genomics.cru.igb.bundles.mi.business.IGBLogger;
import it.iit.genomics.cru.structures.model.AAPosition;
import it.iit.genomics.cru.structures.model.MIGene;
import it.iit.genomics.cru.structures.model.MoleculeEntry;

public class MISymManager {

    private final IGBLogger igbLogger;

    public MISymManager(String label) {
        igbLogger = IGBLogger.getInstance(label);
    }

    HashMap<MIGene, MISymContainer> geneSyms = new HashMap<>();

    // a selected symmetry can cover to more than one gene
    HashMultimap<MIGene, MISymContainer> querySyms = HashMultimap.create();

    HashMap<MoleculeEntry, MISymContainer> proteins = new HashMap<>();

    HashMap<String, MISymContainer> proteinAcs = new HashMap<>();

    HashMap<SeqSymmetry, MISymContainer> results = new HashMap<>();

    HashSet<MISymContainer> queryContainers = new HashSet<>();

    private final HashMultimap<MoleculeEntry, AAPosition> protein2QueryResidues = HashMultimap.create();

    public void addSelectedResidues(MoleculeEntry protein,
            Collection<AAPosition> sequences) {
        protein2QueryResidues.putAll(protein, sequences);
    }

    public Collection<AAPosition> getQueryResidues(
            MoleculeEntry protein) {
        return protein2QueryResidues.get(protein);
    }

    public boolean hasQueryResidues(MoleculeEntry protein) {
        return protein2QueryResidues.containsKey(protein);
    }

    public Collection<MISymContainer> getQueryContainers() {
        return queryContainers;
    }

    public Collection<MISymContainer> getUniprotContainers() {
        return proteins.values();
    }

    public MISymContainer getByGeneSymmetry(MIGene gene) {
        if (geneSyms.containsKey(gene)) {
            return geneSyms.get(gene);
        }

//        /**
//         * It should have been created by entry
//         */
//        igbLogger.severe("Asking for selected sym " + gene.getName()
//                + " whereas the gene has not been created yet");
        return null;
    }

    public Collection<MISymContainer> getBySelectedSymmetry(MIGene selectedSym) {
        if (querySyms.containsKey(selectedSym)) {
            return querySyms.get(selectedSym);
        }

//        /**
//         * It should have been created by entry
//         */
//        igbLogger.severe("Asking for selected sym " + selectedSym.getName()
//                + " whereas the geneSym has not been created yet");
        return null;
    }

    public MISymContainer getByProtein(MoleculeEntry protein) {
        if (proteins.containsKey(protein)) {
            return proteins.get(protein);
        }

        MISymContainer container = new MISymContainer();
        setEntry(container, protein);
        return container;
    }

    public MISymContainer getByProteinAc(String proteinAc) {
        if (proteinAcs.containsKey(proteinAc)) {
            return proteinAcs.get(proteinAc);
        }

        /**
         * It should never been created directly by ac, rather by entry!!
         */
        return null;
    }

    public MISymContainer getByResultSym(SeqSymmetry resultSym) {
        if (results.containsKey(resultSym)) {
            return results.get(resultSym);
        }

        MISymContainer container = new MISymContainer();
        container.setResultSym(resultSym);
        results.put(resultSym, container);

        return container;
    }

    public void addGeneSymmetry(MISymContainer container, MIGene e) {
        container.addMIGene(e);
        geneSyms.put(e, container);
    }

    public void addSelectedSymmetry(MISymContainer container, MIGene selectedSymmetry) {
        container.addMIGene(selectedSymmetry);
        querySyms.put(selectedSymmetry, container);
        queryContainers.add(container);
    }

    public void setResultSym(MISymContainer container, SeqSymmetry resultSym) {
        container.setResultSym(resultSym);
        results.put(resultSym, container);
    }

    public void setEntry(MISymContainer container, MoleculeEntry entry) {
        container.setEntry(entry);
        proteins.put(entry, container);
        proteinAcs.put(entry.getUniprotAc(), container);
    }

    public Collection<MIGene> getSelectedSyms() {
        return querySyms.keySet();
    }

    public Collection<MIGene> getGeneSyms() {
        return geneSyms.keySet();
    }

    public Collection<String> getProteinAcs() {
        return proteinAcs.keySet();
    }

    public Collection<MISymContainer> getInteractorContainers() {
        return results.values();
    }

}
