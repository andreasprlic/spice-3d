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
 * Created on May 15, 2006
 *
 */
package org.biojava.spice.gui;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JCheckBox;
//import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.spice.manypanel.eventmodel.StructureAlignmentListener;
import org.biojava.dasobert.eventmodel.SequenceEvent;
import org.biojava.dasobert.eventmodel.SequenceListener;
import org.biojava.dasobert.eventmodel.StructureEvent;
import org.biojava.dasobert.eventmodel.StructureListener;
import org.biojava.spice.StructureAlignment;
import org.biojava.spice.panel.StructurePanelListener;

/** a JPanel that contains radio buttons to choose, which structures to show superimposed
 * 
 * @author Andreas Prlic
 * @since 10:14:51 AM
 * @version %I% %G%
 */
public class StructureAlignmentChooser 
extends JPanel 
implements ItemListener, 
StructureAlignmentListener {
    
    static final long serialVersionUID = 65937284545329877l;
    
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    List checkButtons;
    StructureAlignment structureAlignment;
    Box vBox;
    List structureListeners;
    List pdbSequenceListeners;
    
    int referenceStructure; // the structure at that position is the first one 
    
    public StructureAlignmentChooser() {
        super();
        
        structureListeners 		= new ArrayList();
        structureAlignment 		= new StructureAlignment(null);
        checkButtons 			= new ArrayList();
        pdbSequenceListeners 	= new ArrayList();
        
        vBox 					= Box.createVerticalBox();
        
        this.add(vBox);
        referenceStructure = -1;
    }
    
    public void clearListeners(){
        structureAlignment = new StructureAlignment(null);
        structureListeners.clear();
        pdbSequenceListeners.clear();
    }
    public void addStructureListener(StructureListener li){
        structureListeners.add(li);
    }
    public void addPDBSequenceListener(SequenceListener li){
        pdbSequenceListeners.add(li);
    }
    private void clearButtons(){
        Iterator iter = checkButtons.iterator();
        
        while (iter.hasNext()){
            JCheckBox b = (JCheckBox)iter.next();
            b.removeItemListener(this);
            vBox.remove(b);
            
        }
        checkButtons.clear();
        vBox.repaint();
    }
    
    public StructureAlignment getStructureAlignment(){
        return structureAlignment;
    }
    
    public void setStructureAlignment(StructureAlignment ali){
        structureAlignment = ali;
        //logger.info("got new structure alignment");
        if ( (ali != null) && ( ali.getIds().length > 0) )
            System.setProperty("SPICE:drawStructureRegion","true");
        
        clearButtons();
        if ( ali == null) {
            clearButtons();
            repaint();
            return;
        }
        
        
        boolean[] selectedArr = ali.getSelection();
        
        String[] ids = ali.getIds();
        Color background = getBackground();
        for ( int i=0; i< ids.length;i++){
            String id = ids[i];
            Color col = structureAlignment.getColor(i);
            if ( i == 0 ){
                
                UIManager.put("CheckBox.background", col);
                UIManager.put("CheckBox.interiorBackground", col);
                UIManager.put("CheckBox.highlite", col);
                
            } else if ( i == 1){
            	 UIManager.put("CheckBox.background", background);
                 UIManager.put("CheckBox.interiorBackground", background);
                 UIManager.put("CheckBox.highlite", background);
            }
            JCheckBox b = new JCheckBox(id);
            boolean selected = false;
            if (selectedArr[i])
                selected = true;
            
            if ( i == 0) {
                
            	
               
            	
            	selected = true;
                
                referenceStructure = 0;
                structureAlignment.select(0);
                                
                System.setProperty("SPICE:StructureRegionColor",new Integer(col.getRGB()).toString());                                      
               
                try {
                    structureAlignment.getStructure(i);
                } catch (StructureException e){
                    selected = false;
                };
               
                
            }
            
            b.setSelected(selected);
            vBox.add(b);
            checkButtons.add(b);
            b.addItemListener(this);
        }
        
        //      update the structure alignment in the structure display.
        Structure newStruc = structureAlignment.createArtificalStructure();
        
        // execute Rasmol cmd...
        String cmd = structureAlignment.getRasmolScript();
        
        if ( newStruc.size() < 1)
            return;
        
        StructureEvent event = new StructureEvent(newStruc);
        
        Iterator iter2 = structureListeners.iterator();
        while (iter2.hasNext()){
            StructureListener li = (StructureListener)iter2.next();
            li.newStructure(event);
            if ( li instanceof StructurePanelListener){
                StructurePanelListener pli = (StructurePanelListener)li;
                pli.executeCmd(cmd);
            }
            
        }
        
        
        repaint();
    }
    
    /** recalculate the displayed alignment. e.g. can be called after toggle full structure
     * 
     *
     */
    public void recalcAlignmentDisplay() {
        logger.info("recalculating the alignment display");
        Structure newStruc = structureAlignment.createArtificalStructure(referenceStructure);
        String cmd = structureAlignment.getRasmolScript(referenceStructure);                    
        
        
        
        StructureEvent event = new StructureEvent(newStruc);
        Iterator iter2 = structureListeners.iterator();
        while (iter2.hasNext()){
            StructureListener li = (StructureListener)iter2.next();
            li.newStructure(event);
            if ( li instanceof StructurePanelListener){
                StructurePanelListener pli = (StructurePanelListener)li;
                pli.executeCmd(cmd);
            }
            
        }
    }
    
    public void itemStateChanged(ItemEvent e) {
        
        Object source = e.getItemSelectable();
        Iterator iter = checkButtons.iterator();
        int i=-1;
        while (iter.hasNext()){
            i++;
            Object o = iter.next();
            JCheckBox box =(JCheckBox)o;
            if ( o.equals(source)){
                String[] ids = structureAlignment.getIds();
                String id = ids[i];
                //System.out.println("do something with " + id);
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    // remove structure from alignment
                    structureAlignment.deselect(i);
                    box.setBackground(this.getBackground());
                    box.repaint();
                    // display the first one that is selected
                    // set the color to that one 
                    //int j = structureAlignment.getFirstSelectedPos();
                    int j = structureAlignment.getLastSelectedPos();
                    if ( j > -1) {
                  
                        Color col = structureAlignment.getColor(j);
                        System.setProperty("SPICE:StructureRegionColor",new Integer(col.getRGB()).toString());
                        
                                              
                    }
                    referenceStructure = j;
                    
                } else {
                    structureAlignment.select(i);
                    // add structure to alignment
                    Color col = structureAlignment.getColor(i);
                    
                    System.setProperty("SPICE:StructureRegionColor",new Integer(col.getRGB()).toString());                                      
                    UIManager.put("CheckBox.background", col);
                    UIManager.put("CheckBox.interiorBackground", col);
                    UIManager.put("CheckBox.highlite", col);
                   
                    box.setBackground(col);
                    box.repaint();
                    
                    // check if we can get the structure...
                    Structure struc = null;
                    try {
                        struc = structureAlignment.getStructure(i);
                        referenceStructure = i;
                        if ( struc.size() > 0) {
                        	Chain c1 = struc.getChain(0);
                        	String sequence = c1.getSequence();
                        	String ac = id + "." + c1.getName();

                        	SequenceEvent sevent = new SequenceEvent(ac,sequence);
                        	//logger.info("*** seqeunce event " + ac);
                        	Iterator iter3 = pdbSequenceListeners.iterator();
                        	while (iter3.hasNext()){
                        		SequenceListener li = (SequenceListener)iter3.next();
                        		li.newSequence(sevent);
                        	}
                        } else {
                        	logger.warning("could not load structure at position " +i );
                        }
                        
                    } catch (StructureException ex){
                        ex.printStackTrace();
                        structureAlignment.deselect(i);
                        //return;
                       JCheckBox b = (JCheckBox) source;
                       b.setSelected(false);
                    }
                }
                
                
                
                Structure newStruc = null; 
//              execute Rasmol cmd...
                String cmd = null;
                
                
                // update the structure alignment in the structure display.
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    newStruc = structureAlignment.createArtificalStructure();
                    cmd = structureAlignment.getRasmolScript();
                    
                }
                else {
                    newStruc = structureAlignment.createArtificalStructure(i);
                    cmd = structureAlignment.getRasmolScript(i);                    
                }
                
                
                StructureEvent event = new StructureEvent(newStruc);
                Iterator iter2 = structureListeners.iterator();
                while (iter2.hasNext()){
                    StructureListener li = (StructureListener)iter2.next();
                    li.newStructure(event);
                    if ( li instanceof StructurePanelListener){
                        StructurePanelListener pli = (StructurePanelListener)li;
                        pli.executeCmd(cmd);
                    }
                    
                }
                
                
                
               
                
                
            }
        }
        
        
        
        
    }
}
