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

import java.net.URL;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;

import java.util.*;
import java.util.logging.Logger;

import org.biojava.spice.das.SpiceDasSource;
import org.biojava.spice.manypanel.eventmodel.*;
import org.biojava.spice.manypanel.managers.*;
import org.biojava.spice.manypanel.renderer.*;

import org.biojava.services.das.registry.DasCoordinateSystem;
import org.biojava.services.das.registry.DasSource;

import javax.swing.BoxLayout;
import java.awt.Dimension;
import java.awt.event.MouseListener;

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
    
    public BrowserPane(String PDBCOORDSYS, String UNIPROTCOORDSYS, String ENSPCOORDSYS) {
        super();
        JPanel contentPanel = new JPanel();
        
        contentPanel.setLayout(new BoxLayout(contentPanel,BoxLayout.X_AXIS));
        
        //JScrollPane scroll = new JScrollPane(contentPanel);
        //Dimension d = new Dimension(DEFAULT_PANE_WIDTH,DEFAULT_PANE_HEIGHT);
      
        //contentPanel.setPreferredSize(d);
        //contentPanel.setSize(d);
        
        //scroll.setPreferredSize(d);
        //scroll.setSize(d);
        
        //this.add(scroll);
        
        this.setOpaque(true);
        
        structureListeners = new ArrayList();
        uniProtListeners   = new ArrayList();
        enspListeners      = new ArrayList();
        allsources         = new ArrayList();
        
       
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
        strucManager = new StructureManager();
        addStructureListener(strucManager);
        
        structureRenderer = new StructureRenderer(); 
        structureRenderer.getStatusPanel().setName("PDB");
        ComponentResizedChainListener strucComponentWidthSetter = new ComponentResizedChainListener(structureRenderer);
        this.addComponentListener(strucComponentWidthSetter);
        
        //JScrollPane structureScroller = new JScrollPane(structureRenderer);
        //structureScroller.getVerticalScrollBar().setUnitIncrement(FeaturePanel.DEFAULT_Y_STEP);
        
        
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
         ComponentResizedChainListener seqComponentWidthSetter = new ComponentResizedChainListener(seqRenderer);
         this.addComponentListener(seqComponentWidthSetter);
         
         
         //JScrollPane seqScroller = new JScrollPane(seqRenderer);
         //seqScroller.getVerticalScrollBar().setUnitIncrement(FeaturePanel.DEFAULT_Y_STEP);
         //seqRenderer.setLayout(new BoxLayout(seqRenderer,BoxLayout.Y_AXIS));
         //box.add(seqScroller);  
         
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
         
         SequenceListener pdbList = aligManager.getSeq1Listener();
         SequenceListener upList = aligManager.getSeq2Listener();
         
         strucManager.addSequenceListener(pdbList);
         seqManager.addSequenceListener(upList);
         
         structureRenderer.addSequenceListener(pdbList);
         seqRenderer.addSequenceListener(upList);
         
         // get the alignment das source
         
         
         aligManager.addObject1Listener(strucManager);
         aligManager.addObject2Listener(seqManager);
         
         aligManager.addSequence1Listener(structureRenderer.getCursorPanel());         
         aligManager.addSequence2Listener(seqRenderer.getCursorPanel());
         
         ///////////////
         // now add the ENSP
         //////////////
         

         enspManager = new SequenceManager();
         addEnspListener(enspManager);
         
          enspRenderer = new SequenceRenderer();
          enspRenderer.getStatusPanel().setName("ENSP");
          ComponentResizedChainListener enspComponentWidthSetter = new ComponentResizedChainListener(enspRenderer);
          this.addComponentListener(enspComponentWidthSetter);
          
          //JScrollPane enspScroller = new JScrollPane(enspRenderer);
          //enspScroller.getVerticalScrollBar().setUnitIncrement(FeaturePanel.DEFAULT_Y_STEP);
          //enspRenderer.setLayout(new BoxLayout(enspRenderer,BoxLayout.Y_AXIS));
          //box.add(enspScroller);  
       
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
          
          ensaligManager.addSequence1Listener(seqRenderer.getCursorPanel());
          ensaligManager.addSequence2Listener(enspRenderer.getCursorPanel());
          ensaligManager.addSequence1Listener(upList);
          
          aligManager.addSequence2Listener(upenspList);
        //browserPane.addPane(structureSequencePane);
        
        
          // reset all the DAS sources...
          clearDasSources();
          
          
        //
        // build up the display from the components:
        //
          
          
        JSplitPane split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,structureRenderer,seqRenderer);
        split1.setOneTouchExpandable(true);
        // uniprot panel gets a little more space, because so many more DAS sources...
        split1.setResizeWeight(0.4);
        
        JSplitPane split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,split1,enspRenderer);
        split2.setOneTouchExpandable(true);
        // uniprot panel gets more space
        split2.setResizeWeight(0.7);
        
        //Dimension d = new Dimension(DEFAULT_PANE_WIDTH,DEFAULT_PANE_HEIGHT);
        //split2.setPreferredSize(d);
        contentPanel.add(split2);
         
        
        /// and now ...
        
        // the scale ...
        int RES_MIN  = 1;
        int RES_MAX  = 100;
        int RES_INIT = 100;
        JSlider residueSizeSlider = new JSlider(JSlider.HORIZONTAL,
                RES_MIN, RES_MAX, RES_INIT);
        residueSizeSlider.setInverted(true);
        //residueSizeSlider.setMajorTickSpacing(5);
        //residueSizeSlider.setMinorTickSpacing(2);
        residueSizeSlider.setPaintTicks(false);
        residueSizeSlider.setPaintLabels(false);
        residueSizeSlider.addChangeListener(this);
        residueSizeSlider.setPreferredSize(new Dimension(100,10));
        Box hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalGlue());
        hBox.add(residueSizeSlider);
        hBox.add(Box.createHorizontalGlue());
        
        this.add(contentPanel);
        this.add(hBox);
        
        
    }

    public void stateChanged(ChangeEvent e) {
        
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
            //System.out.println("slider at " +source.getValue());
            int residueSize = (int)source.getValue();
            structureRenderer.calcScale(residueSize);
            seqRenderer.calcScale(residueSize);
            enspRenderer.calcScale(residueSize);
            //this.repaint();
            //this.revalidate();
            //this.updateUI();
            //int width = getTotalWidth();
            //int height = getTotalHeight();
            //Dimension d = new Dimension(width,height);
            //logger.info("setting preferred size" + width + " " + height);
            //this.setPreferredSize(d);
            //this.setSize(d);
        }
    }
    
       

    

    public void disableDasSource(DasSourceEvent dsEvent) {
      
        
    }

    public void enableDasSource(DasSourceEvent dsEvent) {
        
        
    }

    public void newDasSource(DasSourceEvent dsEvent) {
    
        //DrawableDasSource dds = dsEvent.getDasSource();
        // filer ds
        //SpiceDasSource ds = dds.getDasSource();
        //DasCoordinateSystem[] css =  ds.getCoordinateSystem();
        
        // TODO: enable and filter the new DAS source in the appropriate Manager
        allsources.add(dsEvent.getDasSource().getDasSource());
    }
    
    
    /** set all das sources at once
     * 
     * @param sources
     */
    public void setDasSources(SpiceDasSource[] sources){
        
        // clear the das sources ...
        clearDasSources();
        
        
        for (int i = 0 ; i< sources.length;i++){
            SpiceDasSource ds = sources[i];
            allsources.add(ds);
        }
        
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
        
    }

    
    public void clearDasSources() {
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
    
    public void addSpiceFeatureListener(SpiceFeatureListener li){
        ChainRendererMouseListener mouser = structureRenderer.getChainRendererMouseListener();
        mouser.addSpiceFeatureListener(li);
        
        ChainRendererMouseListener seqmouser = seqRenderer.getChainRendererMouseListener();
        seqmouser.addSpiceFeatureListener(li);
        
        ChainRendererMouseListener enspmouser = enspRenderer.getChainRendererMouseListener();
        enspmouser.addSpiceFeatureListener(li);
    }
    
    public void addStructureListener(StructureListener li){
        structureListeners.add(li);        
        strucManager.addStructureListener(li);
    }
    
    public void addPDBSequenceListener(SequenceListener li){
        strucManager.addSequenceListener(li);
    }
    
    public void addUniProtListener(ObjectListener li){
        uniProtListeners.add(li);
        
    }
    public void addUniProtSequenceListener(SequenceListener li ){
        seqManager.addSequenceListener(li);
    }
    
    public void addEnspListener(ObjectListener li){
        enspListeners.add(li);
    }
    public void addEnspSeqeuenceListener(SequenceListener li){
        enspManager.addSequenceListener(li);
    }
    
    public void addDasSourceListener(DasSourceListener li){
         pdbFeatureManager.addDasSourceListener(li);
         upFeatureManager.addDasSourceListener(li);
         enspFeatureManager.addDasSourceListener(li);
    }
    
    
    
    
    public void triggerLoadStructure(String pdbcode){
        
            Iterator iter = structureListeners.iterator();
            while (iter.hasNext()){
                ObjectListener li = (ObjectListener) iter.next();
                li.newObjectRequested(pdbcode);
            }
    }
    
    public void triggerLoadUniProt(String accessionCode){
        Iterator iter = uniProtListeners.iterator();
        while (iter.hasNext()){
            ObjectListener li = (ObjectListener) iter.next();
            li.newObjectRequested(accessionCode);
        }
    }
    
    public void triggerLoadENSP(String enspCode){
        Iterator iter = enspListeners.iterator();
        while (iter.hasNext()){
            ObjectListener li = (ObjectListener) iter.next();
            li.newObjectRequested(enspCode);
        }
    }
    

   public StructureListener getStructureListener(){
       return strucManager;
       
   }
    
    
    /** test if a server is a UniProt vs PDBresnum alignment server */
    public SpiceDasSource[] getAlignmentServers(List sources,
            DasCoordinateSystem cs1, 
            DasCoordinateSystem cs2) {
        
        List aligservers = new ArrayList();
        //DasCoordinateSystem[] coordsys = source.getCoordinateSystem() ;
        for ( int i = 0; i< sources.size();i++){
            
            if ( ! hasCapability("alignment", (DasSource)sources.get(i)))
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
        DasSource servers[] = getServers(capability);
        
        // now also check the coordinate system
        ArrayList retservers = new ArrayList();
        for ( int i = 0 ; i < servers.length ; i++ ) {
            SpiceDasSource ds = SpiceDasSource.fromDasSource((DasSource)servers[i]);
            
            if ( hasCoordSys(coordSys,ds)) {
                retservers.add(ds);
            }    
        }
        
        return (DasSource[])retservers.toArray(new DasSource[retservers.size()]) ;
    }
    
    
    private boolean hasCapability(String capability, DasSource ds){
        String[] capabilities = ds.getCapabilities() ;
        for ( int c=0; c<capabilities.length ;c++) {
            String capabil = capabilities[c];
            if ( capability.equals(capabil)){
                return true;
            }
        }
        return false;
    }
    
    private DasSource[] getServers(String capability) {
        ArrayList retservers = new ArrayList();
        for ( int i = 0 ; i < allsources.size() ; i++ ) {
            DasSource ds = (DasSource) allsources.get(i);
            if ( hasCapability(capability,ds)){
          
            
                retservers.add(ds);
                  
                    
            }
        }
        
        
        return (DasSource[])retservers.toArray(new DasSource[retservers.size()]) ;
    }

    public void loadingFinished(DasSourceEvent ds) {
       logger.info("loading finished");
        
    }

    public void loadingStarted(DasSourceEvent ds) {
        // TODO Auto-generated method stub
        
    }
    
    /*
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        
        g.drawString("browserPane",10,10);
        
    }*/
    
}
