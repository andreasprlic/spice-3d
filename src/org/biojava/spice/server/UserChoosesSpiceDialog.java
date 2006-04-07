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
 * Created on Apr 7, 2006
 *
 */
package org.biojava.spice.server;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.biojava.spice.SPICEFrame;

public class UserChoosesSpiceDialog {

    public UserChoosesSpiceDialog() {
        super();

    }
    
    public static SPICEFrame choose(SpiceServer server){
        
        String[] options = new String[server.nrInstances()];
        for ( int i=0; i< server.nrInstances();i++){
            SPICEFrame s = server.getInstance(i);
            String txt = "Spice #" + (i+1) +" - "+ s.getPDBCode() + " - " + s.getUniProtCode();
            options[i]=txt;
        }
        
        JFrame frame = new JFrame("please choose SPICE window");
        String msg = "`Please choose window to display the new data... ";
        //frame.getContentPane().add(label);
        
        //JOptionPane opt = new JOptionPane(options);
        String selectedValue =(String) JOptionPane.showInputDialog(frame,
                msg,
                "please choose SPICE window",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
                );
        
        if (selectedValue == null)
            return server.getInstance(0);
        for(int counter = 0, maxCounter = options.length;
            counter < maxCounter; counter++) {
            if(options[counter].equals(selectedValue))
                return server.getInstance(counter);
        }
        
        return server.getInstance(0);
        
    }

}
