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
import java.util.ListIterator ;
// for DAS registration server:
//import org.biojava.services.das.registry.*;


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

    //List serverdata ;
    
    List allservers          ;
    List activeservers       ;
    String[] capabilities    ;
    String[] pdbFileExtensions ;
    public RegistryConfiguration () {
	super();
	//serverdata = new ArrayList();
	allservers    = new ArrayList();
	activeservers = new ArrayList();
	capabilities  = null ;
	pdbFileExtensions = new String[] { ".pdb",".ent"};
	
    }

    public String[] getPDBFileExtensions() { return pdbFileExtensions ;}
    public void setPDBFileExtensions(String[] exts) { pdbFileExtensions = exts ; } 
    
    private boolean isSeqStrucAlignmentServer(SpiceDasSource source) {
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
    
    private boolean hasCoordSys(String coordSys,SpiceDasSource source ) {
	String[] coordsys = source.getCoordinateSystem() ;
	for ( int i = 0 ; i< coordsys.length; i++ ) {
	    String c = coordsys[i];
	    if ( c.equals(coordSys) ) {
		return true ;
	    }
	}
	return false ;

    }

    public void addServer(SpiceDasSource s, boolean status) {	
	allservers.add(s);
	if (s.getStatus()) {
	    activeservers.add(s);
	}
    }


    public String[] getCapabilities() { return capabilities ; }
    public void setCapabilities(String[] capabs){capabilities = capabs ; }

    public void setStatus(String url, boolean status) {
	for ( int i = 0 ; i < allservers.size() ; i++ ) {
	    SpiceDasSource ds = (SpiceDasSource) allservers.get(i);
	    //SpiceDasSource ds = (SpiceDasSource)m.get();
	    if ( ds.getUrl().equals(url)) {
		setStatus(i,status);
		return ;
	    }
	}
	
    }
    public void setStatus(int serverpos, boolean status) {
	if ( serverpos > allservers.size() ) return  ;
	if ( serverpos < 0 ) return  ;
	SpiceDasSource m = (SpiceDasSource) allservers.get(serverpos);
	m.setStatus(status);
	
	if ( ! status) {
	    // remove from active servers ...
	    removeFromActive(m);
	}
	
    }
    
    public boolean getStatus(int serverpos) {
	if ( serverpos > allservers.size() ) return false ;
	if ( serverpos < 0 ) return false ;
	SpiceDasSource m = (SpiceDasSource) allservers.get(serverpos);
	
	return m.getStatus();
	//String status = (String) m.get("status");
	//if( status.equals("1") )
	//  return true ;
	//else 
	//  return false ;

    }

    
    public SpiceDasSource getServer(int serverpos) {
	if ( serverpos > allservers.size() ) return null ;
	if ( serverpos < 0 ) return null ;
	SpiceDasSource ds = (SpiceDasSource) allservers.get(serverpos);
	return ds ;
    }

    public List getAllServers(){
	return allservers ;
    }

    // todo ...
    public List getServers(){
	return activeservers ;	
    }

    public List getServers(String capability, String coordSys){
	List servers = getServers(capability);
	ArrayList retservers = new ArrayList();
	for ( int i = 0 ; i < servers.size() ; i++ ) {
	    SpiceDasSource ds = (SpiceDasSource)servers.get(i);
	    if ( ds.getStatus()) {
		if ( hasCoordSys(coordSys,ds)) {
		    retservers.add(ds);
		}
	    }
	}
	return retservers ;

    }
 

    public List getServers(String capability) {
	ArrayList retservers = new ArrayList();
	for ( int i = 0 ; i < activeservers.size() ; i++ ) {
	    SpiceDasSource ds = (SpiceDasSource) activeservers.get(i);
	    
	    String[] capabilities = ds.getCapabilities() ;
	    for ( int c=0; c<capabilities.length ;c++) {
		String capabil = capabilities[c];
		if ( capability.equals(capabil)){
		    // at the moment we only know about UniProt PDB servers,,,
		    if ( capabil.equals("alignment") ){
			if ( isSeqStrucAlignmentServer(ds) ){
			    retservers.add(ds);
			} else {
			    System.out.println("DasSource " + ds.getUrl() + " is not a UniProt to PDB alignment service, unable to use");
			}
		    } else {
			retservers.add(ds);
		    }
		}
	    }
	}

	return retservers ;
    }


    /** remove a server from the list of active servers */
    private void removeFromActive(SpiceDasSource ds) {
	String serverurl = ds.getUrl();
	ListIterator iter = activeservers.listIterator();
	while (iter.hasNext()) {
	    SpiceDasSource sds = (SpiceDasSource) iter.next();
	    if (sds.getUrl().equals(serverurl));
	    iter.remove();
	    return ;	    
	}	
    }

}


