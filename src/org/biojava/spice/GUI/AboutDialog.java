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

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * @author Andreas Prlic
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AboutDialog
	extends JDialog {
    	
    
    static int H_SIZE = 200;
    static int V_SIZE = 400;
    //JTextField txt ;
    String displayText     ;
    JTextPane txt         ;
    public AboutDialog(JFrame parent)
    {
	// Calls the parent telling it this
	// dialog is modal(i.e true)
	super(parent, true);         
	//setBackground(Color.gray);
	//setLayout(new BorderLayout());
	this.setSize(new Dimension(H_SIZE, V_SIZE)) ;
	
	displayText="" ;
	txt = new JTextPane();

	txt.setEditable(false);
	// Two buttons "Close" and "Help"
	//txt = new JTextField();
	JScrollPane scroll = new JScrollPane(txt);
	//scroll.setPreferredSize(new Dimension(H_SIZE, V_SIZE-50)) ;
	scroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	JPanel p = new JPanel();
	p.add("Center",scroll);
	//p.add(txt);
	p.add("Sourth",new Button("Close"));
	//p.add(new Button("Help"));
	
	//add("South", p);
	this.getContentPane().add(p);
	//this.getContentPane().add(new Button("Close"));
	
	//resize(H_SIZE, V_SIZE);

    }
    
    public boolean action(Event evt, Object arg)
    {
	// If action label(i.e arg) equals 
	// "Close" then dispose this dialog
	if(arg.equals("Close"))
	    {
		dispose();
		return true;
	    }
	return super.handleEvent(evt);
    }

    public void setText(String t) {
	displayText = t ;
	txt.setText(t);
    }

}