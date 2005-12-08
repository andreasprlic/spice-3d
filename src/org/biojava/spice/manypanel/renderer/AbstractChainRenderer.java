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

import java.awt.Color;
import java.awt.Dimension;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.biojava.bio.structure.*;
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.FeatureImpl;
import org.biojava.spice.manypanel.eventmodel.FeatureEvent;
import org.biojava.spice.manypanel.BrowserPane;
import org.biojava.spice.manypanel.drawable.DrawableDasSource;
import org.biojava.spice.manypanel.drawable.DrawableSequence;
import org.biojava.spice.manypanel.eventmodel.*;


import java.util.*;

public abstract class AbstractChainRenderer
    extends JPanel
    implements
    ObjectRenderer,
    DasSourceListener,
    FeatureListener
    
    {
    
    public static final int    MAX_SCALE        =  10;
    public static final int STATUS_PANEL_HEIGHT =  20;
    public static final int FEATURE_PANEL_HEIGHT = 20;
    FeaturePanel featurePanel;
    CursorPanel cursorPanel;
    DrawableSequence sequence;
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    List dasSourcePanels;
    SeqToolTipListener toolTipper;
    ChainRendererMouseListener mouseListener;
    
    int componentWidth;
    int zoomFactor;
    //List featureRenderers;
    StatusPanel statusPanel;
    JLayeredPane layeredPane;
    JScrollPane scrollPane;
    
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
        layeredPane.setBackground(Color.WHITE);
        
        scrollPane = new JScrollPane(layeredPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(true);
        scrollPane.getVerticalScrollBar().setUnitIncrement(FeaturePanel.DEFAULT_Y_STEP);
        
        
        dasSourcePanels = new ArrayList();
        mouseListener = new ChainRendererMouseListener(this);
        toolTipper = new SeqToolTipListener(layeredPane);
        mouseListener.addSequenceListener(toolTipper);
        
        mouseListener.addSpiceFeatureListener(toolTipper);
        componentWidth = BrowserPane.DEFAULT_PANE_WIDTH;
        
        statusPanel = new StatusPanel();
        statusPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,20));
        
        statusPanel.setBorder(BorderFactory.createEmptyBorder());
        // the statusPanel is in the header of the scrollbar
        
        //scrollPane.setColumnHeaderView(statusPanel);
        //JPanel lowerPane = new JPanel();
        //lowerPane.add(scrollPane);
        
        //Box vBox = Box.createVerticalBox();
        //vBox.add(statusPanel);
        //vBox.add(lowerPane);
        //vBox.add(scrollPane);
        //this.add(vBox);
        this.add(statusPanel);
        this.add(scrollPane);
    }

    protected void initPanels(){
        
        
        layeredPane.addMouseMotionListener(mouseListener);
        layeredPane.addMouseListener(mouseListener);
        
        
        //cursorPanel.addMouseListener(mouseListener);
        
        mouseListener.addSequenceListener(cursorPanel);
        mouseListener.addSpiceFeatureListener(cursorPanel);
        
        int width = getDisplayWidth();
        
        //statusPanel.setLocation(0,0);
        //statusPanel.setBounds(0,0,width,STATUS_PANEL_HEIGHT);
        //statusPanel.setPreferredSize(new Dimension(width,20));
        
        //int y = statusPanel.getHeight();
        int y = 0;

        //logger.info("statusp peanel h " + y);
        featurePanel.setBounds(0,y,width,20);
        featurePanel.setLocation(0,y);
	
        //cursorPanel.setPreferredSize(new Dimension(600,600));
        cursorPanel.setLocation(0,y);
        //cursorPanel.setOpaque(true);
        cursorPanel.setBounds(0,y,width,getDisplayHeight());
        //layeredPane.add(statusPanel,new Integer(99));
        layeredPane.add(featurePanel,new Integer(0));
        layeredPane.add(cursorPanel, new Integer(100));
        layeredPane.moveToFront(cursorPanel);
        //scale=1.0f;
        //featurePanel.addMouseMotionListener(mouseListener);
       
        updatePanelPositions();
    }
  
    public void setComponentWidth(int width){
        componentWidth = width;
        //logger.info("componentWidth" + width);
        calcScale(zoomFactor);
        this.revalidate();
        //this.repaint();
        //this.updateUI();
    }
    
    
    public void clearDisplay(){
               
        Iterator iter = dasSourcePanels.iterator();
        while (iter.hasNext()){
            DasSourcePanel panel = (DasSourcePanel)iter.next();
            DrawableDasSource ds = panel.getDrawableDasSource();
            
            ds.clearDisplay();
        }
        
    }
    
    public void clearDasSources(){
        Iterator iter = dasSourcePanels.iterator();
        while (iter.hasNext()){
            DasSourcePanel panel = (DasSourcePanel)iter.next();
            layeredPane.remove(panel);
        }
        dasSourcePanels.clear();
    }
    
    public List getDasSourcePanels(){
        return dasSourcePanels;
    }
    
    public FeaturePanel getFeaturePanel(){
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
        int DEFAULT_X_START = FeaturePanel.DEFAULT_X_START;
        int DEFAULT_X_RIGHT_BORDER = FeaturePanel.DEFAULT_X_RIGHT_BORDER;
        
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
        
        //this.scale=scale;
        featurePanel.setScale(scale);
        cursorPanel.setScale(scale);
        
        
        Iterator iter = dasSourcePanels.iterator();
        while (iter.hasNext()){
            DasSourcePanel dsp = (DasSourcePanel)iter.next();
            dsp.setScale(scale);
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
        int w = l*aminosize + FeaturePanel.DEFAULT_X_START + FeaturePanel.DEFAULT_X_RIGHT_BORDER;
        
        if ( w  < 200){
            w = 200;
        }
        //logger.info("displayWidth " + w + " scale" +scale + " length"+ l);
        return w;
    }
    public int getDisplayHeight(){
        
        int totalH = featurePanel.getHeight() + STATUS_PANEL_HEIGHT; // 20 for statuspanel
        Iterator iter = dasSourcePanels.iterator();
        while (iter.hasNext()){
            DasSourcePanel dsp = (DasSourcePanel)iter.next();
            totalH+=dsp.getDisplayHeight();
       
        }
        if (totalH < 100)
            totalH = 100;
        return totalH;
    }
    
    
    
    
    /** add DasSourcePanels
     * 
     */

    public void newDasSource(DasSourceEvent event) {
       
        DrawableDasSource dds =event.getDasSource();
        //SpiceDasSource ds = dds.getDasSource();
        
        DasSourcePanel dspanel = new DasSourcePanel(dds);
        //dspanel.setLayout(new BoxLayout(dspanel,BoxLayout.Y_AXIS));
        
        // join the listeners
        dds.addFeatureListener(dspanel);
        dds.addFeatureListener(this);        
        
        mouseListener.addSpiceFeatureListener(dspanel);
        //dspanel.addMouseListener(mouseListener);
        //dspanel.addMouseMotionListener(mouseListener);
        //SeqToolTipListener toolTipper = new SeqToolTipListener(dspanel);
        //mouseListener.addSpiceFeatureListener(toolTipper);
        
        
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
        this.repaint();
        this.revalidate();
    

    }
    
    public void updatePanelPositions(){
        //int h = featurePanel.getHeight() + 20;
        int h = FEATURE_PANEL_HEIGHT;
        int width = getDisplayWidth();
        
        //logger.info("update panel positions " + width + " " + h + " " + sequence.getSequence().getLength());
        //logger.info("status panel size " + statusPanel.getHeight() + " " + statusPanel.getWidth());
        
        // put statuspanel on top of visible area
        //Point p = scrollPane.getViewport().getViewPosition();
        //logger.info("" +p);
        Dimension viewSize = scrollPane.getViewport().getViewSize();
        
        
        
        //logger.info("viewSize " + viewSize.getWidth() + " " + viewSize.getHeight());
        this.setPreferredSize(viewSize);
        this.setSize(viewSize);
        scrollPane.setPreferredSize(viewSize);
        //   scrollPane.setSize(viewSize);
        
        
        //int vw = viewSize.width;
        statusPanel.setPreferredSize(new Dimension(viewSize.width,STATUS_PANEL_HEIGHT));
        
        //statusPanel.setLocation(p.x,0);
        //statusPanel.setBounds(p.x,0,vw,STATUS_PANEL_HEIGHT);
        //statusPanel.setBounds(0,0,width,STATUS_PANEL_HEIGHT);
        featurePanel.setBounds(0,0,width,h);
	
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
        }
        
        //logger.info("updatePanelPosition max: " + width + " "  + h);
        
        cursorPanel.setBounds(0,0,width,h);
        Dimension totalD = new Dimension(width+20,h);
        
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

    public void newFeatures(FeatureEvent e) {
        updatePanelPositions();
        
    }

    public void loadingFinished(DasSourceEvent ds) {
        //logger.info("loading finished");
        Iterator iter = dasSourcePanels.iterator();
        DrawableDasSource eventSource = ds.getDasSource();
        while (iter.hasNext()){
            DasSourcePanel dsp = (DasSourcePanel)iter.next();
            DrawableDasSource thisSource = dsp.getDrawableDasSource();
            if ( eventSource.equals(thisSource))
                dsp.setLoading(false);
        }
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
