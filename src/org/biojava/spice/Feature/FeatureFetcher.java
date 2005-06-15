
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
 * Created on 21.09.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.spice.Feature ;

import org.biojava.spice.SPICEFrame    ;
import org.biojava.spice.Config.*    ;

import java.util.Iterator;
import java.util.Map                   ;
import java.util.HashMap               ;
import java.util.List                  ;
import java.util.ArrayList             ;
import java.net.URL                    ;
import java.net.MalformedURLException  ;

import java.awt.Color                  ;
import java.util.logging.*             ;
import org.biojava.bio.structure.Chain ;
import org.biojava.bio.structure.Group ;

import org.biojava.spice.Panel.seqfeat.*;

//import org.biojava.services.das.registry.DasSource;

/** a class to fetch all Features  in parallel threads
 
 * @author Andreas Prlic
 */
public class FeatureFetcher extends Thread
{
    
    public static Color[] entColors = new Color []{
            new Color(153,153,255), 
            new Color(255,153,153), 
            new Color(153,255,153), 
            new Color(255,255,102),
            new Color(102,255,102),
            new Color(0,51,204),
            new Color(253,0,153)
    };
    
    public static final Color HELIX_COLOR  = new Color(255,51,51);
    public static final Color STRAND_COLOR = new Color(255,204,51);
    public static final Color TURN_COLOR   = new Color(204,204,204); 
    SPICEFrame parent ;
    boolean finished ;
    String spId ;
    String pdbId ;
    RegistryConfiguration spiceconfig ;
    List allFeatures ;
    DasResponse[] subthreads ;
    FeatureView[] featureViews;
    Chain chain ;
    
    String[] txtColors;
    
    static String PDBCOORDSYS     = "PDBresnum,Protein Structure";
    static String UNIPROTCOORDSYS = "UniProt,Protein Sequence";
    
    Logger logger        ;
    
    boolean updateDisplay ;
    
    /** @param config the SPICE config Map
     * @param sp_id SwissProt ID
     * @param pdb_id PDB ID
     * @param c Chain object to which these features should be linked
     */
    public FeatureFetcher(SPICEFrame spice, RegistryConfiguration config, String sp_id, String pdb_id, Chain c ) {
        logger = Logger.getLogger("org.biojava.spice");
        parent      = spice ;
        finished    = false ;
        spId        = sp_id ;
        pdbId       = pdb_id ;
        spiceconfig = config ;
        allFeatures = new ArrayList();
        chain       = c ;
        
        txtColors   = new String[] { "blue","pink","green","magenta","orange","pink","cyan"};
        updateDisplay = false ;
	
    }
    
    
    public boolean isDone() {
        return finished ;
    }
    
    /** start one thread per server to fetch all the features!
     */
    public void run() {
        logger.finest("retreiving features for PDB " + pdbId + " UniProt " + spId);
        parent.setLoading(true);
        doDasCommunication() ;
    }
    
    private synchronized void doDasCommunication() {
        
        finished = false ;
        allFeatures = new ArrayList();
        // contact sequence feature servers
        List featservs    =  spiceconfig.getServers("features",UNIPROTCOORDSYS);
        List pdbresservs  =  spiceconfig.getServers("features",PDBCOORDSYS);
        
        int nrservers =0;
        if (spId != null) 
            nrservers += featservs.size() ;
        if (pdbId != null)
            nrservers +=  pdbresservs.size();
        
        logger.finest("total: " + nrservers + "feature servers applicable here");
        // no network connection ( to registry);
        if ( nrservers == 0)
            return;
        
        subthreads = new DasResponse[nrservers];
        
        // an array that stores all feature views...
        featureViews = new FeatureView[nrservers];
        
        int responsecounter = 0 ;
        // start all the sub -threads ;
        for ( int f =0;f<featservs.size();f++) {
            if (spId == null ) 
                continue ;
            
            
            DasResponse d=new DasResponse(UNIPROTCOORDSYS);
            subthreads[responsecounter] = d; 
            SpiceDasSource featureserver = (SpiceDasSource) featservs.get(f) ;
            FeatureView fv = createNewFeatureView(featureserver);
            featureViews[responsecounter] = fv;
            
            
            String url = featureserver.getUrl();
            d.setDasSource(featureserver);
            char lastChar = url.charAt(url.length()-1);		 
            if ( ! (lastChar == '/') ) 
                url +="/" ;
            
            
            String queryString = url + "features?segment="+ spId ;
            URL spUrl = null ;
            try {
                spUrl = new URL(queryString);
            } catch (MalformedURLException e ) {
                e.printStackTrace();
                ArrayList empty = new ArrayList();
                setFinished(responsecounter,empty);
                responsecounter++;
                continue ;
            }
            
            SingleFeatureThread sft = new SingleFeatureThread ( this ,spUrl,responsecounter);
            sft.start();
            responsecounter++;
        }
        
        // and the servers serving in structure coordinates
        for ( int f =0;f<pdbresservs.size();f++) {
            if (pdbId == null ) 
                continue ;
            
            
            DasResponse d=new DasResponse(PDBCOORDSYS);
            subthreads[responsecounter] = d; 
            
            SpiceDasSource featureserver = (SpiceDasSource) pdbresservs.get(f) ;
            
            FeatureView fv = createNewFeatureView(featureserver);
            featureViews[responsecounter] = fv;
            
            String url = featureserver.getUrl();
            d.setDasSource(featureserver);
            String queryString = url + "features?segment="+ pdbId+"."+chain.getName() ;
            URL spUrl = null ;
            try {
                spUrl = new URL(queryString);
            } catch (MalformedURLException e ) {
                e.printStackTrace();
                ArrayList empty = new ArrayList();
                setFinished(responsecounter,empty);
                responsecounter++;
                continue ;
            }
            //logger.finest("starting thread");
            SingleFeatureThread sft = new SingleFeatureThread ( this ,spUrl,responsecounter);
            sft.start();
            responsecounter++;
        }
        
        // if everything finished stop parent loading singn ...
        boolean done = false ;
        while ( ! done) {
            done = allFinished();
            try {
                wait(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
                done = true ;
            }
        }
        parent.setLoading(false);
        notifyAll();
        
        //TODO: clean this up. not needed any longer!
        // wait for results to come back
        // new by AP : no need for this! 
        /*
        boolean done = false ;
        while ( ! done) {
            done = allFinished();
            //logger.finest("FeatureFetcher waiting for features to be retreived: "+done);
            try {
                wait(300);
                //logger.finest("FeatureFetcher waiting "+done);
                if ( updateDisplay ) {
                   
                    List l = null;
                    //List l = getFeatures();
		    //System.out.println(l);
                    if ( l != null )
                        parent.setFeatures(spId,l);
                    updateDisplay = false ;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                done = true ;
            }
            //logger.finest("getNewFeatures :in waitloop");
        }
        */
        
        // finally all sub-threads are finished and we can sent all features back to the main application
        // reset and re-paint all features 
        //allFeatures = new ArrayList();
        
        // extract ALL the features and convert them so they can be used for spice...
        //for ( int i = 0 ; i < subthreads.length; i++ ) {
            //DasResponse d = subthreads[i] ;	    
            //addFeaturesFromDasResponse(d,i);	   
        //}
        
        //finished = true ;
        
    }
    
    public FeatureView createNewFeatureView(SpiceDasSource featureserver){
        
        FeatureView fv = new FeatureView();
        fv.setSeqLength(chain.getLength());
        fv.setLoading(true);
        fv.setLabel(featureserver.getNickname());
        // add fv to viewer ...
        
        SpiceFeatureViewer sfv = parent.getFeatureViewer();
        sfv.addFeatureView(fv);
        sfv.repaint();
        return fv;
    }
    
    /** browse through chain and get UniProt position with pdbResNum 
     @throws an exception if link can not be established...
     */
    private String getUniProtCoord(String pdbResNumb, Chain chain) 
    throws Exception 
    {
        
        ArrayList groups = chain.getGroups();
        
        for (int i=0 ; i<groups.size();i++){
            Group g = (Group)groups.get(i);
                        
            String pdbCode = g.getPDBCode() ;
            if ( pdbCode != null ){
                //logger.finest(g);
                if ( pdbCode.equals(pdbResNumb)) {
                    return "" + ( i+1);
                }
            }
            
        }
        // could not map position!
        //return "" + 0 ;
        //return "" + chain.getLengthAminos();
        throw new Exception("could not find residue " + pdbResNumb + " in chain!");
        
    }
    
    
    /** add all features from a DasResponse to the locally stored ones 
     PDBresnum features are mapped to UniProt coordinate system.
     */
    private synchronized void addFeaturesFromDasResponse(DasResponse d, int threadId) {
        
        
        String coordSys = d.getCoordinateSystem();
        //logger.finest(d);
        List features   = d.getFeatures() ;
        SpiceDasSource dassource = d.getDasSource();
        
        if ( coordSys.equals(PDBCOORDSYS)) {
            // convert PDB resnum coordinates to UniProt coordinates 
            
            for (int j=0; j<features.size();j++){
                HashMap feat = (HashMap)features.get(j);	
                // overwrite the real DAS request...
                // many uusers complained about it...
                String nickname = dassource.getNickname();
                //logger.info("FeatureFeatcher: got dassource "+ " " +dassource.getId() + " " + nickname);
                if ( ! ((nickname == null) || ( nickname.equals("") )) ){
                    feat.put("dassource", nickname);  
                    
                }
                
                String mappDone  = (String)feat.get("PDBmappingDone");
                if ( mappDone == null) {
                    try {
                        String startOrig = (String)feat.get("START");
                        String endOrig   = (String)feat.get("END");
                        //logger.finest("pdbresnum feature: "+feat);
                        
                        
                        String startNew  = getUniProtCoord(startOrig,chain);
                        String endNew    = getUniProtCoord(endOrig,chain);
                        
                        feat.put("START",startNew);
                        feat.put("END",endNew);
                        feat.put("PDBmappingDone","true");
                        
                        mappDone = "true";
                    } catch (Exception e) {
                        logger.finest(e.getMessage());
                        logger.finer("could not map feature to PDB chain " + feat);
                    }
                    //logger.finest("pdb feature: "+feat);
                }

                // by AP
                //if ( (mappDone != null ) && (mappDone.equals("true")))
                //    allFeatures.add(feat) ;		    
            } 
            
            
        } else if ( coordSys.equals(UNIPROTCOORDSYS)) {
            // UniProt features can stay in their coordinate system
            
            for (int j=0; j<features.size();j++){
                HashMap feat = (HashMap)features.get(j);			
                //Feature feat = (Feature)features.get(j);			
                //logger.finest("uniprot feature: "+feat);
                String nickname = dassource.getNickname();
                if ( ! ((nickname == null) || ( nickname.equals("") )) ){
                    feat.put("dassource", nickname);  
                    
                }
                	// by AP
                //allFeatures.add(feat) ;		
            } 
        } 
        
        Feature[] fets =convertMap2Features(features);
        FeatureView fv = featureViews[threadId];
        fv.setFeatures(fets);
        fv.setLoading(false);
        notifyAll();
    }
    
    /** if a sub-thread has finished this procedure is called and the
     * features for this thread are set */
    public synchronized void setFinished(int threadId, List features) {
        //logger.finest("Got "+ features.size()+ " features from " + threadId);
        DasResponse d = subthreads[threadId] ;
        d.setFeatures(features);
        
               
        addFeaturesFromDasResponse(d,threadId);
        
        if ( allFinished()){
            parent.setLoading(true);
        }
        
        updateDisplay = true ;
        notifyAll();	
    }
    
    /** checks if all of the sub-threads have finished */
    private boolean allFinished() {
        
        for ( int i = 0 ; i < subthreads.length; i++ ) {
            DasResponse d = subthreads[i] ;
            if ( ! d.isFinished() ) {
                return false ;
            }
        }
        return true ;
    }
    
    public Feature[] getFeatures() {
        // convert Map containing features into feature objects...
        
        Feature[] spicefeatures = convertMap2Features(allFeatures);
        
        return spicefeatures ;
        //return allFeatures;
    }
    
    
    private FeatureImpl getNewFeat(Map currentFeatureMap) {
        FeatureImpl feat = new FeatureImpl();
        //logger.finest(currentFeatureMap);
        feat.setSource((String)currentFeatureMap.get("dassource"));
        feat.setName(  (String)currentFeatureMap.get("NAME"));
        feat.setType(  (String)currentFeatureMap.get("TYPE"));
        feat.setLink(  (String)currentFeatureMap.get("LINK"));
        feat.setNote(  (String)currentFeatureMap.get("NOTE"));
        String method = (String)currentFeatureMap.get("METHOD");
        if ( method == null) { method = "";}
        feat.setMethod(method);
        feat.setScore( (String)currentFeatureMap.get("SCORE"));
        return feat ;
    }
    
    /** returns a list of SPICE-Features objects */
    public Feature[] convertMap2Features(List mapfeatures){
        
        List features = new ArrayList();
        
        boolean secstruc = false ;
        String prevtype = "@prevtype" ;
        boolean first = true ;
        
        FeatureImpl feat    = null ;
        Segment segment = null ;
        
        //boolean secstrucContained = false;
        //Feature secstrucfeature = new FeatureImpl() ;
        
        for (int i = 0 ; i< mapfeatures.size();i++) {
            HashMap currentFeatureMap = (HashMap) mapfeatures.get(i);
            String type = (String) currentFeatureMap.get("TYPE") ;
            
            // we are skipping literature references for the moment 
            if ( type.equals("reference") || type.equals("GOA")){
                continue ;
            }
            
            if (! first) 
            {
                // if not first feature
                
                if ( ! secstruc ) 			    
                    // if not secondary structure ...
                    features = testAddFeatures(features,feat);
                //features.add(feat);
                
                else if ( ! 
                        (
                                type.equals("HELIX")  || 
                                type.equals("STRAND") || 
                                type.equals("TURN")  
                        ) 
                )
                {
                    // end of secondary structure
                    secstruc = false ;
                    if ( ! (feat==null)) {
                        features = testAddFeatures(features,feat);
                        /*
                        if ( ! secstrucContained) {
                            secstrucfeature = feat;
                            secstrucContained = true;
                            
                            //features.add(feat);
                        } else {
                            //add this feature data to the secstruc feature...
                            // do NOT add this feature to features ...
                            List segs = feat.getSegments();
                            Iterator iter = segs.iterator();
                            while (iter.hasNext()){
                                Segment seg = (Segment)iter.next();
                                secstrucfeature.addSegment(seg);
                            }
                        }
                        */
                    }
                    
                }
            }
            
            first = false ;				
            if ( ! secstruc) {
                feat = getNewFeat(currentFeatureMap);		
            }
            
            //}
            
            
            if (type.equals("STRAND")){
                secstruc = true ;
                currentFeatureMap.put("color",STRAND_COLOR);
                currentFeatureMap.put("colorTxt","yellow");
                feat.setName("SECSTRUC");		
            }
            
            else if (type.equals("HELIX")) {
                secstruc = true ;
                currentFeatureMap.put("color",HELIX_COLOR);
                currentFeatureMap.put("colorTxt","red");
                feat.setName("SECSTRUC");
            }	
            
            else if (type.equals("TURN")) {
                secstruc = true ;
                currentFeatureMap.put("color",TURN_COLOR);
                currentFeatureMap.put("colorTxt","white");
                feat.setName("SECSTRUC");
            } 	  
            else {
                secstruc = false ;
                currentFeatureMap.put("color"   ,entColors[i%entColors.length]);
                currentFeatureMap.put("colorTxt",txtColors[i%txtColors.length]);
                try {
                    feat.setName(type);
                } catch ( NullPointerException e) {
                    //e.printStackTrace();
                    feat.setName("null");
                }
            }
            
            segment = getNewSegment(currentFeatureMap);
            //Feature oldFeat = testIfFit
            feat.addSegment(segment);	    
            //feat.addSegment(currentFeatureMap);
            prevtype = type;
        }	
        //if ( ! (feat==null))  features.add(feat);
        if ( ! (feat==null))  
            features =testAddFeatures(features,feat);
        return (Feature[])features.toArray(new Feature[features.size()]) ;
    }
    
    private boolean isSecondaryStructureFeat(Feature feat){
        String type = feat.getType();
        if (
            type.equals("HELIX")  || 
            type.equals("STRAND") || 
            type.equals("TURN")
            ) return true;
        return false;
    }
    private List testAddFeatures(List features,Feature feat){
        // test if this features is added as a new feature to the features list, or if it is joint with an already existing one...
        //System.out.println("testing " + feat);   
        Iterator iter = features.iterator();
        
        
        while (iter.hasNext()){
            Feature tmpfeat = (Feature) iter.next() ;
            // this only compares method source and type ...
            boolean sameFeat = false;
            if ( tmpfeat.equals(feat))
                sameFeat = true;
            
            if ( ( tmpfeat.getSource().equals(feat.getSource() )) &&
                    ( tmpfeat.getMethod().equals(feat.getMethod())) &&
                    isSecondaryStructureFeat(tmpfeat) && 
                    isSecondaryStructureFeat(feat))
                sameFeat =true;
            
            if ( sameFeat) {
                
                // seems to be of same type, method and source, so check if the segments can be joint
                
                List tmpsegs = tmpfeat.getSegments();
                Iterator segiter = tmpsegs.iterator();
                List newsegs = feat.getSegments();
                Iterator newsegsiter = newsegs.iterator();
                boolean overlap = false;
                while (newsegsiter.hasNext()){
                    Segment newseg = (Segment)newsegsiter.next();
                    
                    
                    while (segiter.hasNext()){
                        Segment tmpseg = (Segment) segiter.next();
                        
                        if (  tmpseg.overlaps(newseg))
                            overlap = true;
                    }
                }
                
                if ( ! overlap){
                    // add all new segments to old features...
                    newsegsiter = newsegs.iterator();
                    while (newsegsiter.hasNext()){
                        Segment newseg = (Segment)newsegsiter.next();
                        tmpfeat.addSegment(newseg);
                    }
                        
                    return features;
                } 
            }
            
        }
//      if we get here, the  features could not be joint with any other one, so there is always some overlap
        // add to the list of known features
        features.add(feat);
        return features;
    }
    
    /** returns a list of SPICE-Features objects 
    public Feature[] convertMap2Features(List mapfeatures){
        List features = new ArrayList();
        
        boolean secstruc = false ;
        String prevtype = "@prevtype" ;
        boolean first = true ;
        
        FeatureImpl feat    = null ;
        Segment segment = null ;
        
        boolean secstrucContained = false;
        Feature secstrucfeature = new FeatureImpl() ;
        
        for (int i = 0 ; i< mapfeatures.size();i++) {
            HashMap currentFeatureMap = (HashMap) mapfeatures.get(i);
            String type = (String) currentFeatureMap.get("TYPE") ;
            
            // we are skipping literature references for the moment 
            if ( type.equals("reference") || type.equals("GOA")){
                continue ;
            }
            
            
            if (! first) 
            {
                // if not first feature
                
                if ( ! secstruc ) 			    
                    // if not secondary structure ...
                    features.add(feat);
                
                else if ( ! 
                        (
                                type.equals("HELIX")  || 
                                type.equals("STRAND") || 
                                type.equals("TURN")  
                        ) 
                )
                {
                    // end of secondary structure
                    secstruc = false ;
                    if ( ! (feat==null)) {
                                                
                        if ( ! secstrucContained) {
                            secstrucfeature = feat;
                            secstrucContained = true;
                            features.add(feat);
                        } else {
                            //add this feature data to the secstruc feature...
                            // do NOT add this feature to features ...
                            	List segs = feat.getSegments();
                            	Iterator iter = segs.iterator();
                            	while (iter.hasNext()){
                            	    Segment seg = (Segment)iter.next();
                            	    secstrucfeature.addSegment(seg);
                            	}
                        }
                    }

                }
            }
            
            first = false ;				
            if ( ! secstruc) {
                feat = getNewFeat(currentFeatureMap);		
            }
            
            //}
            
            
            if (type.equals("STRAND")){
                secstruc = true ;
                currentFeatureMap.put("color",STRAND_COLOR);
                currentFeatureMap.put("colorTxt","yellow");
                feat.setName("SECSTRUC");		
            }
            
            else if (type.equals("HELIX")) {
                secstruc = true ;
                currentFeatureMap.put("color",HELIX_COLOR);
                currentFeatureMap.put("colorTxt","red");
                feat.setName("SECSTRUC");
            }	
            
            else if (type.equals("TURN")) {
                secstruc = true ;
                currentFeatureMap.put("color",TURN_COLOR);
                currentFeatureMap.put("colorTxt","white");
                feat.setName("SECSTRUC");
            } 	  
            else {
                secstruc = false ;
                currentFeatureMap.put("color"   ,entColors[i%entColors.length]);
                currentFeatureMap.put("colorTxt",txtColors[i%txtColors.length]);
                try {
                    feat.setName(type);
                } catch ( NullPointerException e) {
                    //e.printStackTrace();
                    feat.setName("null");
                }
            }
            
            segment = getNewSegment(currentFeatureMap);
            feat.addSegment(segment);	    
            //feat.addSegment(currentFeatureMap);
            prevtype = type;
        }	
        if ( ! (feat==null))  features.add(feat);
        
        return (Feature[])features.toArray(new Feature[features.size()]) ;
    }

  */  
    
    
    
    private Segment getNewSegment(Map featureMap) {
        Segment s = new Segment();
        String sstart = (String)featureMap.get("START") ;
        String send   = (String)featureMap.get("END")   ;
        int start = Integer.parseInt(sstart) ;
        int end   = Integer.parseInt(send)   ;
        s.setStart(start);
        s.setEnd(end);
        s.setName((String)featureMap.get("TYPE"));
        s.setTxtColor((String)featureMap.get("colorTxt"));	
        s.setColor((Color)featureMap.get("color"));
        return s ;
        
    }
    
    
    
}


class DasResponse{
    boolean finished  ;
    List features ;
    String coordinateSystem;
    
    SpiceDasSource dassource ;
    
    public DasResponse(String coordSys) {
        finished = false ;
        features = new ArrayList();	
        coordinateSystem = coordSys ;
        dassource = null;
        
    }
    
    public String toString() {
        String str = "DasResponse: "+ finished + " " + coordinateSystem + " features: " + features.size(); 
        return str ;
    }
    
    public void setDasSource(SpiceDasSource ds){
        dassource = ds;
    }
    
    public SpiceDasSource getDasSource() {
        return dassource;
    }
    public String getCoordinateSystem() {
        return coordinateSystem ;
    }
    
    public boolean isFinished() {
        return finished ;
    }
    
    public synchronized void setFeatures(List feats) {
        //logger.finest(feats);
        // sort features ...
        finished = true ;
        Map[] featarr = (Map[]) feats.toArray(new Map[feats.size()]);
        FeatureMapComparator comp = new FeatureMapComparator();
        java.util.Arrays.sort(featarr,comp) ; 
        
        List sortfeats = java.util.Arrays.asList(featarr);
        //logger.finest(sortfeats); 
        features = sortfeats; 
        
        //features = feats;
        
        notifyAll();
    }
    public List getFeatures() {
        return features ;
    }
    
    
    
    
}




