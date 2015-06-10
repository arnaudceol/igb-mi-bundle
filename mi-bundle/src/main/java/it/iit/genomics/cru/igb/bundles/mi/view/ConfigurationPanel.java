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

import com.affymetrix.genometry.event.GenericAction;
import it.iit.genomics.cru.igb.bundles.mi.commons.MIBundleConfiguration;
import it.iit.genomics.cru.igb.bundles.mi.commons.MIView;
import it.iit.genomics.cru.structures.bridges.pdb.PDBUtils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;

import com.affymetrix.igb.shared.IPrefEditorComponent;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Arnaud Ceol
 *
 * Panel to manage the configuration of the MI Bundle, e.g. local PDB mirror
 *
 */
public final class ConfigurationPanel extends IPrefEditorComponent {

    private static final long serialVersionUID = 1L;

    private final JLabel pdbLocalPathLabel;
    private final JLabel userStructuresLabel;
    private final JLabel i3dLocalPathLabel;

    private JComboBox<String> pdbMirrorBox;

    // cache
    private JLabel cacheLabel = new JLabel();

    public ConfigurationPanel() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setAlignmentY(Component.TOP_ALIGNMENT);

        MIBundleConfiguration.getInstance().loadProperties();

        JPanel left = new JPanel();
        left.setLayout(new GridBagLayout());

        GridBagConstraints cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.weightx = 1;
        cons.gridx = 0;
        cons.gridwidth = 1;
        cons.anchor = GridBagConstraints.FIRST_LINE_START;

        GridBagConstraints lastCons = new GridBagConstraints();
        lastCons.weightx = 1;
        lastCons.gridx = 0;
        lastCons.gridwidth = 1;
        lastCons.weighty = 1.0;
        lastCons.fill = GridBagConstraints.BOTH;

        left.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.setAlignmentY(Component.TOP_ALIGNMENT);

        Dimension min = new Dimension(new Dimension(300, 50));
        left.setMinimumSize(min);

        // cache
        Box cacheBox = new Box(BoxLayout.X_AXIS);
        cacheBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        cacheBox.setBorder(new TitledBorder("File cache"));
        updateCacheLabel();

        JLabel cacheDoc = new JLabel("<html>The MI Bundle stores localy files"
                + " such as structures (pdb) or interaction lists. "
                + " You can clear those files to free disk space. They will be"
                + " automatically downloaded next time the plugin needs them.</html>");

        cacheDoc.setOpaque(true);
        cacheDoc.setBackground(Color.WHITE);
        cacheDoc.setMinimumSize(new Dimension(200, 50));
        cacheDoc.setPreferredSize(new Dimension(400, 100));
        cacheDoc.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        cacheDoc.setBorder(new EmptyBorder(10, 10, 10, 10));

        cacheBox.add(cacheDoc, cons);

        cacheBox.setBorder(new TitledBorder("Cached files"));

        Box subBox5 = new Box(BoxLayout.X_AXIS);
        subBox5.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton clearCacheButton = new JButton();
        clearCacheButton.setAction(new ClearCacheAction());
        clearCacheButton.setText("clear");

        subBox5.add(cacheLabel);
        subBox5.add(clearCacheButton);
        cacheBox.add(cacheDoc);
        cacheBox.add(subBox5);

        left.add(cacheBox, cons);

        Box structureBox1 = new Box(BoxLayout.X_AXIS);
        structureBox1.setBorder(new TitledBorder("PDB mirror"));
        structureBox1.setAlignmentX(Component.LEFT_ALIGNMENT);
        structureBox1.setAlignmentY(Component.TOP_ALIGNMENT);

        pdbMirrorBox = new JComboBox<String>(PDBUtils.getProviderNames()) {

            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                return max;
            }

        };

        pdbMirrorBox.setMaximumSize(new Dimension(150, Integer.MAX_VALUE));
        for (int i = 0; i < pdbMirrorBox.getItemCount(); i++) {
            if (PDBUtils.getUrl(pdbMirrorBox.getItemAt(i)).equals(MIBundleConfiguration.getInstance().getPdbURL())) {
                pdbMirrorBox.setSelectedIndex(i);
            }
        }

        // Load it
        pdbMirrorBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MIBundleConfiguration.getInstance().setPdbUrl(PDBUtils.getUrl((String) pdbMirrorBox.getSelectedItem()));
            }
        });

        structureBox1.add(pdbMirrorBox);
        left.add(structureBox1, cons);

        JLabel dirDoc1 = new JLabel("<html>If you maintain a local mirror of PDB,"
                + " you can use it to avoid downloading the data."
                + " The PDB mirror should maintain the hierarchical structure of "
                + " PDB directory.</html>");
        dirDoc1.setOpaque(true);
        dirDoc1.setBackground(Color.WHITE);
        dirDoc1.setMinimumSize(new Dimension(200, 50));
        dirDoc1.setPreferredSize(new Dimension(400, 100));
        dirDoc1.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        dirDoc1.setBorder(new EmptyBorder(10, 10, 10, 10));

        Box structureBox3 = new Box(BoxLayout.X_AXIS);
        structureBox3.setBorder(new TitledBorder("PDB local mirror"));
        structureBox3.setAlignmentX(Component.LEFT_ALIGNMENT);

        structureBox3.add(dirDoc1, cons);
        structureBox3.add(Box.createHorizontalStrut(10));

        JButton pdbLocalPathButton = new JButton();
        pdbLocalPathButton.setAction(new LocalPDBMirrorAction());
        pdbLocalPathButton.setText("SET");
        pdbLocalPathLabel = new JLabel(MIBundleConfiguration.getInstance()
                .getPdbLocalMirror());
        structureBox3.add(pdbLocalPathButton);
        structureBox3.add(pdbLocalPathLabel);

        left.add(structureBox3, cons);

        Box structureBox4 = new Box(BoxLayout.X_AXIS);
        structureBox4.setBorder(new TitledBorder("Interactome3D local mirror"));

        JLabel dirDoc2 = new JLabel("<html>If you maintain a local mirror"
                + " or Interactome3D, you can use it to avoid downloading the data."
                + " The Interactome3D mirror should contain "
                + " interactions.dat, proteins.dat and all Interactome3D"
                + " structures in PDB format.</html>");

        dirDoc2.setOpaque(true);
        dirDoc2.setBackground(Color.WHITE);
        dirDoc2.setMinimumSize(new Dimension(200, 50));
        dirDoc2.setPreferredSize(new Dimension(400, 100));
        dirDoc2.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        dirDoc2.setBorder(new EmptyBorder(10, 10, 10, 10));

        structureBox4.setAlignmentX(Component.LEFT_ALIGNMENT);

        structureBox4.add(dirDoc2);
        structureBox4.add(Box.createHorizontalStrut(10));

        JButton i3dDirButton = new JButton();
        i3dDirButton.setAction(new I3DStructuresDirectoryAction());
        i3dDirButton.setText("SET");
        i3dLocalPathLabel = new JLabel(MIBundleConfiguration
                .getInstance().getI3DStructuresDirectory());
        structureBox4.add(i3dDirButton);
        structureBox4.add(i3dLocalPathLabel);
        left.add(structureBox4, cons);

        userStructuresLabel = new JLabel();

        Box structureBox6 = new Box(BoxLayout.X_AXIS);
        structureBox6.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton clearButton = new JButton();
        clearButton.setAction(new ClearConfigAction());
        clearButton.setText("RESET");
        structureBox6.add(clearButton);

        left.add(structureBox6, cons);

        left.add(new JLabel(), lastCons);

        left.add(new JLabel(), lastCons);

        this.add(new JScrollPane(left));

        MIView.getInstance().setMiConfigurationPanel(this);

    }

    public void updateCacheLabel() {
        long cacheMb = MIBundleConfiguration.getInstance().getCacheSpace() / 1024 / 1024;
        cacheLabel.setText("Cache size: " + cacheMb + "M");
    }

    @Override
    public void refresh() {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class ClearConfigAction extends GenericAction {

        private static final long serialVersionUID = 1L;

        public ClearConfigAction() {
            super("Clear", KeyEvent.VK_Z);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            if (JOptionPane.showConfirmDialog(new JFrame(),
                    "Do you want to clear your configuration ?",
                    "Clear MI configuration", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                MIBundleConfiguration.getInstance().setPdbLocalMirror(null);
                MIBundleConfiguration.getInstance().setUserStructuresDirectory(null);
                MIBundleConfiguration.getInstance().setI3DStructuresDirectory(null);
                pdbLocalPathLabel.setText("");
                userStructuresLabel.setText("");
                i3dLocalPathLabel.setText("");

                MIView.getInstance().getMiSearch().removeStructureSource(SearchPanel.choicePDBLocal);
            }
        }
    }

    private class ClearCacheAction extends GenericAction {

        private static final long serialVersionUID = 1L;

        public ClearCacheAction() {
            super("Remove", KeyEvent.VK_Z);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            if (JOptionPane.showConfirmDialog(new JFrame(),
                    "Do you want to clear all structures in chache ?",
                    "Clear MI cache", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                MIBundleConfiguration.getInstance().clearCache();
                updateCacheLabel();
            }
        }
    }

    private class LocalPDBMirrorAction extends GenericAction {

        private static final long serialVersionUID = 1L;

        public LocalPDBMirrorAction() {
            super("Local PDB", KeyEvent.VK_Z);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("PDB mirror");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            //
            // disable the "All files" option.
            //
            chooser.setAcceptAllFileFilterUsed(false);
            //
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                MIBundleConfiguration.getInstance().setPdbLocalMirror(path);
                pdbLocalPathLabel.setText(path);
                MIView.getInstance().getMiSearch().addStructureSource(SearchPanel.choicePDBLocal);
            }

        }
    }

    private class UserStructuresDirectoryAction extends GenericAction {

        private static final long serialVersionUID = 1L;

        public UserStructuresDirectoryAction() {
            super("User structures", KeyEvent.VK_Z);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("User structures directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            //
            // disable the "All files" option.
            //
            chooser.setAcceptAllFileFilterUsed(false);
            //
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                MIBundleConfiguration.getInstance().setUserStructuresDirectory(path);
                userStructuresLabel.setText(path);
            }

        }
    }

    private class I3DStructuresDirectoryAction extends GenericAction {

        private static final long serialVersionUID = 1L;

        public I3DStructuresDirectoryAction() {
            super("Interactome3D structures", KeyEvent.VK_Z);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("Interactome3D structures directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            //
            // disable the "All files" option.
            //
            chooser.setAcceptAllFileFilterUsed(false);
            //
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                MIBundleConfiguration.getInstance().setI3DStructuresDirectory(path);
                i3dLocalPathLabel.setText(path);
            }

        }
    }

}
