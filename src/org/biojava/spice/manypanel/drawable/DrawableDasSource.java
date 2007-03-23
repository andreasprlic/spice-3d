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
 * Created on Nov 1, 2005
 *
 */
package org.biojava.spice.manypanel.drawable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.eventmodel.*;
//import org.biojava.servlets.dazzle.datasource.GFFFeature;
import org.biojava.spice.config.SpiceDefaults;
import org.biojava.spice.feature.*;
import org.biojava.spice.manypanel.eventmodel.FeatureListener;

/** all the data about a particular DAS source and the
 * Features associated with if ( and a particular accession code)
 * @author Andreas Prlic
 *
 */
public class DrawableDasSource 
implements Drawable,
FeatureListener{

    SpiceDasSource dasSource ;
    Feature[] features;
    
    String type;
    public static final String TYPE_HISTOGRAM = "histogram";
    public static final String TYPE_DEFAULT   = "default";
    
    public static final  Color[] entColors = new Color []{
        new Color(51,51,255), // blue
        new Color(102,255,255),    // cyan
        new Color(153,255,153), // green
        new Color(153,255,153), // green
        new Color(255,153,153), // pink
        new Color(255,51,51),   // red
        new Color(255,51,255)    // pink 
    };

    /** if the url of two das sources is the same
     *  this will return true;
     *  @param compareToSource a drawable das source to compare with
     *  @return a flag if the two are equal
     */
    public boolean equals(DrawableDasSource compareToSource){
        if ( dasSource.getUrl().equals(compareToSource.getDasSource().getUrl()))
            return true;
        return false;
    }
    
    public static final String[] txtColors = new String[] { "blue","pink","green","yellow","red","cyan","pink"};

    
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    boolean loading;
    List featureListeners;
    
    
    public DrawableDasSource(SpiceDasSource ds) {
        super();
        
        //SpiceDasSource sds = SpiceDasSource.fromDasSource(ds);
        setType(TYPE_DEFAULT);
        
        
        if ( ds.getDisplayType().equalsIgnoreCase(TYPE_HISTOGRAM)) { 
        	setType(TYPE_HISTOGRAM);
        	//logger.info(ds.getNickname() + " is a HISTOGRAM DAS SOURCE");
        } else {
        	//logger.info(ds.getNickname() + " is a DEFAULT DAS SOURCE " + ds.getDisplayType());
        }

        
       
                             
        this.dasSource = ds;   
        clearDisplay();
        clearFeatureListeners();
        loading = false;
       
    }
    
    
    public Feature convertEventFeatures(Feature[] features){
    	
    	// join all features to one where the segments contain the score...
    	Feature f = new FeatureImpl();
    	if ( features.length > 0){
    		f = (Feature)features[0].clone();
    		
    		for(int i = 1 ; i< features.length;i++){
    			Feature t = features[i];
    			List segments = t.getSegments();
    			Iterator iter = segments.iterator();
    			while (iter.hasNext()){
    				Segment s = (Segment) iter.next();  
    				Segment tmp = (Segment)s.clone();
    				f.addSegment(tmp);
    			}
    			
    		}
    		
    		
    	}
    	//logger.info("convert features for histogram data " +f );
    	return f;
    }

    public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public static DrawableDasSource fromDasSource(SpiceDasSource ds){
        DrawableDasSource  dds = new DrawableDasSource(ds);
       
        	
        
        return dds ;
    }
    public void clearFeatureListeners(){
        
        featureListeners = new ArrayList();
    }
    public void addFeatureListener(FeatureListener feat){
        if ( ! featureListeners.contains(feat))
            featureListeners.add(feat);
    }
    
    public SpiceDasSource getDasSource(){
        return dasSource;
    }
    
    public Color getColor() {
     
        return null;
    }

    public boolean getLoading() {
       
        return loading;
    }

    public void setColor(Color col) {
       
        
    }

    public void setLoading(boolean flag) {
        loading = flag;
        
    }
    
    public Map[] getStylesheet(){
        // TODO: load stylesheet into das source at some point...
        
        return dasSource.getStylesheet();
    }

    
    /** a way to set Features from the outside 
     * 
     * @param feats
     */
    public void setFeatures(Feature[] feats){
        features = feats;
    }
    
    public void newFeatures(FeatureEvent e){
        Map[] feats = e.getFeatures();
        //logger.info("got " + feats.length + " features " +
        //       featureListeners.size() + " featureListeners");
        features = convertMap2Features(feats);
        
        //logger.finest("joined to " + features.length + " features -check:"+feats.length  );
        // AbstractChainRenderer is listening to this and
        // needs to resize itself
        //TODO: this should not be part of the Drawable code but of the DasSourcePanel code...
        Iterator iter = featureListeners.iterator();
        while (iter.hasNext()){
            FeatureListener featL = (FeatureListener)iter.next();
            FeatureEvent event = new FeatureEvent(feats,dasSource);
            featL.newFeatures(event);
        }
        
    }
    
    public void comeBackLater(FeatureEvent e){
        //TODO: display that the server is calculating ...
    }
 
    public Feature[] getFeatures(){
    	
    		return features;
    }
    
    public void clearDisplay(){
        features = new FeatureImpl[0];
    }

    /** returns a list of SPICE-Features objects */
    private Feature[] convertMap2Features(Map[] mapfeatures){
        
        List features = new ArrayList();
        
        boolean secstruc = false ;
        //String prevtype = "@prevtype" ;
        boolean first = true ;
        
        FeatureImpl feat    = null ;
        Segment segment = null ;
        
        //boolean secstrucContained = false;
        //Feature secstrucfeature = new FeatureImpl() ;
        int featuresCounter = 0;
        for (int i = 0 ; i< mapfeatures.length;i++) {
            
            Map currentFeatureMap = mapfeatures[i];
            String type = (String) currentFeatureMap.get("TYPE") ;
            
            // we are skipping literature references for the moment 
            // TODO: add a display to spice for non-positional features
            //
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
                    }
                    
                }
            }
            
            first = false ;             
            if ( ! secstruc) {
                featuresCounter +=1;
                feat = getNewFeat(currentFeatureMap);       
            }
            
            //}
            
            
            if (type.equals("STRAND")){
                secstruc = true ;
                currentFeatureMap.put("color",SpiceDefaults.STRAND_COLOR);
                currentFeatureMap.put("colorTxt","yellow");
                feat.setName("SECSTRUC");       
                feat.setType("SECSTRUC");
            }
            
            else if (type.equals("HELIX")) {
                secstruc = true ;
                currentFeatureMap.put("color",SpiceDefaults.HELIX_COLOR);
                currentFeatureMap.put("colorTxt","red");
                feat.setName("SECSTRUC");
                feat.setType("SECSTRUC");
            }   
            
            else if (type.equals("TURN")) {
                secstruc = true ;
                currentFeatureMap.put("color",SpiceDefaults.TURN_COLOR);
                currentFeatureMap.put("colorTxt","white");
                
                feat.setName("SECSTRUC");
                feat.setType("SECSTRUC");
            }     
            else {
                secstruc = false ;
                currentFeatureMap.put("color"   ,entColors[featuresCounter%entColors.length]);
                currentFeatureMap.put("colorTxt",txtColors[featuresCounter%txtColors.length]);
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
            //prevtype = type;
        }   
        //if ( ! (feat==null))  features.add(feat);
        if ( ! (feat==null))  
            features =testAddFeatures(features,feat);
        
        
        Feature[] fs = new Feature[features.size()];
        Iterator iter = features.iterator();
        int i = 0;
        while (iter.hasNext()){
            Feature f = (Feature) iter.next();
            fs[i] = f;
            i++;
        }
        return fs;
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
    
    private boolean isHistogramFeatureType(Feature feat){
        String ftype = feat.getType();
        
        // todo : move this info into a config file...
        if ( ftype.equals("hydrophobicity")){
            return true;
        }
        if ( getType().equals(TYPE_HISTOGRAM) )
        	return true;
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
            if ( isHistogramFeatureType(feat))
                sameFeat = false;
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
    
    private FeatureImpl getNewFeat(Map currentFeatureMap) {
        FeatureImpl feat = new FeatureImpl();
        //logger.finest(currentFeatureMap);
        //System.out.println("DrawableDasSource " + currentFeatureMap);
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
        s.setNote((String) featureMap.get("NOTE"));
        return s ;
        
    }
    
    
    
    
}
