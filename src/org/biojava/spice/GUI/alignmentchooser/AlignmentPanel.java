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


import org.biojava.bio.program.das.dasalignment.*;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Color ;

import javax.swing.JPanel;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.*;
import javax.swing.JToolTip;

import org.biojava.spice.*;
import org.biojava.spice.Panel.SeqFeaturePanel;

import org.biojava.spice.Feature.FeatureImpl;
import org.biojava.spice.DAS.AlignmentTools;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.gui.sequence.BumpedRenderer;
import org.biojava.bio.gui.sequence.FeatureBlockSequenceRenderer;
import org.biojava.bio.gui.sequence.FilteringRenderer;
import org.biojava.bio.gui.sequence.SequencePanel;
import org.biojava.bio.gui.sequence.SequenceRenderContext;
import org.biojava.bio.gui.sequence.SequenceViewerEvent;
import org.biojava.bio.gui.sequence.SequenceViewerListener;
import org.biojava.bio.gui.sequence.SequenceViewerMotionListener;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.symbol.* ;
import org.biojava.utils.ChangeVetoException;
import org.biojava.bio.gui.sequence.*;
import org.biojava.bio.Annotation;
import org.biojava.bio.SimpleAnnotation;

/**
 * @author Andreas Prlic
 *
 */
public class AlignmentPanel extends SequencePanel {
    
    org.biojava.bio.program.das.dasalignment.Alignment[] alignments;
    SPICEFrame spice;
    AlignmentTools aligTools;
    
    Chain chain ;
    int currentChainNumber;
    
    SeqFeaturePanel seqPanel; 
    StructureBuilder struBuild;
    Map pdbFeatures ;
    Map chainMap;
    Logger logger;
    
    MultiLineRenderer multiLineRenderer;
    BumpedRenderer bumpR;
    
    /** a panel to choose a seq-structure alignment
     * 
     */
    public AlignmentPanel(SPICEFrame parent) {
        super();
        logger = Logger.getLogger("org.biojava.spice");
        setDoubleBuffered(true);
        setOpaque(true);
        System.out.println("init aligpanel");
        spice = parent;
        alignments = null;
        chain = null;
        currentChainNumber = -1 ;
        
        pdbFeatures = new HashMap();
        chainMap    = new HashMap();
        
        // needed as helpers for drawing
        aligTools = new AlignmentTools(spice.getConfiguration());
        
        struBuild = new StructureBuilder();
        // TODO Auto-generated constructor stub
        
 
        try {
            multiLineRenderer = new MultiLineRenderer();
            SequenceRenderer ruleR = new RulerRenderer();
            multiLineRenderer.addRenderer(ruleR);
            this.setRenderer(multiLineRenderer);
            // default display first 100 aminos...
            this.setRange(new RangeLocation(1,100));
  
            
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
        AligPanelMouseListener svl = new AligPanelMouseListener(spice,this);
        this.addSequenceViewerListener(svl);
        
        //SequenceViewerMotionListener svm = new MySVL(this);
        this.addSequenceViewerMotionListener(svl);
        
        
    }
    
    public void setChain(Chain c,int number) {
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
        this.setSequence(sequence);
        
        this.repaint();
    }
    
    public Chain getChain(){
        return chain;
    }
    
    /** convert the biojava alignment class into biojava feature objects */
    
    public synchronized void setAlignments(org.biojava.bio.program.das.dasalignment.Alignment[] aligs){
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
            this.setSequence(sequence);
            //display the whole Sequence
            this.setRange(new RangeLocation(1,sequence.length()));
        }
        
        // template for feature.
        org.biojava.bio.seq.Feature.Template temp = new org.biojava.bio.seq.Feature.Template();
        
        for ( int i=0 ; i < alignments.length; i++ ){
            org.biojava.bio.program.das.dasalignment.Alignment ali = alignments[i];
            String PDBcode = aligTools.getPDBCodeFromAlignment(ali);
            Chain currentChain = null;
            try {
                // project the alignment on the sequence...
                currentChain = struBuild.createChain(ali,seq_str);
                //System.out.println()
            } catch ( DASException e){
                e.printStackTrace();
                continue;
            }
            // create features from this chain, 
            List features = getStructureFeatures(currentChain,PDBcode);
            //logger.log(Level.FINEST, "nr features: " +features.size()); 
            
            // convert them to display...
            
            temp.source = PDBcode;
            temp.type = "Protein Structure";
            
            FeatureFilter ff = new FeatureFilter.BySource(PDBcode); 
            //logger.log(Level.INFO,"filtering by "+PDBcode);
            
            
            FilteringRenderer filtR = new FilteringRenderer();
            
            //filtR.setRenderer(bumpR);
            try {
                LabelledSequenceRenderer labelsR = new LabelledSequenceRenderer(50,20);
                labelsR.setFillColor(Color.white);
                labelsR.addLabelString(PDBcode);
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
                
                continue;
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
                    org.biojava.bio.seq.Feature f ;
                    try {
                        f= sequence.createFeature(temp);                        
                        
                    } catch (Exception e){
                        logger.warning(e.getMessage());
                    }   
                }   
            }
        }
        //      set the Sequence to Render
        this.setSequence(sequence);
        
        //display the whole Sequence
        this.setRange(new RangeLocation(1,sequence.length()));
                
        //System.out.println("alignments have been set");
        notifyAll();
        this.getParent().repaint();
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
        
        System.out.println("resizing");
        
        String seq_str = "" ; 
        if ( chain != null) {
            seq_str = chain.getSequence();    
            double y = (d.getWidth() / (double)seq_str.length());
            System.out.println("old y" + this.getScale() + "new y " + y);
            //this.setScale(y);
        }
        
        super.setSize(d);
        
    }
    
    
}






