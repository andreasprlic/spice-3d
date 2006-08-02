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

import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;

public class DasScrollPaneColumnHeader extends JLayeredPane {

    public static final long serialVersionUID = 0l;
    
    //SequenceScalePanel seqScale;
    //CursorPanel cursor;
    
    public DasScrollPaneColumnHeader(SequenceScalePanel seqScale, CursorPanel cursor ) {
        super();
        
        //this.seqScale = seqScale;
        //this.cursor = cursor;
        
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setDoubleBuffered(true);
        this.setOpaque(false);
        this.setBackground(SequenceScalePanel.BACKGROUND_COLOR);
        
        this.add(seqScale);
        this.add(cursor);
        
        moveToFront(cursor);

    }

}
