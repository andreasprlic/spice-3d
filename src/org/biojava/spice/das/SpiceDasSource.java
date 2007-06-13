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

package org.biojava.spice.das ;

//import org.biojava.services.das.registry.*;
import org.biojava.dasobert.das.DAS_StylesheetRetrieve;
import org.biojava.dasobert.dasregistry.Das1Source;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;
import org.biojava.dasobert.dasregistry.DasSource;
import org.biojava.utils.xml.*            ; 
import java.io.IOException                ;
import java.text.DateFormat               ;
import java.text.SimpleDateFormat         ;
import java.util.*;


import java.net.URL;

/** Manages all data about a DAS source that SPICE requires */
public class SpiceDasSource
extends Das1Source 

{


	boolean status ;
	boolean registered ; // a flag to trace if source comes from registry or from user vonfig
	Map[] typeStyles;
	Map[] threeDstyles;
	public static String DEFAULT_NICKNAME = "MyDASsource";
	public static String DEFAULT_CAPABILITY = "features";

	public String displayType = "default";
	public boolean unlimitedFeatures;

	public SpiceDasSource() {
		super();

		status    = true ;  // default source is actived and used .
		registered = true ; // default true = source comes from registry
		setNickname(DEFAULT_NICKNAME);
		typeStyles = null;
		threeDstyles = null;
		String[] caps = new String[1];
		caps[0] = DEFAULT_CAPABILITY;
		setCapabilities(caps);
		unlimitedFeatures = false;
	}



	public boolean hasUnlimitedFeatures() {
		return unlimitedFeatures;
	}



	public void setUnlimitedFeatures(boolean unlimitedFeatures) {
		this.unlimitedFeatures = unlimitedFeatures;
	}



	public String getDisplayType() {
		return displayType;
	}



	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}

	private boolean isHistogramDasSource(){
		if ( typeStyles != null){
			for ( int i =0; i< typeStyles.length ; i++){
				Map m = typeStyles[i];

				String type = (String) m.get("style");
				if ( type != null) {
					//System.out.println("stylesheet type " + type);
					if ( type.equals("gradient") || 
							( type.equals("lineplot")) || 
							( type.equals("histogram")) 
					){

						return true;
					}
				}
			}
		}
		return false;
	}

	public void loadStylesheet(){
		DAS_StylesheetRetrieve dsr = new DAS_StylesheetRetrieve();
		String cmd = getUrl()+"stylesheet";
		URL url = null;
		try {
			url = new URL(cmd);
		} catch (Exception e){
			e.printStackTrace();
			typeStyles = new Map[0];
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

		if ( isHistogramDasSource()){
			setUnlimitedFeatures(true);
		}
	}

	/** returns the Stylesheet that is provided by a DAS source.
	 * It provides info of how to draw a particular feature.
	 * returns null if not attempt has been made to load the stylesheet.
	 * afterwards it returns a Map[0] or the Map[] containing the style data.
	 * 
	 * @return Map containing the stylesheet
	 */
	public Map[] getStylesheet(){
		return typeStyles;
	}

	/** get the stylesheet containing the instructions how to paint in 3D.
	 * 
	 * @return a Map containing the stylesheet information
	 */
	public Map[] get3DStylesheet(){
		return threeDstyles; 
	}

	/** a flag if this das source is active
	 * or 
	 * @param flag
	 */
	public void    setStatus(boolean flag) { status = flag ; }
	public boolean getStatus()             { return status ; }

	public void    setRegistered(boolean flag) { registered = flag ; }
	public boolean getRegistered()             { return registered ; }


	/** convert DasSource to SpiceDasSource 
	 * 
	 * @param ds a DasSource to be converted
	 * @return a new SpiceDasSource object
	 * */
	public static SpiceDasSource  fromDasSource(DasSource ds) {
		SpiceDasSource s = new SpiceDasSource();
		s.setUrl(ds.getUrl());
		s.setAdminemail(ds.getAdminemail());
		s.setDescription(ds.getDescription());
		s.setCoordinateSystem(ds.getCoordinateSystem());
		s.setCapabilities(ds.getCapabilities());
		s.setRegisterDate(ds.getRegisterDate());
		s.setLeaseDate(ds.getLeaseDate());
		s.setNickname(ds.getNickname());


		// testcode now part of coordinate system...
		//s.setTestCode(ds.getTestCode());
		s.setId(ds.getId());
		s.setLabels(ds.getLabels());
		s.setHelperurl(ds.getHelperurl());
		return s;
	}


	public String toString() {
		String txt = getId()  + " " + getNickname() + " " + getUrl() ;
		return txt;
	}

	/** convert to XML.
	 * 
	 * @param xw an XMLWriter to write to
	 * @return XMLWriter returns it again
	 * @throws IOException
	 *  */
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
