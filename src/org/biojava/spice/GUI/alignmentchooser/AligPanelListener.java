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
 * Created on Feb 28, 2005
 *
 */
package org.biojava.spice.GUI.alignmentchooser;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.Component;
import java.awt.Dimension;

import org.biojava.bio.structure.Chain;

/**
 * @author Andreas Prlic
 *
 */
public class AligPanelListener implements ComponentListener {
    
    AlignmentChooser aligPanel;
    /**
     * 
     */
    public AligPanelListener(AlignmentChooser panel) {
        super();
        aligPanel = panel;
       
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
     */
    public void componentResized(ComponentEvent arg0) {
      
        //System.out.println("resized");
        
        Component comp = arg0.getComponent();
                        
        // rescale the alignment Panel...
        Dimension dim = comp.getSize();
        
        Chain chain = aligPanel.getChain();
        String seq_str = "" ; 
        if ( chain != null) {
            seq_str = chain.getSequence();    
            double y = (dim.getWidth() / ((double)seq_str.length() + 100 + 20)); // 100 is the label size
            //System.out.println("old y" + aligPanel.getScale() + "new y " + y);
            aligPanel.setScale(y);
            //comp.repaint() ;
        }
        
        //aliPanel.setSize(d);
        
        
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
     */
    public void componentMoved(ComponentEvent arg0) {
        // TODO Auto-generated method stub
        
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
     */
    public void componentShown(ComponentEvent arg0) {
        // TODO Auto-generated method stub
        
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
     */
    public void componentHidden(ComponentEvent arg0) {
        // TODO Auto-generated method stub
        
    }
    
}
