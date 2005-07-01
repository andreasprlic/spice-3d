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

import java.awt.Graphics;
import java.util.Iterator;

/**
 * @author Andreas Prlic
 *
 */
public class TypePanelContainer extends AbstractFeatureViewContainer {
    SpiceFeatureViewer parent;
    /**
     * 
     */
    public TypePanelContainer(SpiceFeatureViewer parent) {
        super();
       this.parent=parent;
    }

    
    public void paintComponent( Graphics g) {
        //System.out.println("LabelPanelContainer paintComponent");
            //System.out.println("AbstractfeatureContainer paintComponent");
            //if( imbuf == null) 
            initImgBuffer();
            
            super.paintComponent(g); 	
            g.drawImage(imbuf, 0, 0, this);
            g.setColor(java.awt.Color.black);
            g.fillRect(0,0,getWidth(),getPanelHeight());
        int y = 0 ;
        int width = parent.getTypeWidth();
        TypeLabelPanel label      = seqScale.getTypePanel();  
        label.paintComponent(g,width,y);
        
        y=  seqScale.getHeight();
        
        Iterator iter = featureViews.iterator();
        while (iter.hasNext()){
            FeatureView fv = (FeatureView)iter.next();
            TypeLabelPanel      lp = fv.getTypePanel(); 
            //TypeLabelPanel tlp = fv.getTypePanel();
            //FeaturePanel   fp = fv.getFeaturePanel();
            
            lp.paintComponent(g,width,y);
            y+= fv.getHeight();
            //tlp.paintComponent(typeG,y);
            //y = fp.paintComponent(featureG,y);
        }
        
    }

    
}
