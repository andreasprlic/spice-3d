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
import java.net.HttpURLConnection;
import org.xml.sax.InputSource ;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

/**
 * @author andreas
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DAS_SequenceRetreive {

    String sequence ;
    String connection ;

    String sequences ;

    /**
     *  retrieve sequence for this sp_accession e.g. P00280
     */

    
    public DAS_SequenceRetreive(String conns) {
	super();
	// TODO Auto-generated constructor stub
	
	connection = conns ;
	
		 
		
    }

    public String get_sequence(String sp_accession){


	try {
	    String connstr = connection + sp_accession ;

	    System.out.println(connstr) ;
	    URL dasUrl = new URL(connstr);
	    //DAS_httpConnector dhtp = new DAS_httpConnector() ;
			
	    InputStream dasInStream =open(dasUrl); 
			

	    SAXParserFactory spfactory =
		SAXParserFactory.newInstance();
	    
	    String vali = System.getProperty("XMLVALIDATION");
	    boolean validate = false ;
	    if ( vali.equals("true") ) 
		validate = true ;
	    spfactory.setValidating(validate);
			
	    SAXParser saxParser = null ;
			
	    try{
		saxParser =
		    spfactory.newSAXParser();
	    } catch (ParserConfigurationException e) {
		e.printStackTrace();
	    }
	
	    XMLReader xmlreader = saxParser.getXMLReader();
			
	    try {
		xmlreader.setFeature("http://xml.org/sax/features/validation", validate);
	    } catch (SAXException e) {
		System.err.println("Cannot set validation to " + validate); 
	    }
			
	    try {
		xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",validate);
	    } catch (SAXNotRecognizedException e){
		//e.printStackTrace();
		System.err.println("Cannot set load-external-dtd to" + validate); 
	    }


	    //DAS_DNA_Handler cont_handle = new DAS_DNA_Handler() ;
	    DAS_Sequence_Handler cont_handle = new DAS_Sequence_Handler() ;
	    xmlreader.setContentHandler(cont_handle);
	    xmlreader.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
	    InputSource insource = new InputSource() ;
	    insource.setByteStream(dasInStream);
		
	    xmlreader.parse(insource);
	    sequence = cont_handle.get_sequence();
	    System.out.println("Got sequence from DAS: " +sequence);
			
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	}


	return sequence ;
    }

    private InputStream open(URL url) {
	{
	    // TODO Auto-generated method stub
	    
	    InputStream inStream = null;
	    try{
		
		/// PROXY!!!!
		//String proxy = "wwwcache.sanger.ac.uk";
		//String port = "3128" ;
		//Properties systemProperties = System.getProperties();
		//systemProperties.setProperty("proxySet", "true" );
		//	systemProperties.setProperty("http.proxyHost",proxy);
		//	systemProperties.setProperty("http.proxyPort",port);
		
			
		HttpURLConnection huc = null;
		//huc = (HttpURLConnection) dasUrl.openConnection();
		
		//huc = proxyUrl.openConnection();
		
		System.out.println("opening "+url);
		huc = (HttpURLConnection) url.openConnection();
		
		
		System.out.println(huc.getResponseMessage());
		String contentEncoding = huc.getContentEncoding();
		//System.out.println("encoding: " + contentEncoding);
		//System.out.println("code:" + huc.getResponseCode());
		//System.out.println("message:" + huc.getResponseMessage());
		inStream = huc.getInputStream();
		//System.out.println(inStream);
		
		//in	= new BufferedReader(new InputStreamReader(inStream));
		
		//String inputLine ;
		//while (null != (inputLine = in.readLine()) ) {
		
		//System.out.println(inputLine);
		//}
		
			
			
		}
		catch ( Exception ex){
			ex.printStackTrace();
		}
			
		return inStream;
	}

    }
}
