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
 * Created on Feb 2, 2005
 *
 */
package org.biojava.spice.GUI.alignmentchooser;

import org.biojava.spice.SPICEFrame;
import org.biojava.spice.SpiceApplication;
import org.biojava.bio.structure.Chain;
import org.biojava.spice.DAS.AlignmentTools;
import org.biojava.bio.program.das.dasalignment.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
/**
 * A class to choose bewteen differen sequence and structure alignments.
 * The structure choosen here will be displayed as the main structure in SPICE.
 * 
 * @author Andreas Prlic
 *
 */
public class AlignmentChooser {
    
    SPICEFrame spice ;
    JProgressBar progressBar;
    AlignmentPanel aligPanel;
    
    /**
     * @param arg0
     */
    public AlignmentChooser(SPICEFrame parent) {
        spice = parent ;
        
    }
    
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = SpiceApplication.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    public void show(){
        System.out.println("starting to retreive Alignments");
        // get uniprot seq from spice.
        
        int currentChainNumber = spice.getCurrentChainNumber();
        Chain chain = spice.getChain(currentChainNumber);
        String uniprot = chain.getSwissprotId();
        Alignment[] aligs = null ;
       
        	// display in a panel
        
        JFrame alignmentFrame = new JFrame();       
        
        alignmentFrame.setTitle("Choose Sequence - Structure Alignment");
        alignmentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //alignmentFrame.setSize(700, 700);
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(700,700));
        //	JFrame.setDefaultLookAndFeelDecorated(false);
        ImageIcon icon = createImageIcon("spice.png");
        alignmentFrame.setIconImage(icon.getImage());
       
        Box vBox = Box.createVerticalBox();
        Box hBox = Box.createHorizontalBox();
        
        // visualize these.
        JTextField txt =new JTextField("UniProt - PDB Alignments for "+uniprot );
        txt.setMaximumSize(new Dimension(400,20));
        progressBar = new JProgressBar(0,100);
    		progressBar.setValue(0);
    		progressBar.setStringPainted(false);
    		progressBar.setString(""); 
    		progressBar.setMaximumSize(new Dimension(80,20));
    		progressBar.setIndeterminate(true);
    		progressBar.setBorder(BorderFactory.createEmptyBorder());
        
    		
    		hBox.add(txt);
    		hBox.add(progressBar,BorderLayout.EAST);
    		
    		vBox.add(hBox);
    		//panel.add(hBox);
        
        aligPanel = new AlignmentPanel(spice);
        aligPanel.setMinimumSize(new Dimension(30,30));
        //aligPanel.setBackground(Color.black);
        aligPanel.setChain(chain,currentChainNumber);
        aligPanel.setAlignments(aligs);
        
        JScrollPane scroll = new JScrollPane(aligPanel) ;
        scroll.setPreferredSize(new Dimension( 600, 600));
        //scroll.setPreferredSize(new Dimension(600, 600));;
        scroll.setMinimumSize(  new Dimension(30, 30));;
        
        vBox.add(scroll);
        panel.add(vBox);
        //panel.add(scroll);
        //panel.add(aligPanel);
        
        //aligPanel.repaint();
        alignmentFrame.getContentPane().add(panel);
        alignmentFrame.pack();
        
        alignmentFrame.setVisible(true);
        
        AlignmentChooserThread act = new AlignmentChooserThread(uniprot,spice,aligPanel,progressBar);
        act.start();
        
        AligPanelListener apl = new AligPanelListener(aligPanel);
        alignmentFrame.addComponentListener(apl);
        
    }
}
