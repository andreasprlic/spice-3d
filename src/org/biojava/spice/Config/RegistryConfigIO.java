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


package org.biojava.spice.Config                  ;

// to get config file via http
import java.net.URL                        ;

import java.util.Date                      ;
import java.util.ArrayList                 ;
import java.util.List                      ;

// for DAS registration server:
import org.biojava.services.das.registry.* ;
import org.biojava.spice.das.SpiceDasSource;

//for logging
import java.util.logging.*                 ;

// for GUI;
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
extends Thread

{
    // in milliseconds
    // 100 milliseconds * 60 seconds * 60 minutes * 24 hours = 1 day
    public static final long  TIME_BETWEEN_UPDATES = 10*60*60*24;
    
    URL[] registryArray  ;
    
    RegistryConfiguration config    ;
    boolean done ;
    
    JProgressBar progressBar ;
    JFrame progressFrame      ;
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    List configListeners ;
    boolean forceUpdate;
    
    public RegistryConfigIO ( URL[] registryurl) {
        
        //spice = parent ;

        registryArray = registryurl ;
        done = false ;
        configListeners = new ArrayList();
        forceUpdate= false;
    }
    
    public boolean isDone(){
        return done ;
    }
    
    /** enforce connecting to the registry, default: false */
    public void setForceUpdate(boolean flag){
        forceUpdate = flag;
    }
    public void addConfigListener(ConfigurationListener listener){
        configListeners.add(listener);
    }
    
    public void run(){
        try {
            getData();
            
            
            notifyConfigListeners();
            
            
            
        } catch ( ConfigurationException e) {
            //e.printStackTrace();
            logger.log(Level.WARNING,e.getMessage());
        }
    } 
    
    /** tell all configListeners that a new config has been loaded */
    private void notifyConfigListeners(){
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
            System.err.println("an error occured during loading of local config");
            e.printStackTrace();
            logger.log(Level.WARNING,e.getMessage() + "while loading of local config");
            //logger.log(Level.INFO,"contacting registration server");
            doRegistryUpdate();
            
            done = true ; 
            saveConfiguration(); 
            return ;
        } 
        
        if ( persistentconfig != null ) {
            config = persistentconfig ;
            if ( shouldDoUpdate(persistentconfig)){
                logger.finest("contacting registry server");
                doRegistryUpdate(); 
                saveConfiguration();    
            }       
        } else {
            // persistent config = null
            // we need to do an initial contact
            doRegistryUpdate();
            saveConfiguration();
        }
        
        
        done = true ;         
    }
    
    /** Determine if registration server should be contacted */
    
    private boolean shouldDoUpdate(RegistryConfiguration persistentconfig){
        
        String behave = config.getUpdateBehave();
        logger.finest("behave: " + behave);	    
        //behave="always";
        if (! behave.equals("day")) {
            // test if we did already an update today
            // if not do update now
            logger.finest("registry contact behave is " + behave + " doing update now");
            return true ;
        }
        
        if ( forceUpdate ) { return true; }
        
        Date now = new Date();
        Date lastContact = persistentconfig.getContactDate();
        long timenow     = now.getTime();
        long timelast    = lastContact.getTime();
        
        if (( timenow - timelast ) < TIME_BETWEEN_UPDATES ) {
            logger.finest( "timenow " + timenow + " timelast " + timelast + " < " + TIME_BETWEEN_UPDATES);
            return true ;
        } else { 
            logger.finest("last update < 1 day, using saved config");
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
        return false;
    }
    
    
    private  void doRegistryUpdate() 
    	throws ConfigurationException{
        // show dialog
        showProgressBar();
        
        RegistryConfiguration oldconfig = config;
        
        // not finished flag for this thread..
        done = false;

        if ( registryArray == null ) {
	    throw new ConfigurationException("not registration server has been provided!");
	}

        boolean found = false;
        for ( int i =0 ; i < registryArray.length; i++){
            
            // no need to contact a second registry, if the first one was successfull..
            if (found) break ;
            
            URL registryurl = registryArray[i];
            try {
                config = doRegistryUpdate(registryurl);
                config.setRegistryUrl(registryurl);
                Date now = new Date();
                config.setContactDate(now);
                
                found = true;
            } catch (ConfigurationException e){
                // there is a problem with this registry, try another one...
                logger.info("a problem occured while contacting registry - using backup registry");
            }
        }
        
        if ( ! found ){
            logger.warning("problem contacting registry, using locally cached config");
            config = oldconfig;
          //  notifyAll();
            throw new ConfigurationException("Could not contact any Registryation service!");
        }
        
        //      copy old local servers to new config ...
        if ( oldconfig != null ) {
            config.setUpdateBehave(oldconfig.getUpdateBehave());
            config.setPDBFileExtensions(oldconfig.getPDBFileExtensions());
            
            List localservers = oldconfig.getLocalServers();
            for ( int i = 0 ; i < localservers.size() ; i++ ) {
                
                SpiceDasSource ds = (SpiceDasSource) localservers.get(i);
                logger.finest("adding localserver to new config " + ds.getUrl());
                config.addServer(ds);
            }
            //logger.finest("adding registry "+ REGISTRY.toString());
            
        }
        
        disposeProgressBar();
        
        // we are finished with loading data...
        done = true ; 
        //notifyAll();
        
        
        
    }
    
    /** contact DAS registry and update sources ... */
    public  RegistryConfiguration doRegistryUpdate(URL registryurl)
    throws ConfigurationException
    {
           
        
        logger.log(Level.INFO,"contacting DAS registry server at: " +registryurl);
        
        //RegistryConfiguration oldconfig = config;
        
        RegistryConfiguration myconfig = new RegistryConfiguration();
        DasRegistryAxisClient rclient;
        try {
            rclient = new DasRegistryAxisClient(registryurl);
        } catch (Exception e) {
            logger.log(Level.WARNING,e.getMessage());   
            throw new ConfigurationException("Could not init client to contact registration service " + e.getMessage());
        }
        
        String[] capabs ;
        try {
             capabs = rclient.getAllCapabilities();
        } catch (Exception e){
            logger.log(Level.WARNING,e.getMessage());
            throw new ConfigurationException("Could not retreive all capabilities from registraion server");
        }
        myconfig.setCapabilities(capabs);
        //Date d = new Date();
        //config.setContactDate(d);
        DasSource[]sources = null;
        try {
            sources = rclient.listServices();
        }
        catch (Exception e){
            logger.log(Level.WARNING,e.getMessage());          
            throw new ConfigurationException(e.getMessage());
        }
        
        if ( sources==null) {
            
            logger.log(Level.WARNING,"Could not connect to registration service at " + registryurl);
           
            throw new ConfigurationException("Could not connect to registration service at " + registryurl);
        }
        logger.log(Level.CONFIG,"found "+sources.length+" servers"); 
        //config = getDasServers(sources); 
        if ( sources.length < 1){
            logger.log(Level.WARNING,"Did not get any DasSource from registration service at " + registryurl);
            
             throw new ConfigurationException("Did not get any DasSource from registration service at " + registryurl);
          
        }
        //ArrayList servers = new ArrayList();
        
        for (int i = 0 ; i < sources.length; i++) {
            DasSource s = sources[i];	    
            SpiceDasSource sds = new SpiceDasSource();
            sds.fromDasSource(s);
            //logger.info(" RegistryIO  go dassource " +sds.getNickname() );
            myconfig.addServer(sds);
        }
        return myconfig;
        
    }
    
    
    private void showProgressBar(){
        
        
        
        progressFrame = new JFrame("contacting registration service");
        progressFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        /*progressFrame.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent evt) {
         Frame frame = (Frame) evt.getSource();
         frame.setVisible(false);
         frame.dispose();
         }
         });
         */
        
        ImageIcon icon = ConfigGui.createImageIcon("spice.png");
        if (icon != null) {
            progressFrame.setIconImage(icon.getImage());
        }
        JFrame.setDefaultLookAndFeelDecorated(false);
        //progressFrame.setUndecorated(true);
        
        JPanel panel = new JPanel();
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
        progressFrame.setVisible(false);
        progressFrame.dispose();
    }
    
    /** write back the config to the SPICE application */
    public void saveConfiguration() {
        
        logger.finest("trying PersistentConfig");
        try {
            PersistentConfig ps = new PersistentConfig();
            ps.save(config);
        }
        catch ( javax.jnlp.UnavailableServiceException e) {
            logger.log(Level.WARNING,e.getMessage() + "while saving config locally");
        }
        
	notifyConfigListeners();
        
    }
    
    /** returns the Config for SPICE */
    // obsolete!
    /*
    public RegistryConfiguration getConfiguration() {
        while (! isDone()) {	  
            try {
                wait(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
                done = true ;
            }
        }
        return config ; 
    }
    */
    /** set config fromoutside 
     * 
     */
    public void setConfiguration(RegistryConfiguration regi) {
        config = regi;	
    }
 
}


