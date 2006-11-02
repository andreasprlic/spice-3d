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
 * Created on Aug 4, 2006
 *
 */
package org.biojava.spice;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.biojava.spice.config.SpiceDefaults;

/** A class that manages the Strings that are defined in the spice.properties file. 
 * This will be usefull for internationalisation. 
 * 
 * TODO: provide spice.properties files for other locales.
 * e.g. spice_de_DE.properties, etc.
 * 
 * @author Andreas Prlic
 * @since 1:43:04 PM
 * @version %I% %G%
 */
public class ResourceManager {
    
    private static final String BUNDLE_NAME = "spice"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    public static Logger logger = Logger.getLogger(SpiceDefaults.LOGGER);
    
    private ResourceManager() {
    }

    public static String getString(String key) {
        
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
        	logger.config(e.getMessage());
            return '!' + key + '!';
        }
    }
}
