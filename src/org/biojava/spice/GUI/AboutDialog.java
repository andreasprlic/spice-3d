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

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import java.awt.event.ActionListener;

/**
 * @author Andreas Prlic
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AboutDialog
	extends JDialog {
    	static String VERSION = "0.6-pre2";
    static String DESCRIPTION_TEXT = "<html><body>"+
    "<b>The SPICE DAS client</b> V "+ VERSION +" <br>"+
    "(C) Andreas Prlic, Tim Hubbard <br>"+
    "The Wellcome Trust Sanger Institute 2005<br>"+
    "<a href=\"mailto:ap3@sanger.ac.uk\">ap3@sanger.ac.uk</a><p>"+
    
    " Thanks to the following Projects:<br>"+
    " <b>Jmol</b> - http://www.jmol.org - for the 3D rendering API  <br>"+
    " <b>BioJava</b> - http://www.biojava.org - for various libs     <br>"+
    " <b>Geotools</b> - http://modules.geotools.org/ for the logging panel <br>"+
    " <b>Nuvola</b> - http://www.icon-king.com - for a couple of icons  <br>"+
    "</body></html>";
    
    static int H_SIZE = 400;
    static int V_SIZE = 600;
    //JTextField txt ;
    String displayText     ;
    
    JEditorPane txt;
    
    public AboutDialog(JFrame parent)
    {
	// Calls the parent telling it this
	// dialog is modal(i.e true)
	super(parent, true);         
	//setBackground(Color.gray);
	//setLayout(new BorderLayout());
	this.setSize(new Dimension(H_SIZE, V_SIZE)) ;
	
	displayText="" ;
	txt = new JEditorPane("text/html", DESCRIPTION_TEXT);

	txt.setEditable(false);
	// Two buttons "Close" and "Help"
	//txt = new JTextField();
	JScrollPane scroll = new JScrollPane(txt);
	//scroll.setPreferredSize(new Dimension(H_SIZE, V_SIZE-50)) ;
	scroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	//JPanel p = new JPanel();
	//p.add("Center",scroll);
	//p.add(txt);
	//p.add("Sourth",new Button("Close"));
	//p.add(new Button("Help"));
	
	 Box vBox = Box.createVerticalBox();
	 vBox.add(scroll);
	 JButton close = new JButton("Close");
	 vBox.add(close);
	 
	 close.addActionListener(new ActionListener(){
	     public void actionPerformed(ActionEvent event) {
	         dispose();
	     }
	 });
	 
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
