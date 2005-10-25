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
import org.biojava.spice.Config.SpiceDasSource;
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
 {
    
    public static final int    DEFAULT_X_START        = 20  ;
    public static final int    DEFAULT_X_RIGHT_BORDER = 20 ;
    public static final int    DEFAULT_Y_START        = 0 ;
    public static final int    DEFAULT_Y_STEP         = 10 ;
    public static final int    DEFAULT_Y_HEIGHT       = 4 ;
    public static final int    DEFAULT_Y_BOTTOM       = 16 ;
    
    // the line where to draw the structure
    public static final int    DEFAULT_STRUCTURE_Y    = 20 ;
    public static final int    MINIMUM_HEIGHT         = 20;
    
    //Logger logger        ;
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    
    int labelWidth;
    String label;
    Feature[] features;
    
    JSplitPane splitPane;
    LabelPane labelField;
    JSplitPane typeSplitPane;
    FeaturePanel featurePanel;
    TypeLabelPanel typeLabelPanel;
    SpiceFeatureViewer parent;
    int seqLength ;
       boolean selected;
    boolean isLoading;
    

    SpiceDasSource dasSource;
  //  Chain chain;
    
    /**
     * 
     */
    public FeatureView() {
        super();
        
        this.label="";
        //chain = new ChainImpl();
        features   = null;
        seqLength  = 0;
        labelWidth = 60;
        dasSource  = new SpiceDasSource();
        labelField = new LabelPane(this);
        labelField.setLabel(label);
        //labelField.setBorder(BorderFactory.createEmptyBorder());
        
        featurePanel = new FeaturePanel(this);
        //featurePanel.setBorder(BorderFactory.createEmptyBorder());
        
        typeLabelPanel = new TypeLabelPanel(this);
        typeLabelPanel.addSelectedFeatureListener(featurePanel);
        //typeLabelPanel.setBorder(BorderFactory.createEmptyBorder());
       
        
       isLoading = false;
    }
    
    public void setSpiceFeatureViewer(SpiceFeatureViewer viewer){
        this.parent = viewer;
    }
    public SpiceFeatureViewer getSpiceFeatureViewer() {
        return parent ; 
    }
    
    //public void setChain(Chain c){
        //chain = c;
      //  featurePanel.setChain(c);
    //}
    
    public void setDasSource(SpiceDasSource ds){ dasSource = ds; }
    public SpiceDasSource getDasSource(){ return dasSource; }
    
    public FeaturePanel getFeaturePanel() {
        return featurePanel;
    }
    public LabelPane getLabel() {
        return labelField;
    }
    public TypeLabelPanel getTypePanel(){
        return typeLabelPanel;
    }
    
    public void setScale(float scale) {
        featurePanel.setScale(scale);
        
    }
    public float getScale(){
        return featurePanel.getScale();
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
    
    /*
    public void setCanvasHeight(int height) {
     featurePanel.setCanvasHeight(height);
     typeLabelPanel.setCanvasHeight(height);
     labelField.setCanvasHeight(height);
     
    }*/
    
    /**  display a nice JSsscroller to display that data is being loaded ... */
    	public void setLoading(boolean flag){
    	    isLoading = flag;
    	    featurePanel.setLoading(flag);
    	    
    	}
    
  /** set the length of the displayed sequence
   * 
   * @param seqLength
   */  
    public void setSeqLength(int seqLength){
        this.seqLength = seqLength;
        
        featurePanel.setSeqLength(seqLength);
        
        //this.repaint();
    }
    public int getSeqLength(){
        return seqLength;
    }
    
    /** highlite a region */
    public void highlite( int start , int end){
        featurePanel.highlite(start,end);
    }
    

    /** highlite a region */
    public void highlite( int pos ){
        featurePanel.highlite(pos);
    }
    
    public void setLabel(String label){
        this.label=label;
        labelField.setLabel(label);
    }
    
    private void updateDisplay(){
        
	if ( parent != null) {
	    parent.updateDisplay();
	    //parent.revalidate();
	    //parent.repaint();
	}

        //labelField.repaint();
        //typeLabelPanel.repaint();
        //featurePanel.repaint();
        //this.revalidate();
    }
    
    
    public Feature[] getFeatures() {
        return features;
    }
    
    public void setFeatures(Feature[] features){
        // do something with the features.
        this.features = features;
     
        featurePanel.setFeatures(features);
        typeLabelPanel.setFeatures(features);
        labelField.setFeatures(features);
     
        
        //int height = getHeight();
     
        setLoading(false);
	
	JPanel featurePanel = parent.getFeaturePanel();
	JPanel typePanel = parent.getTypePanel();
	JPanel labelPanel = parent.getLabelPanel();


	featurePanel.revalidate();
	typePanel.revalidate();
	labelPanel.revalidate();
	
        updateDisplay();
       
    }
    
    public void setSelected(boolean flag){
        //System.out.println("setting selected " + flag );
        selected = flag;
        typeLabelPanel.setSelected(flag);
        featurePanel.setSelected(flag);
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
            int start = seg.getStart()-1;
            int end = seg.getEnd()-1;
            if (( start <= seqpos) && ( end >= seqpos)) {
                return seg;
            }
        }
        throw new NoSuchElementException(" no Segment found at  " + line + "seq.position" + seqpos);
    }
}
