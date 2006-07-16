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


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
 

/**
 * request all PDB accession codes that match to a particular keyword.
 * All data is retrieved from e.g.
 * http://www.ebi.ac.uk/msd-srv/msdsite/entryQueryXML?act=getall&searchOptions=%26keyword=histone";
 * 
 * @author Andreas Prlic
 *
 */
public class MSDKeywordSearch {
    
    public static String MSDLOCATION = "http://www.ebi.ac.uk/msd-srv/msdsite/entryQueryXML?act=getall&searchOptions=%26keyword=";
    
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    String[] suggestions;
    
    /**
     * 
     */
    public MSDKeywordSearch() {
        super();
        suggestions = new String[0];
    }
    
    public Deposition[] search( String keyword){
        	suggestions = new String[0];
        	
        // remove trainling and ending spaces ...
        keyword = keyword.trim();
        
        if ( keyword.equals("")){
            return new Deposition[0];
        }
        keyword = keyword.replaceAll(" ","%26");
        //System.out.println("keyword: " + keyword);
        URL url;
        try {
            String msdrequest =MSDLOCATION + keyword;
            logger.info("requesting " + msdrequest);
            url = new URL(msdrequest);
        }
        catch (MalformedURLException e){
            e.printStackTrace();
            return null;
        }
        
        InputStream dasInStream =open(url); 
        
        
        SAXParserFactory spfactory =
            SAXParserFactory.newInstance();
        
        String vali = System.getProperty("XMLVALIDATION");
        
        boolean validate = false ;
        if ((vali != null) && ( vali.equals("true")) ) 
            validate = true ;
        spfactory.setValidating(validate);
        
        SAXParser saxParser = null ;
        try {
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
            MSDContentHandler cont_handle = new MSDContentHandler();
            xmlreader.setContentHandler(cont_handle);
            xmlreader.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
            InputSource insource = new InputSource() ;
            insource.setByteStream(dasInStream);
            
            xmlreader.parse(insource);
            Deposition[] depos = cont_handle.getDepositions();
            //logger.finest("Got sequence from DAS: " +sequence);
            suggestions = cont_handle.getSuggestions();
            
            return depos ;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }    
        
    }
    
    public String[] getSuggestions(){
        return suggestions;
    }
    
    /** open an InputStream to the url below. Requests data in gzip encoding, if supported
     * 
     * @param url
     * @return
     */
    private InputStream open(URL url) {
        {
            
            InputStream inStream = null;
            try{
                
              
                /*String proxy = "wwwcache.sanger.ac.uk";
                String port = "3128" ;
                Properties systemProperties = System.getProperties();
                systemProperties.setProperty("proxySet", "true" );
                systemProperties.setProperty("http.proxyHost",proxy);
                systemProperties.setProperty("http.proxyPort",port);
                */
                
                HttpURLConnection huc = null;
               
                huc = org.biojava.spice.SpiceApplication.openHttpURLConnection(url);
                huc.setRequestProperty("Accept-Encoding", "gzip");
                logger.finest(huc.getResponseMessage());
                String contentEncoding = huc.getContentEncoding();
            	
            		inStream = huc.getInputStream();	

            		if (contentEncoding != null) {
            		    if (contentEncoding.indexOf("gzip") != -1) {
            		// 	we have gzip encoding
            		        inStream = new GZIPInputStream(inStream);
            		        //System.out.println("using gzip encoding!");
            		    }
            		}
                
            }
            catch ( Exception ex){
                ex.printStackTrace();
                logger.log(Level.WARNING,"exception while performing keyword search ", ex);
            }
            
            return inStream;
        }
        
    }
    
    
    
    
    
}
