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
 * Created on Jun 6, 2005
 *
 */
package org.biojava.spice.Panel.seqfeat;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.biojava.spice.Panel.seqfeat.FeaturePanel;
import java.awt.event.MouseEvent;
/**
 * @author Andreas Prlic
 *
 */
public class SeqScaleCanvas 
extends FeaturePanel 
{
     
    static final Color BACKGROUND_COLOR = Color.black;
     
    /**
     * 
     */
    public SeqScaleCanvas() {
        super();
        
        this.setBackground(BACKGROUND_COLOR);
        
        ScaleMouseListener sl = new ScaleMouseListener(this); 
        this.addMouseListener(sl);
        this.addMouseMotionListener(sl);
        
        // set tooltip
        
    }
    
    
   
    public void setSeqLength(int length){
        //super.setScale(scale);
        super.setSeqLength(length);
        // move slider to 100% ...
       
    }
    
    public int getImageWidth(){
        return imbuf.getWidth();
    }
    
    public void paintComponent(Graphics g){
        super.paintComponent(g); 	
        g.drawImage(imbuf, 0, 0, this);
        
        Dimension dstruc=this.getSize();
        float scale = getScale();
      
        int aminosize =  Math.round(1 * scale) ;  
        
        Graphics2D g2D = (Graphics2D)g ;
        
        // Set current alpha
        Composite oldComposite = g2D.getComposite();
        
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));                 
        //g2D.setFont(plainFont);
        
      
        /////////////
        //      do the actual painting...
//      background ... :-/
        g2D.setColor(BACKGROUND_COLOR);
        g2D.fillRect(0,0,dstruc.width,dstruc.height);
        
        
        drawScale(g2D,scale,seqLength);
        drawSelection(g2D,aminosize,scale);
        
        if ( selected ){
            // the whole featureview has been selected
            //if selected draw a rectangle over everything
            
            g2D.setColor(SELECTION_COLOR);
            g2D.fillRect(0,0,dstruc.width,dstruc.height);
            
        }
        
        
        g2D.setComposite(oldComposite);
        //featureCanvas.repaint();
        
    }
    
    
    /** draw the Scale */
    public void drawScale(Graphics2D g2D, float scale, int chainlength){
        //System.out.println("drawScale");
        g2D.setColor(Color.GRAY);
        
        for (int i =0 ; i< chainlength ; i++){
            if ( (i%100) == 0 ) {
                int xpos = Math.round(i*scale)+DEFAULT_X_START ;
                g2D.fillRect(xpos, 0, 1, 10);
            }else if  ( (i%50) == 0 ) {
                int xpos = Math.round(i*scale)+DEFAULT_X_START ;
                g2D.fillRect(xpos, 0, 1, 8);
            } else if  ( (i%10) == 0 ) {
                int xpos = Math.round(i*scale)+DEFAULT_X_START ;
                g2D.fillRect(xpos, 0, 1, 4);
            }
        }
    }
    
    /** draw the selected region */
    public void drawSelection(Graphics2D g2D, int aminosize, float scale){
        
        //System.out.println("draw selection " + selectStart + " end" + selectEnd);
        if ( selectStart > -1 ) {
            Dimension dstruc=this.getSize();
            g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));        
            
            g2D.setColor(SELECTION_COLOR);
            int seqx = java.lang.Math.round(selectStart*scale)+DEFAULT_X_START ;
            
            int selectEndX = java.lang.Math.round(selectEnd * scale)-seqx + DEFAULT_X_START +aminosize; 
            if ( selectEndX < aminosize) 
                selectEndX = aminosize ;
            
            if ( selectEndX  < 1 )
                selectEndX = 1 ;
            
            Rectangle selection = new Rectangle(seqx , 0, selectEndX, dstruc.height);
            g2D.fill(selection);
            
        }
    }
    
    public void setToolTip(String txt){
        this.setToolTipText(txt);
        
    }
    
}

class ScaleMouseListener
implements MouseListener, MouseMotionListener
{
    SeqScaleCanvas parent;
    public ScaleMouseListener(SeqScaleCanvas parent){
        this.parent = parent ;
    }
    
    public void mouseDragged(MouseEvent e){}
    public void mouseMoved(MouseEvent e){
        // get sequence position of this mouseEvent ...
        int seqpos = parent.getSeqPos(e);
        if ( seqpos < 0)
            return ;
        if ( seqpos >= parent.getSeqLength())
            return;
            
        parent.setToolTip("Sequence Position " + seqpos);
    
    }
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseClicked(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    
}
