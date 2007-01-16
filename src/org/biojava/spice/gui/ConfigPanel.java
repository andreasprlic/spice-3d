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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;
import org.biojava.spice.ResourceManager;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.config.ConfigurationListener;
import org.biojava.spice.config.DasSourceConfigPanel;
import org.biojava.spice.config.RegistryConfigIO;
import org.biojava.spice.config.RegistryConfiguration;
import org.biojava.spice.config.SpiceDefaults;


/** a Component that provides the GUI for configuring SPICE
 * 
 * @author Andreas Prlic
 * @since 3:31:04 PM
 * @version %I% %G%
 */
public class ConfigPanel extends JPanel implements ConfigurationListener{
    
    private static final long serialVersionUID = 8273923744127087421L;
    static Logger    logger      =  Logger.getLogger(SpiceDefaults.LOGGER); //$NON-NLS-1$
   
    static String[] DASSOURCE_FIELDS = new String[] { 
        ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Url"), //$NON-NLS-1$
        ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Adminemail"),  //$NON-NLS-1$
        ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Capabilities"), //$NON-NLS-1$
        ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.CoordinateSystems"), //$NON-NLS-1$
        ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Description"), //$NON-NLS-1$
        ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Public"), //$NON-NLS-1$
        ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Active") //$NON-NLS-1$
    };
    
   
    RegistryConfiguration   config;
    JTabbedPane             tabbedPane;
    List                    entryFormFields;
    JTextField              pdbDirectory;
    JFileChooser            chooser = new JFileChooser(); 
    JTextField              fileExtensions;      
    JComboBox               updateBehaveList;    
    SpiceApplication        spice                   ;
    DasSourceConfigPanel    dasSourceConfigPanel;
    
    public ConfigPanel(SpiceApplication spice,RegistryConfiguration conf) {
    
        super();
        this.spice = spice;
        this.config = conf;
        
        chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(".")); //$NON-NLS-1$
        chooser.setDialogTitle(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.12")); //$NON-NLS-1$
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        
        tabbedPane = new JTabbedPane();
        ImageIcon icon = SpiceApplication.createImageIcon(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.13")); //$NON-NLS-1$
        
   
        
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
        dasSourceConfigPanel.updateDasSourceTable();
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
        
        String[] freq = { ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Always"),ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Once")}; //$NON-NLS-1$ //$NON-NLS-2$
        updateBehaveList = new JComboBox(freq) ;        
        updateBehaveList.setEditable(false);
        updateBehaveList.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        
        String selectedFreq = ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Always");
        if ( config != null )
            selectedFreq = config.getUpdateBehave();
        
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
        String url = SpiceDefaults.REGISTRY;
        if ( config != null )
            url = config.getRegistryUrl().toString();
        JTextField registry = new JTextField(url);
        v.add(registry);
        
        
        //   configure window behaviour.
        
        JTextField jmoldesc  = new JTextField(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.30")); //$NON-NLS-1$
        jmoldesc.setEditable(false);
        jmoldesc.setBorder(BorderFactory.createEmptyBorder());
        v.add(jmoldesc);
        
        String[] windowPosition = {ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.31"),
                ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.32"),
                ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.33"),
                ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.34")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        
        JComboBox windowlayout = new JComboBox(windowPosition);
        v.add(windowlayout);
        
        // the logging level
        JTextField logLevelDesc = new JTextField(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.LogLevelFieldDesc"));
        logLevelDesc.setEditable(false);
        logLevelDesc.setBorder(BorderFactory.createEmptyBorder());
        v.add(logLevelDesc);
        
        String[] logLevels = {
                ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.LogLevel.Severe"),
                ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.LogLevel.Warning"),
                ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.LogLevel.Info"),
                ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.LogLevel.Config"),
                ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.LogLevel.Fine"),
                ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.LogLevel.Finer"),
                ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.LogLevel.Finest")
        };
        
        JComboBox logLevelsBox = new JComboBox(logLevels);
        v.add(logLevelsBox);
        
        // get current loglevel
        String currentLogLevel = logger.getLevel().getName(); 
            
        int currentLogPos = 0;
        for( int i =0 ; i < logLevels.length ;i++){
            String testLevel = logLevels[i];
            if ( currentLogLevel.equalsIgnoreCase(testLevel)) {
                currentLogPos = i;
                break;
            }
        }
        logLevelsBox.setSelectedIndex(currentLogPos);
        logLevelsBox.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                
                String cmd = (String) cb.getSelectedItem();
               
                if ( cmd.equalsIgnoreCase(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.LogLevel.Severe")))
                    setLevel(Level.SEVERE);
                    
                else if ( cmd.equalsIgnoreCase(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.LogLevel.Warning")))
                    setLevel(Level.WARNING);                    
                
                else if( cmd.equalsIgnoreCase(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.LogLevel.Info")))
                    setLevel(Level.INFO);                    
                    
                else if ( cmd.equalsIgnoreCase( ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.LogLevel.Config")))
                    setLevel(Level.CONFIG);                    
                
                else if ( cmd.equalsIgnoreCase( ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.LogLevel.Fine")))
                    setLevel(Level.FINE);                    
                
                else if ( cmd.equalsIgnoreCase( ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.LogLevel.Finer")))
                    setLevel(Level.FINER);                    
                    
                else if ( cmd.equalsIgnoreCase( ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.LogLevel.Finest")))
                    setLevel(Level.FINEST);                    
                
            }
            
        });
        
        generalConfigForm.add(v);
        //generalConfigForm.add(contactRegistryNow);
        
        return generalConfigForm;
        
        
    }
    
    private void setLevel(Level level){
        logger.info("setting log level to " + level);
        logger.setLevel(level);   
        LoggingPanelManager.setLogLevel(level);
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
                String[] coords = { SpiceDefaults.UNIPROTCOORDSYS, 
                		SpiceDefaults.PDBCOORDSYS,
                		SpiceDefaults.ENSPCOORDSYS};
                JComboBox list = new JComboBox(coords) ;        
                list.setEditable(false);
                list.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
                list.setSelectedIndex(0);
                vBoxRight.add(list);    
                entryFormFields.add(list);
                
            } else if ( col.equals(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Capabilities"))) { //$NON-NLS-1$
                JList list;
                if ( config != null )
                    list = new JList(config.getCapabilities());
                else 
                    list = new JList();
                
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
    
   
    
    protected JPanel getAvailablePanel() {
        dasSourceConfigPanel = new DasSourceConfigPanel(this,config);
        return dasSourceConfigPanel.getPanel() ;
    }
    
   
    
    protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }
    
    
    public void saveConfiguration() {
        //System.out.println("saving config");
        int pos = tabbedPane.getSelectedIndex();
        //System.out.println("active tab: " + pos);
        
        if ( config == null)
            config = new RegistryConfiguration();
        
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
            
            dasSourceConfigPanel.updateDasSourceTable() ;
            
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
            
            DasCoordinateSystem dcs = DasCoordinateSystem.fromString(SpiceDefaults.PDBCOORDSYS);
            DasCoordinateSystem[] dcss = new DasCoordinateSystem[1];
            dcss[0] = dcs;
            sds.setCoordinateSystem(dcss);
            sds.setCapabilities(capabs);
            config.addServer(sds);
            dasSourceConfigPanel.updateDasSourceTable();
        }
        else if ( pos == 3 ) {
            String behave = (String)updateBehaveList.getSelectedItem();
            
            //System.out.println("setting update behaviour to " + behave);
            logger.finest("setting update behaviour to " + behave); //$NON-NLS-1$
            if ( behave.equals(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.141"))) //$NON-NLS-1$
                behave = ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.142") ; //$NON-NLS-1$
            config.setUpdateBehave(behave);
            dasSourceConfigPanel.updateDasSourceTable();
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




