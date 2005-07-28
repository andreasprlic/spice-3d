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

import java.awt.Point;
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

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import java.util.NoSuchElementException;
import java.awt.Cursor;

/**
 * @author Andreas Prlic
 *
 */
public class FeaturePanelMouseListener 
implements PanelListener,
MouseListener, MouseMotionListener {
    
    public static int DEFAULT_Y_STEP  = 10;
    public static boolean PASSIVE_SELECT = false; 
    
    
    FeatureView oldFeatureView;
    SpiceFeatureViewer parent;
    int oldposition;
    boolean dragging;
    int mouseDragStart;
    //List selectedSeqListeners;
    //List featureViewListeners;
    //FeatureView featureView;
    boolean selectionIsLocked;
    Segment oldsegment ;
    JPopupMenu popupMenu ;
    
    static Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    static Cursor handCursor   = new Cursor(Cursor.HAND_CURSOR);
    
    /**
     * 
     */
    public FeaturePanelMouseListener(SpiceFeatureViewer parent) {
        super();
        oldsegment = null;
        this.parent = parent;
        popupMenu = createPopupMenu();
        //selectedSeqListeners = new ArrayList();
        //featureViewListeners = new ArrayList();
        //this.featureView = featureView;
        selectionIsLocked = false;
        oldposition = -1;
        dragging = false;
        mouseDragStart = -1;
    }
    
    
    private void setToolTipText(String txt){
        JPanel panel = parent.getFeaturePanel();
        panel.setToolTipText(txt);
        
    }
    
    public void mouseMoved(MouseEvent e)
    {	
       // System.out.println("mouseMoved " + e.getY());
        
        
        if ( selectionIsLocked ) {
            //System.out.println(" FeaturePanelMouseMove selection locked");
            parent.setCursor(normalCursor);
            return ;
        }
        
        int b = e.getButton();
       
        // do not change selection if we display the popup window
        
        if (b != 0 )
            if ( b != MouseEvent.BUTTON1 ) {
                oldsegment = null;
                parent.setCursor(normalCursor);
                return;
            }
            // make sure the triggering class is a FeaturePanel
        Object c = e.getSource();
        if ( ! (c instanceof FeaturePanelContainer)){
            oldsegment = null;
            parent.setCursor(normalCursor);
            return;
        }
        
        FeaturePanelContainer container = (FeaturePanelContainer) c;
        
        //System.out.println(view);
        FeatureView featureView = parent.getParentFeatureView(e);
        
        //out of range:
        if ( featureView == null) { 
            oldsegment = null;
            setToolTipText(null);
            parent.setCursor(normalCursor);
            return;
        }
        
        FeaturePanel view = featureView.getFeaturePanel();  
        
        //, int x, int y
        int y = e.getY();
        int seqpos = container.getSeqPos(e);
        
        Point p = parent.getLocationOnLabelBox(featureView);
        // y is relative to the TypePanelContainer
        // make it relative to the featureview
        int relY = y -p.y + DEFAULT_Y_STEP;
        TypeLabelPanel typ = featureView.getTypePanel();
        int linenr = typ.getLineNr(relY);
        //System.out.println(linenr + " " + seqpos);
        
        //int linenr = view.getLineNr(e);
        if ( seqpos < 0 ) {
            oldsegment = null;
            setToolTipText(null);
            parent.setCursor(normalCursor);
            return ;
        }
        
        if ( linenr < 0 ) {
            oldsegment = null;
            setToolTipText(null);
            parent.setCursor(normalCursor);
            
            triggerSelectedSeqPosition(seqpos);
            return ;
        }
       
        
        if ( seqpos == oldposition) 
            return;
        
        oldposition = seqpos;
        
        //int featurenr = get_featurenr(y); 
        
        Feature feat ;
        try {
            feat = featureView.getFeatureAt(linenr);
            setToolTipText(feat.toString());
        } catch (NoSuchElementException ex){
            oldsegment = null;
            triggerSelectedSeqPosition(seqpos);
            setToolTipText(null);
            parent.setCursor(normalCursor);
            return;
        }
        Segment seg = null;
        try {
            seg = featureView.getSegmentAt(linenr, seqpos);
            parent.setCursor(handCursor);
        } catch (NoSuchElementException ex) {
            oldsegment = null;
            parent.setCursor(normalCursor);
        }
        
        boolean triggered = false;      
        
            // trigger mouseOveerSegment, featureSelected...
            
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
                if ( PASSIVE_SELECT ){
                    int start = seg.getStart() -1 ;
                    int end   = seg.getEnd() -1 ;
                    parent.selectedSeqRange(start,end);
                }
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
    public void mouseExited(MouseEvent e)   {
        if ( ! selectionIsLocked){
            triggerSelectedSeqPosition(-1);
        }
    }
    
    
    public void mousePressed(MouseEvent e)  {
        
        maybeShowPopup(e);
        oldsegment = null;
        
        // make sure the triggering class is a FeaturePanel
        Object c = e.getSource();
        //System.out.println(c);
        if (c instanceof FeaturePanelContainer ){            
            FeaturePanelContainer view = (FeaturePanelContainer) c;
            //if ( b == MouseEvent.BUTTON1 )
            int seqpos = view.getSeqPos(e);
            mouseDragStart = seqpos;
            
            dragging = false;
            triggerSelectedSeqPosition(seqpos);
            triggerSelectionLocked(false);
            //spice.setSelectionLocked(false);
        }
        
        int mouseButton = e.getButton();
        if (mouseButton == MouseEvent.BUTTON3 ){
            
            FeatureView fv = parent.getParentFeatureView(e) ;
            System.out.println(fv);
            if ( oldFeatureView != null ) {
                oldFeatureView.setSelected(false);
            }
            if ( fv != null ){
                fv.setSelected(true);
                oldFeatureView = fv;
                parent.repaint();
            }
        }
        
    }
    public FeatureView getCurrentFeatureView(){
        return oldFeatureView;
    }
    
    public void mouseReleased(MouseEvent e) {
        int b = e.getButton();
        
        if ( oldFeatureView != null ){
            oldFeatureView.setSelected(false);
        }
        maybeShowPopup(e);
        
        //      make sure the triggering class is a FeaturePanel
        Object c = e.getSource();
        
        if ( c instanceof LabelPane){
            LabelPane txf = (LabelPane) c;
            
            FeatureView fv = parent.getParentFeatureView(e) ;
            if ( fv != null ){
                fv.setSelected(false);
            } else {
                System.err.println("no parent found!");
            }
            return ;
        }
        //System.out.println("feature panel mouse released");
        if (  selectionIsLocked) {
            //System.out.println(" selection  locked");
            return;
        }
        if ( ! (c instanceof FeaturePanelContainer) ){
            return ;
        }
        FeaturePanelContainer container = (FeaturePanelContainer) c;
        FeatureView fv = parent.getParentFeatureView(e);
        FeaturePanel view = fv.getFeaturePanel();
        //System.out.println(view);
        fv.setSelected(false);
        
        int y = e.getY();
        int seqpos = container.getSeqPos(e);
        Point p = parent.getLocationOnLabelBox(fv);
        // y is relative to the TypePanelContainer
        // make it relative to the featureview
        int relY = y -p.y + DEFAULT_Y_STEP;
        TypeLabelPanel typ = fv.getTypePanel();
        int lineNr = typ.getLineNr(relY);
        
       
        
        //System.out.println("checking more");
        
        if ( seqpos < 0 ) return;
        
        if ( dragging) {
            //System.out.print("dragging");
            int start = mouseDragStart ;
            int end   = seqpos         ;
            if ( seqpos < mouseDragStart ) {
                start = seqpos ;
                end = mouseDragStart ;
            } 
            triggerSelectedSeqRange(start,end);
            mouseDragStart =  -1 ;
            triggerSelectionLocked(true);
            dragging = false;
            
        }
        
        if ( lineNr < 0 ) return ;
        FeatureView featureView = parent.getParentFeatureView(e);
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
        
        //System.out.println("selected segment " + seg);
        FeatureViewListener[] fvls = parent.getFeatureViewListeners();
        for (int i = 0 ; i< fvls.length ; i++) {
            
            FeatureViewListener li = fvls[i];
            //FeatureEvent event = new FeatureEvent(featureView,feat);
            
            if ( seg != null ) {
                //li.featureSelected(event);
                FeatureEvent event2 = new FeatureEvent(featureView,seg);
                li.segmentSelected(event2);
            }
        }
        parent.selectedSeqRange(seg.getStart()-1,seg.getEnd()-1);
        triggerSelectionLocked(true);
    }
    
    public void mouseDragged(MouseEvent e) {
        dragging = true;
        //setSelectionLocked(true);
        //System.out.println("dragging mouse in feturePanel" +
        //        " selection:" + selectionIsLocked +
        //        " dragstart:"+mouseDragStart);
        if ( mouseDragStart < 0 )
            return ;        
        
        //if (spice.isSelectionLocked())
        //  return;
        
        int b = e.getButton();
        if ( b == MouseEvent.BUTTON3){
            // do not allow right -mouse dragging ...
            return;
        }
        //      make sure the triggering class is a FeaturePanel
        Object c = e.getSource();
        if ( ! (c instanceof FeaturePanelContainer) ){
            return ;
        }
        FeaturePanelContainer view = (FeaturePanelContainer) c;
        
        int seqpos = view.getSeqPos(e);
        
        int selEnd =  seqpos;
        int start = mouseDragStart ;
        int end   = selEnd         ;
        if ( selEnd < mouseDragStart ) {
            start = selEnd ;
            end = mouseDragStart ;
        } 
        //System.out.println("dragging mouse " + start + " " + end );
        triggerSelectedSeqRange(start,end);
    }  
    
    
    private void triggerSelectedSeqPosition(int seqpos){
        //System.out.println("trigger selected seq position " + seqpos);
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
    
    private void maybeShowPopup(MouseEvent e) {
        
        if (e.isPopupTrigger()) {
            popupMenu.show(e.getComponent(),		       
                    e.getX(), e.getY());
        }
    }
    private JPopupMenu createPopupMenu(){ 
        
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("show DAS-source details");
        PanelPopupMenuListener sdsl = new PanelPopupMenuListener(this);
        menuItem.setActionCommand("select");
        menuItem.addActionListener(sdsl);
        popupMenu.add(menuItem);
        
        JMenuItem disableItem = new JMenuItem("disable this DAS-source");
        disableItem.setActionCommand("disable");
        disableItem.addActionListener(sdsl);
        popupMenu.add(disableItem);
        return popupMenu;
    }
}
