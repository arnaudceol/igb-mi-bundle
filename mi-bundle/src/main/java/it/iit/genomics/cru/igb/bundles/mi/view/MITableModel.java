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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import it.iit.genomics.cru.igb.bundles.mi.business.MIResult;
import it.iit.genomics.cru.igb.bundles.mi.business.MIResult.StructureSummary;
import it.iit.genomics.cru.structures.bridges.psicquic.Interaction;
import it.iit.genomics.cru.structures.model.MoleculeEntry;

/**
 *
 * @author Arnaud Ceol
 *
 * Table Model
 *
 */
public class MITableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    protected final List<MIResult> tableRows = new ArrayList<>(0);

    public MIResult getResult(int row) {
        return tableRows.get(row);
    }

    private final String[] column_names = {
        MIPanel.BUNDLE.getString("miTableTrack"),
        MIPanel.BUNDLE.getString("miTableSyms1"),
        MIPanel.BUNDLE.getString("miTableXrefs1"),
        MIPanel.BUNDLE.getString("miTableSyms2"),
        MIPanel.BUNDLE.getString("miTableXrefs2"),
        MIPanel.BUNDLE.getString("miTableInteractionTypes"),
        MIPanel.BUNDLE.getString("miTableDiseases"),
        MIPanel.BUNDLE.getString("miTableMiStructures")
    };

    public static final int TRACK_COLUMN = 0;
    public static final int SYMS1_COLUMN = 1;
    public static final int INTERACTOR1_COLUMN = 2;
    public static final int SYMS2_COLUMN = 3;
    public static final int INTERACTOR2_COLUMN = 4;
    public static final int INTERACTION_TYPE_COLUMN = 5;
    public static final int DISEASES_COLUMN = 6;
    public static final int STRUCTURES_COLUMN = 7;
    public static final int SCORE_COLUMN = 8;

    public MITableModel(List<MIResult> results) {
        super();

        if (results != null) {
            tableRows.addAll(results);
        }
    }

    @Override
    public Object getValueAt(int row, int col) {

        MIResult miResult = getResult(row);
        Object value;
        switch (col) {
            case TRACK_COLUMN:
                final JButton button = new JButton("create");
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(button),
                                "Button clicked for row " + TRACK_COLUMN);
                    }
                });
                button.setText("create");
                if (false == miResult.hasTrack()) {
                    return button;
                }
                value = new JLabel(miResult.getTrackId());
                break;
            case SYMS1_COLUMN:
                value = miResult.getInteractor1();
                break;
            case INTERACTOR1_COLUMN:
                value = miResult.getInteractor1();
                break;
            case SYMS2_COLUMN:
                value = miResult.getInteractor2();
                break;
            case INTERACTOR2_COLUMN:
                value = miResult.getInteractor2();
                break;
            case INTERACTION_TYPE_COLUMN:

                value = (Interaction) miResult.getInteraction();
                break;
            case DISEASES_COLUMN:
                value = miResult.getDiseasesHtml();
                break;
            case STRUCTURES_COLUMN:
                value = miResult.getStructureSummary();
                break;
            default:
                value = "";
        }
        return value;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == TRACK_COLUMN && (false == getResult(row).hasTrack());
    }

    @Override
    public int getColumnCount() {
        return column_names.length;
    }

    @Override
    public String getColumnName(int col) {
        return column_names[col];
    }

    @Override
    public int getRowCount() {
        return tableRows.size();
    }

    @Override
    public Class<?> getColumnClass(int column) {
        Class clazz;

        switch (column) {
            case (INTERACTION_TYPE_COLUMN):
                clazz = Interaction.class;
                break;
            case (TRACK_COLUMN):
                clazz = JButton.class;
                break;
            case (INTERACTOR1_COLUMN):
            case (INTERACTOR2_COLUMN):
            case (SYMS1_COLUMN):
            case (SYMS2_COLUMN):
                clazz = MoleculeEntry.class;
                break;
            case (STRUCTURES_COLUMN):
                clazz = StructureSummary.class;
                break;
            case (DISEASES_COLUMN):
                clazz = String.class;
                break;
            default:
                clazz = null;
        }

        return clazz;
    }

}
