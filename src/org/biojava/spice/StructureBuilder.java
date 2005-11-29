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
import org.biojava.spice.das.AlignmentTools;

import org.biojava.bio.program.ssbind.AnnotationFactory;
import org.biojava.utils.ChangeVetoException;

public class StructureBuilder{
    
    // hard coded - 
    // find a better solution ...
    static String SEQUENCEDATABASE  = "UniProt,Protein Sequence" ;
    static String STRUCTUREDATABASE = "PDBresnum,Protein Structure" ;
    
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
        
        // tmp test
        Alignment[] alignments = new Alignment[1];
        alignments[0] = alignment ;
        
        Structure spice_pdb = new StructureImpl() ;
        
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
        
        String pdbcode = AlignmentTools.getPDBCodeFromAlignment(alignments[0]);
        Annotation object = AlignmentTools.getObject(pdbcode,alignments[0]);
        List details = (List) object.getProperty("details");
        //logger.info("displaying alignment details:");
        
        Iterator iter = details.iterator();
        Map header = spice_pdb.getHeader();
        while (iter.hasNext()){
            Annotation detail = (Annotation) iter.next();
            //logger.info(detail.toString());
            String property = (String) detail.getProperty("property");
            String detailstr   = (String) detail.getProperty("detail");
            
            if (  property.equals("molecule description")){
                // molecule corresponds to chain...
                // pdbcode is with chain 
                // this is dealt with in createChain method...
            } else {
                header.put(property,detailstr);
            }
        }
        spice_pdb.setHeader(header);
        
        // debug:
        /*List chains = spice_pdb.getChains(0);
         
         Iterator itera = chains.iterator();
         while (itera.hasNext()){
         Chain chain = (Chain) itera.next();
         Annotation a = chain.getAnnotation();
         logger.info(chain.toString());
         logger.info("found annotation " + a);
         }
         */
        return spice_pdb ;
    }
    
    /** map from one segment to the other and store this info in chain  */
    private  void mapSegments(Annotation block, 
            Chain chain,
            Annotation seq_obj, 
            Annotation stru_obj) 
    throws DASException
    {
        logger.finest("mapSegment");
        logger.finest(block.toString());
        // order segmetns
        // HashMap 0 = refers to seq
        // hashMap 1 = refers to struct
        ArrayList segments = (ArrayList)block.getProperty("segments");
        logger.finest("segments size: "+ segments.size());
        Annotation[] arr = new Annotation[segments.size()] ;
        
        String seq_id  = (String) seq_obj.getProperty("intObjectId");
        String stru_id = (String)stru_obj.getProperty("intObjectId");
        
        if ( segments.size() <2) {
            logger.finest("<2 segments in block. skipping");
            return ;
        }
        
        if ( ! seq_id.equals(chain.getSwissprotId() )){
            logger.fine("chain - swissprot does not match Alignment swissprot! can not map segments");
            return;
        }
        
        boolean seqOK = false;
        boolean strucOK = false;
        for ( int s =0; s< segments.size() ; s++) {
            Annotation segment = (Annotation) segments.get(s) ;
            logger.finest(" testing segment " +segment.toString());
            String obid = (String) segment.getProperty("intObjectId");
            if ( obid.equals(seq_id)) {
                logger.finest("got seq object in block");
                arr[0] = segment;
                seqOK = true;
            }
            if ( obid.equals(stru_id)) {		    
                logger.finest("got struc object in block");
                arr[1] = segment;
                strucOK = true;
            }
            
            // if there are other alignments, do not consider them ...
        }
        
        
        if ( ! seqOK ){
            logger.info("problem with alignment sequence " + seq_id + " not found in segments.");
            return;
            
        }
        if ( ! strucOK ){
            logger.info("problem with alignment structure " + stru_id + " not found in segment.");
            return;
        }
        
        // here is the location where the cigar string shouldbe parsed, if there is any
        // for the moment, nope..... !!!
        
        // coordinate system for sequence is always numeric and linear
        // -> phew!
        
        logger.finest(" seq segment: " + arr[0].toString());
        int seqstart = Integer.parseInt((String)arr[0].getProperty("start")); 
        int seqend   = Integer.parseInt((String)arr[0].getProperty("end"));
        
        // size of the segment
        
        int segsize = seqend-seqstart + 1 ;
        
        
        // now build up a segment of aminoacids 
        AminoAcid[] aminosegment = new AminoAcid[segsize]  ;
        int chainlength = chain.getLength();
        //if ( seqend >= chainlength ){
            //logger.warning("warning: potential version conflict: coordinate of alignment ("+seqend+") and sequence length ("+chain.getLength()+"( does not match!");
            //return;
        //}
        for ( int i = 0 ; i < segsize ; i++) {

            int pos = i+seqstart-1;
            if ( pos >= chainlength){
                logger.finest("i "+i + "chainlength "+ chainlength + " segsize " + segsize);
                logger.finest("requesting wrong coordinate - " + pos + " is larger than chainlength " + chainlength);
                break;
            }
            aminosegment[i] = (AminoAcid) chain.getGroup(i+seqstart-1);
        }
        
        
        // map segment of aminoacids to structure...
        int strustart = 0 ;
        //int struend   = 0 ;
        try {
            strustart = Integer.parseInt((String)arr[1].getProperty("start"));
            //struend   = Integer.parseInt((String)arr[1].getProperty("end"));
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
        logger.finest("addChainAlignment");
        
        // go over all blocks of alignment and join pdb info ...
        Annotation seq_object   = getAlignmentObject(ali,SEQUENCEDATABASE );
        
        Annotation stru_object  = getAlignmentObject(ali,STRUCTUREDATABASE);
        
        logger.finest(seq_object.getProperty("intObjectId").toString());
        logger.finest(stru_object.getProperty("intObjectId").toString());
        
        //Simple_AminoAcid_Map current_amino = null ;
        
        Annotation[] blocks = ali.getBlocks() ;
        for ( int i =0; i < blocks.length;i++) {
            Annotation block = blocks[i] ;
            
            mapSegments(block,chain,seq_object,stru_object); 
        }
        
        return chain ;
    }
    
    /* retrieve a chain with a particular chainid from the structure 
    private static Chain findChain(Structure struc, String chainid) throws StructureException{
        
        List chains = struc.getChains(0);
        
        Iterator iter = chains.iterator();
        while (iter.hasNext()){
            Chain chain = (Chain) iter.next();
            String tmpid = chain.getName();
            if ( tmpid.equals(chainid)){
                return chain;
            }
        }
        throw new StructureException("no chain with chainid >" + chainid + "< found in structure");
    }
    */
    private String getChainIdFromPDBCode(String pdbcode) {
        //logger.finest("DASAlignment_Handler: getChainFromPDBCode" + pdbcode);
        String[] spl = pdbcode.split("\\.");
        String chain = spl[1] ;
        //logger.finest("DASAlignment_Handler: getChainFromPDBCode chain:" + chain);
        return chain ;
    }
    
    
    public Chain getChainFromSequence(String sequence){
        
        Chain chain =  new ChainImpl();
        //chain.setSwissprotId(id);
        
        for ( int pos = 0 ; pos < sequence.length() ; pos ++ ){
            AminoAcidImpl s_amino = new AminoAcidImpl();
            
            Character c = new Character(sequence.charAt(pos)) ;
            s_amino.setAminoType(c) ;
            String code1 = c.toString() ;
            String code3 = "" ;
            try {
                code3 = convert_1code_3code(code1);
            } catch (IllegalSymbolException e) {
                //e.printStackTrace();
                code3 = "XXX" ;		
            }
            try {
                s_amino.setPDBName(code3) ;		
            } catch ( PDBParseException e ) {
                e.printStackTrace() ;
                
            }
            chain.addGroup(s_amino) ;	    
            
            
        }
        return chain;
    }
    
    /** create a chain that corrresponds to a sequence */
    public Chain  createChain(Alignment ali, String sequence) 
    throws DASException
    {
        Annotation object     = getAlignmentObject(ali,SEQUENCEDATABASE);
        Annotation struobject = getAlignmentObject(ali,STRUCTUREDATABASE);
        String swissp_id = (String) object.getProperty("dbAccessionId") ;
        //logger.finest(swissp_id);
        //logger.finest(sequence);
        String chainname = (String) struobject.getProperty("dbAccessionId");
        
        Chain chain = getChainFromSequence(sequence) ;
        chain.setSwissprotId(swissp_id);
        chain.setName(getChainIdFromPDBCode(chainname));
        
        //logger.finest(chain);
        Chain retchain = addChainAlignment(chain,ali);
        //logger.finest(retchain);
        
        
        // add annotation to new chain
        List details = (List) struobject.getProperty("details");
        Iterator iter = details.iterator();
        
        while (iter.hasNext()){
            Annotation detail = (Annotation) iter.next();
            //logger.info(detail.toString());
            String property = (String) detail.getProperty("property");
            String detailstr   = (String) detail.getProperty("detail");
            
            if (  property.equals("molecule description")){
                // molecule corresponds to chain...
                // pdbcode is with chain 
                
                Annotation anno = retchain.getAnnotation();
                if (( anno == Annotation.EMPTY_ANNOTATION ) 
                        || (anno == null )) {
                    HashMap m = new HashMap();
                    m.put("description",detailstr);
                    
                    anno = AnnotationFactory.makeAnnotation(m);
                    
                }  else {
                    try {
                        anno.setProperty("description",  detailstr);
                    }
                    catch (ChangeVetoException e){
                        e.printStackTrace();
                    }
                }
                retchain.setAnnotation(anno);
                //logger.info("StructureBuilder createChain setting chain description "+ anno);
                
            } 
        }
        
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
    
    public Annotation getAlignmentObject (Alignment ali,String objecttype) 
    throws DASException
    {
        // go through objects and get sequence one ...
        Annotation[] objects = ali.getObjects();
        //HashMap seq_obj = new HashMap() ;
        
        for (int i =0 ; i<objects.length;i++) {
            Annotation object = objects[i];
            String dbCoordSys = (String)object.getProperty("dbCoordSys");
            
            
            if ( dbCoordSys.equals(objecttype) ) {		
                return object ;
            }
            
            /** TODO: fix this */
            // tmp fix until Alignment server returns the same coordsystems as the registry contains ... :-/
            if ( objecttype.equals(SEQUENCEDATABASE)){
                if ( dbCoordSys.equals("UniProt"))
                    return object;
            }
            if ( objecttype.equals(STRUCTUREDATABASE)){
                if ( dbCoordSys.equals("PDBresnum"))
                    return object;
            }
        }
        
        
        throw new DASException("no >" + objecttype + "< object found as dbSource in alignment!");
        
    }
    
    
    /////////////////////////////////////////////////////////////////
    ////// this section for joining of the two structures
    /////////////////////////////////////////////////////////////////
    
    
    /** joins the empty (= no 3D information) structure created from the uninprto sequences 
     * with the structure retrieved from PDB. 
     */
    
    public Structure joinStructures(Structure spice_structure, Structure pdb_structure) 
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
                    logger.finest("found matching chain " + mapped_chain.getName() + 
                            " " +  mapped_chain.getSwissprotId()+" " + chainName + " " + c.getSwissprotId());
                    
                } catch (DASException e) {
                    // could be a chain that does not contain any amino acids ...
                    // so the chain seems to be completely missing. Add it as a whole ...
                    logger.finest("no matching chain found for chain: " + chainName);
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
        Map pdbheader = pdb_structure.getHeader();
        Map sp_header = spice_structure.getHeader();
        
        // join the two maps...
        Map newheader = new HashMap();
        Set pdbset = pdbheader.keySet();
        Iterator iter = pdbset.iterator();
        while (iter.hasNext()){
            String pdbkey = (String) iter.next();
            newheader.put(pdbkey,pdbheader.get(pdbkey));
        }
        
        Set spset = sp_header.keySet();
        Iterator siter = spset.iterator();
        while (siter.hasNext()){
            String skey = (String) siter.next();
            newheader.put(skey,sp_header.get(skey));
        }
        
        spice_structure.setHeader(newheader);
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
