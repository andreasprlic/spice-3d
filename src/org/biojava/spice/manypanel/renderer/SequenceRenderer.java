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
package org.biojava.spice.manypanel.renderer;



import java.util.Iterator;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.spice.manypanel.drawable.*;


public class SequenceRenderer 

extends AbstractChainRenderer
{

    public static final long serialVersionUID = 2019458429153091l;
    
    public SequenceRenderer() {
        super();
        
        //logger.info("init sequenceRenderer");
        
        sequence = new DrawableSequence("",new ChainImpl());
        featurePanel = new SequenceScalePanel();
        cursorPanel  = new CursorPanel();
        statusPanel.setName("UniProt");
        initPanels();
        
    }

    public void setDrawableSequence(DrawableSequence sequence) {
        statusPanel.setLoading(false);
        //logger.info("setting drawable sequence ");
        this.sequence=sequence;
        statusPanel.setAccessionCode(sequence.getAccessionCode());
        featurePanel.setChain(sequence.getSequence());
        cursorPanel.setChain(sequence.getSequence());
        columnCursor.setChain(sequence.getSequence());
        mouseListener.setChain(sequence.getSequence());
        toolTipper.setChain(sequence.getSequence());
        
        Iterator iter = dasSourcePanels.iterator();
        while (iter.hasNext()){
            DasSourcePanel dsp = (DasSourcePanel)iter.next();
            dsp.setChain(sequence.getSequence());
        }
        
        calcScale(100);
       
    }
    
    public void clearDisplay(){
        super.clearDisplay();
        
        DrawableSequence d = new DrawableSequence("");
        Chain c = new ChainImpl();
        d.setSequence(c);
        setDrawableSequence(d);
    }
    
    
  

  

   

}
