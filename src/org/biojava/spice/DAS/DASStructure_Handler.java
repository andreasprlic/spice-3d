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
package org.biojava.spice;


import java.util.Calendar;

import org.biojava.bio.structure.*;
import org.biojava.bio.structure.io.DASStructureClient;

import java.util.* ;
import java.io.IOException ;
/** a wrapper class that uses the DAS CLient to connect to a DAS
 * Structure services, and retreive a biojava Structure objects.
 * this structure is then converted into a Simple_PDB_Container
 */

public class DASStructure_Handler 
    extends Thread    
{

    //String DASSTRUCTURECOMMAND = "http://dbdeskpro211.internal.sanger.ac.uk:8080/dazzle/mystruc/structure?query=" ;
    //String DASSTRUCTURECOMMAND = "http://protodas.derkholm.net/dazzle/mystruc/structure?query=" ;
    
    String dasstructurecommand ;

    boolean finished ;
    DAS_PDBFeeder master ;
    
    Structure structure ;

         
    public DASStructure_Handler(String command) {
	super () ;
	dasstructurecommand = command ;
	finished = false ;
	//master = parent 
	structure = null ;;
    }
    
    
    protected String getTimeStamp(){

	Calendar cal = Calendar.getInstance() ;
	// Get the components of the time
	int hour24 = cal.get(Calendar.HOUR_OF_DAY);     // 0..23
	int min = cal.get(Calendar.MINUTE);             // 0..59
	int sec = cal.get(Calendar.SECOND);             // 0..59
	String s = "time: "+hour24+" "+min+" "+sec;
	return s ;
    }

    
    /** needed ? 
     */
    //public void run() {
	
    //}

    public synchronized void loadStructure(String pdb_id){
	try {
	    
	    finished = false ;

	    DASStructureClient dasc= new DASStructureClient(dasstructurecommand);
	    System.out.println(getTimeStamp() );
	    System.out.println("getting structure "+pdb_id);	    
	    structure = dasc.getStructureById(pdb_id);	    
	    System.out.println("DASStructure_Handler: got structure:");
	    System.out.println(getTimeStamp() );
	    //System.out.println(structure);
	    //convertStructureContainer(container,structure);
	    //container = structure  ;
	    finished = true ;
	    
	    notifyAll();
	} catch (Exception e) {
	    e.printStackTrace();
	}	
    }

    /** return if loading of structure is finished
     */
    public boolean isDone(){
	return finished ;
    }
    public Structure getStructure(){
	return structure ;
    }

}
