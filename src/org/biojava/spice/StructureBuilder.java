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
 * Created on 13.01.2005
 * @author Andreas Prlic
 *
 */
package org.biojava.spice;


import org.biojava.spice.Config.*                      ;
import org.biojava.bio.structure.*                     ;
import org.biojava.bio.structure.io.PDBParseException  ;
import org.biojava.bio.program.das.dasalignment.*      ;
import org.biojava.bio.seq.ProteinTools                ;
import org.biojava.bio.seq.io.SymbolTokenization       ;
import org.biojava.bio.symbol.Alphabet                 ;
import org.biojava.bio.symbol.Symbol                   ;
import org.biojava.bio.symbol.IllegalSymbolException   ;
import org.biojava.bio.*                               ;

import java.util.*                                     ;
import java.util.logging.*                             ;
import java.io.*;


public class StructureBuilder{
    
    // hard coded - 
    // find a better solution ...
    static String SEQUENCEDATABASE  = "UniProt" ;
    static String STRUCTUREDATABASE = "PDBresnum" ;



 // for conversion 3code 1code
    SymbolTokenization threeLetter ;
    SymbolTokenization oneLetter ;

    Logger logger ;
    public StructureBuilder(){
	logger = Logger.getLogger("org.biojava.spice");
	

	// some utils for conversion of 3code to 1code
	Alphabet alpha_prot = ProteinTools.getAlphabet();

	try {
	    threeLetter = alpha_prot.getTokenization("name");
	    oneLetter  = alpha_prot.getTokenization("token");
	} catch (Exception e) {
	    e.printStackTrace() ;
	}

    }

    /** create a structure to be displayed in SPICE */
    public Structure createSpiceStructure(Alignment alignment, Structure pdbStructure, String sequence) 
	throws DASException
    {

	// the master structure to be returned to SPICE
	Structure spice_pdb = new StructureImpl() ;

	// tmp test
	Alignment[] alignments = new Alignment[1];
	alignments[0] = alignment ;
	//

	// build up SPICE structure based on UniProt sequence and alignment
	for (int i=0;i<alignments.length;i++) {
	    Alignment ali = alignments[i] ;
	    Chain current_chain = createChain(ali,sequence);
	    spice_pdb.addChain(current_chain) ;	    
	}

	// join the sequence based structure, that at this stage does not have coordinates, with PDB structure
	try {
	    spice_pdb = joinStructures(spice_pdb, pdbStructure) ;
	} catch (IOException e) {
	    
	    logger.log(Level.SEVERE,"could not join UniProt sequence and PDB structure!",e);
	}

	return spice_pdb ;
    }

 /** map from one segment to the other and store this info in chain  */
    private  void mapSegments(Annotation block, 
					  Chain chain,
					  Annotation seq_obj, 
					  Annotation stru_obj) 
	throws DASException
    {
	//logger.finest("mapSegment");
	// order segmetns
	// HashMap 0 = refers to seq
	// hashMap 1 = refers to struct
	ArrayList segments = (ArrayList)block.getProperty("segments");
	Annotation[] arr = new Annotation[segments.size()] ;
	
	String seq_id  = (String) seq_obj.getProperty("intObjectId");
	String stru_id = (String)stru_obj.getProperty("intObjectId");

	if ( segments.size() <2) {
	    logger.finest("<2 segments in block. skipping");
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
	    //logger.finest("i "+i);
	    aminosegment[i] = (AminoAcid) chain.getGroup(i+seqstart-1);
	}
	

	// map segment of aminoacids to structure...
	int strustart = 0 ;
	int struend   = 0 ;
	try {
	    strustart = Integer.parseInt((String)arr[1].getProperty("start"));
	    struend   = Integer.parseInt((String)arr[1].getProperty("end"));
	    //logger.finest(strustart + " " + struend);
	} catch (Exception e) {
	    // if this does not work  there is an insertion code


	    // -> segment must be of size one.
	    //  alignment from seq to structure HAS TO BE provided as a one to one mapping
	   
	    //logger.finest("CAUGHT!!!!! conversion of >"+ (String)arr[1].get("start") + "<") ;
	    //e.printStackTrace();

	    if ( segsize != 1 ) {
		throw new DASException(" alignment is not a 1:1 mapping! there is an insertion code -> this does not work!");		
	    }
	    
	    String pdbcode = (String)arr[1].getProperty("start") ;
	    if ( pdbcode.equals("-")) {
		// not mapped ...
		//logger.finest("skipping char - at position " + pdbcode);
		return ;
	    }

	    //e.printStackTrace();
	    logger.finest("Insertion Code ! setting new pdbcode "+pdbcode) ;
	    aminosegment[0].setPDBCode(pdbcode);
	    return ;
	}
	
	//logger.finest("segsize " + segsize);
	for ( int i =0 ; i< segsize; i++) {
	    
	    String pdbcode = Integer.toString(strustart + i) ;
	    //logger.finest(i + " " + pdbcode);
	    aminosegment[i].setPDBCode(pdbcode) ;
	}
       	
    }



    private  Chain addChainAlignment(Chain chain, Alignment ali) 
	throws DASException
    {
	//logger.finest("addChainAlignment");

	// go over all blocks of alignment and join pdb info ...
	Annotation seq_object   = getAlignmentObject(ali,SEQUENCEDATABASE );
	
	Annotation stru_object  = getAlignmentObject(ali,STRUCTUREDATABASE);
	
	//logger.finest(seq_object.get("id"));
	//logger.finest(stru_object.get("id"));

	//Simple_AminoAcid_Map current_amino = null ;
	
	Annotation[] blocks = ali.getBlocks() ;
	for ( int i =0; i < blocks.length;i++) {
	    Annotation block = blocks[i] ;

	    mapSegments(block,chain,seq_object,stru_object); 
	}
	
	return chain ;
    }



 private String getChainFromPDBCode(String pdbcode) {
	//logger.finest("DASAlignment_Handler: getChainFromPDBCode" + pdbcode);
	String[] spl = pdbcode.split("\\.");
	String chain = spl[1] ;
	//logger.finest("DASAlignment_Handler: getChainFromPDBCode chain:" + chain);
	return chain ;
    }


    public Chain  createChain(Alignment ali, String sequence) 
	throws DASException
    {
	Annotation object     = getAlignmentObject(ali,SEQUENCEDATABASE);
	Annotation struobject = getAlignmentObject(ali,STRUCTUREDATABASE);
	String swissp_id = (String) object.getProperty("dbAccessionId") ;
	//logger.finest(swissp_id);
	//logger.finest(sequence);
	
	Chain chain =  new ChainImpl();
	String chainname = (String) struobject.getProperty("dbAccessionId");
	
	chain.setName(getChainFromPDBCode(chainname));
	chain.setSwissprotId(swissp_id);

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
	//logger.finest(chain);
	Chain retchain = addChainAlignment(chain,ali);
	//logger.finest(retchain);
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


    /////////////////////////////////////////////////////////////////
    ////// this section for joining of the two structures
    /////////////////////////////////////////////////////////////////


    /** joins a 3D structure with the alignment. Also retrieves the
     * sequence using DAS the spice_structure created with the
     * alignment, does not contain any 3D information. this is coming
     * from the pdb_structure provided as argument.     
    */
    
    private Structure joinStructures(Structure spice_structure, Structure pdb_structure) 
	throws IOException
    
{
	logger.finest("join With-----------------------------");
	logger.finest("pdb_structure >" +pdb_structure.getPDBCode() + "< size: " + pdb_structure.size());
	try {
	    // for every chain ...
	    for ( int i = 0 ; i< pdb_structure.size() ; i++) {
		Chain c = pdb_structure.getChain(i);
		String chainName = c.getName();
		
		Chain mapped_chain ;
		logger.finest("trying to map chain " +i + " " + chainName);
		try {
		    // get chain from spice 
		    mapped_chain = getMatchingChain(spice_structure, chainName);
		} catch (DASException e) {
		    // could be a chain that does not contain any amino acids ...
		    // so the chain seems to be completely missing. Add it as a whole ...
		    spice_structure.addChain(c);
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

	
	// return the newly joint structure.
	spice_structure.setPDBCode(pdb_structure.getPDBCode());
	return spice_structure;

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
