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

    LogRecord record ;
    JEditorPane editorP;
    
    protected final DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
    
    public LogPopupFrame(LogRecord record) {
        super();
        this.record = record;

        editorP =  new JEditorPane("text/html", "");
        
        String html = record2HTML(record);
        editorP.setText(html);
        
        
    }
    
    private StringBuffer startStringBuffer(){
        StringBuffer text = new StringBuffer(
        "<html><body><font size=\"2\" face=\"Verdana, Arial, Helvetica, sans-serif\">");
        return text;
    }
    
    private void endStringBuffer(StringBuffer text){
        text.append("</font></body></html>");
    }
    
    private String record2HTML(LogRecord record){
        StringBuffer buf =  startStringBuffer();
        
        buf.append("<b>Class:</b> "+record.getSourceClassName()+"<br>");
        buf.append("<b>Method:</b> "+record.getSourceMethodName()+"<br>");
        buf.append("<b>Message:</b> "+record.getMessage()+"<br>");
        buf.append("<b>Time:</b> " + dateFormat.format(new Date(record.getMillis())) + "<br>");
        if ( record.getThrown() != null) {
            buf.append("<b>Trace:</b> ");
        
            buf.append("<pre>");
        
            buf.append(record.getThrown().getStackTrace());
        
            buf.append("</pre><br>");
        }
        
        endStringBuffer(buf);
        
        
        return buf.toString();
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
        
        return panel;
        
        
    }

}
