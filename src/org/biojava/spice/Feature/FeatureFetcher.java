
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

package org.biojava.spice ;

import java.util.Map ;
import java.util.HashMap ;
import java.util.List  ;
import java.util.ArrayList ;
import java.net.URL;
import java.net.MalformedURLException;

import java.awt.Color ;

import org.biojava.bio.structure.Chain ;
import org.biojava.bio.structure.Group ;

//import org.biojava.services.das.registry.DasSource;

/** a class to fetch all Features  in parallel threads

  * @author Andreas Prlic
  */
public class FeatureFetcher extends Thread
{
    SPICEFrame parent ;
    boolean finished ;
    String spId ;
    String pdbId ;
    RegistryConfiguration spiceconfig ;
    List allFeatures ;
    DasResponse[] subthreads ;
    Chain chain ;
    
    String[] txtColors;
    Color [] entColors ;

    /** @param config the SPICE config Map
     * @param sp_id SwissProt ID
     * @param pdb_id PDB ID
     * @param c Chain object to which these features should be linked
     */
    public FeatureFetcher(SPICEFrame spice, RegistryConfiguration config, String sp_id, String pdb_id, Chain c ) {
	parent      = spice ;
	finished    = false ;
	spId        = sp_id ;
	pdbId       = pdb_id ;
	spiceconfig = config ;
	allFeatures = new ArrayList();
	chain       = c ;

	entColors = new Color [7];
	entColors[0] = Color.blue;
	entColors[1] = Color.pink;
	entColors[2] = Color.green;
	entColors[3] = Color.magenta;
	entColors[4] = Color.orange;
	entColors[5] = Color.pink;
	entColors[6] = Color.cyan;

	txtColors   = new String[] { "blue","pink","green","magenta","orange","pink","cyan"};

    }


    public boolean isDone() {
	return finished ;
    }
    
    /** start one thread per server to fetch all the features!
     */
    public void run() {

	doDasCommunication() ;
	
	while ( ! finished) {	    
	    //System.out.println("waiting for features to be retreived: "+done);
	    try {
		wait(300);
	    } catch (InterruptedException e) {
		e.printStackTrace();
		finished = true ;
	    }
	    System.out.println("FeatureFetcher :in waitloop");
	}
	List l = getFeatures();
	parent.setFeatures(spId,l);
    }

    private synchronized void doDasCommunication() {
	
	finished = false ;
	// contact sequence feature servers
	List featservs    =  spiceconfig.getServers("features","UniProt");
	List pdbresservs  =  spiceconfig.getServers("features","PDBresnum");
	
	
	int nrservers = featservs.size() + pdbresservs.size();
	System.out.println("total: " + nrservers + "feature servers");
	subthreads = new DasResponse[nrservers];
	
	int responsecounter = 0 ;
	// start all the sub -threads ;
	for ( int f =0;f<featservs.size();f++) {
	    DasResponse d=new DasResponse("UniProt");
	    subthreads[responsecounter] = d; 
	   
	    SpiceDasSource featureserver = (SpiceDasSource) featservs.get(f) ;
	    String url = featureserver.getUrl();
	    String queryString = url + "features?segment="+ spId ;
	    URL spUrl = null ;
	    try {
		spUrl = new URL(queryString);
	    } catch (MalformedURLException e ) {
		e.printStackTrace();
		ArrayList empty = new ArrayList();
		setFinished(responsecounter,empty);
		continue ;
	    }
	    System.out.println("starting thread");
	    SingleFeatureThread sft = new SingleFeatureThread ( this ,spUrl,responsecounter);
	    sft.run();
	    responsecounter++;
	}

	// and the servers serving in structure coordinates
	for ( int f =0;f<pdbresservs.size();f++) {
	    DasResponse d=new DasResponse("PDBresnum");
	    subthreads[responsecounter] = d; 
	   
	    SpiceDasSource featureserver = (SpiceDasSource) pdbresservs.get(f) ;
	    String url = featureserver.getUrl();
	    String queryString = url + "features?segment="+ pdbId+chain.getName() ;
	    URL spUrl = null ;
	    try {
		spUrl = new URL(queryString);
	    } catch (MalformedURLException e ) {
		e.printStackTrace();
		ArrayList empty = new ArrayList();
		setFinished(responsecounter,empty);
		continue ;
	    }
	    System.out.println("starting thread");
	    SingleFeatureThread sft = new SingleFeatureThread ( this ,spUrl,responsecounter);
	    sft.run();
	    responsecounter++;
	}


	boolean done = false ;
	while ( ! done) {
	    done = allFinished();
	    System.out.println("FeatureFetcher waiting for features to be retreived: "+done);
	    try {
		wait(300);
	    } catch (InterruptedException e) {
		e.printStackTrace();
		done = true ;
	    }
	    //System.out.println("getNewFeatures :in waitloop");
	}


	// now: extract the features and convert them so they can be used for spice...
	for ( int i = 0 ; i < subthreads.length; i++ ) {
	    DasResponse d = subthreads[i] ;
	    String coordSys = d.getCoordinateSystem();
	    //System.out.println(d);
	    List features   = d.getFeatures() ;
	    if ( coordSys.equals("UniProt")) {
		for (int j=0; j<features.size();j++){
		    HashMap feat = (HashMap)features.get(j);			
		    //Feature feat = (Feature)features.get(j);			
		    //System.out.println("got feature: "+feat);
		    allFeatures.add(feat) ;		
		} 
	    } else if ( coordSys.equals("PDBresnum")) {
		// covnert PDB resnum coordinates to UniProt coordinates ;
		for (int j=0; j<features.size();j++){
		    HashMap feat = (HashMap)features.get(j);			
		    //Feature feat = (Feature)features.get(j);			
		    
		    //allFeatures.add(feat) ;		
		    String startOrig = (String)feat.get("START");
		    String endOrig   = (String)feat.get("END");
		    //System.out.println("pdbresnum feature: "+feat);
		    String startNew  = getUniProtCoord(startOrig,chain);
		    String endNew    = getUniProtCoord(endOrig,chain);
		    feat.put("START",startNew);
		    feat.put("END",endNew);
		    //System.out.println("uniprot feature: "+feat);
		    allFeatures.add(feat) ;
		    
		} 
	    }
	}

	finished = true ;
       	notifyAll();
    }
    
    /** browse through chain and get UniProt position with pdbResNum */
    private String getUniProtCoord(String pdbResNumb, Chain chain) {

	ArrayList groups = chain.getGroups();

	for (int i=0 ; i<groups.size();i++){
	    Group g = (Group)groups.get(i);
	    

	    String pdbCode = g.getPDBCode() ;
	    if ( g.has3D()){
		//System.out.println(g);
		if ( pdbCode.equals(pdbResNumb)) {
		    return "" + ( i+1);
		}
	    }
	    
	}
	return "" + chain.getLengthAminos();

    }

    /** if a sub-thread has finished this procedure is called and the
     * features for this thread are set */
    public synchronized void setFinished(int threadId, List features) {
	//System.out.println("Got "+ features.size()+ " features from " + threadId);
	DasResponse d = subthreads[threadId] ;
	d.setFeatures(features);
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

    public List getFeatures() {
	// convert Map containing features into feature objects...
	
	List spicefeatures = convertMap2Features(allFeatures);

	return spicefeatures ;
	//return allFeatures;
    }


    private Feature getNewFeat(Map currentFeatureMap) {
	Feature feat = new Feature();
	//System.out.println(currentFeatureMap);
	feat.setSource((String)currentFeatureMap.get("dassource"));
	feat.setName(  (String)currentFeatureMap.get("NAME"));
	feat.setType(  (String)currentFeatureMap.get("TYPE"));
	feat.setLink(  (String)currentFeatureMap.get("LINK"));
	feat.setNote(  (String)currentFeatureMap.get("NOTE"));
	feat.setMethod((String)currentFeatureMap.get("METHOD"));
	return feat ;
    }

  /** returns a list of SPICE-Features objects */
    public List convertMap2Features(List mapfeatures){
	List features = new ArrayList();

	boolean secstruc = false ;
	String prevtype = "@prevtype" ;
	boolean first = true ;
	
	Feature feat    = null ;
	Segment segment = null ;
	

	for (int i = 0 ; i< mapfeatures.size();i++) {
	    HashMap currentFeatureMap = (HashMap) mapfeatures.get(i);
	    String type = (String) currentFeatureMap.get("TYPE") ;

	    // we are skipping literature references for teh moment 
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
			    if ( ! (feat==null))  features.add(feat);
			    
			    //feat    = getNewFeat(currentFeatureMap);
			    //segment = getNewSegment(currentFeatureMap);
			    //feat.addSegment(segment);
			    //features.add(feat);
			}
		}
	    
	    first = false ;				
	    if ( ! secstruc) {
		feat = getNewFeat(currentFeatureMap);		
	    }
		
	    //}
	    
	    
	    if (type.equals("STRAND")){
		secstruc = true ;
		currentFeatureMap.put("color",Color.yellow);
		currentFeatureMap.put("colorTxt","yellow");
		feat.setName("SECSTRUC");		
	    }
	    
	    else if (type.equals("HELIX")) {
		secstruc = true ;
		currentFeatureMap.put("color",Color.red);
		currentFeatureMap.put("colorTxt","red");
		feat.setName("SECSTRUC");
	    }	

	    else if (type.equals("TURN")) {
		secstruc = true ;
		currentFeatureMap.put("color",Color.white);
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
	
	return features ;
    }


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
    
   

    public DasResponse(String coordSys) {
	finished = false ;
	features = new ArrayList();	
	coordinateSystem = coordSys ;


    }

     public String toString() {
	String str = "DasResponse: "+ finished + " " + coordinateSystem + " features: " + features.size(); 
	return str ;
     }


    public String getCoordinateSystem() {
	return coordinateSystem ;
    }
    
    public boolean isFinished() {
	return finished ;
    }

    public synchronized void setFeatures(List feats) {
	//System.out.println(feats);
	// sort features ...
	Map[] featarr = (Map[]) feats.toArray(new Map[feats.size()]);
	FeatureMapComparator comp = new FeatureMapComparator();
	java.util.Arrays.sort(featarr,comp) ; 
	
	List sortfeats = java.util.Arrays.asList(featarr);
	//System.out.println(sortfeats); 
	features = sortfeats; 
	
	//features = feats;
	finished = true ;
	notifyAll();
    }
    public List getFeatures() {
	return features ;
    }


  

}




class SingleFeatureThread
    extends Thread {
    URL dascommand ;
    FeatureFetcher parentfetcher ;
    int myId ;
    /** contact a single DAS feature server and retreive features 
	@param parent a link to the parent class
	@param urlstring string of server
	@param threadid id for this thread, if job is finished parent
	is told that threadid has finised
     */
    public SingleFeatureThread(FeatureFetcher parent, URL dascmd,int threadid) {
	//System.out.println("init new thread " + threadid + " " + dascmd);
	dascommand = dascmd ;
	parentfetcher = parent ;
	myId = threadid ;
    }
    /** start thread */
    public void run() {
	doDasConnection();
    }

    private synchronized void doDasConnection() {
	
	DAS_FeatureRetreive ftmp = new DAS_FeatureRetreive(dascommand);
	ArrayList features = ftmp.get_features();
	//System.out.println("doDasConnection got " + features.size() + " features") ;
	//new ArrayList();
	//ArrayList tmp = 
	//for (int i=0; i<tmp.size();i++){
	//   HashMap feat = (HashMap)tmp.get(i);			
	    //System.out.println("got feature: "+feat);
	//    features.add(feat) ;		
	//} 
	//System.out.println("done "+ dascommand);
	parentfetcher.setFinished(myId,features);
	notifyAll();
    }

}
