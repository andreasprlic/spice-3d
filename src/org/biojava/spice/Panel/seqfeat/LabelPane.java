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
 * Created on Jun 9, 2005
 *
 */
package org.biojava.spice.Panel.seqfeat;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import org.biojava.spice.Feature.Feature;


/**
 * @author Andreas Prlic
 *
 */
public class LabelPane

extends SizeableJPanel
{
   
    public static final int    DEFAULT_X_SIZE         = 60;
    public static final int    DEFAULT_X_START        = 10  ;
    public static final int    DEFAULT_X_RIGHT_BORDER = 0 ;
    public static final int    DEFAULT_Y_START        = 0 ;
    public static final int    DEFAULT_Y_STEP         = 10 ;
    public static final int    DEFAULT_Y_HEIGHT       = 4 ;
    public static final int    DEFAULT_Y_BOTTOM       = 16 ;
    public static final int    MINIMUM_HEIGHT         = 30 ;
    // the line where to draw the structure
    public static final int    DEFAULT_STRUCTURE_Y    = 20 ;
    
    public static final Color SELECTION_COLOR         = Color.lightGray;
    public static final Color BACKGROUND_COLOR        = Color.BLACK;
    public static final Font plainFont = new Font("SansSerif", Font.PLAIN, 10);
    
    String label;
    int canvasHeight;
    int oldposition;
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    boolean dragging;
    //int height;
    BufferedImage imbuf;
    boolean selected;
    FeatureView parent;
    /**
     * 
     */
    public LabelPane(FeatureView parent) {
        super();
      
        this.parent = parent ;
        label="";
        
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        //this.setPreferredSize(new Dimension(DEFAULT_X_SIZE,MINIMUM_HEIGHT));
        this.setWidth(MINIMUM_HEIGHT);
        this.setHeight(DEFAULT_X_SIZE);
        
        int oldposition = 0;
        
        //height = MINIMUM_HEIGHT;
        selected =false;
        canvasHeight = MINIMUM_HEIGHT;
        initImgBuffer();
      
        
    }

    public FeatureView getParentFeatureView(){ return parent; }
    
    private void initImgBuffer(){
      
        Dimension dstruc = this.getSize();
        int width = getWidth();
        if ( width <= 0)
            width = DEFAULT_WIDTH;
        int height = canvasHeight;
        //System.out.println(width);
        if ( height <= 0) 
            height=MINIMUM_HEIGHT;
        
        
        imbuf = (BufferedImage)this.createImage(width,height); 
        //this.setSize(width,height);
        //this.setPreferredSize(new Dimension(width,height));
        this.setWidth(width);
        this.setHeight(height);
    }
    
   
    public void setCanvasHeight(int height) {
        canvasHeight = height;
        setHeight(height);
        //this.setPreferredSize(new Dimension(DEFAULT_X_SIZE,height));
        //this.height=height;
    }
    
    public int getCanvasHeight(){ return canvasHeight; }
    
    public void setSelected(boolean flag){
        selected = flag;
    }
    
    
    
    /** a FeatureView consists of a Label and the rendered features */
    
    public void paintComponent( Graphics g) {
        //logger.info("paintComponent "  + label);
        if( imbuf == null) initImgBuffer();
        super.paintComponent(g); 	
        g.drawImage(imbuf, 0, 0, this);
        Dimension dstruc=this.getSize();
        
        
        //Graphics g2 = featureCanvas.getGraphics();
        Graphics2D g2D = (Graphics2D)g ;
        //BufferedImage imbuf = (BufferedImage)this.createImage(dstruc.width,dstruc.height);      
        
        // Set current alpha
        Composite oldComposite = g2D.getComposite();
        
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));                 
        //g2D.setFont(plainFont);
        
        /////////////
        //      do the actual painting...
        // background ... :-/
        g2D.setColor(BACKGROUND_COLOR);
        g2D.fillRect(0,0,dstruc.width,dstruc.height);
        
        drawLabel(g2D);
        
        if ( selected ){
            //if selected draw a white rectangle over everything
            g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));
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
    public int drawLabel(Graphics g2D){
        
        //System.out.println("seqFeatCanvas aminosize "+ aminosize);
        g2D.setFont(plainFont);
        
        g2D.setColor(Color.white);
        g2D.drawString(this.label,DEFAULT_X_START,DEFAULT_Y_STEP);
        return DEFAULT_Y_STEP;
    }
    
    public void setLabel(String label){
        this.label=label;
    }
    public String getLabel(){
        return label;
        
    }
    
    
    public void setFeatures(Feature[] features){
        // do something with the features.
        //this.features = features;
        /*int height = (features.length*DEFAULT_Y_STEP) + DEFAULT_Y_START+DEFAULT_Y_STEP;
        if ( height < MINIMUM_HEIGHT ){
            height = MINIMUM_HEIGHT;
        }*/
        //this.setPreferredSize(new Dimension(DEFAULT_X_SIZE,height));
        //System.out.println("labelPane " + label + " setting size " + height );
    }
}


    
