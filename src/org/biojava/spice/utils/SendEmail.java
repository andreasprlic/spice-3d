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
 * Created on Feb 13, 2007
 *
 */
package org.biojava.spice.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import org.biojava.spice.ResourceManager;



public class SendEmail {

  //  public static String mailHost = ResourceManager.getString("org.biojava.spice.BugReportHost");


	static Logger logger      = Logger.getLogger("org.biojava.spice");
    
    public void postMail( 
            String subject,
            String message , 
            String from
            ) 
    throws IOException
 
    {
    	String br = "<br/>";
        String data = "From: " + from + br;
        data += "Subject: " + subject + br;
        data += "Message: " + message + br;
        
        String postURL = ResourceManager.getString("org.biojava.spice.BugReportPost");
       
        URL u = new URL(postURL);
        
        URLConnection conn = u.openConnection();
        conn.setDoOutput(true);
        
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
        writer.write(data);
        writer.flush();
        
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        
        String msg = "";
        String line ;
        while ( (line = reader.readLine() ) != null) {
            msg += line;
        }
        writer.close();
        reader.close();
        
        logger.info("got message " + msg);
          
    }

}
