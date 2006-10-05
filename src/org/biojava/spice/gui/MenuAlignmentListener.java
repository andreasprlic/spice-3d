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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.biojava.bio.Annotation;
import org.biojava.bio.program.das.dasalignment.Alignment;
import org.biojava.dasobert.eventmodel.AlignmentEvent;
import org.biojava.dasobert.eventmodel.AlignmentListener;

public class MenuAlignmentListener implements AlignmentListener {
    
    private static final String[] supportedDetails = new String[] { "LGA_S","LGA_Q","RMSD" };
    
    public static final String filterProperty = "filterBy";
    public static final String showAllObjects = "show all";
    public static final List supportedDetailsList ;
    
    static {
        
        supportedDetailsList = Arrays.asList(supportedDetails);
    }
    
    private static final String menuSortText = "sort by";
    private static final String menuFilterText = "filter by";
    
    JMenu parent;
    JMenu sort;
    StructureAlignmentChooser chooser;
    
    public MenuAlignmentListener(JMenu parent, StructureAlignmentChooser chooser) {
        super();
        this.parent = parent;
        this.chooser = chooser;
    }
    
    public  void newAlignment(AlignmentEvent e) {
        
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
    
    
    
    /** 
     *  get the "filter by" menu
     * @param ali
     * @param listener
     * @return a JMenu for filtering
     */
    public static JMenu getFilterMenuFromAlignment(Alignment ali, ActionListener listener){
        JMenu filter = new JMenu(menuFilterText);
        Annotation[] annos = ali.getObjects();
        
        if ( annos.length < 2)
            return filter;
        
        
        Map m = new HashMap();
        
        for ( int i=1 ; i < annos.length ; i ++) {
            Annotation a = annos[i];
            List details = (List) a.getProperty("details");
            Iterator iter = details.iterator();
            
         
            while (iter.hasNext()){
                Annotation det = (Annotation) iter.next();
                String property = (String) det.getProperty("property");
                //System.out.println(det);
                if ( property.equals(filterProperty)) {
                    String detail = (String) det.getProperty("detail");
                    m.put( detail,"");
                }
            }
        }
        
        JMenuItem all = new JMenuItem(showAllObjects);
        all.addActionListener(listener);
        filter.add(all);
        
        Set keys = m.keySet();
        
        String[] strings = (String[]) keys.toArray(new String[keys.size()]);
        Arrays.sort(strings,String.CASE_INSENSITIVE_ORDER);
        
        List keylist = Arrays.asList(strings);
        
        Iterator iterk = keylist.iterator();
        while (iterk.hasNext()){
            String property = (String) iterk.next();
            JMenuItem item = new JMenuItem(property);
            item.addActionListener(listener);
            filter.add(item);             
        }         
        
        return filter;
    }
    
    
    public static JMenu getSortMenuFromAlignment(Alignment ali, ActionListener listener){
        
        JMenu sort = new JMenu(menuSortText);
        
        Annotation[] annos = ali.getObjects();
        
        if ( annos.length < 2)
            return sort;
        
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
