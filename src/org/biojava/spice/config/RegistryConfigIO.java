/*
 *                    BioJava development code
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
 * Created on 20.09.2004
 * @author Andreas Prlic
 *
 */


package org.biojava.spice.config                  ;


import java.net.URL                        ;
import java.util.Date                      ;
import java.util.ArrayList                 ;
import java.util.List                      ;

import org.biojava.spice.SpiceApplication;
import org.biojava.spice.manypanel.renderer.SequenceScalePanel;
import java.util.logging.*                 ;
import java.awt.Dimension;
import javax.swing.Box                     ;
import javax.swing.ImageIcon               ;
import javax.swing.JLabel                  ;
import javax.swing.JPanel                  ;
import javax.swing.JFrame                  ;
import javax.swing.JProgressBar            ;
import javax.swing.BorderFactory                 ;
import java.util.*;

/** a class to contact and retreive the configuration from a DAS
 * registry server.
 * also provide GUI window to active / deactive DAS servers.
 */
public class RegistryConfigIO 
implements Runnable, ConfigurationListener

{
    // in milliseconds
    // 100 milliseconds * 60 seconds * 60 minutes * 24 hours = 1 day
    public static final long  TIME_BETWEEN_UPDATES = 100*60*60*24;
    
    URL[] registryArray  ;
    
    RegistryConfiguration config    ;
    //boolean done ;
    
    JProgressBar progressBar ;
    JFrame progressFrame      ;
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    List configListeners ;
    boolean forceUpdate;
    boolean  noUpdate;
    
    public RegistryConfigIO ( URL[] registryurl) {
        
        //spice = parent ;
        
        registryArray = registryurl ;
       // done = false ;
        configListeners = new ArrayList();
        forceUpdate= false;
        noUpdate = false;
    }
    
   // public boolean isDone(){
   //     return done ;
   // }
    
    /** set flag if contaction should not be performed
     * forceUpdate is stronger
     * 
     *  @param flag 
     *  */
    public void setNoUpdate(boolean flag){
        noUpdate = flag;
    }
    /** enforce connecting to the registry, default: false 
     * 
     * @param flag
     * */
    public void setForceUpdate(boolean flag){
        forceUpdate = flag;
    }
    public void addConfigListener(ConfigurationListener listener){
        logger.finest("adding new config listener");
        configListeners.add(listener);
    }
    
    public void run(){
        logger.finest("running registryConfigIO");
        try {
            getData();
            
            //notifyConfigListeners();
            
        } catch ( ConfigurationException e) {
            //e.printStackTrace();
            logger.log(Level.WARNING,e.getMessage());
        }
        
        
    } 
    
    /** tell all configListeners that a new config has been loaded */
    private void notifyConfigListeners(){
        logger.finest("notifying config listeners");
        
        Iterator iter = configListeners.iterator();
        while (iter.hasNext()){
            ConfigurationListener listener = (ConfigurationListener)iter.next();
            listener.newConfigRetrieved(config);
            
        }
    }
    
    
    private RegistryConfiguration loadLocalConfig() 
    throws Exception
    {
            PersistentConfig  persistentc = new PersistentConfig();
            return persistentc.load();
        
    }
    
    /** contact the das registry service and retreive new Data */
    
    private  void getData()
    throws ConfigurationException
    {
        //logger.finest("DAS Registry server config thread loadData");
        //PersistentConfig  persistentc = null ;
        RegistryConfiguration persistentconfig = null;
        try {
            //persistentc = new PersistentConfig();
            persistentconfig = null ;
            
            persistentconfig  = loadLocalConfig();
            //} catch ( javax.jnlp.UnavailableServiceException e) {
        } catch ( Exception e) {
            //System.err.println("an error occured during loading of local config");
            e.printStackTrace();
            logger.log(Level.INFO,e.getMessage() + "while loading of local config");
            //logger.log(Level.INFO,"contacting registration server");
            doRegistryUpdate();
            return ;
        } 
        
        if ( persistentconfig != null ) {
            config = persistentconfig ;
            if ( shouldDoUpdate(persistentconfig)){
                doRegistryUpdate();
            }
            
            //done = true ;     
        } else {
            // persistent config = null
            // we need to do an initial contact
           doRegistryUpdate();
        }
        
        
         
    }
    
    /** Determine if registration server should be contacted */
    
    private boolean shouldDoUpdate(RegistryConfiguration persistentconfig){
        
        String behave = config.getUpdateBehave();
        //logger.info("behave: " + behave);	    
        //behave="always";
        
        if ( forceUpdate ) { return true; }
        if ( noUpdate )    { return false; }
        
        if (! behave.equals("day")) {
            // test if we did already an update today
            // if not do update now
            logger.finest("registry contact behave is " + behave + " doing update now");
            return true ;
        }
        
       
        Date now = new Date();
        Date lastContact = persistentconfig.getContactDate();        
        long timenow     = now.getTime();
        long timelast    = lastContact.getTime();
        
        if (( timenow - timelast ) < TIME_BETWEEN_UPDATES ) {
            logger.info( "timenow " + timenow + " timelast " + timelast + " < " + TIME_BETWEEN_UPDATES);
            return true ;
        } else { 
            logger.info("last update < 1 day, using saved config");
            logger.info("forcing update");
            //return true;
            //done =true;
            //return ;                
        }
        
        // test if perhaps registry was changed in config file
        URL oldregistry = config.getRegistryUrl();
        
        if ( registryArray.length > 0 ) {
            if (! oldregistry.equals(registryArray[0])) {
                logger.finest("registry url was changed since last contact, contacting new registry service");
                return true ;
            }
        }
        
        // test if we find all required servers
        if ( persistentconfig.getServers("structure").size() < 1 ) {
            logger.finest("no structure server in local config, do an update!");
            return true;
        }
        
        if ( persistentconfig.getServers("sequence").size() < 1 ) {
            logger.finest("no sequence server in local config, do an update!");
            return true;
        }
        
        logger.finest("there seems to be no need to contact to registry now");
        
        //return false;
        return true;
    }
    
    
    private  void doRegistryUpdate() 
    throws ConfigurationException{
        
        showProgressBar();
        
        RegistryContactRunnable contact = new RegistryContactRunnable(registryArray,config);
        contact.addConfigListener(this);
        Thread t = new Thread(contact);
        t.start();
        
        // the new thread will call newConfigRetrieved when finished...
        
    }
    
    private void showProgressBar(){
        
        progressFrame = new JFrame("contacting DAS directory server");
        progressFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                
        
        ImageIcon icon = SpiceApplication.createImageIcon("spice16x16.gif");
        if (icon != null) {
            progressFrame.setIconImage(icon.getImage());
        }
        JFrame.setDefaultLookAndFeelDecorated(false);
        //progressFrame.setUndecorated(true);
        
        JPanel panel = new JPanel();
        panel.setBackground(SequenceScalePanel.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        Box vbox = Box.createVerticalBox();
        JLabel txt = new JLabel("detecting available DAS servers");	
        vbox.add(txt);
        
        progressBar = new JProgressBar(0,100);
        progressBar.setStringPainted(true); //get space for the string
        progressBar.setString("");          //but don't paint it
        progressBar.setIndeterminate(true);
        progressBar.setValue(0);
        progressBar.setMaximumSize(new Dimension(400,20));
        progressBar.setBorder(BorderFactory.createEmptyBorder());
        
        //progressBar.setMaximum(100);
        //progressBar.setValue(50);
        
        vbox.add(progressBar);
        
        //JLabel server = new JLabel("contacting "+REGISTRY, JLabel.RIGHT);
        //logger.info("contacting DAS registry at "+REGISTRY);
        //vbox.add(server);
        panel.add(vbox);
        progressFrame.getContentPane().add(panel);
        progressFrame.pack();
        
        // get resolution of screen
        Dimension dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        // Determine the new location of the window
        int w = progressFrame.getSize().width;
        int h = progressFrame.getSize().height;
        int x = (dim.width-w)/2;
        int y = (dim.height-h)/2;
        
        // Move the window
        progressFrame.setLocation(x, y);
        
        progressFrame.setVisible(true);
        
    }
    private void disposeProgressBar(){
        if ( progressFrame != null) {
            progressFrame.setVisible(false);
            progressFrame.dispose();
        }
    }
    
    /** write back the config to the SPICE application */
    public void saveConfiguration() {
        
        
        logger.finest("saving configuration - trying PersistentConfig");
        try {
            PersistentConfig ps = new PersistentConfig();
            ps.save(config);
        }
        catch ( Exception e) {
            logger.log(Level.WARNING,e.getMessage() + "while saving config locally");
        }
        
        notifyConfigListeners();
        
    }
    
  
    /** set config fromoutside
     * 
     * @param regi the config
     */
    public void setConfiguration(RegistryConfiguration regi) {
        config = regi;	
    }

    /** waiting for the thread that contacts the DAS registry to finish
     * 
     */
    public synchronized void newConfigRetrieved(RegistryConfiguration config) {
        this.config = config;  
        disposeProgressBar();
        saveConfiguration();      
            
    }    
}

