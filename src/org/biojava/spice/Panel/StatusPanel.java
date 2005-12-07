/*
 *                    BioJava development code
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
 * Created on 21.03.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.spice.Panel;

import org.biojava.bio.structure.AminoAcid;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.spice.SPICEFrame;
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.Segment;
//import org.biojava.spice.Panel.seqfeat.FeatureEvent;
//import org.biojava.spice.Panel.seqfeat.FeatureViewListener;
//import org.biojava.spice.Panel.seqfeat.SelectedSeqPositionListener;
import org.biojava.spice.manypanel.eventmodel.SequenceEvent;
import org.biojava.spice.manypanel.eventmodel.SequenceListener;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureEvent;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureListener;
import org.biojava.spice.manypanel.eventmodel.StructureEvent;
import org.biojava.spice.manypanel.eventmodel.StructureListener;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JEditorPane;
import javax.swing.BoxLayout;
import javax.swing.JProgressBar;
import javax.swing.BorderFactory;
import javax.swing.Box;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import javax.swing.JFrame;
import java.awt.Component;
import java.util.*;
import java.awt.Point;

/** a class to display status information 
 * contains 
 * a status display to display arbitraty text
 * the PDB code of the currently displayed PDB file
 * the UniProt code of the currently displayed UniProt sequence    
 * a progressBar to display ongoing progress
 */
public class StatusPanel
extends JPanel
implements SpiceFeatureListener,
SequenceListener,
StructureListener
{
    
    private static final long serialVersionUID = 928391747589181827L;
    public static String PDBLINK     = "http://www.rcsb.org/pdb/cgi/explore.cgi?pdbId=";
    public static String UNIPROTLINK = "http://www.ebi.uniprot.org/uniprot-srv/uniProtView.do?proteinAc=" ;
    
    Map pdbheader;
    
    JTextField pdbCode ;
    JTextField spCode  ;    
    JTextField status ;
    JTextField pdbDescription;
    JTextField seq_pos ;
    JProgressBar progressBar ;
    SPICEFrame spice ;
    //Structure structure;
    int currentChainNumber;
    PDBDescMouseListener pdbdescMouseListener;
    Chain chain;
    Structure structure;
    
    public StatusPanel(SPICEFrame parent){
        //TODO: clean up this class
     
        spice = parent;
        currentChainNumber = -1;
        //this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        //this.setBorder(BorderFactory.createEmptyBorder());
        
        Box hBox =  Box.createHorizontalBox();
        /*
        JTextField pdbtxt  = new JTextField("PDB code:");
        pdbtxt.setEditable(false);
        pdbtxt.setBorder(BorderFactory.createEmptyBorder());
        pdbtxt.setMaximumSize(new Dimension(70,20));
        pdbtxt.setPreferredSize(new Dimension(70,20));
        hBox.add(pdbtxt);
        
        chain = new ChainImpl();
        structure = new StructureImpl();
        */
        pdbCode = new JTextField("    ");
        /*pdbCode.setEditable(false);
        
        pdbCode.setBorder(BorderFactory.createEmptyBorder());
        pdbCode.setMaximumSize(new Dimension(40,20));
        pdbCode.setPreferredSize(new Dimension(40,20));
        MouseListener mousiPdb = new PanelMouseListener(spice,this,PDBLINK);
        // mouse listener         
        pdbCode.addMouseListener(mousiPdb);
        
        hBox.add(pdbCode);
        hBox.add(pdbCode,BorderLayout.WEST);

        // pdb description
         */
        pdbDescription = new JTextField("pdbDesc");
        /* pdbDescription.setBorder(BorderFactory.createEmptyBorder());
        pdbDescription.setEditable(false);
        pdbDescription.setMaximumSize(new Dimension(150,30));
        */
        pdbdescMouseListener = new PDBDescMouseListener();
        /*
         * //pdbdescMouseListener.setPDBHeader(new HashMap());
        pdbdescMouseListener.setPDBHeader(pdbheader);
        pdbDescription.addMouseListener(pdbdescMouseListener);
        pdbDescription.addMouseMotionListener(pdbdescMouseListener);
        
        hBox.add(pdbDescription);
        
        JTextField sptxt  = new JTextField("UniProt code:");
        sptxt.setEditable(false);
        sptxt.setBorder(BorderFactory.createEmptyBorder());
        sptxt.setMaximumSize(new Dimension(80,20));
        hBox.add(sptxt);
        */
        spCode = new JTextField("      ");
        /*spCode.setBorder(BorderFactory.createEmptyBorder());
        spCode.setEditable(false);
        spCode.setMaximumSize(new Dimension(80,20));
        MouseListener mousiSp = new PanelMouseListener(spice,this,UNIPROTLINK);
        // mouse listener 
        spCode.addMouseListener(mousiSp);
        
        hBox.add(spCode);
        */
        progressBar = new JProgressBar(0,100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setString(""); 
        progressBar.setMaximumSize(new Dimension(80,20));
        progressBar.setIndeterminate(false);
        progressBar.setBorder(BorderFactory.createEmptyBorder());
        //hBox.add(progressBar,BorderLayout.EAST);
        
        
        seq_pos        = new JTextField();
        // init Seqouece position
        //seq_pos.setForeground(new Color(255, 255, 255));
        //seq_pos.setBackground(new Color(0, 0, 0));
        //seq_pos.setSize(700, 30);
        seq_pos.setMaximumSize(new Dimension(Short.MAX_VALUE,20));
        //seq_pos.setBorder(BorderFactory.createEmptyBorder());
        
        //Box vBox = Box.createVerticalBox();
        //vBox.add(hBox);
        //hBox.add(Box.createHorizontalGlue());
        
        hBox.add(seq_pos);
        //vBox.add(seq_pos);
        
        this.add(hBox);	
        
    }
    
    
    
    
    public void newStructure(StructureEvent event) {
        structure = event.getStructure();
        chain = structure.getChain(0);
        setPDB(structure.getPDBCode());
    }




    public void selectedChain(StructureEvent event) {
       chain = structure.getChain(event.getCurrentChainNumber());
        
    }




    public void newObjectRequested(String accessionCode) {
        
        setLoading(true);
    }




    public void newSequence(SequenceEvent e) {
      
        setLoading(false);
     
    }
    
    



    public void setCurrentChainNumber(int number){
        currentChainNumber = number;
    }
    public void setPDBHeader(Map header){
        pdbheader = header;
        pdbdescMouseListener.setPDBHeader(header);
    }
    public Map getPDBHeader(){
        return pdbheader;
    }
    
    public void setStatus(String txt) { status.setText(txt); }
    
    public void setLoading(boolean flag){
        progressBar.setIndeterminate(flag);
        
    }
    public void setPDB(String pdb)    { 
        if (pdb == null) 
            pdb = "-";
        
        pdbCode.setText(pdb);
        pdbCode.repaint();
        
    }
    
    public void setSP(String sp)      { 
        if (sp == null)  
            sp = "-" ;
        
        spCode.setText(sp)  ;
        spCode.repaint();
        
    }
    
    public void setPDBDescription(String desc){
        pdbDescription.setText(desc);
        pdbDescription.repaint();
    }
    
    
   
    private String getToolString(int chainnumber,int seqpos) {
        //return "tmp in getToolString";
        
        
        if ( chain == null) return "" ;
        
        if ( ! ((seqpos >= 0) && (seqpos < chain.getLength()))) {
            return "" ;
        } 
        
        
        
        Group g = chain.getGroup(seqpos);	
        Character amino1 = new Character(" ".charAt(0)) ;
        if (g.getType() == "amino" ) {
            AminoAcid a = (AminoAcid) g ;
            amino1 = a.getAminoType();
        }
        String pdbstr = g.getPDBCode();	
        String name   = g.getPDBName();
        if (pdbstr == null ) {
            pdbstr = "n.a." ;
            
        }
        
        String drstr = "Seq pos "+ (seqpos +1) + "("+amino1+","+name+")" + " PDB ("+ pdbstr +")";  	
        return drstr ;
        
    }
    
    public String getPDBPos( int chainnumber, int seqpos){
        
        if ( seqpos < 0 ) return "n.a.";
        
        if ( chain == null) return "n.a." ;
        if ( seqpos >= chain.getLength()) return "n.a.";
        
        Group g = chain.getGroup(seqpos);	
        //Character amino1 = new Character(" ".charAt(0)) ;
        //if (g.getType() == "amino" ) {
          //  AminoAcid a = (AminoAcid) g ;
            //amino1 = a.getAminoType();
        //}
        String pdbstr = g.getPDBCode();	
        
        if (pdbstr == null ) {
            pdbstr = "n.a." ;
            
        }
        return pdbstr;
    }
    public void selectedSeqRange(int start, int end) {
        
        //System.out.println("selected " + start + " " + end);
        String pdbstart = getPDBPos(currentChainNumber,start);
        String pdbend = getPDBPos(currentChainNumber,end);
        String txt = "selected seq: " + start + " - " + end + " pdb: " +pdbstart +"-" +pdbend;
        seq_pos.setText(txt);
        seq_pos.repaint();
    }
    
    public void selectionLocked(boolean flag){
        // ignore
    }
    
    public void selectedSeqPosition(int seqpos){
        //System.out.println("selected seqpos " + seqpos );
        String str = getToolString(currentChainNumber, seqpos);
        seq_pos.setText(str);
    }
	public void mouseOverFeature(SpiceFeatureEvent e){
	    
	    Feature feat = e.getFeature();
	    //System.out.println("mouse over feature " + feat);
	    String txt= feat.toString();
	    seq_pos.setText(txt);
	}
	
	public void mouseOverSegment(SpiceFeatureEvent e){
	    Segment seg = e.getSegment();
	    //System.out.println("mouse over segment " + seg);
	    String txt = seg.toString();
	    seq_pos.setText(txt);
	}
	public void featureSelected(SpiceFeatureEvent e){
	    Feature feat = e.getFeature();
        if ( feat.getMethod().equals("Unknown")) {
            return;
        }
	    //System.out.println("selected feature " + feat);
	    String txt= feat.toString();
	    seq_pos.setText(txt);
	}
	public void segmentSelected(SpiceFeatureEvent e){
	    Segment seg = e.getSegment();
	    //System.out.println("selected segment " + seg);
	    String txt = seg.toString();
        seq_pos.setText(txt);
	}




    public void clearSelection() {
       seq_pos.setText("");
        
    }
    
    
    
}
/*

class PanelMouseListener
implements MouseListener
{
    SPICEFrame spice;
    StatusPanel parent;
    String caller;
    PanelMouseListener( SPICEFrame spice_, StatusPanel parent_,String caller_){
        spice = spice_;
        parent=parent_;
        caller=caller_;
        
    }
    
    public void mouseClicked(MouseEvent e){
        JTextField source = (JTextField)  e.getSource();
        
        try {
            URL url = new URL(caller+source.getText());
            spice.showDocument(url);
        } catch ( Exception ex){
            
        }
        
        
    }
    public void mouseExited(MouseEvent e){
        // remove tooltip
        JTextField source = (JTextField)e.getSource();
        source.setToolTipText(null);
    }
    public void mouseEntered(MouseEvent e){
        // display tooltip
        JTextField source = (JTextField)e.getSource();
        source.setToolTipText("click to open in browser");
        
    }
    public void mouseReleased(MouseEvent e){}
    public void mousePressed(MouseEvent e){
        
        
     
    }
    
    
    
}

*/

/** a class responsible of creating afloating frame
 *  if the mouse is moved over the description of the PDB file 
class PDBDescMouseListener implements MouseListener, MouseMotionListener {
    Map pdbHeader ;
    boolean frameshown ;
    
    JFrame floatingFrame;
    public PDBDescMouseListener(){
        super();
        pdbHeader = new HashMap();
        frameshown = false;
    }
    
    private void displayFrame() {
        if ( frameshown ) {
            return;
        }
        
        floatingFrame = new JFrame();
        JFrame.setDefaultLookAndFeelDecorated(false);
        floatingFrame.setUndecorated(true);
        updateFrameContent(pdbHeader);       
        
                
        frameshown = true;
        
    }
    
    private void disposeFrame(){
        if ( ! frameshown ){
            return;
        }
        
        floatingFrame.setVisible(false);
        floatingFrame.dispose();
        
        frameshown = false;
    }
    
    private void updateFrameContent(Map h){
        
        if ( h == null )h = new HashMap();
        
        String t = "<html><body><table>";
        Set s = h.keySet();
        Iterator iter = s.iterator();
        while (iter.hasNext()){
            String key = (String) iter.next();
            String value = (String)h.get(key);
            t+="<tr><td>"+key+"</td><td>"+value+"</td></tr>";
        }
        t+="</table></body></html>";
        
        JEditorPane txt = new JEditorPane("text/html",t);
        txt.setEditable(false);
        
        floatingFrame.getContentPane().add(txt);
        floatingFrame.pack();
    }
    
    private void updateFramePosition(MouseEvent e){
        if ( ! frameshown){
            return;
        }
       int x = e.getX();
       int y = e.getY();
       // get parent components locations
       Component compo = e.getComponent();
       Point screenTopLeft = compo.getLocationOnScreen();
       int cx = screenTopLeft.x;
       int cy = screenTopLeft.y;
       
       int compo_h = compo.getHeight();
       //floatingFrame.setLocationRelativeTo(compo);
        
        // update the position of the frame, according to the mouse position
       //System.out.println((cx-x-5)+" " + (cy+ y+5)+" x:" + x + " y:" + y +
         //      " cx:" + cx + " cy:" + cy + " c_h:"+ compo_h  );
        
        Dimension d = floatingFrame.getSize();
        int dx = d.width;
        //int dy = d.height;
        
        int posx = cx + x  - ( dx/2)    ;
        int posy = cy + y + compo_h + 5 ;
        
        floatingFrame.setLocation(posx,posy);
    }
    
    public void setPDBHeader(Map h ){
        pdbHeader = h;
        if ( frameshown){
            updateFrameContent(h);
        }
    }
    
//  for mousemotion:
    public void mouseDragged(MouseEvent e){
        
    }
    
    public void mouseMoved(MouseEvent e){
        
        if ( frameshown) {
            updateFramePosition(e);
        } else {
            displayFrame();
            updateFramePosition(e);
            floatingFrame.setVisible(true);
        }
        
    }
    
    // for mouselistener
    
    
    public void mouseEntered(MouseEvent e){
        displayFrame();
        updateFramePosition(e);
        floatingFrame.setVisible(true);
        //System.out.println("mouse entered");
    }
    
    public void mousePressed(MouseEvent e){
        
    }
    public void mouseClicked(MouseEvent e){
        
    }
    
    public void mouseExited(MouseEvent e){
        disposeFrame();
        //System.out.println("mouse exited");
    }
    public void mouseReleased(MouseEvent e){
        
    }
    
  
    
}
*/
