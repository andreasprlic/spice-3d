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
package org.biojava.spice.manypanel.renderer;


import java.util.Iterator;
import java.util.logging.*;
import org.biojava.bio.structure.*;
import org.biojava.spice.manypanel.drawable.DrawableSequence;
import org.biojava.spice.manypanel.drawable.DrawableStructure;


public class StructureRenderer 
extends AbstractChainRenderer

{

    final static long serialVersionUID = 98237492837402374L;
    
    DrawableStructure structure;
    //StructureFeaturePanel featurePanel;
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
   
    
    public StructureRenderer() {
        //super();
        sequence = new DrawableSequence("",new ChainImpl());
       
        //logger.info("init StructureRenderer");
        
        featurePanel = new StructureScalePanel();
        cursorPanel  = new CursorPanel();  
        
        statusPanel.setName("PDB");
        structure = new DrawableStructure("");
        
        initPanels();
         
    }
    
    
    
    
    /** if a structure region is provided, then 
     * a "feature is drawn in the structure panel that
     * highlights this region. e.g. used to visualize the Sisyphus
     * regions
     *
     */
    public void setStructureRegion(){
        
    }

    
    public void clearDisplay(){
        super.clearDisplay();
        
        Structure s = new StructureImpl();
        s.setPDBCode("");
        Chain c = new ChainImpl();
        s.addChain(c);
        
        DrawableStructure draw = new DrawableStructure("");
        draw.setStructure(s);
        setDrawableStructure(draw);
        
    }
    
       
    public void setDrawableStructure(DrawableStructure draw){
        statusPanel.setLoading(false);
        //logger.info("got new DrawableStructure");
        synchronized(structure){
            structure =draw;
        }

        Structure struc = structure.getStructure();
        Chain c = struc.getChain(structure.getCurrentChainNumber());

        String ac = struc.getPDBCode() + "." + c.getName();
        DrawableSequence ds = DrawableSequence.fromChain(ac,c);
        this.sequence=ds;
        
        statusPanel.setAccessionCode(ac);
        featurePanel.setChain(c);
        cursorPanel.setChain(c);
        columnCursor.setChain(c);
        mouseListener.setChain(c);
        toolTipper.setChain(c);
        
        Iterator iter = dasSourcePanels.iterator();
        while (iter.hasNext()){
            DasSourcePanel dsp = (DasSourcePanel)iter.next();
            dsp.setChain(c);
        }
        calcScale(100);
        

    }

}
