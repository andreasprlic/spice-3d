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
 * Created on 17.05.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.spice;

import org.biojava.bio.structure.*;
import org.biojava.bio.structure.io.PDBParseException ;
import org.biojava.bio.program.das.dasalignment.* ;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.SymbolTokenization ;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.* ;

import java.util.* ;


import java.io.*;

public class DASAlignment_Handler extends Thread
{

    // we know for sure that the alignment contains mapping from seq
    // to structure because we define the path to server here.  if
    // this is changed at some point the place that is changing is
	  
    //static String ALIGNMENTSERVER   = "http://protodas.derkholm.net/dazzle/myalig/alignment?query=" ;
    //static String ALIGNMENTSERVER   = "http://dbdeskpro211.internal.sanger.ac.uk:8080/dazzle/myalig/alignment?query=" ;
    //static String SEQUENCESERVER    = "http://das.ensembl.org/das/swissprot/dna?segment=" ;
	  
    String alignmentserver ;
    String sequenceserver  ;
    
    // hard coded that refference is UniProt!!!
    // find a better solution ...
    static String SEQUENCEDATABASE  = "UniProt" ;
    static String STRUCTUREDATABASE = "PDBresnum" ;

    ArrayList sequencelist ;

    // for conversion 3code 1code
    SymbolTokenization threeLetter ;
    SymbolTokenization oneLetter ;
	  
    Structure pdb_container ;
    String pdb_id ;
    boolean downloadFinished ;
    DAS_PDBFeeder master ;
    /** set the connection commands */


    public DASAlignment_Handler(DAS_PDBFeeder parent, Structure pdbcontainer, String sequenceserv,String alignmentserv,String pdbid) {
	master = parent ;
	pdb_container =  pdbcontainer;
	sequencelist = new ArrayList() ;
	alignmentserver = alignmentserv ;
	sequenceserver  = sequenceserv  ;
	pdb_id = pdbid ;
	downloadFinished = false ;

	Alphabet alpha_prot = ProteinTools.getAlphabet();

	try {
	    threeLetter = alpha_prot.getTokenization("name");
	    oneLetter  = alpha_prot.getTokenization("token");
	} catch (Exception e) {
	    e.printStackTrace() ;
	}
	

    }

    public void run() {
	
	loadAlignments(pdb_id);
	
    }

    public synchronized boolean downloadFinished() {
	//System.out.println("alignmenthandler finished: "+downloadFinished);
	return downloadFinished;
    }

    public synchronized void loadAlignments(String pdb_code) {
	downloadFinished = false ;
	master.setMappingDone(false) ;
	
	pdb_code = pdb_code.toLowerCase() ;
	//pdb_container = new Simple_PDB_Container() ;

	String server = alignmentserver ;

	DASAlignmentClient dasc= new DASAlignmentClient(server);
	Alignment[] alignments = null ;
	try{
	    alignments = dasc.getAlignments(pdb_code);	 

	    System.out.println("DASAlignmentHandler: got "+ alignments.length +" alignment(s):");

	    convertToStructure(alignments);
	    
   
	} catch (Exception e){
	    e.printStackTrace();
	}
	downloadFinished = true ;
	master.setMappingDone(true) ;
	//System.out.println("DASAlignmentHandler: notifyAll()");
	
	notifyAll();


    }


    public Structure get_structure() {
	return pdb_container;
    }

    /** convert an ArrayList of Alignmetn objects into a PDB_COntainer
     * data structure */

    private synchronized void convertToStructure(Alignment[] alignments) 

	throws DASException
    {

	//Simple_PDB_Container conti = new Simple_PDB_Container();
	for (int i=0;i<alignments.length;i++) {
	    Alignment ali = alignments[i] ;
	    System.out.println(ali);
	    Chain current_chain = initializeChain(ali);
	    pdb_container.addChain(current_chain) ;
 
	}
	//System.out.println("DASAlignment_Handler: convertToPDB_Container end");



	
	
    }



    /** retrieve the HashMap for the sequence ... */
    
    private Annotation getAlignmentObject (Alignment ali,String objecttype) 
	throws DASException
    {
	// go through objects and get sequence one ...
	Annotation[] objects = ali.getObjects();
	HashMap seq_obj = new HashMap() ;

	for (int i =0 ; i<objects.length;i++) {
	    Annotation object = objects[i];
	    String dbCoordSys = (String)object.getProperty("dbCoordSys");
	    


	    if ( dbCoordSys.equals(objecttype) ) {		
		return object ;
	    }
	}
	

	throw new DASException("no >" + objecttype + "< object found as dbSource in alignment!");
	
    }


    /** store sequences in memory speed up, so we do not have to make so many DAS calls ... */
    private String getSequenceFromMemory(String swissp_id) {
	String sequence = null ;
	for ( int i = 0 ; i < sequencelist.size() ; i++) {
	    HashMap seq = (HashMap) sequencelist.get(i) ;
	    String sp = (String) seq.get("id");
	    if ( sp.equals(swissp_id)) {
		sequence = (String) seq.get("sequence");
		return sequence ;
	    }
	}
	return sequence ;
    }

    /** store sequences in memory speed up so we do not have to make
     * so many DAS calls ... */
    private void addSequenceToMemory(String sequence,String swissp_id) {
	HashMap h = new HashMap() ;
	h.put("id",swissp_id);
	h.put("sequence",sequence);
	sequencelist.add(h) ;
    }

    /** retreive the sequence from uniprot using DAS */
    private String getDASSequence(String swissp_id) {

	// get Sequence using DAS ...
	// get sequence from sequence server
	
	// speed up -> store sequence mappings in memory -> faster
	String sequence ;
	
	sequence = getSequenceFromMemory(swissp_id) ;
	
	if ( sequence == null ) {

	    String connstr = sequenceserver ;
	    DAS_SequenceRetreive seq_das = new DAS_SequenceRetreive(connstr) ;
	    sequence              = seq_das.get_sequence(swissp_id);
	    //System.out.println("got sequence using DAS: " + sequence);
	    addSequenceToMemory(sequence,swissp_id);
	}

	return sequence ;
    }

    
    /** get UniProt sequence from alignment and set up chain with
     * uniprot sequence */
    private synchronized Chain initializeChain(Alignment ali) 
	throws DASException
    {
	
	Annotation object     = getAlignmentObject(ali,SEQUENCEDATABASE);
	Annotation struobject = getAlignmentObject(ali,STRUCTUREDATABASE);
	String swissp_id = (String) object.getProperty("dbAccessionId") ;
	String sequence  = getDASSequence(swissp_id);
	//System.out.println(swissp_id);
	//System.out.println(sequence);
	
	Chain chain =  new ChainImpl();
	String chainname = (String) struobject.getProperty("dbAccessionId");
	
	chain.setName(getChainFromPDBCode(chainname));
	chain.setSwissprotId(swissp_id);
	// and build up chain ...

	for ( int pos = 0 ; pos < sequence.length() ; pos ++ ){
	    AminoAcidImpl s_amino = new AminoAcidImpl();
	    
	    Character c = new Character(sequence.charAt(pos)) ;
	    s_amino.setAminoType(c) ;
	    String code1 = c.toString() ;
	    String code3 = "" ;
	    try {
		code3 = convert_1code_3code(code1);
	    } catch (IllegalSymbolException e) {
		e.printStackTrace();
		code3 = "XXX" ;		
	    }
	    try {
		s_amino.setPDBName(code3) ;		
	    } catch ( PDBParseException e ) {
		e.printStackTrace() ;
		
	    }
	    chain.addGroup(s_amino) ;	    
	} 
	//System.out.println(chain);
	Chain retchain = addChainAlignment(chain,ali);
	//System.out.println(retchain);
	return retchain ;
	
    }
	  
    /** convert one character amino acid codes into three character
     *  e.g. convert CYS to C
     */
    
    private String convert_1code_3code(String code1) 
	throws IllegalSymbolException
    {
	Symbol sym   =  oneLetter.parseToken(code1) ;
	String code3 =  threeLetter.tokenizeSymbol(sym);
	
	return code3;
		
    }


    private String getChainFromPDBCode(String pdbcode) {
	String[] spl = pdbcode.split("\\.");
	String chain = spl[1] ;
	return chain ;
    }


    private synchronized Chain addChainAlignment(Chain chain, Alignment ali) 
	throws DASException
    {
	//System.out.println("addChainAlignment");

	// go over all blocks of alignment and join pdb info ...
	Annotation seq_object   = getAlignmentObject(ali,SEQUENCEDATABASE );
	
	Annotation stru_object  = getAlignmentObject(ali,STRUCTUREDATABASE);
	
	//System.out.println(seq_object.get("id"));
	//System.out.println(stru_object.get("id"));

	//Simple_AminoAcid_Map current_amino = null ;
	
	Annotation[] blocks = ali.getBlocks() ;
	for ( int i =0; i < blocks.length;i++) {
	    Annotation block = blocks[i] ;

	    mapSegments(block,chain,seq_object,stru_object); 
	}
	
	return chain ;
    }



    /** map from one segment to the other and store this info in chain  */
    private synchronized void mapSegments(Annotation block, 
					  Chain chain,
					  Annotation seq_obj, 
					  Annotation stru_obj) 
	throws DASException
    {
	//System.out.println("mapSegment");
	// order segmetns
	// HashMap 0 = refers to seq
	// hashMap 1 = refers to struct
	ArrayList segments = (ArrayList)block.getProperty("segments");
	Annotation[] arr = new Annotation[segments.size()] ;
	
	String seq_id  = (String)seq_obj.getProperty("intObjectId");
	String stru_id = (String)stru_obj.getProperty("intObjectId");

	if ( segments.size() <2) {
	    System.out.println("<2 segments in block. skipping");
	    return ;
	}

	for ( int s =0; s< segments.size() ; s++) {
	    Annotation segment = (Annotation) segments.get(s) ;

	    String obid = (String) segment.getProperty("intObjectId");
	    if ( obid.equals(seq_id)) {		    
		arr[0] = segment;		
	    }
	    if ( obid.equals(stru_id)) {		    
		arr[1] = segment;		
	    }

	    // if there are other alignments, do not consider them ...
	}

	
	// here is the location where the cigar string shouldbe parsed, if there is any
	// for the moment, nope..... !!!
	
	// coordinate system for sequence is always numeric and linear
	// -> phew!
	int seqstart = Integer.parseInt((String)arr[0].getProperty("start")); 
	int seqend   = Integer.parseInt((String)arr[0].getProperty("end"));
	
	// size of the segment
	int segsize = seqend-seqstart + 1 ;

	   
	// now build up a segment of aminoacids 
	AminoAcid[] aminosegment = new AminoAcid[segsize]  ;
	
	
	for ( int i = 0 ; i < segsize ; i++) {
	    //System.out.println("i "+i);
	    aminosegment[i] = (AminoAcid) chain.getGroup(i+seqstart-1);
	}
	

	// map segment of aminoacids to structure...
	int strustart = 0 ;
	int struend   = 0 ;
	try {
	    strustart = Integer.parseInt((String)arr[1].getProperty("start"));
	    struend   = Integer.parseInt((String)arr[1].getProperty("end"));
	    //System.out.println(strustart + " " + struend);
	} catch (Exception e) {
	    // if this does not work  there is an insertion code
	    // -> segment must be of size one.
	    //  alignment from seq to structure HAS TO BE provided as a one to one mapping
	   
	    //System.out.println("CAUGHT!!!!! conversion of >"+ (String)arr[1].get("start") + "<") ;


	    if ( segsize != 1 ) {
		throw new DASException(" alignment is not a 1:1 mapping! -> this does not work!");		
	    }
	    String pdbcode = (String)arr[1].getProperty("start") ;
	    if ( pdbcode.equals("-")) {
		// not mapped ...
		//System.out.println("skipping char - at position " + pdbcode);
		return ;
	    }

	    e.printStackTrace();
	    System.out.println("setting new pdbcode "+pdbcode) ;
	    aminosegment[0].setPDBCode(pdbcode);
	    return ;
	}
	
	//System.out.println("segsize " + segsize);
	for ( int i =0 ; i< segsize; i++) {
	    
	    String pdbcode = Integer.toString(strustart + i) ;
	    //System.out.println(i + " " + pdbcode);
	    aminosegment[i].setPDBCode(pdbcode) ;
	}
       	
    }


 
   

    

   

    //public PDB_Container getPDBContainer () {
    //return pdb_container ;
    //}

}
