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
package it.iit.genomics.cru.igb.bundles.mi.commons;

import it.iit.genomics.cru.igb.bundles.mi.view.ConfigurationPanel;
import it.iit.genomics.cru.igb.bundles.mi.view.MIPanel;
import it.iit.genomics.cru.igb.bundles.mi.view.SearchPanel;

import javax.swing.JTabbedPane;

/**
 *
 * @author Arnaud Ceol
 *
 * Provides access to the main components of the bundle
 *
 */
public class MIView {

    private static MIView instance;

    private MIView() {

    }
    

    public static MIView getInstance() {
        if (instance == null) {
            instance = new MIView();
        }

        return instance;
    }

    private MIPanel miPanel;

    public MIPanel getMiPanel() {
        return miPanel;
    }

    public void setMiPanel(MIPanel miPanel) {
        this.miPanel = miPanel;
    }

    private SearchPanel miSearch;

    public SearchPanel getMiSearch() {
        return miSearch;
    }

    public void setMiSearch(SearchPanel miSearch) {
        this.miSearch = miSearch;
    }

    private ConfigurationPanel miConfig;

    public ConfigurationPanel getMiConfigurationPanel() {
        return miConfig;
    }

    public void setMiConfigurationPanel(ConfigurationPanel miConfig) {
        this.miConfig = miConfig;
    }

    private JTabbedPane resultsTabbedPan;

    public JTabbedPane getResultsTabbedPan() {
        return resultsTabbedPan;
    }

    public void setResultsTabbedPan(JTabbedPane resultsTabbedPan) {
        this.resultsTabbedPan = resultsTabbedPan;
    }

}
