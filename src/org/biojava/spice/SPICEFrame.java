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
 * Created on 05.06.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.spice;

import org.biojava.spice.config.*;
import org.biojava.spice.gui.SpiceTabbedPane;
import org.biojava.spice.manypanel.BrowserPane;
import org.biojava.spice.server.SpiceServer;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.Chain;
import org.jmol.api.JmolViewer;

import java.awt.Container;

/** an interface that defines methods provided by the master application. 

 
 * @author Andreas Prlic
 */
public interface SPICEFrame 	
{
	
    /** returns the java.awt.Container of the spice object
     * 
     * @return Container
     */
    public Container getParent();
    
    /** starts a new thread and loads a new biological object into spice 
     * @param type the type of the code provided. currently supported: PDB, UniProt, ENSP
     * @param code the code of the entry. e.g. 1a4a, P00280, 1q22 
     */
    public void load(String type, String code);
	
    
    /** reload the currently displayed data
     * 
     *
     */
    public void reload();
        
    
    
    /** retrieve configuration for DAS servers to use 
     * @return RegistryConfiguration the config
     * */    
    public RegistryConfiguration getConfiguration();

    public SpiceServer getSpiceServer();
    
    public void setSpiceTabbedPane(SpiceTabbedPane tab);
    
    public SpiceTabbedPane getSpiceTabbedPane();
    
    /** get the Jmol viewer 
     * 
     * @return JmolViewer the Jmol viewer doing the 3D visualisation
     */
    public JmolViewer getViewer();
    
    /** get the BrowserPane that is doing the 2D display
     * 
     * @return returns the 2D sequence and feature panels
     */
    public BrowserPane getBrowserPane();
    
    
    	/** retreive the internal Structure object 
         * @return Structure the internal structure representing the PDB object  
         * */
    	public Structure getStructure();
        
    public void setStructure(Structure s);
    	
   
    /** reset the display, but do not change data */
    public void resetDisplay();

    /** show Config */
    public void showConfig();
    
    /** retreive info regarding structure 
     * 
     * @param chainnumber the number of the chain
     * @return the Chain object
     * */
    public Chain getChain(int chainnumber);


 
    
    /** returns currently displayed PDB code; null if none
     * 
     * @return String the PDB code
     * */
    public String getPDBCode();
   
    /** returns currently displayed UniProt code; null if none
     * 
     * @return the Uniprot code
     * */
    public String getUniProtCode();

    public String getENSPCode();
    
    /** set which parameters should be used for loading the next molecule.
     * typically set before doing a new load(type,code) call.
     * @param params
     */
    public void setSpiceStartParameters(SpiceStartParameters params);
    
    /** get the parameters that are used while loading the next molecule
     * 
     *  @return the startup parameters
     *  */
    public SpiceStartParameters getSpiceStartParameters();
    
    
}
