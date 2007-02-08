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
 * Created on Feb 8, 2007
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.Color;


public class DrawUtils {
    
    public static Color getAlphaGradient(Color c1, double ratio){
        int alpha = (int)(255 * ratio);
        Color c = new Color(c1.getRed(), c1.getGreen(),
                            c1.getBlue(), alpha);
        return c;
    }
    
    public static Color getColorGradient(Color c1, Color c2, double ratio){
        
          int red = (int)(c2.getRed() * ratio + c1.getRed() * (1 - ratio));
          int green = (int)(c2.getGreen() * ratio +
                            c1.getGreen() * (1 - ratio));
          int blue = (int)(c2.getBlue() * ratio +
                           c1.getBlue() * (1 - ratio));
          Color c = new Color(red, green, blue);
          return c;
    }
    
    
  

}
