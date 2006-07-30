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
package org.biojava.spice.gui;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.biojava.dasobert.eventmodel.SequenceEvent;
import org.biojava.dasobert.eventmodel.SequenceListener;
import org.biojava.dasobert.eventmodel.StructureEvent;
import org.biojava.dasobert.eventmodel.StructureListener;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.utils.JNLPProxy;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Andreas Prlic
 *
 */
public class BrowseMenuListener 
implements ActionListener
{

   // see also  Panel.StatusPanel
    public static String PDBLINK = "http://www.rcsb.org/pdb/cgi/explore.cgi?pdbId=";
    
    public static String UNIPROTLINK = "http://www.ebi.uniprot.org/uniprot-srv/uniProtView.do?proteinAc=" ;
    
    // link to DASTY das client
    public static String DASTYLINK = "http://www.ebi.ac.uk/das-srv/uniprot/dasty/index.jsp?id=";
    
    // link to Proview das client
    public static String PROVIEWLINK = "http://www.efamily.org.uk/perl/proview/proview?id=";
    
    public static String ENSEMBLLINK = "http://www.ensembl.org/Homo_sapiens/protview?peptide=";
    
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    ImageIcon firefoxIcon ;
    
    JMenuItem pdbMenu;
    JMenuItem upMenu;
    JMenuItem dastyMenu;
    JMenuItem proviewMenu;
    JMenuItem enspMenu;
    
    String pdbCode;
    String upCode;
    String enspCode;
    URL url;
    public BrowseMenuListener(){
        url= null;
        pdbCode = "";
        upCode = "";
        enspCode = "";
        firefoxIcon = SpiceApplication.createImageIcon("firefox.png");
        
    }
    public BrowseMenuListener(URL link){
        this();
        url=link;
   
    }

    public JMenu getBrowsermenu(){

        JMenu browseMenu = new JMenu("Browse");
        browseMenu.setMnemonic(KeyEvent.VK_B);
        browseMenu.getAccessibleContext().setAccessibleDescription("open links in browser");
        
        if (firefoxIcon == null )
            pdbMenu = new JMenuItem("PDB");
        else
            pdbMenu = new JMenuItem("PDB",firefoxIcon);
        pdbMenu.setMnemonic(KeyEvent.VK_P);
        pdbMenu.setEnabled(false);
        pdbMenu.addActionListener(this);
        browseMenu.add(pdbMenu);
        if ( firefoxIcon == null )
            upMenu = new JMenuItem("UniProt");
        else 
            upMenu = new JMenuItem("UniProt",firefoxIcon);
        upMenu.setMnemonic(KeyEvent.VK_U);
        upMenu.setEnabled(false);
        upMenu.addActionListener(this);
        browseMenu.add(upMenu);
        
        if ( firefoxIcon == null )
            enspMenu = new JMenuItem("Ensembl");
        else 
            enspMenu = new JMenuItem("Ensembl",firefoxIcon);
        
        enspMenu.setMnemonic(KeyEvent.VK_E);
        enspMenu.setEnabled(false);
        enspMenu.addActionListener(this);
        browseMenu.add(enspMenu);
        
        JMenu dasclientsMenu = new JMenu("Other DAS clients");
        dasclientsMenu.setMnemonic(KeyEvent.VK_O);
        browseMenu.add(dasclientsMenu);
        
        dastyMenu = new JMenuItem("Dasty");
        dastyMenu.setMnemonic(KeyEvent.VK_D);
        dastyMenu.addActionListener(this);
        dasclientsMenu.add(dastyMenu);
        dastyMenu.setEnabled(false);
        
        proviewMenu = new JMenuItem("Proview");
        proviewMenu.setMnemonic(KeyEvent.VK_P);
        proviewMenu.addActionListener(this);
        dasclientsMenu.add(proviewMenu);
        proviewMenu.setEnabled(false);
        
        return browseMenu;
    }
    
    
    public void actionPerformed(ActionEvent e){
      JMenuItem source = (JMenuItem)e.getSource();
      String txt = source.getText();
      if (txt.equals("PDB")){
          // open PDB file
          String u = PDBLINK+pdbCode;
          //System.out.println("opening url "+u );
          showDocument(u);
      } else if ( txt.equals("UniProt")){
          String u = UNIPROTLINK+upCode;
          //System.out.println("opening url "+u );
          showDocument(u);
      } else if ( txt.equals("Dasty")){
        
          String d = DASTYLINK+upCode;
          showDocument(d);
          
      } else if ( txt.equals("Proview")){
          String u = PROVIEWLINK+upCode;
          showDocument(u);
      } else if ( txt.equals("Ensembl")){
          String t = ENSEMBLLINK + enspCode;
          showDocument(t);
      }
      else {
          //String url = txt.substring(16,txt.length());
          showDocument(url);
      }   
    }
   
    
    public void clear(){
        upCode ="";
        upMenu.setEnabled(false);
        pdbCode="";
        pdbMenu.setEnabled(false);
        enspCode="";
        enspMenu.setEnabled(false);
        proviewMenu.setEnabled(false);
        dastyMenu.setEnabled(false);
    }
    
    public SequenceListener getUniProtListener(){
        return new SequenceListener(){
            public void newSequence(SequenceEvent e) {
                upCode=e.getAccessionCode();
                upMenu.setEnabled(true);
                dastyMenu.setEnabled(true);
                proviewMenu.setEnabled(true);
            }
            public void newObjectRequested(String accessionCode) {
                upCode = "";
                upMenu.setEnabled(false);
                dastyMenu.setEnabled(false);
                proviewMenu.setEnabled(false);
            }
            public void noObjectFound(String accessionCode){
                upCode = "";
                upMenu.setEnabled(false);
                dastyMenu.setEnabled(false);
                proviewMenu.setEnabled(false);
            }
            public void clearSelection() {}
            public void selectedSeqPosition(int position) { }
            public void selectedSeqRange(int start, int end) {}
            public void selectionLocked(boolean flag) {}
            
        };
    }
    
    public SequenceListener getEnspListener(){
        return new SequenceListener(){
            public void newSequence(SequenceEvent e) {
                enspCode=e.getAccessionCode();
                enspMenu.setEnabled(true);
            }
            public void newObjectRequested(String accessionCode) {
                enspCode = "";
                enspMenu.setEnabled(false);
            }
            public void noObjectFound(String accessionCode){
                enspCode="";
                enspMenu.setEnabled(false);
            }
            public void clearSelection() {}
            public void selectedSeqPosition(int position) {}
            public void selectedSeqRange(int start, int end) {}
            public void selectionLocked(boolean flag) { }
            
        };
    }
    public StructureListener getPDBListener(){
        return new StructureListener() {
            public void newStructure(StructureEvent event) {
                pdbCode = event.getPDBCode();
                pdbMenu.setEnabled(true);
            }

            public void selectedChain(StructureEvent event) { }

            public void newObjectRequested(String accessionCode) {
                pdbCode="";
                pdbMenu.setEnabled(false);
            }
            public void noObjectFound(String accessionCode){
                pdbCode="";
                pdbMenu.setEnabled(false);
            }
            
        };
    }
    
    private boolean showDocument(URL url) 
    {
        if ( url != null ){
            boolean success = JNLPProxy.showDocument(url); 
            if ( ! success)
                logger.info("could not open URL "+url+" in browser. check your config or browser version.");
        return success;
        
        }
        else
            return false;
    }
    
    private boolean showDocument(String urlstring){
        try{
            URL url = new URL(urlstring);
            
            return showDocument(url);
        } catch (MalformedURLException e){
            logger.warning("malformed URL "+urlstring);
            return false;
        }
    }
   
    
    
    
    
}

