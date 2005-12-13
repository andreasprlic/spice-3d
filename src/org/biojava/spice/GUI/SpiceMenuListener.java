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
import java.util.logging.Logger;


import org.biojava.spice.Panel.StructurePanelListener;
import org.biojava.spice.manypanel.eventmodel.SequenceEvent;
import org.biojava.spice.manypanel.eventmodel.SequenceListener;
import org.biojava.spice.SPICEFrame;
import org.biojava.spice.GUI.alignmentchooser.AlignmentChooser;
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
    static String reset = "select all; " + alloff;
    static String noselect = "select none; ";
    
    static String SPICEMANUAL = "http://www.sanger.ac.uk/Users/ap3/DAS/SPICE/SPICE_manual.pdf" ;
    		
    SPICEFrame parent ;
    StructurePanelListener structurePanelListener;
    boolean selectionIsLocked;
    
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    public SpiceMenuListener (SPICEFrame spice, StructurePanelListener listen) {
        selectionIsLocked = false;
        parent = spice ;
        structurePanelListener = listen ;
    }
    
    public void actionPerformed(ActionEvent e) {
        //System.out.println(e);
        //System.out.println(">"+e.getActionCommand()+"<");
        
        String cmd = e.getActionCommand();
        if ( cmd.equals("Open") ) {
            OpenDialog op = new OpenDialog(parent);
            op.show();
        } else if (cmd.equals("Save")){
            if ( parent instanceof SpiceApplication) {
                SaveLoadSession save = new SaveLoadSession((SpiceApplication)parent);
                save.save();
            }
        } else if (cmd.equals("Load")){
            if ( parent instanceof SpiceApplication) {
                SaveLoadSession load = new SaveLoadSession((SpiceApplication)parent);
                load.load();
            }
        } else if (cmd.equals("Exit")) {
            System.exit(0);
        } else if (cmd.equals("Properties")) {
            parent.showConfig();
            //RegistryConfigIO regi = new RegistryConfigIO(parent,parent.REGISTRY_URL) ;	    
            //regi.setConfiguration(config);
            //regi.showConfigFrame();
        } else if (cmd.equals("Reset")) {
            parent.resetDisplay();
        } else if (cmd.equals("About SPICE")) {
            System.out.println("open about dialog");
            AboutDialog asd = new AboutDialog(parent);
            asd.show();
        } else if (cmd.equals("Manual")) {
            parent.showDocument(SPICEMANUAL);
            
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
    
    
    public void selectedSeqRange(int s,int e){}
    public void selectedSeqPosition(int s){}

    public void newSequence(SequenceEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void newObjectRequested(String accessionCode) {
        // TODO Auto-generated method stub
        
    }
    
    
    
    
    
}
