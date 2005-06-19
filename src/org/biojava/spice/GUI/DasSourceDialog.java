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
 * Created on Jun 19, 2005
 *
 */
package org.biojava.spice.GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.biojava.spice.SPICEFrame;
import org.biojava.spice.Config.SpiceDasSource;

/**
 * @author Andreas Prlic
 *
 */
public class DasSourceDialog
extends JDialog{
    
    
    SpiceDasSource dasSource;
    SPICEFrame spice;
    static int H_SIZE = 750;
    static int V_SIZE = 600;
    JEditorPane txt;
    /**
     * 
     */
    public DasSourceDialog(SPICEFrame spice_, SpiceDasSource dasSource) {
        super();
        this.dasSource = dasSource;
        this.spice=spice_;
        this.setSize(new Dimension(H_SIZE, V_SIZE)) ;
        
        
        String htmlText = getHTMLText(dasSource);
        txt = new JEditorPane("text/html", htmlText);
        
        txt.setEditable(false);
        
        txt.addHyperlinkListener(new HyperlinkListener(){
            public void hyperlinkUpdate(HyperlinkEvent e) {
                //System.out.println(e);
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    String href = e.getDescription();
                    spice.showDocument(href);
                }
            }
        });
        JScrollPane scroll = new JScrollPane(txt);
        //scroll.setPreferredSize(new Dimension(H_SIZE, V_SIZE-50)) ;
        //scroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //JPanel p = new JPanel();
        //p.add("Center",scroll);
        //p.add(txt);
        //p.add("Sourth",new Button("Close"));
        //p.add(new Button("Help"));
        
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
        
        //add("South", p);
        this.getContentPane().add(vBox);
        
    }
    
    private String getHTMLText(SpiceDasSource ds){
        
        String txt = "";
        
        txt += "<html><body>";
        
        txt += "<table>";
        txt += "<tr><td>DasSource</td><td><b>"+ds.getNickname()+"</b></td></tr>";
        txt += "<tr><td>"+ds.getUrl()+"</td></tr>";
        txt += "</table>";
        txt += "</body></html>";
        return txt;
        
        
        
    }
}
