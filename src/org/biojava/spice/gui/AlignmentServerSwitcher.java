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
 * Created on Oct 19, 2006
 *
 */
package org.biojava.spice.gui;

import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.config.RegistryConfiguration;

public class AlignmentServerSwitcher  {

    SpiceApplication spice;
    
    public AlignmentServerSwitcher(SpiceApplication spice) {
        super();
        this.spice = spice;
        
        SpiceApplication sp = (SpiceApplication) spice;
        List servers = sp.getConfiguration().getServers("alignment");
        //SpiceDasSource[] servs = (SpiceDasSource[])servers.toArray(new SpiceDasSource[servers.size()]);
       
        String[] options = new String[servers.size()]; 
        
        Iterator iter = servers.iterator();
        int i=-1;
        while (iter.hasNext()){
            i++;
            SpiceDasSource source = (SpiceDasSource)iter.next();
            //System.out.println(source);
            options[i] = source.toString();
        }
        
        
        
        
        JFrame frame = new JFrame("Switch Alignment Server");
        String msg = "`Please choose your preferred alignment server ... ";
        
        
        
        String selectedValue =(String) JOptionPane.showInputDialog(frame,
                msg,
                "please choose SPICE window",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
                );
        
        if (selectedValue == null)
            return ;
        
        for(int counter = 0, maxCounter = options.length;
            counter < maxCounter; counter++) {
            if(options[counter].equals(selectedValue)) {
                SpiceDasSource sd = (SpiceDasSource) servers.get(counter);
                //System.out.println("you want server " + counter);
                RegistryConfiguration config = spice.getConfiguration();
                config.moveToPosition(sd.getUrl(),0);
                spice.newConfigRetrieved(config);
                
            }
        }
        
        

    }

    
    
}
