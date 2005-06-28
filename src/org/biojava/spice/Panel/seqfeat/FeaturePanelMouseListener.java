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
 * Created on Jun 10, 2005
 *
 */
package org.biojava.spice.Panel.seqfeat;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.Segment;
import org.biojava.spice.Panel.seqfeat.FeaturePanel;
import org.biojava.spice.Panel.seqfeat.FeatureView;
import org.biojava.spice.Panel.seqfeat.LabelPane;
import org.biojava.spice.Panel.seqfeat.SelectedSeqPositionListener;
import org.biojava.spice.Panel.seqfeat.SpiceFeatureViewer;

import java.util.NoSuchElementException;
/**
 * @author Andreas Prlic
 *
 */
public class FeaturePanelMouseListener 
implements MouseListener, MouseMotionListener {
    SpiceFeatureViewer parent;
    int oldposition;
    boolean dragging;
    int mouseDragStart;
    //List selectedSeqListeners;
    //List featureViewListeners;
    FeatureView featureView;
    boolean selectionIsLocked;
    Segment oldsegment ;
    /**
     * 
     */
    public FeaturePanelMouseListener(SpiceFeatureViewer parent, FeatureView featureView) {
        super();
        oldsegment = null;
        this.parent = parent;
        //selectedSeqListeners = new ArrayList();
        //featureViewListeners = new ArrayList();
        this.featureView = featureView;
        selectionIsLocked = false;
        oldposition = -1;
        dragging = false;
        mouseDragStart = -1;
    }
    
    public void mouseMoved(MouseEvent e)
    {	
        int b = e.getButton();
        //System.out.println("feature panel mouseMoved " + b );
        if ( selectionIsLocked ) {
            //System.out.println(" FeaturePanelMouseMove selection locked");
            return ;
        }
        
        //System.out.println("mouseMoved button " +b);
        
        // do not change selection if we display the popup window
        
        if (b != 0 )
            if ( b != MouseEvent.BUTTON1 ) {
                oldsegment = null;
                return;
            }
            // make sure the triggering class is a FeaturePanel
        Object c = e.getSource();
        //System.out.println(c);
        //System.out.println("mouse Moved, select locked");
        //if ( selectionIsLocked ) return ;
        if ( ! (c instanceof FeaturePanel) ){
            oldsegment = null;
            return ;
        }
        FeaturePanel view = (FeaturePanel) c;
        //System.out.println(view);
        
        //, int x, int y
        int seqpos = view.getSeqPos(e);
        int linenr = view.getLineNr(e);
        
        if ( linenr < 0 ) {
            oldsegment = null;
            return ;
        }
        if ( seqpos < 0 ) {
            oldsegment = null;
            return ;
        }
        
        if ( seqpos == oldposition)
            return;
        //System.out.println("mouse moved " + seqpos + " ("+oldposition+") line" + linenr);
        
        oldposition = seqpos;
        
        //int featurenr = get_featurenr(y); 
        
        
        Feature feat ;
        try {
            feat = featureView.getFeatureAt(linenr);
        } catch (NoSuchElementException ex){
            oldsegment = null;
            triggerSelectedSeqPosition(seqpos);
            return;
        }
        Segment seg = null;
        try {
            seg = featureView.getSegmentAt(linenr, seqpos);
        } catch (NoSuchElementException ex) {
            oldsegment = null;
        }
        
        // trigger mouseOveerSegment, featureSelected...
        boolean triggered = false;
        FeatureViewListener[] fvls = parent.getFeatureViewListeners();
        if ( seg != null ) {
            
            if ( oldsegment != null ){
                if ( seg.equals(oldsegment)) {
                    // nothing to do here ...
                    return;
                }
            }
            oldsegment = seg;
            for (int i = 0 ; i< fvls.length ; i++) {
            
                FeatureViewListener li = fvls[i];
                // do not trigger mouse over feature here ...
                // this is done by TypeLabelpane...
                //FeatureEvent event = new FeatureEvent(featureView,feat);
                //li.mouseOverFeature(event);
                FeatureEvent event2 = new FeatureEvent(featureView,seg);
                li.mouseOverSegment(event2);
                triggered = true;
            }
            int start = seg.getStart() -1 ;
            int end   = seg.getEnd() -1 ;
            parent.selectedSeqRange(start,end);
        }
        
        if ( ! triggered ) {
            triggerSelectedSeqPosition(seqpos);
            //parent.selectedSeqPosition(seqpos);
        }
    }
    
    
    
    
    public void mouseClicked(MouseEvent e)
    {  
        
        //logger.finest("mouseClicked");
        
        //this.setToolTipText(null);
        return  ;
    }	
    
    public void mouseEntered(MouseEvent e)  {}
    public void mouseExited(MouseEvent e)   {}
    
    
    public void mousePressed(MouseEvent e)  {
        int b = e.getButton();
        
        triggerSelectionLocked(false);
        oldsegment = null;
        
        // make sure the triggering class is a FeaturePanel
        Object c = e.getSource();
        
        if (c instanceof FeaturePanel ){            
            FeaturePanel view = (FeaturePanel) c;
          
            
            if ( b == MouseEvent.BUTTON1 )
                mouseDragStart = view.getSeqPos(e);
            
            dragging = false;
            //spice.setSelectionLocked(false);
        }
        
        else if ( c instanceof LabelPane){
            LabelPane txf = (LabelPane) c;
            
            FeatureView fv = parent.getParentFeatureView(e, LabelPane.class) ;
            if ( fv != null ){
                fv.setSelected(true);
            } else {
                System.err.println("no parent found!");
            }
        }
        
    }
    
    
    public void mouseReleased(MouseEvent e) {
        int b = e.getButton();
        //System.out.println("mouseReleased "+b);       
        if ( b != MouseEvent.BUTTON1) {          
            return;
        }           
        
        
        //      make sure the triggering class is a FeaturePanel
        Object c = e.getSource();
        
        if ( c instanceof LabelPane){
            LabelPane txf = (LabelPane) c;
            
            FeatureView fv = parent.getParentFeatureView(e,LabelPane.class) ;
            if ( fv != null ){
                fv.setSelected(false);
            } else {
                System.err.println("no parent found!");
            }
            return ;
        }
        //System.out.println("feature panel mouse released");
        if (  selectionIsLocked) {
            //System.out.println(" but select locked");
            return;
        }
        if ( ! (c instanceof FeaturePanel) ){
            return ;
        }
        FeaturePanel view = (FeaturePanel) c;
        //System.out.println(view);
        
        int seqpos = view.getSeqPos(e);
        int lineNr = view.getLineNr(e);
        //System.out.println("mouseReleased at " + seqpos + " line " + lineNr);
        
        //if ( seqpos > seqLength) return ;
        
        //System.out.println("checking more");
        if ( lineNr < 0 ) return ;
        if ( seqpos < 0 ) return;
        
        if ( dragging) {
            //System.out.print("getSegmentUnder");
            triggerSelectedSeqRange(mouseDragStart,seqpos);
            mouseDragStart =  -1 ;
            triggerSelectionLocked(true);
            
        }
        Feature feat = null;
        try {
            feat = featureView.getFeatureAt(lineNr);
        } catch (NoSuchElementException ex){
            return;
        }
        
        Segment seg = null;
        
        try {
            seg = featureView.getSegmentAt(lineNr, seqpos);
        } catch (NoSuchElementException ex){
            // do nothing ...
            return;
        }
        
        
        FeatureViewListener[] fvls = parent.getFeatureViewListeners();
        for (int i = 0 ; i< fvls.length ; i++) {
            
            FeatureViewListener li = fvls[i];
            FeatureEvent event = new FeatureEvent(featureView,feat);
            
            if ( seg != null ) {
                li.featureSelected(event);
                FeatureEvent event2 = new FeatureEvent(featureView,seg);
                li.segmentSelected(event2);
            }
        }
        
        
        
        
    }
    
    public void mouseDragged(MouseEvent e) {
        dragging = true;
        //setSelectionLocked(true);
        //System.out.println("dragging mouse in feturePanel selection:" + selectionIsLocked);
        if ( mouseDragStart < 0 )
            return ;        
        
        //if (spice.isSelectionLocked())
        //  return;
        
        int b = e.getButton();
                
        //      make sure the triggering class is a FeaturePanel
        Object c = e.getSource();
        if ( ! (c instanceof FeaturePanel) ){
            return ;
        }
        FeaturePanel view = (FeaturePanel) c;
      
        int seqpos = view.getSeqPos(e);
        
        int selEnd =  seqpos;
        int start = mouseDragStart ;
        int end   = selEnd         ;
        if ( selEnd < mouseDragStart ) {
            start = selEnd ;
            end = mouseDragStart ;
        } 
        triggerSelectedSeqRange(start,end);
    }  
    
    
    private void triggerSelectedSeqPosition(int seqpos){

        SelectedSeqPositionListener[] selectedSeqListeners = parent.getSelectedSeqPositionListeners();
        for ( int i =0 ; i < selectedSeqListeners.length; i++) {
            
            SelectedSeqPositionListener seli = selectedSeqListeners[i];
            seli.selectedSeqPosition(seqpos);
        }
        parent.selectedSeqPosition(seqpos);
        
    }
    
    private void triggerSelectedSeqRange(int start, int end){

        SelectedSeqPositionListener[] selectedSeqListeners = parent.getSelectedSeqPositionListeners();
        for ( int i =0 ; i < selectedSeqListeners.length; i++) {
            
            SelectedSeqPositionListener seli = selectedSeqListeners[i];
            seli.selectedSeqRange(start,end);
        } 
        parent.selectedSeqRange(start,end);
    }
    
    public void selectionLocked(boolean flag){
        selectionIsLocked = flag;
    }
    
    /** goes through all SeqPositionSelected listeners and locks/unlocks selection */
    private void triggerSelectionLocked(boolean flag){
        selectionIsLocked = flag;
        parent.selectionLocked(flag);
        SelectedSeqPositionListener[] selectedSeqListeners = parent.getSelectedSeqPositionListeners();
        for ( int i =0 ; i < selectedSeqListeners.length; i++) {
            
            SelectedSeqPositionListener seli = selectedSeqListeners[i];
            seli.selectionLocked(flag);
        }
        
    }
    
}
