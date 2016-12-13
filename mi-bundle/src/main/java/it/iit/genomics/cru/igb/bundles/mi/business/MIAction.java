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

import static javax.swing.JOptionPane.ERROR_MESSAGE;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.lorainelab.igb.services.IgbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.event.SymSelectionEvent;
import com.affymetrix.genometry.event.SymSelectionListener;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;

import it.iit.genomics.cru.igb.bundles.mi.commons.MIView;
import it.iit.genomics.cru.igb.bundles.mi.query.MIQuery;
import it.iit.genomics.cru.igb.bundles.mi.query.MIQueryManager;
import it.iit.genomics.cru.igb.bundles.mi.view.ProgressPanel;
import it.iit.genomics.cru.structures.bridges.commons.BridgesRemoteAccessException;
import it.iit.genomics.cru.structures.bridges.uniprot.UniprotkbUtils;

/**
 * @author Arnaud Ceol
 *
 * Main action for the MI Bundle. Create a Query based on the user choices and
 * run the framework. The actual work is done in background by the MIWorker.
 */
public class MIAction extends GenericAction  implements SymSelectionListener {

	private static final Logger logger = LoggerFactory.getLogger(MIAction.class);
	
    private static final long serialVersionUID = 1L;
    private final IgbService igbService;

    List<SeqSymmetry> selected_syms ;
    
    public MIAction(IgbService igbService) {
        super("MI Search", KeyEvent.VK_Z);
        this.igbService = igbService;
        GenometryModel.getInstance().addSymSelectionListener(this);        
        selected_syms = new ArrayList<>();
    }
    

	@Override
	public void symSelectionChanged(SymSelectionEvent evt) {
		selected_syms = evt.getSelectedGraphSyms();
	}

    @Override
    public void actionPerformed(ActionEvent event) {
        super.actionPerformed(event);

        // MIView.getInstance().getStructurePanel().clean();
        // Check that sequences have been initialized
        try {
            if (MIQueryManager.getInstance().getSequences().isEmpty()) {
                for (String[] species : UniprotkbUtils.getSpeciesFromName(igbService.getSelectedSpecies())) {
                    MIQueryManager.getInstance().setSpecies(
                            igbService.getSelectedSpecies());
                    MIQueryManager.getInstance().setTaxid(species[1]);

                    ArrayList<String> sequences = new ArrayList<>();

                    for (BioSeq sequence
                            : GenometryModel
                            .getInstance().getSelectedGenomeVersion().getSeqList()) {

                        sequences.add(sequence.getId());
                    }
//                    for (int i = 0; i < GenometryModel.getInstance()
//                            .getSelectedSeqGroup().getSeqCount(); i++) {
//                        sequences.add(GenometryModel.getInstance()
//                                .getSelectedSeqGroup().getSeq(i).getID());
//                    }
                    MIQueryManager.getInstance().setSequences(sequences);
                    break;
                }
            }
        } catch (BridgesRemoteAccessException be) {
            logger.error("Cannot access Uniprot!");
        }
        MIQuery query = MIQueryManager.getInstance().getMIQuery();

        if (query.getSelectedSymmetries().isEmpty() && MIView.getInstance().getMiSearch().isBoxSearchEnabled()) {
            JOptionPane.showMessageDialog(MIView.getInstance().getMiSearch(),
                    "Please add one or more genomic region to the selection box (e.g. a gene, an exon or a variant).", "Select a region", ERROR_MESSAGE);
            return;
        }

        if (false == MIView.getInstance().getMiSearch().isBoxSearchEnabled()) {

            if (selected_syms.isEmpty()) {
                JOptionPane.showMessageDialog(MIView.getInstance().getMiSearch(),
                        "Please select one genomic region (e.g. a gene, an exon or a variant).", "Select a region", ERROR_MESSAGE);
                return;
            }

            query.getSelectedSymmetries().clear();
            query.getSelectedSymmetries().addAll(
            		selected_syms);
        }

        JProgressBar progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setStringPainted(true);
        progressBar.setString(query.getLabel());

        ProgressPanel.getInstance().addBar(progressBar);

        List<MIResult> results = new ArrayList<>();

        MIWorker worker = new MIWorker(results, igbService, query, progressBar);

        worker.execute();

    }

}
