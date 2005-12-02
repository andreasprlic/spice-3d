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
 * Created on Nov 30, 2005
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.Cursor;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;

import org.biojava.bio.structure.Chain;
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.Segment;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureEvent;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureListener;
import org.biojava.spice.manypanel.eventmodel.SequenceEvent;
import org.biojava.spice.manypanel.eventmodel.SequenceListener;

public class SeqToolTipListener
implements SequenceListener, SpiceFeatureListener {

    JComponent parent;
    int length;
    String sequence;
    int oldpos ;
    ToolTipManager toolM;
    public static Logger logger = Logger.getLogger("org.biojava.spice");
    
    public SeqToolTipListener(JComponent parentPanel) {
        super();
        parent = parentPanel;
        length = -1;
        sequence = "";
        oldpos = -1;
        toolM = ToolTipManager.sharedInstance();
        
        toolM.setInitialDelay(0);
        toolM.setReshowDelay(0);
        toolM.setDismissDelay(10000);
        //toolM.setLightWeightPopupEnabled(true);
        
        
    }

    public void newSequence(SequenceEvent e) {
       
        
    }
    
    public void setChain(Chain c){
        sequence = c.getSequence();
        length = sequence.length();
    }

    public void selectedSeqPosition(int position) {
        parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        if (( position < 0) || (position >= length)) {
            parent.setToolTipText("");
            return;
        }
        
        if ( position == oldpos)
            return;
        
        
        oldpos = position;
        
        //System.out.println("toolTipper set "+ position + " " + length);
        parent.setToolTipText(""+(position+1) + " " + sequence.substring(position,position+1));
        
    }

    public void selectedSeqRange(int start, int end) {
       // parent.setToolTipText("");
        
    }

    public void selectionLocked(boolean flag) {
       
        
    }

    public void newObjectRequested(String accessionCode) {
        // TODO Auto-generated method stub
        
    }

    public void featureSelected(SpiceFeatureEvent e) {
        //logger.info("toolTipper feature selected ");
        parent.setToolTipText(e.getFeature().toString());
        
    }

    public void mouseOverFeature(SpiceFeatureEvent e) {
        
        Feature f = e.getFeature();
        //logger.info("toolTipper over feature " + f);
       
        if ( f.getType().equals("unknown")){
            parent.setToolTipText("");
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else {
            parent.setToolTipText(f.toString());
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }     
    }

    public void mouseOverSegment(SpiceFeatureEvent e) {
        //System.out.println("toolTipper mouseOverSegment " + e.getSegment());
        //parent.setToolTipText(e.getSegment().toString());
        Feature f = e.getFeature();
        Segment s = e.getSegment();
        
        if ( f.getType().equals("unknown")){
            parent.setToolTipText("");
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else {
            parent.setToolTipText(s.toString());
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        
        
    }

    public void segmentSelected(SpiceFeatureEvent e) {
        parent.setToolTipText(e.getSegment().toString());
        
    }

   
    
    
}
