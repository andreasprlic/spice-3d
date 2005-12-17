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
 * Created on Nov 20, 2005
 *
 */


import org.biojava.services.das.registry.DasSource;
import org.biojava.spice.das.SpiceDasSource;
import org.biojava.spice.das.SingleFeatureThread;
import org.biojava.services.das.registry.DasCoordinateSystem;
import org.biojava.services.das.registry.DasRegistryAxisClient;
import org.biojava.spice.manypanel.eventmodel.FeatureListener;
import org.biojava.spice.manypanel.eventmodel.FeatureEvent;
import java.net.URL;
import java.util.Map;
import java.util.ArrayList;

/** an example that first connects to the DAS registration server,
 * then selects all DAS-sources that are in <i>UniProt,Protein
 * sequence</i> coordinate system and then does feature requests for
 * them.
 */
public class getFeatures {


    public static void main (String[] args) {
	
	getFeatures f = new getFeatures();
	f.showExample();
	
	

    }
    
    public void showExample() {
	try {

	    // first we set some system properties
	   
	    // make sure we use the Xerces XML parser..
	    System.setProperty("javax.xml.parsers.DocumentBuilderFactory","org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
	    System.setProperty("javax.xml.parsers.SAXParserFactory","org.apache.xerces.jaxp.SAXParserFactoryImpl");

	    // if you are behind a proxy, please uncomment the following lines
	    //System.setProperty("proxySet","true");
	    //System.setProperty("proxyHost","wwwcache.sanger.ac.uk");
	    //System.setProperty("proxyPort","3128");


	    // get all das sources
	    DasSource[] sources             = contactRegistry();
	    
	    // generate a coordinate system for Uniprot:
	    DasCoordinateSystem uniprotCoords = new DasCoordinateSystem();
	    uniprotCoords.setName("UniProt");
	    uniprotCoords.setCategory("Protein sequence");
	    
	    // now filter out the das sources that speak the DAS -
	    // features command and that take uniprot coord.sys.
	    
	    DasSource[] uniprotSources = getServers(sources,"features",uniprotCoords);
	    
	    
	    // before continuing we need to convert the to SpiceDasSources
	    SpiceDasSource[] spiceSources   = convertToSpiceDasSource(sources);
	    

	    // we want to get the features for this UniProt entry:
	    String accessionCode = "P50225";
	    
	    doFeatureRequests(accessionCode,spiceSources);
	    
	    // do a loop over 10 seconds. the das sources really should respond during this time.
	    int i = 0 ;
	    while (true){
		System.out.println(i  + " seconds have passed");
		i++;	
		Thread.sleep(1000);
		if ( i > 10) {
		    System.err.println("We assume that das source do not take more than 10 seconds to provide a response.");
		    System.exit(1);
		}	    
	    }	
	} catch (Exception e){
	    e.printStackTrace();
	}
    }

    /** contacts the DAS registry and gets a list of all available DAS
	sources 
	@see contactRegistry.java
    */

    private DasSource[] contactRegistry() throws Exception{
	String registrylocation = "http://servlet.sanger.ac.uk/dasregistry/services/das_registry";

	URL registryURL = new URL(registrylocation);
	DasRegistryAxisClient client = new DasRegistryAxisClient(registryURL);
	
	DasSource[] sources = client.listServices();
	
	return sources;
    }


    
    /** convert the DasSources into SpiceDasSources objects that are used for spice */
    private SpiceDasSource[] convertToSpiceDasSource(DasSource[] sources){

	SpiceDasSource[] spiceSources = new SpiceDasSource[sources.length];
	for (int i=0; i < sources.length;i++ ) {
	    spiceSources[i] = SpiceDasSource.fromDasSource(sources[i]);
	}

	return spiceSources;
    }
    
    /** get the subset that takes feature responsens and speaks in
     * UniProt,Protein sequence coordinate system
     */

    private DasSource[] getServers(DasSource[] allsources, String capability, DasCoordinateSystem uniprotCoords){
	ArrayList retservers = new ArrayList();
        for ( int i = 0 ; i < allsources.length ; i++ ) {
	    DasSource ds = (DasSource)allsources[i];
	    if ( hasCoordSys(uniprotCoords,ds)) {
		if ( hasCapability(capability,ds)){
		    // this DasSource fits the requested criteria
		    retservers.add(ds);
		}
	    }
	}
	return (DasSource[]) retservers.toArray(new DasSource[retservers.size()]);
    }



    /** test if a DasSource supports a particular coordinate system */

    private boolean hasCoordSys(DasCoordinateSystem coordSys,DasSource source ) {
        DasCoordinateSystem[] coordsys = source.getCoordinateSystem() ;
        for ( int i = 0 ; i< coordsys.length; i++ ) {
            String c = coordsys[i].toString();           
            if ( c.equals(coordSys) ) {                
                return true ;
            }            
        }
        return false ;        
    }
    
    /** test if a das source has a particular capability */
    private boolean hasCapability(String capability, DasSource ds){
        String[] capabilities = ds.getCapabilities() ;
        for ( int c=0; c<capabilities.length ;c++) {
            String capabil = capabilities[c];
            if ( capability.equals(capabil)){
                return true;
            }
        }
        return false;
    }
    
    /** request the features for each of this DAS sources */
    private void doFeatureRequests(String accessionCode, SpiceDasSource[] sources){
	for ( int i = 0 ; i< sources.length;i++ ) {
	    requestFeatures(accessionCode,sources[i]);
	}
	
    }
   
    /** request the features for a singe das source.
     */
    private void requestFeatures(String accessionCode, SpiceDasSource source) {
	
	// that is the class that listens to features
	FeatureListener listener = new MyListener();

	// now create the thread that will do the DAS requests
	SingleFeatureThread thread = new SingleFeatureThread(accessionCode, source);
	
	// and register the listener
	thread.addFeatureListener(listener);

	// launch the thread
	thread.start();
	
    }

    class MyListener 
	implements FeatureListener{
	public synchronized void newFeatures(FeatureEvent e){
	    SpiceDasSource ds = e.getDasSource();
	    Map[] features = e.getFeatures();

	    System.out.println("das source " + ds.getNickname() + " returned " + features.length +" features");
	}
	public void featureSelected(FeatureEvent e){}
    }
}
