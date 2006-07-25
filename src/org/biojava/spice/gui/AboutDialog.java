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
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import java.awt.event.ActionListener;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

/** A Dialog to show version and copyright information for SPICE.
 * @author Andreas Prlic
 *
 */
public class AboutDialog
extends JDialog {
    
    private static final long serialVersionUID = 8273923744121231231L;
    
    public static final String VERSION = "0.8.3-devel";
      
    static final String AUTHORS = 
        " <a href=\"mailto:ap3@sanger.ac.uk\">Andreas Prlic</a>, Thomas Down, Tim Hubbard <br>"+
        "The Wellcome Trust Sanger Institute 2006<p>"; 
    
    static final String SPICEINFO = 
        " More Info about SPICE: <br>"+
        " <a href=\"http://www.efamily.org.uk/software/dasclients/spice/\">Homepage</a><br>"+
        " <a href=\"http://lists.sanger.ac.uk/mailman/listinfo/spice-das\">Mailing List</a><br>"+
        " <a href=\"http://www.sanger.ac.uk/Users/ap3/DAS/SPICE/SPICE_manual.pdf\">Manual</a><br>"+
        " <a href=\"http://www.derkholm.net/svn/repos/spice/trunk/\">Source code</a><br>" +
        " <a href=\"http://www.gnu.org/copyleft/lesser.html\">License</a> (LGPL)<p>";
    
    static final String THANKS = 
        " Thanks to the following Projects:<br>"+
        " <b>Jmol</b> - <a href=\"http://www.jmol.org\">http://www.jmol.org</a> - for the 3D visualization API. (LGPL)<br>"+
        " <b>MSD</b> - <a href=\"http://www.ebi.ac.uk/msd-srv/msdmotif/\">http://www.ebi.ac.uk/msd-srv/msdmotif/</a> for providing the UniProt - PDB alignment data and the keyword search web service. <br>"+
        " <b>BioJava</b> - <a href=\"http://www.biojava.org\">http://www.biojava.org</a> - for various libs. (LGPL)<br>"+
        " <b>Geotools</b> - <a href=\"http://modules.geotools.org/\">http://modules.geotools.org/</a> for the logging panel. (LGPL)<br>"+
        " <b>Nuvola</b> - <a href=\"http://www.icon-king.com\">http://www.icon-king.com</a> - for many of the icons used here. (LGPL) <br>"+       
        " <a href=\"http://das.sanger.ac.uk/registry/\">DAS registration server</a><p>";
   
    static final String LICENSE = 
        " <pre>This library is free software; you can redistribute it and/or <br>"+
        "modify it under the terms of the GNU Lesser General Public <br>"+
        "License as published by the Free Software Foundation; either <br>"+
        "version 2.1 of the License, or (at your option) any later version. <br>" +
        "<br>"+
        "This library is distributed in the hope that it will be useful,<br>"+
        "but WITHOUT ANY WARRANTY; without even the implied warranty of<br>"+
        "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU<br>"+
        "Lesser General Public License for more details.<br>"+
        ""+
        "You should have received a copy of the GNU Lesser General Public<br>"+
        "License along with this library; if not, write to the Free Software<br>" +
        "Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA<br>"+
        "</pre><br>" ;
    
    
    static String DESCRIPTION_TEXT = 
        "<html><body>"+
        "<b>The SPICE DAS client</b> V "+ VERSION +" <br>"+
        "(C)" + 
        AUTHORS + SPICEINFO + THANKS + LICENSE +   
        "</body></html>";
    
    
    static int H_SIZE = 700;
    static int V_SIZE = 700;
    //JTextField txt ;
    
    SPICEFrame spice; 
    JEditorPane txt;
    
    public AboutDialog(SPICEFrame spice_)
    {
        
        spice = spice_;
        
        this.setSize(new Dimension(H_SIZE, V_SIZE)) ;
        
        txt = new JEditorPane("text/html", DESCRIPTION_TEXT);
        
        txt.setEditable(false);
        
        txt.addHyperlinkListener(new HyperlinkListener(){
            
            public void hyperlinkUpdate(HyperlinkEvent e) {
                //System.out.println(e);
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    String href = e.getDescription();
                    spice.showDocument(href);
                }
                if ( e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                    // change the mouse curor
                    txt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
                if (e.getEventType() == HyperlinkEvent.EventType.EXITED) { 
                    txt.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        
        JScrollPane scroll = new JScrollPane(txt);
        
        Box vBox = Box.createVerticalBox();
        vBox.add(scroll);
        
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
