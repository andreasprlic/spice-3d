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

package org.biojava.spice.DAS ;

import org.biojava.spice.DAS.*		   ;
import org.biojava.spice.manypanel.drawable.*;
import org.biojava.spice.manypanel.eventmodel.*;
import java.util.logging.*             ;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List             ;
import java.util.Map;
import java.net.MalformedURLException;
import java.net.URL                    ;
import org.biojava.spice.Config.*;

//import java.util.Iterator;

/** a thread that connects to a DAS - Feature service and gets the features
 * 
 * @author Andreas Prlic
 */

public class SingleFeatureThread
extends Thread 

{
    URL dascommand ;    
  
    
    SpiceDasSource dasSource;
    static int MAX_NR_FEATURES = 300;
    List dasSourceListeners;
    List featureListeners;
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    /** contact a single DAS feature server and retreive features 
     @param parent a link to the parent class
     @param urlstring string of server
     @param threadid id for this thread, if job is finished parent
     is told that threadid has finised
     */
    public SingleFeatureThread( String accessionCode, SpiceDasSource ds) {
       
        //logger.finest("init new thread " +  " " + accessionCode);
       
        String url = ds.getUrl() ;
        char lastChar = url.charAt(url.length()-1);        
        if ( ! (lastChar == '/') ) 
            url +="/" ;
        
        String queryString = url + "features?segment="+ accessionCode ;
        URL Url = null ;
        try {
            Url = new URL(queryString);
        } catch (MalformedURLException e ) {
            logger.warning("got MalformedURL from das source " +ds);
            e.printStackTrace();
           
        }
        
        dascommand = Url ;
        
        
        dasSource = ds;
    
        clearFeatureListeners();
        clearDasSourceListeners();
    }
    
    public void clearFeatureListeners(){
        featureListeners = new ArrayList();
    }
    
    public void clearDasSourceListeners(){
        dasSourceListeners = new ArrayList();
    }
    
    
    public void addFeatureListener(FeatureListener li){
        featureListeners.add(li);
    }
    
    public void addDasSourceListener(DasSourceListener li){
     dasSourceListeners.add(li);   
    }
    
    /** start thread */
    public void run() {
        doDasConnection();
        
    }
    
    private synchronized void doDasConnection() {
        if ( dascommand == null){
            
            return;
        }
        
        notifyLoadingStarted();
        
                
        //logger.finer("opening " + dascommand);
        DAS_FeatureRetrieve ftmp = new DAS_FeatureRetrieve(dascommand);
        List features = ftmp.get_features();
        
        // a fallback mechanism to prevent DAS sources from bringing down spice
        if ( features.size() > MAX_NR_FEATURES){
            logger.warning("DAS source returned more than " + MAX_NR_FEATURES + "features. " +
                    	" throwing away excess features at " +dascommand);
            features = features.subList(0,MAX_NR_FEATURES);
        }
        
              
        // notify FeatureListeners
        Map[] feats = (Map[])features.toArray(new Map[features.size()]);
        notifyFeatureListeners(feats);
                
        // now with support for stylesheets.
        
        Map[] typeStyle = dasSource.getStylesheet();
        // is null if no stylesheet has been loaded ...
        if ( typeStyle == null){
            
            dasSource.loadStylesheet();
            typeStyle = dasSource.getStylesheet();
            for ( int m=0; m< typeStyle.length;m++){
                logger.finest("got stylesheet: " + typeStyle[m]);    
            }
        }
               
        
        notifyLoadingFinished();
    }
 
    
    private void notifyFeatureListeners(Map[] feats){

        FeatureEvent fevent = new FeatureEvent(feats);
        Iterator fiter = featureListeners.iterator();
        while (fiter.hasNext()){
            FeatureListener fi = (FeatureListener)fiter.next();
            fi.newFeatures(fevent);
        }
    }
    
    private void notifyLoadingStarted(){

        DrawableDasSource drawableDs = new DrawableDasSource(dasSource);
        DasSourceEvent dsEvent = new DasSourceEvent(drawableDs);
        Iterator iter = dasSourceListeners.iterator();
        while (iter.hasNext()){
            DasSourceListener li = (DasSourceListener) iter.next();
            li.loadingStarted(dsEvent);
        }
    }
    
    private void notifyLoadingFinished(){
        DrawableDasSource drawableDs = new DrawableDasSource(dasSource);
        DasSourceEvent dsEvent = new DasSourceEvent(drawableDs);
        Iterator iter = dasSourceListeners.iterator();
        while (iter.hasNext()){
            DasSourceListener li = (DasSourceListener) iter.next();
            li.loadingFinished(dsEvent);
        }
    }
}
