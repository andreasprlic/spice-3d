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
import java.awt.event.WindowAdapter ;
import java.awt.event.WindowEvent ;
import javax.swing.JFrame ;

// for config file 
import java.net.URL;
import java.net.MalformedURLException ;

public class Spice extends Applet {

    SpiceButton spicebutton ;
    
    public static void main(String[] argv) {

	if ( argv.length != 2 ) {
	    System.err.println(" wrong arguments: correct call: java -jar spice.jar PDBcode configfileUrl"+ System.getProperty("line.separator")+" example: java -jar spice.jar 5pti http://www.sanger.ac.uk/Users/ap3/DAS/SPICE/stable/config.xml");
	    return;
	}
	
	URL url ;
	try {
	    //System.out.println(argv[1]);
	    url = new URL(argv[1]);
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	    return ;
	}

	addMoreSpice(argv[0],url);
	
    }

    public void init() {
	spicebutton=new SpiceButton(this);
	add(spicebutton);
	/*qpdbButton.show();*/
	spicebutton.setVisible(true);
	setBackground(Color.white);

    }

    public static void addMoreSpice(String pdbcode,URL configfileurl){
	SpiceApplication appFrame = new SpiceApplication(pdbcode, configfileurl) ;	
	//System.out.println("init of SpiceApplication single structure mode");
	appFrame.setTitle("SPICE") ;
	appFrame.setSize(700, 700);
	appFrame.show();

	appFrame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent evt) {
		    Frame frame = (Frame) evt.getSource();
		    frame.setVisible(false);
		    frame.dispose();
		}
	    });
	
    }


    public static void displayStructureAlignment(String pdb1, String pdb2, URL configfileurl) {

	SpiceApplication appFrame = new SpiceApplication(pdb1, pdb2, configfileurl) ;	
	//System.out.println("init of SpiceApplication structure alignment mode");
	appFrame.setTitle("SPICE") ;
	appFrame.setSize(700, 700);
	appFrame.show();

	appFrame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent evt) {
		    Frame frame = (Frame) evt.getSource();
		    frame.setVisible(false);
		    frame.dispose();
		}
	    });
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

		    URL url ;
		    try {
			url = new URL(configurl);
		    } catch (MalformedURLException e) {
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
			    spice.displayStructureAlignment(defaultpdb,alignpdb,url);
			}
			
		     } catch ( Exception e){
			e.printStackTrace();
			// do nothing if parameter not present
			
		    }
		    if ( ! structurealignment ) {
			//System.out.println("init single struc mode");
			spice.addMoreSpice(defaultpdb, url);
		    }

		 
		    return null ;
		}
	    });
	
	return true ;
    }

    
}


