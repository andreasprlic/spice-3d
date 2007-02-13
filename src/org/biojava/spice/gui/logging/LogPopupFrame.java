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
 * Created on Oct 24, 2006
 *
 */
package org.biojava.spice.gui.logging;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.LogRecord;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.biojava.spice.gui.AbstractPopupFrame;
import org.biojava.spice.manypanel.renderer.SegmentPopupFrame;

public class LogPopupFrame extends AbstractPopupFrame{


    JEditorPane editorP;
    
    protected final DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
    
    public LogPopupFrame() {
        super();
        editorP =  new JEditorPane("text/html", "");
        
        
    }
    public LogPopupFrame(LogRecord record) {
            super();

        String html = record2HTML(record);
        editorP.setText(html);
        editorP.setEditable(false);
        editorP.repaint();
        
        
    }
    
    public StringBuffer startStringBuffer(){
        StringBuffer text = new StringBuffer(
        "<html><body><font size=\"2\" face=\"Verdana, Arial, Helvetica, sans-serif\">");
        return text;
    }
    
    
    public void endStringBuffer(StringBuffer text){
        text.append("</font></body></html>");
    }
    
    
    private String protect(String html){
        html = html.replaceAll("<","&lt;");
        html = html.replaceAll(">","&gt;");
            
        return html;
    }
    
    public void record2HTMLBody(LogRecord rec, StringBuffer buf){
        buf.append("<b>Class:</b> "   +rec.getSourceClassName()+"<br>");
        buf.append("<b>Method:</b> "  +rec.getSourceMethodName()+"<br>");
        buf.append("<b>Message:</b> " +rec.getMessage()+"<br>");
        buf.append("<b>Time:</b> "    + dateFormat.format(new Date(rec.getMillis())) + "<br>");
        
        if ( rec.getThrown() != null) {
        
            buf.append("<b>Trace:</b><br><font size=\"1\">");
                   
            StackTraceElement[] stack = rec.getThrown().getStackTrace();
            
            buf.append("stack length: " + stack.length +"<br>");
            
            for (int i=0 ; i< stack.length ; i++ ) {
                StackTraceElement e = stack[i];
                buf.append(
                        "at " + protect(e.getClassName())   +
                        "."   +  protect(e.getMethodName()) +
                        "("   + protect(e.getFileName())    + 
                        ":"   + e.getLineNumber() +")"      +                            
                        " <br>");
            }
        
            buf.append("</font><br>");
        }
    }
    
    public String record2HTML(LogRecord rec){
        StringBuffer buf =  startStringBuffer();
       
        record2HTMLBody(rec, buf);
        
        endStringBuffer(buf);
        
        
        String s = buf.toString();
      
        return s;
    }

    public Container getContent() {
     
        
        JPanel panel = new JPanel();
        panel.setBackground(Color.white);
        panel.setBorder(BorderFactory.createEmptyBorder());
        
        panel.setPreferredSize(new Dimension(SegmentPopupFrame.FRAME_WIDTH,SegmentPopupFrame.FRAME_PREF_HEIGHT));
        editorP.setPreferredSize(new Dimension(SegmentPopupFrame.FRAME_WIDTH,SegmentPopupFrame.FRAME_PREF_HEIGHT));
        
        JScrollPane scroll = new JScrollPane(editorP);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        
        
        panel.add(scroll);
        panel.repaint();
        return panel;
        
        
    }

}
