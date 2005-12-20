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
 * Created on Dec 19, 2005
 *
 */
package org.biojava.spice;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.biojava.services.das.registry.DasSource;
import org.biojava.spice.das.SpiceDasSource;



/** a class that filters SpiceDasSources based on the SpiceStartParameters
 * 
 * @author Andreas Prlic
 *
 */
public class StartParametereFilter {

    SpiceStartParameters parameters;
    String[] displayLabels;
    String[] displayDASServers;
    
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    
    public StartParametereFilter(SpiceStartParameters params) {
        super();
        parameters = params;
        displayLabels = new String[0];
        displayDASServers = new String[0];
        
        setDisplayServers(params.getDisplay());
        setDisplayLabels(params.getDisplayLabel());
        
    }

    /** convert a ";" separated list of DAS source ids e.g. DS_101;DS_102;DS_110
     * into the unique idds of DAS servers
     * @param spiceargument
     */
    private void setDisplayServers(String spiceargument){
        //logger.info("SpiceParameterFilter  " + spiceargument);
        if ( spiceargument == null )
            if ((displayLabels == null ) || ( displayLabels.length == 0)) {
                displayDASServers = new String[0];
                return;
            } else{
                displayDASServers = null;
                return;
            }
        if ( (spiceargument.equals("all")) || ( spiceargument.equals(""))) {
            if ( (displayLabels == null ) || ( displayLabels.length == 0)) {
                displayDASServers = new String[0];
                return;
            } else {
                displayDASServers = null;
                return;
            }
        }
        
        String[] spl =  spiceargument.split(";");
        if ( spl.length == 0)
            return ;
        
        
        List ds = new ArrayList();
        // process the input ...
        for ( int i = 0 ; i< spl.length ; i++){
            
            String code = spl[i];
            
            // each code must match to the following pattern:
            // something + "_" + a number.
            
            String[] codespl = code.split("_");
            if ( codespl.length != 2 ) {
                logger.warning("DAS-source id does not contain one >_< character " + code);
                return;
            }
            try {
                Integer.parseInt(codespl[1]);
            } catch (Exception e){
                logger.warning("DAS-source id does not contain a number after the >_< " + code);
                return;
            }
            
            if ( code.length() > 100){
                logger.warning("DAS-source id is too long! ("+code.length()+" > 100 chars)");
                return;
            }
            ds.add(code);
        }
        String[] ds_ids = (String[]) ds.toArray(new String[ds.size()]);
        displayDASServers = ds_ids;
        if ( (displayLabels != null) && (displayLabels.length == 0)) 
            displayLabels = null;
    }
    
    private void setDisplayLabels(String spiceargument){
        //logger.info("FeatureFetcher got labels " + spiceargument);
        if ( spiceargument == null ) {
            if ( (displayDASServers == null ) || (displayDASServers.length == 0))
                displayLabels = new String[0];
            else
                displayLabels = null;
            return;
        }
        
        if ( spiceargument.equals("all")){
            if ((displayDASServers == null ) || ( displayDASServers.length == 0))
                displayLabels = new String[0];
            else 
                displayLabels = null;
            return;
        }
        
        
        String[] spl =  spiceargument.split(";");
        if ( spl.length == 0)
            return ;
        
        List ds = new ArrayList();
        // process the input ...
        for ( int i = 0 ; i< spl.length ; i++){
            String label = spl[i];
            if ( label.length() > 40){
                logger.warning("Label length is too long! (" + label.length() +">40)");
                continue;
            }
            ds.add(label);
        }
        String []label_ids = (String[])ds.toArray(new String[ds.size()]);
        displayLabels = label_ids;
        if ( (displayDASServers != null ) && (displayDASServers.length == 0)){
            displayDASServers = null;
        }
        
    }
    
    
    
    public SpiceDasSource[] filterSources(SpiceDasSource[] allSources){
        List servers = getUserRequestedServers(allSources);
        
        return (SpiceDasSource[]) servers.toArray(new SpiceDasSource[servers.size()]);
    }
    
    
    private boolean isInDisplayServers(SpiceDasSource ds){
        // no servers provided, but label
        if ( displayDASServers == null )
            return false;
        if ( displayDASServers.length == 0)
            return true ;
        
        // check if in ids
        String id = ds.getId();
        for ( int i=0; i< displayDASServers.length;i++){
            String testid = displayDASServers[i];
            if ( testid.equals(id)){
                logger.info("isInDisplayServers " + ds);
                return true;
            }
        }
        
        return false;
    }
    
    private boolean hasCapability(String capability, DasSource ds){
        String[] capabilities = ds.getCapabilities() ;
        for ( int c=0; c<capabilities.length ;c++) {
            String capabil = capabilities[c];
            if ( capability.equals(capabil)){
                return true;
            }
        }
        return false;
    }
    
    /** seleect only those servers, that the user wants to see.
     * 
     * @param servers
     * @return list of requested servers.
     */
    private List getUserRequestedServers(SpiceDasSource[] servers){
        
        
        // PART I :  if nothing provided return all */
        if (   ( displayDASServers != null )
                && ( displayDASServers.length == 0 ) 
                && ( displayLabels != null )
                && ( displayLabels.length == 0)) {
            
            List retlst = new ArrayList();  
            for (int i = 0 ; i < servers.length; i++){
                
                SpiceDasSource ds = servers[i];
                
                
                //System.out.println(ds.getNickname() + ds.getStatus());
                //logger.info("getUserRequestServers o " + ds.getNickname() +" " + ds.getStatus());
                // skip disabled servers ...
                if ( ds.getStatus() == false ){
                    //logger.info("skipping das source " + ds.getNickname());
                    continue;
                }

                 
                
                retlst.add(ds);
            }
            return retlst;
        }
        
        // PART II: iterate over all servers and select only those that match
        List retlst = new ArrayList();  
        for (int i = 0 ; i < servers.length; i++){
            
            SpiceDasSource ds = servers[i];
            //System.out.println(ds.getNickname() + ds.getStatus());
            //logger.info("getuserRequestServers u" + ds.getNickname() +" " + ds.getStatus());
            // skip disabled servers ...
            if ( ds.getStatus() == false ){
                //logger.info("skipping das source " + ds.getNickname());
                continue;
            }
            
            // never skip reference servers ...
            if (( hasCapability("sequence",ds)) || 
                    ( hasCapability("structure",ds)) ||
                    ( hasCapability("alignment",ds))){
                    retlst.add(ds);
                    continue;
            }
            
//          always display user config servers
            if (! ds.getRegistered()){
                retlst.add(ds);
                //logger.info("adding local DAS source " + ds);
                continue;
            }
            if ( isInDisplayLabels(ds)) {
                retlst.add(ds);
                continue;
            }
            if( isInDisplayServers(ds)){
                retlst.add(ds);
                continue;
            }
            
            
            
        }
        return retlst;
    }
    
    
    private boolean isInDisplayLabels(SpiceDasSource ds){
        // no labels provided but servers
        if ( displayLabels == null ) return false;
        if ( displayLabels.length == 0 )
            return true;
        
        //      check if in labels;
        String[] labels = ds.getLabels();
        if ( labels != null){
            for ( int i = 0 ; i< labels.length ; i++){
                String label = labels[i];
                
                for ( int l = 0 ; l< displayLabels.length;l++){
                    String dlabel = displayLabels[l];
                    if ( label.equals(dlabel)){
                        return true;
                    }
                }
            }
        }
        return false ;
    }
    
}
