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
package org.biojava.spice.GUI;
import org.biojava.bio.program.das.dasalignment.*;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.biojava.spice.*;
import org.biojava.spice.Panel.SeqFeaturePanel;

import org.biojava.spice.Feature.Feature;
import org.biojava.spice.DAS.AlignmentTools;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;

/**
 * @author Andreas Prlic
 *
 */
public class AlignmentPanel extends JPanel {
    
    Alignment[] alignments;
    SPICEFrame spice;
    AlignmentTools aligTools;
    
    Chain chain ;
    int currentChainNumber;
    
    SeqFeaturePanel seqPanel; 
    StructureBuilder struBuild;
    Map pdbFeatures ;
    Map chainMap;
    
    /** a panel to choose a seq-structure alignment
     * 
     */
    public AlignmentPanel(SPICEFrame parent) {
        super();
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
        
        seqPanel = new SeqFeaturePanel(spice);
        struBuild = new StructureBuilder();
        // TODO Auto-generated constructor stub
    }
    
    public void setChain(Chain c,int number) {
        chain = c;
        currentChainNumber = number;
        //this.paintComponent(this.getGraphics());
        this.repaint();
    }
    
    
    public void setAlignments(Alignment[] aligs){
        alignments = aligs;
        
        String sequence = chain.getSequence();
        
        for ( int i=0 ; i < alignments.length; i++ ){
            Alignment ali = alignments[i];
            String PDBcode = aligTools.getPDBCodeFromAlignment(ali);
            Chain currentChain = null;
            try {
                // project the alignment on the sequence...
                currentChain = struBuild.createChain(ali,sequence);
                //System.out.println()
            } catch ( DASException e){
                e.printStackTrace();
                continue;
            }
            // create features from this chain, 
            List features = getStructureFeatures(currentChain,PDBcode);
            System.out.println(features); 
            // set in in seqpanel
            pdbFeatures.put(PDBcode,features);
            chainMap.put(PDBcode,currentChain);
            
            
            
        }
        System.out.println("alignments have been set");
    }
    public void paintComponent( Graphics g) {
        System.out.println("paintComponent aligpanel");
        //logger.entering(this.getClass().getName(),"paintComponent");
        super.paintComponent(g); 	
        
        Dimension dstruc=this.getSize();
        BufferedImage imbuf = (BufferedImage)this.createImage(dstruc.width,dstruc.height);
        
        
        // for each alignment draw a line on a graphic
        
        Graphics2D g2D = (Graphics2D)g ;
        
        int seqLen = chain.getLength() ;
        
        System.out.println("sequence length " +seqLen);
        
        seqPanel.setChain(chain,currentChainNumber);
        seqPanel.scale(dstruc);
        seqPanel.drawScale(g2D,seqLen);
        seqPanel.drawSequence(g2D,seqLen);
        
        if ( alignments == null )
            return ;		
        
        int aminosize = seqPanel.getAminoSize();
        System.out.println("aminosize " + aminosize);
        
        ArrayList empty = new ArrayList();
        int y = 22 ;
        for ( int i=0 ; i < alignments.length; i++ ){
            Alignment ali = alignments[i];
            String PDBcode = aligTools.getPDBCodeFromAlignment(ali);
            List features = (List) pdbFeatures.get(PDBcode);
            Chain currentChain = (Chain) chainMap.get(PDBcode);
            seqPanel.setChain(currentChain,0);
            seqPanel.setFeatures(features);
                        
            y = seqPanel.drawFeatures(g2D,aminosize,y);
            currentChain = null;
        }
        
        
        //          and repaint it ...
        seqPanel.setFeatures(empty);
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
                    Feature f = makeFeat(start,end,pdbcode);
                    features.add(f);
                    start = -1 ;
                }
            }
        }
        // finish
        if ( start > -1){
            Feature f = makeFeat(start,end,pdbcode);
            features.add(f);
        }
        
        
        return features;
    }
    
    /** create a feature */
    public Feature makeFeat(int start, int end, String pdbcode){
        
        Feature f = new Feature();
        f.setName(pdbcode);
        String txt ="sequence covered with structure (PDB code "+ pdbcode +")";
        f.addSegment(start,end,txt);
        f.setMethod("retreived from a DAS Alignmet server ");
        f.setSource("");
        
        return f;
    }
}
