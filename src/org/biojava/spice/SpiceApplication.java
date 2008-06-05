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
 * Created on 06.10.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.spice ;

import org.biojava.spice.alignment.StructureAlignmentBuilder;
import org.biojava.spice.config.*;
import org.biojava.spice.das.SpiceDasSource;
import org.biojava.spice.gui.*;
import org.biojava.spice.gui.aligchooser.MenuAlignmentListener;
import org.biojava.spice.jmol.JmolSpiceTranslator;
import org.biojava.spice.jmol.StructurePanel;
import org.biojava.spice.jmol.StructurePanelListener;

import org.biojava.bio.structure.*;
import org.biojava.dasobert.dasregistry.Das1Source;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;
import org.biojava.dasobert.eventmodel.AlignmentEvent;
import org.biojava.dasobert.eventmodel.AlignmentListener;
import org.biojava.dasobert.eventmodel.SequenceListener;
import org.biojava.dasobert.eventmodel.StructureEvent;
import org.biojava.dasobert.eventmodel.StructureListener;

//to get config file via http
import java.net.URL;

//some utils 
import java.text.MessageFormat;
import java.util.HashMap   ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.Map;

//logging
import java.util.logging.* ;
import java.util.Iterator  ;


//gui
import java.awt.BorderLayout;
import java.awt.Dimension                       ;
import java.awt.event.*                         ;

import javax.swing.Box                          ;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane                   ;
import javax.swing.JScrollPane                  ;
import javax.swing.JTextField                   ;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.ImageIcon                    ;
import javax.swing.BorderFactory                ;
import javax.swing.JMenuBar                     ;
import javax.swing.JMenu                        ;
import javax.swing.JMenuItem                    ;

//import java.util.Map;
import javax.swing.JDialog;
import java.awt.Container;

import org.biojava.spice.manypanel.BrowserPane;
import org.biojava.spice.manypanel.eventmodel.DasSourceEvent;
import org.biojava.spice.manypanel.eventmodel.DasSourceListener;
import org.biojava.spice.manypanel.eventmodel.StructureAlignmentListener;
import org.biojava.spice.manypanel.renderer.SegmentPopupFrame;
import org.biojava.spice.server.SpiceServer;
import org.biojava.spice.utils.BrowserOpener;
import org.biojava.spice.utils.UniProtAccessionCodeTools;
import org.jmol.api.JmolViewer;



/** the main application layer of SPICE
 * do not interact with this class directly, but interact with SPICEFrame interface.
 *
 * @author Andreas Prlic
 */
public class SpiceApplication 
extends JPanel
implements SPICEFrame, 
ConfigurationListener
{         
	private static final long serialVersionUID = 8273923744127087422L;       

	public static Logger logger =  Logger.getLogger(SpiceDefaults.LOGGER);

	static String baseName = "spice";

	URL[] REGISTRY_URLS    ; // the url to the registration server

	static int    DEFAULT_Y_SCROLL = 50 ;    

	RegistryConfiguration config      ;
	Structure structure ; 
	String pdbcode      ;
	String pdbcode2     ; // only set if displaying structure alignments 

	Chain currentChain ;
	int currentChainNumber  ;  

	List<Map<String,String>> loadQueue;

	JmolSpiceTranslator jmolSpiceTranslator;
	StructurePanelListener structurePanelListener ;   
	SelectionPanel selectionPanel;

	JSplitPane sharedPanel;
	JSplitPane mainsharedPanel;      
	JSplitPane seqSplitPane  ;

	JMenuItem lock;
	JMenuItem unlock;
	JMenuItem lockMenu;

	SpiceDasSource[] knownSources;

	JTextField getCom ;
	List knownFeatureLinks;
	StructurePanel structurePanel;
	StructureCommandPanel  strucommand; 
	StatusPanel statusPanel ;

	boolean first_load ;
	boolean selectionLocked ;
	boolean structureAlignmentMode ;

	boolean configLoaded ;
	SpiceMenuListener spiceMenuListener;
	BrowseMenuListener browseMenu;
	BrowserPane browserPane ;

	SpiceTabbedPane spiceTabbedPane;
	SpiceStartParameters startParameters;
	SpiceServer spiceServer;
	JMenuBar menu;
	JMenu alignmentMenu;
	String preferredStructureAlignmentServerName;
	Box vBox;

	static SegmentPopupFrame popupFrame = new SegmentPopupFrame();


	String currentAccessionCode = "";
	String currentType          = "";

	/** 
	 * start the spice appplication
	 * 
	 * @param params the parameters for starting up...
	 */
	public SpiceApplication( SpiceStartParameters params) {
		super();
		logger.setLevel(SpiceDefaults.LOG_LEVEL);

		startParameters = params;

		knownSources = new SpiceDasSource[0]; 

		// a few error checks.
		checkStartParameters();

		// init variables ...
		setCurrentChain(null,-1);
		selectionLocked = false ;
		configLoaded    = false;        
		structure       = null ;
		pdbcode         = null ;
		pdbcode2        = null ;
		preferredStructureAlignmentServerName = "";
		structureAlignmentMode = false ; 

		loadQueue = new ArrayList<Map<String,String>>();

		initPanels1();

		// first thing is to start das - registry communication        
		requestRegistryConfig();        

		// init all panels, etc..
		initPanels2();

		initListeners();

	}

	private void initPanels1(){
		
		if ( logger.isLoggable(Level.FINEST)) {
			logger.finest("start parameters: " + startParameters.getCodetype() + " " + startParameters.getCode());
			System.out.println("start parameters: " + startParameters.getCodetype() + " " + startParameters.getCode());
		}
		
		if (startParameters.getStructureAlignmentMode().equals(SpiceDefaults.CASPCOORDSYS)){
			browserPane = new BrowserPane(startParameters.getPdbcoordsys());
		} else  {
			
			if ( startParameters.getCodetype().equalsIgnoreCase(SpiceDefaults.MODELType)) {
			
				
				browserPane = new BrowserPane(SpiceDefaults.MODELCOORDSYS,
						startParameters.getUniprotcoordsys(), 
						startParameters.getEnspcoordsys());
			} else if ( startParameters.getCodetype().equalsIgnoreCase("GENCODE")) {
                browserPane = new BrowserPane(startParameters.getPdbcoordsys(),
                        startParameters.getUniprotcoordsys(), 
                        SpiceDefaults.GENCODECOORDSYS);
            } else {
            
			// init the 2D display
			browserPane = new BrowserPane(startParameters.getPdbcoordsys(),
					startParameters.getUniprotcoordsys(), 
					startParameters.getEnspcoordsys());
            }
		}

		structurePanel      = new StructurePanel();
		structurePanel.initJmolDisplay();
		jmolSpiceTranslator = new JmolSpiceTranslator();
		structurePanel.addJmolStatusListener(jmolSpiceTranslator);

		structurePanelListener = new StructurePanelListener(structurePanel);

	}

	private void requestRegistryConfig(){

		config = null;

		URL[] registries = getAllRegistryURLs();

		RegistryConfigIO regi = new RegistryConfigIO(registries);
		if ( startParameters.isNoRegistryContact()) {
			regi.setNoUpdate(true);
		} else {
			regi.addConfigListener(this);            
			// we do this via the swing main thread because we want to see
			// a busy progress bar...
			javax.swing.SwingUtilities.invokeLater(regi);
		}
	}



	public String getPreferredStructureAlignmentServerName() {
		return preferredStructureAlignmentServerName;
	}

	public void setPreferredStructureAlignmentServerName(
			String preferredStructureAlignmentServerName) {
		this.preferredStructureAlignmentServerName = preferredStructureAlignmentServerName;
	}

	private void initPanels2(){


		statusPanel    = new StatusPanel(this);
		statusPanel.setBorder(BorderFactory.createEmptyBorder());

		strucommand    = new StructureCommandPanel(structurePanelListener);

		// initMenu should be before arrangePanels, because structurealignmentchooser needs the alignmentmenu
		spiceMenuListener = new SpiceMenuListener(this,structurePanelListener) ;

		menu = initMenu();

		vBox = arrangePanels(statusPanel,structurePanel,browserPane,strucommand,"left"); 

		this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));

		this.add(vBox);

	}

	/** return all StructurteAlignmentListeners
	 * 
	 * @return components that are listening to the calculation of new StructureAlignments
	 * 
	 */
	public StructureAlignmentListener[] getStructureAlignmentListeners(){

		StructureAlignmentListener li = selectionPanel.getStructureAlignmentListener();
		return new StructureAlignmentListener[] {li};

	}

	private URL[] getAllRegistryURLs(){
		URL[] regis;
		int numberregis = 1 ;
		URL[] registryurls = startParameters.getRegistryurls() ;
		if (registryurls != null) {
			numberregis += registryurls.length;
		}
		try {
			URL primaryRegistry = new URL(startParameters.getRegistry());
			if ( primaryRegistry != null ){
				regis = new URL[numberregis];
				regis[0] = primaryRegistry;
				for ( int i =0; i<registryurls.length; i++) {
					System.out.println(registryurls[i]);
					regis[i+1] = registryurls[i];
				}   
			} else {
				regis = registryurls;
			}
		} catch (Exception e){
			regis = registryurls;
		}

		return regis;
	}


	/** do some check to avoid garbage input
	 * 
	 *
	 */
	 private void checkStartParameters(){

		int seqSelectStart = startParameters.getSeqSelectStart();
		int seqSelectEnd   = startParameters.getSeqSelectEnd();

		if ( seqSelectStart > seqSelectEnd){
			int tmp = seqSelectEnd;
			startParameters.setSeqSelectEnd(seqSelectStart);
			startParameters.setSeqSelectStart( tmp);
		}

		if ( seqSelectEnd >= 0 )
			startParameters.setSeqSelectEnd(seqSelectEnd);
		else 
			startParameters.setSeqSelectEnd(seqSelectStart);


		// only select structure if seq. is not selected...
		if ( seqSelectStart > 0) {
			startParameters.setPdbSelectStart( null);
			startParameters.setPdbSelectEnd(null);;
		}
	 }


	 /** return the chain / alignment selection panel in the middle
	  * 
	  * @return the SelectionPanel
	  */
	 public SelectionPanel getSelectionPanel(){
		 return selectionPanel;
	 }

	 public SpiceTabbedPane getSpiceTabbedPane(){
		 return spiceTabbedPane;
	 }

	 public void setSpiceTabbedPane(SpiceTabbedPane tab) {
		 spiceTabbedPane = tab;        
	 }

	 public SpiceServer getSpiceServer(){
		 return spiceServer;
	 }

	 public void setSpiceServer(SpiceServer server){     
		 spiceServer = server; 
	 }

	 /**
	  *  statusPanel   = StatusPanel(); 
	  * seq_pos        = new JTextField();
	  * structurePanel = new StructurePanel(this);	
	  * dascanv        = new SeqFeatureCanvas(this);
	  * strucommand    = new StructureCommandPanel()  ;
	  * structureLocation location of structure, either "top", left, right or bottom


	  * @return a Box containing the Panels.
	  */
	 private Box arrangePanels(StatusPanel statusPanel,
			 StructurePanel structurePanel,
			 BrowserPane browserPane, 
			 StructureCommandPanel strucommand,
			 String structureLocation){

		 Box vBox = Box.createVerticalBox();

		 Box vBox2 = Box.createVerticalBox();
		 structurePanel.setMinimumSize(new Dimension(200,200));
		 vBox2.add(structurePanel);

		 strucommand.setMaximumSize(new Dimension(Short.MAX_VALUE,30));    
		 vBox2.add(strucommand);

		 if ( alignmentMenu == null)
			 logger.warning("alignment menu has not been initialized!");
		 selectionPanel = new SelectionPanel(alignmentMenu);
		 selectionPanel.setStructurePanel(structurePanel);

		 JScrollPane chainPanel = new JScrollPane(selectionPanel);
		 
		 selectionPanel.setPreferredSize(new Dimension(60,60));

		 chainPanel.setBorder(BorderFactory.createEmptyBorder());

		 browserPane.setBorder(BorderFactory.createEmptyBorder());
		 browserPane.setPreferredSize(new Dimension(200,200));
		 sharedPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				 chainPanel, browserPane);
		 sharedPanel.setOneTouchExpandable(true);
		 sharedPanel.setResizeWeight(0);
		 sharedPanel.setBorder(BorderFactory.createEmptyBorder());
		 selectionPanel.setSplitPanel(sharedPanel);

		 if (structureLocation.equals("top"))
			 mainsharedPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, vBox2,sharedPanel);
		 else if  (structureLocation.equals("bottom"))
			 mainsharedPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sharedPanel,vBox2);
		 else if  (structureLocation.equals("left"))
			 mainsharedPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, vBox2,sharedPanel);
		 else if  (structureLocation.equals("right"))
			 mainsharedPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sharedPanel,vBox2);

		 mainsharedPanel.setOneTouchExpandable(true);
		 mainsharedPanel.setResizeWeight(0.6);

		 mainsharedPanel.setPreferredSize(new Dimension(790, 590));

		 Box hBox1 =  Box.createHorizontalBox();
		 hBox1.add(mainsharedPanel);
		 vBox.add(hBox1);


		 statusPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
		 statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));

		 Box hBox = Box.createHorizontalBox();
		 hBox.setBorder(BorderFactory.createEmptyBorder());
		 hBox.add(statusPanel);
		 vBox.add(hBox);
		 //vBox.add(hBox1);

		 //vBox.add(loggingPanel);
		 return vBox;
	 }


	 /** unregister all event listeners and make the application ready for being garbage collected
	  * 
	  *
	  */
	 public void clearListeners(){

		 spiceMenuListener.clearListeners();
		 spiceMenuListener = null;
		 browserPane.clearListeners();
		 jmolSpiceTranslator.clearListeners();
		 selectionPanel.clearListeners();
		 selectionPanel.setStructurePanel(null);


	 }

	 /** initialize the listeners of the various componentns 
	  * 
	  *
	  */
	 private void initListeners(){

		 jmolSpiceTranslator.addPDBSequenceListener(statusPanel);
		 SequenceListener[] li = browserPane.getPDBSequenceListener();
		 for ( int i = 0 ; i < li.length;i++){
			 jmolSpiceTranslator.addPDBSequenceListener(li[i]);
			 // TODO: enable next line (???)
			 //selectionPanel.addPDBSequenceListener(li[i]);

		 }

		 browserPane.addPDBSequenceListener(statusPanel);
		 browserPane.addUniProtSequenceListener(statusPanel);
		 browserPane.addStructureListener(structurePanelListener);
		 browserPane.addPDBPositionListener(structurePanelListener);
		 browserPane.addPDBSpiceFeatureListener(statusPanel);
		 browserPane.addPDBPositionListener(statusPanel);
		 browserPane.addStructureListener(statusPanel);
		 browserPane.addStructureListener(browseMenu.getPDBListener());
		 browserPane.addUniProtSequenceListener(browseMenu.getUniProtListener());
		 browserPane.addEnspSequenceListener(browseMenu.getEnspListener());

		 // listener for das sources
		 browserPane.addPDBSpiceFeatureListener(structurePanelListener);


		 MyDasSourceListener mdsl = new MyDasSourceListener();
		 browserPane.addDasSourceListener(mdsl);
		 browserPane.addPDBSequenceListener(spiceMenuListener);

		 // things related to selecting chains
		 //chainDisplay = new SpiceChainDisplay(chainList);
		 SpiceChainDisplay chainDisplay = selectionPanel.getChainDisplay();
		 browserPane.addStructureListener(chainDisplay);
		 browserPane.addStructureListener(jmolSpiceTranslator);

		 browserPane.addStructureListener(selectionPanel);

		 //chainList.addListSelectionListener(chainDisplay);
		 chainDisplay.addStructureListener(browserPane.getStructureManager());
		 chainDisplay.addStructureListener(structurePanelListener);
		 chainDisplay.addStructureListener(statusPanel);
		 chainDisplay.addStructureListener(jmolSpiceTranslator);

		 //chainDisplay.addStructureListener(spiceMenuListener);

		 selectionPanel.addStructureListener(browserPane.getStructureManager());
		 selectionPanel.addStructureListener(jmolSpiceTranslator);
		 selectionPanel.addStructureListener(structurePanelListener);        
		 selectionPanel.addStructureListener(browseMenu.getPDBListener());
		 selectionPanel.addStructureListener(browserPane.getTopAlignmentManager());
	 }

	 public void setMenu(JMenuBar menu) {
		 this.menu=menu;
	 }


	 public JMenuBar getMenu(){
		 if ( menu == null)
			 menu = initMenu();
		 return menu;
	 }

	 public Logger getLogger(){
		 return logger;
	 }
	 /**
	  * @returns the Menu to be displayed on top of the application
	  */
	 private JMenuBar initMenu() {

		 menu = new JMenuBar();

		 // the three menus
		 JMenu file = MenuCreator.createFileMenu(spiceMenuListener);
		 menu.add(file); 
		 // DIsplay submenu

		 JMenu display = MenuCreator.createDisplayMenu(spiceMenuListener);
		 menu.add(display);

		 // unique action listener for the browse buttons
		 browseMenu = new BrowseMenuListener();
		 JMenu bm = browseMenu.getBrowsermenu();        
		 menu.add(bm);

		 alignmentMenu = MenuCreator.createAlignmentMenu(spiceMenuListener);
		 menu.add(alignmentMenu);

		 menu.add(Box.createGlue());

		 // Help submenu
		 JMenu help = MenuCreator.createHelpMenu(spiceMenuListener);
		 menu.add(help);

		 return menu ;

	 }



	 /** Returns an ImageIcon, or null if the path was invalid. 
	  * @param path the path to the icon
	  * @return ImageIcon object*/
	 public static ImageIcon createImageIcon(String path) {

		 java.net.URL imgURL = SpiceApplication.class.getResource(path);

		 if (imgURL != null) {
			 return new ImageIcon(imgURL);
		 } else {
			 logger.log(Level.FINE,"Couldn't find file: " + path);
			 return null;
		 }

	 }


	 public void setSpiceStartParameters(SpiceStartParameters parameters){
		 startParameters = parameters;        
		 dealWithStartParameters();
		 testAddLocalServer();

		 setDasSources();
	 }


	 public SpiceStartParameters getSpiceStartParameters(){
		 return startParameters;
	 }

	 /* (non-Javadoc)
	  * @see org.biojava.spice.SPICEFrame#load(java.lang.String, java.lang.String)
	  */
	 public void load(String type, String code){
		 logger.info("loading " +type + " " +code);

		 Map<String,String> m = new HashMap<String, String>();
		 m.put("type",type);
		 m.put("code",code);

		 loadQueue.add(m);

		 if (config != null)            
			 processNextInQueue();
	 }

	 /** force a re-load of all the currently displayed data
	  * 
	  *
	  */
	 public void reload(){
		 logger.info("reloading display");

		 structurePanel.clearDisplay();
		 browserPane.clearDisplay();
		 selectionPanel.clearDisplay();

		 load(currentType, currentAccessionCode);

	 }

	 private void processNextInQueue(){

		 if ( loadQueue.size() <1)
			 return;

		 Map<String, String> m =  loadQueue.get(0);

		 String type = (String) m.get("type");
		 String code = (String) m.get("code");

		 loadQueue.remove(0);

		 String msg = "SpiceApplication load: " + type + " " + code;
		 System.out.println(msg);

		 //MyDataLoadRunnable runner = new MyDataLoadRunnable(this,type,code);

		 //Thread t = new Thread(runner);
		 //t.start();

		 if (logger.isLoggable(Level.FINEST)){
			 logger.finest(msg);
		 }

		 if (type.equals(SpiceDefaults.PDBType) ||
				 type.equalsIgnoreCase(SpiceDefaults.MODELType)){

			 this.loadStructure(code,type);

		 }
		 else if (type.equals(SpiceDefaults.UniProtType)) {
			 //logger.info("got uniprot");
			 // connect to Uniprot -pdb alignment service, get PDB code and load it ...
			 loadUniprot(code);

		 }
		 else if (type.equals(SpiceDefaults.EnspType) || 
                 type.equalsIgnoreCase(SpiceDefaults.GENCODEType)) {
			 loadEnsp(code,type);

		 }
		 else if (type.equals(SpiceDefaults.AlignmentType)){
			 // spice will be running in structure alignment mode
			 String aligcs = startParameters.getStructureAlignmentMode();
			 DasCoordinateSystem dcs = DasCoordinateSystem.fromString(aligcs);
			 loadAlignment(code,dcs);

		 }
		 else {
			 // unknown code type!     
			 String warn =  ResourceManager.getString("org.biojava.spice.SpiceApplication.load.wrongType");

			 Object[] arg = {type};
			 String txt =  MessageFormat.format(warn,arg);
			 logger.warning(txt);
			 return;
		 }


	 }




	 public RegistryConfiguration getConfiguration() {
		 return config ;
	 }



	 /** start a new thead that retrieves uniprot sequence, and if available
     protein structure
	  */
	 protected void loadUniprot(String uniprot) {

		 currentType = SpiceDefaults.UniProtType;
         
         if ( UniProtAccessionCodeTools.isEntryName(uniprot)){
             String ac = UniProtAccessionCodeTools.translateName2Accession(uniprot);
             if ( ! ac.equals(""))
                 uniprot = ac;
         }
         
		 currentAccessionCode = uniprot;

		 logger.info("SpiceApplication loadUniprot " + uniprot);
		 System.setProperty("SPICE:drawStructureRegion","false");
		 if ( config == null){
			 // we have to wait until contacting the DAS registry is finished ...
			 Map<String, String> m = new HashMap<String,String>();
			 m.put("type",SpiceDefaults.UniProtType);
			 m.put("code", uniprot);
			 loadQueue.add(m);
			 return;
		 }

		 clear();

		 browserPane.triggerLoadUniProt(uniprot);
	 }

	 /** start a new thead that retrieves uniprot sequence, and if available
    protein structure
	  */
	 protected void loadEnsp(String ensp) {
	     loadEnsp(ensp, SpiceDefaults.EnspType);
     }
     
     protected void loadEnsp(String ensp, String type){
         
		 //logger.info("SpiceApplication loadEnsp" + ensp);
		 currentType = type;
		 currentAccessionCode = ensp;

		 System.setProperty("SPICE:drawStructureRegion","false");
		 if ( config == null){
			 // we have to wait until contacting the DAS registry is finished ...
			 Map<String,String> m = new HashMap<String, String>();
			 m.put("type",type);
			 m.put("code", ensp);
			 loadQueue.add(m);
			 return;
		 }


		 clear();
		 browserPane.triggerLoadENSP(ensp);


	 }


	 
     

	 /** starts a new thread that retreives protein structure using the
     DAS structure command from some other server this thread will
     call the setStructure method to set the protein structure.
     *
     * @param pdbcod an accession code
     * @param type is it SpiceDefaults.PDBTYpe or SpiceDefaults.MODELType
	  */
	 protected void loadStructure(String pdbcod,String type) {
		 
		 currentType = type;
		 currentAccessionCode = pdbcod;

		 System.setProperty("SPICE:drawStructureRegion","false");
		 //currentChain = null ;
		 setCurrentChain(null,-1);
		 if (logger.isLoggable(Level.FINER)) {
			 logger.entering(this.getClass().getName(), "loadStructure",  new Object[]{pdbcod});
		 }

		 logger.log(Level.INFO,"getting new structure "+pdbcod);

		 if (logger.isLoggable(Level.FINEST)) {
			 logger.finest("SpiceApplication: getStructure "+ pdbcod);
		 }

		 if ( config == null){
			 // we have to wait until contacting the DAS registry is finished ...
			 Map<String,String> m = new HashMap<String, String>();
			 m.put("type",type);
			 m.put("code", pdbcod);
			 loadQueue.add(m);
			 return;
		 }


		 //this.setLoading(true);


		 clear();
		 pdbcode = pdbcod ;
		 browserPane.triggerLoadStructure(pdbcode);

	 }

	 /** load an alignment from an alignment server
	  * 
	  * @param alignmentCode the accession code to be sent to the alignment server
	  * @param aligCs the coordinate system of the alignment server to use
	  */
	 public void loadAlignment(String alignmentCode, DasCoordinateSystem aligCs){

		 currentType = SpiceDefaults.AlignmentType;
		 currentAccessionCode = alignmentCode;
		 if (logger.isLoggable(Level.INFO)){
			 logger.info("loading Structure alignment for coordinate system " + aligCs.toString());
		 }
		 List<SpiceDasSource> aligservers = config.getServers("alignment", aligCs.toString());
		 if (logger.isLoggable(Level.INFO)){
			 logger.info("found " +aligservers.size() + " alignment servers");
		 }
		 SpiceDasSource[] ads = aligservers.toArray(new SpiceDasSource[aligservers.size()]);

		 List<SpiceDasSource> strucservers = config.getServers("structure");
		 if (logger.isLoggable(Level.INFO)){
			 logger.info("found " + strucservers.size() + " structure servers");
		 }
		 SpiceDasSource[] sds = strucservers.toArray(new SpiceDasSource[strucservers.size()]);

		 StructureAlignmentBuilder sacreator = new StructureAlignmentBuilder(aligCs);
		 //sacreator.addStructureListener(structurePanelListener);
		 //sacreator.setStructurePanel(structurePanel);
		 sacreator.setSelectionPanel(selectionPanel);
		 sacreator.setAlignmentServers(ads);
		 sacreator.setStructureServers(sds);

		 AlignmentListener ali = new MyAlignmentListener(this,spiceTabbedPane);

		 sacreator.addAlignmentListener(ali);
		 MenuAlignmentListener menuListener = new MenuAlignmentListener(alignmentMenu,selectionPanel.getAlignmentChooser());
		 sacreator.addAlignmentListener(menuListener);

		 sacreator.request(alignmentCode);
	 }


	 /** clear the displayed data */
	 public void clear(){

		 setCurrentChain(null,-1);

		 //pdbMenu.setEnabled(false);
		 //upMenu.setEnabled(false);
		 //dastyMenu.setEnabled(false);
		 //proviewMenu.setEnabled(false);
		 Structure s = new StructureImpl();

		 structurePanelListener.setStructure(s);
		 browseMenu.clear();
	 }


	 public static SegmentPopupFrame getPopupFrame() {
		 return popupFrame;
	 }


	 public BrowserPane getBrowserPane(){
		 return browserPane;
	 }

	 /** test if a local DAS source is defined in the startup parameters
	  * If yes, add it to the list of servers in config
	  */
	 private void testAddLocalServer(){
		 if ( config == null){
			 return ;
		 }
		 String localurl = startParameters.getLocalServerURL();

		 if (! ((localurl == null) || (localurl.equals("")))) {
			 String localcood = startParameters.getLocalServerCoordSys();
			 String localname = startParameters.getLocalServerName();
			 String localcapability = startParameters.getLocalServerCapability();
			 
			 String localDisplayType = startParameters.getLocalServerDisplayType();
			 if (localcapability == null)
				 localcapability = "features";
			 //logger.info("adding new local DAS source");

			 SpiceDasSource localDs = new SpiceDasSource();
			 localDs.setUrl(localurl);
			 localDs.setStatus(true);
			 localDs.setRegistered(false);
			 localDs.setNickname(localname);
			 logger.info(localDisplayType);
			 if (( localDisplayType != null) && 
					 (! localDisplayType.equals(""))) {
				 //System.out.println("localDIsplayType :" + localDisplayType);
				 localDs.setDisplayType(localDisplayType);
			 }
			 
			 if (startParameters.isLocalUnlimitedFeatures()){
				 localDs.setUnlimitedFeatures(true);
			 }

			 
			 String[] caps = new String[1];
			 caps[0]=localcapability;
			 localDs.setCapabilities(caps);
			 DasCoordinateSystem dcs = DasCoordinateSystem.fromString(localcood);
			 DasCoordinateSystem[] csa = new DasCoordinateSystem[1];
			 csa[0]=dcs;
			 localDs.setCoordinateSystem(csa);

			 config.addServerAtStart(localDs);

			 // and clean up - we do not need to do this a second time again after ...
			 startParameters.setLocalServerURL("");              
			 startParameters.setLocalServerName(SpiceDasSource.DEFAULT_NICKNAME);
		 }
	 }



	 private void dealWithStartParameters(){

		 if ( startParameters.getRasmolScript() != null){
			 // only execute the rasmol script command the first time.
			 structurePanelListener.setRasmolScript(startParameters.getRasmolScript());
			 startParameters.setRasmolScript( null);
		 }

		 if ( startParameters.getDisplayMessage() != null ){
			 displayMessage(startParameters.getDisplayMessage(),
					 startParameters.getMessageWidth().intValue(),
					 startParameters.getMessageHeight().intValue());
			 startParameters.setDisplayMessage(null);
		 }

		 int seqSelectStart = startParameters.getSeqSelectStart();
		 int seqSelectEnd   = startParameters.getSeqSelectEnd();

		 if ( seqSelectStart >=0 ){
			 //dascanv.selectedSeqRange(seqSelectStart, seqSelectEnd);
			 //structurePanelListener.selectedSeqRange(seqSelectStart, seqSelectEnd);
			 //seqTextPane.selectedSeqRange(seqSelectStart, seqSelectEnd);

			 browserPane.setSeqSelection(seqSelectStart,seqSelectEnd);

			 // lock selectiion
			 //dascanv.selectionLocked(true);
			 //structurePanelListener.selectionLocked(true);
			 //seqTextPane.selectionLocked(true);

			 //reset...
			 seqSelectStart = -1;
			 seqSelectEnd = -1;
			 startParameters.setSeqSelectStart(-1);
			 startParameters.setSeqSelectEnd(-1);
		 } else {
			 // perhaps a PDB range has been provided
			 String pdbSelectStart = startParameters.getPdbSelectStart();
			 String pdbSelectEnd   = startParameters.getPdbSelectEnd();

			 if ( pdbSelectStart != null){
				 //String cmd = "select " + pdbSelectStart + " - " + pdbSelectEnd + "; set displaySelected";
				 //structurePanelListener.executeCmd(cmd);
				 structurePanelListener.setPDBSelectStart(pdbSelectStart);
				 structurePanelListener.setPDBSelectEnd(pdbSelectEnd);

				 pdbSelectStart = null;
				 pdbSelectEnd = null;
				 startParameters.setPdbSelectStart(null);
				 startParameters.setPdbSelectEnd(null);
			 }
		 }



		 //updateDisplays();
		 //notifyAll();

	 }


	 /** display a dialog with a message... 
	  * 
	  * @param txt
	  * @param width
	  * @param height
	  */
	 private void displayMessage(String msg, int width, int height){

		 JDialog dialog = new JDialog();

		 dialog.setSize(new Dimension(width,height));

		 JEditorPane txt = new JEditorPane("text/html", msg);
		 txt.setEditable(false);

		 txt.addHyperlinkListener(new HyperlinkListener(){
			 public void hyperlinkUpdate(HyperlinkEvent e) {
				 //System.out.println(e);
				 if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					 String href = e.getDescription();
					 BrowserOpener.showDocument(href);
				 }
			 }
		 });

		 JScrollPane scroll = new JScrollPane(txt);

		 Box vBox = Box.createVerticalBox();
		 vBox.add(scroll);

		 JButton close = new JButton("Close");

		 close.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent event) {
				 Object source = event.getSource();
				 //System.out.println(source);
				 JButton but = (JButton)source;
				 Container parent = but.getParent().getParent().getParent().getParent().getParent().getParent() ;
				 //System.out.println(parent);
				 JDialog dia = (JDialog) parent;
				 dia.dispose();
			 }
		 });

		 Box hBoxb = Box.createHorizontalBox();
		 hBoxb.add(Box.createGlue());
		 hBoxb.add(close,BorderLayout.EAST);

		 vBox.add(hBoxb);

		 dialog.getContentPane().add(vBox);
		 dialog.setVisible(true);

	 }


	 public Structure getStructure(){
		 return structure;
	 }

	 public void setStructure(Structure s){
		 StructureEvent event = new StructureEvent(s);
		 browserPane.getStructureManager().newStructure(event);
		 StructureListener[] sls = browserPane.getStructureListener();
		 for (int i = 0 ; i < sls.length ; i++){
			 StructureListener li = sls[i];
			 li.newStructure(event);
		 }
		 structurePanelListener.newStructure(event);
		 selectionPanel.newStructure(event);
		 //chainDisplay.newStructure(event);
	 }

	 /** send a command to Jmol *
    public void executeCmd(String cmd) {
        //logger.finest("executing Command "+ cmd);
        structurePanel.executeCmd(cmd);	
    }
	  */
	 public String getPDBCode(){

		 return browserPane.getStructureManager().getAccessionCode();
	 }

	 public String getUniProtCode(){
		 return browserPane.getUPManager().getAccessionCode();       
	 }

	 public String getENSPCode(){
		 return browserPane.getENSPManager().getAccessionCode();
	 }

	 public int getCurrentChainNumber() {
		 return selectionPanel.getCurrentChainNumber();
	 }


	 public synchronized void setCurrentChain(Chain c, int chainNumber){
		 currentChain = c;
		 currentChainNumber = chainNumber;
		 notifyAll();
	 }

	 public JmolViewer getViewer(){
		 return structurePanel.getViewer();
	 }


	 /** get Chain number X from structure 
	  * @return a Chain object or null ;
	  */
	 public Chain getChain(int chainnumber) {

		 return selectionPanel.getChain(chainnumber);
		 //return chainDisplay.getChain(chainnumber);

	 }

	 /** reset the Jmol panel */
	 public void resetDisplay(){
		 structurePanelListener.resetDisplay();
		 //setSelectionLocked(false);

	 }




	 private SpiceDasSource[] filterSourcesWithStartupData(List l){


		 Iterator iter = l.iterator();


		 SpiceDasSource[] sources = new SpiceDasSource[l.size()];

		 int i = 0;
		 while (iter.hasNext()){
			 SpiceDasSource ds = (SpiceDasSource) iter.next();
			 //logger.finest(ds.getNickname()+" "+ds.getStatus());
			 sources[i] = ds;
			 i++;
		 }

		 StartParameterFilter filter = new StartParameterFilter();
		 filter.setDisplayServers(startParameters.getDisplay());
		 filter.setDisplayLabels(startParameters.getDisplayLabel());

		 // need to clear this for multiple tabs, otherwise this would
		 // cause problems ...
		 startParameters.setDisplay(null);
		 startParameters.setDisplayLabel(null);

		 return filter.filterSources(sources);
	 }


	 /** make sure that already know DAS sources are re-used
	  * 
	  * @param newSources sources to be checked
	  * @return an array of SpiceDasSources
	  */
	 private SpiceDasSource[] filterSourcesWithKnowData(SpiceDasSource[] newSources){


		 SpiceDasSource[] sources = new SpiceDasSource[newSources.length];

		 for (int i = 0 ; i< newSources.length;i++){

			 SpiceDasSource newSource = newSources[i];

			 boolean known = false;

			 for (int j = 0 ; j < knownSources.length;j++){

				 SpiceDasSource knownSource = knownSources[j];

				 if ( knownSource.getUrl().equals(newSource.getUrl())){
					 known = true;
					 //logger.info("duplicate  " + knownSource.getNickname() + knownSource.getStatus()+ " " + newSource.getStatus());
					 knownSource.setStatus(newSource.getStatus());
					 sources[i] = knownSource;
					 break;
				 }                
			 }

			 if ( ! known) {
				 sources[i] = newSource;
				 //logger.finest("new source " + newSource.getNickname());
			 }
		 }
		 knownSources = sources;
		 return sources;

	 }





	 private void setDasSources(){

		 List l = new ArrayList();
		 if ( config != null ) 
			 l = config.getAllServers();

		 //Q: if we do not have servers, should we trigger a request for them here?
		 if (logger.isLoggable(Level.FINEST)){
			 logger.finest("got " + l.size() + " servers");
		 }

		 //if ( logger.isLoggable(Level.FINEST)) {
		 //Iterator iter = l.iterator();
		 //while (iter.hasNext()){
		 //SpiceDasSource ds = (SpiceDasSource)iter.next();
		 //logger.finest("setDasSources " + ds.getNickname() + " " + ds.getStatus());
		 //}
		 //}

		 SpiceDasSource[] sources = filterSourcesWithStartupData(l);



		 sources = filterSourcesWithKnowData(sources);

		 /* for (int i=0 ; i < sources.length;i++){
            System.out.println("a pos:"+(i+1) + " " + sources[i].getNickname() + " " + sources[i].getUrl());
            SpiceDasSource ds = sources[i];
            config.moveToPosition(ds.getUrl(),i);            
        }*/

		 //browserPane.clearDasSources();
		 browserPane.setDasSources(sources);

	 }


	 public synchronized void newConfigRetrieved(RegistryConfiguration conf){
		 logger.finest("got new config " );
		 config = conf;
		 configLoaded =true;
		 if ( conf == null)
			 return;
		 testAddLocalServer();
		 setDasSources();

		 dealWithStartParameters();

		 processNextInQueue();

	 }


	 public void showConfig() {
		 //RegistryConfigIO regi = new RegistryConfigIO(this,REGISTRY_URLS);
		 //regi.setConfiguration(config);
		 //regi.run();
		 //regi.showConfigFrame();
		 ConfigGui cfg = new ConfigGui(this);
		 cfg.showConfigFrame();
	 }



	 /** update the DIsplays of the subpanes */
	 public void updateDisplays() {
		 //logger.finest("updateDisplays + features size: " + features.size());
		 //SeqFeatureCanvas dascanv = daspanel.getCanv();

		 browserPane.repaint();
		 //dascanv.paint(dascanv.getGraphics());

		 sharedPanel.paint(sharedPanel.getGraphics());
		 //leftPanel.paint(leftPanel.getGraphics());
		 //chainList.paint(chainList.getGraphics());
		 selectionPanel.repaint();

		 //lcr.paint();
		 //ListCellRenderer lcr = ent_list.getCellRenderer();
		 //ent_list.paint() ;
		 sharedPanel.setVisible(true);
		 //dasPanel.updateUI();
		 //dasPanel.revalidate();
		 //dasPanel.repaint();

		 //dasPanel.revalidate();
		 //dasPanel.repaint();

		 vBox.repaint();
	 }



	 /** retreive the chainNumber by PDB character
     @param chainPDBcode PDB code for chain
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

		 Chain chain = getChain(currentChainNumber) ;
		 if ( chain == null ) return -1 ;

		 //logger.finest( "testing for insertion code " +residuePDBcode);
		 // see if there is an "^" character indicating insertionCode...


		 //List groups = chain.getGroups();

		 for ( int i = 0 ; i < chain.getLengthAminos(); i++ ) {
			 Group g = chain.getGroup(i);
			 if ( g.has3D()) 
				 if (g.getPDBCode().equals(residuePDBcode)) {
					 return i;
				 }
		 }
		 return -1 ;
	 } 
}

class MyAlignmentListener implements AlignmentListener{

	SpiceApplication parent;
	SpiceTabbedPane spiceTabbedPane;

	public MyAlignmentListener(SpiceApplication parent,SpiceTabbedPane tab){
		this.parent = parent;
		spiceTabbedPane = tab;
	}


	public void clearAlignment() {                          
	}

	public void newAlignment(AlignmentEvent e) {     
		if (SpiceApplication.logger.isLoggable(Level.FINEST))
			SpiceApplication.logger.log(Level.FINEST,"new alignment myAlignmentListener");
		String txt =  e.getAccessionCode();

		Das1Source source = e.getSource();
		DasCoordinateSystem[] coords = source.getCoordinateSystem();
		for ( int i=0 ; i < coords.length; i++){
			DasCoordinateSystem cs = coords[i];

			if ( cs.getName().equalsIgnoreCase("CASP")){
				txt += " - " + source.getNickname();
				break;
			}
			String nick = source.getNickname();
			parent.setPreferredStructureAlignmentServerName(nick);
		}		

		spiceTabbedPane.setFrameTitle("SPICE - " + txt);
		spiceTabbedPane.setTabTitle(parent,txt);

	}

	public void noAlignmentFound(AlignmentEvent e) {               

	}

}


class MyDasSourceListener implements DasSourceListener{

	public MyDasSourceListener(){

	}
	public void selectedDasSource(DasSourceEvent event){
		SpiceDasSource ds = event.getDasSource().getDasSource();
		DasSourceDialog dsd = new DasSourceDialog(ds);
		dsd.setVisible(true);
	}

	public void removeDasSource(DasSourceEvent ds){

	}
	public void disableDasSource(DasSourceEvent ds) {


	}
	public void enableDasSource(DasSourceEvent ds) {


	}
	public void loadingFinished(DasSourceEvent ds) {


	}
	public void loadingStarted(DasSourceEvent ds) {


	}
	public void newDasSource(DasSourceEvent ds) {

	}

}






