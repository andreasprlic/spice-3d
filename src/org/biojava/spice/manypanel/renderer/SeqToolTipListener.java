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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.dasobert.eventmodel.SequenceEvent;
import org.biojava.dasobert.eventmodel.SequenceListener;
import org.biojava.spice.feature.Feature;
import org.biojava.spice.feature.Segment;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureEvent;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureListener;


/** a class that displays a tooltip if mouse over a feature.
 * it also copies the current selection to the clipboard
 * 
 * @author Andreas Prlic
 *
 */
public class SeqToolTipListener
implements SequenceListener, SpiceFeatureListener {

    JComponent parent;
    int length;
    String sequence;
    int oldpos ;
    ToolTipManager toolM;
    Chain chain;
    Clipboard clipboard;
    
    
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
        //toolM.setL
        chain = new ChainImpl();
        
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        
    }

    public void newSequence(SequenceEvent e) {
       
        
    }
    
    public void setChain(Chain c){
        sequence = c.getSequence();
        length = sequence.length();
        this.chain = c;
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
        String s = "" ;
        Component where = getRenderer();
        
        //System.out.println(where);
        
        if (where instanceof SequenceRenderer ) {
            s = ""+(position+1) + " " + sequence.substring(position,position+1);
        } else {
            Group g1 = chain.getGroup(position);
            s = "Seq:" + (position+1) +" PDB:" + g1.getPDBCode() + " " + sequence.substring(position,position+1);
            
        }
        parent.setToolTipText(s);
    }
    
    private Component getRenderer(){
        return parent.getParent().getParent().getParent();
    }

    /** stores the text in the clipboard 
     * 
     */
    public void selectedSeqRange(int start, int end) {
        if (( start >= sequence.length()) || 
                (end >= sequence.length())) {
            // requested wrong range...
            return;
        }
        if  ( ( start < 0) || ( end < 0))
            // requested wrong range
            return;
        
        
        String seq = sequence.substring(start, end+1);
        
        Transferable transferText = new StringSelection(seq);
        clipboard.setContents(transferText,null);
        
    }

    public void selectionLocked(boolean flag) {
       
        
    }

    public void newObjectRequested(String accessionCode) {}
    public void noObjectFound(String accessionCode){
        sequence = "";
        length = 0;
    }
    
    public void featureSelected(SpiceFeatureEvent e) {
        //logger.info("toolTipper feature selected ");
        Feature f = e.getFeature();
        String s = s = f.toString();        
        parent.setToolTipText(s);
            
        
    }

    
    
    public void clearSelection() {
        parent.setToolTipText("");
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
            Component where = getRenderer();
            String txt = "";
            if ( where instanceof SequenceRenderer) {
                txt = s.toString();
            } else {
                
                String name = s.getName();
                String note = s.getNote();
                int start = s.getStart();
                int end = s.getEnd();
                Group gs = chain.getGroup(start-1);
                Group ge = chain.getGroup(end-1);
                
                String str = "Segment: " +name + " Seq:" +start + "-" + end +" PDB:" + gs.getPDBCode() + "-"+ge.getPDBCode();
                if ( ( note != null ) && ( ! note.equals("null")))
                    if ( note.length() >40)
                        str += note.substring(0,39)+"...";
                    else
                        str += note;
                txt = str;
                
            }
            parent.setToolTipText(txt);
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        
        
    }

    public void segmentSelected(SpiceFeatureEvent e) {
        parent.setToolTipText(e.getSegment().toString());
        
    }

   
    
    
}
