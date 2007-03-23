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
 * Created on Feb 9, 2005
 *
 */
package org.biojava.spice.feature;

import java.awt.Graphics2D;
import java.util.List;

/**
 * @author Andreas Prlic
 *
 */
public interface Feature {
    
	
	public Object clone();
	
    /** returns true if the specified sequence position is within the range of this Feature
     * 
     * @param seqPosition the position to check
     * @return true if the position is within the ranges of the segments of this feature
     */
    public boolean overlaps(int seqPosition);
    
    public  String toString();

    public  void setSource(String s);

    public  String getSource();

    public  void setName(String nam);

    public  String getName();

    public  void setMethod(String methd);

    public  String getMethod();

    public  void setType(String typ);

    public  String getType();

    public  void setNote(String nte);

    public  String getNote();

    public  void setLink(String lnk);

    public  String getLink();
    
    public  void setScore(String score);
    
    public  String getScore();
    
    /** test if two features are equivalent
     * 
     * @param feat feature to compare with 
     * @return true if equivalend
     */
    public abstract boolean equals(Feature feat);

    /** add a segment to this feature
     * 
     * @param start position
     * @param end position 
     * @param name of feature
     */
    public abstract void addSegment(int start, int end, String name);

    public abstract void addSegment(Segment s);

    public abstract List getSegments();

    /** draw this feature at graphics g, at a given Y coordinate 
     * the scaling is done at the segment level. Segments know about scaling
     * @param y the y position where to start drawing  
     * @param g the graphics2D object to use for painting
     * @deprecated
     * */
    public abstract void draw(Graphics2D g, int y);
}