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
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.lorainelab.igb.services.IgbService;
import it.iit.genomics.cru.bridges.ensembl.EnsemblClient;
import it.iit.genomics.cru.bridges.ensembl.EnsemblClientManager;
import it.iit.genomics.cru.bridges.ensembl.model.EnsemblException;
import it.iit.genomics.cru.bridges.ensembl.model.Exon;
import it.iit.genomics.cru.bridges.ensembl.model.Gene;
import it.iit.genomics.cru.igb.bundles.commons.business.IGBLogger;
import it.iit.genomics.cru.structures.model.MIExon;
import it.iit.genomics.cru.structures.model.MIGene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


/**
 * @author Arnaud Ceol
 *
 * Get information about genes and exons from Ensembl
 *
 */
public class EnsemblGeneManager extends GeneManager {

    private final IGBLogger igbLogger;
    
    private static final HashMap<String, EnsemblGeneManager> instances = new HashMap<>();

    private EnsemblClient client;

    protected IgbService igbService;

    protected EnsemblGeneManager(IgbService igbService, String species) {
        igbLogger = IGBLogger.getMainInstance();
        try {
            client = EnsemblClientManager.getInstance().getClient(species);
            this.igbService = igbService;
        } catch (EnsemblException e) {
            igbLogger.severe("Cannot access Ensembl MART");
        }
    }

    public static EnsemblGeneManager getInstance(IgbService igbService, String species) {
        if (false == instances.containsKey(species)) {
            EnsemblGeneManager instance = new EnsemblGeneManager(igbService, species);
            instances.put(species, instance);
        }

        return instances.get(species);
    }

    private HashMap<String, MIGene> cachedGenes = new HashMap<>();

    @Override
    public Collection<MIGene> getByPosition(String chromosome, int start, int end) {
        long startTime = System.currentTimeMillis();
        ArrayList<MIGene> results = new ArrayList<>();

        if (chromosome.startsWith("chr")) {
            chromosome = chromosome.substring(3);
        }

        // in cache?
        try {
            Collection<Gene> genes = client.getGenesByPosition(chromosome,
                    start, end);
            for (Gene gene : genes) {
                if (false == cachedGenes.containsKey(gene.getEnsemblGeneID())) {
                    MIGene miGene = new MIGene(gene.getEnsemblGeneID(), gene.getName(), gene.getChromosomeName(), gene.getStart(), gene.getEnd(), false == gene.isReverseStrand());
                    cachedGenes.put(gene.getEnsemblGeneID(), miGene);
                }
                results.add(cachedGenes.get(gene.getEnsemblGeneID()));
            }
        } catch (EnsemblException e) {
            igbLogger.severe(e.getMessage());
            igbLogger.getLogger().error( null, e);
        }

        long estimatedTime = System.currentTimeMillis() - startTime;
        igbLogger.info("getByEnsemblID " + estimatedTime + " ms");
        return results;
    }

    @Override
    public Collection<MIGene> getByID(String geneId) {
        long startTime = System.currentTimeMillis();

        ArrayList<MIGene> genes = new ArrayList<>();

        if (cachedGenes.containsKey(geneId)) {
            genes.add(cachedGenes.get(geneId));
            return genes;
        }

        // in cache?
        try {
            Collection<Gene> ensemblGenes = client.getGenesEnsemblGeneId(geneId);

            if (ensemblGenes.size() > 1) {
                igbLogger.severe(
                        "Strange: more than one entry for ensembl gene id "
                        + geneId);
            }

            if (ensemblGenes.isEmpty()) {
                igbLogger.severe(
                        "Strange: no gene found for ensembl gene id " + geneId);
                return null;
            }

            /**
             * TODO: check if only one
             */
            for (Gene gene : ensemblGenes) {
                if (null == inferSequenceName(gene.getChromosomeName())) {
                    continue;
                }

                MIGene miGene = new MIGene(gene.getEnsemblGeneID(), gene.getName(), gene.getChromosomeName(), gene.getStart(), gene.getEnd(), false == gene.isReverseStrand());

                cachedGenes.put(gene.getEnsemblGeneID(), miGene);
                genes.add(miGene);
                return genes;
            }
        } catch (EnsemblException e) {
            igbLogger.severe(e.getMessage());
            igbLogger.getLogger().error( null, e);
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        igbLogger.info("getByEnsemblID " + estimatedTime + " ms");
        return null;
    }

    @Override
    public void loadExons(MIGene gene) {
        try {
            long startTime = System.currentTimeMillis();
            // get exons
            Collection<Exon> exons = client.getExons(gene.getID());

            BioSeq chromosome = getSequence(gene.getChromosomeName());

            if (chromosome == null) {
                igbLogger.warning("Unavailable sequence: " + gene.getChromosomeName());
                return;
            }

            SimpleSeqSpan span = new SimpleSeqSpan(gene.getMin(),
                    gene.getMax(), chromosome);

            igbService.loadResidues(span, true);

            for (Exon exon : exons) {
                MIExon miExon = new MIExon(exon.getStart(), exon.getEnd());
                String sequence = chromosome.getResidues(exon.getStart(),
                        exon.getEnd());
                miExon.setSequence(sequence);
                gene.getExons().add(miExon);
            }

            long estimatedTime = System.currentTimeMillis() - startTime;
            igbLogger.info("getByEnsemblID " + estimatedTime + " ms");
        } catch (EnsemblException e) {
            igbLogger.severe(e.getMessage());
            igbLogger.getLogger().error( null, e);
        }
    }

}
