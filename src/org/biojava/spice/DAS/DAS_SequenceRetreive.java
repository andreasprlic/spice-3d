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
package org.biojava.spice.DAS                  ;

import org.biojava.spice.Config.*          ;
import java.net.URL                        ;
import java.io.InputStream                 ;
import java.net.HttpURLConnection          ;
import org.xml.sax.InputSource             ;
import org.xml.sax.XMLReader               ;
import org.xml.sax.*                       ;
import javax.xml.parsers.*                 ;
import java.util.List                      ;
import java.util.Iterator                  ;
import java.util.logging.*                 ;
import java.util.ArrayList ;
import java.util.HashMap ;
/**
 * performs a DAS - sequence request.
 * @author Andreas Prlic
 *
 */
public class DAS_SequenceRetreive {

    String sequence ;
    //String connection ;
    RegistryConfiguration config ;
    List sequenceServers  ;

    ArrayList sequencelist ;


    /**
     *  retrieve sequence for this sp_accession e.g. P00280
     */

    Logger logger ;
    public DAS_SequenceRetreive(RegistryConfiguration configuration) {
	super();
	// TODO Auto-generated constructor stub
	logger = Logger.getLogger("org.biojava.spice");
	//connection = conns ;
	config = configuration ;
	sequenceServers = config.getServers("sequence","UniProt");
	//logger.finest(sequenceServers);
	sequencelist = new ArrayList() ;
		
    }

    // enable some sort of caching ...
    
    private String getSequenceFromMemory(String swissp_id) {
	String sequence = null ;
	for ( int i = 0 ; i < sequencelist.size() ; i++) {
	    HashMap seq = (HashMap) sequencelist.get(i) ;
	    String sp = (String) seq.get("id");
	    if ( sp.equals(swissp_id)) {
		sequence = (String) seq.get("sequence");
		return sequence ;
	    }
	}
	return sequence ;
    }

    /** store sequences in memory speed up so we do not have to make
     * so many DAS calls ... */
    private void addSequenceToMemory(String sequence,String swissp_id) {
	HashMap h = new HashMap() ;
	h.put("id",swissp_id);
	h.put("sequence",sequence);
	sequencelist.add(h) ;
    }


    public String get_sequence(String sp_accession)
	throws ConfigurationException
    {
	
	String sequence ;
	
	sequence = getSequenceFromMemory(sp_accession) ;
	
	if ( sequence != null )
	    return sequence ;

	    
	sequence = "" ;
	
	//logger.finest("sequenceServers size: " + sequenceServers.size());
	if ( sequenceServers.size() == 0) {
	    Exception ex = new ConfigurationException("no UniProt sequence DAS servers found!");
	    logger.throwing(this.getClass().getName(), "get_seqeunce", ex);
	    throw (ConfigurationException)ex ;
	}
	

	
	Iterator iter = sequenceServers.iterator();
	boolean gotSequence = false ;
	while (iter.hasNext()){
		
	    if ( gotSequence ) break ;
	    
	    SpiceDasSource ds = (SpiceDasSource) iter.next();
	    String url = ds.getUrl() ;
	    char lastChar = url.charAt(url.length()-1);		 
	    if ( ! (lastChar == '/') ) 
		url +="/" ;
	    String dascmd = url + "sequence?segment=";
	    String connstr = dascmd + sp_accession ;
	    
	    try {
		
		sequence = retreiveSequence(connstr);
		gotSequence = true ;
	    }
	    catch (Exception ex) {
		//ex.printStackTrace();		
		logger.log(Level.WARNING,ex.getMessage() + " while retreiving "+connstr);
		if ( iter.hasNext()) {
		    logger.log(Level.INFO,"error while retreiving sequence, trying other server");
		} else {
		    logger.log(Level.SEVERE,"could not retreive UniProt sequence from any available DAS sequence server");
		    
		    Exception exc = new ConfigurationException("could not retreive UniProt sequence from any available DAS sequence server");
		    logger.throwing(this.getClass().getName(), "get_seqeunce", exc);
		    throw (ConfigurationException)exc ;
		}
	    
	    }		
	}
	addSequenceToMemory(sequence,sp_accession);	
	return sequence ;
    }
    private String retreiveSequence( String connstr) 
	throws Exception 
    {

	//logger.finest("trying: " + connstr) ;
	URL dasUrl = new URL(connstr);
	//DAS_httpConnector dhtp = new DAS_httpConnector() ;
	logger.info("requesting sequence from " + connstr);
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
	    //e.printStackTrace();
	    logger.log(Level.FINER,"Uncaught exception", e);
	}
	
	XMLReader xmlreader = saxParser.getXMLReader();
	
	try {
	    xmlreader.setFeature("http://xml.org/sax/features/validation", validate);
	} catch (SAXException e) {
	    logger.finer("Cannot set validation to " + validate); 
	    logger.log(Level.FINER,"Uncaught exception", e);
	}
	
	try {
	    xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",validate);
	} catch (SAXNotRecognizedException e){
	    //e.printStackTrace();
	    logger.finer("Cannot set load-external-dtd to" + validate); 
	    logger.log(Level.FINER,"Uncaught exception", e);
	    //System.err.println("Cannot set load-external-dtd to" + validate); 
	}
	
	
	//DAS_DNA_Handler cont_handle = new DAS_DNA_Handler() ;
	DAS_Sequence_Handler cont_handle = new DAS_Sequence_Handler() ;
	xmlreader.setContentHandler(cont_handle);
	xmlreader.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
	InputSource insource = new InputSource() ;
	insource.setByteStream(dasInStream);
	
	xmlreader.parse(insource);
	sequence = cont_handle.get_sequence();
	//logger.finest("Got sequence from DAS: " +sequence);
	logger.exiting(this.getClass().getName(), "retreiveSequence",  sequence);
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
		
		logger.finer("opening "+url);
		huc = (HttpURLConnection) url.openConnection();
		
		
		logger.finest(huc.getResponseMessage());
		String contentEncoding = huc.getContentEncoding();
		//logger.finest("encoding: " + contentEncoding);
		//logger.finest("code:" + huc.getResponseCode());
		//logger.finest("message:" + huc.getResponseMessage());
		inStream = huc.getInputStream();
		//logger.finest(inStream);
		
		//in	= new BufferedReader(new InputStreamReader(inStream));
		
		//String inputLine ;
		//while (null != (inputLine = in.readLine()) ) {
		
		//logger.finest(inputLine);
		//}
		
			
			
		}
		catch ( Exception ex){
			ex.printStackTrace();
			logger.log(Level.WARNING,"Uncaught exception", ex);
		}
			
		return inStream;
	}

    }
}
