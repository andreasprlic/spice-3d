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
 * Created on Jan 11, 2007
 *
 */
package org.biojava.spice.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.biojava.dasobert.das.DAS_FeatureRetrieve;
import org.biojava.spice.config.SpiceDefaults;

public class UniProtAccessionCodeTools {

    
    static Logger logger = Logger.getLogger(SpiceDefaults.LOGGER);
    
    public UniProtAccessionCodeTools() {
        super();

    }
    
    /** test if a code is a uniprot entry name
     * e.g. BRCA1_HUMAN
     * @param code
     * @return flag
     */
    public static final boolean isEntryName(String code){
        
        
        if ( code.indexOf("_") > 0 ){
            return true;
        }
        
        return false;
        
    }
    
    /** translate a unitprot entry name to the corresponding accession code
     * 
     * @param entry_name
     * @return the id or an empty string if the translation could not be performed
     */
    public static final String translateName2Accession(String entry_name){
     
        String translationURL = "http://www.ebi.ac.uk/das-srv/uniprot/das/aristotle/features?segment=";
        translationURL += entry_name;
        URL cmd ;
        try {
         cmd = new URL(translationURL);
        } catch (MalformedURLException ex){
            ex.printStackTrace();
            return "";
        }
        
        logger.info("converting swiss prot entry name to accession code: " + entry_name);
        DAS_FeatureRetrieve ftmp = new DAS_FeatureRetrieve(cmd);
        List features = ftmp.get_features();
        
        if ( features.size() > 1) {
            Map f1 = (Map)features.get(0);
            String ac = (String)f1.get("id");
            
            return ac;            
        }
        logger.info("could not convert...");    
        return "";
        
    }
    
    public static void main(String[] args){
        String name = "BRCA1_HUMAN";
        String ac = UniProtAccessionCodeTools.translateName2Accession(name);
        System.out.println(ac);
    }

}
