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

import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Arnaud Ceol
 *
 * Singleton, contains all the progress bars. Necessary in addition to the
 * ProgressBar panel to display a scroll bar.
 *
 */
public class ProgressPanel extends JScrollPane {

    private static final long serialVersionUID = 1L;

    // Only one panel
    private static ProgressPanel instance;
    
	private static final Logger logger = LoggerFactory.getLogger(ProgressPanel.class);
	
    private ProgressPanel() {
        super(ProgressBarPanel.getInstance());
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    public static ProgressPanel getInstance() {
        if (instance == null) {
            instance = new ProgressPanel();
        }

        return instance;
    }

    public void addBar(JProgressBar progressBar) {
        logger.info("add progress bar");
        ProgressBarPanel.getInstance().add(progressBar);
        JScrollBar sb = this.getVerticalScrollBar();
        sb.setValue(sb.getMaximum());
        revalidate();
    }

}
