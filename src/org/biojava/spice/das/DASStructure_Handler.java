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
 * Created on 06.05.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.spice.das;

import org.biojava.spice.config.* ;

import org.biojava.bio.structure.*                     ;
import org.biojava.bio.structure.io.DASStructureClient ;
import org.biojava.bio.structure.io.PDBFileReader      ;
import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.das.SpiceStructureFeeder;

import java.util.*                                     ;
import java.util.logging.*                             ;
import java.io.IOException                             ;

/** a wrapper class that uses the DAS CLient to connect to a DAS
 * Structure services, and retreive a biojava Structure objects.
 * this structure is then converted into a Simple_PDB_Container
 */

public class DASStructure_Handler 
    extends Thread    
{

    //String DASSTRUCTURECOMMAND = "http://dbdeskpro211.internal.sanger.ac.uk:8080/dazzle/mystruc/structure?query=" ;
    //String DASSTRUCTURECOMMAND = "http://protodas.derkholm.net/dazzle/mystruc/structure?query=" ;
    
    boolean finished            ;
    //DAS_PDBFeeder master        ;
    
    Structure structure         ; 
    RegistryConfiguration config;
    List   structureservers     ;
    String pdbCode              ;
    Logger logger               ;             
    SpiceStructureFeeder parent ;
    
    static String STRUCTUREDATABASE = "PDBresnum,Protein Structure" ;
    
    public DASStructure_Handler(RegistryConfiguration configuration, String pdbcode,SpiceStructureFeeder parent_) {
	super () ;
	
	parent = parent_ ;
	logger = Logger.getLogger("org.biojava.spice");

	pdbCode = pdbcode ;
	finished = false ;

	structure = null ;;
	config = configuration ;

	structureservers = new ArrayList();
	List tmplist = config.getServers("structure",STRUCTUREDATABASE); 
	Iterator iter = tmplist.iterator();

	// local structure server is always first
	while ( iter.hasNext()) {
	    SpiceDasSource ds = (SpiceDasSource)iter.next();
	    String url = ds.getUrl();
	    if ( url.substring(0,7).equals("file://") ) {
		// move to first position		
		structureservers.add(0,ds);
	    } else {
		structureservers.add(ds);
	    }
	}
    }
    
    
    

    
   

    public void run() {
	loadStructure(pdbCode);
    }


    public synchronized void loadStructure(String pdb_id){
	try {
	    
	    finished = false ;

	    Iterator iter = structureservers.iterator();
	    while (iter.hasNext()){
		SpiceDasSource ds = (SpiceDasSource)iter.next();
		String url = ds.getUrl();
		logger.finest(url);
		
		if ( url.substring(0,7).equals("file://") ) {
		    // load local PDB file
		    String dir  = url.substring(7);
		    logger.finer("trying to get PDB file from " + dir);
		    structure = getLocalPDB(dir,pdb_id);
		    if ( structure != null) {
			logger.exiting(this.getClass().getName(), "loadStructure");
			finished=true;
			notifyAll();
			return ;
		    }
		    continue ;
		} else {
		    // trying structure server ..
		    char lastChar = url.charAt(url.length()-1);		 
		    if ( ! (lastChar == '/') ) 
			url +="/" ;

		    String dasstructurecommand = url + "structure?model=1&query=";
	    

		    DASStructureClient dasc= new DASStructureClient(dasstructurecommand);
		    logger.info("requesting structure from "+dasstructurecommand  +pdb_id);	    
		    try {
			structure = dasc.getStructureById(pdb_id);	    			
		    }
		    catch (Exception e) {
			logger.log(Level.WARNING,"could not retreive structure from "+dasstructurecommand ,e);
			
		    }
		    logger.finest("DASStructure_Handler: got structure:");

		    //logger.finest(structure);
		    //convertStructureContainer(container,structure);
		    //container = structure  ;
		   
		    if ( structure != null) {
			parent.setStructure(structure);
			finished=true;
			notifyAll();
			return ;
		    }
		    continue ;
		}
		    

	    }

	    parent.setStructure(structure);
	    finished = true ;	    
	    notifyAll();
	    
	    
	   
	} catch (Exception e) {
	    e.printStackTrace();
	}	
    }


    private Structure getLocalPDB(String dir,String pdbcode) {
	PDBFileReader parser = new PDBFileReader() ;
	String[] extensions = config.getPDBFileExtensions();
	for (int i =0; i< extensions.length ; i++){
	    String s = extensions[i];
	    parser.addExtension(s);
	}
	parser.setPath(dir+ java.io.File.separator);
	Structure struc = null ;
	try {
	    struc = parser.getStructureById(pdbcode);
	}  catch ( IOException e) {
	    logger.log(Level.INFO,"local structure "+pdbcode+" not found, trying somewhere else");
	    //e.printStackTrace();
	    return null ;
	}
	return struc ;
    }
    

    /** return if loading of structure is finished
     */
    public boolean isDone(){
	return finished ;
    }
    
    //public Structure getStructure(){
    //return structure ;
    //}

}
