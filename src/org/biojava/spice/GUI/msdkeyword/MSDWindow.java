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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
public class MSDWindow extends JDialog{
    
    static final String[] columnNames = {"code","method","resolution","classification"};
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    SPICEFrame spice;
    JTable dataTable;
    JTextField kwsearch;
    JLabel title;
    static int H_SIZE = 600;
    static int V_SIZE = 200 ;
    /**
     * 
     */
    public MSDWindow(SPICEFrame parent,String keyword) {
        super();
        this.spice = parent;
        
        
        logger.info("searching MSD search web service for keyword " + keyword);
        
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
        
         
        Object[][] data = new Object[0][0];
        
        //dataTable = new JTable(data,columnNames) ;
        dataTable = new JTable(new MyTableModel(data, columnNames));
        
        TableColumn col0 = dataTable.getColumnModel().getColumn(0);
        col0.setPreferredWidth(30);
        TableColumn col1 = dataTable.getColumnModel().getColumn(2);
        col1.setPreferredWidth(30);
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
        
        vBox.add(hBox2);
        this.getContentPane().add(vBox);
        this.setSize(H_SIZE, V_SIZE);
        this.show();
        search(keyword);
        
    }

        private void search(String keyword ){
            MSDKeywordSearch msd = new MSDKeywordSearch();
            Deposition[] depos = msd.search(keyword);
            //System.out.println("got " + depos.length + " results.");
            int length = depos.length;
            title.setText(length + " results for keyword " + keyword); 
            
            Object[][] data =  getData(depos);
            dataTable.setModel(new MyTableModel(data, columnNames));
            dataTable.repaint();
            
        }
        
    private Object[][] getData(Deposition[] depos){
        Object[][] data = new Object[depos.length][4];
        
        for ( int i = 0 ; i < depos.length; i++){
            Deposition d = depos[i];
            data[i][0] = d.getAccessionCode();
            data[i][1] = d.getExpData();
            data[i][2] = new Float(d.getResolution());
            data[i][3] = d.getClassification();
        }
        return data;
        
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



