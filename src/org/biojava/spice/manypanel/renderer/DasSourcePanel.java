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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import java.awt.*;

import org.biojava.bio.structure.Chain;
import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.eventmodel.*;
import org.biojava.spice.ResourceManager;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.config.SpiceDefaults;
import org.biojava.spice.feature.Feature;
import org.biojava.spice.feature.Segment;
import org.biojava.spice.manypanel.eventmodel.FeatureListener;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureEvent;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureListener;
import org.biojava.spice.manypanel.drawable.*;

/** a class that draws the content of a das source
 * 
 * @author Andreas Prlic
 *
 */
public class DasSourcePanel 
extends JPanel
implements FeatureListener,SpiceFeatureListener
{
    static final long serialVersionUID = 17439836750348543l;
    
    static Logger logger = Logger.getLogger(SpiceDefaults.LOGGER);
    
    public static final Font  plainFont;
    public static final Font  headFont; 
    public static final Color SELECTED_FEATURE_COLOR;
    
    
    float     scale;
    boolean   selected;
    boolean   featureSelected;
    int       selectedFeaturePos;
    ImageIcon linkIcon ;
    ImageIcon infoIcon;
    int       chainLength;    
   
    
    ProgressThread    progressThread;
    DrawableDasSource drawableDasSource;
    CoordManager coordManager;
    
    static {
     
        
        String fontName = ResourceManager.getString("org.biojava.spice.manypanel.renderer.DasSourcePanel.FontName");        
        String fn       = ResourceManager.getString("org.biojava.spice.manypanel.renderer.DasSourcePanel.FontSize");
        int fsize       = Integer.parseInt(fn);
        plainFont       = new Font(fontName, Font.PLAIN, fsize);
        
        String headFontName = ResourceManager.getString("org.biojava.spice.manypanel.renderer.DasSourcePanel.HeadFontName");
        String fh           = ResourceManager.getString("org.biojava.spice.manypanel.renderer.DasSourcePanel.HeadFontSize");        
        int hsize           = Integer.parseInt(fh);
        headFont            = new Font(headFontName, Font.BOLD,hsize);
        
        String selColor = ResourceManager.getString("org.biojava.spice.manypanel.renderer.DasSourcePanel.SelectedFeatureColor");
        SELECTED_FEATURE_COLOR = Color.decode(selColor);
    }
    
    public DasSourcePanel(DrawableDasSource ds) {
        super();
        drawableDasSource = ds;
        scale = 1.0f;
        setOpaque(true);
        
        //add(bar);
        this.setBackground(Color.white);
        featureSelected = false;
        selectedFeaturePos = -1;
        selected = false;
        chainLength = 0;
        linkIcon = SpiceApplication.createImageIcon("firefox10x10.png");
        infoIcon = SpiceApplication.createImageIcon("messagebox_info16x16.png");
        coordManager = new CoordManager();
    }
    
    
    /** returns a DrawableDasSource - i.e. a source that can contain features
     * 
     * @return a DrawableDaasSource object
     */
    public DrawableDasSource getDrawableDasSource(){
        return drawableDasSource;
    }
    
    public void setDrawableDasSource(DrawableDasSource ds){
        drawableDasSource = ds;
    }
    
    public void setChain(Chain chain){
  
            chainLength = chain.getLengthAminos();
            coordManager.setLength(chainLength);
        
    }
    
    public void setScale(float scale) {
        
        this.scale=scale;
        coordManager.setScale(scale);
        this.repaint();
    }
    
    public int getDisplayHeight(){
        int h = SequenceScalePanel.DEFAULT_Y_START + SequenceScalePanel.DEFAULT_Y_STEP + SequenceScalePanel.LINE_HEIGHT;
        h += (drawableDasSource.getFeatures().length +1 ) * SequenceScalePanel.DEFAULT_Y_STEP ;
        //logger.info(dasSource.getDasSource().getNickname() + " height:" + h);
        return h;
    }
    
    
    public void setLoading(boolean flag){
        drawableDasSource.setLoading(flag);
    }
    
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        //System.out.println("painting das source " +dasSource.getDasSource().getNickname());
        
        // now to the features ...
        
        Graphics2D g2D = (Graphics2D)g;
        
        Feature[] features = drawableDasSource.getFeatures();
                
        int y = SequenceScalePanel.DEFAULT_Y_START + SequenceScalePanel.DEFAULT_Y_STEP ;      
        
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Composite oldComp = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));  
        
        drawFeatures(g2D,features,y);
        g2D.setComposite(oldComp);
        
        if ( selected ){
            // the whole featureview has been selected
            //if selected draw a rectangle over everything
            
            g2D.setColor(SELECTED_FEATURE_COLOR);
            g2D.fillRect(0,y,getWidth(),getHeight());
            
        }
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
        
        SpiceDasSource ds = drawableDasSource.getDasSource();
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
    
    
    
    
    
    public void newFeatures(FeatureEvent e) {

        logger.finest(" dassourcepanel:  das source >" + drawableDasSource.getDasSource().getNickname()+"< got new ("+ drawableDasSource.getFeatures().length + ") features, repaint!");
        int panelWidth = getWidth();
        int panelHeight = getDisplayHeight();
        Dimension d = new Dimension(panelWidth,panelHeight);
        
        this.setPreferredSize(d);
        this.setSize(d);
        this.repaint();
        //this.updateUI();
        //this.revalidate();
    }
    
    
    public void comeBackLater(FeatureEvent e){
        // TODO do something here...
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
        if ( aminosize < 1)
            aminosize = 1;
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
        
        
        int xstart =  coordManager.getPanelPos(start);
        //start * aminosize + FeaturePanel.DEFAULT_X_START;
        int width   = coordManager.getPanelPos(end) -xstart + aminosize;
        //* aminosize - xstart +  FeaturePanel.DEFAULT_X_START+aminosize ;
        
        int half = drawHeight / 2 ;
        
        
        if ( width > 4) {
            g2D.fillRect(xstart,y+half-2,width-4,drawHeight-half);
            // draw arrow head
            int x1 = xstart + width -4  ; int y1 = y ;
            int x2 = xstart + width -4  ; int y2 = y + SequenceScalePanel.DEFAULT_Y_HEIGHT;
            int x3 = xstart + width     ; int y3 = y + half ; 
            int[] xPoints =  { x1,x2,x3};
            int[] yPoints =  { y1,y2,y3};
            g2D.fillPolygon(xPoints,yPoints, 3);
        }
        else
            g2D.fillRect(xstart,y,width,drawHeight);
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
        //    g2D.setColor(Color.yelldrawBoxow);
        //}
        int aminosize = Math.round(1*scale);
        if ( aminosize < 1 )
            aminosize = 1;
        for (int s=0; s<segments.size();s++){
            Segment segment=(Segment) segments.get(s);
            
            int start     = segment.getStart() -1 ;
            int end       = segment.getEnd()   -1 ;
            
            int xstart = coordManager.getPanelPos(start); 
                //
                //start * aminosize + FeaturePanel.DEFAULT_X_START;
            int width   = coordManager.getPanelPos(end) - xstart + aminosize;
                //end * aminosize - xstart +  FeaturePanel.DEFAULT_X_START+aminosize ;
            
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
        if ( aminosize < 1 )
            aminosize = 1;
        int xstart = coordManager.getPanelPos(start);  
            //start * aminosize + FeaturePanel.DEFAULT_X_START;
        int width   = coordManager.getPanelPos(end) - xstart + aminosize;
            //end * aminosize - xstart +  FeaturePanel.DEFAULT_X_START+aminosize ;
        
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
        if ( aminosize < 1)
            aminosize = 1;
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
            
            int xstart = coordManager.getPanelPos(start);  
                //start * aminosize + FeaturePanel.DEFAULT_X_START;
            int width   = coordManager.getPanelPos(end) - xstart + aminosize; 
                //end * aminosize - xstart +  FeaturePanel.DEFAULT_X_START+aminosize ;
            
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
        if ( aminosize < 1)
            aminosize = 1;
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
                
                int xstart = coordManager.getPanelPos(start); 
                    // start * aminosize + FeaturePanel.DEFAULT_X_START;
                int width   = coordManager.getPanelPos(end) - xstart + aminosize; 
                    // end * aminosize - xstart +  FeaturePanel.DEFAULT_X_START+aminosize ;
                
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
    
    private void checkDrawSelectedFeature(Feature feature,int featurePos, Graphics g,int y){
        Graphics2D g2D =(Graphics2D) g;
        int f = featurePos;
        
        if ( featureSelected){
            //logger.info("feature selected " + selectedFeaturePos);
            if (f == selectedFeaturePos) {
                int fullwidth = Math.round(scale*chainLength);
               
                g2D.setColor(SELECTED_FEATURE_COLOR);
                Composite oldComp = g2D.getComposite();
                int drawHeight = SequenceScalePanel.DEFAULT_Y_STEP;
                
                g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER ,0.6f));
                g2D.fillRect(0,y,fullwidth+SequenceScalePanel.DEFAULT_X_START,drawHeight);
                g2D.setComposite(oldComp);
            }
        }
    }
    
    private int paintNoStylesheetFeatures(Graphics g, Feature[] features,int y) {
        
        
        for ( int f =0 ; f< features.length;f++) {
            
            y += SequenceScalePanel.DEFAULT_Y_STEP;
            Feature feature = features[f];
             
            setColor(g,feature,new HashMap());
            String featureType = feature.getType();
            
            if (  featureType.equals(SpiceDefaults.DISULFID_TYPE)){
                drawSpanFeature(feature,f,SequenceScalePanel.DEFAULT_Y_HEIGHT,g,y);
                
            } else if (  featureType.equals("SECSTRUC") || 
                    featureType.equals("HELIX") || 
                    featureType.equals("STRAND") || 
                    featureType.equals("COIL") ||
                    featureType.equals("TURN")
            ){
                drawSecstrucFeature(feature,f,SequenceScalePanel.DEFAULT_Y_HEIGHT,g,y);
            } else { 
                drawLineFeature(feature,f,SequenceScalePanel.DEFAULT_Y_HEIGHT,g,y);
            }
            
            checkDrawSelectedFeature(feature,f,g,y);
            
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
        if ( aminosize < 1)
            aminosize = 1;
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
            
            int xstart =  coordManager.getPanelPos(start);
            //start * aminosize + FeaturePanel.DEFAULT_X_START;
            int width   = coordManager.getPanelPos(end) - xstart +aminosize;
            // end * aminosize - xstart +  FeaturePanel.DEFAULT_X_START+aminosize ;
            
            int height = drawHeight ;
            
            // draw the line ...
            g2D.fillRect(xstart,y,width,height);
            Color c = g2D.getColor();
            g2D.setColor(Color.black);
            g2D.drawRect(xstart,y,width,height);
            g2D.setColor(c);
            
        }
    }
    
    private void drawTriangleFeature(Feature feature,int featurePos, int drawHeight,Graphics g, int y) 
    {
        // logger.finest("draw Triangle Feature " + feature );
        Graphics2D g2D =(Graphics2D) g;
        List segments = feature.getSegments() ;
        //int f = featurePos;     
        int aminosize = Math.round(1*scale);
        if ( aminosize < 1 )
            aminosize = 1;
        
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
            
            int xstart = coordManager.getPanelPos(start);
            // start * aminosize + FeaturePanel.DEFAULT_X_START;
            int width   = coordManager.getPanelPos(end)-xstart + aminosize;
            //end * aminosize - xstart +  FeaturePanel.DEFAULT_X_START+aminosize ;
            
            //int height = drawHeight ;
            
            // draw the line ...
            //g2D.fillRect(xstart,y,width,height);
            int middlex = xstart + (width/2);
            g2D.drawLine(xstart,(y+SequenceScalePanel.DEFAULT_Y_HEIGHT),middlex,y);
            g2D.drawLine(middlex,y,xstart+width,(y+SequenceScalePanel.DEFAULT_Y_HEIGHT));
            g2D.drawLine(xstart,(y+SequenceScalePanel.DEFAULT_Y_HEIGHT),xstart+width,(y+SequenceScalePanel.DEFAULT_Y_HEIGHT));
            
            
            
        }
    }
    
    
    
    
    /** set the color to be used for painting 
     * 
     * @param feature
     * @param style
     */
    private void setColor(Graphics g, Feature feature, Map style){
        Color c = (Color) style.get("color");
        if ( c != null) {
            
            //logger.info("using stylesheet defined color " + c);
            g.setColor(c);
        } else {
            //logger.info("no stylesheet defined color found for" + feature.getName());
            if ( feature.getType().equals(SpiceDefaults.DISULFID_TYPE)){
                g.setColor(Color.yellow);
            } else {
                setDefaultColor(g,feature);
            }
        }
    }
    
    private void setDefaultColor(Graphics g, Feature feature){
        List segments = feature.getSegments();
        Color col = Color.BLUE;
        if ( segments.size() > 0) {
            Segment seg0 = (Segment) segments.get(0) ;
        
            col =  seg0.getColor();
        }
        g.setColor(col);
    }
    
    private int getDrawHeight(Map styleMap){
        
        String height = (String)styleMap.get("height");
        int h = SequenceScalePanel.DEFAULT_Y_HEIGHT;
        if ( height != null){
            try {
                h = Integer.parseInt(height);
            } catch (Exception e){}
        }
        
        if (h > SequenceScalePanel.DEFAULT_Y_HEIGHT){
            h = SequenceScalePanel.DEFAULT_Y_HEIGHT;
        }
        return h;
    }
    
    private int paintStylesheetFeatures(Map[] style,Graphics g, Feature[] features,int y) {
        //logger.info("paintSylesheetFeatures " );
        //Graphics2D g2D =(Graphics2D) g;
        
        
        for ( int f =0 ; f< features.length;f++) {
            
            y += SequenceScalePanel.DEFAULT_Y_STEP;
            
            Feature  feat = features[f];
            String featureType = feat.getType();
            
            // disulfid bridges are never overwritten...            
            if (  featureType.equals(SpiceDefaults.DISULFID_TYPE)){ 
                drawSpanFeature(feat,f,SequenceScalePanel.DEFAULT_Y_HEIGHT,g,y);
                continue;
            }
                    
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
                if (  featureType.equals(SpiceDefaults.DISULFID_TYPE)){
                    drawSpanFeature(feat,f,SequenceScalePanel.DEFAULT_Y_HEIGHT,g,y);
                } else if (  featureType.equals("SECSTRUC") || 
                        featureType.equals("HELIX") || 
                        featureType.equals("STRAND") || 
                        featureType.equals("COIL") ||
                        featureType.equals("TURN")
                ){
                    drawSecstrucFeature(feat,f,SequenceScalePanel.DEFAULT_Y_HEIGHT,g,y);
                } else { 
                    drawLineFeature(feat,f,SequenceScalePanel.DEFAULT_Y_HEIGHT,g,y);
                }
            }
            checkDrawSelectedFeature(feat,f,g,y);            
        }

        
        return y;
    }

    
    public void featureSelected(SpiceFeatureEvent e) {
        
        
        Feature f = e.getFeature();
        
        //logger.info("feature selected " + f);
        
        if ( f == null) {
            if ( featureSelected){
                featureSelected = false;
                selectedFeaturePos = -1;
                this.repaint();
                
            }
            return;
        }
        
        featureSelected = false;
        Feature[] allFeats = drawableDasSource.getFeatures();
        boolean featureOnThisPanel = false;
        for (int i = 0 ; i< allFeats.length;i++){
            if ( f.equals(allFeats[i])){
                
                // compare all the segments ...
                List seg1 = f.getSegments();
                List seg2 = allFeats[i].getSegments();
                
                boolean found = true ;
                Iterator iter1 = seg1.iterator();
                while ( iter1.hasNext()) {
                    Segment segment1 = (Segment) iter1.next();
                    Iterator iter2 = seg2.iterator();
                    boolean thisOneFound = false;
                    while (iter2.hasNext()){
                        Segment segment2 = (Segment) iter2.next();
                        if ( segment1.equals(segment2)){
                            thisOneFound = true;
                            break;
                        }
                    }
                    if ( ! thisOneFound){
                        // must be another line ...
                        found = false;
                        break;
                    }
                }
                if ( ! found){
                    continue;
                }
                    //logger.info("setting feature pos "  + i);
                selectedFeaturePos = i;
                featureOnThisPanel = true;
                featureSelected = true;
                break;
            }
        }
        
        if ( ! featureOnThisPanel){
            selectedFeaturePos = -1;            
        }
        this.repaint();
        
    }

    public void mouseOverFeature(SpiceFeatureEvent e) {}


    public void mouseOverSegment(SpiceFeatureEvent e) {}


    public void segmentSelected(SpiceFeatureEvent e) {}


    public void clearSelection() {
        //logger.info("clear selection");
        featureSelected = false;
        selectedFeaturePos = -1;
        this.repaint();
      
        
    }

    
    
    
    
    
}


class ProgressThread extends Thread{
    Component comp;
    JProgressBar bar;
    boolean continueFlag;
    
    public ProgressThread(JProgressBar bar, Component comp){
        this.bar =bar ;
        this.comp = comp;
        continueFlag = false;
    }
    
    
    public void run(){
        continueFlag = true;
        while (continueFlag){
            try {
            
                wait(100);
            } catch (Exception e){
            
            }
            bar.paint(comp.getGraphics());
            //bar.revalidate();
        }
       System.out.println("stopping thread");
    }
    
    public void cancel(){
        continueFlag = false;
    }
}
