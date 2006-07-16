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
package org.biojava.spice.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;

/**
 * @author Andreas Prlic
 *
 */
public class SelectionLockPopupListener 
extends MouseAdapter {
    JPopupMenu popup;
    //SPICEFrame spice;
    public SelectionLockPopupListener(JPopupMenu popupMenu) {
        //spice = parent_;
        popup  = popupMenu;
    }
    
    public void mousePressed(MouseEvent e) {
        //System.out.println(e);
        maybeShowPopup(e);
    }
    
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }
    
    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            
            /*// get the menu items
            MenuElement[] m =	popup.getSubElements() ;
            JMenuItem m0 = (JMenuItem)m[0].getComponent();
            //JMenuItem m1 = (JMenuItem)m[1].getComponent();
            
            // adapt the display of the MenuItems
            if ( spice.isSelectionLocked()) 
                m0.setText("Unlock Selection") ;
            else 
                m0.setText("Lock Selection");
                  */     
            popup.show(e.getComponent(),		       
                    e.getX(), e.getY());
        }
    }
}

