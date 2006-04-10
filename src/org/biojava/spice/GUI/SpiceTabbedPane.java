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
 * Created on Apr 9, 2006
 *
 */
package org.biojava.spice.GUI;


import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.biojava.spice.SPICEFrame;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.server.SpiceServer;

public class SpiceTabbedPane extends CloseableTabbedPane 
implements WindowListener{
    
    SpiceServer server;
    
    static final long serialVersionUID = 7893248790189123l;
    static ImageIcon spiceIcon = SpiceApplication.createImageIcon("spice16x16.gif");
    static ImageIcon delTabIcon = SpiceApplication.createImageIcon("editdelete.png");
    
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    
    List tabbedSpices;
    
    final JFrame frame;
    
    public SpiceTabbedPane(SpiceServer server){
        super(delTabIcon);
        this.server = server;
        tabbedSpices = new ArrayList();
        
        frame = new JFrame("SPICE");
        JFrame.setDefaultLookAndFeelDecorated(false);
        
        
    }
        
    public SpiceTabbedPane(SpiceServer server,  SpiceApplication spice) {
        this(server);
         
        JMenuBar menu = spice.getMenu();
        frame.setJMenuBar(menu);
        
        
        if ( spiceIcon != null)
            frame.setIconImage(spiceIcon.getImage());
        frame.pack();
        
        frame.getContentPane().add(spice);
        frame.pack();
        frame.setVisible(true);
        
        tabbedSpices.add(spice);
        spice.setSpiceTabbedPane(this);
        server.registerInstance(spice);
        spice.setSpiceServer(server);
        
        frame.addWindowListener(this);
        
        this.addTabListener(new TabListener(){
            
            public void tabSelected(TabEvent e){
                int i = e.getTabNumber();
                //logger.info("selected new tab " + i);
                SpiceApplication spice = (SpiceApplication)tabbedSpices.get(i);
              
                // make sure the menu is linked to the current active spice
                JMenuBar menu = spice.getMenu();                
                frame.setJMenuBar(menu);
            }
            
            public void tabClosed(TabEvent e) {
                
                int i = e.getTabNumber();
                //logger.info("got close tab event for tab " + i);
                removeSPICEAt(i);
                
            }
            
        });
    }
    
    private void removeSPICEAt(int tabnr){
        SpiceApplication sp = (SpiceApplication)tabbedSpices.get(tabnr);
        removeSPICE(sp);
    }
    
    private void addTab(SpiceApplication spice){
        
        addTab("SPICE",spice,true);
        
    }
    
    public void addSpice(SPICEFrame spice){
        //logger.info("adding a new spice instance.");
        
        if ( tabbedSpices.contains(spice)){
            //logger.info("known instance");
            return;
        }
        
        SpiceApplication sp = (SpiceApplication)spice;
        
        //logger.info(" adding new before add: #spices in tab: " + tabbedSpices.size());
        
        if ( tabbedSpices.size() == 1){
            
            SpiceApplication sp1 = (SpiceApplication) tabbedSpices.get(0);
            Container conti = sp1.getTopLevelAncestor();
            //Container conti = this.getTopLevelAncestor();
            
            
            if ( conti instanceof JFrame) {
                //logger.info("topancestor is Jframe");
                JFrame frame = (JFrame) conti;
                frame.remove(sp1);
                frame.getContentPane().add(this);
                frame.repaint();
            } else {
                //logger.info("topancesot is not jframe " + conti);
            }
            addTab(sp1);
        }                                    
        
        addTab(sp);
        
        // make sure the menu is linked to the current active spice
        JMenuBar menu = sp.getMenu();                
        frame.setJMenuBar(menu);
        
        this.repaint();
        
        
        server.registerInstance(spice);        
        tabbedSpices.add(spice);
        spice.setSpiceTabbedPane(this);
    }
    
    public void removeSPICE(SPICEFrame spice){
        //System.out.println("removing spice " + spice.getPDBCode() + " tabbedSpices length:" + tabbedSpices.size());
        
        if ( ! tabbedSpices.contains(spice))
            return;
        //logger.info("rm check passed");
        
        SpiceApplication sp = (SpiceApplication)spice;
        //Container conti = this.getTopLevelAncestor();
        
        server.removeInstance(spice);
        this.remove(sp);
        tabbedSpices.remove(spice);
        
        if ( tabbedSpices.size() == 0 ){
                        
            frame.remove(sp);
            frame.dispose();                            
        }        
        
        if ( tabbedSpices.size()==1){                      
                frame.remove(this);
                SpiceApplication sp1 = (SpiceApplication) tabbedSpices.get(0);
                frame.getContentPane().add(sp1);
                frame.repaint();            
        }
        //logger.info("after rm tabbedSpices: " + tabbedSpices.size());
        this.repaint();
        
        
    }
    
    
    private void removeAllSPICEs(){
        //logger.info("close all spices in tab (total:" + tabbedSpices.size()+")");
        
       SPICEFrame[] spices = (SPICEFrame[]) tabbedSpices.toArray(new SpiceApplication[tabbedSpices.size()]);

       for (int i=0;i<spices.length;i++){
           
            //logger.info("closing " + i);
            SPICEFrame spice = spices[i];
            removeSPICE(spice);
        }          
    }
    
    public void windowDeiconified(WindowEvent e){}
    public void windowIconified(WindowEvent e){}
    public void windowActivated(WindowEvent e){}
    public void windowDeactivated(WindowEvent e){}
    public void windowOpened(WindowEvent e){}
    public void windowClosing(WindowEvent e){
        //logger.info("closing SPICE window");
        this.removeAllSPICEs();
    }
    
    public void windowClosed(WindowEvent e){}
    
}
