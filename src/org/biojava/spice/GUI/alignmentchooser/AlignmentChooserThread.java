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
 * Created on Feb 27, 2005
 *
 */
package org.biojava.spice.GUI.alignmentchooser;


import javax.swing.JProgressBar;


import org.biojava.spice.SPICEFrame;
import org.biojava.spice.das.AlignmentTools;
import org.biojava.bio.program.das.dasalignment.Alignment;

/**
 * load all alignment for the given uniprot and puts them into AlignmentPanel
 * 
 * @author Andreas Prlic
 *
 */
public class AlignmentChooserThread extends Thread {
    String uniprot;
    AlignmentChooser aligChooser;
    JProgressBar progressBar;
    //SPICEFrame spice;
    /**
     * 
     */
    // todo: remove this class ...
    public AlignmentChooserThread(String up, AlignmentChooser aP, JProgressBar pB ) {
        super();
        System.err.println("obsolete class!");
        //spice = parent;
        uniprot = up;
        aligChooser = aP;
        progressBar =pB;
        // TODO Auto-generated constructor stub
        
    }
    
    public void run() {
        progressBar.setIndeterminate(true);
        //spice.setLoading(true);   
        Alignment[] aligs = null ;
        
        if (uniprot != null) { 
            // get alternative alignments with structure for it
            /*
            AlignmentTools aligTools = new AlignmentTools(spice.getConfiguration());
            aligs = aligTools.getAlignments(uniprot);
            if ( aligs != null ) {
                //System.out.println("got "+aligs.length+ " alignments");
                aligChooser.setAlignments(aligs);
                
            }
            */
           
        }
        
        progressBar.setIndeterminate(false);
        //Container c = aligPanel.getParent().getParent();
        //System.out.println(c);
        //if ( c instanceof JPanel ){
        //    System.out.println("jpanel");
        //}
        //Dimension d = aligPanel.getSize();
        //aligPanel.setSize(d);
        //c.repaint();
        //aligPanel.paint(aligPanel.getGraphics());
        //spice.setLoading(false);
        //aligPanel.repaint();
           
    }
    
    
    
    
}
