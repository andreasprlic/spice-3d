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
 * Created on Feb 4, 2005
 *
 */
package org.biojava.spice.DAS;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.*;

import org.biojava.bio.Annotation;
import org.biojava.bio.program.das.dasalignment.Alignment;
import org.biojava.bio.program.das.dasalignment.DASAlignmentClient;
import org.biojava.spice.Config.RegistryConfiguration;
import org.biojava.spice.Config.SpiceDasSource;

/**
 * @author Andreas Prlic
 *
 */
public class AlignmentTools {

    Logger logger;
    RegistryConfiguration config;
    
    /**
     * 
     */
    public AlignmentTools(RegistryConfiguration conf) {
        super();
        config=conf;
        logger = Logger.getLogger("org.biojava.spice");


    }
    
    /** get alignments for a particular uniprot or pdb code */
    public  Alignment[] getAlignments(String code) {
    	logger.finest("searching for alignments of "+code+" against PDB");
    	Alignment[] alignments = null ;

    	List aligservers = config.getServers("alignment");
    	logger.finest("found " + aligservers.size() + " alignment servers");

    String 	dasalignmentcommand = null  ;
    	
    	// loop over all available alignment servers 
    	for ( int i =0 ; i < aligservers.size() ; i++ ) {
    	    SpiceDasSource sds= (SpiceDasSource)aligservers.get(i);
    	   
    	    logger.finest("investigating " + sds.getUrl());
    	    //System.out.println("investigating" + sds.getUrl());
    	    // only consider those serving uniprot and PDB alignments
    	    if ( config.isSeqStrucAlignmentServer(sds) ) {
    		
    		
    		String url = sds.getUrl() ;
    		char lastChar = url.charAt(url.length()-1);		 
    		if ( ! (lastChar == '/') ) 
    		    url +="/" ;
    		dasalignmentcommand  = url +  "alignment?query=" ;

    		logger.info("contacing alignment server " + dasalignmentcommand+code);
    		//System.out.println("contacing alignment server " + dasalignmentcommand);
    		DASAlignmentClient dasc= new DASAlignmentClient(dasalignmentcommand);
    		
    		try{
    		    alignments = dasc.getAlignments(code);
    		    
    		    logger.finest("DASAlignmentHandler: got "+ alignments.length +" alignment(s):");
    		    if ( alignments.length == 0 ) {
    			// check next alignment server ...
    			continue ;
    		    }
    		    return alignments ;
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    	    }
    	}
    	
    	    

    	logger.log(Level.SEVERE,"no UniProt - PDBresnum alignment found!, unable to map sequence to structure");
    	
    	

    	return null ;
    }
    
    public String getPDBCodeFromAlignment(Alignment ali) {
	Annotation[] objects = ali.getObjects();
	for (int i =0 ; i<objects.length;i++) {
	    Annotation object = objects[i];
	    String dbCoordSys = (String)object.getProperty("dbCoordSys");

	    if ( dbCoordSys.equals("PDBresnum") ) {		
		return (String)object.getProperty("dbAccessionId") ;
	    }
	}
	
	return null ;
    }

}
