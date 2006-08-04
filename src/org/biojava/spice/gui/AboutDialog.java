/*
 *  *                    BioJava development code
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
 * Created on Dec 1, 2004
 * @author Andreas Prlic
 * 
 */
package org.biojava.spice.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import org.biojava.spice.SPICEFrame;
import org.biojava.spice.utils.BrowserOpener;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import java.awt.event.ActionListener;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.util.ResourceBundle;
/** A Dialog to show version and copyright information for SPICE.
 * @author Andreas Prlic
 *
 */
public class AboutDialog
extends JDialog {
    
    private static final long serialVersionUID = 8273923744121231231L;
    
    static String baseName="aboutspice";

    public static final String VERSION ;      
    static final String AUTHORS;         
    static final String SPICEINFO;    
    static final String THANKS;           
    static final String LICENSE; 
    static final String TITLE;
    static String DESCRIPTION_TEXT;
    
    static int H_SIZE = 700;
    static int V_SIZE = 700;
    //JTextField txt ;
    
    SPICEFrame spice; 
    JEditorPane txt;
    Box vBox;
    
    
    static {
        ResourceBundle bundle = ResourceBundle.getBundle(baseName);
        VERSION   = bundle.getString("org.biojava.spice.Version");
        AUTHORS   = bundle.getString("org.biojava.spice.Authors");
        SPICEINFO = bundle.getString("org.biojava.spice.SpiceInfo");
        LICENSE   = bundle.getString("org.biojava.spice.License");
        THANKS    = bundle.getString("org.biojava.spice.Thanks");
        TITLE     = bundle.getString("org.biojava.spice.Title");
        DESCRIPTION_TEXT = 
            "<html><body>"+
            TITLE + " V "+ VERSION +" <br>"+
            "(C)" + 
            AUTHORS + SPICEINFO + THANKS + LICENSE +   
            "</body></html>";
    }
    
    public AboutDialog(SPICEFrame spice_)
    {
        
        spice = spice_;
        
        this.setSize(new Dimension(H_SIZE, V_SIZE)) ;
        
        vBox = Box.createVerticalBox();
        
        
        txt = new JEditorPane("text/html", DESCRIPTION_TEXT);
        
        JScrollPane scroll = new JScrollPane(txt);
        vBox.add(scroll);
        
        txt.setEditable(false);
        
        txt.addHyperlinkListener(new HyperlinkListener(){
            
            public void hyperlinkUpdate(HyperlinkEvent e) {
                //System.out.println(e);
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    String href = e.getDescription();
                    BrowserOpener.showDocument(href);
                }
                if ( e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                    // change the mouse curor
                    vBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
                if (e.getEventType() == HyperlinkEvent.EventType.EXITED) { 
                    vBox.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        
       
        
       
        
        JButton close = new JButton("Close");
        
        close.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        
        Box hBoxb = Box.createHorizontalBox();
        hBoxb.add(Box.createGlue());
        hBoxb.add(close,BorderLayout.EAST);
        
        vBox.add(hBoxb);
        
        this.getContentPane().add(vBox);
        
    }
    
    public static String getVersion() {
        
        return VERSION;
    }
    
    public void setText(String t) {
        txt.setText(t);
    }
    
}
