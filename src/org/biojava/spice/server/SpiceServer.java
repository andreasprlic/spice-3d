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

import org.biojava.spice.*;
import java.net.*;
import java.util.logging.Logger;
import java.io.*;


/**
 * @author Andreas Prlic
 *
 */
public class SpiceServer {
    
    public static final int SPICEPORT = 48494;
    
    ServerListeningThread listener;
    /**
     * 
     */
    public SpiceServer(SPICEFrame spice) {
        super();
        
        listener = new ServerListeningThread(SPICEPORT,spice);
        listener.start();
    }
    
    /** stop listening at SPICEPORT for incoming connections ... */
    public void destroy(){
        listener.setListening(false);
        listener.destroy();
    }
    
}

/** create a socket that listens on SPICEPORT and listen for communication
 * 
 * @author Andreas Prlic
 *
 */
class ServerListeningThread extends Thread {
    ServerSocket serverSocket;
    Socket clientSocket ;
    SPICEFrame spice;
    int port;
    boolean listening;
    
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    public ServerListeningThread(int port, SPICEFrame spice) {
        this.port = port;
        this.spice = spice;
        serverSocket = null;
        clientSocket = null;
        listening = false;
    }
    public void run(){
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            logger.warning("Could not listen on port: "+port);
            
        }
        listening = true;
        
        try {
            while (listening)
                new SpiceMultiServerThread(serverSocket.accept(), spice).start();
        } catch (IOException e){
            logger.warning("something went wrong whith SpiceMultiServerThread " + e.getMessage() );
            e.printStackTrace();
        }
        System.out.println("server stopped.");
    }
    
    
    
    public synchronized void setListening(boolean flag){
        listening = flag;
        
    }
    
    public void destroy() {
        listening = false;
        //super.destroy();
        
    }
}

