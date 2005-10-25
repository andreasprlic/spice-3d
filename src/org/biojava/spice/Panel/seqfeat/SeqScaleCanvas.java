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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import org.biojava.spice.Panel.seqfeat.FeaturePanel;


/**
 * @author Andreas Prlic
 *
 */
public class SeqScaleCanvas 
extends FeaturePanel 
{
//  the line where to draw the structure
    
    
    static final Color BACKGROUND_COLOR = Color.black;
     
    /**
     * 
     */
    public SeqScaleCanvas(FeatureView parent) {
        super(parent);
     
    }
    
    public void setSeqLength(int seqL){
        //System.out.println("SeqScaleCanv set SeqLength "+seqL + "seqlength" + seqLength + " scale:" + scale);
        super.setSeqLength(seqL);
        //this.seqLength = seqL;
        //System.out.println("having value seqLength" + this.seqLength);
    }
       
    public void setScale(float scale){
        
        //super.setScale(scale);
        this.scale = scale;
        //System.out.println("SeqScaleCanv seqlength" + this.seqLength + " scale:" + this.scale);
    }
    
    public float getScale(){ return scale;}
    public int getImageWidth(){
        return imbuf.getWidth();
    }
    
    
    public int paintComponent(Graphics g,int width, int y){
        
        //Dimension dstruc=this.getSize();
        float scale = getScale();
        //System.out.println("SeqScaleCanvas paintComponent at y" + y +
          //      	" seqLength:" + this.seqLength + 
            //    	" scale:" + this.scale);
        
        //int aminosize =  Math.round(1 * scale) ;  
        
        Graphics2D g2D = (Graphics2D)g ;
        
        // Set current alpha
        Composite oldComposite = g2D.getComposite();
        
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));                 
        //g2D.setFont(plainFont);
        
      
        /////////////
        //      do the actual painting...
        
        int newy = drawScale(g2D,scale,seqLength,y);
        //      draw region covered with structure
        //newy = drawStructureRegion(g2D,aminosize, newy);
        
        //int height = this.canvasHeight;
        int height = parent.getHeight();
        if ( selected ){
            // the whole featureview has been selected
            //if selected draw a rectangle over everything
            
            g2D.setColor(SELECTION_COLOR);
            g2D.fillRect(0,y,width,height);
            
        }
        
        
        g2D.setComposite(oldComposite);
        //featureCanvas.repaint();
        return newy ;
    }
    
    
    /** draw the Scale */
    private int drawScale(Graphics2D g2D, float scale, int length, int y){
        
        g2D.setColor(Color.GRAY);
        
        // the base line:
        int l = Math.round(length*scale)+DEFAULT_X_START ;
        
        //System.out.println("drawScale at y" + y + " l:" + l + " seqLength " + length + " scale " + scale);
        
        Rectangle seqline = new Rectangle(DEFAULT_X_START, y, l, 2);
        
        
        g2D.fill(seqline);
        for (int i =0 ; i<= length ; i++){
            if ( (i%100) == 0 ) {
                int xpos = Math.round(i*scale)+DEFAULT_X_START ;
                g2D.fillRect(xpos, y, 1, 10);
            }else if  ( (i%50) == 0 ) {
                int xpos = Math.round(i*scale)+DEFAULT_X_START ;
                g2D.fillRect(xpos, y, 1, 8);
            } else if  ( (i%10) == 0 ) {
                int xpos = Math.round(i*scale)+DEFAULT_X_START ;
                g2D.fillRect(xpos, y, 1, 4);
            }
        }
        
        return y+ 15;
    }
    
    /** draw the selected region */
    public void drawSelection(Graphics2D g2D, int aminosize, float scale, int y){
        
        //System.out.println("draw selection " + selectStart + " end" + selectEnd);
        if ( selectStart > -1 ) {
            //Dimension dstruc=this.getSize();
            g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));        
            
            g2D.setColor(SELECTION_COLOR);
            int seqx = java.lang.Math.round(selectStart*scale)+DEFAULT_X_START ;
            
            int selectEndX = java.lang.Math.round(selectEnd * scale)-seqx + DEFAULT_X_START +aminosize; 
            if ( selectEndX < aminosize) 
                selectEndX = aminosize ;
            
            if ( selectEndX  < 1 )
                selectEndX = 1 ;
            
            Rectangle selection = new Rectangle(seqx , y, selectEndX, parent.getHeight());
            g2D.fill(selection);
            
        }
    }
    
    
    /** draw structrure covered region as feature 
    private int drawStructureRegion(Graphics2D g2D, int aminosize, int y){
        // data is coming from chain;
      
        //g2D.drawString("Structure",1,DEFAULT_STRUCTURE_Y+DEFAULT_Y_HEIGHT);
        //System.out.println("draw structure " + chain.getLength());
        
        
        List segments = structureFeature.getSegments();
        Iterator iter = segments.iterator();
        
        while (iter.hasNext()){
            Segment s = (Segment) iter.next();
            int start = s.getStart();
            int end   = s.getEnd();
            drawStruc(g2D,start,end,aminosize, y);
        }
        y+= DEFAULT_Y_STEP;
        return y;
        
    }
    private void drawStruc(Graphics2D g2D, int start, int end, int aminosize, int y){
        //System.out.println("Structure " + start + " " + end);
        //int y = DEFAULT_STRUCTURE_Y ;
        
        int xstart = java.lang.Math.round(start * scale) + DEFAULT_X_START;
        int endx   = java.lang.Math.round(end * scale)-xstart + DEFAULT_X_START +aminosize;
        int width  = aminosize ;
        int height = DEFAULT_Y_HEIGHT ;
        
        // draw the red structure line
        g2D.setColor(STRUCTURE_COLOR);	
        g2D.fillRect(xstart,y,endx,height);
        
        /*
        // highlite the background
        Composite origComposite = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));
        g2D.setColor(STRUCTURE_BACKGROUND_COLOR);
        //Dimension dstruc=this.getSize();
        Rectangle strucregion = new Rectangle(xstart , y, endx, parent.getHeight());
        g2D.fill(strucregion);
        g2D.setComposite(origComposite);
        
    }
    */
    
    
    //public void setToolTip(String txt){
      //  this.setToolTipText(txt);
        
    //}
    
}


/*
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
            
        JPanel panel = parent.get
        parent.setToolTip("Sequence Position " + seqpos);
    
    }
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseClicked(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    
}
*/