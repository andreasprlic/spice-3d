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
package org.biojava.spice;

import javax.swing.JPanel        ;
import javax.swing.JLabel        ;
import javax.swing.BoxLayout     ;
import javax.swing.JProgressBar  ;
import javax.swing.BorderFactory ;

import java.awt.Dimension        ;
import java.awt.BorderLayout     ;
import java.awt.Color            ;

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


    JLabel pdbCode ;
    JLabel spCode  ;    
    JLabel status ;

    JProgressBar progressBar ;
    JPanel pdbPanel ;
    JPanel spPanel  ;

    public StatusPanel(){
	this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

	//status  = new JLabel();
	//status.setText("");
	//this.add(status);
	
	pdbPanel = new JPanel();
	JLabel pdbtxt  = new JLabel("PDB code:");
	pdbCode = new JLabel("    ");
	//pdbPanel.setBorder(BorderFactory.createLineBorder(Color.black));
	pdbPanel.add(pdbtxt);
	pdbPanel.add(pdbCode);
	//pdbPanel.setMaximumSize(new Dimension(80,20));
	this.add(pdbPanel,BorderLayout.WEST);

	spPanel = new JPanel();
	JLabel sptxt  = new JLabel("UniProt code:");
	spCode = new JLabel("      ");
	//spPanel.setBorder(BorderFactory.createLineBorder(Color.black));
	spPanel.add(sptxt);
	spPanel.add(spCode);
	//spPanel.setMaximumSize(new Dimension(80,20));
	this.add(spPanel,BorderLayout.WEST);

	progressBar = new JProgressBar(0,100);
	progressBar.setValue(0);
	progressBar.setStringPainted(false);
	progressBar.setString(""); 
	progressBar.setMaximumSize(new Dimension(80,20));
	progressBar.setIndeterminate(false);
	this.add(progressBar,BorderLayout.EAST);


	

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
