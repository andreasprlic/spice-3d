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
import java.util.logging.Logger;


import org.biojava.services.das.registry.DasCoordinateSystem;
import org.biojava.services.das.registry.DasSource;
import org.biojava.spice.das.SpiceDasSource;
import org.biojava.spice.manypanel.drawable.DrawableDasSource;
import org.biojava.spice.manypanel.eventmodel.*;

public abstract class AbstractChainManager 
implements ObjectManager {
    
    //FeatureManager featureManager;
   
    
    DrawableDasSource[] dasSources ;
   
    DasCoordinateSystem coordinateSystem;
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    List sequenceListeners;
    
    public AbstractChainManager() {
        super();
       
        //featureManager = new FeatureManager(); 
    
        clearSequenceListeners();
        clearDasSources();
    }
    
    public void clear() {
        
    }
    
    public void clearSequenceListeners(){
        sequenceListeners = new ArrayList();
    }
    
    public void addSequenceListener(SequenceListener li){
        sequenceListeners.add(li);
    }
    
    
    //public FeatureManager getFeatureManager(){
    //    return featureManager;
    //}
    
    //public void setFeatureManager(FeatureManager fm){
    //    featureManager = fm;
    //}
    
   
    
    public DrawableDasSource[] getDasSources() {
        
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
    public void setDasSources(DasSource[] dasSources) {
        
        List dsses = new ArrayList();
        for ( int i = 0 ; i< dasSources.length; i++){
            // TODO: add check that only a DAS source of the correct coordSys is being added
            DrawableDasSource ds = DrawableDasSource.fromDasSource(dasSources[i]);
            dsses.add(ds);
        }
        DrawableDasSource[] drawableDasSources = (DrawableDasSource[]) dsses.toArray(
                new DrawableDasSource[dsses.size()]);
        
        //Iterator iter = structureRenderers.iterator();
        
        //while (iter.hasNext()){
          //  StructureRenderer renderer = (StructureRenderer)iter.next();
            
            //renderer.setDasSource(drawableDasSources);
       // }
        this.dasSources = drawableDasSources; 
    }
    
    public void clearDasSources(){
        dasSources = new DrawableDasSource[0];
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


