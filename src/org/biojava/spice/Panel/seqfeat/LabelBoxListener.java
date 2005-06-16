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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPopupMenu;

import org.biojava.spice.Panel.seqfeat.FeatureView;
import org.biojava.spice.Panel.seqfeat.LabelPane;
import org.biojava.spice.Panel.seqfeat.SpiceFeatureViewer;

/** a mouse listener class that acts on the labels in the spice feature view
 * @author Andreas Prlic
 *
 */

class LabelBoxListener implements MouseListener,MouseMotionListener{
    SpiceFeatureViewer parent ;
    int prev_y ;
    boolean moved;
    boolean isDragging ;
    FeatureView selectedFeatureView;
    JPopupMenu popupMenu ;
    public LabelBoxListener(SpiceFeatureViewer parent) {
        this.parent = parent;
        popupMenu = new JPopupMenu();
        prev_y = -1;
        moved = false;
        isDragging =  false;
        selectedFeatureView = null;
        
    }
    public void mouseClicked(MouseEvent e) {
        //System.out.println("mouse clicked" + e.getX()+" " + e.getY());    
    }
    public void mousePressed(MouseEvent e) {
       // System.out.println("mouse pressed" + e.getX()+" " + e.getY());
        
        
        FeatureView fv = parent.getParentFeatureView(e, LabelPane.class) ;
        if ( fv != null ){
            fv.setSelected(true);
            selectedFeatureView = fv;
        } else {
            System.err.println("no parent found!");
        }
    }
    
    
   private void testMoveFV(MouseEvent e, FeatureView fv){

       Component compo = e.getComponent();
       
       
       //int compo_h = compo.getHeight();
       LabelPane lab = fv.getLabel();
       
       //Point screenTopLeft = lab.getLocationOnScreen();
       //int cx = screenTopLeft.x;
       //int cy = screenTopLeft.y;
       
       Point relative_p = parent.getLocationOnLabelBox(fv);
       int relative_y = relative_p.y;
       
       //System.out.println("y " + y + "prev_y" + prev_y + " cx:" + cx + " cy:" + cy + " compo_h " + compo_h );
       
       int compo_h = lab.getHeight();
       int y = e.getY();
       
       //System.out.println("y " + y + " rel_y " + relative_y + " h " + compo_h );
       
       
       // prevent big jumps 
       if ( y== prev_y){
           return;
       }
       prev_y = y ;
       
       // moving down one ...
       if ( y > (compo_h+relative_y ) ){
           // reorder FeatureView one down ...
           //System.out.println("md " + y + " " + prev_y);
           //FeatureView fv = parent.getParentFeatureView(lp,LabelPane.class);
           parent.moveDown(fv);
           moved = true;
           
       }
       
       if ( y < relative_y ) {
           //System.out.println("mu"+ y + " " + prev_y);
           //FeatureView fv = parent.getParentFeatureView(lp,LabelPane.class);
           parent.moveUp(fv);
           moved =true;
       }
       
   }
    
    public void mouseReleased(MouseEvent e) {
        //System.out.println("mouse Released" + e.getX()+" " + e.getY());
        moved = false;
        
        int mouseButton = e.getButton();
        if ( mouseButton == MouseEvent.BUTTON1 )  {
        //FeatureView fv = parent.getParentFeatureView(e) ;
        FeatureView fv = selectedFeatureView;
        if ( fv != null ){
            fv.setSelected(false);
            selectedFeatureView = null;
            	if (! isDragging) {
            	    return;
            	} 
            //	System.out.println("isdragging: " + isDragging);
            	isDragging = false;
            	testMoveFV(e,fv);
            	
            
            
        } else {
            System.err.println("no parent found!");
        }
        
        } else if( mouseButton == MouseEvent.BUTTON2 ) {
            // open a popupMenu...
            
            
        }
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {
        //System.out.println("mouse moved" + e.getX()+" " + e.getY());
    }
    public void mouseDragged(MouseEvent e) {
        //System.out.println("mouse dragged" + e.getX()+" " + e.getY());
        
        isDragging = true ;
        FeatureView fv = selectedFeatureView;
        if ( fv != null ){
            testMoveFV(e,fv);
        }
        
    }
    
}
