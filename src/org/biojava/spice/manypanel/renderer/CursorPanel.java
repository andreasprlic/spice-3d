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
 * Created on Oct 28, 2005
 *
 */
package org.biojava.spice.manypanel.renderer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import org.biojava.bio.structure.*;
import org.biojava.dasobert.eventmodel.SequenceEvent;
import org.biojava.dasobert.eventmodel.SequenceListener;
import org.biojava.spice.feature.Segment;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureEvent;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureListener;


/** a class that paints the sequence position "cursor" and the selected region of a sequence
 * 
 * @author Andreas Prlic
 * @since 4:15:29 PM
 * @version %I% %G%
 */
public class CursorPanel 

extends JPanel
implements
SequenceListener,
SpiceFeatureListener

{
    static final long serialVersionUID = 92019290011924233l;
    Color selectionColor ;
    int selectionStart;
    int selectionEnd;
    int oldSelectionStart;
    int oldSelectionEnd;
    float scale;
    Chain chain;
    int chainLength;
    
    boolean selectionLocked;
    
    List sequenceListeners;
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
        
    int storeStart ;
    int storeEnd ; 
    
    static String baseName="spice";
    ResourceBundle resource;
    
    public CursorPanel() {
        super(); 
        this.setOpaque(false);
        setDoubleBuffered(true);
             
        resource = ResourceBundle.getBundle(baseName);
        
        selectionLocked = false;
        
        storeStart = -1;
        storeEnd   = -1;
        String col = resource.getString("org.biojava.spice.manypanel.renderer.CursorPanel.CursorColor");
        selectionColor = Color.decode(col);
    }
     
    
    
    public void newSequence(SequenceEvent e) {
       
        
    }
    
    public void clearSelection(){
        //logger.info("cursorPanel clear selection");
        selectionLocked = false;
        setSelectionStart(-1);
        setSelectionEnd(-1);
        
        this.repaint();
    }
    
    /** this selection will be auto-selected when the next sequence is loaded
     * 
     * @param start
     * @param end
     */
    public void setSeqSelection(int start, int end){
        storeStart = start;
        storeEnd   = end;
    }
    
    public void selectedSeqPosition(int position) {
        if (  selectionLocked )
            return;
        //logger.info("selected seq position " + position);
        //setToolTipText("");
        
        setSelectionStart(position);
        setSelectionEnd(position);
        this.repaint();
        this.revalidate();
        //this.paintComponent(this.getGraphics());
        
    }
    
    public void selectedSeqRange(int start, int end) {
        if ( selectionLocked) 
            return;
        //setToolTipText("");
        setSelectionStart(start);
        setSelectionEnd(end);
        this.repaint();
        
    }
    
    public void selectionLocked(boolean flag) {
        selectionLocked = flag;
        
    }
    
    public void newObjectRequested(String accessionCode) {
       
        
    }
   
    public void noObjectFound(String accessionCode){
        chain = new ChainImpl();
        chainLength = 0;
    }
   
    
    public Group[] getSelection(){
        List sel = new ArrayList();
        List groups = chain.getGroups("amino");
        for ( int i = selectionStart ; i < selectionEnd;i++ ) {
            if ( i > groups.size())
                continue;
            Group g = (Group)groups.get(i);
            // Hm why is this needed? looks like the parent gets lost at some point ..
            g.setParent(chain);
            sel.add(g);
            
        }
        return (Group[])sel.toArray(new Group[sel.size()]);
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
    
    public void setScale(float scale) {
        
        this.scale=scale;
        this.repaint();
    }
    
    public void setChain(Chain c){
        chain = c;
        chainLength = c.getLengthAminos();
       
        if ( storeStart > -1){
            selectedSeqRange(storeStart,storeEnd);
            storeStart = -1;
            storeEnd   = -1;
        }
        
    }
    
    
    public void featureSelected(SpiceFeatureEvent e) {
   
        //setToolTipText(e.getFeature().toString());
        
        
    }






    public void mouseOverFeature(SpiceFeatureEvent e) {
       
        //setToolTipText(e.getFeature().toString());
        
    }






    public void mouseOverSegment(SpiceFeatureEvent e) {
        
        //setToolTipText(e.getSegment().toString());
        
    }


    public void segmentSelected(SpiceFeatureEvent e) {
        
        Segment segment = e.getSegment();
        selectedSeqRange(segment.getStart()-1,segment.getEnd()-1);
        //setToolTipText(segment.toString());
    }


    public void paintComponent(Graphics g){
        
        super.paintComponent(g);
        
        if (  ( selectionStart < 0) && (selectionEnd < 0))
            return;
        
        
        if ( ( chain == null) || (chainLength < 1))
            return;
        
        
//      translate the seq positions into graphics positions ..
        int tmpSelectionStart = selectionStart;
        if (( selectionStart < 0 ) && ( selectionEnd >=0)) {
            tmpSelectionStart = 0;
        }
        Graphics2D g2D =(Graphics2D) g;
        
        Composite oldComp = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));        
        
        g2D.setColor(selectionColor);
        
        
        int startX = Math.round(tmpSelectionStart *scale) + SequenceScalePanel.DEFAULT_X_START;
        int endX   = Math.round((selectionEnd-tmpSelectionStart+1)*scale) +1;
        if (endX <0)
            endX = 0;
        //logger.info("selection " + selectionStart + " " + selectionEnd + 
        //        " startX " + startX + " endX " + endX);
        g2D.fillRect(startX,0,endX,getHeight());
        g2D.setComposite(oldComp);
        
        //g2D.drawString("cursorPanel",10,40);
        
    }
    

    
}
