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
 * Created on Oct 30, 2005
 *
 */
package org.biojava.spice.manypanel;

//import java.net.URL;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;

import java.awt.Color;
import java.util.*;
import java.util.logging.Logger;

import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.dasregistry.Das1Source;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;
import org.biojava.dasobert.dasregistry.DasSource;
import org.biojava.dasobert.eventmodel.*;
import org.biojava.spice.manypanel.drawable.DrawableDasSource;
import org.biojava.spice.manypanel.eventmodel.DasSourceEvent;
import org.biojava.spice.manypanel.eventmodel.DasSourceListener;
import org.biojava.spice.manypanel.eventmodel.SpiceFeatureListener;
import org.biojava.spice.manypanel.managers.*;
import org.biojava.spice.manypanel.renderer.*;


//import javax.swing.BoxLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//import java.awt.event.MouseListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** The main Container for the different sequence and alignment panels
 * 
 * @author Andreas Prlic
 *
 */
public class BrowserPane 


/** the contentPane for the frame
 * 
 */
extends JPanel
implements DasSourceListener,
ChangeListener

{
    final static long serialVersionUID = 879143879134613639L;
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    //public static  String registry = "http://servlet.sanger.ac.uk/dasregistry/services/das_registry";
    //public static  String registry = "http://www.spice-3d.org/dasregistry/services/das_registry";
    
    public static String DEFAULT_PDBCOORDSYS     = "PDBresnum,Protein Structure";
    public static String DEFAULT_UNIPROTCOORDSYS = "UniProt,Protein Sequence";
    public static String DEFAULT_ENSPCOORDSYS    = "Ensembl,Protein Sequence";
    
    List allsources ; // a list of DasSource[] 
    
    List structureListeners;
    List uniProtListeners;
    List enspListeners;
    StructureRenderer structureRenderer ;
    SequenceRenderer seqRenderer;
    SequenceRenderer enspRenderer;
    
    int storeStart;
    int storeEnd;
    
    StructureManager strucManager;
    SequenceManager seqManager;
    SequenceManager enspManager;
    AlignmentManager aligManager;
    AlignmentManager ensaligManager;
    
    FeatureManager pdbFeatureManager;
    FeatureManager upFeatureManager;
    FeatureManager enspFeatureManager;
    
    public static int DEFAULT_PANE_WIDTH  = 600;
    public static int DEFAULT_PANE_HEIGHT = 600;
    
    static Color BG_COLOR = Color.WHITE;
    JPanel contentPanel;
    
    
    JSlider residueSizeSlider;
    
    public BrowserPane(String PDBCOORDSYS, String UNIPROTCOORDSYS, String ENSPCOORDSYS) {
        super();
        contentPanel = new JPanel();
        
        contentPanel.setLayout(new BoxLayout(contentPanel,BoxLayout.X_AXIS));
        
        
        structureListeners = new ArrayList();
        uniProtListeners   = new ArrayList();
        enspListeners      = new ArrayList();
        allsources         = new ArrayList();
        
        
        
        strucManager = new StructureManager();
        addStructureListener(strucManager);
        
        structureRenderer = new StructureRenderer();
        //structureRenderer = new StructureRenderer(); 
        structureRenderer.getStatusPanel().setName("PDB");
        structureRenderer.setBackground(BG_COLOR);
        
        ComponentResizedChainListener strucComponentWidthSetter = new ComponentResizedChainListener(structureRenderer);
        contentPanel.addComponentListener(strucComponentWidthSetter);
        
        
        strucManager.addStructureRenderer(structureRenderer);
        
        DasCoordinateSystem dcs = DasCoordinateSystem.fromString(PDBCOORDSYS);
        strucManager.setCoordinateSystem(dcs);
        
        
        pdbFeatureManager = new FeatureManager();
        pdbFeatureManager.setCoordinateSystem(dcs);
        pdbFeatureManager.addDasSourceListener(structureRenderer);
        //addStructureListener(fm);
        FeatureRenderer featureRenderer = new FeatureRenderer();
        pdbFeatureManager.addFeatureRenderer(featureRenderer);
        
        
        
        //fm.addDasSourceListener(structureRenderer);
        
        //structureRenderer.addFeatureRenderer(featureRenderer);
        
        
        // link the feature manager to the StructureManager        
        //strucManager.setFeatureManager(fm);
        strucManager.addSequenceListener(pdbFeatureManager);
        //structureSequencePane.set
        
        
        
        ///////////////
        // now add the UniProt
        //////////////
        
        seqManager = new SequenceManager();
        addUniProtListener(seqManager);
        
        seqRenderer = new SequenceRenderer();
        seqRenderer.getStatusPanel().setName("UniProt");
        seqRenderer.setBackground(BG_COLOR);
        
        ComponentResizedChainListener seqComponentWidthSetter = new ComponentResizedChainListener(seqRenderer);
        contentPanel.addComponentListener(seqComponentWidthSetter);
        
        DasCoordinateSystem seqdcs = DasCoordinateSystem.fromString(UNIPROTCOORDSYS);
        seqManager.setCoordinateSystem(seqdcs);
        seqManager.addSequenceRenderer(seqRenderer);
        
        upFeatureManager = new FeatureManager();
        upFeatureManager.setCoordinateSystem(seqdcs);
        upFeatureManager.addDasSourceListener(seqRenderer);
        addUniProtListener(upFeatureManager);
        FeatureRenderer seqFeatureRenderer = new FeatureRenderer();
        upFeatureManager.addFeatureRenderer(seqFeatureRenderer);
        
        //seqManager.setFeatureManager(seqfm);
        seqManager.addSequenceListener(upFeatureManager);
        
        
        ///////////////
        // now add the Alignment PDB to UniProt
        //////////////
        
        aligManager = new AlignmentManager("PDB_UP",dcs,seqdcs);
        
        strucManager.addStructureListener(aligManager);
        SequenceListener pdbList = aligManager.getSeq1Listener();
        SequenceListener upList = aligManager.getSeq2Listener();
        
        strucManager.addSequenceListener(pdbList);
        seqManager.addSequenceListener(upList);
        
        structureRenderer.addSequenceListener(pdbList);
        seqRenderer.addSequenceListener(upList);
        
        aligManager.addObject1Listener(strucManager);
        aligManager.addObject2Listener(seqManager);
        
        CursorPanel[] structureCursors =structureRenderer.getCursorPanels();
        for (int i = 0; i < structureCursors.length;i++){
            aligManager.addSequence1Listener(structureCursors[i]);
        }
        CursorPanel[] seqCursors =seqRenderer.getCursorPanels();
        for (int i = 0; i < seqCursors.length;i++){        
            aligManager.addSequence2Listener(seqCursors[i]);
        }
        
        AlignmentRenderer seqAligRenderer = new AlignmentRenderer();
        aligManager.addAlignmentRenderer(seqAligRenderer);
        
        structureRenderer.addScaleChangeListener(seqAligRenderer.getSeq1ScaleListener());
        seqRenderer.addScaleChangeListener(seqAligRenderer.getSeq2ScaleListener());
        
        structureRenderer.addSequenceListener(seqAligRenderer.getSequenceListener1());
        seqRenderer.addSequenceListener(seqAligRenderer.getSequenceListener2());
        aligManager.addSequence1Listener(seqAligRenderer.getSequenceListener1());
        aligManager.addSequence2Listener(seqAligRenderer.getSequenceListener2());
        
        structureRenderer.addAdjustmentListener(seqAligRenderer.getAdjust1());
        seqRenderer.addAdjustmentListener(seqAligRenderer.getAdjust2());
        
        seqAligRenderer.setPreferredSize(new Dimension(400,20));
        
        
        
        ///////////////
        // now add the ENSP
        //////////////
        
        
        enspManager = new SequenceManager();
        addEnspListener(enspManager);
        
        enspRenderer = new SequenceRenderer();
        enspRenderer.getStatusPanel().setName("ENSP");
        enspRenderer.setBackground(BG_COLOR);
        
        ComponentResizedChainListener enspComponentWidthSetter = new ComponentResizedChainListener(enspRenderer);
        contentPanel.addComponentListener(enspComponentWidthSetter);
        
        
        DasCoordinateSystem enspdcs = DasCoordinateSystem.fromString(ENSPCOORDSYS);
        enspManager.setCoordinateSystem(enspdcs);
        
        
        enspManager.addSequenceRenderer(enspRenderer);
        
        enspFeatureManager = new FeatureManager();
        enspFeatureManager.setCoordinateSystem(enspdcs);
        enspFeatureManager.addDasSourceListener(enspRenderer);
        addEnspListener(enspFeatureManager);
        FeatureRenderer enspFeatureRenderer = new FeatureRenderer();
        enspFeatureManager.addFeatureRenderer(enspFeatureRenderer);
        
        
        DasSource[]enspfeatservs = getServers("features",enspdcs);
        
        
        enspFeatureManager.setDasSources(enspfeatservs);
        
        //enspManager.setFeatureManager(enspfm);
        enspManager.addSequenceListener(enspFeatureManager);
        
        
        
        ///////////////
        // now add the Alignment ENSP to UniProt
        //////////////
        
        ensaligManager = new AlignmentManager("UP_ENSP",seqdcs,enspdcs);
        
        SequenceListener upenspList = ensaligManager.getSeq1Listener();
        SequenceListener enspList   = ensaligManager.getSeq2Listener();
        
        seqRenderer.addSequenceListener(upenspList);
        enspRenderer.addSequenceListener(enspList);
        
        //strucManager.addSequenceListener(pdbList);
        seqManager.addSequenceListener(upenspList);
        enspManager.addSequenceListener(enspList);
        
        
        
        ensaligManager.addObject1Listener(seqManager);
        ensaligManager.addObject2Listener(enspManager);
        
        for (int i = 0; i < seqCursors.length;i++){        
            ensaligManager.addSequence1Listener(seqCursors[i]);
        }
        CursorPanel[] enspCursors = enspRenderer.getCursorPanels();
        for (int i = 0; i < enspCursors.length;i++){
            ensaligManager.addSequence2Listener(enspCursors[i]);
        }
        ensaligManager.addSequence1Listener(upList);
        
        aligManager.addSequence2Listener(upenspList);
        
        
        AlignmentRenderer enspAligRenderer = new AlignmentRenderer();
        ensaligManager.addAlignmentRenderer(enspAligRenderer);
        
        seqRenderer.addScaleChangeListener(enspAligRenderer.getSeq1ScaleListener());
        enspRenderer.addScaleChangeListener(enspAligRenderer.getSeq2ScaleListener());
        
        seqRenderer.addSequenceListener(enspAligRenderer.getSequenceListener1());
        enspRenderer.addSequenceListener(enspAligRenderer.getSequenceListener2());
        aligManager.addSequence2Listener(enspAligRenderer.getSequenceListener1());
        ensaligManager.addSequence1Listener(enspAligRenderer.getSequenceListener1());
        ensaligManager.addSequence2Listener(enspAligRenderer.getSequenceListener2());
        ensaligManager.addSequence1Listener(seqAligRenderer.getSequenceListener2());
        //browserPane.addPane(structureSequencePane);
        enspAligRenderer.setPreferredSize(new Dimension(400,20));
        
        
        seqRenderer.addAdjustmentListener(enspAligRenderer.getAdjust1());
        enspRenderer.addAdjustmentListener(enspAligRenderer.getAdjust2());
        
        // reset all the DAS sources...
        clearDasSources();
        
        
        registerEventTranslators();
        
        
        //
        // build up the display from the components:
        //
        
        
        JSplitPane splito = new JSplitPane(JSplitPane.VERTICAL_SPLIT,structureRenderer,seqAligRenderer);
        splito.setOneTouchExpandable(true);
        splito.setResizeWeight(1.0);
        splito.setBorder(BorderFactory.createEmptyBorder());
        
        MyPropertyChangeListener mpcl = new MyPropertyChangeListener(seqAligRenderer,this,splito,"bottom");
        splito.addPropertyChangeListener("dividerLocation", mpcl );
        
        JSplitPane split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,splito,seqRenderer);
        split1.setOneTouchExpandable(true);
        // uniprot panel gets a little more space, because so many more DAS sources...
        split1.setResizeWeight(0.5);
        
        split1.setBorder(BorderFactory.createEmptyBorder());
        
        
        
        
//      JPanel p2 = new JPanel();
//      p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
//      p2.add(enspAligRenderer);
//      p2.add(enspRenderer);
        
        JSplitPane splitb = new JSplitPane(JSplitPane.VERTICAL_SPLIT,enspAligRenderer,enspRenderer);
        splitb.setOneTouchExpandable(true);
        splitb.setResizeWeight(0);
        splitb.setBorder(BorderFactory.createEmptyBorder());
        
        //MyComponentListener mycompo2 = new MyComponentListener(enspAligRenderer,enspRenderer);
        
        //splitb.addComponentListener(mycompo2);
        
        MyPropertyChangeListener mpcl2 = new MyPropertyChangeListener(enspAligRenderer,this,splitb,"top");
        splitb.addPropertyChangeListener("dividerLocation",mpcl2);
        
        JSplitPane split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,split1,splitb);
        split2.setOneTouchExpandable(true);
        // uniprot panel gets more space
        split2.setResizeWeight(0.7);
        split2.setBorder(BorderFactory.createEmptyBorder());
        
        //Dimension d = new Dimension(DEFAULT_PANE_WIDTH,DEFAULT_PANE_HEIGHT);
        //split2.setPreferredSize(d);
        contentPanel.add(split2);
        
        
        /// and now ...
        
        // the scale ...
        int RES_MIN  = 1;
        int RES_MAX  = 100;
        int RES_INIT = 100;
        residueSizeSlider = new JSlider(JSlider.HORIZONTAL,
                RES_MIN, RES_MAX, RES_INIT);
        residueSizeSlider.setInverted(true);
        //residueSizeSlider.setMajorTickSpacing(5);
        //residueSizeSlider.setMinorTickSpacing(2);
        residueSizeSlider.setPaintTicks(false);
        residueSizeSlider.setPaintLabels(false);
        residueSizeSlider.addChangeListener(this);
        residueSizeSlider.setPreferredSize(new Dimension(100,15));
        
        
        Box hBox = Box.createHorizontalBox();
        hBox.setBackground(BG_COLOR);
        hBox.add(Box.createHorizontalGlue());
        hBox.add(residueSizeSlider);
        hBox.add(Box.createHorizontalGlue());
        
        // register the managers
        registerManagers();
        
        this.setOpaque(true);
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
        this.add(contentPanel);
        this.add(hBox);
        
        
    }
    

    /** make sure that feature events are correctly translated from one coord sys (panel) to another 
     * 
     *
     */ 
    private void registerEventTranslators(){

        
        ChainRendererMouseListener mouserPdb  = structureRenderer.getChainRendererMouseListener();
        ChainRendererMouseListener mouserUp   = seqRenderer.getChainRendererMouseListener();
        ChainRendererMouseListener mouserEnsp = enspRenderer.getChainRendererMouseListener();
        
        // for selection of the whole feature
        RowHeaderMouseListener upRowHeader   = seqRenderer.getRowHeaderListener();
        RowHeaderMouseListener pdbRowHeader  = structureRenderer.getRowHeaderListener();
        RowHeaderMouseListener enspRowHeader = enspRenderer.getRowHeaderListener();
        
        SpiceFeatureListener li1 = aligManager.getFeatureTranslator1();
        SpiceFeatureListener li2 = aligManager.getFeatureTranslator2();
        
        SpiceFeatureListener li3 = ensaligManager.getFeatureTranslator1();
        SpiceFeatureListener li4 = ensaligManager.getFeatureTranslator2();
        
        
        mouserPdb.addSpiceFeatureListener(li1);
        pdbRowHeader.addSpiceFeatureListener(li1);
        
        mouserUp.addSpiceFeatureListener(li2);
        mouserUp.addSpiceFeatureListener(li3);        
        upRowHeader.addSpiceFeatureListener(li2);
        upRowHeader.addSpiceFeatureListener(li3);
        
        mouserEnsp.addSpiceFeatureListener(li4);
        enspRowHeader.addSpiceFeatureListener(li4);
        
        // and register the cursor panels ...
                
        //aligManager.addSeq1FeatureListener(li1);
        aligManager.addSeq2FeatureListener(li3);
//               
        ensaligManager.addSeq1FeatureListener(li2);        
//        ensaligManager.addSeq2FeatureListener(li4);
    }
    
    /** add the managers to the ArrowPanel of the statuspanels...
     * 
     *
     */
    private void registerManagers(){
        
        
        ArrowPanel a2 = seqRenderer.getArrowPanel();
        a2.setUpperAlignmentManager(aligManager);
        a2.setLowerAlignmentManager(aligManager);
        a2.setUpperObjectListener(strucManager);
        a2.setLowerObjectListener(seqManager);
        
        
        ArrowPanel a3 = enspRenderer.getArrowPanel();
        
        a3.setUpperAlignmentManager(ensaligManager);
        a3.setLowerAlignmentManager(ensaligManager);
        a3.setUpperObjectListener(seqManager);
        a3.setLowerObjectListener(enspManager);
        
     
        
        
    }
    
    public void stateChanged(ChangeEvent e) {
        
        JSlider source = (JSlider)e.getSource();
        //if (!source.getValueIsAdjusting()) {
        //System.out.println("slider at " +source.getValue());
        int residueSize = (int)source.getValue();
        structureRenderer.calcScale(residueSize);
        seqRenderer.calcScale(residueSize);
        enspRenderer.calcScale(residueSize);
        contentPanel.repaint();
        
        this.repaint();
        this.revalidate();
        //this.updateUI();
        //int width = getTotalWidth();
        //int height = getTotalHeight();
        //Dimension d = new Dimension(width,height);
        //logger.info("setting preferred size" + width + " " + height);
        //this.setPreferredSize(d);
        //this.setSize(d);
        // }
    }
    
    
    
    
    
    public void disableDasSource(DasSourceEvent dsEvent) {
        
        
    }
    
    public void enableDasSource(DasSourceEvent dsEvent) {
        
        
    }
    public void removeDasSource(DasSourceEvent dsEvent){
        logger.finest("BrowserPane got removeDasSource ");
    }
    public void newDasSource(DasSourceEvent dsEvent) {
        
        allsources.add(dsEvent.getDasSource().getDasSource());
    }
    
    
    /** set all das sources at once
     * 
     * @param sources
     */
    public  void setDasSources(SpiceDasSource[] sources){
        
        // clear the das sources ...
        //clearDasSources();       
         
        List newsources = new ArrayList();        
        
        // keep the das sources that have already been know...
        // trigger remove of those that are not in list any longer...
        for (int i = 0 ; i< sources.length;i++){
            SpiceDasSource ds = sources[i];
            
            // is inactive, do not add
            if (! ds.getStatus()) {
                logger.finest(ds.getNickname() + " status: " + ds.getStatus());    
                continue;
            }
            
            SpiceDasSource oldds = null;
            
            boolean known = false;
            Iterator iter = allsources.iterator();
            while (iter.hasNext()){
                SpiceDasSource testds = (SpiceDasSource)iter.next();
                if ( ds.getUrl().equals(testds.getUrl())){
                    known = true;
                    oldds = testds;
                  
                    break;
                }
            }
            if ( ! known) {
                logger.finest("new server! " + ds.getNickname());
                newsources.add(ds);
                
            }
            else {
                newsources.add(oldds);
                logger.finest("old server: " + ds.getNickname() + ds.getStatus());
            }
        }
        
        //logger.finest("# of old sources " + allsources.size() );
        // remove  the old ones that are not used any more:
        Iterator iter = allsources.iterator();
        while (iter.hasNext()){
            SpiceDasSource ds = (SpiceDasSource)iter.next();
            Iterator newiter = newsources.iterator();
            boolean found = false;
            while (newiter.hasNext()){
                SpiceDasSource newds = (SpiceDasSource)newiter.next();
                //logger.finest(newds.getUrl() + " should  remove? " + ds.getUrl());
                if ( newds.getUrl().equals(ds.getUrl())){
                    found = true;
                    break;
                }
            }
            if ( ! found){
                
                triggerRemoveDasSource(ds);
            }
        }
        
        allsources = newsources;
        
        // this are the coord.sys.
        
        DasCoordinateSystem dcs     = strucManager.getCoordinateSystem();
        DasCoordinateSystem seqdcs  = seqManager.getCoordinateSystem();
        DasCoordinateSystem enspdcs = enspManager.getCoordinateSystem();
        
        
        // set the reference servers 
        DasSource[]strucservs = getServers("structure",dcs);
        strucManager.setDasSources(strucservs);
        
        DasSource[]seqservs = getServers("sequence",seqdcs);        
        seqManager.setDasSources(seqservs);
        
        DasSource[]enspservs = getServers("sequence",enspdcs);        
        enspManager.setDasSources(enspservs);        
        
        // set the annotation servers
        
        DasSource[]featservs = getServers("features",dcs);
        pdbFeatureManager.setDasSources(featservs);
        
        
        DasSource[]seqfeatservs = getServers("features",seqdcs);        
        upFeatureManager.setDasSources(seqfeatservs);
        
        DasSource[] enspfeatservs = getServers("features",enspdcs);
        enspFeatureManager.setDasSources(enspfeatservs);
        
        SpiceDasSource[] strucaligs = getAlignmentServers(allsources,dcs,seqdcs);
        aligManager.setAlignmentServers(strucaligs);
        
        // get the alignment das source
        SpiceDasSource[] enspupaligs = getAlignmentServers(allsources,seqdcs,enspdcs);
        ensaligManager.setAlignmentServers(enspupaligs);
        
        this.repaint();
        
    }
    
    private void triggerRemoveDasSource(SpiceDasSource ds){
        logger.finest("triggerRemoveDasSource " + ds.getNickname());
        
        DasSourceEvent event = new DasSourceEvent(DrawableDasSource.fromDasSource((DasSource)ds));
        
        DasCoordinateSystem[] cs = ds.getCoordinateSystem();
        for ( int i=0 ; i< cs.length; i++){
            DasCoordinateSystem dcs = cs[i];
            if ( dcs.toString().equals(DEFAULT_PDBCOORDSYS)){
                // remove from structure panel
                structureRenderer.removeDasSource(event);
            } else if ( dcs.toString().equals(DEFAULT_UNIPROTCOORDSYS))
                seqRenderer.removeDasSource(event);
            else if ( dcs.toString().equals(DEFAULT_ENSPCOORDSYS))
                enspRenderer.removeDasSource(event);
        }
        
    }
    
    public  void clearDasSources() {
        //logger.info("browserPane clear das sources");
        allsources = new ArrayList();
        ensaligManager.clearDasSources();
        aligManager.clearDasSources();
        upFeatureManager.clearDasSources();
        enspManager.clearDasSources();
        seqManager.clearDasSources();
        pdbFeatureManager.clearDasSources();
        strucManager.clearDasSources();
        
        
        
    }
    
    
    public void selectedDasSource(DasSourceEvent ds) {
        
        
    }
    
    
    public void addPDBPositionListener(SequenceListener li){
        ChainRendererMouseListener mouser = structureRenderer.getChainRendererMouseListener();
        mouser.addSequenceListener(li);
        aligManager.addSequence1Listener(li);
    }
    
    public void addPDBSpiceFeatureListener(SpiceFeatureListener li){
        // now done by renderer...
        //ChainRendererMouseListener mouser = structureRenderer.getChainRendererMouseListener();
        //mouser.addSpiceFeatureListener(li);        
        structureRenderer.addSpiceFeatureListener(li);
        aligManager.addSeq1FeatureListener(li);
        
    
        
    }
    
    public void addUniProtSpiceFeatureListener(SpiceFeatureListener li){
        ChainRendererMouseListener seqmouser = seqRenderer.getChainRendererMouseListener();
        seqmouser.addSpiceFeatureListener(li);
        
        aligManager.addSeq2FeatureListener(li);
        ensaligManager.addSeq1FeatureListener(li);
        
    }
    
    public void addEnspSpiceFeatureListener(SpiceFeatureListener li){
        ChainRendererMouseListener enspmouser = enspRenderer.getChainRendererMouseListener();
        enspmouser.addSpiceFeatureListener(li);
        ensaligManager.addSeq2FeatureListener(li);
    }
    
    public void addStructureListener(StructureListener li){
        structureListeners.add(li);        
        strucManager.addStructureListener(li);
    }
    
    public void removeStructureListener(StructureListener li){
        structureListeners.remove(li);
        strucManager.removeStructureListener(li);
    }
    
    public StructureListener[] getStructureListener(){
        return strucManager.getStructureListener();
    }
    
    public void addPDBSequenceListener(SequenceListener li){
        strucManager.addSequenceListener(li);
        aligManager.addSequence1Listener(li);
    }
    
    public SequenceListener[] getPDBSequenceListener(){
        CursorPanel[] cursors = structureRenderer.getCursorPanels();
        SequenceListener[] sli = new SequenceListener[cursors.length + 1];
        for ( int i=0;i < cursors.length;i++){
            sli[i] = cursors[i];
            
        }
        sli[cursors.length] = aligManager.getSeq1Listener();
        return sli;
    }
    public void addUniProtListener(ObjectListener li){
        uniProtListeners.add(li);
        
    }
    
    public void addUniProtSequenceListener(SequenceListener li ){
        seqManager.addSequenceListener(li);
    }
    
    public void removeUniProtSequenceListener(SequenceListener li){
        seqManager.removeSequenceListener(li);
    }
    
    public void addEnspListener(ObjectListener li){
        enspListeners.add(li);
    }
    
    public void addEnspSequenceListener(SequenceListener li){
        enspManager.addSequenceListener(li);
    }
    
    public void removeEnspSequenceListener(SequenceListener li){
        enspManager.removeSequenceListener(li);
    }
    
    public void addDasSourceListener(DasSourceListener li){
        pdbFeatureManager.addDasSourceListener(li);
        upFeatureManager.addDasSourceListener(li);
        enspFeatureManager.addDasSourceListener(li);
    }
    
    public void triggerLoadStructure(String pdbcode){
        logger.finest("trigger load structure " + pdbcode);
        clearDisplay();
        
        aligManager.clearAlignment();
        ensaligManager.clearAlignment();
        
        
        Iterator iter = structureListeners.iterator();
        while (iter.hasNext()){
            ObjectListener li = (ObjectListener) iter.next();
            li.newObjectRequested(pdbcode);
        }
    }
    
    public void triggerLoadUniProt(String accessionCode){
        
        clearDisplay();
        
        Iterator iter = uniProtListeners.iterator();
        while (iter.hasNext()){
            ObjectListener li = (ObjectListener) iter.next();
            li.newObjectRequested(accessionCode);
        }
    }
    
    public void triggerLoadENSP(String enspCode){
        
        clearDisplay();
        
        Iterator iter = enspListeners.iterator();
        while (iter.hasNext()){
            ObjectListener li = (ObjectListener) iter.next();
            li.newObjectRequested(enspCode);
        }
    }
    
    public AlignmentManager getTopAlignmentManager(){
        return aligManager;
    }
    public AlignmentManager getBottomAlignmentManager(){
        return ensaligManager;
    }
    
    
    public void clearDisplay(){
        
        aligManager.clearAlignment();
        
        
        ensaligManager.clearAlignment();
        
        strucManager.clear();
        
        structureRenderer.clearDisplay();
        seqRenderer.clearDisplay();
        enspRenderer.clearDisplay();
        
        residueSizeSlider.setValue(100);
        
    }
    
    /** removes all listeners */
    public void clearListeners(){
        uniProtListeners.clear();
        enspListeners.clear();
        structureListeners.clear();
        
        strucManager.clearStructureListeners();
        strucManager.clearSequenceListeners();
        seqManager.clearSequenceListeners();
        enspManager.clearSequenceListeners();
        
        structureRenderer.clearListeners();
        seqRenderer.clearListeners();
        enspRenderer.clearListeners();
        
        
    }
    
    public StructureManager getStructureManager(){
        return strucManager;
        
    }
    
    public StructureRenderer getStructureRenderer(){
        return structureRenderer;
    }
    
    public SequenceManager getUPManager(){
        return seqManager;
    }
    
    public SequenceManager getENSPManager(){
        return enspManager;
    }
    
    public void setSeqSelection(int start, int end){
        CursorPanel[] cursors = seqRenderer.getCursorPanels();
        for (int i=0 ; i < cursors.length ; i++) {
            cursors[i].setSeqSelection(start,end);
        }       
        
    }
    
    
    
    
    /** test if a server is a UniProt vs PDBresnum alignment server
     * 
     * @param sources
     * @param cs1
     * @param cs2
     * @return an array of SpiceDasSources
     */
    public SpiceDasSource[] getAlignmentServers(List sources,
            DasCoordinateSystem cs1, 
            DasCoordinateSystem cs2) {
        
        List aligservers = new ArrayList();
        //DasCoordinateSystem[] coordsys = source.getCoordinateSystem() ;
        for ( int i = 0; i< sources.size();i++){
            
            if ( ! hasCapability("alignment", (Das1Source)sources.get(i)))
                continue;
            SpiceDasSource source = (SpiceDasSource)sources.get(i);
            
            boolean uniprotflag = false ;
            boolean pdbflag     = false ;
            
            pdbflag     =  hasCoordSys(cs1,source) ;
            uniprotflag =  hasCoordSys(cs2,source)   ;
            //System.out.println(pdbflag + " " + uniprotflag);
            if (( uniprotflag == true) && ( pdbflag == true)) {
                aligservers.add(source);
            }
            
        }
        
        return (SpiceDasSource[]) aligservers.toArray(new SpiceDasSource[aligservers.size()]);
    }
    
    private boolean hasCoordSys(DasCoordinateSystem cs,SpiceDasSource source ) {
        DasCoordinateSystem[] coordsys = source.getCoordinateSystem() ;
        for ( int i = 0 ; i< coordsys.length; i++ ) {
            DasCoordinateSystem thiscs  =  coordsys[i];
            //System.out.println("comparing " + cs.toString() + " " + thiscs.toString());
            if ( cs.toString().equals( thiscs.toString()) ) {
                //System.out.println("match");
                return true ;
            }
            
        }
        return false ;
        
    }
    
    
    private DasSource[] getServers(String capability, DasCoordinateSystem coordSys){
        // get all servers with a particular capability
        Das1Source servers[] = getServers(capability);
        
        // now also check the coordinate system
        ArrayList retservers = new ArrayList();
        for ( int i = 0 ; i < servers.length ; i++ ) {
            SpiceDasSource ds = SpiceDasSource.fromDasSource((DasSource)servers[i]);
            
            if ( hasCoordSys(coordSys,ds)) {
                retservers.add(ds);
            }    
        }
        
        return (DasSource[])retservers.toArray(new Das1Source[retservers.size()]) ;
    }
    
    
    private boolean hasCapability(String capability, Das1Source ds){
        String[] capabilities = ds.getCapabilities() ;
        for ( int c=0; c<capabilities.length ;c++) {
            String capabil = capabilities[c];
            if ( capability.equals(capabil)){
                return true;
            }
        }
        return false;
    }
    
    private Das1Source[] getServers(String capability) {
        ArrayList retservers = new ArrayList();
        for ( int i = 0 ; i < allsources.size() ; i++ ) {
            Das1Source ds = (Das1Source) allsources.get(i);
            if ( hasCapability(capability,ds)){
                
                retservers.add(ds);
                
            }
        }
        
        
        return (Das1Source[])retservers.toArray(new Das1Source[retservers.size()]) ;
    }
    
    public void loadingFinished(DasSourceEvent ds) {
        logger.info("loading finished");
        
    }
    
    public void loadingStarted(DasSourceEvent ds) {
        
    }
    
    
}

class MyPropertyChangeListener 
implements PropertyChangeListener{
    AlignmentRenderer re;
    BrowserPane parent;
    JSplitPane split;
    String position;
    public MyPropertyChangeListener( AlignmentRenderer re, BrowserPane par, JSplitPane split, String position){
        this.re=re;
        this.parent = par;
        this.split = split;
        this.position = position;
        
    }
    public void propertyChange(PropertyChangeEvent e) {
        
        Number newH = (Number) e.getNewValue();
        
        int sH = split.getHeight();
        
        int preferredH = sH - newH.intValue();
        if ( position.equals("top")) {
            
            preferredH = newH.intValue();
        } else {
            
        }
        
        re.setPreferredSize(new Dimension(re.getWidth(),preferredH));
        re.setSize(new Dimension(re.getWidth(),preferredH));
        
        parent.repaint();
        parent.revalidate();
        
        
        re.repaint();
        re.revalidate();
        
        re.updatePanelPositions();
        
        //System.out.println("new height:position changed " + re.getHeight());
    }
}
