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
 * Created on 22.09.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.spice ;




import java.util.List  ;
import java.util.ArrayList ;
import java.awt.Graphics ;

/** a class to store FeatureData and to visualize them
 * coordinate system of features is always UniProt !
 * PDBresnum features served by DAS need to be converted into UniProt coord sys first.
 *
 * a feature consists of one or several segments.
 * segmetns cotnains <start> and <end> information.
 *
 * @author Andreas Prlic
 */
public class Feature {
    String name   ;
    String method ;
    String type   ;
    List   segments ;
    String note   ;
    String link   ;


    public Feature() {
	method = "Unknown";
	type   = "Unknown";
	note   = "";
	link   = "";
	
	segments = new ArrayList();
	       
    }

    public String toString() {
	String str = "Feature: method: " + method +" type: " + type + " note: "+note + " link: "+ link;
	
	str += segments ;
	return str ;
    }

    public void setName(String nam) { name = nam; }
    public String getName() { return name; }
    
    public void setMethod(String methd) { method = method ; }
    public String getMethod() { return method ; }

    public void setType(String typ) { type = typ ; }
    public String getType() { return type ; }
    
    public void setNote(String nte) { note = nte; }
    public String getNote() { return note ; }
    
    public void setLink(String lnk) { link = lnk;}
    public String getLink() { return link;}

    /** add a segment to this feature */
    public void addSegment(int start, int end, String name) {
	Segment s = new Segment() ;
	s.setStart(start);
	s.setEnd(end) ;
	s.setName(name);
	s.setParent(this);
	segments.add(s);
    }
    
    public void addSegment( Segment s ){
	s.setParent(this);
	segments.add(s);
    }

    public List getSegments() { return segments ;}

    /** draw this feature at graphics g, at a given Y coordinate */
    public void draw ( Graphics g, int y, double scale) {
	
    }

}




