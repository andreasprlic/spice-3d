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
 * Created on Dec 5, 2005
 *
 */
package org.biojava.spice.GUI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.spice.manypanel.eventmodel.StructureEvent;
import org.biojava.spice.manypanel.eventmodel.StructureListener;

public class SpiceChainDisplay 
implements ListSelectionListener,
StructureListener {

    JList ent_list;
    Structure structure;
    List structureListeners;
    static Logger logger = Logger.getLogger("org.biojava.spice");
  
    
    public SpiceChainDisplay(JList list) {
        super();
        
        ent_list = list;
        clearStructureListeners();
    }
    
    public void clearStructureListeners(){
        structureListeners = new ArrayList();
    }
    
    public void addStructureListener(StructureListener li){
        structureListeners.add(li);
    }
    
    //public void itemStateChanged(ItemEvent event) {
    public void valueChanged(ListSelectionEvent event) {
        
        
        
        //logger.finest("EVENT!");
        //logger.finest(event);
        JList list = (JList)event.getSource();
        
        // Get all selected items
        //int i  = ((Integer)list.getSelectedValue()).intValue() ;
        int i  = list.getSelectedIndex();

        if ( i < 0) return ; 
        
        
        StructureEvent sevent = new StructureEvent(structure,i);
        
        Iterator iter = structureListeners.iterator();
        while (iter.hasNext()){
            StructureListener li = (StructureListener)iter.next();
            li.selectedChain(sevent);
        }
        
    }

   
    public void newStructure(StructureEvent event) {
        structure = event.getStructure();
        DefaultListModel model = (DefaultListModel) ent_list.getModel() ;
        synchronized (model) {
            model.clear() ;
                  
            ArrayList chains = (ArrayList) structure.getChains(0);
            for (int i=0; i< chains.size();i++) {
                Chain ch = (Chain) chains.get(i);
                model.add(i,ch.getName());
            }
          
               
        }
        
    }
    
    public void selectedChain(StructureEvent event) {
      
        
        
    }
    public void newObjectRequested(String accessionCode) {
        DefaultListModel model = (DefaultListModel) ent_list.getModel() ;
        synchronized (model) {
            model.clear() ;
        }
        
    }
    
    
    

}
