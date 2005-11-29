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

import org.biojava.bio.structure.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.*;

import org.biojava.spice.das.SpiceDasSource;
import org.biojava.spice.das.StructureThread;
import org.biojava.spice.manypanel.drawable.*;
import org.biojava.spice.manypanel.eventmodel.*;
import org.biojava.spice.manypanel.renderer.*;


/** a manager class to manage to load structure data and convert them into a Drawable
 * 
 * @author Andreas Prlic
 *
 */
public class StructureManager
extends AbstractChainManager
implements ObjectManager, StructureListener {
    
   
    List structureRenderers;
    List structureListeners;
    
    String pdbCode;
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    public StructureManager() {
        super();
        structureRenderers = new ArrayList();
        pdbCode ="";
        structureListeners = new ArrayList();
    }
    
    public void clearDasSources(){
        super.clearDasSources();
        if ( structureRenderers == null ) {
            return;
        }
        Iterator iter = structureRenderers.iterator();
        while (iter.hasNext()){
            StructureRenderer rend = (StructureRenderer)iter.next();
            rend.clearDasSources();
        }
    }
    
    public void addStructureRenderer(StructureRenderer renderer){
        structureRenderers.add(renderer);
        //renderer.setDasSource(dasSources);
    }
    
    public void addStructureListener(StructureListener li){
        structureListeners.add(li);
    }
    
    /** a new structure should be loaded
     * trigger the loading threads.
     */
    public void newObjectRequested(String accessionCode) {
        
        logger.info("newObjectRequested " + accessionCode);
        
        String[] spl = accessionCode.split("\\.");
       String code ="";
        if ( spl.length < 1)    
            code = accessionCode;
        else 
            code=spl[0];
                      
                      
        if ( pdbCode.equals(code)){
            // this structure is already displayed, do nothing...
            return;
        }
        SpiceDasSource[] sds = toSpiceDasSource(dasSources);
        StructureThread dsh = new StructureThread(code,sds);
        dsh.addStructureListener(this);      
        Iterator iter = structureListeners.iterator();
        while (iter.hasNext()){
            StructureListener li = (StructureListener)iter.next();
            dsh.addStructureListener(li);
        }
        //dsh.addObjectListener(featureManager);
        dsh.start();
        //featureManager.newObjectRequested(accessionCode);
       
        
    }
    
    
    public void newObject(Object object){
        if ( object instanceof Structure) {
            Structure s = (Structure)object;
            drawStructure(s);
            pdbCode = s.getPDBCode();
        }
    }
    /** a new Structure has been retrieved
     * 
     */
    public void newStructure(StructureEvent event) {
        
        logger.info("got new structure " + event.getPDBCode());
        
        // convert structure to drawable structure ...
        Structure s = event.getStructure();
        drawStructure(s);
        Chain c = s.getChain(event.getCurrentChainNumber());
        
        String code = event.getPDBCode() ;
        code = code.toLowerCase();
        code += "."+c.getName();
        /*if ( ! c.getName().equals(" "))
            code +=  "." + c.getName();
        else 
            code +=".";
        */
        
        SequenceEvent sevent = new SequenceEvent(code,c.getSequence());
        
        //featureManager.newSequence(sevent);
        
        Iterator iter = sequenceListeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
         
            li.newSequence(sevent);
        }
    }
    
    private void drawStructure(Structure struc){
        DrawableStructure draw = new DrawableStructure();
        draw.setStructure(struc);
        draw.setLoading(false);
        Iterator iter = structureRenderers.iterator();
        
        while (iter.hasNext()){
            StructureRenderer renderer = (StructureRenderer)iter.next();
            
            renderer.setDrawableStructure(draw);
        }
    }
    
    public void selectedChain(StructureEvent event) {
        //int nr = event.getCurrentChainNumber();
        
        // change the displayed sequence ...
    }
    
    
    
   
    
    
}
