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
package org.biojava.spice.das;


import org.biojava.spice.config.*;
import org.biojava.spice.*       ;

import org.biojava.bio.Annotation;
import org.biojava.bio.program.das.dasalignment.*      ;
import org.biojava.bio.structure.Structure ;
import org.biojava.bio.structure.StructureImpl ;
import org.biojava.bio.structure.Chain ;
import org.biojava.dasobert.das.SpiceStructureFeeder;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;


import java.io.*								;
import java.util.Iterator 						;
import java.util.Calendar                      	;
import java.util.List;
import java.util.Map;
import java.util.logging.*                     	;




/** a class that connects to a DAS structure service and retreives a
 * structure.
 * @author Andreas Prlic
 */
public class DAS_Feeder 
implements SpiceStructureFeeder 
{
    
    /* make connectin to a DAS structure service and 
     get back structure
     */
    //Structure pdb_container ; // used for alignment
    Structure pdb_structure ; // retreived from DAS structure requres
    
    String dasalignmentcommand ;
    
    boolean structureDone ;
    RegistryConfiguration config ;
    Logger logger        ;
    
    
    public DAS_Feeder( RegistryConfiguration configuration) {
        logger = Logger.getLogger("org.biojava.spice");
        
        config = configuration ;
        
        
        //pdb_container =  new StructureImpl();
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
    
    public DasCoordinateSystem getStructureCoordSys(){
        
        String cs = SpiceDefaults.PDBCOORDSYS;
        return DasCoordinateSystem.fromString(cs);
    }
    
    public DasCoordinateSystem getUniProtCoordSys(){
        String cs = SpiceDefaults.UNIPROTCOORDSYS;
        return DasCoordinateSystem.fromString(cs);
    }
    
    public synchronized Structure loadPDB(String pdbcode)
    throws FileNotFoundException, IOException {
        Structure struc = new StructureImpl();
        AlignmentTools aligTools = new AlignmentTools(config);
        logger.finest (" in DAS_Feeder - loadPDB");
        
        DASStructure_Handler structure_handler = new DASStructure_Handler(config,pdbcode,this);
        structure_handler.start();
        
        Alignment[] alignments = aligTools.getAlignments(pdbcode);
        boolean noAlignmentFound = false;
        if ( alignments == null ) {
            // do rescue - return structure
            noAlignmentFound = true;
        }
        
        // o.k. we are here if we found alignments
        boolean done = false;
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
        
        logger.finest("DAS_Feeder (loadPDB) got sequence and structure");
        logger.finest(pdb_structure.toString());
        pdb_structure.setPDBCode(pdbcode);
        if ( noAlignmentFound ) {
            logger.finest("no alignment found - returning pdb structue");
            notifyAll();
            return pdb_structure;
        }
        
        Structure emptyStruc = createEmptyStructure(alignments);
        //logger.info("finished creating empty structure");
        StructureBuilder sbuilder = new StructureBuilder(getUniProtCoordSys(),getStructureCoordSys());
        struc = sbuilder.joinStructures(emptyStruc,pdb_structure);
        
        // add alignment annotation data
        if ( alignments.length > 0 ){
            Alignment ali = alignments[0];
            
            String pdbchain = AlignmentTools.getPDBCodeFromAlignment(ali);
            Annotation object = AlignmentTools.getObject(pdbchain,alignments[0]);
            List details = (List) object.getProperty("details");
            //logger.info("displaying alignment details:");
            
            Iterator iter = details.iterator();
            Map header = struc.getHeader();
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
            struc.setHeader(header);
        }
        
        
        
        // build up an empty (=no 3d info) based on the sequences.
        notifyAll();
        return struc;
    }
    
    /** create an "empty" = no 3D info from the sequences in the alignment*/
    private Structure createEmptyStructure(Alignment[] alignments){
        StructureImpl struc = new StructureImpl();
        StructureBuilder sbuilder = new StructureBuilder(getUniProtCoordSys(),getStructureCoordSys());
        
        for ( int i = 0; i < alignments.length; i++){
            Alignment ali = alignments[i];
            // get uniprot code from a;ignment
            String uniprotcode = AlignmentTools.getUniProtCodeFromAlignment(ali);
            logger.finest("creating empty chain for "+ uniprotcode);
            String sequence = getSequence(uniprotcode);
            if (sequence == null ) {
                continue;
            }
            try {
                Chain c = sbuilder.createChain(ali,sequence);
                struc.addChain(c);
            } catch (DASException e){
                e.printStackTrace();
            }
        }
        return struc;
    }
    
    public synchronized Structure loadUniProt(String uniprotcode)
    throws FileNotFoundException, IOException {
        Structure struc = new StructureImpl();
        AlignmentTools aligTools = new AlignmentTools(config);
        try {
            
            logger.finest("in DAS_Feeder UniProt... " + uniprotcode);
            
            // get matching pdb codes
            // by making DAS_Alignment request
            
            Alignment[] alignments = aligTools.getAlignments(uniprotcode);
            
            
            // Problem: No alignment found
            if ( (alignments == null ) || ( alignments.length == 0)) {
                // aargh catch exception ...	
                logger.warning("could not retreive any UniProt-PDB alignment from DAS servers");
                String sequence = getSequence(uniprotcode);
                if (sequence == null ) {
                    notifyAll();
                    return struc ;
                }
                struc = makeStructureFromSequence(uniprotcode,sequence);
                notifyAll();
                return struc ;
            }
            
            // we only take first PDB code we find ...
            String pdbcode = null ;
            Alignment ali  = null ;
            
            for ( int i = 0 ; i< alignments.length ; i++ ) {
                
                ali = alignments[i];
                pdbcode = AlignmentTools.getPDBCodeFromAlignment(ali);
                if ( pdbcode.equals("null"))
                    continue ;
                if ( pdbcode != null )
                    break ;
                
            }
            
            
            // Problem: no Structure found
            if (pdbcode == null ) {
                /// argh catch exception ...
                logger.warning("could not find pdb code in alignment");
                // return "empty" structure...
                // get sequence
                String sequence = getSequence(uniprotcode);
                if (sequence == null ) {
                    notifyAll();
                    return struc ;
                }
                struc = makeStructureFromSequence(uniprotcode,sequence);
                notifyAll();
                return struc;
            }
            
            // NEW: build chain for first PDB file...
            
            logger.finest("found alignment with " +pdbcode);
            // remove chain from code :
            String[] spl = pdbcode.split("\\.");
            if ( spl.length > 1 ) 
                pdbcode = spl[0];
            logger.finest("pdbcode now " +pdbcode);
            // get structure / sequence in parallel
            
            // hm: that would be good, but we need to find a way to "enforce" this uniprot...
            // return loadPDB(pdbcode);
            
            
            DASStructure_Handler structure_handler = new DASStructure_Handler(config,pdbcode,this);
            structure_handler.start();
            
            // wait for threads to be finished ..
            boolean done           = false ;
            
            String sequence = getSequence(uniprotcode);
            
            
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
            logger.finest("DAS_Feeder got sequence and structure");
            logger.finest(pdb_structure.toString());
            pdb_structure.setPDBCode(pdbcode);
            
            // Problem, found structure and alignment, but no sequence
            if (sequence == null ) {
                logger.warning("no sequence found, using PDB structure");
                notifyAll();
                return pdb_structure ;
            }
            
            
            // join the three bits 
            StructureBuilder strucbuilder = new StructureBuilder(getUniProtCoordSys(),getStructureCoordSys());
            //Alignment[] aliarr = new Alignment[1];
            //aliarr[0] = ali;
            struc = strucbuilder.createSpiceStructure(ali, pdb_structure, sequence);
            struc.setPDBCode(pdbcode);
            
            logger.finest("joining of data finished " +getTimeStamp() );
            logger.finest(struc.toString());
            
            //java.util.List chains = pdb_container.getChains(0);
            //for ( int i =0;i<chains.size();i++){
            //logger.finest("Displaying chain: " + i);
            //Chain c = (Chain)chains.get(i);
            //logger.finest(c);
            //}
            //pdb_container = dasali.getPDBContainer() ;
            
            
            //return struc ;
            
            
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
        notifyAll();
        return struc ;
        
    }
    
    /** "emergency" procedure to create an "empty" Structure, which represents the sequence to be displayed in SPICE */
    private Structure makeStructureFromSequence(String id,String sequence ) {
        logger.finest("makeStructureFromSequence for " +id);
        StructureBuilder sbuilder = new StructureBuilder(getUniProtCoordSys(),getStructureCoordSys());
        Chain chain =  sbuilder.getChainFromSequence(sequence);
        chain.setSwissprotId(id);
        StructureImpl struc = new StructureImpl();
        struc.addChain(chain);
        //struc.setSwissProtID(id);
        return struc;
        
    }
    
    /** do DAS communication to get sequence */
    private String getSequence(String uniprotcode) {
        logger.finest("loading sequence " + uniprotcode);
        String sequence = null ;
        DAS_SequenceRetrieve seq_das = new DAS_SequenceRetrieve(config) ;
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
        logger.finest("setting structure in DAS_Feeder");
        pdb_structure = struc ;
        notifyAll();
    }
    
    //public Structure getStructure(){	
      //  return pdb_container ;
    //}
    
    
    /*
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
    */
    
    /* find the group that has PDBCode pdbcode in Chain mapped_chain 
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
    */
}
