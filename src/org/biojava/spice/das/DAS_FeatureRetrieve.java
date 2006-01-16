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
package org.biojava.spice.das;


import java.net.URL                         ;
import java.io.InputStream                  ;
import org.xml.sax.InputSource              ;
import org.xml.sax.XMLReader                ;
import javax.xml.parsers.*                  ;
import org.xml.sax.SAXException             ;
import org.xml.sax.*                        ;
import java.util.ArrayList                  ;
import java.util.List;
import java.util.logging.*                  ;
import java.net.HttpURLConnection           ;


/**
 * A class to perform a DAS features request
 * 
 * @author Andreas Prlic
 *
 */
public class DAS_FeatureRetrieve {
    
    List features ;
    Logger logger     ;
    int comeBackLater;
    URL url;
    /**
     *  
     */
    public DAS_FeatureRetrieve(URL url) {
        super();
        
        logger = Logger.getLogger("org.biojava.spice");
        features = new ArrayList() ;
        comeBackLater = -1;
        this.url=url;
        reload();
    }
    
    
    /** contact the DAS-feature server again. Usually
     * it is not necessary to call this again, because the constructor already does, but
     * if comeBackLater > -1 this should be called again.
     *
     */
    public void reload(){
        
        try {
            
            InputStream dasInStream = null;
            try {
                dasInStream	= open(url); 
            } catch (Exception e ){
                comeBackLater = -1;
                logger.log(Level.FINE,"could not open connection to " + url,e);
                return ;
            }
            
            
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
                logger.log(Level.FINE,"Cannot set validation " + validation); 
            }
            
            try {
                xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",validation);
            } catch (SAXNotRecognizedException e){
                e.printStackTrace();
                logger.log(Level.FINE,"Cannot set load-external-dtd "+validation); 
                
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
                comeBackLater = cont_handle.getComBackLater();
            } 
            catch ( Exception e){
                e.printStackTrace();
                logger.log(Level.FINE,"error while parsing response from "+ url);
                comeBackLater = -1;
                features = new ArrayList();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            comeBackLater = -1;
        }
    }
    
    private InputStream open(URL url)
    throws java.io.IOException, java.net.ConnectException
    {
        InputStream inStream = null;
        
        
        HttpURLConnection huc = org.biojava.spice.SpiceApplication.openHttpURLConnection(url);
        
        inStream = huc.getInputStream();		
        
        return inStream;
        
    }
    
    /** returns a List of Features */
    public List get_features() {
      
        return features;
    }
    
    /** returns the comeBackLater value - if a server returned suchh - 
     * 
     * @return comeBackLater in seconds, or -1 if not provided by server 
     */
    public int getComeBackLater(){
        
        return comeBackLater;
        
    }
    
    
}
