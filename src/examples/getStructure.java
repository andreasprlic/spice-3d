/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on 24.11.2005
 * @author Andreas Prlic
 *
 */

import org.biojava.bio.structure.Structure;
import org.biojava.spice.das.StructureThread;
import org.biojava.spice.das.SpiceDasSource;
import org.biojava.spice.manypanel.eventmodel.StructureListener;
import org.biojava.spice.manypanel.eventmodel.StructureEvent;

/** a class that demonstrates how to get a protein structure
 * from a structure DAS server.
 */


public class getStructure  {

    public static void main (String[] args) {
	

	getStructure s = new getStructure();
	s.showExample();
	
    }

    public void showExample(){
	try {
	    // Since there is a time-delay between sending a DAS
	    // request and getting the response, SPICE launches its
	    // own thread to perform the request. The main application
	    // can then continue with whatever it wished to do. A
	    // "Listener" class is waiting for the response.
	    
	    
	    // first let's create a SpiceDasSource which knows where the
	    // DAS server is located.
	    
	    SpiceDasSource dasSource = new SpiceDasSource();
	    
	    dasSource.setUrl("http://das.sanger.ac.uk/das/structure/");
	    
	    
	    String pdbCode = "1boi";
	    
	    // now we create the thread that will fetch the structure
	    StructureThread thread = new StructureThread(pdbCode,dasSource);
	    
	    // add a structureListener that simply prints the PDB code
	    StructureListener listener = new MyStructureListener();
	    thread.addStructureListener(listener);

	    // and now start the DAS request
	    thread.start();


	    // do an (almost) endless loop which is terminated in the StructureListener...
	    int i = 0 ;
	    while (true){
		System.out.println(i  + "/10th seconds have passed");
		i++;	
		Thread.sleep(100);
		if ( i > 1000) {
		    System.err.println("something went wrong. Perhaps a proxy problem?");
		    System.exit(1);
		}
	
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }



    class MyStructureListener 
	implements StructureListener {

	/** this method is called when the Thread finishes */
	public synchronized void newStructure(StructureEvent event){
	    Structure s = event.getStructure();
	    System.out.println(s.toPDB()); 
	    //System.out.println(s.toPDB()); 
	    System.exit(0);
	}
	


	// the methods below are required by the interface but not needed here
	public void selectedChain(StructureEvent event){}
	public void newObjectRequested(String name){}
	
    }

}
