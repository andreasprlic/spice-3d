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

import org.biojava.services.das.registry.DasCoordinateSystem;
import org.biojava.spice.Panel.*;
import org.biojava.spice.Config.*;
import org.biojava.spice.GUI.*;

import java.lang.reflect.*;
import org.biojava.bio.structure.*;

// to get config file via http
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.ConnectException;

// some utils 
import java.util.HashMap   ;
import java.util.ArrayList ;
import java.util.List ;

import java.io.IOException;

// logging
import java.util.logging.* ;
import java.util.Iterator  ;


// gui
import java.awt.BorderLayout;
import java.awt.Dimension                       ;
import java.awt.Color                           ;
import java.awt.Event                           ;
import java.awt.event.*                         ;

import javax.swing.Box                          ;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JSplitPane                   ;
import javax.swing.JFrame                       ;
import javax.swing.JList                        ;
import javax.swing.JScrollPane                  ;
import javax.swing.DefaultListModel             ;
import javax.swing.JTextField                   ;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.ImageIcon                    ;
import javax.swing.BorderFactory                ;
import javax.swing.JMenuBar                     ;
import javax.swing.JMenu                        ;
import javax.swing.JMenuItem                    ;
import org.biojava.bio.Annotation               ;
//import java.util.Map;
import javax.swing.JDialog;
import java.awt.Container;

import org.biojava.spice.das.SpiceDasSource;
import org.biojava.spice.manypanel.BrowserPane;
import org.biojava.spice.manypanel.eventmodel.DasSourceEvent;
import org.biojava.spice.manypanel.eventmodel.DasSourceListener;
import org.biojava.spice.server.SpiceServer;


/** the main application layer of SPICE
 * do not interact with this class directly, but interact with SPICEFrame interface.
 *
 * @author Andreas Prlic
 */
public class SpiceApplication 
extends  JFrame
implements SPICEFrame, 
ConfigurationListener

{     
    
    private static final long serialVersionUID = 8273923744127087422L;
        
    
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    URL[] REGISTRY_URLS    ; // the url to the registration server
    
    static int    CONNECTION_TIMEOUT = 15000;// timeout for http connection = 15. sec
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
        
    //StructurePanel structurePanel ;  
    StructurePanelListener structurePanelListener ;
    //JTextField seq_pos ;
    JList chainList;   // list available chains
    //SeqFeaturePanel dascanv ;
    //SpiceFeatureViewer dascanv;
    JScrollPane dasPanel ;
    //JPanel leftPanel ;
    JSplitPane sharedPanel;
    JSplitPane mainsharedPanel;
    
    JScrollPane seqScrollPane ;
    JSplitPane  seqSplitPane  ;
    //SeqTextPane seqTextPane      ;
    JMenuItem lock;
    JMenuItem unlock;
    JMenuItem lockMenu;
    
    
    //JMenuBar menuBar ;
    JTextField getCom ;
    List knownFeatureLinks;
    
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
    SpiceChainDisplay chainDisplay;
    
    SpiceStartParameters startParameters;
    SpiceServer spiceServer;
    
    /** 
     * start the spice appplication
     * 
     * @param the parameters for starting up...
     */
    public SpiceApplication( SpiceStartParameters params) {
        super();
        
        startParameters = params;
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
              
        
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
        
        // init logging related things
        initLoggingPanel();
        
        // set some system properties
        setSystemProperties();     
        
        // init the 2D display
        browserPane = new BrowserPane(params.getPdbcoordsys(),params.getUniprotcoordsys(), params.getEnspcoordsys());
        
        // first thing is to start das - registry communication
        URL[] registries = getAllRegistryURLs();
        RegistryConfigIO regi = new RegistryConfigIO(registries);
        regi.addConfigListener(this);
        regi.run();
        
              
        StructurePanel structurePanel = new StructurePanel(this);	
        structurePanelListener = new StructurePanelListener(structurePanel);
         
        
        // init all panels, etc..
        statusPanel    = new StatusPanel(this);
        statusPanel.setBorder(BorderFactory.createEmptyBorder());
        
        strucommand    = new StructureCommandPanel(structurePanelListener);
        
        Box vBox = arrangePanels(statusPanel,structurePanel,browserPane,strucommand,"left"); 
        
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(),BoxLayout.X_AXIS));
        this.getContentPane().add(vBox);
        this.setLoading(false);
       
        spiceMenuListener = new SpiceMenuListener(this,structurePanelListener) ;
       
        
        memoryfeatures = new HashMap();
        features = new ArrayList();
        
        JMenuBar menu = initMenu();
        this.setJMenuBar(menu);
       
        initListeners();
        
        this.setTitle("SPICE") ;
             
        JFrame.setDefaultLookAndFeelDecorated(false);
	
        firefoxIcon = createImageIcon("firefox.png");
        ImageIcon icon = createImageIcon("spice16x16.gif");
        this.setIconImage(icon.getImage());
        this.pack();
        
        //this.setSize(800, 600);
        this.setVisible(true);
        
        
        initSpiceServer();
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
    
    /** launch the Server that listens to a port for requests from other instances of SPICE
     * 
     *
     */
    private void initSpiceServer(){
        
        spiceServer = new SpiceServer(this);
        
        this.addWindowListener(new WindowListener() {
            public void windowDeiconified(WindowEvent e){}
            public void windowIconified(WindowEvent e){}
            public void windowActivated(WindowEvent e){}
            public void windowDeactivated(WindowEvent e){}
            public void windowOpened(WindowEvent e){}
            public void windowClosing(WindowEvent e){
                logger.info("destroying SPICE server");
                
                spiceServer.destroy();
            }
            public void windowClosed(WindowEvent e){}
        });
        
    }
    
    
   
    private void initLoggingPanel(){
        LoggingPanel loggingPanel = new LoggingPanel(logger);
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
        
        int timeout = CONNECTION_TIMEOUT;
        
        
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
    
    /** Constructor for structure alignment visualization 
     currently disabled
     SpiceApplication(String pdb1, String pdb2, URL config_url, URL registry_url) {
     this(pdb1, config_url,registry_url);
     structureAlignmentMode = true ;
     pdbcode2 = pdb2 ;
     logger.finest("finished init of structure alignment");
     
     }
     */
    
    
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
        
        //vBox.setBackground(Color.blue);
        // move to submenu
        //this.getContentPane().add(statusPanel,BorderLayout.SOUTH);
        //this.getContentPane().add(statusPanel);
        
        
        
       
        Box vBox2 = Box.createVerticalBox();
        structurePanel.setMinimumSize(new Dimension(200,200));
        vBox2.add(structurePanel);
        strucommand.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        //this.getContentPane().add(strucommand,BorderLayout.SOUTH);
        //this.getContentPane().add(strucommand);
        vBox2.add(strucommand);
        //structurePanel.setLayout(new BoxLayout(structurePanel,BoxLayout.X_AXIS)); 

                      
      
        DefaultListModel model = new DefaultListModel();
        model.add(0,"");
        chainList=new JList(model);
        
        
        //ent_list.setPreferredSize(new Dimension(30,30));
        
        
        JScrollPane chainPanel = new JScrollPane(chainList);
        chainPanel.setPreferredSize(new Dimension(30,30));
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
        mainsharedPanel.setResizeWeight(0.5);
        
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
        hBox.add(statusPanel);
        vBox.add(hBox);
        //vBox.add(hBox1);
        
        //vBox.add(loggingPanel);
        return vBox;
    }
    
    /** initialize the listeners of the various componentns 
     * 
     *
     */
    private void initListeners(){
        
               
        browserPane.addPDBSequenceListener(statusPanel);
        browserPane.addUniProtSequenceListener(statusPanel);
        browserPane.addStructureListener(structurePanelListener);
        browserPane.addPDBPositionListener(structurePanelListener);
        browserPane.addSpiceFeatureListener(statusPanel);
        browserPane.addPDBPositionListener(statusPanel);
        browserPane.addStructureListener(statusPanel);
        browserPane.addStructureListener(browseMenu.getPDBListener());
        browserPane.addUniProtSequenceListener(browseMenu.getUniProtListener());
        browserPane.addEnspSeqeuenceListener(browseMenu.getEnspListener());
        
        // listener for das sources
        browserPane.addSpiceFeatureListener(structurePanelListener);
        
        
        MyDasSourceListener mdsl = new MyDasSourceListener(this);
        browserPane.addDasSourceListener(mdsl);
        browserPane.addPDBSequenceListener(spiceMenuListener);
            
        // things related to selecting chains
        chainDisplay = new SpiceChainDisplay(chainList);
        browserPane.addStructureListener(chainDisplay);
        chainList.addListSelectionListener(chainDisplay);
        chainDisplay.addStructureListener(browserPane.getStructureListener());
        chainDisplay.addStructureListener(structurePanelListener);
        chainDisplay.addStructureListener(statusPanel);
        //chainDisplay.addStructureListener(spiceMenuListener);
            
    }
    
    /**
     * @returns the Menu to be displayed on top of the application
     */
    private JMenuBar initMenu() {
        
        JMenuBar menu = new JMenuBar();
        
        // the three menus
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        file.getAccessibleContext().setAccessibleDescription("the file menu");
        
        menu.add(file);
        
        
        JMenuItem openpdb;
        ImageIcon openIcon = createImageIcon("network.png");
        if ( openIcon == null)
            openpdb = new JMenuItem("Open");
        else
            openpdb = new JMenuItem("Open", openIcon);
        
        openpdb.setMnemonic(KeyEvent.VK_O);
        
        JMenuItem save ;
        ImageIcon saveIcon = createImageIcon("3floppy_unmount.png");
        if ( saveIcon == null)
            save = new JMenuItem("Save");
        else
            save = new JMenuItem("Save",saveIcon);
        save.setMnemonic(KeyEvent.VK_S);
        
        JMenuItem revert;
        ImageIcon revertIcon = createImageIcon("revert.png");
        if (revertIcon == null)
            revert = new JMenuItem("Load");
        else
            revert = new JMenuItem("Load",revertIcon);
        revert.setMnemonic(KeyEvent.VK_L);
        
        ImageIcon exitIcon = createImageIcon("exit.png");
        JMenuItem exit;
        if ( exitIcon != null)
            exit    = new JMenuItem("Exit",exitIcon);
        else
            exit    = new JMenuItem("Exit");
        exit.setMnemonic(KeyEvent.VK_X);
        
        ImageIcon propIcon = createImageIcon("configure.png");
        JMenuItem props ;
        if ( propIcon != null )
            props   = new JMenuItem("Properties",propIcon);
        else
            props   = new JMenuItem("Properties");
        props.setMnemonic(KeyEvent.VK_P);
        
        
        SpiceMenuListener ml = spiceMenuListener;
        
        openpdb.addActionListener( ml );
        save.addActionListener   ( ml );
        revert.addActionListener ( ml );
        exit.addActionListener   ( ml );
        props.addActionListener  ( ml );

        file.add( openpdb );
        file.add( save    );
        file.add( revert  );
        file.addSeparator();
        file.add( props   );
        file.addSeparator();
        file.add( exit    );
        
        // DIsplay submenu
        
        JMenu display = StructurePanel.createMenu(ml);
        menu.add(display);
        
        
        // Browse menu
       
       
        // unique action listener for the browse buttons
        browseMenu = new BrowseMenuListener();
        JMenu bm = browseMenu.getBrowsermenu();
        
        menu.add(bm);

        
        
        // Alignment submenu
        /*
        JMenu align = new JMenu("Alignment");
        align.setMnemonic(KeyEvent.VK_A);
        align.getAccessibleContext().setAccessibleDescription("show alignments");
        menu.add(align);
        
        ImageIcon chooseIcon = createImageIcon("view_choose.png");
        
        JMenuItem seqstrucalig;
        if ( chooseIcon ==  null )
            seqstrucalig = new JMenuItem("Choose PDB-UniProt");
        else 
            seqstrucalig = new JMenuItem("Choose PDB-UniProt",chooseIcon);
        seqstrucalig.addActionListener(ml);
        seqstrucalig.setMnemonic(KeyEvent.VK_C);
        align.add(seqstrucalig);
        
        
        JMenuItem uniprotensp;
        if ( chooseIcon ==  null )
            uniprotensp = new JMenuItem("Choose UniProt-Ensp");
        else 
            uniprotensp = new JMenuItem("Choose UniProt-Ensp",chooseIcon);
        seqstrucalig.addActionListener(ml);
        seqstrucalig.setMnemonic(KeyEvent.VK_C);
        align.add(seqstrucalig);
        */
        
        menu.add(Box.createGlue());
        
        // Help submenu
        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);
        help.getAccessibleContext().setAccessibleDescription("get help");
        menu.add(help);
        
        ImageIcon helpIcon = createImageIcon("help.png");
        
        JMenuItem aboutspice;
        if ( helpIcon == null )
            aboutspice = new JMenuItem("About SPICE");
        else
            aboutspice = new JMenuItem("About SPICE",helpIcon);
        aboutspice.addActionListener  ( ml );
        aboutspice.setMnemonic(KeyEvent.VK_A);
        help.add(aboutspice);
        
        JMenuItem spicemanual;
        ImageIcon manualIcon =  createImageIcon("toggle_log.png");
        if ( manualIcon == null)
            spicemanual = new JMenuItem("Manual");
        else
            spicemanual = new JMenuItem("Manual",manualIcon);
        spicemanual.addActionListener(ml);
        spicemanual.setMnemonic(KeyEvent.VK_M);
        help.add(spicemanual);
        
        return menu ;
        
    }
    
    
    
    /** Returns an ImageIcon, or null if the path was invalid. */
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
        testAddLocalServer();
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
            if ( code.length() == 4 ) 
                this.loadStructure(code);
            else  
                logger.info("please provide only 4 characters for the PDB code.");
            
        }
        else if (type.equals("UniProt")) {
            //logger.info("got uniprot");
            // connect to Uniprot -pdb alignment service, get PDB code and load it ...
            loadUniprot(code);
            
        }
        else if (type.equals("ENSP")) {
            loadEnsp(code);
            
        }
        else {
            // unknown code type!
            //System.err.println("unknown code type >"+type+"< currently supported: PDB,UniProt,ENSP");
            logger.warning("unknown code type >"+type+"< currently supported: PDB,UniProt,ENSP");
            return;
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
        /*
        if ( config == null ) {
            return ;
        }
        if ( pdbcode == null){
            return;
        }
        if ( ! structureAlignmentMode ) {
            //logger.finest("not in alignment mode");
            loadStructure(pdbcode);
        } else {
            //showStatus("Loading...Wait...",Color.red);
            
            LoadStructureAlignmentThread thr = new 
            LoadStructureAlignmentThread(this,
                    pdbcode,
                    pdbcode2);
            thr.start();
            
        }
        */
        
    }
       
    
    public RegistryConfiguration getConfiguration() {
        return config ;
    }
    
    
    
    /*private void resetStatusPanel(){
        statusPanel.setPDB("");
        statusPanel.setSP("");
        statusPanel.setLoading(false);
        statusPanel.setPDBDescription("");
        statusPanel.setPDBHeader(new HashMap());
    }*/
    
    /** start a new thead that retrieves uniprot sequence, and if available
     protein structure
     */
    public void loadUniprot(String uniprot) {
        logger.info("SpiceApplication loadUniprot " + uniprot);
        
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
   public void loadEnsp(String ensp) {
       logger.info("SpiceApplication loadEnsp" + ensp);
       
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
    public void loadStructure(String pdbcod) {
        
               
        
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
    
    

    private String makeFeatureMemoryCode(String sp_id){
        int i = getCurrentChainNumber();
        // test if not PDB structure has been loaded (e.g. no network connection, 
        // seq not mapped to PDB, etc.).
        if ( i == -1 )
            return sp_id;
        Chain c = getChain(i); 
        String mem_id = sp_id +","+pdbcode + c.getName() ;
        return mem_id;
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
    
    /** return the features */
    public List getFeatures(){
        /*FeatureView[] fvs = dascanv.getFeatureViews();
        
        List features = new ArrayList();
        for ( int i = 0 ; i < fvs.length ; i++){
            FeatureView fv = fvs[i];
            Feature[] feats =fv.getFeatures();
            for ( int f = 0 ; f< feats.length;f++){
                Feature feat =feats[f];
                features.add(feat);
            }
        }
        */
        return null;
        //return features;
        //return dascanv.getFeatures();
    }
    
    //TODO: remove this method
    /**  update the currently displayed features */
    public void setFeatures(String sp_id, List tmpfeat) {
        //TODO: build up a new dascanv!
        logger.warning("depreciated method");
        logger.info("setting features");
        
        //first_load = false ;
        //logger.info("SpiceAplication setting features for "+sp_id);
        String mem_id = makeFeatureMemoryCode(sp_id);
        //System.out.println(mem_id);
       
        // TODO: need to move caching of features on a different level.
        // We need to distinguish SP and PDB features...
        // ev. better move cache to FeatureFetcher
        memoryfeatures.put(mem_id,tmpfeat);
        
        
        //statusPanel.setLoading(false);
        features.clear();
        features = tmpfeat ;
        
        // test if features have a LINK field
        // if yes, add to browse menu
        //registerBrowsableFeatures(tmpfeat);
        
        //this.paint(this.getGraphics());
        updateDisplays();
    }
    
    
    /*
    private void clearBrowsableButtons(){
        int nr = browseMenu.getItemCount();
        //System.out.println("cleaning "+nr+ " menus");
        for ( int i = nr-2; i > 1; i--){
            browseMenu.remove(i);
        }
        knownFeatureLinks = new ArrayList();
    }


    private void registerBrowsableFeatures(List feats){
        Iterator iter = feats.iterator();
        // add to menu
        
    
        clearBrowsableButtons();
        while (iter.hasNext()){
            FeatureImpl f = (FeatureImpl)iter.next();
            //System.out.println(f);
            String link =f.getLink();
            if ( link != null){
                if ( knownFeatureLinks.contains(link))
                    continue ;
                JMenuItem item;
                if ( firefoxIcon == null)
                     item = new JMenuItem("open in browser "+ f.getMethod());
                else
                    item = new JMenuItem("open in browser "+ f.getMethod(), firefoxIcon);
                URL u;
                try {
                    u = new URL(link);
                } catch (MalformedURLException e){
                    // if somebody e.g. provides the accession code 
                    // instead of a proper url in the link field...
                    continue;
                }
                ActionListener bl = new BrowseMenuListener(this,u);                    
                item.addActionListener(bl);
                
                browseMenu.add(item,(browseMenu.getItemCount()-1));
                knownFeatureLinks.add(link);
            }
        }
    } */


    private  void getNewFeatures(String sp_id) {
        //logger.("SpiceApplication get new Features " + sp_id);
        //ArrayList featureservers = getFeatureServers() ;
        logger.finest(" getNewFeatures" + sp_id);
        Chain chain = getChain(currentChainNumber) ;
        if ( chain == null) {
            System.out.println("SpiceApplication chain == null, returning ...");
            	return ;
            	
        }
        //first_load = true ;
        this.setLoading(true);
        features.clear();
        //dascanv.clear();
        //dascanv.setSeqLength(chain.getLength());

        
        if ( config != null ){
            // test if a new local DAS source is in params
         
            testAddLocalServer();
            
           
            

        }
       
      

        System.out.println("SpiceApplication init feature fetcher");
        logger.finest("init feature fetcher");


        
        String disp = startParameters.getDisplay();
        String labe = startParameters.getDisplayLabel();
        logger.info("got " + disp + " " + labe);

        
        if (! disp.equals("all")){
            if ( labe.equals("all")){
                labe = "";
            }
        }
        
        if ( ! labe.equals("all"))
            if ( disp.equals("all"))
                disp = "";
        logger.finest("got display list: "+ disp );
        
        
        
    }

    private void testAddLocalServer(){
        if ( config == null){
            return ;
        }
        String localurl = startParameters.getLocalServerURL();
        
        if (! ((localurl == null) || (localurl.equals("")))) {
            String localcood = startParameters.getLocalServerCoordSys();
            String localname = startParameters.getLocalServerName();
            //logger.info("adding new local DAS source");
            
            SpiceDasSource localDs = new SpiceDasSource();
            localDs.setUrl(localurl);
            localDs.setStatus(true);
            localDs.setRegistered(false);
            localDs.setNickname(localname);
            DasCoordinateSystem dcs = DasCoordinateSystem.fromString(localcood);
            DasCoordinateSystem[] csa = new DasCoordinateSystem[1];
            csa[0]=dcs;
            localDs.setCoordinateSystem(csa);
            config.addServer(localDs);
            
            // and clean up - we do not need to do this a second time again after ...
            startParameters.setLocalServerURL("");              
            startParameters.setLocalServerName(SpiceDasSource.DEFAULT_NICKNAME);
        }
    }
    
    /*
    // store all features in memory -> speed up
    private ArrayList getFeaturesFromMemory(String mem_id) {
        logger.entering(this.getClass().getName(), "getFeaturesFromMemory()",  new Object[]{mem_id});
        //logger.finest("getFeaturesFromMemory");
        ArrayList arr = new ArrayList() ;
        
        for (Iterator ti = memoryfeatures.keySet().iterator(); ti.hasNext(); ) {
            String key = (String) ti.next() ;
            logger.finest("in mem: " + key);
            //logger.finest(key);
            if ( key == null) { continue; }
            
            if (key.equals(mem_id)) {
                logger.finest("found features in memory for spi_id " + mem_id);
                
                arr = (ArrayList) memoryfeatures.get(mem_id) ;
                
                for ( int i = 0 ; i < arr.size() ; i++ ) {
                    FeatureImpl f = (FeatureImpl) arr.get(i);
                    logger.finest(" got memory feature " + f.toString());
                }
                return arr ;
            }
        }
        
        return arr ;
    }
    
    
    */
   
    
    
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
        
        if ( startParameters.getDisplayMessage() != null) {
            boolean displayScript = false; 
            structurePanelListener.setStructure(structure,displayScript);
        } else {
            structurePanelListener.setStructure(structure);
        }
        
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
        
        
        if ( startParameters.getRasmolScript() != null){
            // only execute the rasmol script command the first time.
            structurePanelListener.executeCmd(startParameters.getRasmolScript());
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
            structurePanelListener.selectedSeqRange(seqSelectStart, seqSelectEnd);
            //seqTextPane.selectedSeqRange(seqSelectStart, seqSelectEnd);
            
            // lock selectiion
            //dascanv.selectionLocked(true);
            structurePanelListener.selectionLocked(true);
            //seqTextPane.selectionLocked(true);
            
            //reset...
            seqSelectStart = -1;
            seqSelectEnd = -1;
        } else {
            // perhaps a PDB range has been provided
            String pdbSelectStart = startParameters.getPdbSelectEnd();
            String pdbSelectEnd   = startParameters.getPdbSelectEnd();
            
            if ( pdbSelectStart != null){
                String cmd = "select " + pdbSelectStart + " - " + pdbSelectEnd + "; set displaySelected";
                structurePanelListener.executeCmd(cmd);
                pdbSelectStart = null;
                pdbSelectEnd = null;
            }
        }
        
      
        
        updateDisplays();
        //notifyAll();
                
    }
    */
    
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
    
    /** send a command to Jmol *
    public void executeCmd(String cmd) {
        //logger.finest("executing Command "+ cmd);
        structurePanel.executeCmd(cmd);	
    }
    */
    public String getPDBCode(){
        return structure.getPDBCode();
    }
    
    public String getUniProtCode(){
        int c = getCurrentChainNumber();
        Chain chain = getChain(c);
        return chain.getSwissprotId();
    }
    
    public void setConfiguration(RegistryConfiguration reg) {
        logger.info("setting configuration");
        config = reg;
        List s = config.getServers();
        SpiceDasSource[] sources = new SpiceDasSource[s.size()];
        
        for ( int i=0; i< s.size();i++){
            SpiceDasSource d = (SpiceDasSource)s.get(i);
            sources[i] = d;
            logger.finest(d.getUrl());
        }
        browserPane.setDasSources(sources);
        
        Chain chain = getChain(currentChainNumber) ;
        if ( chain != null) {
            String sp_id = chain.getSwissprotId() ;
            getNewFeatures(sp_id) ;
            //updateDisplays();
        }
        
    }
    
    public int getCurrentChainNumber() {
        return chainDisplay.getCurrentChainNumber();
        //return browserPane.getCurrentChainNumber();
        //return currentChainNumber;
   }
    
    /*
    public void setCurrentChainNumber( int newCurrentChain) {
        setCurrentChainNumber(newCurrentChain,true);
    }*/
    
    public synchronized void setCurrentChain(Chain c, int chainNumber){
        currentChain = c;
        currentChainNumber = chainNumber;
        notifyAll();
    }
    
    /*
    //todo: remove this method:
    public  void setCurrentChainNumber( int newCurrentChain,boolean getNewFeaturesFlag) {
        //logger.info("setCurrentChainNumber " + newCurrentChain + " " + getNewFeaturesFlag);
        System.out.println("SpiceApplication setCurrentChainNumber " + newCurrentChain);
        logger.warning("obsolete method!");
        //structurePanelListener.setCurrentChainNumber(newCurrentChain);
        statusPanel.setCurrentChainNumber(newCurrentChain);
        
        //TODO move this functionality into other class
        // move to SpiceDasServerConfigListener ???
        // update features to be displayed ...
        Chain chain = getChain(newCurrentChain) ;
        System.out.println("SpiceApplication got chain " );
        setCurrentChain(chain, newCurrentChain);
        //currentChain = chain;
        //currentChainNumber = newCurrentChain ;
        if ( chain == null) {
            //notifyAll();
            return ;
        }
        System.out.println("SpiceApplication chain != null " );
        String sp_id = chain.getSwissprotId() ;
        System.out.println("SpiceApplication sp: " +sp_id);
        //logger.info("SP_ID "+sp_id);
        //logger.info("getting annotation");
        // display pdb annotation
        Annotation anno = chain.getAnnotation();
        System.out.println("SpiceApplication anno: " +anno);
        boolean annotationFound = false ;
        //logger.info("chain annotation " + anno);
        if (  ( anno != Annotation.EMPTY_ANNOTATION) && ( anno != null )){
        
            if ( anno.containsProperty("description")){
                //statusPanel.setPDBDescription((String)anno.getProperty("description"));
                //logger.info("PDB description of chain: "+(String)anno.getProperty("description") );
                annotationFound = true ;
            }
        }
        
        if ( ! annotationFound ) {
            //statusPanel.setPDBDescription("no chain description");
            //logger.info("not chain data found :-(");
        }
        
        //statusPanel.setSP(sp_id);
        System.out.println("SpiceApplication statusPanel set: ");
        if (sp_id != null){
            //upMenu.setEnabled(true);
            //dastyMenu.setEnabled(true);
            //proviewMenu.setEnabled(true);
            
        } else {
            //upMenu.setEnabled(false);
            //dastyMenu.setEnabled(false);
            //proviewMenu.setEnabled(false);
            
            logger.info("no UniProt sequence found for"+chain.getName());
        }
        
        System.out.println("SpiceApplication setCurrentChain .. setChain dascanv");
        //dascanv.setChain(chain);
        System.out.println("SpiceApplication setCurrentChain ..  chain set in dascanv");
        if(getNewFeaturesFlag){
            //logger.info("getting new features");            
            getNewFeatures(sp_id) ;
        }
        
        //notifyAll();
        /*
         //TODO: re-enable feature caching ...
        String mem_id = makeFeatureMemoryCode(sp_id);
        ArrayList tmpfeat = getFeaturesFromMemory(mem_id) ;
        
        if ( tmpfeat.size() == 0 ) {
            if ( isLoading()) {
                //logger.log(Level.WARNING,"already loading data, please wait");
                return ;
            }
            getNewFeatures(sp_id) ;
        } else {
            logger.finest("setting features for seq " + sp_id + " features size: " + tmpfeat.size());
            //tures(sp_id,tmpfeat);	    
            //features.clear()                     ;
            features = tmpfeat                   ;
            //SeqFeatureCanvas dascanv = daspanel.getCanv();
            dascanv.setSeqLength(chain.getLength());
            dascanv.setChain(chain);
            //dascanv.setChain(chain,currentChainNumber) ;
            //dascanv.setBackground(Color.black)   ;
            seqTextPane.setChain(chain,currentChainNumber);
            //updateDisplays();
        }
        
        
        
    }
*/
    
    public void setLoading(boolean status){
        first_load = status;
        //statusPanel.setLoading(status);
       
    }
    
 
    
    /** get Chain number X from structure 
     * @return a Chain object or null ;
     */
    public Chain getChain(int chainnumber) {
        
        return chainDisplay.getChain(chainnumber);
        
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
    
       
    public synchronized void newConfigRetrieved(RegistryConfiguration conf){
        logger.info("received new config " );
        config = conf;
        configLoaded =true;
        
        List l = config.getAllServers();
        logger.finest("got " + l.size() + " servers");
        Iterator iter = l.iterator();
        SpiceDasSource[] sources = new SpiceDasSource[l.size()];
        int i = 0;
        while (iter.hasNext()){
            SpiceDasSource ds = (SpiceDasSource) iter.next();
            //logger.finest(ds.get)
            sources[i] = ds;
            i++;
        }
        
        // add das sources from arguments ...
        //TODO; add them
        
        browserPane.clearDasSources();
        browserPane.setDasSources(sources);
        
        
        triggerQueuedRequests();
        notifyAll();
    }
    
    public  void triggerQueuedRequests(){
        // trigger queued requests
        logger.info("triggerQueuedRequest");
               
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
        logger.finest("updateDisplays + features size: " + features.size());
        //SeqFeatureCanvas dascanv = daspanel.getCanv();
        
        browserPane.repaint();
        //dascanv.paint(dascanv.getGraphics());
        
        sharedPanel.paint(sharedPanel.getGraphics());
        //leftPanel.paint(leftPanel.getGraphics());
        chainList.paint(chainList.getGraphics());
        
        //lcr.paint();
        //ListCellRenderer lcr = ent_list.getCellRenderer();
        //ent_list.paint() ;
        sharedPanel.setVisible(true);
        //dasPanel.updateUI();
        dasPanel.revalidate();
        dasPanel.repaint();
        
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


    /** open HttpURLConnection. Recommended way to open
     * HttpURLConnections, since this take care of setting timeouts
     * properly for java 1.4 and 1.5*/
    public static HttpURLConnection openHttpURLConnection(URL url) 
	throws IOException, ConnectException {
	HttpURLConnection huc = null;
	huc = (HttpURLConnection) url.openConnection();
	
	String os_name    = java.lang.System.getProperty("os.name");
	String os_version = java.lang.System.getProperty("os.version");
	String os_arch    = java.lang.System.getProperty("os.arch");
	
	String userAgent = "SPICE/" + AboutDialog.getVersion() + "("+os_name+"; "+os_arch + " ; "+ os_version+")";
	//e.g. "Mozilla/5.0 (Windows; U; Win98; en-US; rv:1.7.2) Gecko/20040803"
     huc.addRequestProperty("User-Agent", userAgent);
	//logger.finest("opening "+url);


	// use reflection to determine if get and set timeout methods for urlconnection are available
        // seems java 1.5 does not watch the System properties any longer...
        // and java 1.4 did not provide these...
	// for 1.4 see setSystemProperties
	int timeout = CONNECTION_TIMEOUT;
	try {
	    // try to use reflection to set timeout property
	    Class urlconnectionClass = Class.forName("java.net.HttpURLConnection");
	    
            Method setconnecttimeout = urlconnectionClass.getMethod (
								     "setConnectTimeout", new Class [] {int.class}        
								     );	
	    setconnecttimeout.invoke(huc,new Object[] {new Integer(timeout)});
	    
	    Method setreadtimeout = urlconnectionClass.getMethod (
								  "setReadTimeout", new Class[] {int.class}
								  );
	    setreadtimeout.invoke(huc,new Object[] {new Integer(timeout)});
	    //System.out.println("successfully set java 1.5 timeout");
	} catch (Exception e) {
	    //e.printStackTrace();
	    // most likely it was a NoSuchMEthodException and we are running java 1.4.
	}
	return huc;
    }
     
  
}

class MyDasSourceListener implements DasSourceListener{
    SPICEFrame parent;
    public MyDasSourceListener(SPICEFrame parent){
        this.parent=parent;
    }
    public void selectedDasSource(DasSourceEvent event){
        SpiceDasSource ds = event.getDasSource().getDasSource();
        DasSourceDialog dsd = new DasSourceDialog(parent, ds);
        dsd.show();
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

class MyLoadingThread extends Thread{
    
    SpiceApplication parent;
    String type;
    String code;
    
    public MyLoadingThread(String type, String code, SpiceApplication parent){
        System.out.println("init MyLoadingThread");
        this.parent = parent;
        this.type = type;
        this.code=code;
    }
    
    public void run(){
        System.out.println("started new MyLoadingThread " + type + " " +code);
        parent.load(type,code);
        
    }
}






