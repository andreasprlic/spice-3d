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
 * Created on Oct 4, 2006
 *
 */
package org.biojava.spice.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.biojava.bio.Annotation;
import org.biojava.bio.program.das.dasalignment.Alignment;
import org.biojava.bio.program.das.dasalignment.DASException;
import org.biojava.bio.structure.Structure;
import org.biojava.spice.StructureAlignment;
import org.biojava.spice.StructureAlignmentComparator;

public class AlignmentSortPopup 
implements MouseListener, ActionListener
{

    
    private static final String[] supportedDetails = new String[] { "LGA_S","LGA_Q","RMSD" };
    
    public static final List supportedDetailsList ;
    
    static {
        
        supportedDetailsList = Arrays.asList(supportedDetails);
    }
    
    StructureAlignment alignment;
    JPopupMenu menu ;
    
    StructureAlignmentChooser chooser;
    
    boolean sortReverse = false;
    
    public AlignmentSortPopup(StructureAlignment alignment, StructureAlignmentChooser chooser, boolean sortReverse) {
        super();
        this.alignment = alignment;
        
        this.chooser = chooser;
        this.sortReverse = sortReverse;
        
        menu = new JPopupMenu();
        JMenu sort = new JMenu("sort by");
        menu.add(sort);
        
        int nrs = alignment.getNrStructures();
        
        if (nrs < 2) {
            return;
        }

        Alignment ali = alignment.getAlignment();
        
        Annotation[] annos = ali.getObjects();
        Annotation a = annos[2];
        
        List details = (List) a.getProperty("details");
        Iterator iter = details.iterator();
        while (iter.hasNext()){
            Annotation det = (Annotation) iter.next();
            String property = (String) det.getProperty("property");
            String value    = (String) det.getProperty("detail");
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
                
                item.addActionListener(this);
            }
            
        }
        
        
    }

    public void mouseClicked(MouseEvent arg0) {
       
    }

    public void mousePressed(MouseEvent e) {
        maybePopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        //System.out.println("released right mouse " + e.isPopupTrigger());
        maybePopup(e);
           
    }
    
    private void maybePopup(MouseEvent e){
        if ( e.getButton() != MouseEvent.BUTTON3) {
            return;
        }
        if ( e.isPopupTrigger() ||( ! menu.isVisible())){
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public void mouseEntered(MouseEvent arg0) {
       
    }

    public void mouseExited(MouseEvent arg0) {
        
    }

    public void actionPerformed(ActionEvent e) {
       // System.out.println("sort by" + e.getActionCommand());
        
        String sortfield = e.getActionCommand();
        
        StructureAlignmentComparator sorter = new StructureAlignmentComparator(sortfield);
        // change the order in this alignment and
        // set it back in chooser ...
        
        //sorter.sort(e.getActionCommand(),sortReverse,alignment,chooser);
        
        Alignment a = alignment.getAlignment();
        
        List oldobjects = Arrays.asList(a.getObjects());
        
        Annotation[] objects = (Annotation[]) a.getObjects().clone();
        
        Arrays.sort(objects,sorter);
        
        if ( ! sortfield.equals("RMSD")) {
            // reverse the order!
            List tl = Arrays.asList(objects);
            List tmplst = new ArrayList(tl);
            Collections.reverse(tmplst);
            Annotation target = (Annotation) tmplst.get(tmplst.size()-1);
            tmplst.remove(tmplst.size()-1);
            tmplst.add(0,target);
            
            objects = (Annotation[]) tmplst.toArray(new Annotation[tmplst.size()]);
            
        }
        
        // copy objects form old location to new one ...
        List newobjects = Arrays.asList(objects);
        int nr = alignment.getNrStructures();
        
        Annotation[] oldmaxs = a.getMatrices();                     
        Annotation[] oldvectors = a.getVectors();
        Annotation[] oldblocks = a.getBlocks();
        
        Iterator iter = newobjects.iterator();
        int pos = 0;
        Alignment newa = new Alignment();
        Structure[] oldstructs = alignment.getStructures();
        Structure[] newstructs = new Structure[nr];
        String[] accessionCodes = new String[nr];
        
        while (iter.hasNext()){
            
            Annotation o = (Annotation) iter.next();
            int oldpos = oldobjects.indexOf(o); 
            accessionCodes[pos] =  (String)o.getProperty("dbAccessionId");
            //System.out.println("position new " + newobjects.indexOf(o) + " old:" + oldobjects.indexOf(o) );
            try {
                newa.addObject(objects[pos]);
                newa.addMatrix(oldmaxs[oldpos]);
                newa.addVector(oldvectors[oldpos]);
                
                if ( oldblocks.length > pos){
                    newa.addBlock(oldblocks[pos]);
                }
                
                if ( oldstructs[oldpos] != null) {
                    newstructs[pos] = oldstructs[oldpos];
                }
            } catch (DASException ex) {
                ex.printStackTrace();
            }
            
            pos++;
        }
        
        int oldselected = alignment.getLastSelectedPos();
        Annotation oldselobj = (Annotation)a.getObjects()[oldselected];
        int newselpos = newobjects.indexOf(oldselobj);
        
        
        
        StructureAlignment newstrucalig = new StructureAlignment(alignment.getCoordinateSystem());
        newstrucalig.setStructureServers(alignment.getStructureServers());
   
        try {
            newstrucalig.setAlignment(newa);
            newstrucalig.setAccessionCodes(accessionCodes);
            
        } catch (Exception ex ){
            ex.printStackTrace();        
        }
        
        newstrucalig.setStructures(newstructs);
        
        for (int i = 0 ; i < nr ; i ++) {
          
            Annotation object = objects[i];
          
            int oldpos = oldobjects.indexOf(object);
            
            if ( alignment.isLoaded(oldpos))
                newstrucalig.setLoaded(i,true);
                        
            if ( alignment.isSelected(oldpos))
                newstrucalig.setSelected(i,true);
        }
        
        newstrucalig.select(newselpos);
        chooser.setStructureAlignment(newstrucalig);
        
    }

}
