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

import it.iit.genomics.cru.igb.bundles.mi.view.StructuresPanel.ResiduesType;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import com.lorainelab.igb.services.IgbService;

/**
 *
 * @author Arnaud Ceol
 *
 * Structure table
 *
 */
public class StructureTable extends JTable {

    private static final long serialVersionUID = 1L;

    public StructureTable(StructureTableModel model, IgbService igbService) {
        super(model);

        TableRowSorter<StructureTableModel> sorter = new TableRowSorter<>(
                model);
        setRowSorter(sorter);

        model.fireTableDataChanged();
        this.getTableHeader().setReorderingAllowed(false);

        this.setDefaultRenderer(StructureItem.class, new StructuresRenderer());

    }

    class StructuresRenderer extends JTextArea implements TableCellRenderer {

        private static final long serialVersionUID = 1L;

        public StructuresRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {

            setFont(new java.awt.Font("Arial Unicode MS", 0, 10));

            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }

            int modelRow = convertRowIndexToModel(row);
            StructureItem rowResult = ((StructureTableModel) getModel())
                    .getResult(modelRow);

            this.setText((String) value);

            if (rowResult.getResiduesType().equals(ResiduesType.INTERFACE)) {
                setForeground(Color.RED);
            } else if (rowResult.getResiduesType().equals(ResiduesType.OTHER)) {
                setForeground(Color.ORANGE);
            } else {
                setForeground(Color.BLACK);
            }

            if (isSelected) {
                setBackground(Color.GRAY);
            } else {
                setBackground(Color.WHITE);
            }
            return this;

        }

    }

}
