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
 * Created on 29.02.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.spice;

import org.biojava.bio.program.das.dasalignment.DASException ;

import org.biojava.bio.structure.Structure ;
import org.biojava.bio.structure.StructureImpl ;
import org.biojava.bio.structure.Chain ;
import org.biojava.bio.structure.Group ;
import org.biojava.bio.structure.Atom ;

import java.awt.Color;
import java.io.*;
import java.util.Iterator ;
import java.util.ArrayList ;



import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;
import java.util.Calendar;


import org.xml.sax.XMLReader;

import java.awt.Color;

public class DAS_PDBFeeder 
{

    /* make connectin to a DAS structure service and 
       get back structure
     */
    Structure pdb_container ; // used for alignment
    Structure pdb_structure ; // retreived from DAS structure requres
    

    Color currentColor, initColor, seColors[][], entColors[];
    String dasstructurecommand ;
    String dassequencecommand ;
    String dasalignmentcommand ;

    boolean structureDone ;
    boolean mappingDone   ;
    

    public DAS_PDBFeeder( String struccommand,String seqcommand, String aligcommand) {
	structureDone = false ;
	mappingDone   = false ;
	
	entColors = new Color [7];
	entColors[0] = Color.blue;
	entColors[1] = Color.red;
	entColors[2] = Color.green;
	entColors[3] = Color.magenta;
	entColors[4] = Color.orange;
	entColors[5] = Color.pink;
	entColors[6] = Color.yellow;
	dasstructurecommand  = struccommand      ;
	dassequencecommand   = seqcommand   ;
	dasalignmentcommand  = aligcommand  ;

	pdb_container =  new StructureImpl();
	pdb_structure =  new StructureImpl();
    }


    public synchronized void setStructure(Structure struc) {
	pdb_structure = struc ;
    }

    public synchronized void setStructureDone(boolean flag) {
	structureDone = flag ;
    }

    public synchronized void setMappingDone(boolean flag) {
	System.out.println("setMappingDone " + flag);
	mappingDone = flag ;
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

    public synchronized void loadPDB(String pdbcode)
	throws FileNotFoundException, IOException {
	try {
	    
	    // connect to structure service and retireve structure entry
	    DASStructure_Handler structure_handler = new DASStructure_Handler(dasstructurecommand);


	    
	    // wait for threads to be finished ..
	    boolean done = false ;
	    boolean stru_finished  = false ;
	    boolean seqfeat_finished = false ;
	    

	    // and not incorporate the structure data ...
	    //String pdb_id = pdbcode.toUpperCase();
	    String pdb_id = pdbcode ;
	    //structure_handler.set_id(pdb_id) ;
	    
	    structure_handler.start();
	    structure_handler.loadStructure(pdb_id);
	    
	   
	    // if not found   -> add error message ...

	    // if entry found ->  good

	    // then contact the alignment server to obtain the alignment
	    // and find out to which UniProt sequence this chain mapps to ...

	    System.out.println(getTimeStamp() );
	    DASAlignment_Handler dasali = new DASAlignment_Handler(this,pdb_container,dassequencecommand,dasalignmentcommand,pdb_id);
	   
	    dasali.start();
	    
	    
	    System.out.println("contacting DAS servers, please be patient... " + getTimeStamp() );

	    
	    //stru_finished    = structure_handler.downloadFinished();
	    //seqfeat_finished = dasali.downloadFinished();
	  

	    while ( ! done ) {
	
		
		try {
		    //System.out.println("DAS_PDBFeeder :xin waitloop");
		    //System.out.println(structureDone + " " + mappingDone);
		    //sleep(10);
		    wait(30);
		   
		} catch ( InterruptedException e) {		
		    done = true ;
		}

		if ( structure_handler.isDone()){
		    structureDone = true ;
		    pdb_structure = structure_handler.getStructure();
		}

		if ( structureDone && mappingDone) { 
		    done = true ; 
		}

		//wait(10);
		//stru_finished    = structure_handler.downloadFinished();
		//seqfeat_finished = dasali.downloadFinished();
	
		
	    }
	    //
	    // 

	    System.out.println("download finished " + getTimeStamp() );
	    System.out.println("pdb_container:" + pdb_container);
	    System.out.println("pdb_structure:" + pdb_structure);

	    

	    //pdb_data      = structure_handler.get_structure();
	    //pdb_container = dasali.get_structure();

	    // and join everything into a combined datastructure ...
	    joinWith(pdb_structure);
	    System.out.println("joining of data finished " +getTimeStamp() );
	    System.out.println(pdb_container);

	    //java.util.List chains = pdb_container.getChains(0);
	    //for ( int i =0;i<chains.size();i++){
	    //System.out.println("Displaying chain: " + i);
	    //Chain c = (Chain)chains.get(i);
	    //System.out.println(c);
	    //}
	    //pdb_container = dasali.getPDBContainer() ;
       

	

	} 
	catch (IOException e) {
	    e.printStackTrace();
	    System.err.println("I/O exception reading XML document");
	} 
	/*
	catch (SAXException e) {
	    e.printStackTrace();
	    System.err.println("XML exception reading document.");
	}
	*/


	
    }

    public Structure getStructure(){	
	return pdb_container ;
    }


    /** joins a 3D structure with the alignment. Also retrieves the
     * sequence using DAS the pdb_container created with the
     * alignment, does not contain any 3D information. this is coming
     * from the pdb_data provided as argument.     
    */
    
    public void joinWith( Structure pdb_data) 
	throws IOException
    {
	System.out.println("join With-----------------------------");
	System.out.println("pdb_data size: " + pdb_data.size());
	try {
	    // for every chain ...
	    for ( int i = 0 ; i< pdb_data.size() ; i++) {
		Chain c = pdb_data.getChain(i);
		String chainName = c.getName();
		
		Chain mapped_chain ;
		System.out.println("trying to map chain " +i + " " + chainName);
		try {
		    mapped_chain = getMatchingChain(pdb_container, chainName);
		} catch (DASException e) {
		    // could be a chain that does not contain any amino acids ...
		    // so the chain seems to be completely missing. Add it as a whole ...
		    pdb_container.addChain(c);
		    continue ;
		}
		//System.out.println(c+" " +mapped_chain);
		for (int j = 0 ; j < c.getLength(); j ++) {

		    Group amino = c.getGroup(j);
		    if ( amino == null ) {
			System.err.println("why is amino ("+j+", chain "+c.getName()+")== null??");
			continue ;
		    }

		    // skip nucleotides and hetatoms ...
		    if ( ! amino.getType().equals("amino")) {
			continue ;
		    }
		    if ( amino.has3D()){
			Group mappedamino = null ;
			try {
			    mappedamino = getMatchingGroup (mapped_chain,amino.getPDBCode());
			} catch ( DASException e) {
			    System.out.println("could not find residue nr"  +amino.getPDBCode() + " in chain "  + mapped_chain.getName() );
			    continue ;
			}
		    
			Iterator iter = amino.iterator() ;
			//System.out.println("amino size" + amino + " " +amino.size());
			while ( iter.hasNext() ) {
			    Atom a = (Atom) iter.next() ;
			    mappedamino.addAtom(a);
			}		   
		    } 
		}
		
		// add hetero atoms and nucleotides ...
		// they are not matched ...
		ArrayList groups = c.getGroups();
		for (int gi=0;gi<groups.size();gi++){
		    Group g = (Group)groups.get(gi);
		    if (g.getType().equals("hetatm")
			||g.getType().equals("nucleotide")
			){
			mapped_chain.addGroup(g);
		    }
	}		

	    }

	} catch ( Exception e) {
	    e.printStackTrace();
	    throw new IOException("could not join data...");
	}

    }

    private Chain getMatchingChain(Structure pdbcontainer, String chainID) 
	throws DASException
    {

	for ( int i = 0 ; i < pdbcontainer.size() ; i++) {
	    Chain c = pdbcontainer.getChain(i) ;
	    //System.out.println("get matching chain" + c.getName() + " " + chainID); 
	    if ( c.getName().equals(chainID)) {
		return c;
	    }
	}

	throw new DASException("no chain with ID >"+ chainID +"< found");
    }

    private Group getMatchingGroup(Chain mapped_chain,String pdbcode) 
	throws DASException
    {

	//System.out.println("getMatchingAmino: "+ pdbcode + "in chain:"+mapped_chain.getName());

	for (int j = 0 ; j < mapped_chain.getLength(); j ++) {
	    Group amino = mapped_chain.getGroup(j);
	    //System.out.println(j+ " " + amino) ;
	    
	    String pdbtmp = amino.getPDBCode();
	    if (pdbtmp == null) continue ;
	    if (pdbtmp.equals(pdbcode)){
		return amino ;
	    }
	}
	
	// can happen aat N terminal MET that is not aligned . e.g. pdb 103m
	throw new DASException("no aminoacid found with pdbcode >"+pdbcode+"<");
    }

}
