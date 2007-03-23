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

package org.biojava.spice.das ;


import org.biojava.dasobert.das.DAS_FeatureRetrieve;
import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.dasregistry.DasSource;
import org.biojava.dasobert.eventmodel.*;
import org.biojava.spice.config.SpiceDefaults;
import org.biojava.spice.manypanel.drawable.*;
import org.biojava.spice.manypanel.eventmodel.DasSourceEvent;
import org.biojava.spice.manypanel.eventmodel.DasSourceListener;
import org.biojava.spice.manypanel.eventmodel.FeatureListener;

import java.util.logging.*             ;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List             ;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL                    ;
import java.net.URLEncoder;

//import java.util.Iterator;

/** a thread that connects to a DAS - Feature service and gets the features
 * 
 * @author Andreas Prlic
 */

public class SingleFeatureThread
extends Thread 

{
    /** number of times the client tries to reconnect to the server if a "come back later" is returned.
     * the server should provide a reasonable estimation how long it will take him to create results.
     * if this number of requests is still not successfull, give up.
     */
    public static int MAX_COME_BACK_ITERATIONS = 5;

    URL dascommand ;    


    SpiceDasSource dasSource;
    static int MAX_NR_FEATURES = 300;
    List dasSourceListeners;
    List featureListeners;

    static Logger logger = Logger.getLogger(SpiceDefaults.LOGGER);

    /** contact a single DAS feature server and retreive features
     * 
     * @param accessionCode the accession code for which features should be fetched
     * @param ds the das source to be used
     */
    public SingleFeatureThread( String accessionCode, SpiceDasSource ds) {

        //logger.finest("init new thread " +  " " + accessionCode);


        String url = ds.getUrl() ;
        char lastChar = url.charAt(url.length()-1);        
        if ( ! (lastChar == '/') ) 
            url +="/" ;

        // if this is a PDB code, check for empty chain.


        if ( accessionCode.substring(4,5).equals(".") ){
            if ( accessionCode.substring(5,6).equals(" ")){
                // found a PDB code with empty chain.
                // change to PDB code only.
                accessionCode = accessionCode.substring(0,4);
            }
        }


        String queryString = "";
        try {
            queryString = url + "features?segment=" + URLEncoder.encode(  accessionCode, "UTF-8");
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }

        URL Url = null ;
        try {

            Url = new URL(queryString);
        } catch (MalformedURLException e ) {
            logger.warning("got MalformedURL from das source " +ds);
            e.printStackTrace();

        }
        logger.finest("setting feature request url " + Url);
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

        //System.out.println(dascommand);
        logger.finest("do feature fetch from " + dascommand);
        DAS_FeatureRetrieve ftmp = new DAS_FeatureRetrieve(dascommand);




        int comeBackLater = ftmp.getComeBackLater();
        int securityCounter = 0;
        while ( comeBackLater > 0 ) {
            securityCounter++;
            if ( securityCounter >= MAX_COME_BACK_ITERATIONS){
                comeBackLater = -1; 
                break;

            }
            notifyComeBackLater(comeBackLater);

            // server is still calculating - asks us to come back later
            try {
                wait (comeBackLater);
            } catch (InterruptedException e){
                comeBackLater = -1;
                break;
            }

            ftmp.reload();
            comeBackLater = ftmp.getComeBackLater(); 
        }

        List features = ftmp.get_features();

        int cutoff = MAX_NR_FEATURES;


        if ( dasSource.getNickname().equals("KyleDoolittle")){
            // this should be a histogram DAS source!
            // set maximum to unlimited
            cutoff = -1;
        }


        // a fallback mechanism to prevent DAS sources from bringing down spice
        if ( cutoff > 0) {
            if ( features.size() > cutoff){
                logger.warning("DAS source returned more than " + MAX_NR_FEATURES + "features. " +
                        " throwing away excess features at " +dascommand);
                features = features.subList(0,MAX_NR_FEATURES);
            }
        }


        // notify FeatureListeners
        Map[] feats = (Map[])features.toArray(new Map[features.size()]);
        notifyFeatureListeners(feats);

        // now with support for stylesheets.        
        Map[] typeStyle = dasSource.getStylesheet();
        // is null if no stylesheet has been loaded ...
        if ( typeStyle == null){
            doStyleSheetRequest();

        }


        notifyLoadingFinished();
    }


    private void doStyleSheetRequest(){

        dasSource.loadStylesheet();
        Map[] typeStyle = dasSource.getStylesheet();
        for ( int m=0; m< typeStyle.length;m++){
            logger.finest("got stylesheet: " + typeStyle[m]);    
        }
    }

    /** the Annotation server requested to be queried again in a while
     * 
     * @param comeBackLater
     */
    private void notifyComeBackLater(int comeBackLater){
        FeatureEvent event = new FeatureEvent(new HashMap[0],dasSource);
        event.setComeBackLater(comeBackLater);
        Iterator fiter = featureListeners.iterator();
        while (fiter.hasNext()){
            FeatureListener fi = (FeatureListener)fiter.next();
            fi.comeBackLater(event);
        }

    }

    private void notifyFeatureListeners(Map[] feats){
        logger.finest("SingleFeatureThread found " + feats.length + " features");
        FeatureEvent fevent = new FeatureEvent(feats,dasSource);
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
