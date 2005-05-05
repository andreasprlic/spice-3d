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

// for applet
import java.awt.*;
import java.applet.Applet;
import java.security.*;
import javax.swing.JFrame ;

// for config file 
import java.net.URL;
import java.net.MalformedURLException ;
import java.util.ArrayList;


/** the startup class of SPICE */
public class Spice extends Applet {

    SpiceButton spicebutton ;
    
    public static void main(String[] argv) {

	if ( argv.length < 3 ) {
	    System.err.println(" wrong arguments: correct call: java -jar spice.jar type code registryurl "+ System.getProperty("line.separator")+" example: java -jar spice.jar PDB 5pti http://servlet.sanger.ac.uk/dasregistry/services/das_registry/");
	    return;
	}
	

	URL registry_url=null ; 
	ArrayList urls = new ArrayList();
	for ( int i = 2 ; i < argv.length; i++ ){
	    try {
	        //System.out.println(argv[1]);
	        //url = new URL(argv[2]);
	        //registry_url = new URL("http://localhost:8080/axis/services/dasregistry/");
	        registry_url = new URL(argv[i]);
	        urls.add(registry_url);
	    } catch (MalformedURLException e) {
	        //System.err.println(url+" " + registry_url);
	        e.printStackTrace();
	    
	    }
	}
	if ( urls.size() < 1 ){
	    System.err.println("no registration URL found...");
	    
	    return;
	    	}
	    
	    	URL[] reg_urls = (URL[])urls.toArray(new URL[urls.size()]);
	    	addMoreSpice(argv[0],argv[1],reg_urls);
	
    }

    public void init() {
	spicebutton=new SpiceButton(this);
	add(spicebutton);
	/*qpdbButton.show();*/
	spicebutton.setVisible(true);
	setBackground(Color.white);

    }

    // replace configurl as soon as registry server communication is working properly
    public static void addMoreSpice(String codetype, String code, URL[] registryurls){
	System.out.println("Welcome to the SPICE - DAS client!");
	System.out.println("displaying for you: " + codetype + " " + code);
	SpiceApplication appFrame = new SpiceApplication(registryurls) ;	
	//System.out.println("init of SpiceApplication single structure mode");
	//appFrame.setTitle("SPICE") ;
	//appFrame.setSize(800, 600);
	
	//appFrame.show();
	appFrame.load(codetype,code);
	
	appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	
    }


    public static void displayStructureAlignment(String pdb1, String pdb2, URL configfileurl,URL registryurl) {

	//SpiceApplication appFrame = new SpiceApplication(pdb1, pdb2, configfileurl,registryurl) ;	
	//System.out.println("init of SpiceApplication structure alignment mode");
	//appFrame.setTitle("SPICE") ;
	//appFrame.setSize(700, 700);
	//appFrame.show();
	System.out.println("currently disabled");
	/*
	appFrame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent evt) {
		    Frame frame = (Frame) evt.getSource();
		    frame.setVisible(false);
		    frame.dispose();
		}
	    });
	*/
    }

}

class SpiceButton extends Button {
    
    Spice spice ;
    

    SpiceButton(Spice spice_) {
	super("start SPICE");
	spice = spice_;
    }
    public boolean action(Event e, Object w) {
	//String defaultpdb = "1a4a" ;
	
	AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    String defaultpdb = spice.getParameter("PDB_CODE");
		    String configurl  = spice.getParameter("CONFIG_XML");	
		    String registryserver  = spice.getParameter("DAS_REGISTRY");	
		    URL url   =null   ;
		    URL registryurl=null ;
		    try {
			url = new URL(configurl);
			registryurl = new URL(registryserver);
		    } catch (MalformedURLException e) {
			System.err.println(configurl+" " + registryserver);
			e.printStackTrace();
			return null ;
		    }
		 
		    // check if structure alignment has been requested
		    boolean structurealignment = false ;
		    try {
			String alignpdb = spice.getParameter("ALIGNWITH");
			//System.out.println("ALGINWITH: "+alignpdb);
			if (alignpdb != null){
			    structurealignment = true ;
			    //spice.displayStructureAlignment(defaultpdb,alignpdb,url,registryurl);
			    // CURRENTLY DISABLED!
			    System.out.println("currently disabled");
			}
			
		     } catch ( Exception e){
			e.printStackTrace();
			// do nothing if parameter not present
			
		    }
		    if ( ! structurealignment ) {
			//System.out.println("init single struc mode");
			//spice.addMoreSpice(defaultpdb, url,registryurl);
		    }

		 
		    return null ;
		}
	    });
	
	return true ;
    }

    
}


