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
 * Created on Oct 31, 2005
 *
 */
package org.biojava.spice.manypanel.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.biojava.bio.structure.Structure;
import org.biojava.services.das.registry.DasCoordinateSystem;
import org.biojava.services.das.registry.DasSource;
import org.biojava.spice.das.SpiceDasSource;
import org.biojava.spice.das.SingleFeatureThread;
import org.biojava.spice.manypanel.drawable.DrawableDasSource;
import org.biojava.spice.manypanel.eventmodel.*;
import org.biojava.spice.manypanel.renderer.*;

import java.util.*;

/** takes care of dealing with Features - requests them from DAS and
 * converts them into Drawables
 * 
 * @author Andreas Prlic
 *
 */
public class FeatureManager 
implements ObjectManager ,SequenceListener{

    DasCoordinateSystem coordSys;
    DrawableDasSource[] dasSources;
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    List featureRenderers ;
    List dasSourceListeners;
    
    String currentAccessionCode ;
    
    public FeatureManager() {
        super();
        
        
        featureRenderers = new ArrayList();
        dasSourceListeners = new ArrayList();
        
        clear();
        clearDasSources();
        //dasSources = new DrawableDasSource[0];
    }
    
    public void clear() {
        currentAccessionCode = "";
       
    }
    
    public void clearDasSources(){
        dasSources = new DrawableDasSource[0];
        Iterator iter = featureRenderers.iterator();
        while (iter.hasNext()){
            FeatureRenderer rend = (FeatureRenderer)iter.next();
            rend.clearDasSources();
        }
    }

    public void addDasSourceListener(DasSourceListener dsl ){
        //logger.info("got new DasSourceListener " + dsl);
        dasSourceListeners.add(dsl);
    }
    
    public DasCoordinateSystem getCoordinateSystem() {
      
        return coordSys;
    }

    public DrawableDasSource[] getDasSources() {
      
        return dasSources;
    }

            
    
    public void addFeatureRenderer(FeatureRenderer rend){
        featureRenderers.add(rend);
    }
    
    
    
    
    
    public void setCoordinateSystem(DasCoordinateSystem coordSys) {
        this.coordSys = coordSys;
    }

    public void setDasSources(DasSource[] dasSourcs) {
        //logger.info("got " + dasSourcs.length + "feature sources for coordSYs " + coordSys);
        
        Iterator iter = featureRenderers.iterator();
        while (iter.hasNext()){
            FeatureRenderer rend = (FeatureRenderer)iter.next();
            rend.clearDasSources();
        }
        
        List dsses = new ArrayList();
        for ( int i = 0 ; i< dasSourcs.length; i++){
            //logger.info("got new feature source " + dasSourcs[i].getUrl());
            DasSource sds = dasSourcs[i];
            
            DrawableDasSource ds = DrawableDasSource.fromDasSource(sds);
            dsses.add(ds);
            //ds.setLoading(false);
            
            Iterator iter2 = featureRenderers.iterator();
            while (iter2.hasNext()){
                FeatureRenderer rend = (FeatureRenderer)iter2.next();
                
               rend.addDrawableDasSource(ds);
                
                
            }
            //logger.info("found " + dasSourceListeners.size() + "dasSourceListener");
            //DrawableDasSource dds = new DrawableDasSource(dasSourcs[i]);
            DasSourceEvent event = new DasSourceEvent(ds);
            Iterator iter3 = dasSourceListeners.iterator();
            while (iter3.hasNext()){
                DasSourceListener dsl = (DasSourceListener)iter3.next();
                dsl.newDasSource(event);
                
            }
            
        }
        DrawableDasSource[] drawableDasSources = (DrawableDasSource[]) dsses.toArray(
                new DrawableDasSource[dsses.size()]);
        
        this.dasSources = drawableDasSources;
        
        
        
    }


    /** a new object has been requested.
     * do feature requests.
     */
    public void newObjectRequested(String accessionCode) {
        
        // start feature requests
        // cache them until newObject has the object,
        // then notify Drawer ...
        
        triggerFeatureRequests(accessionCode);
        
    }

    public void noObjectFound(String accessionCode){
        
    }
    /** triggers DAS requests for each of the DAS sources
     * 
     *      * @param accessionCode
     * @return
     */
    private void triggerFeatureRequests(String accessionCode){
        //logger.info("triggerFeatureRequests");
        
        
        if (! accessionCode.equals(currentAccessionCode)){
            
        for ( int i = 0 ; i< dasSources.length; i++){
            DrawableDasSource ds = (DrawableDasSource)dasSources[i];
            SpiceDasSource sds = ds.getDasSource();
            //logger.info("triggering Feature request from " + sds);
            
            SingleFeatureThread thread = new SingleFeatureThread(accessionCode, sds);
            Iterator iter = dasSourceListeners.iterator();
            while (iter.hasNext()){
                DasSourceListener li = (DasSourceListener)iter.next();
                thread.addDasSourceListener(li);    
            }
            
            thread.addFeatureListener(ds);
            thread.start();
        }
        }
    }
    
    
    /** a new object has been returned
     * 
     */
    public void newObject(Object object) {
        
        if ( object instanceof Structure) {
            Structure struc = (Structure)object;
            String accessionCode = struc.getPDBCode();
            String chainId = struc.getChain(0).getName();
            
            
            triggerFeatureRequests(accessionCode + "." + chainId);
        }
        
    }

    public void newSequence(SequenceEvent e) {
      triggerFeatureRequests(e.getAccessionCode());  
    }

    public void selectedSeqPosition(int position) {
        // TODO Auto-generated method stub
        
    }

    public void selectedSeqRange(int start, int end) {
        // TODO Auto-generated method stub
        
    }

    public void selectionLocked(boolean flag) {
        // TODO Auto-generated method stub
        
    }
    
    public void clearSelection(){};
    
    

}
