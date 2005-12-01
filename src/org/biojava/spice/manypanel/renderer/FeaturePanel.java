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
 * Created on Nov 8, 2005
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.logging.*;
import javax.swing.JPanel;
import org.biojava.bio.structure.*;


import java.awt.Color;
import java.util.*;
import org.biojava.spice.Feature.*;


import java.awt.Image;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class FeaturePanel
extends JPanel{
    
    static final long serialVersionUID = 7893248902423l;
    
    public static final int    DEFAULT_X_START          = 50  ;
    public static final int    DEFAULT_X_RIGHT_BORDER   = 20 ;
    public static final int    DEFAULT_Y_START          = 0 ;
    public static final int    DEFAULT_Y_STEP           = 10 ;
    public static final int    DEFAULT_Y_HEIGHT         = 8 ;// thes size of the boxs
    public static final int    DEFAULT_Y_BOTTOM         = 16 ;
    public static final int    LINE_HEIGHT              = 10 ;
    
    public static final int    MINIMUM_HEIGHT           = 20;
    public static final Color  SEQUENCE_COLOR           = Color.LIGHT_GRAY;
    public static final Color  SCALE_COLOR              = Color.black;
    public static final Color  TEXT_SCALE_COLOR         = Color.GRAY;
    
    Character[] seqArr;
    
    Chain chain;
    float scale;
    Logger logger = Logger.getLogger("org.biojava.spice");
    
    //Feature[] features;
    
    private Image dbImage;
    private Graphics dbg;
    
    public static final Font seqFont = new Font("SansSerif", Font.BOLD, 10);
   
    
    public FeaturePanel() {
        super();
        this.setBackground(Color.white);
        chain = new ChainImpl();
        setDoubleBuffered(true);
        //features = getRandomFeatures();
        seqArr = new Character[0];       
    }
     
    public void setChain(Chain c){
        logger.info("FeaturePanel setting chain >" + c.getName()+"<");
        List a = c.getGroups("amino");
        seqArr = new Character[a.size()];
        
        chain = new ChainImpl();
        
        Iterator iter = a.iterator();
        int i = 0;
        while (iter.hasNext()){
            AminoAcid aa = (AminoAcid) iter.next();
            chain.addGroup(aa);
            seqArr[i] = aa.getAminoType();
            i++;
        }
        
            
    }
    
    public float getScale(){
        return scale;
    }
    
    public void setScale(float scale) {
        
        this.scale=scale;
        this.repaint();
    }
    
    
    public void update (Graphics g)
    {
        logger.info("update FeaturePanel");
        // initialize buffer
        if (dbImage == null)
        {
           
            dbImage = createImage (this.getSize().width, this.getSize().height);
            dbg = dbImage.getGraphics ();
        }
        
        // clear screen in background
        dbg.setColor (getBackground ());
        dbg.fillRect (0, 0, this.getSize().width, this.getSize().height);
        
        // draw elements in background
        dbg.setColor (getForeground());
        paint (dbg);
        
        // draw image on the screen
        g.drawImage (dbImage, 0, 0, this);
        
        
        
    }
    
  
    
    public void paint(Graphics g){
        super.paint(g);
        //public void paintComponent(Graphics g){
        // logger.info("paint featurePanel");
        //  super.paintComponent(g);
        Graphics2D g2D =(Graphics2D) g;
        
        //  1st: draw the scale
        int length = chain.getLength();
        int y = 1;
        y = drawScale(g2D,scale,length,1);
        
        // 2nd: sequence
        y = drawSequence(g2D,scale,length,y);
        
        
        // Rectangle test = new Rectangle(0,0, 50,20);
        //g2D.fill(test);
        
       
        //g2D.drawString("featurePanel",10,20);
        
        
        
    }
    /** draw the Scale */
    private int drawSequence(Graphics2D g2D, float scale, int length, int y){
        //g2D.drawString(panelName,10,10);
        
        g2D.setColor(SEQUENCE_COLOR);
        
        int l = Math.round(length*scale) ;
        
        Composite oldComp = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));  
        //logger.info("paint l " + l + " length " + length );
        if ( scale < 9){
            Rectangle seqline = new Rectangle(DEFAULT_X_START, y, l, LINE_HEIGHT);
            
            //g2D=  (Graphics2D)g;
            g2D.fill(seqline);   
            g2D.setColor(Color.black);
            g2D.draw(seqline);
        }
        
        if ( scale > 9){
            g2D.setColor(Color.black);
                  
            g2D.setFont(seqFont);
            //g2D.setColor(SCALE_COLOR);
            
            // display the actual sequence!;
            for ( int i = 0 ; i < length;i++){
                int xpos = Math.round(i*scale)+DEFAULT_X_START ;
                                
                // TODO:
                // color amino acids by hydrophobicity
                g2D.drawString(seqArr[i].toString(),xpos+1,y+2+DEFAULT_Y_STEP);
            }         
        }
        g2D.setComposite(oldComp);
        y+= DEFAULT_Y_STEP;
        return y;
    }
    
  
       
    /** draw the Scale */
    private int drawScale(Graphics2D g2D, float scale, int length, int y){
        
        g2D.setColor(SCALE_COLOR);
        
        int aminosize = Math.round(1*scale);
        //y = y + DEFAULT_Y_STEP;
        // the base line:
        
        int l = Math.round(length*scale) ;
        
        Rectangle baseline = new Rectangle(DEFAULT_X_START, y, l, 2);
        
        g2D.fill(baseline);
        
        // draw the vertical lines
        for (int i =1 ; i<= length ; i++){
            int xpos = Math.round(i*scale)+DEFAULT_X_START ;
            if ( ((i+1)%100) == 0 ) {
                
                if ( scale> 0.1) {
                    
                    //g2D.drawString(""+(i+1),xpos -5,y);
                    g2D.setColor(TEXT_SCALE_COLOR);
                    g2D.fillRect(xpos, y+2, aminosize, y+8);
                    g2D.setColor(SCALE_COLOR);
                }
                
            }else if  ( ((i+1)%50) == 0 ) {
                if ( scale>1.4) {
                    //g2D.drawString(""+(i+1),xpos-5,y);
                    g2D.setColor(TEXT_SCALE_COLOR);
                    g2D.fillRect(xpos,y+2, aminosize, y+8);
                    g2D.setColor(SCALE_COLOR);
                }
                
            } else if  ( ((i+1)%10) == 0 ) {                
                if ( scale> 3) {
                    //g2D.drawString(""+(i+1),xpos-5,y);
                    g2D.setColor(TEXT_SCALE_COLOR);
                    g2D.fillRect(xpos, y+2, aminosize, y+10);
                    g2D.setColor(SCALE_COLOR);
                }
                
            } 
            
        }
        
        int lastPos = Math.round(length*scale)+DEFAULT_X_START + 2;
        g2D.drawString(""+length,lastPos,y+DEFAULT_Y_STEP);
        
        return y ;
        
    }
    
    
    public Feature[] getRandomFeatures(){
        List feats = new ArrayList();
        Random generator = new Random();
        int length = 660;
        // draw random features
        for ( int i = 20;i<800;i+=DEFAULT_Y_STEP+DEFAULT_Y_HEIGHT){
            int nr = generator.nextInt(10);
            Feature f = new FeatureImpl();
            for ( int p =0 ; p < nr;p++){
                int pstart = generator.nextInt(length);
                int pend = generator.nextInt(length-pstart+1);
                f.addSegment(pstart,pend,"random");
                
                
                
            }
            feats.add(f);                     
        }
        
        return (Feature[]) feats.toArray(new Feature[feats.size()]);
    }
    
    
}
