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
 * ****************************************************************************
 */
package it.iit.genomics.cru.igb.bundles.commons.business;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Arnaud Ceol
 *
 * Manage log messages both in the standard output and in the Log panel.
 *
 */
public class IGBLogger {

    public final static String GLOBAL_LOGGER_NAME = "IGB-CRU";

    private boolean hasError = false;

    private static final Logger logger = Logger.getLogger(IGBLogger.class.getName());

    public final static int maxLines = 100;

    /**
     * associate a unique name to this logger, e.g. the query ID
     */
    private final String name;

    JTextPane area;

    private IGBLogger(String name) {
        this.name = name;

        area = new JTextPane();
        area.setEditable(false);
    }

    private final static HashMap<String, IGBLogger> instances = new HashMap<>();

    public static IGBLogger getInstance(String name) {
        if (false == instances.containsKey(name)) {
            IGBLogger instance = new IGBLogger(name);
            instances.put(name, instance);
            return instance;
        }
        return instances.get(name);
    }

    public static IGBLogger getMainInstance() {
        return getInstance(GLOBAL_LOGGER_NAME);
    }

    public Logger getLogger() {
        return logger;
    }

//    public void setTextPane(JTextPane area) {
//        this.area = area;
//    }
    public void warning(String message) {
        if (logger != null) {
            logger.warning(message);
        }

        if (area != null) {
            String text = "Warning: " + message;
            appendArea(text + "\n", Color.ORANGE);
        }
    }

    public void debug(String message) {
        if (logger != null) {
            logger.fine(message);
        }

    }

    public void info(String message) {
        if (logger != null) {
            logger.info(message);
        }

        if (area != null) {
            String text = "Info: " + message;
            appendArea(text + "\n", Color.BLUE);
        }
    }

    public void severe(String message) {
        if (logger != null) {
            logger.severe(message);
        }

        if (area != null) {
            String text = "Error: " + message;
            appendArea(text + "\n", Color.RED);
        }
    }

    public void error(String message, Throwable t) {
        this.severe(message, t);
    }   

    public void severe(String message, Throwable t) {

        hasError = true;

        if (logger != null) {
            logger.log(Level.SEVERE, message, t);
        }

        if (area != null) {
            String text = "Error: " + message;
            appendArea(text + "\n", Color.RED);
        }
    }

    public boolean hasError() {
        return hasError;
    }

    protected void appendArea(String text, Color color) {
        area.setForeground(color);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Calendar cal = Calendar.getInstance();
        String time = dateFormat.format(cal.getTime());

        text = time + " " + text;

        SimpleAttributeSet set = new SimpleAttributeSet();
        // StyleConstants.setBackground(set, new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));

        StyledDocument doc = area.getStyledDocument();
        try {
            StyleConstants.setForeground(set, color);
            doc.insertString(doc.getLength(), text, doc.getStyle("bold"));
            doc.setCharacterAttributes(doc.getLength() - text.length(), text.length(), set, true);
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }
        area.setCaretPosition(area.getDocument().getLength());
        area.setForeground(Color.BLACK);

        /* If not the main one, add it aso to the main logger */
        if (false == name.equals(GLOBAL_LOGGER_NAME)) {
            getInstance(GLOBAL_LOGGER_NAME).appendArea(text, color);
        }
    }

    public JTextPane getArea() {
        return area;
    }

}
