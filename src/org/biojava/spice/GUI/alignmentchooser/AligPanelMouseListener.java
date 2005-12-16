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

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JToolTip;

import org.biojava.bio.gui.sequence.SequencePanel;
import org.biojava.bio.gui.sequence.SequenceViewerEvent;
import org.biojava.bio.gui.sequence.SequenceViewerListener;
import org.biojava.bio.gui.sequence.SequenceViewerMotionListener;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.spice.manypanel.eventmodel.ObjectListener;
import org.biojava.bio.Annotation;
/**
 * @author Andreas Prlic
 *
 */
public class AligPanelMouseListener 
implements SequenceViewerListener,
        SequenceViewerMotionListener {

    
    //SPICEFrame spice;
    SequencePanel seqPanel;
    JToolTip tooltip;
    List objectListeners;
    int cursortype ;
    String tooltiptext;
    
    public AligPanelMouseListener(SequencePanel srcxt){
        cursortype = 0;
        seqPanel = srcxt;
        tooltip = seqPanel.createToolTip();
        tooltip.setEnabled(true);
        tooltiptext = "";
        clearObjectListeners();        
    }
    public void clearObjectListeners(){
        objectListeners = new ArrayList();
    }
    
    public void addObjectListener(ObjectListener li){
        objectListeners.add(li);
    }
    
    public void mouseReleased(SequenceViewerEvent ewt){
         
        //int pos = ewt.getPos();
        //int seqpos = seqPanel.graphicsToSequence(pos);
        //System.out.println("seqpos "+seqpos);
        Object target = ewt.getTarget();
        if ( target instanceof FeatureHolder ){
            FeatureHolder fh = (FeatureHolder) target;
            //System.out.println("simple fh" + fh);
            java.util.Iterator iter = fh.features();
            Feature f = null;
            while (iter.hasNext()){
                f = (Feature)iter.next();
                //System.out.println(f);
                String pdbcode = f.getSource();
                //System.out.println("from PDB " + pdbcode);
                
                //System.out.println("loading PDB " + pdbcode.substring(0,4));               
                
                triggerNewObjectRequested(pdbcode);
                return;
            }
        }
    }
    
    public void triggerNewObjectRequested(String code){
        Iterator iter = objectListeners.iterator();
        while (iter.hasNext()){
            ObjectListener li =  (ObjectListener)iter.next();
            li.newObjectRequested(code);
        }
    }
    
    public void mousePressed(SequenceViewerEvent ewt){
        
    }
    
    public void mouseClicked(SequenceViewerEvent ewt){
        
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
                //System.out.println(f);
                String pdbcode = f.getSource();
                //System.out.println("from PDB " + pdbcode);
                Annotation anno = f.getAnnotation();
                String description = "";
                if ( anno.containsProperty("description")){
                    description = (String) anno.getProperty("description");
                }
                String newtxt = "load PDB " + pdbcode  + description;
                if (! tooltiptext.equals(newtxt)) {
                    //System.out.println("setting new text " + newtxt);
                    seqPanel.setToolTipText(newtxt);
                    tooltiptext = newtxt;
                }
                // change mouse cursor ...
                if ( cursortype == 0) {
                    seqPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    cursortype = 1;
                }
            }
            if ( f == null){
                seqPanel.setToolTipText(null);
                tooltiptext = "";
                if ( cursortype == 1) {
                    seqPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    cursortype = 0;
                }
            }
        }
    }
    
   
    

}
