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
 * Created on 20.03.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.spice ;

import java.awt.Color ;

import java.util.Map ;
import java.util.List ;

import org.biojava.bio.structure.Structure ;
import org.biojava.bio.structure.StructureImpl ;


/** Loads a structure object in an independent Thread.  once loading
 * is finished sets the new structure object in the master SPICEFrame
 * @author Andreas Prlic
 */

public class LoadStructureThread 
    extends Thread {

    SPICEFrame spiceframe ;
    String pdb_file ;
    
    boolean finished ;
    
    Structure structure ;

    public LoadStructureThread(SPICEFrame master,String pdbfile) {
	spiceframe = master ;
	pdb_file = pdbfile ;
	finished = false ;
	structure = null ;
    }



    public void run () {
	loadCompound() ;
    }

    public boolean isDone() {
	return finished ;
    }
    
    public Structure getStructure() {
	return structure ;
    }
   


    public synchronized void loadCompound() {
	
	try {
	
	    //String dassequencecommand  = spiceframe.getSequenceServer()  + "sequence?segment=";
	    //String dasalignmentcommand = dasalignmentcommand = getAlignmentServer() + "alignment?query=" ;
	   
	    spiceframe.showStatus("Loading...Wait...",Color.red);
	    //DAS_PDBFeeder pdb_f =  new DAS_PDBFeeder(structureURL,dassequencecommand,dasalignmentcommand) ;
	    DAS_PDBFeeder pdb_f =  new DAS_PDBFeeder(spiceframe.getConfiguration()) ;
	    //System.out.println("pdb_f.loadPDB");
	    pdb_f.loadPDB(pdb_file);
	    //System.out.println("pdb_f.getStructure");
	    
	    structure = pdb_f.getStructure() ;
	    structure.setPDBCode(pdb_file);
	    // System.out.println("set Structure");
	    
	    spiceframe.setStructure(structure);
	    spiceframe.showStatus(pdb_file +" loaded");
	    //System.out.println("LoadStructureThread finished");
	    finished = true ;
	    notifyAll();
	    }
	catch (Exception e){ 
	    // at some point raise some IO exception, which should be defined by the Inferface
	    e.printStackTrace();
	    finished = true ;
	    StructureImpl n = new StructureImpl();
	    spiceframe.setStructure(n);
			
	}

    }
}
