/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on 24.11.2005
 * @author Andreas Prlic
 *
 */

import java.net.URL;
import org.biojava.services.das.registry.DasRegistryAxisClient;
import org.biojava.services.das.registry.DasSource;
import org.biojava.spice.das.SpiceDasSource;



/** an example of how to contact the DAS - registration server.  This
 *  can be done as part of the dasregistry package. This example gets
 *  a list of DasSources from the registration servers.
 *  
 */

public class contactRegistry {

    public static void main (String[] args) {

	try {
	    // the DAS registration server provides a SOAP web service,
	    // which can be used to get a list of all available DAS
	    // servers. This is the location of the web service

	    String registrylocation = "http://servlet.sanger.ac.uk/dasregistry/services/das_registry";

	    URL registryURL = new URL(registrylocation);
	
	    // if you are behind a proxy, please uncomment the following lines
	    //System.setProperty("proxySet","true");
	    //System.setProperty("proxyHost","yourhosthere");
	    //System.setProperty("proxyPort","yourporthere");


	    // the DasRegistryAxisClient class provides the wrapper for
	    // the web service.
	    DasRegistryAxisClient client = new DasRegistryAxisClient(registryURL);
	
	    // and now get the list of servers
	    DasSource[] sources = client.listServices();

	    System.out.println("got " + sources.length + " das sources from the registry");


	} catch (Exception e) {
	    e.printStackTrace();
	}

    }


}
