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

import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.color.RGB;
import com.affymetrix.genometry.parsers.TrackLineParser;
import com.affymetrix.genometry.style.SimpleTrackStyle;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;

import com.affymetrix.genometry.symmetry.impl.TypeContainerAnnot;
import it.iit.genomics.cru.igb.bundles.commons.business.IGBLogger;
import it.iit.genomics.cru.igb.bundles.mi.business.MIResult;
import it.iit.genomics.cru.igb.bundles.mi.business.MIResult.StructureSummary;
import it.iit.genomics.cru.igb.bundles.mi.commons.Utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringUtils;

import com.affymetrix.igb.service.api.IGBService;
import com.lorainelab.igb.genoviz.extensions.api.TierGlyph;
import it.iit.genomics.cru.bridges.interactome3d.ws.Interactome3DException;
import static it.iit.genomics.cru.igb.bundles.mi.business.MIResult.HTML_SCORE_0;
import static it.iit.genomics.cru.igb.bundles.mi.business.MIResult.HTML_SCORE_1;
import static it.iit.genomics.cru.igb.bundles.mi.business.MIResult.HTML_SCORE_2;
import static it.iit.genomics.cru.igb.bundles.mi.business.MIResult.HTML_SCORE_3;
import it.iit.genomics.cru.igb.bundles.mi.model.TaxonColorer;
import it.iit.genomics.cru.igb.bundles.mi.query.MIQuery;
import it.iit.genomics.cru.structures.bridges.psicquic.Interaction;
import static it.iit.genomics.cru.structures.bridges.psicquic.Interaction.INTERACTION_TYPE_I3D;
import static it.iit.genomics.cru.structures.bridges.psicquic.Interaction.INTERACTION_TYPE_PDB;
import it.iit.genomics.cru.structures.model.MoleculeEntry;
import java.util.Comparator;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.ToolTipManager;

/**
 *
 * @author Arnaud Ceol
 *
 * Table that contains the description of all interactions found.
 *
 */
public class MITable extends JTable {

    private static final long serialVersionUID = 1L;

    private final IGBLogger igbLogger;

    private final IGBService igbService;

    private boolean showInteractionsWoStructure = true;

    private StructuresPanel structuresPanel;

    private boolean showPhysical = true;
    private boolean showAssociations = true;
    private boolean showEnzymatic = true;
    private boolean showUnspecified = true;
    private boolean showOther = true;
    private boolean showStructures = true;

    private int scoreLimit = 0;

    public void setShowPhysical(boolean showPhysical) {
        this.showPhysical = showPhysical;
        ((MITableModel) this.getModel()).fireTableDataChanged();
    }

    public void setShowAssociations(boolean showAssociations) {
        this.showAssociations = showAssociations;
        ((MITableModel) this.getModel()).fireTableDataChanged();
    }

    public void setShowEnzymatic(boolean showEnzymatic) {
        this.showEnzymatic = showEnzymatic;
        ((MITableModel) this.getModel()).fireTableDataChanged();
    }

    public void setShowUnspecified(boolean showUnspecified) {
        this.showUnspecified = showUnspecified;
        ((MITableModel) this.getModel()).fireTableDataChanged();
    }

    public void setShowOther(boolean showOther) {
        this.showOther = showOther;
        ((MITableModel) this.getModel()).fireTableDataChanged();
    }

    public void setShowStructure(boolean showStructures) {
        this.showStructures = showStructures;
        ((MITableModel) this.getModel()).fireTableDataChanged();
    }

    public void setScoreLimit(int scoreLimit) {
        this.scoreLimit = scoreLimit;
        ((MITableModel) this.getModel()).fireTableDataChanged();
    }

    public void setStructuresPanel(StructuresPanel structuresPanel) {
        this.structuresPanel = structuresPanel;
    }

    public boolean isShowInteractionsWoStructure() {
        return showInteractionsWoStructure;
    }

    public void setShowInteractionsWoStructure(
            boolean showInteractionsWoStructure) {
        igbLogger.info(
                "Set show wo structs.: " + showInteractionsWoStructure);
        this.showInteractionsWoStructure = showInteractionsWoStructure;
        ((MITableModel) this.getModel()).fireTableDataChanged();
    }

    private final MIQuery query;

    public MITable(MITableModel model, IGBService service, final MIQuery query) {
        super(model);

        this.query = query;
        igbLogger = IGBLogger.getInstance(query.getLabel());
        this.igbService = service;

        this.SymSelectionListener = new MouseListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getComponent().isEnabled()
                        && e.getButton() == MouseEvent.BUTTON1) {

                    if (getSelectedRow() >= 0) {
                        int modelRow = convertRowIndexToModel(getSelectedRow());

                        MIResult interaction = ((MITableModel) getModel())
                                .getResult(modelRow);

                        structuresPanel
                                .setCurrentInteraction(interaction);
                    }

                    MITable table = (MITable) e.getComponent();
                    int modelRow = convertRowIndexToModel(table.getSelectedRow());
                    int column = table.getSelectedColumn();

                    if (e.getClickCount() == 1) {
                        if (column == MITableModel.TRACK_COLUMN) {
                            Object value = table.getValueAt(modelRow, column);
                            if (value instanceof JButton) {
                                MIResult result = ((MITableModel) table.getModel())
                                        .getResult(modelRow);

                                TypeContainerAnnot interactorTrack = result.createTrack();

                                igbService.addTrack(interactorTrack, interactorTrack.getID());

                                igbService.getSeqMapView().updatePanel();

                                for (TierGlyph t : igbService.getAllTierGlyphs()) {

                                    if (TierGlyph.TierType.ANNOTATION.equals(t.getTierType()) && (t.getAnnotStyle().getTrackName().equals(interactorTrack.getID()))) {

                                        SimpleTrackStyle style = new SimpleTrackStyle(interactorTrack.getID(), false) {

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

                                igbService.getSeqMapView().updatePanel();

                                ((JButton) value).setText(interactorTrack.getID());
                                ((JButton) value).setEnabled(false);

                                updateUI();
                            }
                        }
                    } else {
                        // symmetry: zoom-in
                        if (column == MITableModel.SYMS1_COLUMN) {
                            MoleculeEntry entry = ((MITableModel) table.getModel())
                                    .getResult(modelRow).getInteractor1();
                            if (query.getTaxid().equals(entry.getTaxid())) {
                                Collection<SeqSymmetry> syms = ((MITableModel) table.getModel())
                                        .getResult(modelRow).getSymmetries1();
                                zoomToSym(syms);
                            }
                        } else if (column == MITableModel.SYMS2_COLUMN) {
                            MoleculeEntry entry = ((MITableModel) table.getModel())
                                    .getResult(modelRow).getInteractor2();
                            if (query.getTaxid().equals(entry.getTaxid())) {
                                Collection<SeqSymmetry> syms = ((MITableModel) table.getModel())
                                        .getResult(modelRow).getSymmetries2();
                                zoomToSym(syms);
                            }
                        }

                        // Protein: link to uniprot
                        if (column == MITableModel.INTERACTOR1_COLUMN
                                || column == MITableModel.INTERACTOR2_COLUMN) {

                            MIResult miResult = ((MITableModel) table.getModel())
                                    .getResult(modelRow);

                            String id;
                            String taxid;

                            MoleculeEntry interactor;
                            if (column == MITableModel.INTERACTOR1_COLUMN) {
                                interactor = miResult.getInteractor1();
                            } else {
                                interactor = miResult.getInteractor2();
                            }

                            taxid = interactor.getTaxid();

                            String query;
                            String anchor = "";
                            switch (taxid) {
                                case MoleculeEntry.TAXID_DNA:
                                case MoleculeEntry.TAXID_RNA:
                                case MoleculeEntry.TAXID_LIGAND:
                                    if (miResult.getInteractionStructures().isEmpty()) {
                                        return;
                                    }
                                    query = "http://www.pdb.org/pdb/explore/explore.do?structureId=" + miResult.getInteractionStructures().iterator().next().getStructureID();
                                    break;
                                case MoleculeEntry.TAXID_MODIFICATION:
                                    query = "http://www.uniprot.org/uniprot/" + miResult.getInteractor1().getUniprotAc();
                                    anchor = "#ptm_processing";
                                    break;
                                default:
                                    id = interactor.getUniprotAc();
                                    query = "http://www.uniprot.org/uniprot/" + id;
                                    break;
                            }
                            try {
                                URI uri = new URI(URIUtil.encodeQuery(query) + anchor);

                                Desktop desktop = Desktop.isDesktopSupported() ? Desktop
                                        .getDesktop() : null;
                                if (desktop != null
                                        && desktop.isSupported(Desktop.Action.BROWSE)) {
                                    desktop.browse(uri);
                                }
                            } catch (IOException ioe) {
                                JOptionPane.showMessageDialog(null,
                                        "Cannot reach Uniprot website.");
                                return;
                            } catch (URISyntaxException ue) {
                                JOptionPane.showMessageDialog(null,
                                        "Cannot reach Uniprot website: " + query);
                            }
                        }

                        // interaction type: link to psicquic
                        if (column == MITableModel.INTERACTION_TYPE_COLUMN) {

                            MIResult miResult = ((MITableModel) table.getModel())
                                    .getResult(modelRow);

                            String queryURL;
                            String idA = miResult.getInteractor1().getUniprotAc();
                            String idB = miResult.getInteractor2().getUniprotAc();

                            if (null == miResult.getPsicquicUrl()) {
                                // from the structure database
                                if (query.searchDSysMap()) {
                                    queryURL = "http://http://dsysmap.irbbarcelona.org/results.php?type=proteins&neigh=2&value=" + idA + "," + idB;
                                } else if (query.searchInteractome3D()) {
                                    try {
                                        queryURL = "http://interactome3d.irbbarcelona.org/interaction.php?ids=" + idA + ";" + idB + "&dataset=" + it.iit.genomics.cru.bridges.interactome3d.ws.Utils.getDataset(query
                                                .getTaxid());
                                    } catch (Interactome3DException e3d) {
                                        // it will never happend: if the taxid was not known by 
                                        // I3D, we wouldn't have an interaction
                                        return;
                                    }

                                } else {
                                    return;
                                }
                            } else {

                                if (false == idA.equals(idB)) {
                                    queryURL = miResult.getPsicquicUrl() + "query/id:"
                                            + miResult.getInteractor1().getUniprotAc()
                                            + "* AND id:"
                                            + miResult.getInteractor2().getUniprotAc()
                                            + "*";
                                } else {
                                    queryURL = miResult.getPsicquicUrl() + "query/idA:"
                                            + miResult.getInteractor1().getUniprotAc()
                                            + "* AND idB:"
                                            + miResult.getInteractor2().getUniprotAc()
                                            + "*";
                                }
                            }
                            try {
                                URI uri = new URI(URIUtil.encodeQuery(queryURL));

                                Desktop desktop = Desktop.isDesktopSupported() ? Desktop
                                        .getDesktop() : null;
                                if (desktop != null
                                        && desktop.isSupported(Desktop.Action.BROWSE)) {
                                    desktop.browse(uri);
                                }
                            } catch (IOException ioe) {
                                JOptionPane.showMessageDialog(null,
                                        "Cannot reach psicquic server.");
                            } catch (URISyntaxException ue) {
                                JOptionPane.showMessageDialog(null,
                                        "Cannot reach psicquic server: " + queryURL);
                            }

                        }
                    }
                }

            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        };

        TableRowSorter<MITableModel> sorter = new MITableRowSorter(
                model);
        setRowSorter(sorter);

        sorter.setRowFilter(evidenceRowFilter());
        model.fireTableDataChanged();
        this.getTableHeader().setReorderingAllowed(false);

        TableCellRenderer rend = getTableHeader().getDefaultRenderer();
        TableColumnModel tcm = getColumnModel();
        for (int j = 0; j < tcm.getColumnCount(); j += 1) {
            TableColumn tc = tcm.getColumn(j);
            TableCellRenderer rendCol = tc.getHeaderRenderer(); // likely null
            if (rendCol == null) {
                rendCol = rend;
            }
            Component c = rendCol.getTableCellRendererComponent(this,
                    tc.getHeaderValue(), false, false, 0, j);
            tc.setPreferredWidth(c.getPreferredSize().width);
        }

        TableCellRenderer buttonRenderer = new JTableButtonRenderer();

        getColumn(model.getColumnName(MITableModel.TRACK_COLUMN))
                .setCellRenderer(buttonRenderer);

        getColumn(model.getColumnName(MITableModel.SYMS1_COLUMN))
                .setCellRenderer(new GeneRenderer());
        getColumn(model.getColumnName(MITableModel.SYMS2_COLUMN))
                .setCellRenderer(new GeneRenderer());

        getColumn(model.getColumnName(MITableModel.INTERACTOR1_COLUMN))
                .setCellRenderer(new MoleculeRenderer());
        getColumn(model.getColumnName(MITableModel.INTERACTOR2_COLUMN))
                .setCellRenderer(new MoleculeRenderer());

        getColumn(model.getColumnName(MITableModel.INTERACTION_TYPE_COLUMN))
                .setCellRenderer(new EvidenceRenderer());

        getColumn(model.getColumnName(MITableModel.STRUCTURES_COLUMN))
                .setCellRenderer(new StructuresRenderer());

        setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        int smallWidth = 75;
        int mediumWidth = 120;
        int largeWidth = 200;
        getColumnModel().getColumn(MITableModel.TRACK_COLUMN).setMinWidth(
                smallWidth);

        getColumnModel().getColumn(MITableModel.STRUCTURES_COLUMN).setMinWidth(
                smallWidth);
        getColumnModel().getColumn(MITableModel.STRUCTURES_COLUMN).setMaxWidth(
                smallWidth);
        getColumnModel().getColumn(MITableModel.STRUCTURES_COLUMN)
                .setPreferredWidth(smallWidth);

        addMouseListener(SymSelectionListener);

        getSelectionModel()
                .addListSelectionListener(new RowSelectionListener());

    }

    private static class JTableButtonRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            if (JButton.class.isInstance(value)) {
                return (JButton) value;
            }

            return (JLabel) value; //new JLabel(result.getTrackId());
        }
    }

    private class RowSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (getSelectedRow() >= 0) {
                int modelRow = convertRowIndexToModel(getSelectedRow());

                MIResult interaction = ((MITableModel) getModel())
                        .getResult(modelRow);

                structuresPanel
                        .setCurrentInteraction(interaction);
            }
        }
    }

    private final MouseListener SymSelectionListener;

    public void zoomToSym(Collection<SeqSymmetry> syms) {

        if (syms != null && false == syms.isEmpty()) {
            SeqSpan span = syms.iterator().next().getSpan(0);
            igbService.zoomToCoord(span.getBioSeq().getID(), span.getStart(), span.getEnd());
        }
    }

    class GeneRenderer extends DefaultTableCellRenderer
            implements TableCellRenderer {

        @SuppressWarnings("unchecked")
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) super
                    .getTableCellRendererComponent(table, value, isSelected,
                            hasFocus, row, column);

            MoleculeEntry protein = (MoleculeEntry) value;

            String display = protein.getGeneName();

            if (false == query.getTaxid().equals(protein.getTaxid())) {
                switch (protein.getTaxid()) {
                    case MoleculeEntry.TAXID_DNA:
                        // display += " (DNA)";
//                            renderer.setForeground(Color.DARK_GRAY);
                        break;
                    case MoleculeEntry.TAXID_RNA:
                        // display += " (RNA)";
//                            renderer.setForeground(Color.DARK_GRAY);
                        break;
                    case MoleculeEntry.TAXID_LIGAND:
                        //display += " (ligand)";
//                            renderer.setForeground(Color.MAGENTA);
                        break;
                    case MoleculeEntry.TAXID_MODIFICATION:
                        //display += " (ligand)";
//                            renderer.setForeground(Color.CYAN);
                        break;
                    default:
                        display += " (" + protein.getOrganism() + ")";
//                            renderer.setForeground(new Color(255,158,0));
                        break;
                }
            }

            if (isSelected) {
                renderer.setForeground(table.getSelectionForeground());
                renderer.setBackground(table.getSelectionBackground());
            } else {
                renderer.setForeground(table.getForeground());
                renderer.setBackground(table.getBackground());

                if (false == query.getTaxid().equals(protein.getTaxid())) {
                    renderer.setForeground(TaxonColorer.getColorer(query.getTaxid()).getColor(protein.getTaxid()));
                }
            }

            renderer.setText(display);

            return renderer;
        }

    }

    class MoleculeRenderer extends DefaultTableCellRenderer
            implements TableCellRenderer {

        @SuppressWarnings("unchecked")
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) super
                    .getTableCellRendererComponent(table, value, isSelected,
                            hasFocus, row, column);

            MoleculeEntry protein = (MoleculeEntry) value;

            String display = protein.getUniprotAc();

            if (isSelected) {
                renderer.setForeground(table.getSelectionForeground());
                renderer.setBackground(table.getSelectionBackground());
            } else {
                renderer.setForeground(table.getForeground());
                renderer.setBackground(table.getBackground());

                if (false == query.getTaxid().equals(protein.getTaxid())) {
                    renderer.setForeground(TaxonColorer.getColorer(query.getTaxid()).getColor(protein.getTaxid()));
                }
            }

            renderer.setText(display);

            return renderer;
        }

    }

    class EvidenceRenderer extends DefaultTableCellRenderer
            implements TableCellRenderer {

        @SuppressWarnings("unchecked")
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) super
                    .getTableCellRendererComponent(table, value, isSelected,
                            hasFocus, row, column);

            Interaction interaction = (Interaction) value;

            String summary = interaction.getInteractionTypes().contains(INTERACTION_TYPE_PDB) || interaction.getInteractionTypes().contains(INTERACTION_TYPE_I3D)
                    ? "<font color=\"#D3D3D3\">S</font>"
                    : "<font color=\"green\"><b>S</b></font>";

            String color = interaction.isPhysical() ? "red" : "#D3D3D3";
            summary += "<font color=\"" + color + "\">P</font>";

            color = interaction.isAssociation() ? "orange" : "#D3D3D3";
            summary += "<font color=\"" + color + "\">A</font>";

            color = interaction.isEnzymatic() ? "blue" : "#D3D3D3";
            summary += "<font color=\"" + color + "\">E</font>";

            color = interaction.isOther() ? "#FFC0CB" : "#D3D3D3";
            summary += "<font color=\"" + color + "\">O</font>";

            color = interaction.isUnspecified() ? "black" : "#D3D3D3";
            summary += "<font color=\"" + color + "\">U</font>";

            summary += "&#32;";

            int evidenceScore = interaction.getScore();

            if (evidenceScore == 0) {
                summary += HTML_SCORE_0;
            } else if (evidenceScore == 1) {
                summary += HTML_SCORE_1;
            } else if (evidenceScore == 2) {
                summary += HTML_SCORE_2;
            } else if (evidenceScore == 3) {
                summary += HTML_SCORE_3;
            }

            if (isSelected) {
                renderer.setForeground(table.getSelectionForeground());
                renderer.setBackground(table.getSelectionBackground());
            } else {
                renderer.setForeground(table.getForeground());
                renderer.setBackground(table.getBackground());
            }

            renderer.setText("<html>" + summary + "</html>");

            return renderer;
        }

    }

    class SeqSymmetryCollectionRenderer extends DefaultTableCellRenderer
            implements TableCellRenderer {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unchecked")
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) super
                    .getTableCellRendererComponent(table, value, isSelected,
                            hasFocus, row, column);

            renderer.setText(StringUtils.join(
                    Utils.getSymIds(((Collection<SeqSymmetry>) value)), ", "));

            if (column == MITableModel.SYMS1_COLUMN) {
                renderer.setForeground(Color.GREEN);
            } else if (column == MITableModel.SYMS2_COLUMN) {
                renderer.setForeground(Color.BLUE);
            }

            return renderer;
        }

    }

    class TrackRenderer extends JButton
            implements TableCellRenderer {

        public TrackRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            setText((value == null) ? "" : value.toString());
            return this;
        }

    }

    private class Square extends JComponent {

        private static final long serialVersionUID = 1L;

        Color color;

        // residue
        boolean redBorder;

        // interface
        boolean yellowBorder;

        public Square(Color color, boolean redBorder, boolean yellowBorder) {
            this.color = color;
            this.redBorder = redBorder;
            this.yellowBorder = yellowBorder;
        }

        @Override
        public void paint(Graphics g) {

            if (this.yellowBorder) {
                g.setColor(Color.RED);
                g.fillRoundRect(1, 1, this.getParent().getWidth() - 2, this
                        .getParent().getHeight() - 4, 2, 0);
            } else if (this.redBorder) {
                g.setColor(Color.ORANGE);
                g.fillRoundRect(1, 1, this.getParent().getWidth() - 2, this
                        .getParent().getHeight() - 4, 2, 0);
            }

            g.setColor(this.color);
            g.fillRoundRect(3, 3, this.getParent().getWidth() - 7, this
                    .getParent().getHeight() - 7, 2, 0);

        }
    }

    class StructuresRenderer extends JPanel implements TableCellRenderer {

        private static final long serialVersionUID = 1L;

        public StructuresRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            // setText("Check structure");
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

            StructureSummary s = (StructureSummary) value;

            if (s.hasStructure()) {
                panel.add(new Square(Color.GREEN, s.hasResidueA(), s.hasInterfaceA()));
                panel.add(new Square(Color.BLUE, s.hasResidueB(), s.hasInterfaceB()));
            } else {
                panel.add(new Square(Color.WHITE, false, false));
                panel.add(new Square(Color.WHITE, false, false));
            }

            return panel;
        }

    }

    private RowFilter<MITableModel, Integer> structureRowFilter() {
        return new RowFilter<MITableModel, Integer>() {
            @Override
            public boolean include(
                    Entry<? extends MITableModel, ? extends Integer> entry) {
                MITableModel model = entry.getModel();
                MIResult result = model.getResult(entry.getIdentifier());
                return showInteractionsWoStructure
                        || false == result.getInteractionStructures().isEmpty();
            }
        };
    }

    private RowFilter<MITableModel, Integer> evidenceRowFilter() {
        return new RowFilter<MITableModel, Integer>() {
            @Override
            public boolean include(
                    RowFilter.Entry<? extends MITableModel, ? extends Integer> entry) {
                MITableModel model = entry.getModel();
                MIResult result = model.getResult(entry.getIdentifier());

                Interaction interaction = result.getInteraction();

                if (interaction.getScore() < scoreLimit) {
                    return false;
                }

                if (showPhysical && interaction.isPhysical()) {
                    return true;
                }

                if (showAssociations && interaction.isAssociation()) {
                    return true;
                }

                if (showEnzymatic && interaction.isEnzymatic()) {
                    return true;
                }

                if (showOther && interaction.isOther()) {
                    return true;
                }

                if (showUnspecified && interaction.isUnspecified()) {
                    return true;
                }

                if (showStructures && false == result.getInteractionStructures().isEmpty()) {
                    return true;
                }

                return false;
            }
        };
    }

    // Implement table cell tool tips.
    @Override
    public String getToolTipText(MouseEvent e) {
        String tip = null;
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);

        try {

            ToolTipManager.sharedInstance().setInitialDelay(500);

            if (colIndex == MITableModel.STRUCTURES_COLUMN) {
                return "";
            }

            // comment row, exclude heading
            if (rowIndex >= 0) {
                int modelRow = rowAtPoint(e.getPoint()); // getSelectedRow(); //convertRowIndexToModel(getSelectedRow());
                Object value = getValueAt(modelRow, colIndex);
                if (colIndex == MITableModel.SYMS1_COLUMN
                        || colIndex == MITableModel.SYMS2_COLUMN
                        || colIndex == MITableModel.INTERACTOR1_COLUMN
                        || colIndex == MITableModel.INTERACTOR2_COLUMN) {
                    MoleculeEntry entry = (MoleculeEntry) value;
                    tip = entry.getGeneName() + " (" + entry.getUniprotAc() + ", " + entry.getOrganism() + ")";
                } else if (colIndex == MITableModel.TRACK_COLUMN) {
                    tip = ((MIResult) value).getTrackId();
                } else if (colIndex == MITableModel.INTERACTION_TYPE_COLUMN) {
                    Interaction interaction = (Interaction) value;
                    tip = "<html>Type: " + StringUtils.join(interaction.getInteractionTypes(), ", ")
                            + "<br/>Method: " + StringUtils.join(interaction.getMethods(), ", ")
                            + "<br/>Bibliographic references: " + StringUtils.join(interaction.getBibRefs(), ", ") + "</html>";
                } else {
                    tip = value.toString();
                }
            }
        } catch (RuntimeException e1) {
            igbLogger.getLogger().error("exception, row " + rowIndex, e1);
            // catch null pointer exception if mouse is over an empty line
        }

        return tip;
    }

    class MITableRowSorter extends TableRowSorter<MITableModel> {

        public MITableRowSorter(MITableModel model) {
            setModel(model);
        }

        Comparator GN_COMPARATOR = new Comparator<MoleculeEntry>() {

            @Override
            public int compare(MoleculeEntry o1, MoleculeEntry o2) {
                return o1.getGeneName().compareTo(o2.getGeneName());
            }
        };

//        Comparator EVIDENCE_COMPARATOR = new Comparator<Interaction>() {
//
//            @Override
//            public int compare(Interaction a1, Interaction a2) {
//                igbLogger.info("O1: " + a1.getClass() + ", " + a1);
//                igbLogger.info("O2: " + a2.getClass() + ", " + a2);
//                
//                Interaction o2 = (Interaction) a2;
//                Interaction o1 = (Interaction) a1;
//                
//                int score1 = o1.getScore();
//                int score2 = o2.getScore();
//
//                if (score1 != score2) {
//                    return Integer.compare(score1, score2);
//                }
//
//                int evidenceScore1 = 0;
//                if (o1.getInteractionTypes().contains(INTERACTION_TYPE_I3D) || o1.getInteractionTypes().contains(INTERACTION_TYPE_PDB)) {
//                    evidenceScore1 = 5;
//                } else if (o1.isPhysical()) {
//                    evidenceScore1 = 4;
//                } else if (o1.isAssociation()) {
//                    evidenceScore1 = 3;
//                } else if (o1.isEnzymatic()) {
//                    evidenceScore1 = 2;
//                } else if (o1.isOther()) {
//                    evidenceScore1 = 1;
//                } else if (o1.isUnspecified()) {
//                    evidenceScore1 = 0;
//                }
//
//                int evidenceScore2 = 0;
//                if (o2.getInteractionTypes().contains(INTERACTION_TYPE_I3D) || o2.getInteractionTypes().contains(INTERACTION_TYPE_PDB)) {
//                    evidenceScore2 = 5;
//                } else if (o2.isPhysical()) {
//                    evidenceScore2 = 4;
//                } else if (o2.isAssociation()) {
//                    evidenceScore2 = 3;
//                } else if (o2.isEnzymatic()) {
//                    evidenceScore2 = 2;
//                } else if (o2.isOther()) {
//                    evidenceScore2 = 1;
//                } else if (o2.isUnspecified()) {
//                    evidenceScore2 = 0;
//                }
//
//                return Integer.compare(evidenceScore1, evidenceScore2);
//            }
//        };
        Comparator MOLECULE_COMPARATOR = new Comparator<MoleculeEntry>() {

            @Override
            public int compare(MoleculeEntry o1, MoleculeEntry o2) {
                return o1.compareTo(o2);
            }
        };

        Comparator DEFAULT_COMPARATOR = new ComparableComparator<>();
//new Comparator<T extends Comparable<T>>() {
////public class ComparableComparator<T extends Comparable<T>> implements Comparator<T> {
//            @Override
//            public int compare(T lhs, T rhs) {
//                return lhs.compareTo(rhs);
//            }
//        };
//            @Override
//            public int compare(String o1, String o2) {
//                return o1.compareTo(o2);
//            }
//        };

        @Override
        public Comparator<?> getComparator(int column) {
            if (column == MITableModel.SYMS1_COLUMN || column == MITableModel.SYMS2_COLUMN) {
                return GN_COMPARATOR;
            } else if (column == MITableModel.INTERACTOR1_COLUMN || column == MITableModel.INTERACTOR2_COLUMN) {
                return MOLECULE_COMPARATOR;
            } else if (column == MITableModel.INTERACTION_TYPE_COLUMN) {
                return DEFAULT_COMPARATOR;
            } else {
                return DEFAULT_COMPARATOR;
            }
        }

    }

    public class ComparableComparator<T extends Comparable<T>> implements Comparator<T> {

        @Override
        public int compare(T lhs, T rhs) {
            return lhs.compareTo(rhs);
        }
    }

}
