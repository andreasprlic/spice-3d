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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.biojava.spice.SPICEFrame;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.SpiceStartParameters;
import org.biojava.spice.config.SpiceDefaults;

import java.util.logging.*;


/** a class that defines the protocol how two
 * instances of spice can communicate with each other.
 * 
 * @author Andreas Prlic
 *
 */
public class SpiceProtocol {
    // the response
    public final static String SPICE_OK         = "SPICE: OK";
    public final static String SPICE_WHAT       = "SPICE: WHAT?";
    public final static String SPICE_ERROR      = "SPICE: ERROR";
    public final static String NO_RUNNING_SPICE = "NO SPICE!";
    
    Logger logger = Logger.getLogger("org.biojava.spice");
    
    SpiceStartParameters params;
    BeanInfo bi;
    Map propertiesByName;
    /**
     * 
     */
    public SpiceProtocol() {
        super();
        params = new SpiceStartParameters();
        try {
            bi = Introspector.getBeanInfo(SpiceStartParameters.class);
        } catch (Exception e){
            e.printStackTrace();
            //logger.log("could not init SpiceStartParameters bean");
            bi = null;
        }
        propertiesByName = new HashMap();
        for (Iterator pi = Arrays.asList(bi.getPropertyDescriptors()).iterator(); pi.hasNext(); ) {
            PropertyDescriptor pd = (PropertyDescriptor) pi.next();
            propertiesByName.put(pd.getName(), pd);
        }
        
    }
    
  
    
    
    /** The input should look like:
     * 
     * SPICE: test check if SPICE is there.
     * SPICE: param <b>parameterName<b> <i>parameterValue</i> 
     * set display parameters. See SpiceStartParameters.java for arguments that SPICE understands
     *              currently this are
     *              <ul><li>
     *              display</li>,<li>displayLabel</li>,
     *              <li>rasmolScript</li>,<li>seqSelectStart</li>, 
     *              <li>seqSelectEnd</li>, <li>pdbSelectStart</li>,<li>pdbSelectEnd</li>,
     *              <li>message</li>, <li>messageWidth</li>, <li>messageHeight</li>) ;    
     
     * SPICE: load Type Accessioncode
     * 
     * where Type is the type of accession code ( 'UniProt' or 'PDB' )
     * Accessioncode is the accession code (e.g. '5pti' or 'P50225' ) 
     * 
     * 
     * the response can have three types of responses:
     *  SPICE_OK ... successfully sent to SPICE
     *  SPICE_WHAT ... I do not understand what you say (you do not stick to this protocol)
     *  SPICE_ERROR .. something went wrong 
     *  NO_RUNNING_SPICE ... there is no spice running 
     * 
     * @param str
     * @param server
     * @return a string
     */
    public String processInput(String str,SpiceServer server){
        
        if ( str.length() < 11) {
            logger.warning("do not understand command >" + str + "<");
            return SPICE_WHAT;
        }
        
        try {
            String start = str.substring(0,11);
            System.out.println(start);
            if ( str.length() > 2000){
                logger.info("too long string " + start + "...");
                return SPICE_WHAT;
            }
            if (start.equals("SPICE: test")) {
                return SPICE_OK ;
            }
            if ( start.equals("SPICE: init")){
                params = new SpiceStartParameters();    
                return SPICE_OK;
            }
            else if (str.substring(0,12).equals("SPICE: param")) {
                String[] split = str.split(" ");
                if ( split.length < 4) {
                    logger.info("message does not have right length...") ;
//                  use default settings for this parameter...
                    
                    return SPICE_OK; 
                    // use default settings for this parameter...
                    //return SPICE_WHAT;
                }
                String parameterName  = split[2];
                String parameterValue = str.substring((13 + parameterName.length()+1),str.length());
                
                testSetParameter(parameterName,parameterValue);
                
                return SPICE_OK;
            }
            else if (start.equals("SPICE: load")){
                
                // seems to be a SPICE protocol message...
                String[] split = str.split(" ");
                if ( split.length != 4) {
                    //logger.info("message does not have right length...") ;
                    return SPICE_WHAT;
                }
                String type = split[2];
                String accessionCode = split[3];
                
                //System.out.println("SpiceProtocol recieved request to display " +type+ " " + accessionCode);
                
                if ( ! SpiceDefaults.argumentTypes.contains(type))  {
                	 // what kind of argument type is this ???
                    logger.info("unknown type found : " + type);
                    return SPICE_WHAT;  
                }
                
                SPICEFrame spice = null;
                
                logger.info(" currently: " + server.nrInstances() + " spice instances");
                
                if ( server.nrInstances() == 1){               
                	spice = server.getInstance(0);
                } else if ( server.nrInstances() > 1){

                	spice = UserChoosesSpiceDialog.choose(server);

                }

                if ( params == null)
                	params = new SpiceStartParameters();
                spice.setSpiceStartParameters(params);
                
                spice.load(type,accessionCode);

                if ( spice instanceof SpiceApplication ){
                	SpiceApplication parent = (SpiceApplication) spice;
                	parent.setVisible(true);
                	parent.requestFocus();

                }
                return SPICE_OK;

                
            } else {
                logger.info("unknown SPICE command found : " + start);
                
                return SPICE_WHAT;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return SPICE_ERROR;
        }
        
        //   return SPICE_ERROR;
    }
 
    
    private void testSetParameter( String parameterName, String parameterValue) 
        throws InvocationTargetException,IllegalAccessException {
        
        //System.out.println("setting " + parameterName + " " + parameterValue);
        //logger.info("setting " + parameterName + " " + parameterValue);
        if ( parameterName.equals("backupRegistry")){
            String[] urls = new String[1];
            urls[0] = parameterValue;
            params.setBackupRegistry(urls);
        } else {
            PropertyDescriptor pd = (PropertyDescriptor) propertiesByName.get(parameterName);
            Class propType = pd.getPropertyType();
            if (propType == Integer.TYPE) {
                int intValue = Integer.parseInt(parameterValue);
                //logger.info("parsed integer " + intValue);
                pd.getWriteMethod().invoke(params, new Object[] {new Integer(intValue)});
            } 
            
            if (propType == String.class) {
                pd.getWriteMethod().invoke(params, new Object[] {parameterValue});
        
            }
        }
    }
    
}
