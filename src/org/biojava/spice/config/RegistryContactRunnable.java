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
 * Created on Jul 29, 2006
 *
 */
package org.biojava.spice.config;

import java.net.Authenticator;

import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.bio.program.das.dasalignment.DASException;
import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.das2.Das2Source;
import org.biojava.dasobert.das2.DasSourceConverter;
import org.biojava.dasobert.das2.io.DasSourceReaderImpl;
import org.biojava.dasobert.dasregistry.Das1Source;
import org.biojava.dasobert.dasregistry.DasSource;


class RegistryContactRunnable implements Runnable {
    
    static Logger logger      = Logger.getLogger(SpiceDefaults.LOGGER);
    
    URL[] registryArray  ;
    RegistryConfiguration config;
    List configListeners;
    
    public RegistryContactRunnable(URL[] registryArray, RegistryConfiguration oldConfig){
        config = oldConfig;
        this.registryArray = registryArray;
        configListeners = new ArrayList();
    }
    
    public void run() {
        
        RegistryConfiguration oldconfig = config;
        
        // not finished flag for this thread..
        
        
        if ( registryArray == null ) {
            logger.severe("no registration server has been provided!");
            return;
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
            logger.severe("Could not contact any Registryation service!");
        }
        
        //      copy old local servers to new config ...
        if ( oldconfig != null ) {
            config.setUpdateBehave(oldconfig.getUpdateBehave());
            config.setPDBFileExtensions(oldconfig.getPDBFileExtensions());
            
            
            copyOldServerStatus(config,oldconfig);
            
            
            
            List localservers = oldconfig.getLocalServers();
            for ( int i = 0 ; i < localservers.size() ; i++ ) {
                
                SpiceDasSource ds = (SpiceDasSource) localservers.get(i);
                //logger.finest("adding localserver to new config " + ds.getUrl());
                config.addServer(ds);
            }
            //logger.finest("adding registry "+ REGISTRY.toString());
            
        }
        
        
        javax.swing.SwingUtilities.invokeLater( new Runnable() {
            
            public void run() {
                notifyConfigListeners();
            }
        });
        
    }
    
    public void addConfigListener(ConfigurationListener listener){
        logger.finest("adding new config listener");
        configListeners.add(listener);
    }
    
    /** tell all configListeners that a new config has been loaded */
    private void notifyConfigListeners(){
        logger.finest("notifying config listeners");
        
        Iterator iter = configListeners.iterator();
        while (iter.hasNext()){
            ConfigurationListener listener = (ConfigurationListener)iter.next();
            listener.newConfigRetrieved(config);
            
        }
        config = null;
    }
    
 
    /** copies enabled/disabled status from old config to new config...
     * 
     * @param newc
     * @param oldc
     */
    private void copyOldServerStatus(RegistryConfiguration newc, RegistryConfiguration oldc){

        // copy old server status to new servers ...
        List allservers = oldc.getAllServers();
        Iterator itera = allservers.iterator();
        List newservers = newc.getAllServers();
        
        while (itera.hasNext()){
            SpiceDasSource oldds = (SpiceDasSource)itera.next();
            Iterator iterb = newservers.iterator();
            while (iterb.hasNext()){
                SpiceDasSource newds = (SpiceDasSource)iterb.next();
                if ( newds.getUrl().equals(oldds.getUrl())){
                    newds.setStatus(oldds.getStatus());
                    break;
                }
            }            
        }
        
    }
    
    /** contact DAS registry and update sources ... */
    private  RegistryConfiguration doRegistryUpdate(URL registryurl)
    throws ConfigurationException
    {
        
        
        logger.log(Level.INFO,"contacting DAS directory server at: " +registryurl);
        
        //RegistryConfiguration oldconfig = config;
        
        RegistryConfiguration myconfig = new RegistryConfiguration();
     
        DasSource[] sources = new DasSource[0];
        try {   
            logger.info("doing new DAS2 style request");
            sources = getDas1Sources(registryurl);
            logger.info("finding " + sources.length + " servers");
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
            
            //DasCoordinateSystem[] dcs = s.getCoordinateSystem();
//            for (int j=0;j< dcs.length;j++){
//                System.out.println(dcs[j].toString());
//            }
            
            SpiceDasSource sds = new SpiceDasSource();
            sds = SpiceDasSource.fromDasSource(s);
            //logger.info(" RegistryIO  go dassource " +sds.getCoordinateSystem().length +
            //        " " + sds.getNickname() + sds.getUrl() );
            //dcs = sds.getCoordinateSystem();
            //for (int j=0;j< dcs.length;j++){
            //    System.out.println(dcs[j].toString());
            //}
            myconfig.addServer(sds);
        }
        return myconfig;
        
    }
    
    
  /*  public class MyAuthenticator extends Authenticator {
        // This method is called when a password-protected URL is accessed
        protected PasswordAuthentication getPasswordAuthentication() {
        	logger.warning("using hard coded CASP authentication");
            // Get information about the request
            //String promptString = getRequestingPrompt();
            //String hostname = getRequestingHost();
            //InetAddress ipaddr = getRequestingSite();
            //int port = getRequestingPort();
             
            // Get the username from the user...
            String username = "";
    
            // Get the password from the user...
            String password = "";
    
            // Return the information
            return new PasswordAuthentication(username, password.toCharArray());
        }
    }*/

    
    public Das1Source[] getDas1Sources(URL url) throws MalformedURLException, DASException{
        
        DasSourceReaderImpl reader = new DasSourceReaderImpl();
       
        logger.info("reading " + url);
       //logger.warning("remove hard coded name / password for CASP!");
        
        
       //Authenticator.setDefault(new MyAuthenticator());
        
        DasSource[] sources = reader.readDasSource(url);
        
        List das1sources = new ArrayList();
        for (int i=0;i< sources.length;i++){
            DasSource ds = sources[i];
            //System.out.println(ds);
            if ( ds instanceof Das2Source){
                //System.out.println("das2source");
                Das2Source d2s = (Das2Source)ds;
                if (d2s.hasDas1Capabilities()){
                    Das1Source d1s = DasSourceConverter.toDas1Source(d2s);
                    das1sources.add(d1s);
                }
                    
            } else if ( ds instanceof Das1Source){
                //logger.info("das1source");
                das1sources.add((Das1Source)ds);
            }
        }
        
        return (Das1Source[])das1sources.toArray(new Das1Source[das1sources.size()]);
        
        
        
    }
}