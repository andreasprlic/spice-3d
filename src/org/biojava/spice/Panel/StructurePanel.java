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

class StructurePanel extends JPanel {

    JmolViewer viewer;
    ModelAdapter adapter;
    TextField strucommand  ; 

    
    StructurePanel() {
	adapter = new SmarterModelAdapter(null);
	viewer = new JmolViewer(this, adapter);
    }
    
    public JmolViewer getViewer() {
	return viewer;
    }


    public void forceRepaint() {
	//System.out.println("forcing repaint");
	//this.paint(this.getGraphics());
	//System.out.println("done forcing repaint");
    }

    public void paint(Graphics g) {
	//System.out.println("painting structure");
	viewer.setScreenDimension(getSize(currentSize));
	Rectangle rectClip = new Rectangle();
	g.getClipBounds(rectClip);
	viewer.renderScreenImage(g, currentSize, rectClip);
    }

    
    public void executeCmd(String command) {
	//command += ";refresh;";
	//System.out.println("sending Jmol command: " +command);
	
	viewer.evalString(command);
	

	//System.out.println("update paint...");
	//this.paint(this.getGraphics());
    }

    public  void setStructure(Structure structure) {
	
	String pdbstr = structure.toPDB();
	
	synchronized ( viewer) {
	    viewer.openStringInline(pdbstr);
	}
	
	//String cmd ="select *;color chain; refresh;";
	//String cmd ="select not protein and not solvent;spacefill 1.0;select not selected;cpk off;"  ;
	String cmd ="select all; cpk off; wireframe off;"  ;
	//System.out.println(cmd);
	executeCmd(cmd);
	    
	
	String strError = viewer.getOpenFileError();
	if (strError != null)
	    System.out.println(strError);
	//this.paint(this.getGraphics());
	
    }

    
    final Dimension currentSize = new Dimension();


}
