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
 * Created on Jun 10, 2005
 *
 */
package org.biojava.spice.Panel.seqfeat;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;
import org.biojava.spice.JNLPProxy;
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Panel.seqfeat.FeatureView;
import org.biojava.spice.Panel.seqfeat.SelectedFeatureListener;
import org.biojava.spice.Panel.seqfeat.SpiceFeatureViewer;
import org.biojava.spice.Panel.seqfeat.TypeLabelPanel;

import java.awt.Point;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.*;

/**
 * @author Andreas Prlic
 *
 */
public class TypePanelMouseListener
implements MouseListener, MouseMotionListener {
    
    SpiceFeatureViewer parent;
    TypeLabelPanel oldSelection;
    
    public static int DEFAULT_X_START = 11;
    public static int DEFAULT_Y_STEP  = 10;
    public static Logger logger = Logger.getLogger("org.biojava.spice");
    /**
     * 
     */
    public TypePanelMouseListener(SpiceFeatureViewer parent) {
        super();
        this.parent=parent;
        oldSelection = null;
        
    }
      
    public void mouseMoved(MouseEvent e){
        int x = e.getX();
        int y = e.getY();
        
        FeatureView fv = parent.getParentFeatureView(e) ;
        if ( fv == null ){
            System.err.println("no parent found!");
            //fv.setSelected(true);
            //selectedFeatureView = fv;
            return;
        } 
        
        
        TypeLabelPanel typ = fv.getTypePanel();
        if (  oldSelection != null){
            if ( ! oldSelection.equals(typ)){
                disableSelection(oldSelection);
            }
        }
        oldSelection = typ;
        
        if ( typ.isSelectionLocked()){
            //System.out.println("selection locked, returning");
            return;
        }
        
        Point p = parent.getLocationOnLabelBox(fv);
        // y is relative to the TypePanelContainer
        // make it relative to the featureview
        int relY = y -p.y + DEFAULT_Y_STEP;
        
        int line = typ.getLineNr(relY);
        //System.out.println("p " + p  +" " + x + " "+ y +" line "+ line);
        if ( line < 0 ) { 
            disableSelection(e);
        }
        Feature f;
        try {
            f = fv.getFeatureAt(line);
        } catch (java.util.NoSuchElementException ex){
            //System.out.println(ex.getMessage());
            
            setToolTipText(null);
            typ.setSelectedType(-1);
            //System.out.println("no feature selected, returning");
            return;
        }
        //System.out.println(f);
        
        if ( x < DEFAULT_X_START){
            // test if a link is selected
            String link = f.getLink();
            //System.out.println("type panel mouselistener:" + link);
            if (( link != null) && (! link.equals(""))) {
                //System.out.println("setting link true");
                typ.setSelectedLink(line, true);
                setToolTipText(link);
                return;
            }
        } else {
            String link = f.getLink();
            if (( link != null) && (! link.equals(""))) 
                typ.setSelectedLink(line,false);
        }
        
        typ.setSelectedType(line);
        setToolTipText(f.toString());
        
        SelectedFeatureListener[] featlisteners = typ.getSelectedFeatureListeners();
        for ( int i = 0 ; i< featlisteners.length ; i++ ){
            
            SelectedFeatureListener lisi = featlisteners[i];
            lisi.selectedFeature(f);
        }
           
        FeatureViewListener[] fvls = parent.getFeatureViewListeners();
        for ( int i = 0 ; i< fvls.length ; i++ ){
            FeatureViewListener fvl = fvls[i];
            FeatureEvent event = new FeatureEvent(fv,f);
            fvl.mouseOverFeature(event);
        }
        parent.repaint();
    }
    public void mouseEntered(MouseEvent e){}
    
    
    private void disableSelection(TypeLabelPanel typ){
        setToolTipText(null);
        typ.setSelectedType(-1);
        SelectedFeatureListener[] featlisteners = typ.getSelectedFeatureListeners();
        for ( int i = 0 ; i< featlisteners.length ; i++ ){
            SelectedFeatureListener lisi = featlisteners[i];
            lisi.selectedFeature(null);
            
        }
    }
    
    private void setToolTipText(String txt){
        JPanel panel = parent.getTypePanel();
        panel.setToolTipText(txt);
    }
    
    private void disableSelection(MouseEvent e){
        
        FeatureView fv = parent.getParentFeatureView(e) ;
        if ( fv == null ){
            System.err.println("no parent found!");
            //fv.setSelected(true);
            //selectedFeatureView = fv;
            return;
        } 
        
        //Point p = parent.getLocationOnLabelBox(fv);
        TypeLabelPanel typ = fv.getTypePanel();
        disableSelection(typ);
    }
    public void mouseExited(MouseEvent e){
        //System.out.println("mouse exited");
        disableSelection(e);     
    }
    public void mousePressed(MouseEvent e){
        triggerSelectionLocked(false);
    }
    public void mouseReleased(MouseEvent e){
        
        //System.out.println("mouse released ");
        FeatureView fv = parent.getParentFeatureView(e) ;
        if ( fv == null ){
            System.err.println("no parent found!");
            //fv.setSelected(true);
            //selectedFeatureView = fv;
            return;
        } 
        Point p = parent.getLocationOnLabelBox(fv);
        TypeLabelPanel typ = fv.getTypePanel();
        if (  oldSelection != null){
            if ( ! oldSelection.equals(typ)){
                disableSelection(oldSelection);
            }
        }
        oldSelection = typ;
        int x = e.getX();
        int y = e.getY();
        // get position relative to featureview
        int relY = y -p.y + DEFAULT_Y_STEP;
        
        int line = typ.getLineNr(relY);
        //System.out.println("TypePanelMouseListener mouse released x " + x + " line " + line);
        
        
        if ( line < 0 )  
            return;
        
        typ.setSelectedType(line);
        Feature f = null;
        try {
            f = fv.getFeatureAt(line);
        } catch (java.util.NoSuchElementException ex){
            //System.out.println(ex.getMessage());
            return;
        }
        
        if ( x < DEFAULT_X_START) {
            //System.out.println("clicked link region 2");
            String link = f.getLink();
            if ( (link != null) && ( ! link.equals(""))){
                System.out.println("open link " + link);
                showDocument(link);
            }
        }
        
        
        SelectedFeatureListener[] featlisteners = typ.getSelectedFeatureListeners();
        for ( int i = 0 ; i< featlisteners.length ; i++ ){
            
            SelectedFeatureListener lisi = featlisteners[i];
            
            lisi.selectedFeature(f);
            
        }
        
        FeatureViewListener[] fvls = parent.getFeatureViewListeners();
        for ( int i = 0 ; i< fvls.length ; i++ ){
            FeatureViewListener fvl = fvls[i];
            FeatureEvent event = new FeatureEvent(fv,f);
            fvl.featureSelected(event);
        }
        
        triggerSelectionLocked(true);
        
        parent.repaint();
    }
    
    /** goes through all SeqPositionSelected listeners and locks/unlocks selection */
    private void triggerSelectionLocked(boolean flag){
        boolean selectionIsLocked = flag;
        parent.selectionLocked(flag);
        SelectedSeqPositionListener[] selectedSeqListeners = parent.getSelectedSeqPositionListeners();
        for ( int i =0 ; i < selectedSeqListeners.length; i++) {
            
            SelectedSeqPositionListener seli = selectedSeqListeners[i];
            seli.selectionLocked(flag);
        }
        
    }
    
    
    public boolean showDocument(URL url) 
    {
        if ( url != null ){
            boolean success = JNLPProxy.showDocument(url); 
            if ( ! success)
                logger.info("could not open URL "+url+" in browser. check your config or browser version.");
	    return success;
	    
        }
        else
            return false;
    }
    
    public boolean showDocument(String urlstring){
        try{
            URL url = new URL(urlstring);
            
            return showDocument(url);
        } catch (MalformedURLException e){
            logger.warning("malformed URL "+urlstring);
            return false;
        }
    }
    
    
    public void mouseClicked(MouseEvent e){}
    public void mouseDragged(MouseEvent e){}
    
}
