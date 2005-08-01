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

import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import org.biojava.spice.*;


/** a dialog that lists the results of a keword search
 *  and allows to open matching PDB files.
 * @author Andreas Prlic
 *
 */
public class MSDWindow extends JDialog{
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    SPICEFrame spice;
    JTable dataTable;
    static int H_SIZE = 600;
    static int V_SIZE = 200 ;
    /**
     * 
     */
    public MSDWindow(SPICEFrame parent,String keyword) {
        super();
        this.spice = parent;
        
        
        logger.info("searching MSD search web service for keyword " + keyword);
        MSDKeywordSearch msd = new MSDKeywordSearch();
        Deposition[] depos = msd.search(keyword);
        //System.out.println("got " + depos.length + " results.");
        int length = depos.length;
        JLabel title = new JLabel(length + " results for keyword " + keyword);
        Box vBox  = Box.createVerticalBox();
        vBox.add(title);
        
        String[] columnNames = {"code","method","resolution","classification"};
        Object[][] data =  getData(depos);
        dataTable = new JTable(data,columnNames) ;
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
        this.getContentPane().add(vBox);
        this.setSize(H_SIZE, V_SIZE);
        this.show();
        
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
}
