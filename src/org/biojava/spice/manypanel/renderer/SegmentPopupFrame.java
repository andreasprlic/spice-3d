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

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
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

public class SegmentPopupFrame
extends AbstractPopupFrame    
implements SpiceFeatureListener, 
SequenceListener
{
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    Container content ;
    JPanel panel;
  
    private static final int FRAME_WIDTH  = 300;
    private static final int FRAME_PREF_HEIGHT = 250;
    private static final int FRAME_MAX_HEIGHT = 300;
    
    private static String font  =  "<b><font color=\"#0000FF\">";
    private static String efont = "</font></b>";
    private static String endl  = "<br>";
    private static String ahref = "<a href=\"";
    private static String ehref = "</a>";
    
    public SegmentPopupFrame() {
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
        if ( f == null)
            return;
        text.append(font + "feature: " + efont + f.getName()   + endl);
        text.append(font + "type: "   + efont +  f.getType()   + endl);
        text.append(font + "method: "  + efont + f.getMethod() + endl);
        
        if ( f.getNote() != null)
            text.append(font + "note: "  + efont + f.getNote() + endl);
        
        if ( f.getName() != null)
            text.append(font + "name: " + efont + f.getName() + endl);
        if ( f.getScore() != null)
            text.append(font + "score: " + efont + f.getScore()+ endl);
        
        if ( f.getLink() != null)
            text.append(font + "link: " + efont + ahref + f.getLink() + "\">" + f.getLink()  + ehref + endl);
        text.append(font + "source: " + efont + ahref  + f.getSource() + "\">"+ f.getSource()+ ehref + endl);
                
    }
    
    private Container createContent(Feature f){
        JPanel panel = new JPanel();
        panel.setBackground(Color.white);
        panel.setMaximumSize(new Dimension(FRAME_WIDTH,FRAME_MAX_HEIGHT));
        panel.setPreferredSize(new Dimension(FRAME_WIDTH,FRAME_PREF_HEIGHT));
        
       
        
        JEditorPane descriptionPane = new JEditorPane("text/html", "");
        descriptionPane.setMaximumSize(new Dimension(FRAME_WIDTH,FRAME_MAX_HEIGHT));
        descriptionPane.setPreferredSize(new Dimension(FRAME_WIDTH,FRAME_PREF_HEIGHT));
        panel.add(descriptionPane);
        
        StringBuffer text = startStringBuffer();
        appendFeatureDesc(text,f);        
        endStringBuffer(text);
        descriptionPane.setText(text.toString());
        addHyperLinkListener(descriptionPane);
     
        return panel;        
    }
    
    
    
    private Container createContent(Segment s){
        panel = new JPanel();
        panel.setMaximumSize(new Dimension(FRAME_WIDTH,FRAME_MAX_HEIGHT));
        panel.setPreferredSize(new Dimension(FRAME_WIDTH,FRAME_PREF_HEIGHT));
        panel.setBackground(Color.white);
        JEditorPane descriptionPane = new JEditorPane("text/html", "");
        descriptionPane.setMaximumSize(new Dimension(FRAME_WIDTH,FRAME_MAX_HEIGHT));
        descriptionPane.setPreferredSize(new Dimension(FRAME_WIDTH,FRAME_PREF_HEIGHT));
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
        //System.out.println(text.toString());
        descriptionPane.setText(text.toString());
        addHyperLinkListener(descriptionPane);
        
        return panel;        
    }
    
    
    private void addHyperLinkListener(JEditorPane descriptionPane){
        descriptionPane.setEditable(false);
        
        descriptionPane.addHyperlinkListener(
                new HyperlinkListener()
                {
                    public void hyperlinkUpdate(HyperlinkEvent e)
                    {                    
                        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            String href = e.getDescription();
                            BrowserOpener.showDocument(href);
                        }
                        // change the mouse curor
                        if ( e.getEventType() == HyperlinkEvent.EventType.ENTERED) {                           
                            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));                            
                        }
                        if (e.getEventType() == HyperlinkEvent.EventType.EXITED) { 
                            panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));                           
                        }
                    }
                });
        
    }
    
    public Container getContent() {
        return content;
    }
    
    public void mouseOverFeature(SpiceFeatureEvent e) {
        displayFrame();
     
        content =createContent(e.getFeature());
        repaint();
        
    }
    
    public void mouseOverSegment(SpiceFeatureEvent e) {
        displayFrame();
       
        content = createContent(e.getSegment());
        repaint();
        
    }
    
    public void featureSelected(SpiceFeatureEvent e) {
    }
    
    public void segmentSelected(SpiceFeatureEvent e) {
    }
    
    public void clearSelection() {
        markForHide();
    }
    
    public void selectedSeqPosition(int position) {           
        markForHide();        
    }
    
    public void selectedSeqRange(int start, int end) {
    }
    
    public void selectionLocked(boolean flag) {
    }
    
    public void newSequence(SequenceEvent e) {
    }
    
    public void newObjectRequested(String accessionCode) {
    }
    
    public void noObjectFound(String accessionCode) {
    }
    
}
