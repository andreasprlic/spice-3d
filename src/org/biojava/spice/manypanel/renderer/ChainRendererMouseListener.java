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

//import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.biojava.bio.structure.Chain;
import org.biojava.dasobert.eventmodel.SequenceListener;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.feature.Feature;
import org.biojava.spice.feature.FeatureImpl;
import org.biojava.spice.feature.Segment;
import org.biojava.spice.manypanel.drawable.DrawableDasSource;
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
    Feature oldFeature;
    Segment oldSegment;
    CoordManager coordManager;
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    MySequencePositionTrigger mySequencePositionTrigger = new MySequencePositionTrigger();
    
    SegmentPopupFrame popupFrame;
    
    public ChainRendererMouseListener(AbstractChainRenderer renderer) {
        super();
        this.renderer = renderer;
        selectionLocked = false;
        dragging = false;
        oldFeature = new FeatureImpl();
        selectionStart = -1 ;
        selectionEnd = -1;
        oldSelectionStart = -1;
        draggingStart = -1;
        chainLength = 0;
        clearSequenceListeners();
        clearSpiceFeatureListeners();
        coordManager = new CoordManager();
        
        popupFrame = SpiceApplication.getPopupFrame();
        //renderer.getLayeredPane().addMouseListener(popupFrame);
        renderer.getLayeredPane().addMouseMotionListener(popupFrame);
        addSpiceFeatureListener(popupFrame);
        addSequenceListener(popupFrame);
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
        triggerClearSelection();
        triggerSelectionLocked(false);
        triggerNewSequencePosition(pos,event.getY());
        
        
        
        
    }
    
    // mouse motion part
    
    /** get the sequence position of the current mouse event
     * 
     */
    private int getSeqPos(MouseEvent e) {
        
        int x = e.getX();
        //int y = e.getY();
        //float scale = seqScale.getScale();
        //int DEFAULT_X_START = SequenceScalePanel.DEFAULT_X_START;
        float scale = renderer.getScale();
        coordManager.setScale(scale);
        int seqpos = coordManager.getSeqPos(x-2);
        
   
        return seqpos  ;
    }   
    
    
    /** creates a spiceFeatureEvent from a mouse event
     * or null, if the event was not over a feature line
     * 
     * @param e
     * @return a SpiceFeatureEvent
     */
    private SpiceFeatureEvent getSpiceFeatureEvent(MouseEvent e){
        
        int y = e.getY();        
        int h = 0;               
        
        if ( y < h){
            
            //logger.info("event occured in FeaturePanel - check if it a structure one ...");
            if  ( y < SequenceScalePanel.DEFAULT_Y_STEP )
                return null ;
            if ( StructureScalePanel.shouldDrawStructureRegion()){
                SequenceScalePanel ssp = renderer.getFeaturePanel();
                if ( ssp instanceof StructureScalePanel) {
                    StructureScalePanel strucsp = (StructureScalePanel)ssp;
                    Feature f = strucsp.getStructureFeature();
                    int pos = getSeqPos(e);
                    if ( f.overlaps(pos))
                        return new SpiceFeatureEvent(null, f);
                }
            }
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
        if (eventPanel == null) {
            // no panel found below this event
            return null;
        }
        
        h += SequenceScalePanel.DEFAULT_Y_START + SequenceScalePanel.DEFAULT_Y_STEP + SequenceScalePanel.LINE_HEIGHT;
        if ( y < h){
            // smaller than the "heading section" of the display
            return null;
        }
        
        DrawableDasSource source = eventPanel.getDrawableDasSource();
        
        Feature[] feats = source.getFeatures();
        for (int i = 0 ; i< feats.length; i ++){
            h += SequenceScalePanel.DEFAULT_Y_STEP;
            if ( y < h) {
                SpiceFeatureEvent event = new SpiceFeatureEvent(source.getDasSource(), feats[i]);
                return event;
            }
        }
        return null;
    }
    
    public void setChain(Chain c){
        
        chainLength = c.getLength();
        coordManager.setLength(chainLength);
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
        
        
        popupFrame.interruptTimer();
        
        int pos = getSeqPos(e) ;
        
        
        if (( pos < 0)|| ( pos> chainLength)){
            return;
        }
        
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
        
        
        
        int pos = getSeqPos(e) ;
        SpiceFeatureEvent event = getSpiceFeatureEvent(e);
        Feature feat = null;
        if ( event != null ) {
            feat = event.getFeature();
           
        }
        
        // test if nothing changed ...
        // if nothing changed, return
        if ( feat != null ){
            if ( feat.equals(oldFeature)) {
                if ( pos == oldSelectionStart) {
                   
                    return;
                }
            } else {
                popupFrame.createContent(feat);
                
            }
            
        } else {
            //System.out.println("marking for hide");
            popupFrame.markForHide();
            if ( pos == oldSelectionStart)                
                return;                       
        }
        
        oldSelectionStart = pos;
        
        
        this.setSelectionStart(pos);
        this.setSelectionEnd(pos);
        
        triggerNewSequencePosition(pos,e.getY());
        
        if ( feat == null) 
            return;
        
        
        oldFeature = feat;
        
        
        if ( pos >= 0) {
//          check if pos is over a feature then trigger new SegmentSelected
            List segments = feat.getSegments();
            Iterator iter = segments.iterator();
            while (iter.hasNext()) {
                Segment s = (Segment)iter.next();
                
                // pos is in coords starting with 0
                // segment is in DAS coords starting with 1!
                if ( s.overlaps(pos+1)) {
                    
                    event.setSegment(s);
                    triggerMouseOverSegment(event);
                   
                    //triggerSegmentSelected(feat,s);
                }
            }
        } else {
            triggerMouseOverFeature(event);
        }
    }
    
    
    
    public void mouseClicked(MouseEvent arg0) {
        
        
    }
    
    public void mouseEntered(MouseEvent arg0) {
        
        
    }
    
    public void mouseExited(MouseEvent arg0) {
     
    }
    
    
    public void mouseReleased(MouseEvent event) {
        //logger.info("mouse released");
        
        
        
        draggingStart = -1;
        
        
        if ( dragging) {
            if  ( ! selectionLocked) {
                triggerSelectionLocked(true);
            }
        } else {
            
            int pos = getSeqPos(event) ;
            
            SpiceFeatureEvent spiceEvent = getSpiceFeatureEvent(event);
            
            Feature feat = null ;
            if ( spiceEvent != null ) 
                feat = spiceEvent.getFeature();
            
            //System.out.println(pos + " " + feat);
            if ( feat == null) {
                oldFeature = new FeatureImpl();
                popupFrame.markForHide();
                return;
            }
            if ( pos >= 0) {
//              check if pos is over a feature then trigger new SegmentSelected
                List segments = feat.getSegments();
                popupFrame.updateFramePosition(event);
                
                boolean somethingTriggered = checkOverSegment(segments, pos, spiceEvent, selectionLocked);
                if ( ! somethingTriggered){
                    if ( ! selectionLocked)
                        triggerNewSequencePosition(pos,event.getX());
                    popupFrame.markForHide();
                    
                }
            }
            oldFeature = feat;
            
        }
        
        dragging = false ;
    }
    
    private boolean checkOverSegment(List segments, int pos, SpiceFeatureEvent spiceEvent, boolean selectionLocked){
        Iterator iter = segments.iterator();
        boolean somethingTriggered = false;
       // if ( event.getClickCount() > 1 ) {
        while (iter.hasNext()) {
            Segment s = (Segment)iter.next();
            if ( s.overlaps(pos+1)) {  
                //System.out.println(s);
                popupFrame.createContent(s);  
                
                popupFrame.showFrame();
                if ( ! selectionLocked) {
                    spiceEvent.setSegment(s);
                    //triggerSegmentSelected(spiceEvent);
                    triggerNewSequenceRange(s.getStart()-1,s.getEnd()-1);
                    triggerSelectionLocked(true);
                    somethingTriggered = true;
                }
            }
        //}
        }
        return somethingTriggered;
    }
    
    public void clearSequenceListeners(){
        sequenceListeners = new ArrayList();
        mySequencePositionTrigger.clearListeners();
    }
    
    public void addSequenceListener(SequenceListener li){
        sequenceListeners.add(li);
        mySequencePositionTrigger.addSequenceListener(li);
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
    
    protected void triggerNewSequencePosition(int pos, int mouseY){
        if ( selectionLocked )
            return;
        /*Iterator iter = sequenceListeners.iterator();
        
        while(iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectedSeqPosition(pos);
        }*/
        
        mySequencePositionTrigger.setPosition(pos,mouseY);
        if ( ! mySequencePositionTrigger.isPainting())
            javax.swing.SwingUtilities.invokeLater(mySequencePositionTrigger);
        
        
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
    
    protected void triggerFeatureSelected(SpiceFeatureEvent event){
        Feature feature = event.getFeature();
        logger.info("trigger feture selected " + feature);
        if ( selectionLocked)
            return;
        
        if (feature == null){
            return;
        }
        
        //logger.info("trigger featureSelected " + feature);
        
        Iterator iter = spiceFeatureListeners.iterator();
        while( iter.hasNext()){
            SpiceFeatureListener li = (SpiceFeatureListener)iter.next();
            li.featureSelected(event);
            
        }
    }
    
    protected void triggerClearSelection(){
        
        //logger.info("trigger clearSelection " );
        
        //logger.info("trigger featureSelected " + feature);
        
        Iterator iter = spiceFeatureListeners.iterator();
        while( iter.hasNext()){
            SpiceFeatureListener li = (SpiceFeatureListener)iter.next();
            li.clearSelection();
            
        }
        
        Iterator iter2 = sequenceListeners.iterator();
        while (iter2.hasNext()){
            SequenceListener li = (SequenceListener) iter2.next();
            li.clearSelection();
        }
    }
    
    
    protected void triggerSegmentSelected(SpiceFeatureEvent event){
        if ( selectionLocked)
            return;
        
        Segment segment = event.getSegment();
        
        if ( segment == null){
            segment = new Segment();
            event.setSegment(segment);
        }
        //logger.info("trigger segment selected " + segment);
        
        Iterator iter = spiceFeatureListeners.iterator();
        while (iter.hasNext()){
            SpiceFeatureListener li = (SpiceFeatureListener)iter.next();
            li.segmentSelected(event);
        }
    }
    
    
    protected void triggerMouseOverFeature(SpiceFeatureEvent event){
        Feature feature = event.getFeature();
        if ( feature == null ) {
            feature = new FeatureImpl();
            event.setFeature(feature);
        }
        Iterator iter = spiceFeatureListeners.iterator();
        while( iter.hasNext()){
            SpiceFeatureListener li = (SpiceFeatureListener)iter.next();
            li.mouseOverFeature(event);
            
        }
    }
    
    protected void triggerMouseOverSegment(SpiceFeatureEvent event){
      
        MyMouseOverSegment trigger = new MyMouseOverSegment(event, spiceFeatureListeners);
        
        javax.swing.SwingUtilities.invokeLater(trigger);
      
        
    }
        
}

class MyMouseOverSegment implements Runnable {
    List spiceFeatureListeners;
    SpiceFeatureEvent event;
    
    public MyMouseOverSegment( SpiceFeatureEvent e, List listeners) {
        event = e;
        spiceFeatureListeners = listeners;
    }
    
    public void run() {
    
        Segment segment = event.getSegment();
        if ( segment == null){
            segment = new Segment();
            event.setSegment(segment);
        }
        
        
        
        
        Iterator iter = spiceFeatureListeners.iterator();
        while (iter.hasNext()){
            SpiceFeatureListener li = (SpiceFeatureListener)iter.next();
            li.mouseOverSegment(event);
        }
        
    }
    
}


class MySequencePositionTrigger implements Runnable {
    
        int pos, mouseY;
        boolean painting;
        List sequenceListeners;
        
        int savePos, saveMouse; // the coordinates of the next invocation...
        
        public MySequencePositionTrigger() {
            pos = 0 ;
            mouseY = 0;
            
            savePos   = -1;
            saveMouse = -1;
            
            sequenceListeners = new ArrayList();
        }
        
      
        
        public void addSequenceListener(SequenceListener li){
            sequenceListeners.add(li);
        }
        
        public synchronized void clearListeners() {
            sequenceListeners.clear();
        }
        
        
        public synchronized void setPosition(int pos, int mouseY){
            //this.pos = pos;
            //this.mouseY = mouseY;
            if ( ! painting) {
                this.pos = pos;
                this.mouseY = mouseY;
                savePos = -1;
                saveMouse = -1;
            } else {
                savePos   = pos;
                saveMouse = mouseY;
            }
        }
        
        public boolean isPainting(){
            return painting;
        }
        
        public synchronized void setPainting(boolean flag){
            painting = flag;
        }
        
        public void run() {
            setPainting(true);
            Iterator iter = sequenceListeners.iterator();
            
            while(iter.hasNext()){
                SequenceListener li = (SequenceListener)iter.next();
                li.selectedSeqPosition(pos);
                
                
                /*// ugly! TODO: find a nice solution for this ...
                // display seq cursor only over the sequyece ...
                if ( li instanceof SeqToolTipListener ){
                    if ( mouseY < 20)
                        li.selectedSeqPosition(pos);
                    else 
                        li.selectedSeqPosition(-1);
                    
                } else {
                    li.selectedSeqPosition(pos);
                }*/
            }
            
            
            //TODO: is this actually necessary? 
            if ( savePos > -1) {
                System.out.println("got new mouse position, notifying...");
                pos = savePos;
                savePos = -1;           
                mouseY = saveMouse;
                saveMouse = -1;
                run();
            }
            setPainting(false);
        }
        


}
