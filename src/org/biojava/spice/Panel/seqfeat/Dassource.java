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
package org.biojava.spice.Panel.seqfeat;
import java.awt.Graphics2D;

/**
 * a container for several lines and features
 * 
 * @author Andreas Prlic
 *
 */
public class Dassource {

    	org.biojava.spice.Panel.seqfeat.Line[] lines;
    	int default_Y_separation;
    	
    	/** 
    	 * 
    	 * @param y_separation separation between lines
    	 */
    public Dassource(int y_separation){
        default_Y_separation = y_separation;
    }
    
    /** go over all lines belonging to this DAs source and paint them */
    public int paint (Graphics2D g, int y){
        for (int i=0; i<lines.length;i++){
            Line line = lines[i];
            line.paint(g,y);
            y+= default_Y_separation;
        }
        return -1;
        
    }
}
