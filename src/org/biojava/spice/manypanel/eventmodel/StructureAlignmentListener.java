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
 * Created on Jul 16, 2006
 *
 */
package org.biojava.spice.manypanel.eventmodel;

import org.biojava.spice.alignment.StructureAlignment;

public interface StructureAlignmentListener {

    /** sets a structure alignments
     * 
     * @param alignment
     */
    public void setStructureAlignment(StructureAlignment alignment);
    
    
}
