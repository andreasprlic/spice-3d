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
 * Created on Mar 1, 2005
 *
 */
package org.biojava.spice.GUI.alignmentchooser;

import javax.swing.JToolTip;

import org.biojava.bio.gui.sequence.SequencePanel;
import org.biojava.bio.gui.sequence.SequencePanel;
import org.biojava.bio.gui.sequence.SequenceViewerEvent;
import org.biojava.bio.gui.sequence.SequenceViewerListener;
import org.biojava.bio.gui.sequence.SequenceViewerMotionListener;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.spice.*;

/**
 * @author Andreas Prlic
 *
 */
public class AligPanelMouseListener 
implements SequenceViewerListener,
        SequenceViewerMotionListener {

    
    SPICEFrame spice;
    SequencePanel seqPanel;
    JToolTip tooltip;
    
    public AligPanelMouseListener(SPICEFrame parent,SequencePanel srcxt){
        seqPanel = srcxt;
        tooltip = seqPanel.createToolTip();
        tooltip.setEnabled(true);
        spice = parent;
    }
    
    public void mouseClicked(SequenceViewerEvent ewt){
         
        int pos = ewt.getPos();
        int seqpos = seqPanel.graphicsToSequence(pos);
        System.out.println("seqpos "+seqpos);
        Object target = ewt.getTarget();
        if ( target instanceof FeatureHolder ){
            FeatureHolder fh = (FeatureHolder) target;
            //System.out.println("simple fh" + fh);
            java.util.Iterator iter = fh.features();
            Feature f = null;
            while (iter.hasNext()){
                f = (Feature)iter.next();
                System.out.println(f);
                String pdbcode = f.getSource();
                //System.out.println("from PDB " + pdbcode);
                
                System.out.println("loading PDB " + pdbcode.substring(0,4)); 
//              set focus to spice
                if ( spice instanceof SpiceApplication){
                    System.out.println("requesting focus");
                    SpiceApplication spiceapp = (SpiceApplication) spice;
                    spiceapp.requestFocus();
                }
                spice.load("PDB",pdbcode.substring(0,4));
                return;
            }
        }
    }
    
    public void mousePressed(SequenceViewerEvent ewt){
        
    }
    
    public void mouseReleased(SequenceViewerEvent ewt){
        
    }
    
    public void mouseDragged( SequenceViewerEvent ewt) {
        //System.out.println(ewt.getPos());
    }
    
    public void mouseMoved(SequenceViewerEvent ewt){
        //System.out.println("moved at" + ewt.getPos());
        Object target = ewt.getTarget();
        if ( target instanceof FeatureHolder ){
            FeatureHolder fh = (FeatureHolder) target;
            //System.out.println("simple fh" + fh);
            java.util.Iterator iter = fh.features();
            Feature f = null;
            while (iter.hasNext()){
                f = (Feature)iter.next();
                System.out.println(f);
                String pdbcode = f.getSource();
                //System.out.println("from PDB " + pdbcode);
                
                seqPanel.setToolTipText("load PDB " + pdbcode);                
            }
            if ( f == null){
                seqPanel.setToolTipText(null);
            }
        }
    }
    
   
    

}
