
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
 * Created on 21.03.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.spice.panel;

import org.biojava.bio.structure.Chain ;


/** All "SeqPanel" object need to implement these functions. SeqPanel
 * objects have the UniProt sequence (i.e. the central SPICEFrame
 * structure object) as coordinate System 
 * @author Andreas Prlic
*/

public interface SeqPanel {

    /** set a Chain, tells which chain number in spice master application */
    public void setChain(Chain c,int spice_chainnumber) ;   

    /** highighting of range of residues
	make sure that this is only called for the currently displayed chain fromthe outside!
	
    */
    public void highlite( int start, int end);

    /** highighting of single residue */    
    public void highlite( int seqpos);

    /** select range of residues */
    public void select( int start, int end);
    
    /** select single residue */
    public void select( int seqpos);
 
    
}
