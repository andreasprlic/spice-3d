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
 * Created on Jul 30, 2006
 *
 */
package org.biojava.spice.gui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.spice.config.SpiceDefaults;
import org.biojava.spice.gui.logging.LoggingPanel;

public class LoggingPanelManager {

    public static Logger logger =  Logger.getLogger(SpiceDefaults.LOGGER);
      
    public static final LoggingPanel loggingPanel = new LoggingPanel(logger);
    
    public LoggingPanelManager() {
        super();
    }
    
    public static void setLogLevel(Level level){
        loggingPanel.getHandler().setLevel(level); 
        logger.setLevel(level);
    }
    
    public static void show(){
        setLogLevel(SpiceDefaults.LOG_LEVEL);        
        loggingPanel.show(null);
        
    }

}
