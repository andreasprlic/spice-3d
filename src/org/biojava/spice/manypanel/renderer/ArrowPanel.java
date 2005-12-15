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
 * Created on Dec 13, 2005
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.biojava.bio.structure.Chain;
import org.biojava.services.das.registry.DasCoordinateSystem;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.GUI.alignmentchooser.AlignmentChooser;
import org.biojava.spice.das.SpiceDasSource;
import org.biojava.spice.manypanel.BrowserPane;
import org.biojava.spice.manypanel.eventmodel.ObjectListener;
import org.biojava.spice.manypanel.managers.AlignmentManager;


/** a class to draw two arrows, that, if pressed will launch an alignmentChooser ...
 * 
 * @author Andreas Prlic
 *
 */
public class ArrowPanel 
extends JPanel 

{
    
    
    private static final long serialVersionUID = 8209177194129832981l;
    
    AlignmentManager upperAlignmentManager;
    AlignmentManager lowerAlignmentManager;
    
    ObjectListener upperObjectListener;
    ObjectListener lowerObjectListener;
    ImageIcon upperA;
    ImageIcon lowerA;
    Box vBox;
    JLabel upperLabel ;
    JLabel lowerLabel ;
    Chain chain;
    public ArrowPanel() {
        super();
        
        upperAlignmentManager = null;
        lowerAlignmentManager = null;
        
        vBox = Box.createVerticalBox();
        upperA = SpiceApplication.createImageIcon("1uparrow.png");
        lowerA = SpiceApplication.createImageIcon("1downarrow.png");
        
        upperLabel = new JLabel("Choose",upperA,JLabel.RIGHT);
        lowerLabel = new JLabel("Choose",lowerA,JLabel.RIGHT);
        //vBox.add(upperLabel);
        //vBox.add(lowerLabel);
        ArrowMouseListener aml = new ArrowMouseListener(this,1);
        upperLabel.addMouseListener(aml);
        
        ArrowMouseListener aml2 = new ArrowMouseListener(this,2);
        lowerLabel.addMouseListener(aml2);
               
        this.add(vBox);
        upperLabel.setEnabled(false);
        lowerLabel.setEnabled(false);
        
    }
    
    public void setUpperObjectListener(ObjectListener li){
        upperObjectListener = li;
    }
    public void setLowerObjectListener(ObjectListener li){
        lowerObjectListener = li;
    }
    
    public ObjectListener getUpperObjectListener(){
        return upperObjectListener;
    }
    public ObjectListener getLowerObjectListener(){
        return lowerObjectListener;
    }
    
    public void setUpperAlignmentManager(AlignmentManager ma){
        upperAlignmentManager = ma;
        upperLabel.setEnabled(true);
        vBox.add(upperLabel);
        this.repaint();
    }
    
    public AlignmentManager getUpperAlignmentManager(){
        return upperAlignmentManager;
    }
    
    public AlignmentManager getLowerAlignmentManager(){
        return lowerAlignmentManager;
    }
    
    public void setLowerAlignmentManager(AlignmentManager ma){
        lowerAlignmentManager = ma;
        lowerLabel.setEnabled(true);
        vBox.add(lowerLabel);
    }
    
        
    public JLabel getLabel1(){
        return upperLabel;
    }
    public JLabel getLabel2(){
        return lowerLabel;
    }

    
    
}

class ArrowMouseListener
implements MouseListener {
    ArrowPanel parent;
    int panelNumber;
    public ArrowMouseListener(ArrowPanel panel,int position){
        parent = panel;
        panelNumber = position;
    }
    
    private AlignmentManager getAlignmentManager(){
        if ( panelNumber == 1)
            return parent.getUpperAlignmentManager();
        else
            return parent.getLowerAlignmentManager();
    }
    
    
    
    private String getId(){
        String code ="";
        if ( panelNumber == 1){
            code = parent.getUpperAlignmentManager().getId2();
            if (! parent.getUpperAlignmentManager().getCoordSys2().toString().equals(BrowserPane.DEFAULT_PDBCOORDSYS)){
                code = code.toUpperCase();
            }
        }
        else {
            code= parent.getLowerAlignmentManager().getId1();
            if (! parent.getUpperAlignmentManager().getCoordSys1().toString().equals(BrowserPane.DEFAULT_PDBCOORDSYS)){
                code = code.toUpperCase();
            }
        }
        
        return code;
        
    }
    
    private DasCoordinateSystem getQueryCoordinateSystem(){
        if (panelNumber == 1)
            return parent.getUpperAlignmentManager().getCoordSys2();
        else
            return parent.getLowerAlignmentManager().getCoordSys1();
    }
    
    private DasCoordinateSystem getSubjectCoordinateSystem(){
        if (panelNumber == 1)
            return parent.getUpperAlignmentManager().getCoordSys1();
        else
            return parent.getLowerAlignmentManager().getCoordSys2();
    }
    
    
    private JLabel getLabel(){
        if (panelNumber == 1)
            return parent.getLabel1();
        else
            return parent.getLabel2();
        
    }
    private ObjectListener getObjectListener(){
        if ( panelNumber == 1)
            return parent.getUpperObjectListener();
        else
            return parent.getLowerObjectListener();
    }
    
    private Chain getChain(){
        if ( panelNumber == 1)
            return parent.getUpperAlignmentManager().getSequence2();            
         else
             return parent.getLowerAlignmentManager().getSequence1();
            
    
    
    }
    
    private SpiceDasSource[] getDasSources(){
        if ( panelNumber == 1)
            return parent.getUpperAlignmentManager().getAlignmentServers();
        else
            return parent.getLowerAlignmentManager().getAlignmentServers();
    }
    
    public void mouseClicked(MouseEvent e){
        // trigger load of  structure ...
        System.out.println("loading structure " + panelNumber);
        
        
        AlignmentManager aligM = getAlignmentManager();
        
        if ( aligM != null) {
            String code = getId();            
            DasCoordinateSystem cs1 = getQueryCoordinateSystem();
            DasCoordinateSystem cs2 = getSubjectCoordinateSystem();
            ObjectListener li = getObjectListener();
            
            AlignmentChooser chooser = new AlignmentChooser(cs1,cs2);
            chooser.addObjectListener(li);
            SpiceDasSource[] sources = getDasSources();
            
            for (int i = 0 ; i< sources.length;i++){
                System.out.println(sources[i]);
            }
            chooser.setDasSources(sources);
            Chain c = getChain();
            //System.out.println(c +" >"+c.getSequence()+"<");
            c.setSwissprotId(code);
         
            chooser.setChain(c,0);
            //chooser.s
            chooser.show();
            //chooser.addObjectListener()
            //code,cs,lowerAlignmentManager.getSeq2Listener());
            
        }
        
        
    }
    
    public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub
        String status = "";
        AlignmentManager aligM = getAlignmentManager();
        if (  aligM != null) {
            String code = getId();
            JLabel upperLabel = getLabel();
            if ( code.equals(""))
                upperLabel.setEnabled(false);
            else 
                upperLabel.setEnabled(true);
            status += "alignments of " + code;
            
            status += " vs. " + getSubjectCoordinateSystem();
            upperLabel.setToolTipText("click here to load " + status );
        }
     
    }
    
    public void mouseExited(MouseEvent arg0) {
            getLabel().setToolTipText("");
    }
    
    public void mousePressed(MouseEvent arg0) {}
    
    public void mouseReleased(MouseEvent arg0) {  }
    
}



