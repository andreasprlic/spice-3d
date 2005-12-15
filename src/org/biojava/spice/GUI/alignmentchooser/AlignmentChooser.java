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
import org.biojava.bio.SimpleAnnotation;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.RangeLocation;
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
import org.biojava.spice.Feature.FeatureImpl;
import org.biojava.spice.Panel.SeqFeaturePanel;
import org.biojava.bio.gui.sequence.BasicFeatureRenderer;
import org.biojava.bio.gui.sequence.BumpedRenderer;
import org.biojava.bio.gui.sequence.FeatureBlockSequenceRenderer;
import org.biojava.bio.gui.sequence.FilteringRenderer;
import org.biojava.bio.gui.sequence.LabelledSequenceRenderer;
import org.biojava.bio.gui.sequence.MultiLineRenderer;
import org.biojava.bio.gui.sequence.RulerRenderer;
import org.biojava.bio.gui.sequence.SequenceRenderer;
import org.biojava.bio.gui.sequence.SequencePanel;
import org.biojava.bio.program.das.dasalignment.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.Iterator;
import java.util.NoSuchElementException;

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
    
    SpiceDasSource[] dasSources;
    SequencePanel sequencePanel;
    MultiLineRenderer multiLineRenderer;
    BumpedRenderer bumpR;
    
    AligPanelMouseListener mouseListener;
    
    /**
     * @param arg0
     */
    public AlignmentChooser(DasCoordinateSystem queryCs, DasCoordinateSystem subjectCs) {
        logger = Logger.getLogger("org.biojava.spice");
        
        logger.info("new ALignmentChooser " + queryCs + " " + subjectCs);
        //spice = parent ;
        chain = new ChainImpl();
       
        dasSources = new SpiceDasSource[0];
        queryCoordSys = queryCs;
        subjectCoordSys = subjectCs;
        
        initSequencePanel(); 
    }
    
    /** a panel to choose a seq-structure alignment
     * 
     */
    public void initSequencePanel() {
        //super();
        //logger = Logger.getLogger("org.biojava.spice");
        sequencePanel = new SequencePanel();
        sequencePanel.setDoubleBuffered(true);
        sequencePanel.setOpaque(true);
        
        
        alignments = null;
        chain = null;
        currentChainNumber = -1 ;
        
        pdbFeatures = new HashMap();
        chainMap    = new HashMap();
        
        // needed as helpers for drawing
        //aligTools = new AlignmentTools(spice.getConfiguration());
        
        //struBuild = new StructureBuilder(queryCoordSys,subjectCoordSys);
        
        
        
        try {
            multiLineRenderer = new MultiLineRenderer();
            SequenceRenderer ruleR = new RulerRenderer();
            multiLineRenderer.addRenderer(ruleR);
            sequencePanel.setRenderer(multiLineRenderer);
            // default display first 100 aminos...
            sequencePanel.setRange(new RangeLocation(1,100));
            
            
            FeatureBlockSequenceRenderer fbr = new FeatureBlockSequenceRenderer();     
            BasicFeatureRenderer featr = new BasicFeatureRenderer();
            featr.setOutline(Color.white);
            //featr.setPaint(Color);
            fbr.setFeatureRenderer(featr);
            bumpR = new BumpedRenderer(fbr);        
            
            //set the MultiLineRenderer as the SequencePanels renderer
        } catch (Exception e){
            logger.warning(e.getMessage());
        }
        
        // add listeners
        mouseListener = new AligPanelMouseListener(sequencePanel);
        sequencePanel.addSequenceViewerListener(mouseListener);
        
        //SequenceViewerMotionListener svm = new MySVL(this);
        sequencePanel.addSequenceViewerMotionListener(mouseListener);
        
     
        
    }
    
    public void addObjectListener(ObjectListener li){
        mouseListener.addObjectListener(li);
    }
    
    public void show(){
        //System.out.println("starting to retreive Alignments");
        // get uniprot seq from spice.
        
        //int currentChainNumber = spice.getCurrentChainNumber();
        //if ( currentChainNumber < 0) {
        //  logger.warning(" no active chain found ");
        //  return;
        //}
        //Chain chain = spice.getChain(currentChainNumber);
        
        String uniprot = chain.getSwissprotId();
        //Alignment[] aligs = null ;
        
        
        // display in a new frame
        JFrame alignmentFrame = new JFrame();       
        
        alignmentFrame.setTitle("Choose Alignment");
        alignmentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //alignmentFrame.setSize(700, 700);
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(700,700));
        //	JFrame.setDefaultLookAndFeelDecorated(false);
        ImageIcon icon = SpiceApplication.createImageIcon("spice.png");
        alignmentFrame.setIconImage(icon.getImage());
        
        Box vBox = Box.createVerticalBox();
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
        hBox.add(Box.createGlue());
        
        hBox.add(progressBar,BorderLayout.EAST);
        
        vBox.add(hBox);
        
        
        setChain(chain,currentChainNumber);
        //setAlignments(aligs);
        
        //JScrollPane scroll = new JScrollPane(sequencePanel) ;
        //scroll.setPreferredSize(new Dimension( 600, 600));
        //scroll.setPreferredSize(new Dimension(600, 600));;
        //scroll.setMinimumSize(  new Dimension(30, 30));;
        
        //vBox.add(scroll);
        
        vBox.add(sequencePanel);
        
        panel.add(vBox);
        //panel.add(scroll);
        //panel.add(aligPanel);
        
        //aligPanel.repaint();
        alignmentFrame.getContentPane().add(panel);
        alignmentFrame.pack();
        
        alignmentFrame.setVisible(true);
        
        loadAlignments(uniprot);
        
        AligPanelListener apl = new AligPanelListener(this);
        alignmentFrame.addComponentListener(apl);
        
        
    }
    
    public double getScale() {
        return sequencePanel.getScale();
    }
    
    public void setScale(double s){
        sequencePanel.setScale(s);
        sequencePanel.repaint();
        
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
        
        logger.info("loading  " + code);
        AlignmentParameters param = new AlignmentParameters();
        
        param.setDasSources(dasSources);
        if ( queryCoordSys.equals(BrowserPane.DEFAULT_PDBCOORDSYS ))
            if ( code.length() == 6) {
                
                String chain = code.substring(5,6);
                code = code.substring(0,4);
                param.setQueryPDBChainId(chain);
            }
        param.setQuery(code);
        param.setQueryCoordinateSystem(queryCoordSys);
        param.setSubjectCoordinateSystem(subjectCoordSys);
        
        AlignmentThread thread = new AlignmentThread(param);
        thread.addAlignmentListener(this);
        thread.start();
    }
    
    public void setChain(Chain c,int number) {
        
        logger.info("set Chain " + c.getSwissprotId() + " " + number + " >" + c.getSequence()+"<");
        chain = c;
        currentChainNumber = number;
        //this.paintComponent(this.getGraphics());
        String seq_str = chain.getSequence();
        Sequence sequence ;
        try {         
            sequence = ProteinTools.createProteinSequence(seq_str, "prot_1");
        }
        catch (IllegalSymbolException e){
            // can not convert sequence!
            logger.warning(e.getMessage());
            notifyAll();
            return;
        }
        sequencePanel.setSequence(sequence);
        
        sequencePanel.repaint();
    }
    
    public Chain getChain(){
        return chain;
    }
    
    /** convert the biojava alignment class into biojava feature objects */
    
    public synchronized void newAlignment(AlignmentEvent event){
        logger.info("AllignmentChooser got new Alignments");
        
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
        Sequence sequence ;
        try {         
            sequence = ProteinTools.createProteinSequence(seq_str, "prot_1");
        }
        catch (IllegalSymbolException e){
            // can not convert sequence!
            logger.warning(e.getMessage());
            notifyAll();
            return;
        }
        if ( alignments == null) {
            sequencePanel.setSequence(sequence);
            //display the whole Sequence
            sequencePanel.setRange(new RangeLocation(1,sequence.length()));
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
            
            
            
            //logger.info(queryId + " " + a1.getProperty("type"));
            //logger.info(subjectId + " " + a2.getProperty("type"));
            
            // get the 
           
            
            //String PDBcode = AlignmentTools.getPDBCodeFromAlignment(ali);
            logger.info("alignments for "+ queryId + " " + subjectId );
            
            
            Chain currentChain = null;
            try {
                // project the alignment on the sequence...
                //currentChain = struBuild.createChain(ali,seq_str);
                currentChain = AlignmentTool.createChain(ali,seq_str,queryId,subjectId);
                //System.out.println()
            } catch ( Exception e){
                e.printStackTrace();
                continue;
            }
            
            
            Annotation object = AlignmentTools.getObject(subjectId,ali);
            paintSequence(sequence,currentChain,queryId,subjectId,object);
        }
        
    }
    
    
    private void paintSequence( Sequence sequence, Chain currentChain, String queryId,String subjectId, Annotation subjectAnnotation ){
            // create features from this chain, 
            List features = getStructureFeatures(currentChain,subjectId);
            //logger.log(Level.FINEST, "nr features: " +features.size()); 
            
            // convert them to display...
            // template for feature.
            org.biojava.bio.seq.Feature.Template temp = new org.biojava.bio.seq.Feature.Template();
            
            temp.source = subjectId;
            temp.type = "Protein Structure";
            
            FeatureFilter ff = new FeatureFilter.BySource(subjectId); 
            //logger.log(Level.INFO,"filtering by "+PDBcode);
            
            
            FilteringRenderer filtR = new FilteringRenderer();
            
            // build up label ...
            String labelstring = subjectId;
            //Annotation object = AlignmentTools.getObject(subjectId,ali);
            Annotation object =subjectAnnotation;
            List details = new ArrayList();
            
            try {
                details = (List) object.getProperty("details");
            } catch (NoSuchElementException e){
                // details are not provided
                logger.info("alignment does not contain details");
            }
            Iterator iter = details.iterator();
            while (iter.hasNext()){
                Annotation detail = (Annotation) iter.next();
                //logger.info(detail.get("property").toString() + " " + detail.get("detail").toString());
                String property = (String) detail.getProperty("property");
                String detailstr   = (String) detail.getProperty("detail");
                
                if ( property.equals("resolution")){
                    if ( detailstr != null )
                        labelstring += " " + detailstr + "A" ; 
                }
                if ( property.equals("experiment_type")){
                    labelstring += " " + detailstr ;
                }
                if ( property.equals("molecule description")){
                    labelstring += " description: " + detailstr;
                }
            }
            
            //filtR.setRenderer(bumpR);
            try {
                LabelledSequenceRenderer labelsR = new LabelledSequenceRenderer(50,20);
                labelsR.setFillColor(Color.white);
                labelsR.addLabelString(labelstring);
                labelsR.setRenderer(bumpR);
                //multiLineRenderer.addRenderer(labelsR);
                //labelsR.addLabelString(PDBcode);
                filtR.setRenderer(labelsR);
                filtR.setFilter(ff);           
                //filtR.setRenderer(bumpR);
                
                multiLineRenderer.addRenderer(filtR);
                
                //StackedFeatureRenderer stackR = new StackedFeatureRenderer();
                //stackR.addRenderer(labelR);
                //stackR.addRenderer(filtR);
                //multiLineRenderer.addRenderer(stackR);
            } catch ( Exception e) {
                logger.warning(e.getMessage());
                e.printStackTrace();
                
                return;
            }  
            
            
            // iterate over all features
            for (int ai=0; ai< features.size(); ai++) {
                org.biojava.spice.Feature.Feature feat = 
                    (org.biojava.spice.Feature.Feature)features.get(ai);
                
                //String source = feat.getSource();
                //temp.source=source;
                //System.out.println(source);
                
                // and create Biojav Feature  object
                temp.type=feat.getType();
                Annotation anno = new SimpleAnnotation();
                try {
                    anno.setProperty("name",feat.getName());
                    anno.setProperty("description",labelstring);
                } catch (Exception e){
                    logger.warning(e.getMessage());
                }
                temp.annotation = anno;
                List segments = feat.getSegments();
                for ( int s =0; s< segments.size();s++){
                    org.biojava.spice.Feature.Segment segment = 
                        (org.biojava.spice.Feature.Segment) segments.get(s);
                    int start = segment.getStart();
                    int end = segment.getEnd();
                    
                    
                    Location l = new RangeLocation(start,end);
                    temp.location = l;
                    //org.biojava.bio.seq.Feature f ;
                    try {
                        sequence.createFeature(temp);                        
                        
                    } catch (Exception e){
                        logger.warning(e.getMessage());
                    }   
                }   
            }
    
    
        // now set the new sequence ...
        //      set the Sequence to Render
        sequencePanel.setSequence(sequence);
        
        //display the whole Sequence
        sequencePanel.setRange(new RangeLocation(1,sequence.length()));
        //sequencePanel.setSize(sequencePanel.getSize());      
        //System.out.println("alignments have been set");
       
        //sequencePanel.getParent().repaint();
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
        if ( s1.equals(query))
            return s2;
        else
            return s1;
        
    }
    
    public List getStructureFeatures(Chain chain,String pdbcode){
        ArrayList features = new ArrayList();
        
        
        int start = -1;
        int end   = -1;
        
        for ( int i=0 ; i< chain.getLength() ; i++ ) {
            Group g = chain.getGroup(i);
            String aminopdb = g.getPDBCode();
            
            if ( aminopdb != null ){
                if ( start == -1){
                    start = i;
                }
                end = i;
            } else {
                if ( start > -1) {
                    FeatureImpl f = makeFeat(start,end,pdbcode);
                    features.add(f);
                    start = -1 ;
                }
            }
        }
        // finish
        if ( start > -1){
            FeatureImpl f = makeFeat(start,end,pdbcode);
            features.add(f);
        }
        
        
        return features;
    }
    
    /** create a feature */
    public FeatureImpl makeFeat(int start, int end, String pdbcode){
        
        FeatureImpl f = new FeatureImpl();
        f.setName(pdbcode);
        String txt ="sequence covered with structure (PDB code "+ pdbcode +")";
        f.addSegment(start,end,txt);
        f.setMethod("retreived from a DAS Alignmet server ");
        f.setSource("");
        
        return f;
    }
    
    /** overwrite resize, to change the scale of the displayed sequence */
    public void setSize(Dimension d){
        
        //System.out.println("resizing");
        
        String seq_str = "" ; 
        if ( chain != null) {
            seq_str = chain.getSequence();    
            double y = (d.getWidth() / (double)seq_str.length());
            //System.out.println("old y" + this.getScale() + "new y " + y);
            sequencePanel.setScale(y);
        }
        
        sequencePanel.setSize(d);
        
    }
    
    public void newObjectRequested(String accessionCode) {
        chain = new ChainImpl();
        
    }
    public void clearAlignment(){}
    public void clearSelection() { }
    
    public void newSequence(SequenceEvent e) {
       
        SequenceManager sm = new SequenceManager();
        chain = sm.getChainFromString(e.getSequence());
        chain.setSwissprotId(e.getAccessionCode());
        
    }
    
    public void selectedSeqPosition(int position) { }
    
    public void selectedSeqRange(int start, int end) {}
    
    public void selectionLocked(boolean flag) {}
    
    
    
    
}
