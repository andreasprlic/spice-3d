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
 * Created on Jul 28, 2006
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.Container;
import java.awt.Dimension;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.biojava.dasobert.eventmodel.SequenceEvent;
import org.biojava.dasobert.eventmodel.SequenceListener;
import org.biojava.spice.feature.Feature;
import org.biojava.spice.feature.Segment;
import org.biojava.spice.gui.AbstractPopupFrame;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureEvent;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureListener;
import org.biojava.spice.utils.BrowserOpener;

public class SegmentDisplayFloatingFrame
extends AbstractPopupFrame    
implements SpiceFeatureListener, 
SequenceListener
{
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    Container content ;
    
    private static String font  = "<font color=\"#0000FF\">";
    private static String efont = "</font>";
    private static String endl  = "<br>";
    
    public SegmentDisplayFloatingFrame() {
        super();
        content = null;
        
    }
    
    
    private StringBuffer startStringBuffer(){
        StringBuffer text = new StringBuffer(
        "<html><body><font size=\"2\" face=\"Verdana, Arial, Helvetica, sans-serif\">");
        return text;
    }
    
    private void endStringBuffer(StringBuffer text){
        text.append("</font></body></html>");
    }
    
    private void appendFeatureDesc(StringBuffer text, Feature f){
        text.append(font + "feature: " + efont + f.getName()   + endl);
        text.append(font + "type: "   + efont +  f.getType()   + endl);
        text.append(font + "method: "  + efont + f.getMethod() + endl);
        
        if ( f.getNote() != null)
            text.append(font + "note: "  + efont + f.getNote() + endl);
        
        if ( f.getName() != null)
            text.append(font + "name: " + efont + f.getName() + endl);
        if ( f.getScore() != null)
            text.append(font + "score: " + efont + f.getScore()+ endl);
        
        
    }
    
    private Container createContent(Feature f){
        JPanel panel = new JPanel();
        
        JEditorPane descriptionPane = new JEditorPane("text/html", "");
        descriptionPane.setMaximumSize(new Dimension(200,200));
        panel.add(descriptionPane);
        
        StringBuffer text = startStringBuffer();
        appendFeatureDesc(text,f);        
        endStringBuffer(text);
        descriptionPane.setText(text.toString());
        return panel;
        
    }
    
    
    
    private Container createContent(Segment s){
        JPanel panel = new JPanel();
        
        JEditorPane descriptionPane = new JEditorPane("text/html", "");
        descriptionPane.setMaximumSize(new Dimension(200,200));
        panel.add(descriptionPane);
        
        StringBuffer text = startStringBuffer();
        
        String name = s.getName();
        String note = s.getNote();        
        int start = s.getStart();
        int end = s.getEnd();
        
        text.append(font + "segment:"+ efont + name + endl);
        text.append(font + "start:"  + efont + start+ font + " end: " + efont + end +endl);        
        if ( note != null)
            text.append(font + "note: "  + efont + note +endl);
        
        Feature f = s.getParent();
        appendFeatureDesc(text,f);
        endStringBuffer(text); 
        descriptionPane.setText(text.toString());
        
        descriptionPane.addHyperlinkListener(new HyperlinkListener()
                {
                  public void hyperlinkUpdate(HyperlinkEvent e)
                  {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        String href = e.getDescription();
                        BrowserOpener.showDocument(href);
                    }
                  }
                });
     
        
        return panel;
        
    }
    
    
    
    public Container getContent() {
        return content;
    }
    
    public void mouseOverFeature(SpiceFeatureEvent e) {
        displayFrame();
        
        
        //System.out.println("floating frame : overFeature");
        Container c =createContent(e.getFeature());
        
        content =c ;
        repaint();
        
    }
    
    public void mouseOverSegment(SpiceFeatureEvent e) {
        displayFrame();
        
        
       // System.out.println("floating frame : overSegment");
        //Component c = new JLabel("over a segment");
        Container c = createContent(e.getSegment());
        content = c;
        repaint();
        
    }
    
    public void featureSelected(SpiceFeatureEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    public void segmentSelected(SpiceFeatureEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    public void clearSelection() {
       // System.out.println("clear selection");
        markForHide();
    }
    
    public void selectedSeqPosition(int position) {        
        //disposeFrame();
        //System.out.println("selected seq position "+ position);
        markForHide();
        //repaint();
    }
    
    public void selectedSeqRange(int start, int end) {
        // TODO Auto-generated method stub
        
    }
    
    public void selectionLocked(boolean flag) {
        // TODO Auto-generated method stub
        
    }
    
    public void newSequence(SequenceEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    public void newObjectRequested(String accessionCode) {
        // TODO Auto-generated method stub
        
    }
    
    public void noObjectFound(String accessionCode) {
        // TODO Auto-generated method stub
        
    }
    
}
