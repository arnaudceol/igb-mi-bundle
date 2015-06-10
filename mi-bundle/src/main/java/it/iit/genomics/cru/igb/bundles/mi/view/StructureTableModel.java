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

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Arnaud Ceol
 *
 * Structure table model
 *
 */
public class StructureTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    protected List<StructureItem> tableRows = new ArrayList<>(0);

    public StructureItem getResult(int row) {
        return tableRows.get(row);
    }

    private final String[] column_names = {"structureId"};

    public static final int ID_COLUMN = 0;

    public void clear() {
        tableRows.clear();
    }

    public void addRow(StructureItem rowData) {
        tableRows.add(rowData);
    }

    public StructureTableModel(List<StructureItem> results) {
        super();
        if (results != null) {
            tableRows.addAll(results);
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        StructureItem StructureItem = getResult(row);
        return StructureItem.getName();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
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
        if (tableRows == null) {
            return 0;
        }
        return tableRows.size();
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return StructureItem.class;
    }

}
