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
package org.biojava.spice.GUI;

import java.awt.BorderLayout;
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

/**
 * @author Andreas Prlic
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AboutDialog
	extends JDialog {
    	static String VERSION = "0.6-pre3";
    static String DESCRIPTION_TEXT = "<html><body>"+
    "<b>The SPICE DAS client</b> V "+ VERSION +" <br>"+
    "(C) <a href=\"mailto:ap3@sanger.ac.uk\">Andreas Prlic</a>, Tim Hubbard <br>"+
    "The Wellcome Trust Sanger Institute 2005<p>"+
    
    " More Info about SPICE: <br>"+
    " <a href=\"http://www.efamily.org.uk/software/dasclients/spice/\">Homepage</a><br>"+
    " <a href=\"http://www.sanger.ac.uk/Users/ap3/DAS/SPICE/SPICE_manual.pdf\">Manual</a><br>"+
    " <a href=\"http://www.derkholm.net/svn/repos/spice/trunk/\">Source code</a><br>" +
    " <a href=\"http://www.gnu.org/copyleft/lesser.html\">License</a> (LGPL)<p>"+
    
    " Thanks to the following Projects:<br>"+
    " <b>Jmol</b> - <a href=\"http://www.jmol.org\">http://www.jmol.org</a> - for the great 3D visualization API. (LGPL)<br>"+
    " <b>BioJava</b> - <a href=\"http://www.biojava.org\">http://www.biojava.org</a> - for various libs. (LGPL)<br>"+
    " <b>Geotools</b> - <a href=\"http://modules.geotools.org/\">http://modules.geotools.org/</a> for the logging panel. (LGPL)<br>"+
    " <b>Nuvola</b> - <a href=\"http://www.icon-king.com\">http://www.icon-king.com</a> - for many of the icons used here. (LGPL) <br>"+
    " <b>Axis</b> - <a href=\"http://ws.apache.org/axis/\">http://ws.apache.org/axis/</a> - for the WebService (SOAP) library used for contacting the "+
    " <a href=\"http://das.sanger.ac.uk/registry/\">DAS registration server</a> <p>"+
    
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
    "Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA</pre><br>"+
    "</body></html>";
    
    
    static int H_SIZE = 750;
    static int V_SIZE = 600;
    //JTextField txt ;
    String displayText     ;
    SPICEFrame spice; 
    JEditorPane txt;
    
    public AboutDialog(SPICEFrame spice_)
    {
	// Calls the parent telling it this
	// dialog is modal(i.e true)
        //super(spice_,true);
        	spice = spice_;
      
        	
	spice = spice_;
	//setBackground(Color.gray);
	//setLayout(new BorderLayout());
	this.setSize(new Dimension(H_SIZE, V_SIZE)) ;
	
	displayText="" ;
	txt = new JEditorPane("text/html", DESCRIPTION_TEXT);

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
	
	// Two buttons "Close" and "Help"
	//txt = new JTextField();
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
	
	//this.getContentPane().add(new Button("Close"));
	
	//resize(H_SIZE, V_SIZE);

    }
    
    

    public void setText(String t) {
	displayText = t ;
	txt.setText(t);
    }

}
