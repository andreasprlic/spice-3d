/**
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
package org.biojava.spice.DAS;


import java.net.URL                         ;
import java.io.InputStream                  ;
import org.xml.sax.InputSource              ;
import org.xml.sax.XMLReader                ;
import javax.xml.parsers.*                  ;
import org.xml.sax.SAXException             ;
import org.xml.sax.*                        ;
import java.util.ArrayList                  ;
import java.util.logging.*                  ;
import java.net.HttpURLConnection           ;


/**
 * A class to perform a DAS features request
 * @author Andreas Prlic
 *
 */
public class DAS_FeatureRetreive {

	ArrayList features ;
    Logger logger     ;
    
	/**
	 * 
	 */
	public DAS_FeatureRetreive(URL url) {
		super();
		// TODO Auto-generated constructor stub
		logger = Logger.getLogger("org.biojava.spice");
		features = new ArrayList() ;
		
		try {
			
		    //DAS_httpConnector dhtp = new DAS_httpConnector() ;
		    //System.out.println("DasFeatureRetureive"+url);
		    InputStream dasInStream = null;
		    try {
			dasInStream	= open(url); 
		    } catch (Exception e ){
			
			logger.log(Level.WARNING,"could not open connection to " + url,e);
			return ;
		    }
			
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
			if ( vali != null )
			    if ( vali.equals("true") ) 
				validation = true ;
			
			
			XMLReader xmlreader = saxParser.getXMLReader();
			
			//XMLReader xmlreader = XMLReaderFactory.createXMLReader();
			try {
			    xmlreader.setFeature("http://xml.org/sax/features/validation", validation);
			} catch (SAXException e) {
			    logger.log(Level.WARNING,"Cannot set validation " + validation); 
			}
			
			try {
			    xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",validation);
			} catch (SAXNotRecognizedException e){
			    //e.printStackTrace();
			    logger.log(Level.WARNING,"Cannot set load-external-dtd "+validation); 
			    
			}
			
			DAS_Feature_Handler cont_handle = new DAS_Feature_Handler() ;
			cont_handle.setDASCommand(url.toString());
			xmlreader.setContentHandler(cont_handle);
			xmlreader.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
			InputSource insource = new InputSource() ;
			insource.setByteStream(dasInStream);
			
			try {
			    xmlreader.parse(insource);			
			    features = cont_handle.get_features();
			} 
			catch ( Exception e){
			    logger.log(Level.WARNING,"error while parsing response from "+ url);
			    
			    features = new ArrayList();
			}
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}


    private InputStream open(URL url)
	throws java.io.IOException, java.net.ConnectException
    {
	InputStream inStream = null;

				
	HttpURLConnection huc = null;
	logger.finest("opening "+url);
	huc = (HttpURLConnection) url.openConnection();
	
	
	logger.finest("got connection: "+huc.getResponseMessage());
	String contentEncoding = huc.getContentEncoding();
	inStream = huc.getInputStream();		
	return inStream;
    
    }
	
    /** returns a List of Features */
    public ArrayList get_features() {
	logger.finest("DAS_FeatureRetrieve: returning features");
	logger.finest(features.toString());
	return features;
    }

	
}
