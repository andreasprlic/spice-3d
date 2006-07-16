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
 * Created on Jun 19, 2005
 *
 */
package org.biojava.spice.GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;
import org.biojava.spice.JNLPProxy;


/**
 * @author Andreas Prlic
 *
 */
public class DasSourceDialog
extends JDialog{
    
    private static final long serialVersionUID = 8273923712341234123L;
    
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    SpiceDasSource dasSource;
 
    static int H_SIZE = 750;
    static int V_SIZE = 600;
    JEditorPane txt;
    /**
     * 
     */
    public DasSourceDialog( SpiceDasSource dasSource) {
        super();
        this.dasSource = dasSource;
      
        this.setSize(new Dimension(H_SIZE, V_SIZE)) ;
        
        
        String htmlText = getHTMLText(dasSource);
        txt = new JEditorPane("text/html", htmlText);
        
        txt.setEditable(false);
        
        txt.addHyperlinkListener(new HyperlinkListener(){
            public void hyperlinkUpdate(HyperlinkEvent e) {
                //System.out.println(e);
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    String href = e.getDescription();
                    showDocument(href);
                }
            }
        });
        JScrollPane scroll = new JScrollPane(txt);
        //scroll.setPreferredSize(new Dimension(H_SIZE, V_SIZE-50)) ;
        //scroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //JPanel p = new JPanel();
        //p.add("Center",scroll);
        //p.add(txt);
        //p.add("Sourth",new Button("Close"));
        //p.add(new Button("Help"));
        
        Box vBox = Box.createVerticalBox();
        vBox.add(scroll);
        
        
        JButton close = new JButton("Close");
        
        
        close.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        
        
        Box hBoxb = Box.createHorizontalBox();
        hBoxb.add(Box.createGlue());
        hBoxb.add(close,BorderLayout.EAST);
        
        vBox.add(hBoxb);
        
        //add("South", p);
        this.getContentPane().add(vBox);
        
    }
    
    public boolean showDocument(URL url) 
    {
        if ( url != null ){
            boolean success = JNLPProxy.showDocument(url); 
            if ( ! success)
                logger.info("could not open URL "+url+" in browser. check your config or browser version.");
        return success;
        
        }
        else
            return false;
    }
    
    public boolean showDocument(String urlstring){
        try{
            URL url = new URL(urlstring);
            
            return showDocument(url);
        } catch (MalformedURLException e){
            logger.warning("malformed URL "+urlstring);
            return false;
        }
    }
    
    
    private String getHTMLText(SpiceDasSource ds){
        
        String txt = "";
        
        txt += "<html><body>";
        
        txt += "<h2> DasSource " + ds.getNickname() +" details</h2>";
        txt += "<table>";
        txt += "<tr><td>nickname</td><td><b>"+ds.getNickname()+"</b></td></tr>";
        txt += "<tr><td colspan=\"2\">"+ds.getUrl()+"</td></tr>";        
        txt += "<tr><td>unique id</td><td>" + ds.getId()+"</td></tr>";
        txt += "<tr><td> description</td><td>" + ds.getDescription()+"</td></tr>";
        txt += "<tr><td>admin email</td><td><a href=\"mailto:"+ds.getAdminemail()+"\">"+ds.getAdminemail()+"</a></td></tr>";
        txt += "<tr><td>helperurl</td><td>";
        String helperurl = ds.getHelperurl();
        if (( helperurl != null) && ( ! helperurl.equals(""))) {
            txt += "<a href=\""+helperurl+"\">"+helperurl+"</a>";

        }
        txt += "</td></tr>";
        
        //txt += "<tr><td>testcode</td><td>"+ds.getTestCode()+"</td></tr>";
        
        txt += "<tr><td>coordinate systems</td><td>";
        // coordinate systems
        DasCoordinateSystem[] coords = ds.getCoordinateSystem();
       String testCode = "";
        for (int i = 0 ; i< coords.length;i++){
            txt += coords[i].toString() + " testCode: " + coords[i].getTestCode() ;
            testCode = coords[i].getTestCode();
        }
        txt+="</td></tr>";
        
        txt += "<tr<td>capabilities</td><td>";
        
       
        
        // capabilities;
       
        String[] caps = ds.getCapabilities();
        for (int i = 0 ; i< caps.length;i++) {
            if (( testCode == null) || ( testCode.equals(""))) {
                txt += caps[i] + " ";
                continue;
            }
            
            String cmd = ds.getUrl();
            String capability = caps[i];
            if ( capability.equals("sequence") || 
                    capability.equals("features")) {
                cmd += capability +"?segment="+testCode;
            } else if ( capability.equals("alignment") ||
                    capability.equals("structure") ){
                cmd += capability+"?query="+testCode;
            }
            
            else {
               txt+= caps[i] + " ";
               continue;
            }
            
            txt+= "<a href=\""+cmd+"\">"+capability+"</a> ";
        }
        txt +="</td></tr>";
        
        txt += "<tr><td>registered at</td><td>"+ds.getRegisterDate()+"</td></tr>";
        txt += "<tr><td>last tested successfully at</td><td>"+ds.getLeaseDate()+"</td></tr>";
        txt += "<tr><td>labels</td>";
        String[] labels = ds.getLabels();
        for (int i=0;i< labels.length;i++ ){
            txt += labels[i] + " ";
        }
        txt += "</td></tr>";
            
        txt += "</table>";
        txt += "<p>";
        txt += "<a href=\"http://das.sanger.ac.uk/registry/validateServer.jsp?auto_id="+ds.getId()+"\">validate</a> this DasSource.";
        txt += "</p><p>";
        txt += "<a href=\"http://das.sanger.ac.uk/registry/sendToFriend.jsp?auto_id="+ds.getId()+"\">send</a> this DasSource to a friend.";
        txt += "</p>";
        txt += "</body></html>";
        
        
        return txt;
            
            
            
        }
    }
