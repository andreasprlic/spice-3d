package org.biojava.spice ;

// for Jmol stuff
import org.jmol.api.ModelAdapter;
import org.jmol.adapter.smarter.SmarterModelAdapter;
import org.openscience.jmol.viewer.JmolViewer;
import org.openscience.jmol.viewer.JmolStatusListener;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// structure stuff
import org.biojava.bio.structure.Structure ;
//import org.biojava.bio.structure.io.PDBFileReader ;



/** a Panel that provides a wrapper around the Jmol viewer */
class StructurePanel extends JPanel {

    JmolViewer viewer;
    ModelAdapter adapter;
    TextField strucommand  ; 

    
    StructurePanel() {
	adapter = new SmarterModelAdapter(null);
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
	//command += ";refresh;";
	//System.out.println("sending Jmol command: " +command);
	
	viewer.evalString(command);
	

	//System.out.println("update paint...");
	//this.paint(this.getGraphics());
    }

    /** display a new PDB structure in Jmol 
     * @param structre a Biojava structure object     
     */
    public  void setStructure(Structure structure) {
	
	String pdbstr = structure.toPDB(); 
	
	synchronized ( viewer) {
	    viewer.openStringInline(pdbstr);
	}
	
	String cmd ="select all; cpk off; wireframe off;"  ;
	executeCmd(cmd);
	    
	
	String strError = viewer.getOpenFileError();
	if (strError != null)
	    System.out.println(strError);
    }

    
    final Dimension currentSize = new Dimension();

}
