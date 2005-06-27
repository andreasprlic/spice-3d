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
 * Created on Jun 15, 2005
 *
 */
package org.biojava.spice.Panel.seqfeat;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JScrollPane;


/** if the parent component of the spiceFeauteViewer is resized
 * also adapt the size of the spicefeauteViewer. 
 * This is required due to the SizeableJPanel which is used to tightly control the size of overything.
 * @author Andreas Prlic
 *
 */
public class SpiceComponentListener 
implements ComponentListener {
    SpiceFeatureViewer parent;
    JScrollPane scroll;
    public SpiceComponentListener(SpiceFeatureViewer fv){
        parent = fv;
        
    }
    public void componentHidden(ComponentEvent e){}
    public void componentMoved(ComponentEvent e){}
    public void componentResized(ComponentEvent e){
        Component c = e.getComponent();
        //System.out.println("component parent of spicefeauteview was resized " + c);
        
        parent.evaluateLayout();
        
        /*
        Dimension d = c.getSize();        
        
        int newWidth = d.width  ;
        int newHeight = d.height ;
        int subHeight = parent.getSubHeight() ;
        
        if ( newHeight < subHeight ) {
            newHeight = subHeight;
        }
        parent.setWidth(newWidth);
        parent.setHeight(newHeight);
        
        int stuffWidth = parent.getLabelWidth() + parent.getTypeWidth();
        newWidth = newWidth - stuffWidth;
        if ( newWidth < 1 ) {
            newWidth = 1 ;
        }
        parent.setFeaturePaneVisibleSize(newWidth, subHeight);
        //parent.setPreferredSize(new Dimension(d.width,d.height));
         * 
         */
        parent.revalidate();
        parent.repaint();
        //c.invalidate();
        
        
        if  ( c instanceof JScrollPane ){
            JScrollPane scroll = (JScrollPane)c;
            scroll.doLayout();
        }
        c.invalidate();
        c.validate();
        c.repaint();
    }
    public void componentShown(ComponentEvent e){}
}
