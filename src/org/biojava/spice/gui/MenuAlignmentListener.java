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
 * Created on Oct 5, 2006
 *
 */
package org.biojava.spice.gui;

import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.biojava.bio.Annotation;
import org.biojava.bio.program.das.dasalignment.Alignment;
import org.biojava.dasobert.eventmodel.AlignmentEvent;
import org.biojava.dasobert.eventmodel.AlignmentListener;

public class MenuAlignmentListener implements AlignmentListener {
    
    private static final String[] supportedDetails = new String[] { "LGA_S","LGA_Q","RMSD" };
    
    public static final List supportedDetailsList ;
    
    static {
        
        supportedDetailsList = Arrays.asList(supportedDetails);
    }
    
    
    JMenu parent;
    JMenu sort;
    StructureAlignmentChooser chooser;
    
    public MenuAlignmentListener(JMenu parent, StructureAlignmentChooser chooser) {
        super();
        this.parent = parent;
        this.chooser = chooser;
    }
    
    public synchronized void newAlignment(AlignmentEvent e) {
      
        AlignmentSortPopup sorter = new AlignmentSortPopup(chooser.getStructureAlignment(), chooser, false);
        if ( sort != null)
            parent.remove(sort);
        sort = getMenuFromAlignment(e.getAlignment(),sorter);
        parent.add(sort);
        
    }
    
    public void noAlignmentFound(AlignmentEvent e) {
      
        if ( sort != null)
            sort.setEnabled(false);
    }
    
    public void clearAlignment() {
        if ( sort != null) {
            parent.remove(sort);
            sort = null;
        }
      
        
    }
    
    
    public static JMenu getMenuFromAlignment(Alignment ali, ActionListener listener){
        
        JMenu sort = new JMenu("sort by");
        
        Annotation[] annos = ali.getObjects();
        Annotation a = annos[2];
        
        List details = (List) a.getProperty("details");
        Iterator iter = details.iterator();
        while (iter.hasNext()){
            Annotation det = (Annotation) iter.next();
            String property = (String) det.getProperty("property");
            //String value    = (String) det.getProperty("detail");
            //System.out.println("prop " + property + " " + value);
            
            boolean okProperty = false;
            Iterator iter2 = supportedDetailsList.iterator();
            while (iter2.hasNext()){
                String supported = (String)iter2.next();
                if ( supported.equals(property)){
                    okProperty = true;
                    break;
                }
            }
            
            if ( okProperty) {
                JMenuItem item = new JMenuItem(property);
                sort.add(item);
                
                item.addActionListener(listener);
            }
            
        }
        
        
        return sort;
    }
    
    
}
