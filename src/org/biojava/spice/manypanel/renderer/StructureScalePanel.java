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
import java.awt.Rectangle;
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
    
    public Feature getStructureFeature(){
        return structureFeature;        
    }

    public void setChain(Chain chn){
        //logger.info("StructureFeaturePanel setChain " + chn.getName());
        super.setChain(chn);
        chain = chn;
        structureFeature = new FeatureImpl();
        structureFeature.setName("structure region 1");
        structureFeature.setMethod("structure region 2");
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
                    Segment s = createSegment(start,end);
                    structureFeature.addSegment(s);
                    start = -1 ;
                }
            }
        }
        // finish
        if ( start > -1) {
            Segment s = createSegment(start,end);
            
            structureFeature.addSegment(s);
        }
        
    }
    
    private Segment createSegment(int start, int end){
        Segment s = new Segment();
        s.setStart(start+1);
        s.setEnd(end+1);
        s.setName("structure region displayed in 3D display");
        return s;
    }
    
    public void paintComponent(Graphics g){
        //super.paintComponent(g);

        Rectangle drawHere = g.getClipBounds();                
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(drawHere.x,drawHere.y, drawHere.width, drawHere.height);
        
        
        Graphics2D g2D =(Graphics2D) g;

        setPaintDefaults(g2D);
        
        int y = 1;
        y = drawScale(g2D,y);
        y = drawSequence(g2D,y);
       
        
         
        if ( shouldDrawStructureRegion()) {
            String colorStruct = System.getProperty("SPICE:StructureRegionColor");
            if ( colorStruct != null ){
                //System.out.println(colorStruct);
                structureColor = Color.getColor("SPICE:StructureRegionColor");
            }
                        
            // now draw the Structure region       
            drawStructureRegion(g2D,y);
        }
        
    }
    
    public static boolean shouldDrawStructureRegion(){
        String drawStruct = System.getProperty("SPICE:drawStructureRegion");
        boolean drawStructureRegion = false;
        //System.out.println("drawStruct " + drawStruct);
        if (( drawStruct != null ) && ( drawStruct.equals("true")) )
                drawStructureRegion = true;
        return drawStructureRegion;
    }
    
    private void drawStruc(Graphics2D g2D, int start, int end, int y){
        //System.out.println("Structure " + start + " " + end);
        
                
        int xstart = coordManager.getPanelPos(start);
        int endx   = coordManager.getPanelPos(end)+1 ;
        
        int height = DEFAULT_Y_HEIGHT ;
        
        // draw the structure line
        g2D.setColor(structureColor);  
        int diff = endx - xstart + 1;
        g2D.fillRect(xstart,y,diff,height);
              
    }
    
    /** draw structrure covered region as feature the data is coming from the chain object ( if a group has coordinates then something will be painted here
     * 
     * @param g2D the Graphics2D object to paint on
     * @param scale the scale of the sequence
     * @param length length of it
     * @param y the y position to draw this
     */ 
 
    private void drawStructureRegion(Graphics2D g2D,  int y){
        // data is coming from chain;
        
        int textpos = y+DEFAULT_Y_HEIGHT -2;
      
        if ( scale > 9)
            textpos -= 2;
        
        g2D.drawString("Structure region",1, textpos);
        
        
        List segments = structureFeature.getSegments();
        Iterator iter = segments.iterator();
       
        while (iter.hasNext()){
            Segment s = (Segment) iter.next();
            int start = s.getStart()-1;
            int end   = s.getEnd()-1;
            drawStruc(g2D,start,end,y);
        }        
    }        
}
