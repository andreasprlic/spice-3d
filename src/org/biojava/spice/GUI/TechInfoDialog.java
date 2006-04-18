/*
 *                  BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 * 
 * Created on Apr 12, 2006
 *
 */
package org.biojava.spice.GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;


public class TechInfoDialog
extends JDialog {
    
    static int H_SIZE = 700;
    static int V_SIZE = 600;
     
    private static final long serialVersionUID = 8273923744123423441L;

    public TechInfoDialog() {
        super();
        
        this.setSize(new Dimension(H_SIZE, V_SIZE)) ;
        
        Box vBox = Box.createVerticalBox();
        
        
        
        String txt = "Java version : " + System.getProperty("java.version");
        txt += "<br>";
        txt += "Java vendor : " + System.getProperty("java.vendor");
        txt += "<br>";
        txt += "Java home : " + System.getProperty("java.home");
        txt += "<br>";
        txt += "Os.arch : " + System.getProperty("os.arch");
        txt += "<br>";
        txt += "Os.name : " + System.getProperty("os.name");
        txt += "<br>";
        txt += "Os.version : " + System.getProperty("os.version");
        txt += "<br>";
        
        JEditorPane editor = new JEditorPane("text/html", txt);
        JPanel panel = new JPanel();
        
        panel.add(editor);
        vBox.add(panel);
        
        
        final MemoryMonitor demo = new MemoryMonitor();
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {demo.surf.stop();}
            public void windowDeiconified(WindowEvent e) { demo.surf.start(); }
            public void windowIconified(WindowEvent e) { demo.surf.stop(); }
        };
        this.addWindowListener(l);
        
        vBox.add(demo);
        
        JButton close = new JButton("Close");
        
        close.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {               
                dispose();
            }
        });
        
        JButton gcbutton = new JButton("Run Garbage Collector");
        gcbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                Runtime.getRuntime().gc();
            }
        });
        
        
        Box hBoxb = Box.createHorizontalBox();
        
        hBoxb.add(gcbutton,BorderLayout.WEST);
        hBoxb.add(Box.createGlue());
        hBoxb.add(close,BorderLayout.EAST);
        
        vBox.add(hBoxb);
        
        this.getContentPane().add(vBox);
        
        demo.surf.start();
        
        this.setVisible(true);
        this.pack();
        this.show();
        
    }

   
    
}

