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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.biojava.bio.Annotation;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.spice.manypanel.eventmodel.StructureEvent;
import org.biojava.spice.manypanel.eventmodel.StructureListener;

public class SpiceChainDisplay 
implements ListSelectionListener,
StructureListener {

    JList ent_list;
    Structure structure;
    List structureListeners;
  
    int chainNumber;
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
  
    
    public SpiceChainDisplay(JList list) {
        super();
        
        ent_list = list;
        clearStructureListeners();
        chainNumber = -1;
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
        chainNumber = i;
        
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
        chainNumber = 0;
        
    }
    
    public void selectedChain(StructureEvent event) {
        chainNumber = event.getCurrentChainNumber();
      
        
        
    }
    
    public Chain getChain(int chainnumber){

        //System.out.println("SpiceApplication... get chain " + chainnumber);
                        
        if ( structure == null ) {
            //logger.log(Level.WARNING,"no structure loaded, yet");
            return null ;
        }
        
        if ( structure.size() < 1 ) {
            //logger.log(Level.WARNING,"structure object is empty, please load new structure");
            return null ;
        }
        
        if ( chainnumber > structure.size()) {
            logger.log(Level.WARNING,"requested chain number "+chainnumber+" but structure has size " + structure.size());
            return null ;
        }
        
        Chain c = structure.getChain(chainnumber);
        // almost the same as Chain.clone(), here:
        // browse through all groups and only keep those that are amino acids...
        ChainImpl n = new ChainImpl() ;
        //logger.finest(c.getName());
        //logger.finest(c.getSwissprotId());
        n.setName(c.getName());
        n.setSwissprotId(c.getSwissprotId());
        Annotation anno = c.getAnnotation();
        n.setAnnotation(anno);
        ArrayList groups = c.getGroups("amino");
        for (int i = 0 ; i<groups.size();i++){
            Group group = (Group) groups.get(i);
            n.addGroup(group);      
        }
        return n;
        
    }
    
    
    public int getCurrentChainNumber(){
        return chainNumber;
    }
    
    public void newObjectRequested(String accessionCode) {
        DefaultListModel model = (DefaultListModel) ent_list.getModel() ;
        synchronized (model) {
            model.clear() ;
        }
        
    }
    
    
    

}
