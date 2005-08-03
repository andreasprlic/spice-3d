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
 * Copyright for this cilode is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on 06.10.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.spice ;

import org.biojava.spice.utils.CliTools;
import java.applet.Applet;
import org.biojava.spice.Config.ConfigurationException;
import java.net.URL;
import java.net.MalformedURLException ;
import java.util.ArrayList;
import javax.swing.JFrame;
import org.biojava.spice.GUI.AboutDialog;
import java.util.List;

/** the startup class of SPICE. It takes care of correctly parsing the arguments that are given to SPICE.
 * currently supported arguments are:
 * <ul>
 * 	<li><b>-codetype</b> the <i>type</i> of the provided code. currently supported: <i>PDB</i> and <i>UniProt</i>.</li>
 * 	<li><b>-code</b>the <i>Accession code</i>. e.g. 5pti for PDB or P00280 for UniProt.</li>
 *  <li><b>-registry</b> the URL for the DAS - registration web service. Usually will be http://servlet.sanger.ac.uk/dasregistry/services/das_registry</li>
 *  <li><b>-backupRegistry</b> (optional) the URl for a backup registration service. To be used if the primary service provided by <i>-registry</i> fails.</li>
 *  <li><b>-display</b> (optional) a list of DAS - sources (by their unique Id from registry) to be highlited. A ";" separated list of DAS source ids e.g. DS_101;DS_102;DS_110</li>.
 *  <li><b>-displayLabel</b> (optional) Choose all das source belonging to a particular label to be highlited. A ";" separated list of labels e.f. biosapiens;efamily;</li>
 *  <li><b>-rasmolScript</b> (optional) Send a rasmol script to be executed after the (first) structure has been loaded. 
 *  <li><b>-seqSelectStart</b> (optional) Select a region in sequence coordinates (start position).
 *  <li><b>-seqSelectEnd</b> (optional) Select a region in sequence coordinates (end position).
 *  <li><b>-pdbSelectStart</b> (optional) Select a region in PDB resnum coordinates (start position).
 *  <li><b>-pdbSelectEnd</b> (optional) Select a region in PDB resnum coordinates (end position).
 *  <li><b>-displayMessage</b> (optional) display a (html formatted) message when the structure has been loaded.
 *  <li><b>-displayMessageWidth</b> (optional) set the width of the message window.
 *  <li><b>-displayMessageHeight</b> (optional) set the height of the message window. 
 * @author Andreas Prlic
 * 
 * */
public class Spice extends Applet {
    private URL primaryRegistry ;
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
    private String message;
    
    public static void main(String[] argv) {
        
        Spice app = new Spice();
        
        // init the configuration
        
        List mandatoryArgs= new ArrayList();
        mandatoryArgs.add("primaryRegistry");
        mandatoryArgs.add("code");
        mandatoryArgs.add("codetype");
        
        
        for (int i = 0 ; i < argv.length; i++){
            String arg = argv[i];
            String[] tmp = new String[1];
            tmp[0] = arg;
            
            try {
                CliTools.configureBean(app, tmp);        
            } catch (ConfigurationException e){
                e.printStackTrace();
                if ( mandatoryArgs.contains(arg) ) {
                    // there must not be a ConfigurationException with mandatory arguments.
                    return;
                } else {
                    // but there can be with optional ...
                }
            }           
        }
         
        app.run();
        
    } 
    
    /** Start SPICE @see SpiceApplication */
    public void run(){
        
        System.out.println("Welcome to the SPICE - DAS client!");
        System.out.println("SPICE version: " + AboutDialog.VERSION);
        System.out.println("displaying for you: " + codetype + " " + code);
       
        
        URL[] regis ;
        int numberregis = 1 ;
        if (registryurls != null) {
            numberregis += registryurls.length;
        }
        if ( primaryRegistry != null ){
            regis = new URL[numberregis];
            regis[0] = primaryRegistry;
            for ( int i =0;i<registryurls.length;i++) {
                regis[i+1] = registryurls[i];
            }
        } else {
            regis = registryurls;
        }
        
        // start spice
        SpiceApplication appFrame = new SpiceApplication(regis, display,displayLabel,rasmolScript,seqSelectStart, seqSelectEnd, pdbSelectStart,pdbSelectEnd, message, messageWidth, messageHeight) ;	
        appFrame.load(codetype,code);
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
    }
    
    /** set a list of DAS - sources (by their unique Id from registry) to be highlited
     * 
     * @param dasSourceIds a ";" separated list of DAS source ids e.g. DS_101;DS_102;DS_110
     */
    public void setDisplay(String dasSourceIds){
        System.out.println("restricting display to servers with Unique Ids " + dasSourceIds);
        display = dasSourceIds;
        if ( displayLabel.equals("all")){
            displayLabel = "";
        }
    }
    
    /** choose all das source belonging to a particular label to be highlited.
     * 
     * @param label a ";" separated list of labels e.f. biosapiens;efamily
     */
    public void setDisplayLabel(String label){
        System.out.println("restricting display to servers with label " + label);
        this.displayLabel = label;
        if ( display.equals("all"))
            display = "";
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
            primaryRegistry = new URL(url);
            
        } catch (MalformedURLException e){
            e.printStackTrace();
        } 
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
                regis.add(u);
                
            } catch (MalformedURLException e){
                e.printStackTrace();
            }
        }
        registryurls = (URL[]) regis.toArray(new URL[regis.size()]);
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
        message = txt;
    }
    
    public void setMessageWidth(int width){
        messageWidth = width;
    }
    
    public void setMessageHeight(int height){
        messageHeight = height;
    }
    	
    
}





