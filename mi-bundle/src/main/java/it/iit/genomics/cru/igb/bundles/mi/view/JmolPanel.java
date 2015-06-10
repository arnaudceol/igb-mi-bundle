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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JPanel;

import org.biojava.nbio.structure.Structure;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.viewer.Viewer;

/**
 *
 * @author Arnaud Ceol
 *
 * Jmol Panel
 *
 */
public class JmolPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    Viewer viewer;

    JmolAdapter adapter;

    public JmolPanel() {
        adapter = new SmarterJmolAdapter();
        viewer = (Viewer) JmolViewer.allocateViewer(this, adapter);
    }

    public Viewer getViewer() {
        return viewer;
    }

    public void executeCmd(String rasmolScript) {
        viewer.evalStringQuiet(rasmolScript);
    }

    final Dimension currentSize = new Dimension();
    final Rectangle rectClip = new Rectangle();

    @Override
    public void paint(Graphics g) {
        getSize(currentSize);
        g.getClipBounds(rectClip);
        viewer.renderScreenImage(g, currentSize.width, currentSize.height);
    }

    public void setStructure(Structure s) {

        this.setName(s.getPDBCode());

        String pdb = s.toPDB();

        Viewer structureViewer = this.getViewer();
        structureViewer.evalString("set showScript OFF");
        // send the PDB file to Jmol.
        structureViewer.openStringInline(pdb);
        structureViewer.evalString("select *; spacefill off; wireframe off; backbone 0.4;  ");
        structureViewer.evalString("color chain;  ");

        this.viewer = structureViewer;

    }

}
