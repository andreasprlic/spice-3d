/*
 *                    BioJava development code
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
 * @author Andreas Prlic
 *
 */

package org.biojava.spice.Panel ;

import org.biojava.spice.SpiceApplication;
import org.biojava.spice.SPICEFrame;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;
// for Jmol stuff
import org.jmol.api.*;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
// biojava structure stuff
import org.biojava.bio.structure.Structure ;
import org.biojava.bio.structure.StructureImpl ;
// logging
import java.util.logging.*;

import org.openscience.jmol.ui.JmolPopup;
import java.awt.event.ActionListener;

/** a Panel that provides a wrapper around the Jmol viewer. Code heavily
 * inspired by
 * http://cvs.sourceforge.net/viewcvs.py/jmol/Jmol/examples/Integration.java?view=markup
 * - the Jmol example of how to integrate Jmol into an application.
 *
 * 
 */
public class StructurePanel
extends JPanel
implements JmolStatusListener

{
    
    final  Dimension currentSize = new Dimension();
    final Rectangle  rectClip    = new Rectangle();
    
    static Logger    logger      = Logger.getLogger("org.biojava.spice");
    static String    EMPTYCMD = "set echo top center; font echo 22; color echo white;echo \"no structure found\";";
    JmolViewer  viewer;
    JmolAdapter adapter;
    
    SPICEFrame  spice ;
    JmolPopup jmolpopup ;
    
    JTextField  strucommand  ; 
    int currentChainNumber;
    Structure structure ;
    
    public StructurePanel(){
        this(null);
    }
    
    public StructurePanel(SPICEFrame parent) {
        super();
        spice   = parent ;
        
        adapter = new SmarterJmolAdapter(null);
        
        viewer  = org.jmol.viewer.Viewer.allocateViewer(this, adapter);
        viewer.setJmolStatusListener(this);
        jmolpopup = JmolPopup.newJmolPopup(viewer);
    }
    
    
    
    
    
    /** returns the JmolViewer */
    public JmolViewer getViewer() {
        return viewer;
    }
    
    /** paint Jmol */
    public void paint(Graphics g) {
        getSize(currentSize);
        g.getClipBounds(rectClip);
        viewer.renderScreenImage(g, currentSize, rectClip);
        
    }
    
    /** reset the Jmol display */
    public void reset() {
        viewer.homePosition();
        
    }
    public void notifyFileLoaded(String fullPathName, String fileName,
            String modelName, Object clientFile,
            String errorMessage){
        logger.finest("StructurePanel notifyFileLoaded ");
        if (errorMessage != null){
            logger.log(Level.SEVERE,errorMessage);
        }
        
    }
    
    public void showConsole(boolean showConsole){
        logger.finest("jmol: showConsole "+showConsole);
    }
    
    /** send a RASMOL like command to Jmol
     * @param command - a String containing a RASMOL like command. e.g. "select protein; cartoon on;"
     */
    public void executeCmd(String command) {
        
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("sending Jmol command: "+command);
        }
        System.out.println("sending Jmol command: " + command);
        
        //TODO: is this needed?
        synchronized(viewer){
            viewer.evalString(command);
        }
        
        //JmolThread thr = new JmolThread(viewer,command);
        //thr.start();
        //viewer.evalString(command);
        logger.finest("sent command");
        System.out.println("sent command");
       
    }
    
    /** display a new PDB structure in Jmol 
     * @param structure a Biojava structure object    
     *
     */
    public  void setStructure(Structure structure) {
        if ( structure == null ) {
            structure = new StructureImpl();            
        }
        
        //if ( structure.size() == 0 ) {
          //  executeCmd(EMPTYCMD);
            //notifyAll();
            //return;
        
        
        String pdbstr = structure.toPDB();
        //System.out.println(pdbstr);
        /*if ( (pdbstr == null) || 
                (pdbstr.equals("\n")) ||
                (pdbstr.equals(""))) {
            executeCmd(EMPTYCMD);
            //notifyAll();
            return;
        }*/
        
         
         viewer.openStringInline(pdbstr);
         if ( structure.size() == 0 ) {
             executeCmd(EMPTYCMD);
         }
       
        //String cmd ="select all; cpk off; wireframe off;"  ;
        //executeCmd(cmd);
        
        
        String strError = viewer.getOpenFileError();
        if (strError != null) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.severe("could not open PDB file in viewer "+ strError);
            }
        }
        jmolpopup.updateComputedMenus();
        if ( pdbstr.equals("")){
            executeCmd(EMPTYCMD);
        }
        logger.finest("end of setStructure");
        //notifyAll();
    }
    
    
    
    /*
     public void mouseDragged(MouseEvent e) {
     //logger.finest("dragging mouse "+e);
      }	
      */
    
    /* when the mouse is moved of the structure panel,
     the corresponing position in the sequence is highlited
     */
    
    
    /*
     public void mouseMoved(MouseEvent e) {
     //logger.finest("moving mouse over StructurePanel "+e);    	
      int pos = viewer.findNearestAtomIndex(e.getX(),e.getY());
      if ( pos == -1 ) { return ; }
      
      String chainId = viewer.getAtomChain( pos) ;
      String seqCode = viewer.getAtomSequenceCode( pos) ;
      
      String[] spl = seqCode.split("\\^");
      if ( spl.length > 1) {
      //logger.finest("insertion code found! " + residuePDBcode );
       seqCode = spl[0] + spl[1];
       }
       
       
       //logger.finest("chainid " + chainId + " seqcode: " + seqCode);
        int chainpos = 0 ;
        if  ( chainId != null ) 
        chainpos   = spice.getChainPosByPDB(chainId);
        
        // what is the default return value for empty chain in Jmol ?
         if (chainpos == -1 )
         chainpos = 0 ;
         int residuepos = spice.getSeqPosByPDB(seqCode);
         
         spice.select(chainpos,residuepos);
         spice.showSeqPos(chainpos,residuepos);
         
         //logger.finest("atomIndex "+atom.getAtomIndex());
          //logger.finest(viewer.getElementNumber(pos));
           //logger.finest(viewer.getElementSymbol(pos));
            //logger.finest("atomName " + viewer.getAtomName(pos));
             //logger.finest("seqCode " + atom.getSeqcodeString());
              
              }
              
              */
    /*
     public void mouseClicked(MouseEvent e)
     {
     //logger.finest("mouseClick in structure Panel"+e);
      
      
      // if right mouse button 
       
       
       int pos = viewer.findNearestAtomIndex(e.getX(),e.getY());
       if ( pos == -1 ) { return ; }
       
       String chainId = viewer.getAtomChain( pos) ;
       String seqCode = viewer.getAtomSequenceCode( pos) ;
       
       String[] spl = seqCode.split("\\^");
       if ( spl.length > 1) {
       //logger.finest("insertion code found! " + residuePDBcode );
        seqCode = spl[0] + spl[1];
        }
        
        
        
        //logger.finest("chainid " + chainId + " seqcode: " + seqCode);
         int chainpos = 0 ;
         if  ( chainId != null ) 
         chainpos   = spice.getChainPosByPDB(chainId);
         
         // what is the default return value for empty chain in Jmol ?
          if (chainpos == -1 )
          chainpos = 0 ;
          int residuepos = spice.getSeqPosByPDB(seqCode);
          
          spice.highlite(chainpos,residuepos);
          spice.showSeqPos(chainpos,residuepos);	
          
          }
          public void mouseEntered(MouseEvent e)  {}
          public void mouseExited(MouseEvent e)   {}
          public void mousePressed(MouseEvent e)  {}
          public void mouseReleased(MouseEvent e) {}
          */
    
    public void notifyAtomPicked(int atomIndex, String strInfo){
        logger.finest("notifyAtomPicked "  + atomIndex + " " + strInfo);
        logger.finest("atomName:" + viewer.getAtomName(atomIndex));
        java.util.Properties props = viewer.getModelProperties(atomIndex);
        logger.finest(props.toString());
    }
    
    /*public void notifyFileLoaded(String fullPathName, String fileName,
     String modelName, Object clientFile){
     //logger.finest("Jmol loaded File "+ fileName); 
      }
      */
    
    public void notifyFileNotLoaded(String fullPathName, String errorMsg){}
    
    public void setStatusMessage(String statusMessage){
        logger.log(Level.INFO,statusMessage);
    }
    
    public void scriptEcho(String strEcho){
        logger.log(Level.INFO, "jmol scriptEcho: " + strEcho);
    }
    
    public void scriptStatus(String strStatus){
        logger.log(Level.FINEST,"jmol scriptStatus: " +strStatus);
    }
    
    public void notifyScriptTermination(String statusMessage, int msWalltime){
        
        logger.fine("Script finished in " + msWalltime + "ms");
    }
    
    public void handlePopupMenu(int x, int y){
        logger.finest("handlePopupMenu");
        //viewer.popupMenu(e.getX(),e.getY());
        jmolpopup.show(x,y);
        
        
        
    }
    
    public void notifyMeasurementsChanged(){
        logger.finest("nofiyMeasurementsChanged");
    }
    
    public void notifyFrameChanged(int frameNo){}
    
    public void showUrl(String urlString) {
        logger.finest("showUrl: " +urlString);
    }
    
    /** create a JMenu to be used for interacting with the structure Panel.
     * requires an ActionListener to be called when one of the Menus is being used. */
    public static JMenu createMenu(ActionListener ml){
        JMenu display = new JMenu("Display");
        display.setMnemonic(KeyEvent.VK_D);
        display.getAccessibleContext().setAccessibleDescription("change display");
        
        ImageIcon resetIcon = SpiceApplication.createImageIcon("reload.png");
        JMenuItem reset;
        if ( resetIcon == null)
            reset   = new JMenuItem("Reset");
        else
            reset   = new JMenuItem("Reset",resetIcon);
        reset.setMnemonic(KeyEvent.VK_R);
        
        /*
         * ImageIcon lockIcon = createImageIcon("lock.png");
         
         if (lockIcon != null)
         lock = new JMenuItem("Lock Selection",lockIcon);
         else
         lock = new JMenuItem("Lock Selection");
         
         ImageIcon unlockIcon = createImageIcon("decrypted.png");
         if ( unlockIcon == null)
         unlock = new JMenuItem("Unlock Selection");
         else
         unlock = new JMenuItem("Unlock Selection", unlockIcon);
         
         lockMenu = unlock;
         lockMenu.setMnemonic(KeyEvent.VK_U);
         lockMenu.setEnabled(selectionLocked);
         */
        JMenuItem backbone   = new JMenuItem("Backbone");
        JMenuItem wireframe  = new JMenuItem("Wireframe");
        JMenuItem cartoon    = new JMenuItem("Cartoon");
        JMenuItem ballnstick = new JMenuItem("Ball and Stick");
        JMenuItem spacefill  = new JMenuItem("Spacefill");
        
        JMenuItem colorchain = new JMenuItem("Color - chain");
        JMenuItem colorsec   = new JMenuItem("Color - secondary");
        JMenuItem colorcpk   = new JMenuItem("Color - cpk");
        
        reset.addActionListener     ( ml );
        //lockMenu.addActionListener    ( ml );
        backbone.addActionListener  ( ml );
        wireframe.addActionListener ( ml );	
        cartoon.addActionListener   ( ml );
        ballnstick.addActionListener( ml );
        spacefill.addActionListener ( ml );		
        colorchain.addActionListener( ml );
        colorsec.addActionListener  ( ml );
        colorcpk.addActionListener  ( ml );
        
        
        display.add( reset   );
        //display.add( lockMenu  );
        display.addSeparator();
        
        display.add( backbone   );
        display.add( wireframe  );
        display.add( cartoon    );
        display.add( ballnstick );
        display.add( spacefill  );
        display.addSeparator();
        
        display.add(colorchain);
        display.add(colorsec)   ;
        display.add(colorcpk)  ;
        
        return display;
    }
    
    
    
    
    
}


