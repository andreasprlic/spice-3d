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
 * Created on 23.09.2004
 * @author Andreas Prlic
 *
 */


package org.biojava.spice.feature ;

import java.util.Comparator ;


/** a comparator to sort Features byt type
 * @author Andreas Prlic
 */

public class FeatureComparator 
    implements Comparator
{

    public FeatureComparator() {
    }

    public int compare(Object a, Object b) {
	FeatureImpl x = (FeatureImpl) a;
	FeatureImpl y = (FeatureImpl) b;

	String typea = x.getType();
	String typeb = y.getType();
	
	return typea.compareTo(typeb);
    }

}