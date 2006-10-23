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
 * Created on May 10, 2005
 *
 */
package org.biojava.spice.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.spice.ResourceManager;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.gui.ConfigPanel;

/**
 * @author Andreas Prlic
 *
 */
public class ConfigGui {
    SpiceApplication spice;
    RegistryConfiguration config ;
  
    
    /* a configuration window
     * 
     * @param spicef the SpiceApplication
     */
    public ConfigGui(SpiceApplication spicef) {
        super();
        
        spice =spicef;
        config = spice.getConfiguration();
    }
     
    public void showConfigFrame(){
        //RegistryConfiguration config = spice.getConfiguration();
        
        //Create and set up the window.
        JFrame frame = new JFrame("SPICE configuration window");
        frame.setVisible(false);
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        //Make sure we have the standard desktop window decorations.
        JFrame.setDefaultLookAndFeelDecorated(false);
        ImageIcon icon = SpiceApplication.createImageIcon("spice16x16.gif");
        if ( icon != null)
            frame.setIconImage(icon.getImage());
        
      
        ConfigPanel tpd = new ConfigPanel(spice,config);
        tpd.setLayout(new BoxLayout(tpd,BoxLayout.X_AXIS));
        
        Box vbox = Box.createVerticalBox();
        vbox.add(tpd);
        
        
        JButton saveb   = new JButton(ResourceManager.getString("org.biojava.spice.action.save"));
        JButton cancelb = new JButton(ResourceManager.getString("org.biojava.spice.action.close"));
        
        saveb.addActionListener(   new ButtonListener(frame, tpd) );
        cancelb.addActionListener( new ButtonListener(frame, tpd) );
        
        //frame.getContentPane().add(saveb);
        //frame.getContentPane().add(cancelb);
        Box hbox = Box.createHorizontalBox();
        hbox.add(saveb);
        hbox.add(cancelb);
        
        vbox.add(hbox);
        
        //Display the window.
        frame.getContentPane().add(vbox);
        frame.pack();
        frame.setVisible(true);
    }
    
    /** set status of server
     * 
     * @param url
     * @param flag
     */
    public void setServerStatus(String url, Boolean flag) {
        // browse through config and set status of server
        List servers =  config.getAllServers();
        for (int i = 0 ; i < servers.size(); i++) {
            Map s = (Map)servers.get(i) ;
            SpiceDasSource ds = (SpiceDasSource) s.get("server");
            String surl = ds.getUrl();
            if ( surl.equals(url) ) {
                boolean f = flag.booleanValue();
                config.setStatus(i,f);
            }	    
        }
    }
    
}



class ButtonListener
implements ActionListener

{
    JFrame parent ;
    ConfigPanel configpane ;
    
    public ButtonListener( JFrame parent_,ConfigPanel tpd) {
        parent = parent_ ;
        configpane = tpd ;
    }
    
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
   
        if ( cmd.equals(ResourceManager.getString("org.biojava.spice.action.close"))) {
            parent.dispose();
        } else  if (cmd.equals(ResourceManager.getString("org.biojava.spice.action.save"))) {
            
            configpane.saveConfiguration();
            parent.dispose();
            
        }
    }
}




