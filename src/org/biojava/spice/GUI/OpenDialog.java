/*
 *                    BioJava development code
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
 *
 */
package org.biojava.spice.GUI;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextField;

import org.biojava.spice.SPICEFrame;

/**
 * Enter a new PDB, UniProt, or ENSP code to be loaded in SPICE.
 * 
 * @author Andreas Prlic
 *
 */
public class OpenDialog 
	extends JDialog 
	 {

	     //static final String[] supportedCoords = { "PDB","UniProt","ENSP"};
	     static final String[] supportedCoords = { "PDB","UniProt"};
	     static int H_SIZE = 200;
	     static int V_SIZE = 90 ;
	     SPICEFrame spice       ;
	     JTextField getCom      ;
	     JComboBox  list        ;
	     String     currentType ;
	     
    
 
    /** a dialog responsible for opening new entries. */
    public OpenDialog(SPICEFrame parent){
    	// Calls the parent telling it this
		// dialog is modal(i.e true)	
		super((JFrame)parent, true); 

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
			    JFrame frame = (JFrame) evt.getSource();
			    frame.setVisible(false);
			    frame.dispose();
			}
		    });

		spice = parent ;
		
		this.setTitle("enter code") ;
		
		JPanel p = new JPanel();
		
		Box vBox  = Box.createVerticalBox();
		Box hBox1 = Box.createHorizontalBox();
		Box hBox2 = Box.createHorizontalBox();
		
		int startpos   = 0;
		currentType    = supportedCoords[startpos];
		
		list = new JComboBox(supportedCoords) ;		
		list.setEditable(false);
		list.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
		list.setSelectedIndex(startpos);
		
		hBox1.add(list);
		
		
		getCom = new JTextField(10);
		//TextFieldListener txtlisten = new TextFieldListener(parent,getCom);
		//getCom.addActionListener(txtlisten);

		getCom.addActionListener(new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				
			    String code = getCom.getText();
			    String type = (String)list.getSelectedItem() ;
			    System.out.println("open " + code + " " + type);
			    if ( type.equals("PDB")){
			    	if ( code.length() == 4 ) {
			    		spice.load(type,code);
			    		dispose();	
			    	}
			    } 
			    else if ( type.equals("ENSP") ||
			    		type.equals("UniProt")){
			    	spice.load(type,code);
		    		dispose();			    
			    }
			}
		    });

		hBox1.add(getCom);
		vBox.add(hBox1);
		
		JButton openB =new JButton("Open");
		openB.addActionListener(new ButtonListener(spice,this));
		
		JButton cancelB = new JButton("Cancel");
		cancelB.addActionListener(new ButtonListener(spice,this));
		
		hBox2.add(openB);
		hBox2.add(cancelB);
		vBox.add(hBox2);
		
		p.add(vBox);
		this.getContentPane().add(p);
		
		this.setSize(H_SIZE, V_SIZE);
	    }
	
}

class ButtonListener
	implements ActionListener{
	SPICEFrame spice;
	OpenDialog parent ;
	
	public ButtonListener( SPICEFrame spice_, OpenDialog parent_) {
		spice = spice_ ;
		parent = parent_ ;
	    }
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
	
		String cmd = e.getActionCommand();
		if(cmd.equals("Cancel"))
	    {
		parent.dispose();
		return ;
	    }
	else if ( cmd.equals("Open"))
	    {
		if ( spice.isLoading() ) {
		    parent.getCom.setText("please wait, already loading");
		    return  ;
		}
		//System.out.println("open");
		String type = (String)parent.list.getSelectedItem() ;
		String code = parent.getCom.getText();
		//System.out.println("open" + type + " " + code);
		if ( code.length() == 4 ) {
		    spice.load("PDB",code);
		    parent.dispose();
		}
		else if ( type.equals("ENSP") ||
	    		type.equals("UniProt")){
	    	spice.load(type,code);
    		parent.dispose();			    
	    }
		return ;	
	    }

	}



}
