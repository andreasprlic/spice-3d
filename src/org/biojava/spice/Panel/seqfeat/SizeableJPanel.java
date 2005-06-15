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
 * Created on Jun 14, 2005
 *
 */
package org.biojava.spice.Panel.seqfeat;


import javax.swing.*;
import java.awt.Dimension;


/** A JPanel that can have a specified size
 * @author Andreas Prlic
 *
 */


public class SizeableJPanel
extends JPanel
{
    public static final int DEFAULT_WIDTH  = 60;
    public static final int DEFAULT_HEIGHT = 30;
    
    private int height, width;
    
    
    public SizeableJPanel() {
        super();
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
    }
    public SizeableJPanel( int width, int height ){
        super();
        this.width = width;
        this.height = height;
        
    }
    
    public void setHeight(int height){
        this.height = height;
    }
    
    public void setWidth(int width){
        this.width = width;
    }
    public Dimension getMaximumSize(){ 
        Dimension d;
        
        if ( height == 0 || width == 0 ){
            d = super.getMaximumSize();
            
            if ( height != 0 )
                d.setSize( d.getWidth(), height );
            
            else
                d.setSize( width, d.getHeight() );    
        }
        else
            d = new Dimension( width, height );
        
        return d; 
    }
    
    public Dimension getPreferredSize(){ 
        Dimension d;
        
        if ( height == 0 || width == 0 ){
            d = super.getPreferredSize();
            
            if ( height != 0 )
                d.setSize( d.getWidth(), height );
            
            else
                d.setSize( width, d.getHeight() );
            
        }
        else
            d = new Dimension( width, height );
        
        
        return d; 
    }
    
    /** the width is flexible, but the height is not ... */
    public Dimension getMinimumSize(){ 
        Dimension d;
        
        d = super.getPreferredSize();
        if ( height != 0 )
            d.setSize(d.getWidth(), height);
        
        return d; 
    }
   
}
