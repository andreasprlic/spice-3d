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
 * Created on Feb 5, 2007
 *
 */
package org.biojava.spice.gui.aligchooser;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import org.biojava.bio.Annotation;

public class AligLabel {

    JCheckBox check;
    JRadioButton radio;
    //JLabel label;
    
    public AligLabel(String description, Annotation anno,String filterBy){
        
        //UIManager.put("JLabel.height", new Integer(13));
        
       // label = new JLabel(description);
        check = new JCheckBox(description);
        radio = new JRadioButton();
        
        // get tooltip
        String tooltip = getButtonTooltip(anno);
        check.setToolTipText(tooltip);     
        radio.setToolTipText(tooltip);
             
        boolean doShow = isVisibleAfterFilter(anno,filterBy);
        if ( ! doShow) {
            check.setVisible(false);
            radio.setVisible(false);
        }        
    }
    
    public void setBorder(Border b){
        check.setBorder(b);
        radio.setBorder(b);
    }
    
    public void setBorderPainted(boolean flag){
        check.setBorderPainted(flag);
        radio.setBorderPainted(flag);
    }
    
    public void setBackground(Color bg){
        check.setBackground(bg);
        radio.setBackground(bg);
        radio.setForeground(bg);
        //label.setForeground(bg);
    }
    
    public void repaint(){
        check.repaint();
        radio.repaint();
        //label.repaint();
    }
    
    public void setSelected(boolean flag){
        check.setSelected(flag);
        radio.setSelected(flag);
    }
    
    public void setVisible(boolean flag){
        check.setVisible(flag);
        radio.setVisible(flag);
        //label.setVisible(flag);
    }

    public JCheckBox getCheck() {
        return check;
    }

    public void setCheck(JCheckBox check) {
        this.check = check;
    }

    //public JLabel getLabel() {
    //    return label;
    //}

    //public void setLabel(JLabel label) {
    //    this.label = label;
    //}

    public JRadioButton getRadio() {
        return radio;
    }

    public void setRadio(JRadioButton radio) {
        this.radio = radio;
    }
    
    
    
    
    /** return true if it should be displayed
     * 
     * @param object
     * @param filterBy
     * @return flag if is visible or not
     */
    private boolean isVisibleAfterFilter(Annotation object, String filterBy){
        boolean show = true;
        if ( filterBy == null)
            return true;
        if ( filterBy.equalsIgnoreCase(MenuAlignmentListener.showAllObjects))
            return true;
        
        List details = (List) object.getProperty("details");
        Iterator iter = details.iterator();
        while ( iter.hasNext()) {
            Annotation d = (Annotation) iter.next();
            String prop = (String) d.getProperty("property");
            if (! prop.equals(MenuAlignmentListener.filterProperty))
                continue;
            String det  = (String) d.getProperty("detail");
            if (! det.equalsIgnoreCase(filterBy)){                
                return false;
            }
        }        
        return show;
    }
    
    /** check if a detail as returned by the DAS response should be displayed
     * 
     * @param det
     * @return flag
     */
    protected static boolean shouldIgnoreDetail(String det){
        
         if ( det.startsWith("-99.") || det.equals("0.0") || det.startsWith("999."))
            return true;
         return false;
    }
    
    private String getButtonTooltip(Annotation anno){
        String tooltip = "";
        
        List details = new ArrayList();
       // System.out.println(anno);
        try {
            details = (List) anno.getProperty("details");
        } catch (NoSuchElementException e){}
        
        if ( details != null) {
        
            Iterator iter = details.iterator();
            while ( iter.hasNext()) {
                Annotation d = (Annotation) iter.next();
                String prop = (String) d.getProperty("property");
                if ( prop.equals(MenuAlignmentListener.filterProperty))
                    continue;
                String det  = (String) d.getProperty("detail");
                if ( shouldIgnoreDetail(det))
                    continue;
                if ( ! tooltip.equals("") )
                    tooltip += " | ";
                tooltip += prop + " " + det;
            }                
        }
        return tooltip;
    }
    
}
