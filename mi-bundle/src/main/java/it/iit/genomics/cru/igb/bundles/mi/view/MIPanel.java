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

import java.awt.BorderLayout;
import java.util.ResourceBundle;

import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.lorainelab.igb.services.window.tabs.IgbTabPanel;
import org.lorainelab.igb.services.window.tabs.IgbTabPanelI;

import aQute.bnd.annotation.component.Component;
import it.iit.genomics.cru.igb.bundles.mi.ServiceManager;
import it.iit.genomics.cru.igb.bundles.mi.business.IGBLogger;
import it.iit.genomics.cru.igb.bundles.mi.business.PsicquicInitWorker;
import it.iit.genomics.cru.igb.bundles.mi.commons.MIView;



/**
 * @author Arnaud Ceol
 *
 * Main Panel
 *
 */
@Component(name = MIPanel.COMPONENT_NAME, provide = IgbTabPanelI.class, immediate = true)
public class MIPanel extends IgbTabPanel {

    public static final String COMPONENT_NAME = "MI";
    
    private static final long serialVersionUID = 1L;

    private JTextArea textArea;

    private final JTabbedPane resultsTabbedPan;

    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("mi");

    private final IGBLogger igbLogger;	

    
    public MIPanel() {
//    	IgbService igbService
//IgbService igbService, String displayName, String title, boolean main
        super( "MI", "Molecular Interactions", "MI", false);
        getContentPane().setLayout(new BorderLayout(10, 1));

        resultsTabbedPan = new JTabbedPane();

        igbLogger = IGBLogger.getMainInstance();

        // Search
//        SearchPanel panel = new SearchPanel();
        	
        resultsTabbedPan.addTab("Search", new SearchPanel(ServiceManager.getInstance().getService()));

        this.add(resultsTabbedPan, BorderLayout.CENTER);

        this.pack();
	
        MIView.getInstance().setResultsTabbedPan(resultsTabbedPan);

        MIView.getInstance().setMiPanel(this);
        
        // Load psicquic
        PsicquicInitWorker worker = new PsicquicInitWorker();

        worker.execute();

        

    }

//    public JTextArea getTextArea() {
//        return textArea;
//    }

}
