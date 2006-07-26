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
package org.biojava.spice.gui;

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
import org.biojava.bio.structure.StructureImpl;
import org.biojava.dasobert.eventmodel.StructureEvent;
import org.biojava.dasobert.eventmodel.StructureListener;

public class SpiceChainDisplay 

implements ListSelectionListener,
StructureListener {

    JList ent_list;
    Structure structure;
    List structureListeners;
  
    int chainNumber;
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
  
    String code;
    String chain;
    String requestedCode;
    
    public SpiceChainDisplay(JList list) {
        super();
        
        ent_list = list;
        clearStructureListeners();
        chainNumber = -1;
        code = "";
        chain = "";
        requestedCode = "";
    }
    
    public void clearStructureListeners(){
        structure = new StructureImpl();
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
        
        triggerSelectedChain(structure,i);
    }
    
    private void triggerSelectedChain(Structure s, int chainNumber){
        logger.info("trigger new chain " + chainNumber);
        int i = chainNumber;
        
        StructureEvent sevent = new StructureEvent(s,i);
        
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
            if ( structure.size() > 0) {      
                ArrayList chains = (ArrayList) structure.getChains(0);
                for (int i=0; i< chains.size();i++) {
                    Chain ch = (Chain) chains.get(i);
                    model.add(i,ch.getName());
                }
            }
        }
        
        chainNumber = 0;
        //logger.info("* * * got code >" + code + "< chain >" + chain+"< >" +structure.getPDBCode() +"<");
        
        if ( code.equalsIgnoreCase(structure.getPDBCode())){
            if ( ! (chain.equals(""))) {
                List chains = structure.getChains(0);
                Iterator iter = chains.iterator();
                int i = -1;
                while (iter.hasNext()){
                    i++;
                    Chain c = (Chain) iter.next();
                    String cId = c.getName();
                    if ( cId.equals(chain)){
                        triggerSelectedChain(structure,i);
                     
                        break;
                    }
                }
            }
        }
        
        code = "";
        chain = "";
    }
    
    public void selectedChain(StructureEvent event) {
        chainNumber = event.getCurrentChainNumber();
      
        
        
    }
    
    public Chain getChain(int chainnumber){

        //System.out.println("SpiceApplication... get chain " + chainnumber);
        //logger.info("getChain " + chainnumber);
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
        List groups = c.getGroups("amino");
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
        
        // try to see if the accessioncode is with chain ...
       
        //logger.info("reqeuested new structure " + accessionCode);
        String[] spl = accessionCode.split("\\.");
        
        String oldchain = chain;
        code ="";
        chain = "";
        
        
        if ( spl.length < 2)    
             code = accessionCode;
         else {
             code = spl[0];
             chain = spl[1];
         }        
        
        if ( code.equalsIgnoreCase(requestedCode)){
            chain = oldchain;
            return;
        }
        requestedCode = code;
        
    }
    public void noObjectFound(String accessionCode){
        DefaultListModel model = (DefaultListModel) ent_list.getModel() ;
        synchronized (model) {
            model.clear() ;
        }
    }
    
    
    

}
