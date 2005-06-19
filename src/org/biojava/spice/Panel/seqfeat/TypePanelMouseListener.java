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
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Panel.seqfeat.FeatureView;
import org.biojava.spice.Panel.seqfeat.SelectedFeatureListener;
import org.biojava.spice.Panel.seqfeat.SpiceFeatureViewer;
import org.biojava.spice.Panel.seqfeat.TypeLabelPanel;

import java.awt.Point;

/**
 * @author Andreas Prlic
 *
 */
public class TypePanelMouseListener
implements MouseListener, MouseMotionListener {
    
    SpiceFeatureViewer parent;
    TypeLabelPanel oldSelection;
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
        
        FeatureView fv = parent.getParentFeatureView(e,TypeLabelPanel.class) ;
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
        
        int line = typ.getLineNr(y-p.y);
        //System.out.println("p " + p  +" " + x + " "+ y +" line "+ line);
        if ( line < 0 ) { 
            disableSelection(e);
        }
        
        typ.setSelectedType(line);
        Feature f;
        try {
            f = fv.getFeatureAt(line);
        } catch (java.util.NoSuchElementException ex){
            //System.out.println(ex.getMessage());
            return;
        }
        //System.out.println(f);
        
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
        
        
    }
    public void mouseEntered(MouseEvent e){}
    
    
    private void disableSelection(TypeLabelPanel typ){

        typ.setSelectedType(-1);
        SelectedFeatureListener[] featlisteners = typ.getSelectedFeatureListeners();
        for ( int i = 0 ; i< featlisteners.length ; i++ ){
            SelectedFeatureListener lisi = featlisteners[i];
            lisi.selectedFeature(null);
            
        }
    }
    
    private void disableSelection(MouseEvent e){
        
        FeatureView fv = parent.getParentFeatureView(e,TypeLabelPanel.class) ;
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
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){
        FeatureView fv = parent.getParentFeatureView(e,TypeLabelPanel.class) ;
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
        int y = e.getY();
        int line = typ.getLineNr(y-p.y);
        //System.out.println("p " + p  +" " + x + " "+ y +" line "+ line);
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
    }
    
    public void mouseClicked(MouseEvent e){}
    public void mouseDragged(MouseEvent e){}
    
}
