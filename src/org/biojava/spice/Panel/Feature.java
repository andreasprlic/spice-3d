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

package org.biojava.spice  ;

import java.util.ArrayList ;
import java.util.HashMap   ;


class Feature {

    ArrayList segments ;
    String name ;
    public Feature(){
	segments = new ArrayList();
    }
    
    public void addSegment(HashMap segment){
	segments.add(segment) ;
    }
    public ArrayList getSegments() {
	return segments ;
    }
    public void setName(String n) {
	name = n ;
    }
    public String getName(){
	return name ;
    }
    public String toString(){
	String str ="feature "+name+" segments:"+segments.size();
	return str ;
    }
    
}
