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

import java.awt.Color ;


/** a class to keep track of location information for a feature */
class Segment {
    int start   ;
    int end     ;
    String name ;
    Color color ;
    Feature parent ;
    String txtColor ;
    public Segment() {
	start = 0 ;
	end   = 0 ;
	name  = "Unknown";
	color = Color.white ;
	txtColor = "white" ;
	parent = null ;
    }

    public String toString() {
	String str = "Segment: " +name + " " +start + " " + end ;
	return str ;
    }

    public void setStart(int strt) {start = strt ; }
    public int  getStart() {return start ;}
    
    public void setEnd(int ed) { end = ed;}
    public int getEnd() { return end;}

    public void setName(String nam) { name = nam;}
    public String getName() { return name ; }

    public void setColor(Color col) { color = col; }
    public Color getColor() { return color ; }

    public void setParent(Feature f) { parent = f;}
    public Feature getParent(){ return parent;}
    
    public void setTxtColor(String str) { txtColor = str; }
    public String getTxtColor() { return txtColor;}
    
}
