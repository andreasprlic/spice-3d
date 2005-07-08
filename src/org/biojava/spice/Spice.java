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

/** the startup class of SPICE. It takes care of correctly parsing the arguments that are given to SPICE.
 * currently supported arguments are:
 * <ul>
 * 	<li><b>-codetype</b> the <i>type</i> of the provided code. currently supported: <i>PDB</i> and <i>UniProt</i>.</li>
 * 	<li><b>-code</b>the <i>Accession code</i>. e.g. 5pti for PDB or P00280 for UniProt.</li>
 *  <li><b>-registry</b> the URL for the DAS - registration web service. Usually will be http://servlet.sanger.ac.uk/dasregistry/services/das_registry</li>
 *  <li><b>-backupRegistry</b> the URl for a backup registration service. To be used if the primary service provided by <i>-registry</i> fails.</li>
 *  <li><b>-display</b>  a list of DAS - sources (by their unique Id from registry) to be highlited. A ";" separated list of DAS source ids e.g. DS_101;DS_102;DS_110</li>.
 *  <li><b>-displayLabel</b> Choose all das source belonging to a particular label to be highlited. A ";" separated list of labels e.f. biosapiens;efamily;</li>
 * 
 * 
 * 
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
    
    public static void main(String[] argv) {
        
        Spice app = new Spice();
        try {
            // init the configuration
            argv = CliTools.configureBean(app, argv);
            app.run();
        } catch(ConfigurationException e){
            e.printStackTrace();
        }
        
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
        SpiceApplication appFrame = new SpiceApplication(regis, display,displayLabel) ;	
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
    
}





