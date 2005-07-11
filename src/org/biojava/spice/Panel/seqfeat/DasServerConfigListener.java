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
 * Created on Jul 11, 2005
 *
 */
package org.biojava.spice.Panel.seqfeat;

/** an interface for a configuration change events regarding the displayed DAS servers..
 * 
 * @author Andreas Prlic
 *
 */
public interface DasServerConfigListener {
    /** display all available DAS servers */
    public void enableAllDasSources();
    /** disable the display of all das sources */
    public void disableAllDasSources();
    
    /** remove a particular DAS source from the display */
    public void disableDasSource(String uniqueId);
}
