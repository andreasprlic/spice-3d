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
import org.biojava.spice.*       ;

import org.biojava.bio.program.das.dasalignment.DASException ;
import org.biojava.bio.program.das.dasalignment.*      ;
import org.biojava.bio.structure.Structure ;
import org.biojava.bio.structure.StructureImpl ;
import org.biojava.bio.structure.Chain ;
import org.biojava.bio.structure.Group ;
import org.biojava.bio.structure.Atom ;

import org.biojava.bio.seq.ProteinTools                ;
import org.biojava.bio.seq.io.SymbolTokenization       ;
import org.biojava.bio.symbol.Alphabet                 ;
import org.biojava.bio.symbol.Symbol                   ;
import org.biojava.bio.symbol.IllegalSymbolException   ;
import org.biojava.bio.*                               ;

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
public class DAS_UniProtFeeder 
    implements SpiceStructureFeeder 
{

    /* make connectin to a DAS structure service and 
       get back structure
     */
    Structure pdb_container ; // used for alignment
    Structure pdb_structure ; // retreived from DAS structure requres
    

    //String dassequencecommand ;
    String dasalignmentcommand ;

    boolean structureDone ;
    RegistryConfiguration config ;
    Logger logger        ;

    

    //public DAS_PDBFeeder( String struccommand,String seqcommand, String aligcommand) {
    public DAS_UniProtFeeder( RegistryConfiguration configuration) {
	logger = Logger.getLogger("org.biojava.spice");

	config = configuration ;

	

	//List seqservers = config.getServers("sequence","UniProt");
	//SpiceDasSource ds = (SpiceDasSource)seqservers.get(0);
	
	//dassequencecommand  = ds.getUrl()  + "sequence?segment=";



	pdb_container =  new StructureImpl();
	pdb_structure =  new StructureImpl();


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


    private  Alignment[] getAlignments(String uniprotcode) {
	logger.finest("searching for alignments of "+uniprotcode+" against PDB");
	Alignment[] alignments = null ;

	List aligservers = config.getServers("alignment");
	logger.finest("found " + aligservers.size() + " alignment servers");

	dasalignmentcommand = null  ;
	
	// loop over all available alignment servers 
	for ( int i =0 ; i < aligservers.size() ; i++ ) {
	    SpiceDasSource sds= (SpiceDasSource)aligservers.get(i);
	   
	    logger.finest("investigating " + sds.getUrl());
	    //System.out.println("investigating" + sds.getUrl());
	    // only consider those serving uniprot and PDB alignments
	    if ( config.isSeqStrucAlignmentServer(sds) ) {
		
		
		String url = sds.getUrl() ;
		char lastChar = url.charAt(url.length()-1);		 
		if ( ! (lastChar == '/') ) 
		    url +="/" ;
		dasalignmentcommand  = url +  "alignment?query=" ;

		logger.info("contacing alignment server " + dasalignmentcommand+uniprotcode);
		//System.out.println("contacing alignment server " + dasalignmentcommand);
		DASAlignmentClient dasc= new DASAlignmentClient(dasalignmentcommand);
		
		try{
		    alignments = dasc.getAlignments(uniprotcode);
		    
		    logger.finest("DASAlignmentHandler: got "+ alignments.length +" alignment(s):");
		    if ( alignments.length == 0 ) {
			// check next alignment server ...
			continue ;
		    }
		    return alignments ;
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}
	
	    

	logger.log(Level.SEVERE,"no UniProt - PDBresnum alignment found!, unable to map sequence to structure");
	
	

	return null ;
    }
    
    private String getPDBCodeFromAlignment(Alignment ali) {
	Annotation[] objects = ali.getObjects();
	for (int i =0 ; i<objects.length;i++) {
	    Annotation object = objects[i];
	    String dbCoordSys = (String)object.getProperty("dbCoordSys");

	    if ( dbCoordSys.equals("PDBresnum") ) {		
		return (String)object.getProperty("dbAccessionId") ;
	    }
	}
	
	return null ;
    }

    
    public synchronized Structure loadUniProt(String uniprotcode)
	throws FileNotFoundException, IOException {
	Structure struc = new StructureImpl();
	try {
	    
	    logger.finest("in DAS_UniProtFeeder ...");

	    // get matching pdb codes
	    // by making DAS_Alignment request

	    Alignment[] alignments = getAlignments(uniprotcode);

	    if ( alignments == null ) {
		// aargh catch exception ...	
		logger.log(Level.SEVERE,"could not retreive any UniProt-PDB alignment from DAS servers");
		String sequence = getSequence(uniprotcode);
		if (sequence == null ) {
		    return struc ;
		}
		struc = makeStructureFromSequence(uniprotcode,sequence);
		return struc ;
	    }
	  
	    // we only take first PDB code we find ...
	    String pdbcode = null ;
	    Alignment ali  = null ;

	    for ( int i = 0 ; i< alignments.length ; i++ ) {

		ali = alignments[i];
		pdbcode = getPDBCodeFromAlignment(ali);
		if ( pdbcode.equals("null"))
		     continue ;
		if ( pdbcode != null )
		     break ;
		
	    }

	    if (pdbcode == null ) {
		/// argh catch exception ...
		logger.log(Level.SEVERE,"could not retreive any pdb code from Alignment");
		// return "empty" structure...
		// get sequence
		String sequence = getSequence(uniprotcode);
		if (sequence == null ) {
		    return struc ;
		}
		struc = makeStructureFromSequence(uniprotcode,sequence);
		return struc;
	    }
	    logger.finest("found alignment with " +pdbcode);
	    // remove chain from code :
	    String[] spl = pdbcode.split("\\.");
	    if ( spl.length > 1 ) 
		pdbcode = spl[0];
	    logger.finest("pdbcode now " +pdbcode);
	    // get structure / sequence in parallel
	    
	    
	    DASStructure_Handler structure_handler = new DASStructure_Handler(config,pdbcode,this);
	    structure_handler.start();
	    
	    // wait for threads to be finished ..
	    boolean done           = false ;
	    
	    String sequence = getSequence(uniprotcode);
	    if (sequence == null ) {
		return struc ;
	    }
	   
	    while ( ! done ) {
			
		try {
		    wait(30);
		   
		} catch ( InterruptedException e) {		
		    done = true ;
		}

		if ( structure_handler.isDone()){
		    done = true ;
		    // structurehandler sets the structure in pdb_structure
		    
		    //pdb_structure = structure_handler.getStructure();
		}
	    }
	    logger.finest("DAS_UniProtFeeder got sequence and structure");
	    logger.finest(pdb_container.toString());
	    pdb_structure.setPDBCode(pdbcode);
		
	    // join the three bits 
	    StructureBuilder strucbuilder = new StructureBuilder();
	    struc = strucbuilder.createSpiceStructure(ali, pdb_structure, sequence);
	    struc.setPDBCode(pdbcode);

	    logger.finest("joining of data finished " +getTimeStamp() );
	    logger.finest(pdb_container.toString());

	    //java.util.List chains = pdb_container.getChains(0);
	    //for ( int i =0;i<chains.size();i++){
	    //logger.finest("Displaying chain: " + i);
	    //Chain c = (Chain)chains.get(i);
	    //logger.finest(c);
	    //}
	    //pdb_container = dasali.getPDBContainer() ;
       
	    return struc ;
	

	} 
	catch (Exception e) {
	    //e.printStackTrace();
	    logger.log(Level.SEVERE,"an exception occured",e);
	} 
	/*
	catch (SAXException e) {
	    e.printStackTrace();
	    System.err.println("XML exception reading document.");
	}
	*/

	return struc ;
	
    }

    /** emergency procedure to create an "empty" Structure, which represents the sequence to be displayed in SPICE */
    private Structure makeStructureFromSequence(String id,String sequence ) {
	StructureBuilder sbuilder = new StructureBuilder();
	Chain chain =  sbuilder.getChainFromSequence(id,sequence);
	StructureImpl struc = new StructureImpl();
	struc.addChain(chain);
	//struc.setSwissProtID(id);
	return struc;

    }

    /** do DAS communication to get sequence */
    private String getSequence(String uniprotcode) {
	String sequence = null ;
	DAS_SequenceRetreive seq_das = new DAS_SequenceRetreive(config) ;
	try {
	    sequence              = seq_das.get_sequence(uniprotcode);
	} catch ( ConfigurationException e) {
	    
	    // arggh.
	    e.printStackTrace();
	    logger.log(Level.SEVERE,"could not retreive any sequence from DAS servers");
	    return null;
	}
	return sequence ;
    }

    public synchronized void setStructure(Structure struc) {
	logger.finest("setting structure in DAS_UniProtFeeder");
	pdb_structure = struc ;
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
