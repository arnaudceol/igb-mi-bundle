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

import java.util.HashSet;

import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;

import it.iit.genomics.cru.structures.model.MIGene;
import it.iit.genomics.cru.structures.model.MoleculeEntry;


public class MISymContainer {

    private static int containerId = 0;

    HashSet<MIGene> miGenes = new HashSet<>();

    //HashSet<MIGene> genes = new HashSet<>();

    SeqSymmetry resultSym;

    MoleculeEntry entry;

    private final int id;

    public int getId() {
        return id;
    }

    public MISymContainer() {
        this.id = containerId++;
    }

    public SeqSymmetry getResultSym() {
        return resultSym;
    }

    public void setResultSym(SeqSymmetry resultSym) {
        this.resultSym = resultSym;
    }

    public MoleculeEntry getEntry() {
        return entry;
    }

    public void setEntry(MoleculeEntry entry) {
        this.entry = entry;
    }

    public HashSet<MIGene> getMiGenes() {
        return miGenes;
    }

    public void addMIGene(MIGene e) {
        miGenes.add(e);
    }

    public String getChromosomeName() {
        return miGenes.iterator().next().getChromosomeName();
    }

}
