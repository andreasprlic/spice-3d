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

import org.biojava.services.das.registry.DasSource;
import org.biojava.spice.Config.SpiceDasSource;
//import org.biojava.servlets.dazzle.datasource.GFFFeature;
import org.biojava.spice.Feature.*;
import org.biojava.spice.manypanel.eventmodel.*;

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
    
    public static final  Color[] entColors = new Color []{
        new Color(51,51,255), // blue
        new Color(255,153,153), // pink
        new Color(153,255,153), // green
        new Color(255,255,102), //yellow
        new Color(255,51,51),   // red
        new Color(102,255,255),    // cyan
        new Color(255,51,255)    // pink 
    };


    public static final String[] txtColors = new String[] { "blue","pink","green","yellow","red","cyan","pink"};

    
    
    public static final Color HELIX_COLOR  = new Color(255,51,51);
    public static final Color STRAND_COLOR = new Color(255,204,51);
    public static final Color TURN_COLOR   = new Color(204,204,204); 
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    boolean loading;
    List featureListeners;
    
    
    public DrawableDasSource(DasSource ds) {
        super();
        SpiceDasSource sds = SpiceDasSource.fromDasSource(ds);
        this.dasSource = sds;   
        features = new Feature[0];
        clearFeatureListeners();
        loading = false;
    }

    public static DrawableDasSource fromDasSource(DasSource ds){
        DrawableDasSource  dds = new DrawableDasSource(ds);
        
        return dds ;
    }
    public void clearFeatureListeners(){
        
        featureListeners = new ArrayList();
    }
    public void addFeatureListener(FeatureListener feat){
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

    
    public void newFeatures(FeatureEvent e){
        Map[] feats = e.getFeatures();
        //logger.info("go " + feats.length + " features " +
         //       featureListeners.size() + " featureListeners");
        features = convertMap2Features(feats);
        
        Iterator iter = featureListeners.iterator();
        while (iter.hasNext()){
            FeatureListener featL = (FeatureListener)iter.next();
            FeatureEvent event = new FeatureEvent(feats);
            featL.newFeatures(event);
        }
        
    }
    
    
    
    public void featureSelected(FeatureEvent e) {
        // TODO Auto-generated method stub
        
    }

    public Feature[] getFeatures(){
        return features;
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
            featuresCounter +=1;
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