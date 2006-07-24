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
 * Created on Aug 23, 2005
 *
 */
package org.biojava.spice.server;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.Arrays;
//import java.util.HashMap;
import java.util.Iterator;
//import java.util.Map;
import java.util.logging.Logger;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import org.biojava.spice.SpiceStartParameters;
//import org.biojava.spice.Config.ConfigurationException;

//import com.sun.jdi.InvocationException;

/**
 * @author Andreas Prlic
 *
 */
public class SpiceClient {
    
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    public final static int SPICE_SUBMITTED =  1;
    public final static int NO_SPICE_FOUND  = -1;
    
    /**
     * 
     */
    public SpiceClient() {
        super();
       
    }
    
    
    /** try to connect to SPICEPORT and see if already an instance of SPICE is running there
     * 
     * @param params parameters
     * @return intstatuscode
     * @throws IOException
     */
    public int send(SpiceStartParameters params) 
    throws IOException{
        
        Socket spiceSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        
        try {
            spiceSocket = new Socket("localhost",SpiceServer.SPICEPORT);
            out = new PrintWriter(spiceSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
                    spiceSocket.getInputStream()));
        } catch (UnknownHostException e) {
            logger.info("Don't know about host: localhost.");
            return NO_SPICE_FOUND;
            
        } catch (IOException e) {
            logger.info("Couldn't get I/O for "
                    + "the connection to: localhost.");
            return NO_SPICE_FOUND;
        }
        
        String initCmd = "SPICE: init";
        logger.info("sending request " + initCmd);
        out.println(initCmd);
        String response = in.readLine();
        logger.info("got response " + response);
        
        // set all the parameters ...
           
        response = setSpiceParameters(out,in, params);
        if (! response.equals(SpiceProtocol.SPICE_OK)){
            return NO_SPICE_FOUND;
        }
        
        String sendRequest = "SPICE: load "+ params.getCodetype() + " " + params.getCode();
        logger.info("sending request "+sendRequest);
        out.println(sendRequest);
        response = in.readLine();
        logger.info("got response " + response);

        String closeCommand = "SPICE: close";
        out.println(closeCommand);
        
        out.close();
        in.close();
        spiceSocket.close();
        
        if ( response.equals(SpiceProtocol.SPICE_OK)){
            return SPICE_SUBMITTED;
        } 
        
        return NO_SPICE_FOUND;
    }
    
    
    private String sendParameter(PrintWriter out, BufferedReader in, String name, String value){

        String command = "SPICE: param " + name.trim();
        command += " " + value.trim(); 
        //command += ";";
          
         logger.info("sending request "+command);
          out.println(command);
          try {
              String response = in.readLine();
              logger.info("got response " + response);
              if (! response.equals(SpiceProtocol.SPICE_OK)){
                  return SpiceProtocol.SPICE_WHAT;
                          
              }
          } catch (IOException e){
              return SpiceProtocol.SPICE_WHAT;
          }
          
          return SpiceProtocol.SPICE_OK;
        
    }
    
    /** set all the parameters from this spice in the already running instance...
     * 
     * @param out
     * @param in
     * @param params
     * @return a string that gives either OK or WHAT or ERROR status
     */
    private String setSpiceParameters(PrintWriter out, BufferedReader in, SpiceStartParameters params){
        
        BeanInfo bi;
        try {
            bi = Introspector.getBeanInfo(SpiceStartParameters.class);
        } catch (Exception ex) {
            logger.info("Couldn't get information for target bean " + ex.getMessage());
            return SpiceProtocol.SPICE_WHAT;
        }
        
        //Map propertiesByName = new HashMap();
        for (Iterator pi = Arrays.asList(bi.getPropertyDescriptors()).iterator(); pi.hasNext(); ) {
            PropertyDescriptor pd = (PropertyDescriptor) pi.next();
            //propertiesByName.put(pd.getName(), pd);
            //pd.getWriteMethod().invoke(params, new Object[] {propVal});
            if ( pd.getName().equals("class")){
                continue;
            }
            System.out.println(pd.getName());
            logger.info("current: >" + pd.getName() + "< " );
            // this part is a little complicated because we need to serialize the array ..
            if ( pd.getName().equals("backupRegistry")){
                String[] strs = params.getBackupRegistry();
                for (int i=0;i<strs.length;i++){
                    String str = strs[i];
                    String resp = sendParameter(out,in,"backupRegistry",str);
                    if ( ! resp.equals(SpiceProtocol.SPICE_OK)){
                        return SpiceProtocol.SPICE_ERROR;
                    }
                }
                continue;
            }
            
            
            Object value = null;
            try {
                value = pd.getReadMethod().invoke(params,new Object[]{});
            } catch (InvocationTargetException e){
                e.printStackTrace();
                logger.warning(e.getMessage());
            } catch (IllegalAccessException e){
                e.printStackTrace();
                logger.warning(e.getMessage());
            }
            
            if ( value == null ) {
                System.out.println(pd.getName()+ " is null");
                logger.info(pd.getName() + "is null");
                continue;
            }
            logger.info("sending >" + pd.getName() + "< " + value);
            String resp = sendParameter(out,in,pd.getName(),value.toString());
            if ( ! resp.equals(SpiceProtocol.SPICE_OK)){
                return SpiceProtocol.SPICE_ERROR;
            }
        }
            
        return SpiceProtocol.SPICE_OK;
    }
    
}
