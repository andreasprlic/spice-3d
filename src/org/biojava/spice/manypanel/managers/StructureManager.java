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
    int currentChainNr ;
    String pdbCode;
    static Logger logger = Logger.getLogger("org.biojava.spice");
    Structure structure ;
    
    String code;
    String chain;
    String requestedCode;
    
    public StructureManager() {
        super();
        structureRenderers = new ArrayList();
        pdbCode ="";
        structureListeners = new ArrayList();
        structure = new StructureImpl();
        currentChainNr = -1;
        code = "";
        chain = "";
        requestedCode = "" ;
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
        code ="";
        String oldchain = chain;
        chain = " ";
        if ( spl.length < 2)    
            code = accessionCode;
        else {
            code = spl[0];
            chain = spl[1];
        }        
           
        if ( code.equalsIgnoreCase(requestedCode)){
            chain = oldchain;
            return ;
        }
        requestedCode = code;
        logger.info("accession code " + accessionCode + " co " + code + " ch " + chain +
               " pdbcode " + pdbCode);
        
        if ( pdbCode.equalsIgnoreCase(code)){
            // this structure is already displayed, 
            // change the active chain
            // get the chain number
            int chainNumber = getActiveChainFromName(chain);
            //logger.info("chainNumber " + chainNumber);
            if ( chainNumber >= 0 ){
                StructureEvent event = new StructureEvent(structure, chainNumber);
                Iterator iter = structureListeners.iterator();
                while (iter.hasNext()){
                    StructureListener li = (StructureListener) iter.next();
                    li.selectedChain(event);
                }
            }
            return;
        }
        //logger.info("requesting new structure " + code);
        // a new structure should be loaded
        structure = new StructureImpl();
        
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
       
        Iterator iter2 = structureRenderers.iterator();
        while (iter2.hasNext()){
            StructureRenderer re = (StructureRenderer)iter2.next();
            re.getStatusPanel().setLoading(true);
        }
        
        
    }
    
   
    
    public int getActiveChainFromName(String name){
        List chains = structure.getChains(0);
        Iterator iter = chains.iterator();
        int i = 0 ;
        while (iter.hasNext()){
            Chain c = (Chain) iter.next();
            if (c.getName().equalsIgnoreCase(name) )
                return i;
            i++;
        }
                
        return -1;
        
    }
    
    public void newObject(Object object){
        //logger.info("new object " + object);
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
        
        if ( event.getPDBCode().equalsIgnoreCase(pdbCode)) {
            // we already have got this one ...
            return;
        }
            
        logger.info("got new structure " + event.getPDBCode() + " old " + pdbCode);
        
        // convert structure to drawable structure ...
        Structure s = event.getStructure();
        structure = s;
        drawStructure(s);
        
        
        currentChainNr = event.getCurrentChainNumber();
        if ( s.getPDBCode().equalsIgnoreCase(code)){
            if ( ! chain.equals("")){
                currentChainNr = getActiveChainFromName(chain);
            }
        }
        code = "";
        chain = "";
        if ( currentChainNr == -1)
            currentChainNr = 0;
        Chain c = s.getChain(currentChainNr);
        logger.info("got new structure - displaying chain " + c.getName());
        triggerNewSequence(c,event.getPDBCode());
    }
    
    public void noObjectFound(String accessionCode){
        // clear the display...
       
        Iterator iter = structureRenderers.iterator();
        while (iter.hasNext()){
            StructureRenderer rend = (StructureRenderer) iter.next();
            rend.clearDisplay();
        }
    }
    
    
    private void triggerNewSequence(Chain c,String code){
        //String code = event.getPDBCode() ;
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
        drawStructure(struc,0);
    }
    private void drawStructure(Structure struc,int currentChainId){
        DrawableStructure draw = new DrawableStructure(struc.getPDBCode());
        draw.setStructure(struc);
        draw.setCurrentChainNumber(currentChainId);
        draw.setLoading(false);
        Iterator iter = structureRenderers.iterator();
        
        while (iter.hasNext()){
            StructureRenderer renderer = (StructureRenderer)iter.next();
            renderer.setDrawableStructure(draw);         
        }
    }
    
    public void selectedChain(StructureEvent event) {
    
        int nr = event.getCurrentChainNumber();
        //logger.info("selected chain " + nr);
        if ( nr == currentChainNr){
            logger.info("already selected, ignoring...");
            return;
        }
        Structure s = event.getStructure();
        // change the displayed sequence ...
        Chain c = s.getChain(nr);
        currentChainNr = nr ;
        drawStructure(s,nr);
        triggerNewSequence(c,event.getPDBCode());
      
    }
    
    
    
   
    
    
}
