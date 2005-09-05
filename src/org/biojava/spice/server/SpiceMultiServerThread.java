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
 * Created on Aug 24, 2005
 *
 */
package org.biojava.spice.server;

import java.net.*;
import java.io.*;
import org.biojava.spice.*;

/**
 * @author Andreas Prlic
 *
 */
public class SpiceMultiServerThread 
extends Thread {
    
    
    private Socket socket ;
    private SPICEFrame spice;
    
    /**
     * 
     */
    public SpiceMultiServerThread(Socket socket,SPICEFrame spice) {
        super("SpiceMultServerThread");
        System.out.println("new SpiceMultiServerThread");
        this.socket=socket;
        this.spice = spice;
    }
    
    public void run() {
        
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));
            
            String inputLine, outputLine;
            SpiceProtocol kkp = new SpiceProtocol();
            //outputLine = kkp.processInput(null);
            //out.println(outputLine);
            
            String msg =in.readLine();
            
            System.out.println("received msg "+msg);
            
            outputLine = kkp.processInput(msg, spice);
            out.println(outputLine);
            
            out.close();
            in.close();
            socket.close();
            
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}