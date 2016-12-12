/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.iit.genomics.cru.igb.bundles.mi.view;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 *
 * @author arnaudceol
 */
public class TestHelp {
    
    public static void main(String ars[]) {
        JFrame helpFrame;
        
        helpFrame = new JFrame("MI Bundle Help");
        Dimension preferredSize = new Dimension(800, 500);
        
        helpFrame.setPreferredSize(preferredSize);
        helpFrame.setMinimumSize(preferredSize);
        
        InfoPanel.getInstance().setPreferredSize(preferredSize);
        InfoPanel.getInstance().setMinimumSize(preferredSize);
        
        helpFrame.add(InfoPanel.getInstance());
        
        JButton button = new JButton("test");
        button.setBackground(Color.red);
        
        helpFrame.add(button);
        
        helpFrame.setVisible(true);
     
        
        
    }
    
}
