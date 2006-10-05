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
 * Created on Oct 5, 2006
 *
 */
package org.biojava.spice.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.biojava.bio.structure.jama.Matrix;
import org.biojava.spice.StructureAlignment;

public class AlignmentFilterActionListener implements ActionListener{

    StructureAlignmentChooser chooser;
    public AlignmentFilterActionListener(StructureAlignmentChooser chooser) {
        super();
        
        this.chooser = chooser;

    }

    
    
    public void actionPerformed(ActionEvent e) {
        String filterBy = e.getActionCommand();
        System.out.println("filter by " + filterBy);
        
        Matrix jmolRotation = chooser.getJmolRotation();
        StructureAlignment structureAlignment = chooser.getStructureAlignment();
        
        structureAlignment.setFilterBy(filterBy);
        
        chooser.setStructureAlignment(structureAlignment);
        chooser.rotateJmol(jmolRotation);
        
        
    }

}
