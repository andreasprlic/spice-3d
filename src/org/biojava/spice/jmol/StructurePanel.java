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
import javax.vecmath.Matrix3f;

import org.jmol.api.*;
import org.jmol.popup.JmolPopup;

import org.biojava.bio.structure.AminoAcidImpl;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.AtomImpl;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure ;
import org.biojava.bio.structure.StructureImpl ;
import org.biojava.bio.structure.io.PDBParseException;
import org.biojava.bio.structure.jama.Matrix;
import org.biojava.spice.ResourceManager;
import org.biojava.spice.config.SpiceDefaults;

import java.text.DecimalFormat;
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
    
    static Logger    logger      = Logger.getLogger(SpiceDefaults.LOGGER);
    
    static String    EMPTYCMD =  ResourceManager.getString("org.biojava.spice.panel.StructurePanel.EmptyCmd");
        
    JmolViewer  viewer;
    
    JmolAdapter adapter;
    
    JmolPopup jmolpopup ;
    
    JTextField  strucommand  ; 
    
    int currentChainNumber;
    
    Structure structure ;
    
    public StructurePanel() {
        super();        
        
        adapter = new SpiceJmolAdapter();
        
        initJmolInstance();
        initJmolDisplay();
        
        //viewer.openClientFile("","",new StructureImpl());   
                
    }
    
    /** reset the Jmol display */
    public void reset() {
        viewer.homePosition();
        
    }
    
    /** if Jmol crahsed, drop it and get a new one
     * 
     *
     */
    private void initJmolInstance(){
        logger.info("init jmol instance");
        
        Matrix jmolRotation = null;
        if ( viewer != null)
        	jmolRotation = getJmolRotation();
        
        viewer  = org.jmol.viewer.Viewer.allocateViewer(this, adapter);
        
        jmolpopup = JmolPopup.newJmolPopup(viewer,true);
        
        // this is important to make Jmol thread -safe !!
        viewer.evalString("set scriptQueue on;");
        //initJmolDisplay();
        if ( jmolRotation != null )
        	rotateJmol(jmolRotation);
        
        
    }
    /** call this upon startup. This is a workaround to a bug in Jmol.
     * 
     *
     */
    public void initJmolDisplay(){
    	
        //String pdb = "ATOM     63  CA  GLY     9      47.866  28.415   2.952 \n" ;
        //viewer.openStringInline(pdb);
        
    	Atom a =new AtomImpl();
    	a.setName("CA");
    	a.setFullName(" CA ");
    	a.setCoords(new double[]{47.866,  28.415,   2.952});
    	
    	Group g = new AminoAcidImpl();
    	g.setPDBCode("9");
    	try {
    	g.setPDBName("GLY");
    	} catch (PDBParseException e){
    		e.printStackTrace();
    	}
    	g.addAtom(a);
    	
    	Chain c =new ChainImpl();
    	c.addGroup(g);
    	
    	Structure s = new StructureImpl();
    	s.addChain(c);
    	
    	setStructure(s);
    	
    	s.setPDBCode("1AND");
    	
    	executeCmd("select *; spacefill off;");
        
        
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
    
    
    public void clearDisplay(){
        executeCmd("zap;");
        setStructure(new StructureImpl());
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
    
   
    
    
    /** Send a RASMOL like command to Jmol
     * @param command - a String containing a RASMOL like command. e.g. "select protein; cartoon on;"
     */
    public void executeCmd(String command) {
        logger.info(command);
        if (viewer.isScriptExecuting()) 
            logger.info("viewer is executing");
        
        viewer.script(command);
       
    }
    
    
    /** get the rotation out of Jmol 
    * 
    * @return the jmol rotation matrix
    */
   public Matrix getJmolRotation(){
       	Matrix jmolRotation = Matrix.identity(3, 3);
      
           //structurePanel.executeCmd("show orientation;");
       	JmolViewer jmol = getViewer();
       	Object obj = jmol.getProperty(null,"transformInfo","");
       	// System.out.println(obj);
       	if ( obj instanceof Matrix3f ) {
       		Matrix3f max = (Matrix3f) obj;
       		jmolRotation = new Matrix(3,3);
       		for (int x=0; x<3;x++) {
       			for (int y=0 ; y<3;y++){
       				float val = max.getElement(x,y);
       				// System.out.println("x " + x + " y " + y + " " + val);
       				jmolRotation.set(x,y,val);
       			}
       		}                
       	}                               
       return jmolRotation;
   }    
    
    /** send a rotation to Jmol
     * 
     * @param jmolRotation
     */
    public void rotateJmol(Matrix jmolRotation) {
       
            if ( jmolRotation != null) {
                //jmolRotation.print(3,3);
                double[] zyz = Calc.getZYZEuler(jmolRotation);
                DecimalFormat df = new DecimalFormat("0.##");
                
                String script = "reset; rotate z "
                    + df.format(zyz[0]) 
                    + "; rotate y " 
                    + df.format(zyz[1]) 
                    +"; rotate z "
                    + df.format(zyz[2])+";";
                    
               // logger.info(script);
                executeCmd(script);
                /*structurePanel.executeCmd("show orientation");
                JmolViewer viewer = structurePanel.getViewer();
                System.out.println("rotating jmol ... " + script);
                viewer.homePosition();
                viewer.rotateToZ(Math.round(zyz[0]));
                viewer.rotateToY(Math.round(zyz[1]));
                viewer.rotateToZ(Math.round(zyz[2]));
                */
            }
          
        
    }
    
    
    
    
    /** return the currently displayed structure object
     * 
     * @return the currently displayed structure
     */
    public Structure getStructure(){
        return structure;
    }
    
    
    
    
    /** display a new PDB structure in Jmol 
     * @param structure a Biojava structure object    
     *
     */
    public void setStructure(Structure structure) {
        
        if ( structure == null ) {
            structure = new StructureImpl();
            initJmolDisplay();
            
           
        }       
        
        this.structure = structure;
        
        if ( structure.size() < 1 ) {
            //logger.info("got structure of size < 1");
            viewer.evalString(EMPTYCMD);
            return;
        }       
        
        if (viewer.isScriptExecuting()) {
            // something is going wrong with jmol!
            // drop it an get a new instance
         
            logger.info("StructurePanel.setStructure(): jmol is still executing - seems to be crashed!");
            initJmolInstance();
        }
        
        viewer.evalString("exit");
        //viewer.evalString("zap");
        
         
        
        if ( adapter instanceof SpiceJmolAdapter){
            //logger.info("using the new SpiceJmolAdapter");
            SpiceJmolAdapter sad = (SpiceJmolAdapter)adapter;
            sad.setStructure(structure);
            
            /*class MyRunnable implements Runnable {
                Structure structure;
                public MyRunnable(Structure struc){
                    super();
                    structure = struc;
                }
                public void run() {
                    viewer.openClientFile("","",structure);         
                    jmolpopup.updateComputedMenus();
                    executeCmd(StructurePanelListener.INIT_SELECT);
                    
                }
            }
            
            new MyRunnable(structure).run();
            
            //SwingUtilities.invokeLater(new MyRunnable(structure));
             */
            viewer.openClientFile("","",structure);         
            jmolpopup.updateComputedMenus();
            executeCmd(StructurePanelListener.INIT_SELECT);
            
            
            
        } else {
            // most likely the adapter is SmarterJmolAdapter
            // in this case we convert the structure object to a PDB file
            // and let Jmol parse it again.
            // disavantage: much slower!
            
            logger.info("calling toPDB");
            String pdbstr = structure.toPDB();

            viewer.openStringInline(pdbstr);

            
            if ( pdbstr.equals("")){
                executeCmd(EMPTYCMD);
            }
            String strError = viewer.getOpenFileError();
            if (strError != null) {

                if (logger.isLoggable(Level.WARNING)) {
                    logger.severe("could not open PDB file in viewer "+ strError);
                }
            } else {
                jmolpopup.updateComputedMenus();
                executeCmd(StructurePanelListener.INIT_SELECT);
            }
        }
                
        jmolpopup.updateComputedMenus();
         
        logger.finest("end of setStructure");
   
    }
    
}


