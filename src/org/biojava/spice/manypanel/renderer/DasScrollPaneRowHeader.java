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
 * Created on Jul 31, 2006
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import org.biojava.spice.manypanel.drawable.DrawableDasSource;
import org.biojava.spice.manypanel.eventmodel.DasSourceEvent;
import org.biojava.spice.manypanel.eventmodel.DasSourceListener;


/* this class it the parallel of AbstractChainRender for the
 * scrollPane header column
 */

public class DasScrollPaneRowHeader 
extends JPanel 
implements DasSourceListener
{
    
    static final long serialVersionUID = 0l;
    public static final int SIZE = 70;
  
   
    List dasSources;
    JScrollPane scrollPane;
    
    public DasScrollPaneRowHeader(JScrollPane scroll){
        dasSources  = new ArrayList();
        scrollPane = scroll;
        this.setBackground(Color.WHITE);
       setPrefSize();
    }
    
   
    public int getDisplayHeight(){
        
        int totalH = 0;
        totalH +=  SequenceScalePanel.SIZE;
        
        
        Iterator iter = dasSources.iterator();
        while (iter.hasNext()){
            DasSourcePanelHeader dsp = (DasSourcePanelHeader)iter.next();
            totalH += dsp.getDisplayHeight();
            totalH += SequenceScalePanel.DEFAULT_Y_STEP;
       
        }
        if (totalH < 100)
            totalH = 100;
        return totalH;
    }

  
  

   public void paintComponent(Graphics g){
       //super.paintComponent(g);
       
       g.setColor(SequenceScalePanel.BACKGROUND_COLOR);       
       Rectangle drawHere = g.getClipBounds();        
       g.fillRect(drawHere.x,drawHere.y, drawHere.width, drawHere.height);
       
       
       // paint everything in one panel...
       
       int y = SequenceScalePanel.DEFAULT_Y_START + SequenceScalePanel.DEFAULT_Y_STEP ; 
       Iterator iter = dasSources.iterator();
       while (iter.hasNext()){
           DasSourcePanelHeader dsp = (DasSourcePanelHeader)iter.next();
           
           dsp.paintComponent(g,y);
           y += dsp.getDisplayHeight();
           
       }
       
       //Graphics2D g2D = (Graphics2D) g;
       //g2D.clip(drawHere);
   }
   
   
   public void setPrefSize(){
       
       int h = getDisplayHeight();
       //System.out.println("setting pref. size " + h);
       this.setPreferredSize(new Dimension(SIZE,h));
   }
   
   /* public void updatePanelPositions(){
        //int h = featurePanel.getHeight() + 20;
        int h = 0;
        int width = SIZE;
        
        
        /*Dimension viewSize = scrollPane.getViewport().getViewSize();
        
        //logger.info("viewSize w" + viewSize.getWidth() + " " + viewSize.getHeight());
        if (viewSize.getWidth() > width )
            viewSize = new Dimension(width,viewSize.height);
        //logger.info(viewSize+"");
        this.setPreferredSize(viewSize);
        this.setSize(viewSize);
        * //
        
        // x .. width
        // y .. height
        // (x1,y1,x2,y2)
        Iterator iter = dasSources.iterator();
        while (iter.hasNext()){
            DasSourcePanelHeader dsp = (DasSourcePanelHeader)iter.next();
            
            int panelHeight = dsp.getDisplayHeight();
            //System.out.println(dsp.getDrawableDasSource().getDasSource().getNickname() + " height:" + panelHeight);
            dsp.setBounds(0,h,width,panelHeight);
            Dimension d = new Dimension(width,panelHeight);
            dsp.setPreferredSize(d);
            dsp.setSize(d);
            
            h+= panelHeight;
            dsp.repaint();
            dsp.revalidate();
        }
               
        
        this.repaint();
        this.revalidate();
    }*/

   
   public DasSourcePanelHeader[] getDasSources(){
       return (DasSourcePanelHeader[]) dasSources.toArray(new DasSourcePanelHeader[dasSources.size()]);
       
   }
   
   

    public void newDasSource(DasSourceEvent ds) {
        DrawableDasSource dds = ds.getDasSource();
        
        DasSourcePanelHeader dsph = new DasSourcePanelHeader(dds);
        dasSources.add(dsph); 
        this.add(dsph);
        //dsph.setPreferredSize(new Dimension(SIZE,dsph.getDisplayHeight()));     
        
        //this.setPreferredSize(new Dimension(SIZE, getDisplayHeight()));
       
        //updatePanelPositions();
        setPrefSize();
    }
    
    public void clearDasSources(){
        dasSources.clear();
        this.removeAll();
        setPrefSize();
        
    }

    public void removeDasSource(DasSourceEvent ds) {}

    public void selectedDasSource(DasSourceEvent ds) {   }

    public void loadingStarted(DasSourceEvent ds) {}

    public void loadingFinished(DasSourceEvent ds) {
        //updatePanelPositions();
        this.repaint();
        setPrefSize();
        
    }

    public void enableDasSource(DasSourceEvent ds) {}

    public void disableDasSource(DasSourceEvent ds) { }
    
}