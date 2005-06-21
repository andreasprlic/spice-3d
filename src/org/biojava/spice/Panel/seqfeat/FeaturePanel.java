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
 * Created on Jun 5, 2005
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
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.*;

import java.util.List;
import java.util.Iterator;
import java.util.logging.Logger;

import org.biojava.bio.structure.Group;
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.FeatureImpl;
import org.biojava.spice.Feature.Segment;
import org.biojava.spice.Panel.seqfeat.SelectedFeatureListener;
import org.biojava.bio.structure.*;
/**
 * @author Andreas Prlic
 *
 */
public class FeaturePanel 
	extends SizeableJPanel
	implements SelectedFeatureListener
	{
   
    public static final int    DEFAULT_X_START        = 20  ;
    public static final int    DEFAULT_X_RIGHT_BORDER = 20 ;
    public static final int    DEFAULT_Y_START        = 0 ;
    public static final int    DEFAULT_Y_STEP         = 10 ;
    public static final int    DEFAULT_Y_HEIGHT       = 4 ;
    public static final int    DEFAULT_Y_BOTTOM       = 16 ;
    
    
    public static final int    MINIMUM_HEIGHT         = 30;
    
    public static final Color BACKGROUND_COLOR        = Color.black;
    public static final Color SELECTION_COLOR         = Color.lightGray;
    public static final Color SELECTED_FEATURE_COLOR  = Color.yellow;
    public static final Color STRUCTURE_COLOR         = Color.red;
    public static final Color STRUCTURE_BACKGROUND_COLOR = new Color(0.5f, 0.1f, 0.5f, 0.5f);
    
    
    int mouseDragStart ;
    //int height;
    Feature[] features;
    int seqLength;
    JPopupMenu popupMenu;
    int oldposition;
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    boolean dragging;
    
    int selectStart;
    int selectEnd;
    boolean selected;
    boolean featureSelected;
    int selectedFeaturePos;
    //int oldSelectedFeaturePos;
    boolean isLoading;
    JProgressBar progressBar;
    float scale;
    BufferedImage imbuf;
    int canvasHeight;
    FeaturePanelMouseListener featurePanelMouseListener;
    Feature structureFeature;
    /**
     * 
     */
    public FeaturePanel() {
        super();
        
        scale = 2.0f;
        seqLength = 0;
        features = null;
        this.setBackground(BACKGROUND_COLOR);
        //this.setOpaque(true);
        this.setDoubleBuffered(true);
        //this.setPreferredSize(new Dimension(400,MINIMUM_HEIGHT));
        this.setWidth(400);
        this.setHeight(MINIMUM_HEIGHT);
        
        popupMenu = new JPopupMenu();
        featurePanelMouseListener = null;
        int oldposition = 0;
        
        selectStart    = -1 ;
        selectEnd      =  1 ;
        selectedFeaturePos = -1;
        //oldSelectedFeaturePos  =1;
        featureSelected = false;
        //height = MINIMUM_HEIGHT;
       selected =false;
       
       isLoading = false;
       progressBar = new JProgressBar(0,100);
       progressBar.setStringPainted(true); //get space for the string
       progressBar.setString("");          //but don't paint it
       progressBar.setIndeterminate(true);
       progressBar.setValue(0);
       //progressBar.setMaximumSize(new Dimension(400,20));
       progressBar.setBorder(BorderFactory.createEmptyBorder());
       
        progressBar.setVisible(false);
        
        float scale = getScale();
        initImgBuffer();
        structureFeature = new FeatureImpl();
        this.add(progressBar);
    }
    
    public void setFeaturePanelMouseListener(FeaturePanelMouseListener fpml){
        featurePanelMouseListener = fpml;
        this.addMouseMotionListener(fpml);
        this.addMouseListener(      fpml);
    }
    
    public FeaturePanelMouseListener getFeaturePanelMouseListener(){
        return featurePanelMouseListener;
    }
    
    
    public void setLoading(boolean flag){
        isLoading = flag;
        
        this.progressBar.setVisible(flag);
        if ( isLoading)
            this.progressBar.setSize(this.getSize());
    }
    
    public void setSelected(boolean flag){
        selected = flag;
    }
    
    /*public int getHeight(){
        return height; 
    }*/
    public void setCanvasHeight(int height) {
      //Dimension d = getSize();
      //setPreferredSize(new Dimension(d.width, height));
      canvasHeight = height;
      this.setHeight(height);
    }
    
    public int getCanvasHeight(){ return canvasHeight;}
    public void setFeatures(Feature[] features){
       
        this.features = features;

    }
    
    public void setSeqLength(int seqLength){
        this.seqLength = seqLength;
        initImgBuffer();
        this.revalidate();
    }
    
    public int getSeqLength(){
        return seqLength;
    }
    
    private void initImgBuffer(){
        int aminosize =  Math.round(1 * scale) ;
        Dimension dstruc = this.getSize();
        int width = this.getImgWidth(aminosize);
        int height = getCanvasHeight();
        //System.out.println(width);
        if ( height <= 0) 
            height=MINIMUM_HEIGHT;
        
        imbuf = (BufferedImage)this.createImage(width,height); 
        
        
        //g2D.fillRect(0,0,dstruc.width,dstruc.height);
        //this.setSize(width,height);
        //this.setPreferredSize(new Dimension(width,height));
        this.setHeight(height);
        this.setWidth(width);
    }
    
    
    
    private int getImgWidth(int aminosize){
        // 
        //if ( aminosize < 1) 
            //aminosize =1;
        //int imgwidth = DEFAULT_X_START+DEFAULT_X_RIGHT_BORDER + (seqLength* aminosize);
        //int imgwidth =  (seqLength * aminosize);
        int imgwidth = Math.round(seqLength * scale) + DEFAULT_X_START + DEFAULT_X_RIGHT_BORDER;
        if (imgwidth <= 0 )
            imgwidth = 1;
        return imgwidth;
    }
    /** a FeatureView consists of a Label and the rendered features */
    
    public void paintComponent( Graphics g) {
        //System.out.println("paintComponent - featurePanel");
        if( imbuf == null) initImgBuffer();
      
        super.paintComponent(g); 	
        g.drawImage(imbuf, 0, 0, this);
        
        //logger.info("paintComponent "  + label);
//      Graphics g2 = featureCanvas.getGraphics();
        Graphics2D g2D = (Graphics2D)g ;
        float scale = getScale();
        //	 minimum size of one aminoacid
        int aminosize =  Math.round(1 * scale) ;
        Dimension dstruc=new Dimension ( getImgWidth(aminosize), getHeight());
        
        /////////////
        //      do the actual painting...
        
        
        // Set current alpha
        Composite oldComposite = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));                 

         
        // background ... :-/ ???
        // seems that img does not have background ...
        g2D.setColor(BACKGROUND_COLOR);
        g2D.fillRect(0,0,dstruc.width,dstruc.height);
        
        if ( isLoading){
            //System.out.println("isLoading - return");
            return;
        }
       
        // draw region covered with structure
        drawStructureRegion(g2D,aminosize);
        
        int y = drawFeatures(g2D,aminosize,DEFAULT_Y_START,seqLength,scale);
     
       
        
        drawSelection(g2D, aminosize, scale);
        
        if ( selected ){
            // the whole featureview has been selected
            //if selected draw a rectangle over everything
            
            g2D.setColor(SELECTION_COLOR);
            g2D.fillRect(0,0,dstruc.width,dstruc.height);
            
        }
        
        
        
        //this.setPreferredSize(new Dimension(y+DEFAULT_Y_STEP,dstruc.width));
        g2D.setComposite(oldComposite);
        //featureCanvas.repaint();
        
    }
    
    /** draw the features starting at position y
     * returns the y coordinate of the last feature ;
     * */
    public int drawFeatures(Graphics g, int aminosize, int y, int chainlength,float scale){
        
        Graphics2D g2D =(Graphics2D) g;
        //System.out.println("FeaturePanel drwaFeatures aminosize "+ aminosize);
        
        boolean secstruc = false ;
        
        if ( features == null) 
            return y;
                        
        for ( int f =0 ; f< features.length;f++) {
            
            y+= DEFAULT_Y_STEP;
           
            Feature feature = features[f];
            List segments = feature.getSegments() ;
             
            Segment seg0 = (Segment) segments.get(0) ;
            
            Color col =  seg0.getColor();	
            g2D.setColor(col);
            
            if ( featureSelected){
                if (f == selectedFeaturePos) {
                    Dimension dstruc = this.getSize();
                    g2D.setColor(SELECTED_FEATURE_COLOR);
                    Composite oldComp = g2D.getComposite();
                    g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN ,0.6f));
                    g2D.fillRect(0,y,dstruc.width,DEFAULT_Y_HEIGHT);
                    g2D.setComposite(oldComp);
                }
            }
            //g2D.drawString(feature.getName(), 1,y+DEFAULT_Y_HEIGHT);
            
            for (int s=0; s<segments.size();s++){
                Segment segment=(Segment) segments.get(s);
                
                int start     = segment.getStart() -1 ;
                int end       = segment.getEnd()   -1 ;
                
                // hum some people say this if annotation relates to whole seq.
                if (( start == -1) && ( end == -1 )){
                    //System.out.println(feature);
                    start = 0;
                    end = chainlength - 1;
                    segment.setStart(1);
                    segment.setEnd(chainlength);
                }
                
                if ( ! (featureSelected && ( f== selectedFeaturePos))){
                    col = segment.getColor();
                    g2D.setColor(col);
                }
                
                int xstart =  java.lang.Math.round(start * scale) + DEFAULT_X_START;
                int width   = java.lang.Math.round(  end * scale) - xstart +  DEFAULT_X_START+aminosize ;
                
                int height = DEFAULT_Y_HEIGHT ;
                //logger.info(feature+ " " + end +" " + width);
                //logger.finest("color"+entColors[i%entColors.length]);
                //logger.info("new feature  ("+f+"): x1:"+ xstart+" y1:"+y+" width:"+width+" height:"+height);
                String type = feature.getType() ;
                if (  type.equals("DISULFID")){
                    
                    g2D.fillRect(xstart,y,aminosize,height);
                    g2D.fillRect(xstart,y+(height/2),width,1);
                    g2D.fillRect(xstart+width-aminosize,y,aminosize,height);
                } else {
                    g2D.fillRect(xstart,y,width,height);
                }
            }
            
            
        }
        
        return y ;
    }
    
    /** draw the selected region */
    public void drawSelection(Graphics2D g2D, int aminosize, float scale){
        
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
    
    /** draw structrure covered region as feature */
    private void drawStructureRegion(Graphics2D g2D, int aminosize){
        // data is coming from chain;
      
        //g2D.drawString("Structure",1,DEFAULT_STRUCTURE_Y+DEFAULT_Y_HEIGHT);
        //System.out.println("draw structure " + chain.getLength());
        
        List segments = structureFeature.getSegments();
        Iterator iter = segments.iterator();
        while (iter.hasNext()){
            Segment s = (Segment) iter.next();
            int start = s.getStart();
            int end   = s.getEnd();
            drawStruc(g2D,start,end,aminosize);
        }
        
    }
    private void drawStruc(Graphics2D g2D, int start, int end, int aminosize){
        //System.out.println("Structure " + start + " " + end);
        //int y = DEFAULT_STRUCTURE_Y ;
        
        int xstart = java.lang.Math.round(start * scale) + DEFAULT_X_START;
        int endx   = java.lang.Math.round(end * scale)-xstart + DEFAULT_X_START +aminosize;
        int width  = aminosize ;
        //int height = DEFAULT_Y_HEIGHT ;
        
        // draw the red structure line
        //g2D.setColor(STRUCTURE_COLOR);	
        //g2D.fillRect(xstart,y,endx,height);
        
        // highlite the background
        Composite origComposite = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));
        g2D.setColor(STRUCTURE_BACKGROUND_COLOR);
        //Dimension dstruc=this.getSize();
        Rectangle strucregion = new Rectangle(xstart , 0, endx, getHeight());
        g2D.fill(strucregion);
        g2D.setComposite(origComposite);
    }
    
    
    /** a feature has been selected */
    public void selectedFeature(Feature feat){
        //System.out.println("selected feature " + feat);
        
        if ( feat == null) { 
            featureSelected = false;
            selectedFeaturePos = -1;
        		this.repaint();
        		return;
        }
        featureSelected = true;
        // determin on which position this featue is ...
        for ( int f =0 ; f< features.length;f++) {
            
            Feature feature = features[f];
            if ( feature.equals(feat)){
                // check also all segments ...
                List tmpsegs = feat.getSegments();
                List segs = feature.getSegments();
                if ( tmpsegs.equals(segs)){
                
                    selectedFeaturePos = f;
                }
                //System.out.println("featurepanel selected feature " + feat + " pos" + f);
            }
        }
        //if ( oldSelectedFeaturePos != selectedFeaturePos){
          //  this.repaint();
        //}
        this.repaint();
        //oldSelectedFeaturePos = selectedFeaturePos;
    }
    
    public void setScale(float scale){
        this.scale=scale;
        initImgBuffer();
        this.revalidate();
    }
    
    public float getScale(){
        return scale;
       
    }
    
    /** get the sequence position of the current mouse event */
    public int getSeqPos(MouseEvent e) {
        
        int x = e.getX();
        int y = e.getY();
        float scale = getScale();
        int seqpos =  java.lang.Math.round((x-DEFAULT_X_START-2)/scale) ;
        
        return seqpos  ;
    }	
    
    /* check if the mouse is over a feature and if it is 
     * return the feature number 
     * @author andreas
     *
     * To change the template for this generated type comment go to
     * Window - Preferences - Java - Code Generation - Code and Comments
     */
    public int getLineNr(MouseEvent e){
        int mouseY = e.getY();
        
        float top = mouseY - DEFAULT_Y_START +1 ;
        // java.lang.Math.round((y-DEFAULT_Y_START)/ (DEFAULT_Y_STEP + DEFAULT_Y_HEIGHT-1));
        float interval  = DEFAULT_Y_STEP  ;
        
        int linenr = java.lang.Math.round(top/interval) -1 ;
        //logger.finest("top "+top+" interval "+ interval + " top/interval =" + (top/interval) );	
        
        
        return linenr ;
        
    }
    
    /** same as select here. draw a line at current seqence position
     * only if chain_number is currently being displayed
     */
    public void highlite( int seqpos){
        //System.out.println("featurePanel highlite seqpos" + seqpos);
        
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
    
    
}
