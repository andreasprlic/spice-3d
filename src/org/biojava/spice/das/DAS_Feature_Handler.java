/*
 *                    BioJava development code
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
 * Created on 19.03.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.spice.das;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

import java.util.ArrayList ;
import java.util.HashMap ;

/**
 * a class to parse the response of a DAS - Feature request
 * @author Andreas Prlic
 *
 */
public class DAS_Feature_Handler  extends DefaultHandler{

	/**
	 * 
	 */
    ArrayList features ;
    boolean first_flag ;
    HashMap feature ;
    String featurefield ;
    String characterdata ;
    String dasCommand ;
    
	public DAS_Feature_Handler() {
		super();
		// TODO Auto-generated constructor stub
		features= new ArrayList() ;
		first_flag = true ;
		featurefield = "" ;
		characterdata = "";
		dasCommand = "" ;
	}

    public void setDASCommand(String cmd) { dasCommand = cmd ;}
    public String getDASCommand() { return dasCommand; }

	public ArrayList get_features() {
		return features ;
	}
	
	void start_feature(String uri, String name, String qName, Attributes atts) {
	    feature = new HashMap() ;
	    String id 	= atts.getValue("id");
	    feature.put("id",id);
	    feature.put("dassource",dasCommand);
	    characterdata = "";
	}
	
	void add_featuredata(String uri, String name, String qName) {
		//System.out.println("featurefield "+featurefield+ " data "+characterdata);
		// NOTE can have multiple lines ..
	    
	    String data = (String)feature.get(featurefield);
	    if (data != null){
	        characterdata = data + " " + characterdata;
	    }
	    
	    feature.put(featurefield,characterdata);
		featurefield = "";
		characterdata = "";
	}
	
	public void startElement (String uri, String name, String qName, Attributes atts){
	    //System.out.println("new element "+qName);
		
	    if (qName.equals("FEATURE")) 
		start_feature(uri,  name,  qName,  atts);
	    else if ( qName.equals("METHOD") || 
		      qName.equals("TYPE") ||
		      qName.equals("START") ||
		      qName.equals("END") ||
		      qName.equals("NOTE") ||
		      qName.equals("LINK") ||
		      qName.equals("SCORE")
		      ){
		characterdata ="";
		featurefield = qName ;
	    }
	    
	}
    
	public void startDocument() {
	    //System.out.println("start document");
	}
	
	public void endDocument ()	{
	    //System.out.println("adding feature " + feature);
	    //features.add(feature);
		
	}
		public void endElement(String uri, String name, String qName) {
			//System.out.println("end "+name);
			if ( qName.equals("METHOD") || 
			     qName.equals("TYPE") ||
			     qName.equals("START") ||
			     qName.equals("END") ||
			     qName.equals("NOTE") ||
			     qName.equals("LINK") ||
			     qName.equals("SCORE")
			) {
			    add_featuredata(uri,name,qName);
			}
			else if ( qName.equals("FEATURE")) {
			    //System.out.println("adding ffeature " + feature);
			    features.add(feature);
			}
		}
		
		public void characters (char ch[], int start, int length){
			//System.out.println("characters");
			for (int i = start; i < start + length; i++) {
		
				characterdata += ch[i];
			}
		
		}
		
}
