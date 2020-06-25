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
import it.iit.genomics.cru.structures.model.MIGene;

import java.util.Collection;
import java.util.List;


/**
 * @author Arnaud Ceol
 *
 * A GeneManager allow to query information about genes and exons from several
 * resources.
 *
 */
public abstract class GeneManager {

    /**
     * Chromosome names
     */
    private List<String> sequences;

    public void setSequences(List<String> sequences) {
        this.sequences = sequences;
    }

    public abstract Collection<MIGene> getByID(String geneId);

    public abstract Collection<MIGene> getByPosition(String chromosome,
            int start, int end);

    public abstract void loadExons(MIGene gene);

    public BioSeq getSequence(String name) {
        name = inferSequenceName(name);

        if (null == name) {
            return null;
        }

        return GenometryModel.getInstance().getSelectedGenomeVersion().getSeq(name);
    }

    /**
     * TODO: use synonyms instead
     *
     * @param name
     * @return
     */
    protected String inferSequenceName(String name) {
        if (false == sequences.contains(name)) {
            if (sequences.contains("chr" + name)) {
                name = "chr" + name;
            } else {
                return null;
            }
        }
        return name;
    }

}
