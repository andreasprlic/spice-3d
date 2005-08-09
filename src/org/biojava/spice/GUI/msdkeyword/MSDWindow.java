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
 * Created on Aug 1, 2005
 *
 */
package org.biojava.spice.GUI.msdkeyword;

import java.awt.Component;
//import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import org.biojava.spice.*;



/** a dialog that lists the results of a keword search
 *  and allows to load matching PDBs in spice.
 * @author Andreas Prlic
 *
 */
public class MSDWindow {
    
    /**
     * 
     */
    public MSDWindow(SPICEFrame parent,String keyword) {
        super();
        //this.spice = parent;
        
        MSDPanel msdp = new MSDPanel(parent);
        msdp.search(keyword); 
        //this.getContentPane().add(msdp);
        //.setSize(H_SIZE, V_SIZE);
        msdp.show(null);
        
        
       
    }
    
}


class MSDPanel extends JPanel{
    static final String[] columnNames = {"code","method","resolution","classification","title"};
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    SPICEFrame spice;
    JTable dataTable;
    JTextField kwsearch;
    JLabel title;
    
    static int H_SIZE = 750;
    static int V_SIZE = 300 ;
    MyTableModel model;
    
    public MSDPanel(SPICEFrame parent){
        super();
        
        spice = parent;
        //logger.info("searching MSD search web service for keyword " + keyword);
        String keyword = "";
        title = new JLabel("searching " + keyword);
        Box vBox  = Box.createVerticalBox();
        vBox.add(title);
        
        kwsearch = new JTextField(10);
        kwsearch.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                
                String kw  = kwsearch.getText();
                
                //System.out.println("search kw " + kw);
                search(kw);
                
            }
            
        });
        JButton openKw = new JButton("Search");
        openKw.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                
                String kw  = kwsearch.getText();
                
                //System.out.println("search kw " + kw);
                search(kw);
            }
        });
        
        JLabel kwl = new JLabel("keyword:");
        Box hBox2 = Box.createHorizontalBox();
        hBox2.add(kwl);
        hBox2.add(kwsearch);
        hBox2.add(openKw);
        
        //Object[][] data = new Object[0][0];
        Deposition[] depos = new Deposition[0];
        Object[][] data =  getData(depos);
        
        //dataTable = new JTable(data,columnNames) ;
        model = new MyTableModel(data, columnNames);
        dataTable = new JTable(model);
        
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        //dataTable.setModel(new MyTableModel(data, columnNames));
        
        setColumnWidth();
        
        /*dataTable.getColumnModel().getColumn(0).setPreferredWidth(30);
         dataTable.getColumnModel().getColumn(1).setPreferredWidth(30);
         dataTable.getColumnModel().getColumn(2).setPreferredWidth(30);
         dataTable.getColumnModel().getColumn(3).setPreferredWidth(30);
         dataTable.getColumnModel().getColumn(4).setPreferredWidth(480);
         */
        
        JScrollPane sc = new JScrollPane(dataTable);
        
        vBox.add(sc);
        
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel rowSM = dataTable.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e) {
                if ( e.getValueIsAdjusting()) return;
                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if ( lsm.isSelectionEmpty()){
                    // nothing selected
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    String pdbcode = (String) dataTable.getValueAt(selectedRow,0);
                    spice.load("PDB",pdbcode);
                }
            }
        });
        
   	   hBox2.add(Box.createGlue());
        vBox.add(hBox2);
        
        add(vBox);
        //this.setPreferredSize(new Dimension(H_SIZE, V_SIZE));
        //this.setSize(new Dimension(H_SIZE, V_SIZE));
        //this.show();
        //search(keyword);
        
    }
    private void setColumnWidth(){

        int width = 300;
        final TableColumnModel columns = dataTable.getColumnModel();
        for (int i=model.getColumnCount(); --i>=0;) {
            columns.getColumn(i).setPreferredWidth(width);
            //columns.getColumn(i).setWidth(width);
            width = 80;
        }
        //doLayout();
    }
    public void search(String keyword ){
        MSDKeywordSearch msd = new MSDKeywordSearch();
        Deposition[] depos = msd.search(keyword);
        //System.out.println("got " + depos.length + " results.");
        int length = depos.length;
        title.setText(length + " results for keyword " + keyword); 
        title.repaint();
        Object[][] data =  getData(depos);
        model = new MyTableModel(data, columnNames);
        dataTable.setModel(model);
        dataTable.repaint();
        //this.revalidate();
        setColumnWidth();
        doLayout();
        this.repaint();
    }
    
    private Object[][] getData(Deposition[] depos){
        Object[][] data = new Object[depos.length][5];
        
        for ( int i = 0 ; i < depos.length; i++){
            Deposition d = depos[i];
            data[i][0] = d.getAccessionCode();
            data[i][1] = d.getExpData();
            data[i][2] = new Float(d.getResolution());
            data[i][3] = d.getClassification();
            data[i][4] = d.getTitle();
        }
        return data;
        
    }
    
    
    /**
     * Layout this component. This method give all the remaining space, if any,
     * to the last table's column. This column is usually the one with logging
     * messages.
     */
    public void doLayout() {
        //logger.info("do Layout!");
        final TableColumnModel model = dataTable.getColumnModel();
        final int      messageColumn = model.getColumnCount()-1;
        Component parent = dataTable.getParent();
        int delta = parent.getWidth();
        if ((parent=parent.getParent()) instanceof JScrollPane) {
            delta -= ((JScrollPane) parent).getVerticalScrollBar().getPreferredSize().width;
        }
        for (int i=0; i<messageColumn; i++) {
            delta -= model.getColumn(i).getWidth();
        }
        //logger.info("setting column " + messageColumn + " width " + delta);
        final TableColumn column = model.getColumn(messageColumn);
        if (delta > Math.max(column.getWidth(), column.getPreferredWidth())) {
            column.setPreferredWidth(delta);
        }
        super.doLayout();
    }
    
    public Component show(final Component owner) { 
        
        int frameWidth  = H_SIZE ;
        int frameHeight = V_SIZE ;
        
       
        
        // Get the size of the default screen
        //	java.awt.Dimension dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        
        //System.out.println("LoggingPanel show!!!!!!!");
        JFrame frame = new JFrame();
        //frame.setLocation((dim.width - frameWidth),(dim.height - frameHeight));
        frame.setLocation(0,0);
        frame.setTitle("MSD - keyword search");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
                {
            public void windowClosed(WindowEvent event) {
                //dispose();
            }
                });
        frame.getContentPane().add(this);
        
        
        frame.pack();
        
        doLayout();
        //frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);
       
        frame.show();
        return frame;
    }
    
    
    class MyTableModel extends AbstractTableModel{
        Object[][] data;
        String[] columnNames;
        public MyTableModel(Object[][] data, String[] columnNames){
            this.data = data;
            this.columnNames = columnNames;
        }
        
        
        
        public String getColumnName(int col) {
            return columnNames[col].toString();
        }
        public int getRowCount() { return data.length; }
        public int getColumnCount() { return columnNames.length; }
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }
        public boolean isCellEditable(int row, int col)
        { return false; }
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
        
    }
    
}



