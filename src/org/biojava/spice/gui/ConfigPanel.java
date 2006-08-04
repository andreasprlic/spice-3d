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
 * Created on Aug 4, 2006
 *
 */
package org.biojava.spice.gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.MenuElement;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;
import org.biojava.spice.SPICEFrame;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.config.ConfigurationListener;
import org.biojava.spice.config.RegistryConfigIO;
import org.biojava.spice.config.RegistryConfiguration;
import org.biojava.spice.manypanel.BrowserPane;
import org.biojava.spice.utils.BrowserOpener;

/** a Component that provides the GUI for configuring SPICE
 * 
 * @author Andreas Prlic
 * @since 3:31:04 PM
 * @version %I% %G%
 */
public class ConfigPanel extends JPanel implements ConfigurationListener{
    
    private static final long serialVersionUID = 8273923744127087421L;
    static Logger    logger      = Logger.getLogger("org.biojava.spice");
    static String[] colNames= new String [] {"active","nickname","public"};
    static String[] DASSOURCE_FIELDS = new String[] { 
        "url",
        "adminemail", 
        "capabilities",
        "descrioption",
        "public",
        "active"
    };
    
   
    RegistryConfiguration   config;
    JTabbedPane             tabbedPane;
    List                    entryFormFields;
    MyTableModel            dasSourceTableModel;
    JTable                  dasSourceTable;
    JTextField              pdbDirectory;
    JFileChooser            chooser = new JFileChooser(); 
    JTextField              fileExtensions;      
    JEditorPane             sourceDescription;
    JScrollPane             descriptionScrollPane;
    JPopupMenu              tablePopup;
    JComboBox               updateBehaveList;    
    
    int selectMoveStartPosition;
    SpiceApplication spice                   ;
    
    String baseName="spice";
    ResourceBundle resource;
   
    
    public ConfigPanel(SpiceApplication spice,RegistryConfiguration conf) {
    
        super();
        
        resource = ResourceBundle.getBundle(baseName);
        
        this.spice = spice;
        this.config = conf;
        
        chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("select directory containing PDB files");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        
        tabbedPane = new JTabbedPane();
        ImageIcon icon = SpiceApplication.createImageIcon("spice16x16.gif");
        
        selectMoveStartPosition = -1 ;
        
        
        ////////////////////////////////////////////////////////
        /// list available DAS servers
        ////////////////////////////////////////////////////////
        
        
        JPanel seqstrucpanel = getAvailablePanel();
        tabbedPane.addTab("list", icon, seqstrucpanel,
        "configure sequence and structure servers");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        
        
        ////////////////////////////////////////////////////////
        /// add a local DAS source Panel 
        ////////////////////////////////////////////////////////
        
        
        JPanel addLocalPanel = getAddLocalPanel();              
        tabbedPane.addTab("add local", icon, addLocalPanel,"add a local DAS source");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);       
        
        ////////////////////////////////////////////////////////
        /// get PDB from local installation
        ////////////////////////////////////////////////////////
        
        JPanel localPDBPanel = getLocalPDBPanel();       
        tabbedPane.addTab("local PDB files", icon, localPDBPanel,"try to load PDB files from local directory first.");
        
        
        ////////////////////////////////////////////////////////
        /// general Spice Config
        ////////////////////////////////////////////////////////
        
        JPanel generalConfigPanel = getGeneralConfigPanel();        
        tabbedPane.addTab("general config", icon, generalConfigPanel,"general Spice config");
        
        
        
        ////////////////////////////////////////////////////////
        //Add the tabbed pane to this panel.
        Box vBox = Box.createHorizontalBox();
        vBox.setBorder(BorderFactory.createEmptyBorder());
        vBox.add(tabbedPane);
        add(vBox);
        
        //Uncomment the following line to use scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }
    
    public String[] getColNames(){
        return colNames ;
    }
    
    
    public void newConfigRetrieved (RegistryConfiguration cfg ) {
        config = cfg;
        updateDasSourceTable();
    }
    
    protected JPanel getGeneralConfigPanel(){
        //JPanel panel = new JPanel();
        
        TitledBorder dasborder;
        dasborder = BorderFactory.createTitledBorder("general");
        
        JPanel generalConfigForm = new JPanel();
        
        generalConfigForm.setBorder(dasborder);
        
        Box v = Box.createVerticalBox();
        JTextField txt = new JTextField(" contact DAS-registration server");
        txt.setEditable(false);
        txt.setBorder(BorderFactory.createEmptyBorder());
        v.add(txt);
        
        String[] freq = { "always","once per day"};
        updateBehaveList = new JComboBox(freq) ;        
        updateBehaveList.setEditable(false);
        updateBehaveList.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        String selectedFreq = config.getUpdateBehave();
        int index = 1 ;
        if (selectedFreq.equals("always")) 
            index = 0 ;
        updateBehaveList.setSelectedIndex(index);
        
        v.add(updateBehaveList);
        
        
        JButton contactRegistryNow = new JButton ("Now");
        ConfigActionListener cal = new ConfigActionListener(spice,config,this);
        contactRegistryNow.addActionListener(cal);
        
        
        Box h = Box.createHorizontalBox();
        JTextField txt2 = new JTextField("detect available servers") ;
        txt2.setEditable(false);
        txt2.setBorder(BorderFactory.createEmptyBorder());
        
        h.add(txt2);
        h.add(contactRegistryNow);
        
        
        
        v.add(h);
        
        
        // config which registry to use.    
        JTextField regdesc  = new JTextField("use registry");
        regdesc.setEditable(false);
        regdesc.setBorder(BorderFactory.createEmptyBorder());
        v.add(regdesc);
        JTextField registry = new JTextField(config.getRegistryUrl().toString());
        v.add(registry);
        
        
        //   configure window behaviour.
        
        JTextField jmoldesc  = new JTextField("position of Jmol - 3D structure window");
        jmoldesc.setEditable(false);
        jmoldesc.setBorder(BorderFactory.createEmptyBorder());
        v.add(jmoldesc);
        
        String[] windowPosition = {"top", "right","left","bottom"};
        JComboBox windowlayout = new JComboBox(windowPosition);
        v.add(windowlayout);
        
        generalConfigForm.add(v);
        //generalConfigForm.add(contactRegistryNow);
        
        return generalConfigForm;
        
        
    }
    
    protected JPanel getLocalPDBPanel() {
        JPanel localPDBPanel = new JPanel();
        
        
        TitledBorder dasborder3;
        dasborder3 = BorderFactory.createTitledBorder("get PDB from local dir");
        
        JPanel pdbDirForm = new JPanel();
        
        Box v = Box.createVerticalBox();
        
        pdbDirForm.setBorder(dasborder3);
        
        Box h = Box.createHorizontalBox();
        
        JTextField txt = new JTextField("try to get PDB files from local directory, if not found, get from public DAS server");
        txt.setEditable(false);
        txt.setBorder(BorderFactory.createEmptyBorder());
        v.add(txt);
        
        
        //txt.setEditable(false);
        
        JTextField f = new JTextField("file://");
        f.setEditable(false);
        f.setBorder(BorderFactory.createEmptyBorder());
        h.add(f);
        pdbDirectory = new JTextField("");
        //pdbDirectory.setMaximumSize(new Dimension(300,30));
        //pdbDirectory.setPreferredSize(new Dimension(300,30));
        h.add(pdbDirectory);
        
        JButton go = new JButton("Choose ...");
        
        go.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //int result;
                
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
                    
                    //System.out.println("getCurrentDirectory(): " +  chooser.getCurrentDirectory());
                    //System.out.println("getSelectedFile() : " +  chooser.getSelectedFile());
                    pdbDirectory.setText( chooser.getSelectedFile().toString());
                    
                }
                else {
                    //System.out.println("No Selection ");
                }
            }   
            
        });
        
        h.add(go);
        v.add(h);
        
        Box h2 = Box.createHorizontalBox();
        
        JTextField fileExtensionsTxt = new JTextField("file extensions for PDB files:");
        fileExtensionsTxt.setEditable(false);
        fileExtensionsTxt.setBorder(BorderFactory.createEmptyBorder());
        
        h2.add(fileExtensionsTxt);
        
        fileExtensions = new JTextField( ".ent .pdb .ent.Z .pdb.Z" );
        //fileExtensions.setPreferredSize( new Dimension(300,30)      );
        h2.add(fileExtensions);
        
        v.add(h2);
        
        
        pdbDirForm.add(v);
        localPDBPanel.add(pdbDirForm);
        return localPDBPanel ;
        
    }
    
    protected JPanel getAddLocalPanel() {
        JPanel addLocalPanel = new JPanel();
        addLocalPanel.setLayout(new BoxLayout(addLocalPanel, BoxLayout.LINE_AXIS));
        
        TitledBorder dasborder2;
        dasborder2 = BorderFactory.createTitledBorder("add local DAS source");
        
        JPanel entryForm = new JPanel();
        entryForm.setBorder(dasborder2);
        entryForm.setLayout(new BoxLayout(entryForm, BoxLayout.LINE_AXIS));
        
        Box vBoxRight =  Box.createVerticalBox();
        vBoxRight.setBorder(BorderFactory.createEmptyBorder());
        Box vBoxLeft  =  Box.createVerticalBox();
        vBoxLeft.setBorder(BorderFactory.createEmptyBorder());
        
        entryFormFields = new ArrayList();
        
        
        for ( int i = 0 ; i < DASSOURCE_FIELDS.length; i++) {
            String col = DASSOURCE_FIELDS[i];
            
            if ( col.equals("public")  || col.equals("active") ) 
                continue ;
            
            JTextField txt1 = new JTextField(col);
            txt1.setEditable(false);
            txt1.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
            txt1.setBorder(BorderFactory.createEmptyBorder());
            vBoxLeft.add(txt1);
            
            if (col.equals("coordinateSystems")) {
                // display coordinateSystems box
                String[] coords = { BrowserPane.DEFAULT_UNIPROTCOORDSYS, BrowserPane.DEFAULT_PDBCOORDSYS, BrowserPane.DEFAULT_ENSPCOORDSYS};
                JComboBox list = new JComboBox(coords) ;        
                list.setEditable(false);
                list.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
                list.setSelectedIndex(0);
                vBoxRight.add(list);    
                entryFormFields.add(list);
                
            } else if ( col.equals("capabilities")) {
                JList list = new JList(config.getCapabilities());
                list.setVisibleRowCount(1);
                //list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                if ( list.getMaxSelectionIndex() > 3) {
                    list.setSelectedIndex(4);
                }
                list.setMaximumSize(new Dimension(Short.MAX_VALUE,60));
                JScrollPane jsp = new JScrollPane(list);
                jsp.setMaximumSize(new Dimension(Short.MAX_VALUE,60));
                jsp.setViewportView(list);
                vBoxRight.add(jsp);
                entryFormFields.add(list);
                
                /**
                 JComboBox comboBox = new JComboBox(config.getCapabilities());
                 comboBox.setEditable(false);
                 comboBox.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
                 comboBox.setSelectedIndex(4);
                 vBoxRight.add(comboBox);
                 
                 entryFormFields.add(comboBox);
                 */
                
            } else {
                
                JTextField txt2 = new JTextField("");
                txt2.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
                vBoxRight.add(txt2);
                entryFormFields.add(txt2);
            }
        }
        
        
        entryForm.add(vBoxLeft);
        entryForm.add(vBoxRight);
        addLocalPanel.add(entryForm);
        return addLocalPanel ;
    }
    
    protected void selectAllSources(){
        List servers = config.getAllServers();

        int i = -1;
        Iterator iter = servers.iterator();
        while (iter.hasNext()){
            i++;
            SpiceDasSource ds = (SpiceDasSource) iter.next();
            config.setStatus(ds.getUrl(),true);
            dasSourceTableModel.setValueAt(new Boolean(true),i,0);
        }
    }
    
    protected JPanel getAvailablePanel() {
        TitledBorder dasborder1;
        dasborder1 = BorderFactory.createTitledBorder("available DAS sources");
        
        // Make sequence and structure Panel
        JPanel seqstrucpanel = new JPanel();
        seqstrucpanel.setLayout(new BoxLayout(seqstrucpanel, BoxLayout.Y_AXIS));    
        //List sequenceservers = config.getServers() ;
        
        JButton selectAll = new JButton("Select All");
        Box xBox = Box.createHorizontalBox();
        xBox.add(selectAll);
        seqstrucpanel.add(xBox);
        
        selectAll.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent arg0) {
              // selectAllDasSources
                selectAllSources();
            }
            
        });
        
        
        Action searchAction = new AbstractAction(resource.getString("org.biojava.spice.gui.ConfigPanel.Search")) {
            public static final long serialVersionUID = 0l;
            // This method is called when the button is pressed
            public void actionPerformed(ActionEvent evt) {
                // Perform action...
               System.out.println("search action!");
            }
        };
        
        JButton search = new JButton(searchAction);
        xBox.add(search);
        JTextField searchBox = new JTextField();
        searchBox.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        xBox.add(searchBox);
        
        
        
        
        Object seqdata[][] = getTabData();
        
        //System.out.println(seqdata);
        //JTable table= new JTable(seqdata,colNames);
        dasSourceTableModel = new MyTableModel(this,seqdata,colNames);
        //mtm.getModel().addTableModelListener(this);
        
        dasSourceTable  = new JTable(dasSourceTableModel);
        
        // Disable auto resizing
        dasSourceTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        
        for (int vColIndex=0; vColIndex< colNames.length ; vColIndex++){
            
            TableColumn col = dasSourceTable.getColumnModel().getColumn(vColIndex);
            
            int width = 30;
            if ( colNames[vColIndex].equals("nickname"))
                width = 160;
            col.setPreferredWidth(width);
        }
                
        // Configure some of JTable's paramters
        dasSourceTable.setShowHorizontalLines( false );
        dasSourceTable.setRowSelectionAllowed( true );
                
        dasSourceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JList tmp = new JList() ;
        
        ListSelectionModel lsm = tmp.getSelectionModel();
        lsm.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
              
                if (lsm.isSelectionEmpty()) {
                    return ;
                }
                List servers = config.getAllServers();
                
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
         
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                        SpiceDasSource ds = (SpiceDasSource) servers.get(i);  
                        sourceDescription.setText(convertDasSource2HTML(ds));
                        
                        descriptionScrollPane.getViewport().setViewPosition(new Point(0,0));
                        descriptionScrollPane.getVerticalScrollBar().setValue(0);
                        repaint();
                        sourceDescription.repaint();
                        descriptionScrollPane.repaint();
                       
                        
                    }
                }
                
            }
        });
        
        dasSourceTable.setSelectionModel(lsm);
        
        dasSourceTable.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e) {
                
                ListSelectionModel lsm = dasSourceTable.getSelectionModel();
                
         
                int minIndex = lsm.getMinSelectionIndex();
                
                selectMoveStartPosition = minIndex ;
                
            }
        });
        
        
        // sorting of order of sources within table ...
        dasSourceTable.addKeyListener(new KeyListener(){
            public void keyTyped(KeyEvent e){}
            public void keyPressed(KeyEvent e){}
            public void keyReleased(KeyEvent e){
                int code = e.getKeyCode();
                               
                // get selected DAS source 
                ListSelectionModel lsm = dasSourceTable.getSelectionModel();
                          
                int minIndex = lsm.getMinSelectionIndex();
            
                
                if (selectMoveStartPosition == -1 ) {
                    return ;
                }
                
                if  (
                        ( code == KeyEvent.VK_PAGE_UP) || 
                        ( code == KeyEvent.VK_UP) ||
                        ( code == KeyEvent.VK_PAGE_DOWN) ||
                        ( code == KeyEvent.VK_DOWN) 
                )
                    
                {
                    config.moveServer(selectMoveStartPosition,minIndex);
                    updateDasSourceTable();
                }
                selectMoveStartPosition = minIndex ;
            }            
        });
        
        
        //Create the popup menu.
        tablePopup = new JPopupMenu();
        
        
        MenuListener ml = new MenuListener(spice,dasSourceTable,config,this);
        
        JMenuItem menuItem = new JMenuItem("activate");
        menuItem.addActionListener(ml);
        tablePopup.add(menuItem);
        menuItem = new JMenuItem("delete");
        menuItem.addActionListener(ml);
        tablePopup.add(menuItem);
        
        
        MouseListener popupListener = new PopupListener(tablePopup,dasSourceTable,config);
        
        dasSourceTable.addMouseListener(popupListener);
        
        // Add the table to a scrolling pane      
        JScrollPane seqscrollPane = new JScrollPane(dasSourceTable) ;
        seqscrollPane.setBorder(dasborder1);
        
        sourceDescription = new JEditorPane("text/html", "<html><body>"+
                "<font size=\"2\" face=\"Verdana, Arial, Helvetica, sans-serif\">"+
                "Please select a DAS source"+
                "</font></body></html>");
        
        // add mouse cursor 
        addHyperLinkListener();
        
        descriptionScrollPane = new JScrollPane(sourceDescription);
        
        JSplitPane jsplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, seqscrollPane, descriptionScrollPane);
       jsplit.setPreferredSize(new Dimension(200,200));
        jsplit.setOneTouchExpandable(true);   
        jsplit.setResizeWeight(0.5);
                
        Box hBoxer = Box.createHorizontalBox();
        hBoxer.add(jsplit);
        seqstrucpanel.add(hBoxer);
        return seqstrucpanel ;
    }
    
    private void addHyperLinkListener(){
        sourceDescription.setEditable(false);
        
        sourceDescription.addHyperlinkListener(
                new HyperlinkListener()
                {
                    public void hyperlinkUpdate(HyperlinkEvent e)
                    {                    
                        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            String href = e.getDescription();
                            BrowserOpener.showDocument(href);
                        }
                        // change the mouse curor
                        if ( e.getEventType() == HyperlinkEvent.EventType.ENTERED) {                           
                            sourceDescription.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); 
                            sourceDescription.setToolTipText("open in browser: " + e.getURL());
                        }
                        if (e.getEventType() == HyperlinkEvent.EventType.EXITED) { 
                            sourceDescription.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            sourceDescription.setToolTipText("");
                        }
                    }
                });        
    }
    
    
    private String convertDasSource2HTML(SpiceDasSource ds){
        
        
        StringBuffer text = new StringBuffer(
        "<html><body><font size=\"2\" face=\"Verdana, Arial, Helvetica, sans-serif\">");
        
        String font = "<font color=\"#0000FF\"><b>"; 
        String efont = "</b></font>";
        text.append(font + "Id: "+efont  + ds.getId() +
        "<br>");
        text.append(font +"Nickname: "+efont +
                ds.getNickname() + "<br>");
        
        text.append(font + "URL:"+efont + ds.getUrl() +
        "<br>");
        
        text.append(font + "Description: "+efont +
                ds.getDescription() + "<br>");
        
        text.append(font + "Admin Email: "+efont+" <a href=\"mailto:"
                +ds.getAdminemail()
                +"\">"+ds.getAdminemail()+"</a>" +
        "<br>");
        
        
        text.append(font + "Registered at: "+efont + ds.getRegisterDate() +
        "<br>");
        
        text.append(font + "Last successful test: "+efont + ds.getLeaseDate() +
        "<br>");
        
        text.append(font + "Labels: " +efont );
        for(int s=0; s<ds.getLabels().length; s++)
        {
            text.append( ds.getLabels()[s]);
            if(s<ds.getLabels().length-1)
                text.append(",");
            text.append(" ");
        }
        text.append("<br>");
        
        
        
        text.append(font + "Capabilities: "+efont);
        String[] scap = ds.getCapabilities();
        DasCoordinateSystem[] dcs = ds.getCoordinateSystem();
        String testCode = "";
        if ( dcs.length > 0)
            testCode = dcs[0].getTestCode();
        
        for (int j = 0; j < scap.length; j++)
        {
            
            String cap = ds.getUrl()  + scap[j];
            // these DAS commands need a testcode...
            
            
            if ( scap[j].equals("sequence"))
                cap += "?segment="+testCode;
            if ( scap[j].equals("structure"))
                cap += "?query="+testCode;
            if ( scap[j].equals("features"))
                cap += "?segment="+testCode;
            if ( scap[j].equals("alignment"))
                cap += "?query="+testCode;
            
            
            text.append("<a href=\""+cap+"\">"+scap[j]+"</a>");
            if (j < scap.length - 1)
                text.append(", ");
        }
        text.append("<br>");
        
        text.append(font + "Coordinates: " + efont);
        
        for (int j = 0; j < dcs.length; j++)
        {
            text.append(dcs[j].getCategory() + ", " + dcs[j].getName());
            if (dcs[j].getNCBITaxId() != 0)
                text.append(", " + dcs[j].getNCBITaxId());
            if (dcs[j].getOrganismName().length() > 0)
                text.append(", " + dcs[j].getOrganismName());
            
            text.append("<br>");
        }
        
        
        
        
        if (ds.getHelperurl()!=null
                && ds.getHelperurl().length() > 0)
        {
            text.append("<font color=\"#0000FF\"><a href=\"" +
                    ds.getHelperurl()
                    + "\">Go to site</a></font<br>");
        }
        
        text.append("</font><body></html>");
        
        return text.toString();
    }
    
    protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }
    
    private Map convertSource2Map(SpiceDasSource source) {
        HashMap server = new HashMap();
        server.put("name",source.getUrl()); // for backwards compability
        server.put("url",source.getUrl());
        server.put("coordinateSystems",source.getCoordinateSystem());
        server.put("description",source.getDescription());
        server.put("adminemail",source.getAdminemail());
        server.put("nickname",source.getNickname());
        if (source.getRegistered()) 
            server.put("public","Y");
        else 
            server.put("public","N");
        server.put("capabilities",source.getCapabilities());
        server.put("active",new Boolean(source.getStatus()));
        return server ;
    }
    
    
    public Object[][] getTabData() {
        List servers = config.getAllServers();

        // active nickname public
        
        Object[][] data = new Object[servers.size()][colNames.length+1];
        
        for ( int i =0; i< servers.size(); i++ ) {
            SpiceDasSource ds = (SpiceDasSource) servers.get(i);
            Map server = convertSource2Map(ds);
            for ( int j =0;j<colNames.length;j++){
                String colname = colNames[j];
                //System.out.println(colname);
                String s = "" ;
                if (colname.equals("active")) {
                    data[i][j] = server.get(colname);
                    continue;
                }
                else {
                    s = (String)server.get(colname);
                }
                data[i][j] = s;
                //data[i][colnames.length] = config.getStatus(i);
            }
        }
        return data ;
    }
    
    public void setServerStatus(String url, Boolean status){
        System.out.print("Setting server status " + url + " " + status);
        logger.finer("setting server status " + url + " " + status);
        boolean flag = status.booleanValue();
        config.setStatus(url,flag);
    }
    
    
    public void saveConfiguration() {
        //System.out.println("saving config");
        int pos = tabbedPane.getSelectedIndex();
        //System.out.println("active tab: " + pos);
        
        
        
        // add a new local DAS source ...
        if ( pos == 1 ) {       
            
            //System.out.println("adding new local DAS source");
            logger.finest("adding new local DAS source");
            HashMap formdata = new HashMap();
            int formPos = -1 ;
            for ( int i = 0 ; i < DASSOURCE_FIELDS.length; i++) {
                String col = DASSOURCE_FIELDS[i];
                if ( col.equals("public") || col.equals("active")) 
                    continue ;
                formPos++ ;
                Object o = entryFormFields.get(formPos);
                
                if ( o instanceof JTextField ) {
                    //JTextField txt = (JTextField)entryFormFields.get(i);
                    JTextField txt = (JTextField)o;
                    String data = txt.getText();
                    //System.out.println(col + " " + data);
                    formdata.put(col,data);
                } else if ( o instanceof JList ) {
                    JList l = (JList) o ;
                    Object[] obj = l.getSelectedValues();
                    String[] data = new String[obj.length];
                    System.arraycopy(obj,0,(Object[])data,0,obj.length);
                    formdata.put(col,data);         
                } else if ( o  instanceof JComboBox) {
                    JComboBox j = (JComboBox) o ;
                    String[] data = new String[1];
                    data[0] = (String)j.getSelectedItem();
                    formdata.put(col,data);
                }
            }
            
            SpiceDasSource sds = new SpiceDasSource();
            sds.setRegistered(false);
            sds.setStatus(true);
            sds.setUrl(              (String) formdata.get("url"));
            sds.setAdminemail(       (String) formdata.get("adminemail"));
            sds.setDescription(      (String) formdata.get("description"));
            
            
            String [] coordSys = (String[]) formdata.get("coordinateSystems");
            DasCoordinateSystem[] dcss = new DasCoordinateSystem[coordSys.length];
            for ( int i = 0 ; i< coordSys.length;i++) {
                DasCoordinateSystem dcs = DasCoordinateSystem.fromString(coordSys[i]);
                dcss[i] = dcs;
            }
            sds.setCoordinateSystem(dcss);
            
            String[] capabs =  (String[]) formdata.get("capabilities") ;        
            //String[] split = capabs.split(" ");
            sds.setCapabilities(capabs);
            
            
            config.addServer(sds);
            
            updateDasSourceTable() ;
            
            //registryIO.saveConfiguration();
        }
        // get PDB files from locally
        else if ( pos == 2 ) {
            String e = fileExtensions.getText() ;
            String[] exts = e.split(" ");
            config.setPDBFileExtensions(exts);
            // add a new "pseudo" DAS source
            
            SpiceDasSource sds = new SpiceDasSource();
            sds.setRegistered(false);
            sds.setUrl("file://"+pdbDirectory.getText());
            sds.setAdminemail("unknown@localhost.org");
            sds.setDescription("Access PDB files from local installation. If file not found, retreive from public DAS server");
            //String[] coordSys = new String[] { "PDBresnum", };
            String[] capabs   = new String[] { "structure", };
            
            DasCoordinateSystem dcs = DasCoordinateSystem.fromString(BrowserPane.DEFAULT_PDBCOORDSYS);
            DasCoordinateSystem[] dcss = new DasCoordinateSystem[1];
            dcss[0] = dcs;
            sds.setCoordinateSystem(dcss);
            sds.setCapabilities(capabs);
            config.addServer(sds);
            updateDasSourceTable();
        }
        else if ( pos == 3 ) {
            String behave = (String)updateBehaveList.getSelectedItem();
            
            //System.out.println("setting update behaviour to " + behave);
            logger.finest("setting update behaviour to " + behave);
            if ( behave.equals("once per day"))
                behave = "day" ;
            config.setUpdateBehave(behave);
            updateDasSourceTable();
        }
        
        // save overall registry
        URL registryurl = config.getRegistryUrl();
        URL[] uarr = new URL[1];
        uarr[0] = registryurl;
        RegistryConfigIO registryIO = new RegistryConfigIO(uarr);
        registryIO.setConfiguration(config);
        registryIO.addConfigListener(spice);
        registryIO.saveConfiguration();
        
    }
    
    public void updateDasSourceTable(){
        Object seqdata[][] = getTabData();
        dasSourceTableModel = new MyTableModel(this,seqdata,colNames);
        dasSourceTable.setModel(dasSourceTableModel);
        this.repaint();
        tabbedPane.setSelectedIndex(0);
    }

    


}

/** a table model where the last colun is a checkbox to deceide if true or false */
class MyTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 8273923744127087420L;
    ConfigPanel parent ;
    
    private Object[][] data ;
    private String[]   columnNames  ;
    
    public MyTableModel(ConfigPanel parent_,Object[][]seqdata, String[] columnNames_){
        super();
        parent = parent_ ;
        columnNames = columnNames_;
        
        setData(seqdata);
        
    }
    
    
    private void setData(Object[][]seqdata) {
        Object[][] o = new Object[seqdata.length][columnNames.length];
        for ( int i = 0 ; i < seqdata.length; i++){
            for ( int j =0 ; j < columnNames.length; j++){
                o[i][j] = seqdata[i][j];
            }
            //o[i][columnNames.length-1] = new Boolean(true);
        }
        data = o ;
    }
    
    public int getColumnCount() {
        return columnNames.length;
    }
    
    public int getRowCount() {
        return data.length;
    }
    
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    public Object getValueAt(int row, int col) {
        //System.out.println("getValueAt");
        if ((row > data.length) || ( col > columnNames.length))
        {
            //System.out.println("out of range");
            return null ;
        }
        return data[row][col];
    }
    
    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        //System.out.println("getColumnClass " + c);
        return getValueAt(0, c).getClass();
    }
    
    
    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col > 0  ) {
            return false;
        } else {
            return true;
        }
    }
    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        
         System.out.println("Setting value at " + row + "," + col
         + " to " + value
         + " (an instance of "
         + value.getClass() + ")");
         
         
        data[row][col] = value;
        
        if ( col == 0) {
            
            //String url = (String)model.getValueAt(row,0);
            // Do something with the data...
            //Boolean status = (Boolean) model.getValueAt(row, column);
            String url = (String)getValueAt(row,2);
            System.out.println("setting server status " + value);
            parent.setServerStatus(url,(Boolean)value) ;
        }
        
        fireTableCellUpdated(row, col);
    }
    
    public void tableChanged(TableModelEvent e) {
        System.out.println("tableChanged " + e.getColumn());
        int row = e.getFirstRow();
        int column = e.getColumn();
        MyTableModel model = (MyTableModel)e.getSource();
        //String columnName = model.getColumnName(column);
        //Object cell = model.getValueAt(row, column);
        
        if ( column == 0) {
            
            String url = (String)model.getValueAt(row,2);
            // Do something with the data...
            Boolean status = (Boolean) model.getValueAt(row, column);
            parent.setServerStatus(url,status) ;
        }
    }
}



class PopupListener extends MouseAdapter {
    JPopupMenu popup;
    JTable table    ;
    RegistryConfiguration config ;
    PopupListener(JPopupMenu popupMenu,JTable tab,RegistryConfiguration conf) {
        popup  = popupMenu;
        table  = tab     ;
        config = conf    ;
    }
    
    public void mousePressed(MouseEvent e) {
        //System.out.println(e);
        maybeShowPopup(e);
    }
    
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }
    
    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            
            int pos = table.getSelectedRow();
            if ( pos < 0) 
                return ;
            //System.out.println("seleceted pos " + pos);
            
            SpiceDasSource ds = config.getServer(pos);
            
            // get the menu items
            MenuElement[] m =   popup.getSubElements() ;
            JMenuItem m0 = (JMenuItem)m[0].getComponent();
            JMenuItem m1 = (JMenuItem)m[1].getComponent();
            
            // adapt the display of the MenuItems
            if ( ds.getStatus()) 
                m0.setText("inactivate") ;
            else 
                m0.setText("activate");
            
            if (ds.getRegistered())
                m1.setEnabled(false);
            else
                m1.setEnabled(true);

            popup.show(e.getComponent(),               
                    e.getX(), e.getY());
        }
    }
}


class ConfigActionListener implements ActionListener{
    SPICEFrame spice ;
    RegistryConfiguration config;
    ConfigPanel parent;
    static Logger    logger      = Logger.getLogger("org.biojava.spice");
    
    public ConfigActionListener(SPICEFrame spice_, RegistryConfiguration config_, ConfigPanel tpd){
        spice = spice_;
        config = config_;
        parent = tpd;
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            URL registryurl = config.getRegistryUrl();
            URL[] uarr = new URL[1];
            uarr[0] = registryurl;
            RegistryConfigIO registryIO = new RegistryConfigIO(uarr);
            registryIO.setForceUpdate(true);
            registryIO.addConfigListener(parent);
            registryIO.run();       
            
        } catch (Exception ex) {            
            logger.log(Level.WARNING,ex.getMessage());
        }
    }   
    
}




class MenuListener
implements ActionListener
{
    JTable table                 ;
    RegistryConfiguration config ;
    ConfigPanel parent        ;
    SPICEFrame spice             ;
    static Logger    logger      = Logger.getLogger("org.biojava.spice");
    
    public MenuListener( SPICEFrame spice_, 
            JTable tab,RegistryConfiguration conf,
            ConfigPanel tabd ){
        table  = tab  ;
        config = conf ;
        parent = tabd  ;
        spice = spice_;
    }
    
    public void actionPerformed(ActionEvent e){
        JMenuItem source = (JMenuItem)(e.getSource());
        
        
        String cmd =  source.getText();
        int    pos = table.getSelectedRow();
        if ( pos < 0) 
            return ;
        //logger.finest("selected in row "+pos+" cmd "+cmd);
        SpiceDasSource ds = config.getServer(pos);
        String[] colNames = parent.getColNames();
        
        if (cmd.equals("activate")) {
            ds.setStatus(true);
            table.setValueAt(new Boolean(true),pos,colNames.length-1);
        }
        else if ( cmd.equals("inactivate")) {
            ds.setStatus(false);
            table.setValueAt(new Boolean(false),pos,colNames.length-1);
        }
        else if ( cmd.equals("delete")) { 
            logger.finest("deleteting das source ..." +pos);
            config.deleteServer(pos);
        }
        
        
        parent.updateDasSourceTable();
        
    }
}

