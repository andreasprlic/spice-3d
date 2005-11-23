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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;

import java.util.*;
import java.util.logging.Logger;

import org.biojava.spice.Config.*;
//import org.biojava.spice.manypanel.drawable.*;
import org.biojava.spice.manypanel.eventmodel.*;
import org.biojava.spice.manypanel.managers.*;
import org.biojava.spice.manypanel.renderer.*;

import org.biojava.services.das.registry.DasCoordinateSystem;
import org.biojava.services.das.registry.DasRegistryAxisClient;
import org.biojava.services.das.registry.DasSource;
import java.awt.Dimension;

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
    
    public static  String registry = "http://servlet.sanger.ac.uk/dasregistry/services/das_registry";
    //public static  String registry = "http://www.spice-3d.org/dasregistry/services/das_registry";
    
    public static String PDBCOORDSYS     = "PDBresnum,Protein Structure";
    public static String UNIPROTCOORDSYS = "UniProt,Protein Sequence";
    public static String ENSPCOORDSYS    = "Ensembl,Protein Sequence";
 
    DasSource[] allsources ;
    List structureListeners;
    List uniProtListeners;
    List enspListeners;
    StructureRenderer structureRenderer ;
    SequenceRenderer seqRenderer;
    SequenceRenderer enspRenderer;
    public static int DEFAULT_PANE_WIDTH = 600;
    public static int DEFAULT_PANE_HEIGHT = 600;
    
    public BrowserPane() {
        super();
        JPanel contentPanel = new JPanel();
        JScrollPane scroll = new JScrollPane(contentPanel);
        Dimension d = new Dimension(DEFAULT_PANE_WIDTH,DEFAULT_PANE_HEIGHT);
        scroll.setPreferredSize(d);
        scroll.setSize(d);
        
        this.add(scroll);
        
        this.setOpaque(true);
        
        structureListeners = new ArrayList();
        uniProtListeners   = new ArrayList();
        enspListeners      = new ArrayList();
        
        try {
            allsources = getAllDasSources();
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
        logger.info("got " + allsources.length + " das sources");
        //this.setOpaque(true);
        
        Box box = Box.createVerticalBox();
        
        StructureManager strucManager = new StructureManager();
        addStructureListener(strucManager);
        
        structureRenderer = new StructureRenderer();   
                
        //this.setBounds(0,0,200,300);
        //structureRenderer.setBounds(0,0,800,800);
        //structureRenderer.setPreferredSize(new Dimension(400,400));
        box.add(structureRenderer);
        contentPanel.add(box);  
        //setBounds(0,0,200,300);
        //this.setPreferredSize(new Dimension(1000,1000));
        strucManager.addStructureRenderer(structureRenderer);
       
        DasCoordinateSystem dcs = DasCoordinateSystem.fromString(PDBCOORDSYS);
        strucManager.setCoordinateSystem(dcs);
        
        DasSource[]strucservs = getServers("structure",PDBCOORDSYS);
        strucManager.setDasSources(strucservs);
        
                
        FeatureManager fm = new FeatureManager();
        fm.setCoordinateSystem(dcs);
        fm.addDasSourceListener(structureRenderer);
        //addStructureListener(fm);
        FeatureRenderer featureRenderer = new FeatureRenderer();
        fm.addFeatureRenderer(featureRenderer);
        
        
        DasSource[]featservs = getServers("features",PDBCOORDSYS);
        fm.setDasSources(featservs);
        //fm.addDasSourceListener(structureRenderer);
        
        //structureRenderer.addFeatureRenderer(featureRenderer);
        
        
        // link the feature manager to the StructureManager        
        //strucManager.setFeatureManager(fm);
        strucManager.addSequenceListener(fm);
        //structureSequencePane.set
      
        

        ///////////////
        // now add the UniProt
        //////////////
        
        SequenceManager seqManager = new SequenceManager();
        addUniProtListener(seqManager);
        
         seqRenderer = new SequenceRenderer();
         box.add(seqRenderer);  
         
         DasCoordinateSystem seqdcs = DasCoordinateSystem.fromString(UNIPROTCOORDSYS);
         seqManager.setCoordinateSystem(seqdcs);
         
         DasSource[]seqservs = getServers("sequence",UNIPROTCOORDSYS);
        
         seqManager.setDasSources(seqservs);
         seqManager.addSequenceRenderer(seqRenderer);
                 
         FeatureManager seqfm = new FeatureManager();
         seqfm.setCoordinateSystem(seqdcs);
         seqfm.addDasSourceListener(seqRenderer);
         addUniProtListener(seqfm);
         FeatureRenderer seqFeatureRenderer = new FeatureRenderer();
         seqfm.addFeatureRenderer(seqFeatureRenderer);
         
         
         DasSource[]seqfeatservs = getServers("features",UNIPROTCOORDSYS);
         //TODO remove debug
         //DasSource[] ss = new DasSource[1];
         //ss[0]=seqfeatservs[0];
         
         seqfm.setDasSources(seqfeatservs);
         
         //seqManager.setFeatureManager(seqfm);
         seqManager.addSequenceListener(seqfm);
         
         
         ///////////////
         // now add the Alignment PDB to UniProt
         //////////////
         
         AlignmentManager aligManager = new AlignmentManager("PDB_UP",dcs,seqdcs);
         
         SequenceListener pdbList = aligManager.getSeq1Listener();
         SequenceListener upList = aligManager.getSeq2Listener();
         
         strucManager.addSequenceListener(pdbList);
         seqManager.addSequenceListener(upList);
         
         structureRenderer.addSequenceListener(pdbList);
         seqRenderer.addSequenceListener(upList);
         
         // get the alignment das source
         SpiceDasSource[] strucaligs = getAlignmentServers(allsources,dcs,seqdcs);
         aligManager.setAlignmentServers(strucaligs);
         
         aligManager.addObject1Listener(strucManager);
         aligManager.addObject2Listener(seqManager);
         
         ///////////////
         // now add the ENSP
         //////////////
         

         SequenceManager enspManager = new SequenceManager();
         addEnspListener(enspManager);
         
          enspRenderer = new SequenceRenderer();
          box.add(enspRenderer);  
       
          DasCoordinateSystem enspdcs = DasCoordinateSystem.fromString(ENSPCOORDSYS);
          enspManager.setCoordinateSystem(enspdcs);
          
          DasSource[]enspservs = getServers("sequence",ENSPCOORDSYS);
         
          enspManager.setDasSources(enspservs);
          enspManager.addSequenceRenderer(enspRenderer);
                  
          FeatureManager enspfm = new FeatureManager();
          enspfm.setCoordinateSystem(enspdcs);
          enspfm.addDasSourceListener(enspRenderer);
          addEnspListener(enspfm);
          FeatureRenderer enspFeatureRenderer = new FeatureRenderer();
          enspfm.addFeatureRenderer(enspFeatureRenderer);
          
          
          DasSource[]enspfeatservs = getServers("features",ENSPCOORDSYS);
         
          
          enspfm.setDasSources(enspfeatservs);
          
          //enspManager.setFeatureManager(enspfm);
          enspManager.addSequenceListener(enspfm);
         
         
          
          ///////////////
          // now add the Alignment ENSP to UniProt
          //////////////
          
          AlignmentManager ensaligManager = new AlignmentManager("UP_ENSP",seqdcs,enspdcs);
          
          SequenceListener upenspList = ensaligManager.getSeq1Listener();
          SequenceListener enspList   = ensaligManager.getSeq2Listener();
          
          seqRenderer.addSequenceListener(upenspList);
          enspRenderer.addSequenceListener(enspList);
          
          //strucManager.addSequenceListener(pdbList);
          seqManager.addSequenceListener(upenspList);
          enspManager.addSequenceListener(enspList);
          
          // get the alignment das source
          SpiceDasSource[] enspupaligs = getAlignmentServers(allsources,seqdcs,enspdcs);
          ensaligManager.setAlignmentServers(enspupaligs);
          
          ensaligManager.addObject1Listener(seqManager);
          ensaligManager.addObject2Listener(enspManager);
          
          
        //browserPane.addPane(structureSequencePane);
        
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
        this.add(residueSizeSlider);
        
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
        
        
    }

    public void selectedDasSource(DasSourceEvent ds) {
       
        
    }
    
    public void addStructureListener(ObjectListener li){
        structureListeners.add(li);
    }
    
    public void addUniProtListener(ObjectListener li){
        uniProtListeners.add(li);
    }
    public void addEnspListener(ObjectListener li){
        enspListeners.add(li);
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
    

    public DasSource[] getAllDasSources() throws Exception{
       
        URL rurl = new URL(registry);
        DasRegistryAxisClient rclient = new DasRegistryAxisClient(rurl);
        DasSource[]  allsources = rclient.listServices();
        return allsources;
    }
    
    //public void initData() {
//      String registry = args[0] ;
        
        //String registry = "http://www.spice-3d.org/dasregistry/services/das_registry";
        //String registry = "http://localhost:8080/dasregistry/services/das_registry";
        //String upcode = "P00280";
        //String upcode = "P08045";
        //String upcode = "P50225";
        //String upcode = "P43379";
        
        /*
         System.setProperty("proxySet","true");
         String proxyname = "localhost";
         System.setProperty("proxyHost",proxyname);
         System.setProperty("http.proxyHost",proxyname);
         System.setProperty("proxyPort","3128");
         System.setProperty("http.proxyPort","3128");
         
        URL rurl = new URL(registry);
        DasRegistryAxisClient rclient = new DasRegistryAxisClient(rurl);
        
        
        DasSource[]seqservs = getServers("sequence",UNIPROTCOORDSYS);
        DasSource seqs = seqservs[0];
        String url = seqs.getUrl();
        char lastChar = url.charAt(url.length()-1);      
        if ( ! (lastChar == '/') ) 
            url +="/" ;
        
        String scmd  = url + "sequence?segment=" + upcode;
        
   
        DasSource[] featservs   = getServers("features",UNIPROTCOORDSYS);
        DasSource[] pdbresservs = getServers("features",PDBCOORDSYS);
        
        System.out.println("found " + featservs.length   + "UniProt DAS sources");
        System.out.println("found " + pdbresservs.length + "PDB DAS sources");
        */
    //}
    
    private boolean hasCoordSys(String coordSys,DasSource source ) {
        DasCoordinateSystem[] coordsys = source.getCoordinateSystem() ;
        for ( int i = 0 ; i< coordsys.length; i++ ) {
            String c = coordsys[i].toString();
            //System.out.println(">"+c+"< >"+coordSys+"<");
            if ( c.equals(coordSys) ) {
                //System.out.println("match");
                return true ;
            }
            
        }
        return false ;
        
    }
    
    
    
    
    /** test if a server is a UniProt vs PDBresnum alignment server */
    public SpiceDasSource[] getAlignmentServers(DasSource[] sources,
            DasCoordinateSystem cs1, 
            DasCoordinateSystem cs2) {
        
        List aligservers = new ArrayList();
        //DasCoordinateSystem[] coordsys = source.getCoordinateSystem() ;
        for ( int i = 0; i< sources.length;i++){
            
            if ( ! hasCapability("alignment", sources[i]))
                    continue;
            SpiceDasSource source = SpiceDasSource.fromDasSource(sources[i]);
            
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
            
            if ( cs.toString().equals( thiscs.toString()) ) {
                //System.out.println("match");
                return true ;
            }
            
        }
        return false ;
        
    }
    
    
    public DasSource[] getServers(String capability, String coordSys){
        DasSource servers[] = getServers(capability);
        ArrayList retservers = new ArrayList();
        for ( int i = 0 ; i < servers.length ; i++ ) {
            DasSource ds = (DasSource)servers[i];
            
            if ( hasCoordSys(coordSys,ds)) {
                retservers.add(ds);
            }    
        }
        
        return (DasSource[])retservers.toArray(new DasSource[retservers.size()]) ;
    }
    
    
    public boolean hasCapability(String capability, DasSource ds){
        String[] capabilities = ds.getCapabilities() ;
        for ( int c=0; c<capabilities.length ;c++) {
            String capabil = capabilities[c];
            if ( capability.equals(capabil)){
                return true;
            }
        }
        return false;
    }
    
    public DasSource[] getServers(String capability) {
        ArrayList retservers = new ArrayList();
        for ( int i = 0 ; i < allsources.length ; i++ ) {
            DasSource ds = (DasSource) allsources[i];
            if ( hasCapability(capability,ds)){
          
            
                retservers.add(ds);
                    //if ( capabil.equals("alignment") ){
                    
                    //if ( isSeqStrucAlignmentServer(ds) ){
                    
                    //} else {
                    //System.out.println("DasSource " + ds.getUrl() + " is not a UniProt to PDB alignment service, unable to use");
                    //}
                    //} else {
                    //retservers.add(ds);
                    //}
                    
            }
        }
        
        
        return (DasSource[])retservers.toArray(new DasSource[retservers.size()]) ;
    }

    public void loadingFinished(DasSourceEvent ds) {
        // TODO Auto-generated method stub
        
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
