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
import java.util.*;
import org.biojava.spice.Feature.*;

// obsolete!
// remove...

/** a class that takes care about the information regarding a line in the SeqFeaturePanel display 
 * @author Andreas Prlic
 *
 */
public class Line {
    int start;
    int end;
    int xStart;
    int xEnd;
    int yStart;
    int yEnd;
    
    List features;
    public Line(){
        start = 0;
        end = 0;
        xStart =0;
        xEnd =0;
        yStart=0;
        yEnd=0;
        
        features = new ArrayList();
    }
 
    
    public void setStart(int startPosition){start = startPosition;}
    public int getStart(){return start;}
    public void setEnd(int endPosition){ end=endPosition;}
    public int getEnd(){return end;}
    
    /** a List of Feature objects 
     * 
     * @param features_
     */
    public void setFeatures(List features_){        features =features_;    }
    public List getFeatures(){ return features;}
    
    public void setXStart(int x){xStart =x;}
    public int getXStart(){return xStart;}
    
    public void setXEnd(int x){xEnd=x;}
    public int getXEnd(){ return xEnd;}
    
    public void setYStart(int y){yStart = y;}
    public int getYStart(){return yStart;}
    public void setYEnd(int y){yEnd =y;}
    public int getYEnd(){return yEnd;}
    
    /** calculate the coordinates in pixels */
    public void scale(float scale){
        Iterator iter = features.iterator();
        
        while ( iter.hasNext()){
            FeatureImpl f = (FeatureImpl) iter.next();
            
            
        }
        
    }
    /** x,y are scaled according to scale
     * 
     * @param x x coordinate in pixels
     * @param y y coordinate in pixels
     * @return
     */
    public boolean dotInRange(int x, int y){
        if ( ((x>=xStart ) && (x<=xEnd)) &&
             (( y>=yStart) && (y<=yEnd))
        	   )
            return true;
        else
            return false;
    }
    
    /** paint all features in this line */
    public int paint (Graphics2D g, int y){
        Iterator iter = features.iterator();
        
        while ( iter.hasNext()){
            FeatureImpl f = (FeatureImpl) iter.next();
            //f.paint(g,y);
            
            
        }
        return y;
    }
    
}
