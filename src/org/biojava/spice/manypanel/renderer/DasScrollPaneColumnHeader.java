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
 * Created on Aug 2, 2006
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;

public class DasScrollPaneColumnHeader extends JLayeredPane {

    public static final long serialVersionUID = 0l;
    
   
    
    public DasScrollPaneColumnHeader(SequenceScalePanel seqScale, CursorPanel cursor ) {
        super();
        
       
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setDoubleBuffered(true);
        this.setOpaque(false);       
        this.setBackground(Color.blue);
        
        this.add(seqScale, new Integer(1));
        this.add(cursor, new Integer(2));
        
        moveToFront(cursor);

    }

}
