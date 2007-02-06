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
package org.biojava.spice.gui.aligchooser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

//import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.biojava.bio.Annotation;

import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.jama.Matrix;
import org.biojava.spice.ResourceManager;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.alignment.StructureAlignment;
import org.biojava.spice.config.SpiceDefaults;
import org.biojava.spice.jmol.StructurePanel;
import org.biojava.spice.jmol.StructurePanelListener;
import org.biojava.spice.manypanel.eventmodel.StructureAlignmentListener;
import org.biojava.spice.manypanel.renderer.SequenceScalePanel;
import org.biojava.dasobert.eventmodel.SequenceEvent;
import org.biojava.dasobert.eventmodel.SequenceListener;
import org.biojava.dasobert.eventmodel.StructureEvent;
import org.biojava.dasobert.eventmodel.StructureListener;
import org.jmol.api.JmolViewer;

import javax.vecmath.Matrix3f;

/** a JPanel that contains radio buttons to choose, which structures to show superimposed
 * 
 * @author Andreas Prlic
 * @since 10:14:51 AM
 * @version %I% %G%
 */
public class StructureAlignmentChooser 
extends JScrollPane 
implements  
StructureAlignmentListener {
    
    static final long serialVersionUID = 65937284545329877l;
    
    public static Logger logger =  Logger.getLogger(SpiceDefaults.LOGGER);
    
    List labels;
    ButtonGroup buttonGroup;
    
    StructureAlignment structureAlignment;
    
   // Box vBox;
    Box vBoxL;
    Box vBoxR;
    List structureListeners;
 
    int referenceStructure; // the structure at that position is the first one 
    
    JTextField searchBox;
    
    StructurePanel structurePanel;
    
    public final static float radiansPerDegree = (float) (2 * Math.PI / 360);
    public final static float degreesPerRadian = (float) (360 / (2 * Math.PI));
    
    boolean sortReverse;
    JMenu parent;
    JMenuItem sort;
    JMenuItem filter;
    ImageIcon deleteIcon;
    AlignmentItemListener aliItemListener;
    JPanel content;
    
    public StructureAlignmentChooser(JMenu parent) {
        super();
        
        Box hBox = Box.createHorizontalBox();
        content = new JPanel();
        content.add(hBox);

        getViewport().setView(content);
        
        this.parent = parent;
        
        //hBox.setBackground(Color.yellow);
        content.setBackground(SequenceScalePanel.BACKGROUND_COLOR);
        //content.setBackground(Color.yellow);
        this.setBackground(SequenceScalePanel.BACKGROUND_COLOR);
        
        content.setBorder(BorderFactory.createEmptyBorder());
                
        aliItemListener         = new AlignmentItemListener(this);
        structureListeners 		= new ArrayList();
        structureAlignment 		= new StructureAlignment(null);

        labels                  = new ArrayList();
        buttonGroup             = new ButtonGroup();

        vBoxL                   = Box.createVerticalBox();
        vBoxR                   = Box.createVerticalBox();
               
        hBox.add(vBoxL);      
        hBox.add(vBoxR);
        
        referenceStructure = -1;
        
        searchBox = new JTextField();
        searchBox.setEditable(true);
        searchBox.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        searchBox.addKeyListener(new MyKeyListener(this));
        
        deleteIcon = SpiceApplication.createImageIcon(ResourceManager.getString("org.biojava.spice.Icons.EditDelete"));
    
        //int nr = structureAlignment.getNrStructures();
        this.getVerticalScrollBar().setUnitIncrement(13); // 13 is the default height of a button
      
    
    }
    
    public JPanel getContentPane(){
        return content;
    }
    
    /** the structure at that position is the first one
     * 
     * @return position
     */ 
    public int getReferenceStructure() {
        return referenceStructure;
    }


    /** the structure at that position is the first one
     * 
     * 
     * @param referenceStructure the position
     */
    public void setReferenceStructure(int referenceStructure) {
        this.referenceStructure = referenceStructure;
    }



    public void setStructurePanel(StructurePanel panel){
        this.structurePanel = panel;
    }
    
    public void setSortReverse(boolean direction){
        sortReverse = direction;
    }
    
    
    protected List getLabels() {
        return labels;
    }
    
    public JTextField getSearchBox() {
        return searchBox;
    }
    
    public void addPDBSequenceListener(SequenceListener li){
        aliItemListener.addPDBSequenceListener(li);
    }
    
    public ImageIcon getDeleteIcon() {
        return deleteIcon;
    }

    public void setDeleteIcon(ImageIcon deleteIcon) {
        this.deleteIcon = deleteIcon;
    }

    public void clearListeners(){
        structureAlignment = new StructureAlignment(null);
        structureListeners.clear();
        aliItemListener.clearListeners();
        parent = null;
    }
    public void addStructureListener(StructureListener li){
        structureListeners.add(li);
    }
    public List getStructureListeners(){
        return structureListeners;
    }
    
    private void clearButtons(){
        Iterator iter = labels.iterator();
        
        while (iter.hasNext()){
            
            AligLabel l = (AligLabel) iter.next();
            
            
            JCheckBox b = l.getCheck();
            b.removeItemListener(aliItemListener.getCheckBoxListener());
            vBoxL.remove(b);
            
            
            JRadioButton r = l.getRadio();
            r.removeItemListener(aliItemListener.getRadioButtonListener());
            vBoxR.remove(r);
            buttonGroup.remove(r);
            
        }
       
        labels.clear();   
        vBoxL.repaint();
        vBoxR.repaint();
        
    }
    
    public StructureAlignment getStructureAlignment(){
        return structureAlignment;
    }
    
    
    private void updateMenuItems(){

        if ( parent == null) {
            return;
        }

        if ( sort != null)            
            parent.remove(sort);

        if ( filter != null)
            parent.remove(filter);
        
        AlignmentSortPopup sorter = new AlignmentSortPopup(structureAlignment, this, false);
        
        AlignmentFilterActionListener filterAction = new AlignmentFilterActionListener(this);
        
        sort = MenuAlignmentListener.getSortMenuFromAlignment(structureAlignment.getAlignment(),sorter);
        parent.add(sort);     
        
        filter = MenuAlignmentListener.getFilterMenuFromAlignment(structureAlignment.getAlignment(),filterAction);        
        parent.add(filter);
      
    }
    
    public void setStructureAlignment(StructureAlignment ali){
        
        structureAlignment = ali;
        aliItemListener.setStructureAlignmnent(ali);
        //logger.info("got new structure alignment");
        if ( (ali != null) && ( ali.getIds().length > 0) )
            System.setProperty("SPICE:drawStructureRegion","true");
        
                
        clearButtons();
        if ( ali == null) {
            clearButtons();
            repaint();
            return;
        }
        
        updateMenuItems();
                
        new AlignmentSortPopup(ali,this, sortReverse);
        
        Annotation[] objects = structureAlignment.getAlignment().getObjects();
        
        boolean[] selectedArr = ali.getSelection();
      
        String[] ids = ali.getIds();
        Color background = getBackground();
        
        int displayPosition = 1;
        Color col = structureAlignment.getColor(0);
        
        for ( int i=0; i< ids.length;i++){
            
            String id = ids[i];
            
            if ( ( i == 0 ) || (structureAlignment.isSelected(i))){
            	    col = structureAlignment.getColor(i);
                UIManager.put("CheckBox.background", col);
                UIManager.put("CheckBox.interiorBackground", col);
                UIManager.put("CheckBox.highlite", col);
                
            } else {
                UIManager.put("CheckBox.background", background);
                UIManager.put("CheckBox.interiorBackground", background);
                UIManager.put("CheckBox.highlite", background);
            }
            
            
            AligLabel label = new AligLabel(displayPosition + " " + id,
                    objects[i],
                    structureAlignment.getFilterBy());
            
            
            JCheckBox structureCheckBox = label.getCheck();
            JRadioButton dasBox = label.getRadio();
            
            if ( dasBox.isVisible())
                displayPosition++;
            
            
            boolean selected = false;
            if (selectedArr[i]) {
                selected = true;
                // always show selected / even if filtered out!
                if ( ! structureCheckBox.isVisible())
                    displayPosition++;
                label.setVisible(true);
                
            }
            
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
              
            label.setSelected(selected);
            labels.add(label);
            
            vBoxL.add(structureCheckBox);
            vBoxR.add(dasBox);
            //vBox.add(label.getLabel());
            
            buttonGroup.add(dasBox);
            dasBox.addItemListener(aliItemListener.getRadioButtonListener());
            structureCheckBox.addItemListener(aliItemListener.getCheckBoxListener());
            
        }
        
        aliItemListener.setLabels(labels);
  
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
        
        
        this.repaint();
    }
    
    /** recalculate the displayed alignment. e.g. can be called after toggle full structure
     * 
     *
     */
    public void recalcAlignmentDisplay() {
    	
    	if (structureAlignment == null)
    		return;
    	
    	if (structureAlignment.getNrStructures() ==0)
    		return;
    	
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
    
    
    
    public void rotateJmol(Matrix jmolRotation) {
        if ( structurePanel != null){
            if ( jmolRotation != null) {
 
                double[] zyz = Calc.getZYZEuler(jmolRotation);
                
                String script = "reset; rotate z "+zyz[0] +"; rotate y " + zyz[1] +"; rotate z"+zyz[2]+";";

                structurePanel.executeCmd(script);
                /*structurePanel.executeCmd("show orientation");
                JmolViewer viewer = structurePanel.getViewer();
                System.out.println("rotating jmol ... " + script);
                viewer.homePosition();
                viewer.rotateToZ(Math.round(zyz[0]));
                viewer.rotateToY(Math.round(zyz[1]));
                viewer.rotateToZ(Math.round(zyz[2]));
                */
            }
        }
    }
    
    /** get the rotation out of Jmol 
     * 
     * @return the jmol rotation matrix
     */
    public Matrix getJmolRotation(){
        Matrix jmolRotation = null;
        if ( structurePanel != null){
            //structurePanel.executeCmd("show orientation;");
            JmolViewer jmol = structurePanel.getViewer();
            Object obj = jmol.getProperty(null,"transformInfo","");
            //System.out.println(obj);
            if ( obj instanceof Matrix3f ) {
                Matrix3f max = (Matrix3f) obj;
                jmolRotation = new Matrix(3,3);
                for (int x=0; x<3;x++) {
                    for (int y=0 ; y<3;y++){
                        float val = max.getElement(x,y);
                        //System.out.println("x " + x + " y " + y + " " + val);
                        jmolRotation.set(x,y,val);
                    }
                }
                
            }                             
        }    
        return jmolRotation;
    }    
    
    
     
    
    
}

class MyKeyListener extends KeyAdapter {
    
    StructureAlignmentChooser chooser;
    
    public MyKeyListener(StructureAlignmentChooser chooser){
        this.chooser = chooser;
    }
    
    public void keyPressed(KeyEvent evt) {

        JTextField txt = (JTextField) evt.getSource();
        char ch = evt.getKeyChar();
        
        String search = txt.getText();
        
        // If a printable character add to search text
        if (ch != KeyEvent.CHAR_UNDEFINED) {
            if ( ch != KeyEvent.VK_ENTER )
                if ( ch != KeyEvent.VK_HOME )
                    search += ch;
        }
                 
        StructureAlignment ali = chooser.getStructureAlignment();
        String[] ids = ali.getIds();
        if ( search.equals("")){
            search = "no search provided";
        }
        
        Border b = BorderFactory.createMatteBorder(3,3,3,3,Color.blue);
        
        List labels = chooser.getLabels();
        boolean firstFound = false;
           
        int  h = 0;
        for (int i=0; i <ids.length;i++){
            AligLabel label = (AligLabel)labels.get(i);
            JCheckBox box = label.getCheck();
            //JLabel box = label.getLabel();
            
            if ( ids[i].indexOf(search) > -1) {
                // this is the selected label
                //System.out.println("selected label " + ids[i]);
                label.setBorder(b);
                label.setBorderPainted(true);
                
                if ( ! firstFound ) {
                    // scroll to this position
                    if ( chooser != null){
                    
                        chooser.getViewport().setViewPosition(new Point (0,h)); 
                    }
                }
                firstFound = true;
                
            } else {
                // clear checkbutton
                label.setBorderPainted(false);
              
            }
            label.repaint();
            h+= box.getHeight();
        }
        if (! firstFound) {
            if ( ( search.length() > 0) && (ch != KeyEvent.CHAR_UNDEFINED) ) {
                if (! (ch == KeyEvent.VK_BACK_SPACE)) {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }
               
    }
}


/** a runnable that notifies sequence listeners
 * 
 * @author Andreas Prlic
 * @since 2:26:13 PM
 * @version %I% %G%
 */
class MySequenceRunnable implements Runnable {
    
    String ac;
    String sequence;
    List pdbSequenceListeners;
    
    public MySequenceRunnable(String ac, String sequence,List pdbSequenceListeners){
        super();
        this.ac = ac;
        this.sequence = sequence;
        this.pdbSequenceListeners = pdbSequenceListeners;
    }
    
    public void run() {
        SequenceEvent sevent = new SequenceEvent(ac,sequence);
        //logger.info("*** seqeunce event " + ac);
        Iterator iter3 = pdbSequenceListeners.iterator();
        while (iter3.hasNext()){
            
            
            SequenceListener li = (SequenceListener)iter3.next();
            li.newSequence(sevent);
        }
    }
}



    
   
    
   


