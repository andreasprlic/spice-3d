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
 * Created on 06.10.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.spice ;



// for protein 3D stuff
import org.biojava.bio.structure.*;
import org.biojava.bio.structure.io.*;

// xml file parsing
import org.xml.sax.*;
import javax.xml.parsers.*;
import org.biojava.utils.xml.*;
import org.w3c.dom.*;

// some utils 
import java.io.InputStream ;
import java.util.Properties;
import java.util.HashMap   ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;

// to get config file via http
import java.net.HttpURLConnection ;
import java.net.URL;
import java.io.IOException ;

// gui
import java.awt.Dialog          ;
import java.awt.Dimension          ;
import java.awt.Color          ;
import java.awt.MenuItem          ;
import java.awt.Menu          ;
import java.awt.MenuBar          ;
import java.awt.Graphics          ;
import java.awt.Frame          ;
import java.awt.Button          ;
import java.awt.Panel          ;
import java.awt.BorderLayout       ;
import java.awt.FlowLayout ;
import java.awt.Event ;
import java.awt.TextField ;


import java.awt.event.*    ;
import javax.swing.text.Document ;
import javax.swing.text.Element ;
import javax.swing.JSplitPane ;
import javax.swing.JFrame  ;
import javax.swing.JPanel  ;
import javax.swing.BoxLayout;
import javax.swing.JList       ;
import javax.swing.ListCellRenderer ;
import javax.swing.JScrollPane ;
import javax.swing.DefaultListModel   ;
import javax.swing.JTextField   ;
import javax.swing.event.ListSelectionListener  ;
import javax.swing.event.ListSelectionEvent ;    
//import javax.swing.event.CaretEvent;
//import javax.swing.event.CaretListener;

// menu
import javax.swing.JMenuBar    ;
import javax.swing.JMenuItem   ;

// features
import java.net.URL        ;
import java.util.Iterator  ;

// text coloring 
import javax.swing.JTextPane   ;
import javax.swing.text.*;

// for DAS registration server:
//import org.biojava.services.das.registry.*;

public class SpiceApplication 
    extends  JFrame
    implements SPICEFrame 
{

    //public static final String CONFIG_FILE = "config.xml";
    URL CONFIG_URL      ; 
    URL REGISTRY_URL    ;

    static String XMLVALIDATION = "false" ;   
    static String INIT_SELECT = "select all; cpk off ; wireframe off ; backbone on ; colour chain;select not protein and not solvent;spacefill 2.0;";
    RegistryConfiguration config      ;
    Structure structure ; 
    String pdbcode      ;
    String pdbcode2     ; // only set if displaying structure alignments 

    int currentChain = -1 ;
    HashMap memoryfeatures; // all features in memory
    List features ;    // currently being displayed 


    //  the GUI:
    //JFrame frame                  ;
    //Panel mainPanel               ;
    //GridBagLayout gridbag         ;
    //GridBagConstraints c          ;
    //SequenceDASPanel seqDasPanel  ;    
    StructurePanel structurePanel ;    
    JTextField seq_pos ;
    JList ent_list;   // list available chains
    SeqFeatureCanvas dascanv ;
    JScrollPane dasPanel ;
    JPanel leftPanel ;
    JSplitPane sharedPanel;
    JSplitPane mainsharedPanel;

    JScrollPane seqScrollPane ;
    JSplitPane  seqSplitPane  ;
    SeqTextPane seqField      ;
    
    JMenuBar menuBar ;
    JTextField getCom ;

    JTextField strucommand  ; 

    String[] txtColor     ;
    Color[] entColors ;
    Color oldColor ; 
    boolean first_load ;

    boolean structureAlignmentMode ;

    MenuBar  menu ;
    MenuItem exit ;
    MenuItem props ;
    MenuItem reset ;
    MenuItem aboutspice ;
    MenuItem aboutdas   ;
    MenuItem openpdb    ;


    SpiceApplication(String pdbcode_, URL config_url, URL registry_url) {
	super();

	System.setProperty("XMLVALIDATION",XMLVALIDATION);
	int timeout = 10000;
	System.out.println("setting timeouts to " + timeout) ;
	System.setProperty("sun.net.client.defaultConnectTimeout", ""+timeout);
	System.setProperty("sun.net.client.defaultReadTimeout", ""+timeout);


	CONFIG_URL   = config_url ;
	REGISTRY_URL = registry_url ;
	
	// first thing is to start communication

	RegistryConfigIO regi = new RegistryConfigIO (REGISTRY_URL);
	regi.run();

	structure = null ;
	pdbcode = pdbcode_ ;
	pdbcode2 = null ;
	txtColor = new String[7] ;
	txtColor[0]="blue";
	txtColor[1]="pink";
	txtColor[2]="green";
	txtColor[3]="magenta";
	txtColor[4]="orange";
	txtColor[5]="pink";
	txtColor[6]="cyan";


	entColors = new Color [7];
	entColors[0] = Color.blue;
	entColors[1] = Color.pink;
	entColors[2] = Color.green;
	entColors[3] = Color.magenta;
	entColors[4] = Color.orange;
	entColors[5] = Color.pink;
	entColors[6] = Color.cyan;


	first_load = true ;

	structureAlignmentMode = false ;

	this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));


	// add the Menu
	menu = new MenuBar();
	this.setMenuBar(menu);
	Menu file = new Menu("File");
	Menu help = new Menu("Help");
	menu.add(file);
	menu.add(help);

	openpdb = new MenuItem("open PDB");
	exit  = new MenuItem("Exit");
	props = new MenuItem("Properties");
	reset = new MenuItem("Reset");
	//exit.addActionListener(this);
	file.add(openpdb);
	file.add(reset);
	file.add(props);
	file.add(exit);
	
	aboutspice = new MenuItem("About SPICE");
	aboutdas = new MenuItem("About DAS");

	//aboutspice.addActionListener(this);
	//aboutdas.addActionListener(this);
	help.add(aboutspice);
	help.add(aboutdas);
	
	
	
	
	// init Seqouece position
	seq_pos = new JTextField();
	seq_pos.setForeground(new Color(255, 255, 255));
	seq_pos.setBackground(new Color(0, 0, 0));
	seq_pos.setSize(700, 30);
	seq_pos.setMaximumSize(new Dimension(Short.MAX_VALUE,30));

	this.getContentPane().add(seq_pos,BorderLayout.NORTH);


	showStatus("contacting DAS registry");
	config =regi.getConfiguration();
	if ( config == null ) {
	    String msg = "Unable to contact DAS registration service, can not continue!" ;
	    seq_pos.setText(msg);
	    System.err.println(msg);
	    return ;
	    
	}
	/// init Structure Panel
	structurePanel        = new StructurePanel();
	//structurePanel.setLayout(new BoxLayout(structurePanel, BoxLayout.X_AXIS));
	structurePanel.setPreferredSize(new Dimension(700, 700));
	structurePanel.setMinimumSize(new Dimension(200,200));

	//this.add(structurePanel,BorderLayout.CENTER);




	DefaultListModel model = new DefaultListModel();
	model.add(0,"loading...");
	ent_list=new JList(model);
	EntListCommandListener entact = new EntListCommandListener(this);
	ent_list.addListSelectionListener(entact);
	ent_list.setPreferredSize(new Dimension(30,30));
	ent_list.setSize(30,30);

	//ent_list.setFixedCellWidth(20);
	//JScrollPane scrollingList = new JScrollPane(ent_list);
	//ent_list.setSize(30,180);
	//ent_list.setFixedCellWidth(40);
	//ent_list.setFixedCellHeight(18);
	//ent_list.addItemListener(entact);
			
	//getCom = new JTextField(1);
	//TextFieldListener txtlisten = new TextFieldListener(this,getCom);
	//getCom.addActionListener(txtlisten);
	//getCom.setMinimumSize(new Dimension(Short.MIN_VALUE,10));
	//

    	leftPanel = new JPanel() ;
	leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

	leftPanel.add(ent_list,BorderLayout.NORTH);
	//leftPanel.add(getCom,BorderLayout.SOUTH);
	leftPanel.setMinimumSize(  new Dimension(30,30));
	leftPanel.setPreferredSize(new Dimension(30,30));
	leftPanel.setMaximumSize(  new Dimension(40,30));
	leftPanel.setSize(30,30);
	//ent_list.repaint();

	
	//ScrollPane scroll = new ScrollPane();

	
	// init dascanv
	dascanv=new SeqFeatureCanvas(this);       
	dascanv.setForeground(Color.blue);
	dascanv.setBackground(Color.black);
	dascanv.addMouseMotionListener(dascanv);
	dascanv.addMouseListener(dascanv);
	dascanv.setOpaque(true) ;
	//dascanv.setPreferredSize(new Dimension(200, 200));
	//dascanv.setSize(700,300);
	
	dasPanel = new JScrollPane(dascanv);
	dasPanel.setOpaque(true);
	dasPanel.setBackground(Color.black);
	//scroll.add(dascanv);

	//daspanel = new SeqPanel(this);
	//daspanel.setMinimumSize(new Dimension(100,100));

	
	sharedPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                           leftPanel, dasPanel);
	sharedPanel.setOneTouchExpandable(true);
	//sharedPanel.setDividerLocation(150);
	sharedPanel.setPreferredSize(new Dimension(200, 200));
	sharedPanel.setOpaque(true);
	sharedPanel.setResizeWeight(0);	

	seqField      = new SeqTextPane(this);
	seqField.setSize( 700, 30);
	seqField.setPreferredSize(new Dimension(700, 30));
	seqField.setMinimumSize(new Dimension(700, 30));
	seqField.addMouseMotionListener(seqField);
	seqField.addMouseListener(seqField);


	// add onMouseOver action

	seqScrollPane = new JScrollPane(seqField) ;
	seqScrollPane.setSize( 700, 30);
	seqScrollPane.setPreferredSize(new Dimension(700, 30));;
	seqScrollPane.setMinimumSize(  new Dimension(700, 30));;

	seqSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				      sharedPanel,seqScrollPane);
	
	seqSplitPane.setOneTouchExpandable(true);
	seqSplitPane.setOpaque(true);
	seqSplitPane.setResizeWeight(0.9);
	//seqSplitPane.setDividerLocation(600);
	
	//sharedPanel.setLayout(new BoxLayout(sharedPanel, BoxLayout.X_AXIS));
	//sharedPanel.add(leftPanel,BorderLayout.EAST);
	//sharedPanel.add(scroll,BorderLayout.WEST);
	//sharedPanel.add(dascanv,BorderLayout.WEST);
	//sharedPanel.add(daspanel,BorderLayout.WEST);
	//sharedPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,300));
	//this.add(sharedPanel,BorderLayout.SOUTH);

	mainsharedPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					  structurePanel,seqSplitPane);
	mainsharedPanel.setOneTouchExpandable(true);
	//mainsharedPanel.setDividerLocation(150);
	//mainsharedPanel.setPreferredSize(new Dimension(200, 200));
	mainsharedPanel.setOpaque(true);
	//mainsharedPanel.setResizeWeight(0.7);
	this.getContentPane().add(mainsharedPanel,BorderLayout.NORTH);




	strucommand = new JTextField()  ;
	strucommand.setText("enter command...");
	ActionListener listener = new StructureCommandListener(this,strucommand) ;
	strucommand.addActionListener(listener);
	strucommand.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
	this.getContentPane().add(strucommand,BorderLayout.SOUTH);

	/*
	// menu
	 menuBar = new JMenuBar();
	 this.setJMenuBar(menuBar);
	 

	 JMenu fileMenu = new JMenu("File");
	 menuBar.add(fileMenu);
	 JMenuItem configItem = new JMenuItem("Config");
	 fileMenu.add(configItem);
	 // add actionListeners
	 */

	memoryfeatures = new HashMap();
	features = new ArrayList();



	this.setTitle("SPICE") ;
	this.setSize(700, 700);
	//this.show();
	
	this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	this.setDefaultLookAndFeelDecorated(false);
	this.pack();
	this.setVisible(true);
    }


    /** Constructor for structure alignment visualization 
     */
    SpiceApplication(String pdb1, String pdb2, URL config_url, URL registry_url) {
	this(pdb1, config_url,registry_url);
	structureAlignmentMode = true ;
	pdbcode2 = pdb2 ;
	System.out.println("finished init of structure alignment");
		
    }


    public boolean isLoading() {
	return first_load;
    }

    public void show(){
	super.show();
	//System.out.println("SHOW: getting Structure data from new thread");
	// and now load data ...

	
	if ( ! structureAlignmentMode ) {
	    System.out.println("not in alignment mode");
	    getStructure(pdbcode);
	} else {
	    showStatus("Loading...Wait...",Color.red);
	    
	    LoadStructureAlignmentThread thr = new 
		LoadStructureAlignmentThread(this,
					     pdbcode,
					     pdbcode2);
	    thr.start();
	    
	    // get all data from thr
	    //System.out.println("visualize data for ...");
	    
	}
    }


    /*
    public void addComponent(Container container, Component component,
			     int gridx, int gridy, int gridwidth, int gridheight,
			     int anchor, int fill, double weightx, double weighty)
    {
	c.gridx=gridx; c.gridy=gridy; c.gridwidth=gridwidth;
	c.gridheight=gridheight; c.anchor=anchor; c.fill=fill; 
	c.weightx=weightx; c.weighty=weighty;
	GridBagLayout gbl = (GridBagLayout) container.getLayout();
	gbl.setConstraints(component, c);
	
	container.add(component);
    }



    public void addComponent(Container container, Component component,
			     int gridx, int gridy, int gridwidth, int gridheight,
			     int anchor, int fill, double weightx, double weighty,
			     int top, int left, int bottom, int right)
    {
	c.gridx=gridx; c.gridy=gridy; c.gridwidth=gridwidth;
	c.gridheight=gridheight; c.anchor=anchor; c.fill=fill; 
	c.weightx=weightx; c.weighty=weighty;
	c.insets=new Insets(top, left, bottom, right);
	GridBagLayout gbl = (GridBagLayout) container.getLayout();
	gbl.setConstraints(component, c);
	
	container.add(component);
    }
   
    &/
    /** remember the previous color */
    public void setOldColor(Color c) {
	oldColor = c ;
    }

   

   
   
    /* do the actual parsing of the config.xml file 
    private HashMap parseConfigFile(InputSource is) 
	throws Exception
    {
	HashMap   configr         = new HashMap();
	ArrayList featservers     = new ArrayList() ;
	HashMap   structureserver = new HashMap();
	HashMap   sequenceserver  = new HashMap();
	HashMap   alignmentserver = new HashMap();
	
	DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	Element cfgDoc = parser.parse(is).getDocumentElement();
	Node chld = cfgDoc.getFirstChild();
	while (chld != null) {
	    if (chld instanceof Element) {
		Element echld = (Element) chld;
		if (echld.getTagName().equals("featureserver")) {
		    String url = echld.getAttribute("url");
		    if (url == null) {
			System.err.println("missing attribute >url< in featureserver");
		    }

		    String name = echld.getAttribute("name");
		    if (name == null) {
			System.err.println("missing attribute >name< in featureserver");
		    }
		    System.out.println("using featureserver: "+name+" "+url);
		    HashMap server = new HashMap();
		    server.put("name",name);
		    server.put("url",url);
		    featservers.add(server);
		}
		if(echld.getTagName().equals("structureserver")) {
		     String url = echld.getAttribute("url");
		    if (url == null) {
			System.err.println("missing attribute >url< in structureserver");
		    }

		    String name = echld.getAttribute("name");
		    if (name == null) {
			System.err.println("missing attribute >name< in structureserver");
		    }
		    System.out.println("using structureserver: "+name+" "+url);
		    
		    structureserver.put("name",name);
		    structureserver.put("url",url);
		}
		if(echld.getTagName().equals("sequenceserver")) {
		     String url = echld.getAttribute("url");
		    if (url == null) {
			System.err.println("missing attribute >url< in sequenceserver");
		    }

		    String name = echld.getAttribute("name");
		    if (name == null) {
			System.err.println("missing attribute >name< in sequenceserver");
		    }
		    System.out.println("using sequenceserver: "+name+" "+url);
		    
		    sequenceserver.put("name",name);
		    sequenceserver.put("url",url);
		}
		if(echld.getTagName().equals("alignmentserver")) {
		     String url = echld.getAttribute("url");
		    if (url == null) {
			System.err.println("missing attribute >url< in alignmentserver");
		    }

		    String name = echld.getAttribute("name");
		    if (name == null) {
			System.err.println("missing attribute >name< in alignmentserver");
		    }
		    System.out.println("using alignmentserver: "+name+" "+url);
		    
		    alignmentserver.put("name",name);
		    alignmentserver.put("url",url);
		}

	    }
	    chld = chld.getNextSibling();
	}
	
	configr.put("structureserver", structureserver);
	configr.put("sequenceserver", sequenceserver);
	configr.put("alignmentserver", alignmentserver);
	configr.put("featureservers",featservers);
    
	return configr ;

    }
    */

    public RegistryConfiguration getConfiguration() {
	return config ;
    }

    /** return the urn of the structuresercer */
    //public String getStructureServer() {
    //	HashMap strucconfig = (HashMap) config.get("structureserver");
    //	String u = (String) strucconfig.get("url");

    //	return   u;
    //}

    /** return the urn of the structuresercer */
    //public String getSequenceServer() {
    //	HashMap h = (HashMap) config.get("sequenceserver");
    //	String u = (String) h.get("url");
    //	return   u;
    //    }

    /** return the urn of the structuresercer */
    //public String getAlignmentServer() {
    //	HashMap h = (HashMap) config.get("alignmentserver");
    //	String u = (String) h.get("url");
    //	return   u;
    //}


    /** return the feature servers */
    private List getFeatureServers() {
	return config.getServers("feature");
    }



    /** starts a new thread that retreives protein structure using the
	DAS structure command from some other server this thread will
	call the setStructure method to set the protein structure.
     */
    public void getStructure(String pdbcod) {
	//String server = "http://protodas.derkholm.net/dazzle/mystruc/structure?query=";
	

	// proxy should be set at startup ( if called from command line) otherwise the browsery proxy settings are being used ...

	/*
	Properties systemSettings = System.getProperties();
	systemSettings.put("proxySet", "true");
	systemSettings.put("proxyHost", "wwwcache.sanger.ac.uk");
	systemSettings.put("proxyPort", "3128");
	System.setProperties(systemSettings);
	*/
	first_load = true ;
	pdbcode = pdbcod ;
	LoadStructureThread thr = new LoadStructureThread(this,pdbcod);
	thr.start();
	


	 

	
    }

    
    public String getToolString(int chainnumber,int seqpos) {
	Chain chain = getChain(chainnumber);
	
	if ( ! ((seqpos >= 0) && (seqpos < chain.getLength()))) {
	    return "" ;
	} 

	Group g = chain.getGroup(seqpos);	
	Character amino1 = new Character(" ".charAt(0)) ;
	if (g.getType() == "amino" ) {
	    AminoAcid a = (AminoAcid) g ;
	    amino1 = a.getAminoType();
	}
	String pdbstr = g.getPDBCode();	
	String name   = g.getPDBName();
	if (pdbstr == null ) {
	    pdbstr = "n.a." ;
	    
	}
	
	String drstr = "Seq pos "+ seqpos + "("+amino1+","+name+")" + " PDB ("+ pdbstr +")";  	
	return drstr ;
    }

    public void showSeqPos(int chainnumber, int seqpos){
	String drstr = getToolString(chainnumber,seqpos);
	showStatus(drstr);
	
    }


    /** show status notification in defaul color */
    public void showStatus(String status) {
	showStatus(status,Color.white);
	return ;
    }
    
    /** show status notification in specified color */
    public void showStatus(String status,Color c) {	

	//seq_pos.setColor(c);
	seq_pos.setForeground(c);
	seq_pos.setText(status);
    }
    

    /** set a structure to be displayed and sends a script command to
     * color structure 
     * @param structre_ a Biojava structure object
     * @param selectcmd a rasmol like select command ( all commands in one line, spearated by ";"
     */
    
    public void setStructure(Structure structure_, String selectcmd ) {
	first_load = false ;
	structure = structure_ ; 

	System.out.println("got final structure:"+structure);
	    	
	DefaultListModel model = (DefaultListModel) ent_list.getModel() ;
	synchronized (model) {
	    model.clear() ;
	    //System.out.println(pdbstr);		
	    ArrayList chains = (ArrayList) structure.getChains(0);
	    for (int i=0; i< chains.size();i++) {
		Chain ch = (Chain) chains.get(i);
		model.add(i,ch.getName());
	    }
	}
	
	structurePanel.setStructure(structure);

	structurePanel.executeCmd(selectcmd);
	
	// get sequence
	Chain c = structure.getChain(0);
	List aminos = c.getGroups("amino");
	StringBuffer sequence = new StringBuffer() ;
	for ( int i=0 ; i< aminos.size(); i++){
	    AminoAcid a = (AminoAcid)aminos.get(i);
	    sequence.append( a.getAminoType());
	}
	
	String s = sequence.toString();
	seqField.setText(s);

	setCurrentChain(0);
	updateDisplays();
    

    }


    /** set a structure to be displayed. Use a default select command
     * to color structure
     * @param structre_ a Biojava structure object
     */
    public void setStructure(Structure structure_ ) {
	//System.out.println("setting structure");
	String cmd = INIT_SELECT;

	setStructure(structure_,cmd);

    }

    /** send a command to Jmol */
    public void executeCmd(String cmd) {
	//System.out.println("executing Command "+ cmd);
	structurePanel.executeCmd(cmd);	
    }


    public void setConfiguration(RegistryConfiguration reg) {
	config = reg;
	Chain chain = getChain(currentChain) ;
	String sp_id = chain.getSwissprotId() ;
	getNewFeatures(sp_id) ;
	updateDisplays();
    }

    public void setCurrentChain( int newCurrentChain) {
	System.out.println("setCurrentChain " + newCurrentChain);
	currentChain = newCurrentChain ;
	
	// update features to be displayed ...
	Chain chain = getChain(currentChain) ;
	String sp_id = chain.getSwissprotId() ;
	System.out.println("SP_ID "+sp_id);
	
	ArrayList tmpfeat = getFeaturesFromMemory(sp_id) ;
	
	if ( tmpfeat.size() == 0 ) {
	    getNewFeatures(sp_id) ;
	} else {
	    
	    setFeatures(sp_id,tmpfeat);	    
	    //SeqFeatureCanvas dascanv = daspanel.getCanv();
	    dascanv.setChain(chain,currentChain);
	    dascanv.setBackground(Color.black);
	    seqField.setChain(chain,currentChain);
	}
	

    }

    private synchronized void getNewFeatures(String sp_id) {
	//ArrayList featureservers = getFeatureServers() ;
	Chain chain = getChain(currentChain) ;
	
	FeatureFetcher ff = new FeatureFetcher(this,config,sp_id,pdbcode,chain);
	ff.start() ;

	dascanv.setChain(chain,currentChain);
	dascanv.setBackground(Color.black);
	seqField.setChain(chain,currentChain);

	/**
	boolean done = false ;
	while ( ! done) {
	    done = ff.isDone();
	    //System.out.println("waiting for features to be retreived: "+done);
	    try {
		wait(300);
	    } catch (InterruptedException e) {
		e.printStackTrace();
		done = true ;
	    }
	    //System.out.println("getNewFeatures :in waitloop");
	}
	
	ArrayList tmpfeat = (ArrayList) ff.getFeatures();
	//System.out.println("got new features: " + tmpfeat);
	memoryfeatures.put(sp_id,tmpfeat);
	setNewFeatures(tmpfeat);	
	//SeqFeatureCanvas dascanv = daspanel.getCanv();
	*/
    }

    /**  update the currently displayed features */
    public  void setFeatures(String sp_id, List tmpfeat) {
	memoryfeatures.put(sp_id,tmpfeat);

	features.clear();
	features = tmpfeat ;
	this.paint(this.getGraphics());
	updateDisplays();


    }

    // store all features in memory -> speed up
    private ArrayList getFeaturesFromMemory(String sp_id) {
	System.out.println("getFeaturesFromMemory");
	ArrayList arr = new ArrayList() ;
	
	for (Iterator ti = memoryfeatures.keySet().iterator(); ti.hasNext(); ) {
	    String key = (String) ti.next() ;
	    
	    System.out.println(key);
	    if ( key == null) { continue; }

	    if (key.equals(sp_id)) {
		arr = (ArrayList) memoryfeatures.get(sp_id) ;
		return arr ;
	    }
	}
	
	return arr ;
    }
    /** retreive info regarding structure */
    public Chain getChain(int chainnumber) {
	Chain c = structure.getChain(chainnumber);

	// almost the same as Chain.clone(), here:
	// browse through all groups and only keep those that are amino acids...
	ChainImpl n = new ChainImpl() ;
	//System.out.println(c.getName());
	//System.out.println(c.getSwissprotId());
	n.setName(c.getName());
	n.setSwissprotId(c.getSwissprotId());
	
	ArrayList groups = c.getGroups("amino");
	for (int i = 0 ; i<groups.size();i++){
	    Group group = (Group) groups.get(i);
	    n.addGroup(group);	    
	}
	return n;
    }


    public void colour(int chainNumber, int start, int end, String colour) {
	if (first_load)       return ;		
	if ( start    < 0 ) return ;
	if (chainNumber < 0 ) return ;

	seqField.highlite(start-1,end-1);
	
	String cmd = getSelectStr( chainNumber,  start,  end);
	
	cmd += "colour "+ colour+";";
	structurePanel.executeCmd(cmd);
	structurePanel.forceRepaint();

	    
    }

    public void colour(int chainNumber, int seqpos, String colour) {
	if (first_load)       return ;		
	if ( seqpos    < 0 ) return ;
	if (chainNumber < 0 ) return ;
	String cmd = getSelectStr( chainNumber,  seqpos);
	
	cmd += "colour "+ colour+";";
	structurePanel.executeCmd(cmd);
	structurePanel.forceRepaint();
	
	    
    }

    public void highlite(int chainNumber, int start, int end, String colour){
	System.out.println("highlite start end" + start + " " + end );
	if (first_load)       return ;		
	if ( start    < 0 ) return ;
	if (chainNumber < 0 ) return ;
	
	seqField.highlite(start,end);
	String cmd = getSelectStr( chainNumber,  start,  end);
	cmd +=  " spacefill on; " ;
	if ( colour  != "") {
	    cmd += "colour " +colour ;
	    colour(chainNumber,start,end,colour) ;
	}
	structurePanel.executeCmd(cmd);
	structurePanel.forceRepaint();
	

	/*seqField.setSelectionStart(start);
	seqField.setSelectionEnd(end);
	seqField.setSelectionColor(Color.red);
	seqField.setSelectedTextColor(Color.red);
	seqField.requestFocus();
	seqField.repaint();
	*/
	
	
    }
    public void highlite(int chainNumber, int start, int end) {
	highlite(chainNumber, start, end, "");
	
    }

    public void highlite(int chainNumber, int seqpos, String colour) {
	System.out.println("highlite " + seqpos);
	if (first_load)       return ;		
	if ( seqpos     < 0 ) return ;
	if (chainNumber < 0 ) return ;
	
	seqField.highlite(seqpos);
	
	String cmd = getSelectStr( chainNumber,  seqpos);
	cmd +=  " spacefill on ;" ;
	structurePanel.executeCmd(cmd);
	structurePanel.forceRepaint();

	if ( colour  != "") {
	    colour(chainNumber,seqpos,colour) ;
	}


	/*seqField.setSelectionStart(seqpos);
	seqField.setSelectionEnd(seqpos);
	seqField.setSelectionColor(Color.red);
	seqField.requestFocus();
	seqField.repaint();	
	*/

    }
    public void highlite(int chain_number,int seqpos){
	
	highlite(chain_number,seqpos,"");

    }

    private Group getGroupNext(int chain_number,int startpos, String direction) {
	Chain chain = getChain(chain_number) ;


	while ( (startpos >= 0 ) && (startpos < chain.getLength())){
	    Group g = chain.getGroup(startpos);	
	    if (g.has3D()){
		return g ;
	    }
	    if ( direction.equals("incr") ) {
		startpos += 1;
	    } else {
		startpos -= 1 ;
	    }
	}
	return null ;
    }

    /** return a select command that can be send to executeCmd*/
    public String getSelectStr(int chain_number, int start, int end) {
	Chain chain = getChain(chain_number) ;
	String chainid = chain.getName() ;
	
	Group gs = getGroupNext( chain_number,(start-1),"incr");
	//Group gs = chain.getGroup(start-1);	
	Group ge = getGroupNext( chain_number,(end-1),"decr");
	//= chain.getGroup(end-1);	
	//System.out.println("gs: "+gs+" ge: "+ge);
	if (( gs == null) && (ge == null) ) {
	    return "" ;
	}

	if (gs == null) {
	    return getSelectStr( chain_number, end-1) ;
	}

	if (ge == null) {
	    return getSelectStr( chain_number, start-1) ;
	}


	String startpdb = gs.getPDBCode() ;
	String endpdb = ge.getPDBCode() ;
	
	String cmd =  "select "+startpdb+"-"+endpdb+"and **" +chainid+"; set display selected;" ;
	return cmd ;
    }
    
    /** return a select command that can be send to executeCmd*/
    public String getSelectStr(int chain_number,int seqpos) {

	String pdbdat = getSelectStrSingle(chain_number, seqpos);
	
	if (pdbdat.equals("")){
	    return "" ;
	}	
	String cmd = "select " + pdbdat + ";";
	return cmd ;

    }

    /** return the pdbcode + chainid to select a single residue. This
     * can be used to create longer select statements for individual
     * amino acids. */

    public String getSelectStrSingle(int chain_number, int seqpos) {
	Chain chain = getChain(chain_number) ;
	
	if ( ! ((seqpos >= 0) && (seqpos < chain.getLength()))) {
	   return "" ;
	}
	//SeqFeatureCanvas dascanv = daspanel.getCanv();
	dascanv.highlite(chain_number,seqpos);
	Group g = chain.getGroup(seqpos);
	if (! g.has3D()){
	    return "" ;
	}
	String pdbcod = g.getPDBCode() ;
	//String pdbname = g.getPDBName() ;
	String chainid = chain.getName() ;
	//System.out.println("selected "+pdbcod+" " +pdbname);
	String cmd =  pdbcod+chainid ;
	return cmd ;
    }

    /** select a range of  residue */
    public void select(int chain_number, int start, int end) {
	System.out.println("select start end" + start + " " + end);

	seqField.select(start,end);

	String cmd = getSelectStr( chain_number,  start,  end);
	if (cmd.equals("")) { return ; } 
	cmd += " set display selected;" ;
	structurePanel.executeCmd(cmd);
	structurePanel.forceRepaint();
	
		
	

    }

    /** select a single residue */
    public void select(int chain_number,int seqpos){
	System.out.println("select seqpos" + seqpos);
	seqField.select(seqpos);

	String cmd = getSelectStr( chain_number, seqpos) ;
	if (cmd.equals("")) { return ; } 
	cmd += " set display selected;" ;
	structurePanel.executeCmd(cmd);
	structurePanel.forceRepaint();
	

    }

    public void scale() {
	// reset the sizes of the sub canvases ..
	
    }
    
    /** update the DIsplays of the subpanes */
    public void updateDisplays() {
	//System.out.println("updateDisplays");
	//SeqFeatureCanvas dascanv = daspanel.getCanv();
	dascanv.setFeatures(features);
	dascanv.paint(dascanv.getGraphics());
	
	
	sharedPanel.paint(sharedPanel.getGraphics());
	//leftPanel.paint(leftPanel.getGraphics());
	ent_list.paint(ent_list.getGraphics());
	
	//lcr.paint();
	//ListCellRenderer lcr = ent_list.getCellRenderer();
	//ent_list.paint() ;
	sharedPanel.show();
	this.repaint();
    }

    
   

    /** Event handling */
    public boolean handleEvent(Event event) 
    {
	//System.out.println("EVENT!");
	System.out.println(event.target);
	System.out.println(event.id);

	
	switch(event.id) 
	    {
	    case WindowEvent.WINDOW_CLOSING:
		dispose();
		return true;
		//case Event.ACTION_EVENT:				
	    }

	if ( event.target == reset) {
	    System.out.println("resetting display");
	    String cmd = INIT_SELECT;
	    executeCmd(cmd);
	    return true;
	}

	if ( event.target == props) {
	    System.out.println("modify properties");
	    RegistryConfigIO regi = new RegistryConfigIO(REGISTRY_URL) ;
	    regi.run();
	    regi.showConfigFrame();
		
	    return true;
	}
	else if ( event.target == exit) {
	    dispose();
	    return true;
	}
	else if ( event.target == openpdb ) {
	    System.out.println("open DAS");
	    OpenDialog op = new OpenDialog(this);
	    op.show();
	}

	else if ( event.target == aboutdas ) {
	    System.out.println("about DAS");
	    //AboutDialog asd = new AboutDialog(this);
	    //asd.setText("DAS homepage: http://www.biodas.org") ;
	    //asd.show();
	     AboutDialog asd = new AboutDialog(this);
	     asd.setText("DAS homepage: http://www.biodas.org") ;
	     asd.show();
	    
	    return true;
	}
	else if ( event.target ==  aboutspice ) {
	    System.out.println("about SPICE");
	    AboutDialog asd = new AboutDialog(this);

	    asd.setText("The SPICE Applet. V 0.1 (C) Andreas Prlic, Tim Hubbard\n The Wellcome Trust Sanger Institute 2004 mailto:ap3@sanger.ac.uk") ;

	    asd.show();
	    return true;
	}

	return true ;

    }


}





class ApplicationCloser extends WindowAdapter {
  public void windowClosing(WindowEvent e) {
    System.exit(0);
  }
}

class StructureCommandListener 
    implements ActionListener {
    JTextField textfield ;
    SPICEFrame spice    ;
    
    public StructureCommandListener (SPICEFrame spice_, JTextField textfield_) {
	super();
	spice = spice_ ;
	textfield = textfield_ ;
	
    }
    public void actionPerformed(ActionEvent event) {
	//System.out.println("in TextField");
	//System.out.println("EVENT!");
	//System.out.println(event);
	if ( spice.isLoading() ) {
	    System.out.println("loading data, please be patient");
	    return ;
	}
	String cmd = textfield.getText();
	spice.executeCmd(cmd);
	textfield.setText("");
	
	//System.out.println(event.getActionCommand());
	//System.out.println(event.getModifiers());
	//System.out.println(event.paramString());
	//System.out.println(event.id);
    }

    
}

class TextFieldListener 
    implements ActionListener {
    JTextField textfield ;
    SPICEFrame spice    ;

    public TextFieldListener (SPICEFrame spice_, JTextField textfield_) {
	super();
	spice = spice_ ;
	textfield = textfield_ ;
    }
    public void actionPerformed(ActionEvent event) {
	//System.out.println("in TextField");
	//System.out.println("EVENT!");
	//System.out.println(event);

	if ( spice.isLoading() ) {
	    System.out.println("loading data, please be patient");
	    return ;
	}
	String pdbcod = textfield.getText();
	//spice.executeCmd(cmd);
	spice.getStructure(pdbcod);
	textfield.setText("");
	//System.out.println(event.getActionCommand());
	//System.out.println(event.getModifiers());
	//System.out.println(event.paramString());
    
	//System.out.println(event.id);
    }
}



 
//class EntListCommandListener implements ItemListener {
class EntListCommandListener implements ListSelectionListener {
    SPICEFrame spice ;

    public EntListCommandListener ( SPICEFrame spice_) {
	super();
	spice = spice_;
    }
    //public void itemStateChanged(ItemEvent event) {
    public void valueChanged(ListSelectionEvent event) {
	
	if ( spice.isLoading() ) {
	    System.out.println("loading data, please be patient");
	    return ;
	}

	//System.out.println("EVENT!");
	//System.out.println(event);
	JList list = (JList)event.getSource();
	
	// Get all selected items
	//int i  = ((Integer)list.getSelectedValue()).intValue() ;
	int i  = list.getSelectedIndex();

	if ( i < 0) return ; 
	
	//System.out.println("item:" + event.getItem());
	//System.out.println("param:" + event.paramString());
	//int i = ((Integer)event.getItem()).intValue();
	spice.setCurrentChain(i);
	//System.out.println(event.id);
	//System.out.println(event.arg);
	
    }

}


class OpenDialog extends Dialog
{
    static int H_SIZE = 200;
    static int V_SIZE = 90;
    SPICEFrame spice ;
    JTextField getCom ;
    
    public OpenDialog(SPICEFrame parent){
	// Calls the parent telling it this
	// dialog is modal(i.e true)	
	super((Frame)parent, true); 

	this.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent evt) {
		    Frame frame = (Frame) evt.getSource();
		    frame.setVisible(false);
		    frame.dispose();
		}
	    });

	spice = parent ;
	Panel p = new Panel();
	this.setTitle("enter PDBcode") ;
	getCom = new JTextField(4);
	//TextFieldListener txtlisten = new TextFieldListener(parent,getCom);
	//getCom.addActionListener(txtlisten);

	p.add(getCom);
	p.add(new Button("Open"));
	p.add(new Button("Cancel"));
	add(p);
	resize(H_SIZE, V_SIZE);


    }
    
    public boolean action(Event evt, Object arg)
    {
	// If action label(i.e arg) equals 
	// "Close" then dispose this dialog

	if(arg.equals("Cancel"))
	    {
		dispose();
		return true;
	    }
	else if ( arg.equals("Open"))
	    {
		if ( spice.isLoading() ) {
		    getCom.setText("please wait, already loading");
		    return true ;
		}
		//System.out.println("open" + getCom.getText());
		String pdbcod = getCom.getText();
		spice.getStructure(pdbcod);
		dispose();
		return true;	
	    }

	return super.handleEvent(evt);
    }

}


class AboutDialog extends Dialog
{
    static int H_SIZE = 600;
    static int V_SIZE = 200;
    //JTextField txt ;
    String displayText ;

    public AboutDialog(Frame parent)
    {
	// Calls the parent telling it this
	// dialog is modal(i.e true)
	super(parent, true);         
	setBackground(Color.gray);
	setLayout(new BorderLayout());
	
	displayText="" ;

	// Two buttons "Close" and "Help"
	//txt = new JTextField();
	
	Panel p = new Panel();
	//p.add(txt);
	p.add(new Button("Close"));
	p.add(new Button("Help"));
	
	add("South", p);
	resize(H_SIZE, V_SIZE);
    }

    public boolean action(Event evt, Object arg)
    {
	// If action label(i.e arg) equals 
	// "Close" then dispose this dialog
	if(arg.equals("Close"))
	    {
		dispose();
		return true;
	    }
	return super.handleEvent(evt);
    }

    public void setText(String txt) {
	displayText = txt ;
    }

    public void paint(Graphics g)
    {
	
	g.drawString(displayText, H_SIZE/7, V_SIZE/3);
	//txt.setText(displayText) ;
	/*g.setColor(Color.white);
	g.drawString("The SPICE Applet"      , H_SIZE/4, V_SIZE/3);
	g.drawString("Version 0.1"           , (H_SIZE/4)+20, V_SIZE/3+20);      
	g.drawString("(C) Andreas Prlic 2004", (H_SIZE/4)+40, V_SIZE/3);      
	g.drawString("to learn more about SPICE, please go to", (H_SIZE/4)+60, V_SIZE/3);      
	g.drawString("http://www.sanger.ac.uk/Users/ap3/DAS/SPICE/stable/stable.html", (H_SIZE/4)+60, V_SIZE/3);      
	*/
    }

}
