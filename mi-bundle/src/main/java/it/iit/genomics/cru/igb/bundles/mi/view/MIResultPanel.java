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

import static it.iit.genomics.cru.igb.bundles.mi.business.MIResult.HTML_SCORE_0;
import static it.iit.genomics.cru.igb.bundles.mi.business.MIResult.HTML_SCORE_1;
import static it.iit.genomics.cru.igb.bundles.mi.business.MIResult.HTML_SCORE_2;
import static it.iit.genomics.cru.igb.bundles.mi.business.MIResult.HTML_SCORE_3;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.httpclient.util.URIUtil;
import org.lorainelab.igb.services.IgbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.affymetrix.common.CommonUtils;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import it.iit.genomics.cru.igb.bundles.mi.business.MIResult;
import it.iit.genomics.cru.igb.bundles.mi.commons.MIBundleConfiguration;
import it.iit.genomics.cru.igb.bundles.mi.commons.MICommons;
import it.iit.genomics.cru.igb.bundles.mi.model.TaxonColorer;
import it.iit.genomics.cru.igb.bundles.mi.query.MIQuery;
import it.iit.genomics.cru.structures.model.MoleculeEntry;

/**
 *
 * @author Arnaud Ceol
 *
 * Panel to display all results of a query.
 */
public class MIResultPanel extends JPanel {

    private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(MIResultPanel.class);

    private MITable miTable;

    private StructuresPanel structures;

    private final JSplitPane resultBox;

    private String label;

    private TaxonColorer colorer;

    private final static String HTML_CHECKBOX_STRUCTURE = "<html><font color=\"green\"><b>S</b></font>ructure/model</html>";
    private final static String HTML_CHECKBOX_PHYSICAL = "<html><font color=\"red\"><b>P</b></font>hysical</html>";
    private final static String HTML_CHECKBOX_ASSOCIATION = "<html><font color=\"orange\"><b>A</b></font>ssociation</html>";
    private final static String HTML_CHECKBOX_ENZYMATIC = "<html><font color=\"blue\"><b>E</b></font>nzymatic</html>";
    private final static String HTML_CHECKBOX_OTHER = "<html><font color=\"pink\"><b>O</b></font>ther</html>";
    private final static String HTML_CHECKBOX_UNSPECIFIED = "<html><font color=\"black\"><b>U</b></font>nspecified</html>";

    public MIResultPanel(IgbService service, String summary,
            List<MIResult> results, String label, MIQuery query) {
        setLayout(new BorderLayout());

        this.label = label;


        colorer = TaxonColorer.getColorer(query.getTaxid());

        Box menuBox = new Box(BoxLayout.X_AXIS);

        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        Box buttonBox1 = new Box(BoxLayout.X_AXIS);

        Box buttonBox3 = new Box(BoxLayout.X_AXIS);
        buttonBox.add(buttonBox1);

        buttonBox.add(buttonBox3);

        JTextPane querySummary = new JTextPane();
        querySummary.setContentType("text/html");
        querySummary.setEditable(false);
        querySummary.setText(summary);

        menuBox.add(querySummary);

        menuBox.add(buttonBox);

        final JFrame logFrame = new JFrame("MI Bundle Log");
        logFrame.setVisible(false);
        Dimension preferredSize = new Dimension(800, 500);

        logFrame.setPreferredSize(preferredSize);
        logFrame.setMinimumSize(preferredSize);

        JButton networkButton = new JButton("");
        networkButton.setIcon(new ImageIcon(getClass().getResource("/network.jpg")));
        networkButton.addActionListener(new DisplayNetworkActionListener());

        buttonBox1.add(networkButton);

        buttonBox1.add(new JSeparator(JSeparator.VERTICAL));

        buttonBox1.add(new JLabel("Save: "));
        JButton exportButton = new JButton("text");
        exportButton.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/save.png"));
        exportButton.addActionListener(new ExportActionListener());

        if (false == MICommons.testVersion) {
            buttonBox1.add(exportButton);
        }

        JButton exportXgmmlButton = new JButton("xgmml");
        exportXgmmlButton.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/save.png"));
        exportXgmmlButton.addActionListener(new ExportXgmmlActionListener());
        if (false == MICommons.testVersion) {
            buttonBox1.add(exportXgmmlButton);
        }

        buttonBox1.add(new JSeparator(JSeparator.VERTICAL));

        buttonBox1.add(new JLabel("View structure: "));

        structures = new StructuresPanel(service, label);

        buttonBox1.add(structures.getJmolButton());
        buttonBox1.add(structures.getLinkButton());

        // Filters
        ButtonGroup scoreGroup = new ButtonGroup();
        JRadioButton scoreButton0 = new JRadioButton("<html>" + HTML_SCORE_0 + "</html>");
        JRadioButton scoreButton1 = new JRadioButton("<html>" + HTML_SCORE_1 + "</html>");
        JRadioButton scoreButton2 = new JRadioButton("<html>" + HTML_SCORE_2 + "</html>");
        JRadioButton scoreButton3 = new JRadioButton("<html>" + HTML_SCORE_3 + "</html>");
        scoreButton0.setSelected(true);

        ScoreListener scoreListener = new ScoreListener();
        scoreButton0.addActionListener(scoreListener);
        scoreButton1.addActionListener(scoreListener);
        scoreButton2.addActionListener(scoreListener);
        scoreButton3.addActionListener(scoreListener);

        scoreGroup.add(scoreButton0);
        scoreGroup.add(scoreButton1);
        scoreGroup.add(scoreButton2);
        scoreGroup.add(scoreButton3);

        buttonBox1.add(new JSeparator(JSeparator.VERTICAL));
        buttonBox1.add(new JLabel("Score: "));
        buttonBox1.add(scoreButton0);
        buttonBox1.add(scoreButton1);
        buttonBox1.add(scoreButton2);
        buttonBox1.add(scoreButton3);

        buttonBox3.add(new JLabel("Interaction type: "));

        JCheckBox EvidencePhysicalButton = new JCheckBox(HTML_CHECKBOX_PHYSICAL);
        JCheckBox EvidenceAssociationButton = new JCheckBox(HTML_CHECKBOX_ASSOCIATION);
        JCheckBox EvidenceEnzymaticButton = new JCheckBox(HTML_CHECKBOX_ENZYMATIC);
        JCheckBox EvidenceOtherButton = new JCheckBox(HTML_CHECKBOX_OTHER);
        JCheckBox EvidenceUnspecifiedButton = new JCheckBox(HTML_CHECKBOX_UNSPECIFIED);
        JCheckBox EvidenceStructureButton = new JCheckBox(HTML_CHECKBOX_STRUCTURE);

        EvidencePhysicalButton.setSelected(true);
        EvidenceAssociationButton.setSelected(true);
        EvidenceEnzymaticButton.setSelected(true);
        EvidenceOtherButton.setSelected(true);
        EvidenceUnspecifiedButton.setSelected(true);
        EvidenceStructureButton.setSelected(true);

        buttonBox3.add(EvidencePhysicalButton);
        buttonBox3.add(EvidenceAssociationButton);
        buttonBox3.add(EvidenceEnzymaticButton);
        buttonBox3.add(EvidenceOtherButton);
        buttonBox3.add(EvidenceUnspecifiedButton);
        buttonBox3.add(EvidenceStructureButton);

        EvidenceTypeListener evidenceListener = new EvidenceTypeListener();
        EvidencePhysicalButton.addActionListener(evidenceListener);
        EvidenceAssociationButton.addActionListener(evidenceListener);
        EvidenceEnzymaticButton.addActionListener(evidenceListener);
        EvidenceOtherButton.addActionListener(evidenceListener);
        EvidenceUnspecifiedButton.addActionListener(evidenceListener);
        EvidenceStructureButton.addActionListener(evidenceListener);

        Box tableBox = new Box(BoxLayout.Y_AXIS);

        MITableModel model = new MITableModel(results);

        miTable = new MITable(model, service, query);

        miTable.setFillsViewportHeight(true);

        miTable.setStructuresPanel(structures);

        tableBox.add(miTable.getTableHeader());
        tableBox.add(miTable);

        JScrollPane tableScroll = new JScrollPane(tableBox);

        tableScroll.setMinimumSize(new Dimension(800, 50));
        tableScroll.setPreferredSize(new Dimension(800, 50));
        tableScroll.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        structures.setMinimumSize(new Dimension(200, 500));
        structures.setPreferredSize(new Dimension(200, 500));
        structures.setMaximumSize(new Dimension(200, Short.MAX_VALUE));

        resultBox = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                tableScroll, structures);
        resultBox.setOneTouchExpandable(true);

        add(menuBox, BorderLayout.NORTH);
        add(resultBox, BorderLayout.CENTER);

    }

    public class PsicquicLinkActionListener implements ActionListener {

        PsicquicLinkActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {

                if (false == miTable.getSelectedRow() >= 0) {
                    JOptionPane.showMessageDialog(null,
                            "Please select an interaction (row).");
                    return;
                }

                int modelRow = miTable.convertRowIndexToModel(miTable
                        .getSelectedRow());

                MIResult miResult = ((MITableModel) miTable.getModel())
                        .getResult(modelRow);

                String query;

                String idA = miResult.getInteractor1().getUniprotAc();
                String idB = miResult.getInteractor2().getUniprotAc();

                if (false == idA.equals(idB)) {
                    query = miResult.getPsicquicUrl() + "query/id:"
                            + miResult.getInteractor1().getUniprotAc()
                            + "* AND id:"
                            + miResult.getInteractor2().getUniprotAc() + "*";
                } else {
                    query = miResult.getPsicquicUrl() + "query/idA:"
                            + miResult.getInteractor1().getUniprotAc()
                            + "* AND idB:"
                            + miResult.getInteractor2().getUniprotAc() + "*";
                }

                URI uri = new URI(URIUtil.encodeQuery(query));

                Desktop desktop = Desktop.isDesktopSupported() ? Desktop
                        .getDesktop() : null;
                if (desktop != null
                        && desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(uri);
                }

            } catch (HeadlessException | URISyntaxException | IOException ex) {
                logger.error(null, ex);
            }
        }
    }

    public class ExportActionListener implements ActionListener {

        ExportActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            logger.info(
                    "Generate tab delimited code for query " + label);

            String header = "uniprot A\tgene A\tGenome Positions A\tResidues A"
                    + "\tuniprot B\tgene B\tGenome Positions B\tResidues B"
                    + "\tSelected regions A\tSelected regions B"
                    + "\tSelected contact regions A\tSelected contact regions B"
                    + "\tSelected contact AA A\tSelected contact AA B";

            CustomFileChooser fileChooser = new CustomFileChooser(MIBundleConfiguration
                    .getInstance().getExportFolder(), "txt", "tab-delimited file");

            fileChooser.setDialogTitle("Save results");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);

            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String fileName = file.getPath();

                String folder = file.getParentFile().getAbsolutePath();
                if (false == folder.equals(MIBundleConfiguration.getInstance()
                        .getExportFolder())) {
                    MIBundleConfiguration.getInstance().setExportFolder(folder);
                }

                try {
                    try (FileWriter out = new FileWriter(file)) {
                        out.append(header);
                        for (int tableRow = 0; tableRow < miTable.getRowCount(); tableRow++) {
                            int modelRow = miTable.convertRowIndexToModel(tableRow);
                            MIResult miResult = ((MITableModel) miTable.getModel())
                                    .getResult(modelRow);
                            out.append("\n" + miResult.toTab());
                        }
                        out.flush();
                    }
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(new JFrame(),
                            "Fail to save the results in " + fileName,
                            "Export error", JOptionPane.ERROR_MESSAGE);
                }

            }

        }
    }

    public class DisplayNetworkActionListener implements ActionListener {

        DisplayNetworkActionListener() {
        }

        private class EdgeInteraction {

            private boolean hasStructure;
            private boolean hasContactsA;
            private boolean hasContactsB;
            private String label;

            public EdgeInteraction(boolean hasStructure, boolean hasContactsA, boolean hasContactsB, String label) {
                this.hasStructure = hasStructure;
                this.hasContactsA = hasContactsA;
                this.hasContactsB = hasContactsB;
                this.label = label;
            }

            public boolean hasStructure() {
                return hasStructure;
            }

            public boolean hasContactsA() {
                return hasContactsA;
            }

            public boolean hasContactsB() {
                return hasContactsB;
            }

            public String getLabel() {
                return label;
            }

        }

        @Override
        public void actionPerformed(ActionEvent e) {

            Graph<MoleculeEntry, EdgeInteraction> graph = new SparseMultigraph<>();

            for (int tableRow = 0; tableRow < miTable.getRowCount(); tableRow++) {
                int modelRow = miTable.convertRowIndexToModel(tableRow);
                MIResult miResult = ((MITableModel) miTable.getModel())
                        .getResult(modelRow);

                EdgeInteraction edge = new EdgeInteraction(false == miResult.getInteractionStructures().isEmpty(), miResult.hasInterfaceOnStructureA(), miResult.hasInterfaceOnStructureB(), miResult.getTrackId());
                graph.addEdge(edge, miResult.getInteractor1(), miResult.getInteractor2(), EdgeType.UNDIRECTED);

            }

            Layout<MoleculeEntry, EdgeInteraction> layout = new FRLayout(graph);
            layout.setSize(new Dimension(500, 600)); // sets the initial size of the space

            VisualizationViewer<MoleculeEntry, EdgeInteraction> vv
                    = new VisualizationViewer<>(layout);
            vv.setPreferredSize(new Dimension(550, 650)); //Sets the viewing area size
            vv.setBackground(Color.WHITE);
            Transformer<MoleculeEntry, Paint> vertexPaint = new Transformer<MoleculeEntry, Paint>() {
                @Override
                public Paint transform(MoleculeEntry molecule) {
                    return colorer.getColor(molecule.getTaxid());
                }
            };

            Transformer<EdgeInteraction, Paint> edgePaint = new Transformer<EdgeInteraction, Paint>() {
                @Override
                public Paint transform(EdgeInteraction interaction) {
                    return interaction.hasStructure ? Color.BLACK : Color.GRAY;
                }
            };

            final Stroke edgeStroke01 = new BasicStroke();

            final float nodeSize = 20;

            final Stroke edgeStrokeBothContacts = new ShapeStroke(new Shape[]{
                new Ellipse2D.Float(0, 0, 10, 10)
            },
                    nodeSize, true, true);

            final Stroke edgeStrokeStartContacts = new ShapeStroke(new Shape[]{
                new Ellipse2D.Float(0, 0, 10, 10)
            },
                    nodeSize, true, false);

            final Stroke edgeStrokeEndContacts = new ShapeStroke(new Shape[]{
                new Ellipse2D.Float(0, 0, 10, 10)
            },
                    nodeSize, false, true);

            final Stroke edgeStrokeBothContact = new CompoundStroke(edgeStroke01, edgeStrokeBothContacts, CompoundStroke.ADD);

            final Stroke edgeStrokeStartContact = new CompoundStroke(edgeStroke01, edgeStrokeStartContacts, CompoundStroke.ADD);

            final Stroke edgeStrokeEndContact = new CompoundStroke(edgeStroke01, edgeStrokeEndContacts, CompoundStroke.ADD);

            Transformer<EdgeInteraction, Stroke> edgeStrokeTransformer
                    = new Transformer<EdgeInteraction, Stroke>() {
                        @Override
                        public Stroke transform(EdgeInteraction s) {
                            if (s.hasContactsA && s.hasContactsB) {
                                return edgeStrokeBothContact;
                            }

                            if (s.hasContactsA) {
                                return edgeStrokeStartContact;
                            }

                            if (s.hasContactsB) {
                                return edgeStrokeEndContact;
                            }

                            return edgeStroke01;
                        }
                    };

            Transformer<MoleculeEntry, String> moleculeLabeller
                    = new Transformer<MoleculeEntry, String>() {
                        @Override
                        public String transform(MoleculeEntry s) {
                            return s.getGeneName() != null ? s.getGeneName() : s.getUniprotAc();
                        }
                    };

            vv.getRenderContext()
                    .setVertexFillPaintTransformer(vertexPaint);
            vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);
            vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);

            vv.getRenderContext()
                    .setVertexLabelTransformer(moleculeLabeller);

            vv.getRenderer()
                    .getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

            DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();

            graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

            vv.setGraphMouse(graphMouse);
            JFrame frame = new JFrame("Network " + label);

            frame.getContentPane().add(vv);
            frame.pack();
            frame.setVisible(true);
        }

    }

    public class ExportXgmmlActionListener implements ActionListener {

        ExportXgmmlActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            logger.info(
                    "Generate xgmml code for query " + label);

            // default file name
            CustomFileChooser fileChooser = new CustomFileChooser(MIBundleConfiguration
                    .getInstance().getExportFolder(), "xgmml", "eXtensible Graph Markup and Modeling Language");

            fileChooser.setDialogTitle("Save results");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);

            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                String folder = file.getParentFile().getAbsolutePath();
                if (false == folder.equals(MIBundleConfiguration.getInstance()
                        .getExportFolder())) {
                    MIBundleConfiguration.getInstance().setExportFolder(folder);
                }

                String export = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                        + "\n<graph label=\""
                        + label
                        + "\""
                        + "\nxmlns:dc=\"http://purl.org/dc/elements/1.1/\""
                        + "\nxmlns:xlink=\"http://www.w3.org/1999/xlink\""
                        + "\nxmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
                        + "\nxmlns:cy=\"http://www.cytoscape.org\""
                        + "\nxmlns=\"http://www.cs.rpi.edu/XGMML\""
                        + "\ndirected=\"0\">";

                String xgmmlEdges = "";

                ArrayList<String> interactors = new ArrayList<>();

                for (int tableRow = 0; tableRow < miTable.getRowCount(); tableRow++) {
                    int modelRow = miTable.convertRowIndexToModel(tableRow);
                    MIResult miResult = ((MITableModel) miTable.getModel())
                            .getResult(modelRow);
                    xgmmlEdges += "\n" + miResult.toXgmml();

                    if (false == interactors.contains(miResult
                            .getContainerId1())) {
                        interactors.add(miResult.getContainerId1());
                        export += "\n"
                                + "<node id=\""
                                + miResult.getContainerId1()
                                + "\" label=\""
                                + miResult.getInteractor1().getGeneName()
                                + "\"><att type=\"string\" name=\"uniprotAcc\" value=\""
                                + miResult.getInteractor1().getUniprotAc()
                                + "\"/><att type=\"integer\" name=\"numRegions\" value=\""
                                + miResult.getResiduesA().size()
                                + "\"/><att type=\"string\" name=\"species\" value=\""
                                + miResult.getInteractor1().getOrganism()
                                + "\"/></node>";
                    }

                    if (false == interactors.contains(miResult
                            .getContainerId2())) {
                        interactors.add(miResult.getContainerId1());
                        export += "\n"
                                + "<node id=\""
                                + miResult.getContainerId2()
                                + "\" label=\""
                                + miResult.getInteractor2().getGeneName()
                                + "\"><att type=\"string\"  name=\"uniprotAcc\" value=\""
                                + miResult.getInteractor2().getUniprotAc()
                                + "\"/><att type=\"integer\" name=\"numRegions\" value=\""
                                + miResult.getResiduesB().size()
                                + "\"/><att type=\"string\" name=\"species\" value=\""
                                + miResult.getInteractor2().getOrganism()
                                + "\"/></node>";
                    }
                }

                export += xgmmlEdges;
                export += "\n</graph>";

                String fileName = file.getPath();
                try {
                    try (FileWriter out = new FileWriter(file)) {
                        out.append(export);
                        out.flush();
                    }
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(new JFrame(),
                            "Fail to save the results in " + fileName,
                            "Export error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public class FileTypeFilter extends FileFilter {

        private final String extension;
        private final String description;

        public FileTypeFilter(String extension, String description) {
            this.extension = extension;
            this.description = description;
        }

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            return file.getName().endsWith(extension);
        }

        @Override
        public String getDescription() {
            return description + String.format(" (*%s)", extension);
        }
    }

    /**
     * The divider location should be set after the component has become
     * visible.
     */
    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        resultBox.setDividerLocation((int) resultBox.getParent().getParent().getWidth() * 4 / 5); //.setDividerLocation((int) (resultBox.getWidth() - structures.getWidth()));
    }

    public static class ShapeStroke implements Stroke {

        private Shape shapes[];
        private float advance;

        private final boolean repeat = true;
        private final AffineTransform t = new AffineTransform();
        private static final float FLATNESS = 1;

        private boolean start;
        private boolean end;

        public ShapeStroke(Shape shapes, float advance, boolean start, boolean end) {
            this(new Shape[]{shapes}, advance, start, end);
        }

        public ShapeStroke(Shape shapes[], float advance, boolean start, boolean end) {
            this.advance = advance;
            this.shapes = new Shape[shapes.length];
            this.start = start;
            this.end = end;

            for (int i = 0; i < this.shapes.length; i++) {
                Rectangle2D bounds = shapes[i].getBounds2D();
                t.setToTranslation(-bounds.getCenterX(), -bounds.getCenterY());
                this.shapes[i] = t.createTransformedShape(shapes[i]);
            }
        }

        @Override
        public Shape createStrokedShape(Shape shape) {
            GeneralPath result = new GeneralPath();

            if (false == start && false == end) {
                return result;
            }

            PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);
            float points[] = new float[6];
            float moveX = 0, moveY = 0;
            float lastX = 0, lastY = 0;
            float thisX = 0, thisY = 0;
            int type = 0;
            boolean first = false;

            Shape secondLast = null;
            Shape lastShape = null;

            float firstX = 0;
            float firstY = 0;

            float next = 0;
            int currentShape = 0;
            int length = shapes.length;

            float factor = 1;
            boolean firstDrawn = false;
            while (currentShape < length && !it.isDone()) {
                type = it.currentSegment(points);
                switch (type) {
                    case PathIterator.SEG_MOVETO:
                        moveX = lastX = points[0];
                        moveY = lastY = points[1];
                        result.moveTo(moveX, moveY);
                        if (false == first) {

                            firstX = lastX;
                            firstY = lastY;
                        }
                        first = true;
                        next = 0;
                    //break;

                    case PathIterator.SEG_CLOSE:
                        points[0] = moveX;
                        points[1] = moveY;
                    // Fall into....

                    case PathIterator.SEG_LINETO:
                        thisX = points[0];
                        thisY = points[1];
                        float dx = thisX - lastX;
                        float dy = thisY - lastY;
                        float distance = (float) Math.sqrt(dx * dx + dy * dy);

                        float sdx = thisX - firstX;
                        float sdy = thisY - firstY;

                        if (distance >= next) {
                            float r = 1.0f / distance;
                            float angle = (float) Math.atan2(dy, dx);
                            while (currentShape < length && distance >= next) {
                                float x = lastX + next * dx * r;
                                float y = lastY + next * dy * r;
                                t.setToTranslation(x, y);
                                t.rotate(angle);
                                Shape s = t.createTransformedShape(shapes[currentShape]);
                                if (lastShape == null) {
                                    secondLast = s;
                                } else {
                                    secondLast = lastShape;
                                }
                                lastShape = s;
                                float d = (float) Math.sqrt(sdx * sdx + sdy * sdy);
                                if (start && d > advance && false == firstDrawn) {
                                    result.append(s, false);
                                    firstDrawn = true;
                                    if (false == end) {
                                        return result;
                                    }
                                }
                                next += advance;
                                currentShape++;
                                if (repeat) {
                                    currentShape %= length;
                                }
                            }
                        }
                        next -= distance;
                        first = false;
                        lastX = thisX;
                        lastY = thisY;
                        break;
                }
                it.next();
            }
            if (end) {
                result.append(secondLast, false);
            }
            return result;
        }

    }

    public static class CompoundStroke implements Stroke {

        public final static int ADD = 0;
        public final static int SUBTRACT = 1;
        public final static int INTERSECT = 2;
        public final static int DIFFERENCE = 3;

        private final Stroke stroke1, stroke2;
        private final int operation;

        public CompoundStroke(Stroke stroke1, Stroke stroke2, int operation) {
            this.stroke1 = stroke1;
            this.stroke2 = stroke2;
            this.operation = operation;
        }

        @Override
        public Shape createStrokedShape(Shape shape) {
            Area area1 = new Area(stroke1.createStrokedShape(shape));
            Area area2 = new Area(stroke2.createStrokedShape(shape));
            switch (operation) {
                case ADD:
                    area1.add(area2);
                    break;
                case SUBTRACT:
                    area1.subtract(area2);
                    break;
                case INTERSECT:
                    area1.intersect(area2);
                    break;
                case DIFFERENCE:
                    area1.exclusiveOr(area2);
                    break;
            }
            return area1;
        }
    }

    class ScoreListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            switch (e.getActionCommand()) {
                case "<html>" + HTML_SCORE_0 + "</html>":
                    miTable.setScoreLimit(0);
                    break;
                case "<html>" + HTML_SCORE_1 + "</html>":
                    miTable.setScoreLimit(1);
                    break;
                case "<html>" + HTML_SCORE_2 + "</html>":
                    miTable.setScoreLimit(2);
                    break;
                case "<html>" + HTML_SCORE_3 + "</html>":
                    miTable.setScoreLimit(3);
                    break;
            }
        }
    }

    class EvidenceTypeListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();

            switch (e.getActionCommand()) {
                case HTML_CHECKBOX_PHYSICAL:
                    miTable.setShowPhysical(abstractButton.isSelected());
                    break;
                case HTML_CHECKBOX_ASSOCIATION:
                    miTable.setShowAssociations(abstractButton.isSelected());
                    break;
                case HTML_CHECKBOX_ENZYMATIC:
                    miTable.setShowEnzymatic(abstractButton.isSelected());
                    break;
                case HTML_CHECKBOX_OTHER:
                    miTable.setShowOther(abstractButton.isSelected());
                    break;
                case HTML_CHECKBOX_UNSPECIFIED:
                    miTable.setShowUnspecified(abstractButton.isSelected());
                    break;
                case HTML_CHECKBOX_STRUCTURE:
                    miTable.setShowStructure(abstractButton.isSelected());
                    break;
            }
        }
    }

    public static void main(String[] args) {
        MIResultPanel p = new MIResultPanel(null, "test",
                new ArrayList<MIResult>(), "test", new MIQuery());
        JFrame f = new JFrame();
        f.setSize(1024, 800);
        f.add(p);
        f.show();
    }

}
