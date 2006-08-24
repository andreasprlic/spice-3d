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
 * Created on Aug 5, 2006
 *
 */
package org.biojava.spice.config;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.MenuElement;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;
import org.biojava.spice.ResourceManager;
import org.biojava.spice.gui.ConfigPanel;
import org.biojava.spice.config.DasSourcePanelTableModel;
import org.biojava.spice.utils.BrowserOpener;

public class DasSourceConfigPanel {

    static Logger    logger      = Logger.getLogger("org.biojava.spice"); //$NON-NLS-1$
    
    //ConfigPanel parent;
    JTextField              searchBox;
    JEditorPane             sourceDescription;
    RegistryConfiguration   config;
    DasSourcePanelTableModel dasSourceTableModel;
    JTable                  dasSourceTable;
    JPopupMenu              tablePopup;
    JScrollPane             descriptionScrollPane;
    int selectMoveStartPosition;
    
    public static String[] colNames= new String [] {ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Active"),ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Nickname"),ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Public")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    
    public DasSourceConfigPanel(ConfigPanel parent, RegistryConfiguration config) {
        super();
        //this.parent = parent;
        this.config = config;
        
        selectMoveStartPosition = -1;
    }

    public void showAllSources(){
        updateDasSourceTable();
        
    }
    
    public void selectAllSources(){
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
    
    public DasSourcePanelTableModel getTableModel(){
        return  (DasSourcePanelTableModel)dasSourceTable.getModel();
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
    
    
    
    protected RegistryConfiguration getConfiguration(){
        return config;
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
    
    public  JPanel getPanel(){
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
        List sources = new ArrayList();
        if ( config != null )
            sources = config.getAllServers();
        
        SpiceDasSource[] sourceArray = (SpiceDasSource[]) sources.toArray(new SpiceDasSource[sources.size()]);
        dasSourceTableModel = new DasSourcePanelTableModel(this,seqdata,colNames,sourceArray);
        
        dasSourceTable  = new JTable(dasSourceTableModel);
        
        
        SearchAction searchAction = new SearchAction(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Search"),
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
                
                DasSourcePanelTableModel model = (DasSourcePanelTableModel) dasSourceTable.getModel();
                SpiceDasSource[] servers = model.getServers();
                //List servers = config.getAllServers();
                
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
         
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                        SpiceDasSource ds = (SpiceDasSource) servers[i];  
                        sourceDescription.setText(convertDasSource2HTML(ds));
                        
                        //descriptionScrollPane.getViewport().setViewPosition(new Point(0,0));
                        //descriptionScrollPane.getVerticalScrollBar().setValue(0);
                        javax.swing.SwingUtilities.invokeLater(new Runnable()
                                {
                                  public void run()
                                  {
                                    descriptionScrollPane.getVerticalScrollBar().setValue(0);
                                  }
                                });
                        //sourceDescription.repaint();
                        //descriptionScrollPane.repaint();
                       
                        
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
        return seqstrucpanel;
    }
    
    private  void addHyperLinkListener(){
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
    

    public void updateDasSourceTable(){
        Object seqdata[][] = getTabData();
        List sources = config.getAllServers();
        SpiceDasSource[] sourceArray = (SpiceDasSource[]) sources.toArray(new SpiceDasSource[sources.size()]);
        
        dasSourceTableModel = new DasSourcePanelTableModel(this,seqdata,colNames,sourceArray);
        dasSourceTable.setModel(dasSourceTableModel);
        dasSourceTable.repaint();
       
    }

    
    
    public Object[][] getTabData() {
        List servers = new ArrayList();
        if ( config != null )
            servers = config.getAllServers();
        SpiceDasSource[] sources = (SpiceDasSource[]) servers.toArray(new SpiceDasSource[servers.size()]);
        return getTabData(sources);
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
    
    private static String convertDasSource2HTML(SpiceDasSource ds){
        
        
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
    
}




class MenuListener
implements ActionListener
{
    JTable table                 ;  
    DasSourceConfigPanel parent        ;
  
    
    static Logger    logger      = Logger.getLogger("org.biojava.spice"); //$NON-NLS-1$
    
    public MenuListener(  
            JTable tab,
            DasSourceConfigPanel tabd
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
        DasSourcePanelTableModel model = parent.getTableModel();
        SpiceDasSource ds = model.getServerAt(pos);
        String[] colNames = DasSourceConfigPanel.colNames;
        
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
    DasSourceConfigPanel parent;
    JTextField searchBox;
    JTable table;
    public SearchAction(String title, DasSourceConfigPanel parent, JTextField box, JTable table){
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
        
             
        DasSourcePanelTableModel dasSourceTableModel = new DasSourcePanelTableModel(parent,seqdata,DasSourceConfigPanel.colNames, selectedSources);
        table.setModel(dasSourceTableModel);
        table.repaint();
    }

    public void keyTyped(KeyEvent arg0) { }

    public void keyPressed(KeyEvent arg0) {}

    public void keyReleased(KeyEvent arg0) {
        searchText();
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
            DasSourcePanelTableModel model = (DasSourcePanelTableModel)table.getModel();
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

