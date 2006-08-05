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
 * Created on Aug 4, 2006
 *
 */
package org.biojava.spice.config;

import java.util.ArrayList;
import java.util.List;

import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;

/** a class that searches all DasSources for a particular keyword and returns a list of DasSources that match it
 * 
 * @author Andreas Prlic
 * @since 5:13:19 PM
 * @version %I% %G%
 */
public class SearchDasSourceManager {

    /** search all sources for a particular keyword and return matching das sources
     * 
     * @param sources
     * @param text the keyword to search for
     * @return das sources that contain this keyword
     */
    public static  SpiceDasSource[] searchForKeyword(SpiceDasSource[] sources, String text){
        
        List newSources = new ArrayList();
        
        for (int i = 0 ; i < sources.length;i++){
            SpiceDasSource s = sources[i];
            
            if ( containsKeyword(s, text)) {
                newSources.add(s);
            }
        }
        
        return (SpiceDasSource[])newSources.toArray(new SpiceDasSource[newSources.size()]);
    }

    /** check if a das source contains a keyword
     * 
     * @param s the DAS Source
     * @param text the keyword to search for
     * @return returns true if this keyword is contained in the source
     */
    public static boolean containsKeyword(SpiceDasSource s, String text){
        
        String keyword = text.toLowerCase();
        
        if ( contains (s.getDescription(),keyword))
            return true;
        
        if (contains (s.getNickname(),keyword))
            return true;
        
        if ( contains (s.getUrl(),keyword))
            return true;
        
        if ( contains (s.getAdminemail(),keyword))
            return true;
        
        if ( contains (s.getId(),keyword ))
            return true;
        
        String[] labels = s.getLabels();
        for (int i=0;i< labels.length;i++){
            String label = labels[i];
            if ( contains (label, keyword))
                return true;                                  
        }
        
        DasCoordinateSystem[] coords = s.getCoordinateSystem();
        for (int i=0 ; i< coords.length;i++){
            DasCoordinateSystem ds = coords[i];
            if ( contains (ds.toString(), keyword))
                return true;
            String testCode = ds.getTestCode();
            if ( contains ( testCode, keyword))
                return true;
        }
        
        if ( contains ( s.getHelperurl(), keyword))
            return true;
        
        
        return false;
        
    }
    
    /** do a lowercase of field and check if the keyword is in it
     * this is because indexOf is case sensitive
     * @param field
     * @param keyword
     * @return flag if is contained
     */
    private static boolean contains(String field, String keyword){
        if ( field == null)
            return false;
        
        String lowerfield = field.toLowerCase();
        
        
        if ( lowerfield.indexOf(keyword) > -1) {
            //System.out.println(field + " " + keyword);            
            return true;
        }
        return false;
    }
}
