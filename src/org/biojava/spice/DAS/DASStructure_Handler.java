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
    String pdb_id ;

    boolean finished ;
    DAS_PDBFeeder master ;
    
    public DASStructure_Handler(DAS_PDBFeeder parent,String command) {
	super () ;
	dasstructurecommand = command ;
	finished = false ;
	master = parent ;
    }

   
    
    public void set_id(String id) {
	id = id.toLowerCase();
	pdb_id = id ;
    }
    public String get_id() {
	return pdb_id ;
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


    public void run() {

	loadStructure();
	
	//System.out.println("DASStructure_Handler: converting biojava Structure to Simple_PDBCOntainer");
	
    }
  

    public synchronized void loadStructure(){
	try {
	    finished = false ;
	    master.setStructureDone(false);

	    DASStructureClient dasc= new DASStructureClient(dasstructurecommand);
	    Structure structure ;
	    System.out.println(getTimeStamp() );
	    System.out.println("getting structure "+pdb_id);
	    structure = dasc.getStructure(pdb_id);	    
	    System.out.println("DASStructure_Handler: got structure:");
	    System.out.println(getTimeStamp() );
	    System.out.println(structure);
	    //convertStructureContainer(container,structure);
	    //container = structure  ;
	    finished = true ;
	    master.setStructure(structure);
	    master.setStructureDone(true);
	    notifyAll();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	
    }


    /*
    private synchronized void convertStructureContainer(PDB_Container conti,Structure structure) {
	//Simple_PDB_Container conti = container;
	
	//Simple_PDB_Container conti = new Simple_PDB_Container();

	conti.set_pdb_id( structure.getPDBCode());
	conti.set_name(   structure.getName()   );

	
	// currently use only model 1
	// go over chains..

	ArrayList chains= structure.getChains(0);
	for (int i =0; i< chains.size();i++){
	    PDB_Chain chain = convertChainPDB_Chain((Chain)chains.get(i));
	    // frequently a chain consists only of water ...
	    //System.out.println("adding chain");
	    if (chain.get_length() >0) {
		//System.out.println("length"+chain.get_length());
		conti.add_chain(chain);
	    }
	
	}
	

	
    }


    private PDB_Chain convertChainPDB_Chain(Chain oldchain){
	//System.out.println("convertChain ");
	Simple_PDB_Chain chain = new Simple_PDB_Chain();
	
	chain.set_name(oldchain.getName());
	
	// loop over all aminoacids
	ArrayList groups = oldchain.getGroups("amino");
	for ( int i=0;i<groups.size();i++){
	    // each group is an amino acid...

	    AminoAcid_Map amino = convertAminoAcid((AminoAcid)groups.get(i));
	    chain.add_aminoacid(amino);
	
	}

	return chain;
    }

    private AminoAcid_Map convertAminoAcid(AminoAcid oldamino){
	//System.out.println("convert AminoAcid");
	Simple_AminoAcid_Map amino = new Simple_AminoAcid_Map();

	amino.set_PDB_code(oldamino.getPDBCode()) ;
	amino.set_char(oldamino.getAminoType());
	
	Atom atom  ;
	try {
	    atom = oldamino.getCA();
	    double[] xyz = new double[3] ;
	    xyz[0] = atom.getX();
	    xyz[1] = atom.getY();
	    xyz[2] = atom.getZ();
	    amino.set_coords_Calpha(xyz);
	    
	    amino.set_pdbflag(true);
	} catch ( Exception e) {
	    //e.printStackTrace() ;
	    System.out.println(e.getMessage());
	    return null ;
	}
	   
	
	return amino ;

    }
    */
}
