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

import org.biojava.spice.SPICEFrame;

import java.awt.*;
import javax.swing.*;

// for Jmol stuff
import org.jmol.api.*;
import org.jmol.adapter.smarter.SmarterJmolAdapter;



// biojava structure stuff
import org.biojava.bio.structure.Structure ;

// logging
import java.util.logging.*;



import org.openscience.jmol.ui.JmolPopup;


/** a Panel that provides a wrapper around the Jmol viewer. Code heavily
 * inspired by
 * http://cvs.sourceforge.net/viewcvs.py/jmol/Jmol/examples/Integration.java?view=markup
 * - the Jmol example of how to integrate Jmol into an application.
 * Here some mouse listeners are added that talk back to the main SPICE application.
 * 
 */
public class StructurePanel extends JPanel
implements JmolStatusListener {
    
    final  Dimension currentSize = new Dimension();
    final Rectangle  rectClip    = new Rectangle();
    
    static Logger    logger      = Logger.getLogger("org.biojava.spice");
    
    JmolViewer  viewer;
    JmolAdapter adapter;
    
    SPICEFrame  spice ;
    JmolPopup jmolpopup ;
    
    JTextField  strucommand  ; 
    
    
    public StructurePanel(SPICEFrame parent) {
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
        
        
        JmolThread thr = new JmolThread(viewer,command);
        thr.start();
        //viewer.evalString(command);
    }
    
    /** display a new PDB structure in Jmol 
     * @param structure a Biojava structure object    
     *
     */
    public  void setStructure(Structure structure) {
        if ( structure == null )
            return;
        
        String pdbstr = structure.toPDB(); 
        //System.out.println(pdbstr);
        synchronized ( viewer) {
            viewer.openStringInline(pdbstr);
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
    
    public void notifyFileLoaded(String fullPathName, String fileName,
            String modelName, Object clientFile){
        //logger.finest("Jmol loaded File "+ fileName); 
    }
    
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
    
}


