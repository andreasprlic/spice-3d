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
 * Created on Nov 18, 2005
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;


import javax.swing.JPanel;
import javax.swing.JProgressBar;

import java.awt.*;

import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.Segment;
import org.biojava.spice.manypanel.drawable.*;
import org.biojava.spice.manypanel.eventmodel.*;

/** a class that draws the content of a das source
 * 
 * @author Andreas Prlic
 *
 */
public class DasSourcePanel 
extends JPanel
implements FeatureListener
{
    DrawableDasSource dasSource;
    
    float scale;
    static Logger logger = Logger.getLogger("org.biojava.spice");
    JProgressBar bar;
    public DasSourcePanel(DrawableDasSource ds) {
        super();
        dasSource = ds;
        scale = 1.0f;
        setOpaque(true);
        bar = new JProgressBar();
        bar.setIndeterminate(false);
        bar.setPreferredSize(new Dimension(100,10));
        bar.setVisible(true);
        bar.setLocation(1,10);
        bar.setBounds(0,0,100,10);
        //add(bar);
        this.setBackground(Color.white);
    }
    
    
    
    public void setScale(float scale) {
        
        this.scale=scale;
        this.repaint();
    }
    
    public int getDisplayHeight(){
        int h = FeaturePanel.DEFAULT_Y_START + 10 + FeaturePanel.LINE_HEIGHT;
        h += (dasSource.getFeatures().length +1 ) * FeaturePanel.DEFAULT_Y_STEP ;
        //logger.info(dasSource.getDasSource().getNickname() + " height:" + h);
        return h;
    }
    
    
    public void setLoading(boolean flag){
        dasSource.setLoading(flag);
    }
    
    public void paint(Graphics g){
        super.paint(g);
        
        if (dasSource.getLoading()){
            logger.info(" draw loading bar");
            // add a progressbar           
            bar.setIndeterminate(true);
            //add
            bar.paint(g);
            
        } else {
            bar.setIndeterminate(false);
        }
        
        g.setColor(Color.black);
        
        String str = dasSource.getDasSource().getNickname();
        //logger.info("paint DasSourcePanel "+str);
        
        g.drawString(str,10,10);
        //g.fillRect(10,10,20,20);
        Feature[] features = dasSource.getFeatures();
        
        Graphics2D g2D = (Graphics2D)g;
        
        int y = FeaturePanel.DEFAULT_Y_START + 10 ;
        //logger.info(dasSource.getDasSource().getNickname() + " " + dasSource.getLoading());
        
        //Composite oldComp = g2D.getComposite();
        //g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));        
        
        for ( int i = 0 ; i < features.length; i++){
            
            Feature f = features[i];
            g.drawString(f.getType(),10,y+10);
            //logger.info(f.toString());
            List segs = f.getSegments();
            Iterator iter = segs.iterator();
            while ( iter.hasNext()){
                Segment s = (Segment)iter.next();
                g.setColor(s.getColor());
                int pstart = s.getStart()-1;
                int pend   = s.getEnd()-1;
                int startX = Math.round(pstart*scale) + FeaturePanel.DEFAULT_X_START;
                int segl = pend - pstart +1;
                int endX   = Math.round(segl*scale);
                //logger.info(s.getName()+ " "+ pstart+" ("+startX + ") " 
                //+ pend + " (" +endX+") y:" + y );
                Rectangle feature = new Rectangle(startX,y,endX,FeaturePanel.LINE_HEIGHT);
                g2D.fill(feature);
                //g2D.fillRect(startX,y,endX,FeaturePanel.LINE_HEIGHT);
            }
            
            y+= FeaturePanel.DEFAULT_Y_STEP;    
        }
        
        //g2D.setComposite(oldComp);
        
    }
    
    
    
    public void featureSelected(FeatureEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    
    
    public void newFeatures(FeatureEvent e) {
        //logger.info(" dassourcepanel: drawable das source got new features, repaint!");
        int panelWidth = getWidth();
        int panelHeight = getDisplayHeight();
        Dimension d = new Dimension(panelWidth,panelHeight);
       
        this.setPreferredSize(d);
        this.setSize(d);
        this.repaint();
        //this.updateUI();
        //this.revalidate();
    }
    
}
