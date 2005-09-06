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


package org.biojava.spice.Config ;


import java.io.IOException ;
import java.io.PrintWriter                ;

import java.text.SimpleDateFormat         ;
import java.text.DateFormat               ;

import java.util.Date                     ;
import java.util.ArrayList ;
import java.util.List ;

import org.biojava.utils.xml.*            ; 
import java.net.MalformedURLException;
import java.net.URL;
import org.biojava.services.das.registry.*;
import java.util.Iterator;
import java.util.logging.Logger;

/** Container class for configuration
 */
public class RegistryConfiguration 
{
    
    public static final String XML_CONTENT_TYPE = "text/xml";
    public static final String XML_DTD          = "spiceconfig.dtd";
    
    
    List allservers            ;
    List activeservers         ;
    String[] capabilities      ;
    String[] pdbFileExtensions ;
    String updateBehave        ;
    Date   lastContact         ;
    URL registryUrl         ;
    
    static String PDBCOORDSYS     = "PDBresnum,Protein Structure";
    static String UNIPROTCOORDSYS = "UniProt,Protein Sequence";
    
    static String DEFAULTREGISTRY = "http://servlet.sanger.ac.uk/registry/services/das_registry";
    
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    
    public RegistryConfiguration () {
        super();
        
        
        allservers        = new ArrayList();
        activeservers     = new ArrayList();
        capabilities      = new String[] { "sequence","structure","alignment","features","entry_points"} ;
        pdbFileExtensions = new String[] { ".pdb",".ent"};
        updateBehave      = "always";
        lastContact       = new Date(0) ;
        try {
            registryUrl       = new URL(DEFAULTREGISTRY);
        } catch( MalformedURLException e ){
            e.printStackTrace();
            registryUrl = null;
        }
    }
    
    
    
    public void setRegistryUrl(URL url) {
        registryUrl = url;
    }
    
    public URL getRegistryUrl() {
        return registryUrl ;
    }
    
    public void setContactDate(Date d) {
        lastContact = d;
    }
    
    public Date getContactDate() {
        return lastContact;
    }
    
    /** set to "always", "day" */
    public void setUpdateBehave(String b) {
        if (b.equals("always"))
            updateBehave = b;
        else if (b.equals("day"))
            updateBehave = b ;
    }
    
    public String getUpdateBehave() {
        return updateBehave;
    }
    
    public String[] getPDBFileExtensions() { return pdbFileExtensions ;}
    public void setPDBFileExtensions(String[] exts) { pdbFileExtensions = exts ; } 
    
    /** test if a server is a UniProt vs PDBresnum alignment server */
    public boolean isSeqStrucAlignmentServer(SpiceDasSource source) {
        boolean msdmapping = false ;
        //DasCoordinateSystem[] coordsys = source.getCoordinateSystem() ;
        
        boolean uniprotflag = false ;
        boolean pdbflag     = false ;
        
        pdbflag     =  hasCoordSys(PDBCOORDSYS,source) ;
        uniprotflag =  hasCoordSys(UNIPROTCOORDSYS,source)   ;
        //System.out.println(pdbflag + " " + uniprotflag);
        if (( uniprotflag == true) && ( pdbflag == true)) {
            msdmapping = true ;
        }
        return msdmapping ;
    }
    
    private boolean hasCoordSys(String coordSys,SpiceDasSource source ) {
        DasCoordinateSystem[] coordsys = source.getCoordinateSystem() ;
        for ( int i = 0 ; i< coordsys.length; i++ ) {
            String c = coordsys[i].toString();
            //System.out.println(">"+c+"< >"+coordSys+"<");
            if ( c.equals(coordSys) ) {
                //System.out.println("match");
                return true ;
            }
            
            
            
        }
        return false ;
        
    }
    
    
    /** returns a list of local SpiceDasSources */
    public List getLocalServers(){
        
        ArrayList tmp = new ArrayList();
        for ( int i = 0 ; i < allservers.size() ; i++ ) {
            SpiceDasSource ds = (SpiceDasSource) allservers.get(i);
            if ( ! ds.getRegistered() ) 
                tmp.add(ds);
        }
        return tmp ;
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
                // test if server in active list, if not, add
                if ( status) {
                    if ( ! activeservers.contains(ds)){
                        activeservers.add(ds);
                    }
                } else {
                    // remove from active servers ...
                    removeFromActive(ds);
                } 
                return ;
            }
        }
        
    }
    public void setStatus(int serverpos, boolean status) {
        if ( serverpos > allservers.size() ) return  ;
        if ( serverpos < 0 ) return  ;
        SpiceDasSource ds = (SpiceDasSource) allservers.get(serverpos);
        ds.setStatus(status);
        
        if ( ! status) {
            // remove from active servers ...
            removeFromActive(ds);
        } else {
            if ( ! activeservers.contains(ds)){
                activeservers.add(ds);
            }
        }
       logger.info("set status of " + serverpos + " " + ds.getNickname() + " to: " + getStatus(serverpos));
    }
    
    public boolean getStatus(int serverpos) {
        if ( serverpos > allservers.size() ) return false ;
        if ( serverpos < 0 ) return false ;
        SpiceDasSource ds = (SpiceDasSource) allservers.get(serverpos);
        
        return ds.getStatus();
        
    }
    
    
    /** move a server in the list of servers ... */
    public void moveServer(int startpos, int endpos) {
        //System.out.println("moveServer " + startpos + " " + endpos );
        
        if ( ( startpos < 0 ) ||
                ( endpos > allservers.size() ) 
        )
            return ;
        
        if ( endpos > startpos )
            endpos = endpos - 1 ;
        
        
        SpiceDasSource ds = getServer(startpos);
        allservers.remove(startpos);
        allservers.add(endpos,ds);
        
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
    
    public List getServers(){
        return activeservers ;	
    }
    
    /** returns all DAS sources with a particular capability and coordinate system. Returns all of them, no matter if they are enabled or disabled */
    public List getServers(String capability, String coordSys){
        List servers = getServers(capability);
        ArrayList retservers = new ArrayList();
        
        Iterator iter = servers.iterator();
        while ( iter.hasNext()){
            SpiceDasSource ds = (SpiceDasSource) iter.next();
            
            if ( hasCoordSys(coordSys,ds)) {
                retservers.add(ds);
            }
        }
        return retservers ;
        
    }
    
    /** returns all servers with a particular capaibility. Returns enabled as well as disabled servers */
    public List getServers(String capability) {
        ArrayList retservers = new ArrayList();
        
        Iterator iter = allservers.iterator();
        while ( iter.hasNext()){
            SpiceDasSource ds = (SpiceDasSource) iter.next();
            
            String[] capabilities = ds.getCapabilities() ;
            for ( int c=0; c<capabilities.length ;c++) {
                String capabil = capabilities[c];
                if ( capability.equals(capabil)){
                    // knowledge about about UniProt PDB servers needs to be handled by clients...
                    retservers.add(ds);
                }
            }
        }
        
        
        return retservers ;
    }
    
    
    
    /** delete server. only possible for local servers */
    public void deleteServer(int pos){
        SpiceDasSource ds = (SpiceDasSource)allservers.get(pos);
        if (!ds.getRegistered()) {
            allservers.remove(ds);
        }
    }
    /** remove a server from the list of active servers */
    private void removeFromActive(SpiceDasSource ds) {
        //String serverurl = ds.getUrl();
        activeservers.remove(ds);
    }
    
    
    /** convert Configuration to an XML file so it can be serialized
     */
    
    public XMLWriter toXML(PrintWriter pw) 
    throws IOException
    {
        //StringWriter stw = new StringWriter   ()   ;
        //PrintWriter   pw = new PrintWriter    (stw);
        XMLWriter     xw = new PrettyXMLWriter( pw);
        
        
        
        
        toXML(xw);
        return xw ;
    }
    
    
    /** convert Configuration to an XML file so it can be serialized
     add to an already existing xml file.
     */
    public XMLWriter toXML(XMLWriter xw) 
    throws IOException
    {
        xw.printRaw("<?xml version='1.0' standalone='no' ?>");
        //xw.printRaw("<!DOCTYPE " + XML_CONTENT_TYPE + " SYSTEM '" + XML_DTD + "' >");
        xw.openTag("SpiceConfig");
        for ( int i = 0 ; i < allservers.size() ; i++ ) {
            SpiceDasSource ds = (SpiceDasSource) allservers.get(i);
            ds.toXML(xw);
        }
        
        //pdbFileExtensions ;
        for (int i =0;i<pdbFileExtensions.length;i++){
            System.out.println("pdb ext" + pdbFileExtensions[i]);
            
            xw.openTag("pdbFileExtension");
            xw.attribute("name",pdbFileExtensions[i]);
            xw.closeTag("pdbFileExtension");
        }
        xw.openTag("update");
        xw.attribute("behave",updateBehave);
        xw.closeTag("update");
        
        // last contact with registry
        xw.openTag("lastContact");
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy"); 
        //	DateFormat df = DateFormat.getDateInstance();
        String lcontact = df.format(lastContact);
        xw.attribute("date",lcontact);
        xw.closeTag("lastContact");
        
        
        // all possible capabilities
        for (int i=0;i<capabilities.length;i++){
            xw.openTag("possibleCapability");
            xw.attribute("name",capabilities[i]);
            xw.closeTag("possibleCapability");
        }
        
        
        xw.openTag("registryUrl");
        xw.attribute("url",registryUrl.toString());
        xw.closeTag("registryUrl");
        
        xw.closeTag("SpiceConfig");
        return xw ;
        
    }
    
    
}


