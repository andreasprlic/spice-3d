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
 * Created on Sep 13, 2006
 * 
 */

package org.biojava.spice.config;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.biojava.spice.ResourceManager;



public class SpiceDefaults {

	// logging stuff
	public static final String LOGGER = "org.biojava.spice";
	public static final Level  LOG_LEVEL = Level.INFO;
	
	public static final String REGISTRY    = ResourceManager.getString("org.biojava.spice.SpiceDefaults.Registry");
	
    public static final Color HELIX_COLOR  = new Color(255,51,51);
    public static final Color STRAND_COLOR = new Color(255,204,51);
    public static final Color TURN_COLOR   = new Color(204,204,204); 
    
    public static final List argumentTypes;
    
    	static { 
    			argumentTypes = new ArrayList();
    			argumentTypes.add("PDB");
    			argumentTypes.add("UniProt");
    			argumentTypes.add("ENSP");
    			argumentTypes.add("alignment");
    	}
    
}
