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
 * Created on Feb 6, 2005
 *
 */
package org.biojava.spice.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import org.biojava.spice.SPICEFrame;

/**
 * @author Andreas Prlic
 *
 */
public class SelectionLockMenuListener 
implements ActionListener {
    
    //SeqFeaturePanel parent;
    SPICEFrame spice;
    
    public SelectionLockMenuListener(SPICEFrame parent_){
        spice=parent_;
    }
    
    public void actionPerformed(ActionEvent e){
        JMenuItem source = (JMenuItem)(e.getSource());
                
        boolean locked = spice.isSelectionLocked();
        if ( locked )
            spice.setSelectionLocked(false);
        else
            spice.setSelectionLocked(true);
    }
}