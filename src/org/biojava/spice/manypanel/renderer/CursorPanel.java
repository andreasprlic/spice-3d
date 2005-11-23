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
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.awt.event.*;



import org.biojava.bio.structure.*;
import org.biojava.spice.manypanel.eventmodel.SequenceListener;

public class CursorPanel 

extends JPanel
implements MouseMotionListener, MouseListener


{

        public static final Color SELECTION_COLOR = Color.DARK_GRAY;
        int selectionStart;
        int selectionEnd;
        int oldSelectionStart;
        int oldSelectionEnd;
        float scale;
        Chain chain;
        int chainLength;
        
        boolean dragging;
        int draggingStart;
        
        List sequenceListeners;
        
        static Logger logger = Logger.getLogger("org.biojava.spice");
        
       //private BufferedImage imbuf;
        
        
    public CursorPanel() {
        super(); 
        this.setOpaque(false);
        setDoubleBuffered(true);
        dragging = false;
        draggingStart = -1;
        clearSequenceListeners();
    }
    
    public void clearSequenceListeners(){
        sequenceListeners = new ArrayList();
    }
    
    public void addSequenceListener(SequenceListener li){
        sequenceListeners.add(li);
    }
    
    public void setSelectionStart(int start){
        if ( start < 0 )
            start = 0;
        if ( start > chainLength)
            start = chainLength;
        selectionStart = start;
    }
    
    public void setSelectionEnd(int end){
        if ( end < 0 )
            end = 0;
        if ( end > chainLength)
            end = chainLength;
        selectionEnd = end;
    }
    
    public void setScale(float scale) {
        // TODO Auto-generated method stub
        this.scale=scale;
        this.repaint();
    }
    
    public void setChain(Chain c){
        chain = c;
        chainLength = c.getLength();
    }
    
    /** get the sequence position of the current mouse event 
     * */
    public int getSeqPos(MouseEvent e) {
        
        int x = e.getX();
        //int y = e.getY();
        //float scale = seqScale.getScale();
        int DEFAULT_X_START = FeaturePanel.DEFAULT_X_START;
        int seqpos =  java.lang.Math.round((x-DEFAULT_X_START-2)/scale) ;
        
        return seqpos  ;
    }   
    
    
    public void paintComponent(Graphics g){
        //logger.info("paint cursorPanel");
        super.paintComponent(g);
        
        //g.drawImage(imbuf, 0, 0, this);
        
        Graphics2D g2D =(Graphics2D) g;
        
        Composite oldComp = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));        
        
        g2D.setColor(SELECTION_COLOR);
        
        // translate the seq positions into graphics positions ..
       
       
        int startX = Math.round(selectionStart *scale) + FeaturePanel.DEFAULT_X_START;
        int endX   = Math.round((selectionEnd-selectionStart+1)*scale) ;
       if (endX <1)
           endX = 1;
        //logger.info("selection " + selectionStart + " " + selectionEnd + 
        //        " startX " + startX + " endX " + endX);
        g2D.fillRect(startX,0,endX,getHeight());
        g2D.setComposite(oldComp);
       
        //g2D.drawString("cursorPanel",10,40);
        
        
        
    }
   
    
    
    public void mouseDragged(MouseEvent e) {
     
        
        if (dragging){
        
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
            this.repaint(); 
        }
        
        
    }
    
    
    

    public void mouseMoved(MouseEvent e) {
        //int x = e.getX();
        int pos = getSeqPos(e) ;
        if ( pos == oldSelectionStart)
            return;
        //logger.info("CursorPanel: mouse moved " + x + " " + pos);
        oldSelectionStart = pos;
        this.setSelectionStart(pos);
        this.setSelectionEnd(pos);
        
        triggerNewSequencePosition(pos);
        
        this.repaint();
        
        
    }
    
    private void triggerNewSequencePosition(int pos){
        Iterator iter = sequenceListeners.iterator();
        while(iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectedSeqPosition(pos);
        }
        
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

    public void mousePressed(MouseEvent e) {
        //logger.info("mouse pressed");
        dragging = true;
        int pos = getSeqPos(e);
        draggingStart=pos;
        selectionStart = pos ;
        //selectionEnd   = pos ;
        
    }
    
   

    public void mouseReleased(MouseEvent arg0) {
        //logger.info("mouse released");
        dragging = false ;
        draggingStart = -1;

    }
    
    private void triggerNewSequenceRange(int start,int end){
        Iterator iter = sequenceListeners.iterator();
        while(iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectedSeqRange(start,end);
        }
        
    }
    
    
}
