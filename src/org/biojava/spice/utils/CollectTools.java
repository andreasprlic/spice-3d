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
 * Created on Jun 9, 2005
 *
 */
package org.biojava.spice.utils;

import java.util.*;

/**
 * Utilities for working with collections.
 *
 * @author Thomas Down
 */
public class CollectTools {
    public static int[] toIntArray(Collection l) {
        int[] a = new int[l.size()];
        int i = 0;
        for (Iterator j = l.iterator(); j.hasNext(); ) {
            a[i++] = ((Number) j.next()).intValue();
        }
        return a;
    }
    
    public static double[] toDoubleArray(Collection l) {
        double[] a = new double[l.size()];
        int i = 0;
        for (Iterator j = l.iterator(); j.hasNext(); ) {
            a[i++] = ((Number) j.next()).doubleValue();
        }
        return a;
    }
    
    public static Object randomPick(Collection col) {
        Object[] objs = col.toArray(new Object[col.size()]);
        return objs[(int) Math.floor(Math.random() * objs.length)];
    }
}
