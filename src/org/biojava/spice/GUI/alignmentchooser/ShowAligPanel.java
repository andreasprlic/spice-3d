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
 * Created on Dec 18, 2005
 *
 */
package org.biojava.spice.GUI.alignmentchooser;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.JPanel;

import org.biojava.bio.Annotation;
import org.biojava.bio.program.das.dasalignment.Alignment;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.FeatureImpl;
import org.biojava.spice.Feature.Segment;
import org.biojava.spice.das.AlignmentTools;
import org.biojava.spice.manypanel.renderer.DasSourcePanel;
import org.biojava.spice.manypanel.renderer.FeaturePanel;

public class ShowAligPanel 
extends JPanel{
    Chain sequence;
    float scale;
    Feature structureFeature;
    int aminosize;
    Alignment alignment;
    String labelstring;
    
    public static final Color STRUCTURE_COLOR            = Color.red;
    public static final Color STRUCTURE_BACKGROUND_COLOR = new Color(0.5f, 0.1f, 0.5f, 0.5f);

    public static final long serialVersionUID = 9173629552941296348l;
    
    public ShowAligPanel(Alignment ali) {
        super();
        sequence = new ChainImpl();
        scale =1.0f;
        structureFeature = new FeatureImpl();
        aminosize = 1;
        alignment = ali;
        labelstring = "";
        this.setBackground(FeaturePanel.BACKGROUND_COLOR);
        this.setPreferredSize(new Dimension(400,25));
    }
    
    public void setChain(Chain chain){
        sequence = chain;
        
        int start = -1;
        int end   = -1;
        
        Annotation object = AlignmentTools.getObject(chain.getSwissprotId(),alignment);
       
        List details = new ArrayList();
        labelstring =  chain.getSwissprotId();
        
        try {
            details = (List) object.getProperty("details");
        } catch (NoSuchElementException e){
            // details are not provided
           // logger.info("alignment does not contain details");
        }
        Iterator iter = details.iterator();
        while (iter.hasNext()){
            Annotation detail = (Annotation) iter.next();
            //logger.info(detail.getProperty("property").toString() + " " + detail.getProperty("detail").toString());
            String property = (String) detail.getProperty("property");
            String detailstr   = (String) detail.getProperty("detail");
            
            if ( property.equals("resolution")){
                if ( detailstr != null )
                    labelstring += " " + detailstr + "A" ; 
            }
            if ( property.equals("experiment_type")){
                labelstring += " " + detailstr ;
            }
            if ( property.equals("molecule description")){
                labelstring += " description: " + detailstr;
            }
        }
        
        
        
        structureFeature = new FeatureImpl();
        
        for ( int i=0 ; i< chain.getLength() ; i++ ) {
            Group g = chain.getGroup(i);
            String aminopdb = g.getPDBCode();
            
            if ( aminopdb != null ){
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
    
    
    public void setScale(float f){
        scale =f;
        aminosize = Math.round(1*scale);
        this.repaint();
    }
    
    
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D)g;
        int y = 2;
        
       
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        // draw the name of the das source
        g2D.setFont(DasSourcePanel.headFont);
        g2D.setColor(Color.black);
        
        String str = labelstring;
        //logger.info("paint DasSourcePanel "+str);
        
        g2D.drawString(str,2,y + 10);
        g2D.setFont(DasSourcePanel.plainFont);
        y+= FeaturePanel.DEFAULT_Y_STEP;
        drawStructureRegion(g2D,y);
    }
                
                
    /** draw structrure covered region as feature */
    private void drawStructureRegion(Graphics2D g2D,int y){
        // data is coming from chain;
        
        //g2D.drawString("Structure",1,DEFAULT_STRUCTURE_Y+DEFAULT_Y_HEIGHT);
            //logger.info("draw structure " + structureFeature.getName());
        
        List segments = structureFeature.getSegments();
        Iterator iter = segments.iterator();
        
        while (iter.hasNext()){
            Segment s = (Segment) iter.next();
            int start = s.getStart();
            int end   = s.getEnd();
            drawStruc(g2D,start,end,y);
        }
        
    }
    
    private void drawStruc(
            Graphics2D g2D,
            int start,
            int end, 
             int y){
        //System.out.println("Structure " + start + " " + end);
        
        
        int DEFAULT_X_START = FeaturePanel.DEFAULT_X_START;
        int xstart = java.lang.Math.round(start * scale) + DEFAULT_X_START;
        int endx   = java.lang.Math.round(end * scale)-xstart + DEFAULT_X_START +aminosize;
        //int width  = aminosize ;
        int height = FeaturePanel.DEFAULT_Y_HEIGHT ;
        
        // draw the red structure line
        g2D.setColor(STRUCTURE_COLOR);  
        g2D.fillRect(xstart,y,endx,height);
        
        // highlite the background
       
        Composite origComposite = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));
        g2D.setColor(STRUCTURE_BACKGROUND_COLOR);
        //Dimension dstruc=this.getSize();
        Rectangle strucregion = new Rectangle(xstart , y, endx, FeaturePanel.DEFAULT_Y_HEIGHT);
        g2D.fill(strucregion);
        g2D.setComposite(origComposite);
    }
}
