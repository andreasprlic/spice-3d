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
import java.net.*;
import java.util.logging.Logger;
import java.io.*;


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
     * @param codetype
     * @param accessioncode
     * @return statuscode
     */
    public int send(String codetype, String accessioncode) 
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
        
        String sendRequest = "SPICE: load "+ codetype + " " + accessioncode;
        logger.info("sending request "+sendRequest);
        out.println(sendRequest);
        String response = in.readLine();
        logger.info("got response " + response);

        out.close();
        in.close();
        spiceSocket.close();
        
        if ( response.equals(SpiceProtocol.SPICE_OK)){
            return SPICE_SUBMITTED;
        } 
        
        return NO_SPICE_FOUND;
    }
    
}
