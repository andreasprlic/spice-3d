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
 * Created on 30.10.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.spice.Config;

import java.util.*                        ;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes ;
import java.net.URL;
import java.net.MalformedURLException;
/**
 * XML content handler for serialisation of RegistryConfiguration class
 */
public class ConfigXMLHandler extends DefaultHandler {

    RegistryConfiguration config ;
    SpiceDasSource source        ;
    List extensions              ;
    List coords                  ;
    List capabs                  ;
    List possibleCapabs         ;
    String status                ;
    DateFormat myDateFormat      ;
    String description           ;

    /**
     * 
     */
    public ConfigXMLHandler() {
	super();
	
	config         = new RegistryConfiguration();
	source         = null ;
	extensions     = new ArrayList();
	coords         = new ArrayList();
	capabs         = new ArrayList();
	possibleCapabs = new ArrayList();
	myDateFormat   = new SimpleDateFormat("dd.MM.yyyy");
	status         = "start";
	description    = ""     ;
    }

    public void startElement (String uri, String name, String qName, Attributes atts){
	//System.out.println("new element >" + name + "< >" + qName+"<" + uri);
	if ( qName.equals("SpiceDasSource")){
	    source = new SpiceDasSource();	    
	    coords = new ArrayList();
	    capabs = new ArrayList();
	    
	    source.setUrl(atts.getValue("url"));
	    source.setAdminemail(atts.getValue("adminemail"));
	    source.setNickname(atts.getValue("nickname"));
	    String status = atts.getValue("status") ;
	    boolean flag = false ;
	    if (status.equals("true"))
		flag = true ;
	    source.setStatus(flag);
	    
	    String regist = atts.getValue("registered");
	    boolean rflag = false ;
	    if ( regist.equals("true"))
		rflag = true ;
	    source.setRegistered(rflag);
	    
	    
	    
	    
	    Date regDate = getDatefromString(atts.getValue("registerDate"));
	    source.setRegisterDate(regDate);
	    Date leDate  = getDatefromString(atts.getValue("leaseDate"));
	    source.setLeaseDate(leDate);

	}
	else if ( qName.equals("lastContact")) {
	    Date lcontact = getDatefromString(atts.getValue("date"));
	    config.setContactDate(lcontact);
	}
	
	else if ( qName.equals("description")) {
	    status = "description";
	    description = "" ;
	}
		
	else if ( qName.equals("coordinateSystem")) 
	    coords.add(atts.getValue("name"));	
	else if ( qName.equals("capability"))
	    capabs.add(atts.getValue("service"));
	else if (qName.equals("pdbFileExtension"))
	    extensions.add(atts.getValue("name"));
	else if (qName.equals("update"))
	    config.setUpdateBehave(atts.getValue("behave"));
	else if (qName.equals("possibleCapability")) {
	    possibleCapabs.add(atts.getValue("name"));
	}
	else if (qName.equals("registryUrl")) {
	    String ustr = atts.getValue("url");
	    try {
	        URL regiurl = new URL(ustr);
	        config.setRegistryUrl(regiurl);
	    } catch(MalformedURLException e){
	        e.printStackTrace();
	    }
	}
    }
	
    private Date getDatefromString(String d){
	Date myDate = new Date() ;
	try { 		
	    myDate = myDateFormat.parse(d);
	} catch (Exception e) {
	    System.out.println("Invalid Date Parser Exception ");
	    e.printStackTrace();
	    
	}
	return myDate ;
    }
    
    public void endElement(String uri,String name, String qName){
	//System.out.println("end element >" + name + "< >" + qName+"<" + uri);
	if ( qName.equals("SpiceDasSource")){
	    config.addServer(source,source.getStatus());
	} else if ( qName.equals("coordinateSystems")){
	    String[] coordSys = (String[])coords.toArray(new String[coords.size()]);
	    source.setCoordinateSystem(coordSys);
	    //System.out.println("coords:" +coords);
	} else if ( qName.equals("capabilities")){
	    String[] capabilities = (String[])capabs.toArray(new String[capabs.size()]);
	    source.setCapabilities(capabilities);
	} else if ( qName.equals("description")) {
	    status = "";
	} else if ( qName.equals("SpiceDasSource")) {
	    String[] exts = (String[])extensions.toArray(new String[extensions.size()]);
	    config.setPDBFileExtensions(exts);
	} else if ( qName.equals("SpiceConfig")){
	    String[] c =  (String[])possibleCapabs.toArray(new String[possibleCapabs.size()]);
	    config.setCapabilities(c);
	}
    }
    

    public void characters (char ch[], int start, int length){
	    //System.out.print("Characters:    \"");
	if ( status.equals("description")) {
	    for (int i = start; i < start + length; i++) {
	    description += ch[i] ;
	    }
	}
	
    }
    
    public RegistryConfiguration getConfig() {
	return config ;
    }
	
}
