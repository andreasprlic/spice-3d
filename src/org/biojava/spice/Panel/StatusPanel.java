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
 * Created on 21.03.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.spice.Panel;

import org.biojava.spice.SPICEFrame;
import javax.swing.JPanel        ;
import javax.swing.JTextField    ;
import javax.swing.BoxLayout     ;
import javax.swing.JProgressBar  ;
import javax.swing.BorderFactory ;
import javax.swing.Box           ;

import java.awt.Dimension        ;
import java.awt.BorderLayout     ;


import java.awt.event.*;
import java.net.URL;

/** a class to display status information 
 * contains 
 * a status display to display arbitraty text
 * the PDB code of the currently displayed PDB file
 * the UniProt code of the currently displayed UniProt sequence    
 * a progressBar to display ongoing progress
*/
public class StatusPanel
    extends JPanel
{
    public static String PDBLINK = "http://www.rcsb.org/pdb/cgi/explore.cgi?pdbId=";
    public static String UNIPROTLINK = "http://www.ebi.uniprot.org/uniprot-srv/uniProtView.do?proteinAc=" ;
    
    
        JTextField pdbCode ;
    JTextField spCode  ;    
    JTextField status ;

    JProgressBar progressBar ;
    SPICEFrame spice ;
    public StatusPanel(SPICEFrame parent){
        spice = parent;
	this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
	this.setBorder(BorderFactory.createEmptyBorder());
	Box hBox =  Box.createHorizontalBox();
	JTextField pdbtxt  = new JTextField("PDB code:");
	pdbtxt.setEditable(false);
	pdbtxt.setBorder(BorderFactory.createEmptyBorder());
	hBox.add(pdbtxt);

	pdbCode = new JTextField("    ");
	pdbCode.setEditable(false);

	pdbCode.setBorder(BorderFactory.createEmptyBorder());
	
	MouseListener mousiPdb = new PanelMouseListener(spice,this,PDBLINK);
	// mouse listener 
	pdbCode.addMouseListener(mousiPdb);
	
	
	
	hBox.add(pdbCode);
	hBox.add(pdbCode,BorderLayout.WEST);
	

	JTextField sptxt  = new JTextField("UniProt code:");
	sptxt.setEditable(false);
	sptxt.setBorder(BorderFactory.createEmptyBorder());
	hBox.add(sptxt);

	spCode = new JTextField("      ");
	spCode.setBorder(BorderFactory.createEmptyBorder());
	spCode.setEditable(false);
	MouseListener mousiSp = new PanelMouseListener(spice,this,UNIPROTLINK);
	// mouse listener 
	spCode.addMouseListener(mousiSp);
	
	
	hBox.add(spCode);


	progressBar = new JProgressBar(0,100);
	progressBar.setValue(0);
	progressBar.setStringPainted(false);
	progressBar.setString(""); 
	progressBar.setMaximumSize(new Dimension(80,20));
	progressBar.setIndeterminate(false);
	progressBar.setBorder(BorderFactory.createEmptyBorder());
	hBox.add(progressBar,BorderLayout.EAST);
	
	this.add(hBox);	

    }
    
 

    public void setStatus(String txt) { status.setText(txt); }
    
    public void setLoading(boolean flag){
	progressBar.setIndeterminate(flag);
	
    }
    public void setPDB(String pdb)    { 
	if (pdb == null) 
	    pdb = "-";
	
	pdbCode.setText(pdb);
	    
    }
    
    public void setSP(String sp)      { 
	if (sp == null)  
	    sp = "-" ;
	
	spCode.setText(sp)  ;
	 
    }

    
    
    
}


class PanelMouseListener
implements MouseListener
{
    SPICEFrame spice;
    StatusPanel parent;
    String caller;
    PanelMouseListener( SPICEFrame spice_, StatusPanel parent_,String caller_){
        spice = spice_;
        parent=parent_;
        caller=caller_;
        
    }
    
    public void mouseClicked(MouseEvent e){
        JTextField source = (JTextField)  e.getSource();
        
        try {
            URL url = new URL(caller+source.getText());
            spice.showDocument(url);
        } catch ( Exception ex){
            
        }
        
        
    }
    public void mouseExited(MouseEvent e){
        // remove tooltip
        JTextField source = (JTextField)e.getSource();
        source.setToolTipText(null);
    }
    public void mouseEntered(MouseEvent e){
        // display tooltip
        JTextField source = (JTextField)e.getSource();
        source.setToolTipText("click to open in browser");
    
    }
    public void mouseReleased(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
}
