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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.biojava.spice.SpiceApplication;
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.Segment;
import org.biojava.spice.Panel.seqfeat.SelectedFeatureListener;
import java.net.URL;
import java.net.MalformedURLException;


/**
 * @author Andreas Prlic
 *
 */
public class TypeLabelPanel  {

    // use this font for the text
    public static final Font plainFont = new Font("SansSerif", Font.PLAIN, 10);
    
    
    public static final int    DEFAULT_X_START        = 10  ;
    public static final int    DEFAULT_X_RIGHT_BORDER = 0 ;
    public static final int    DEFAULT_Y_START        = 0 ;
    public static final int    DEFAULT_Y_STEP         = 10 ;
    public static final int    DEFAULT_Y_HEIGHT       = 4 ;
    public static final int    DEFAULT_Y_BOTTOM       = 16 ;
    public static final Color  SELECTION_COLOR        = Color.lightGray;
    public static final Color  SELECTED_TYPE_COLOR    = Color.yellow;
    public static final Color  BACKGROUND_COLOR       = Color.black;
    public static final int    DEFAULT_WIDTH             = 60;
    // the line where to draw the structure
    public static final int    DEFAULT_STRUCTURE_Y    = 20 ;
    public static final int    MINIMUM_HEIGHT         = 30;
    boolean selected;
    Feature[] features;
    List selectedFeatureListeners;
    boolean typeSelected ;
    int selectedType; 
    int oldSelectedType;
    BufferedImage imbuf;
    int canvasHeight ;
    boolean linkSelected;
    public static Logger logger = Logger.getLogger("org.biojava.spice");

    ImageIcon miniFirefox ;
    FeatureView parent;
    /**
     * 
     */
    public TypeLabelPanel(FeatureView parent) {
        super();
        this.parent = parent;
        //this.setBackground(Color.black);
        //this.setDoubleBuffered(true);
        //this.setPreferredSize(new Dimension(DEFAULT_WIDTH,MINIMUM_HEIGHT));
        //this.setWidth(DEFAULT_WIDTH);
        //this.setHeight(MINIMUM_HEIGHT);
        
        selected = false;
        selectedFeatureListeners = new ArrayList();
        
        typeSelected =false;
        selectedType = -1 ;
        oldSelectedType = -1;
        //initImgBuffer();
        canvasHeight = MINIMUM_HEIGHT;
        
        miniFirefox = createImageIcon("firefox10x10.png");
        
    }
    
    /** Returns an ImageIcon, or null if the path was invalid. */
    public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = SpiceApplication.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            logger.log(Level.WARNING,"Couldn't find file: " + path);
            return null;
        }
    }

   
    
    public void setSelected(boolean flag){
        selected = flag;
    }
    
    public void setSelectedType(int linenr){
        if ( linenr < 0){
            selectedType = linenr;
            typeSelected = false;
             
            oldSelectedType = selectedType;  
            linkSelected = false;
            return;
        }
        selectedType = linenr;
        typeSelected = true;
        
        oldSelectedType = selectedType;
    }
    
    public void setSelectedLink(int linenr, boolean flag) {
        
    		setSelectedType(linenr);
    		linkSelected = flag; 
    		
    }

    
    public void setFeatures(Feature[] features){
        // do something with the features.
        this.features = features;
    }
    
    public void addSelectedFeatureListener(SelectedFeatureListener lisi){
        selectedFeatureListeners.add(lisi);
    }
    public SelectedFeatureListener[] getSelectedFeatureListeners() {
        return (SelectedFeatureListener[])
        selectedFeatureListeners.toArray(
                	new SelectedFeatureListener[selectedFeatureListeners.size()]);
    }
    
   
   
    public int paintComponent( Graphics g, int width, int y) {
        //logger.info("paintComponent "  + label);
        //Graphics g2 = featureCanvas.getGraphics();
        Graphics2D g2D = (Graphics2D)g ;
        //BufferedImage imbuf = (BufferedImage)this.createImage(dstruc.width,dstruc.height);      
        
        // Set current alpha
        Composite oldComposite = g2D.getComposite();
        
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));                 
        g2D.setFont(plainFont);
        
        /////////////
        //      do the actual painting...
//      
        int newy = drawFeatures(g2D,width,y);
        
        if ( selected ){
            
                //if selected draw a white rectangle over everything
                g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));
                g2D.setColor(SELECTION_COLOR);
                g2D.fillRect(0,y,width,parent.getHeight());
        }
        
        g2D.setComposite(oldComposite);
        //featureCanvas.repaint();
        return newy;
    }
    
    /** draw the features starting at position y
     * returns the y coordinate of the last feature ;
     * */
    private int drawFeatures(Graphics g2D,int width, int y){
        
        boolean secstruc = false ;
        
        if ( features == null) 
            return y;
                
        for ( int f =0 ; f< features.length;f++) {
            
            Feature feature = features[f];
            y+= DEFAULT_Y_STEP;
                        
            List segments = feature.getSegments() ;
            
            // draw the firefox icon 
            String link = feature.getLink();
            if (( link != null) && (! link.equals(""))){
                URL url ;
                try {
                    url = new URL(link);
                } catch (MalformedURLException e){
                    continue ;
                }
                //g2D.drawString("L->", 1,y+DEFAULT_Y_HEIGHT);
                if ( miniFirefox != null)
                    miniFirefox.paintIcon(null, g2D, 1,y-DEFAULT_Y_HEIGHT);
            }
            
            // draw text
            if ( segments.size() < 1) {
                logger.finest("can not find segments in " + feature.getMethod());
                continue ;
            }
            Segment seg0 = (Segment) segments.get(0) ;
            Color col =  seg0.getColor();	
            g2D.setColor(col);
            
            g2D.drawString(feature.getName(), DEFAULT_X_START,y+DEFAULT_Y_HEIGHT);
            
            // draw selected type:
            if ( typeSelected ){
                if (f == selectedType) {
                    Graphics2D g2d = (Graphics2D)g2D ;
                		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));
                    
                		if ( linkSelected ){
                		    if (( link != null) && (! link.equals(""))){
                		        g2D.setColor(SELECTED_TYPE_COLOR);
                		        g2D.fillRect(0,y-(DEFAULT_Y_HEIGHT/2)-2,DEFAULT_X_START,DEFAULT_Y_STEP);
                		    }
                    } else {
                    
                        	//Dimension dstruc = this.getSize();
                    		g2d.setColor(SELECTED_TYPE_COLOR);
                    		g2d.fillRect(DEFAULT_X_START,y-(DEFAULT_Y_HEIGHT/2)-2,width,DEFAULT_Y_STEP);
                    		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
                    }
                }
            }
        }
        //y+= DEFAULT_Y_STEP;
        return y ;
    }
    
    public int getLineNr(int eventY){
       
        if ( features == null) return -1;
        
        int mouseY = eventY;
        
        float top = mouseY - DEFAULT_Y_STEP -1 ;
        // java.lang.Math.round((y-DEFAULT_Y_START)/ (DEFAULT_Y_STEP + DEFAULT_Y_HEIGHT-1));
        float interval  = DEFAULT_Y_STEP  ;
        
        int linenr = java.lang.Math.round(top/interval) -1 ;
        //logger.finest("top "+top+" interval "+ interval + " top/interval =" + (top/interval) );	
        //if ( linenr >= drawLines.size()){
            // can happen at bottom part
            // simply skip it ...
        //    return -1;
        //}
       
        if ( linenr > features.length ) return -1;
        
        return linenr ;
        
    }
    
    
}
