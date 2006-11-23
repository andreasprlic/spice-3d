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
 * Created on Oct 4, 2006
 *
 */
package org.biojava.spice.alignment;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.biojava.bio.Annotation;
import org.biojava.spice.gui.aligchooser.MenuAlignmentListener;

public  class StructureAlignmentComparator implements Comparator{

    
    private final String field;
    
    public StructureAlignmentComparator(String field) {
        super();
        this.field = field;
    }
    
        
    private Double getValue(List details, String propfield){
        Iterator iter1 = details.iterator();
        Double val = null;
        
        while (iter1.hasNext()){
            Annotation detail = (Annotation) iter1.next();
            String property = (String) detail.getProperty("property");
           
            if ( property == null )
                return null;
           
            if ( property.equals(propfield)) {
                String data     = (String) detail.getProperty("detail");
                try {
                    double d = Double.parseDouble(data);
                    val = new Double(d);
                } catch (Exception e){}
            }
            
        }
        return val;
    }
    
    public int compare(Object arg0, Object arg1) {
        
        Annotation o1 = (Annotation) arg0;
        Annotation o2 = (Annotation) arg1;
        
        
        if ( field.equals(MenuAlignmentListener.SORT_BY_NAME)) {
        	String s1 = (String)o1.getProperty("dbAccessionId");
        	String s2 = (String)o2.getProperty("dbAccessionId");
        	//System.out.println(s1 + " " + s2) ;
        	return s1.compareToIgnoreCase(s2);
        }
        List det1 = (List) o1.getProperty("details");
        List det2 = (List) o2.getProperty("details");
        
        Double d1 = getValue(det1,field);
        Double d2 = getValue(det2,field);
        
        if  ( ( d1 == null) && (d2 == null )) {
            return 0;
        }
        if ( d1 == null)
            return -1;
        if ( d2 == null)
            return 1;
        
        return d1.compareTo(d2);
        
        
    }
    
    
    
    
}
