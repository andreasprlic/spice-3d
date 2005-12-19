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
package org.biojava.spice.manypanel.renderer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;

public class AlignmentPanel extends JPanel {
    
    public static Color COLOR_ONE = new Color(0,153,255);
    public static Color COLOR_TWO = new Color(0,51,255);
    
    
    Chain sequence1;
    Chain sequence2;
    float scale1;
    float scale2;
    int length1;
    int length2;
    
    Map alignmentMap1;
    Map alignmentMap2;
    
    int scrollLeftX1 ; // 
    int scrollLeftX2 ;
    
    final static long serialVersionUID = 98567459640964908l;
    
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    public AlignmentPanel() {
        super();
        sequence1 = new ChainImpl();
        sequence2 = new ChainImpl();
        length1=0;
        length2=0;
        this.setBackground(FeaturePanel.BACKGROUND_COLOR);
        
        scale1 = 1.0f;
        scale2 = 1.0f;
        
        scrollLeftX1 = 0;
        scrollLeftX2 = 0;
        
        clearAlignment();
    }
    
    public void clearAlignment(){
        alignmentMap1 = new HashMap();
        alignmentMap2 = new HashMap();
        scrollLeftX1 = 0;
        scrollLeftX2 = 0;
    }
    
    public void setSequence1(Chain c){
        sequence1 = c;
        length1=c.getLength();
        scrollLeftX1 = 0;
    }
    
    public void setSequence2(Chain c){
        sequence2 = c;
        length2 = c.getLength();
        scrollLeftX2 = 0;
        
    }
    public void setScrolled1(int v){
        scrollLeftX1 = v;
    }
    
    public void setScrolled2(int v){
        scrollLeftX2 =v;
    }
    public void setAlignmentMap1(Map m){
        alignmentMap1 = m;
    }
    
    public void setAlignmentMap2(Map m){
        alignmentMap2 = m;
    }
    
    public void setScale1(float scale){
        scale1 =scale;
        this.repaint();
    }
    public void setScale2(float scale){
        scale2=scale;
        this.repaint();
    }
    
   
    
    
    public void paint(Graphics g){
        //super.paintComponent(g);
        super.paint(g);
        
        //logger.info("paint " + length1 + " " +
        //        scale1 + " " + scale2 + " " +
        //        this.getWidth() );
        Graphics2D g2D = (Graphics2D) g;
        
     
        
        g2D.setColor(FeaturePanel.SEQUENCE_COLOR);
        int aminosize1 = Math.round(1*scale1);
        if ( aminosize1 < 1)
            aminosize1 = 1;
        
        int aminosize2 = Math.round(scale2);
        if ( aminosize2 <1)
            aminosize2 = 1;
        
        
        
        
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Composite oldComp = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));  
        
       // logger.info("paint l " + aminosize1 + " length " + length1 + " " + scale1 + " " + scrollLeftX1 );
        
        
        // paint alignment
        
        
        
        Set keys1=  alignmentMap1.keySet();
        Iterator iter = keys1.iterator();
        while (iter.hasNext()){
            Object o = iter.next();
            if ( ! (o instanceof Integer)){
            // logger.info("what is that? " + o);
                continue;
            }
            Integer  pos1 = (Integer) o;
            
            
           
            Object o2 = alignmentMap1.get(pos1);
            if ( ! (o2 instanceof Integer)) {
               // logger.info("what is that? " + o2);
                continue;
            }
            Integer pos2 = (Integer) o2;
            
            int h1 = pos1.intValue();
            int h2 = pos2.intValue();
            
            int p1 = Math.round(h1*scale1) + FeaturePanel.DEFAULT_X_START - scrollLeftX1;
            int p2 = Math.round(h2*scale2) + FeaturePanel.DEFAULT_X_START - scrollLeftX2;
            
            if (h1 % 2 == 0)
                g2D.setColor(COLOR_ONE);
            else
                g2D.setColor(COLOR_TWO);
            
            Polygon pol = new Polygon();
            
            pol.addPoint(p1,0);
            pol.addPoint(p2,20);
            pol.addPoint(p2+aminosize2+1,20);
            pol.addPoint(p1+aminosize1+1,0);
            g2D.fill(pol);
        }
        g2D.setComposite(oldComp);
    }

    
}
