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
package org.biojava.spice.DAS;


import org.biojava.spice.Config.*;
import org.biojava.bio.program.das.dasalignment.DASException ;

import org.biojava.bio.structure.Structure ;
import org.biojava.bio.structure.StructureImpl ;
import org.biojava.bio.structure.Chain ;
import org.biojava.bio.structure.Group ;
import org.biojava.bio.structure.Atom ;

import java.awt.Color							;
import java.io.*								;
import java.util.Iterator 						;
import java.util.ArrayList 						;
import java.util.List 							;
import java.util.Calendar                      	;
import java.util.logging.*                     	;




/** a class that connects to a DAS structure service and retreives a
 * structure.
 * @author Andreas Prlic
*/
public class DAS_PDBFeeder 
    implements SpiceStructureFeeder
{

    /* make connectin to a DAS structure service and 
       get back structure
     */
    Structure pdb_container ; // used for alignment
    Structure pdb_structure ; // retreived from DAS structure requres
    

    Color currentColor, initColor, seColors[][], entColors[];
    //String dassequencecommand ;
    String dasalignmentcommand ;

    boolean structureDone ;
    boolean mappingDone   ;
    RegistryConfiguration config ;
    Logger logger        ;
    //public DAS_PDBFeeder( String struccommand,String seqcommand, String aligcommand) {
    public DAS_PDBFeeder( RegistryConfiguration configuration) {
	logger = Logger.getLogger("org.biojava.spice");
	structureDone = false ;
	mappingDone   = false ;
	config = configuration ;
	entColors = new Color [7];
	entColors[0] = Color.blue;
	entColors[1] = Color.red;
	entColors[2] = Color.green;
	entColors[3] = Color.magenta;
	entColors[4] = Color.orange;
	entColors[5] = Color.pink;
	entColors[6] = Color.yellow;

	//List seqservers = config.getServers("sequence","UniProt");
	//SpiceDasSource ds = (SpiceDasSource)seqservers.get(0);
	
	//dassequencecommand  = ds.getUrl()  + "sequence?segment=";

	List aligservers = config.getServers("alignment");
	dasalignmentcommand = null  ;
	
	for ( int i =0 ; i < aligservers.size() ; i++ ) {
	    SpiceDasSource sds= (SpiceDasSource)aligservers.get(i);
	   
	    if ( config.isSeqStrucAlignmentServer(sds) ) {
	
		//
		String url = sds.getUrl() ;
		char lastChar = url.charAt(url.length()-1);		 
		if ( ! (lastChar == '/') ) 
		    url +="/" ;
		dasalignmentcommand  = url +  "alignment?query=" ;
		break ;
	    }
	}
	
	    
	if ( dasalignmentcommand == null ) {
	    logger.log(Level.SEVERE,"no UniProt - PDBresnum alignment server found!, unable to map sequence to structure");
	    dasalignmentcommand = "" ;
	}

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
	logger.finest("setMappingDone " + flag);
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
	    DASStructure_Handler structure_handler = new DASStructure_Handler(config,pdbcode,this);
	    
	    // wait for threads to be finished ..
	    boolean done = false ;
	    boolean stru_finished  = false ;
	    boolean seqfeat_finished = false ;
	    

	    // and not incorporate the structure data ...
	    //String pdb_id = pdbcode.toUpperCase();
	    String pdb_id = pdbcode ;
	    //structure_handler.set_id(pdb_id) ;
	    
	    structure_handler.start();
	    //structure_handler.loadStructure();
	    
	   
	    // if not found   -> add error message ...

	    // if entry found ->  good

	    // then contact the alignment server to obtain the alignment
	    // and find out to which UniProt sequence this chain mapps to ...

	    logger.finest(getTimeStamp() );
	    DASAlignment_Handler dasali = new DASAlignment_Handler(this,pdb_container,config,dasalignmentcommand,pdb_id);
	   
	    dasali.start();
	    
	    
	    logger.finest("contacting DAS servers, please be patient... " + getTimeStamp() );

	    
	    //stru_finished    = structure_handler.downloadFinished();
	    //seqfeat_finished = dasali.downloadFinished();
	  

	    while ( ! done ) {
	
		
		try {
		    //logger.finest("DAS_PDBFeeder :xin waitloop");
		    //logger.finest(structureDone + " " + mappingDone);
		    //sleep(10);
		    wait(30);
		   
		} catch ( InterruptedException e) {		
		    done = true ;
		}

		if ( structure_handler.isDone()){
		    structureDone = true ;
		    // structurehandler sets structure here.
		    //pdb_structure = structure_handler.getStructure();
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

	    logger.finest("download finished " + getTimeStamp() );
	    logger.finest("pdb_container:" + pdb_container.toString());
	    logger.finest("pdb_structure:" + pdb_structure.toString());

	    

	    //pdb_data      = structure_handler.get_structure();
	    //pdb_container = dasali.get_structure();

	    // and join everything into a combined datastructure ...
	    joinWith(pdb_structure);
	    logger.finest("joining of data finished " +getTimeStamp() );
	    logger.finest(pdb_container.toString());

	    //java.util.List chains = pdb_container.getChains(0);
	    //for ( int i =0;i<chains.size();i++){
	    //logger.finest("Displaying chain: " + i);
	    //Chain c = (Chain)chains.get(i);
	    //logger.finest(c);
	    //}
	    //pdb_container = dasali.getPDBContainer() ;
       

	

	} 
	catch (IOException e) {
	    //e.printStackTrace();
	    logger.log(Level.SEVERE,"I/O exception reading XML document",e);
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
	logger.finest("join With-----------------------------");
	logger.finest("pdb_data size: " + pdb_data.size());
	try {
	    // for every chain ...
	    for ( int i = 0 ; i< pdb_data.size() ; i++) {
		Chain c = pdb_data.getChain(i);
		String chainName = c.getName();
		
		Chain mapped_chain ;
		logger.finest("trying to map chain " +i + " " + chainName);
		try {
		    mapped_chain = getMatchingChain(pdb_container, chainName);
		} catch (DASException e) {
		    // could be a chain that does not contain any amino acids ...
		    // so the chain seems to be completely missing. Add it as a whole ...
		    pdb_container.addChain(c);
		    continue ;
		}
		//logger.finest(c+" " +mapped_chain);
		for (int j = 0 ; j < c.getLength(); j ++) {

		    Group amino = c.getGroup(j);
		    if ( amino == null ) {
			logger.finer("why is amino ("+j+", chain "+c.getName()+")== null??");
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
			    logger.finest("could not find residue nr"  +amino.getPDBCode() + " in chain "  + mapped_chain.getName() );
			    continue ;
			}
		    
			Iterator iter = amino.iterator() ;
			//logger.finest("amino size" + amino + " " +amino.size());
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
	    //logger.finest("get matching chain" + c.getName() + " " + chainID); 
	    if ( c.getName().equals(chainID)) {
		return c;
	    }
	}

	throw new DASException("no chain with ID >"+ chainID +"< found");
    }


    /** find the group that has PDBCode pdbcode in Chain mapped_chain */
    private Group getMatchingGroup(Chain mapped_chain,String pdbcode) 
	throws DASException
    {

	//logger.finest("getMatchingAmino: "+ pdbcode + "in chain:"+mapped_chain.getName());

	for (int j = 0 ; j < mapped_chain.getLength(); j ++) {
	    Group amino = mapped_chain.getGroup(j);
	    //logger.finest(j+ " " + amino) ;
	    
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
