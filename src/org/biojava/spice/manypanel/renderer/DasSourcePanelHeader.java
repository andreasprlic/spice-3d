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
 * Created on Jul 31, 2006
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.biojava.spice.ResourceManager;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.feature.Feature;
import org.biojava.spice.feature.Segment;
import org.biojava.spice.manypanel.drawable.DrawableDasSource;



/** a class that takes care of rendering the head of a DrawableDassource (the header part of the scrollPane)
 * 
 * @author Andreas Prlic
 * @since 3:56:49 PM
 * @version %I% %G%
 */
public class DasSourcePanelHeader 
extends DasSourcePanel{

    static final long serialVersionUID = 0l;
    
    public static final int INFO_ICON_SIZE = 16;
    public static final int LINK_ICON_SIZE = 10;
    
    DrawableDasSource dasSource;
 
    static Color TEXT_COLOR ;
    
    static ImageIcon clock;
    
    static {
        String col = ResourceManager.getString("org.biojava.spice.manypanel.renderer.FontColor");
        TEXT_COLOR = Color.decode(col);
        
        clock = SpiceApplication.createImageIcon("clock.png");
        
    }
    
    public DasSourcePanelHeader(DrawableDasSource ds) {
        super(ds);
        dasSource = ds;
        
    }


  
    
    public void paintComponent(Graphics g){
        int y = SequenceScalePanel.DEFAULT_Y_START + SequenceScalePanel.DEFAULT_Y_STEP ; 
        paintComponent(g,y);
    
    }
    
    public void paintComponent(Graphics g, int y){
      
    
        //super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
                  
        if (drawableDasSource.getLoading()){
  
            if (clock != null)
                clock.paintIcon(null, g, 1,y + clock.getIconHeight());
            else                   
                g2D.drawString("loading ... ",1,y+(SequenceScalePanel.DEFAULT_Y_STEP*2));
            
        }
        
     
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        // draw the name of the das source
        g2D.setFont(headFont);
        
        g2D.setColor(TEXT_COLOR);
        
        String str = drawableDasSource.getDasSource().getNickname();
        //logger.info("paint DasSourcePanel "+str);
        if ( infoIcon != null)
            infoIcon.paintIcon(null, g, 1,y); 
        else 
            g2D.drawString("i",1,y+SequenceScalePanel.DEFAULT_Y_STEP);
        
        g2D.drawString(str,INFO_ICON_SIZE,y+SequenceScalePanel.DEFAULT_Y_STEP);
        g2D.setFont(plainFont);
        
        y += SequenceScalePanel.DEFAULT_Y_STEP;
        Map[] stylesheet = drawableDasSource.getStylesheet();
        Feature[] features = drawableDasSource.getFeatures();
        for ( int f =0 ; f< features.length;f++) {
            Feature feature = features[f];
            if ( feature.getType().equals("hydrophobicity"))
                if (f >0)
                    continue;
            drawLabel(g,feature,y, stylesheet);
            y += SequenceScalePanel.DEFAULT_Y_STEP;
        }
        
    }
    
    /** display the type of the feature at the beginnng of the line
     * 
     * @param g
     * @param f
     * @param y
     */
    private void drawLabel(Graphics g, Feature f, int y, Map[] stylesheet){
        String type = f.getType();
        
        List segs = f.getSegments();
        Color c = Color.white;
        
        if ( stylesheet == null)
            stylesheet = new Map[0];
        
        // check if we have a stylesheet for this Features
        boolean foundAStyleSheet = false;
        for ( int i=0 ; i< stylesheet.length ;i++ ){
            Map m = stylesheet[i];
            String styleType = (String) m.get("type");
            if ( f.getType().equals(styleType)){
                Color col= (Color)(m.get("color"));
                if ( col != null) {
                    c = col;
                    foundAStyleSheet = true;
                    break;
                }
            }
        }
        
        // otherwise use the one from the segment
        if (! foundAStyleSheet) {
            if ( segs.size() > 0){
                Segment s = (Segment) segs.get(0);
                c = s.getColor();
            } 
        }
        g.setColor(c);
               
        Shape clip = g.getClip();
        
        // draw a background rectangle
        g.fillRect(0,y,DasScrollPaneRowHeader.SIZE ,SequenceScalePanel.DEFAULT_Y_STEP);
                
        
        // draw the link icon
        // check if link
        String link = f.getLink();
        if ( ( link != null) && (! link.equals(""))){
            //URL url ;
            try {
                new URL(link);
                //g2D.drawString("L->", 1,y+DEFAULT_Y_HEIGHT);
                if ( linkIcon != null)
                    linkIcon.paintIcon(null, g, 1,y);
                else {
                    g.setColor(TEXT_COLOR);
                    g.drawString("->",1,y+SequenceScalePanel.DEFAULT_Y_STEP);
                }
            } catch (MalformedURLException e){
                //continue ;
            }
        }
        
        
        // now draw the actual label - text
        g.setColor(Color.black);
        
        g.clipRect(0,y,DasScrollPaneRowHeader.SIZE ,SequenceScalePanel.DEFAULT_Y_STEP);
        
        g.drawString(type,2+INFO_ICON_SIZE,y+SequenceScalePanel.DEFAULT_Y_STEP);
        //g.setFont(plainFont);
        
        g.setClip(clip);
        
    }
    
}
