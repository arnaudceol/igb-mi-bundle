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
import com.affymetrix.genometry.color.RGB;
import com.affymetrix.genometry.parsers.TrackLineParser;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.style.SimpleTrackStyle;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.SimpleSymWithProps;
import com.affymetrix.genometry.symmetry.impl.TypeContainerAnnot;
import com.lorainelab.igb.genoviz.extensions.glyph.TierGlyph;
import com.lorainelab.igb.services.IgbService;

import it.iit.genomics.cru.bridges.interactome3d.local.Interactome3DLocalRepository;
import it.iit.genomics.cru.igb.bundles.commons.business.IGBLogger;
import it.iit.genomics.cru.igb.bundles.mi.business.genes.EnsemblGeneManager;
import it.iit.genomics.cru.igb.bundles.mi.business.genes.IGBQuickLoadGeneManager;
import it.iit.genomics.cru.bridges.interactome3d.local.I3DDownload;
import it.iit.genomics.cru.igb.bundles.mi.commons.MICommons;
import it.iit.genomics.cru.igb.bundles.mi.commons.MIView;
import it.iit.genomics.cru.igb.bundles.mi.model.MISymContainer;
import it.iit.genomics.cru.igb.bundles.mi.model.MISymManager;
import it.iit.genomics.cru.igb.bundles.mi.model.ProgressManager;
import it.iit.genomics.cru.igb.bundles.mi.query.AbstractMIQuery.QueryType;
import it.iit.genomics.cru.igb.bundles.mi.query.MIQuery;
import it.iit.genomics.cru.igb.bundles.mi.view.MIResultPanel;
import it.iit.genomics.cru.structures.bridges.pdb.PDBWSClient;
import it.iit.genomics.cru.structures.bridges.pdb.model.Chain;
import it.iit.genomics.cru.structures.bridges.pdb.model.Ligand;
import it.iit.genomics.cru.structures.bridges.pdb.model.MoleculeDescription;
import it.iit.genomics.cru.structures.bridges.pdb.model.Polymer;
import it.iit.genomics.cru.structures.bridges.pdb.model.StructureID;
import it.iit.genomics.cru.structures.bridges.psicquic.PsicquicUtils;
import it.iit.genomics.cru.structures.model.MoleculeEntry;
import it.iit.genomics.cru.structures.bridges.uniprot.UniprotkbUtils;
import it.iit.genomics.cru.structures.bridges.userData.UserStructuresManager;
import it.iit.genomics.cru.structures.model.AAPosition;
import it.iit.genomics.cru.structures.model.AAPositionManager;
import it.iit.genomics.cru.structures.model.ChainMapping;
import it.iit.genomics.cru.structures.model.MIGene;
import it.iit.genomics.cru.utils.maps.MapOfMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;

import it.iit.genomics.cru.igb.bundles.mi.commons.MIBundleConfiguration;
import it.iit.genomics.cru.igb.bundles.mi.genometry.MIGeneSymmetry;
import it.iit.genomics.cru.structures.bridges.commons.BridgesRemoteAccessException;
import it.iit.genomics.cru.structures.bridges.psicquic.Interaction;
import static it.iit.genomics.cru.structures.bridges.psicquic.Interaction.INTERACTION_TYPE_I3D;
import static it.iit.genomics.cru.structures.bridges.psicquic.Interaction.INTERACTION_TYPE_PDB;
import it.iit.genomics.cru.structures.bridges.psicquic.InteractionManager;
import it.iit.genomics.cru.structures.model.ModifiedResidue;
import it.iit.genomics.cru.structures.model.Range;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * @author Arnaud Ceol
 *
 *         Run the framework.
 *
 */
public class MIWorker extends SwingWorker<ArrayList<MIResult>, String> {

	private final IGBLogger igbLogger;

	private final JProgressBar progressBar;

	private final static String featureName = "mi";

	IgbService service;

	MIQuery query;

	List<MIResult> results;

	MISymManager symManager;

	IGBQuickLoadGeneManager geneManager;

	HashSet<SeqSymmetry> miSymmetries = new HashSet<>();

	String trackId;

	MapOfMap<MIGene, AAPosition> gene2pos = new MapOfMap<>();

	// We need the transcript sequences only for those entries
	HashSet<String> uniprotNeedMapping = new HashSet<>();

	// Associate Gene symmetries (from the genome loaded) to the selected
	// symmetries (e.g. mutations)
	MapOfMap<MIGene, SeqSymmetry> miGene2selectedSyms = new MapOfMap<>();

	public MIWorker(List<MIResult> results, IgbService service, MIQuery query, JProgressBar progressBar) {

		this.service = service;
		this.query = query;
		this.results = results;
		this.progressBar = progressBar;
		this.trackId = query.getLabel();

		igbLogger = IGBLogger.getInstance(trackId);

		progressBar.setIndeterminate(true);

		this.geneManager = IGBQuickLoadGeneManager.getInstance(service, query.getSpecies());
		geneManager.setSequences(query.getSequences());
	}

	@Override
	public ArrayList<MIResult> doInBackground() {

		// Display the log tab
		MIView.getInstance().getResultsTabbedPan().setSelectedIndex(0);

		UniprotkbUtils uniprotUtil = UniprotkbUtils.getInstance(query.getTaxid());

		// Create a new Symmetry manager for each query
		symManager = new MISymManager(this.trackId);

		Collection<SeqSymmetry> selectedSyms = query.getSelectedSymmetries();

		// Initialize progress property.
		progressBar.setIndeterminate(false);
		ProgressManager progressManager = new ProgressManager(5);

		setProgress(progressManager.getProgress());

		Set<String> queryUniprotAcs = new HashSet<>();
		Set<String> targetUniprotAcs = new HashSet<>();

		// interactions
		MapOfMap<String, String> uniprotAc2uniprotAcs;

		// Interaction found
		ArrayList<MIResult> resultsInBackground = new ArrayList<>();

		logAndPublish("map selection to genome and proteins..");

		// Step 1
		progressManager.nextMajorStep(selectedSyms.size());
		// Get gene Symmetries covered by query symmetries
		// don't create container at the moment: many genes syms for a single
		// gene may be present

		// Order list of syms
		SymListOrderer list = new SymListOrderer();

		for (SeqSymmetry querySym : selectedSyms) {
			list.addSymmetry(querySym);
		}

		for (BioSeq chr : list.getSequences()) {
			ArrayList<SeqSymmetry> querySyms = list.getSymmetries(chr);
			getGenes(querySyms, chr, true);
			// for (MIGene gene : getGenes(querySyms, chr, true)) {
			// // load exons (we only need this for the selected ones)
			// miGene2selectedSyms.add(gene, querySym);
			// }
			progressManager.nextStep();
			setProgress(progressManager.getProgress());
		}

		// Step 2
		progressManager.nextMajorStep(miGene2selectedSyms.keySet().size());
		// Associate selected residues
		for (MIGene gene : miGene2selectedSyms.keySet()) {

			logAndPublish("Associate residues to " + gene.getID());

			MoleculeEntry protein = gene.getProtein();

			if (protein != null) {
				queryUniprotAcs.add(protein.getUniprotAc());

				MISymContainer container = symManager.getByProtein(protein);
				symManager.addGeneSymmetry(container, gene);

				symManager.addSelectedSymmetry(container, gene);
			} else {
				igbLogger.getLogger().warn("No protein for {0}", gene.getID());
			}
			progressManager.nextStep();
			setProgress(progressManager.getProgress());
		}

		// Step 3
		progressManager.nextMajorStep(queryUniprotAcs.size());

		// Get interactors
		uniprotAc2uniprotAcs = new MapOfMap<>(queryUniprotAcs);

		logAndPublish("get interactions");

		HashMap<String, MoleculeEntry> targetUniprotEntries = new HashMap<>();

		InteractionManager interactors = new InteractionManager();

		for (String ac : queryUniprotAcs) {

			logAndPublish("Get interactions for " + ac);

			if (ac == null) {
				continue;
			}

			try {
				if (false == PsicquicInitWorker.nullServer.equals(query.getPsiquicServer())
						&& null != query.getPsiquicServer()) {
					for (Interaction interaction : PsicquicUtils.getInstance().getInteractors(query.getPsiquicServer(),
							ac)) {
						interactors.merge(interaction);
					}
				}
			} catch (BridgesRemoteAccessException be) {
				igbLogger.severe("Cannot access PSICQUIC server!");
				break;
			}

			// Add interactors from User structures?
			if (null != query.getUserStructuresPath() && query.searchUserStructures()) {
				Interactome3DLocalRepository userStructures = UserStructuresManager.getInstance()
						.getUserRepository(query.getUserStructuresPath());
				for (String interactorAc : userStructures.getInteractors(ac)) {
					interactors.getOrCreateInteraction(ac, interactorAc).addType(INTERACTION_TYPE_I3D);
					uniprotNeedMapping.add(interactorAc);
				}
			}

			// Add interactors from I3D?
			if (query.searchInteractome3D() || query.searchDSysMap()) {
				// Check or download I3D interaction file
				I3DDownload download = new I3DDownload(MIBundleConfiguration.getInstance().getCachePath());

				if (false == download.isDatDownloaded(query.getTaxid())) {
					logAndPublish("download interactions from Interactome3D");
					download.downloadDat(query.getTaxid());
				}

				// get interactions
				Interactome3DLocalRepository userStructures = UserStructuresManager.getInstance()
						.getUserRepository(download.getI3DdatPath(query.getTaxid()));
				for (String interactorAc : userStructures.getInteractors(ac)) {
					interactors.getOrCreateInteraction(ac, interactorAc).addType("direct interaction (Interactome3D)");
					uniprotNeedMapping.add(interactorAc);
				}
			}

			// add interactors from PDB structures
			if (query.searchPDB() || query.searchPDBLocal() || query.searchEPPIC()) {
				MoleculeEntry entry = symManager.getByProteinAc(ac).getEntry();

				PDBWSClient client = new PDBWSClient();
				// Do only 10 by 10
				List<String> pdbs = new ArrayList<>();
				pdbs.addAll(entry.getPdbs());

				while (false == pdbs.isEmpty()) {
					List<String> subset = pdbs.subList(0, Math.min(10, pdbs.size()));
					pdbs = pdbs.subList(Math.min(10, pdbs.size()), pdbs.size());

					if (query.searchPPI() || query.searchNucleicAcid()) {

						MoleculeDescription molDesc;
						try {
							molDesc = client.getDescription(subset);
						} catch (BridgesRemoteAccessException be) {
							igbLogger.severe("Cannot access PDB!");
							break;
						}

						if (molDesc != null) {
							for (StructureID structureId : molDesc.getStructureId()) {

								for (Polymer polymer : structureId.getPolymers()) {
									if (polymer.getPolymerDescription() == null) {
										igbLogger.severe("No description for " + structureId.getId());
									}
									if (null != polymer.getType()) {
										switch (polymer.getType()) {
										case "protein":
											if (query.searchPPI() && null != polymer.getMacromolecule()) {
												String proteinAc = polymer.getMacromolecule().getAccession().get(0);

												if (false == proteinAc.equals(entry.getUniprotAc())
														|| polymer.getChains().size() > 1) {

													interactors.getOrCreateInteraction(ac, proteinAc)
															.addType(INTERACTION_TYPE_PDB);
													uniprotNeedMapping.add(ac);
												}
											}
											break;
										case "dna":
											if (false == query.searchNucleicAcid()) {
												break;
											}
											// Merge all DNA entries, use "DNA
											// as name rather that the
											// desciption
											MISymContainer dnaSym = symManager.getByProteinAc(MoleculeEntry.TAXID_DNA);
											uniprotNeedMapping.add(ac);

											interactors.getOrCreateInteraction(ac, MoleculeEntry.TAXID_DNA)
													.addType(INTERACTION_TYPE_PDB);

											if (dnaSym == null) {
												MoleculeEntry dnaEntry = new MoleculeEntry(MoleculeEntry.TAXID_DNA);
												dnaEntry.setSequence("");
												dnaEntry.setTaxid(MoleculeEntry.TAXID_DNA);

												targetUniprotEntries.put(MoleculeEntry.TAXID_DNA, dnaEntry);
												dnaEntry.addGeneName(MoleculeEntry.TAXID_DNA);

												dnaSym = symManager.getByProtein(dnaEntry);
											}

											MoleculeEntry dnaEntry = dnaSym.getEntry();

											for (Chain chain : polymer.getChains()) {
												ChainMapping chainMapping = new ChainMapping(structureId.getId(),
														chain.getId(), 0, 0);
												dnaEntry.addChain(structureId.getId(), chainMapping, "unspecified");
											}

											break;

										case "rna":
											if (false == query.searchNucleicAcid()) {
												break;
											}
											uniprotNeedMapping.add(ac);
											// Merge all RNA entries, use "RNA
											// as name rather that the
											// desciption
											MISymContainer rnaSym = symManager.getByProteinAc(MoleculeEntry.TAXID_RNA);

											interactors.getOrCreateInteraction(ac, MoleculeEntry.TAXID_RNA)
													.addType(INTERACTION_TYPE_PDB);

											if (rnaSym == null) {
												MoleculeEntry rnaEntry = new MoleculeEntry(MoleculeEntry.TAXID_RNA);
												rnaEntry.setSequence("");
												rnaEntry.setTaxid(MoleculeEntry.TAXID_RNA);

												targetUniprotEntries.put(MoleculeEntry.TAXID_RNA, rnaEntry);
												rnaEntry.addGeneName(MoleculeEntry.TAXID_RNA);

												rnaSym = symManager.getByProtein(rnaEntry);
											}

											MoleculeEntry rnaEntry = rnaSym.getEntry();

											for (Chain chain : polymer.getChains()) {
												ChainMapping chainMapping = new ChainMapping(structureId.getId(),
														chain.getId(), 0, 0);
												rnaEntry.addChain(structureId.getId(), chainMapping, "unspecified");
											}

											break;
										}
									}
								}
							}
						}
					}
					if (query.searchLigands() && false == query.searchEPPIC()) {
						try {
							for (Ligand ligand : client.getLigands(subset)) {

								/**
								 * Only non polymer ligands
								 */
								if (false == ligand.isNonPolymer()) {
									continue;
								}

								int numAtoms = 0;

								for (String atom : ligand.getFormula().split(" ")) {

									String num = atom.replaceAll("\\D+", "").trim();
									if ("".equals(num)) {
										numAtoms++;
									} else {
										numAtoms += Integer.parseInt(num);
									}
								}

								if (numAtoms <= 10) {
									igbLogger.info("Skip ligand: " + ligand.getFormula());
									continue;
								}
								uniprotNeedMapping.add(ac);
								MISymContainer misym = symManager.getByProteinAc(ligand.getChemicalName());

								interactors.getOrCreateInteraction(ac, ligand.getChemicalName())
										.addType(INTERACTION_TYPE_PDB);

								if (misym == null) {
									MoleculeEntry ligandEntry = new MoleculeEntry(ligand.getChemicalName());
									ligandEntry.setSequence("");
									ligandEntry.setTaxid(MoleculeEntry.TAXID_LIGAND);

									ligandEntry.addGeneName(ligand.getChemicalId());
									targetUniprotEntries.put(ligand.getChemicalName(), ligandEntry);

									misym = symManager.getByProtein(ligandEntry);
								}

								MoleculeEntry ligandEntry = misym.getEntry();

								ChainMapping chainMapping = new ChainMapping(ligand.getStructureId(), "ligand", 0, 0);
								ligandEntry.addChain(ligand.getStructureId(), chainMapping, "unspecified");

							}
						} catch (BridgesRemoteAccessException be) {
							igbLogger.severe("Cannot access PDB!");
							break;
						}
					}
				}

			}

			if (query.searchModifications()) {
				MoleculeEntry entry = symManager.getByProteinAc(ac).getEntry();

				for (ModifiedResidue modification : entry.getModifications()) {
					MISymContainer misym = symManager.getByProteinAc(modification.getDescription());

					uniprotNeedMapping.add(ac);
					if (misym == null) {

						interactors.getOrCreateInteraction(ac, modification.getDescription())
								.addType("direct interaction (Uniprot)");
						// interactors.add(modification.getDescription(),
						// "association");
						MoleculeEntry ligandEntry = new MoleculeEntry(modification.getDescription());
						ligandEntry.setSequence("");
						ligandEntry.setTaxid(MoleculeEntry.TAXID_MODIFICATION);

						ligandEntry.addGeneName(modification.getDescription());
						targetUniprotEntries.put(modification.getDescription(), ligandEntry);

						symManager.getByProtein(ligandEntry);
					}

				}

			}

			Collection<String> interactorUniprotAcs = interactors.getInteractors();

			for (String interactorUniprotAc : interactorUniprotAcs) {
				// Skip interaction if we the type of query is INTRA (i.e. only
				// interactions between selected genes)
				// and one of the protein was not selected
				if (QueryType.EXTRA.equals(query.getQueryType()) || queryUniprotAcs.contains(interactorUniprotAc)) {
					uniprotAc2uniprotAcs.add(ac, interactorUniprotAc);
					targetUniprotAcs.add(interactorUniprotAc);

					// String key = ac + "#" + interactorUniprotAc;
					// interactionTypes.addAll(key,
					// interactors.get(interactorUniprotAc));
					// At this point we may not have created the symmetry
				}
			}

			progressManager.nextStep();
			setProgress(progressManager.getProgress());
		}

		// Only look for uniprot Acs for which we don't have an entry yet
		HashSet<String> uniprotAcToSearch = new HashSet<>();

		uniprotAcToSearch.addAll(targetUniprotAcs);

		uniprotAcToSearch.removeAll(symManager.getProteinAcs());

		// Allow proteins from other species
		try {
			targetUniprotEntries.putAll(uniprotUtil.getUniprotEntriesFromUniprotAccessions(uniprotAcToSearch, false));
		} catch (BridgesRemoteAccessException be) {
			igbLogger.severe("Cannot access Uniprot!");

			return resultsInBackground;
		}

		for (MoleculeEntry entry : targetUniprotEntries.values()) {
			MISymContainer container = symManager.getByProtein(entry);
			if (container == null) {
			}
		}

		// missing ones?
		Collection<String> missingUniprotAcs = new ArrayList<>();

		missingUniprotAcs.addAll(uniprotAcToSearch);

		missingUniprotAcs.removeAll(targetUniprotEntries.keySet());

		for (String missingAc : missingUniprotAcs) {
			MICommons.getInstance().addProteinToBlackList(missingAc);
		}

		for (MISymContainer container : symManager.getQueryContainers()) {
			if (null != container.getEntry()) {
				targetUniprotEntries.put(container.getEntry().getUniprotAc(), container.getEntry());
			}
		}

		// Do I need it if I don't need symmetries?
		// Step 4
		progressManager.nextMajorStep(targetUniprotEntries.values().size());
		for (MoleculeEntry uniprotEntry : targetUniprotEntries.values()) {

			logAndPublish("create symmetry for " + uniprotEntry.getUniprotAc());

			// Get symmetry, it has not been necessarily created
			MISymContainer container = symManager.getByProtein(uniprotEntry);

			Collection<String> geneIds;

			// Check if we are using Ensembl web service or QuickLoad.
			if (EnsemblGeneManager.class.isInstance(geneManager)) {
				geneIds = uniprotEntry.getEnsemblGenes();
			} else {
				geneIds = new HashSet<>();
				geneIds.addAll(uniprotEntry.getGeneNames());
				geneIds.addAll(uniprotEntry.getRefseqs());
				geneIds.addAll(uniprotEntry.getEnsemblGenes());
			}

			SimpleSymWithProps overlappingSym = new SimpleSymWithProps();
			overlappingSym.setProperty(TrackLineParser.ITEM_RGB, Color.RED);

			overlappingSym.setID(this.trackId + "-" + uniprotEntry.getGeneName());

			for (String geneId : geneIds) {

				Collection<MIGene> genes = geneManager.getByID(geneId);

				// For each gene create a "result symmetry", which will be
				// displayed in the interaction track
				if (genes.isEmpty()) {
					continue;
				}

				RangeMerger merger = new RangeMerger();

				for (MIGene gene : genes) {

					if (null == gene) {
						continue;
					}

					if (null != uniprotEntry.getVarSpliceAC(gene.getID())) {

						gene.getUniprotAcs().add(uniprotEntry.getUniprotAc());
						gene.setProtein(uniprotEntry);
						symManager.addGeneSymmetry(container, gene);

						BioSeq chromosome = geneManager.getSequence(gene.getChromosomeName());

						if (chromosome == null) {
							igbLogger.severe("Unavailable sequence: " + gene.getChromosomeName()
									+ ", there may be a network problem.");
							continue;
						}

						merger.addRange(chromosome.getId(), new Range(gene.getMin(), gene.getMax()));
					}
				}

				for (String seq : merger.getSequences()) {
					BioSeq chromosome = geneManager.getSequence(seq);

					if (chromosome == null) {
						igbLogger.severe("No sequence for chromosome: " + seq);
					}
					for (Range range : merger.getRanges(seq)) {
						SeqSpan span = new SimpleSeqSpan(range.getMin(), range.getMax(), chromosome);

						// Check if it has already this span
						boolean hasSpan = false;
						for (int i = 0; i < overlappingSym.getSpanCount(); i++) {
							SeqSpan otherSpan = overlappingSym.getSpan(i);
							if (otherSpan.getMin() == span.getMin() && otherSpan.getMax() == span.getMax()) {
								hasSpan = true;
								break;
							}
						}

						if (false == hasSpan) {
							overlappingSym.addSpan(span);
						}
					}
				}

				if (false == genes.isEmpty()) {
					// we found it
					break;
				}
			}

			symManager.setResultSym(container, overlappingSym);

			progressManager.nextStep();
			setProgress(progressManager.getProgress());

		}

		for (String ac : uniprotNeedMapping) {
			MISymContainer proteinContainer = symManager.getByProteinAc(ac);
			for (MIGene gene : proteinContainer.getMiGenes()) {

				if (false == miGene2selectedSyms.containsKey(gene)) {
					continue;
				}
				for (SeqSymmetry selectedSym : miGene2selectedSyms.get(gene)) {
					logAndPublish("Load residues for " + gene.getID());
					geneManager.loadTranscriptSequence(selectedSym.getSpanSeq(0), gene);

					// Maybe the protein was already assigned to the gene.
					// In order to be sure we are working on the right one,
					// Don't use the protein variable, but get it fromthe gene
					ArrayList<AAPosition> aaPositions = new ArrayList<>();

					// symmetry are 0-based exclusive,
					// use max -1 to have inclusive coordinates
					Collection<AAPosition> positions = AAPositionManager.getAAPositionManager(query.getLabel())
							.getAAPositions(gene, selectedSym.getSpan(0).getMin(), selectedSym.getSpan(0).getMax() - 1);
					aaPositions.addAll(positions);

					for (AAPosition aa : aaPositions) {
						gene2pos.add(gene, aa);
					}
					symManager.addSelectedResidues(gene.getProtein(), aaPositions);
				}
			}
		}

		// Step 5
		// don't add twice the same interaction
		HashSet<String> interactionsDone = new HashSet<>();

		progressManager.nextMajorStep(symManager.getQueryContainers().size());

		for (MISymContainer container : symManager.getQueryContainers()) {

			logAndPublish(container.getEntry().getGeneName());

			if (null == container.getEntry()) {
				continue;
			}

			if (null == container.getResultSym()) {
				continue;
			}

			String queryUniprotAc = container.getEntry().getUniprotAc();

			if (null == uniprotAc2uniprotAcs.get(queryUniprotAc)) {
				continue;
			}

			if (MICommons.getInstance().isBlackListed(queryUniprotAc)) {
				continue;
			}

			for (String targetUniprotAc : uniprotAc2uniprotAcs.get(queryUniprotAc)) {

				if (MICommons.getInstance().isBlackListed(targetUniprotAc)) {
					continue;
				}

				// An interaction may be slected twice, as A-B and B-A,
				// avoid this.
				if (interactionsDone.contains(targetUniprotAc + "#" + queryUniprotAc)
						|| interactionsDone.contains(queryUniprotAc + "#" + targetUniprotAc)) {
					continue;
				}
				interactionsDone.add(queryUniprotAc + "#" + targetUniprotAc);

				MISymContainer targetContainer = symManager.getByProteinAc(targetUniprotAc);

				if (targetContainer == null) {
					continue;
				}

				if (targetContainer.getEntry() == null) {
					continue;
				}

				if (targetContainer.getResultSym() == null) {
					continue;
				}

				MIResult result = new MIResult(trackId, container, targetContainer,
						interactors.getOrCreateInteraction(container.getEntry().getUniprotAc(),
								targetContainer.getEntry().getUniprotAc()),
						query, symManager);

				resultsInBackground.add(result);
				miSymmetries.add(targetContainer.getResultSym());
			}

			progressManager.nextStep();

			setProgress(progressManager.getProgress());
		}

		AAPositionManager.removeManager(query.getLabel());

		return resultsInBackground;

	}

	@Override
	protected void process(List<String> chunks) {
		for (String message : chunks) {
			progressBar.setValue(getProgress());
			progressBar.setString(query.getLabel() + ": " + message);
		}
	}

	/**
	 * Get the symmetries at the same position on the main sequence, i.e. get
	 * the gene corresponding to a selected Symmetry.
	 *
	 * @param selectedSymmetry
	 * @return
	 */
	private void getGenes(ArrayList<SeqSymmetry> selectedSymmetries, BioSeq seq, boolean ignoreIfNoProtein) {

		// Don't map residues if they cover a full gene
		boolean skipSearchResidues = false;

		MapOfMap<String, MoleculeEntry> proteins = new MapOfMap<>();

		HashSet<String> searchGeneNames = new HashSet<>();
		HashSet<String> searchRefSeq = new HashSet<>();
		HashSet<String> searchEnsembl = new HashSet<>();

		MapOfMap<SeqSymmetry, MIGene> candidates = geneManager.getBySymList(seq, selectedSymmetries);

		/**
		 * TODO : may be more than one!!!
		 */
		for (SeqSymmetry sym : candidates.keySet()) {
			logAndPublish("map " + sym.getID());
			for (MIGene gene : candidates.get(sym)) {

				String refseqPattern = "[A-Z]{2}\\_[0-9]+";
				if (gene.getID().matches(refseqPattern)) {
					searchRefSeq.add(gene.getID());
				} else if (gene.getID().startsWith("ENS")) {
					searchEnsembl.add(gene.getID());
				} else {
					searchGeneNames.add(gene.getID());
				}
			}

			try {
				if (false == searchRefSeq.isEmpty()) {
					proteins.merge(
							UniprotkbUtils.getInstance(query.getTaxid()).getUniprotEntriesFromRefSeqs(searchRefSeq));

				}

				if (false == searchEnsembl.isEmpty()) {
					proteins.merge(
							UniprotkbUtils.getInstance(query.getTaxid()).getUniprotEntriesFromEnsembl(searchEnsembl));
				}

				if (false == searchGeneNames.isEmpty()) {
					proteins.merge(
							UniprotkbUtils.getInstance(query.getTaxid()).getUniprotEntriesFromGenes(searchGeneNames));
				}
			} catch (BridgesRemoteAccessException be) {
				igbLogger.severe("Cannot access Uniprot!");
			}

			for (MIGene gene : candidates.get(sym)) {
				// Try to find the best protein,
				// e.g. Swissprot rather than Trembl
				MoleculeEntry protein = null;

				for (MoleculeEntry uniprotProtein : proteins.get(gene.getID())) {
					if (protein == null) {
						protein = uniprotProtein;
					} else {
						if (false == protein.isSwissprot() && uniprotProtein.isSwissprot()) {
							protein = uniprotProtein;
						}
					}
				}

				if (protein == null) {
					igbLogger.warning("No protein for gene " + gene.getID());
				} else {
					miGene2selectedSyms.add(gene, sym);

					gene.getUniprotAcs().add(protein.getUniprotAc());
					gene.setProtein(protein);

				}
			}

		}

	}

	@Override
	protected void done() {

		boolean failed = false;

		try {
			results.addAll(get());
		} catch (InterruptedException | ExecutionException ignore) {
			igbLogger.getLogger().error("Fail to analyze the selected regions", ignore);
			failed = true;
		}

		HashSet<String> querySummaryParts = new HashSet<>();
		HashSet<String> queryChromosomes = new HashSet<>();

		for (MISymContainer container : symManager.getQueryContainers()) {
			String chr = container.getChromosomeName();

			String protein = "<b>" + container.getEntry().getGeneName() + "</b>/" + container.getEntry().getUniprotAc();

			querySummaryParts.add(protein);
			queryChromosomes.add(chr.replace("chr", ""));

		}

		String querySummary = "Query: type=" + query.getQueryType() + ", "
				+ PsicquicUtils.getInstance().getServerName(query.getPsiquicServer());

		if (query.searchEPPIC()) {
			querySummary += " EPPIC ";
		}

		if (query.searchPDB()) {
			querySummary += " PDB ";
		}

		if (query.searchInteractome3D()) {
			querySummary += " Interactome3D ";
		}
		if (query.searchDSysMap()) {
			querySummary += " DSysMap ";
		}
		
		if (query.searchPPI()) {
			querySummary += ", protein-protein";
		}
		
		if (query.searchNucleicAcid()) {
			querySummary += ", DNA/RNA";
		}

		if (query.searchLigands()) {
			querySummary += ", small molecules";
		}

		querySummary += "<br/>";

		if (querySummaryParts.size() <= 5) {
			querySummary += StringUtils.join(querySummaryParts, ", ");
		} else {
			querySummary += querySummaryParts.size() + " genes on chromosome(s) "
					+ StringUtils.join(queryChromosomes, ",");
		}

		if (failed) {
			querySummary += " <b><font color=\"red\">The query failed, please check the log for more informations.</font></b>";
		} else if (igbLogger.hasError()) {
			querySummary += " <font color=\"red\">Some errors happend, please check the log for more informations.</font>";
		}

		addResultTab(querySummary, results, query.getLabel(), query);

		MIView.getInstance().getMiConfigurationPanel().updateCacheLabel();

		createTrack();
		setProgress(100);
		logAndPublish("done");

		igbLogger.info("Query over.");
	}

	public void addResultTab(String summary, List<MIResult> results, String label, MIQuery query) {

		JTabbedPane resultsTabbedPan = MIView.getInstance().getResultsTabbedPan();

		MIResultPanel resultPane = new MIResultPanel(service, summary, results, label, query);

		addClosableTab(resultsTabbedPan, resultPane, label);
		// select the new (last) tab
		resultsTabbedPan.setSelectedIndex(resultsTabbedPan.getTabCount() - 1);

	}

	/**
	 * Adds a component to a JTabbedPane with a little “close tab" button on the
	 * right side of the tab.
	 *
	 * @param tabbedPane
	 *            the JTabbedPane
	 * @param c
	 *            any JComponent
	 * @param title
	 *            the title for the tab
	 */
	public static void addClosableTab(final JTabbedPane tabbedPane, final JComponent c, final String title) {
		// Add the tab to the pane without any label
		tabbedPane.addTab(null, c);
		int pos = tabbedPane.indexOfComponent(c);

		// Create a FlowLayout that will space things 5px apart
		FlowLayout f = new FlowLayout(FlowLayout.CENTER, 5, 0);

		// Make a small JPanel with the layout and make it non-opaque
		JPanel pnlTab = new JPanel(f);
		pnlTab.setOpaque(false);

		// Add a JLabel with title and the left-side tab icon
		JLabel lblTitle = new JLabel(title);

		// Create a JButton for the close tab button
		JButton btnClose = new JButton("x");
		// btnClose.setOpaque(false);
		int size = 17;
		btnClose.setPreferredSize(new Dimension(size, size));
		btnClose.setToolTipText("close this tab");
		// Make the button looks the same for all Laf's
		btnClose.setUI(new BasicButtonUI());
		// Make it transparent
		btnClose.setContentAreaFilled(false);
		// No need to be focusable
		btnClose.setFocusable(false);
		btnClose.setBorder(BorderFactory.createEtchedBorder());
		btnClose.setBorderPainted(false);
		// Making nice rollover effect
		// we use the same listener for all buttons
		btnClose.setRolloverEnabled(true);
		// Close the proper tab by clicking the button

		// Configure icon and rollover icon for button
		btnClose.setRolloverEnabled(true);

		// Set border null so the button doesn’t make the tab too big
		btnClose.setBorder(null);
		// Make sure the button can’t get focus, otherwise it looks funny
		btnClose.setFocusable(false);

		// Put the panel together
		pnlTab.add(lblTitle);
		pnlTab.add(btnClose);

		// Add a thin border to keep the image below the top edge of the tab
		// when the tab is selected
		pnlTab.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		// Now assign the component for the tab
		tabbedPane.setTabComponentAt(pos, pnlTab);

		// Add the listener that removes the tab
		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(new JFrame(),
						"Are you sure you want to remove this tab? All results will be lost!", "Remove tab",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					tabbedPane.remove(c);
				}
			}
		};
		btnClose.addActionListener(listener);

		// Optionally bring the new tab to the front
		tabbedPane.setSelectedComponent(c);

	}

	/**
	 * Create a single track with each gene found and each contact
	 */
	private void createTrack() {
		TypeContainerAnnot interactorTrack = new TypeContainerAnnot(trackId);
		interactorTrack.setID(trackId);
		interactorTrack.setProperty(TrackLineParser.ITEM_RGB, Color.PINK);

		BioSeq aseq = GenometryModel.getInstance().getSelectedSeq().get();

		RangeMerger merger = new RangeMerger();

		for (MIResult result : results) {
			merger.merge(result.getRangeMerger());
		}

		for (SeqSymmetry symFound : miSymmetries) {

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

			if (symFound.getSpanCount() == 0 || symFound.getSpan(0) == null) {
				continue;
			}
			SeqSpan span = symFound.getSpan(sequence);

			ArrayList<Integer> emins = new ArrayList<>();
			ArrayList<Integer> emaxs = new ArrayList<>();

			if (merger.getRanges(sequence.getId()) != null) {
				for (Range range : merger.getRanges(sequence.getId())) {
					if (range.getMin() >= span.getMin() && range.getMax() <= span.getMax()) {
						emins.add(range.getMin());
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

			MIGeneSymmetry geneSym = new MIGeneSymmetry(symFound.getID(), symFound.getID(), symFound.getID(), sequence,
					span.isForward(), span.getMin(), span.getMax(), span.getMin(), span.getMax(), eminsA, emaxsA,
					symManager.getByResultSym(symFound).getMiGenes());

			if (eminsA.length > 0) {
				geneSym.setProperty(TrackLineParser.ITEM_RGB, Color.RED);
			} else {
				geneSym.setProperty(TrackLineParser.ITEM_RGB, Color.BLACK);
			}

			interactorTrack.addChild(geneSym);
		}

		service.addTrack(interactorTrack, trackId);

		service.getSeqMapView().updatePanel();

		for (TierGlyph t : service.getAllTierGlyphs()) {

			if (TierGlyph.TierType.ANNOTATION.equals(t.getTierType())
					&& (t.getAnnotStyle().getTrackName().equals(trackId))) {

				SimpleTrackStyle style = new SimpleTrackStyle(trackId, false) {

					@Override
					public boolean drawCollapseControl() {
						return false;
					}
				};

				t.getAnnotStyle().copyPropertiesFrom(style);
				t.getAnnotStyle().setColorProvider(new RGB());
				interactorTrack.setProperty(TrackLineParser.ITEM_RGB, "on");
			}
		}

		service.getSeqMapView().updatePanel();

	}

	private void logAndPublish(String message) {
		igbLogger.info(message);
		publish(message);
	}

	private class SymListOrderer {

		HashMap<BioSeq, ArrayList<SeqSymmetry>> chrToSyms = new HashMap<>();

		public void addSymmetry(SeqSymmetry symToInsert) {
			BioSeq chr = symToInsert.getSpan(0).getBioSeq();

			if (false == chrToSyms.containsKey(chr)) {
				ArrayList<SeqSymmetry> syms = new ArrayList<>();
				chrToSyms.put(chr, syms);
			}

			ArrayList<SeqSymmetry> syms = chrToSyms.get(chr);

			int index = 0;

			while (index < syms.size()) {
				SeqSymmetry sym = syms.get(index);
				if (symToInsert.getSpan(0).getMin() <= sym.getSpan(0).getMin()) {
					syms.add(index, symToInsert);
					return;
				}
				index++;

			}
			syms.add(symToInsert);

		}

		public Collection<BioSeq> getSequences() {
			return chrToSyms.keySet();
		}

		public ArrayList<SeqSymmetry> getSymmetries(BioSeq chr) {
			return chrToSyms.get(chr);
		}

	}

}
