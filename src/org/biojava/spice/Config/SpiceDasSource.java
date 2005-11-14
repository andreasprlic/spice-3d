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
import org.biojava.services.das.registry.DasCoordinateSystem;
import org.biojava.utils.xml.*            ; 
import java.io.IOException                ;
import java.text.DateFormat               ;
import java.text.SimpleDateFormat         ;
import java.util.*;
import org.biojava.spice.DAS.DAS_StylesheetRetrieve;
import java.net.URL;

/** Manages all data about a DAS source that SPICE requires */
public class SpiceDasSource
    extends DasSource 

{


    boolean status ;
    boolean registered ; // a flag to trace if source comes from registry or from user vonfig
    Map[] typeStyles;
    Map[] threeDstyles;
    
    public SpiceDasSource() {
	super();
	status    = true ;  // default source is actived and used .
	registered = true ; // default true = source comes from registry
	setNickname("MyDASsource");
	typeStyles = null;
	threeDstyles = null;
    }

    public void loadStylesheet(){
        DAS_StylesheetRetrieve dsr = new DAS_StylesheetRetrieve();
        String cmd = getUrl()+"stylesheet";
        URL url = null;
        try {
            url = new URL(cmd);
        } catch (Exception e){
            e.printStackTrace();
            return ;
        }
        Map[] styles = dsr.retrieve(url);
        
        if ( styles != null){
            typeStyles = styles;
        } else {
            typeStyles = new Map[0];	
        }
        
        
        Map[] t3dStyles = dsr.get3DStyle();
        if ( t3dStyles != null){
            threeDstyles = t3dStyles;
        } else {
            threeDstyles = new Map[0];
        }
    }
    
    /** returns the Stylesheet that is provided by a DAS source.
     * It provides info of how to draw a particular feature.
     * returns null if not attempt has been made to load the stylesheet.
     * afterwards it returns a Map[0] or the Map[] containing the style data.
     * 
     * @return
     */
    public Map[] getStylesheet(){
        return typeStyles;
    }
    
    /** get the stylesheet containing the instructions how to paint in 3D.
     * 
     * @return
     */
    public Map[] get3DStylesheet(){
       return threeDstyles; 
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
	this.setTestCode(ds.getTestCode());
	this.setId(ds.getId());
	this.setLabels(ds.getLabels());
	this.setHelperurl(ds.getHelperurl());
    }

    
    public String toString() {
        String txt = getId()  + " " + getNickname() + " " + getUrl() ;
        return txt;
    }

    /** convert to XML. */
    public XMLWriter toXML(XMLWriter xw)
	throws IOException
    {
	//System.out.println("writing XML of" + getUrl());
	xw.openTag("SpiceDasSource");
	xw.attribute("url",getUrl());
	xw.attribute("nickname",getNickname());
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
	DasCoordinateSystem[] coords = getCoordinateSystem();
	for (int i = 0; i < coords.length; i++){
	    xw.openTag("coordinateSystem");
	    xw.attribute("name",coords[i].toString());
	    xw.attribute("uniqId",coords[i].getUniqueId());
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
