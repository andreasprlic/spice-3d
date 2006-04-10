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

import org.biojava.spice.SPICEFrame;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.io.*;


/**
 * @author Andreas Prlic
 *
 */
public class SpiceServer {
    
    public static final int SPICEPORT = 48494;
 
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    ServerListeningThread listener;
    
    List otherSpices;
    
    /**
     * 
     */
    public SpiceServer() {
        super();
        otherSpices = new ArrayList();
        //registerInstance(spice);
        listener = new ServerListeningThread(SPICEPORT,this);
        listener.start();
    }
    
    public int nrInstances(){
        return otherSpices.size();
    }
    
    public SPICEFrame getInstance(int position){
        return (SPICEFrame)otherSpices.get(position);
    }
    
    public void registerInstance(SPICEFrame spice){
        logger.info("adding spice instance " + otherSpices.size());
        if (! otherSpices.contains(spice)) {
            otherSpices.add(spice);
        } else {
            logger.info("already known instance, not registering again");
        }
    }
    
    public void removeInstance(SPICEFrame spice){
        otherSpices.remove(spice);
        logger.info("removed spice instance ("+otherSpices.size()+" left)");
        if ( otherSpices.size() == 0){
            logger.info("no active spice left, shutting down");
            System.exit(0);
        }
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
    SpiceServer server;
    int port;
    boolean listening;
    
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    public ServerListeningThread(int port, SpiceServer server) {
        this.port = port;
        this.server = server;
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
                new SpiceMultiServerThread(serverSocket.accept(), server).start();
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

