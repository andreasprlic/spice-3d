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
 * Created on Mar 6, 2005
 *
 */
package org.biojava.spice.Panel;

import org.jmol.api.JmolViewer;

/** A thread solely to execute a rasmol like command, that is being sent to Jmol
 * in a separate thread.
 * this is because the sequence panel trigger many <i>select</i> commands to be sent
 * to the structure panel. These select commands should not be run on the main thread
 *  of the application. ( but is still does not solve the problem that the SequenceFeaturePanel is slow)
 * TODO: improve speed of SequenceFeaturePanel.
 * 
 * @author Andreas Prlic
 *
 */
public class JmolThread extends Thread {
    String command ;
    JmolViewer  viewer;
    /**
     * 
     */
    public JmolThread(JmolViewer viewer_, String command_) {
        super();
        command = command_;
        viewer = viewer_;
       
    }

    public void run(){
        
        viewer.evalString(command);
    }

}
