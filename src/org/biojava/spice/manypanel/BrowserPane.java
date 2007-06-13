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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;

import java.awt.Color;
import java.util.*;
import java.util.logging.Logger;

import org.biojava.dasobert.dasregistry.Das1Source;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;
import org.biojava.dasobert.eventmodel.*;
import org.biojava.spice.ResourceManager;
import org.biojava.spice.config.SpiceDefaults;
import org.biojava.spice.das.SpiceDasSource;
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
    
    static Logger logger = Logger.getLogger(SpiceDefaults.LOGGER);
    
     
    
    List allsources ; // a list of DasSource[] 
    
    List structureListeners;
    List uniProtListeners;
    List enspListeners;
    StructureRenderer renderer_Pdb ;
    SequenceRenderer renderer_UP;
    SequenceRenderer renderer_Ensp;
    
    int storeStart;
    int storeEnd;
    
    StructureManager strucManager;
    SequenceManager seqManager;
    SequenceManager enspManager;
    
    AlignmentManager aligManager_PdbUp;
    AlignmentManager aligManager_UpEnsp;
    
    AlignmentRenderer aligRenderer_PdbUp;
    AlignmentRenderer aligRenderer_UpEnsp;
    
    FeatureManager pdbFeatureManager;
    FeatureManager upFeatureManager;
    FeatureManager enspFeatureManager;
        
    
    //static Color BG_COLOR = Color.WHITE;
    JPanel contentPanel;
    
    
    JSlider residueSizeSlider;
    JLabel  percentageDisplay;
    /** create a BrowserPane that only has a structure Panel
     * 
     * @param PDBCOORDSYS
     */
    public BrowserPane(String PDBCOORDSYS){
        
    	initPanels(PDBCOORDSYS, SpiceDefaults.UNIPROTCOORDSYS, SpiceDefaults.ENSPCOORDSYS);
    	
        // reset all the DAS sources...
        clearDasSources();
                
        registerEventTranslators();
        
        String bgcol = ResourceManager.getString("org.biojava.spice.manypanel.renderer.BackgroundColor");
        Color BG_COLOR = Color.decode(bgcol);
        //
        // build up the display from the components:
        //
        
        
        //Dimension d = new Dimension(DEFAULT_PANE_WIDTH,DEFAULT_PANE_HEIGHT);
        //split2.setPreferredSize(d);
        contentPanel.add(renderer_Pdb);
        
        
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
        
   
        
        updatePercentageDisplay();
        
        Box hBox = Box.createHorizontalBox();
        hBox.setBackground(BG_COLOR);
        hBox.add(Box.createHorizontalGlue());
        hBox.add(residueSizeSlider);
        hBox.add(percentageDisplay);
        hBox.add(Box.createHorizontalGlue());
        
        // register the managers
        registerManagers();
        
        this.setOpaque(true);
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
        this.add(contentPanel);
        this.add(hBox);
        
    	
    }
    
    private void updatePercentageDisplay(){
    	int perc = residueSizeSlider.getValue();
    	percentageDisplay.setText(perc+ " %");
    }
    
    /** create a browserPane that has PDB,  Uniprot and ENSP panels
     * 
     * @param PDBCOORDSYS
     * @param UNIPROTCOORDSYS
     * @param ENSPCOORDSYS
     */
    public BrowserPane(String PDBCOORDSYS, String UNIPROTCOORDSYS, String ENSPCOORDSYS) {
        super();
        initPanels(PDBCOORDSYS, UNIPROTCOORDSYS, ENSPCOORDSYS);
        // reset all the DAS sources...
        clearDasSources();
        
        
        registerEventTranslators();
        
        String bgcol = ResourceManager.getString("org.biojava.spice.manypanel.renderer.BackgroundColor");
        Color BG_COLOR = Color.decode(bgcol);
        //
        // build up the display from the components:
        //
        
        
        JSplitPane splito = new JSplitPane(JSplitPane.VERTICAL_SPLIT,renderer_Pdb,aligRenderer_PdbUp);
        splito.setOneTouchExpandable(true);
        splito.setResizeWeight(1.0);
        splito.setBorder(BorderFactory.createEmptyBorder());
        
        MyPropertyChangeListener mpcl = new MyPropertyChangeListener(aligRenderer_PdbUp,this,splito,"bottom");
        splito.addPropertyChangeListener("dividerLocation", mpcl );
        
        JSplitPane split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,splito,renderer_UP);
        split1.setOneTouchExpandable(true);
        // uniprot panel gets a little more space, because so many more DAS sources...
        split1.setResizeWeight(0.5);
        
        split1.setBorder(BorderFactory.createEmptyBorder());
        
        
        
        
//      JPanel p2 = new JPanel();
//      p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
//      p2.add(enspAligRenderer);
//      p2.add(enspRenderer);
        
        JSplitPane splitb = new JSplitPane(JSplitPane.VERTICAL_SPLIT,aligRenderer_UpEnsp,renderer_Ensp);
        splitb.setOneTouchExpandable(true);
        splitb.setResizeWeight(0);
        splitb.setBorder(BorderFactory.createEmptyBorder());
        
        //MyComponentListener mycompo2 = new MyComponentListener(enspAligRenderer,enspRenderer);
        
        //splitb.addComponentListener(mycompo2);
        
        MyPropertyChangeListener mpcl2 = new MyPropertyChangeListener(aligRenderer_UpEnsp,this,splitb,"top");
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
        hBox.add(percentageDisplay);
        hBox.add(Box.createHorizontalGlue());
        
        // register the managers
        registerManagers();
        
        this.setOpaque(true);
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
        this.add(contentPanel);
        this.add(hBox);
    }
    
    private void initPanels(String PDBCOORDSYS, String UNIPROTCOORDSYS, String ENSPCOORDSYS) {
        
        // TODO: actually the initialization of all the Listeners is pretty horrible and
        // it would be nice to have something easier for doing so. It is the result of the
        // event model as being used by SPICE. AP 20070116
        
        contentPanel = new JPanel();
        percentageDisplay = new JLabel("100 %");
        String bgcol = ResourceManager.getString("org.biojava.spice.manypanel.renderer.BackgroundColor");
        Color BG_COLOR = Color.decode(bgcol);
        
        contentPanel.setLayout(new BoxLayout(contentPanel,BoxLayout.X_AXIS));
        
        structureListeners = new ArrayList();
        uniProtListeners   = new ArrayList();
        enspListeners      = new ArrayList();
        allsources         = new ArrayList();
        
        strucManager = new StructureManager();
        addStructureListener(strucManager);
        
        renderer_Pdb = new StructureRenderer();
        renderer_Pdb.getStatusPanel().setName("PDB");
        renderer_Pdb.setBackground(BG_COLOR);
        
        ComponentResizedChainListener strucComponentWidthSetter = new ComponentResizedChainListener(renderer_Pdb);
        contentPanel.addComponentListener(strucComponentWidthSetter);
        
        strucManager.addStructureRenderer(renderer_Pdb);
        
        DasCoordinateSystem dcs = DasCoordinateSystem.fromString(PDBCOORDSYS);
        strucManager.setCoordinateSystem(dcs);
        
        pdbFeatureManager = new FeatureManager();
        pdbFeatureManager.setCoordinateSystem(dcs);
        pdbFeatureManager.addDasSourceListener(renderer_Pdb);
       
        FeatureRenderer featureRenderer = new FeatureRenderer();
        pdbFeatureManager.addFeatureRenderer(featureRenderer);
        strucManager.addSequenceListener(pdbFeatureManager);
        
        
        ///////////////
        // now add the UniProt
        //////////////
        
        seqManager = new SequenceManager();
        addUniProtListener(seqManager);
        
        renderer_UP = new SequenceRenderer();
        renderer_UP.getStatusPanel().setName("UniProt");
        renderer_UP.setBackground(BG_COLOR);
        
        ComponentResizedChainListener seqComponentWidthSetter = new ComponentResizedChainListener(renderer_UP);
        contentPanel.addComponentListener(seqComponentWidthSetter);
        
        DasCoordinateSystem seqdcs = DasCoordinateSystem.fromString(UNIPROTCOORDSYS);
        seqManager.setCoordinateSystem(seqdcs);
        seqManager.addSequenceRenderer(renderer_UP);
        
        upFeatureManager = new FeatureManager();
        upFeatureManager.setCoordinateSystem(seqdcs);
        upFeatureManager.addDasSourceListener(renderer_UP);
        
        addUniProtListener(upFeatureManager);
        FeatureRenderer seqFeatureRenderer = new FeatureRenderer();
        upFeatureManager.addFeatureRenderer(seqFeatureRenderer);        
        seqManager.addSequenceListener(upFeatureManager);
        
        
        ///////////////
        // now add the Alignment PDB to UniProt
        //////////////
        
        aligManager_PdbUp = new AlignmentManager("PDB_UP",dcs,seqdcs);
        
        strucManager.addStructureListener(aligManager_PdbUp);

        SequenceListener pdbList = aligManager_PdbUp.getSeq1Listener();
        SequenceListener upList  = aligManager_PdbUp.getSeq2Listener();
       
        strucManager.addSequenceListener(pdbList);
        seqManager.addSequenceListener(upList);
        
        renderer_Pdb.addSequenceListener(pdbList);
        renderer_UP.addSequenceListener(upList);
        
        aligManager_PdbUp.addObject1Listener(strucManager);
        aligManager_PdbUp.addObject2Listener(seqManager);
        
        CursorPanel[] structureCursors =renderer_Pdb.getCursorPanels();
        for (int i = 0; i < structureCursors.length;i++){
            aligManager_PdbUp.addSequence1Listener(structureCursors[i]);
        }
        CursorPanel[] seqCursors =renderer_UP.getCursorPanels();
        for (int i = 0; i < seqCursors.length;i++){        
            aligManager_PdbUp.addSequence2Listener(seqCursors[i]);
        }
        
        aligRenderer_PdbUp = new AlignmentRenderer();
        aligManager_PdbUp.addAlignmentRenderer(aligRenderer_PdbUp);
        
        renderer_Pdb.addScaleChangeListener(aligRenderer_PdbUp.getSeq1ScaleListener());
        renderer_UP.addScaleChangeListener(aligRenderer_PdbUp.getSeq2ScaleListener());
        
        renderer_Pdb.addSequenceListener(aligRenderer_PdbUp.getSequenceListener1());
        renderer_UP.addSequenceListener(aligRenderer_PdbUp.getSequenceListener2());
        aligManager_PdbUp.addSequence1Listener(aligRenderer_PdbUp.getSequenceListener1());
        aligManager_PdbUp.addSequence2Listener(aligRenderer_PdbUp.getSequenceListener2());
        
        renderer_Pdb.addAdjustmentListener(aligRenderer_PdbUp.getAdjust1());
        renderer_UP.addAdjustmentListener(aligRenderer_PdbUp.getAdjust2());
        
        aligRenderer_PdbUp.setPreferredSize(new Dimension(400,20));
        
        
        
        ///////////////
        // now add the ENSP
        //////////////
        
        
        enspManager = new SequenceManager();
        addEnspListener(enspManager);
        
        renderer_Ensp = new SequenceRenderer();
        if ( ENSPCOORDSYS.equals(SpiceDefaults.GENCODECOORDSYS))
            renderer_Ensp.getStatusPanel().setName("GENCODE");
        else
            renderer_Ensp.getStatusPanel().setName("ENSP");
        renderer_Ensp.setBackground(BG_COLOR);
        
        ComponentResizedChainListener enspComponentWidthSetter = new ComponentResizedChainListener(renderer_Ensp);
        contentPanel.addComponentListener(enspComponentWidthSetter);
        
        
        DasCoordinateSystem enspdcs = DasCoordinateSystem.fromString(ENSPCOORDSYS);
        enspManager.setCoordinateSystem(enspdcs);
        
        
        enspManager.addSequenceRenderer(renderer_Ensp);
        
        enspFeatureManager = new FeatureManager();
        enspFeatureManager.setCoordinateSystem(enspdcs);
        enspFeatureManager.addDasSourceListener(renderer_Ensp);
        addEnspListener(enspFeatureManager);
        FeatureRenderer enspFeatureRenderer = new FeatureRenderer();
        enspFeatureManager.addFeatureRenderer(enspFeatureRenderer);
        
        
        SpiceDasSource[]enspfeatservs = getServers("features",enspdcs);
        
        
        enspFeatureManager.setDasSources(enspfeatservs);
        
        //enspManager.setFeatureManager(enspfm);
        enspManager.addSequenceListener(enspFeatureManager);
        
        
        
        ///////////////
        // now add the Alignment ENSP to UniProt
        //////////////
        
        aligManager_UpEnsp = new AlignmentManager("UP_ENSP",seqdcs,enspdcs);
        
        SequenceListener upenspList = aligManager_UpEnsp.getSeq1Listener();
        SequenceListener enspList   = aligManager_UpEnsp.getSeq2Listener();
        
        renderer_UP.addSequenceListener(upenspList);
        renderer_Ensp.addSequenceListener(enspList);
        
        //strucManager.addSequenceListener(pdbList);
        seqManager.addSequenceListener(upenspList);
        enspManager.addSequenceListener(enspList);
        
        
        
        aligManager_UpEnsp.addObject1Listener(seqManager);
        aligManager_UpEnsp.addObject2Listener(enspManager);
        
        for (int i = 0; i < seqCursors.length;i++){        
            aligManager_UpEnsp.addSequence1Listener(seqCursors[i]);
        }
        CursorPanel[] enspCursors = renderer_Ensp.getCursorPanels();
        for (int i = 0; i < enspCursors.length;i++){
            aligManager_UpEnsp.addSequence2Listener(enspCursors[i]);
        }
        aligManager_UpEnsp.addSequence1Listener(upList);
        
        aligManager_PdbUp.addSequence2Listener(upenspList);
        
        
        aligRenderer_UpEnsp = new AlignmentRenderer();
        aligManager_UpEnsp.addAlignmentRenderer(aligRenderer_UpEnsp);
        
        renderer_UP.addScaleChangeListener(aligRenderer_UpEnsp.getSeq1ScaleListener());
        renderer_Ensp.addScaleChangeListener(aligRenderer_UpEnsp.getSeq2ScaleListener());
        
        renderer_UP.addSequenceListener(aligRenderer_UpEnsp.getSequenceListener1());
        renderer_Ensp.addSequenceListener(aligRenderer_UpEnsp.getSequenceListener2());
                
        aligManager_PdbUp.addSequence2Listener(aligRenderer_UpEnsp.getSequenceListener1());
        aligManager_UpEnsp.addSequence1Listener(aligRenderer_UpEnsp.getSequenceListener1());
        aligManager_UpEnsp.addSequence2Listener(aligRenderer_UpEnsp.getSequenceListener2());
        aligManager_UpEnsp.addSequence1Listener(aligRenderer_PdbUp.getSequenceListener2());
        
        //browserPane.addPane(structureSequencePane);
        aligRenderer_UpEnsp.setPreferredSize(new Dimension(400,20));
        
        
        renderer_UP.addAdjustmentListener(aligRenderer_UpEnsp.getAdjust1());
        renderer_Ensp.addAdjustmentListener(aligRenderer_UpEnsp.getAdjust2());
        
       
        
        
    }
    

    /** make sure that feature events are correctly translated from one coord sys (panel) to another 
     * 
     *
     */ 
    private void registerEventTranslators(){

        
        ChainRendererMouseListener mouserPdb  = renderer_Pdb.getChainRendererMouseListener();
        ChainRendererMouseListener mouserUp   = renderer_UP.getChainRendererMouseListener();
        ChainRendererMouseListener mouserEnsp = renderer_Ensp.getChainRendererMouseListener();
        
        // for selection of the whole feature
        RowHeaderMouseListener upRowHeader   = renderer_UP.getRowHeaderListener();
        RowHeaderMouseListener pdbRowHeader  = renderer_Pdb.getRowHeaderListener();
        RowHeaderMouseListener enspRowHeader = renderer_Ensp.getRowHeaderListener();
        
        SpiceFeatureListener li1 = aligManager_PdbUp.getFeatureTranslator1();
        SpiceFeatureListener li2 = aligManager_PdbUp.getFeatureTranslator2();
        
        SpiceFeatureListener li3 = aligManager_UpEnsp.getFeatureTranslator1();
        SpiceFeatureListener li4 = aligManager_UpEnsp.getFeatureTranslator2();
        
        
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
        aligManager_PdbUp.addSeq2FeatureListener(li3);
//               
        aligManager_UpEnsp.addSeq1FeatureListener(li2);        
//        ensaligManager.addSeq2FeatureListener(li4);
    }
    
    /** add the managers to the ArrowPanel of the statuspanels...
     * 
     *
     */
    private void registerManagers(){
        
        
        ArrowPanel a2 = renderer_UP.getArrowPanel();
        a2.setUpperAlignmentManager(aligManager_PdbUp);
        a2.setLowerAlignmentManager(aligManager_PdbUp);
        a2.setUpperObjectListener(strucManager);
        a2.setLowerObjectListener(seqManager);
        
        
        ArrowPanel a3 = renderer_Ensp.getArrowPanel();
        
        a3.setUpperAlignmentManager(aligManager_UpEnsp);
        a3.setLowerAlignmentManager(aligManager_UpEnsp);
        a3.setUpperObjectListener(seqManager);
        a3.setLowerObjectListener(enspManager);
        
     
        
        
    }
    
    public void stateChanged(ChangeEvent e) {
        
        JSlider source = (JSlider)e.getSource();
        //if (!source.getValueIsAdjusting()) {
        //System.out.println("slider at " +source.getValue());
        int residueSize = (int)source.getValue();
        renderer_Pdb.calcScale(residueSize);
        renderer_UP.calcScale(residueSize);
        renderer_Ensp.calcScale(residueSize);
        contentPanel.repaint();
        updatePercentageDisplay();
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
        clearDasSources();       
         
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
        SpiceDasSource[]strucservs = getServers("structure",dcs);
        strucManager.setDasSources(strucservs);
        
        SpiceDasSource[]seqservs = getServers("sequence",seqdcs);        
        seqManager.setDasSources(seqservs);
        
        SpiceDasSource[]enspservs = getServers("sequence",enspdcs);        
        enspManager.setDasSources(enspservs);        
        
        // set the annotation servers
        
        SpiceDasSource[]featservs = getServers("features",dcs);
        pdbFeatureManager.setDasSources(featservs);
        
        
        SpiceDasSource[]seqfeatservs = getServers("features",seqdcs);  
        //logger.info("setting UniProt DAS servers: " + seqfeatservs.length);
        //for (int i=0 ; i < 3 && i < seqfeatservs.length ; i ++){
        //	logger.info("setting UP das source " + seqfeatservs[i].getNickname() + " " + seqfeatservs[i].getDisplayType());
        //}
        upFeatureManager.setDasSources(seqfeatservs);
        
        SpiceDasSource[] enspfeatservs = getServers("features",enspdcs);
        enspFeatureManager.setDasSources(enspfeatservs);
        
        SpiceDasSource[] strucaligs = getAlignmentServers(allsources,dcs,seqdcs);
        aligManager_PdbUp.setAlignmentServers(strucaligs);
        
        // get the alignment das source
        SpiceDasSource[] enspupaligs = getAlignmentServers(allsources,seqdcs,enspdcs);
        aligManager_UpEnsp.setAlignmentServers(enspupaligs);
        
        this.repaint();
        
    }
    
    private void triggerRemoveDasSource(SpiceDasSource ds){
        logger.finest("triggerRemoveDasSource " + ds.getNickname());
        
        DasSourceEvent event = new DasSourceEvent(DrawableDasSource.fromDasSource(ds));
        
        DasCoordinateSystem[] cs = ds.getCoordinateSystem();
        for ( int i=0 ; i< cs.length; i++){
            DasCoordinateSystem dcs = cs[i];
            if ( dcs.toString().equals(SpiceDefaults.PDBCOORDSYS)){
                // remove from structure panel
                renderer_Pdb.removeDasSource(event);
            } else if ( dcs.toString().equals(SpiceDefaults.UNIPROTCOORDSYS))
                renderer_UP.removeDasSource(event);
            else if ( dcs.toString().equals(SpiceDefaults.ENSPCOORDSYS))
                renderer_Ensp.removeDasSource(event);
        }
        
    }
    
    public  void clearDasSources() {
        logger.info("browserPane clear das sources");
        allsources = new ArrayList();
        aligManager_UpEnsp.clearDasSources();
        aligManager_PdbUp.clearDasSources();
        upFeatureManager.clearDasSources();
        enspManager.clearDasSources();
        seqManager.clearDasSources();
        pdbFeatureManager.clearDasSources();
        strucManager.clearDasSources();
        
        
        
    }
    
    
    public void selectedDasSource(DasSourceEvent ds) {
        
        
    }
    
    
    public void addPDBPositionListener(SequenceListener li){
        ChainRendererMouseListener mouser = renderer_Pdb.getChainRendererMouseListener();
        mouser.addSequenceListener(li);
        aligManager_PdbUp.addSequence1Listener(li);
    }
    
    public void addPDBSpiceFeatureListener(SpiceFeatureListener li){
        // now done by renderer...
        //ChainRendererMouseListener mouser = structureRenderer.getChainRendererMouseListener();
        //mouser.addSpiceFeatureListener(li);        
        renderer_Pdb.addSpiceFeatureListener(li);
        aligManager_PdbUp.addSeq1FeatureListener(li);
        
    
        
    }
    
    public void addUniProtSpiceFeatureListener(SpiceFeatureListener li){
        ChainRendererMouseListener seqmouser = renderer_UP.getChainRendererMouseListener();
        seqmouser.addSpiceFeatureListener(li);
        
        aligManager_PdbUp.addSeq2FeatureListener(li);
        aligManager_UpEnsp.addSeq1FeatureListener(li);
        
    }
    
    public void addEnspSpiceFeatureListener(SpiceFeatureListener li){
        ChainRendererMouseListener enspmouser = renderer_Ensp.getChainRendererMouseListener();
        enspmouser.addSpiceFeatureListener(li);
        aligManager_UpEnsp.addSeq2FeatureListener(li);
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
        aligManager_PdbUp.addSequence1Listener(li);
    }
    
    public SequenceListener[] getPDBSequenceListener(){
        CursorPanel[] cursors = renderer_Pdb.getCursorPanels();
        SequenceListener[] sli = new SequenceListener[cursors.length + 1];
        for ( int i=0;i < cursors.length;i++){
            sli[i] = cursors[i];
            
        }
        sli[cursors.length] = aligManager_PdbUp.getSeq1Listener();
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
        
        aligManager_PdbUp.clearAlignment();
        aligManager_UpEnsp.clearAlignment();
        
        
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
        return aligManager_PdbUp;
    }
    public AlignmentManager getBottomAlignmentManager(){
        return aligManager_UpEnsp;
    }
    
    
    public void clearDisplay(){
        
        aligManager_PdbUp.clearAlignment();
        
        
        aligManager_UpEnsp.clearAlignment();
        
        strucManager.clear();
        
        renderer_Pdb.clearDisplay();
        renderer_UP.clearDisplay();
        renderer_Ensp.clearDisplay();
        
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
        
        renderer_Pdb.clearListeners();
        renderer_UP.clearListeners();
        renderer_Ensp.clearListeners();
        
        
    }
    
    public StructureManager getStructureManager(){
        return strucManager;
        
    }
    
    public StructureRenderer getStructureRenderer(){
        return renderer_Pdb;
    }
    
    public SequenceManager getUPManager(){
        return seqManager;
    }
    
    public SequenceManager getENSPManager(){
        return enspManager;
    }
    
    public void setSeqSelection(int start, int end){
        CursorPanel[] cursors = renderer_UP.getCursorPanels();
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
    
    
    private SpiceDasSource[] getServers(String capability, DasCoordinateSystem coordSys){
        // get all servers with a particular capability
        SpiceDasSource[] servers = getServers(capability);
        
        // now also check the coordinate system
        ArrayList retservers = new ArrayList();
        for ( int i = 0 ; i < servers.length ; i++ ) {
        	
            //SpiceDasSource ds = SpiceDasSource.fromDasSource((DasSource)servers[i]);
            SpiceDasSource ds = servers[i];
            
                        
            if ( hasCoordSys(coordSys,ds)) {
                retservers.add(ds);
            }    
        }
        
        return (SpiceDasSource[])retservers.toArray(new SpiceDasSource[retservers.size()]) ;
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
    
    private SpiceDasSource[] getServers(String capability) {
        ArrayList retservers = new ArrayList();
        for ( int i = 0 ; i < allsources.size() ; i++ ) {
            SpiceDasSource ds = (SpiceDasSource) allsources.get(i);
            if ( hasCapability(capability,ds)){
                
                retservers.add(ds);
                
            }
        }
                
        return (SpiceDasSource[])retservers.toArray(new SpiceDasSource[retservers.size()]) ;
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
