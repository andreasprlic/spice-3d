/*
 *                    BioJava development code
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
 * Created on 20.09.2004
 * @author Andreas Prlic
 *
 */


package org.biojava.spice ;

// to get config file via http
import java.net.HttpURLConnection ;
import java.net.URL;
import java.io.IOException ;

import java.util.HashMap   ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.List ;
// for DAS registration server:
import org.biojava.services.das.registry.*;


// for GUI;
import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JTable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;


/** a class to retreive the configuration from a DAS registry server.
 */
public class RegistryConfiguration 
    extends Thread
    
{

    URL REGISTRY  ;
	
    Map config    ;
    boolean done ;
    public RegistryConfiguration ( URL registryurl) {
	REGISTRY = registryurl ;
	done = false ;
    }
    public boolean isDone(){
	return done ;
    }

    public void run(){
	getData();
    }

    private synchronized void getData(){
	System.out.println("DAS REgistry server config thread loadData");
	done = false ;
	System.out.println("contacting DAS registry server at: " +REGISTRY);
	DasRegistryAxisClient rclient = new DasRegistryAxisClient(REGISTRY);
	
	DasSource[] sources = rclient.listServices();
	
	System.out.println("found "+sources.length+" servers");
	config = getDasServers(sources);
	done = true ;
	notifyAll();

	
    }
    
    /** returns the Config for SPICE */
    public Map getConfiguration() {
	while (! isDone()) {	  
	    try {
		wait(30);
	    } catch (InterruptedException e) {
		e.printStackTrace();
		done = true ;
	    }
	}
	return config ; 
    }

    private Map getDasServers(DasSource[] sources){
	HashMap   configr           = new HashMap()   ;
	ArrayList featservers       = new ArrayList() ;
	ArrayList pdbresfeatservers = new ArrayList() ;
	Map   structureserver       = null;
	Map   sequenceserver        = null;
	Map   alignmentserver       = null;

	
	for ( int i=0;i<sources.length;i++) {
	    DasSource source = sources[i] ;
	    String[] capabilities = source.getCapabilities() ;
	    System.out.println(source.getUrl());
	    for ( int c=0; c<capabilities.length ;c++) {
		String capabil = capabilities[c];
		System.out.println(capabil);
		if ( capabil.equals("structure")){
		    
		    Map server = convertSource2Map(source) ;
		    structureserver = server ;
		}
		if ( capabil.equals("alignment") ){
		    if ( isSeqStrucAlignmentServer(source) ){
			Map server = convertSource2Map(source) ;
			alignmentserver = server ;
		    }
		}
		if ( capabil.equals("sequence") ){
		    
		    if ( hasCoordSys("UniProt",source)) {
			Map server = convertSource2Map(source) ;
			sequenceserver = server ;
		    }
		}

		if ( capabil.equals("features") ){

		    if ( hasCoordSys("UniProt",source)) {
			Map server = convertSource2Map(source) ;
			featservers.add(server);
		    } else if ( hasCoordSys("PDBresnum",source)) {
			Map server = convertSource2Map(source) ;
			pdbresfeatservers.add(server);
		    }
		}
	    }
	}

	configr.put("structureserver", structureserver);
	configr.put("sequenceserver", sequenceserver);
	configr.put("alignmentserver", alignmentserver);
	configr.put("featureservers",featservers);
	configr.put("pdbresfeatureservers",pdbresfeatservers);
	return configr ;
    }
    
    private Map convertSource2Map(DasSource source) {
	HashMap server = new HashMap();
	server.put("name",source.getUrl()); // for backwards compability
	server.put("url",source.getUrl());
	server.put("coordinateSystems",source.getCoordinateSystem());
	server.put("description",source.getDescription());
	server.put("adminemail",source.getAdminemail());
	server.put("capabilities",source.getCapabilities());
	return server ;
    }

   /** test if MSD mapping  - PDB - UniProt is provide */
       
    private boolean isSeqStrucAlignmentServer(DasSource source) {
	boolean msdmapping = false ;
	String[] coordsys = source.getCoordinateSystem() ;
	
	boolean uniprotflag = false ;
	boolean pdbflag     = false ;

	pdbflag     =  hasCoordSys("PDBresnum",source) ;
	uniprotflag =  hasCoordSys("UniProt",source)   ;
	
	if (( uniprotflag == true) && ( pdbflag == true)) {
	    msdmapping = true ;
	}
	return msdmapping ;
    }

    private boolean hasCoordSys(String coordSys,DasSource source ) {
	String[] coordsys = source.getCoordinateSystem() ;
	for ( int i = 0 ; i< coordsys.length; i++ ) {
	    String c = coordsys[i];
	    if ( c.equals(coordSys) ) {
		return true ;
	    }
	}
	return false ;

    }
   

    public void showConfigFrame(){
	//Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("SPICE configuration window");
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
	
        //Create and set up the content pane.
        JComponent newContentPane = new TabbedPaneDemo(config);
        newContentPane.setOpaque(true); //content panes must be opaque

        frame.getContentPane().add(new TabbedPaneDemo(config),
                                 BorderLayout.CENTER);
	
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}



class TabbedPaneDemo extends JPanel {
    static String[] colNames= new String [] {"name","url","coordinateSystems","adminemail","capabilities","description"};

    public TabbedPaneDemo(Map config) {
        super(new GridLayout(1, 1));

        JTabbedPane tabbedPane = new JTabbedPane();
        ImageIcon icon = createImageIcon("images/middle.gif");


	// Make sequence and structure Panel
	JPanel seqstrucpanel = new JPanel();
	seqstrucpanel.setLayout(new BoxLayout(seqstrucpanel, BoxLayout.Y_AXIS));	
	List sequenceservers = new ArrayList();
	sequenceservers.add(config.get("sequenceserver"));
	
	String seqdata[][] = getTabData(sequenceservers);

	//System.out.println(seqdata);
	JTable table= new JTable(seqdata,colNames);
	// Configure some of JTable's paramters
	table.setShowHorizontalLines( false );
	table.setRowSelectionAllowed( true );
	table.setColumnSelectionAllowed( true );
		
	// Add the table to a scrolling pane
	JScrollPane seqscrollPane = table.createScrollPaneForTable( table );
	seqstrucpanel.add( seqscrollPane, BorderLayout.CENTER );

	// add structure servers
	List structureservers= new ArrayList();
	structureservers.add(config.get("structureserver"));
	String strucdata[][] = getTabData(structureservers);
	JTable stable = new JTable(strucdata,colNames);
	stable.setShowHorizontalLines( false );
	stable.setRowSelectionAllowed( true );
	stable.setColumnSelectionAllowed( true );

	JScrollPane strucScrollPane = stable.createScrollPaneForTable( stable );
	seqstrucpanel.add( strucScrollPane, BorderLayout.CENTER );


	tabbedPane.addTab("Seq./Struc. ", icon, seqstrucpanel,
                          "configure sequence and structure servers");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);


	

	


	// Features
	List featureservers = (List) config.get("featureservers");
	String[][] featdata=getTabData(featureservers);
	JTable ftab = new JTable(featdata,colNames);
	// Configure some of JTable's paramters
	ftab.setShowHorizontalLines( false );
	ftab.setRowSelectionAllowed( true );
	ftab.setColumnSelectionAllowed( true );
	JScrollPane scrollFeatPane = 	ftab.createScrollPaneForTable(ftab);
	JPanel featpanel = new JPanel();
	featpanel.setLayout(new BoxLayout(featpanel, BoxLayout.Y_AXIS));
	featpanel.add(scrollFeatPane, BorderLayout.CENTER );
	
	// and structure features
	List pdbresfeatureservers = (List) config.get("pdbresfeatureservers");
	String[][] pdbresfeatdata=getTabData(pdbresfeatureservers);
	JTable pftab = new JTable(pdbresfeatdata,colNames);
	// Configure some of JTable's paramters
	pftab.setShowHorizontalLines( false );
	pftab.setRowSelectionAllowed( true );
	pftab.setColumnSelectionAllowed( true );
	JScrollPane pdbresscrollFeatPane = 	pftab.createScrollPaneForTable(pftab);
	JPanel pdbresfeatpanel = new JPanel();

	featpanel.add(pdbresscrollFeatPane, BorderLayout.CENTER );
	

        tabbedPane.addTab("Feature ", icon,featpanel ,
                          "Config Feature servers");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);


	// Alignment
	List alignmentservers = new ArrayList();
	alignmentservers.add( config.get("alignmentserver"));	
	String[][] alidata =getTabData(alignmentservers);
	JTable atab = new JTable(alidata,colNames);
	atab.setShowHorizontalLines( false );
	atab.setRowSelectionAllowed( true );
	atab.setColumnSelectionAllowed( true );
	JScrollPane scrollAliPane = 	atab.createScrollPaneForTable(atab);
	JPanel alipanel = new JPanel();
	alipanel.setLayout(new BoxLayout(alipanel, BoxLayout.Y_AXIS));
	alipanel.add(scrollAliPane, BorderLayout.CENTER );

        tabbedPane.addTab("Alignment ", icon, alipanel,
			                  "config alignment servers");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

   

        //Add the tabbed pane to this panel.
        add(tabbedPane);
        
        //Uncomment the following line to use scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = TabbedPaneDemo.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }


    public String[][] getTabData(List servers) {
	String[][] data = new String[servers.size()][7];
       	
	for ( int i =0; i< servers.size(); i++ ) {
	    Map server = (Map) servers.get(i);
	    for ( int j =0;j<colNames.length;j++){
		String colname = colNames[j];
		//System.out.println(colname);
		String s = "" ;
		if (( colname.equals("coordinateSystems")) || 
		    (colname.equals("capabilities"))) {
		    String[] stmp = (String[])server.get(colname);
		    s = "" ;
		    for ( int u = 0; u<stmp.length;u++){
			s += stmp[u]+" ";
		    }
		} else {
		    s = (String)server.get(colname);
		}
		data[i][j] = s;
	    }
	}
	return data ;
    }
}
