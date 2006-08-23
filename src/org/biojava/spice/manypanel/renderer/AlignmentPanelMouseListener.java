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
 * Created on Aug 9, 2006
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class AlignmentPanelMouseListener implements MouseListener,
        MouseMotionListener {

    AlignmentRenderer parent;
    
    public AlignmentPanelMouseListener(AlignmentRenderer parent) {
        super();
        
        this.parent = parent;
        

    }

    public void mouseClicked(MouseEvent arg0) {
    

    }

    public void mousePressed(MouseEvent arg0) {
   

    }

    public void mouseReleased(MouseEvent arg0) {
     

    }

    public void mouseEntered(MouseEvent arg0) {
       

    }

    public void mouseExited(MouseEvent arg0) {
      

    }

    public void mouseDragged(MouseEvent arg0) {
       

    }

    public void mouseMoved(MouseEvent e) {

        /*int seqPos1 = getSeqPos1(e);
        int seqPos2 = getSeqPos2(e);
        System.out.println(e.getX());
        System.out.println(" mouse moved at " + seqPos1 + " " + seqPos2);
        */
        
        
        
    }
    
    /*
    private int getSeqPos1(MouseEvent e){
        float scale1  = parent.getScale1();
        int scrolled1 = parent.getScrolled1();
        int XBORDER   = SequenceScalePanel.DEFAULT_X_START;
        int pos1      = Math.round((e.getX()-XBORDER+scrolled1) / scale1) ;
        return pos1;
    }
    
    private int getSeqPos2(MouseEvent e){
        float scale2  = parent.getScale2();
        int scrolled2 = parent.getScrolled2();
        int XBORDER   = SequenceScalePanel.DEFAULT_X_START;
        int pos2      = Math.round((e.getX()-XBORDER+scrolled2) / scale2) ;
        return pos2;
    }*/

}
