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
import org.biojava.spice.ResourceManager;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.config.ConfigurationListener;
import org.biojava.spice.config.RegistryConfigIO;
import org.biojava.spice.config.RegistryConfiguration;
import org.biojava.spice.config.SearchDasSourceManager;
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
    static Logger    logger      = Logger.getLogger("org.biojava.spice"); //$NON-NLS-1$
    public static String[] colNames= new String [] {ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Active"),ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Nickname"),ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Public")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    static String[] DASSOURCE_FIELDS = new String[] { 
        ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Url"), //$NON-NLS-1$
        ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Adminemail"),  //$NON-NLS-1$
        ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Capabilities"), //$NON-NLS-1$
        ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Description"), //$NON-NLS-1$
        ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Public"), //$NON-NLS-1$
        ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Active") //$NON-NLS-1$
    };
    
   
    RegistryConfiguration   config;
    JTabbedPane             tabbedPane;
    List                    entryFormFields;
    MyTableModel            dasSourceTableModel;
    JTable                  dasSourceTable;
    JTextField              pdbDirectory;
    JFileChooser            chooser = new JFileChooser(); 
    JTextField              fileExtensions;      
    JTextField              searchBox;
    JEditorPane             sourceDescription;
    JScrollPane             descriptionScrollPane;
    JPopupMenu              tablePopup;
    JComboBox               updateBehaveList;    
    
    int selectMoveStartPosition;
    SpiceApplication spice                   ;
    
    String baseName="spice"; //$NON-NLS-1$
    ResourceBundle resource;
   
    
    
    public ConfigPanel(SpiceApplication spice,RegistryConfiguration conf) {
    
        super();
        
        resource = ResourceBundle.getBundle(baseName);
        
        this.spice = spice;
        this.config = conf;
        
        chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(".")); //$NON-NLS-1$
        chooser.setDialogTitle(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.12")); //$NON-NLS-1$
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        
        tabbedPane = new JTabbedPane();
        ImageIcon icon = SpiceApplication.createImageIcon(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.13")); //$NON-NLS-1$
        
        selectMoveStartPosition = -1 ;
        
        
        ////////////////////////////////////////////////////////
        /// list available DAS servers
        ////////////////////////////////////////////////////////
        
        
        JPanel seqstrucpanel = getAvailablePanel();
        tabbedPane.addTab(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.14"), icon, seqstrucpanel, //$NON-NLS-1$
        ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.15")); //$NON-NLS-1$
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        
        
        ////////////////////////////////////////////////////////
        /// add a local DAS source Panel 
        ////////////////////////////////////////////////////////
        
        
        JPanel addLocalPanel = getAddLocalPanel();              
        tabbedPane.addTab(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.16"), icon, addLocalPanel,ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.17")); //$NON-NLS-1$ //$NON-NLS-2$
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);       
        
        ////////////////////////////////////////////////////////
        /// get PDB from local installation
        ////////////////////////////////////////////////////////
        
        JPanel localPDBPanel = getLocalPDBPanel();       
        tabbedPane.addTab(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.18"), icon, localPDBPanel,ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.19")); //$NON-NLS-1$ //$NON-NLS-2$
        
        
        ////////////////////////////////////////////////////////
        /// general Spice Config
        ////////////////////////////////////////////////////////
        
        JPanel generalConfigPanel = getGeneralConfigPanel();        
        tabbedPane.addTab(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.20"), icon, generalConfigPanel,ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.21")); //$NON-NLS-1$ //$NON-NLS-2$
        
        
        
        ////////////////////////////////////////////////////////
        //Add the tabbed pane to this panel.
        Box vBox = Box.createHorizontalBox();
        vBox.setBorder(BorderFactory.createEmptyBorder());
        vBox.add(tabbedPane);
        add(vBox);
        
        //Uncomment the following line to use scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }
    
    public RegistryConfiguration getConfiguration(){
        return config;
    }
    
    public void newConfigRetrieved (RegistryConfiguration cfg ) {
        config = cfg;
        updateDasSourceTable();
    }
    
    protected JPanel getGeneralConfigPanel(){     
        
        TitledBorder dasborder;
        dasborder = BorderFactory.createTitledBorder(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.22")); //$NON-NLS-1$
        
        JPanel generalConfigForm = new JPanel();
        
        generalConfigForm.setBorder(dasborder);
        
        Box v = Box.createVerticalBox();
        JTextField txt = new JTextField(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.23")); //$NON-NLS-1$
        txt.setEditable(false);
        txt.setBorder(BorderFactory.createEmptyBorder());
        v.add(txt);
        
        String[] freq = { ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Always"),ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.25")}; //$NON-NLS-1$ //$NON-NLS-2$
        updateBehaveList = new JComboBox(freq) ;        
        updateBehaveList.setEditable(false);
        updateBehaveList.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        String selectedFreq = config.getUpdateBehave();
        int index = 1 ;
        if (selectedFreq.equals(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Always")))  //$NON-NLS-1$
            index = 0 ;
        updateBehaveList.setSelectedIndex(index);
        
        v.add(updateBehaveList);
        
        
        JButton contactRegistryNow = new JButton (ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Now")); //$NON-NLS-1$
        ConfigActionListener cal = new ConfigActionListener(config,this);
        contactRegistryNow.addActionListener(cal);
        
        
        Box h = Box.createHorizontalBox();
        JTextField txt2 = new JTextField(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.28")) ; //$NON-NLS-1$
        txt2.setEditable(false);
        txt2.setBorder(BorderFactory.createEmptyBorder());
        
        h.add(txt2);
        h.add(contactRegistryNow);
        
        v.add(h);
        
        
        // config which registry to use.    
        JTextField regdesc  = new JTextField(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.29")); //$NON-NLS-1$
        regdesc.setEditable(false);
        regdesc.setBorder(BorderFactory.createEmptyBorder());
        v.add(regdesc);
        JTextField registry = new JTextField(config.getRegistryUrl().toString());
        v.add(registry);
        
        
        //   configure window behaviour.
        
        JTextField jmoldesc  = new JTextField(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.30")); //$NON-NLS-1$
        jmoldesc.setEditable(false);
        jmoldesc.setBorder(BorderFactory.createEmptyBorder());
        v.add(jmoldesc);
        
        String[] windowPosition = {ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.31"), ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.32"),ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.33"),ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.34")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        JComboBox windowlayout = new JComboBox(windowPosition);
        v.add(windowlayout);
        
        generalConfigForm.add(v);
        //generalConfigForm.add(contactRegistryNow);
        
        return generalConfigForm;
        
        
    }
    
    protected JPanel getLocalPDBPanel() {
        JPanel localPDBPanel = new JPanel();
        
        
        TitledBorder dasborder3;
        dasborder3 = BorderFactory.createTitledBorder(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.35")); //$NON-NLS-1$
        
        JPanel pdbDirForm = new JPanel();
        
        Box v = Box.createVerticalBox();
        
        pdbDirForm.setBorder(dasborder3);
        
        Box h = Box.createHorizontalBox();
        
        JTextField txt = new JTextField(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.36")); //$NON-NLS-1$
        txt.setEditable(false);
        txt.setBorder(BorderFactory.createEmptyBorder());
        v.add(txt);
        
        
        //txt.setEditable(false);
        
        JTextField f = new JTextField("file://"); //$NON-NLS-1$
        f.setEditable(false);
        f.setBorder(BorderFactory.createEmptyBorder());
        h.add(f);
        pdbDirectory = new JTextField(""); //$NON-NLS-1$
        //pdbDirectory.setMaximumSize(new Dimension(300,30));
        //pdbDirectory.setPreferredSize(new Dimension(300,30));
        h.add(pdbDirectory);
        
        JButton go = new JButton(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.39")); //$NON-NLS-1$
        
        go.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //int result;
                
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
                    
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
        
        JTextField fileExtensionsTxt = new JTextField(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.40")); //$NON-NLS-1$
        fileExtensionsTxt.setEditable(false);
        fileExtensionsTxt.setBorder(BorderFactory.createEmptyBorder());
        
        h2.add(fileExtensionsTxt);
        
        fileExtensions = new JTextField( ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.41") ); //$NON-NLS-1$
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
        dasborder2 = BorderFactory.createTitledBorder(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.42")); //$NON-NLS-1$
        
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
            
            if ( col.equals(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Public"))  || col.equals(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Active")) )  //$NON-NLS-1$ //$NON-NLS-2$
                continue ;
            
            JTextField txt1 = new JTextField(col);
            txt1.setEditable(false);
            txt1.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
            txt1.setBorder(BorderFactory.createEmptyBorder());
            vBoxLeft.add(txt1);
            
            if (col.equals(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.CoordinateSystems"))) { //$NON-NLS-1$
                // display coordinateSystems box
                String[] coords = { BrowserPane.DEFAULT_UNIPROTCOORDSYS, BrowserPane.DEFAULT_PDBCOORDSYS, BrowserPane.DEFAULT_ENSPCOORDSYS};
                JComboBox list = new JComboBox(coords) ;        
                list.setEditable(false);
                list.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
                list.setSelectedIndex(0);
                vBoxRight.add(list);    
                entryFormFields.add(list);
                
            } else if ( col.equals(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Capabilities"))) { //$NON-NLS-1$
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
                
                JTextField txt2 = new JTextField(""); //$NON-NLS-1$
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
    
    protected void showAllSources(){
        updateDasSourceTable();
        
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
        dasborder1 = BorderFactory.createTitledBorder(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.AvailableSources")); //$NON-NLS-1$
        
        // Make sequence and structure Panel
        JPanel seqstrucpanel = new JPanel();
        seqstrucpanel.setLayout(new BoxLayout(seqstrucpanel, BoxLayout.Y_AXIS));    
        //List sequenceservers = config.getServers() ;
        
        JButton selectAll = new JButton(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.SelectAll")); //$NON-NLS-1$
        Box xBox = Box.createHorizontalBox();
        xBox.add(selectAll);
        seqstrucpanel.add(xBox);
        
        selectAll.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent arg0) {          
                selectAllSources();
            }            
        });
        
        
        Action showAllAction = new AbstractAction(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.ShowAll")) {//$NON-NLS-1$

            static final long serialVersionUID = 0l;
            public void actionPerformed(ActionEvent arg0) {
                showAllSources();                
            }            
        };
        
        JButton showAll = new JButton(showAllAction);
        xBox.add(showAll);
        
        searchBox = new JTextField();
        searchBox.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        
        Object seqdata[][] = getTabData();
        
        //System.out.println(seqdata);
        //JTable table= new JTable(seqdata,colNames);
        List sources = config.getAllServers();
        SpiceDasSource[] sourceArray = (SpiceDasSource[]) sources.toArray(new SpiceDasSource[sources.size()]);
        dasSourceTableModel = new MyTableModel(this,seqdata,colNames,sourceArray);
        
        dasSourceTable  = new JTable(dasSourceTableModel);
        
        
        SearchAction searchAction = new SearchAction(resource.getString("org.biojava.spice.gui.ConfigPanel.Search"),
                this,searchBox,dasSourceTable); //$NON-NLS-1$
        
        searchBox.addActionListener(searchAction);
        searchBox.addKeyListener(searchAction);
        JButton search = new JButton(searchAction);
        xBox.add(searchBox);
        xBox.add(search);
       
        
        
        
        // Disable auto resizing
        dasSourceTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        
        for (int vColIndex=0; vColIndex< colNames.length ; vColIndex++){
            
            TableColumn col = dasSourceTable.getColumnModel().getColumn(vColIndex);
            
            int width = 30;
            if ( colNames[vColIndex].equals(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Nickname"))) //$NON-NLS-1$
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
                
                MyTableModel model = (MyTableModel) dasSourceTable.getModel();
                SpiceDasSource[] servers = model.getServers();
                //List servers = config.getAllServers();
                
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
         
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                        SpiceDasSource ds = (SpiceDasSource) servers[i];  
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
        
        
        MenuListener ml = new MenuListener(dasSourceTable,this);
        
        JMenuItem menuItem = new JMenuItem(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Activate")); //$NON-NLS-1$
        menuItem.addActionListener(ml);
        tablePopup.add(menuItem);
        menuItem = new JMenuItem(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Delete")); //$NON-NLS-1$
        menuItem.addActionListener(ml);
        tablePopup.add(menuItem);
        
        
        MouseListener popupListener = new PopupListener(tablePopup,dasSourceTable);
        
        dasSourceTable.addMouseListener(popupListener);
        
        // Add the table to a scrolling pane      
        JScrollPane seqscrollPane = new JScrollPane(dasSourceTable) ;
        seqscrollPane.setBorder(dasborder1);
        
        sourceDescription = new JEditorPane("text/html", "<html><body>"+ //$NON-NLS-1$ //$NON-NLS-2$
                "<font size=\"2\" face=\"Verdana, Arial, Helvetica, sans-serif\">"+ //$NON-NLS-1$
                ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.11")+ //$NON-NLS-1$
                "</font></body></html>"); //$NON-NLS-1$
        
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
                            sourceDescription.setToolTipText(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.60") + e.getURL()); //$NON-NLS-1$
                        }
                        if (e.getEventType() == HyperlinkEvent.EventType.EXITED) { 
                            sourceDescription.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            sourceDescription.setToolTipText(""); //$NON-NLS-1$
                        }
                    }
                });        
    }
    
    
    private String convertDasSource2HTML(SpiceDasSource ds){
        
        
        StringBuffer text = new StringBuffer(
        "<html><body><font size=\"2\" face=\"Verdana, Arial, Helvetica, sans-serif\">"); //$NON-NLS-1$
        
        String font = "<font color=\"#0000FF\"><b>";  //$NON-NLS-1$
        String efont = "</b></font>"; //$NON-NLS-1$
        text.append(font + ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.65")+efont  + ds.getId() + //$NON-NLS-1$
        "<br>"); //$NON-NLS-1$
        text.append(font +ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.67")+efont + //$NON-NLS-1$
                ds.getNickname() + "<br>"); //$NON-NLS-1$
        
        text.append(font + ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.69")+efont + ds.getUrl() + //$NON-NLS-1$
        "<br>"); //$NON-NLS-1$
        
        text.append(font + ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.71")+efont + //$NON-NLS-1$
                ds.getDescription() + "<br>"); //$NON-NLS-1$
        
        text.append(font + ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.73")+efont+" <a href=\"mailto:" //$NON-NLS-1$ //$NON-NLS-2$
                +ds.getAdminemail()
                +"\">"+ds.getAdminemail()+"</a>" + //$NON-NLS-1$ //$NON-NLS-2$
        "<br>"); //$NON-NLS-1$
        
        
        text.append(font + ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.78")+efont + ds.getRegisterDate() + //$NON-NLS-1$
        ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.79")); //$NON-NLS-1$
        
        text.append(font + ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.80")+efont + ds.getLeaseDate() + //$NON-NLS-1$
        "<br>"); //$NON-NLS-1$
        
        text.append(font + ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.82") +efont ); //$NON-NLS-1$
        for(int s=0; s<ds.getLabels().length; s++)
        {
            text.append( ds.getLabels()[s]);
            if(s<ds.getLabels().length-1)
                text.append(","); //$NON-NLS-1$
            text.append(" "); //$NON-NLS-1$
        }
        text.append("<br>"); //$NON-NLS-1$
        
        
        
        text.append(font + ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.86")+efont); //$NON-NLS-1$
        String[] scap = ds.getCapabilities();
        DasCoordinateSystem[] dcs = ds.getCoordinateSystem();
        String testCode = ""; //$NON-NLS-1$
        if ( dcs.length > 0)
            testCode = dcs[0].getTestCode();
        
        for (int j = 0; j < scap.length; j++)
        {
            
            String cap = ds.getUrl()  + scap[j];
            // these DAS commands need a testcode...
            
            
            if ( scap[j].equals("sequence")) //$NON-NLS-1$
                cap += "?segment="+testCode; //$NON-NLS-1$
            if ( scap[j].equals("structure")) //$NON-NLS-1$
                cap += "?query="+testCode; //$NON-NLS-1$
            if ( scap[j].equals("features")) //$NON-NLS-1$
                cap += "?segment="+testCode; //$NON-NLS-1$
            if ( scap[j].equals("alignment")) //$NON-NLS-1$
                cap += "?query="+testCode; //$NON-NLS-1$
            
            
            text.append("<a href=\""+cap+"\">"+scap[j]+"</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (j < scap.length - 1)
                text.append(", "); //$NON-NLS-1$
        }
        text.append("<br>"); //$NON-NLS-1$
        
        text.append(font + ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.10") + efont); //$NON-NLS-1$
        
        for (int j = 0; j < dcs.length; j++)
        {
            text.append(dcs[j].getCategory() + ", " + dcs[j].getName()); //$NON-NLS-1$
            if (dcs[j].getNCBITaxId() != 0)
                text.append(", " + dcs[j].getNCBITaxId()); //$NON-NLS-1$
            if (dcs[j].getOrganismName().length() > 0)
                text.append(", " + dcs[j].getOrganismName()); //$NON-NLS-1$
            
            text.append("<br>"); //$NON-NLS-1$
        }
        
        
        
        
        if (ds.getHelperurl()!=null
                && ds.getHelperurl().length() > 0)
        {
            text.append("<font color=\"#0000FF\"><a href=\"" + //$NON-NLS-1$
                    ds.getHelperurl()
                    + ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.107")); //$NON-NLS-1$
        }
        
        text.append("</font><body></html>"); //$NON-NLS-1$
        
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
        server.put(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Name"),source.getUrl()); // for backwards compability //$NON-NLS-1$
        server.put(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Url"),source.getUrl()); //$NON-NLS-1$
        server.put(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.CoordinateSystems"),source.getCoordinateSystem()); //$NON-NLS-1$
        server.put(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Description"),source.getDescription()); //$NON-NLS-1$
        server.put(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Adminemail"),source.getAdminemail()); //$NON-NLS-1$
        server.put(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Nickname"),source.getNickname()); //$NON-NLS-1$
        if (source.getRegistered()) 
            server.put(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Public"),ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Y")); //$NON-NLS-1$ //$NON-NLS-2$
        else 
            server.put(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Public"),ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.N")); //$NON-NLS-1$ //$NON-NLS-2$
        server.put(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Capabilities"),source.getCapabilities()); //$NON-NLS-1$
        server.put(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Active"),new Boolean(source.getStatus())); //$NON-NLS-1$
        return server ;
    }
    
    
    public Object[][] getTabData() {
        List servers = config.getAllServers();
        SpiceDasSource[] sources = (SpiceDasSource[]) servers.toArray(new SpiceDasSource[servers.size()]);
        return getTabData(sources);
    }
    
    protected MyTableModel getTableModel(){
        return  (MyTableModel)dasSourceTable.getModel();
    }
    
    public Object[][] getTabData(SpiceDasSource[] servers){
        
        
        
        Object[][] data = new Object[servers.length][colNames.length+1];
        
        for ( int i =0; i< servers.length; i++ ) {
            SpiceDasSource ds = (SpiceDasSource) servers[i];
            Map server = convertSource2Map(ds);
            for ( int j =0;j<colNames.length;j++){
                String colname = colNames[j];
                //System.out.println(colname);
                String s = "" ; //$NON-NLS-1$
                if (colname.equals(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Active"))) { //$NON-NLS-1$
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
        //System.out.print("Setting server status " + url + " " + status); //$NON-NLS-1$ //$NON-NLS-2$
        logger.finer("setting server status " + url + " " + status); //$NON-NLS-1$ //$NON-NLS-2$
        boolean flag = status.booleanValue();
        config.setStatus(url,flag);
    }
    
    
    protected void deleteServer(SpiceDasSource ds){
        List servers = config.getAllServers();
        
        for ( int i =0; i< servers.size();i++ ){
            SpiceDasSource tds = (SpiceDasSource) servers.get(i);
            if ( tds.getUrl().equals(ds.getUrl())){
                config.deleteServer(i);
                break;
            }
        }
        
        
    }
    
    public void saveConfiguration() {
        //System.out.println("saving config");
        int pos = tabbedPane.getSelectedIndex();
        //System.out.println("active tab: " + pos);
        
        
        
        // add a new local DAS source ...
        if ( pos == 1 ) {       
            
            //System.out.println("adding new local DAS source");
            logger.finest(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.127")); //$NON-NLS-1$
            HashMap formdata = new HashMap();
            int formPos = -1 ;
            for ( int i = 0 ; i < DASSOURCE_FIELDS.length; i++) {
                String col = DASSOURCE_FIELDS[i];
                if ( col.equals(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Public")) || col.equals(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Active")))  //$NON-NLS-1$ //$NON-NLS-2$
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
            sds.setUrl(              (String) formdata.get(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Url"))); //$NON-NLS-1$
            sds.setAdminemail(       (String) formdata.get(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Adminemail"))); //$NON-NLS-1$
            sds.setDescription(      (String) formdata.get(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Description"))); //$NON-NLS-1$
            
            
            String [] coordSys = (String[]) formdata.get(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.CoordinateSystems")); //$NON-NLS-1$
            DasCoordinateSystem[] dcss = new DasCoordinateSystem[coordSys.length];
            for ( int i = 0 ; i< coordSys.length;i++) {
                DasCoordinateSystem dcs = DasCoordinateSystem.fromString(coordSys[i]);
                dcss[i] = dcs;
            }
            sds.setCoordinateSystem(dcss);
            
            String[] capabs =  (String[]) formdata.get(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.134")) ;         //$NON-NLS-1$
            //String[] split = capabs.split(" ");
            sds.setCapabilities(capabs);
            
            
            config.addServer(sds);
            
            updateDasSourceTable() ;
            
            //registryIO.saveConfiguration();
        }
        // get PDB files from locally
        else if ( pos == 2 ) {
            String e = fileExtensions.getText() ;
            String[] exts = e.split(" "); //$NON-NLS-1$
            config.setPDBFileExtensions(exts);
            // add a new "pseudo" DAS source
            
            SpiceDasSource sds = new SpiceDasSource();
            sds.setRegistered(false);
            sds.setUrl("file://"+pdbDirectory.getText()); //$NON-NLS-1$
            sds.setAdminemail(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.137")); //$NON-NLS-1$
            sds.setDescription(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.138")); //$NON-NLS-1$
            //String[] coordSys = new String[] { "PDBresnum", };
            String[] capabs   = new String[] { "structure", }; //$NON-NLS-1$
            
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
            logger.finest("setting update behaviour to " + behave); //$NON-NLS-1$
            if ( behave.equals(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.141"))) //$NON-NLS-1$
                behave = ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.142") ; //$NON-NLS-1$
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
        List sources = config.getAllServers();
        SpiceDasSource[] sourceArray = (SpiceDasSource[]) sources.toArray(new SpiceDasSource[sources.size()]);
        
        dasSourceTableModel = new MyTableModel(this,seqdata,colNames,sourceArray);
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
    SpiceDasSource[] sources;
    
    public MyTableModel(ConfigPanel parent_,Object[][]seqdata, 
            String[] columnNames_, SpiceDasSource[] sources){
        super();
        parent = parent_ ;
        columnNames = columnNames_;
        this.sources = sources; 
        setData(seqdata);
        
    }
    
    public SpiceDasSource getServerAt(int rowPosition){
        return sources[rowPosition];
    }
    
    public SpiceDasSource[] getServers() {
        return sources;
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
        
         System.out.println("Setting value at " + row + "," + col //$NON-NLS-1$ //$NON-NLS-2$
         + " to " + value //$NON-NLS-1$
         + " (an instance of " //$NON-NLS-1$
         + value.getClass() + ")"); //$NON-NLS-1$
         
         
        data[row][col] = value;
        
        if ( col == 0) {
            
            //String url = (String)model.getValueAt(row,0);
            // Do something with the data...
            //Boolean status = (Boolean) model.getValueAt(row, column);
            SpiceDasSource ds = sources[row];
            String url = ds.getUrl();
            System.out.println("setting server status " + value); //$NON-NLS-1$
            parent.setServerStatus(url,(Boolean)value) ;
        }
        
        fireTableCellUpdated(row, col);
    }
    
    public void tableChanged(TableModelEvent e) {
        System.out.println("tableChanged " + e.getColumn()); //$NON-NLS-1$
        int row = e.getFirstRow();
        int column = e.getColumn();
        MyTableModel model = (MyTableModel)e.getSource();
        //String columnName = model.getColumnName(column);
        //Object cell = model.getValueAt(row, column);
        
        if ( column == 0) {
            
            //String url = (String)model.getValueAt(row,2);
            SpiceDasSource ds = sources[row];
            String url = ds.getUrl();
            // Do something with the data...
            Boolean status = (Boolean) model.getValueAt(row, column);
            parent.setServerStatus(url,status) ;
        }
    }
}



class PopupListener extends MouseAdapter {
    JPopupMenu popup;
    JTable table    ;
   
    PopupListener(JPopupMenu popupMenu,JTable tab) {
        popup  = popupMenu;
        table  = tab     ;
        
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
            System.out.println("seleceted pos " + pos);
            MyTableModel model = (MyTableModel)table.getModel();
            SpiceDasSource ds = model.getServerAt(pos);
            
            // get the menu items
            MenuElement[] m =   popup.getSubElements() ;
            JMenuItem m0 = (JMenuItem)m[0].getComponent();
            JMenuItem m1 = (JMenuItem)m[1].getComponent();
            
            // adapt the display of the MenuItems
            if ( ds.getStatus()) 
                m0.setText(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Inactivate")) ; //$NON-NLS-1$
            else 
                m0.setText(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Activate")); //$NON-NLS-1$
            
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
  
    RegistryConfiguration config;
    ConfigPanel parent;
    static Logger    logger      = Logger.getLogger("org.biojava.spice"); //$NON-NLS-1$
    
    public ConfigActionListener( RegistryConfiguration config_, ConfigPanel tpd){        
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
    ConfigPanel parent        ;
  
    
    static Logger    logger      = Logger.getLogger("org.biojava.spice"); //$NON-NLS-1$
    
    public MenuListener(  
            JTable tab,
            ConfigPanel tabd
            ){
        table  = tab  ;
       
        parent = tabd  ;
       
    }
    
    public void actionPerformed(ActionEvent e){
        JMenuItem source = (JMenuItem)(e.getSource());
        
        
        String cmd =  source.getText();
        int    pos = table.getSelectedRow();
        if ( pos < 0) 
            return ;
        logger.info("selected in row "+pos+" cmd "+cmd);
        MyTableModel model = parent.getTableModel();
        SpiceDasSource ds = model.getServerAt(pos);
        String[] colNames = ConfigPanel.colNames;
        
        if (cmd.equals(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Activate"))) { //$NON-NLS-1$
            ds.setStatus(true);
            table.setValueAt(new Boolean(true),pos,colNames.length-1);
        }
        else if ( cmd.equals(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Inactivate"))) { //$NON-NLS-1$
            ds.setStatus(false);
            table.setValueAt(new Boolean(false),pos,colNames.length-1);
        }
        else if ( cmd.equals(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Delete"))) {  //$NON-NLS-1$
            logger.finest("deleteting das source ..." +pos); //$NON-NLS-1$
                     
            parent.deleteServer(ds);
        }
        
        
        parent.updateDasSourceTable();
        
    }
}

class SearchAction extends AbstractAction implements KeyListener{
    
    public static final long serialVersionUID = 0l;
    ConfigPanel parent;
    JTextField searchBox;
    JTable table;
    public SearchAction(String title, ConfigPanel parent, JTextField box, JTable table){
        super(title);
        this.parent = parent;
        searchBox = box;
        this.table = table;
        
    }
    
    /** This method is called when the button or Enter is pressed
     * 
     */
    public void actionPerformed(ActionEvent evt) {
        
        searchText();
    }
    
    private void searchText() {
        
        
        String searchText = searchBox.getText();
       
        List servers = parent.getConfiguration().getAllServers();
        SpiceDasSource[] sources = (SpiceDasSource[]) servers.toArray(new SpiceDasSource[servers.size()]);
        SpiceDasSource[] selectedSources = SearchDasSourceManager.searchForKeyword(sources,searchText);
        Object seqdata[][] = parent.getTabData(selectedSources);
        
             
        MyTableModel dasSourceTableModel = new MyTableModel(parent,seqdata,ConfigPanel.colNames, selectedSources);
        table.setModel(dasSourceTableModel);
        table.repaint();
    }

    public void keyTyped(KeyEvent arg0) { }

    public void keyPressed(KeyEvent arg0) {}

    public void keyReleased(KeyEvent arg0) {
        searchText();
       }
    
}

