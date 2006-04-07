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


import java.awt.*;
import javax.swing.*;
import org.jmol.api.*;
import org.jmol.popup.JmolPopup;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.biojava.bio.structure.Structure ;
import org.biojava.bio.structure.StructureImpl ;
import java.util.logging.*;



/** a Panel that provides a wrapper around the Jmol viewer. Code heavily
 * inspired by
 * http://cvs.sourceforge.net/viewcvs.py/jmol/Jmol/examples/Integration.java?view=markup
 * - the Jmol example of how to integrate Jmol into an application.
 *
 * 
 */
public class StructurePanel
extends JPanel

{
    
    private static final long serialVersionUID = 969575436790157931L;
    final  Dimension currentSize = new Dimension();
    final Rectangle  rectClip    = new Rectangle();
    
    static Logger    logger      = Logger.getLogger("org.biojava.spice");
    static String    EMPTYCMD = "zap; set echo top center; font echo 22; color echo white;echo \"no structure found\";";
    
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
        
    }
    
    public void addJmolStatusListener(JmolStatusListener listener) {
        viewer.setJmolStatusListener(listener);
        
        // in order to provide a statuslistener for jmol we need to know the popup and viewer..        
        if ( listener instanceof JmolSpiceTranslator) {
            JmolSpiceTranslator transe = (JmolSpiceTranslator)listener;
            transe.setJmolViewer(viewer);
            transe.setJmolPopup(jmolpopup);
        }
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
    
    /** send a RASMOL like command to Jmol
     * @param command - a String containing a RASMOL like command. e.g. "select protein; cartoon on;"
     */
    public void executeCmd(String command) {
        //logger.info(command);
       
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
            logger.info("got structure of size < 1");
            viewer.evalStringSync("zap");
            //viewer.evalStringSync(EMPTYCMD);
            return;
        }
        logger.info("setting new structure in Jmol " + structure.getPDBCode() + " " + structure.size());
        
        String pdbstr = structure.toPDB();
        logger.info("pdbstring "+pdbstr.substring(0,200) );
        viewer.openStringInline(pdbstr);
        
        logger.info("finished loading structure ");
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


