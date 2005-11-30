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
 * Created on Nov 29, 2005
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.biojava.bio.structure.Chain;
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.FeatureImpl;
import org.biojava.spice.manypanel.drawable.DrawableDasSource;
import org.biojava.spice.manypanel.eventmodel.SequenceListener;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureEvent;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureListener;


/** a mouse listener for the AbstractChainRenderer class
 * it listens to all mouse events and triggeres appropriate
 * SequenceListener and FeatureListener events
 * 
 * @author Andreas Prlic
 *
 */
public class ChainRendererMouseListener implements
MouseListener,
MouseMotionListener
{

    AbstractChainRenderer renderer;
    boolean selectionLocked;
    boolean dragging;
    
    int selectionStart;
    int selectionEnd;
    int draggingStart;
    int oldSelectionStart;
    
    int chainLength;
    List sequenceListeners;
    List spiceFeatureListeners;
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    
    public ChainRendererMouseListener(AbstractChainRenderer renderer) {
        super();
        this.renderer = renderer;
        selectionLocked = false;
        dragging = false;
        
        selectionStart = -1 ;
        selectionEnd = -1;
        oldSelectionStart = -1;
        draggingStart = -1;
        chainLength = 0;
        clearSequenceListeners();
        clearSpiceFeatureListeners();
    }
    
    public void clearSpiceFeatureListeners(){
        spiceFeatureListeners = new ArrayList();
    }
    
    public void addSpiceFeatureListener(SpiceFeatureListener li){
        spiceFeatureListeners.add(li);
    }
    
    public void mousePressed(MouseEvent event) {
        
        int pos  = getSeqPos(event);
        
        
          
        draggingStart=pos;
        selectionStart = pos ;
        //selectionEnd   = pos ;
        triggerFeatureSelected(null);
        triggerSelectionLocked(false);
            
        
    }
    
    // mouse motion part
    /** get the sequence position of the current mouse event 
     * */
    private int getSeqPos(MouseEvent e) {
        
        int x = e.getX();
        //int y = e.getY();
        //float scale = seqScale.getScale();
        int DEFAULT_X_START = FeaturePanel.DEFAULT_X_START;
        float scale = renderer.getScale();
        int seqpos =  java.lang.Math.round((x-DEFAULT_X_START-2)/scale) ;
        
        return seqpos  ;
    }   
    
    private Feature getFeatureFromEvent(MouseEvent e){
        
        
        int y = e.getY();
        
        int h =  renderer.getFeaturePanel().getHeight();
        if ( y < h ){
            return null ;
        }
        
        
        DasSourcePanel eventPanel = null;
        
        Iterator iter = renderer.getDasSourcePanels().iterator();
        while (iter.hasNext()){
            DasSourcePanel dsp = (DasSourcePanel)iter.next();
            int panelHeight = dsp.getDisplayHeight();
            h+= panelHeight;
            
            if (y < h ) {
                eventPanel = dsp;
                h -= panelHeight;
                break;
            }
                
        }  
        
        h+= FeaturePanel.DEFAULT_Y_START + FeaturePanel.DEFAULT_Y_STEP + FeaturePanel.DEFAULT_Y_STEP;
        
        DrawableDasSource source = eventPanel.getDrawableDasSource();
        
        Feature[] feats = source.getFeatures();
        for (int i = 0 ; i< feats.length; i ++){
            h += FeaturePanel.DEFAULT_Y_STEP;
            if ( y < h) {
                return feats[i];
            }
        }
        return null;
    }
    
    public void setChain(Chain c){
     
        chainLength = c.getLength();
    }
    
    private void setSelectionStart(int start){
        if ( start < 0 )
            start = 0;
        if ( start > chainLength)
            start = chainLength;
        selectionStart = start;
    }
    
    
    private void setSelectionEnd(int end){
        //if ( end < 0 )
            //end = 0;
        if ( end > chainLength)
            end = chainLength;
        selectionEnd = end;
    }
    
    
    public void mouseDragged(MouseEvent e) {
        dragging = true;
                
        
        int pos = getSeqPos(e) ;
        
        //logger.info("mouseDragged " +pos + " " + dragging + " " 
        //      + selectionStart + " " + selectionEnd);
        
        if (( pos < 0)|| ( pos> chainLength)){
            return;
        }
        
        //if ( pos < selectionStart){
        //selectionEnd = selectionStart;
        //   selectionStart = pos;
        // this.repaint();
        //return;
        //}
        if (pos == oldSelectionStart){
            return;
        }
        oldSelectionStart = pos;
        
        if ( pos > draggingStart){
            selectionStart = draggingStart;
            selectionEnd = pos ;
        } else {
            selectionStart = pos;
            selectionEnd = draggingStart;
        }
        triggerNewSequenceRange(selectionStart,selectionEnd);
      
        
        
        
    }
    
    
    
    
    public void mouseMoved(MouseEvent e) {
        if ( selectionLocked )
            return;
        //int x = e.getX();
        int pos = getSeqPos(e) ;
        if ( pos == oldSelectionStart)
            return;
        //logger.info("CursorPanel: mouse moved " + x + " " + pos);
        oldSelectionStart = pos;
        this.setSelectionStart(pos);
        this.setSelectionEnd(pos);
        
        triggerNewSequencePosition(pos);
        
    }
    
    
    
    public void mouseClicked(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }
    
    public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }
    
    public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }
    
    
    
    
    
    public void mouseReleased(MouseEvent event) {
        //logger.info("mouse released");
        
        draggingStart = -1;
        
        if  ( ! selectionLocked) {
            if ( dragging) {
                triggerSelectionLocked(true);
            } else {
                int pos = getSeqPos(event) ;
                Feature feat = getFeatureFromEvent(event);
                System.out.println(pos + " " + feat);
                
                if ( pos < 0) {
                    // the user clicked on the label -> trigger a new FeatureSelected
                    
                    triggerFeatureSelected(feat);
                    triggerSelectionLocked(true);
                    // else -> check if pos is over a feature then trigger new SegmentSelected
                }
            }
        }
        dragging = false ;
    }
    
    
    
    public void clearSequenceListeners(){
        sequenceListeners = new ArrayList();
    }
    
    public void addSequenceListener(SequenceListener li){
        sequenceListeners.add(li);
    }
    
 
    
    protected void triggerSelectionLocked(boolean flag){
        selectionLocked = flag;
        //logger.info("trigger selectionLocked " + flag);
        
        Iterator iter = sequenceListeners.iterator();
        while(iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectionLocked(flag);
        }
    }
    
    protected void triggerNewSequencePosition(int pos){
        if ( selectionLocked )
            return;
        
        
        
        Iterator iter = sequenceListeners.iterator();
        while(iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectedSeqPosition(pos);
        }
        
    }
    
    protected void triggerNewSequenceRange(int start,int end){
        if ( selectionLocked)
            return;
        
        Iterator iter = sequenceListeners.iterator();
        while(iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectedSeqRange(start,end);
        }
        
    }
    
    protected void triggerFeatureSelected(Feature feature){
        
        if ( selectionLocked)
            return;
        if (feature == null){
            feature = new FeatureImpl(); 
        }
        //logger.info("trigger featureSelected " + feature);
        SpiceFeatureEvent event = new SpiceFeatureEvent(feature);
        Iterator iter = spiceFeatureListeners.iterator();
        while( iter.hasNext()){
            SpiceFeatureListener li = (SpiceFeatureListener)iter.next();
            li.featureSelected(event);
            
        }
    }
    
    
    
    
    

}
