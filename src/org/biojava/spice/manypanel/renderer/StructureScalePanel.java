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
 * Created on Nov 17, 2005
 *
 */
package org.biojava.spice.manypanel.renderer;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.List;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.spice.feature.FeatureImpl;
import org.biojava.spice.feature.Segment;
import org.biojava.spice.feature.Feature;

public class StructureScalePanel 
extends SequenceScalePanel{

    public static final Color STRUCTURE_DEFAULT_COLOR            = Color.red;
    public static final Color STRUCTURE_BACKGROUND_COLOR = new Color(0.5f, 0.1f, 0.5f, 0.5f);

    public static final long serialVersionUID = 9173629552947296348l;
    
    // the line where to draw the structure
    //public static final int    DEFAULT_STRUCTURE_Y    = 30 ;
    
    Feature structureFeature;
    
  
    
    Color structureColor ;
    
    public StructureScalePanel(){
        super();
        structureFeature = new FeatureImpl();
       
        structureColor = STRUCTURE_DEFAULT_COLOR; 
    }
    
    
    
    /** get the color do draw the structure region
     * 
     * @return Color
     */
    public Color getStructureColor() {
        return structureColor;
    }



    /** set the color do draw the structure region
     * 
     * @param structureColor the Color to use
     */

    public void setStructureColor(Color structureColor) {
        this.structureColor = structureColor;
    }

    public void setChain(Chain chn){
        //logger.info("StructureFeaturePanel setChain " + chn.getName());
        super.setChain(chn);
        chain = chn;
        structureFeature = new FeatureImpl();
        int start = -1;
        int end   = -1;
       
        List groups = chain.getGroups("amino");
        
        for ( int i=0 ; i< groups.size() ; i++ ) {
            Group g = (Group)groups.get(i);
            
            if ( g.size() > 0 ){
                if ( start == -1){
                    start = i;
                }
                end = i;
            } else {
                if ( start > -1) {                  
                    
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
    
    
    
    public void paint(Graphics g){
        super.paint(g);
        //public void paintComponent(Graphics g){
        int length = chain.getLengthAminos();
        //logger.info("paint featurePanel " + scale + " " + length + " " + this.getWidth() + " " + this.getHeight());
        //  super.paintComponent(g);
        Graphics2D g2D =(Graphics2D) g;
        
        //  1st: draw the scale        
        int y = 1;
        y = drawScale(g2D,scale,length,1);
        
        // 2nd: sequence
        y = drawSequence(g2D,scale,length,y);
        
        
        String colorStruct = System.getProperty("SPICE:StructureRegionColor");
        if ( colorStruct != null ){
            //System.out.println(colorStruct);
            structureColor = Color.getColor("SPICE:StructureRegionColor");
        }
        
        String drawStruct = System.getProperty("SPICE:drawStructureRegion");
        boolean drawStructureRegion = false;
        if (( drawStruct != null ) && ( drawStruct.equals("true")) )
                drawStructureRegion = true;
        
        if ( drawStructureRegion) {
            // now draw the Structure region       
            drawStructureRegion(g2D,scale,length,y);
        }
        
    }
    
    
    
    private void drawStruc(Graphics2D g2D, int start, int end, 
            int aminosize,float scale, int y){
        //System.out.println("Structure " + start + " " + end);
        
        
        int DEFAULT_X_START = SequenceScalePanel.DEFAULT_X_START;
        int xstart = java.lang.Math.round(start * scale) + DEFAULT_X_START;
        int endx   = java.lang.Math.round(end * scale)-xstart + DEFAULT_X_START +aminosize +1 ;
        //int width  = aminosize ;
        int height = DEFAULT_Y_HEIGHT ;
        
        // draw the red structure line
        g2D.setColor(structureColor);  
        g2D.fillRect(xstart,y,endx,height);
        
        // highlite the background
       /* currently disabled ...
        Composite origComposite = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));
        g2D.setColor(STRUCTURE_BACKGROUND_COLOR);
        //Dimension dstruc=this.getSize();
        Rectangle strucregion = new Rectangle(xstart , y, endx, DEFAULT_Y_HEIGHT);
        g2D.fill(strucregion);
        g2D.setComposite(origComposite);
        */
    }
    
    /** draw structrure covered region as feature the data is coming from the chain object ( if a group has coordinates then something will be painted here
     * 
     * @param g2D the Graphics2D object to paint on
     * @param scale the scale of the sequence
     * @param length length of it
     * @param y the y position to draw this
     */ 
 
    private void drawStructureRegion(Graphics2D g2D,  float scale, int length, int y){
        // data is coming from chain;
      
        //g2D.drawString("Structure",1,DEFAULT_STRUCTURE_Y+DEFAULT_Y_HEIGHT);
        //logger.info("draw structure " + structureFeature.getName());
        int aminosize = Math.round(1*scale);
        if ( aminosize < 1)
            aminosize = 1;
        //int l = Math.round(length*scale);
        
        
        List segments = structureFeature.getSegments();
        Iterator iter = segments.iterator();
       
        while (iter.hasNext()){
            Segment s = (Segment) iter.next();
            int start = s.getStart();
            int end   = s.getEnd();
            drawStruc(g2D,start,end,aminosize,scale,y);
        }
        
    }
    
    
}
