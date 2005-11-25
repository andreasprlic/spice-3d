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
 * Created on Nov 18, 2005
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


import javax.swing.JPanel;
import javax.swing.JProgressBar;

import java.awt.*;

import org.biojava.bio.structure.Chain;
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.Segment;
import org.biojava.spice.das.SpiceDasSource;
import org.biojava.spice.manypanel.drawable.*;
import org.biojava.spice.manypanel.eventmodel.*;

/** a class that draws the content of a das source
 * 
 * @author Andreas Prlic
 *
 */
public class DasSourcePanel 
extends JPanel
implements FeatureListener
{
    static final long serialVersionUID = 17439836750348543l;
    
    public static final Color SELECTED_FEATURE_COLOR  = Color.yellow;
    
    DrawableDasSource dasSource;
    
    float scale;
    static Logger logger = Logger.getLogger("org.biojava.spice");
    JProgressBar bar;
    
    boolean selected;
    boolean featureSelected;
    int selectedFeaturePos;
    
    int chainLength;
    
    public DasSourcePanel(DrawableDasSource ds) {
        super();
        dasSource = ds;
        scale = 1.0f;
        setOpaque(true);
        bar = new JProgressBar();
        bar.setIndeterminate(false);
        bar.setPreferredSize(new Dimension(100,10));
        bar.setVisible(true);
        bar.setLocation(1,10);
        bar.setBounds(0,0,100,10);
        //add(bar);
        this.setBackground(Color.white);
        featureSelected = false;
        selectedFeaturePos = -1;
        selected = false;
        chainLength = 0;
    }
    
    public void setChain(Chain chain){
        chainLength = chain.getLength();
    }
    
    public void setScale(float scale) {
        
        this.scale=scale;
        this.repaint();
    }
    
    public int getDisplayHeight(){
        int h = FeaturePanel.DEFAULT_Y_START + 10 + FeaturePanel.LINE_HEIGHT;
        h += (dasSource.getFeatures().length +1 ) * FeaturePanel.DEFAULT_Y_STEP ;
        //logger.info(dasSource.getDasSource().getNickname() + " height:" + h);
        return h;
    }
    
    
    public void setLoading(boolean flag){
        dasSource.setLoading(flag);
    }
    
    public void paint(Graphics g){
        super.paint(g);
        
        if (dasSource.getLoading()){
            logger.info(" draw loading bar");
            // add a progressbar           
            bar.setIndeterminate(true);
            //add
            bar.paint(g);
            
        } else {
            bar.setIndeterminate(false);
        }
        
        g.setColor(Color.black);
        
        String str = dasSource.getDasSource().getNickname();
        //logger.info("paint DasSourcePanel "+str);
        
        g.drawString(str,10,10);
        //g.fillRect(10,10,20,20);
        Feature[] features = dasSource.getFeatures();
        
        Graphics2D g2D = (Graphics2D)g;
        
        int y = FeaturePanel.DEFAULT_Y_START + 10 ;
        //logger.info(dasSource.getDasSource().getNickname() + " " + dasSource.getLoading());
        
        //Composite oldComp = g2D.getComposite();
        //g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));        
        //int aminosize = Math.round(1*scale);
        
        drawFeatures(g2D,features,y);
        
        // TODO: add listener for DasSource selected
        if ( selected ){
            // the whole featureview has been selected
            //if selected draw a rectangle over everything
            
            //g2D.setColor(SELECTION_COLOR);
            //g2D.fillRect(0,y,width,parent.getHeight());
            
        }
        
        /*
        for ( int i = 0 ; i < features.length; i++){
            
            Feature f = features[i];
            g.drawString(f.getType(),10,y+10);
            //logger.info(f.toString());
            List segs = f.getSegments();
            Iterator iter = segs.iterator();
            while ( iter.hasNext()){
                Segment s = (Segment)iter.next();
                g.setColor(s.getColor());
                int pstart = s.getStart()-1;
                int pend   = s.getEnd()-1;
                int startX = Math.round(pstart*scale) + FeaturePanel.DEFAULT_X_START;
                int segl = pend - pstart +1;
                int endX   = Math.round(segl*scale);
                //logger.info(s.getName()+ " "+ pstart+" ("+startX + ") " 
                //+ pend + " (" +endX+") y:" + y );
                Rectangle feature = new Rectangle(startX,y,endX,FeaturePanel.LINE_HEIGHT);
                g2D.fill(feature);
                //g2D.fillRect(startX,y,endX,FeaturePanel.LINE_HEIGHT);
            }
            
            y+= FeaturePanel.DEFAULT_Y_STEP;    
        }*/
        
        //g2D.setComposite(oldComp);
        
    }
    
    /** draw the features starting at position y
     * returns the y coordinate of the last feature ;
     * */
    private int drawFeatures(Graphics g, Feature[] features,  int y){
        
        
        //System.out.println("FeaturePanel drwaFeatures aminosize "+ aminosize + " y " + y);
        //logger.info("drawFeatures " + features);
        //boolean secstruc = false ;
        
        if ( features == null) 
            return y;
        
        SpiceDasSource ds = dasSource.getDasSource();
        Map[] styleSheetMap = ds.getStylesheet() ;
        if (( styleSheetMap == null) || (styleSheetMap.length == 0) ) {
            //logger.info(parent.getDasSource().getNickname() + " did not provide stylesheet");
            y = paintNoStylesheetFeatures(g,features,y);
        }
        else {
            //logger.info(parent.getDasSource().getNickname() + "painting with stylesheet");
            y = paintStylesheetFeatures(styleSheetMap,g,features,y);
        }
        return y;
    }
    
    public void featureSelected(FeatureEvent e) {
        // TODO add feature selection
        
    }
    
    
    
    public void newFeatures(FeatureEvent e) {
        //logger.info(" dassourcepanel: drawable das source got new features, repaint!");
        int panelWidth = getWidth();
        int panelHeight = getDisplayHeight();
        Dimension d = new Dimension(panelWidth,panelHeight);
       
        this.setPreferredSize(d);
        this.setSize(d);
        this.repaint();
        //this.updateUI();
        //this.revalidate();
    }
    
    
    
    private void drawArrowFeature(Feature feature,int featurePos, int drawHeight,Graphics g,int y) 
    {
        //logger.finest("draw Box Feature " + feature );
        
        
        List segments = feature.getSegments() ;
        //int f = featurePos;     
        
        //Segment seg0 = (Segment) segments.get(0) ;
        
        //Color col =  seg0.getColor(); 
        //g2D.setColor(col);
        
        
        for (int s=0; s<segments.size();s++){
            
            
            Segment segment=(Segment) segments.get(s);
            
            drawArrowSegment(segment,drawHeight, g,  y);
        }
    }
    
    private void drawArrowSegment(Segment segment, int drawHeight,Graphics g, int y){
        //logger.finest("drawArrowsegment");
        Graphics2D g2D =(Graphics2D) g;
        int start     = segment.getStart() -1 ;
        int end       = segment.getEnd()   -1 ;
        int aminosize = Math.round(1*scale);
        // hum some people say this if annotation relates to whole seq.
        if (( start == -1) && ( end == -1 )){
            //System.out.println(feature);
            start = 0;
            end = chainLength - 1;
            segment.setStart(1);
            segment.setEnd(chainLength);
        }
        
        
        //Color col = segment.getColor();
        //g2D.setColor(col);
        
        
        int xstart =  java.lang.Math.round(start * scale) + FeaturePanel.DEFAULT_X_START;
        int width   = java.lang.Math.round(  end * scale) - xstart +  FeaturePanel.DEFAULT_X_START+aminosize ;
        
        int half = drawHeight / 2 ;
        
        
        if ( width > 4) {
            g2D.fillRect(xstart,y+half-1,width-4,drawHeight-half+1);
            // draw arrow head
            int x1 = xstart + width -4  ; int y1 = y ;
            int x2 = xstart + width -4  ; int y2 = y + FeaturePanel.DEFAULT_Y_HEIGHT;
            int x3 = xstart + width     ; int y3 = y + half ; 
            int[] xPoints =  { x1,x2,x3};
            int[] yPoints =  { y1,y2,y3};
            g2D.fillPolygon(xPoints,yPoints, 3);
        }
        else
            g2D.fillRect(xstart,y+half,width,drawHeight-half);
        //g2D.drawLine(xstart,y,)
    }
    
    
    
    
    
    private void drawHelixFeature(Feature feature,int featurePos, int drawHeight,Graphics g, int y){
        //logger.finest("draw Helix Feature " + feature );
        //Graphics2D g2D =(Graphics2D) g;
        List segments = feature.getSegments() ;
        
        
        
        for (int s=0; s<segments.size();s++){
            Segment segment=(Segment) segments.get(s);
            
            drawHelixSegment(segment, drawHeight, g,  y);
            
        }
        
    }
    
    private void drawSpanFeature(Feature feature,int featurePos, int drawHeight,Graphics g,int y){
        //logger.finest("draw Span Feature " + feature.getName() );
        Graphics2D g2D =(Graphics2D) g;
        List segments = feature.getSegments() ;
        
        //if ( feature.getType().equals("DISULFID")){
        //    g2D.setColor(Color.yellow);
        //}
        int aminosize = Math.round(1*scale);
        for (int s=0; s<segments.size();s++){
            Segment segment=(Segment) segments.get(s);
            
            int start     = segment.getStart() -1 ;
            int end       = segment.getEnd()   -1 ;
            
            int xstart =  java.lang.Math.round(start * scale) + FeaturePanel.DEFAULT_X_START;
            int width   = java.lang.Math.round(  end * scale) - xstart +  FeaturePanel.DEFAULT_X_START+aminosize ;
            
            int height = drawHeight;
            g2D.fillRect(xstart,y,aminosize,height);
            g2D.fillRect(xstart,y+(height/2),width,1);
            g2D.fillRect(xstart+width-aminosize,y,aminosize,height);
            
        }
        
    }
    
    
    private void drawHelixSegment(Segment segment, int drawHeight,Graphics g, int y){
        //logger.finest("drawHelixSegment");
        Graphics2D g2D =(Graphics2D) g;
        int start     = segment.getStart() -1 ;
        int end       = segment.getEnd()   -1 ;
        int aminosize = Math.round(1*scale);
        int xstart =  java.lang.Math.round(start * scale) + FeaturePanel.DEFAULT_X_START;
        int width   = java.lang.Math.round(  end * scale) - xstart +  FeaturePanel.DEFAULT_X_START+aminosize ;
  
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
    private void drawBoxFeature(Feature feature,int featurePos, int drawHeight,Graphics g, int y) 
    {
        //logger.finest("draw Box Feature " + feature );
        
        Graphics2D g2D =(Graphics2D) g;
        List segments = feature.getSegments() ;
        //int f = featurePos;     
        int aminosize = Math.round(1*scale);
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
                end = chainLength - 1;
                segment.setStart(1);
                segment.setEnd(chainLength);
            }
            
            //if ( ! (featureSelected && ( f== selectedFeaturePos))){
            //    col = segment.getColor();
            //    g2D.setColor(col);
            //}
            
            int xstart =  java.lang.Math.round(start * scale) + FeaturePanel.DEFAULT_X_START;
            int width   = java.lang.Math.round(  end * scale) - xstart +  FeaturePanel.DEFAULT_X_START+aminosize ;
            
            g2D.drawRect(xstart,y,width,drawHeight);
            //g2D.drawLine(xstart,y,)
        }
        
    }
    
    
    private void drawSecstrucFeature(Feature feature,int featurePos, int drawHeight,Graphics g, int y) 
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
        int aminosize = Math.round(1*scale);
        for (int s=0; s<segments.size();s++){
            Segment segment=(Segment) segments.get(s);
            //logger.finest(""+segment);
            if ( segment.getName().equals("HELIX")){
                // draw helix
                drawHelixSegment(segment, drawHeight,g,y);
            } else if ( segment.getName().equals("STRAND")){
                g.setColor(Color.yellow);
                drawArrowSegment(segment, drawHeight,g, y);
            
            } else {
                
                g.setColor(Color.gray);
                int start     = segment.getStart() -1 ;
                int end       = segment.getEnd()   -1 ;
                
                // hum some people say this if annotation relates to whole seq.
                if (( start == -1) && ( end == -1 )){
                    //System.out.println(feature);
                    start = 0;
                    end = chainLength - 1;
                    segment.setStart(1);
                    segment.setEnd(chainLength);
                }
                
                //if ( ! (featureSelected && ( f== selectedFeaturePos))){
                //col = segment.getColor();
                //g2D.setColor(col);
                //}
                
                int xstart =  java.lang.Math.round(start * scale) + FeaturePanel.DEFAULT_X_START;
                int width   = java.lang.Math.round(  end * scale) - xstart +  FeaturePanel.DEFAULT_X_START+aminosize ;
                
                int height = drawHeight ;
                
                // draw the line ...
                
                
                g2D.fillRect(xstart,y,width,height);
            }
            
        }
        
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
    
    private void checkDrawSelectedFeature(Feature feature,int featurePos, int drawHeight,Graphics g,int y){
        Graphics2D g2D =(Graphics2D) g;
        int f = featurePos;
        // TODO color check for selection of features
        if ( featureSelected){
            if (f == selectedFeaturePos) {
                int fullwidth = Math.round(scale*chainLength);
                //Dimension dstruc = this.getSize();
                g2D.setColor(SELECTED_FEATURE_COLOR);
                Composite oldComp = g2D.getComposite();
                g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN ,0.6f));
                g2D.fillRect(0,y,fullwidth,drawHeight);
                g2D.setComposite(oldComp);
            }
        }
    }
    
private int paintNoStylesheetFeatures(Graphics g, Feature[] features,int y) {
        
        
        for ( int f =0 ; f< features.length;f++) {
            
            y += FeaturePanel.DEFAULT_Y_STEP;
            Feature feature = features[f];
            
            checkDrawSelectedFeature(feature,f,FeaturePanel.DEFAULT_Y_HEIGHT,g,y);
            setColor(g,feature,new HashMap());
            String featureType = feature.getType();
            
            if (  featureType.equals("DISULFID")){
                drawSpanFeature(feature,f,FeaturePanel.DEFAULT_Y_HEIGHT,g,y);
                
            } else if (  featureType.equals("SECSTRUC") || 
                    featureType.equals("HELIX") || 
                    featureType.equals("STRAND") || 
                    featureType.equals("COIL") ||
                    featureType.equals("TURN")
            ){
                drawSecstrucFeature(feature,f,FeaturePanel.DEFAULT_Y_HEIGHT,g,y);
            } else { 
                drawLineFeature(feature,f,FeaturePanel.DEFAULT_Y_HEIGHT,g,y);
            }
            //drawLineFeature(feature,f,DEFAULT_Y_HEIGHT,g,aminosize,fullwidth,y,chainlength,scale);
        }
        return y;
        
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
private void drawLineFeature(Feature feature,int featurePos, int drawHeight,Graphics g,int y) 
{
    // logger.finest("draw Line Feature " + feature );
    Graphics2D g2D =(Graphics2D) g;
    List segments = feature.getSegments() ;
    //int f = featurePos;     
    int aminosize = Math.round(1*scale);
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
            end = chainLength - 1;
            segment.setStart(1);
            segment.setEnd(chainLength);
        }
        
        //if ( ! (featureSelected && ( f== selectedFeaturePos))){
        //    col = segment.getColor();
        //    g2D.setColor(col);
        //}
        
        int xstart =  java.lang.Math.round(start * scale) + FeaturePanel.DEFAULT_X_START;
        int width   = java.lang.Math.round(  end * scale) - xstart +  FeaturePanel.DEFAULT_X_START+aminosize ;
        
        int height = drawHeight ;
        
        // draw the line ...
        g2D.fillRect(xstart,y,width,height);
        
    }
}

private void drawTriangleFeature(Feature feature,int featurePos, int drawHeight,Graphics g, int y) 
{
    // logger.finest("draw Triangle Feature " + feature );
    Graphics2D g2D =(Graphics2D) g;
    List segments = feature.getSegments() ;
    //int f = featurePos;     
    int aminosize = Math.round(1*scale);
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
            end = chainLength - 1;
            segment.setStart(1);
            segment.setEnd(chainLength);
        }
        
        //if ( ! (featureSelected && ( f== selectedFeaturePos))){
        //    col = segment.getColor();
        //    g2D.setColor(col);
        //}
        
        int xstart =  java.lang.Math.round(start * scale) + FeaturePanel.DEFAULT_X_START;
        int width   = java.lang.Math.round(  end * scale) - xstart +  FeaturePanel.DEFAULT_X_START+aminosize ;
        
        //int height = drawHeight ;
        
        // draw the line ...
        //g2D.fillRect(xstart,y,width,height);
        int middlex = xstart + (width/2);
        g2D.drawLine(xstart,(y+FeaturePanel.DEFAULT_Y_HEIGHT),middlex,y);
        g2D.drawLine(middlex,y,xstart+width,(y+FeaturePanel.DEFAULT_Y_HEIGHT));
        g2D.drawLine(xstart,(y+FeaturePanel.DEFAULT_Y_HEIGHT),xstart+width,(y+FeaturePanel.DEFAULT_Y_HEIGHT));
        
        
        
    }
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
    
 private int getDrawHeight(Map styleMap){
        
        String height = (String)styleMap.get("height");
        int h = FeaturePanel.DEFAULT_Y_HEIGHT;
        if ( height != null){
            try {
                h = Integer.parseInt(height);
            } catch (Exception e){}
        }
        
        if (h > FeaturePanel.DEFAULT_Y_HEIGHT){
            h = FeaturePanel.DEFAULT_Y_HEIGHT;
        }
        return h;
    }
    
    private int paintStylesheetFeatures(Map[] style,Graphics g, Feature[] features,int y) {
        //logger.info("paintSylesheetFeatures " );
        //Graphics2D g2D =(Graphics2D) g;
        
        for ( int f =0 ; f< features.length;f++) {
            
            y += FeaturePanel.DEFAULT_Y_STEP;
            
            Feature  feat = features[f];
            String featureType = feat.getType();
            
            checkDrawSelectedFeature(feat,f,FeaturePanel.DEFAULT_Y_HEIGHT,g,y);
            
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
                        
                        drawLineFeature(feat,f,h,g,y);
                    }
                    else if ( featStyle.equals("box")) {
                        //logger.finest("drawing box + style ");
                        matchingStyle = true ;
                        drawBoxFeature(feat,f,h,g,y);
                    }
                    else if ( featStyle.equals("span")) {
                        //logger.finest("drawing span + style ");
                        matchingStyle = true ;
                        drawSpanFeature(feat,f,h,g,y);
                    }
                    else if ( featStyle.equals("triangle")) {
                        // logger.finest("drawing triangle + style ");
                        matchingStyle = true ;
                        drawTriangleFeature(feat,f,h,g,y);
                    }
                    else if ( featStyle.equals("helix")) {
                        // logger.finest("drawing triangle + style ");
                        matchingStyle = true ;
                        drawHelixFeature(feat,f,h,g,y);
                    }
                    else if ( featStyle.equals("arrow")) {
                        // logger.finest("drawing triangle + style ");
                        matchingStyle = true ;
                        drawArrowFeature(feat,f,h,g,y);
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
                    drawSpanFeature(feat,f,FeaturePanel.DEFAULT_Y_HEIGHT,g,y);
                } else if (  featureType.equals("SECSTRUC") || 
                        featureType.equals("HELIX") || 
                        featureType.equals("STRAND") || 
                        featureType.equals("COIL") ||
                        featureType.equals("TURN")
                ){
                    drawSecstrucFeature(feat,f,FeaturePanel.DEFAULT_Y_HEIGHT,g,y);
                } else { 
                    drawLineFeature(feat,f,FeaturePanel.DEFAULT_Y_HEIGHT,g,y);
                }
            }
        }
        
        return y;
    }
    
    
    
}
