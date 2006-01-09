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
 * Created on Feb 2, 2005
 *
 */
package org.biojava.spice.GUI.alignmentchooser;

import org.biojava.services.das.registry.DasCoordinateSystem;
import org.biojava.spice.SpiceApplication;
import org.biojava.bio.Annotation;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.spice.das.AlignmentParameters;
import org.biojava.spice.das.AlignmentThread;
import org.biojava.spice.das.AlignmentTools;
import org.biojava.spice.das.SpiceDasSource;
import org.biojava.spice.manypanel.AlignmentTool;
import org.biojava.spice.manypanel.BrowserPane;
import org.biojava.spice.manypanel.eventmodel.AlignmentEvent;
import org.biojava.spice.manypanel.eventmodel.AlignmentListener;
import org.biojava.spice.manypanel.eventmodel.ObjectListener;
import org.biojava.spice.manypanel.eventmodel.SequenceEvent;
import org.biojava.spice.manypanel.eventmodel.SequenceListener;
import org.biojava.spice.manypanel.managers.SequenceManager;
import org.biojava.spice.manypanel.renderer.FeaturePanel;
import org.biojava.spice.Panel.SeqFeaturePanel;

import org.biojava.bio.program.das.dasalignment.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.Iterator;


/**
 * A class to choose bewteen differen sequence and structure alignments.
 * The structure choosen here will be displayed as the main structure in SPICE.
 * 
 * @author Andreas Prlic
 *
 */
public class AlignmentChooser implements 
SequenceListener,
AlignmentListener
{
    
    
    JProgressBar progressBar;
    
    Alignment[] alignments;
    //SPICEFrame spice;
    AlignmentTools aligTools;
    
    Chain chain ;
    int currentChainNumber;
    DasCoordinateSystem queryCoordSys;
    DasCoordinateSystem subjectCoordSys;
    SeqFeaturePanel seqPanel; 
    //StructureBuilder struBuild;
    Map pdbFeatures ;
    Map chainMap;
    Logger logger;
    List aligPanels;
    Box vBox;
    SpiceDasSource[] dasSources;
    List objectListeners;
    JScrollPane scroll;
    //SequencePanel sequencePanel;
    //MultiLineRenderer multiLineRenderer;
    //BumpedRenderer bumpR;
    
    //AligPanelMouseListener mouseListener;
    JPanel panel;
    FeaturePanel featurePanel;
    
    /**
     * @param arg0
     */
    public AlignmentChooser(DasCoordinateSystem queryCs, DasCoordinateSystem subjectCs) {
        logger = Logger.getLogger("org.biojava.spice");
        
        //logger.info("new ALignmentChooser " + queryCs + " " + subjectCs);
        //spice = parent ;
        chain = new ChainImpl();
        
        dasSources = new SpiceDasSource[0];
        queryCoordSys = queryCs;
        subjectCoordSys = subjectCs;
        objectListeners = new ArrayList();
        
        initPanels();
        //mouseListener = new AligPanelMouseListener(this);
    }
    
    /** a panel to choose a seq-structure alignment
     * 
     */
    
    
    public void addObjectListener(ObjectListener li){
        //mouseListener.addObjectListener(li);
        objectListeners.add(li);
    }
    
    public void initPanels(){
        scroll = new JScrollPane();
        featurePanel = new FeaturePanel();
        featurePanel.setPreferredSize(new Dimension(400,30));
        panel = new JPanel();
        aligPanels = new ArrayList();
        vBox = Box.createVerticalBox();
    }
    
    public void show(){
        
        String uniprot = chain.getSwissprotId();            
        
        float scl = (float)(530 / ((float)chain.getLength() + 100 + 20)); 
        setScale(scl);
        
        // display in a new frame
        JFrame alignmentFrame = new JFrame();       
        
        alignmentFrame.setTitle("Choose Alignment");
        alignmentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        
        //	JFrame.setDefaultLookAndFeelDecorated(false);
        ImageIcon icon = SpiceApplication.createImageIcon("spice16x16.gif");
        alignmentFrame.setIconImage(icon.getImage());
        
        //vBox = Box.createVerticalBox();
        Box hBox = Box.createHorizontalBox();
        
        // visualize these.
        JTextField txt =new JTextField("Alignment for "+uniprot + " vs. " + subjectCoordSys);
        txt.setMaximumSize(new Dimension(400,20));
        txt.setBorder(BorderFactory.createEmptyBorder());
        hBox.add(txt);
        
        progressBar = new JProgressBar(0,100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setString(""); 
        progressBar.setMaximumSize(new Dimension(80,20));
        progressBar.setIndeterminate(true);
        progressBar.setBorder(BorderFactory.createEmptyBorder());
        
        // space filler
        //hBox.add(Box.createGlue());
        
        hBox.add(progressBar,BorderLayout.EAST);
        
        vBox.add(hBox);
        
        
        vBox.add(featurePanel);
        
        loadAlignments(uniprot);
        
        AligPanelListener apl = new AligPanelListener(this);
        alignmentFrame.addComponentListener(apl);
        
        JButton close = new JButton("Close");
        
        MyActionListener myAl = new MyActionListener(alignmentFrame);
        close.addActionListener(myAl);
        
        Box hBoxb = Box.createHorizontalBox();
        hBoxb.add(Box.createGlue());
        hBoxb.add(close,BorderLayout.EAST);
        
        //vBox.add(hBoxb);
        scroll = new JScrollPane(vBox);  
        //vBox.setPreferredSize(new Dimension(530,600));
        panel = new JPanel();
        panel.setBackground(FeaturePanel.BACKGROUND_COLOR);
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(530,600));
        panel.add(scroll);
        panel.add(Box.createGlue());
        panel.add(hBoxb);
        
        alignmentFrame.getContentPane().add(panel);
        alignmentFrame.pack();
        
        alignmentFrame.setVisible(true);
        repaint();
        
    }
    
    public float getScale() {
        return featurePanel.getScale();
    }
    
    public void setScale(float s){
        //logger.info("alginment chooser setScale " + s);
        featurePanel.setScale(s);
        //sequencePanel.repaint();
        
        Iterator iter = aligPanels.iterator();
        while (iter.hasNext()){
            ShowAligPanel pan = (ShowAligPanel)iter.next();
            pan.setScale(s);
        }
        repaint();
    }
    
    public void repaint() {
        Iterator iter = aligPanels.iterator();
        while (iter.hasNext()){
            ShowAligPanel pan = (ShowAligPanel)iter.next();
            pan.repaint();
        }
        
       /* featurePanel.revalidate();
        featurePanel.repaint();
        vBox.repaint();
        vBox.revalidate();
        scroll.repaint();
        scroll.revalidate();*/
        panel.repaint();
        panel.revalidate();
        
   
    }
    
    
    public void setDasSources(SpiceDasSource[] sources){
        dasSources = sources;
    }
    
    /*public void setQueryCoordinateSystem(DasCoordinateSystem ds){
     queryCoordSys = ds;
     }
     
     public void setSubjectCoordinateSystem(DasCoordinateSystem ds){
     subjectCoordSys = ds;
     }*/
    
    public void loadAlignments(String code){
        // triger the load of the alignments...
        //Todo add this...
        
        logger.info("loading  " + code + queryCoordSys);
        AlignmentParameters param = new AlignmentParameters();
        
        param.setDasSources(dasSources);
        if ( queryCoordSys.toString().equals(BrowserPane.DEFAULT_PDBCOORDSYS ))
            if ( code.length() == 6) {
                
                String chain = code.substring(5,6).toUpperCase();
                logger.info("setting query pdb chain "+chain);
                code = code.substring(0,4);
                param.setQueryPDBChainId(chain);
            }
        
        param.setQuery(code);
        param.setQueryCoordinateSystem(queryCoordSys);
        param.setSubjectCoordinateSystem(subjectCoordSys);
        
        //TODO: clean this up ...
        DasCoordinateSystem ecs = new DasCoordinateSystem();
        ecs.setName("ensemblpep-human-ncbi35");
        
        if ( queryCoordSys.toString().equals(BrowserPane.DEFAULT_ENSPCOORDSYS)){
            param.setQueryCoordinateSystem(ecs);               
        }
        if ( subjectCoordSys.toString().equals(BrowserPane.DEFAULT_ENSPCOORDSYS)) {
            param.setSubjectCoordinateSystem(ecs);
        }
        
        AlignmentThread thread = new AlignmentThread(param);
        thread.addAlignmentListener(this);
        thread.start();
    }
    
    public void setChain(Chain c,int number) {
        
        //logger.info("set Chain " + c.getSwissprotId() + " " + number + " >" + c.getSequence()+"<");
        chain = c;
        currentChainNumber = number;
        //this.paintComponent(this.getGraphics());
        //String seq_str = chain.getSequence();
        /*Sequence sequence ;
         try {         
         sequence = ProteinTools.createProteinSequence(seq_str, "prot_1");
         }
         catch (IllegalSymbolException e){
         // can not convert sequence!
          logger.warning(e.getMessage());
          notifyAll();
          return;
          }*/
        featurePanel.setChain(chain);
        
        
        repaint();
    }
    
    public Chain getChain(){
        return chain;
    }
    
    public void noAlignmentFound(AlignmentEvent event){
        progressBar.setIndeterminate(false);
        repaint();
    }
    
    /** convert the biojava alignment class into biojava feature objects */
    
    public synchronized void newAlignment(AlignmentEvent event){
        //logger.info("AllignmentChooser got new Alignments");
        
        progressBar.setIndeterminate(false);
        Alignment[] aligs = event.getAllAlignments();
        alignments = aligs;
        if ( aligs == null){
            logger.warning("no alignments given");
            notifyAll();
            return;
        }
        
        if ( chain == null ){
            logger.warning("trying to set alignments before chain has been set");
            notifyAll();
            return;
        }
        logger.info("you can choose between " + aligs.length + " alignments");
        String seq_str = chain.getSequence();
        
        if ( alignments == null) {
            
            return;
        }
        
        
        for ( int i=0 ; i < alignments.length; i++ ){
            org.biojava.bio.program.das.dasalignment.Alignment ali = alignments[i];
            
            //subject is object 2
            Annotation[] os = ali.getObjects();
            if ( os.length < 2){
                // something strange is going on here..
                logger.warning(" got  alignment of wrong # objects...");
                return;
            }
            
            String queryId = getQueryId();
            String subjectId = getSubjectId(ali);
            
            logger.info("alignments for query: "+ queryId + " subject:" + subjectId );
            
            
            Chain currentChain = null;
            try {
                // project the alignment on the sequence...
                //currentChain = struBuild.createChain(ali,seq_str);
                currentChain = AlignmentTool.createChain(ali,seq_str,queryId,subjectId);
                //System.out.println(currentChain);
            } catch ( Exception e){
                e.printStackTrace();
                continue;
            }
            
            ShowAligPanel pan = new ShowAligPanel(ali);
            
            pan.setChain(currentChain);
            pan.setScale(featurePanel.getScale());
            vBox.add(pan);
            aligPanels.add(pan);
            
            pan.addMouseListener(new MyMouseListener(currentChain.getSwissprotId(),this,pan));
            this.setScale(getScale());
            repaint();
            
            
            
        }
        vBox.add(Box.createGlue());
        repaint();
    }
    
    
    
    public void triggerNewObjectRequested(String code){
        Iterator iter = objectListeners.iterator();
        while (iter.hasNext()){
            ObjectListener li =  (ObjectListener)iter.next();
            li.newObjectRequested(code);
        }
    }
    
    private String getQueryId(){
        
        
        return chain.getSwissprotId();
        
        
    }
    
    private String getSubjectId(Alignment ali){
        Annotation[] os = ali.getObjects();
        if ( os.length < 2){
            // something strange is going on here..
            logger.warning(" got  alignment of wrong # objects...");
            return "";
        }
        
        Annotation a1 = os[0];
        Annotation a2 = os[1];
        String query = getQueryId();
        String s1 =  (String) a1.getProperty("dbAccessionId");
        String s2 =  (String) a2.getProperty("dbAccessionId");        
        if ( s1.equalsIgnoreCase(query))
            return s2;
        else if ( s2.equalsIgnoreCase(query))
            return s1;
        else
            return s1;
        
    }
    
    
    /** overwrite resize, to change the scale of the displayed sequence */
    public void setSize(Dimension d){
        
        //System.out.println("resizing");
        
        String seq_str = "" ; 
        if ( chain != null) {
            seq_str = chain.getSequence();    
            float y = (float)(d.getWidth() / (float)seq_str.length());
            //System.out.println("old y" + this.getScale() + "new y " + y);
            featurePanel.setScale(y);
        }
        repaint();
        //sequencePanel.setSize(d);
        
    }
    
    public void newObjectRequested(String accessionCode) {
        // chain = new ChainImpl();
        // featurePanel.setChain(chain);
        // featurePanel.repaint();
    }
    
    public void noObjectFound(String accessionCode){
        clearAlignment();
    }
    
    public void clearAlignment(){
        chain = new ChainImpl();
        progressBar.setIndeterminate(false);
        vBox.repaint();
        featurePanel.repaint();
        
    }
    public void clearSelection() { }
    
    public void newSequence(SequenceEvent e) {
        
        SequenceManager sm = new SequenceManager();
        chain = sm.getChainFromString(e.getSequence());
        chain.setSwissprotId(e.getAccessionCode());
        featurePanel.setChain(chain);
        repaint();
    }
    
    public void selectedSeqPosition(int position) { }
    
    public void selectedSeqRange(int start, int end) {}
    
    public void selectionLocked(boolean flag) {}
    
    
    
    
}


class MyActionListener implements
ActionListener {
    JFrame parent;
    public MyActionListener(JFrame alignmentFrame){
        parent =alignmentFrame;
    }
    public void actionPerformed(ActionEvent event) {
        //dispose();
        parent.dispose();
    }
}

class MyMouseListener implements MouseListener
{
    String code;
    AlignmentChooser parent;
    ShowAligPanel pan;
    static Logger logger = Logger.getLogger("org.biojava.spice");
    public MyMouseListener(String accessionCode, AlignmentChooser parent, ShowAligPanel pan){
        code = accessionCode;
        this.parent = parent;
        this.pan = pan;
    }
    public void mouseClicked(MouseEvent arg0) {
        logger.info("requested new object " + code);
        parent.triggerNewObjectRequested(code);
        
    }
    
    public void mouseEntered(MouseEvent arg0) {
        pan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
    }
    
    public void mouseExited(MouseEvent arg0) {
        pan.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    public void mousePressed(MouseEvent arg0) {
        
    }
    
    public void mouseReleased(MouseEvent arg0) {
        
    }
    
}
