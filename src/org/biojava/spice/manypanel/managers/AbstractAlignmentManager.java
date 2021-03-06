/*
 *                  BioJava development code
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
 * Created on Jan 9, 2006
 *
 */
package org.biojava.spice.manypanel.managers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.bio.Annotation;
import org.biojava.bio.program.das.dasalignment.Alignment;
import org.biojava.bio.program.das.dasalignment.DASException;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;
import org.biojava.dasobert.eventmodel.AlignmentEvent;
import org.biojava.dasobert.eventmodel.AlignmentListener;
import org.biojava.spice.config.SpiceDefaults;
import org.biojava.spice.manypanel.AlignmentTool;

public class AbstractAlignmentManager
implements AlignmentListener{

    DasCoordinateSystem coordSys1;
    DasCoordinateSystem coordSys2;
    Chain sequence1;
    Chain sequence2;
    
    int seqLength1;
    int seqLength2;
    
    Map alignmentMap1;
    Map alignmentMap2;
    
    Alignment alignment;
    
    protected static int SEARCH_DIRECTION_INCREASE = 1;
    protected static int SEARCH_DIRECTION_DECREASE = 2;
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    /** find the position in the other sequence closest to pos2
     * 
     * @param pos2
     * @param searchdirection
     * @return closest position to pos2
     */
    protected int getNextPosition1(int pos2, int searchdirection, int stop){
        
        int i = pos2;
        
       // logger.info("searching for next position1 of " + i + " " + searchdirection + " " + stop + "length " + sequence2.getLength());
          
        
        // test the max and min boundaries
        if ( pos2 > seqLength2)
            if ( ( searchdirection == SEARCH_DIRECTION_DECREASE) &&
                    ( stop < seqLength2))
                i = seqLength2-1;
        
        if ( pos2 < 0 )
            if ( ( searchdirection == SEARCH_DIRECTION_INCREASE) &&
                ( stop < seqLength2) )
                i =0;
        
        if ( stop > seqLength2){
            if ( (searchdirection == SEARCH_DIRECTION_DECREASE))
                stop = seqLength2-1;
        }
        if ( stop < 0) {
            if (    ( searchdirection == SEARCH_DIRECTION_INCREASE) &&
                    ( pos2 > -1 )    ) 
                stop = 0;
        }
        
        
        while ( (i >=-1 ) && ( i < seqLength2)){
            Integer p = new Integer(i);
            //logger.info("testing " + p);
            if ( alignmentMap2.containsKey(p)){
                //System.out.println(alignmentMap2.get(p));
                Integer spos1 = (Integer) alignmentMap2.get(p);
                //logger.info("found " + p + " " + spos1);
                return spos1.intValue();
            }
            
            if ( searchdirection == SEARCH_DIRECTION_INCREASE) {
                i += 1;
                if ( i> stop+1)
                    break;
            }
            else {
                i -= 1;
                if ( i < stop-1)
                    break;
            }
        }
        
        //logger.info("i " + i + " pos2 " + pos2 + " stop " + stop + " direction " + searchdirection);
       
        if ( i >= seqLength2)
            return seqLength1-1;
            
        if ( i < 0)
            return -1;
        
        int newstop = 0;
        if (( searchdirection == SEARCH_DIRECTION_INCREASE) )
                newstop = seqLength2-1;
        
        
        int afterpos = getNextPosition1(i,searchdirection,newstop);
       // logger.info("afterpos 1 " +afterpos);
        if ( afterpos > 0 ) {     
           return afterpos;
            
        }
        return -1;
  
    }
    
    /** find the position in the other sequence closest to pos1
     * 
     * @param pos1
     * @param searchdirection
     * @return closest position to pos1
     */
    protected int getNextPosition2(int pos1, int searchdirection, int stop){
        int i = pos1;
        //logger.info("get next position 2 " + pos1 + " " + searchdirection + " " + stop);
        if ( pos1 > seqLength1)
            if ( ( searchdirection == SEARCH_DIRECTION_DECREASE) &&
                    ( stop < seqLength1))
                i = seqLength1-1;
        if ( pos1 < 0 )
            if ( ( searchdirection == SEARCH_DIRECTION_INCREASE) &&
                ( stop < seqLength1) )
                i =0;
        
        if ( stop > seqLength1){
            if ( (searchdirection == SEARCH_DIRECTION_DECREASE))
                stop = seqLength1-1;
        }
        if ( stop < 0) {
            if (    ( searchdirection == SEARCH_DIRECTION_INCREASE) &&
                    ( pos1 > -1 )    ) 
                stop = 0;
        }
        while ( (i >=-1 ) && ( i < seqLength1)){ 
            Integer p = new Integer(i);
            //logger.info("testing " + p);
            
            if ( alignmentMap1.containsKey(p)){
                //System.out.println(alignmentMap2.get(p));
                Integer spos2 = (Integer) alignmentMap1.get(p);
                //logger.info("found " + p);
                return spos2.intValue();
            }
            
            if ( searchdirection == SEARCH_DIRECTION_INCREASE) {
                i += 1;
                if ( i > stop+1)
                    break;
            }
            else {
                i -= 1;
                if ( i < stop-1)
                    break;
            }
        }
        
       //logger.info("i " + i + " pos1 " + pos1 + " stop " + stop + " direction " + 
       //        searchdirection + " l " + seqLength1);
        
        if ( i >= seqLength1)
            return seqLength2-1;
        if ( i < 0)
            return -1;
        int newstop = 0;
        if (( searchdirection == SEARCH_DIRECTION_INCREASE))
                newstop = seqLength1-1;
            
        int afterpos = getNextPosition2(i,searchdirection,newstop);
        //logger.info("afterpos 2 " + afterpos);
        if ( afterpos > 0 ) {
           return afterpos;
        }
        else
            return -1;
        
       }
    
    public void newAlignment(AlignmentEvent event){
        synchronized (alignment) {
            alignment = event.getAlignment();
        }
       
    }
    public void clearAlignment(){
        alignment = new Alignment();
        
        alignmentMap1 = new HashMap();
        alignmentMap2 = new HashMap();
        
        sequence1 = new ChainImpl();
        sequence2 = new ChainImpl();
           
    }
    
    
    protected  void storeAlignment(Alignment ali) 
    throws DASException
    {
        //System.out.println("storing alignment");
       //      logger.info("storing alignment " + ali);
        
        // go over all blocks of alignment and join pdb info ...
        Annotation seq_object   = getAlignmentObject(ali,coordSys1.toString() );
        
        Annotation stru_object  = getAlignmentObject(ali,coordSys2.toString());
        
        
        /*
        String obj1Id = seq_object. getProperty("dbAccessionId").toString();
        
        if ( coordSys1.toString().equals(SpiceDefaults.PDBCOORDSYS)){
            obj1Id = seq_object. getProperty("intObjectId").toString();
        }
        String obj2Id = stru_object.getProperty("dbAccessionId").toString();
        */
        String obj1Id = seq_object. getProperty("intObjectId").toString();
        String obj2Id = stru_object.getProperty("intObjectId").toString();
        
        
       // logger.info("storing alignment " + obj1Id + " " + obj2Id);
        
        //Simple_AminoAcid_Map current_amino = null ;
        List aligMap1 = AlignmentTool.createAlignmentTable(ali,obj1Id);
        List aligMap2 = AlignmentTool.createAlignmentTable(ali,obj2Id);
               
      
        // the two maps MUST have the same size!
        //if ( aligMap1.size() != aligMap2.size()){
        //    logger.warning("can ")
        //    return
        //}
                       
        // resolve the alignment maps
        alignmentMap1 = new HashMap();
        alignmentMap2 = new HashMap();
        
        for ( int pos = 0 ; pos < aligMap1.size(); pos++ ) {
            Map m1 = (Map) aligMap1.get(pos);
            Map m2 = (Map) aligMap2.get(pos);
            //System.out.println("1:"+m1);
            //System.out.println("2:"+m2);
            Object s1 =  m1.get("seqpos");
            Object s2 =  m2.get("seqpos");
            //logger.info("s1 " + s1 + " s2" + s2);
            if (( s1 == null ) || (s2 == null)) {
                // this sequence position is not aligned ...
                continue;
            }
            
            //if ( coordSys1.toString().equals(SpiceDefaults.PDBCOORDSYS)) {
            //logger.info(coordSys1.getCategory() + " " + s1 + " " + s2);
            
            if ( coordSys1.getCategory().equals("Protein Structure")){
                int snew = getSeqPosForPDB(sequence1, s1.toString());
                s1 = new Integer(snew);
            }
            if ( coordSys2.getCategory().equals("Protein Structure")){            
            //if ( coordSys2.toString().equals(SpiceDefaults.PDBCOORDSYS)) {
                
                int snew = getSeqPosForPDB(sequence2, s2.toString());
                s2 = new Integer(snew);
            }
            
                       
            alignmentMap1.put(s1,s2);
            alignmentMap2.put(s2,s1);
            
        }
      
    }
    private int getSeqPosForPDB(Chain seq, String pdbPos){
            if ( seq == null) {
                logger.warning("seq == null");
                return -1;
            }
            Chain c = seq;
            
            List groups = c.getGroups();
            
            // now iterate over all groups in this chain.
            // in order to find the amino acid that has this pdbRenum.               
            
            Iterator giter = groups.iterator();
            int i = 0;
            while (giter.hasNext()){
                i++;
            
                Group g = (Group) giter.next();
                String rnum = g.getPDBCode();
                
                if ( rnum == null) {
                	//System.out.println(c.getName() + " " + g);
                    continue;
                }
                if ( rnum.equals(pdbPos)) {
                    //System.out.println(i + " = " + rnum);
                    return i;
                }
            }
            //if (logger.isLoggable(Level.FINEST)){
            	//logger.warning("could not map pdb pos " + pdbPos + " to sequence!");
            //}
            return -1;
    }
    
    
    public void noAlignmentFound(AlignmentEvent event){
        alignment = event.getAlignment();
    }
    
    // get the position of the second object ...
    protected int getPosition2(int pos1){
        int pos2 = -1;
        //TODO add support for insertion codes...
        try {
            //System.out.println(alignmentMap1);
            Integer p = new Integer(pos1);
            if ( alignmentMap1.containsKey(p)){
                //System.out.println(alignmentMap1.get(p));
                Integer spos2 = (Integer) alignmentMap1.get(p);
                pos2 = spos2.intValue() ;
            }
        } catch (Exception e){ e.printStackTrace();}
            
        return pos2;
        
    }
    
    protected int getPosition1(int pos2){
        
        int pos1 = -1;
        try {
           //System.out.println(alignmentMap2);
            Integer p = new Integer(pos2);
            if ( alignmentMap2.containsKey(p)){
                //System.out.println(alignmentMap2.get(p));
                Integer spos1 = (Integer) alignmentMap2.get(p);
                pos1 = spos1.intValue() ;
            }
        } catch (Exception e){}
        return pos1;
        
    }

    private Annotation getAlignmentObject (Alignment ali,String objecttype) 
    throws DASException
    {
        // go through objects and get sequence one ...
        Annotation[] objects = ali.getObjects();
        //HashMap seq_obj = new HashMap() ;
        
        for (int i =0 ; i<objects.length;i++) {
            Annotation object = objects[i];
            String dbCoordSys = (String)object.getProperty("dbCoordSys");
            
            
            if ( dbCoordSys.equalsIgnoreCase(objecttype) ) {      
                return object ;
            }
            
            if (logger.isLoggable(Level.FINEST)){
            	logger.finest("otype: " + objecttype + " cs:" + dbCoordSys);
            }
            
            /** TODO: fix this */
            // tmp fix until Alignment server returns the same coordsystems as the registry contains ... :-/
            if ( objecttype.equals(SpiceDefaults.UNIPROTCOORDSYS )){
                if ( dbCoordSys.equals("UniProt") || dbCoordSys.equals(SpiceDefaults.UNIPROTCOORDSYS))
                    return object;
            }
            if ( objecttype.equals(SpiceDefaults.PDBCOORDSYS)){
                if ( dbCoordSys.equals("PDBresnum"))
                    return object;
            }
            if ( objecttype.equals(SpiceDefaults.MODELCOORDSYS)){
                if ( dbCoordSys.equals("MODEL") || dbCoordSys.equals(SpiceDefaults.MODELCOORDSYS))
                    return object;
            }
            
            if ( objecttype.equals(SpiceDefaults.ENSPCOORDSYS)){
                if ( dbCoordSys.equals("ENSEMBLPEP"))
                    return object;
            }
            
            if ( objecttype.equalsIgnoreCase("Gencode_2.2,Protein Sequence")){
                if (dbCoordSys.equalsIgnoreCase("Gencode_2.2,Protein Sequence,Homo sapiens"))
                    return object;
            }
        }      
        
        throw new DASException("no >" + objecttype + "< object found as dbSource in alignment!");        
    }
    
    

}
