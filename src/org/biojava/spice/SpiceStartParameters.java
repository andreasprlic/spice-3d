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
 * Created on Nov 13, 2005
 *
 */
package org.biojava.spice;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.biojava.spice.manypanel.BrowserPane;

/** a class to manage the SPICE statup parameters.
 * display</li>,<li>displayLabel</li>,
     *              <li>rasmolScript</li>,<li>seqSelectStart</li>, 
     *              <li>seqSelectEnd</li>, <li>pdbSelectStart</li>,<li>pdbSelectEnd</li>,
     *              <li>message</li>, <li>messageWidth</li>, <li>messageHeight</li>) ;    
            
 * @author Andreas Prlic
 *
 */
public class SpiceStartParameters {
  
    private URL registry ;
    private URL[] registryurls;
    private String code;
    private String codetype;
    private String displayLabel = "all";
    private String display = "all";
    private String rasmolScript   = null ;
    private int seqSelectStart = -1;
    private int seqSelectEnd   = -1;
    private String pdbSelectStart = null;
    private String pdbSelectEnd   = null;
    private int messageWidth = 300;
    private int messageHeight = 100;
    private String displayMessage;
    private String localServerURL = "";
    private String localServerCoordSys = "";
    private String localServerName="";
    
    private String pdbcoordsys;
    private String uniprotcoordsys;
    private String enspcoordsys;
    
    public String getLocalServerName() {
        return localServerName;
    }


    public void setLocalServerName(String localServerName) {
        this.localServerName = localServerName;
    }


    public SpiceStartParameters() {
        super();
        pdbcoordsys     = BrowserPane.DEFAULT_PDBCOORDSYS;
        uniprotcoordsys = BrowserPane.DEFAULT_UNIPROTCOORDSYS;
        enspcoordsys    = BrowserPane.DEFAULT_ENSPCOORDSYS;
    }

    
    public String getEnspcoordsys() {
        return enspcoordsys;
    }


    public void setEnspcoordsys(String enspcoordsys) {
        this.enspcoordsys = enspcoordsys;
    }


    public String getPdbcoordsys() {
        return pdbcoordsys;
    }


    public void setPdbcoordsys(String pdbcoordsys) {
        this.pdbcoordsys = pdbcoordsys;
    }


    public String getUniprotcoordsys() {
        return uniprotcoordsys;
    }


    public void setUniprotcoordsys(String uniprotcoordsys) {
        this.uniprotcoordsys = uniprotcoordsys;
    }


    public String getDisplayMessage() {
        return displayMessage;
    }


    //public URL getPrimaryRegistry() {
    //    return registry;
    //}


    //public void setPrimaryRegistry(URL primaryRegistry) {
    //    this.registry = primaryRegistry;
   // }


    public URL[] getRegistryurls() {
        return registryurls;
    }


    public void setRegistryurls(URL[] registryurls) {
        this.registryurls = registryurls;
    }


    public int getSeqSelectEnd() {
        return seqSelectEnd;
    }


    public void setSeqSelectEnd(int seqSelectEnd) {
        this.seqSelectEnd = seqSelectEnd;
    }


    public int getSeqSelectStart() {
        return seqSelectStart;
    }


    public void setSeqSelectStart(int seqSelectStart) {
        this.seqSelectStart = seqSelectStart;
    }


    public String getDisplay() {
        
        return display;
    }


    public String getDisplayLabel() {
        return displayLabel;
    }


    public Integer getMessageHeight() {
        return new Integer(messageHeight);
    }


    public Integer getMessageWidth() {
        return new Integer(messageWidth);
    }


    public String getPdbSelectEnd() {
        return pdbSelectEnd;
    }


    public String getPdbSelectStart() {
        return pdbSelectStart;
    }


    public String getRasmolScript() {
        return rasmolScript;
    }


    /** set a list of DAS - sources (by their unique Id from registry) to be highlited
     * 
     * @param dasSourceIds a ";" separated list of DAS source ids e.g. DS_101;DS_102;DS_110
     */
    public void setDisplay(String dasSourceIds){
        //System.out.println("restricting display to servers with Unique Ids " + dasSourceIds);
        display = dasSourceIds;
        
    }
    
    /** choose all das source belonging to a particular label to be highlited.
     * 
     * @param label a ";" separated list of labels e.f. biosapiens;efamily
     */
    public void setDisplayLabel(String label){
        System.out.println("restricting display to servers with label " + label);
        this.displayLabel = label;
        
    }
    
    /** set the accession code to be displayed in SPICE. eg. PDB - 5pti UniProt P00280
     * 
     * @param accessioncode the accession code to be displayed in SPICE. eg. PDB - 5pti UniProt P00280
     */
    public void setCode(String accessioncode){
        code = accessioncode;
    }
    /** get the accession code
     * 
     * @returns the accesion code
     */
    public String getCode(){ return code;}
    
    /** sets the type of the accession code being displayed.
     * Currently supported PDB, UniProt. 
     * 
     * @param codetype currently supported: PDB, UniProt
     */
    public void setCodetype(String codetype){
        this.codetype = codetype;
    }
    /** returns the type of the accession code that is displayed. Currently supported PDB, UniProt. 
     * 
     * @return the codetype
     */
    public String getCodetype() {
        return codetype;
    }
    
    /** set the primary registry.
     * @param url  Usually this should be:
     * http://servlet.sanger.ac.uk/dasregistry/services/das_registry
     */
    public void setRegistry(String url){
        try {
            registry = new URL(url);
            
        } catch (MalformedURLException e){
            e.printStackTrace();
        } 
    }
    
    public String getRegistry(){
        if ( registry != null)  {
            return registry.toString();
        }
        return null;
    }
        
    public String[] getBackupRegistry(){
        //return registryurls;
    
        List lst = new ArrayList();
        for (int i=0; i< registryurls.length;i++){
            lst.add(registryurls[i].toString());
            
        }
        
        return (String[]) lst.toArray(new String[lst.size()]); 
    }
    
    /** set backup registry servers. These will be contacted 
     * only if there is a problem occuring with the primary
     * 
     */
    public void setBackupRegistry(String[] urls){
        
        
        ArrayList regis = new ArrayList();
        for ( int i = 0 ;i< urls.length;i++){
            try {
                URL u = new URL(urls[i]);
                //System.out.println("adding " + u );
                regis.add(u);
                
            } catch (MalformedURLException e){
                e.printStackTrace();
            }
        }
        
        URL[] oldregistryurls = registryurls ;
        int oldsize = 0;
        if ( registryurls != null) {
            oldsize = registryurls.length;
        }
        URL[] tmpregistryurls = (URL[]) regis.toArray(new URL[regis.size()]);
        int newsize = oldsize + regis.size();
        registryurls = new URL[newsize];
        
        
        for ( int i =0; i<oldsize; i++) {
            //System.out.println("debug 1a" + oldregistryurls[i]);
            registryurls[i] = oldregistryurls[i];
        }
        
        for ( int i =0; i<tmpregistryurls.length; i++) {
            //System.out.println("debug 1b" + tmpregistryurls[i]);
            registryurls[i+oldsize] = tmpregistryurls[i];
        }
        
    
    }
    
    public void setRasmolScript(String script){
        rasmolScript = script;
    }
    
    public void setSeqSelectStart(String start){
        seqSelectStart = Integer.parseInt(start);
    }
    
    public void setSeqSelectEnd(String end){
        seqSelectEnd = Integer.parseInt(end);
    }
    
    public void setPdbSelectStart(String start){
        pdbSelectStart = start;
    }
    
    public void setPdbSelectEnd(String end){
        pdbSelectEnd = end;
    }
    
    public void setDisplayMessage(String txt){
        displayMessage = txt;
    }
    
    public void setMessageWidth(Integer width){
        messageWidth = width.intValue();
    }
    
    public void setMessageHeight(Integer height){
        messageHeight = height.intValue();
    }


    public String getLocalServerCoordSys() {
        return localServerCoordSys;
    }


    public void setLocalServerCoordSys(String localServerCoordSys) {
        this.localServerCoordSys = localServerCoordSys;
    }


    public String getLocalServerURL() {
        return localServerURL;
    }


    public void setLocalServerURL(String localServerURL) {
        this.localServerURL = localServerURL;
    }
        

    
}
