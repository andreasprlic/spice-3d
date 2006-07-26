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

import org.biojava.spice.panel.*;
import org.biojava.spice.config.*;
import org.biojava.spice.gui.*;
import org.biojava.spice.jmol.JmolSpiceTranslator;

import org.biojava.bio.structure.*;
import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;
import org.biojava.dasobert.eventmodel.AlignmentEvent;
import org.biojava.dasobert.eventmodel.AlignmentListener;
import org.biojava.dasobert.eventmodel.SequenceListener;
import org.biojava.dasobert.eventmodel.StructureEvent;
import org.biojava.dasobert.eventmodel.StructureListener;

// to get config file via http
import java.net.URL;
import java.net.MalformedURLException;

// some utils 
import java.util.HashMap   ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.ResourceBundle;

// logging
import java.util.logging.* ;
import java.util.Iterator  ;


// gui
import java.awt.BorderLayout;
import java.awt.Dimension                       ;
import java.awt.Color                           ;
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
import org.biojava.spice.server.SpiceServer;
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
        
    
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    static String baseName="spice";
    
    URL[] REGISTRY_URLS    ; // the url to the registration server
     
    static int    DEFAULT_Y_SCROLL = 50 ;
    static String XMLVALIDATION = "false" ;   
        
    RegistryConfiguration config      ;
    Structure structure ; 
    String pdbcode      ;
    String pdbcode2     ; // only set if displaying structure alignments 
    
    Chain currentChain ;
    int currentChainNumber  ;
    HashMap memoryfeatures; // all features in memory
    List features ;    // currently being displayed 
        

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
   
    Color oldColor ; 
    boolean first_load ;
    boolean selectionLocked ;
    boolean structureAlignmentMode ;
    
    //public static Logger logger = Logger.getLogger("org.biojava.spice");
    String waitingCode;
    String waitingType;
    
    ImageIcon firefoxIcon ;
    
    boolean configLoaded ;
    SpiceMenuListener spiceMenuListener;
    BrowseMenuListener browseMenu;
    BrowserPane browserPane ;
    //SpiceChainDisplay chainDisplay;
    SpiceTabbedPane spiceTabbedPane;
    SpiceStartParameters startParameters;
    SpiceServer spiceServer;
    JMenuBar menu;
    
   // JTabbedPane tabbedPane;
    Box vBox;
    
    /** 
     * start the spice appplication
     * 
     * @param params the parameters for starting up...
     */
    public SpiceApplication( SpiceStartParameters params) {
        super();
        
        startParameters = params;
        
        //this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
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
        
        structureAlignmentMode = false ; 
        
        if (params.isInitSpiceServer()) {
            // only one logging panel for all spice windows ...
            // init logging related things
            initLoggingPanel();
        }
        
        // set some system properties
        setSystemProperties();     
        
        // init the 2D display
        browserPane = new BrowserPane(params.getPdbcoordsys(),params.getUniprotcoordsys(), params.getEnspcoordsys());
              
        structurePanel = new StructurePanel();
        jmolSpiceTranslator = new JmolSpiceTranslator();
        structurePanel.addJmolStatusListener(jmolSpiceTranslator);
        
        structurePanelListener = new StructurePanelListener(structurePanel);
       
        // first thing is to start das - registry communication
        
        config = new RegistryConfiguration();
        
        URL[] registries = getAllRegistryURLs();
        RegistryConfigIO regi = new RegistryConfigIO(registries);
        if ( params.isNoRegistryContact()) {
            regi.setNoUpdate(true);
        } else {
            regi.addConfigListener(this);
            regi.run();
        }
        
        
        
        
        // init all panels, etc..
        statusPanel    = new StatusPanel(this);
        statusPanel.setBorder(BorderFactory.createEmptyBorder());
        
        strucommand    = new StructureCommandPanel(structurePanelListener);
        
        //if ( params.isInitSpiceServer())
        //    initSpiceServer();
        
        vBox = arrangePanels(statusPanel,structurePanel,browserPane,strucommand,"left"); 
        
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        this.add(vBox);
     
      
        spiceMenuListener = new SpiceMenuListener(this,structurePanelListener) ;
       
        
        memoryfeatures = new HashMap();
        features = new ArrayList();
        
        menu = initMenu();
       
        initListeners();
       
        firefoxIcon = createImageIcon("firefox.png");
        
        
        
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
   
    private void initLoggingPanel(){
        
        final LoggingPanel loggingPanel = new LoggingPanel(logger);
        loggingPanel.getHandler().setLevel(Level.INFO);	
        logger.setLevel(Level.INFO);
        loggingPanel.show(null);
                       
    }
    
    /** set  a couple of System Properties also contains some hacks around some strange implementation differences*/
   
    private void setSystemProperties(){
        
        //  on osx move menu to the top of the screen
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        
        // do xml validation when parsing DAS responses (true/false)
        System.setProperty("XMLVALIDATION",XMLVALIDATION);
        
        ResourceBundle resource = ResourceBundle.getBundle(baseName);
        
        String to = resource.getString("org.biojava.spice.ConnectionTimeout");
        int timeout = Integer.parseInt(to);
        
        
        //logger.finest("setting timeouts to " + timeout);
        
        
        // timeouts when doing http connections
        // this only applies to java 1.4
        // java 1.5 timeouts are set by openHttpURLConnection
        System.setProperty("sun.net.client.defaultConnectTimeout", ""+timeout);
        System.setProperty("sun.net.client.defaultReadTimeout", ""+timeout);
        
              
        // bugfix for some strange setups!!!
        String proxyHost  = System.getProperty("proxyHost");
        String proxyPort  = System.getProperty("proxyPort");
        
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("proxyHost"         + proxyHost);
            logger.finest("proxyPort"         + proxyPort);
            logger.finest("http.proxyHost"    + System.getProperty("http.proxyHost"));
            logger.finest("http.proxyPort"    + System.getProperty("http.proxyPort"));
        }
        // hack around some config problems ... argh!
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
        //this.getContentPane().add(strucommand,BorderLayout.SOUTH);
        //this.getContentPane().add(strucommand);
        vBox2.add(strucommand);
        //structurePanel.setLayout(new BoxLayout(structurePanel,BoxLayout.X_AXIS)); 

                      
      
        //DefaultListModel model = new DefaultListModel();
        //model.add(0,"");
        //chainList=new JList(model);
        selectionPanel = new SelectionPanel();
        
        //ent_list.setPreferredSize(new Dimension(30,30));
        
        
        JScrollPane chainPanel = new JScrollPane(selectionPanel);
        selectionPanel.setPreferredSize(new Dimension(60,60));
        //chainPanel.setLayout(new BoxLayout(chainPanel,BoxLayout.X_AXIS)); 
        chainPanel.setBorder(BorderFactory.createEmptyBorder());
        
        browserPane.setBorder(BorderFactory.createEmptyBorder());
        
        sharedPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                chainPanel, browserPane);
        sharedPanel.setOneTouchExpandable(true);
        sharedPanel.setResizeWeight(0);
        //sharedPanel.setPreferredSize(new Dimension(400, 400));
        //sharedPanel.setLayout(new BoxLayout(sharedPanel,BoxLayout.Y_AXIS)); 
        sharedPanel.setBorder(BorderFactory.createEmptyBorder());
        
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
        
        //mainsharedPanel.setBorder(BorderFactory.createEmptyBorder());
        //mainsharedPanel.setLayout(new BoxLayout(mainsharedPanel,BoxLayout.Y_AXIS));  
        Box hBox1 =  Box.createHorizontalBox();
        hBox1.add(mainsharedPanel);
        vBox.add(hBox1);
        
        
        statusPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        //statusPanel.setLayout(new BoxLayout(statusPanel,BoxLayout._AXIS));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        //statusPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,20));
        //Box hBox1 = Box.createHorizontalBox();
        //hBox1.add(statusPanel);
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
            // TODO: enable next line
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
        
        
        selectionPanel.addStructureListener(jmolSpiceTranslator);
        selectionPanel.addStructureListener(browserPane.getStructureManager());
        selectionPanel.addStructureListener(structurePanelListener);
        selectionPanel.addStructureListener(browserPane.getTopAlignmentManager());
        selectionPanel.addStructureListener(browseMenu.getPDBListener());
    }
    
    public void setMenu(JMenuBar menu) {
        this.menu=menu;
    }
    
    
    public JMenuBar getMenu(){
        if ( menu == null)
            menu = initMenu();
        return menu;
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
        
        JMenu alig = MenuCreator.createAlignmentMenu(spiceMenuListener);
        menu.add(alig);

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
            logger.log(Level.WARNING,"Couldn't find file: " + path);
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
        String msg = "SpiceApplication load: " + type + " " + code;
        System.out.println(msg);
        logger.finest(msg);
        
        
        
        if (type.equals("PDB")){
            
            this.loadStructure(code);
           
        }
        else if (type.equals("UniProt")) {
            //logger.info("got uniprot");
            // connect to Uniprot -pdb alignment service, get PDB code and load it ...
            loadUniprot(code);
            
        }
        else if (type.equals("ENSP")) {
            loadEnsp(code);
            
        }
        else if (type.equals("alignment")){
            // spice will be running in structure alignment mode
            String aligcs = startParameters.getStructureAlignmentMode();
            DasCoordinateSystem dcs = DasCoordinateSystem.fromString(aligcs);
            loadAlignment(code,dcs);
            
        }
        else {
            // unknown code type!
            //System.err.println("unknown code type >"+type+"< currently supported: PDB,UniProt,ENSP");
            logger.warning("unknown code type >"+type+"< currently supported: PDB,UniProt,ENSP");
            return;
        }
        
        
    }
    
    
         
    
    public RegistryConfiguration getConfiguration() {
        return config ;
    }
    
    
       
    /** start a new thead that retrieves uniprot sequence, and if available
     protein structure
     */
    private void loadUniprot(String uniprot) {
        logger.info("SpiceApplication loadUniprot " + uniprot);
        System.setProperty("SPICE:drawStructureRegion","false");
        if ( config == null){
            // we have to wait until contacting the DAS registry is finished ...
            waitingType="UniProt";
            waitingCode=uniprot;
            return;
        }
        
       clear();
       
       browserPane.triggerLoadUniProt(uniprot);
    }

    /** start a new thead that retrieves uniprot sequence, and if available
    protein structure
    */
   private void loadEnsp(String ensp) {
       //logger.info("SpiceApplication loadEnsp" + ensp);
       System.setProperty("SPICE:drawStructureRegion","false");
       if ( config == null){
           // we have to wait until contacting the DAS registry is finished ...
           waitingType="Ensp";
           waitingCode=ensp;
           return;
       }
       
     
       clear();
       browserPane.triggerLoadENSP(ensp);
       
       
   }

   
    
    
    /** starts a new thread that retreives protein structure using the
     DAS structure command from some other server this thread will
     call the setStructure method to set the protein structure.
     */
    private void loadStructure(String pdbcod) {
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
            waitingType="PDB";
            waitingCode= pdbcod;
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
        logger.info("loading Structure alignment for coordinate system " + aligCs.toString());
        List aligservers = config.getServers("alignment", aligCs.toString());
        logger.info("found " +aligservers.size() + "alignment servers");
        SpiceDasSource[] ads = (SpiceDasSource[])aligservers.toArray(new SpiceDasSource[aligservers.size()]);
        List strucservers = config.getServers("structure");
        SpiceDasSource[] sds = (SpiceDasSource[])strucservers.toArray(new SpiceDasSource[strucservers.size()]);
        
        StructureAlignmentBuilder sacreator = new StructureAlignmentBuilder();
        //sacreator.addStructureListener(structurePanelListener);
        //sacreator.setStructurePanel(structurePanel);
        sacreator.setSelectionPanel(selectionPanel);
        sacreator.setAlignmentServers(ads);
        sacreator.setStructureServers(sds);
        
        AlignmentListener ali = new AlignmentListener(){

            public void clearAlignment() {          
                
            }

            public void newAlignment(AlignmentEvent e) {
                String txt = "SPICE - " + e.getAccessionCode();
                spiceTabbedPane.setFrameTitle(txt);
                
            }

            public void noAlignmentFound(AlignmentEvent e) {
               
                
            }
            
        };
        sacreator.addAlignmentListener(ali);
      
        
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
            if (localcapability == null)
                localcapability = "features";
            //logger.info("adding new local DAS source");
            
            SpiceDasSource localDs = new SpiceDasSource();
            localDs.setUrl(localurl);
            localDs.setStatus(true);
            localDs.setRegistered(false);
            localDs.setNickname(localname);
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
    
    
    
    /** set a structure to be displayed and sends a script command to
     * color structure 
     * @param structure_ a Biojava structure object
     */
    /*
    public  void setStructure(Structure structure_ ) {
        logger.warning("depreciated method!");
        if (logger.isLoggable(Level.FINER)) {
            logger.entering(this.getClass().getName(), "setStructure",  new Object[]{"got structure object"});
        }
        
        logger.finest("SpiceApplication got structure " + structure_);
        
        this.setLoading(false);
        //first_load = false ;
        //statusPanel.setLoading(false);
        
        if ( structure_.size() < 1 ){
            logger.log(Level.INFO,"got no structure");
            return ;
        }
        
        structure = structure_ ;
        //currentChain = null;
        setCurrentChain(null,-1);
        pdbcode = structure.getPDBCode();
        
        //if (pdbcode != null)
          //  pdbMenu.setEnabled(true);
        //if (logger.isLoggable(Level.FINEST)) {
        //System.out.println(structure.toPDB());	    
        //}
        
        
        
        DefaultListModel model = (DefaultListModel) chainList.getModel() ;
        synchronized (model) {
            model.clear() ;
            //logger.finest(pdbstr);		
            ArrayList chains = (ArrayList) structure.getChains(0);
            for (int i=0; i< chains.size();i++) {
                Chain ch = (Chain) chains.get(i);
                model.add(i,ch.getName());
            }
            //notifyAll();
               
        }
        
        //System.out.println("SpiceApplication... setting structure in Jmol");
        
        
        
        //System.out.println("SpiceApplication... back in main spice");
        logger.finest("back in main spice ...");
        
        //Map header = structure.getHeader();
        //logger.info("structure header " + header);
        //statusPanel.setPDB(structure.getPDBCode());
        //statusPanel.setPDBHeader(structure.getHeader());
        
        //structurePanelListener.executeCmd(selectcmd);
        //System.out.println("SpiceApplication... setting chain");
        logger.finest("setting chain...");
        //setCurrentChainNumber(0);
        //System.out.println("SpiceApplication... requesting getChain");
        //Chain chain = getChain(currentChainNumber) ;
        
        
        //currentChain = chain;
        //System.out.println("SpiceApplication... setting chain in dascanv");
        //dascanv.setChain(chain);
        //System.out.println("SpiceApplication... set chain in dascanv");
        
        //if ( chain != null) 
            //seqTextPane.setChain(chain,0);
        */

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
                    showDocument(href);
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
        dialog.show();
        
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
    
    /** display an URL in the browser that started SPICE */
    public boolean showURL(URL url) {
        return showDocument(url);
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
        
        StartParametereFilter filter = new StartParametereFilter(startParameters);
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

        List l = config.getAllServers();
        logger.finest("got " + l.size() + " servers");
        
        if ( logger.isLoggable(Level.FINEST)) {
            //Iterator iter = l.iterator();
            //while (iter.hasNext()){
                //SpiceDasSource ds = (SpiceDasSource)iter.next();
                //logger.finest("setDasSources " + ds.getNickname() + " " + ds.getStatus());
            //}
        }
       
        SpiceDasSource[] sources = filterSourcesWithStartupData(l);
         
        
        sources = filterSourcesWithKnowData(sources);
        
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
                
        triggerQueuedRequests();
        dealWithStartParameters();
        notifyAll();
    }
    
    public  void triggerQueuedRequests(){
        // trigger queued requests
        //logger.info("triggerQueuedRequest");
               
        if ( waitingType != null){
            //MyLoadingThread thr = new MyLoadingThread(waitingType,waitingCode,this);
            //thr.start();
            load(waitingType,waitingCode);
            
            waitingType = null;
            waitingCode = null;
            
        }
        
      
       
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
        
        for ( int i = 0 ; i < chain.getLength(); i++ ) {
            Group g = chain.getGroup(i);
            if ( g.has3D()) 
                if (g.getPDBCode().equals(residuePDBcode)) {
                    return i;
                }
        }
        return -1 ;
    }
    
    
    /** Event handling 
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
        
    }    */
    
    public boolean showDocument(URL url) 
    {
        if ( url != null ){
            boolean success = JNLPProxy.showDocument(url); 
            if ( ! success)
                logger.info("could not open URL "+url+" in browser. check your config or browser version.");
	    return success;
	    
        }
        else
            return false;
    }
    
    public boolean showDocument(String urlstring){
        try{
            URL url = new URL(urlstring);
            
            return showDocument(url);
        } catch (MalformedURLException e){
            logger.warning("malformed URL "+urlstring);
            return false;
        }
    }

   
     
  
}

class MyDasSourceListener implements DasSourceListener{
   
    public MyDasSourceListener(){
       
    }
    public void selectedDasSource(DasSourceEvent event){
        SpiceDasSource ds = event.getDasSource().getDasSource();
        DasSourceDialog dsd = new DasSourceDialog(ds);
        dsd.show();
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






