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
 * Created on Jun 5, 2005
 *
 */
package org.biojava.spice.Panel.seqfeat;
import org.biojava.spice.Feature.*;
import org.biojava.spice.Panel.seqfeat.FeaturePanel;
import org.biojava.spice.Panel.seqfeat.LabelPane;
import org.biojava.spice.Panel.seqfeat.TypeLabelPanel;

import javax.swing.*;
import java.util.logging.Logger;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.Iterator;

/** A class that renders features. E.g. all the features retrieved from a DasSource.
 * It consists of a Label and the rendered features.
 * 
 * @author Andreas Prlic
 *
 */
public class FeatureView 
extends JPanel
 {
    
    public static final int    DEFAULT_X_START        = 20  ;
    public static final int    DEFAULT_X_RIGHT_BORDER = 20 ;
    public static final int    DEFAULT_Y_START        = 0 ;
    public static final int    DEFAULT_Y_STEP         = 10 ;
    public static final int    DEFAULT_Y_HEIGHT       = 4 ;
    public static final int    DEFAULT_Y_BOTTOM       = 16 ;
    
    // the line where to draw the structure
    public static final int    DEFAULT_STRUCTURE_Y    = 20 ;
    public static final int    MINIMUM_HEIGHT         = 30;
    
    //Logger logger        ;
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    
    int labelWidth;
    String label;
    Feature[] features;
    
    JSplitPane splitPane;
    LabelPane labelField;
    JSplitPane typeSplitPane;
    FeaturePanel featureCanvas;
    TypeLabelPanel typeLabelPanel;
    
    int seqLength ;
       boolean selected;
    boolean isLoading;
    /**
     * 
     */
    public FeatureView() {
        super();
        // TODO Auto-generated constructor stub
        this.label="";
        features = null;
        seqLength = 0;
        labelWidth = 60;
        
        labelField = new LabelPane();
        labelField.setLabel(label);
        labelField.setBorder(BorderFactory.createEmptyBorder());
        
        featureCanvas = new FeaturePanel();
        featureCanvas.setBorder(BorderFactory.createEmptyBorder());
        
        typeLabelPanel = new TypeLabelPanel();
        typeLabelPanel.addSelectedFeatureListener(featureCanvas);
        typeLabelPanel.setBorder(BorderFactory.createEmptyBorder());
        
        
       isLoading = false;
    }
    
    public FeaturePanel getFeaturePanel() {
        return featureCanvas;
    }
    public LabelPane getLabel() {
        return labelField;
    }
    public TypeLabelPanel getTypePanel(){
        return typeLabelPanel;
    }
    
    public void setScale(float scale) {
        featureCanvas.setScale(scale);
        
    }
    public float getScale(){
        return featureCanvas.getScale();
    }
    
    public int getHeight() {
        //System.out.println("getting height");
        if ( features == null ){
            //return DEFAULT_Y_START+DEFAULT_Y_STEP ;
            return MINIMUM_HEIGHT;
        }
        if ( features.length == 0)
            return MINIMUM_HEIGHT;
        
        int h = (features.length*DEFAULT_Y_STEP) + DEFAULT_Y_START+DEFAULT_Y_STEP ;
        	if ( h > MINIMUM_HEIGHT )
        	    return h;
        	
        	else return MINIMUM_HEIGHT;
        
    }
    
    public void setCanvasHeight(int height) {
     featureCanvas.setCanvasHeight(height);
     typeLabelPanel.setCanvasHeight(height);
     labelField.setCanvasHeight(height);
     
    }
    
    /**  display a nice JSsscroller to display that data is being loaded ... */
    	public void setLoading(boolean flag){
    	    isLoading = flag;
    	    featureCanvas.setLoading(flag);
    	}
    
  /** set the length of the displayed sequence
   * 
   * @param seqLength
   */  
    public void setSeqLength(int seqLength){
        this.seqLength = seqLength;
        
        featureCanvas.setSeqLength(seqLength);
        
        //this.repaint();
    }
    public int getSeqLength(){
        return seqLength;
    }
    
    /** highlite a region */
    public void highlite( int start , int end){
        featureCanvas.highlite(start,end);
    }
    

    /** highlite a region */
    public void highlite( int pos ){
        featureCanvas.highlite(pos);
    }
    
    public void setLabel(String label){
        this.label=label;
        labelField.setLabel(label);
    }
    
    private void updateDisplay(){
        
        labelField.repaint();
        typeLabelPanel.repaint();
        featureCanvas.repaint();
        this.revalidate();
    }
    
    public void setFeatures(Feature[] features){
        // do something with the features.
        this.features = features;
        //System.out.println("setting " + features.length + " features");
        featureCanvas.setFeatures(features);
        typeLabelPanel.setFeatures(features);
        labelField.setFeatures(features);
        //int x = labelField.getWidth();
        //int y = typeLabelPanel.getHeight();
        //System.out.println("setting height " + x + " " + y);
        //labelField.setPreferredSize(new Dimension(x,y));
        
        int height = getHeight();
        setCanvasHeight(height);
        setLoading(false);
        updateDisplay();
       
    }
    
    public void setSelected(boolean flag){
        //.println("setting selected " + flag );
        selected = flag;
        typeLabelPanel.setSelected(flag);
        featureCanvas.setSelected(flag);
        labelField.setSelected(flag);
        updateDisplay();
        
    }
    public boolean getSelected(){
        return selected;
    }
    
    public Feature getFeatureAt(int line) throws NoSuchElementException{
        if ( line < 0)
            	throw new NoSuchElementException(" no line " + line + " found");
        if ( features == null)
            throw new NoSuchElementException(" no features in FeatureView " + label);
        if ( line < features.length ) {
            return features[line];
        }
        throw new NoSuchElementException(" no line " + line + " found");
        
    }
    
    public Segment getSegmentAt(int line, int seqpos) throws NoSuchElementException {
        Feature feat = getFeatureAt(line);
        
        List segments = feat.getSegments();
        Iterator iter =segments.iterator();
        while ( iter.hasNext()){
            Segment seg = (Segment) iter.next();
            int start = seg.getStart();
            int end = seg.getEnd();
            if (( start <= seqpos) && ( end >= seqpos)) {
                return seg;
            }
        }
        throw new NoSuchElementException(" no Segment found at  " + line + "seq.position" + seqpos);
    }
}
