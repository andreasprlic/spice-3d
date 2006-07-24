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
 * Created on Oct 28, 2005
 *
 */
package org.biojava.spice.manypanel.drawable;

import java.awt.Color;

/** the interface for something that can be drawn
 * 
 * @author Andreas Prlic
 *
 */
public interface Drawable 

{
    
    /** set the color of the object
     * 
     * @param col
     */
    public void setColor(Color col);
    
    /** get the color of the object
     * 
     * @return a color
     */
    public Color getColor();
    
    /** the data for this drawable is currently being loaded
     * 
     * @param flag
     */
    public void setLoading(boolean flag);
    
    /** is the Drawable currently being loaded?
     * 
     * @return a flag if currently is loading data
     */
    public boolean getLoading();
    
  
    
}
