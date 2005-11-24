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
 * Created on 22.11.2004
 */

package org.biojava.spice.manypanel;

import org.biojava.utils.*;
import org.biojava.utils.cache.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.db.biosql.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.program.das.dasalignment.*;
import org.biojava.bio.program.das.*;
import org.biojava.bio.program.gff.*;
import org.biojava.bio.program.das.dasalignment.*;
import org.biojava.utils.xml.*;
import org.biojava.spice.DAS.*;

import java.util.* ;
import java.net.*;

public class AlignmentTool {

   
    
    public static String getCigar(Alignment a, String intObjectId) 
	throws Exception
    {
	Annotation[] objects = a.getObjects();
	Annotation[] blocks = a.getBlocks();
	if ( blocks.length > 1 ) {
	    throw new Exception("Not Implemented Error, blocks> 1");
	}
	Annotation block = blocks[0];
        List segments = (List)block.getProperty("segments");
	for ( int i = 0 ; i < segments.size(); i++ ) {
	    Annotation segment = (Annotation)segments.get(i);
	    String sintObjectId = (String)segment.getProperty("intObjectId");
	
	    if (  intObjectId.equals(sintObjectId) ) {
		String cigar = (String)segment.getProperty("cigar");
		return cigar;
	    }
	       	    
	}
	throw new Exception("no object with id " + intObjectId + " found !");
    }


    public static String extractId(Alignment a,String code) {
	Annotation[] objects = a.getObjects();
	for ( int i = 0 ; i < objects.length; i++ ) {
	    Annotation obj = objects[i];
	    String source = (String)obj.getProperty("dbSource");
	    if (  source.equals(code) ) {
		return (String)obj.getProperty("dbAccessionId");
	    }
	    
	}
	return null ;
    }


    public static List createAlignmentTable(Alignment ali, String code) {
	try { 
	    String cigar = getCigar(ali,code);
	    return getTableByCigar(cigar) ;
	} catch (Exception e) {
	    // alignment does not have Cigar string...
	    // use blocks from - to to create map...

	    List mapList = new ArrayList();
	    Annotation[] blocks = ali.getBlocks();
	    int arraypos = -1 ;
	    
	    for ( int i=0; i < blocks.length ; i++) {
		Annotation block  = blocks[i];
		List segments = (List)block.getProperty("segments");
		for ( int j = 0 ; j < segments.size(); j++ ) {
		    Annotation segment = (Annotation)segments.get(j);
		    String sintObjectId = (String)segment.getProperty("intObjectId");
		    if ( sintObjectId.equals(code)) {
			arraypos++ ;
			
			// get start, end of segment
			String sstart = (String)segment.getProperty("start");
			String send   = (String)segment.getProperty("end");
			int start = -9999;
			int end   = -9999;
			try {
			    start = Integer.parseInt(sstart);
			    end   = Integer.parseInt(send);
			} catch (NumberFormatException ex) {
			    // an insertion code found!
			    Map map = new HashMap();
			    map.put("arraypos", new Integer(arraypos));
			    map.put("seqpos",sstart);
			    mapList.add(map);
			    continue;
			}
			for ( int k=start; k<=end;k++) {
			    Map map = new HashMap();
			    map.put("arraypos", new Integer(arraypos));
			    map.put("seqpos"  , new Integer(k));
			    mapList.add(map);
			}		    
		    }
		}		
		
	    }
	    return mapList;
	} 
	//catch (Exception e) {
	//   e.printStackTrace();
	//    return null ;
	//	}
    }

    /** returns a list of maps */
    public static List getTableByCigar(String cigar) {


	List mapList = new ArrayList();
	// split a string into it's fragments 
	StringBuffer numberBuffer = new StringBuffer()  ;
	

	int arraypos = 0 ;
	int seqpos   = 0 ;
	for ( int i =0 ; i < cigar.length() ; i++ ) {
	    char c = cigar.charAt(i);
	    Character ch = new Character(c);
	    if (Character.getType(c) == Character.DECIMAL_DIGIT_NUMBER) {
		// got a number
		
		//System.out.println(ch + " = number");
		numberBuffer.append(c);

		
	    }
	    else {
		String s = numberBuffer.toString();
		int size = 1 ;
		try {
		    if ( s.length() > 0 ) 
			size = Integer.parseInt(s);
		} catch ( NumberFormatException e) {
		    e.printStackTrace();
		}
		//System.out.println(size + " " + c);

		numberBuffer = new StringBuffer() ;

		// fill up master tabl
		for ( int j =0 ; j < size; j++ ) {
		    arraypos++;
		    Integer value = null ;
		    if (c=='M') {
			seqpos++;
			value = new Integer(seqpos);
		    }
		    Map map = new HashMap();
		    map.put("arraypos", new Integer(arraypos));
		    map.put("seqpos"  , value);
		    mapList.add(map);
		}
	    }
	}


	
	return mapList;
    }
   

   



}
