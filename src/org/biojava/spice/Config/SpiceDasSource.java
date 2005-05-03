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
 * Created on 17.10.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.spice.Config ;

//import org.biojava.services.das.registry.*;
import org.biojava.services.das.registry.DasSource;
import org.biojava.utils.xml.*            ; 
import java.io.IOException                ;
import java.text.DateFormat               ;
import java.text.SimpleDateFormat         ;

/** Manages all data about a DAS source that SPICE requires */
public class SpiceDasSource
    extends DasSource 

{


    boolean status ;
    boolean registered ; // a flag to trace if source comes from registry or from user vonfig
    public SpiceDasSource() {
	super();
	status    = true ;  // default source is actived and used .
	registered = true ; // default true = source comes from registry
    }

    public void    setStatus(boolean flag) { status = flag ; }
    public boolean getStatus()             { return status ; }

    public void    setRegistered(boolean flag) { registered = flag ; }
    public boolean getRegistered()             { return registered ; }
    
    
    /** convert DasSource to SpiceDasSource */
    public void fromDasSource(DasSource ds) {
	this.setUrl(ds.getUrl());
	this.setAdminemail(ds.getAdminemail());
	this.setDescription(ds.getDescription());
	this.setCoordinateSystem(ds.getCoordinateSystem());
	this.setCapabilities(ds.getCapabilities());
	this.setRegisterDate(ds.getRegisterDate());
	this.setLeaseDate(ds.getLeaseDate());
	this.setNickname(ds.getNickname());
    }


    /** convert to XML. */
    public XMLWriter toXML(XMLWriter xw)
	throws IOException
    {
	System.out.println("writing XML of" + getUrl());
	xw.openTag("SpiceDasSource");
	xw.attribute("url",getUrl());
	xw.attribute("adminemail",getAdminemail());
	//xw.attribute("description",getDescription());
	xw.attribute("status",""+status);
	xw.attribute("registered",""+registered);
	DateFormat df = new SimpleDateFormat("dd.MM.yyyy"); 
	//DateFormat df = DateFormat.getDateInstance();
	String rds = df.format(getRegisterDate());
	String lds = df.format(getLeaseDate());
	xw.attribute("registerDate",rds);
	xw.attribute("leaseDate",lds);

	// description
	xw.openTag("description");
	xw.print(getDescription());
	xw.closeTag("description");

	// coordinateSystems
	xw.openTag("coordinateSystems");
	String[] coords = getCoordinateSystem();
	for (int i = 0; i < coords.length; i++){
	    xw.openTag("coordinateSystem");
	    xw.attribute("name",coords[i]);
	    xw.closeTag("coordinateSystem");
	} 
	xw.closeTag("coordinateSystems");

	// capabilities
	xw.openTag("capabilities");
	String[] caps = getCapabilities();
	for (int i = 0; i < caps.length; i++){
	    xw.openTag("capability");
	    xw.attribute("service",caps[i]);
	    xw.closeTag("capability");
	} 
	xw.closeTag("capabilities");

	xw.closeTag("SpiceDasSource");
	return xw ;
    }
    

}
