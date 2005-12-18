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
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.spice.Feature.FeatureImpl;
import org.biojava.spice.Feature.Segment;
import org.biojava.spice.Feature.Feature;

public class StructureFeaturePanel 
extends FeaturePanel{

    public static final Color STRUCTURE_COLOR            = Color.red;
    public static final Color STRUCTURE_BACKGROUND_COLOR = new Color(0.5f, 0.1f, 0.5f, 0.5f);

    public static final long serialVersionUID = 9173629552947296348l;
    
    // the line where to draw the structure
    //public static final int    DEFAULT_STRUCTURE_Y    = 30 ;
    
    Feature structureFeature;
    
    public StructureFeaturePanel(){
        super();
        structureFeature = new FeatureImpl();
    }
    
    //public void paint(Graphics g){
    //public void paintComponent(Graphics g){
        //logger.info("paint structureFeaturePanel");
     //   super.paint(g);
        //super.paintComponent(g);
       // Graphics2D g2D =(Graphics2D) g;
       
        // draw the structure features ...    
        //int aminosize =  Math.round(1 * scale) ;
        //int y = DEFAULT_STRUCTURE_Y;
        //drawStructureRegion(g2D,aminosize,scale,y);
     
        //g2D.drawString("structureFeaturePanel",10,30);
        
    //}
    
    /*
    private void drawStruc(Graphics2D g2D, int start, int end, 
            int aminosize,float scale, int y){
        //System.out.println("Structure " + start + " " + end);
        
        
        int DEFAULT_X_START = FeaturePanel.DEFAULT_X_START;
        int xstart = java.lang.Math.round(start * scale) + DEFAULT_X_START;
        int endx   = java.lang.Math.round(end * scale)-xstart + DEFAULT_X_START +aminosize;
        //int width  = aminosize ;
        int height = DEFAULT_Y_HEIGHT ;
        
        // draw the red structure line
        g2D.setColor(STRUCTURE_COLOR);  
        g2D.fillRect(xstart,y,endx,height);
        
        // highlite the background
       
        Composite origComposite = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));
        g2D.setColor(STRUCTURE_BACKGROUND_COLOR);
        //Dimension dstruc=this.getSize();
        Rectangle strucregion = new Rectangle(xstart , y, endx, DEFAULT_Y_HEIGHT);
        g2D.fill(strucregion);
        g2D.setComposite(origComposite);
    }
    */
    /** draw structrure covered region as feature 
    private void drawStructureRegion(Graphics2D g2D, int aminosize, float scale,int y){
        // data is coming from chain;
      
        //g2D.drawString("Structure",1,DEFAULT_STRUCTURE_Y+DEFAULT_Y_HEIGHT);
        //logger.info("draw structure " + structureFeature.getName());
        
        List segments = structureFeature.getSegments();
        Iterator iter = segments.iterator();
       
        while (iter.hasNext()){
            Segment s = (Segment) iter.next();
            int start = s.getStart();
            int end   = s.getEnd();
            drawStruc(g2D,start,end,aminosize,scale,y);
        }
        
    }*/
    
    public void setChain(Chain chn){
        //logger.info("StructureFeaturePanel setChain " + chn.getName());
        super.setChain(chn);
        chain = chn;
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
    
}
