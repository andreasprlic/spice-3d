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
 * Created on Oct 28, 2005
 *
 */
package org.biojava.spice.manypanel;

import javax.swing.*;

import java.awt.Dimension;
import java.net.*;

import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.das2.io.DasSourceReaderImpl;
import org.biojava.dasobert.dasregistry.*;

public class SpiceLayeredPanel 
{
    
    public static String PDBCOORDSYS     = "PDBresnum,Protein Structure";
    public static String UNIPROTCOORDSYS = "UniProt,Protein Sequence";
    public static String ENSPCOORDSYS    = "Ensembl,Protein Sequence";
    public static  String registry = "http://servlet.sanger.ac.uk/dasregistry/services/das_registry";
    
    //JLayeredPane layeredPane;
    
    
    public SpiceLayeredPanel() {
        super();
    }
    
    public static DasSource[] getAllDasSources() throws Exception{
        
        URL rurl = new URL(registry);
        //DasRegistryAxisClient rclient = new DasRegistryAxisClient(rurl);
        //DasSource[]  allsources = rclient.listServices();
        DasSourceReaderImpl reader = new DasSourceReaderImpl();
        
        DasSource[] allsources = reader.readDasSource(rurl);
        
        return allsources;
    }   
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        
        JFrame spiceFrame = new JFrame("SPICE - devel");
        spiceFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        BrowserPane browserPane = new BrowserPane(PDBCOORDSYS,UNIPROTCOORDSYS,ENSPCOORDSYS);
        try {
            DasSource[] dss = SpiceLayeredPanel.getAllDasSources();
            SpiceDasSource[] sds = new SpiceDasSource[dss.length];
            for (int i =0 ; i < dss.length;i++){
                sds[i] = SpiceDasSource.fromDasSource(dss[i]);
            }
            browserPane.setDasSources(sds);
            browserPane.triggerLoadStructure("1boi");
            
            //browserPane.triggerLoadUniProt("P50225");
            //browserPane.triggerLoadENSP("ENSP00000346625");
            browserPane.setPreferredSize(new Dimension(600, 600));
            //browserPane.setOpaque(true); // contentPanes must be opaque
        } catch (Exception e){
            e.printStackTrace();
        }
        spiceFrame.setContentPane(browserPane); 
        spiceFrame.pack();
        spiceFrame.setVisible(true);
        
        
        
    }
    
    
    
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
    
    
}
