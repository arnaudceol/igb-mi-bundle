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
package it.iit.genomics.cru.igb.bundles.mi.view;

import static javax.swing.JOptionPane.ERROR_MESSAGE;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.apache.commons.lang.StringUtils;
import org.biojava.nbio.structure.Structure;
import org.jmol.api.JmolViewer;
import org.lorainelab.igb.services.IgbService;
import org.lorainelab.igb.services.window.tabs.IgbTabPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.affymetrix.common.CommonUtils;

import it.iit.genomics.cru.bridges.interactome3d.ws.Interactome3DException;
import it.iit.genomics.cru.bridges.interactome3d.ws.Utils;
import it.iit.genomics.cru.igb.bundles.mi.business.MIResult;
import it.iit.genomics.cru.igb.bundles.mi.commons.MIView;
import it.iit.genomics.cru.igb.bundles.mi.view.ColourIterator.ColourScheme;
import it.iit.genomics.cru.structures.model.ChainMapping;
import it.iit.genomics.cru.structures.model.InteractionStructure;
import it.iit.genomics.cru.structures.model.StructureException;

/**
 *
 * @author Arnaud Ceol
 *
 * Displays the list of structures available for an interaction. The list is
 * created when an interaction is submitted with the method
 * setCurrentInteraction()
 *
 */
public class StructuresPanel extends IgbTabPanel {

	private static final Logger logger = LoggerFactory.getLogger(MITable.class);

    private final static String ACTION_LINK = "View structure source";

    private final static String ACTION_JMOL = "View structure in Jmol";

    private final boolean paintIsosurface = false;

    private static final long serialVersionUID = 1L;

    private String currentStructureId = "";

    /**
     * Structures to display: all, only the ones for interactions, or the ones
     * with some residues
     */
    public enum ResiduesType {

        INTERFACE, OTHER, NONE
    }

    public enum StructureDisplay {

        ALL, INTERACTION, INTERACTION_WITH_RESIDUES
    }

    private MIResult currentInteraction;

    private final StructureTable structureList;

    private final HashSet<String> miStructures = new HashSet<>();

    private static final String PDB_COMMAND_CARTOON = "wireframe off; spacefill 0%; CARTOON";
    private static final String PDB_COMMAND_BALL_AND_STICK = "cartoon off; wireframe 0.15; spacefill 23%;";

    private static final String PDB_STRUCTURE_STYLE_DEFAULT = PDB_COMMAND_BALL_AND_STICK; //"cartoon"; // "trace";
    private static final String PDB_INTERFACE_STYLE_DEFAULT = "cartoon";
    private static final String PDB_STRUCTURE_STYLE_RESIDUE = "cpk"; // "cpk";

    private static final String JMOL_DISPLAY_CARTOON = "cartoon";
    private static final String JMOL_DISPLAY_BALL_AND_STICK = "ball & sticks";

    private final JButton jmolButton = new JButton();

    private final JButton linkButton = new JButton();

    /**
     * If unchecked, only structures with mapped residues will be displayed
     */
    JFrame jmolFrame;
    JmolPanel jmolPanel;
    JmolViewer viewer;

    public StructuresPanel(IgbService service, String label) {

        super("MI Structures", "MI Structures", "Display structure",
                true);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Create hidden JmolFrame
        jmolFrame = new JFrame();

        jmolPanel = new JmolPanel();

        jmolPanel.setPreferredSize(new Dimension(500, 500));

        Box jmolBox = new Box(BoxLayout.Y_AXIS);
        Box jmolButtonBox = new Box(BoxLayout.X_AXIS);
        jmolFrame.add(jmolBox);
        jmolBox.add(jmolPanel);
        jmolBox.add(jmolButtonBox);

        jmolButtonBox.add(new JLabel("Display type:"));

        ButtonGroup displayGroup = new ButtonGroup();
        JRadioButton cartoonButton = new JRadioButton(JMOL_DISPLAY_CARTOON);
        JRadioButton ballAndSticksButton = new JRadioButton(JMOL_DISPLAY_BALL_AND_STICK);

        JmolDisplayListener listener = new JmolDisplayListener();
        cartoonButton.addActionListener(listener);
        ballAndSticksButton.addActionListener(listener);

        displayGroup.add(cartoonButton);
        displayGroup.add(ballAndSticksButton);

        jmolButtonBox.add(cartoonButton);
        jmolButtonBox.add(ballAndSticksButton);

        ballAndSticksButton.setSelected(true);

        jmolFrame.pack();
        jmolFrame.setVisible(false);

        jmolButton.addActionListener(new JmolActionListener());
        jmolButton.setIcon(new ImageIcon(getClass().getResource("/jmol.jpg")));

        linkButton.addActionListener(new ExternalLinkActionListener());
        linkButton.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/searchweb.png"));

        StructureTableModel model = new StructureTableModel(
                new ArrayList<StructureItem>(0));

        structureList = new StructureTable(model, service);
        structureList.setTableHeader(null);

        structureList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane structureListPane = new JScrollPane(structureList);

        add(structureListPane);
    }

//    @Override
//    public TabState getDefaultState() {
//        return TabState.COMPONENT_STATE_RIGHT_TAB;
//    }

    @Override
    public boolean isEmbedded() {
        return true;
    }

    public void clean() {
        structureList.removeAll();
    }

    public void setCurrentInteraction(MIResult currentInteraction) {
        this.currentInteraction = currentInteraction;
        miStructures.clear();

        if (currentInteraction == null) {
            int numRows = structureList.getModel().getRowCount();

            if (numRows > 0) {
                ((StructureTableModel) structureList.getModel()).clear();
            }
            return;
        }

        ArrayList<StructureItem> pdbVector = new ArrayList<>();

        for (InteractionStructure is : currentInteraction
                .getInteractionStructures()) {
            miStructures.add(is.getStructureID());

            ResiduesType type;

            if (currentInteraction.getStructuresWithQueryResiduesAtInterface()
                    .contains(is.getStructureID())) {
                type = ResiduesType.INTERFACE;
            } else if (currentInteraction.getStructuresWithQueryResidues()
                    .contains(is.getStructureID())) {
                type = ResiduesType.OTHER;
            } else {
                type = ResiduesType.NONE;
            }

            pdbVector.add(new StructureItem(is.getStructureID(), is
                    .getSourceType(), type));
        }

        Collections.sort(pdbVector);

        int numRows = structureList.getModel().getRowCount();

        if (numRows > 0) {
            ((StructureTableModel) structureList.getModel()).clear();
        }

        ((StructureTableModel) structureList.getModel()).setColumnCount(1);

        for (StructureItem item : pdbVector) {
            ((StructureTableModel) structureList.getModel()).addRow(item);
        }

        numRows = structureList.getModel().getRowCount();

        if (numRows > 0) {
            ((StructureTableModel) structureList.getModel())
                    .fireTableRowsInserted(0, numRows - 1);
            if (structureList.getRowCount() > 0) {
                structureList.setRowSelectionInterval(0, 0);
            }
        }

    }

    public class JmolActionListener implements ActionListener {

        JmolActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            try {

                if (structureList.getSelectedRowCount() == 0) {
                    JOptionPane.showMessageDialog(MIView.getInstance().getResultsTabbedPan(),
                            "Please select a structure.", "Jmol", ERROR_MESSAGE);
                    return;
                }

                int modelRow = structureList
                        .convertRowIndexToModel(structureList
                                .getSelectedRow());

                StructureItem structureItem = ((StructureTableModel) structureList
                        .getModel()).getResult(modelRow);

                String structureID = structureItem.getName();

                currentStructureId = structureID;

                Structure struc = currentInteraction.getStructureSource()
                        .getStructure(structureID);

                if (struc == null) {
                    logger.error(
                            "No PDB file available for " + structureID
                            + ", skip");
                    return;
                }

                jmolFrame.setName(structureID);

                jmolPanel.setStructure(struc);

                executeJmolCommand("background white");
                executeJmolCommand("select all; "
                        + PDB_STRUCTURE_STYLE_DEFAULT
                        + "; backbone off; colour grey");

                Collection<String> interfaceResidues
                        = currentInteraction.getStructuresResiduesAtInterfaces(structureID);
                Collection<String> otherResidues = currentInteraction.getStructuresResidues(structureID);

                ArrayList<ColourIterator> colourIterators = new ArrayList<>();

                if (false == currentInteraction.isHomodimer() && false == currentInteraction.getInteractor2().isLigand()) {
                    colourIterators.add(new ColourIterator(
                            ColourScheme.BLUE));
                }

                colourIterators.add(new ColourIterator(ColourScheme.GREEN));

                Iterator<ColourIterator> colourIteratorsIterator = colourIterators
                        .iterator();

                int chainsLength = 0;

                int i = 0;
                if (false == currentInteraction.isHomodimer() && false == currentInteraction
                        .getInteractor2().isLigand()) {

                    ColourIterator chainColourIterator = colourIteratorsIterator
                            .next();

                    for (ChainMapping chain : currentInteraction
                            .getInteractor2().getChains(structureID)) {

                        chainsLength += chain.getSequence() != null ? chain.getSequence().length() : 0;

                        String colour = chainColourIterator.next();
                        executeJmolCommand("select :" + chain.getChain()
                                + "; colour \"" + colour + "\"");

                        i++;
                    }
                }

                ColourIterator chainColourIterator = colourIteratorsIterator
                        .next();

                for (ChainMapping chain : currentInteraction
                        .getInteractor1().getChains(structureID)) {

                    chainsLength += chain.getSequence() != null ? chain.getSequence().length() : 0;

                    String colour = chainColourIterator.next();

                    executeJmolCommand("select :" + chain.getChain()
                            + "; colour  \"" + colour + "\"");

                    i++;

                }

                if (false == otherResidues.isEmpty()) {
                    // Only colour orange if it is not covering the full length

                    executeJmolCommand("select "
                            + StringUtils.join(otherResidues, ", ")
                            + "; color \""
                            + "orange" + "\"");

                }

                if (false == interfaceResidues.isEmpty()) {
                    executeJmolCommand("select "
                            + StringUtils.join(interfaceResidues, ", ")
                            + ";wireframe 0.15;  color \""
                            + "red" + "\"");

                }

                if (currentInteraction.getInteractor2().isLigand()) {

                    String command
                            = "select * AND HETERO AND [" + currentInteraction.getInteractor2().getGeneName() + "];  wireframe 0.30; cartoon off; color \""
                            + "gold" + "\"";
                    if (currentInteraction.getInteractor2().getUniprotAc().endsWith(" ION")) {
                        command += "; CPK";
                    }
                    executeJmolCommand(command);
                }

                jmolFrame.setVisible(true);

            } catch (HeadlessException | StructureException ex) {
                logger.error(null, ex);
            }
        }

    }

    private void executeJmolCommand(String command) {
        jmolPanel.executeCmd(command);
    }

    public class ExternalLinkActionListener implements ActionListener {

        ExternalLinkActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            try {

                if (structureList.getSelectedRowCount() == 0) {
                    JOptionPane.showMessageDialog(MIView.getInstance().getResultsTabbedPan(),
                            "Please select a structure.", "Jmol", ERROR_MESSAGE);
                    return;
                }

                int modelRow = structureList
                        .convertRowIndexToModel(structureList
                                .getSelectedRow());

                StructureItem structureItem = ((StructureTableModel) structureList
                        .getModel()).getResult(modelRow);

                String structureID = structureItem.getName();

                String url;

                if (structureID.length() == 4) {
                    // PDB
                    url = "http://www.pdb.org/pdb/explore/explore.do?structureId="
                            + structureID;
                } else {
                    // interactome3d
                    String[] fields = structureID.split("-");

                    String dataset = Utils.getDataset(currentInteraction
                            .getQueryTaxid());

                    if (miStructures.contains(structureID)) {
                        // interaction
                        String ac1 = fields[0];
                        String ac2 = fields[1];
                        url = "http://interactome3d.irbbarcelona.org/interaction.php?ids="
                                + ac1 + ";" + ac2 + "&dataset=" + dataset;
                    } else {
                        // protein
                        String ac = fields[0];
                        url = "http://interactome3d.irbbarcelona.org/protein.php?ids="
                                + ac + "&dataset=" + dataset;
                    }

                }

                Desktop desktop = Desktop.isDesktopSupported() ? Desktop
                        .getDesktop() : null;
                if (desktop != null
                        && desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                }

            } catch (HeadlessException | Interactome3DException | URISyntaxException | IOException ex) {
                logger.error(null, ex);
            }
        }

    }

    public JButton getJmolButton() {
        return jmolButton;
    }

    public JButton getLinkButton() {
        return linkButton;
    }

    class JmolDisplayListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            switch (e.getActionCommand()) {
                case JMOL_DISPLAY_BALL_AND_STICK:
                    executeJmolCommand("select ALL; " + PDB_COMMAND_BALL_AND_STICK);
                    break;
                case JMOL_DISPLAY_CARTOON:
                    executeJmolCommand("select ALL; " + PDB_COMMAND_CARTOON);
                    Collection<String> interfaceResidues
                            = currentInteraction.getStructuresResiduesAtInterfaces(currentStructureId);

                    if (false == interfaceResidues.isEmpty()) {
                        executeJmolCommand("select "
                                + StringUtils.join(interfaceResidues, ", ")
                                + ";wireframe 0.15;");
                    }
                    if (currentInteraction.getInteractor2().isLigand()) {
                        String command
                                = "select * AND HETERO AND [" + currentInteraction.getInteractor2().getGeneName() + "];  wireframe 0.30; cartoon off; color \""
                                + "gold" + "\"";
                        if (currentInteraction.getInteractor2().getUniprotAc().endsWith(" ION")) {
                            command += "; CPK";
                        }
                        executeJmolCommand(command);
                    }
                    break;
            }
        }
    }
}
