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
 * Created on Jun 29, 2005
 *
 */
package org.biojava.spice.Panel.seqfeat;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JPanel;

/** an abstract class that manages the set of feature views that are being displayed .
 * 
 * @author Andreas Prlic
 *
 */
public abstract class AbstractFeatureViewContainer 
extends JPanel{
    List featureViews;
    SeqScale seqScale;
    BufferedImage imbuf;
    
    /**
     * 
     */
    public AbstractFeatureViewContainer() {
        super();
        featureViews = new ArrayList();
        seqScale = new SeqScale();
        initImgBuffer();
        this.setDoubleBuffered(true);
    }

    public void clear(){
        featureViews = new ArrayList();
        
    }
    
    public void setSeqScale(SeqScale seqScale){
        this.seqScale = seqScale;
    }
    
    public void addFeatureView(FeatureView view) {
        featureViews.add(view);
        
        int height = getPanelHeight();
        this.setPreferredSize(new Dimension(this.getWidth(), height));
    }
    
    public int getPanelHeight(){
        int y = 0;
        
        if (seqScale != null) {
            y+= seqScale.getHeight();
            //LabelPane label      = seqScale.getLabel();
            //y += label.getCanvasHeight();
        }
        
        if ( featureViews != null ) {
            Iterator iter = featureViews.iterator();
            while ( iter.hasNext()){
                FeatureView fvtmp = (FeatureView)iter.next();
                y+= fvtmp.getHeight();
                //LabelPane lab = fvtmp.getLabel();
                //y+= lab.getCanvasHeight();
            } 
        }
        //System.out.println("FeatureContainer: getPanelHeight: " + y);
        return y;
    }
    
    public void initImgBuffer(){
        
        float scale = seqScale.getScale();
        int aminosize =  Math.round(1 * scale) ;
        Dimension dstruc = this.getSize();
        int width = this.getWidth();
        int height = getPanelHeight();
        //System.out.println("AbstractFeatureContainer initImgBuffer" + width + " " + height);
        //System.out.println(width);
        if ( height < 1) 
            height=1;
        
        //System.out.println("AbstractFeatueContainer init img " + width + " " + height);
        imbuf = (BufferedImage)this.createImage(width,height);
        this.setPreferredSize(new Dimension(width,height));
        // color background
         
        
    }
    
    
      
}
