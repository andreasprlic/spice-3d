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
package org.biojava.spice.gui;


import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.biojava.dasobert.eventmodel.SequenceEvent;
import org.biojava.dasobert.eventmodel.SequenceListener;
import org.biojava.dasobert.eventmodel.StructureEvent;
import org.biojava.dasobert.eventmodel.StructureListener;
import org.biojava.spice.SPICEFrame;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.manypanel.BrowserPane;
import org.biojava.spice.server.SpiceServer;

public class SpiceTabbedPane extends CloseableTabbedPane 
implements WindowListener{
    
    SpiceServer server;
    
    static final long serialVersionUID = 7893248790189123l;
    static final ImageIcon spiceIcon = SpiceApplication.createImageIcon("spice16x16.gif");
    static final ImageIcon delTabIcon = SpiceApplication.createImageIcon("editdelete.png");
    
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    
    List tabbedSpices;
    
    JFrame frame;
    TabEventListener tabEventListener;
    
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
        frame.toFront();
        frame.setVisible(true);
        
        tabbedSpices.add(spice);
        spice.setSpiceTabbedPane(this);
        server.registerInstance(spice);
        spice.setSpiceServer(server);
        registerSpiceListeners(spice);
        setFrameTitle(spice);
        
        frame.addWindowListener(this);
        
        this.addTabListener(new TabListener(){
            
            public void tabSelected(TabEvent e){
                int i = e.getTabNumber();
                //logger.info("selected new tab " + i);
                SpiceApplication spice = (SpiceApplication)tabbedSpices.get(i);
                
                // make sure the menu is linked to the current active spice
                JMenuBar menu = spice.getMenu();                
                frame.setJMenuBar(menu);
                
                setFrameTitle(spice);
                
            }
            
            public void tabClosing(TabEvent e) {
                
                //int i = e.getTabNumber();
                logger.info("got close tab event for tab " + e.getTabNumber());
                SpiceApplication s= (SpiceApplication) e.getComponent(); 
                removeSPICE(s);
                
            }
            public void tabClosed(TabEvent e){
                logger.info("got tab closed event");
               
            }
            
        });
    }
    
    public void setFrameTitle(String text){
        frame.setTitle(text);
        frame.repaint();
    }
    
    protected void setFrameTitle(SPICEFrame spice){
        String tabText = getSpiceText(spice);
        String txt = "SPICE";
        if ( ! (tabText.equals(""))){
            txt += " - " + tabText;
        }
        setFrameTitle(txt);
        
    }
    
    private void registerSpiceListeners(SPICEFrame spice){
        BrowserPane seqDisp = spice.getBrowserPane();
        
        tabEventListener = new TabEventListener(spice,this);
        
        seqDisp.addStructureListener(tabEventListener);
        seqDisp.addUniProtSequenceListener(tabEventListener);
        seqDisp.addEnspSequenceListener(tabEventListener);
    }
    
    private void deregisterSpiceListeners(SPICEFrame spice){
        BrowserPane seqDisp = spice.getBrowserPane();
        
        seqDisp.removeStructureListener(tabEventListener);
        seqDisp.removeUniProtSequenceListener(tabEventListener);
        seqDisp.removeEnspSequenceListener(tabEventListener);
        
        seqDisp.clearListeners();
    }
    
    public int getTabForSpice(SPICEFrame spice){
        return tabbedSpices.indexOf(spice);
    }
    
    public int getNumberSpices(){
        return tabbedSpices.size();
    }
    
    public JFrame getMainFrame(){
        return frame;
    }
    //private void removeSPICEAt(int tabnr){
      //  removeTabAt(tabnr);
        //SpiceApplication sp = (SpiceApplication)tabbedSpices.get(tabnr);
        //removeSPICE(sp);
    //}
    
    private void addTab(SpiceApplication spice){
        String tabText = getSpiceText(spice);
        if ( tabText.equals(""))
            tabText = " - ";
        addTab(tabText,spice,true);
        setFrameTitle(spice);
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
        
        registerSpiceListeners(spice);
    }
    
    public void removeSPICE(SPICEFrame spice){
        System.out.println("removing spice " + spice.getPDBCode() + " tabbedSpices length:" + tabbedSpices.size());
        
        if ( ! tabbedSpices.contains(spice)) {
            logger.warning("did not find spice instance in list, can not remove!");
            return;
        }
        //logger.info("rm check passed");
        
        SpiceApplication sp = (SpiceApplication)spice;
        //Container conti = this.getTopLevelAncestor();
        
        deregisterSpiceListeners(spice);
        server.removeInstance(spice);
        tabbedSpices.remove(spice);
        int pos = getSelectedIndex();        
        //this.remove(sp);
        if ( pos > -1 )
            removeTabAt(pos);
         
            
        sp.newConfigRetrieved(null);
        sp.setSpiceServer(null);
        sp.setSpiceTabbedPane(null);
        sp.setMenu(null);
        sp.clearListeners();
        
        if ( tabbedSpices.size() == 0 ){
            
            frame.remove(sp);
            frame.dispose();                            
        }        
        
        if ( tabbedSpices.size()==1){                      
            frame.remove(this);
            SpiceApplication sp1 = (SpiceApplication) tabbedSpices.get(0);
            frame.getContentPane().add(sp1);
            frame.repaint();    
            JMenuBar menu = sp1.getMenu();
            frame.setJMenuBar(menu);
            setFrameTitle(sp1);
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
    
    protected String getSpiceText(SPICEFrame spice){
        
        String pdb = spice.getPDBCode();
        String up = spice.getUniProtCode();
        String ensp = spice.getENSPCode();
        String txt = "";
        
        if ( ! (pdb.equals(""))) 
            txt += pdb;
        
        if ( ! ( pdb.equals("") && up.equals("")))
            txt += " - ";
        
        if ( ! (up.equals("")))
            txt += up;
        
        if ( ! (up.equals("") && ensp.equals("")))
            txt += " - "  ;
        
        if ( ! (ensp.equals("")))
            txt += ensp;
        
        return txt;
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

class TabEventListener 
implements StructureListener, 
SequenceListener
{
    
    SPICEFrame spice;
    SpiceTabbedPane tabPane;
    
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    
    
    public TabEventListener(SPICEFrame spice, SpiceTabbedPane tabPane) {
        this.tabPane = tabPane;
        this.spice = spice;
    }       
    
   
    
    public void newStructure(StructureEvent event) {
        //logger.info("got new structure");
        int i =  tabPane.getTabForSpice(spice);
        
        if ( tabPane.getTabCount() > 0)
            tabPane.setTitleAt(i,tabPane.getSpiceText(spice));
        tabPane.setFrameTitle(spice);
    }
    
    public void selectedChain(StructureEvent event) {}
    
    public void newObjectRequested(String accessionCode) {}
    
    public void noObjectFound(String accessionCode) {}

    public void clearSelection() {    }

    public void newSequence(SequenceEvent e) {

        
        int i =  tabPane.getTabForSpice(spice);
        if ( tabPane.getTabCount() > 0)
            tabPane.setTitleAt(i,tabPane.getSpiceText(spice));
        tabPane.setFrameTitle(spice);
        
    }

    public void selectedSeqPosition(int position) {}

    public void selectedSeqRange(int start, int end) {}

    public void selectionLocked(boolean flag) {}
    
}

