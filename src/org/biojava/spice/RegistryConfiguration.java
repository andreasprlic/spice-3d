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


package org.biojava.spice ;

// to get config file via http
import java.net.HttpURLConnection ;
import java.net.URL;
import java.io.IOException ;

import java.util.HashMap   ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.List ;
// for DAS registration server:
import org.biojava.services.das.registry.*;


// for GUI;
import java.awt.Frame ;
import java.awt.event.*    ;


import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;


/** Container class for configuration
 */
public class RegistryConfiguration 
{

    List serverdata ;

    public RegistryConfiguration () {
	super();
	serverdata = new ArrayList();
    }

   

    private boolean isSeqStrucAlignmentServer(DasSource source) {
	boolean msdmapping = false ;
	String[] coordsys = source.getCoordinateSystem() ;
	
	boolean uniprotflag = false ;
	boolean pdbflag     = false ;
	
	pdbflag     =  hasCoordSys("PDBresnum",source) ;
	uniprotflag =  hasCoordSys("UniProt",source)   ;
	
	if (( uniprotflag == true) && ( pdbflag == true)) {
	    msdmapping = true ;
	}
	return msdmapping ;
    }
    
    private boolean hasCoordSys(String coordSys,DasSource source ) {
	String[] coordsys = source.getCoordinateSystem() ;
	for ( int i = 0 ; i< coordsys.length; i++ ) {
	    String c = coordsys[i];
	    if ( c.equals(coordSys) ) {
		return true ;
	    }
	}
	return false ;

    }

    public void addServer(DasSource s, boolean status) {
	HashMap ds = new HashMap();
	ds.put("server",s);
	if ( status)
	    ds.put("status","1");
	else
	    ds.put("status","0");
	serverdata.add(ds);	
    }

    public void setStatus(int serverpos, boolean status) {
	if ( serverpos > serverdata.size() ) return  ;
	if ( serverpos < 0 ) return  ;
	Map m = (Map) serverdata.get(serverpos);
	if ( status)
	    m.put("status","1");
	else
	    m.put("status","0");

	
    }
    
    public boolean getStatus(int serverpos) {
	if ( serverpos > serverdata.size() ) return false ;
	if ( serverpos < 0 ) return false ;
	Map m = (Map) serverdata.get(serverpos);
	String status = (String) m.get("status");
	if( status.equals("1") )
	    return true ;
	else 
	    return false ;

    }
    public DasSource getServer(int serverpos) {
	if ( serverpos > serverdata.size() ) return null ;
	if ( serverpos < 0 ) return null ;
	Map m = (Map) serverdata.get(serverpos);
	DasSource ds = (DasSource)m.get("server");
	return ds ;
    }
    public List getServers(){
	ArrayList retservers = new ArrayList();
	for ( int i = 0 ; i < serverdata.size() ; i++ ) {
	    Map m = (Map) serverdata.get(i);
	    DasSource ds = (DasSource)m.get("server");
	    retservers.add(ds);
	}
	return retservers ;
    }

    public List getServers(String capability, String coordSys){
	List servers = getServers(capability);
	ArrayList retservers = new ArrayList();
	for ( int i = 0 ; i < servers.size() ; i++ ) {
	    DasSource ds = (DasSource)servers.get(i);
	    if ( hasCoordSys(coordSys,ds)) {
		retservers.add(ds);
	    }
	}
	return retservers ;

    }
 

    public List getServers(String capability) {
	ArrayList retservers = new ArrayList();
	for ( int i = 0 ; i < serverdata.size() ; i++ ) {
	    Map m = (Map) serverdata.get(i);
	    DasSource ds = (DasSource)m.get("server");
	    String status = (String) m.get("status");
	    // true == active
	    if ( status.equals("1") ) {
		String[] capabilities = ds.getCapabilities() ;
		for ( int c=0; c<capabilities.length ;c++) {
		    String capabil = capabilities[c];
		    if ( capability.equals(capabil)){
			// at the moment we only know about UniProt PDB servers,,,
			if ( capabil.equals("alignment") ){
			    if ( isSeqStrucAlignmentServer(ds) ){
				retservers.add(ds);
			    }
			} else {
			    retservers.add(ds);
			}
		    }
		}
	    }

	}
	return retservers ;
    }
}


