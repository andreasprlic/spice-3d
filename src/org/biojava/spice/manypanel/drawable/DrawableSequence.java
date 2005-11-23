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
package org.biojava.spice.manypanel.drawable;

import java.awt.Color;
import java.util.List;



import org.biojava.bio.structure.*;


/** at its core a DraawableSequence is also a Structure obbject - but without coordinates
 * 
 * @author Andreas Prlic
 *
 */
public class DrawableSequence 
 
implements Drawable

{
    Chain sequence;
    boolean loading;

    
    public DrawableSequence(){
        this.sequence = new ChainImpl();
        this.loading = false;
        
        
    }
    public DrawableSequence(Chain sequence){
        super();
        this.sequence = sequence;
        
    }
    
    public static DrawableSequence fromChain(Chain sequence){
        List aminos = sequence.getGroups("amino");
        Chain newChain = new ChainImpl();
        for ( int i=0 ; i< aminos.size(); i++){
            AminoAcid a = (AminoAcid)aminos.get(i);
            newChain.addGroup(a);
        }
        newChain.setName(sequence.getName());
        newChain.setSwissprotId(sequence.getSwissprotId());
        newChain.setAnnotation(sequence.getAnnotation());
        return new DrawableSequence(newChain);
    }
    
  

    public Color getColor() {
        
        return null;
    }

    public void setColor(Color col) {

        
    }

    public boolean getLoading() {
       
        return loading;
    }

    public void setLoading(boolean flag) {
       loading = flag;
        
    }

    public Chain getSequence() {
        // TODO Auto-generated method stub
        return sequence;
    }

    public void setSequence(Chain sequence) {
        this.sequence = sequence;
    }

 
  
    
    
}
