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
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.spice.ResourceManager;

public class AlignmentPanel extends JPanel {
    
    final static long serialVersionUID = 98567459640964908l;    
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    public static Color COLOR_MATCH_ONE;
    public static Color COLOR_MATCH_TWO;
    
    public static Color COLOR_MISMATCH_ONE;
    public static Color COLOR_MISMATCH_TWO;
    
  
    static {
     
        
        String col1 = ResourceManager.getString("org.biojava.spice.manypanel.renderer.AlignmentPanel.ColorMatchOne");
        COLOR_MATCH_ONE= Color.decode(col1); 
        
        String col2 = ResourceManager.getString("org.biojava.spice.manypanel.renderer.AlignmentPanel.ColorMatchTwo");
        COLOR_MATCH_TWO = Color.decode(col2);
        
        String col3 = ResourceManager.getString("org.biojava.spice.manypanel.renderer.AlignmentPanel.ColorMisMatchOne");
        COLOR_MISMATCH_ONE= Color.decode(col3); 
        
        String col4 = ResourceManager.getString("org.biojava.spice.manypanel.renderer.AlignmentPanel.ColorMisMatchTwo");
        COLOR_MISMATCH_TWO= Color.decode(col4); 
    }
    
    
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
    
   
    
    public AlignmentPanel() {
        super();
        
        sequence1 = new ChainImpl();
        sequence2 = new ChainImpl();
        length1=0;
        length2=0;
        this.setBackground(SequenceScalePanel.BACKGROUND_COLOR);
        
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
        synchronized(sequence1){
            sequence1 = c;
        }
        length1=c.getLength();
        scrollLeftX1 = 0;
    }
    
    public void setSequence2(Chain c){
        synchronized (sequence2){
            sequence2 = c;
        }
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
        synchronized (alignmentMap1) {
            alignmentMap1 = m;
        }
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
       
        super.paint(g);
        
        //TODO: find the bug why sometimes start with UniProt does not color alignment correctly
        
        //System.out.println("sequence1 " + sequence1.getSequence());
        //System.out.println("sequence2 " + sequence2.getSequence());       
        
        Graphics2D g2D = (Graphics2D) g;        
        
        g2D.setColor(SequenceScalePanel.SEQUENCE_COLOR);
        int aminosize1 = Math.round(1*scale1);
        if ( aminosize1 < 1)
            aminosize1 = 1;
        
        int aminosize2 = Math.round(scale2);
        if ( aminosize2 <1)
            aminosize2 = 1;                
        
        
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Composite oldComp = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));  
       
        // paint alignment
        
        int h = this.getHeight();
        
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
            
            if ( h1 < 0 )
                h1 = 0;
            if ( h2 < 0)
                h2 = 0;
            
            int p1 = Math.round(h1*scale1) + SequenceScalePanel.DEFAULT_X_START - scrollLeftX1 ;
            int p2 = Math.round(h2*scale2) + SequenceScalePanel.DEFAULT_X_START - scrollLeftX2 ;
            
            Group g1 = null;
            Group g2 = null;
            
            
            
            //logger.info("seq1 length " + sequence1.getLength() + " " + h1);
            if ( sequence1.getLength() > h1)
                g1 = sequence1.getGroup(h1);
            
            //logger.info("seq2 length " + sequence2.getLength() + " " + h2);            
            if ( sequence2.getLength() > h2)
                g2 = sequence2.getGroup(h2);
            
            if ( ((g1 != null) && (g2 != null))
                    &&
                    ( g1.getPDBName().equals(g2.getPDBName()))) {
            
                if (h1 % 2 == 0)
                    g2D.setColor(COLOR_MATCH_ONE);
                else
                    g2D.setColor(COLOR_MATCH_TWO);
            } else {
                //if (g1 != null && g2 != null)
                //    System.out.println(h1 + " " + g1 + " "+ h2 + " "  + g2);
                if (h1 % 2 == 0)
                    g2D.setColor(COLOR_MISMATCH_ONE);
                else
                    g2D.setColor(COLOR_MISMATCH_TWO);
            
            }
            
          
            GeneralPath path = new GeneralPath();
            path.moveTo(p1,0);            
            path.lineTo(p2,h);
            path.lineTo(p2+aminosize2+1,h);
            path.lineTo(p1+aminosize1+1,0);
            path.lineTo(p1,0);
            g2D.fill(path);
                      
            
        }
        g2D.setComposite(oldComp);
    }

    
}
