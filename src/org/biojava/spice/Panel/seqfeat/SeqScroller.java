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
 * Created on Jun 13, 2005
 *
 */
package org.biojava.spice.Panel.seqfeat;

import java.util.ArrayList;

import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

//import org.biojava.spice.Panel.seqf.FeaturePanel;

import java.util.List;
import java.awt.Color;
/**
 * @author Andreas Prlic
 *
 */
public class SeqScroller
extends SizeableJPanel 
 {
    static final int RES_MIN  = 1;
    static final int RES_MAX  = 100;
    static final int RES_INIT = 100;
    
    public static final Color DEFAULT_BACKGROUND = Color.black; 
    JSlider residueSizeSlider;
    //this.add(residueSizeSlider);
    
    
    List changeListeners ;
    
    /**
     * 
     */
    public SeqScroller() {
        super();
        changeListeners = new ArrayList();    

        residueSizeSlider = new JSlider(JSlider.HORIZONTAL,
                RES_MIN, RES_MAX, RES_INIT);
        residueSizeSlider.setInverted(true);
        //residueSizeSlider.setMajorTickSpacing(5);
        //residueSizeSlider.setMinorTickSpacing(2);
        residueSizeSlider.setPaintTicks(false);
        residueSizeSlider.setPaintLabels(false);
        setBackground(DEFAULT_BACKGROUND);
        this.add(residueSizeSlider);
        
    }

    public void addChangeListener(ChangeListener cl){
        changeListeners.add(cl);
        residueSizeSlider.addChangeListener(cl);
    }
    
    public void setScrollValue(int value) {
        residueSizeSlider.setValue(value);
      
    }
    
}
