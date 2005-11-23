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
 * Created on Jul 11, 2005
 *
 */
package org.biojava.spice.Panel.seqfeat;

import java.util.List;

import org.biojava.spice.SpiceStartParameters ;
import org.biojava.spice.SPICEFrame ;
import org.biojava.spice.Config.RegistryConfiguration;
import org.biojava.spice.das.SpiceDasSource;



/** change the availablility of DasSources for the display. 
 * 
 * 
 * @author Andreas Prlic
 *
 */
public class SpiceDasServerConfigListener 
implements DasServerConfigListener
{

    	SPICEFrame parent;
    /**
     * 
     */
    public SpiceDasServerConfigListener(SPICEFrame parent) {
        super();
        this.parent = parent;
      
    }
    
    /** display all available DAS servers */
    public void enableAllDasSources(){
        SpiceStartParameters params = parent.getSpiceStartParameters();
        params.setDisplay ("all");
        params.setDisplayLabel("all");
        parent.setSpiceStartParameters(params);
        RegistryConfiguration config = parent.getConfiguration();
        
        List servers =  config.getAllServers();
        for (int i = 0 ; i < servers.size(); i++) {
            config.setStatus(i,true);
        }
        
        parent.setCurrentChainNumber(parent.getCurrentChainNumber());
        
    }
    /** disable the display of all das sources */
    public void disableAllDasSources(){

        SpiceStartParameters params = parent.getSpiceStartParameters();
        params.setDisplay ("all");
        params.setDisplayLabel("all");
        parent.setSpiceStartParameters(params);
        
        RegistryConfiguration config = parent.getConfiguration();
        
        List servers =  config.getAllServers();
        for (int i = 0 ; i < servers.size(); i++) {
            config.setStatus(i,false);
        }
        
        parent.setCurrentChainNumber(parent.getCurrentChainNumber());
         
    }
    
    /** remove a particular DAS source from the display */
    public void disableDasSource(String uniqueId){

        
        RegistryConfiguration config = parent.getConfiguration();
        
        List servers =  config.getAllServers();
        for (int i = 0 ; i < servers.size(); i++) {
            SpiceDasSource s = (SpiceDasSource) servers.get(i) ;
          
            String sid = s.getId();
            if ( sid.equals(uniqueId) ) {
               // System.out.println(s);
                config.setStatus(i,false);
                //s.setStatus(false);
                //System.out.println(config.getStatus(i));
            }	    
        }
       // parent.setCurrentChainNumber(parent.getCurrentChainNumber());
    }
    

}
