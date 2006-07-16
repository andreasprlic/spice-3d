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

package org.biojava.spice.Feature;




import java.util.Iterator;
import java.awt.Graphics2D ;

/** a class to store FeatureData and to visualize them
 * coordinate system of features is always UniProt !
 * PDBresnum features served by DAS need to be converted into UniProt coord sys first.
 *
 * a feature consists of one or several segments.
 * segmetns cotnains <start> and <end> information.
 *
 * @author Andreas Prlic
 */
public class FeatureImpl 
extends AbstractFeature
implements Feature

{
   
    /** draw this feature at graphics g, at a given Y coordinate 
     * the scaling is done at the segment level. Segments know about scaling
     * 
     */
    public void draw ( Graphics2D g, int y) {
        Iterator iter = segments.iterator();
        
        while ( iter.hasNext()){
            Segment s = (Segment) iter.next();
            s.paint(g,y);
        }
    }
    

}




