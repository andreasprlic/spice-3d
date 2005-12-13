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

import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.biojava.services.das.registry.DasCoordinateSystem;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.GUI.alignmentchooser.AlignmentChooser;
import org.biojava.spice.manypanel.eventmodel.ObjectListener;
import org.biojava.spice.manypanel.managers.AlignmentManager;

/** a class to draw two arrows, that, if pressed will launch an alignmentChooser ...
 * 
 * @author Andreas Prlic
 *
 */
public class ArrowPanel extends JPanel {
    
    AlignmentManager upperAlignmentManager;
    AlignmentManager lowerAlignmentManager;
    
    ObjectListener upperObjectListener;
    ObjectListener lowerObjectListener;
    ImageIcon upperA;
    ImageIcon lowerA;
    Box vBox;
    JLabel upperLabel ;
    JLabel lowerLabel ;
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
        upperLabel.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e){
                // trigger load of upper structure ...
                System.out.println("loading upper structure");
            }

            public void mouseEntered(MouseEvent arg0) {
                // TODO Auto-generated method stub
                String status = "";
                if ( upperAlignmentManager != null) {
                    String code = upperAlignmentManager.getId2();
                    if ( code.equals(""))
                        upperLabel.setEnabled(false);
                    else 
                        upperLabel.setEnabled(true);
                    status += "alignments of " + code;
                    
                    status += " vs. " + upperAlignmentManager.getCoordSys1();
                }
                upperLabel.setToolTipText("click here to load " + status );
            }

            public void mouseExited(MouseEvent arg0) {
                upperLabel.setToolTipText("");
            }

            public void mousePressed(MouseEvent arg0) {
                // TODO Auto-generated method stub
                
            }

            public void mouseReleased(MouseEvent arg0) {
                // TODO Auto-generated method stub
                
            }

           
            
        });

        lowerLabel.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e){
                // trigger load of upper structure ...
                System.out.println("loading lower structure");
                if ( lowerAlignmentManager != null) {
                    String code = lowerAlignmentManager.getId1();
                    DasCoordinateSystem cs = lowerAlignmentManager.getCoordSys2();
                    AlignmentChooser chooser = new AlignmentChooser();
                    chooser.addObjectListener(upperObjectListener);
                    //chooser.setChain()
                    chooser.show();
                    //chooser.addObjectListener()
                    //code,cs,lowerAlignmentManager.getSeq2Listener());
                    
                }
            }

            public void mouseEntered(MouseEvent arg0) {
                
                String status = "";
                if ( lowerAlignmentManager != null) {
                    String code = lowerAlignmentManager.getId1();
                    if ( code.equals(""))
                        lowerLabel.setEnabled(false);
                    else 
                        lowerLabel.setEnabled(true);
                    status += "alignments of "+  code;
                    status += " vs." + lowerAlignmentManager.getCoordSys2();                    
                }
                lowerLabel.setToolTipText("click here to load " + status);
            }

            public void mouseExited(MouseEvent arg0) {
                lowerLabel.setToolTipText("");
                
            }

            public void mousePressed(MouseEvent arg0) {
                // TODO Auto-generated method stub
                
            }

            public void mouseReleased(MouseEvent arg0) {
                // TODO Auto-generated method stub
                
            }
            
          
            
        });

        
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

    public void setUpperAlignmentManager(AlignmentManager ma){
        upperAlignmentManager = ma;
        upperLabel.setEnabled(true);
        vBox.add(upperLabel);
        this.repaint();
    }
    
    public void setLowerAlignmentManager(AlignmentManager ma){
        lowerAlignmentManager = ma;
        lowerLabel.setEnabled(true);
        vBox.add(lowerLabel);
    }
    
}


