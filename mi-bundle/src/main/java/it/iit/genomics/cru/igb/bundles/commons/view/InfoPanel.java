/**
 * *****************************************************************************
 * Copyright 2014 Fondazione Istituto Italiano di Tecnologia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *****************************************************************************
 */
package it.iit.genomics.cru.igb.bundles.commons.view;

import it.iit.genomics.cru.igb.bundles.commons.business.IGBLogger;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 *
 * @author Arnaud Ceol
 *
 * To display basic information about the plugin.
 *
 */
public class InfoPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Dimension d = new Dimension(800, 600);

    // Only one log panel
    private static InfoPanel instance;

    private InfoPanel() {
        super();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JEditorPane htmlPane = new JEditorPane();
        htmlPane.setContentType("text/html");

        htmlPane.setEditable(false);
        htmlPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        htmlPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent event) {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(event.getURL().toURI());
                        } catch (IOException | URISyntaxException e) {
                            IGBLogger.getMainInstance().getLogger().log(Level.SEVERE, "Cannot manage hyperlinks ", e);
                        }
                    }
                }
            }
        });

        try {
            htmlPane.setPage(getClass().getResource("/info.html"));
        } catch (Exception e) {
            e.printStackTrace(System.err);
            // handle load failure
        }

        JScrollPane scroll = new JScrollPane(htmlPane);

        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        add(scroll);
    }

    public static InfoPanel getInstance() {
        if (instance == null) {
            instance = new InfoPanel();
        }

        return instance;
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setSize(d);
        f.setLayout(new FlowLayout());
        InfoPanel i = new InfoPanel();

        i.setSize(f.getSize());
        i.setPreferredSize(f.getSize());
        f.add(i);
        f.setVisible(true);
    }

}
