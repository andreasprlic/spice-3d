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
 * Created on Oct 28, 2005
 *
 */
package org.biojava.spice.manypanel.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.Iterator;


import org.biojava.spice.config.SpiceDefaults;
import org.biojava.spice.feature.Feature;
import org.biojava.spice.feature.FeatureImpl;
import org.biojava.spice.feature.Segment;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureEvent;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureListener;
import org.biojava.spice.manypanel.renderer.AlignmentRenderer;
import org.biojava.spice.utils.UniProtAccessionCodeTools;
import org.biojava.bio.Annotation;
import org.biojava.bio.program.das.dasalignment.Alignment;
import org.biojava.bio.program.das.dasalignment.DASException;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Structure;
import org.biojava.dasobert.das.AlignmentParameters;
import org.biojava.dasobert.das.AlignmentThread;
import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.dasregistry.*;
import org.biojava.dasobert.eventmodel.*;


public class AlignmentManager 
extends AbstractAlignmentManager 
implements StructureListener{
    
     private static String ASSEMBLYNAME = "ensemblpep-human-ncbi36";
    
   
    SpiceDasSource[] refservers1;
    SpiceDasSource[] refservers2;
    SpiceDasSource[] alignmentServers;
    AlignmentSequenceListener object1Listener;
    AlignmentSequenceListener object2Listener;
    List sequence1Listeners;
    List sequence2Listeners;
    
    List object1Listeners;
    List object2Listeners;
    List alignmentRenderers;
    
   List alignmentListeners;
        
    String object1Id;
    String object2Id;
 
    List seq1FeatureListener;
    List seq2FeatureListener;
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    String panelName;
    
    MyFeatureTranslator translator1;
    MyFeatureTranslator translator2;
   
    
    public AlignmentManager(String panelName,DasCoordinateSystem coordSys1, DasCoordinateSystem coordSys2){
        this.panelName = panelName;
        
        this.coordSys1 = coordSys1;
        this.coordSys2 = coordSys2;
        clearDasSources();
   
        
        
        object1Listener = new AlignmentSequenceListener(this,1);
        object2Listener = new AlignmentSequenceListener(this,2);
        
        clearAlignmentRenderers();
        
        clearAlignment();
        
        sequence1 =  new ChainImpl();
        sequence2 = new ChainImpl();
        object1Id = "";
        object2Id = "";
        seq1FeatureListener = new ArrayList();
        seq2FeatureListener = new ArrayList();
        
        clearSequenceListeners();
        
        clearObjectListeners();
        
        alignmentListeners = new ArrayList();
   
        translator1 = new MyFeatureTranslator(1,this);
        translator2 = new MyFeatureTranslator(2,this);
    }
    
    public SpiceFeatureListener getFeatureTranslator1(){
        return translator1;
    }
    
    public SpiceFeatureListener getFeatureTranslator2(){
        return translator2;
    }
    
    public void addAlignmentListener(AlignmentListener li){
        alignmentListeners.add(li);
    }
    
    public Chain getSequence1(){
        //logger.info(" stored seq1:" + sequence1.toString());
        return sequence1;
    }
    
    public Chain getSequence2(){
        //logger.info(" stored seq2:" +sequence2.toString());
        return sequence2;
    }
    public String getId1(){
        return object1Id;
    }
    
    public String getId2(){
        return object2Id;
    }
    
    public DasCoordinateSystem getCoordSys1(){
        return coordSys1;
    }
    
    public DasCoordinateSystem getCoordSys2(){
        return coordSys2;
    }
    
    public void clearDasSources(){
        alignmentServers = new SpiceDasSource[0];
    }
    
    public void clearObjectListeners(){
        object1Listeners = new ArrayList();
        object2Listeners = new ArrayList();
    }
    
    public void clearSequenceListeners() {
        sequence1Listeners = new ArrayList();
        sequence2Listeners = new ArrayList();
        
    }
    
    public void clearAlignmentRenderers(){
        alignmentRenderers = new ArrayList();
    }
    
    public void addAlignmentRenderer(AlignmentRenderer re){
        alignmentRenderers.add(re);   
    }
    
    public void addSequence1Listener(SequenceListener one) {
        sequence1Listeners.add(one);
    }
    
    public void addSequence2Listener(SequenceListener two){
        sequence2Listeners.add(two);
    }
    
    public void addObject1Listener(ObjectListener li){
        object1Listeners.add(li);
    }
    public void addObject2Listener(ObjectListener li){
        object2Listeners.add(li);
    }
    public void setCoordSys1(DasCoordinateSystem coordSys){
        this.coordSys1 = coordSys;
    }
    public void setCoordSys2(DasCoordinateSystem coordSys) {
        this.coordSys2 = coordSys; 
    }
    
    public void setReferenceServers1(DasSource[] refservers){
        
        refservers1 = new SpiceDasSource[refservers.length];
        for ( int i = 0 ; i< refservers.length; i++){
            refservers1[i] =  SpiceDasSource.fromDasSource(refservers[i]);
        }
        
    }
    public void setReferenceServers2(DasSource[] refservers){
        
        refservers2 = new SpiceDasSource[refservers.length];
        for ( int i = 0 ; i< refservers.length; i++){
            refservers2[i] =  SpiceDasSource.fromDasSource(refservers[i]);
        }
        
    }
    
    
    public SpiceDasSource[] getAlignmentServers(){
        return alignmentServers;
    }
    
    public void setAlignmentServers(SpiceDasSource[] servers){
        logger.info(panelName+ " got "+servers.length+"  alignmentservers");
        alignmentServers = servers;
    }
    public SequenceListener getSeq1Listener(){
        return object1Listener ;
    }
    
    
    public SequenceListener getSeq2Listener(){
        return object2Listener ;
    }
    
    /** no alignment could be found for this object ...
     * 
     */
    public void noAlignmentFound(AlignmentEvent event){
        //logger.info("no alignment found!");
        Annotation[] os = event.getAlignment().getObjects();
        if ( os.length < 2){
            // something strange is going on here..
            // perhaps a crazy server
            //logger.warning(panelName+" got  alignment of wrong # objects...");
            return;
        }
        alignment = event.getAlignment();
        
        Annotation a1 = os[0];
        Annotation a2 = os[1];
        String cs1 = (String) a1.getProperty("dbCoordSys");
        String cs2 = (String) a2.getProperty("dbCoordSys");
        String ac1 =  uniProtProtect( (String) a1.getProperty("dbAccessionId"),cs1);
        String ac2 =  uniProtProtect( (String) a2.getProperty("dbAccessionId"),cs2);
        
        //logger.info("no alignment for " + ac1 + " " + ac2);
        if ( ac1.equalsIgnoreCase(object1Id)) {
            triggerNoObject2(ac2 );
        }
        if ( ac1.equalsIgnoreCase(object2Id)){
            triggerNoObject1(ac2);
        }
        if ( ac2.equalsIgnoreCase(object1Id))
            triggerNoObject1(ac1);
        if ( ac2.equalsIgnoreCase(object2Id))
            triggerNoObject2(ac1);
        
    }
    
    private String uniProtProtect(String ac, String csString){
        ac = ac.toLowerCase();
        //System.out.println("1: " + csString);
        if  ( csString.equalsIgnoreCase(SpiceDefaults.UNIPROTCOORDSYS)){
            if ( UniProtAccessionCodeTools.isEntryName(ac)){
                String aac = UniProtAccessionCodeTools.translateName2Accession(ac);
                if ( aac != null && (! aac.equals(""))){
                    ac = aac;
                }
            }
        }

        //System.out.println("converted unirpto to " + ac  );
        return ac;

    }
    
    public void newAlignment(AlignmentEvent event){
        Annotation[] os = event.getAlignment().getObjects();
        if ( os.length < 2){
            // something strange is going on here..
            // perhaps a crazy server
            //logger.warning(panelName+" got  alignment of wrong # objects...");
            return;
        }
        
        super.newAlignment(event);
                
        Annotation a1 = os[0];
        Annotation a2 = os[1];
        
        String cs1 = (String) a1.getProperty("dbCoordSys");
        
        String ac1 = uniProtProtect( (String) a1.getProperty("dbAccessionId"),cs1);
        a1.setProperty("dbAccessionId", ac1);
        
        
        String cs2 = (String) a2.getProperty("dbCoordSys");
        String ac2 = uniProtProtect( (String) a2.getProperty("dbAccessionId"),cs2);
        a2.setProperty("dbAccessionId", ac2);
       
        /*System.out.println(a1);
        System.out.println(a2);
        System.out.println(alignment.getObjects()[0]);
        System.out.println(alignment.getObjects()[1]);
        */
        logger.info(panelName+" got new Alignment "+ac1 + " " + ac2+   " currently know:"+object1Id+" " + object2Id);
        
        // we need to find out which of the two objects is object1/object2 ...
        
       
//      triggerObject1Request(ac1);
//      triggerObject2Request(ac2);
    //  see wich object already was there..
       
       
        if ( ac1.equalsIgnoreCase(object1Id) || 
                ( (coordSys1.toString().equals(SpiceDefaults.PDBCOORDSYS)) &&
                        (ac1.substring(0,4).equalsIgnoreCase(object1Id)))){
            // object1 = ac1
           
            triggerObject2Request(ac2);
        } else if ( ac1.equalsIgnoreCase(object2Id)|| 
                ((coordSys1.toString().equals(SpiceDefaults.PDBCOORDSYS)) &&
                        ( ac1.substring(0,4).equalsIgnoreCase(object2Id)))){
            // object2 = ac1
          
            triggerObject1Request(ac2);
        } else if ( ac2.equalsIgnoreCase(object1Id)|| 
                ((coordSys2.toString().equals(SpiceDefaults.PDBCOORDSYS)) &&
                ( ac2.substring(0,4).equalsIgnoreCase(object1Id)))){
           
            triggerObject2Request(ac1);
            
        } else if ( ac2.equalsIgnoreCase(object2Id)|| 
                ((coordSys2.toString().equals(SpiceDefaults.PDBCOORDSYS)) &&
                ( ac2.substring(0,4).equalsIgnoreCase(object2Id)))){
          
            triggerObject1Request(ac1);
        } else {
            logger.fine(panelName+" could not detect correct accessionCode " +ac1 + " " + ac2);
        }
        
        tryCreateAlignmentChain();
        
        
    }
    
    private void tryCreateAlignmentChain(){
        //logger.info("tryCreatealignmentChain");
        Annotation[] os = alignment.getObjects();
        if ( os.length < 2){
            // something strange is going on here..
            //logger.warning(panelName+" got  alignment of wrong # objects..." + os.length);
            return;
        }
        Annotation a1 = os[0];
        Annotation a2 = os[1];
        
        String cs1 = (String) a1.getProperty("dbCoordSys");
        String cs2 = (String) a2.getProperty("dbCoordSys");
        
        String ac1 =  uniProtProtect((String) a1.getProperty("dbAccessionId"),cs1);
        String ac2 =  uniProtProtect((String) a2.getProperty("dbAccessionId"),cs2);
        
       
        boolean found1 = false;
        boolean found2 = false;
        if ( object1Id.equalsIgnoreCase(ac1) || object2Id.equalsIgnoreCase(ac1))
            found1 =true;
        if ( object1Id.equalsIgnoreCase(ac2) || object2Id.equalsIgnoreCase(ac2))
            found2 =true;
        
        if ( ! (found1 && found2)) {
            logger.info(panelName + " can not create alignmentChain, yet >" + ac1 + "< >" + ac2 + 
                    "< >" + object1Id + "< >" + object2Id+"<");
            return;
        }
        
        //  create the internal alignment representation that allows to project one position in
        // one alignment onte the other ...
        
        try {
           storeAlignment(alignment);
           //logger.info("stored alignment in manager");
        } catch (Exception e){
            e.printStackTrace();
            //logger.warning(e.getMessage());
        }
        
    }
    
    public void triggerNoObject1(String ac){
        logger.info("trigger no object 1");
        Iterator iter = object1Listeners.iterator();
        if  (! coordSys1.toString().equals(SpiceDefaults.PDBCOORDSYS) )
            ac = ac.toUpperCase();
        
        while (iter.hasNext()){
            ObjectListener li = (ObjectListener)iter.next();
            li.noObjectFound(ac);
        }
    }
    

    public void triggerNoObject2(String ac){
        logger.info("trigger no object 2");
        Iterator iter = object2Listeners.iterator();
        if  (! coordSys2.toString().equals(SpiceDefaults.PDBCOORDSYS) )
            ac = ac.toUpperCase();
        
        while (iter.hasNext()){
            ObjectListener li = (ObjectListener)iter.next();
            li.noObjectFound(ac);
        }
    }
    
    
    public void triggerObject1Request(String ac){
        logger.finest("should trigger object 1 request ?" + ac + " " + object1Id);
        if ( ac.equalsIgnoreCase(object1Id)) {
            //logger.info("no...");
            return;
        }
        logger.finest("yes, do trigger object 1 request " + ac);
        object1Id =ac;
        
        Iterator iter = object1Listeners.iterator();
        if  (! coordSys1.toString().equals(SpiceDefaults.PDBCOORDSYS) )
            ac = ac.toUpperCase();
        
        while (iter.hasNext()){
            ObjectListener li = (ObjectListener)iter.next();
            li.newObjectRequested(ac);
        }
    }
    
    public void triggerObject2Request(String ac){
        //logger.info("should trigger object 2 request ? " + ac);
        if ( ac.equalsIgnoreCase(object2Id)) {
            //logger.info("no...");
            return;
        }
        object2Id = ac;
        
        if  (! coordSys2.toString().equals(SpiceDefaults.PDBCOORDSYS) )
            ac = ac.toUpperCase();
        //logger.info("do triggerObject2Request " + ac);
        
        Iterator iter = object2Listeners.iterator();
        while (iter.hasNext()){
            ObjectListener li = (ObjectListener)iter.next();
            li.newObjectRequested(ac);
        }
    }
    
    
    public void newObject1 (Object o) {
        
    }
    
    public void newObject2 (Object o){
        
    }
    
    
    public void clearSelection1(){
        Iterator iter = sequence1Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.clearSelection();
        }
    }
    
    public void clearSelection2(){
        Iterator iter = sequence2Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.clearSelection();
        }
    }
    
    
    private boolean isKnownAc(String ac){
        if ( object1Id.equalsIgnoreCase(ac) || 
                object2Id.equalsIgnoreCase(ac) 
                )
            return true;
        return false;
    }
    
    public void newSequence1(SequenceEvent e){
       //logger.info(panelName+" alignment : new sequence1:" + e.getAccessionCode() + " currently know:"+object1Id+" " + object2Id);
        
       String ac = e.getAccessionCode().toLowerCase();
       SequenceManager sm = new SequenceManager();
        if (  isKnownAc(ac)){
            
            if ( alignmentIsLoaded(object1Id,object2Id)) {
                //logger.info("we got the alignment, now checking sequences");
                if ( sequence1.getSequence().equals(e.getSequence())) {
                // we already go this one, ignore...
                    //logger.info("already loaded, skipping");
                    tryCreateAlignmentChain();
                    return;
                } else {
                    
                    sequence1 = sm.getChainFromString(e.getSequence());
                    seqLength1 = sequence1.getLengthAminos();
                    object1Id = ac;
                    //object1Id = "";
                    sequence1.setSwissprotId(ac);
                    
                    Iterator iter = alignmentRenderers.iterator();
                    while (iter.hasNext()){
                        AlignmentRenderer re = (AlignmentRenderer)iter.next();
                        re.setSequence1(sequence1);
                    }
                    
                    tryCreateAlignmentChain();
                    return;
                }
            }
        
        }
       
        //logger.info("setting new sequence 1" + e.getSequence());

        sequence1 = sm.getChainFromString(e.getSequence());
        seqLength1 = sequence1.getLengthAminos();
        // a new object, request the data...
        object1Id = ac;
        sequence1.setSwissprotId(ac);
        //object1Id = "";
        
        Iterator iter = alignmentRenderers.iterator();
        while (iter.hasNext()){
            AlignmentRenderer re = (AlignmentRenderer)iter.next();
            re.setSequence1(sequence1);
        }
        
        alignment = new Alignment();
        
        alignmentMap1 = new HashMap();
        alignmentMap2 = new HashMap();
        
        if ( (object2Id == null) || (object2Id.equals(""))) {
            // get first alignment for this sequence..
        
            //requestAlignment(object1Id);
            AlignmentParameters params = new AlignmentParameters();
            params.setQuery(object1Id);
            if ( coordSys1.toString().equals(SpiceDefaults.PDBCOORDSYS )) {
                String chainId = getChainIdFromPDBCode(object1Id);
                if ( chainId != null)
                    params.setQueryPDBChainId(chainId);
            } else {
                params.setQuery(object1Id.toUpperCase());
            }
            params.setQueryCoordinateSystem(coordSys1);
            params.setSubjectCoordinateSystem(coordSys2);
            params.setDasSources(alignmentServers);
            requestAlignment(params);
        
        } else {
            
            
            String o1 = object1Id;
            String o2 = object2Id;
           
            AlignmentParameters params = new AlignmentParameters();
            
            if (! coordSys1.toString().equals(SpiceDefaults.PDBCOORDSYS ))
                o1 = o1.toUpperCase();
            else {
                String chainId = getChainIdFromPDBCode(object1Id);
                params.setQueryPDBChainId(chainId);
            }
            if (! coordSys2.toString().equals(SpiceDefaults.PDBCOORDSYS )) 
                o2 = o2.toUpperCase();
            else {
                
                String chainId = getChainIdFromPDBCode(object2Id);
                params.setSubjectPDBChainId(chainId);
            }
            
            params.setQuery(o1);
            params.setSubject(o2);
            params.setQueryCoordinateSystem(coordSys1);
            params.setSubjectCoordinateSystem(coordSys2);
            params.setDasSources(alignmentServers);
            requestAlignment(params);
            //requestAlignment(o1,o2,coordSys1);
          
        }
    
        tryCreateAlignmentChain();
    }
    
    
    private boolean alignmentIsLoaded(String query, String subject){
        logger.fine(panelName + " is alignment loaded ? for " + query + " and " + subject);
        logger.fine("o1: " + object1Id + " o2:" + object2Id);
        
        // check if alignment is already known
        Annotation[] os = alignment.getObjects();
        if ( os.length < 2){
            // something strange is going on here..
            //logger.warning(panelName+" got  alignment of wrong # objects..." + os.length);
            return false;
        }
        Annotation a1 = os[0];
        Annotation a2 = os[1];
        
        String cs1 = (String) a1.getProperty("dbCoordSys");
        String cs2 = (String) a2.getProperty("dbCoordSys");
        
        
        String ac1 =  uniProtProtect((String) a1.getProperty("dbAccessionId"),cs1);
        String ac2 =  uniProtProtect((String) a2.getProperty("dbAccessionId"),cs2);
        
      
        
        if   (query.equalsIgnoreCase(ac1) || query.equalsIgnoreCase(ac2) ) { 
            if  ( subject.equalsIgnoreCase(ac1) ||  subject.equalsIgnoreCase(ac2))
            {
                //logger.info("already know alignment, not requesting again");
                return true;
            } 
        }
        
        return false;
        
    }
    
    
    /** request the first alignment for a particular accession code 
    * 
    * @param params
    */  
    private void requestAlignment(AlignmentParameters params){
        // TODO fix this:
        logger.fine(panelName +" requesting alignment " + params);
        // the alignmetn server should use the correct coord sys ...
        if ( params.getSubjectCoordinateSystem().toString().equals(SpiceDefaults.ENSPCOORDSYS)) {
            DasCoordinateSystem ecs = new DasCoordinateSystem();
            ecs.setName(ASSEMBLYNAME);
            params.setSubjectCoordinateSystem(ecs);   
        }
        AlignmentThread thread =    new AlignmentThread(params);
        thread.addAlignmentListener(this);
        Iterator iter = alignmentListeners.iterator();
        while (iter.hasNext()){
            AlignmentListener li = (AlignmentListener) iter.next();
            thread.addAlignmentListener(li);
        }
        thread.start();
    }
    
    
    
    public void newSequence2(SequenceEvent e){
        logger.finest(panelName+" alignment : new sequence2:"+e.getAccessionCode() + 
                " currently know 1: >"+object1Id+"< 2: >" + object2Id + "< seq: >" + sequence2.getSequence()+"< + e:" + e.getSequence());
        
        String ac = e.getAccessionCode().toLowerCase();
        
        SequenceManager sm = new SequenceManager();
        if ( isKnownAc(ac)){
            if ( alignmentIsLoaded(object1Id,object2Id)) {
                //logger.info("we got the alignment, now checking sequences");
                String s2 = sequence2.getSequence();
                String es = e.getSequence();
                
                //logger.info("seq s: " + s2);
                //logger.info("event s " + es);
                //logger.info("comp: " + s2.compareTo(es)+"");
                if ( s2.equals(es)) {
                    // we already go this one, ignore...
                    //logger.info("already loaded, skipping >" + es+"<");
                    tryCreateAlignmentChain();
                    return;
                } else {
                    sequence2 = sm.getChainFromString(e.getSequence());
                    seqLength2 = sequence2.getLengthAminos();
                    object2Id = ac;
                    //object1Id = "";
                    Iterator iter = alignmentRenderers.iterator();
                    while (iter.hasNext()){
                        AlignmentRenderer re = (AlignmentRenderer)iter.next();
                        re.setSequence2(sequence2);
                    }
                    
                    sequence2.setSwissprotId(ac);
                    tryCreateAlignmentChain();
                    return;
                }
            }
        }
        
        logger.finest("setting new sequence 2" + e.getSequence());
        
        sequence2 = sm.getChainFromString(e.getSequence());
        seqLength2 = sequence2.getLengthAminos();
        object2Id = ac;
        //object1Id = "";
        sequence2.setSwissprotId(ac);
        Iterator iter = alignmentRenderers.iterator();
        while (iter.hasNext()){
            AlignmentRenderer re = (AlignmentRenderer)iter.next();
            //bug #20157 in RT -> this was set wrongly set to 1!!
            re.setSequence2(sequence2);
        }
        
        alignment = new Alignment();
        
        alignmentMap1 = new HashMap();
        alignmentMap2 = new HashMap();
                
        
        if ( (object1Id == null)||(object1Id.equals(""))) {
            // get first alignment for this sequence..
                
            AlignmentParameters params = new AlignmentParameters();
            if (! coordSys2.toString().equals(SpiceDefaults.PDBCOORDSYS))
                params.setQuery(object2Id.toUpperCase());
            else 
                params.setQuery(object2Id);
            params.setQueryCoordinateSystem(coordSys2);
            params.setSubjectCoordinateSystem(coordSys1);
            params.setDasSources(alignmentServers);
            requestAlignment(params);
            
        } else {
           
            String o1 = object1Id;
            String o2 = object2Id;
            
            AlignmentParameters params = new AlignmentParameters();
            
            if (! coordSys1.toString().equals(SpiceDefaults.PDBCOORDSYS))
                o1 = o1.toUpperCase();
            else {
                String chainId = getChainIdFromPDBCode(object1Id);
                params.setSubjectPDBChainId(chainId);
            }
            if (! coordSys2.toString().equals(SpiceDefaults.PDBCOORDSYS )) 
                o2 = o2.toUpperCase();
            else {
                
                String chainId = getChainIdFromPDBCode(object2Id);
                params.setQueryPDBChainId(chainId);
            }
            params.setQuery(o2);
            params.setSubject(o1);
            params.setQueryCoordinateSystem(coordSys2);
            params.setSubjectCoordinateSystem(coordSys1);
            params.setDasSources(alignmentServers);
            requestAlignment(params);
            //requestAlignment(o2,o1, coordSys2);
             
        }
        
        tryCreateAlignmentChain();
       
    }
    
    private String getChainIdFromPDBCode(String code){
        if ( code.substring(4,5).equals(".")){
            return code.substring(5,6);
        }
        return null;
        
    }
    
    public void requestedObject1(String o){
        //logger.info("a new object1 has been requested");
        Iterator iter = sequence1Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener )iter.next();
            li.newObjectRequested(o);
        }        
    }
    
    public void requestedObject2(String o){
        //logger.info("a new object2 has been requested");
        Iterator iter = sequence2Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener )iter.next();
            li.newObjectRequested(o);
        }
    }
    
  
    
    
    
    public void selectedSeqPosition1(int pos){
        //logger.info("selected seq pos1: " + pos + " pos2:" + getPosition2(pos));
        int npos = getPosition2(pos);
        Iterator iter = sequence2Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectedSeqPosition(npos);
        }
    }
    
    public void selectedSeqPosition2(int pos){
        //logger.info("selected seq pos1: " + getPosition1(pos) + " pos2:" + pos);
        int npos = getPosition1(pos);
        Iterator iter = sequence1Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectedSeqPosition(npos);
        }
    }
    
    public void selectedSeqRange1(int start, int end ){
        
        int s = getPosition2(start);
        
        if ( s == -1)
            s = getNextPosition2(start,SEARCH_DIRECTION_INCREASE,end);
       
        int e = getPosition2(end);
        if ( e == -1)
            e = getNextPosition2(end,SEARCH_DIRECTION_DECREASE,start);
        
        if ( s < 0)
            s = 0;
        if ( e > sequence2.getLength()-1)
            e = sequence2.getLength()-1;
        
        
        //logger.info("s " + s + " e " + e);
        Iterator iter = sequence2Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectedSeqRange(s,e);
        }
        
    }
    
    public void selectedSeqRange2(int start, int end ){
        int s = getPosition1(start);
        
        if ( s == -1)
            s = getNextPosition1(start,SEARCH_DIRECTION_INCREASE,end);
        
        int e = getPosition1(end);
        if ( e == -1)
            e = getNextPosition1(end, SEARCH_DIRECTION_DECREASE,start);
        
        if ( s < 0)
            s = 0;
        if ( e > sequence1.getLength()-1)
            e = sequence1.getLength()-1;
        
        Iterator iter = sequence1Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectedSeqRange(s,e);
        }
    }
    
    public void selectionLocked1(boolean flag){
     
        Iterator iter = sequence1Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectionLocked(flag);
        }
        object2Listener.selectionLocked(flag);
    }
    
    public void selectionLocked2(boolean flag){
        Iterator iter = sequence2Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectionLocked(flag);
        }
        object1Listener.selectionLocked(flag);
    }
    
    
    

    protected void storeAlignment(Alignment ali) throws DASException {
        super.storeAlignment(ali);
        
        Iterator iter = alignmentRenderers.iterator();
        while (iter.hasNext()){
            AlignmentRenderer re = (AlignmentRenderer)iter.next();
            re.setAlignmentMap1(alignmentMap1);
            re.setAlignmentMap2(alignmentMap2);
        }
    }
    
    public void clearAlignment(){
        //logger.info("clear alignmenty");
        
        super.clearAlignment();
        object1Id = "" ;
        object2Id = "" ;
        
           
        Iterator iter = alignmentRenderers.iterator();
        while (iter.hasNext()){
            AlignmentRenderer re = (AlignmentRenderer)iter.next();
            re.clearAlignment();
        }
    }
    
    
    public void addSeq1FeatureListener(SpiceFeatureListener li){
        seq1FeatureListener.add(li);   
    }
    
    public void addSeq2FeatureListener(SpiceFeatureListener li){
        seq2FeatureListener.add(li);
    }
    
    public SpiceFeatureListener[] getSeq1FeatureListeners(){
        
        return (SpiceFeatureListener[]) seq1FeatureListener.toArray(new SpiceFeatureListener[seq1FeatureListener.size()]);
    }
    
    public SpiceFeatureListener[] getSeq2FeatureListeners(){
        return (SpiceFeatureListener[]) seq2FeatureListener.toArray(new SpiceFeatureListener[seq2FeatureListener.size()]);
    }

    public void newStructure(StructureEvent event) {
       
        //logger.info("alig manager got new structure");
        if ( coordSys1.toString().equals(SpiceDefaults.PDBCOORDSYS)){
            Structure s = event.getStructure();
            if ( s.size() < 1){
                return;
            }
            int numb = event.getCurrentChainNumber();
            sequence1 = s.getChain(numb);
            Iterator iter = alignmentRenderers.iterator();
            while (iter.hasNext()){
                AlignmentRenderer re = (AlignmentRenderer)iter.next();
                re.setSequence1(sequence1);
            }
            
            tryCreateAlignmentChain();
        }
        
    }

    public void selectedChain(StructureEvent event) {
        //logger.info("sected chain in aligmanager ");
       if ( coordSys1.toString().equals(SpiceDefaults.PDBCOORDSYS)){
           Structure s = event.getStructure();
           int numb = event.getCurrentChainNumber();
           sequence1 = s.getChain(numb);
           Iterator iter = alignmentRenderers.iterator();
           while (iter.hasNext()){
               AlignmentRenderer re = (AlignmentRenderer)iter.next();
               re.setSequence1(sequence1);
           }
           
           tryCreateAlignmentChain();
       }
        
    }

    public void newObjectRequested(String accessionCode) { }

    public void noObjectFound(String accessionCode) {}
    
    
    
    
}



class MyFeatureTranslator implements SpiceFeatureListener {
    
    int pos; // where the event occured - the other one should be triggered
    AlignmentManager parent;
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    Segment currentSegmentMO; // the original segment
    Segment currentSegmentCL; // the converted one
    
    Feature currentFeatureMO;
    Feature currentFeatureCL;
    
    public MyFeatureTranslator(int nr, AlignmentManager parent){
        this.parent = parent;
        pos = nr ;
    }
    public void clearSelection() {
        currentSegmentMO = null;
        currentFeatureMO = null;
        currentSegmentCL = null;
        currentFeatureCL = null;
        
    }
    
    /** a feature in one panel has been selected, translate into coordinates of other panel ...
     * and trigger event for these...
     * 
     */
    public void featureSelected(SpiceFeatureEvent e) {
       
        Feature f = e.getFeature();
        if ( currentFeatureCL != null ) {
            if (f.equals(currentFeatureCL))
                return;
        }
        //logger.info("feature selected " + pos + " " + f);
        currentFeatureCL =f;
        currentFeatureMO = null;
        Feature newF = convertFeature(f);
       
        SpiceFeatureEvent event = new SpiceFeatureEvent(e.getDasSource(),newF);
        triggerFeatureSelected(event);
    }

    public void mouseOverFeature(SpiceFeatureEvent e) {
        //logger.info("mouse over feature " + e);
        Feature f = e.getFeature();
        
        if ( currentFeatureMO != null ) {
            if (f.equals(currentFeatureMO))
                return;
        }
        currentFeatureMO = f;
        currentFeatureCL = null;
        Feature newF = convertFeature(f);
        SpiceFeatureEvent event = new SpiceFeatureEvent(e.getDasSource(),newF);
        triggerMouseOverFeature(event);
    }
    
    private Feature convertFeature(Feature f){
        
        Feature newF = new FeatureImpl();
        newF.setLink(f.getLink());
        newF.setMethod(f.getMethod());
        newF.setNote(f.getNote());
        newF.setScore(f.getScore());
        newF.setType(f.getType());
        newF.setName(f.getName());
        newF.setSource(f.getSource());
        
        List segments = f.getSegments();
        Iterator iter = segments.iterator();
        while (iter.hasNext()){
            Segment oldS = (Segment) iter.next();
            Segment newS = convertSegment(oldS);
            newF.addSegment(newS);
        }
        return newF;
    }
    

    private Segment convertSegment(Segment seg){
        //System.out.println("convert segment " + seg.getStart() + " " + seg.getEnd());
        Segment s = (Segment)seg.clone();
        int st = s.getStart()-1;
        int en = s.getEnd()-1;
        
        int newS;
        int newE;
        int length;
        if ( pos == 1){            
            newS = parent.getNextPosition2(st,AbstractAlignmentManager.SEARCH_DIRECTION_INCREASE,en)+1;
            newE = parent.getNextPosition2(en,AbstractAlignmentManager.SEARCH_DIRECTION_DECREASE,st)+1;
            length = parent.seqLength2;
        } else {
            newS = parent.getNextPosition1(st,AbstractAlignmentManager.SEARCH_DIRECTION_INCREASE,en)+1;
            newE = parent.getNextPosition1(en,AbstractAlignmentManager.SEARCH_DIRECTION_DECREASE,st)+1;
            length = parent.seqLength1;
        }
        
        if ( newS < 1) 
            newS = 1;            
        
        if ( newE > (length-1))
            newE = length-1;
        
        s.setStart(newS);
        s.setEnd(newE);
        
        //System.out.println("converted to " + s.getStart() + " " + s.getEnd() + " length: " + length);
        //logger.info("new segment " + s);
        return s;
        
        
    }

    public void mouseOverSegment(SpiceFeatureEvent e) {
           
        Segment s = e.getSegment();
      
        if ( currentSegmentMO != null){
            if ( s.equals(currentSegmentMO))
               return; 
        }
        //logger.info("mouse over segment " + s + " " + currentSegmentMO);
        currentSegmentMO = s;
        currentSegmentCL = null;
        
        s = convertSegment(s);
        SpiceFeatureEvent event = new SpiceFeatureEvent(e.getDasSource(),e.getFeature(),s);
        triggerMouseOverSegment(event);
    }

    
    
    public void segmentSelected(SpiceFeatureEvent e) {
        
        Segment s = e.getSegment();
        
        if ( currentSegmentCL != null){
            if ( s.equals(currentSegmentCL))
               return; 
        }
        //logger.info("segment selected " + s + " " + currentSegmentCL);
        currentSegmentCL = s;
        currentSegmentMO = null;
        
        s = convertSegment(s);
        
                
        SpiceFeatureEvent event = new SpiceFeatureEvent(e.getDasSource(),e.getFeature(),s);
        
        triggerSegmentSelected(event);
    }
    
    private void triggerMouseOverSegment(SpiceFeatureEvent e){
        SpiceFeatureListener[] sfl;
        if ( pos == 2){
            sfl = parent.getSeq1FeatureListeners();
        } else {
            sfl = parent.getSeq2FeatureListeners();
        }
        
        for ( int i=0 ; i< sfl.length;i++){
            SpiceFeatureListener li = sfl[i];
            li.mouseOverSegment(e);
        }
    }
    
    private void triggerMouseOverFeature(SpiceFeatureEvent e){
        SpiceFeatureListener[] sfl;
        if ( pos == 2){
            sfl = parent.getSeq1FeatureListeners();
        } else {
            sfl = parent.getSeq2FeatureListeners();
        }
        
        for ( int i=0 ; i< sfl.length;i++){
            SpiceFeatureListener li = sfl[i];
            li.mouseOverFeature(e);
        }
    }
    
    private void triggerFeatureSelected(SpiceFeatureEvent e){
        //logger.info("triggerFeatureSelected " +e.getFeature());
        SpiceFeatureListener[] sfl;
        if ( pos == 2){
            //logger.info("triggerFeatureSelected in panel 1" );
            sfl = parent.getSeq1FeatureListeners();
        } else {
            //logger.info("triggerFeatureSelected in panel 2" );
            sfl = parent.getSeq2FeatureListeners();
        }
        
        for ( int i=0 ; i< sfl.length;i++){
            SpiceFeatureListener li = sfl[i];
            li.featureSelected(e);
        }
    }
    
    private void triggerSegmentSelected(SpiceFeatureEvent e){
        //logger.info("triggerSegmentSelected " + e.getSegment());
        
        SpiceFeatureListener[] sfl;
        if ( pos == 2){
            sfl = parent.getSeq1FeatureListeners();
        } else {
            sfl = parent.getSeq2FeatureListeners();
        }
        
        for ( int i=0 ; i< sfl.length;i++){
            SpiceFeatureListener li = sfl[i];
            li.segmentSelected(e);
        }
            
    }
    
}




class AlignmentSequenceListener implements SequenceListener{
    int objectNr;
    int oldpos;
    int oldend;
    boolean selectionLocked;
    AlignmentManager parent;
    boolean selectionCleared;
    
    public AlignmentSequenceListener(AlignmentManager parent, int nr){
        this.parent=parent;
        objectNr = nr;
        oldpos = -1;
        oldend = -1;
        selectionLocked = false;
        selectionCleared = false;
    }
    
    public synchronized void newSequence(SequenceEvent e) {
        //System.out.println("alignment sequence listener got sequence " + objectNr + " " +  e.getSequence());
        selectionCleared = false;
        if ( objectNr == 1)
            parent.newSequence1(e);
        else
            parent.newSequence2(e);
    }
    public synchronized void selectedSeqPosition(int position) {
        selectionCleared =false;
        if ( selectionLocked )
            return;
         
        
        if ( oldpos == position)            
            return;
        oldpos = position;
        oldend = position;
        
        if ( objectNr == 1)
            parent.selectedSeqPosition1(position);
        else
            parent.selectedSeqPosition2(position);
        
    }
    public synchronized void selectedSeqRange(int start, int end) {
        selectionCleared = false;
       if ( selectionLocked )
           return;
        if (( oldpos == start ) && ( oldend == end)){
            return;
        }
        oldpos = start;
        oldend = end;
        
        if ( objectNr == 1)
            parent.selectedSeqRange1(start,end);
        else
            parent.selectedSeqRange2(start,end);
    }
    
    public void selectionLocked(boolean flag) {
        selectionCleared=false;
        //System.out.println("AlignmentManager got selectionLocked " + flag + " "+selectionLocked);
        if ( selectionLocked == flag) {
            //System.out.println("not reporting further");
            return;
        }
        selectionLocked = flag;
        // selection lock is for all objects...
        parent.selectionLocked1(flag);
        parent.selectionLocked2(flag);
    }
    
    public synchronized void newObject(Object object) {
        selectionCleared = false;
        
        if ( objectNr == 1){
            parent.newObject1(object);
        } else {
            parent.newObject2(object);
            
        }
    }
    
    public void noObjectFound(String accessionCode){
       
    }
    
    public synchronized void newObjectRequested(String accessionCode) {
     
        
        if ( objectNr == 1){
            parent.requestedObject1(accessionCode);
        } else {
            parent.requestedObject2(accessionCode);
            
        }
    }

    public void clearSelection() {
      if ( selectionCleared )
          return;
      selectionCleared = true;
      if ( objectNr == 1){
          parent.clearSelection1();
      } else {
          parent.clearSelection2();
            
      }
        
    }
    
    
    
}





