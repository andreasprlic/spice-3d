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
 * Created on 20.09.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.spice;

import javax.jnlp.*                        ; 

import java.io.*                           ;
import java.net.URL                        ;
import org.xml.sax.InputSource             ;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader               ;
import org.xml.sax.helpers.*               ;
import org.xml.sax.*                       ;
import javax.xml.parsers.*                 ;



import org.biojava.utils.xml.*             ; 

/** a class to store the config */
public class PersistentConfig
{

    PersistenceService ps; 
    BasicService bs; 
    
    public PersistentConfig(){
	System.out.println("init PersistentConfig");
	try { 
	    ps = (PersistenceService)ServiceManager.lookup("javax.jnlp.PersistenceService"); 
	    bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService"); 
	} catch (UnavailableServiceException e) { 
	    e.printStackTrace();
	    ps = null; 
	    bs = null; 
	} 
    }

    /** writes the configuration */
    public void save(RegistryConfiguration config ) {
	if (ps != null && bs != null) { 
	    // Persistent Service is available, running as javaws
	    saveWebStart(config) ;
	} else {
	    System.out.println("can not save using persistentservice!");
	}
    }
    
    private void saveWebStart(RegistryConfiguration config ){
	System.out.println("saving webstart");
	try {
	    
            // find all the muffins for our URL
            URL codebase = bs.getCodeBase(); 
	    System.out.println("codebase" + codebase);

	    FileContents fc = null ;


	    try {		
		// test if persistent storage already created

		fc = ps.get(codebase);
		System.out.println("deleting old muffin");
		ps.delete(codebase);
		
	    } catch (IOException e){
	    }
	    System.out.println("creating new muffin");
	    // seems not, create it first
	    ps.create(codebase,3000000);
	    fc = ps.get(codebase);
	    

	    OutputStream os = fc.getOutputStream(true); 
	    
	    //StringWriter sw = new StringWriter();
	    //StringWriter stw = new StringWriter(os)   ;
	    PrintWriter pw = new PrintWriter(os,true);
	    XMLWriter xw = config.toXML(pw);
	   
	    //sw.flush();
	    //System.out.println(sw.toString());
	    //sw.close();
	    //xw.flush();
	    pw.flush();
	    os.flush();

	    xw.close();
	    pw.close();
            os.close(); 

	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    /** loads Config from PersistenceService
     *  returns null if no PErsistenceService has been created ...
     */
    public RegistryConfiguration load() {
	if (ps != null && bs != null) { 
	    // Persistent Service is available, running as javaws
	    return loadWebStart() ;
	} else {
	    System.out.println("can not load from persistentservice!");
	}
	return null ;
    }


    /** loads Config from PersistenceService
     *  returns null if no PErsistenceService has been created ...
     */
    private RegistryConfiguration loadWebStart() {
	RegistryConfiguration config = null;
	try { 
	    URL codebase = bs.getCodeBase(); 
	    System.out.println("codebase" + codebase);
	    
	    FileContents fc = null ;
	    
	    try {
		System.out.println("trying to get old muffin");
		fc = ps.get(codebase);
	    } catch (IOException e){
		// has not been created, so nothing can be loaded ...
		return null ;
	    }	



	    // parse the XML file ...
	    InputStream stream = fc.getInputStream();
	    config = parseConfigFile(stream);
	    

	    
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return config ;
	
    }


    private RegistryConfiguration parseConfigFile(InputStream inStream) {

	try {
	    SAXParserFactory spfactory =
		SAXParserFactory.newInstance();
	    
	    SAXParser saxParser = null ;
	    
	    try{
		saxParser =
		    spfactory.newSAXParser();
	    } catch (ParserConfigurationException e) {
		e.printStackTrace();
	    }
	    
	    XMLReader xmlreader = saxParser.getXMLReader();
	    
	    ConfigXMLHandler cont_handle = new ConfigXMLHandler();
	    xmlreader.setContentHandler(cont_handle);
	    xmlreader.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
	    
	    InputSource insource = new InputSource() ;
	    insource.setByteStream(inStream);
	    
	    // the actual parsing starts now ...
	    xmlreader.parse(insource);
	    
	    
	    RegistryConfiguration config = cont_handle.getConfig();
	    return config ;

	} catch (Exception e){
	    e.printStackTrace();
	    return null;
	}

    }
}
