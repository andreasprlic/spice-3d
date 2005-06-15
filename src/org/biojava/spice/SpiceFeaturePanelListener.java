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
 * Created on Jun 15, 2005
 *
 */
package org.biojava.spice;

import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.Segment;
import org.biojava.spice.Panel.seqfeat.FeatureEvent;
import org.biojava.spice.Panel.seqfeat.FeatureViewListener;
import org.biojava.spice.Panel.seqfeat.SelectedSeqPositionListener;

/** This class listenes to various events that occur in the SpiceFeaturePanel
 * @author Andreas Prlic
 *
 */
public class SpiceFeaturePanelListener 
implements FeatureViewListener, SelectedSeqPositionListener
{
    
    SPICEFrame parent ;
    /**
     * 
     */
    public SpiceFeaturePanelListener(SPICEFrame parent) {
        super();
        this.parent = parent;
    }
    
    
    public void selectedSeqRange(int start, int end) {
        System.out.println("selected " + start + " " + end);
    }
    
    public void selectedSeqPosition(int seqpos){
        System.out.println("selected seqpos " + seqpos );
    }
	public void mouseOverFeature(FeatureEvent e){
	    
	    Feature feat = (Feature) e.getSource();
	    System.out.println("mouse over feature " + feat);
	}
	
	public void mouseOverSegment(FeatureEvent e){
	    Segment seg = (Segment)e.getSource();
	    System.out.println("mouse over segment " + seg);
	}
	public void featureSelected(FeatureEvent e){
	    Feature feat = (Feature) e.getSource();
	    System.out.println("selected feature " + feat);
	}
	public void segmentSelected(FeatureEvent e){
	    Segment seg = (Segment)e.getSource();
	    System.out.println("selected segment " + seg);
	}

    
    
    
   
}
