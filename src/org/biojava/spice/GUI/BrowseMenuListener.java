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
 * Created on Feb 7, 2005
 *
 */
package org.biojava.spice.GUI;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.biojava.spice.SPICEFrame;

import javax.swing.JMenuItem;

import java.net.URL;

/**
 * @author Andreas Prlic
 *
 */
public class BrowseMenuListener implements ActionListener {

    
    // see also  Panel.StatusPanel
    public static String PDBLINK = "http://www.rcsb.org/pdb/cgi/explore.cgi?pdbId=";
    
    public static String UNIPROTLINK = "http://www.ebi.uniprot.org/uniprot-srv/uniProtView.do?proteinAc=" ;
    
    // link to DASTY das client
    
    public static String DASTYLINK = "http://www.ebi.ac.uk/das-srv/uniprot/dasty/index.jsp?ID=";
    
    SPICEFrame spice;
    URL url;
    public BrowseMenuListener(SPICEFrame parent){
        url= null;
        spice = parent;
    }
    public BrowseMenuListener(SPICEFrame parent,URL link){
        url=link;
        spice = parent;
    }

    public void actionPerformed(ActionEvent e){
      JMenuItem source = (JMenuItem)e.getSource();
      String txt = source.getText();
      if (txt.equals("PDB")){
          // open PDB file
          String u = PDBLINK+spice.getPDBCode();
          //System.out.println("opening url "+u );
          spice.showDocument(u);
      } else if ( txt.equals("UniProt")){
          String u = UNIPROTLINK+spice.getUniProtCode();
          //System.out.println("opening url "+u );
          spice.showDocument(u);
      } else if ( txt.equals("Dasty")){
        
          String d = DASTYLINK+spice.getUniProtCode();
          spice.showDocument(d);
          
      } else {
          //String url = txt.substring(16,txt.length());
          spice.showDocument(url);
      }
      
    }
}

