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
 * Created on Nov 17, 2005
 *
 */
package org.biojava.spice.manypanel.renderer;


import java.awt.Dimension;
import java.awt.event.AdjustmentListener;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.biojava.bio.structure.*;
import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.eventmodel.*;
import org.biojava.spice.feature.Feature;
import org.biojava.spice.feature.FeatureImpl;
import org.biojava.spice.feature.Segment;
import org.biojava.spice.manypanel.eventmodel.DasSourceEvent;
import org.biojava.spice.manypanel.eventmodel.DasSourceListener;
import org.biojava.spice.manypanel.eventmodel.FeatureListener;
import org.biojava.spice.manypanel.eventmodel.ScaleEvent;
import org.biojava.spice.manypanel.eventmodel.ScaleListener;
import org.biojava.spice.manypanel.BrowserPane;
import org.biojava.spice.manypanel.drawable.DrawableDasSource;
import org.biojava.spice.manypanel.drawable.DrawableSequence;


import java.util.*;

public abstract class AbstractChainRenderer
    extends JPanel
    implements
    ObjectRenderer,
    DasSourceListener,
    FeatureListener
    
    {
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    // TODO: movie these to resource file
    public static final int MAX_SCALE               = 10;
    public static final int STATUS_PANEL_HEIGHT     = 20;
    public static final int FEATURE_PANEL_HEIGHT    = 20;
    
    
    DrawableSequence sequence; // the sequence to be drawn..
    

    // different panels that are used for the visualisation
   
    CursorPanel             cursorPanel;
    CursorPanel             columnCursor;
    StatusPanel             statusPanel;
    JLayeredPane            layeredPane;
    JScrollPane             scrollPane;
    DasScrollPaneRowHeader  dasScrollPaneRowHeader;    
    DasScrollPaneColumnHeader columnHeader;
    SequenceScalePanel      featurePanel;
    
    List                    dasSourcePanels;
    List                    scaleChangeListeners;
    
    AdjustmentListener      adjustmentListener;
    SeqToolTipListener      toolTipper;    
    ChainRendererMouseListener mouseListener;
    
    int componentWidth;
    int zoomFactor;
    
    public AbstractChainRenderer() {
        super();        
        this.setOpaque(true);
        setDoubleBuffered(true);

        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder());
        
        //clearFeatureRenderers();
        layeredPane = new JLayeredPane();
        layeredPane.setBorder(BorderFactory.createEmptyBorder());
        layeredPane.setDoubleBuffered(true);
        layeredPane.setOpaque(true);
        layeredPane.setBackground(SequenceScalePanel.BACKGROUND_COLOR);
        
        scrollPane = new JScrollPane(layeredPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(true);
        scrollPane.getVerticalScrollBar().setUnitIncrement(SequenceScalePanel.DEFAULT_Y_STEP);
        
        dasScrollPaneRowHeader = new DasScrollPaneRowHeader(scrollPane);
        scrollPane.setRowHeaderView(dasScrollPaneRowHeader);
       
       //dasScrollPaneColumnHeader = new DasScrollPaneColumnHeader();
     
        
        
        dasSourcePanels      = new ArrayList();
        scaleChangeListeners = new ArrayList();
        
        
        mouseListener = new ChainRendererMouseListener(this);
        toolTipper = new SeqToolTipListener(layeredPane);
        mouseListener.addSequenceListener(toolTipper);
        
        mouseListener.addSpiceFeatureListener(toolTipper);
        componentWidth = BrowserPane.DEFAULT_PANE_WIDTH;
        
        statusPanel = new StatusPanel();
        statusPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,20));
        
        statusPanel.setBorder(BorderFactory.createEmptyBorder());

        this.add(statusPanel);
        this.add(scrollPane);
        
        columnCursor = new CursorPanel();
    }
    
    
    /** add an adjustmentListener to the horizontal scrollbar
     * 
     * @param li
     */
    public void addAdjustmentListener(AdjustmentListener li){
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(li);
    }

    protected void initPanels(){
        
        int width = getDisplayWidth();
                
        columnHeader = new DasScrollPaneColumnHeader(featurePanel, columnCursor);
        columnHeader.setPreferredSize(new Dimension(width,SequenceScalePanel.SIZE));
        //scrollPane.setColumnHeaderView(columnHeader);
        scrollPane.setColumnHeaderView(featurePanel);
        
        layeredPane.addMouseMotionListener(mouseListener);
        layeredPane.addMouseListener(mouseListener);
        
        SegmentPopupFrame fframe = new SegmentPopupFrame();
        layeredPane.addMouseListener(fframe);
        layeredPane.addMouseMotionListener(fframe);
        
        //cursorPanel.addMouseListener(mouseListener);

        mouseListener.addSequenceListener(cursorPanel);
        mouseListener.addSpiceFeatureListener(cursorPanel);
        mouseListener.addSpiceFeatureListener(fframe);
        mouseListener.addSequenceListener(fframe);
        mouseListener.addSequenceListener(columnCursor);
        mouseListener.addSpiceFeatureListener(columnCursor);
        
       
        
        //statusPanel.setLocation(0,0);
        //statusPanel.setBounds(0,0,width,STATUS_PANEL_HEIGHT);
        //statusPanel.setPreferredSize(new Dimension(width,20));
        
        //int y = statusPanel.getHeight();
        int y = 0;

        //logger.info("statusp peanel h " + y);
        ///featurePanel.setBounds(0,y,width,20);
        ///featurePanel.setLocation(0,y);
	
        //cursorPanel.setPreferredSize(new Dimension(600,600));
        cursorPanel.setLocation(0,y);
        //cursorPanel.setOpaque(true);
        cursorPanel.setBounds(0,y,width,getDisplayHeight());
        //layeredPane.add(statusPanel,new Integer(99));
        //layeredPane.add(featurePanel,new Integer(0));
        layeredPane.add(cursorPanel, new Integer(100));
        layeredPane.moveToFront(cursorPanel);
        //scale=1.0f;
        //featurePanel.addMouseMotionListener(mouseListener);
       
        updatePanelPositions();
    }
  
    public ArrowPanel getArrowPanel(){
        return statusPanel.getArrowPanel();
    }
    
    public void setComponentWidth(int width){
        componentWidth = width;
        //logger.info("componentWidth" + width);
        calcScale(zoomFactor);
        this.revalidate();
        this.repaint();
        //this.updateUI();
    }
    
    
    public void clearDisplay(){
               
        Iterator iter = dasSourcePanels.iterator();
        while (iter.hasNext()){
            DasSourcePanel panel = (DasSourcePanel)iter.next();
            DrawableDasSource ds = panel.getDrawableDasSource();
            
            ds.clearDisplay();
        }
        getStatusPanel().setLoading(false);
       setScale(1.0f);
       cursorPanel.clearSelection();
       columnCursor.clearSelection();
       
        
    }
    
    /** removes the DasSourcePanels from display 
     * 
     *
     */
    public void clearDasSources(){
        //logger.finest("AbstractChainrenderer clearDasSources");
        Iterator iter = dasSourcePanels.iterator();
        while (iter.hasNext()){
            DasSourcePanel panel = (DasSourcePanel)iter.next();
            layeredPane.remove(panel);
        }
        dasSourcePanels.clear();
    }
    
    public JScrollPane getScrollPane(){
        return scrollPane;
    }
    
    public List getDasSourcePanels(){
        return dasSourcePanels;
    }
    
    public SequenceScalePanel getFeaturePanel(){
        return featurePanel;
    }
    
    public StatusPanel getStatusPanel(){
        return statusPanel;
    }
    
    public SeqToolTipListener getToolTipListener(){
        return toolTipper;
    }
    
    public CursorPanel getCursorPanel(){
        return cursorPanel;
    }
    
    public ChainRendererMouseListener getChainRendererMouseListener(){
        return mouseListener;
    }
    
    public void addSequenceListener(SequenceListener li){
        mouseListener.addSequenceListener(li);
    }
   
    public void addScaleChangeListener(ScaleListener li){
        scaleChangeListeners.add(li);
    }
  
    /** calculate the float that is used for display.
     * 1 * scale = size of 1 amino acid (in pixel).
     * maximum @see MAX_SCALE
     * @param zoomFactor
     * @return a float that is the display "scale" - an internal value required for paintin.
     * user should only interact with the zoomfactor ...
     */
    private float getScaleForZoom(int zoomFactor){
        if ( zoomFactor > 100)
            zoomFactor = 100;
        if ( zoomFactor < 1)
            zoomFactor = 1;
        
        this.zoomFactor = zoomFactor;
        int DEFAULT_X_START = SequenceScalePanel.DEFAULT_X_START;
        int DEFAULT_X_RIGHT_BORDER = SequenceScalePanel.DEFAULT_X_RIGHT_BORDER;
        
        int seqLength = getSequenceLength();
        // the maximum width depends on the size of the parent Component
        int defaultWidth = componentWidth;
        int width=defaultWidth;
        //int width = l  + FeaturePanel.DEFAULT_X_START + FeaturePanel.DEFAULT_X_RIGHT_BORDER;
        //int seqLength = sequence.getSequence().getLength();
        
        float s = width / (float) ( seqLength + DEFAULT_X_START + DEFAULT_X_RIGHT_BORDER );
        //logger.info("scale for 100% " + s + " " + seqLength);
        s = 100 * s /(zoomFactor) ;
        if ( s > MAX_SCALE)
            s = MAX_SCALE;
        //if (s < MIN_SCALE)
          //  s = MIN_SCALE;
        //logger.info("but changed to " + s);
        return s;
    }
    
    /** a value of 100 means that the whole sequence should be displayed in the current visible window
     * a factor of 1 means that one amino acid shoud be drawn as big as possible   
     *  
     * @param zoomFactor - a value between 1 and 100
     *
     *  
     */
    public void calcScale(int zoomFactor){
        
        float s = getScaleForZoom(zoomFactor);
        
        //logger.info("calc scale zoom:"+zoomFactor+ " s: " + s);
        setScale(s);
        //return scale;
        
    }
    


    protected float getScale(){
        return featurePanel.getScale();
    }

    protected void setScale(float scale) {
        
        featurePanel.setScale(scale);
        cursorPanel.setScale(scale);   
        columnCursor.setScale(scale);

        Iterator iter = dasSourcePanels.iterator();
        while (iter.hasNext()){
            DasSourcePanel dsp = (DasSourcePanel)iter.next();
            dsp.setScale(scale);
        }
        
        ScaleEvent event = new ScaleEvent(scale);
        Iterator iter2 = scaleChangeListeners.iterator();
        while (iter2.hasNext()){
            ScaleListener li = (ScaleListener) iter2.next();
            li.scaleChanged(event);
        }
        
        updatePanelPositions();        
    }


    private int getSequenceLength(){
        int l = 0 ;
        if ( sequence != null) {
        Chain c = sequence.getSequence();
        if ( c != null)
            l = c.getLength();
        }
        return l;
    }
    public int getDisplayWidth() {
        int l = getSequenceLength();
        
        float scale = featurePanel.getScale();
        
        int aminosize = Math.round(1*scale);
        if ( aminosize < 1)
            aminosize = 1;
        int w = Math.round(l*scale) + aminosize+  SequenceScalePanel.DEFAULT_X_START + SequenceScalePanel.DEFAULT_X_RIGHT_BORDER;
        
        if ( w  < 200){
            w = 200;
        }
        //logger.info("displayWidth " + w + " scale" +scale + " length"+ l);
        return w;
    }
    public int getDisplayHeight(){
        
        int totalH = STATUS_PANEL_HEIGHT; // 20 for statuspanel
        Iterator iter = dasSourcePanels.iterator();
        while (iter.hasNext()){
            DasSourcePanel dsp = (DasSourcePanel)iter.next();
            totalH+=dsp.getDisplayHeight();
       
        }
        if (totalH < 100)
            totalH = 100;
        return totalH;
    }
    
    
    
    
    private boolean isKnownDrawableDasSource(DrawableDasSource drawds){
        
        SpiceDasSource ds = drawds.getDasSource();
        
        Iterator iter = dasSourcePanels.iterator();
        while (iter.hasNext()) {
            DasSourcePanel panel = (DasSourcePanel) iter.next();
            DrawableDasSource dds = panel.getDrawableDasSource();
            //System.out.println("comparing " + ds.getNickname() + " " + dds.getDasSource().getNickname());
            if ( ds.getUrl().equals(dds.getDasSource().getUrl())) {
                logger.finest("AbstractChainRenderer known das source!" + ds.getUrl());
                drawds.addFeatureListener(panel);
                drawds.addFeatureListener(this);
                drawds.setFeatures(dds.getFeatures());
                panel.setDrawableDasSource(drawds);
                
                return true;
            }
        }
        logger.finest("do not know ds  " + ds.getNickname() );
        return false;
    }
    
    
    public void removeDasSource(DasSourceEvent event) {
        
        SpiceDasSource remove = event.getDasSource().getDasSource();
        logger.finest("AbstractChainrenderer removing DAS source " + remove.getNickname());
        Iterator iter = dasSourcePanels.iterator();
        DasSourcePanel removeMe = null;
        while (iter.hasNext()){
            DasSourcePanel pan = (DasSourcePanel)iter.next();
            DrawableDasSource test = pan.getDrawableDasSource();
            logger.finest("AbstractChainRenderer comp " + test.getDasSource().getNickname() + " " + remove.getNickname());
            if ( test.getDasSource().getUrl().equals(remove.getUrl())){
                removeMe = pan;  
                break;
            }
        }
        if ( removeMe != null){
            logger.finest("removing panel " + dasSourcePanels.contains(removeMe));
             
            dasSourcePanels.remove(removeMe);
            layeredPane.remove(removeMe);
            updatePanelPositions();
        }
    }
    
    
    
        
    
    
    /** add DasSourcePanels
     * 
     */

    public void newDasSource(DasSourceEvent event) {
        // the column header drawer ...
        dasScrollPaneRowHeader.newDasSource(event);
        
        DrawableDasSource dds =event.getDasSource();
        //SpiceDasSource ds = dds.getDasSource();
        
        // check if we know this already
        boolean known = isKnownDrawableDasSource(dds);
        
        if (known) {
                    
            return;
        }
        //logger.finest("AbstractChainRenderer new DAS source " + event.getDasSource().getDasSource().getNickname());
        
        DasSourcePanel dspanel = new DasSourcePanel(dds);
        
        
        //dspanel.setLayout(new BoxLayout(dspanel,BoxLayout.Y_AXIS));
        
        // join the listeners
        dds.addFeatureListener(dspanel);
        dds.addFeatureListener(this);        
        
        mouseListener.addSpiceFeatureListener(dspanel);
        

        //dspanel.setPreferredSize(new Dimension(200,200));
        int h = getDisplayHeight();
        int width = getDisplayWidth();
        
      
        int panelPos = dasSourcePanels.size();
        //logger.info("AbstractChainRenderer got new das source #" +
        //        dasSourcePanels.size() + " " + ds.getUrl() + " h " + h);
        dspanel.setLocation(0,0);
        int panelHeight = dspanel.getDisplayHeight();
        dspanel.setBounds(0,h,width,panelHeight);
        
        layeredPane.add(dspanel,new Integer(panelPos+1));  
        layeredPane.moveToFront(cursorPanel);
        
        dasSourcePanels.add(dspanel);
        
        Dimension d = new Dimension(width,h+panelHeight);
               
        
        //this.setPreferredSize(d);
        
        this.setSize(d);
        dspanel.repaint();
        layeredPane.repaint();
        this.repaint();
        this.revalidate();
    

    }
    
    public void updatePanelPositions(){
        //int h = featurePanel.getHeight() + 20;
        int h = FEATURE_PANEL_HEIGHT;
        int width = getDisplayWidth();
        
        //int maxsize =  Math.round(featurePanel.getScale()*sequence.getSequence().getLength());
        
        //logger.info("update panel positions " + width + 
        //        " " + featurePanel.getScale() + " " +
        //        sequence.getSequence().getLength());
        
        //logger.info("status panel size " + statusPanel.getHeight() + " " + statusPanel.getWidth());
        
        // put statuspanel on top of visible area
        //Point p = scrollPane.getViewport().getViewPosition();
        //logger.info("" +p);
        Dimension viewSize = scrollPane.getViewport().getViewSize();
        
        //logger.info("viewSize w" + viewSize.getWidth() + " " + viewSize.getHeight());
        if (viewSize.getWidth() > width )
            viewSize = new Dimension(width,viewSize.height);
        //logger.info(viewSize+"");
        this.setPreferredSize(viewSize);
        this.setSize(viewSize);
        
        columnHeader.setPreferredSize(new Dimension(width,SequenceScalePanel.SIZE));
        
        //scrollPane.setPreferredSize(viewSize);
        
                
        //int vw = viewSize.width;
        statusPanel.setPreferredSize(new Dimension(viewSize.width,STATUS_PANEL_HEIGHT));                
	
        // x .. width
        // y .. height
        // (x1,y1,x2,y2)
        Iterator iter = dasSourcePanels.iterator();
        while (iter.hasNext()){
            DasSourcePanel dsp = (DasSourcePanel)iter.next();
            
            int panelHeight = dsp.getDisplayHeight();
            dsp.setBounds(0,h,width,panelHeight);
            Dimension d = new Dimension(width,panelHeight);
            dsp.setPreferredSize(d);
            dsp.setSize(d);
            
            h+= panelHeight;
            dsp.repaint();
            dsp.revalidate();
        }
        
        //logger.info("updatePanelPosition max: " + width + " "  + h);
        
        cursorPanel.setBounds(0,0,width,h);
        
        // why was here a width+20 ?
        Dimension totalD = new Dimension(width,h);
        
        columnHeader.repaint();
        
        layeredPane.setPreferredSize(totalD);
        layeredPane.setSize(totalD);
        layeredPane.repaint();        
        layeredPane.revalidate();
        
        scrollPane.repaint();
        scrollPane.revalidate();
        
        this.repaint();
        this.revalidate();
    }

    public void disableDasSource(DasSourceEvent ds) {
        
        
    }

    public void enableDasSource(DasSourceEvent ds) {
       
        
    }

    

    public void selectedDasSource(DasSourceEvent ds) {
       
        
    }

    /** when new features are  loaded the panel positions need to be recalculated.
     * this method is triggered by the DrawableDasSource, once it has got new features.
     */
    public void newFeatures(FeatureEvent e) {
        
        
        updatePanelPositions();
        
    }
    
    public void comeBackLater(FeatureEvent e){
        //TODO: do something here
    }
    
    
     
    
    
    private Feature mapPDBFeature2Seq(Feature f){
        Feature newF = new FeatureImpl();
        newF.setMethod(f.getMethod());
        newF.setName(f.getName());
        newF.setNote(f.getNote());
        newF.setScore(f.getScore());
        newF.setSource(f.getSource());
        newF.setType(f.getType());
        
        newF.setLink(f.getLink());
        List segments = f.getSegments();
        
        
        
        Iterator iter = segments.iterator();
        while (iter.hasNext()){
            Segment s = (Segment)iter.next();
            Segment newS =(Segment) s.clone();
            // and now re-label the positions!
            int startOld = s.getStart();
            int seqPos = getGroupPosByPDBPos(startOld+"");
            newS.setStart(seqPos);
            int endOld = s.getEnd();
            int seqePos = getGroupPosByPDBPos(endOld+"");
            newS.setEnd(seqePos);
            newF.addSegment(newS);
        }
        return newF;
        
        
    }
    
    private int getGroupPosByPDBPos(String pdbPos){
        Chain c = sequence.getSequence();
    
        List groups = c.getGroups();
        
        // now iterate over all groups in this chain.
        // in order to find the amino acid that has this pdbRenum.               
        
        Iterator giter = groups.iterator();
        int i = 0;
        while (giter.hasNext()){
            i++;
        
            Group g = (Group) giter.next();
            String rnum = g.getPDBCode();
            if ( rnum.equals(pdbPos)) {
                //System.out.println(i + " = " + rnum);
                return i;
            }
        }    
        //logger.warning("could not map pdb pos " + pdbPos + " to sequence!");
        return -1;
    }
    
    
    private void mapPDBPos2Seq(DrawableDasSource ds){
        
        Feature[] feats = ds.getFeatures();
        List newFeats = new ArrayList();
        for (int i=0 ; i < feats.length; i++){
            Feature f = feats[i];
            Feature newF = mapPDBFeature2Seq(f);
            newFeats.add(newF);
        }
        
        Feature[] nfeats = (Feature[])newFeats.toArray(new Feature[newFeats.size()]);
        ds.setFeatures(nfeats);
        
    }
    
    /** set that loading has been finised
     * and re-lable the coordinates for protein structure codes...
     */
    public void loadingFinished(DasSourceEvent ds) {
        //logger.info("loading finished " + ds);
        Iterator iter = dasSourcePanels.iterator();
        DrawableDasSource eventSource = ds.getDasSource();
        while (iter.hasNext()){
            DasSourcePanel dsp = (DasSourcePanel)iter.next();
            DrawableDasSource thisSource = dsp.getDrawableDasSource();
            
            //todo: add the coordinate system of each implementation and
            // teplace this...
           
            
            if ( eventSource.equals(thisSource)) {
                if ( this instanceof StructureRenderer ) {
                    //logger.info("mapping pdb to seq!");
                    mapPDBPos2Seq(thisSource);
                    
                }
                dsp.setLoading(false);
            }
        }
        dasScrollPaneRowHeader.loadingFinished(ds);
    }

    public void loadingStarted(DasSourceEvent ds) {
        
        Iterator iter = dasSourcePanels.iterator();
        DrawableDasSource eventSource = ds.getDasSource();
        //logger.info("loading started "+ eventSource.toString());
        while (iter.hasNext()){
            DasSourcePanel dsp = (DasSourcePanel)iter.next();
            DrawableDasSource thisSource = dsp.getDrawableDasSource();
            if ( eventSource.equals(thisSource))
                dsp.setLoading(true);
        }
        
    }
    
}

