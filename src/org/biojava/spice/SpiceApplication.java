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
 * Copyright for this cilode is held jointly by the individual
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

// to get config file via http
import java.net.HttpURLConnection ;
import java.net.URL;
import java.io.IOException ;

// gui
import java.awt.*          ;
import java.awt.event.*    ;
import javax.swing.JFrame  ;
import javax.swing.JPanel  ;
import javax.swing.BoxLayout;
import javax.swing.JList       ;
import javax.swing.JScrollPane ;
import javax.swing.DefaultListModel   ;
import javax.swing.JTextField   ;
import javax.swing.event.ListSelectionListener  ;
import javax.swing.event.ListSelectionEvent ;    

// menu
import javax.swing.JMenuBar    ;
import javax.swing.JMenuItem   ;

// features
import java.net.URL        ;
import java.util.Iterator  ;

public class SpiceApplication 
    extends  Frame
    implements SPICEFrame 
{

    //public static final String CONFIG_FILE = "config.xml";
    URL CONFIG_URL  ; 
    HashMap config      ;
    Structure structure ; 
    String pdbcode      ;
    int currentChain = -1 ;
    HashMap memoryfeatures; // all features in memory
    ArrayList features ;    // currently being displayed 


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

    JPanel leftPanel ;
    JPanel sharedPanel;
    JMenuBar menuBar ;
    JTextField getCom ;

    JTextField strucommand  ; 

    String[] txtColor     ;
    Color[] entColors ;
    Color oldColor ; 
    boolean first_load ;

    MenuBar  menu ;
    MenuItem exit ;
    MenuItem props ;
    MenuItem aboutspice ;
    MenuItem aboutdas ;
    SpiceApplication(String pdbcode_, URL config_url) {
	super();

	CONFIG_URL = config_url ;
	
	structure = null ;
	pdbcode = pdbcode_ ;

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

	config = readConfiguration();
		
	


	first_load = true ;



	this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


	// add the Menu
	menu = new MenuBar();
	this.setMenuBar(menu);
	Menu file = new Menu("File");
	Menu help = new Menu("Help");
	menu.add(file);
	menu.add(help);
	
	exit  = new MenuItem("Exit");
	props = new MenuItem("Properties");
	//exit.addActionListener(this);
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

	this.add(seq_pos,BorderLayout.NORTH);


	/// init Structure Panel
	structurePanel        = new StructurePanel();
	//structurePanel.setLayout(new BoxLayout(structurePanel, BoxLayout.X_AXIS));
	structurePanel.setSize(700, 700);
	structurePanel.setMinimumSize(new Dimension(400,400));
	this.add(structurePanel,BorderLayout.CENTER);


    	leftPanel = new JPanel() ;
	leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

	DefaultListModel model = new DefaultListModel();
	model.add(0,"loading...");
	ent_list=new JList(model);
	ent_list.setFixedCellWidth(20);
	//JScrollPane scrollingList = new JScrollPane(ent_list);
	EntListCommandListener entact = new EntListCommandListener(this);
	//ent_list.setSize(30,180);
	ent_list.setFixedCellWidth(40);
	ent_list.setFixedCellHeight(18);
	//ent_list.addItemListener(entact);
	ent_list.addListSelectionListener(entact);
	System.out.println("ENT_LIST " + ent_list);
		
	getCom = new JTextField(1);
	TextFieldListener txtlisten = new TextFieldListener(this,getCom);
	getCom.addActionListener(txtlisten);
	getCom.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
	leftPanel.add(ent_list,BorderLayout.NORTH);
	leftPanel.add(getCom,BorderLayout.SOUTH);
	
	ent_list.repaint();

	
	

	dascanv=new SeqFeatureCanvas(this);       
	dascanv.setForeground(new Color(255, 255, 255));
	dascanv.setBackground(new Color(0, 0, 0));
	dascanv.setSize(700,300);
	
	sharedPanel = new JPanel();
	sharedPanel.setLayout(new BoxLayout(sharedPanel, BoxLayout.X_AXIS));
	sharedPanel.add(leftPanel,BorderLayout.EAST);
	sharedPanel.add(dascanv,BorderLayout.WEST);
	sharedPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,300));
	this.add(sharedPanel,BorderLayout.SOUTH);
	
	strucommand = new JTextField()  ;
	strucommand.setText("enter command...");
	ActionListener listener = new StructureCommandListener(this,strucommand) ;
	strucommand.addActionListener(listener);
	strucommand.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
	this.add(strucommand,BorderLayout.SOUTH);

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
	System.out.println("ENT_LIST 2" + ent_list);

    }

    public boolean isLoading() {
	return first_load;
    }

    public void show(){
	super.show();
	//System.out.println("SHOW: getting Structure data from new thread");
	// and now load data ...
	getStructure(pdbcode);
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

    
    /** read the configuration from config.xml file */
    private HashMap readConfiguration() {

	HashMap co = new HashMap() ;
	
	/* old way to read from local file system 
	ClassLoader cl = this.getClass().getClassLoader();
	InputStream fs = null ;
	fs = cl.getResourceAsStream(CONFIG_FILE);
	InputSource is = new InputSource(fs);
	*/

	// new way to read from URL
	URL url = CONFIG_URL;
	HttpURLConnection huc ; 
	InputStream inStream  ; 

	try {
	    huc = (HttpURLConnection) url.openConnection();
	    inStream = huc.getInputStream();
	    
	} catch ( IOException e) {
	    e.printStackTrace();
	    return co ;
	}
	InputSource is = new InputSource(inStream);	
	//


	try {
	    co = parseConfigFile(is);
	} catch (Exception e) {
	    e.printStackTrace() ;
	}
	return co;
    }

    /** do the actual parsing of the config.xml file */
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


    /** return the urn of the structuresercer */
    public String getStructureServer() {
	HashMap strucconfig = (HashMap) config.get("structureserver");
	String u = (String) strucconfig.get("url");

	return   u;
    }

    /** return the urn of the structuresercer */
    public String getSequenceServer() {
	HashMap h = (HashMap) config.get("sequenceserver");
	String u = (String) h.get("url");
	return   u;
    }

    /** return the urn of the structuresercer */
    public String getAlignmentServer() {
	HashMap h = (HashMap) config.get("alignmentserver");
	String u = (String) h.get("url");
	return   u;
    }


    /** return the feature servers */
    public ArrayList getFeatureServers() {
	ArrayList featconfig = (ArrayList) config.get("featureservers");
	return featconfig ;
    }



    /** starts a new thread that retreives protein structure using the
	DAS structure command from some other server this thread will
	call the setStructure method to set the protein structure.
     */
    public void getStructure(String pdbcod) {
	//String server = "http://protodas.derkholm.net/dazzle/mystruc/structure?query=";
	
	/*
	Properties systemSettings = System.getProperties();
	systemSettings.put("proxySet", "true");
	systemSettings.put("proxyHost", "wwwcache.sanger.ac.uk");
	systemSettings.put("proxyPort", "3128");
	System.setProperties(systemSettings);
	*/
	LoadStructureThread thr = new LoadStructureThread(this,pdbcod);
	thr.start();

	/*
	boolean done = false ;
	while ( ! done) {
	    try {
		wait(300);
	    } catch (InterruptedException e) {
		e.printStackTrace();
		if ( structure != null ) {
		    done = true ;
		}
	    }
	}
	System.out.println("survived all loading");
	structurePanel.forceRepaint();
	*/
	/*
	DASStructureClient dasc= new DASStructureClient(server);
	Structure struc = null ;
	try{
	    struc = dasc.getStructure(pdbcode);	    
	    System.out.println("simpleDAS: got structure:");
	    System.out.println(struc);
	} catch (Exception e){
	    e.printStackTrace();
	}
	return struc ;
	*/
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
    
    /** proivde a biojava structure object to use in the master application */
    public void setStructure(Structure structure_ ) {
	//System.out.println("setting structure");
	
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
	String cmd = "select all; cpk off ; wireframe off ; cartoon on ; colour chain;select not protein and not solvent;spacefill 2.0;";
	structurePanel.executeCmd(cmd);
	
	//cmd = "select not protein and not solvent;spacefill 2.0;" ;
	//select not selected;cpk off;";
	//structurePanel.executeCmd(cmd);
    
	first_load = false ;
	setCurrentChain(0);
	updateDisplays();
    
    }

    /** send a command to Jmol */
    public void executeCmd(String cmd) {
	System.out.println("executing Command "+ cmd);
	structurePanel.executeCmd(cmd);	
    }

    public void setCurrentChain( int newCurrentChain) {
	System.out.println("setCurrentChain " + newCurrentChain);
	currentChain = newCurrentChain ;
	
	// update features to be displayed ...
	Chain chain = getChain(currentChain) ;
	String sp_id = chain.getSwissprotId() ;

	ArrayList tmpfeat = getFeaturesFromMemory(sp_id) ;
	
	if ( tmpfeat.size() == 0 ) {
	    getNewFeatures(sp_id) ;
	} else {
	    try { 
		setNewFeatures(tmpfeat);
	    } catch (Exception e) {e.printStackTrace();} ;
	    dascanv.setChain(chain,currentChain);
	}
	

    }

    private synchronized void getNewFeatures(String sp_id) {
	ArrayList featureservers = getFeatureServers() ;
	FeatureFetcher ff = new FeatureFetcher(featureservers,sp_id);
	ff.start() ;
	
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
	
	ArrayList tmpfeat = ff.getFeatures();
	memoryfeatures.put(sp_id,tmpfeat);
	try {
	    setNewFeatures(tmpfeat);
	} catch (Exception e) {e.printStackTrace();} ;
	Chain chain = getChain(currentChain) ;
	dascanv.setChain(chain,currentChain);
    }

    /**  update the currently displayed features */
    private void setNewFeatures(ArrayList tmpfeat) {
	features.clear();

		
	//features = new ArrayList();
	boolean secstruc = false ;
	String prevtype = "@prevtype" ;
	boolean first = true ;
	Feature feat  = null ;

	for (int i = 0 ; i< tmpfeat.size();i++) {
	    HashMap segment = (HashMap) tmpfeat.get(i);
	    // small bugfix, but just fights a cause,not the real symptom.
	    if ( segment == null ) { continue ; }
	    String type = (String) segment.get("TYPE") ;
		    
	    if ( type.equals("reference")){
		continue ;
	    }
		    
	    if ( ! type.equals(prevtype)){
		// a new feature!
		//System.out.println(type + " " + prevtype);

		if (! first) 
		    {
				
			/*
			  System.out.println(type + " " +(
			  type.equals("HELIX")  || 
			  type.equals("STRAND") || 
			  type.equals("TURN")  
			  ));
			*/
			if ( ! secstruc ) {
				    
				   
			    //System.out.println("adding feature " + feat);
			    features.add(feat);
				    
			} else if ( ! 
				    (
				     type.equals("HELIX")  || 
				     type.equals("STRAND") || 
				     type.equals("TURN")  
				     ) 
				    )
			    {
				secstruc = false ;
				//System.out.println("adding feature " + feat);
				features.add(feat);
			    }
		    }
		    
		first = false ;				
		if ( ! secstruc) {
		    feat = new Feature();
		}
			
	    }
		    
	    if (type.equals("STRAND")){
		secstruc = true ;
		segment.put("color",Color.yellow);
		segment.put("colorTxt","yellow");
		feat.setName("SECSTRUC");
	    }
		    
	    else if (type.equals("HELIX")) {
		secstruc = true ;
		segment.put("color",Color.red);
		segment.put("colorTxt","red");
		feat.setName("SECSTRUC");
	    }	

	    else if (type.equals("TURN")) {
		secstruc = true ;
		segment.put("color",Color.white);
		segment.put("colorTxt","white");
		feat.setName("SECSTRUC");
	    } 

	    else {
		secstruc = false ;
		segment.put("color",entColors[i%entColors.length]);
		segment.put("colorTxt",txtColor[i%entColors.length]);
		try {
		    feat.setName(type);
		} catch ( NullPointerException e) {
		    //e.printStackTrace();
		    feat.setName("null");
		}
	    }
		    
		    
	    feat.addSegment(segment);
	    prevtype = type;
	}

	if ( ! (feat==null))  features.add(feat);


	//System.out.println("repainting segdas ");
	this.paint(this.getGraphics());


    }

    // store all features in memory -> speed up
    private ArrayList getFeaturesFromMemory(String sp_id) {

	ArrayList arr = new ArrayList() ;
	
	for (Iterator ti = memoryfeatures.keySet().iterator(); ti.hasNext(); ) {
	    String key = (String) ti.next() ;
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

	// browse through all groups and only keep those that are amino acids...
	ChainImpl n = new ChainImpl() ;

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
	if (first_load)       return ;		
	if ( start    < 0 ) return ;
	if (chainNumber < 0 ) return ;
	
	
	String cmd = getSelectStr( chainNumber,  start,  end);
	cmd +=  " spacefill on; " ;
	if ( colour  != "") {
	    cmd += "colour " +colour ;
	    colour(chainNumber,start,end,colour) ;
	}
	structurePanel.executeCmd(cmd);
	structurePanel.forceRepaint();
	
    }
    public void highlite(int chainNumber, int start, int end) {
	highlite(chainNumber, start, end, "");
	
    }

    public void highlite(int chainNumber, int seqpos, String colour) {
	
	if (first_load)       return ;		
	if ( seqpos     < 0 ) return ;
	if (chainNumber < 0 ) return ;
	
	String cmd = getSelectStr( chainNumber,  seqpos);
	cmd +=  " spacefill on ;" ;
	structurePanel.executeCmd(cmd);
	structurePanel.forceRepaint();

	if ( colour  != "") {
	    colour(chainNumber,seqpos,colour) ;
	}

	
	
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
	Chain chain = getChain(chain_number) ;
	
	if ( ! ((seqpos >= 0) && (seqpos < chain.getLength()))) {
	   return "" ;
	}
	
	dascanv.highlite(chain_number,seqpos);
	Group g = chain.getGroup(seqpos);
	if (! g.has3D()){
	    return "" ;
	}
	String pdbcod = g.getPDBCode() ;
	String pdbname = g.getPDBName() ;
	String chainid = chain.getName() ;
	//System.out.println("selected "+pdbcod+" " +pdbname);
	String cmd =  "select "+pdbcod+chainid+";" ;
	return cmd ;

    }

    /** select a range of  residue */
    public void select(int chain_number, int start, int end) {
	String cmd = getSelectStr( chain_number,  start,  end);
	if (cmd.equals("")) { return ; } 
	cmd += " set display selected;" ;
	structurePanel.executeCmd(cmd);
	structurePanel.forceRepaint();
    }

    /** select a single residue */
    public void select(int chain_number,int seqpos){
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
	dascanv.setFeatures(features);
	dascanv.paint(dascanv.getGraphics());
	
	
	sharedPanel.paint(sharedPanel.getGraphics());
	//leftPanel.paint(leftPanel.getGraphics());
	ent_list.paint(ent_list.getGraphics());

	sharedPanel.show();
	this.repaint();
    }

    


    private class FeatureFetcher extends Thread {
   

    
	private ArrayList featureservers ;
	private boolean done ;
	private ArrayList features ;
	private String sp_id ;

	FeatureFetcher (ArrayList featservers,String spid ) 
	{
	    featureservers = featservers ;
	    done = false ;
	    features = new ArrayList();
	    sp_id = spid ;
	}

	public void run() {
	    retreive() ;
	}
	public boolean isDone(){
	    //System.out.println("featurefetcher:" +done);
	    return done ;
	}

	public ArrayList getFeatures(){
	    return features;
	}
	public synchronized void retreive() {
	    
	    try {
		done = false;
		/// at the moment  only servers serving in sp coordinates is allowed ..		
		for ( int f =0;f<featureservers.size();f++) {	    
		    HashMap featureserver = (HashMap) featureservers.get(f) ;
		    
		    // String queryString = "http://das.ensembl.org/das/swissprot/features?segment="+ sp_id ;
		    String url = (String) featureserver.get("url");
		    String queryString = url + "features?segment="+ sp_id ;
		    
		    System.out.println("contacting DAS server to retreive features @ " + queryString) ;
		    notifyAll();
		    
		    URL spUrl = new URL(queryString);
		    DAS_FeatureRetreive ftmp = new DAS_FeatureRetreive(spUrl);
		    
		    ArrayList tmp = ftmp.get_features();
		    for (int i=0; i<tmp.size();i++){
			HashMap feat = (HashMap)tmp.get(i);			
			//System.out.println("got feature: "+feat);
			features.add(feat) ;		
		    } 
		    notifyAll();


		}
		done = true;
	    } catch ( Exception e) {
		e.printStackTrace();
	    }
	    notifyAll();
	    

	}
 



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

	if ( event.target == props) {
	    System.out.println("modify properties");
	    return true;
	}
	else if ( event.target == exit) {
	    dispose();
	    return true;
	}
	else if ( event.target == aboutdas ) {
	    System.out.println("about DAS");
	    //AboutDialog asd = new AboutDialog(this);
	    //asd.setText("DAS homepage: http://www.biodas.org") ;
	    //asd.show();
	    
	    Dialog dialog = new Dialog(this, "About DAS", true);

	    FlowLayout layout = new FlowLayout();
	    
	    dialog.setLayout(layout);

	    TextField textField = new TextField("DAS homepage: http://www.biodas.org", 20);
	    
	    Button button = new Button("OK");
	    
	    dialog.add(button);
	    
	    dialog.add(textField);
	    dialog.resize(200, 200);
	    dialog.show();




	    return true;
	}
	else if ( event.target ==  aboutspice ) {
	    System.out.println("about SPICE");
	    AboutDialog asd = new AboutDialog(this);
	    asd.setText("The SPICE Applet. V 0.1 (C) Andreas Prlic 2004") ;
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
	System.out.println(event.getActionCommand());
	System.out.println(event.getModifiers());
	System.out.println(event.paramString());
    
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


class AboutDialog extends Dialog
{
    static int H_SIZE = 200;
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
