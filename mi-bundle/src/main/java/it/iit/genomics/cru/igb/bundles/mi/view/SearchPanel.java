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

import com.affymetrix.common.CommonUtils;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;	
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.event.GenomeVersionSelectionEvent;
import com.affymetrix.genometry.event.GroupSelectionListener;
import com.affymetrix.genometry.search.SearchUtils;
import com.affymetrix.genometry.symmetry.RootSeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.util.SeqUtils;
import com.affymetrix.igb.shared.TrackUtils;
import com.lorainelab.igb.genoviz.extensions.glyph.StyledGlyph;
import com.lorainelab.igb.services.IgbService;

import it.iit.genomics.cru.igb.bundles.commons.business.IGBLogger;
import it.iit.genomics.cru.igb.bundles.commons.view.InfoPanel;
import it.iit.genomics.cru.igb.bundles.commons.view.LogPanel;
import it.iit.genomics.cru.igb.bundles.mi.business.MIAction;
import it.iit.genomics.cru.igb.bundles.mi.commons.MIBundleConfiguration;
import it.iit.genomics.cru.igb.bundles.mi.commons.MIView;
import it.iit.genomics.cru.igb.bundles.mi.query.AbstractMIQuery.QueryType;
import it.iit.genomics.cru.igb.bundles.mi.query.MIQueryManager;
import it.iit.genomics.cru.structures.bridges.commons.BridgesRemoteAccessException;
import it.iit.genomics.cru.structures.bridges.pdb.PDBUtils;
import it.iit.genomics.cru.structures.bridges.uniprot.UniprotkbUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Arnaud Ceol
 *
 * Form for the submission of a query.
 *
 */
public class SearchPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final IGBLogger igbLogger;

    // Search type
    private ButtonGroup searchTypeGroup;

    private JRadioButton buttonExtra;
    private JRadioButton buttonInter;

    // Psicquic provider
    private JComboBox<String> psicquicChoice;

    // PDB providers
    private JComboBox<String> structureFromChoice;

    private JCheckBox searchNucleicAcid;

    private JCheckBox searchLigands;

    // Modified residues providers
    private final JComboBox<String> modChoice;

    // species
    private JComboBox<String> pdbProviderBox;

    // Advanced search: selection box
    Box selectionBox;

    // Selection
    private final JTextField searchAndAddArea;
    private final SelectionListModel selectionModel;
    private final JList<SeqSymmetry> selectList;

    public static final String choiceNONE = "== none ==";
    public static final String choicePDB = "PDB";
    public static final String choicePDBLocal = "PDB local";
    public static final String choiceI3D = "Interactome3D";
    public static final String choiceDSysMap = "DSysMap";
    public static final String choiceEPPIC = "EPPIC";
    public static final String choiceUSER = "I3D user data";
    public static final String choiceUniprot = "Uniprotkb";

    private IgbService igbService;

    public void setPsicquicProviders(String[] providers) {
        psicquicChoice.removeAllItems();

        for (String provider : providers) {
            psicquicChoice.addItem(provider);
            if (provider.equals("IntAct")) {
                psicquicChoice.setSelectedItem(provider);
            }
        }
    }

    public SearchPanel(final IgbService igbService) {

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.igbService = igbService;

        igbLogger = IGBLogger.getMainInstance();

        this.initListeners();

        MIView.getInstance().setMiSearch(this);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));

        buttonExtra = new JRadioButton("extend");
        buttonInter = new JRadioButton("connect");

        searchTypeGroup = new ButtonGroup();
        searchTypeGroup.add(buttonExtra);
        searchTypeGroup.add(buttonInter);

        buttonExtra.setSelected(QueryType.EXTRA.equals(MIQueryManager
                .getInstance().getQueryType()));
        buttonInter.setSelected(QueryType.INTRA.equals(MIQueryManager
                .getInstance().getQueryType()));

        this.buttonExtra.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                MIQueryManager.getInstance().setQueryType(QueryType.EXTRA);
            }
        });

        this.buttonInter.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                MIQueryManager.getInstance().setQueryType(QueryType.INTRA);
            }
        });

        JButton searchButton = new JButton("search");

        MIAction action = new MIAction(igbService);
        searchButton.setAction(action);
        searchButton.setText("RUN");

        Box searchTypeBox = new Box(BoxLayout.X_AXIS);
        searchTypeBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchTypeBox.setBorder(new TitledBorder("Search"));

        searchTypeBox.add(buttonExtra);
        searchTypeBox.add(buttonInter);
        searchTypeBox.add(searchButton);

        Box selectionBoxBox = new Box(BoxLayout.X_AXIS);
        selectionBoxBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton showBoxButton = new JButton(new ShowHideBoxAction());
        selectionBoxBox.add(searchTypeBox);
        selectionBoxBox.add(showBoxButton);

        // Psicquic
        String[] providers = {choiceNONE, "...loading..."};
        psicquicChoice = new JComboBox<String>(providers) {

            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                return max;
            }

        };

        this.psicquicChoice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MIQueryManager.getInstance().setPsiquicServer(
                        (String) psicquicChoice.getSelectedItem());
            }
        });

        Box dataSourceBox = new Box(BoxLayout.Y_AXIS);
        dataSourceBox.setBorder(new TitledBorder("Data sources"));

        Box psicquicBox = new Box(BoxLayout.X_AXIS);
        psicquicBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel psicquicLabel = new JLabel("Interactions");
        psicquicBox.add(psicquicLabel);
        psicquicBox.add(psicquicChoice);

        dataSourceBox.add(psicquicBox);

        // Structure source
        String[] structureProviders = {choicePDB, choiceI3D, choiceDSysMap, choiceNONE}; //,, choiceEPPIC choiceUSER 

        structureFromChoice = new JComboBox<String>(structureProviders) {

            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                return max;
            }

        };

        // Load it
        structureFromChoice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseStructureFrom();
            }
        });
        chooseStructureFrom();

        // PDB
        pdbProviderBox = new JComboBox<String>(PDBUtils
                .getAvailablePDBProviders().toArray(
                        new String[PDBUtils.getAvailablePDBProviders().size()])) {

                            private static final long serialVersionUID = 1L;

                            @Override
                            public Dimension getMaximumSize() {
                                Dimension max = super.getMaximumSize();
                                max.height = getPreferredSize().height;
                                return max;
                            }

                        };

                pdbProviderBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        MIBundleConfiguration.getInstance().setPdbUrl(
                                PDBUtils.getUrl((String) pdbProviderBox
                                        .getSelectedItem()));
                    }
                });

                Box structureBox1 = new Box(BoxLayout.X_AXIS);
                structureBox1.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel structureLabel1 = new JLabel("Structures: ");
                structureBox1.add(structureLabel1);
                structureBox1.add(structureFromChoice);

                dataSourceBox.add(structureBox1);

                Box structureBox2 = new Box(BoxLayout.X_AXIS);
                structureBox2.setAlignmentX(Component.LEFT_ALIGNMENT);

                searchNucleicAcid = new JCheckBox("RNA/DNA",
                        MIQueryManager.getInstance().searchNucleicAcid());

                searchNucleicAcid.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        MIQueryManager.getInstance().setSearchNucleicAcid(
                                searchNucleicAcid.isSelected());
                    }
                });

                structureBox2.add(searchNucleicAcid);

                searchLigands = new JCheckBox("Small molecules",
                        MIQueryManager.getInstance().searchLigands());

                searchLigands.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        MIQueryManager.getInstance().setSearchLigands(
                                searchLigands.isSelected());
                    }
                });

                structureBox2.add(searchLigands);

                dataSourceBox.add(structureBox2);

                // modified residues
                String[] modProviders = {choiceNONE, choiceUniprot}; //, choiceUSER 

                modChoice = new JComboBox<String>(modProviders) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Dimension getMaximumSize() {
                        Dimension max = super.getMaximumSize();
                        max.height = getPreferredSize().height;
                        return max;
                    }

                };
                modChoice.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        chooseModFrom();
                    }
                });

                Box modifiedResidues = new Box(BoxLayout.X_AXIS);
                modifiedResidues.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel modLabel1 = new JLabel("Modified residues: ");
                modifiedResidues.add(modLabel1);
                modifiedResidues.add(modChoice);

                dataSourceBox.add(modifiedResidues);

                buttonPane.add(selectionBoxBox);

                buttonPane.add(dataSourceBox);

                JButton testButton = new JButton(new TestAction(igbService));

//                buttonPane.add(testButton);
                buttonPane.setMaximumSize(new Dimension(200, 500));

                this.add(buttonPane);

                // Select box
                selectionModel = new SelectionListModel();
                selectList = new JList<>(selectionModel.getListModel());
                selectList.setCellRenderer(new SelectionListRenderer());
                selectList
                        .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

                selectList.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent evt) {

                        JList list = (JList) evt.getSource();
                        if (evt.getClickCount() == 2) {
                            int index = list.locationToIndex(evt.getPoint());
                            SeqSymmetry sym = selectionModel.getListModel().elementAt(index);
                            SeqSpan span = sym.getSpan(0);
                            igbService.zoomToCoord(span.getBioSeq().getId(), span.getStart(), span.getEnd());
                        }
                    }
                });

                JScrollPane selectionistPane = new JScrollPane(selectList);
                this.add(BorderLayout.CENTER, selectionistPane);

                selectionBox = new Box(BoxLayout.Y_AXIS);
                selectionBox.setVisible(false);

                Box selectionButtonBox = new Box(BoxLayout.X_AXIS);

                JButton addButton = new JButton(new AddSelectionAction(igbService));

                JButton removeButton = new JButton(new RemoveSelectionAction());

                JButton clearButton = new JButton(new ClearSelectionAction());

                JButton selectTrackButton = new JButton(new AddTrackAction(igbService));

                Dimension buttonDimension = new Dimension(80, 20);
                addButton.setPreferredSize(buttonDimension);
                removeButton.setPreferredSize(buttonDimension);
                clearButton.setPreferredSize(buttonDimension);

                selectionButtonBox.add(new JLabel("Selection: "));
                selectionButtonBox.add(addButton);
                selectionButtonBox.add(removeButton);
                selectionButtonBox.add(clearButton);
                selectionButtonBox.add(selectTrackButton);

                // Search and add
                Box searchAndAddBox = new Box(BoxLayout.X_AXIS);
                searchAndAddBox.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
                searchAndAddArea = new JTextField(10);
                JButton searchAndAddButton = new JButton(new SearchAndAddAction());
                searchAndAddBox.add(searchAndAddArea);
                searchAndAddBox.add(searchAndAddButton);
                selectionBox.add(selectionButtonBox);
                selectionBox.add(selectionistPane);
                selectionBox.add(searchAndAddBox);

                this.add(selectionBox);

                // Progress bar
                this.add(ProgressPanel.getInstance());

                Box buttons = new Box(BoxLayout.Y_AXIS);

                final JFrame helpFrame;
                final JFrame setupFrame;
                final JFrame logFrame;

                helpFrame = new JFrame("MI Bundle Help");
                logFrame = new JFrame("MI Bundle Log");
                setupFrame = new JFrame("MI Bundle Setup");

                Dimension preferredSize = new Dimension(900, 500);

                helpFrame.setPreferredSize(preferredSize);
                helpFrame.setMinimumSize(preferredSize);

                logFrame.setPreferredSize(preferredSize);
                logFrame.setMinimumSize(preferredSize);

                setupFrame.setPreferredSize(preferredSize);
                setupFrame.setMinimumSize(preferredSize);

                InfoPanel.getInstance().setPreferredSize(preferredSize);
                InfoPanel.getInstance().setMinimumSize(preferredSize);

                helpFrame.add(InfoPanel.getInstance());
                logFrame.add(new JScrollPane(new LogPanel(IGBLogger.getMainInstance())));
                setupFrame.add(new ConfigurationPanel());

                helpFrame.setVisible(false);
                logFrame.setVisible(false);
                setupFrame.setVisible(false);

                JButton help = new JButton();
                help.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/info.png"));
                help.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        helpFrame.setVisible(true);
                    }

                });

                JButton setup = new JButton();
                setup.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/preferences_updated.png"));
                setup.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setupFrame.setVisible(true);
                    }

                });
                JButton log = new JButton();
                log.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/console.png"));
                log.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        logFrame.setVisible(true);
                    }

                });

                Dimension buttonSize = new Dimension(90, 30);
                help.setMaximumSize(buttonSize);
                help.setPreferredSize(buttonSize);
                help.setMinimumSize(buttonSize);

                setup.setMaximumSize(buttonSize);
                setup.setPreferredSize(buttonSize);
                setup.setMinimumSize(buttonSize);

                log.setMaximumSize(buttonSize);
                log.setPreferredSize(buttonSize);
                log.setMinimumSize(buttonSize);

                buttons.add(help);
                buttons.add(setup);
                buttons.add(log);

                add(buttons);

    }

    private void chooseStructureFrom() {
        if (null != (String) structureFromChoice
                .getSelectedItem()) {
            switch ((String) structureFromChoice
                    .getSelectedItem()) {
                case choiceNONE:
                    MIQueryManager.getInstance().setSearchPDB(false);
                    MIQueryManager.getInstance().setSearchPDBLocal(false);
                    MIQueryManager.getInstance().setSearchInteractome3D(false);
                    MIQueryManager.getInstance().setSearchDSysMap(false);
                    MIQueryManager.getInstance().setSearchUserStructures(false);
                    MIQueryManager.getInstance().setSearchEPPIC(false);
                    break;
                case choicePDB:
                    MIQueryManager.getInstance().setSearchPDB(true);
                    MIQueryManager.getInstance().setSearchPDBLocal(false);
                    MIQueryManager.getInstance().setSearchInteractome3D(false);
                    MIQueryManager.getInstance().setSearchDSysMap(false);
                    MIQueryManager.getInstance().setSearchUserStructures(false);
                    MIQueryManager.getInstance().setSearchEPPIC(false);
                    break;
                case choiceI3D:
                    MIQueryManager.getInstance().setSearchPDB(false);
                    MIQueryManager.getInstance().setSearchPDBLocal(false);
                    MIQueryManager.getInstance().setSearchInteractome3D(true);
                    MIQueryManager.getInstance().setSearchDSysMap(false);
                    MIQueryManager.getInstance().setSearchUserStructures(false);
                    MIQueryManager.getInstance().setSearchEPPIC(false);
                    break;
                case choiceDSysMap:
                    MIQueryManager.getInstance().setSearchPDB(false);
                    MIQueryManager.getInstance().setSearchPDBLocal(false);
                    MIQueryManager.getInstance().setSearchInteractome3D(false);
                    MIQueryManager.getInstance().setSearchDSysMap(true);
                    MIQueryManager.getInstance().setSearchUserStructures(false);
                    MIQueryManager.getInstance().setSearchEPPIC(false);
                    MIQueryManager.getInstance().setSearchEPPIC(false);
                    break;
                case choiceEPPIC:
                    MIQueryManager.getInstance().setSearchPDB(false);
                    MIQueryManager.getInstance().setSearchPDBLocal(false);
                    MIQueryManager.getInstance().setSearchInteractome3D(false);
                    MIQueryManager.getInstance().setSearchDSysMap(false);
                    MIQueryManager.getInstance().setSearchUserStructures(false);
                    MIQueryManager.getInstance().setSearchEPPIC(true);
                    break;
                case choiceUSER:
                    MIQueryManager.getInstance().setSearchPDB(false);
                    MIQueryManager.getInstance().setSearchPDBLocal(false);
                    MIQueryManager.getInstance().setSearchInteractome3D(false);
                    MIQueryManager.getInstance().setSearchDSysMap(false);
                    MIQueryManager.getInstance().setSearchUserStructures(true);
                    MIQueryManager.getInstance().setSearchEPPIC(false);
                    break;
                case choicePDBLocal:
                    MIQueryManager.getInstance().setSearchPDB(false);
                    MIQueryManager.getInstance().setSearchPDBLocal(true);
                    MIQueryManager.getInstance().setSearchInteractome3D(false);
                    MIQueryManager.getInstance().setSearchDSysMap(false);
                    MIQueryManager.getInstance().setSearchUserStructures(false);
                    MIQueryManager.getInstance().setSearchEPPIC(false);
                    break;
            }
        }
    }

    private void chooseModFrom() {
        if (choiceNONE.equals((String) modChoice
                .getSelectedItem())) {
            MIQueryManager.getInstance().setSearchModifications(false);
        } else {
            MIQueryManager.getInstance().setSearchModifications(true);
        }
    }

    public void removeStructureSource(String source) {

        int item = -1;

        for (int i = 0; i < structureFromChoice.getItemCount(); i++) {
            if (source.equals((String) structureFromChoice.getItemAt(i))) {
                item = i;
                break;
            }
        }

        if (item >= 0) {
            structureFromChoice.removeItemAt(item);
        }

        item = 0;
        for (int i = 0; i < structureFromChoice.getItemCount(); i++) {
            if (choicePDB.equals((String) structureFromChoice.getItemAt(i))) {
                item = i;
            }
        }

        structureFromChoice.setSelectedIndex(item);
        if (choicePDB.equals((String) structureFromChoice.getSelectedItem())) {
            MIQueryManager.getInstance().setSearchPDB(true);
            MIQueryManager.getInstance().setSearchPDBLocal(false);
            MIQueryManager.getInstance().setSearchInteractome3D(false);
            MIQueryManager.getInstance().setSearchUserStructures(false);
        }
    }

    public void addStructureSource(String source) {

        int item = -1;

        for (int i = 0; i < structureFromChoice.getItemCount(); i++) {
            if (source.equals((String) structureFromChoice.getItemAt(i))) {
                item = i;
                break;
            }
        }

        if (item < 0) {
            structureFromChoice.addItem(source);
        }

    }

    private void initListeners() {
        GenometryModel.getInstance().addGroupSelectionListener(
                new GroupSelectionListener() {

                    @Override
                    public void groupSelectionChanged(GenomeVersionSelectionEvent evt) {
                        try {

                            for (String[] species : UniprotkbUtils
                            .getSpeciesFromName(igbService
                                    .getSelectedSpecies())) {

                                igbLogger.info(
                                        "Species: " + species[0] + " / "
                                        + species[1]);
                                MIQueryManager.getInstance().setSpecies(
                                        igbService.getSelectedSpecies());
                                MIQueryManager.getInstance().setTaxid(species[1]);

                                ArrayList<String> sequences = new ArrayList<>();

                                for (BioSeq sequence
                                : GenometryModel
                                .getInstance().getSelectedGenomeVersion().getSeqList()) {

                                    sequences.add(sequence.getId());
                                }
                                MIQueryManager.getInstance()
                                .setSequences(sequences);

                                break;
                            }
                        } catch (BridgesRemoteAccessException be) {
                            igbLogger.severe("Cannot access Uniprot!");
                        }
                    }
                });
    }

    public class SelectionListRenderer extends JLabel implements
            ListCellRenderer<SeqSymmetry> {

        private static final long serialVersionUID = 1L;

        public SelectionListRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends SeqSymmetry> list, SeqSymmetry value,
                int index, boolean isSelected, boolean cellHasFocus) {
            // Assumes the stuff in the list has a pretty toString
            setText(value.getID() + ", " + SeqUtils.spanToString(value.getSpan(0)));

            if (isSelected) {
                setBackground(list.getSelectionBackground());
            } else {
                setBackground(list.getBackground());
            }

            return this;
        }

    }

    private class SearchAndAddAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public SearchAndAddAction() {
            super("Search and Add");
        }

        private Pattern getRegex(String search_text) throws Exception {
            Pattern regex;
            String regexText = search_text;
            // Make sure this search is reasonable to do on a remote server.
            if (!(regexText.contains("*") || regexText.contains("^") || regexText
                    .contains("$"))) {
                // Not much of a regular expression. Assume the user wants to
                // match at the start and end
                regexText = ".*" + regexText + ".*";
            }
            regex = Pattern.compile(regexText, Pattern.CASE_INSENSITIVE);
            return regex;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            igbLogger.info("Set selection by search: " + searchAndAddArea.getText());

            if (searchAndAddArea.getText() == null || "".equals(searchAndAddArea.getText().trim())) {
                return;
            }

            GenomeVersion group = GenometryModel.getInstance().getSelectedGenomeVersion();
//                    .getSelectedSeqGroup();

            Pattern regex = null;
            try {
                regex = getRegex(searchAndAddArea.getText().trim());
            } catch (Exception e) {
            } // should not happen, already checked above

            List<SeqSymmetry> symmetries = SearchUtils.findLocalSyms(group,
                    null, regex, true);

            igbLogger.info(
                    "Number of terms found: " + symmetries.size());

            for (SeqSymmetry selected : symmetries) {
                selectionModel.getListModel().addElement(selected);
            }

            MIQueryManager.getInstance().setSelectedSymmetries(
                    Collections.list(selectionModel.getListModel().elements()));
            selectList.ensureIndexIsVisible(selectList.getComponentCount());

        }
    }

    private class AddSelectionAction extends GenericAction {

        private static final long serialVersionUID = 1L;

        IgbService service;

        public AddSelectionAction(IgbService service) {
            super("Add", KeyEvent.VK_Z);
            this.service = service;
        }

        @Override
        public void actionPerformed(ActionEvent event) {

            for (SeqSymmetry selected : GenometryModel.getInstance().getSelectedSymmetries(null)) {
                selectionModel.getListModel().addElement(selected);

            }

            MIQueryManager.getInstance().setSelectedSymmetries(
                    Collections.list(selectionModel.getListModel().elements()));
            selectList.ensureIndexIsVisible(selectList.getComponentCount());

        }
    }

    private class AddTrackAction extends GenericAction {

        private static final long serialVersionUID = 1L;

        IgbService service;

        public AddTrackAction(IgbService service) {
            super("Add selected track", KeyEvent.VK_Z);
            this.service = service;
        }

        @Override
        public void actionPerformed(ActionEvent event) {

            ArrayList<StyledGlyph> tracks = new ArrayList<>();

            for (Object track : service.getSelectedTierGlyphs()) {
                if (StyledGlyph.class.isInstance(track)) {
                    tracks.add((StyledGlyph) track);
                }
            }

            Collection<RootSeqSymmetry> syms = TrackUtils.getInstance().getSymsTierGlyphs(tracks);

            for (SeqSymmetry rsym : syms) {
                for (int i = 0; i < rsym.getChildCount(); i++) {
                    //selectionModel.getListModel().addElement(rsym.getChild(i));
                    for (int j = 0; j < rsym.getChild(i).getChildCount(); j++) {
                        selectionModel.getListModel().addElement(rsym.getChild(i).getChild(j));
                    }
                }
            }

            MIQueryManager.getInstance().setSelectedSymmetries(
                    Collections.list(selectionModel.getListModel().elements()));
            selectList.ensureIndexIsVisible(selectList.getComponentCount());
        }
    }

    private class ClearSelectionAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public ClearSelectionAction() {
            super("Clear");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            selectionModel.getListModel().removeAllElements();
            MIQueryManager.getInstance().setSelectedSymmetries(
                    Collections.list(selectionModel.getListModel().elements()));
        }
    }

    private class RemoveSelectionAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public RemoveSelectionAction() {
            super("Remove");
        }

        @Override
        public void actionPerformed(ActionEvent event) {

            for (SeqSymmetry sym : selectList.getSelectedValuesList()) {
                selectionModel.getListModel().removeElement(sym);
            }

            MIQueryManager.getInstance().setSelectedSymmetries(
                    Collections.list(selectionModel.getListModel().elements()));
        }
    }

    class SelectionListModel {

        private final DefaultListModel<SeqSymmetry> lm_;

        public SelectionListModel() {
            lm_ = new DefaultListModel<>();
        }

        public DefaultListModel<SeqSymmetry> getListModel() {
            return lm_;
        }
    }

    private class ShowHideBoxAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        private static final String LABEL_SHOW = ">>";
        private static final String LABEL_HIDE = "<<";

        public ShowHideBoxAction() {
            super(LABEL_SHOW);
        }

        @Override
        public void actionPerformed(ActionEvent event) {

            if (selectionBox.isVisible()) {
                MIQueryManager.getInstance().setSelectedSymmetries(Collections.EMPTY_LIST);
                selectionBox.setVisible(false);
                putValue(Action.NAME, LABEL_SHOW);
            } else {
                MIQueryManager.getInstance().setSelectedSymmetries(
                        Collections.list(selectionModel.getListModel().elements()));
                selectionBox.setVisible(true);
                putValue(Action.NAME, LABEL_HIDE);
            }

        }
    }

    private class TestAction extends GenericAction {

        private static final long serialVersionUID = 1L;

        IgbService service;

        public TestAction(IgbService service) {
            super("Test", KeyEvent.VK_Z);
            this.service = service;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            SeqSymmetry symFound = getSelectedSymmetries().get(0);
            service.getSeqMapView();
        }
    }

    public boolean isBoxSearchEnabled() {
        return selectionBox.isVisible();
    }

    private List<SeqSymmetry> getSelectedSymmetries() {
       return GenometryModel.getInstance().getSelectedSymmetries(GenometryModel.getInstance().getSelectedSeq().get());
    }

}
