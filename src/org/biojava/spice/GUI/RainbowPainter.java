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
 * Created on Apr 13, 2006
 *
 */
package org.biojava.spice.GUI;

import java.awt.Color;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.spice.Panel.JmolCommander;


/** gets the selection or the whole structure from spice and colors it by rainbow ...
 * 
 * @author Andreas Prlic
 * @since 5:52:01 PM
 * @version %I% %G%
 */
public class RainbowPainter {

    public RainbowPainter(JmolCommander listener, Group[] selection) {
        super();

        
        
        float stepsize = 1.0f / (float)selection.length * 0.7f;
        float saturation = 1.0f;
        float brightness = 1.0f;
        StringBuffer cmd = new StringBuffer();
        String chainId = " ";
        for (int  i=0;i<selection.length;i++){
            Group g = selection[i];
            Chain parent = g.getParent();
            
            if ( parent != null) {
               
                chainId = parent.getName();
                //System.out.println("got chain >" + chainId + "< from parent");
            }
           
            float hue = i * stepsize ;
            
            //System.out.println(">"+chainId + "< " + g.getPDBCode() + " "+ i+ " " + stepsize + " "+ red + " " + green + " " + blue );
            Color col = Color.getHSBColor(hue,saturation,brightness);
            
            cmd.append("select "+g.getPDBCode()+":"+chainId+";");
            //cmd += " color [" +red+","+green +","+blue +"];";
            
            cmd.append(" color [" +col.getRed()+","+col.getGreen() +","+col.getBlue() +"];");
        
        }
//        if ( selection.length >=2){
//            Group g1 = selection[0];
//            Group g2 = selection[selection.length-1];
//            
//            String ci1 = " ";
//            String ci2 = " ";
//            
//            Chain c1= g1.getParent();
//            if ( c1 != null){
//                ci1 = c1.getName();
//            }
//            Chain c2 = g2.getParent();
//            if ( c2 != null) {
//                ci2 = c2.getName();
//            }
//            cmd.append("select " + g1.getPDBCode() + ":"+ci1+"-"+g2.getPDBCode()+":"+ci2+";");
//        }
        
        listener.executeCmd(cmd.toString());
        
    }

}
