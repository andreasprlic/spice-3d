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
import org.biojava.services.das.registry.DasSource;

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
   

    public String getStructureServer(){
	RegistryConfiguration config = spiceframe.getConfiguration();
	//Map strucconfig = (Map) config.get("structureserver");
	System.out.println("spice config:" + config);
	List servers = config.getServers("structure","PDBresnum");
	DasSource ds = (DasSource)servers.get(0);
	String u = ds.getUrl();

	return   u;
    }

    public String getSequenceServer() {
	RegistryConfiguration config = spiceframe.getConfiguration();
	//Map strucconfig = (Map) config.get("structureserver");
	List servers = config.getServers("sequence","UniProt");
	DasSource ds = (DasSource)servers.get(0);
	String u = ds.getUrl();	
    	return   u;
    }

    
    public String getAlignmentServer() 
	throws ConfigurationException
    {

	RegistryConfiguration config = spiceframe.getConfiguration();
	//Map strucconfig = (Map) config.get("structureserver");
	List servers = config.getServers("alignment");
	DasSource ds = (DasSource)servers.get(0);
	String u = ds.getUrl();
    	return   u;
	//Map config = spiceframe.getConfiguration();
    	//Map h = (Map) config.get("alignmentserver");
	//if ( h == null) {
	//  String msg = "No Alignment Server configured! unable to provide alignment";
	//  throw new ConfigurationException(msg);
	    
	//}
    	//String u = (String) h.get("url");

    }

    public synchronized void loadCompound() {
	
	try {
	
	   
	    
	    String dasstructurecommand = getStructureServer() + "structure?model=1&query=";
	    String dassequencecommand  = getSequenceServer()  + "sequence?segment=";
	    //String dassequencecommand  = spiceframe.getSequenceServer()  + "sequence?segment=";
	    String dasalignmentcommand = dasalignmentcommand = getAlignmentServer() + "alignment?query=" ;
	   
	    spiceframe.showStatus("Loading...Wait...",Color.red);
	    DAS_PDBFeeder pdb_f =  new DAS_PDBFeeder(dasstructurecommand,dassequencecommand,dasalignmentcommand) ;
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
