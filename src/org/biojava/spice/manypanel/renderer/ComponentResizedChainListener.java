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
 * Created on Dec 2, 2005
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class ComponentResizedChainListener implements ComponentListener {

    AbstractChainRenderer parent;
    
    public ComponentResizedChainListener(AbstractChainRenderer parent) {
        super();
        this.parent = parent;

    }

    public void componentResized(ComponentEvent event) {
       Component comp = event.getComponent();
      
       //System.out.println("component resized " + comp.getWidth());
       parent.setComponentWidth(comp.getWidth());

    }

    public void componentMoved(ComponentEvent arg0) {
   

    }

    public void componentShown(ComponentEvent arg0) {
      

    }

    public void componentHidden(ComponentEvent arg0) {
    

    }

}
