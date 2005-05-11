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

import org.biojava.spice.DAS.DAS_Feeder ;
import org.biojava.bio.structure.Structure ;
import org.biojava.bio.structure.StructureImpl ;
import java.util.logging.*                             ;
import org.biojava.spice.Config.*;

public class LoadUniProtThread 
    extends Thread {

    SPICEFrame spiceframe ;
    boolean    finished   ;
    String     uniprot    ;
    Logger     logger     ;

    public LoadUniProtThread(SPICEFrame master,String code) {
	finished = false ;
	spiceframe = master ;
	
	uniprot=code ;
	logger = Logger.getLogger("org.biojava.spice");
	logger.finest("init uniprot thread");
    } 

    public void run () {
	logger.finest("running UniProtThread");
	loadCompound() ;
    }

    public boolean isDone() {
	return finished ;
    }
    public synchronized void loadCompound() {
	
	try {
	    logger.finest("loading uniprot " + uniprot);
	    spiceframe.showStatus("Loading...Wait...");
	    spiceframe.setLoading(true);
	    
	    // do something ...
	    RegistryConfiguration config = spiceframe.getConfiguration();
	    while ( config == null){
	        try {
	            wait(30);
	            config = spiceframe.getConfiguration();
	        } catch (InterruptedException e){
	            logger.info("did not get configuration");
	            return;
	        }
	        
	    }
	     
	    if (uniprot == null){
	        logger.finest("got uniprot code null, returning");
	    }
	    DAS_Feeder dasUp = new DAS_Feeder (config);
	    Structure struc = dasUp.loadUniProt(uniprot);
	    spiceframe.setStructure(struc);

	    finished = true ;

	    spiceframe.setLoading(false);
	    spiceframe.showStatus(uniprot +" loaded");
	    notifyAll();

	}catch (Exception e){ 
	    // at some point raise some IO exception, which should be defined by the Inferface
	    e.printStackTrace();
	    finished = true ;	  
	    StructureImpl n = new StructureImpl();
	    spiceframe.setStructure(n);
	}
    }
}
