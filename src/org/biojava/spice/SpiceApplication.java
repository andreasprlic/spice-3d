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

// logging
import java.util.logging.* ;

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

import javax.swing.Box                          ;
import javax.swing.text.Document                ;
import javax.swing.text.Element                 ;
import javax.swing.JSplitPane                   ;
import javax.swing.JFrame                       ;
import javax.swing.JPanel                       ;
import javax.swing.BoxLayout                    ;
import javax.swing.JList                        ;
import javax.swing.ListCellRenderer             ;
import javax.swing.JScrollPane                  ;
import javax.swing.DefaultListModel             ;
import javax.swing.JTextField                   ;
import javax.swing.JTextArea                    ;
import javax.swing.JEditorPane                  ;
import javax.swing.event.ListSelectionListener  ;
import javax.swing.event.ListSelectionEvent     ;  
import javax.swing.ImageIcon                    ;
import javax.swing.BorderFactory                ;
import javax.swing.JDialog                      ;
//import javax.swing.event.CaretEvent;
//import javax.swing.event.CaretListener;

// menu
import javax.swing.JMenuBar    ;
import javax.swing.JMenu       ;
import javax.swing.JMenuItem   ;

// features
import java.net.URL        ;
import java.util.Iterator  ;

// text coloring 
import javax.swing.JTextPane   ;
import javax.swing.text.*;

// for DAS registration server:
//import org.biojava.services.das.registry.*;



/** the main application layer of SPICE
 * do not interact with this class directly, but interact with SPICEFrame interface.
 *
 * @author Andreas Prlic
 */
public class SpiceApplication 
    extends  JFrame
    implements SPICEFrame 

{
    
	
    public static Logger logger =  Logger.getLogger("org.biojava.spice");;
   

    //public static final String CONFIG_FILE = "config.xml";
    URL CONFIG_URL      ; 
    URL REGISTRY_URL    ;


    static int    DEFAULT_Y_SCROLL = 50 ;
    static String XMLVALIDATION = "false" ;   
    static String INIT_SELECT = "select all; cpk off ; wireframe off ; backbone off; cartoon on; colour chain;select not protein and not solvent;spacefill 2.0;";    
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
    //JPanel leftPanel ;
    JSplitPane sharedPanel;
    JSplitPane mainsharedPanel;

    JScrollPane seqScrollPane ;
    JSplitPane  seqSplitPane  ;
    SeqTextPane seqField      ;
    
    //JMenuBar menuBar ;
    JTextField getCom ;

    JTextField  strucommand  ; 
    StatusPanel statusPanel ;
    String[] txtColor     ;
    Color[] entColors ;
    Color oldColor ; 
    boolean first_load ;

    boolean structureAlignmentMode ;
    /*
    JMenuBar  menu ;
    JMenuItem exit ;
    JMenuItem props ;
    JMenuItem reset ;
    JMenuItem aboutspice ;
    JMenuItem aboutdas   ;
    JMenuItem openpdb    ;
    */
    //public static Logger logger = Logger.getLogger("org.biojava.spice");


    SpiceApplication(String pdbcode_, URL config_url, URL registry_url) {
	super();

	
	
	LoggingPanel loggingPanel = new LoggingPanel(logger);
	loggingPanel.getHandler().setLevel(Level.FINE);
	loggingPanel.show(null);
	//ConsoleHandler handler = new ConsoleHandler();
	//handler.setLevel(Level.FINEST);
	//logger.addHandler(loggingPanel.getHandler());

	System.setProperty("XMLVALIDATION",XMLVALIDATION);
	int timeout = 15000;
	logger.setLevel(Level.FINEST);
	if (logger.isLoggable(Level.FINEST)) {
	    logger.finest("setting timeouts to " + timeout);
	}
	
	System.setProperty("sun.net.client.defaultConnectTimeout", ""+timeout);
	System.setProperty("sun.net.client.defaultReadTimeout", ""+timeout);

	//Properties sprops = System.getProperties() ;
	//sprops.put("proxySet", "true" );
	//sprops.put("proxyHost", "wwwcache.sanger.ac.uk" );
	//sprops.put("proxyPort", "3128" );
	//sprops.put("http.proxyHost", "wwwcache.sanger.ac.uk");
	//sprops.put("http.proxyPort", "3128");
	//
	String proxyHost  = System.getProperty("proxyHost");
	String proxyPort  = System.getProperty("proxyPort");
	
	if (logger.isLoggable(Level.FINEST)) {
	    logger.finest("proxyHost"         + proxyHost);
	    logger.finest("proxyPort"         + proxyPort);
	    logger.finest("http.proxyHost"    + System.getProperty("http.proxyHost"));
	    logger.finest("http.proxyPort"    + System.getProperty("http.proxyPort"));
	}
	if ( proxyHost != null ) {
	    System.setProperty("proxySet","true");
	    if ( System.getProperty("http.proxyHost") == null ){
		System.setProperty("http.proxyHost",proxyHost) ;
	    }
	}

	if ( proxyPort != null ) {
	    if ( System.getProperty("http.proxyPort") == null ){
		System.setProperty("http.proxyPort",proxyPort);
	    }
	}
	if (logger.isLoggable(Level.FINEST)) {
	    logger.finest("using Proxy:" + System.getProperty("proxySet"));
	}

	CONFIG_URL   = config_url ;
	REGISTRY_URL = registry_url ;
	
	// first thing is to start communication

	RegistryConfigIO regi = new RegistryConfigIO (this,REGISTRY_URL);
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
	      
	Box vBox = Box.createVerticalBox();

	//vBox.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	//this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));


	// add the Menu
	JMenuBar menu = initMenu();
	this.setJMenuBar(menu);

	
	statusPanel = new StatusPanel();
	//this.getContentPane().add(statusPanel,BorderLayout.SOUTH);
	//this.getContentPane().add(statusPanel);
	statusPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
	vBox.add(statusPanel);

	statusPanel.setLoading(first_load);

	
	
	// init Seqouece position
	seq_pos = new JTextField();
	seq_pos.setForeground(new Color(255, 255, 255));
	seq_pos.setBackground(new Color(0, 0, 0));
	seq_pos.setSize(700, 30);
	seq_pos.setMaximumSize(new Dimension(Short.MAX_VALUE,30));

	//this.getContentPane().add(seq_pos,BorderLayout.NORTH);
	//this.getContentPane().add(seq_pos);
	vBox.add(seq_pos);


	showStatus("contacting DAS registry");

	/// init Structure Panel
	structurePanel        = new StructurePanel(this);
	//structurePanel.setLayout(new BoxLayout(structurePanel, BoxLayout.X_AXIS));
	structurePanel.setPreferredSize(new Dimension(700, 700));
	structurePanel.setMinimumSize(new Dimension(200,200));
	//structurePanel.addMouseMotionListener(structurePanel);
	//structurePanel.addMouseListener(      structurePanel);
	//this.add(structurePanel,BorderLayout.CENTER);




	DefaultListModel model = new DefaultListModel();
	model.add(0,"loading...");
	ent_list=new JList(model);
	EntListCommandListener entact = new EntListCommandListener(this);
	ent_list.addListSelectionListener(entact);
	//ent_list.setPreferredSize(new Dimension(30,30));
	

	JScrollPane chainPanel = new JScrollPane(ent_list);
	chainPanel.setPreferredSize(new Dimension(30,30));
	
	//chainPanel.setSize(30,30);

	//chainPanel.setMinimumSize(new Dimension(30,30));
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
	
	/*
    	leftPanel = new JPanel() ;
	leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

	//leftPanel.add(ent_list);
	//leftPanel.add(getCom,BorderLayout.SOUTH);
	leftPanel.setMinimumSize(  new Dimension(30,30));
	leftPanel.setPreferredSize(new Dimension(30,30));
	leftPanel.setMaximumSize(  new Dimension(40,30));
	leftPanel.setSize(30,30);
	//ent_list.repaint();
	*/
	
	//ScrollPane scroll = new ScrollPane();

	
	// init dascanv
	dascanv=new SeqFeatureCanvas(this);       
	dascanv.setForeground(Color.black);
	dascanv.setBackground(Color.black);
	dascanv.addMouseMotionListener(dascanv);
	dascanv.addMouseListener(      dascanv);
	//dascanv.setOpaque(true) ;
	dascanv.setPreferredSize(new Dimension(200, 200));
	//dascanv.setSize(700,300);
	
	dasPanel = new JScrollPane(dascanv);
	//dasPanel.setOpaque(true);
	dasPanel.setBackground(Color.black);
	dasPanel.getVerticalScrollBar().setUnitIncrement(DEFAULT_Y_SCROLL);
	dasPanel.getHorizontalScrollBar().setUnitIncrement(DEFAULT_Y_SCROLL);
	//scroll.add(dascanv);

	//daspanel = new SeqPanel(this);
	//daspanel.setMinimumSize(new Dimension(100,100));

	
	sharedPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                           chainPanel, dasPanel);
	sharedPanel.setOneTouchExpandable(true);
	//sharedPanel.setDividerLocation(150);
	sharedPanel.setPreferredSize(new Dimension(300, 300));
	//sharedPanel.setOpaque(true);
	//sharedPanel.setResizeWeight(0);	
	sharedPanel.setBackground(Color.black);

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
	//seqSplitPane.setOpaque(true);
	seqSplitPane.setResizeWeight(0.6);
	seqSplitPane.setBackground(Color.black);
	seqSplitPane.setPreferredSize(new Dimension(300,300));

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
	mainsharedPanel.setResizeWeight(0.6);
	//mainsharedPanel.setDividerLocation(150);
	//mainsharedPanel.setPreferredSize(new Dimension(200, 200));
	//mainsharedPanel.setOpaque(true);
	mainsharedPanel.setBackground(Color.black);
	//mainsharedPanel.setPreferredSize(new Dimension(700,700));
	//mainsharedPanel.setResizeWeight(0.7);
	//this.getContentPane().add(mainsharedPanel,BorderLayout.NORTH);
	//this.getContentPane().add(mainsharedPanel);
	mainsharedPanel.setBorder(BorderFactory.createEmptyBorder());
	vBox.add(mainsharedPanel,BorderLayout.CENTER);



	
	strucommand = new JTextField()  ;
	strucommand.setText("enter RASMOL like command...");
	ActionListener listener = new StructureCommandListener(this,strucommand) ;
	strucommand.addActionListener(listener);
	strucommand.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
	//this.getContentPane().add(strucommand,BorderLayout.SOUTH);
	//this.getContentPane().add(strucommand);
	vBox.add(strucommand);
	
	//vBox.add(loggingPanel);


	this.getContentPane().add(vBox);

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
	
	//this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	this.setDefaultLookAndFeelDecorated(false);
	ImageIcon icon = createImageIcon("spice.png");
	this.setIconImage(icon.getImage());
	this.pack();
	
	this.setVisible(true);

	config =regi.getConfiguration();
	if ( config == null ) {
	    String msg = "Unable to contact DAS registration service, can not continue!" ;
	    seq_pos.setText(msg);
	    logger.log(Level.SEVERE,msg);
	    System.err.println(msg);

	    return ;
	    
	}
	


    }


    /** Constructor for structure alignment visualization 
     */
    SpiceApplication(String pdb1, String pdb2, URL config_url, URL registry_url) {
	this(pdb1, config_url,registry_url);
	structureAlignmentMode = true ;
	pdbcode2 = pdb2 ;
	logger.finest("finished init of structure alignment");
		
    }


    private JMenuBar initMenu() {

	JMenuBar menu = new JMenuBar();

	// the three menus
	JMenu file = new JMenu("File");
	file.setMnemonic(KeyEvent.VK_F);
	file.getAccessibleContext().setAccessibleDescription("the file menu");
	
	menu.add(file);


	JMenuItem openpdb = new JMenuItem("open PDB");
	openpdb.setMnemonic(KeyEvent.VK_O);
	JMenuItem exit    = new JMenuItem("Exit");
	exit.setMnemonic(KeyEvent.VK_X);
	JMenuItem props   = new JMenuItem("Properties");
	props.setMnemonic(KeyEvent.VK_P);

	SpiceMenuListener ml = new SpiceMenuListener(this) ;
	openpdb.addActionListener( ml );
	exit.addActionListener   ( ml );
	props.addActionListener  ( ml );


	file.add( openpdb );
	file.add( props   );
	file.add( exit    );
	
	// DIsplay submenu
	
	JMenu display = new JMenu("Display");
	display.setMnemonic(KeyEvent.VK_D);
	display.getAccessibleContext().setAccessibleDescription("change display");

	menu.add(display);

	JMenuItem reset   = new JMenuItem("Reset");
	reset.setMnemonic(KeyEvent.VK_R);

	JMenuItem backbone   = new JMenuItem("Backbone");
	JMenuItem wireframe  = new JMenuItem("Wireframe");
	JMenuItem cartoon    = new JMenuItem("Cartoon");
	JMenuItem ballnstick = new JMenuItem("Ball and Stick");
	JMenuItem spacefill  = new JMenuItem("Spacefill");

	JMenuItem colorchain = new JMenuItem("Color - chain");
	JMenuItem colorsec   = new JMenuItem("Color - secondary");
	JMenuItem colorcpk   = new JMenuItem("Color - cpk");

	reset.addActionListener     ( ml );
	backbone.addActionListener  ( ml );
	wireframe.addActionListener ( ml );	
	cartoon.addActionListener   ( ml );
	ballnstick.addActionListener( ml );
	spacefill.addActionListener ( ml );		
	colorchain.addActionListener( ml );
	colorsec.addActionListener  ( ml );
	colorcpk.addActionListener  ( ml );
	

	display.add( reset   );
	display.addSeparator();
	
	display.add( backbone   );
	display.add( wireframe  );
	display.add( cartoon    );
	display.add( ballnstick );
	display.add( spacefill  );
	display.addSeparator();
	
	display.add(colorchain);
	display.add(colorsec)   ;
	display.add(colorcpk)  ;


	menu.add(Box.createGlue());


	// Help submenu
	JMenu help = new JMenu("Help");
	help.setMnemonic(KeyEvent.VK_H);
	help.getAccessibleContext().setAccessibleDescription("get help");
	menu.add(help);

	JMenuItem aboutspice = new JMenuItem("About SPICE");
	aboutspice.addActionListener  ( ml );
	help.add(aboutspice);

	return menu ;

}



    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = SpiceApplication.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
	    logger.log(Level.WARNING,"Couldn't find file: " + path);
            return null;
        }
    }



    public boolean isLoading() {
	return first_load;
    }


   

    public void show(){
	super.show();
	//logger.finest("SpiceApplication show() : getting Structure data from new thread");
	// and now load data ...
	
	// need to call showduring init, to make sure das registry frame and threads can set status.
	// threads can only be started once the config is loaded from registry 
	
	if ( config == null ) {
	    return ;
	}
	
	if ( ! structureAlignmentMode ) {
	    logger.finest("not in alignment mode");
	    getStructure(pdbcode);
	} else {
	    showStatus("Loading...Wait...",Color.red);
	    
	    LoadStructureAlignmentThread thr = new 
		LoadStructureAlignmentThread(this,
					     pdbcode,
					     pdbcode2);
	    thr.start();
	    	    
	}
	
    }


   
    /** remember the previous color */
    public void setOldColor(Color c) {
	oldColor = c ;
    }

 
    public RegistryConfiguration getConfiguration() {
	return config ;
    }


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

	if (logger.isLoggable(Level.FINER)) {
	    logger.entering(this.getClass().getName(), "getStructure",  new Object[]{pdbcod});
	}

	logger.log(Level.INFO,"getting new structure "+pdbcod);

	if (logger.isLoggable(Level.FINEST)) {
	    logger.finest("SpiceApplication: getStructure "+ pdbcod);
	}
	first_load = true ;
	statusPanel.setLoading(true);
	pdbcode = pdbcod ;
	LoadStructureThread thr = new LoadStructureThread(this,pdbcod);
	thr.start();
	


	 

	
    }

    
    public String getToolString(int chainnumber,int seqpos) {
	Chain chain = getChain(chainnumber);
	if ( chain == null) return "" ;
	
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
     * @param structure_ a Biojava structure object
     * @param selectcmd a rasmol like select command ( all commands in one line, spearated by ";"
     */
    
    public void setStructure(Structure structure_, String selectcmd ) {

	if (logger.isLoggable(Level.FINER)) {
	    logger.entering(this.getClass().getName(), "setStructure",  new Object[]{"got structure object"});
	}
	

	first_load = false ;
	statusPanel.setLoading(false);

	if ( structure_.size() < 1 ){
	    logger.log(Level.WARNING,"got no structure");
	    return ;
	}



	structure = structure_ ; 

	//if (logger.isLoggable(Level.FINEST)) {
	//System.out.println(structure.toPDB());	    
	//}


	    	
	DefaultListModel model = (DefaultListModel) ent_list.getModel() ;
	synchronized (model) {
	    model.clear() ;
	    //logger.finest(pdbstr);		
	    ArrayList chains = (ArrayList) structure.getChains(0);
	    for (int i=0; i< chains.size();i++) {
		Chain ch = (Chain) chains.get(i);
		model.add(i,ch.getName());
	    }
	}
	
	structurePanel.setStructure(structure);
	
	statusPanel.setPDB(structure.getPDBCode());
	
	structurePanel.executeCmd(selectcmd);
	
	/* should be done by SeqTextPane ...
	// get sequence
	Chain c = structure.getChain(0);
	List aminos = c.getGroups("amino");
	StringBuffer sequence = new StringBuffer() ;
	for ( int i=0 ; i< aminos.size(); i++){
	    AminoAcid a = (AminoAcid)aminos.get(i);
	    sequence.append( a.getAminoType());
	}
	
	String s = sequence.toString();
	*/
	//seqField.setText(s);
	//Chain c = structure.getChain(0);
	
	setCurrentChain(0);
	Chain chain = getChain(currentChain) ;
	if ( chain != null) 
	    seqField.setChain(chain,0);
	updateDisplays();
    

    }


    /** set a structure to be displayed. Use a default select command
     * to color structure
     * @param structure_ a Biojava structure object
     */
    public void setStructure(Structure structure_ ) {
	//logger.finest("setting structure");
	String cmd = INIT_SELECT;

	setStructure(structure_,cmd);

    }

    /** send a command to Jmol */
    public void executeCmd(String cmd) {
	//logger.finest("executing Command "+ cmd);
	structurePanel.executeCmd(cmd);	
    }


    public void setConfiguration(RegistryConfiguration reg) {
	config = reg;
	List s = config.getServers();
	for ( int i=0; i< s.size();i++){
	    SpiceDasSource d = (SpiceDasSource)s.get(i);
	    logger.finest(d.getUrl());
	}

	Chain chain = getChain(currentChain) ;
	if ( chain != null) {
	    String sp_id = chain.getSwissprotId() ;
	    getNewFeatures(sp_id) ;
	    //updateDisplays();
	}
	
    }

    public int getCurrentChain() {
	return currentChain;
    }
    public void setCurrentChain( int newCurrentChain) {
	logger.finer("setCurrentChain " + newCurrentChain);
	currentChain = newCurrentChain ;
	
	// update features to be displayed ...
	Chain chain = getChain(currentChain) ;
	if ( chain == null) return ;
	String sp_id = chain.getSwissprotId() ;
	logger.finest("SP_ID "+sp_id);
	
	statusPanel.setSP(sp_id);

	ArrayList tmpfeat = getFeaturesFromMemory(sp_id) ;
	
	if ( tmpfeat.size() == 0 ) {
	    if ( isLoading()) {
		logger.log(Level.WARNING,"already loading data, please wait");
		return ;
	    }
	    getNewFeatures(sp_id) ;
	} else {
	    logger.finest("setting features for seq " + sp_id + " features size: " + tmpfeat.size());
	    //setFeatures(sp_id,tmpfeat);	    
	    //features.clear()                     ;
	    features = tmpfeat                   ;
	    //SeqFeatureCanvas dascanv = daspanel.getCanv();
	    dascanv.setChain(chain,currentChain) ;
	    dascanv.setBackground(Color.black)   ;
	    seqField.setChain(chain,currentChain);
	    //updateDisplays();
	}
	

    }

    private  void getNewFeatures(String sp_id) {
	//ArrayList featureservers = getFeatureServers() ;

	Chain chain = getChain(currentChain) ;
	if ( chain == null) return ;
	first_load = true ;
	FeatureFetcher ff = new FeatureFetcher(this,config,sp_id,pdbcode,chain);	
	ff.start() ;
	statusPanel.setLoading(true);
	dascanv.setChain(chain,currentChain);
	dascanv.setBackground(Color.black);
	seqField.setChain(chain,currentChain);

	/**
	boolean done = false ;
	while ( ! done) {
	    done = ff.isDone();
	    //logger.finest("waiting for features to be retreived: "+done);
	    try {
		wait(300);
	    } catch (InterruptedException e) {
		e.printStackTrace();
		done = true ;
	    }
	    //logger.finest("getNewFeatures :in waitloop");
	}
	
	ArrayList tmpfeat = (ArrayList) ff.getFeatures();
	//logger.finest("got new features: " + tmpfeat);
	memoryfeatures.put(sp_id,tmpfeat);
	setNewFeatures(tmpfeat);	
	//SeqFeatureCanvas dascanv = daspanel.getCanv();
	*/

    }

    /**  update the currently displayed features */
    public   void setFeatures(String sp_id, List tmpfeat) {
	// todo create Feature for structure mapping
	first_load = false ;
	memoryfeatures.put(sp_id,tmpfeat);
	statusPanel.setLoading(false);
	features.clear();
	features = tmpfeat ;
	//this.paint(this.getGraphics());
	updateDisplays();
    }

    // store all features in memory -> speed up
    private ArrayList getFeaturesFromMemory(String sp_id) {
	logger.entering(this.getClass().getName(), "getFeaturesFromMemory()",  new Object[]{sp_id});
	//logger.finest("getFeaturesFromMemory");
	ArrayList arr = new ArrayList() ;
	
	for (Iterator ti = memoryfeatures.keySet().iterator(); ti.hasNext(); ) {
	    String key = (String) ti.next() ;
	    logger.finest("in mem: " + key);
	    //logger.finest(key);
	    if ( key == null) { continue; }

	    if (key.equals(sp_id)) {
		logger.finest("found features in memory for spi_id " + sp_id);

		arr = (ArrayList) memoryfeatures.get(sp_id) ;

		for ( int i = 0 ; i < arr.size() ; i++ ) {
		    Feature f = (Feature) arr.get(i);
		    logger.finest(" got memory feature " + f.toString());
		}
		return arr ;
	    }
	}
	
	return arr ;
    }
    /** get Chain number X from structure 
     * @return a Chain object or null ;
     */
    public Chain getChain(int chainnumber) {
	
	if ( structure == null ) {
	    logger.log(Level.WARNING,"no structure loaded, yet");
	    return null ;
	}

	if ( structure.size() < 1 ) {
	    logger.log(Level.WARNING,"structure object is empty, please load new structure");
	    return null ;
	}
	    
	if ( chainnumber > structure.size()) {
	    logger.log(Level.WARNING,"requested chain number "+chainnumber+" but structure has size " + structure.size());
	    return null ;
	}

	Chain c = structure.getChain(chainnumber);
	// almost the same as Chain.clone(), here:
	// browse through all groups and only keep those that are amino acids...
	ChainImpl n = new ChainImpl() ;
	//logger.finest(c.getName());
	//logger.finest(c.getSwissprotId());
	n.setName(c.getName());
	n.setSwissprotId(c.getSwissprotId());
	
	ArrayList groups = c.getGroups("amino");
	for (int i = 0 ; i<groups.size();i++){
	    Group group = (Group) groups.get(i);
	    n.addGroup(group);	    
	}
	return n;
    }

    /** reset the Jmol panel */
    public void resetDisplay(){
	String cmd = INIT_SELECT;
	this.executeCmd(cmd);
    }

    
    public void showConfig() {
	RegistryConfigIO regi = new RegistryConfigIO(this,REGISTRY_URL);
	regi.setConfiguration(config);
	//regi.run();
	regi.showConfigFrame();
    }
    public void colour(int chainNumber, int start, int end, String colour) {
	if (first_load)       return ;		
	if ( start    < 0 ) return ;
	if (chainNumber < 0 ) return ;


	
	String cmd = getSelectStr( chainNumber,  start,  end);
	
	cmd += "colour "+ colour+";";
	structurePanel.executeCmd(cmd);
	//structurePanel.forceRepaint();
	if ( chainNumber == currentChain) {
	    seqField.highlite(start-1,end-1);
	    dascanv.highlite(start-1,end-1);
	}
    }

    public void colour(int chainNumber, int seqpos, String colour) {
	if (first_load)       return ;		
	if ( seqpos    < 0 ) return ;
	if (chainNumber < 0 ) return ;
	String cmd = getSelectStr( chainNumber,  seqpos);
	
	cmd += "colour "+ colour+";";
	structurePanel.executeCmd(cmd);
	//structurePanel.forceRepaint();
	
	    
    }

    public void highlite(int chainNumber, int start, int end, String colour){
	//logger.finest("highlite start end" + start + " " + end );
	if ( first_load)       return ;		
	if ( start       < 0 ) return ;
	if ( chainNumber < 0 ) return ;
	

	// highlite structure
	String cmd = getSelectStr( chainNumber,  start,  end);
	//cmd +=  " spacefill on; " ;
	if ( colour  != "") {
	    cmd += "colour " +colour ;
	    colour(chainNumber,start,end,colour) ;
	}
	structurePanel.executeCmd(cmd);
	//structurePanel.forceRepaint();
	
	// and now the SeqPanels ...
	if ( chainNumber == currentChain) {
	    dascanv.highlite(start,end);
	    seqField.highlite(start,end);
	}

	this.repaint();
	
    }
    public void highlite(int chainNumber, int start, int end) {
	highlite(chainNumber, start, end, "");
	
    }

    public void highlite(int chainNumber, int seqpos, String colour) {
	//logger.finest("highlite " + seqpos);
	if (first_load)       return ;		
	if ( seqpos     < 0 ) return ;
	if (chainNumber < 0 ) return ;
	

	
	String cmd = getSelectStr( chainNumber,  seqpos);
	cmd +=  " spacefill on ;" ;
	structurePanel.executeCmd(cmd);
	//structurePanel.forceRepaint();

	if ( colour  != "") {
	    colour(chainNumber,seqpos,colour) ;
	}

	if ( chainNumber == currentChain ) {
	    dascanv.highlite(seqpos);
	    seqField.highlite(seqpos);
	}
	this.repaint();
    }
    public void highlite(int chain_number,int seqpos){
	
	highlite(chain_number,seqpos,"");

    }

    private Group getGroupNext(int chain_number,int startpos, String direction) {
	Chain chain = getChain(chain_number) ;
	if ( chain == null) return null ;

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

    /** test if pdbserial has an insertion code */
    private boolean hasInsertionCode(String pdbserial) {
	try {
	    int pos = Integer.parseInt(pdbserial) ;
	} catch (NumberFormatException e) {
	    return true ;
	}
	return false ;
    }


    /** return a select command that can be send to executeCmd*/
    public String getSelectStr(int chain_number, int start, int end) {
	Chain chain = getChain(chain_number) ;
	if ( chain == null) return "" ;
	String chainid = chain.getName() ;
	
	Group gs = getGroupNext( chain_number,(start-1),"incr");
	//Group gs = chain.getGroup(start-1);	
	Group ge = getGroupNext( chain_number,(end-1),"decr");
	//= chain.getGroup(end-1);	
	//logger.finest("gs: "+gs+" ge: "+ge);
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
	if ( chain == null) return "" ;
	
	if ( ! ((seqpos >= 0) && (seqpos < chain.getLength()))) {
	    logger.finest("seqpos " + seqpos + "chainlength:" + chain.getLength());
	    return "" ;
	}
	
	//SeqFeatureCanvas dascanv = daspanel.getCanv();
	//if ( chain_number == currentChain )
	//  dascanv.highlite(seqpos);
	
	Group g = chain.getGroup(seqpos);
	if (! g.has3D()){
	    return "" ;
	}

	String pdbcod = g.getPDBCode() ;
	
	if ( hasInsertionCode(pdbcod) ) {
	    String inscode = pdbcod.substring(pdbcod.length()-1,pdbcod.length());
	    String rawcode = pdbcod.substring(0,pdbcod.length()-1);
	    pdbcod = rawcode +"^" + inscode;
	}


	//String pdbname = g.getPDBName() ;
	String chainid = chain.getName() ;
	//logger.finest("selected "+pdbcod+" " +pdbname);
	String cmd =  pdbcod+chainid ;
	return cmd ;
    }

    /** select a range of  residue */
    public void select(int chain_number, int start, int end) {
	//logger.finest("select start end" + start + " " + end);

	if ( chain_number == currentChain ) {
	    seqField.select(start,end);		
	    dascanv.select(start,end);
	}
	
	
	String cmd = getSelectStr( chain_number,  start,  end);
	if (cmd.equals("")) { return ; } 
	cmd += " set display selected;" ;
	structurePanel.executeCmd(cmd);
	//structurePanel.forceRepaint();
	


    }

    /** select a single residue */
    public void select(int chain_number,int seqpos){
	//logger.finest("select seqpos" + seqpos);

	if ( chain_number == currentChain ){
	    seqField.select(seqpos);
	    dascanv.select(seqpos);
	}


	String cmd = getSelectStr( chain_number, seqpos) ;
	if (cmd.equals("")) { return ; } 
	cmd += " set display selected;" ;
	structurePanel.executeCmd(cmd);
	//structurePanel.forceRepaint();

	

    }

    public void scale() {
	// reset the sizes of the sub canvases ..
	
    }
    
    /** update the DIsplays of the subpanes */
    public void updateDisplays() {
	logger.finest("updateDisplays + features size: " + features.size());
	//SeqFeatureCanvas dascanv = daspanel.getCanv();
	dascanv.setFeatures(features);
	//dascanv.paint(dascanv.getGraphics());
	
	sharedPanel.paint(sharedPanel.getGraphics());
	//leftPanel.paint(leftPanel.getGraphics());
	ent_list.paint(ent_list.getGraphics());
	
	//lcr.paint();
	//ListCellRenderer lcr = ent_list.getCellRenderer();
	//ent_list.paint() ;
	sharedPanel.show();
	dasPanel.updateUI();
	//dasPanel.revalidate();
	//dasPanel.repaint();

	this.repaint();
    }

    

    /** retreive the chainNumber by PDB character
	@param PDB code for chain
	@return number of chain in current structure, or -1.
    */
    public int getChainPosByPDB(String chainPDBcode){


	List chains = (List) structure.getChains(0);
	for (int i=0; i< chains.size();i++) {
	    Chain ch = (Chain) chains.get(i);
	    if ( ch.getName().equals(chainPDBcode))
		return i ;
	}
	return -1 ;
	
    }

    /** retreive the sequence position for a residue by it's PDB code.
	if there is an insertion code append it.
	e.g.
	getSeqPosByPDB("122");
	getSeqPosByPDB("122A");
	@param residuePDBcode PDB Code for residue. append insertion code if needed.
	@return position of group in currently displayed chain or -1.
    */
    public int getSeqPosByPDB(String residuePDBcode){
	
	Chain chain = getChain(currentChain) ;
	if ( chain == null ) return -1 ;
	
	//logger.finest( "testing for insertion code " +residuePDBcode);
	// see if there is an "^" character indicating insertionCode...


	//List groups = chain.getGroups();
	
	for ( int i = 0 ; i < chain.getLength(); i++ ) {
	    Group g = chain.getGroup(i);
	    if ( g.has3D()) 
		if (g.getPDBCode().equals(residuePDBcode)) {
		    return i;
	    }
	}
	return -1 ;
    }
    
  
   

    /** Event handling */
    public boolean handleEvent(Event event) 
    {
	//logger.finest("EVENT!");
	//logger.finest(event.target);
	//logger.finest(event.id);

	
	switch(event.id) 
	    {
	    case WindowEvent.WINDOW_CLOSING:
		dispose();
		return true;
		//case Event.ACTION_EVENT:				
	    }


	return true ;

    }


}



class SpiceMenuListener 
    implements ActionListener
{
    SPICEFrame parent ;

    static String reset = "select all; cpk off ; wireframe off ; backbone off; cartoon off ; ribbons off; " ;

    public SpiceMenuListener (SPICEFrame spice) {
	parent = spice ;
    }

    public void actionPerformed(ActionEvent e) {
	System.out.println(e);
	System.out.println(e.getActionCommand());
	
	String cmd = e.getActionCommand();
	if ( cmd.equals("open PDB") ) {
	    OpenDialog op = new OpenDialog(parent);
	    op.show();
	    
	} else if (cmd.equals("Exit")) {
	    System.exit(0);
	} else if (cmd.equals("Properties")) {
	    parent.showConfig();
	    //RegistryConfigIO regi = new RegistryConfigIO(parent,parent.REGISTRY_URL) ;	    
	    //regi.setConfiguration(config);
	    //regi.showConfigFrame();
	} else if (cmd.equals("Reset")) {
	    parent.resetDisplay();
	} else if (cmd.equals("About SPICE")) {
	    AboutDialog asd = new AboutDialog((JFrame)parent);
	    
	    asd.setText("The SPICE Applet. V 0.51(C) Andreas Prlic, Tim Hubbard\n The Wellcome Trust Sanger Institute 2004 mailto:ap3@sanger.ac.uk") ;
	    
	    asd.show();	    
	} else if ( cmd.equals("Backbone") ){
	    String dcmd  = reset + "backbone on; ";
	    parent.executeCmd(dcmd);
	} else if ( cmd.equals("Wireframe") ){
	    String dcmd  = reset + "wireframe on; ";
	    parent.executeCmd(dcmd);
	} else if ( cmd.equals("Cartoon") ){
	    String dcmd  = reset + "cartoon on; ";
	    parent.executeCmd(dcmd);
	} else if ( cmd.equals("Ball and Stick") ){
	    String dcmd  = reset + "wireframe 0.3; spacefill 0.5; ";
	    parent.executeCmd(dcmd);
	} else if ( cmd.equals("Spacefill") ){
	    String dcmd  = reset + "spacefill on; ";
	    parent.executeCmd(dcmd);
	} else if ( cmd.equals("Color - chain")) {
	    String dcmd = "select all; color chain;" ;
	    parent.executeCmd(dcmd);
	} else if ( cmd.equals("Color - secondary")) {
	    String dcmd = "select all; color structure;" ;
	    parent.executeCmd(dcmd);
	} else if ( cmd.equals("Color - cpk")) {
	    String dcmd = "select all; color cpk;" ;
	    parent.executeCmd(dcmd);
	}
    }
}



//class ApplicationCloser extends WindowAdapter {
//  public void windowClosing(WindowEvent e) {
//    System.exit(0);
//  }
//}


class TextFieldListener 
    implements ActionListener {
    JTextField textfield ;
    SPICEFrame spice    ;
    static Logger logger      = Logger.getLogger("org.biojava.spice");

    public TextFieldListener (SPICEFrame spice_, JTextField textfield_) {
	super();
	spice = spice_ ;
	textfield = textfield_ ;
    }
    public void actionPerformed(ActionEvent event) {
	//logger.finest("in TextField");
	//logger.finest("EVENT!");
	//logger.finest(event);

	if ( spice.isLoading() ) {
	    logger.finest("loading data, please be patient");
	    return ;
	}
	String pdbcod = textfield.getText();
	//spice.executeCmd(cmd);
	spice.getStructure(pdbcod);
	textfield.setText("");
	//logger.finest(event.getActionCommand());
	//logger.finest(event.getModifiers());
	//logger.finest(event.paramString());
    
	//logger.finest(event.id);
    }
}



 
//class EntListCommandListener implements ItemListener {
class EntListCommandListener implements ListSelectionListener {
    SPICEFrame spice ;
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    
    public EntListCommandListener ( SPICEFrame spice_) {
	super();
	spice = spice_;
    }
    //public void itemStateChanged(ItemEvent event) {
    public void valueChanged(ListSelectionEvent event) {
	
	if ( spice.isLoading() ) {
	    logger.log(Level.WARNING,"loading data, please be patient");
	    return ;
	}

	//logger.finest("EVENT!");
	//logger.finest(event);
	JList list = (JList)event.getSource();
	
	// Get all selected items
	//int i  = ((Integer)list.getSelectedValue()).intValue() ;
	int i  = list.getSelectedIndex();

	if ( i < 0) return ; 
	
	//logger.finest("item:" + event.getItem());
	//logger.finest("param:" + event.paramString());
	//int i = ((Integer)event.getItem()).intValue();
	spice.setCurrentChain(i);
	//logger.finest(event.id);
	//logger.finest(event.arg);
	
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

	getCom.addActionListener(new ActionListener()  {
		public void actionPerformed(ActionEvent e) {
		    String pdbc = e.getActionCommand();
		    if ( pdbc.length() == 4 ) {
			spice.getStructure(pdbc);
			dispose();	
		    }
		}
	    });

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
		//logger.finest("open" + getCom.getText());
		String pdbcod = getCom.getText();
		if ( pdbcod.length() == 4 ) {
		    spice.getStructure(pdbcod);
		    dispose();
		}
		return true;	
	    }

	return super.handleEvent(evt);
    }

}


class AboutDialog extends JDialog
{
    static int H_SIZE = 200;
    static int V_SIZE = 400;
    //JTextField txt ;
    String displayText     ;
    JTextPane txt         ;
    public AboutDialog(JFrame parent)
    {
	// Calls the parent telling it this
	// dialog is modal(i.e true)
	super(parent, true);         
	//setBackground(Color.gray);
	//setLayout(new BorderLayout());
	this.setSize(new Dimension(H_SIZE, V_SIZE)) ;
	
	displayText="" ;
	txt = new JTextPane();

	txt.setEditable(false);
	// Two buttons "Close" and "Help"
	//txt = new JTextField();
	JScrollPane scroll = new JScrollPane(txt);
	//scroll.setPreferredSize(new Dimension(H_SIZE, V_SIZE-50)) ;
	scroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	JPanel p = new JPanel();
	p.add("Center",scroll);
	//p.add(txt);
	p.add("Sourth",new Button("Close"));
	//p.add(new Button("Help"));
	
	//add("South", p);
	this.getContentPane().add(p);
	//this.getContentPane().add(new Button("Close"));
	
	//resize(H_SIZE, V_SIZE);

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

    public void setText(String t) {
	displayText = t ;
	txt.setText(t);
    }

}

class StructureCommandListener 
    implements ActionListener {
    JTextField textfield ;
    SPICEFrame spice    ;
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    
    public StructureCommandListener (SPICEFrame spice_, JTextField textfield_) {
	super();
	spice = spice_ ;
	textfield = textfield_ ;
	
    }
    public void actionPerformed(ActionEvent event) {

	if ( spice.isLoading() ) {
	    logger.finest("loading data, please be patient");
	    return ;
	}
	String cmd = textfield.getText();
	spice.executeCmd(cmd);
	textfield.setText("");
	

    }

    
}

