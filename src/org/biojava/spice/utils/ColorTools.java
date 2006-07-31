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
 * Created on Jul 31, 2006
 *
 */
package org.biojava.spice.utils;

import java.awt.Color;

public class ColorTools {
  
    
    /** convert a Color to a html style hexadecimal String representation
     * e.g.  new Color(0,153,255); will be shown as #0099ff
     * @param c color
     * @return a HTML style String representation of the color
     */
    public static String colorToString(Color c) {
        char[] buf = new char[7];
        buf[0] = '#';
        String s = Integer.toHexString(c.getRed());
        if (s.length() == 1) {
            buf[1] = '0';
            buf[2] = s.charAt(0);
        }
        else {
            buf[1] = s.charAt(0);
            buf[2] = s.charAt(1);
        }
        s = Integer.toHexString(c.getGreen());
        if (s.length() == 1) {
            buf[3] = '0';
            buf[4] = s.charAt(0);
        }
        else {
            buf[3] = s.charAt(0);
            buf[4] = s.charAt(1);
        }
        s = Integer.toHexString(c.getBlue());
        if (s.length() == 1) {
            buf[5] = '0';
            buf[6] = s.charAt(0);
        }
        else {
            buf[5] = s.charAt(0);
            buf[6] = s.charAt(1);
        }
        return String.valueOf(buf);

    }

}
