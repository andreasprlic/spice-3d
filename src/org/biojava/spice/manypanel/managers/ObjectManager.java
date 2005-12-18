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
package org.biojava.spice.manypanel.managers;

import org.biojava.services.das.registry.DasCoordinateSystem;
import  org.biojava.services.das.registry.DasSource;
import org.biojava.spice.manypanel.drawable.*;


public interface ObjectManager {
    
    
        public void setCoordinateSystem(DasCoordinateSystem coordSys);
        
        public DasCoordinateSystem getCoordinateSystem();
        
        
        /** list of available Reference DAS servers 
         * 
         * @param dasSources
         */
        public void setDasSources(DasSource[] dasSources);
        
        /** get the list of available Reference DAS servers 
         * 
         * @return 
         */
        
        public DrawableDasSource[] getDasSources();
               
}
