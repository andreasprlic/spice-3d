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
package org.biojava.spice.Feature;

import java.awt.Graphics2D;
import java.util.List;

/**
 * @author Andreas Prlic
 *
 */
public interface Feature {
    public abstract String toString();

    public abstract void setSource(String s);

    public abstract String getSource();

    public abstract void setName(String nam);

    public abstract String getName();

    public abstract void setMethod(String methd);

    public abstract String getMethod();

    public abstract void setType(String typ);

    public abstract String getType();

    public abstract void setNote(String nte);

    public abstract String getNote();

    public abstract void setLink(String lnk);

    public abstract String getLink();
    
    public abstract void setScore(String score);
    
    public abstract String getScore();

    /** add a segment to this feature */
    public abstract void addSegment(int start, int end, String name);

    public abstract void addSegment(Segment s);

    public abstract List getSegments();

    /** draw this feature at graphics g, at a given Y coordinate 
     * the scaling is done at the segment level. Segments know about scaling
     * 
     * */
    public abstract void draw(Graphics2D g, int y);
}