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
import org.biojava.bio.structure.Chain;

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
       seqScaleCanv  = new SeqScaleCanvas();
      
    }
    
    
    public void setCanvasHeight(int height) {
        super.setCanvasHeight(height);
        seqScaleCanv.setCanvasHeight(height);
    }
    
    public SeqScaleCanvas getSeqScaleCanvas(){
        return seqScaleCanv;
    }
    
    
    public void setSeqLength(int seqLength){
        super.setSeqLength(seqLength);
        this.seqLength = seqLength;
        
        seqScaleCanv.setSeqLength(seqLength);
        
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
    
    
    public void setChain(Chain chain){
        super.setChain(chain);
        seqScaleCanv.setChain(chain);
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
