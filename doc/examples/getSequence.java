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
import org.biojava.spice.das.SequenceThread;
import org.biojava.spice.das.SpiceDasSource;
import org.biojava.spice.manypanel.eventmodel.SequenceListener;
import org.biojava.spice.manypanel.eventmodel.SequenceEvent;

/** a class that demonstrates how to get a protein structure
 * from a structure DAS server.
 */


public class getSequence {

    public static void main (String[] args) {
	

	getSequence s = new getSequence();
	s.showExample();
	
    }

    public void showExample(){
	try {



	    // the sequence example is very similar to the structure example.
	    // a new Thread is created that does the DAS communication
	    // a Listener waits for the response.

	    // first we set some system properties
	   
	    // make sure we use the Xerces XML parser..
	    System.setProperty("javax.xml.parsers.DocumentBuilderFactory","org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
	    System.setProperty("javax.xml.parsers.SAXParserFactory","org.apache.xerces.jaxp.SAXParserFactoryImpl");
	    

	    // if you are behind a proxy, please uncomment the following lines
	    System.setProperty("proxySet","true");
	    System.setProperty("proxyHost","wwwcache.sanger.ac.uk");
	    System.setProperty("proxyPort","3128");




	    
	    // first let's create a SpiceDasSource which knows where the
	    // DAS server is located.
	    
	    SpiceDasSource dasSource = new SpiceDasSource();
	    
	    dasSource.setUrl("http://www.ebi.ac.uk/das-srv/uniprot/das/aristotle/");
	    
	    
	    String pdbCode = "P50225";
	    
	    // now we create the thread that will fetch the structure
	    SequenceThread thread = new SequenceThread(pdbCode,dasSource);
	    
	    // add a structureListener that simply prints the PDB code
	    SequenceListener listener = new MyListener();
	    thread.addSequenceListener(listener);

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



    class MyListener 
	implements SequenceListener {

	/** this method is called when the Thread finishes 
	    it prints out the sequence as in Fasta format
	*/
	public synchronized void newSequence(SequenceEvent event){
	    String accessionCode = event.getAccessionCode();
	    String sequence = event.getSequence();
	    System.out.println(">"+accessionCode);
	    System.out.println(sequence);
	    System.exit(0);
	}
	


	// the methods below are required by the interface but not needed here
	public void newObjectRequested(String name){}
	public void selectionLocked(boolean flag){}
	public void selectedSeqRange(int start, int end){}
	public void selectedSeqPosition(int position){}



    }

}
