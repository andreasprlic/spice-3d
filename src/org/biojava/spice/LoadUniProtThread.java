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
 * Created on 20.03.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.spice ;

public class LoadUniProtThread 
    extends Thread {

    SPICEFrame spiceframe ;
    boolean finished ;
    String uniprot   ;
    public LoadUniProtThread(SPICEFrame master,String code) {
	finished = false ;
	spiceframe = master ;
	
	uniprot=code ;
    } 

    public void run () {
	loadCompound() ;
    }

    public boolean isDone() {
	return finished ;
    }
    public synchronized void loadCompound() {
	
	try {
	    //spiceframe.showStatus("Loading...Wait...",Color.red);
	    spiceframe.setLoading(true);
	    
	    // do something ...

	    finished = true ;
	    spiceframe.setLoading(false);
	    notifyAll();

	}catch (Exception e){ 
	    // at some point raise some IO exception, which should be defined by the Inferface
	    e.printStackTrace();
	    finished = true ;	  			
	}
    }
}
