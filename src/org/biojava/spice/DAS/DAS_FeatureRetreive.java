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
package org.biojava.spice;


import java.net.URL;
import java.io.InputStream ;
import org.xml.sax.InputSource ;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
import javax.xml.parsers.*;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource ;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.*;
import org.xml.sax.*;
import java.util.ArrayList ;
import java.net.HttpURLConnection;


/**
 * @author andreas
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DAS_FeatureRetreive {

	ArrayList features ;
	/**
	 * 
	 */
	public DAS_FeatureRetreive(URL url) {
		super();
		// TODO Auto-generated constructor stub
		features = new ArrayList() ;
		
		try {
			
		    //DAS_httpConnector dhtp = new DAS_httpConnector() ;
			//System.out.println("DasFeatureRetureive"+url);
			

			InputStream dasInStream	= open(url); 
			
			//SAXParserFactory spf = SAXParserFactory.newInstance();
			//spf.setNamespaceAware(true);
			//XMLReader xmlreader = spf.newSAXParser().getXMLReader();


			//System.setProperty("org.xml.sax.driver", 
			//	   "org.apache.crimson.parser.XMLReaderImpl");

			SAXParserFactory spfactory =
			    SAXParserFactory.newInstance();
			
			spfactory.setValidating(false);
			
			SAXParser saxParser = null ;
			
			try{
			    saxParser =
				spfactory.newSAXParser();
			} catch (ParserConfigurationException e) {
			    e.printStackTrace();
			}
	
			String vali = System.getProperty("XMLVALIDATION");
			boolean validation = false ;
			if ( vali.equals("true") ) {
			    validation = true ;
			}
			
			XMLReader xmlreader = saxParser.getXMLReader();
			
			//XMLReader xmlreader = XMLReaderFactory.createXMLReader();
			try {
			    xmlreader.setFeature("http://xml.org/sax/features/validation", validation);
			} catch (SAXException e) {
			    System.err.println("Cannot set validation " + validation); 
			}
			
			try {
			    xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",validation);
			} catch (SAXNotRecognizedException e){
			    //e.printStackTrace();
			    System.err.println("Cannot set load-external-dtd "+validation); 
			    
			}
			
			DAS_Feature_Handler cont_handle = new DAS_Feature_Handler() ;
			xmlreader.setContentHandler(cont_handle);
			xmlreader.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
			InputSource insource = new InputSource() ;
			insource.setByteStream(dasInStream);
		
			xmlreader.parse(insource);
			
			features = cont_handle.get_features();
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}


    private InputStream open(URL url){
	InputStream inStream = null;
	    try{
				
		HttpURLConnection huc = null;
		System.out.println("opening "+url);
		huc = (HttpURLConnection) url.openConnection();
		
		
		System.out.println(huc.getResponseMessage());
		String contentEncoding = huc.getContentEncoding();
		inStream = huc.getInputStream();		
		}
		catch ( Exception ex){
			ex.printStackTrace();
		}
			
		return inStream;
    
    }
	
    public ArrayList get_features() {
	//System.out.println("DAS_FeatureRetrieve: returning features");
	return features;
    }

	
}
