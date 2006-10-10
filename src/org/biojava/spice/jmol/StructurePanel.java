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

package org.biojava.spice.jmol ;


import java.awt.*;
import javax.swing.*;
import org.jmol.api.*;
import org.jmol.popup.JmolPopup;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.biojava.bio.structure.Structure ;
import org.biojava.bio.structure.StructureImpl ;
import org.biojava.spice.ResourceManager;

import java.util.logging.*;



/** A panel that provides a wrapper around the Jmol viewer. 
 * 
 * The code for this is heavily
 * inspired by
 * http://cvs.sourceforge.net/viewcvs.py/jmol/Jmol/examples/Integration.java?view=markup
 * - the Jmol example of how to integrate Jmol into an application.
 *
 * 
 */
public class StructurePanel
extends JPanel
implements JmolCommander
{
    
    private static final long serialVersionUID = 969575436790157931L;
    
    final  Dimension currentSize = new Dimension();
    final Rectangle  rectClip    = new Rectangle();
    
    static Logger    logger      = Logger.getLogger("org.biojava.spice");
    
    static String    EMPTYCMD =  ResourceManager.getString("org.biojava.spice.panel.StructurePanel.EmptyCmd");
        
    JmolViewer  viewer;
    
    JmolAdapter adapter;
    
    JmolPopup jmolpopup ;
    
    JTextField  strucommand  ; 
    
    int currentChainNumber;
    
    Structure structure ;
    
    public StructurePanel() {
        super();        
        
        adapter = new SmarterJmolAdapter(null);
        
        viewer  = org.jmol.viewer.Viewer.allocateViewer(this, adapter);
        
        jmolpopup = JmolPopup.newJmolPopup(viewer);
        
        // this is important to make Jmol thread -safe !!
        viewer.evalString("set scriptQueue on;");
        
    }
    
    /** Add a JmolStatus listener to Jmol
     * 
     * @see JmolSpiceTranslator
     * 
     * @param listener
     */
    public void addJmolStatusListener(JmolStatusListener listener) {
        viewer.setJmolStatusListener(listener);
        
        // in order to provide a statuslistener for jmol we need to know the popup and viewer..        
        if ( listener instanceof JmolSpiceTranslator) {
            JmolSpiceTranslator transe = (JmolSpiceTranslator)listener;
            transe.setJmolViewer(viewer);
            transe.setJmolPopup(jmolpopup);
        }
    }
    
    /** remove the listeners from Jmol again. 
     * 
     */
    public void clearListeners(){
        
        viewer.setJmolStatusListener(null);
        
    }
    
    
    /** returns the JmolViewer
     * 
     * @return the viewer
     */
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
    
    /** call this upon startup. This is a workaround to a bug in Jmol.
     * 
     *
     */
    public void initJmolDisplay(){
        String pdb = "ATOM     63  CA  GLY     9      47.866  28.415   2.952 \n" ;
        viewer.openStringInline(pdb);
        executeCmd("select *; spacefill off;");
    }
    
    
    /** Send a RASMOL like command to Jmol
     * @param command - a String containing a RASMOL like command. e.g. "select protein; cartoon on;"
     */
    public void executeCmd(String command) {
        logger.info(command);
        if (viewer.isScriptExecuting()) 
            logger.info("viewer is executing");
        
        viewer.evalString(command);
       
    }
    
    
    
    /** display a new PDB structure in Jmol 
     * @param structure a Biojava structure object    
     *
     */
    public void setStructure(Structure structure) {
        
        if ( structure == null ) {
            structure = new StructureImpl();            
        }       
        
        
        if ( structure.size() < 1 ) {
            //logger.info("got structure of size < 1");
            viewer.evalString(EMPTYCMD);
            return;
        }       
        
        viewer.evalString("exit");
      
        String pdbstr = structure.toPDB();

        viewer.openStringInline(pdbstr);
       
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
   
    }
    
}


