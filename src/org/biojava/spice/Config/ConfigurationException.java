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
 */

package org.biojava.spice ;

import org.biojava.bio.BioException;

/**
 * An exception during the parsing of a PDB file.
 *
 * @author Andreas Prlic, Thomas Down, Benjamin Schuster-B�ckler
 */

public class ConfigurationException extends BioException {
    /**
     * Constructs a StructureException object.
     *
     * @param s  a String ...
     */
    public ConfigurationException(String s) {
	super(s);
    }
    /**
     * Constructs a StructureException object.
     *
     * @param t  a Throwable object
     * @param s  a String ...
     */
    public ConfigurationException (Throwable t, String s) {
	super(t, s);
    }
    /**
     * Constructs a StructureException object.
     *
     * @param t  a Throwable object
     */
    public ConfigurationException (Throwable t) {
	super(t);
    }
}