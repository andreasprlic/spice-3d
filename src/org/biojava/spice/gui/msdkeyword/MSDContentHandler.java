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
 * Created on Aug 1, 2005
 *
 */
package org.biojava.spice.gui.msdkeyword;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * @author Andreas Prlic
 *
 */
public class MSDContentHandler  extends DefaultHandler{
    
    /**
     * a class that parses the XML response of a MSD - keyword search.
     * @author Andreas Prlic
     *
     */
    
    List depositions ;
    Deposition depo;
    String txt;
    List suggestions;
    
    /**
     * 
     */
    public MSDContentHandler() {
        super();
        depositions = new ArrayList();
        depo = new Deposition();
        suggestions = new ArrayList();
        
    }

    public Deposition[] getDepositions(){
        return (Deposition[]) depositions.toArray(new Deposition[depositions.size()]);
    }
    
    
    public String[] getSuggestions(){
        return (String[]) suggestions.toArray(new String[suggestions.size()]);
    }
    
    public void startElement (String uri, String name, String qName, Attributes atts){
        // reset the character string ...
        txt = "";
        
        //<entry id="entry_42769 " accessionCode="3al1" lastModified="2001-09-26" resolution="0.75" rfactor="0.0" hasStructureFactors="true"
	    if ( qName.equals("entry")){
	        depo = new Deposition();
	        
	        String accessionCode = atts.getValue("accessionCode");
	        depo.setAccessionCode(accessionCode);
	        String resolution = atts.getValue("resolution");
	        float res = Float.parseFloat(resolution);
	        depo.setResolution(res);
	        String rfactor = atts.getValue("rfactor");
	        float rfac = Float.parseFloat(rfactor);
	        depo.setRfactor(rfac);
	        String id = atts.getValue("id");
	        //int i = Integer.parseInt(id);
	        depo.setId(id);
	        String d = atts.getValue("lastModified");
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        Date date = new Date() ;
	        try {
	            date = sdf.parse(d);
	        } catch (Exception e){
	            //e.printStackTrace();
	            
	        }
	        depo.setLastModified(date);
	    }
	    
	    
		
	}
    
    public void endElement (String uri,String name, String qName){
        if ( qName.equals("classification")) {
		    //System.out.println("adding ffeature " + feature);
		    depo.setClassification(txt);
		}
        else if ( qName.equals("expData")){
            depo.setExpData(txt);
        } else if ( qName.equals("title")){
            depo.setTitle(txt);
        }
        else if ( qName.equals("entry")){
            depositions.add(depo);
        } 
        else if ( qName.equals("suggestion")){
            suggestions.add(txt);
	    }
        
    }
    
	
	public void characters (char ch[], int start, int length){
	    for (int i = start; i < start + length; i++) {
	        txt += ch[i];
	    }
	
	}
}
