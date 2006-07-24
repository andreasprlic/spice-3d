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
package org.biojava.spice.das;

import java.util.List;
import java.util.logging.*;

import org.biojava.bio.Annotation;
import org.biojava.bio.program.das.dasalignment.Alignment;
import org.biojava.bio.program.das.dasalignment.DASAlignmentCall;
import org.biojava.dasobert.das.AlignmentThread;
import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.spice.config.RegistryConfiguration;
import java.net.*;
import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.*;

/**
 * @author Andreas Prlic
 *
 */
public class AlignmentTools {

    Logger logger;
    RegistryConfiguration config;
    DASAlignmentCall dasalignmentCall;
    static String SEQUENCEDATABASE  = "UniProt,Protein Sequence" ;
    static String STRUCTUREDATABASE = "PDBresnum,Protein Structure" ;
    
    

    /**
     * 
     * @param conf the configuration to be used
     */
    public AlignmentTools(RegistryConfiguration conf) {
        super();
        config=conf;
        logger = Logger.getLogger("org.biojava.spice");
        dasalignmentCall= new DASAlignmentCall();
		

    }
    
    public static Annotation getObject(String s, Alignment a){
        return AlignmentThread.getObject(s,a);
    }
    
    /** get alignments for a particular uniprot or pdb code
     * 
     * @param code
     * @return an array of Alignment object
     */
    public  Alignment[] getAlignments(String code) {
    	logger.finest("searching for alignments of "+code+" against PDB");
    	Alignment[] alignments = null ;

    	List aligservers = config.getServers("alignment");
    	logger.finest("found " + aligservers.size() + " alignment servers");

	String 	dasalignmentcommand = null  ;
    	
    	// loop over all available alignment servers 
    	for ( int i =0 ; i < aligservers.size() ; i++ ) {
    	    SpiceDasSource sds= (SpiceDasSource)aligservers.get(i);
    	   
    	    logger.finest("investigating " + i + " url" + sds.getUrl());
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
    		
    		try{
    		    //alignments = dasc.getAlignments(code);
    		    alignments= retreiveAlignments(dasalignmentcommand+code);
    		    
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
    
    
   
    
    public static String getUniProtCodeFromAlignment(Alignment ali) {
        Annotation[] objects = ali.getObjects();
    	for (int i =0 ; i<objects.length;i++) {
    	    Annotation object = objects[i];
    	    String dbCoordSys = (String)object.getProperty("dbCoordSys");

    	    if ( dbCoordSys.equals(SEQUENCEDATABASE) ) {		
    		return (String)object.getProperty("dbAccessionId") ;
    	    }

    	    /** TODO: fix this */
    	    // tmp until alginmnet server supports new coord sys:
    	    if ( dbCoordSys.equals("UniProt"))
    		return (String)object.getProperty("dbAccessionId") ;
    	}
    	
    	throw new NoSuchElementException("did not find a UniProt code in alignment") ;
        
    }
    
    public static String getPDBCodeFromAlignment(Alignment ali) {
	Annotation[] objects = ali.getObjects();
	for (int i =0 ; i<objects.length;i++) {
	    Annotation object = objects[i];
	    String dbCoordSys = (String)object.getProperty("dbCoordSys");

	    if ( dbCoordSys.equals(STRUCTUREDATABASE) ) {		
		return (String)object.getProperty("dbAccessionId") ;
	    }

	    /** TODO: fix this */
	    // tmp until alginmnet server supports new coord sys:
	    if ( dbCoordSys.equals("PDBresnum"))
		return (String)object.getProperty("dbAccessionId") ;
	}
	
	throw new NoSuchElementException("did not find a PDB code in alignment") ;
    }

    private Alignment[] retreiveAlignments(String url)
	throws IOException
    {
	/* now connect to DAS server */
	
	URL dasUrl = null ;
	try {
	    dasUrl = new URL(url);
	} catch (Exception e) {
	    throw new IOException("error during creation of URL " + e.getMessage());
	}
	
	InputStream inStream = connectDASServer(dasUrl);
	

	Alignment[] ali = null;
	try{
	    ali =  dasalignmentCall.parseDASResponse(inStream) ;
	} catch (Exception e) {
	    throw new IOException("error during creation of URL " + e.getMessage());
	}
	return ali;
	
    }


    /** connect to DAS server and return result as an InputStream.
     *
     */    
    private InputStream connectDASServer(URL url) 
	throws IOException
    {
	InputStream inStream = null ;
				
	System.out.println("opening connection to "+url);
	HttpURLConnection huc = org.biojava.spice.SpiceApplication.openHttpURLConnection(url);  
	 

	//System.out.println("temporarily disabled: accepting gzip encoding ");
	// should make communication much faster!
	huc.setRequestProperty("Accept-Encoding", "gzip");
	
	System.out.println("response code " +huc.getResponseCode());
	String contentEncoding = huc.getContentEncoding();
	System.out.println("getting InputStream");
	inStream = huc.getInputStream();
	if (contentEncoding != null) {
	    if (contentEncoding.indexOf("gzip") != -1) {
		// we have gzip encoding
		inStream = new GZIPInputStream(inStream);
		System.out.println("using gzip encoding!");
	    }
	}
	System.out.println("got InputStream from  DAS Alignment server");
	System.out.println("encoding: " + contentEncoding);

	return inStream;
	
    }
    
    
}
