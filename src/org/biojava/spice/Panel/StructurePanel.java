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
 * @author Andreas Prlic
 *
 */

package org.biojava.spice ;

// for Jmol stuff
import org.jmol.api.JmolAdapter;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.viewer.JmolViewer;
import org.jmol.viewer.JmolStatusListener;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// biojava structure stuff
import org.biojava.bio.structure.Structure ;

// logging
import java.util.logging.*;


/// accessing Jmol directly
import org.jmol.viewer.datamodel.Atom ;

/** a Panel that provides a wrapper around the Jmol viewer. Code heavily
 * inspired by
 * http://cvs.sourceforge.net/viewcvs.py/jmol/Jmol/examples/Integration.java?view=markup
 * - the Jmol example of how to integrate Jmol into an application.
 * 
 */
class StructurePanel extends JPanel
    implements MouseListener, MouseMotionListener {
    
    final  Dimension currentSize = new Dimension();
    static Logger    logger      = Logger.getLogger("org.biojava.spice");

    JmolViewer  viewer;
    JmolAdapter adapter;
    TextField   strucommand  ; 
    
    SPICEFrame  spice ;

    StructurePanel(SPICEFrame parent) {
	spice = parent ;
	adapter = new SmarterJmolAdapter(null);
	viewer = new JmolViewer(this, adapter);
    }
    
    /** returns the JmolViewer */
    public JmolViewer getViewer() {
	return viewer;
    }

    /** paint Jmol */
    public void paint(Graphics g) {

	viewer.setScreenDimension(getSize(currentSize));
	Rectangle rectClip = new Rectangle();
	g.getClipBounds(rectClip);
	viewer.renderScreenImage(g, currentSize, rectClip);
    }

    /** send a RASMOL like command to Jmol
     * @param command - a String containing a RASMOL like command. e.g. "select protein; cartoon on;"
     */
    public void executeCmd(String command) {
	
	if (logger.isLoggable(Level.FINEST)) {
	    logger.finest("sending Jmol command: "+command);
	}

	
	viewer.evalString(command);
    }

    /** display a new PDB structure in Jmol 
     * @param structure a Biojava structure object    
     *
     */
    public  void setStructure(Structure structure) {
	
	String pdbstr = structure.toPDB(); 
	
	synchronized ( viewer) {
	    viewer.openStringInline(pdbstr);
	}
	
	String cmd ="select all; cpk off; wireframe off;"  ;
	executeCmd(cmd);
	    
	
	String strError = viewer.getOpenFileError();
	if (strError != null) {
	    if (logger.isLoggable(Level.WARNING)) {
		logger.severe("could not open PDB file in viewer "+ strError);
	    }
	}
    }

 

    public void mouseDragged(MouseEvent e) {
	//logger.finest("dragging mouse "+e);
    }	

    /** when the mouse is moved of the structure panel,
	the corresponing position in the sequence is highlited
    */
    
    public void mouseMoved(MouseEvent e) {
	//logger.finest("moving mouse over StructurePanel "+e);    	
	int pos = viewer.findNearestAtomIndex(e.getX(),e.getY());
	if ( pos == -1 ) { return ; }

	String chainId = viewer.getAtomChain( pos) ;
	String seqCode = viewer.getAtomSequenceCode( pos) ;

	String[] spl = seqCode.split("\\^");
	if ( spl.length > 1) {
	    //logger.finest("insertion code found! " + residuePDBcode );
	    seqCode = spl[0] + spl[1];
	}


	//logger.finest("chainid " + chainId + " seqcode: " + seqCode);
	int chainpos = 0 ;
	if  ( chainId != null ) 
	    chainpos   = spice.getChainPosByPDB(chainId);
	
	// what is the default return value for empty chain in Jmol ?
	if (chainpos == -1 )
	    chainpos = 0 ;
	int residuepos = spice.getSeqPosByPDB(seqCode);

	spice.select(chainpos,residuepos);
	spice.showSeqPos(chainpos,residuepos);

	//logger.finest("atomIndex "+atom.getAtomIndex());
	//logger.finest(viewer.getElementNumber(pos));
	//logger.finest(viewer.getElementSymbol(pos));
	//logger.finest("atomName " + viewer.getAtomName(pos));
	//logger.finest("seqCode " + atom.getSeqcodeString());

    }
    public void mouseClicked(MouseEvent e)
    {
	logger.finest("mouseClick in structure Panel"+e);

	viewer.popupMenu(e.getX(),e.getY());

	int pos = viewer.findNearestAtomIndex(e.getX(),e.getY());
	if ( pos == -1 ) { return ; }

	String chainId = viewer.getAtomChain( pos) ;
	String seqCode = viewer.getAtomSequenceCode( pos) ;

	String[] spl = seqCode.split("\\^");
	if ( spl.length > 1) {
	    //logger.finest("insertion code found! " + residuePDBcode );
	    seqCode = spl[0] + spl[1];
	}



	//logger.finest("chainid " + chainId + " seqcode: " + seqCode);
	int chainpos = 0 ;
	if  ( chainId != null ) 
	    chainpos   = spice.getChainPosByPDB(chainId);
	
	// what is the default return value for empty chain in Jmol ?
	if (chainpos == -1 )
	    chainpos = 0 ;
	int residuepos = spice.getSeqPosByPDB(seqCode);

	spice.highlite(chainpos,residuepos);
	spice.showSeqPos(chainpos,residuepos);	



    }
    public void mouseEntered(MouseEvent e)  {}
    public void mouseExited(MouseEvent e)   {}
    public void mousePressed(MouseEvent e)  {}
    public void mouseReleased(MouseEvent e) {}
    

}
