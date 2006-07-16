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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

import org.biojava.spice.SPICEFrame;
import java.net.URL;

/**
 * @author Andreas Prlic
 *
 */
public class SelectionLockMenuListener 
implements ActionListener
{
    
    //SeqFeaturePanel parent;
    SPICEFrame spice;
    URL url;
    public SelectionLockMenuListener(SPICEFrame parent_, URL u){
        spice=parent_;
        url = u;
    }
    
    // TODO remove this class it is not needed any longer ...
    
    public void actionPerformed(ActionEvent e){
        JMenuItem source = (JMenuItem)(e.getSource());
        //System.out.println(source);
 
        //System.out.println(source.getText());
        String txt = source.getText();
        System.out.println("source txt >"+txt+"<");
        /* AP:
        if ( txt.equals("Lock Selection")|| (txt.equals("Unlock Selection"))){
            
            boolean locked = isSelectionLocked();
            if ( locked )
                setSelectionLocked(false);
            else
                setSelectionLocked(true);
        } else {
            //String start = txt.substring(0,15);
            //System.out.println(start);
            //String url = txt.substring(16,txt.length());
            //System.out.println(url);
            spice.showDocument(url);
                
        
        }
        */
    }
}