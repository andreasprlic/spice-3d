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
package org.biojava.spice.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import org.biojava.spice.SPICEFrame;


/**This class takes care of the events that are triggered 
 * if a MenuItem is choosen froom the main spice menu.
 * 
 * @author Andreas Prlic
 *
 */
public class SpiceMenuListener   
implements ActionListener
{
    
    
    static String DESCRIPTION_TEXT = "The SPICE Applet. V 0.55 (C) Andreas Prlic, Tim Hubbard\n"+
    "The Wellcome Trust Sanger Institute 2004 mailto:ap3@sanger.ac.uk";
    static String reset = "select all; cpk off ; wireframe off ; backbone off; cartoon off ; ribbons off; " ;
    static String noselect = "select none; ";
    
    SPICEFrame parent ;
    
    public SpiceMenuListener (SPICEFrame spice) {
        parent = spice ;
    }
    
    public void actionPerformed(ActionEvent e) {
        //System.out.println(e);
        //System.out.println(">"+e.getActionCommand()+"<");

        String cmd = e.getActionCommand();
        if ( cmd.equals("Open") ) {
            OpenDialog op = new OpenDialog(parent);
            op.show();
        } else if (cmd.equals("Exit")) {
            System.exit(0);
        } else if (cmd.equals("Properties")) {
            parent.showConfig();
            //RegistryConfigIO regi = new RegistryConfigIO(parent,parent.REGISTRY_URL) ;	    
            //regi.setConfiguration(config);
            //regi.showConfigFrame();
        } else if (cmd.equals("Reset")) {
            parent.resetDisplay();
        } else if (cmd.equals("About SPICE")) {
            AboutDialog asd = new AboutDialog((JFrame)parent);
            
            asd.setText(DESCRIPTION_TEXT) ;
            
            asd.show();	    
        } else if ( cmd.equals("Backbone") ){
            String dcmd  = reset + "backbone 0.5;  " +noselect;
            parent.executeCmd(dcmd);
        } else if ( cmd.equals("Wireframe") ){
            String dcmd  = reset + "wireframe on; "+noselect;
            parent.executeCmd(dcmd);
        } else if ( cmd.equals("Cartoon") ){
            String dcmd  = reset + "cartoon on; "+noselect;
            parent.executeCmd(dcmd);
        } else if ( cmd.equals("Ball and Stick") ){
            String dcmd  = reset + "wireframe 0.3; spacefill 0.5; "+noselect;
            parent.executeCmd(dcmd);
        } else if ( cmd.equals("Spacefill") ){
            String dcmd  = reset + "spacefill on; "+noselect;
            parent.executeCmd(dcmd);
        } else if ( cmd.equals("Color - chain")) {
            String dcmd = "select all; color chain;" +noselect;
            parent.executeCmd(dcmd);
        } else if ( cmd.equals("Color - secondary")) {
            String dcmd = "select all; color structure;" +noselect;
            parent.executeCmd(dcmd);
        } else if ( cmd.equals("Color - cpk")) {
            String dcmd = "select all; color cpk;" +noselect;
            parent.executeCmd(dcmd);
        } else if ( cmd.equals("Choose")){
            //System.out.println("pressed alig window open");
            AlignmentChooser aligc = new AlignmentChooser(parent);
            aligc.show();
        } else {
            System.out.println("unknown menu comand " + cmd);
        }
        
    }
    
}
