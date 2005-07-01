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
 * Created on Jun 6, 2005
 *
 */
package org.biojava.spice.Panel.seqfeat;

import org.biojava.spice.Feature.*;
import org.biojava.spice.Panel.seqfeat.FeatureView;
import org.biojava.spice.Panel.seqfeat.SeqScaleCanvas;


/**
 * @author Andreas Prlic
 *
 */
public class SeqScale extends FeatureView 
{
    
    SeqScaleCanvas seqScaleCanv ;
    
    /**
     * 
     */
    public SeqScale() {
        super();
       seqScaleCanv  = new SeqScaleCanvas(this);
      
    }
    
   
    public SeqScaleCanvas getSeqScaleCanvas(){
        return seqScaleCanv;
    }
    
    public int getHeight(){
        int h =  DEFAULT_STRUCTURE_Y + DEFAULT_Y_STEP * 2;
        //System.out.println("*** seq scale  height " + h );
        return h;
        
    }
    
    public void setSeqLength(int seqL){
        super.setSeqLength(seqL);
        seqLength = seqL;
        
        seqScaleCanv.setSeqLength(seqL);
        
        //this.repaint();
    }
    
    /** highlite a region */
    public void highlite( int start , int end){
        seqScaleCanv.highlite(start,end);
    }
    

    /** highlite a region */
    public void highlite( int pos ){
        seqScaleCanv.highlite(pos);
    }
    
    public void setFeatures(Feature[] feats){
        seqScaleCanv.setFeatures(feats);
        typeLabelPanel.setFeatures(feats);
        labelField.setFeatures(feats);
    }
    
    public void setScale(float scale){
        super.setScale(scale);
        seqScaleCanv.setScale(scale);
    }
    
    public void setSeleceted(boolean flag){
        super.setSelected(flag);
        seqScaleCanv.setSelected(flag);
    }
    

}
