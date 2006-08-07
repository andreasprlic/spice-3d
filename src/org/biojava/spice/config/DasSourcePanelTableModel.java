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

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.biojava.dasobert.das.SpiceDasSource;


public class DasSourcePanelTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 8273923744127087420L;
    DasSourceConfigPanel parent ;
    
    private Object[][] data ;
    private String[]   columnNames  ;
    SpiceDasSource[] sources;
    
    public DasSourcePanelTableModel(DasSourceConfigPanel parent_,Object[][]seqdata, 
            String[] columnNames_, SpiceDasSource[] sources) {
        
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
        DasSourcePanelTableModel model = (DasSourcePanelTableModel)e.getSource();
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


