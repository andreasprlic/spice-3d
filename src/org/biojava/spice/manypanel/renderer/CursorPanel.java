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
import java.util.List;
import java.util.logging.Logger;
import org.biojava.bio.structure.*;
import org.biojava.spice.Feature.Segment;
import org.biojava.spice.manypanel.eventmodel.SequenceEvent;
import org.biojava.spice.manypanel.eventmodel.SequenceListener;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureEvent;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureListener;

public class CursorPanel 

extends JPanel
implements
SequenceListener,
SpiceFeatureListener

{
    static final long serialVersionUID = 92019290011924233l;
    public static final Color SELECTION_COLOR = Color.DARK_GRAY;
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
    
    //private BufferedImage imbuf;
    
    
    public CursorPanel() {
        super(); 
        this.setOpaque(false);
        setDoubleBuffered(true);
             
        selectionLocked = false;
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
    
    public void selectedSeqPosition(int position) {
        if (  selectionLocked )
            return;
        //logger.info("selected seq position " + position);
        //setToolTipText("");
        
        setSelectionStart(position);
        setSelectionEnd(position);
        this.repaint();
        
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
        chainLength = c.getLength();
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
        //logger.info("paint cursorPanel");
        super.paintComponent(g);
        
        //g.drawImage(imbuf, 0, 0, this);
        
        // translate the seq positions into graphics positions ..
        
        if (  ( selectionStart < 0) && (selectionEnd < 0)){
            return;
        }
        
        int tmpSelectionStart = selectionStart;
        if (( selectionStart < 0 ) && ( selectionEnd >=0)) {
            tmpSelectionStart = 0;
        }
        Graphics2D g2D =(Graphics2D) g;
        
        Composite oldComp = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));        
        
        g2D.setColor(SELECTION_COLOR);
        
        
        int startX = Math.round(tmpSelectionStart *scale) + FeaturePanel.DEFAULT_X_START;
        int endX   = Math.round((selectionEnd-tmpSelectionStart+1)*scale) ;
        if (endX <0)
            endX = 0;
        //logger.info("selection " + selectionStart + " " + selectionEnd + 
        //        " startX " + startX + " endX " + endX);
        g2D.fillRect(startX,0,endX,getHeight());
        g2D.setComposite(oldComp);
        
        //g2D.drawString("cursorPanel",10,40);
        
    }
    

    
}
