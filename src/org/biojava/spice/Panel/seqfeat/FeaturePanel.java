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
//import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.List;
import java.util.logging.Logger;
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.Segment;
import org.biojava.spice.Panel.seqfeat.SelectedFeatureListener;
import org.biojava.spice.Config.SpiceDasSource;
import java.util.Map;
import java.util.HashMap;

/** The class responsible for painting of 2D - features.
 * 
 * @author Andreas Prlic
 *
 */
public class FeaturePanel 
//extends SizeableJPanel
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
    
    int mouseDragStart ;
    //int height;
    
    Feature[] features;
    int seqLength;
    float scale;
    
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
    
    BufferedImage imbuf;
    int canvasHeight;
    
    
    FeatureView parent;
    /**
     * 
     */
    public FeaturePanel(FeatureView parent) {
        super();
        this.parent=parent;
        scale = 2.0f;
        seqLength = 0;
        features = null;
        
        popupMenu = new JPopupMenu();
        
        //int oldposition = 0;
        
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
        
        //float scale = getScale();
        //initImgBuffer();
        
        // this.add(progressBar);
    }
    
    
    public void setLoading(boolean flag){
        isLoading = flag;
        
        this.progressBar.setVisible(flag);
        if ( isLoading)
            progressBar.setSize(new Dimension(100,30));
    }
    
    public void setSelected(boolean flag){
        selected = flag;
    }
    
    
    //public int getCanvasHeight(){ return canvasHeight;}
    public void setFeatures(Feature[] features){
        //logger.finest("received " +features.length + "features  from" +parent.getDasSource().getNickname());
        this.features = features;
        
    }
    
    public void setSeqLength(int seqL){
        //System.out.println("featurePanel setSeqLength " + seqL);
        seqLength = seqL;
        //initImgBuffer();
        //this.revalidate();
    }
    
    public int getSeqLength(){
        return seqLength;
    }
    
    
    /*
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
    */
    
    
    /** a FeatureView consists of a Label and the rendered features */
    
    public int paintComponent( Graphics g, int width, int y) {
        //System.out.println("FeaturePanel paintComponent - y" + y + " width:" + width + " seqLength:"+seqLength);
        
        //logger.info("paintComponent "  + label);
        //      Graphics g2 = featureCanvas.getGraphics();
        Graphics2D g2D = (Graphics2D)g ;
        float scale = getScale();
        //	 minimum size of one aminoacid
        int aminosize =  Math.round(1 * scale) ;
        //Dimension dstruc=new Dimension ( getImgWidth(aminosize), getHeight());
        
        /////////////
        //      do the actual painting...
        
        
        // Set current alpha
        Composite oldComposite = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));                 
        
        
        // background ... :-/ ???
        // seems that img does not have background ...
        //g2D.setColor(BACKGROUND_COLOR);
        //g2D.fillRect(0,0,dstruc.width,dstruc.height);
        
        if ( isLoading){
            //System.out.println("isLoading - return");
            return y;
        }
        
        int newy = drawFeatures(g2D,aminosize,width,y,seqLength,scale);
        
        if ( selected ){
            // the whole featureview has been selected
            //if selected draw a rectangle over everything
            
            g2D.setColor(SELECTION_COLOR);
            g2D.fillRect(0,y,width,parent.getHeight());
            
        }
        
        //this.setPreferredSize(new Dimension(y+DEFAULT_Y_STEP,dstruc.width));
        g2D.setComposite(oldComposite);
        //featureCanvas.repaint();
        return newy;
    }
    
    /** draw the features starting at position y
     * returns the y coordinate of the last feature ;
     * */
    private int drawFeatures(Graphics g, int aminosize, int fullwidth, int y, int chainlength,float scale){
        
        
        //System.out.println("FeaturePanel drwaFeatures aminosize "+ aminosize + " y " + y);
        //logger.info("drawFeatures " + features);
        //boolean secstruc = false ;
        
        if ( features == null) 
            return y;
        
        SpiceDasSource ds = parent.getDasSource();
        Map[] styleSheetMap = ds.getStylesheet() ;
        if (( styleSheetMap == null) || (styleSheetMap.length == 0) ) {
            //logger.info(parent.getDasSource().getNickname() + " did not provide stylesheet");
            y = paintNoStylesheetFeatures(g,aminosize,fullwidth,y,chainlength,scale);
        }
        else {
            //logger.info(parent.getDasSource().getNickname() + "painting with stylesheet");
            y = paintStylesheetFeatures(styleSheetMap,g,aminosize,fullwidth,y,chainlength,scale);
        }
        return y;
    }
    
    private int paintNoStylesheetFeatures(Graphics g, int aminosize,int fullwidth,int y,int chainlength, float scale) {
        
        
        for ( int f =0 ; f< features.length;f++) {
            
            y += DEFAULT_Y_STEP;
            Feature feature = features[f];
            
            checkDrawSelectedFeature(feature,f,DEFAULT_Y_HEIGHT,g,aminosize,fullwidth,y,chainlength,scale);
            setColor(g,feature,new HashMap());
            String featureType = feature.getType();
            
            if (  featureType.equals("DISULFID")){
                drawSpanFeature(feature,f,DEFAULT_Y_HEIGHT,g,aminosize,fullwidth,y,chainlength,scale);
                
            } else if (  featureType.equals("SECSTRUC") || 
                    featureType.equals("HELIX") || 
                    featureType.equals("STRAND") || 
                    featureType.equals("COIL") ||
                    featureType.equals("TURN")
            ){
                drawSecstrucFeature(feature,f,DEFAULT_Y_HEIGHT,g,aminosize,fullwidth,y,chainlength,scale);
            } else { 
                drawLineFeature(feature,f,DEFAULT_Y_HEIGHT,g,aminosize,fullwidth,y,chainlength,scale);
            }
            //drawLineFeature(feature,f,DEFAULT_Y_HEIGHT,g,aminosize,fullwidth,y,chainlength,scale);
        }
        return y;
        
    }
    
    /** set the color to be used for painting 
     * 
     * @param feature
     * @param style
     * @return
     */
    private void setColor(Graphics g, Feature feature, Map style){
        Color c = (Color) style.get("color");
        if ( c != null) {
            
            //logger.info("using stylesheet defined color " + c);
            g.setColor(c);
        } else {
            //logger.info("no stylesheet defined color found for" + feature.getName());
            if ( feature.getType().equals("DISULFID")){
                g.setColor(Color.yellow);
            } else {
                setDefaultColor(g,feature);
            }
        }
    }
    
    private void setDefaultColor(Graphics g, Feature feature){
        List segments = feature.getSegments();
        Segment seg0 = (Segment) segments.get(0) ;
        
        Color col =  seg0.getColor();	
        g.setColor(col);
    }
    
    private int paintStylesheetFeatures(Map[] style,Graphics g, int aminosize,int fullwidth,int y,int chainlength, float scale) {
        //logger.info("paintSylesheetFeatures " );
        //Graphics2D g2D =(Graphics2D) g;
        
        for ( int f =0 ; f< features.length;f++) {
            
            y += DEFAULT_Y_STEP;
            
            Feature  feat = features[f];
            String featureType = feat.getType();
            
            checkDrawSelectedFeature(feat,f,DEFAULT_Y_HEIGHT,g,aminosize,fullwidth,y,chainlength,scale);
            
            boolean matchingStyle = false ;
            for (int m=0; m< style.length;m++){
                Map s = style[m];
               // logger.finest(" style:" + s);
                String styleType = (String) s.get("type");
                if ( styleType.equals(featureType) ){
                    // this style sheet applies here!
                    //logger.info("drawing " + styleType + " with stylesheet support");
                    setColor(g,feat,s);
                    
                    String featStyle = (String)s.get("style");
                    int h = getDrawHeight(s);
                    
                    if ( featStyle.equals("line")){
                        //logger.finest("drawing line + style ");
                        matchingStyle = true ;
                        
                        drawLineFeature(feat,f,h,g,aminosize,fullwidth,y,chainlength,scale);
                    }
                    else if ( featStyle.equals("box")) {
                        //logger.finest("drawing box + style ");
                        matchingStyle = true ;
                        drawBoxFeature(feat,f,h,g,aminosize,fullwidth,y,chainlength,scale);
                    }
                    else if ( featStyle.equals("span")) {
                        //logger.finest("drawing span + style ");
                        matchingStyle = true ;
                        drawSpanFeature(feat,f,h,g,aminosize,fullwidth,y,chainlength,scale);
                    }
                    else if ( featStyle.equals("triangle")) {
                        // logger.finest("drawing triangle + style ");
                        matchingStyle = true ;
                        drawTriangleFeature(feat,f,h,g,aminosize,fullwidth,y,chainlength,scale);
                    }
                    else if ( featStyle.equals("helix")) {
                        // logger.finest("drawing triangle + style ");
                        matchingStyle = true ;
                        drawHelixFeature(feat,f,h,g,aminosize,fullwidth,y,chainlength,scale);
                    }
                    else if ( featStyle.equals("arrow")) {
                        // logger.finest("drawing triangle + style ");
                        matchingStyle = true ;
                        drawArrowFeature(feat,f,h,g,aminosize,fullwidth,y,chainlength,scale);
                    }
                    
                    else {
                        logger.finest("could not find matching feat. style " + featStyle + " falling back to default.");
                    }
                } 
            }
            
            if ( ! matchingStyle){
                // color has not been set ...
                setDefaultColor(g,feat);
                
                //logger.finest("no matching stylesheet found for feature type " + featureType);
                // no stylesheet type has been found that describes how to paint this feature - use default...
                if (  featureType.equals("DISULFID")){
                    drawSpanFeature(feat,f,DEFAULT_Y_HEIGHT,g,aminosize,fullwidth,y,chainlength,scale);
                } else if (  featureType.equals("SECSTRUC") || 
                        featureType.equals("HELIX") || 
                        featureType.equals("STRAND") || 
                        featureType.equals("COIL") ||
                        featureType.equals("TURN")
                ){
                    drawSecstrucFeature(feat,f,DEFAULT_Y_HEIGHT,g,aminosize,fullwidth,y,chainlength,scale);
                } else { 
                    drawLineFeature(feat,f,DEFAULT_Y_HEIGHT,g,aminosize,fullwidth,y,chainlength,scale);
                }
            }
        }
        
        return y;
    }
    
    private int getDrawHeight(Map styleMap){
        
        String height = (String)styleMap.get("height");
        int h = DEFAULT_Y_HEIGHT;
        if ( height != null){
            try {
                h = Integer.parseInt(height);
            } catch (Exception e){}
        }
        
        if (h > DEFAULT_Y_HEIGHT){
            h = DEFAULT_Y_HEIGHT;
        }
        return h;
    }
    
    /** check if the feature is selected, if yest, color the background in SELECTED_FEATURE_COLOR
     * 
     * @param feature
     * @param featurePos
     * @param drawHeight
     * @param g
     * @param aminosize
     * @param fullwidth
     * @param y
     * @param chainlength
     * @param scale
     */
    
    private void checkDrawSelectedFeature(Feature feature,int featurePos, int drawHeight,Graphics g, int aminosize,int fullwidth,int y,int chainlength, float scale){
        Graphics2D g2D =(Graphics2D) g;
        int f = featurePos;
        if ( featureSelected){
            if (f == selectedFeaturePos) {
                //Dimension dstruc = this.getSize();
                g2D.setColor(SELECTED_FEATURE_COLOR);
                Composite oldComp = g2D.getComposite();
                g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN ,0.6f));
                g2D.fillRect(0,y,fullwidth,drawHeight);
                g2D.setComposite(oldComp);
            }
        }
    }
    
    /** draw a solid rectangle 
     * 
     * @param feature
     * @param featurePos
     * @param drawHeight
     * @param g
     * @param aminosize
     * @param fullwidth
     * @param y
     * @param chainlength
     * @param scale
     */
    private void drawLineFeature(Feature feature,int featurePos, int drawHeight,Graphics g, int aminosize,int fullwidth,int y,int chainlength, float scale) 
    {
        // logger.finest("draw Line Feature " + feature );
        Graphics2D g2D =(Graphics2D) g;
        List segments = feature.getSegments() ;
        //int f = featurePos;     
        
        //Segment seg0 = (Segment) segments.get(0) ;
        
        //Color col =  seg0.getColor();	
        //g2D.setColor(col);
        
        
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
            
            //if ( ! (featureSelected && ( f== selectedFeaturePos))){
            //    col = segment.getColor();
            //    g2D.setColor(col);
            //}
            
            int xstart =  java.lang.Math.round(start * scale) + DEFAULT_X_START;
            int width   = java.lang.Math.round(  end * scale) - xstart +  DEFAULT_X_START+aminosize ;
            
            int height = drawHeight ;
            
            // draw the line ...
            g2D.fillRect(xstart,y,width,height);
            
        }
    }
    
    private void drawTriangleFeature(Feature feature,int featurePos, int drawHeight,Graphics g, int aminosize,int fullwidth,int y,int chainlength, float scale) 
    {
        // logger.finest("draw Triangle Feature " + feature );
        Graphics2D g2D =(Graphics2D) g;
        List segments = feature.getSegments() ;
        //int f = featurePos;     
        
        //Segment seg0 = (Segment) segments.get(0) ;
        
        //Color col =  seg0.getColor();	
        //g2D.setColor(col);
        
        
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
            
            //if ( ! (featureSelected && ( f== selectedFeaturePos))){
            //    col = segment.getColor();
            //    g2D.setColor(col);
            //}
            
            int xstart =  java.lang.Math.round(start * scale) + DEFAULT_X_START;
            int width   = java.lang.Math.round(  end * scale) - xstart +  DEFAULT_X_START+aminosize ;
            
            //int height = drawHeight ;
            
            // draw the line ...
            //g2D.fillRect(xstart,y,width,height);
            int middlex = xstart + (width/2);
            g2D.drawLine(xstart,(y+DEFAULT_Y_HEIGHT),middlex,y);
            g2D.drawLine(middlex,y,xstart+width,(y+DEFAULT_Y_HEIGHT));
            g2D.drawLine(xstart,(y+DEFAULT_Y_HEIGHT),xstart+width,(y+DEFAULT_Y_HEIGHT));
            
            
            
        }
    }
    
    
    private void drawSecstrucFeature(Feature feature,int featurePos, int drawHeight,Graphics g, int aminosize,int fullwidth,int y,int chainlength, float scale) 
    {
        //logger.finest("draw Secstruc Feature " + feature );
        Graphics2D g2D =(Graphics2D) g;
        List segments = feature.getSegments() ;
        //int f = featurePos;     
        
        //Segment seg0 = (Segment) segments.get(0) ;
        
        //Color col =  seg0.getColor();	
        //g2D.setColor(col);
        
        
        //g2D.drawString(feature.getName(), 1,y+DEFAULT_Y_HEIGHT);
        //logger.finest(""+feature.getName());
        
        for (int s=0; s<segments.size();s++){
            Segment segment=(Segment) segments.get(s);
            //logger.finest(""+segment);
            if ( segment.getName().equals("HELIX")){
                // draw helix
                drawHelixSegment(segment, drawHeight,g, aminosize,fullwidth,y,chainlength, scale);
            } else if ( segment.getName().equals("STRAND")){
                g.setColor(Color.yellow);
                drawArrowSegment(segment, drawHeight,g, aminosize,fullwidth,y,chainlength, scale);
            
            } else {
                
                g.setColor(Color.gray);
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
                
                //if ( ! (featureSelected && ( f== selectedFeaturePos))){
                //col = segment.getColor();
                //g2D.setColor(col);
                //}
                
                int xstart =  java.lang.Math.round(start * scale) + DEFAULT_X_START;
                int width   = java.lang.Math.round(  end * scale) - xstart +  DEFAULT_X_START+aminosize ;
                
                int height = drawHeight ;
                
                // draw the line ...
                
                
                g2D.fillRect(xstart,y,width,height);
            }
            
        }
        
    }
    
    
    private void drawHelixSegment(Segment segment, int drawHeight,Graphics g, int aminosize,int fullwidth,int y,int chainlength, float scale){
        //logger.finest("drawHelixSegment");
        Graphics2D g2D =(Graphics2D) g;
        int start     = segment.getStart() -1 ;
        int end       = segment.getEnd()   -1 ;
        
        int xstart =  java.lang.Math.round(start * scale) + DEFAULT_X_START;
        int width   = java.lang.Math.round(  end * scale) - xstart +  DEFAULT_X_START+aminosize ;
  
        // Helix is  always red...
        g2D.setColor(Color.red);
        g2D.fillRect(xstart,y,width,drawHeight);
        /*
        // draw a sinus - double helix ...
        float ang = 0.0f;
        
        
        // do one helix / 4 amino acids ...
        float inc = (float)(360 /( 4)); 
        logger.finest("increase " + inc);
        double RAD = 3.1415926535 / 180.0 ;
        int oldy1 = y+DEFAULT_Y_HEIGHT;
        int oldy2 = y+DEFAULT_Y_HEIGHT;
  
        
        // iter over every pixel between xstart and width ...
        for ( int i =xstart ; i<= (xstart+width); i++ ){
            ang += inc;
            if ( ang > 360) ang = 0;
            float ypos = (float) (Math.cos(ang) ) + 1;
            //System.out.println("ypos helix " + ypos);
            float ypos2 = (float) (Math.sin(ang) +1);
            int currentY = y ;
            int currentY2 = y ;
            if ( ypos != 0) 
                currentY = y+ Math.round(DEFAULT_Y_HEIGHT / 2 * ypos) -1;
            if ( ypos2 != 0)
                currentY2 = y+ Math.round(DEFAULT_Y_HEIGHT / 2 * ypos2)-1;
            //System.out.println("i " + i + "inc " + inc + " ang " + ang + " ypos helix " + currentY );
            
            
            g2D.drawLine(i,oldy1,i,currentY);
            //g2D.setColor(Color.red);
            //g2D.drawLine(i,oldy2,i,currentY2);
            g2D.drawLine(i,currentY,i,currentY2);
            oldy1= currentY;
            oldy2 = currentY2;
            
            
        }
        */
       
     
    }    
    
    /** draw the frame of a rectangle .
     * 
     * 
     * @param feature
     * @param featurePos
     * @param drawHeight
     * @param g
     * @param aminosize
     * @param fullwidth
     * @param y
     * @param chainlength
     * @param scale
     */
    private void drawBoxFeature(Feature feature,int featurePos, int drawHeight,Graphics g, int aminosize,int fullwidth,int y,int chainlength, float scale) 
    {
        //logger.finest("draw Box Feature " + feature );
        
        Graphics2D g2D =(Graphics2D) g;
        List segments = feature.getSegments() ;
        //int f = featurePos;     
        
        //Segment seg0 = (Segment) segments.get(0) ;
        
        //Color col =  seg0.getColor();	
        //g2D.setColor(col);
        
        
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
            
            //if ( ! (featureSelected && ( f== selectedFeaturePos))){
            //    col = segment.getColor();
            //    g2D.setColor(col);
            //}
            
            int xstart =  java.lang.Math.round(start * scale) + DEFAULT_X_START;
            int width   = java.lang.Math.round(  end * scale) - xstart +  DEFAULT_X_START+aminosize ;
            
            g2D.drawRect(xstart,y,width,drawHeight);
            //g2D.drawLine(xstart,y,)
        }
        
    }
    
    private void drawArrowFeature(Feature feature,int featurePos, int drawHeight,Graphics g, int aminosize,int fullwidth,int y,int chainlength, float scale) 
    {
        //logger.finest("draw Box Feature " + feature );
        
        
        List segments = feature.getSegments() ;
        //int f = featurePos;     
        
        //Segment seg0 = (Segment) segments.get(0) ;
        
        //Color col =  seg0.getColor();	
        //g2D.setColor(col);
        
        
        for (int s=0; s<segments.size();s++){
            
            
            Segment segment=(Segment) segments.get(s);
            
            drawArrowSegment(segment,drawHeight, g,  aminosize, fullwidth, y, chainlength,  scale);
        }
    }
    
    private void drawArrowSegment(Segment segment, int drawHeight,Graphics g, int aminosize,int fullwidth,int y,int chainlength, float scale){
        //logger.finest("drawArrowsegment");
        Graphics2D g2D =(Graphics2D) g;
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
        
        
        //Color col = segment.getColor();
        //g2D.setColor(col);
        
        
        int xstart =  java.lang.Math.round(start * scale) + DEFAULT_X_START;
        int width   = java.lang.Math.round(  end * scale) - xstart +  DEFAULT_X_START+aminosize ;
        
        int half = drawHeight / 2 ;
        
        
        if ( width > 4) {
            g2D.fillRect(xstart,y+half-1,width-4,drawHeight-half+1);
            // draw arrow head
            int x1 = xstart + width -4  ; int y1 = y ;
            int x2 = xstart + width -4  ; int y2 = y + DEFAULT_Y_HEIGHT;
            int x3 = xstart + width     ; int y3 = y + half ; 
            int[] xPoints =  { x1,x2,x3};
            int[] yPoints =  { y1,y2,y3};
            g2D.fillPolygon(xPoints,yPoints, 3);
        }
        else
            g2D.fillRect(xstart,y+half,width,drawHeight-half);
        //g2D.drawLine(xstart,y,)
    }
    
    
    
    
    
    private void drawHelixFeature(Feature feature,int featurePos, int drawHeight,Graphics g, int aminosize,int fullwidth,int y,int chainlength, float scale){
        //logger.finest("draw Helix Feature " + feature );
        //Graphics2D g2D =(Graphics2D) g;
        List segments = feature.getSegments() ;
        
        
        
        for (int s=0; s<segments.size();s++){
            Segment segment=(Segment) segments.get(s);
            
            drawHelixSegment(segment, drawHeight, g,  aminosize, fullwidth, y, chainlength,  scale);
            
        }
        
    }
    
    private void drawSpanFeature(Feature feature,int featurePos, int drawHeight,Graphics g, int aminosize,int fullwidth,int y,int chainlength, float scale){
        //logger.finest("draw Span Feature " + feature.getName() );
        Graphics2D g2D =(Graphics2D) g;
        List segments = feature.getSegments() ;
        
        //if ( feature.getType().equals("DISULFID")){
        //    g2D.setColor(Color.yellow);
        //}
        
        for (int s=0; s<segments.size();s++){
            Segment segment=(Segment) segments.get(s);
            
            int start     = segment.getStart() -1 ;
            int end       = segment.getEnd()   -1 ;
            
            int xstart =  java.lang.Math.round(start * scale) + DEFAULT_X_START;
            int width   = java.lang.Math.round(  end * scale) - xstart +  DEFAULT_X_START+aminosize ;
            
            int height = drawHeight;
            g2D.fillRect(xstart,y,aminosize,height);
            g2D.fillRect(xstart,y+(height/2),width,1);
            g2D.fillRect(xstart+width-aminosize,y,aminosize,height);
            
        }
        
    }
    
    /** draw the selected region 
    private void drawSelection(Graphics2D g2D, int aminosize, float scale){
        
        //System.out.println("draw selection " + selectStart + " end" + selectEnd);
        if ( selectStart > -1 ) {
            //Dimension dstruc=this.getSize();
            Composite oldComp = g2D.getComposite();
            g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));        
            
            g2D.setColor(SELECTION_COLOR);
            int seqx = java.lang.Math.round(selectStart*scale)+DEFAULT_X_START ;
            
            int selectEndX = java.lang.Math.round(selectEnd * scale)-seqx + DEFAULT_X_START +aminosize; 
            if ( selectEndX < aminosize) 
                selectEndX = aminosize ;
            
            if ( selectEndX  < 1 )
                selectEndX = 1 ;
            
            Rectangle selection = new Rectangle(seqx , 0, selectEndX, parent.getHeight());
            g2D.fill(selection);
            g2D.setComposite(oldComp);
            
        }
    }*/
    
    
    
    
    /** a feature has been selected */
    public void selectedFeature(Feature feat){
        //System.out.println("selected feature " + feat);
        
        if ( feat == null) { 
            featureSelected = false;
            selectedFeaturePos = -1;
            //this.repaint();
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
        //this.repaint();
        //oldSelectedFeaturePos = selectedFeaturePos;
    }
    
    public void setScale(float scale){
        //System.out.println("FeaturePanel setScale " + scale);
        this.scale=scale;
        //initImgBuffer();
        //this.revalidate();
    }
    
    
    
    public float getScale(){
        
        return scale;
        
    }
    
    
    
    /* check if the mouse is over a feature and if it is 
     * return the feature number 
     * @author andreas
     *
     * To change the template for this generated type comment go to
     * Window - Preferences - Java - Code Generation - Code and Comments
     */
    public int getLineNr(int eventY){
        //int mouseY = e.getY();
        int mouseY = eventY;
        
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
        
        //      this.repaint();
        
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
        
        //this.repaint();
        
    }
    
    
}
