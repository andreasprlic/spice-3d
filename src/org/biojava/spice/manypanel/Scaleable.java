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
 * Created on Oct 28, 2005
 *
 */
package org.biojava.spice.manypanel;

/** Interface for objects that can be scaled in an out
 * 
 * @author Andreas Prlic
 *
 */
public interface Scaleable {
    /** something between 0 and 100 - percent
     *
     *if 100 - the painting should fill the currently visible display
     * 0 is zoomdepth as big as possible
     *     
     * @param scale
     */
       
    
    public void calcScale(int scale);
    
    
}
