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
 * Created on Nov 7, 2005
 *
 */
package org.biojava.spice.manypanel.drawable;


import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;


public class DrawableStructure 
extends DrawableSequence {

    Structure structure ;
    
    int currentChainNumber ;
    
    public DrawableStructure() {
        super();

    }

   public Structure getStructure() {
       return structure;
   }
    
    
    public void setStructure(Structure structure){
        this.structure = structure ;
        
    }

    public void setCurrentChainNumber(int chainPosition){
        this.currentChainNumber = chainPosition;
        Chain c = structure.getChain(chainPosition);
        setSequence(c);
    }
    
    public int getCurrentChainNumber(){
        return currentChainNumber;
    }
}
