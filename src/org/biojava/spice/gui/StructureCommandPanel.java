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
 * Created on Feb 7, 2005
 *
 */
package org.biojava.spice.gui;

import javax.swing.JTextField;

import org.biojava.spice.jmol.JmolCommander;

/** Provides an TextField that can be used to enter new RASMOL like commands that are sent to JMol.
 * @author Andreas Prlic
 *
 */
public class StructureCommandPanel 
extends JTextField {
    private static final long serialVersionUID = 974238740140894010L;
    
    
    //SPICEFrame spice;
    JmolCommander structurePanelListener;
    
    public StructureCommandPanel (JmolCommander  liste){
        structurePanelListener = liste;
        
        this.setText("enter RASMOL like command...");
        StructureCommandListener listener = new StructureCommandListener(liste,this) ;
        this.addActionListener(listener);
        this.addMouseListener(listener);
        this.addKeyListener(listener);
    }    
}
