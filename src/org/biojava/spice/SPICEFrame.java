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

import org.biojava.spice.Config.*;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.Chain;
import java.util.List;
import java.net.URL;
import org.biojava.spice.Panel.seqfeat.SpiceFeatureViewer;

/** an interface that defines methods provided by the master application. 

 
 * @author Andreas Prlic
 */
public interface SPICEFrame 	
{
	
    /** starts a new thread and loads a new biological object into spice 
     * @param type the type of the code provided. currently supported: PDB, UniProt, ENSP
     * @param code the code of the entry. e.g. 1a4a, P00280, 1q22 
     */
    public void load(String type, String code);
	
    /** retrieve configuration for DAS servers to use */    
    public RegistryConfiguration getConfiguration();

    /** returns a flag if data is being loaded using DAS. This is
     * needed for the differnt sub-frames to prevent them from having
     * problems with the loading threads  */    
    public boolean isLoading() ;

   

    /** set a structure to be displayed. Use a default select command
     * to color structure
     * @param structure a Biojava structure object
     */
    public void   setStructure(Structure structure );

    
    
    	/** retreive the internal Structure object */
    	public Structure getStructure();
    	
    /** set the new features to be displayed in sequence panels */
    public void setFeatures(String sp_id, List features);

    /** get the displayed features */
    	public List getFeatures();
    
    
    /** set the chain nr chainnr as the active one */
    public void setCurrentChainNumber(int chainnr);
    
    /** get the chain nr of the currently active chain */
    public int getCurrentChainNumber() ;
  
    /** reset the display, but do not change data */
    public void resetDisplay();

    /** show Config */
    public void showConfig();

    /** display an URL in the browser that started SPICE */
    public boolean showURL(URL url);
   
    // rescale the windo size 
    //public void scale() ;
    
    /** retreive info regarding structure */
    public Chain getChain(int chainnumber);


    /** set the configuration from outside */
    public void setConfiguration(RegistryConfiguration conf);

    /** specify from outside whether spice should display that it is 
     * loading data */
    public void setLoading(boolean status);
    
  
    
    /** open a web page in the browser 
     * returns true if the request succeeds, otherwise false
     * */
    public boolean showDocument(URL url);
        
    /** converts a String to an URL and then calls showDocument(URL url)*/
    public boolean showDocument(String urlstring);
    
    /** returns currently displayed PDB code; null if none*/
    public String getPDBCode();
   
    /** returns currently displayed UniProt code; null if none*/
    public String getUniProtCode();

    /** return the DAS - feature viewer panel */
    public SpiceFeatureViewer getFeatureViewer();
    
    /**set which Das servers should be displayed
     */
    public void setDasServerString(String dasServerString);
    
    /**     * set which DAS servers should be displayed.
     */
    public void setDasLabelString(String dasLabelString);
        
    
}
