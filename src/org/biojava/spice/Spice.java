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

public class Spice extends Applet {

    SpiceButton spicebutton ;
    
    public static void main(String[] argv) {

	if ( argv.length < 1) {
	    System.err.println(" wrong arguments: please specify PDB code to display");
	    return;
	}

	addMoreSpice(argv[0]);
	
    }

    public void init() {
	spicebutton=new SpiceButton(this);
	add(spicebutton);
	/*qpdbButton.show();*/
	spicebutton.setVisible(true);
	setBackground(Color.white);

    }

    public static void addMoreSpice(String pdbcode){
	SpiceApplication appFrame = new SpiceApplication(pdbcode) ;	
	System.out.println("back from init of SpiceApplication");
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
		    spice.addMoreSpice(defaultpdb);
		    return null ;
		}
	    });
	
	return true ;
    }

    
}


