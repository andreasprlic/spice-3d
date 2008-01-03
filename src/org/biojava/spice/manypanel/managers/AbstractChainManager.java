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
 * Created on Nov 20, 2005
 *
 */
package org.biojava.spice.manypanel.managers;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.biojava.dasobert.dasregistry.DasCoordinateSystem;
import org.biojava.dasobert.eventmodel.*;
import org.biojava.spice.das.SpiceDasSource;
import org.biojava.spice.manypanel.drawable.DrawableDasSource;

public abstract class AbstractChainManager 
implements ObjectManager {
    
    //FeatureManager featureManager;
   
    
    SpiceDasSource[] dasSources ;
   
    DasCoordinateSystem coordinateSystem;
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    List sequenceListeners;
    
    String accessionCode;
    
    public AbstractChainManager() {
        super();
       
        //featureManager = new FeatureManager(); 
        accessionCode = "";
        clearSequenceListeners();
        clearDasSources();
    }
    
   
    
    public String getAccessionCode(){
        return accessionCode;
    }
    
    public void setAccessionCode(String ac){
        accessionCode = ac;
    }
    
    public void clear() {
        
    }
    
    public void clearSequenceListeners(){
        sequenceListeners = new ArrayList();
    }
    
    public void addSequenceListener(SequenceListener li){
        sequenceListeners.add(li);
    }
    
    public void removeSequenceListener(SequenceListener li){
        sequenceListeners.remove(li);
    }
    
    //public FeatureManager getFeatureManager(){
    //    return featureManager;
    //}
    
    //public void setFeatureManager(FeatureManager fm){
    //    featureManager = fm;
    //}
    
   
    
    public SpiceDasSource[] getDasSources() {
        
        return dasSources;
    }
    
    public DasCoordinateSystem getCoordinateSystem(){
        return coordinateSystem;
    }
    
    public void setCoordinateSystem(DasCoordinateSystem coordSys) {
        
        coordinateSystem = coordSys;
    }
    
    /** add reference DAS source
     * 
     */
    public void setDasSources(SpiceDasSource[] sources) {
    	if ( logger.isLoggable(Level.FINEST)) {
    		logger.finest("setting new DAS sources " + sources.length + " previously known: " + dasSources.length);
    	}
        List dsses = new ArrayList();
        for ( int i = 0 ; i< sources.length; i++){
            
            
            SpiceDasSource ds = sources[i];
            DasCoordinateSystem[] dcsarr = ds.getCoordinateSystem();
            boolean knownCoordinateSystem = false;
            for ( int  k =0 ; k < dcsarr.length; k++) {
                DasCoordinateSystem tmp = dcsarr[k];
                if ( tmp.toString().equals(coordinateSystem.toString())) {
                    knownCoordinateSystem = true;
                    break;
                }
                
            }
            if ( ! knownCoordinateSystem ) {
                // this das source does not fit here ...
                continue;
            }
            
            boolean known = false;
            //System.out.println("comparing with dasSources " + dasSources.length);
            for ( int j = 0 ; j < this.dasSources.length; j++){
                SpiceDasSource knownDS = this.dasSources[j];
                //System.out.println(ds.getUrl() + " " + knownDS.getUrl() );
                if ( ds.getUrl().equals(knownDS.getUrl())) {
                    dsses.add(knownDS);   
                    //logger.info(knownDS.getDasSource().getNickname() +  " is a known DAS source");
                    known = true;
                    break;
                }
            }
            
            if ( ! known )
                dsses.add(ds);
        
        }
        SpiceDasSource[] drawableDasSources = (SpiceDasSource[]) dsses.toArray(
                new SpiceDasSource[dsses.size()]);
        
      
        this.dasSources = drawableDasSources; 
    }
    
    public void clearDasSources(){
        //logger.info("abstrachChainManager clearDasSources");
        dasSources = new SpiceDasSource[0];
    }
    
    protected SpiceDasSource[] toSpiceDasSource( DrawableDasSource[] dds ){
        if (dds == null) {
            return new SpiceDasSource[0];
        }
        List sources = new ArrayList();
        for (int i = 0 ; i< dds.length; i++){
            DrawableDasSource draw = dds[i];
            SpiceDasSource ds = draw.getDasSource();
            sources.add(ds);
        }
        
        return (SpiceDasSource[])sources.toArray(new SpiceDasSource[sources.size()]);
    }
  
}


