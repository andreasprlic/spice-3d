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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.config.RegistryConfiguration;
import org.biojava.spice.config.SpiceDefaults;

public class AlignmentServerSwitcher  {
    
    public static Logger logger =  Logger.getLogger(SpiceDefaults.LOGGER);
    
    public static final String STRUCTURE_CATEGORY = "Protein Structure";
    
    public AlignmentServerSwitcher(SpiceApplication spice) {
        super();
        
        
        List servers = spice.getConfiguration().getServers("alignment" );
        
        List structureservers = new ArrayList();
        Iterator iter = servers.iterator();
        while (iter.hasNext()){
            SpiceDasSource source = (SpiceDasSource)iter.next();
            DasCoordinateSystem[] csses = source.getCoordinateSystem();
            for ( int i =0 ; i< csses.length ; i++){
                DasCoordinateSystem cs = csses[i];
                String cat = cs.getCategory();
                
                if ( cat.equals(STRUCTURE_CATEGORY)) {
                    structureservers.add(source);
                    break;
                }
            }
        }
        
        
        String[] options = new String[structureservers.size()]; 
        
        Iterator iter2 = structureservers.iterator();
        int i=-1;
        while (iter2.hasNext()){
            i++;
            SpiceDasSource source = (SpiceDasSource)iter2.next();
            //System.out.println(source);
            String txt = source.getNickname() + " - " + source.getDescription();
            options[i] = txt;
        }
        
        
        
        
        JFrame frame = new JFrame("Switch Alignment Server");
        String msg = "Choose preferred alignment server ... ";
        
        
        
        String selectedValue =(String) JOptionPane.showInputDialog(frame,
                msg,
                "Choose preferred alignment server",
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

                
                // TODO: solve this in a nicer way!!!
                // e.g. use coordinate systems ...
                // for CASP - also the structure servers need to change position -
                // this is because they serve the structures already aligned, rather
                // than letting spice do the rotations :-/
                if ( sd.getNickname().equals("lgaalignments_dep")){
                    
                    moveStructureServerToTop("depstructure",config);
                    
                } else if ( sd.getNickname().equals("lgaalignments_indep")){
                    
                    moveStructureServerToTop("indepstructure",config);
                    
                } else if ( sd.getNickname().equals("dal_alignment")){
                    
                    moveStructureServerToTop("dalstructure",config);
                    
                }
                
                logger.fine("moving alignment server " + sd.getUrl() + " to top position in config");
                config.moveToPosition(sd.getUrl(),0);
                spice.newConfigRetrieved(config);
                
                // trigger reload...
                spice.reload();
                
            }
        }
        
        
        
    }
    
    
    private void moveStructureServerToTop(String nickname, RegistryConfiguration config){
        
        List servers = config.getAllServers();
        Iterator iter = servers.iterator();
        while (iter.hasNext()){
            SpiceDasSource ds = (SpiceDasSource) iter.next();
            if ( ds.getNickname().equalsIgnoreCase(nickname)){
                if ( ds.hasCapability("structure")){
                    logger.finest("moving structure server " + nickname + " to top");
                    config.moveToPosition(ds.getUrl(),0);
                    return;
                }
                // this is not a structure server, so do nothing
                return;
            }
        }
        
    }
    
    
}
