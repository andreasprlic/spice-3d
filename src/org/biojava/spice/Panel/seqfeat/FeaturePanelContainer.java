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
 * Created on Jun 29, 2005
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
import java.util.Iterator;
import java.util.List;
import java.awt.event.MouseEvent;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.FeatureImpl;
import org.biojava.spice.Feature.Segment;

/**
 * @author Andreas Prlic
 *
 */
public class FeaturePanelContainer extends
        AbstractFeatureViewContainer {

    public static final Color SELECTION_COLOR            = Color.lightGray;
    public static final Color STRUCTURE_COLOR            = Color.red;
    public static final Color STRUCTURE_BACKGROUND_COLOR = new Color(0.5f, 0.1f, 0.5f, 0.5f);
    
    
    public static final int    DEFAULT_X_START        = 20  ;
    public static final int    DEFAULT_X_RIGHT_BORDER = 20 ;
    public static final int    DEFAULT_Y_START        = 0 ;
    public static final int    DEFAULT_Y_STEP         = 10 ;
    public static final int    DEFAULT_Y_HEIGHT       = 4 ;
    public static final int    DEFAULT_Y_BOTTOM       = 16 ;
    public static final int    DEFAULT_STRUCTURE_Y    = 15 ;
    Feature structureFeature;
    
    int seqpos, selectStart,selectEnd;
    boolean dragging ;
    int seqLength;
    FeaturePanelMouseListener featurePanelMouseListener;
    SpiceFeatureViewer parent;
    
    
    /**
     * 
     */
    public FeaturePanelContainer(SpiceFeatureViewer parent) {
        super();
        this.parent=parent;
        
        selectStart = selectEnd = seqpos = seqLength = -1 ;
        structureFeature = new FeatureImpl();
    }
    
    public void setFixedSize(Dimension d){
        this.setSize(d);
        this.setPreferredSize(d);
        this.setMinimumSize(d);
        this.setMaximumSize(d);
        this.revalidate();
    }
    
    public FeaturePanel getFeaturePanel(MouseEvent e){
        FeatureView fv = parent.getParentFeatureView(e);
        return fv.getFeaturePanel();
    }
    
    public void setFeaturePanelMouseListener(FeaturePanelMouseListener fpml){
        featurePanelMouseListener = fpml;
        this.addMouseMotionListener(fpml);
        this.addMouseListener(      fpml);
    }
    
    public FeaturePanelMouseListener getFeaturePanelMouseListener(){
        return featurePanelMouseListener;
    }
    
    
    public void setSeqLength(int length){
        seqLength = length;
    }

    public void setChain(Chain chain){
        structureFeature = new FeatureImpl();
        int start = -1;
        int end   = -1;
       
        for ( int i=0 ; i< chain.getLength() ; i++ ) {
            Group g = chain.getGroup(i);
            
            if ( g.size() > 0 ){
                if ( start == -1){
                    start = i;
                }
                end = i;
            } else {
                if ( start > -1) {
                    //drawStruc(g2D,start,end,aminosize);
                    
                    Segment s = new Segment();
                    s.setStart(start);
                    s.setEnd(end);
                    structureFeature.addSegment(s);
                    start = -1 ;
                }
            }
        }
        // finish
        if ( start > -1) {
            Segment s = new Segment();
            s.setStart(start);
            s.setEnd(end);
            structureFeature.addSegment(s);
        }
        
    }
    
    
    /** same as select here. draw a line at current seqence position
     * only if chain_number is currently being displayed
     */
    public void highlite( int seqpos){
        //System.out.println("featurePanel highlite seqpos" + seqpos + "seqLength " + seqLength);
        
        if ( seqpos > seqLength) 
            return ;
        
        selectStart  = seqpos ;
        selectEnd    = seqpos ;
        dragging = false;
      
      this.repaint();
        
    }
    
    /** highlite a region */
    public void highlite( int start , int end){
        
        
        //if (selectionLocked) return ;
        if ( (start > seqLength) || 
                (end   > seqLength)
        ) 	      
            return ;
        
        selectStart = start  ;
        selectEnd   = end    ;
        dragging = true;
        
        this.repaint();
        
    }
    
    /** get the sequence position of the current mouse event */
    public int getSeqPos(MouseEvent e) {
        
        int x = e.getX();
        int y = e.getY();
        float scale = seqScale.getScale();
        int seqpos =  java.lang.Math.round((x-DEFAULT_X_START-2)/scale) ;
        
        return seqpos  ;
    }	
    
    public void paintComponent( Graphics g) {
    
        initImgBuffer();
        
        super.paintComponent(g); 	
        g.drawImage(imbuf, 0, 0, this);
        
        // draw background ...
        g.setColor(java.awt.Color.black);
        g.fillRect(0,0,getWidth(),getPanelHeight());
        
        float scale = seqScale.getScale();
        
        int aminosize =  Math.round(1 * scale) ;
        
        drawStructureRegion((Graphics2D)g,aminosize,scale);
        
        
        // y is the position where to draw the next thingy
        int y = 0 ;
        int width = parent.getFeatureWidth();
        
        SeqScaleCanvas sscale      = seqScale.getSeqScaleCanvas();  
        sscale.paintComponent(g,width,y);
        y = seqScale.getHeight();
        
        Iterator iter = featureViews.iterator();
        while (iter.hasNext()){
            FeatureView fv = (FeatureView)iter.next();
            FeaturePanel      fp = fv.getFeaturePanel(); 
            //TypeLabelPanel tlp = fv.getTypePanel();
            //FeaturePanel   fp = fv.getFeaturePanel();
            
             fp.paintComponent(g,width,y);
             y+= fv.getHeight();
            //tlp.paintComponent(typeG,y);
            //y = fp.paintComponent(featureG,y);
        }
        
        
        
       
        drawSelection((Graphics2D)g,aminosize,scale);
        
    }
    
    /** draw the selected region */
    private void drawSelection(Graphics2D g2D, int aminosize, float scale){
        
        //System.out.println("draw selection " + selectStart + " end" + selectEnd);
        if ( selectStart > -1 ) {
            Dimension dstruc=this.getSize();
            Composite oldComp = g2D.getComposite();
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
            g2D.setComposite(oldComp);
            
        }
    }
    /** draw structrure covered region as feature */
    private void drawStructureRegion(Graphics2D g2D, int aminosize, float scale){
        // data is coming from chain;
      
        //g2D.drawString("Structure",1,DEFAULT_STRUCTURE_Y+DEFAULT_Y_HEIGHT);
        //System.out.println("draw structure " + structureFeature);
        
        List segments = structureFeature.getSegments();
        Iterator iter = segments.iterator();
        while (iter.hasNext()){
            Segment s = (Segment) iter.next();
            int start = s.getStart();
            int end   = s.getEnd();
            drawStruc(g2D,start,end,aminosize,scale);
        }
    }
    
    private void drawStruc(Graphics2D g2D, int start, int end, int aminosize,float scale){
        //System.out.println("Structure " + start + " " + end);
        
        int y = DEFAULT_STRUCTURE_Y ;
        
        int xstart = java.lang.Math.round(start * scale) + DEFAULT_X_START;
        int endx   = java.lang.Math.round(end * scale)-xstart + DEFAULT_X_START +aminosize;
        int width  = aminosize ;
        int height = DEFAULT_Y_HEIGHT ;
        
        // draw the red structure line
        g2D.setColor(STRUCTURE_COLOR);	
        g2D.fillRect(xstart,y,endx,height);
        
        // highlite the background
        Composite origComposite = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));
        g2D.setColor(STRUCTURE_BACKGROUND_COLOR);
        //Dimension dstruc=this.getSize();
        Rectangle strucregion = new Rectangle(xstart , 0, endx, parent.getHeight());
        g2D.fill(strucregion);
        g2D.setComposite(origComposite);
    }
    
    
}
