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
 * Created on Nov 18, 2005
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.util.List;
import java.util.ArrayList;
import org.biojava.spice.manypanel.drawable.*;

public class FeatureRenderer {
    
    List dasSources;
    public FeatureRenderer() {
        super();
        clearDasSources();
    }
    
    public void clearDasSources(){
        //logger.finest("featureRenderer clearDasSources");
        dasSources = new ArrayList();
    }
    
    
    
    public void addDrawableDasSource(DrawableDasSource draw){
        dasSources.add(draw);
    }
    
    public DrawableDasSource[] getDrawableDasSources(){
        return (DrawableDasSource[]) dasSources.toArray(new DrawableDasSource[dasSources.size()]);
    }
    
    public void removeDrawableDasSource(DrawableDasSource draw){
        dasSources.remove(draw);
    }
    
    
    
    
    
}
