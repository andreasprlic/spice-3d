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
 * Created on Jul 12, 2005
 *
 */
package org.biojava.spice.Panel.seqfeat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Andreas Prlic
 *
 */
public class PanelPopupMenuListener  implements ActionListener {
    
    //SpiceFeatureViewer featureView ;
    PanelListener parent;
    
    public PanelPopupMenuListener ( PanelListener parent) {
        //this.featureView = featureView;
        this.parent = parent ;
        
    }
    
    public void actionPerformed(ActionEvent e){
        // show the details of a DAS source ...
        // open a new frame that does something
        String cmd = e.getActionCommand();
 
        if ( cmd.equals("select")) {
            select(e);
        } else if ( cmd.equals("disable")) {
            disable(e);
        }
        
        
    }
    
    private void disable(ActionEvent e){
        Object source = e.getSource();
        
        FeatureView fv = parent.getCurrentFeatureView();
        if ( fv == null) 
            return;
        SpiceFeatureViewer viewer = fv.getSpiceFeatureViewer();
        viewer.remove(fv);
    }
    
    private void select(ActionEvent e){
        Object source = e.getSource();
        
        FeatureView fv = parent.getCurrentFeatureView();
        if ( fv == null) 
            return;
        SpiceFeatureViewer viewer = fv.getSpiceFeatureViewer();
        //displayFeatureViewFrame(viewer,fv);
        
        //System.out.println("trigger selectedDasSource");
        viewer.triggerSelectedDasSource(fv.getDasSource());
        
    }
}







