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

import javax.swing.*;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.*;
import java.awt.Dimension;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import org.biojava.spice.Feature.*;
import org.biojava.spice.Panel.seqfeat.FeaturePanel;
import org.biojava.spice.Panel.seqfeat.FeaturePanelMouseListener;
import org.biojava.spice.Panel.seqfeat.FeatureView;
import org.biojava.spice.Panel.seqfeat.LabelPanelListener;
import org.biojava.spice.Panel.seqfeat.LabelPane;
import org.biojava.spice.Panel.seqfeat.SelectedSeqPositionListener;
import org.biojava.spice.Panel.seqfeat.SeqScale;
import org.biojava.spice.Panel.seqfeat.TypeLabelPanel;
import org.biojava.spice.Panel.seqfeat.TypePanelMouseListener;
import org.biojava.spice.Panel.seqfeat.DasSourceListener;
import org.biojava.spice.Config.SpiceDasSource;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Chain;

/** A class that can display features (e.g. retrieved from different DAS sources).
 * A SpiceFeatureView contains zero, one or multiple FeatureView objects, which do the actual rendering.
 * 
 * @author Andreas Prlic
 *
 */
public class SpiceFeatureViewer 

extends SizeableJPanel
implements SelectedSeqPositionListener,
ChangeListener

{
    
    static int DEFAULT_X_START = 30;
    static int DEFAULT_X_END   = 30;
    static int MAX_SCALE       = 10;
    static int DEFAULT_VISIBLE_WIDTH  = 400;
    static int DEFAULT_VISIBLE_HEIGHT = 100;
    static int DEFAULT_SEQSCROLL_WIDTH = 200;
    static int DEFAULT_SEQSCROLL_HEIGHT = 30;
    static Color BACKGROUND_COLOR = Color.black;
    
    List featureViews;
    
    //int scale;
    Box vBox;
    
    SeqScale seqScale;
    int seqLength;
    
    JSplitPane labelSplit;
    JSplitPane typeSplit;
    //Box labelBox;
    //Box typeBox;
    //Box featureBox;
    JScrollPane featureScroll;
    JSlider residueSizeSlider ;
    
    int residueSize ;
    int preferredVisibleWidth;
    
    List featureViewListeners;
    List selectedSeqPositionListeners;
    List dasSourceListeners;
    
    int typePanelSize;
    int labelPanelSize;
    boolean selectionIsLocked ;
    JPopupMenu popupMenu;
    Chain chain;
    LabelPanelListener lbml;
    
    TypePanelContainer typePanel;
    LabelPanelContainer labelPanel;
    FeaturePanelContainer featurePanel;
    
    /**
     * 
     */
    public SpiceFeatureViewer() {
        super();
        
        dasSourceListeners = new ArrayList();
        // when we are shown, add a resize listener to the parent component to
        // also adapt our size since we are a SizeableJPanel
        //MyComponentShowListener mcsl = new MyComponentShowListener(this); 
        //this.addComponentListener(mcsl);
        selectionIsLocked = false;
        this.setBackground(Color.black);
        
        featureViews = new ArrayList();
        featureViewListeners = new ArrayList();
        selectedSeqPositionListeners = new ArrayList();
        
        //scale = 1;
        seqLength = 0;
        residueSize = 100;
        chain = new ChainImpl();
        // the vertical Box contains the scroller + the featureview panels ...
        vBox = Box.createVerticalBox();
        vBox.setBorder(BorderFactory.createEmptyBorder());
        vBox.setBackground(Color.black);
        
        // allows to change the scale of the sequence
        SeqScroller s1 = new SeqScroller();
        s1.setWidth(DEFAULT_SEQSCROLL_WIDTH);
        s1.setHeight(DEFAULT_SEQSCROLL_HEIGHT);
        
        int typePanelSize = 60;
        int labelPanelSize =  60;
      
        Box hBox1 = Box.createHorizontalBox();
        setBackground(Color.black);
        hBox1.add(s1);
        hBox1.add(Box.createHorizontalGlue());
        vBox.add(hBox1);
        s1.addChangeListener(this);
        
        
        // three boxes to contain the 3 panels as provided by each FeatureView object
        
        //labelBox   = Box.createVerticalBox();
        //labelBox.setBorder(BorderFactory.createEmptyBorder());
        
        //typeBox    = Box.createVerticalBox();
        //typeBox.setBorder(BorderFactory.createEmptyBorder());
        
        //featureBox = Box.createVerticalBox();
        //featureBox.setBorder(BorderFactory.createEmptyBorder());
        //featureBox.setBackground(Color.black);
        
        featurePanel = new FeaturePanelContainer(this);
        typePanel    = new TypePanelContainer(this);
        labelPanel   = new LabelPanelContainer(this);
        
        labelPanel.setPreferredSize(new Dimension(60,labelPanel.getHeight()));
        typePanel.setPreferredSize(new Dimension(60,typePanel.getHeight()));
        
        featurePanel.setBackground(BACKGROUND_COLOR);
        typePanel.setBackground(   BACKGROUND_COLOR);
        labelPanel.setBackground(  BACKGROUND_COLOR);
        
        
        featureScroll = new JScrollPane(featurePanel);
        featureScroll.setBorder(BorderFactory.createEmptyBorder());
        featureScroll.setBackground(Color.black);
        
        // never allow vertical scrolling, otherwise we would need to take care
        // of LabelPane and FeaturePane ...
        featureScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        
        // VERRRRRRRY important to set the size of the scrollable region!!!
        // this took me ages to find out ... :-/
        setFeaturePaneVisibleSize(DEFAULT_VISIBLE_WIDTH,DEFAULT_VISIBLE_HEIGHT);
        
        // there is a sequence ruler on top 
        seqScale = new SeqScale();
        seqScale.setFeatures(new FeatureImpl[0]);

        labelPanel.setSeqScale(seqScale);
        featurePanel.setSeqScale(seqScale);
        typePanel.setSeqScale(seqScale);
        //initLabelPane();
        
        FeaturePanelMouseListener fpml = new FeaturePanelMouseListener(this);
        //fpml.addSelectedSeqListener(this);
        //SeqScaleCanvas ssc = seqScale.getSeqScaleCanvas();
        featurePanel.addMouseListener(fpml);
        featurePanel.addMouseMotionListener(fpml);
        featurePanel.setFeaturePanelMouseListener(fpml);
        
        
        
        lbml = new LabelPanelListener(this);
        popupMenu = createPopupMenu();
        lbml.setPopupMenu(popupMenu);
        labelPanel.addMouseListener(lbml);
        labelPanel.addMouseMotionListener(lbml);
        
        //labelBox.add(popupMenu);

        TypePanelMouseListener tpml = new TypePanelMouseListener(this);
        typePanel.addMouseListener(tpml);
        typePanel.addMouseMotionListener(tpml);
        
       
        
        typeSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,typePanel,featureScroll);
        typeSplit.setBorder(BorderFactory.createEmptyBorder());
      
        labelSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,labelPanel,typeSplit);
        labelSplit.setBorder(BorderFactory.createEmptyBorder());
        //labelSplit.setBackground(Color.black);
        
        Box hBox2 = Box.createHorizontalBox();
        hBox2.setBackground(Color.black);
        hBox2.add(labelSplit);
        vBox.add(hBox2);
        //vBox.add(labelSplit);
        //this.add(labelSplit);
        this.add(vBox);
        
        // listening to events ( i.e. color selection );
        //this.addFeatureViewListener(this);
        //this.addSelectedSeqPositionListener(this);
        //vBox.add(seqScale);
        
        //float scale = calcScale(100);
        //setScale(scale);
        // TODO: remove this:
        vBox.setBorder(BorderFactory.createLineBorder(Color.blue) );
    }
    
    /*
    public void paintComponent( Graphics g) {
        System.out.println("paintComponent");
        super.paintComponent(g);
        
        Graphics labelG   = labelPanel.getGraphics();
        Graphics typeG    = typePanel.getGraphics();
        Graphics featureG = featurePanel.getGraphics();
        
        int y = 0 ;
        
        //LabelPane label      = seqScale.getLabel();    
        TypeLabelPanel typL  = seqScale.getTypePanel();        
        SeqScaleCanvas sscan = seqScale.getSeqScaleCanvas();
        
        //label.setCanvasHeight(labelPanel.getHeight());
        //label.setCanvasWidth(labelPanel.getWidth());
        
        //label.paintComponent(labelG,y);
        typL.paintComponent(typeG,y);
        y = sscan.paintComponent(featureG,y);
         // paint the typePane
        
        // fore each featureview:
        Iterator iter = featureViews.iterator();
        while (iter.hasNext()){
            FeatureView fv = (FeatureView)iter.next();
            //LabelPane      lp = fv.getLabel(); 
            TypeLabelPanel tlp = fv.getTypePanel();
            FeaturePanel   fp = fv.getFeaturePanel();
            
            //lp.paintComponent(labelG,y);
            tlp.paintComponent(typeG,y);
            y = fp.paintComponent(featureG,y);
        }
        
        
        //labelPanel.repaint();
        //typePanel.repaint();
        //featurePanel.repaint();

    }
    */
    
    
    
    /** remove the existing FeatureView objects to free the space for displaying e.g. the 
     * Annotations of a new sequence.
     *
     */
    public void clear() {
        featureViews = new ArrayList();
        updateDisplay();
        featurePanel.clear();
        labelPanel.clear();
        typePanel.clear();
    }
    
    public void addDasSourceListener(DasSourceListener dsl){
        dasSourceListeners.add(dsl);
    }
    
    public DasSourceListener[] getDasSourceListeners() {
        return (DasSourceListener[]) dasSourceListeners.toArray(new DasSourceListener[dasSourceListeners.size()]);
    }
    
    public void triggerSelectedDasSource(SpiceDasSource ds){
        DasSourceListener[] dsls = getDasSourceListeners();
        for (int i = 0 ; i < dsls.length;i++){
            DasSourceListener dsl = dsls[i];
            dsl.selectedDasSource(ds);
            
        }
    }
    public void addSelectedSeqPositionListener(SelectedSeqPositionListener listener){
        selectedSeqPositionListeners.add(listener);
    }
    
    public void addFeatureViewListener(FeatureViewListener listener){
        featureViewListeners.add(listener);
    }
    
    public FeatureViewListener[] getFeatureViewListeners() {
        return (FeatureViewListener[]) featureViewListeners.toArray(new FeatureViewListener[featureViewListeners.size()]);
    }
    
    public SelectedSeqPositionListener[] getSelectedSeqPositionListeners() {
        return (SelectedSeqPositionListener[]) selectedSeqPositionListeners.toArray(new SelectedSeqPositionListener[selectedSeqPositionListeners.size()]);
    }
    
		
    
    /** sets the visible size of the feature panel 
     * If it is larger there will be a scrollpanel...
     * */
    public void setFeaturePaneVisibleSize(int x, int y){
        //System.out.println("setFeaturePaneVisibleSize " + x + " " + y);
        
        //      THIS IS SETTING THE SIZE!!!
        preferredVisibleWidth = x ;
        int height = featurePanel.getHeight();
        Dimension d = new Dimension(x,height);
        featurePanel.setFixedSize(d);
        
        float scale = calcScale(residueSize);
        setScale(scale);
        
        //this.validate();
        
        this.revalidate();
        this.repaint();
        
        
    }
    
    public void setScrollValue(int val){
        //seqScroller.setScrollValue(val);
        float scale = calcScale(val);
        setScale(scale);
    }
    /*
    public void initLabelPane(){
                
        // add the seq. position liner
        //LabelPane label      = seqScale.getLabel();    
        //TypeLabelPanel typL  = seqScale.getTypePanel();
        //SeqScaleCanvas sscan = seqScale.getSeqScaleCanvas();
        
        
        //labelBox.add(label);
        //typeBox.add(typL);
        //featureBox.add(sscan);
       // featureBox.add(sscan);
    }
    */
  
    private float calcScale(int zoomFactor){

        //SeqScaleCanvas sscan = seqScale.getSeqScaleCanvas();
        
        
        //int width = DEFAULT_VISIBLE_WIDTH ;
        //int width = this.getWidth()-20;
        int width = preferredVisibleWidth;
        
        float scale = width / (float) ( seqLength +DEFAULT_X_START + DEFAULT_X_END);
        //System.out.println("100% displayed =  " + scale + " residueSize: " + residueSize);
        scale = 90* scale /(zoomFactor) ;
        
        //float scale =  ( seqLength +DEFAULT_X_START + DEFAULT_X_END) / (residueSize);
        //float scale = 10 - residueSize ;
        //System.out.println("new scale: " + scale + "residueSize " + residueSize);
        float minscale = getMinimalScale();
        if ( scale < minscale) 
            	scale = minscale;
        if ( scale > MAX_SCALE)
            scale = MAX_SCALE;
        return scale;
        
    }
    public void stateChanged(ChangeEvent e) {
        
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
        
            residueSize = (int)source.getValue();
            float scale = calcScale(residueSize);
                
            this.setScale(scale);
        }
    }
    
    public void setChain(Chain chain){
        this.chain = chain;
        featurePanel.setChain(chain);
        
        /*
        if ( seqScale != null)
            seqScale.setChain(chain);
        Iterator iter = featureViews.iterator();
        while (iter.hasNext()){
            FeatureView fv = (FeatureView)iter.next();
            fv.setChain(chain);
        }
        */
        this.revalidate();
        this.repaint();
    }
    
    /** the minimal scale is so that the whole sequence is visible on the screen */
    private float getMinimalScale(){
        
        if ( seqScale == null)
            return MAX_SCALE;
        
        if ( seqLength < 1)
            return MAX_SCALE;
        
        //Dimension d = seqScale.getSeqScaleCanvas().getSize();
        
        int visibleWidth = featureScroll.getWidth()-DEFAULT_X_START - DEFAULT_X_END + 20; 
        //System.out.println("width " + visibleWidth + " " + seqLength);
        float size =visibleWidth / (float)seqLength;
        //System.out.println("size " + size);
        return size;
        
        /*
        int aminosize =  Math.round(1 * size) ;
        float minscale =  calcScale(aminosize);
        System.out.println("minimal scale: " + minscale + " aminosize "+ aminosize);
        return minscale ;
        */
    }
    
    public void setScale(float scale){
        float minscale = getMinimalScale();
        if ( scale < minscale ) {
            //System.out.println("Minscale > scale" + minscale + ">" + scale);
            scale = minscale;
        }
        if ( scale > MAX_SCALE){
            scale = MAX_SCALE;
        }
        
        
        if ( seqScale != null)
            seqScale.setScale(scale);
        
        if ( featureViews == null)
            	return;
        Iterator iter = featureViews.iterator();
        while (iter.hasNext()){
            FeatureView fv = (FeatureView)iter.next();
            fv.setScale(scale);
        }
        
        this.revalidate();
        this.repaint();
    }
    public void setSeqLength(int length){
        this.seqLength = length;
        
        featurePanel.setSeqLength(length);
        
        seqScale.setSeqLength(length);
        Iterator iter = featureViews.iterator();
        while (iter.hasNext()){
            FeatureView fv = (FeatureView)iter.next();
            fv.setSeqLength(length);
        }
        
        // a new sequence has been assigned, reset the scroll value;
        setScrollValue(100);
        float scale = calcScale(100);
        int thissize = Math.round(length * scale) + 
        DEFAULT_X_START + 20;
        Dimension nsize  = new Dimension(thissize,featurePanel.getHeight());
        //featurePanel.setPreferredSize(nsize);
        //featurePanel.setSize(nsize);
        //featurePanel.setMinimumSize(nsize);
        
        if ( thissize < preferredVisibleWidth ) {
            setFeaturePaneVisibleSize(thissize,DEFAULT_VISIBLE_HEIGHT) ;
        } else {
            setFeaturePaneVisibleSize(DEFAULT_VISIBLE_WIDTH, DEFAULT_VISIBLE_HEIGHT);
        }
        
        this.repaint();
        //residueSizeSlider.setValue(100);
    }
    
    public void addFeatureView(FeatureView fv){
        addFeatureView(fv,true);
    }
    
    public void evaluateLayout(){
              
        Dimension d = this.getSize();        
        System.out.println("evaluateLayout old size" + d.width + " " + d.height);
        
        int newHeight = getSubHeight();
        
        int otherStuff = 20 ;
        newHeight += otherStuff;
        //System.out.println("new height " + newHeight);
        
        this.setPreferredSize(new Dimension(d.width,newHeight));
        vBox.setPreferredSize(new Dimension(d.width,newHeight));
        /*int stuffWidth = getLabelWidth() + getTypeWidth();
        newWidth = newWidth - stuffWidth;
        if ( newWidth < 1 ) {
            newWidth = 1 ;
        }
        
        setHeight(newHeight);
        setFeaturePaneVisibleSize(newWidth, newHeight);
        
        //labelPanel.setCanvasHeight(newHeight);
        System.out.println("new size "+newsize);
        //labelPanel.setPreferredSize(newsize);
        featureScroll.setPreferredSize(newsize);
        featureScroll.setSize(new Dimension(newWidth,newHeight));
        //parent.setPreferredSize(new Dimension(d.width,d.height));
        //featureScroll.setPreferredSize(new Dimension(newHeight, newWidth));
        //featureScroll.revalidate();
         * 
         */
        this.revalidate();
        this.repaint();
    }
    
    public void addFeatureView(FeatureView view, boolean update){
       
        view.setSeqLength(seqLength);
        view.setScale(calcScale(residueSize));
        view.setSpiceFeatureViewer(this);
        //view.setChain(chain);
        
        LabelPane lab      = view.getLabel();
        TypeLabelPanel typ = view.getTypePanel();
        FeaturePanel sub   = view.getFeaturePanel();
        

    
        //LabelPaneMouseListener lpml = new LabelPaneMouseListener(this); 
        //lab.addMouseListener(lpml);
        //lab.addMouseMotionListener(lpml);
        //FeaturePanelMouseListener fpml = new FeaturePanelMouseListener(this);
        //fpml.addSelectedSeqListener(this);
        //fpml.addFeatureViewListener(this);
        
        //sub.setFeaturePanelMouseListener(fpml);
        
        featureViews.add(view);
        labelPanel.addFeatureView(view);
        featurePanel.addFeatureView(view);
        typePanel.addFeatureView(view);
        //labelBox.add(lab);
        //typeBox.add(typ);
        //featureBox.add(sub);
        
        //featureScroll.repaint();
        //vBox.add(view);
        //vBox.add(Box.createHorizontalGlue());
        //updateDisplay();
        evaluateLayout();   
        if ( update){
            
            this.repaint();       
        }
    }
    
    public JPopupMenu getPopupMenu(){
        return popupMenu;
    }
    private JPopupMenu createPopupMenu(){ 
    
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("show DAS-source details");
        ShowDasSourceListener sdsl = new ShowDasSourceListener(lbml);
        menuItem.addActionListener(sdsl);
        popupMenu.add(menuItem);
        
        return popupMenu;
    }

    public FeatureView[] getFeatureViews(){
        return (FeatureView[])featureViews.toArray(new FeatureView[featureViews.size()]);
    }
    
    public void selectedSeqPosition(int position) {
        // highlite in FeaturePanels
        
        // draw a line and repaint
        
        if ( selectionIsLocked) return;
        featurePanel.highlite(position);
        /*seqScale.highlite(position);
        Iterator iter = featureViews.iterator();
        while (iter.hasNext()){
            FeatureView fv = (FeatureView)iter.next();
            fv.highlite(position);
        }
        */
        
    }
    public void selectedSeqRange(int start, int end){
        if ( selectionIsLocked) return;
        featurePanel.highlite(start,end);
//      highlite in FeaturePanels
        /*
        
        seqScale.highlite(start,end);
        Iterator iter = featureViews.iterator();
        while (iter.hasNext()){
            FeatureView fv = (FeatureView)iter.next();
            fv.highlite(start,end);
        }
        */
    }
    
    public void selectionLocked(boolean flag){
        //System.out.println("SpiceFeatureView selection locked " + flag);
        selectionIsLocked = flag;
        
        //FeaturePanel fp = seqScale.getSeqScaleCanvas();
        FeaturePanelMouseListener fpml = featurePanel.getFeaturePanelMouseListener();
        fpml.selectionLocked(flag);
        /*
        Iterator iter = featureViews.iterator();
        
        while ( iter.hasNext()){
            FeatureView fv = (FeatureView)iter.next();
            FeaturePanel fpa = fv.getFeaturePanel();
            fpml = fpa.getFeaturePanelMouseListener();
            fpml.selectionLocked(flag);
        }
        */
    }
    
    /** detect the location of a feature view from a mouse event
     * 
     * @param e the mouse event
     * @return the feature view of this mouse event
     */
    public FeatureView getParentFeatureView( MouseEvent e){
        
        int mouse_y = e.getY();
        
        int y = 0;
        y += seqScale.getHeight();
        if ( y > mouse_y ) {
            return seqScale;
        //	return null;
        }
        
        //System.out.println(y);
        Iterator iter = featureViews.iterator();
        
        while ( iter.hasNext()){
            FeatureView fv = (FeatureView)iter.next();
            
            y+= fv.getHeight();
            if (y > mouse_y) {
                return fv;
            }
        }
        
        // out of range;
        return null;
    }
    
    
    public int getFeatureWidth(){
        return featurePanel.getWidth();
    }
    
    public int getTypeWidth() {
        
        return typePanel.getWidth();
    }
    public int getLabelWidth(){
        return labelPanel.getWidth();
    }
    
    
    /** returns the total height of all sub-panels added together*/
    public int getSubHeight() {
        int y = 0;
        
        if (seqScale != null) {
            //LabelPane label      = seqScale.getLabel();
            //y += label.getCanvasHeight();
            y+= seqScale.getHeight();
        }
        
        if ( featureViews != null ) {
            Iterator iter = featureViews.iterator();
            while ( iter.hasNext()){
                FeatureView fvtmp = (FeatureView)iter.next();
               y+= fvtmp.getHeight()+30;
            } 
        }
        //System.out.println("subheight: " + y);
        return y;
    }
    
    /** get the coordinates of the upper right corner of this FeatureView in the LabelBox */
    public Point getLocationOnLabelBox(FeatureView fv) {
        int y = 0;
        
       
        LabelPane label      = seqScale.getLabel();
       
        if ( seqScale.equals(fv) ) {
            return new Point (0,y);
            //return null;
        }
        y += seqScale.getHeight();
        
        Iterator iter = featureViews.iterator();
        while ( iter.hasNext()){
            FeatureView fvtmp = (FeatureView)iter.next();
            if (fv.equals(fvtmp)) {
                return new Point (0,y);
            }
            y+= fvtmp.getHeight();
        }
        
        // out of range;
        return null;
    }
    
    
    /** rebuild the display after the order of featureviews has been changed ...
     * use in combination with clear() */
    public void updateDisplay(){
        //labelBox.removeAll();
        //typeBox.removeAll();
        //featureBox.removeAll();
        //initLabelPane();
        List tmplist = featureViews;
        featureViews = new ArrayList();
        Iterator iter = tmplist.iterator();
        while (iter.hasNext()){
            FeatureView fv = (FeatureView)iter.next();
            addFeatureView(fv, false);
        }
        evaluateLayout();   
        this.repaint();
        //labelSplit.repaint();
        //Component c = this.getParent().getParent();
        //c.repaint();
        
    }
    public void repaint() {
        
        this.revalidate();
        super.repaint();
    }
    /** move the position of a feature view one down... */
    public void moveDown(FeatureView fv){
        
        int position = featureViews.indexOf(fv);
        if ( position >= featureViews.size()-1) return;
        featureViews.remove(position);
        featureViews.add(position+1,fv);
        
        List tmp = featureViews;
        clear();
        featureViews = tmp;
        
        updateDisplay();
        //System.out.println("moved Down to" + (position +1));
    }
    
    public void moveUp(FeatureView fv ){
        
        int position = featureViews.indexOf(fv);
        if ( position < 1) return;
        
        featureViews.remove(position);
        featureViews.add(position-1,fv);
        List tmp = featureViews;
        clear();
        featureViews = tmp;
        updateDisplay();
       // System.out.println("moved Up to" + (position -1));
    }
    

    
}



class ShowDasSourceListener implements ActionListener {
    
    //SpiceFeatureViewer featureView ;
    LabelPanelListener parent;
    
    public ShowDasSourceListener ( LabelPanelListener parent) {
        //this.featureView = featureView;
        this.parent = parent ;
        
    }
    
    public void actionPerformed(ActionEvent e){
        // show the details of a DAS source ...
        // open a new frame that does something
        System.out.println("display DAS data!");
        Object source = e.getSource();
        
        FeatureView fv = parent.getCurrentFeatureView();
        SpiceFeatureViewer viewer = fv.getSpiceFeatureViewer();
        //displayFeatureViewFrame(viewer,fv);
        
        System.out.println("trigger selectedDasSource");
        viewer.triggerSelectedDasSource(fv.getDasSource());
        
    }
}





