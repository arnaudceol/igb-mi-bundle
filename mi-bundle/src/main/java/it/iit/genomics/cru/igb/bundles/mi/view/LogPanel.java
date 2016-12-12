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
package it.iit.genomics.cru.igb.bundles.mi.view;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import it.iit.genomics.cru.igb.bundles.mi.business.IGBLogger;

/**
 *
 * @author Arnaud Ceol
 *
 * Display all log information. Two button allow to clear/copy the output.
 *
 */
public class LogPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JTextPane logArea;

    // Only one log panel
   // private static LogPanel instance;

    public LogPanel(IGBLogger logger) {
        super();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        Box buttonsBox = new Box(BoxLayout.X_AXIS);

        JButton copyButton = new JButton(new CopyAction());
        JButton clearButton = new JButton(new ClearAction());

        Dimension buttonDimension = new Dimension(80, 20);
        copyButton.setPreferredSize(buttonDimension);
        clearButton.setPreferredSize(buttonDimension);
        copyButton.setMinimumSize(buttonDimension);
        clearButton.setMinimumSize(buttonDimension);
        copyButton.setMaximumSize(buttonDimension);
        clearButton.setMaximumSize(buttonDimension);

        buttonsBox.add(copyButton);
        buttonsBox.add(clearButton);

        logArea = logger.getArea();
        
//        logArea = new JTextPane();
//        logArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(logArea);

        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        add(buttonsBox);
        add(scroll);

    }

//    public static LogPanel getInstance(String loggerName) {
//        if (instance == null) {
//            instance = new LogPanel(loggerName);
//        }
//
//        return instance;
//    }

//    public JTextPane getLogArea() {
//        return logArea;
//    }

    private class CopyAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public CopyAction() {
            super("copy");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            String selection = logArea.getText();
            StringSelection data = new StringSelection(selection);
            Clipboard clipboard = Toolkit.getDefaultToolkit()
                    .getSystemClipboard();
            clipboard.setContents(data, data);
        }
    }

    private class ClearAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public ClearAction() {
            super("clear");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            logArea.setText("");
        }
    }

}
