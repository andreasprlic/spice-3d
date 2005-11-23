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
import java.util.logging.Logger;

import javax.swing.JLayeredPane;
import org.biojava.bio.structure.*;
import org.biojava.spice.Config.SpiceDasSource;
import org.biojava.spice.manypanel.BrowserPane;
import org.biojava.spice.manypanel.drawable.DrawableDasSource;
import org.biojava.spice.manypanel.drawable.DrawableSequence;
import org.biojava.spice.manypanel.eventmodel.*;


import java.util.*;

public abstract class AbstractChainRenderer
    extends JLayeredPane
    implements
    ObjectRenderer,
    DasSourceListener,
    FeatureListener
    {
    
    public static final int    MAX_SCALE       = 10;
    
    FeaturePanel featurePanel;
    CursorPanel cursorPanel;
    DrawableSequence sequence;
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    List dasSourcePanels;
    
    //List featureRenderers;
    
    public AbstractChainRenderer() {
        super();        
        this.setOpaque(true);
        setDoubleBuffered(true);
        //clearFeatureRenderers();
        dasSourcePanels = new ArrayList();
        
    }

    protected void initPanels(){
        //featurePanel.setPreferredSize(new Dimension(200,200));
        //      x .. width
        // y .. height
        // (x1,y1,x2,y2)
        int width = getDisplayWidth();
        featurePanel.setBounds(0,0,width,100);
        featurePanel.setLocation(0,0);
        
        //cursorPanel.setPreferredSize(new Dimension(600,600));
        cursorPanel.setLocation(0,0);
        //cursorPanel.setOpaque(true);
        cursorPanel.setBounds(0,0,width,getDisplayHeight());
        
        this.add(featurePanel,new Integer(0));
        this.add(cursorPanel, new Integer(100));
        this.moveToFront(cursorPanel);
        //scale=1.0f;
        this.addMouseMotionListener(cursorPanel);
        this.addMouseListener(cursorPanel);
        updatePanelPositions();
    }
  
    public void addSequenceListener(SequenceListener li){
        cursorPanel.addSequenceListener(li);
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
        
        int DEFAULT_X_START = FeaturePanel.DEFAULT_X_START;
        int DEFAULT_X_RIGHT_BORDER = FeaturePanel.DEFAULT_X_RIGHT_BORDER;
        
        int seqLength = getSequenceLength();
        int defaultWidth = BrowserPane.DEFAULT_PANE_WIDTH;
        int width=defaultWidth;
        //int width = l  + FeaturePanel.DEFAULT_X_START + FeaturePanel.DEFAULT_X_RIGHT_BORDER;
        //int seqLength = sequence.getSequence().getLength();
        
        float s = width / (float) ( seqLength + DEFAULT_X_START + DEFAULT_X_RIGHT_BORDER );
        s = 100 * s /(zoomFactor) ;
        if ( s > MAX_SCALE)
            s = MAX_SCALE;
        
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


    public void featureSelected(FeatureEvent e) {
        // TODO Auto-generated method stub
        
    }


    /*public void addFeatureRenderer(FeatureRenderer rendr){
        featureRenderers.add(rendr);
    }
    
    public void clearFeatureRenderers(){
        featureRenderers = new ArrayList();    
    }
*/
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
        
        int totalH = featurePanel.getHeight();
        Iterator iter = dasSourcePanels.iterator();
        while (iter.hasNext()){
            DasSourcePanel dsp = (DasSourcePanel)iter.next();
            totalH+=dsp.getDisplayHeight();
       
        }
        if (totalH < 200)
            totalH = 200;
        return totalH;
    }
    
    /** add DasSourcePanels
     * 
     */

    public void newDasSource(DasSourceEvent event) {
       
        DrawableDasSource dds =event.getDasSource();
        //SpiceDasSource ds = dds.getDasSource();
        
        DasSourcePanel dspanel = new DasSourcePanel(dds);
        dds.addFeatureListener(dspanel);
        dds.addFeatureListener(this);
        
        //dspanel.setPreferredSize(new Dimension(200,200));
        int h = getDisplayHeight();
        int width = getDisplayWidth();
        
      
        int panelPos = dasSourcePanels.size();
        //logger.info("AbstractChainRenderer got new das source #" +
        //        dasSourcePanels.size() + " " + ds.getUrl() + " h " + h);
        dspanel.setLocation(0,0);
        int panelHeight = dspanel.getDisplayHeight();
        dspanel.setBounds(0,h,width,panelHeight);
        
        this.add(dspanel,new Integer(panelPos+1));  
        moveToFront(cursorPanel);
        dasSourcePanels.add(dspanel);
        Dimension d = new Dimension(width,h+panelHeight);
        this.setPreferredSize(d);
        this.setSize(d);
        this.repaint();
    

    }
    
    public void updatePanelPositions(){
        int h = featurePanel.getHeight();
        int width = getDisplayWidth();
        
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
        Dimension totalD = new Dimension(width,h);
        this.setPreferredSize(totalD);
        this.setSize(totalD);
    }

    public void disableDasSource(DasSourceEvent ds) {
        // TODO Auto-generated method stub
        
    }

    public void enableDasSource(DasSourceEvent ds) {
        // TODO Auto-generated method stub
        
    }

    

    public void selectedDasSource(DasSourceEvent ds) {
        // TODO Auto-generated method stub
        
    }

    public void newFeatures(FeatureEvent e) {
        updatePanelPositions();
        
    }

    public void loadingFinished(DasSourceEvent ds) {
        
        Iterator iter = dasSourcePanels.iterator();
        while (iter.hasNext()){
            DasSourcePanel dsp = (DasSourcePanel)iter.next();
            dsp.setLoading(false);
        }
    }

    public void loadingStarted(DasSourceEvent ds) {

        Iterator iter = dasSourcePanels.iterator();
        while (iter.hasNext()){
            DasSourcePanel dsp = (DasSourcePanel)iter.next();
            dsp.setLoading(true);
        }
        
    }
   

   
  
    
}
