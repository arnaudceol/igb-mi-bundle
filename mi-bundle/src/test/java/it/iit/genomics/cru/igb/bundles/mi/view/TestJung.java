/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.iit.genomics.cru.igb.bundles.mi.view;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import it.iit.genomics.cru.structures.model.MoleculeEntry;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author arnaudceol
 */
public class TestJung {

    private static class EdgeInteraction {

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

    public static void main(String[] args) {

        Graph<MoleculeEntry, EdgeInteraction> graph = new SparseMultigraph<>();

        MoleculeEntry v1 = new MoleculeEntry("A");
        v1.addGeneName("A");
        v1.setTaxid("9606");

        MoleculeEntry v2 = new MoleculeEntry("B");
        v2.addGeneName("b");
        v2.setTaxid("9606");

        MoleculeEntry v3 = new MoleculeEntry("DNA");
        v3.addGeneName("DNA");
        v3.setTaxid(MoleculeEntry.TAXID_DNA);

        EdgeInteraction edge = new EdgeInteraction(true, true,true, "e1");
        graph.addEdge(edge, v1, v2, EdgeType.UNDIRECTED);

        EdgeInteraction edge2 = new EdgeInteraction(false, false, true, "e2");
        graph.addEdge(edge2, v1, v3, EdgeType.UNDIRECTED);

        EdgeInteraction edge3 = new EdgeInteraction(false, false, false, "e3");
        graph.addEdge(edge3, v2, v3, EdgeType.UNDIRECTED);

// The Layout<V, E> is parameterized by the vertex and edge types
        Layout<MoleculeEntry, EdgeInteraction> layout = new ISOMLayout(graph);

        layout.setSize(
                new Dimension(500, 600)); // sets the initial size of the space
// The BasicVisualizationServer<V,E> is parameterized by the edge types
        VisualizationViewer<MoleculeEntry, EdgeInteraction> vv
                = new VisualizationViewer<>(layout);

        vv.setPreferredSize(
                new Dimension(550, 650)); //Sets the viewing area size
vv.setBackground(Color.WHITE);
        Transformer<MoleculeEntry, Paint> vertexPaint = new Transformer<MoleculeEntry, Paint>() {
            @Override
            public Paint transform(MoleculeEntry molecule) {
                switch (molecule.getTaxid()) {
                    case MoleculeEntry.TAXID_DNA:
                        ;
                    case MoleculeEntry.TAXID_RNA:
                        return Color.GREEN;
                    case MoleculeEntry.TAXID_LIGAND:
                        return Color.MAGENTA;
                    default:
                        return Color.GREEN;
                }
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
                        return s.getGeneName();
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

        JFrame frame = new JFrame("Network " + "A");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane()
                .add(vv);
        frame.pack();

        frame.setVisible(
                true);
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
            this(new Shape[]{shapes}, advance,  start,  end);
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
            boolean firstDrawn= false;
            while (currentShape < length && !it.isDone()) {
                type = it.currentSegment(points);
                switch (type) {
                    case PathIterator.SEG_MOVETO:
                        moveX = lastX = points[0];
                        moveY = lastY = points[1];
                        result.moveTo(moveX, moveY);
                        if (false == first) {
                            
                        firstX=lastX;
                        firstY=lastY;
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
                        
                        float sdx =  thisX - firstX;
                        float sdy = thisY -firstY;
                        
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

}
