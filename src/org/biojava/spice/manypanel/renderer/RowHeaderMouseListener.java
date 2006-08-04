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
 * Created on Aug 2, 2006
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.spice.feature.Feature;
import org.biojava.spice.gui.DasSourceDialog;
import org.biojava.spice.manypanel.drawable.DrawableDasSource;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureEvent;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureListener;
import org.biojava.spice.utils.BrowserOpener;


/** a mouse listener that listens on mouse events of the scroll panel's row header
 * 
 * @author Andreas Prlic
 * @since 4:16:44 PM
 * @version %I% %G%
 */
public class RowHeaderMouseListener implements MouseListener,
MouseMotionListener {
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    DasScrollPaneRowHeader parent;    
    List spiceFeatureListeners;
    
    public RowHeaderMouseListener(DasScrollPaneRowHeader parent) {
        super();
        spiceFeatureListeners = new ArrayList();
        this.parent = parent;
    }
    
    
    /** returns the feature if mouse over the link icon 
     * 
     * @param e
     * @return the feature of the link, or null if either not over a feature or feature does not have a link
     */
    private Feature getLinkFeature(MouseEvent e){
        int xe = e.getX();
        if ( xe > DasSourcePanelHeader.LINK_ICON_SIZE )
            return null;        
        int y = SequenceScalePanel.DEFAULT_Y_START + SequenceScalePanel.DEFAULT_Y_STEP ;
        
        int ye = e.getY();
        
        if ( ye < (y+SequenceScalePanel.DEFAULT_Y_STEP)) {
            // moved the mouse in the header region ..
            return null;
        }
        
        DasSourcePanelHeader[] dasSources = parent.getDasSources();
        
        for (int pi = 0 ; pi < dasSources.length ; pi++ ){
            DasSourcePanelHeader dsp = dasSources[pi];
            
            int compH =dsp.getDisplayHeight();
            if ( ye < (y+compH) ) {
                Feature[] feats = dsp.getDrawableDasSource().getFeatures();
                // now get the right feature ...
                Feature found = null;

                // the nickname text in the row ...
                y+= SequenceScalePanel.DEFAULT_Y_STEP;                
                
                for (int i =0 ; i < feats.length ; i++){
                    y += SequenceScalePanel.DEFAULT_Y_STEP;
                    if ( ye < y) {
                      
                        found = feats[i];
                        String link = found.getLink();
                        if (( link != null) && (! link.equals(""))){
                            return found;
                        } else {
                           return null;
                        }
                    }
                }
            }

            if ( ye < y ) {
                // we can at most be over a link icon here..test somewhere else
                return null;
            }
            
        }
        
        return null;
        
        
    }
    
    private DrawableDasSource getInfoButtonDasSource(MouseEvent e){
        
        int xe = e.getX();
        if ( xe > DasSourcePanelHeader.INFO_ICON_SIZE )
            return null;        
        
        int ye = e.getY();
        
        int y = SequenceScalePanel.DEFAULT_Y_START + SequenceScalePanel.DEFAULT_Y_STEP ;
        
        DasSourcePanelHeader[] dasSources = parent.getDasSources();
        
        for (int pi = 0 ; pi < dasSources.length ; pi++ ){
            DasSourcePanelHeader dsp = dasSources[pi];
            
            int compH =dsp.getDisplayHeight();   
            
            if ( ye < ( y + SequenceScalePanel.DEFAULT_Y_STEP)){
                // we are in the right region!
                return dsp.getDrawableDasSource();
            }
            
            y += compH;
            
            if ( ye < y ) {
                // we can at most be over a link icon here..test somewhere else
                return null;
            }
            
        }
        
        return null;
        
    }
    
    private SpiceFeatureEvent getSpiceFeatureEvent(MouseEvent e){
        
        int xe = e.getX();        
        
        // if mouse over info icon this should be determined with some other method
        if ( xe < DasSourcePanelHeader.INFO_ICON_SIZE )
            return null;
        
        int ye = e.getY();
        
        // get the right y position ...
        
        int y = SequenceScalePanel.DEFAULT_Y_START + SequenceScalePanel.DEFAULT_Y_STEP ;
        
        if ( ye < (y+SequenceScalePanel.DEFAULT_Y_STEP)) {
            // moved the mouse in the header region ..
            return null;
        }
        DasSourcePanelHeader[] dasSources = parent.getDasSources();
        
        for (int pi = 0 ; pi < dasSources.length ; pi++ ){
            DasSourcePanelHeader dsp = dasSources[pi];
            
            int compH =dsp.getDisplayHeight();                        
            
            if ( ye < (y+compH) ) {
                // we are over this panel!
                SpiceDasSource ds = dsp.getDrawableDasSource().getDasSource();
                
                // the nickname text in the row ...
                y+= SequenceScalePanel.DEFAULT_Y_STEP;
                
                Feature[] feats = dsp.getDrawableDasSource().getFeatures();
                // now get the right feature ...
                Feature found = null;
                for (int i =0 ; i < feats.length ; i++){
                    y += SequenceScalePanel.DEFAULT_Y_STEP;
                    if ( ye < y) {
                        found = feats[i];
                        break;
                    }
                }
                if ( found == null)
                    return null;
                
                SpiceFeatureEvent event = new SpiceFeatureEvent(ds,found);
                return event;
            }
            y += compH;
            
        }
        return null;
        
        
    }
    
    public void mouseClicked(MouseEvent arg0) {  }
    
    public void mousePressed(MouseEvent arg0) {  }
    
    public void mouseReleased(MouseEvent e) {
        SpiceFeatureEvent event = getSpiceFeatureEvent(e);
        
        if ( event != null){
            triggerFeatureSelectedEvent(event);
        }
        
        DrawableDasSource dds = getInfoButtonDasSource(e);
        if ( dds != null) {
            triggerDasSourceInfo(dds);
        }
        
        
        Feature f = getLinkFeature(e);
        if ( f != null){
            triggerLinkSelected(f);
        }
        
    }
    
    public void mouseEntered(MouseEvent arg0) { }
    
    public void mouseExited(MouseEvent arg0) {}
    
    public void mouseDragged(MouseEvent arg0) {}
    
    public void mouseMoved(MouseEvent e) {        
        
        SpiceFeatureEvent event = getSpiceFeatureEvent(e);
        if ( event != null){                      
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            triggerMouseOverFeatureEvent(event);
            return;
        }

        
        DrawableDasSource dds = getInfoButtonDasSource(e);
        if ( dds != null) {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }

        
        Feature f = getLinkFeature(e);
        if ( f != null){        
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
               
        parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        return;
        
    }
    
    
    
    
    public void clearSpiceFeatureListeners(){
        spiceFeatureListeners = new ArrayList();
    }
    
    public void addSpiceFeatureListener(SpiceFeatureListener li){
        spiceFeatureListeners.add(li);
    }
    
    public void triggerMouseOverFeatureEvent(SpiceFeatureEvent event) {
        // System.out.println("mouse over feature trigger");
        Iterator iter = spiceFeatureListeners.iterator();
        while (iter.hasNext()){
            SpiceFeatureListener li = (SpiceFeatureListener) iter.next();
            li.mouseOverFeature(event);
            
        }
    }
    
    public void triggerFeatureSelectedEvent(SpiceFeatureEvent event) {
        //System.out.println("triggerFeatureSelected");
        Iterator iter = spiceFeatureListeners.iterator();
        while (iter.hasNext()){
            SpiceFeatureListener li = (SpiceFeatureListener) iter.next();
            li.featureSelected(event);
            
        }
    }
    
    protected void triggerDasSourceInfo(DrawableDasSource ds){
        SpiceDasSource sds = ds.getDasSource();
        DasSourceDialog dialog = new DasSourceDialog(sds);
        dialog.show();
        
    }
    
    protected void triggerLinkSelected(Feature f){
        String link = f.getLink();
        
        if (( link != null) && (! link.equals(""))){
            
            BrowserOpener.showDocument(link);
        }
    }        
    
}
