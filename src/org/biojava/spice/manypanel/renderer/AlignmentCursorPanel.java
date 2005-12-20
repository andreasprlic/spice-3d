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
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.spice.manypanel.eventmodel.SequenceEvent;

public class AlignmentCursorPanel extends JPanel
{

    
    
    static final long serialVersionUID = 23112350310101729l;
    Map alignmentMap1;
    Map alignmentMap2;
    int selectionStart1;
    int selectionEnd1;
    int selectionStart2;
    int selectionEnd2;
    boolean selectionLocked;
    Chain sequence1;
    Chain sequence2;
    
    float scale1;
    float scale2;
    
    int aminosize1;
    int aminosize2;
    int scrollLeftX1 ; // 
    int scrollLeftX2 ;
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    
    public AlignmentCursorPanel() {
        super();
        this.setOpaque(false);
        setDoubleBuffered(true);
        
        clearSelection();
        clearAlignment();
       
        
        aminosize1 = 1;
        aminosize2 = 1;
     
    }

    public void clearAlignment(){
        alignmentMap1 = new HashMap();
        alignmentMap2 = new HashMap();
        scrollLeftX1 = 0;
        scrollLeftX2 = 0;
        sequence1 =new ChainImpl();
        sequence2 = new ChainImpl();
        scale1 =1.0f;
        scale2 = 1.0f;
    }
    
    public void clearSelection() {
        selectionStart1 =-1;
        selectionEnd1 =-1;
        selectionStart2 = -1;
        selectionEnd2 = -1;
        selectionLocked = false;
    }

    public void setScrolled1(int v){
        scrollLeftX1 = v;
        this.repaint();
    }
    
    public void setScrolled2(int v){
        scrollLeftX2 =v;
        this.repaint();
    }
    
    public void setChain1(Chain s1){
        sequence1 = s1;
        scrollLeftX1 = 0;
    }
    
    public void setChain2(Chain s2){
        sequence2 = s2;
        scrollLeftX2 = 0;
    }
    
    public void setScale1(float s1){
        scale1 =s1;
        aminosize1 = Math.round(1*scale1);
        if ( aminosize1 < 1)
            aminosize1 = 1;
       
    }
    
    public void setScale2(float s2){
        scale2 = s2;
        
        aminosize2 = Math.round(scale2);
        if ( aminosize2 <1)
            aminosize2 = 1;
    
    }
    
    public void newSequence1(SequenceEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void selectedSeqPosition1(int position) {
        if ( selectionLocked) 
            return;
        selectionStart1 = position;
        selectionEnd1 = position;
        
    }

    public void selectedSeqRange1(int start, int end) {
        if ( selectionLocked) 
            return;
        selectionStart1 = start;
        selectionEnd1 = end;
        
    }

    public void selectionLocked(boolean flag) {
       selectionLocked = flag;
        
    }

    public void newObjectRequested1(String accessionCode) {
       alignmentMap1 = new HashMap();
       sequence1 = new ChainImpl();
        
    }

    public void noObjectFound1(String accessionCode) {
        // TODO Auto-generated method stub
        
    }
    
   

    public void newSequence2(SequenceEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void selectedSeqPosition2(int position) {
        if ( selectionLocked) 
            return;
       selectionStart2 = position;
       selectionEnd2 = position;
        
    }

    public void selectedSeqRange2(int start, int end) {
        if ( selectionLocked) 
            return;
       selectionStart2 = start;
       selectionEnd2 = end;
        
    }

 
    

    public void newObjectRequested2(String accessionCode) {
      alignmentMap2 = new HashMap();
      sequence2 = new ChainImpl();
        
    }

    public void noObjectFound2(String accessionCode) {
        // TODO Auto-generated method stub
        
    }
    
    
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        
        if (  ( selectionStart1 < 0) && (selectionEnd1 < 0)){
            return;
        }
        
        if (  ( selectionStart2 < 0) && (selectionEnd2 < 0)){
            return;
        }
        
        int h = this.getHeight();
        
        Graphics2D g2D =(Graphics2D) g;
        
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			     RenderingHints.VALUE_ANTIALIAS_ON);
        Composite oldComp = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));        
        
        g2D.setColor(CursorPanel.SELECTION_COLOR);
        
        int tmpSelectionStart1 = selectionStart1;
        if (( selectionStart1 < 0 ) && ( selectionEnd1 >=0)) {
            tmpSelectionStart1 = 0;
        }
        
        int tmpSelectionStart2 = selectionStart2;
        if (( selectionStart2 < 0 ) && ( selectionEnd2 >=0)) {
            tmpSelectionStart2 = 0;
        }
      
        int startX1 = Math.round(tmpSelectionStart1 *scale1) + FeaturePanel.DEFAULT_X_START - scrollLeftX1;
        int endX1   = Math.round(selectionEnd1*scale1) + aminosize1 + FeaturePanel.DEFAULT_X_START - scrollLeftX1 ;
        if (endX1 <1)
            endX1 = 1;
        
        int startX2 = Math.round(tmpSelectionStart2 *scale2) + FeaturePanel.DEFAULT_X_START - scrollLeftX2;
        int endX2   = Math.round(selectionEnd2*scale2) + aminosize2 + FeaturePanel.DEFAULT_X_START - scrollLeftX2;
        if (endX2 <1)
            endX2 = 1;
        
        //logger.info(startX1 +  " " + endX1 + " " + startX2  + " " +endX2);
        
//        Polygon pol = new Polygon();
//        pol.addPoint(startX1,0);
//        pol.addPoint(startX2,h);
//        pol.addPoint(endX2,h);
//        pol.addPoint(endX1,0);
//        g2D.fill(pol);
        
        GeneralPath path = new GeneralPath();
        path.moveTo(startX1,0);            
        path.lineTo(startX2,h);
        path.lineTo(endX2,h);
        path.lineTo(endX1,0);
        path.lineTo(startX1,0);
        
        g2D.fill(path);
        
        
        g2D.setComposite(oldComp);
        
    }

}
