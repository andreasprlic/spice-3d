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
 * Created on Feb 1, 2007
 *
 */
package org.biojava.spice.gui.aligchooser;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.jama.Matrix;
import org.biojava.dasobert.eventmodel.SequenceListener;
import org.biojava.dasobert.eventmodel.StructureEvent;
import org.biojava.dasobert.eventmodel.StructureListener;
import org.biojava.spice.alignment.StructureAlignment;
import org.biojava.spice.config.SpiceDefaults;
import org.biojava.spice.jmol.StructurePanelListener;


public class AlignmentItemListener {

    CheckBoxListener checkBoxListener;
    RadioButtonListener    radioButtonListener;
    StructureAlignment structureAlignment;
    StructureAlignmentChooser parent;
    List pdbSequenceListeners;
    //List checkButtons;
    //List radioButtons;
    List labels;
    public static Logger logger =  Logger.getLogger(SpiceDefaults.LOGGER);
    
    public AlignmentItemListener(StructureAlignmentChooser parent) {
        structureAlignment = new StructureAlignment(null);
        this.parent = parent;
        pdbSequenceListeners        = new ArrayList();
        
        checkBoxListener = new CheckBoxListener(this);
        radioButtonListener = new RadioButtonListener(this);
        
        //radioButtons = new ArrayList();
        //checkButtons = new ArrayList();
        labels = new ArrayList();
    }
    
    public ItemListener getCheckBoxListener(){
        return checkBoxListener;
    }
    public ItemListener getRadioButtonListener(){
        return radioButtonListener;
    }
   
    public List getLabels() {
        return labels;
    }

    public void setLabels(List labels) {
        this.labels = labels;
    }

    public void addPDBSequenceListener(SequenceListener li){
        pdbSequenceListeners.add(li);
    }
    
    public void clearListeners(){
        pdbSequenceListeners.clear();
        //checkButtons.clear();
        //radioButtons.clear();
        labels.clear();
    }
    
    
    public void setStructureAlignmnent(StructureAlignment ali){
        structureAlignment = ali;
        
    }
    
    public StructureAlignment getStructureAlignment(){
        return structureAlignment;
    }
    
    /** a checkbox has been clicked - update the 3D display
     * 
     * @param box
     * @param e
     * @param i
     */
    protected void updateBox( AligLabel label, ItemEvent e, int i) {

        String[] ids = structureAlignment.getIds();
        String id = ids[i];
        //System.out.println("do something with " + id);
        
        if (e.getStateChange() == ItemEvent.DESELECTED) {
            // remove structure from alignment
            structureAlignment.deselect(i);
            label.setBackground(parent.getBackground());
            label.repaint();
            
            // display the first one that is selected
            // set the color to that one 
            //int j = structureAlignment.getFirstSelectedPos();
            
            int j = structureAlignment.getLastSelectedPos();
            if ( j > -1) {
                
                Color col = structureAlignment.getColor(j);
                System.setProperty("SPICE:StructureRegionColor",new Integer(col.getRGB()).toString());                        
                
            }
            parent.setReferenceStructure(j);
            
        } else {
            
            structureAlignment.select(i);
            // add structure to alignment
            Color col = structureAlignment.getColor(i);
            
            System.setProperty("SPICE:StructureRegionColor",new Integer(col.getRGB()).toString());                                      
            
            repaintBox(label,i,col);            
            
            // check if we can get the structure...
            Structure struc = null;
            try {
                struc = structureAlignment.getStructure(i);
                parent.setReferenceStructure(i);
                if ( struc.size() > 0) {
                    Chain c1 = struc.getChain(0);
                    String sequence = c1.getSequence();
                    String ac = id + "." + c1.getName();
                    
                    Runnable run = new MySequenceRunnable(ac,sequence, pdbSequenceListeners);
                    run.run();
                    
                    SwingUtilities.invokeLater(run);
                    //Thread t = new Thread(run);
                    //t.start();
                    
                } else {
                    logger.warning("could not load structure at position " +i );
                   
                    label.setSelected(false);
                }
                
            } catch (StructureException ex){
                ex.printStackTrace();
                structureAlignment.deselect(i);
                //return;
               
                label.setSelected(false);
            }
        }
        
        Matrix jmolRotation = parent.getJmolRotation();
        
        Structure newStruc = null; 
//      execute Rasmol cmd...
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
        Iterator iter2 = parent.getStructureListeners().iterator();
        while (iter2.hasNext()){
            StructureListener li = (StructureListener)iter2.next();
            li.newStructure(event);
            if ( li instanceof StructurePanelListener){
                StructurePanelListener pli = (StructurePanelListener)li;
                pli.executeCmd(cmd);
            }           
        }        
        parent.rotateJmol(jmolRotation);    
    }
    
    protected void repaintBox(AligLabel label, int pos, Color backgroundColor) {
        Color col = backgroundColor;
        UIManager.put("CheckBox.background", col);
        UIManager.put("CheckBox.interiorBackground", col);
        UIManager.put("CheckBox.highlite", col);
        
        //box.setBackground(col);
        label.setBackground(col);
        label.repaint();
    }
    
    protected JScrollPane getParent(){
        return parent;
    }
    
   
    
}


class RadioButtonListener
    implements ItemListener{
    AlignmentItemListener parent;
    
    public RadioButtonListener(AlignmentItemListener parent){
        this.parent=parent;
    }

    public void itemStateChanged(ItemEvent e) {
        //System.out.println("pressed radio button! " + e.getStateChange() + " " + ItemEvent.SELECTED);
        
        if ( e.getStateChange() != ItemEvent.SELECTED)
            return;
        
        Object source = e.getItemSelectable();
        List labels = parent.getLabels();
        Iterator iter = labels.iterator();
        StructureAlignment structureAlignment = parent.getStructureAlignment();
        int i=-1;
        while ( iter.hasNext()){
            i++;
            AligLabel label = (AligLabel) iter.next();
            JRadioButton b = label.getRadio();
            if ( source.equals(b)){
                //JCheckBox box =label.getCheck();
                //box.setSelected(true);
                label.setSelected(true);
                parent.updateBox(label, e,i); 
                //System.out.println("update " + e.getStateChange() + " " + ItemEvent.SELECTED);
                Color col ;
                if ( structureAlignment.isSelected(i))
                    col = structureAlignment.getColor(i);
                else
                    col = parent.getParent().getBackground(); 
                parent.repaintBox(label,i,col);
            }
        }
    }
    
    
}


class CheckBoxListener 
    implements ItemListener{
    
    
  
    AlignmentItemListener parent;
    
    public CheckBoxListener(AlignmentItemListener parent){
      
      
        this.parent = parent;
    }
    
   
    
    
    public void itemStateChanged(ItemEvent e) {
   
        Object source = e.getItemSelectable();
        List labels = parent.getLabels();
        Iterator iter = labels.iterator();
        int i=-1;
        StructureAlignment structureAlignment = parent.getStructureAlignment();
        while (iter.hasNext()){
            
            i++;
            AligLabel label = (AligLabel)iter.next();
            JCheckBox box =(JCheckBox)label.getCheck();
            if ( box.equals(source)){
                //System.out.println("update");
                parent.updateBox(label, e,i);
                
                if (e.getStateChange() == ItemEvent.SELECTED ) {
                    JRadioButton radio = label.getRadio();
                    radio.setSelected(true);
                } else {
                    int pos =  structureAlignment.getLastSelectedPos();
                    AligLabel prevLabel = (AligLabel)labels.get(pos);
                    JRadioButton radio = prevLabel.getRadio(); 
                    radio.setSelected(true);
                    
                }
            } else {
                Color col = null;
                
                if ( structureAlignment.isSelected(i))
                    col = structureAlignment.getColor(i);
                else
                    col = parent.getParent().getBackground(); 
                parent.repaintBox(label,i,col);
            }           
        }        
    }
   
}