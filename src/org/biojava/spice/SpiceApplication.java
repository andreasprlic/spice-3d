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


import org.biojava.spice.Panel.*;
import org.biojava.spice.Config.*;
import org.biojava.spice.Feature.*;
import org.biojava.spice.GUI.*;

// for protein 3D stuff
import org.biojava.bio.structure.*;


// some utils 
import java.util.HashMap   ;
import java.util.ArrayList ;
import java.util.List ;


// to get config file via http
import java.net.URL;
import java.net.MalformedURLException;

// jnlp - to display URL in browser 
import javax.jnlp.*;

// logging
import java.util.logging.* ;
import java.util.Iterator  ;

// gui
import java.awt.Dimension                       ;
import java.awt.Color                           ;
import java.awt.BorderLayout                    ;
import java.awt.Event                           ;
import java.awt.event.*                         ;

import javax.swing.Box                          ;
import javax.swing.JSplitPane                   ;
import javax.swing.JFrame                       ;
import javax.swing.JList                        ;
import javax.swing.JScrollPane                  ;
import javax.swing.DefaultListModel             ;
import javax.swing.JTextField                   ;
import javax.swing.event.ListSelectionListener  ;
import javax.swing.event.ListSelectionEvent     ;  
import javax.swing.ImageIcon                    ;
import javax.swing.BorderFactory                ;
import javax.swing.JMenuBar                     ;
import javax.swing.JMenu                        ;
import javax.swing.JMenuItem                    ;



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
    
    
    URL REGISTRY_URL    ; // the url to the registration server
    
    
    static int    DEFAULT_Y_SCROLL = 50 ;
    static String XMLVALIDATION = "false" ;   
    static String INIT_SELECT = "select all; cpk off ; wireframe off ; backbone off; cartoon on; colour chain;select not protein and not solvent;spacefill 2.0;";    
    RegistryConfiguration config      ;
    Structure structure ; 
    String pdbcode      ;
    String pdbcode2     ; // only set if displaying structure alignments 
    
    Chain currentChain ;
    int currentChainNumber  ;
    HashMap memoryfeatures; // all features in memory
    List features ;    // currently being displayed 
    
    
    
    StructurePanel structurePanel ;    
    JTextField seq_pos ;
    JList ent_list;   // list available chains
    SeqFeaturePanel dascanv ;
    JScrollPane dasPanel ;
    //JPanel leftPanel ;
    JSplitPane sharedPanel;
    JSplitPane mainsharedPanel;
    
    JScrollPane seqScrollPane ;
    JSplitPane  seqSplitPane  ;
    SeqTextPane seqField      ;
    JMenuItem unlock;
    JMenu browseMenu;
    JMenuItem pdbMenu;
    JMenuItem upMenu;
    //JMenuBar menuBar ;
    JTextField getCom ;
    List knownFeatureLinks;
    
    
    StructureCommandPanel  strucommand; 
    StatusPanel statusPanel ;
    String[] txtColor     ;
    Color[] entColors ;
    Color oldColor ; 
    boolean first_load ;
    boolean selectionLocked ;
    boolean structureAlignmentMode ;
    
    //public static Logger logger = Logger.getLogger("org.biojava.spice");
  
    
    
    
    /** start the spice appplication
     */
    public SpiceApplication( URL registry_url) {
        super();
        
        // selection is possible at the start ;
        selectionLocked = false ;
        
        
        currentChainNumber = -1 ;
        currentChain = null;
        
        LoggingPanel loggingPanel = new LoggingPanel(logger);
        loggingPanel.getHandler().setLevel(Level.FINEST);	
        loggingPanel.show(null);
        logger.setLevel(Level.FINEST);
        
        setSystemProperties();
        
        
        
        REGISTRY_URL = registry_url ;
        
        // first thing is to start communication
        
        RegistryConfigIO regi = new RegistryConfigIO (this,REGISTRY_URL);
        regi.run();
        
        structure = null ;
        pdbcode   = null ;
        pdbcode2  = null ;
        txtColor  = new String[7] ;
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
        
        
        //first_load = false ;
        
        structureAlignmentMode = false ;
        
        
        //vBox.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        
        
        // add the Menu
        JMenuBar menu = initMenu();
        this.setJMenuBar(menu);
        
        // init all panels, etc..
        statusPanel    = new StatusPanel(this);
        seq_pos        = new JTextField();
        structurePanel = new StructurePanel(this);	
        dascanv        = new SeqFeaturePanel(this);
        strucommand    = new StructureCommandPanel(this);
        //strucommand    = new JTextField()  ;
        
        Box vBox = arrangePanels(statusPanel,seq_pos,structurePanel,dascanv,strucommand,"left"); 
        
        this.getContentPane().add(vBox);
        this.setLoading(false);
        
        
        memoryfeatures = new HashMap();
        features = new ArrayList();
        
        
        
        this.setTitle("SPICE") ;
        this.setSize(700, 700);
        //this.show();
        
        //this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JFrame.setDefaultLookAndFeelDecorated(false);
        ImageIcon icon = createImageIcon("spice.png");
        this.setIconImage(icon.getImage());
        this.pack();
        
        this.setVisible(true);
        
        logger.finest("getting config");
        config =regi.getConfiguration();
        logger.finest("got config");
        if ( config == null ) {
            String msg = "Unable to contact DAS registration service, can not continue!" ;
            seq_pos.setText(msg);
            logger.log(Level.SEVERE,msg);
            System.err.println(msg);
            
            return ;
            
        }
    }
    
    /** set  a couple of System Properties */
   
    private void setSystemProperties(){
//      on osx move menu to the top of the screen
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        
        // do xml validation when parsing DAS responses (true/false)
        System.setProperty("XMLVALIDATION",XMLVALIDATION);
        
        int timeout = 15000;
        
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("setting timeouts to " + timeout);
        }
        
        // timeouts when doing http connections
        System.setProperty("sun.net.client.defaultConnectTimeout", ""+timeout);
        System.setProperty("sun.net.client.defaultReadTimeout", ""+timeout);
        
        
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
            JTextField seq_pos,
            StructurePanel structurePanel,
            SeqFeaturePanel dascanv, 
            StructureCommandPanel strucommand,
            String structureLocation){
        
        Box vBox = Box.createVerticalBox();
        
        
        // move to submenu
        //this.getContentPane().add(statusPanel,BorderLayout.SOUTH);
        //this.getContentPane().add(statusPanel);
        
        statusPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        vBox.add(statusPanel);
        
        //statusPanel.setLoading(first_load);
        
        
        // init Seqouece position
        seq_pos.setForeground(new Color(255, 255, 255));
        seq_pos.setBackground(new Color(0, 0, 0));
        seq_pos.setSize(700, 30);
        seq_pos.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        seq_pos.setBorder(BorderFactory.createEmptyBorder());
        //this.getContentPane().add(seq_pos,BorderLayout.NORTH);
        //this.getContentPane().add(seq_pos);
        vBox.add(seq_pos);
        
        showStatus("contacting DAS registry");
        
        /// init Structure Panel
        
        //structurePanel.setLayout(new BoxLayout(structurePanel, BoxLayout.X_AXIS));
        structurePanel.setPreferredSize(new Dimension(500, 300));
        structurePanel.setMinimumSize(new Dimension(200,200));
        //structurePanel.addMouseMotionListener(structurePanel);
        //structurePanel.addMouseListener(      structurePanel);
        //this.add(structurePanel,BorderLayout.CENTER);
        
        
        
        
        DefaultListModel model = new DefaultListModel();
        model.add(0,"");
        ent_list=new JList(model);
        EntListCommandListener entact = new EntListCommandListener(this);
        ent_list.addListSelectionListener(entact);
        //ent_list.setPreferredSize(new Dimension(30,30));
        
        
        JScrollPane chainPanel = new JScrollPane(ent_list);
        chainPanel.setPreferredSize(new Dimension(30,30));
        
        
        // init dascanv
        
        //dascanv.setForeground(Color.black);
        dascanv.setBackground(Color.black);
        dascanv.addMouseMotionListener(dascanv);
        dascanv.addMouseListener(      dascanv);
        //dascanv.setOpaque(true) ;
        dascanv.setPreferredSize(new Dimension(200, 200));
        //dascanv.setSize(700,300);
        
        dasPanel = new JScrollPane(dascanv);
        //dasPanel.setOpaque(true);
        //dasPanel.setBackground(Color.black);
        dasPanel.getVerticalScrollBar().setUnitIncrement(DEFAULT_Y_SCROLL);
        dasPanel.getHorizontalScrollBar().setUnitIncrement(DEFAULT_Y_SCROLL);
        //scroll.add(dascanv);
        
        //daspanel = new SeqPanel(this);
        //daspanel.setMinimumSize(new Dimension(100,100));
        
        
        sharedPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                chainPanel, dasPanel);
        sharedPanel.setOneTouchExpandable(true);
        //sharedPanel.setDividerLocation(150);
        sharedPanel.setPreferredSize(new Dimension(400, 400));
        //sharedPanel.setOpaque(true);
        //sharedPanel.setResizeWeight(0);	
        //sharedPanel.setBackground(Color.black);
        
        seqField = new SeqTextPane(this);
        seqField.setSize( 700, 30);
        seqField.setPreferredSize(new Dimension(700, 30));
        seqField.setMinimumSize(new Dimension(700, 30));
        seqField.addMouseMotionListener(seqField);
        seqField.addMouseListener(seqField);
        
        
        // add onMouseOver action
        
        seqScrollPane = new JScrollPane(seqField) ;
        seqScrollPane.setSize( 700, 30);
        //seqScrollPane.setPreferredSize(new Dimension(700, 30));;
        //seqScrollPane.setMinimumSize(  new Dimension(700, 30));;
        
        seqSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                sharedPanel,seqScrollPane);
        
        seqSplitPane.setOneTouchExpandable(true);
        //seqSplitPane.setOpaque(true);
        seqSplitPane.setResizeWeight(0.6);
        //seqSplitPane.setBackground(Color.black);
        seqSplitPane.setPreferredSize(new Dimension(300,300));
        
        //seqSplitPane.setDividerLocation(600);
        
        //sharedPanel.setLayout(new BoxLayout(sharedPanel, BoxLayout.X_AXIS));
        //sharedPanel.add(leftPanel,BorderLayout.EAST);
        //sharedPanel.add(scroll,BorderLayout.WEST);
        //sharedPanel.add(dascanv,BorderLayout.WEST);
        //sharedPanel.add(daspanel,BorderLayout.WEST);
        //sharedPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,300));
        //this.add(sharedPanel,BorderLayout.SOUTH);
        
        if (structureLocation.equals("top"))
            mainsharedPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, structurePanel,seqSplitPane);
        else if  (structureLocation.equals("bottom"))
            mainsharedPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, seqSplitPane,structurePanel);
        else if  (structureLocation.equals("left"))
            mainsharedPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, structurePanel,seqSplitPane);
        else if  (structureLocation.equals("right"))
            mainsharedPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, seqSplitPane,structurePanel);
        
        mainsharedPanel.setOneTouchExpandable(true);
        mainsharedPanel.setResizeWeight(0.6);
        //mainsharedPanel.setDividerLocation(150);
        //mainsharedPanel.setPreferredSize(new Dimension(200, 200));
        //mainsharedPanel.setOpaque(true);
        //mainsharedPanel.setBackground(Color.black);
        //mainsharedPanel.setPreferredSize(new Dimension(700,700));
        //mainsharedPanel.setResizeWeight(0.7);
        //this.getContentPane().add(mainsharedPanel,BorderLayout.NORTH);
        //this.getContentPane().add(mainsharedPanel);
        mainsharedPanel.setBorder(BorderFactory.createEmptyBorder());
        vBox.add(mainsharedPanel,BorderLayout.CENTER);
        
        strucommand.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        //this.getContentPane().add(strucommand,BorderLayout.SOUTH);
        //this.getContentPane().add(strucommand);
        vBox.add(strucommand);
        
        //vBox.add(loggingPanel);
        return vBox;
    }
    
    
    
    
    /* (non-Javadoc)
     * @see org.biojava.spice.SPICEFrame#load(java.lang.String, java.lang.String)
     */
    public void load(String type, String code){
        if (type.equals("PDB")){
            if ( code.length() == 4 ) 
                this.getStructure(code);
            else  
                logger.info("please provide only 4 characters for the PDB code.");
            
        }
        else if (type.equals("UniProt")) {
            //logger.info("got uniprot");
            // connect to Uniprot -pdb alignment service, get PDB code and load it ...
            getUniprot(code);
            
        }
        else if (type.equals("ENSP")) {
            logger.info("load ENSP not implemented, yet!");
            
        }
        else {
            // unknown code type!
            //System.err.println("unknown code type >"+type+"< currently supported: PDB,UniProt,ENSP");
            logger.warning("unknown code type >"+type+"< currently supported: PDB,UniProt,ENSP");
            return;
        }
        
        
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
        
        
        JMenuItem openpdb = new JMenuItem("Open");
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
        unlock = new JMenuItem("Unlock Selection");
        unlock.setMnemonic(KeyEvent.VK_U);
        unlock.setEnabled(selectionLocked);
        
        JMenuItem backbone   = new JMenuItem("Backbone");
        JMenuItem wireframe  = new JMenuItem("Wireframe");
        JMenuItem cartoon    = new JMenuItem("Cartoon");
        JMenuItem ballnstick = new JMenuItem("Ball and Stick");
        JMenuItem spacefill  = new JMenuItem("Spacefill");
        
        JMenuItem colorchain = new JMenuItem("Color - chain");
        JMenuItem colorsec   = new JMenuItem("Color - secondary");
        JMenuItem colorcpk   = new JMenuItem("Color - cpk");
        
        reset.addActionListener     ( ml );
        unlock.addActionListener    ( ml );
        backbone.addActionListener  ( ml );
        wireframe.addActionListener ( ml );	
        cartoon.addActionListener   ( ml );
        ballnstick.addActionListener( ml );
        spacefill.addActionListener ( ml );		
        colorchain.addActionListener( ml );
        colorsec.addActionListener  ( ml );
        colorcpk.addActionListener  ( ml );
        
        
        display.add( reset   );
        display.add( unlock  );
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
        
        
        
        // Browse menu
        
        browseMenu = new JMenu("Browse");
        browseMenu.setMnemonic(KeyEvent.VK_B);
        browseMenu.getAccessibleContext().setAccessibleDescription("open links in browser");
        
        // unique action listener for the browse buttons
        ActionListener bl = new BrowseMenuListener(this);
        menu.add(browseMenu);
        pdbMenu = new JMenuItem("PDB");
        pdbMenu.setMnemonic(KeyEvent.VK_P);
        pdbMenu.setEnabled(false);
        pdbMenu.addActionListener(bl);
        browseMenu.add(pdbMenu);
        
        upMenu = new JMenuItem("UniProt");
        upMenu.setMnemonic(KeyEvent.VK_U);
        upMenu.setEnabled(false);
        upMenu.addActionListener(bl);
        browseMenu.add(upMenu);
        
        
        
        // Alignment submenu
        JMenu align = new JMenu("Alignment");
        align.setMnemonic(KeyEvent.VK_A);
        align.getAccessibleContext().setAccessibleDescription("show alignments");
        menu.add(align);
        
        JMenuItem seqstrucalig = new JMenuItem("Choose");
        seqstrucalig.addActionListener(ml);
        seqstrucalig.setMnemonic(KeyEvent.VK_C);
        align.add(seqstrucalig);
        
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
    
    /** start a new thead that retrieves uniprot sequence, and if available
     protein structure
     */
    public void getUniprot(String uniprot) {
        logger.info("SpiceApplication getUniprot " + uniprot);
        currentChain = null;
        statusPanel.setLoading(true);
        statusPanel.setPDB("");
        statusPanel.setSP(uniprot);	
        LoadUniProtThread thr = new LoadUniProtThread(this,uniprot) ;
        thr.start();
        pdbMenu.setEnabled(false);
        upMenu.setEnabled(false);
    }
    
    
    /** starts a new thread that retreives protein structure using the
     DAS structure command from some other server this thread will
     call the setStructure method to set the protein structure.
     */
    public void getStructure(String pdbcod) {
        
        
        currentChain = null ;
        
        if (logger.isLoggable(Level.FINER)) {
            logger.entering(this.getClass().getName(), "getStructure",  new Object[]{pdbcod});
        }
        
        logger.log(Level.INFO,"getting new structure "+pdbcod);
        
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("SpiceApplication: getStructure "+ pdbcod);
        }
        this.setLoading(true);
        pdbcode = pdbcod ;
        
        //first_load = true ;
        statusPanel.setLoading(true);
        statusPanel.setPDB(pdbcode);
        statusPanel.setSP("");
        
        
        LoadStructureThread thr = new LoadStructureThread(this,pdbcod);
        thr.start();
        
        
        
    }

    private String makeFeatureMemoryCode(String sp_id){
        int i = getCurrentChainNumber();
        
        Chain c = getChain(i); 
        String mem_id = sp_id +","+pdbcode + c.getName() ;
        return mem_id;
    }

    /**  update the currently displayed features */
    public void setFeatures(String sp_id, List tmpfeat) {
        // todo create Feature for structure mapping
        //first_load = false ;
        String mem_id = makeFeatureMemoryCode(sp_id);
        
        

        // TODO: need to move caching of features on a different level.
        // We need to distinguish SP and PDB features...
        // ev. better move cache to FeatureFetcher
        memoryfeatures.put(mem_id,tmpfeat);
        
        
        //statusPanel.setLoading(false);
        features.clear();
        features = tmpfeat ;
        
        // test if features have a LINK field
        // if yes, add to browse menu
        registerBrowsableFeatures(tmpfeat);
        
        //this.paint(this.getGraphics());
        updateDisplays();
    }
    
    
    private void clearBrowsableButtons(){
        int nr = browseMenu.getItemCount();
        //System.out.println("cleaning "+nr+ " menus");
        for ( int i = nr-1; i > 1; i--){
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
            String link =f.getLink();
            if ( link != null){
                if ( knownFeatureLinks.contains(link))
                    continue ;
                
                JMenuItem item = new JMenuItem("open in browser "+ f.getName());
                ActionListener bl = new BrowseMenuListener(this,link);                    
                item.addActionListener(bl);
                browseMenu.add(item);
                knownFeatureLinks.add(link);
            }
        }
    }


    private  void getNewFeatures(String sp_id) {
        //ArrayList featureservers = getFeatureServers() ;
        
        Chain chain = getChain(currentChainNumber) ;
        if ( chain == null) return ;
        //first_load = true ;
        this.setLoading(true);
        features.clear();
        FeatureFetcher ff = new FeatureFetcher(this,config,sp_id,pdbcode,chain);	
        ff.start() ;
        statusPanel.setLoading(true);
        dascanv.setChain(chain,currentChainNumber);
        //dascanv.setBackground(Color.);
        seqField.setChain(chain,currentChainNumber);
        
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
    
    public void showSeqPos(int chainnumber, int seqpos){
        String drstr = getToolString(chainnumber,seqpos);
        showStatus(drstr);
        
    }


    public String getToolString(int chainnumber,int seqpos) {
        //return "tmp in getToolString";
        
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
        
        logger.finest("SpiceApplication got structure " + structure_);
        
        this.setLoading(false);
        //first_load = false ;
        statusPanel.setLoading(false);
        
        if ( structure_.size() < 1 ){
            logger.log(Level.WARNING,"got no structure");
            return ;
        }
        
        
        
        structure = structure_ ; 
        pdbcode = structure.getPDBCode();
        
        if (pdbcode != null)
            pdbMenu.setEnabled(true);
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
        
        setCurrentChainNumber(0);
        Chain chain = getChain(currentChainNumber) ;
        currentChain = chain;
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
    
    public String getPDBCode(){
        return structure.getPDBCode();
    }
    
    public String getUniProtCode(){
        int c = getCurrentChainNumber();
        Chain chain = getChain(c);
        return chain.getSwissprotId();
    }
    public void setConfiguration(RegistryConfiguration reg) {
        config = reg;
        List s = config.getServers();
        for ( int i=0; i< s.size();i++){
            SpiceDasSource d = (SpiceDasSource)s.get(i);
            logger.finest(d.getUrl());
        }
        
        Chain chain = getChain(currentChainNumber) ;
        if ( chain != null) {
            String sp_id = chain.getSwissprotId() ;
            getNewFeatures(sp_id) ;
            //updateDisplays();
        }
        
    }
    
    public int getCurrentChainNumber() {
        return currentChainNumber;
    }
    public void setCurrentChainNumber( int newCurrentChain) {
        logger.finer("setCurrentChain " + newCurrentChain);
        
        
        // update features to be displayed ...
        Chain chain = getChain(newCurrentChain) ;
        currentChain = chain;
        currentChainNumber = newCurrentChain ;
        if ( chain == null) return ;
        String sp_id = chain.getSwissprotId() ;
        logger.finest("SP_ID "+sp_id);
        
        statusPanel.setSP(sp_id);
        if (sp_id != null){
            upMenu.setEnabled(true);
        } else {
            upMenu.setEnabled(false);
        }
        
        String mem_id = makeFeatureMemoryCode(sp_id);
        ArrayList tmpfeat = getFeaturesFromMemory(mem_id) ;
        
        if ( tmpfeat.size() == 0 ) {
            if ( isLoading()) {
                logger.log(Level.WARNING,"already loading data, please wait");
                return ;
            }
            getNewFeatures(sp_id) ;
        } else {
            logger.finest("setting features for seq " + sp_id + " features size: " + tmpfeat.size());
            //tures(sp_id,tmpfeat);	    
            //features.clear()                     ;
            features = tmpfeat                   ;
            //SeqFeatureCanvas dascanv = daspanel.getCanv();
            dascanv.setChain(chain,currentChainNumber) ;
            //dascanv.setBackground(Color.black)   ;
            seqField.setChain(chain,currentChainNumber);
            //updateDisplays();
        }
        
        
    }
    
    public void setLoading(boolean status){
        first_load = status;
        statusPanel.setLoading(status);
    }
    
    public void setSelectionLocked(boolean status) {
        selectionLocked = status ;
        unlock.setEnabled(selectionLocked);     
    }
    
    public boolean isSelectionLocked() {
        return selectionLocked ;
    }
    
    /** get Chain number X from structure 
     * @return a Chain object or null ;
     */
    public Chain getChain(int chainnumber) {
        
        // speedup
        if ( chainnumber == currentChainNumber ){
            if ( currentChain != null)
                return currentChain;
        }
        
        if ( structure == null ) {
            //logger.log(Level.WARNING,"no structure loaded, yet");
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
    
    /** display an URL in the browser that started SPICE */
    public boolean showURL(URL url) {
        try {
            // Lookup the javax.jnlp.BasicService object
            BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
            // Invoke the showDocument method
            return bs.showDocument(url);
        } catch(UnavailableServiceException ue) {
            // Service is not supported
            return false;
        }
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
        if ( chainNumber == currentChainNumber) {
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
        if ( selectionLocked ) return ;
        
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
        if ( chainNumber == currentChainNumber) {
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
        if ( selectionLocked ) return ;
        
        
        String cmd = getSelectStr( chainNumber,  seqpos);
        cmd +=  " spacefill on ;" ;
        structurePanel.executeCmd(cmd);
        //structurePanel.forceRepaint();
        
        if ( colour  != "") {
            colour(chainNumber,seqpos,colour) ;
        }
        
        if ( chainNumber == currentChainNumber ) {
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
        if ( selectionLocked ) return ;
        
        if ( chain_number == currentChainNumber ) {
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
        if ( selectionLocked ) return ;
        
        if ( chain_number == currentChainNumber ){
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
        //dascanv.repaint();
        dascanv.paint(dascanv.getGraphics());
        
        sharedPanel.paint(sharedPanel.getGraphics());
        //leftPanel.paint(leftPanel.getGraphics());
        ent_list.paint(ent_list.getGraphics());
        
        //lcr.paint();
        //ListCellRenderer lcr = ent_list.getCellRenderer();
        //ent_list.paint() ;
        sharedPanel.setVisible(true);
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
        try
        {
           return JNLPProxy.showDocument(url);
        }
        catch ( Exception  ex )
        {
            //ex.printStackTrace ( );
            logger.warning(ex.getMessage());            
        }
        return false;
    }
    
    public boolean showDocument(String urlstring){
        try{
            URL url = new URL(urlstring);
            return showDocument(url);
        } catch (MalformedURLException e){
            logger.warning(e.getMessage());
            return false;
        }
    }
}


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
        spice.setCurrentChainNumber(i);
        //logger.finest(event.id);
        //logger.finest(event.arg);
        
    }
    
}





