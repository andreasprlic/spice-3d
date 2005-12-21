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
 * Created on Dec 21, 2005
 *
 */
package org.biojava.spice.manypanel.managers;

import org.biojava.bio.program.das.dasalignment.Alignment;
import org.biojava.spice.manypanel.eventmodel.AlignmentEvent;
import org.biojava.spice.manypanel.eventmodel.AlignmentListener;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureEvent;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureListener;


/** a class that converts a SpiceFeatureEvent from one
 * coordinate system into another
 * 
 * @author Andreas Prlic
 *
 */
public class SpiceFeatureEventTranslator 
implements AlignmentListener
{

    Alignment alignment;
    
    SpiceFeatureListener seq1Listener;
    SpiceFeatureListener seq2Listener;
    
    public SpiceFeatureEventTranslator() {
        super();
        
       clearAlignment();
       
       seq1Listener = new MyFeatureListener(1,this);
       seq2Listener = new MyFeatureListener(2,this);

    }

    public void clearAlignment() {
        alignment = new Alignment();
        
    }

    public void newAlignment(AlignmentEvent e) {
        alignment = e.getAlignment();
        
    }

    public void noAlignmentFound(AlignmentEvent e) {
       clearAlignment();
        
    }
    
    public void addSeq1FeatureListener(SpiceFeatureListener li){
        
    }
    
    public void addSeq2FeatureListener(SpiceFeatureListener li){
        
    }

}
// TODO:
// FInish implmementation of this class !!!

class MyFeatureListener implements SpiceFeatureListener {
    
    int pos;
    SpiceFeatureEventTranslator parent;
    
    public MyFeatureListener(int nr, SpiceFeatureEventTranslator parent){
        this.parent = parent;
    }
    public void clearSelection() {
        
        
    }

    public void featureSelected(SpiceFeatureEvent e) {
       
        
    }

    public void mouseOverFeature(SpiceFeatureEvent e) {
      
        
    }

    public void mouseOverSegment(SpiceFeatureEvent e) {
   
        
    }

    public void segmentSelected(SpiceFeatureEvent e) {
        
        
    }
    
}


