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
 * Created on Feb 2, 2005
 *
 */
package org.biojava.spice.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.GroupIterator;
import org.biojava.bio.structure.Structure;
import org.biojava.dasobert.eventmodel.SequenceEvent;
import org.biojava.dasobert.eventmodel.SequenceListener;
import org.biojava.spice.Panel.StructurePanelListener;
import org.biojava.spice.manypanel.managers.StructureManager;
import org.biojava.spice.server.SpiceServer;
import org.biojava.spice.SPICEFrame;
import org.biojava.spice.SpiceStartParameters;
//import org.biojava.spice.Panel.seqfeat.*;
import org.biojava.spice.SpiceApplication;

/**This class takes care of the events that are triggered 
 * if a MenuItem is choosen from the main spice menu.
 * 
 * @author Andreas Prlic
 *
 */
public class SpiceMenuListener   
implements ActionListener,
SequenceListener
{
    
    static String alloff = "cpk off ; wireframe off ; backbone off; cartoon off ; ribbons off; " ;
    static String reset = "select *; " + alloff;
    static String noselect = "select none; ";
    
    static String SPICEMANUAL = "http://www.sanger.ac.uk/Users/ap3/DAS/SPICE/SPICE_manual.pdf" ;
    
    SPICEFrame spice ;
    StructurePanelListener structurePanelListener;
    boolean selectionIsLocked;
    
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    public SpiceMenuListener (SPICEFrame sp, StructurePanelListener listen) {
        selectionIsLocked = false;
        spice = sp ;
        structurePanelListener = listen ;
    }
    
    public void actionPerformed(ActionEvent e) {
        //System.out.println(e);
        //System.out.println(">"+e.getActionCommand()+"<");
        
        String cmd = e.getActionCommand();
        
        if ( cmd.equals("New Window")){
            
            SpiceStartParameters params = spice.getSpiceStartParameters();
            params.setInitSpiceServer(false);
            
            SpiceApplication newSpice = new SpiceApplication(params);         
            
            SpiceServer server = spice.getSpiceServer();
            //newSpice.setSpiceServer(server);
            //server.registerInstance(newSpice);
            new SpiceTabbedPane(server,newSpice);
            
            
        }
        
        else if ( cmd.equals("New Tab")){
            
            SpiceStartParameters params = spice.getSpiceStartParameters();
            params.setInitSpiceServer(false);
            params.setNewTab(true);
            params.setNoRegistryContact(true); 
           
            SpiceApplication newSpice = new SpiceApplication(params);         
            //SpiceServer server = spice.getSpiceServer();
            //newSpice.setSpiceServer(server);
            
           // SpiceServer server = spice.getSpiceServer();
            
            SpiceTabbedPane tabbed = spice.getSpiceTabbedPane();                                               
            tabbed.addSpice(newSpice);
            
            
        }
        else if ( cmd.equals("Open") ) {
            if ( spice instanceof SpiceApplication){
                SpiceApplication sp = (SpiceApplication)spice;
                OpenDialog op = new OpenDialog(sp);
                op.show();
            }
            
        } else if (cmd.equals("Save")){
            
            if ( spice instanceof SpiceApplication) {
                SaveLoadSession save = new SaveLoadSession((SpiceApplication)spice);
                save.save();
            }
            
        } else if (cmd.equals("Load")){
            
            if ( spice instanceof SpiceApplication) {
                SaveLoadSession load = new SaveLoadSession((SpiceApplication)spice);
                load.load();
            }
            
        } else if (cmd.equals("Exit")) {
            
            System.exit(0);
            
        } else if (cmd.equals("Properties")) {
            spice.showConfig();
            //RegistryConfigIO regi = new RegistryConfigIO(parent,parent.REGISTRY_URL) ;	    
            //regi.setConfiguration(config);
            //regi.showConfigFrame();
        } else if (cmd.equals("Reset")) {
            spice.resetDisplay();
        } else if (cmd.equals("About SPICE")) {
            System.out.println("open about dialog");
            AboutDialog asd = new AboutDialog(spice);
            asd.show();
        } else if (cmd.equals("Tech Info")) {
            TechInfoDialog tid = new TechInfoDialog();
            tid.show();
        } else if (cmd.equals("Manual")) {
            spice.showDocument(SPICEMANUAL);
            
        } else if ( cmd.equals("Backbone") ){
            String dcmd;
            if ( isSelectionLocked()) 
                dcmd  = alloff + "backbone 0.5;  ";
            else 
                dcmd  = reset + "backbone 0.5;  " +noselect;
            structurePanelListener.executeCmd(dcmd);
        } else if ( cmd.equals("Wireframe") ){
            String dcmd ;
            if (isSelectionLocked())
                dcmd= alloff + "wireframe on; ";
            else
                dcmd= reset + "wireframe on; "+noselect;
            structurePanelListener.executeCmd(dcmd);
        } else if ( cmd.equals("Cartoon") ){
            String dcmd ;
            if (isSelectionLocked())
                dcmd  = alloff + "cartoon on; ";
            else
                dcmd  = reset + "cartoon on; "+noselect;
            structurePanelListener.executeCmd(dcmd);
        } else if ( cmd.equals("Ball and Stick") ){
            String dcmd ;
            if (isSelectionLocked())
                dcmd  = alloff + "wireframe 0.3; spacefill 0.5; ";
            else
                dcmd  = reset + "wireframe 0.3; spacefill 0.5; "+noselect;
            structurePanelListener.executeCmd(dcmd);
        } else if ( cmd.equals("Spacefill") ){
            String dcmd ;
            if (isSelectionLocked())
                dcmd  = alloff + "spacefill on; ";
            else
                dcmd  = reset + "spacefill on; "+noselect;
            structurePanelListener.executeCmd(dcmd);
        } else if ( cmd.equals("Color - rainbow")){
            StructureManager sm = spice.getBrowserPane().getStructureManager();
            
            Group[] selection = new Group[0];
            
            
            if ( isSelectionLocked()){
                //logger.info("getting selection from cursor panel");
                selection = spice.getBrowserPane().getStructureRenderer().getCursorPanel().getSelection();
            } else {
                //logger.info("coloring the whole structure");
                Structure s = sm.getStructure();
                //System.out.println(s.toPDB());
                // convert s  tp group[]
                List sel = new ArrayList();
                
                // there should be always at least an empty chain there ..
                List chains = s.getChains(0);
                
                Iterator iter = chains.iterator();
                while (iter.hasNext()){
                    Chain c = (Chain) iter.next();
                    List aminos = c.getGroups("amino");
                    Iterator aiter = aminos.iterator();
                    while (aiter.hasNext()){
                        Group g = (Group) aiter.next();
                        if ( g.getParent() == null){
                            g.setParent(c);
                        }
                        sel.add(g);
                    }
                    
                }
                
                // hm: this would be my preferred waay to do it,                
//              butlike this the parent get's lost!
                
//                GroupIterator iter = new GroupIterator(s);
//                while (iter.hasNext()){
//                    Group g = (Group)iter.next();
//                    if ( g.getType().equals("amino")) {
//                        
//                        sel.add(g);
//                    }
//                }
                selection = (Group[])sel.toArray(new Group[sel.size()]);
            }
            new RainbowPainter(structurePanelListener,selection);
            
        } else if ( cmd.equals("Color - chain")) {
            String dcmd ;
            if (isSelectionLocked())
                dcmd = "color chain;";
            else
                dcmd = "select all; color chain;" +noselect;
            structurePanelListener.executeCmd(dcmd);
        } else if ( cmd.equals("Color - secondary")) {
            String dcmd ;
            if (isSelectionLocked())
                dcmd = "color structure;";
            else
                dcmd = "select all; color structure;" +noselect;
            structurePanelListener.executeCmd(dcmd);
        } else if ( cmd.equals("Color - cpk")) {
            String dcmd ;
            if (isSelectionLocked())
                dcmd = "color cpk;";
            else
                dcmd = "select all; color cpk;" +noselect;
            structurePanelListener.executeCmd(dcmd);
        } else if ( cmd.substring(0,8).equals("Color - ") ) {
            String color = cmd.substring(8,cmd.length());
            String dcmd;
            if (isSelectionLocked())
                dcmd = "color " + color + ";";
            else
                dcmd = "select all; color " + color +";" +noselect;
            structurePanelListener.executeCmd(dcmd);
        } else if ( cmd.substring(0,9).equals("Select - ")){
            String sel = cmd.substring(9,cmd.length());
            String dcmd = "select " + sel ;
            structurePanelListener.executeCmd(dcmd);
            
            
        } else if ( cmd.equals("Choose")){
            //System.out.println("pressed alig window open");
            //AlignmentChooser aligc = new AlignmentChooser(parent);
            //aligc.show();
            
            // has been moved to BrowserPane !
        } else {
            //System.out.println("unknown menu comand " + cmd);
        }
        
    }
    
    public void selectionLocked(boolean flag){
        // logger.warning("spicemenulistener selectionLocked " + flag );
        selectionIsLocked = flag;
    }
    
    public void clearSelection(){
        selectionIsLocked =false;
        
    }
    
    private boolean isSelectionLocked(){
        return selectionIsLocked;
    }
    
    
    public void selectedSeqRange(int s,int e){
        
    }
    public void selectedSeqPosition(int s){}
    
    public void newSequence(SequenceEvent e) {
        
        
    }
    
    public void newObjectRequested(String accessionCode) {
        
        
    }
    public void noObjectFound(String accessionCode) {
        
    }
    
    
    
    
    
}
