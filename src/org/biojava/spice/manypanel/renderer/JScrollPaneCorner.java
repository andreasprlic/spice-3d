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
 * Created on Aug 3, 2006
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

public class JScrollPaneCorner extends JComponent {

    static final long serialVersionUID = 0l;
    protected void paintComponent(Graphics g) {
        
        g.setColor(SequenceScalePanel.BACKGROUND_COLOR);
        
        Rectangle drawHere = g.getClipBounds();
        g.fillRect(drawHere.x,drawHere.y, drawHere.width,drawHere.height);
        
    }
}
