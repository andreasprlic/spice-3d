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
 * Created on Oct 31, 2005
 *
 */
package org.biojava.spice.manypanel.eventmodel;

import org.biojava.bio.structure.*;

public class StructureEvent {

    String pdbCode;
    Structure structure;
    int currentChainNumber;
    
    /** the PDB code ( 4 chars ) */
    public StructureEvent(String pdbCode){
        this.pdbCode = pdbCode.substring(0,4);
        structure = new StructureImpl();
        currentChainNumber = -1;
    }
    
    public StructureEvent(Structure s) {
        this(s,0);
    }
    public StructureEvent(Structure s, int activeChain) {
        super();

        this.structure = s;
        this.pdbCode = structure.getPDBCode();
        currentChainNumber = activeChain;
    }

    /** returns the chain number of the current even
     * 
     * @return
     */
    public int getCurrentChainNumber(){
        return currentChainNumber;
    }
    
    public Structure getStructure(){
            return structure;
    }
    
    
    public String getPDBCode(){
        return pdbCode;
    }
}
