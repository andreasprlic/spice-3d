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
 * Created on Jan 25, 2006
 *
 */
package org.biojava.spice.Panel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.spice.manypanel.eventmodel.SequenceListener;
import org.biojava.spice.manypanel.eventmodel.StructureEvent;
import org.biojava.spice.manypanel.eventmodel.StructureListener;
import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolViewer;
import org.jmol.popup.JmolPopup;

public class JmolSpiceTranslator 

implements JmolStatusListener, StructureListener
{
    
    static Logger    logger      = Logger.getLogger("org.biojava.spice");
    JmolViewer  viewer;    
    JmolPopup jmolpopup ;
    
    Structure structure;
    int currentChainNumber;
    List pdbSequenceListener;
    
    public JmolSpiceTranslator() {
        super();
        structure = new StructureImpl();
        currentChainNumber = -1;
        pdbSequenceListener = new ArrayList();
    }
    
    public void setJmolViewer(JmolViewer viewer){
        this.viewer = viewer;
    }
    
    public void setJmolPopup(JmolPopup popup){
        this.jmolpopup = popup;
    }
    
    public void notifyFileLoaded(String fullPathName, String fileName,
            String modelName, Object clientFile,
            String errorMessage){
        logger.finest("StructurePanel notifyFileLoaded ");
        if (errorMessage != null){
            logger.log(Level.SEVERE,errorMessage);
        }        
    }
    
    public void notifyFileNotLoaded(String fullPathName, String errorMsg){}
    
    public void setStatusMessage(String statusMessage){
        logger.log(Level.INFO,statusMessage);
    }
    
    public void scriptEcho(String strEcho){
        if (  strEcho.equals("no structure found"))
            return;
        logger.log(Level.INFO, "jmol scriptEcho: " + strEcho);
    }
    
    public void scriptStatus(String strStatus){
        logger.log(Level.FINE,"jmol scriptStatus: " +strStatus);
    }
    
    public void notifyScriptTermination(String statusMessage, int msWalltime){
        
        //logger.fine("Script finished in " + msWalltime + "ms");
    }
    
    public void showUrl(String urlString) {
        logger.finest("showUrl: " +urlString);
    }
    
    public void showConsole(boolean showConsole){
        logger.finest("jmol: showConsole "+showConsole);
    }
    
    public void handlePopupMenu(int x, int y){
        //logger.finest("handlePopupMenu");
        //viewer.popupMenu(e.getX(),e.getY());
        if ( jmolpopup != null) {
            jmolpopup.show(x,y);
        }
           
    }
    
    
    
    public void notifyAtomPicked(int atomIndex, String strInfo){
        logger.info("Atom picked "  + atomIndex + " " + strInfo);
        
        if ( viewer != null ) {
            //logger.info("atomName:" + viewer.getAtomName(atomIndex));
            int mod = viewer.getAtomModelIndex(atomIndex);
            //java.util.Properties props = viewer.getModelProperties(mod);
            //logger.finest(props.toString());
            //System.out.println(props);
            //logger.info(viewer.getAtomNumber(atomIndex)+"");
            
            String pdbcode = viewer.getAtomSequenceCode(atomIndex);
            String chainId = viewer.getAtomChain(atomIndex); 
            
            //logger.info(pdbcode);
            //logger.info(chainId);
            
            highlitePdbPosition(pdbcode,chainId);
        }
    }
    
    private void highlitePdbPosition(String pdbresnum,String chainId){
        // notify that a particulat position has been selected
        
        Chain currentChain = structure.getChain(currentChainNumber);
        //logger.info("current chain is " + currentChain.getName() + " selected is " + chainId);
        if ( currentChain.getName().equals(chainId)){
            int seqPos = getSeqPosFromPdb(pdbresnum, currentChain);
            //logger.info("is spice seq. position " + seqPos);
            if ( seqPos >=0){
                triggerSelectedSeqPos(seqPos);
            } 
        }  else {
            logger.info("selected residue " + pdbresnum + " chain " + chainId + " (chain currently not active in sequence dispay)");

        }
    }
    
    private int getSeqPosFromPdb(String pdbresnum, Chain currentChain){
        List groups = currentChain.getGroups();
        try {
            Group g = currentChain.getGroupByPDB(pdbresnum);
            return groups.indexOf(g);
          
        } catch (StructureException e) {
            return -1;
        }
        
    }
    
    
    
    public void notifyMeasurementsChanged(){
        logger.finest("nofiyMeasurementsChanged");
    }
    
    public void notifyFrameChanged(int frameNo){}

    
    // now the Spice Structure events ...
    
    public void newStructure(StructureEvent event) {
        //logger.info("JmolSpiceTranslator got new structure " + event.getPDBCode() + " " + structure.getPDBCode());
        String p = event.getPDBCode();
        
        if ( p.equalsIgnoreCase(structure.getPDBCode())) {
            // already known
            return;
        }
        this.structure = event.getStructure();
        this.currentChainNumber = event.getCurrentChainNumber();
    }

    public void selectedChain(StructureEvent event) {
        //logger.info("JmolSpiceTranslator selected Chain" + event.getCurrentChainNumber());
        this.structure = event.getStructure();
        this.currentChainNumber = event.getCurrentChainNumber();
        
    }

    public void newObjectRequested(String accessionCode) {
        this.structure = new StructureImpl();
        this.currentChainNumber = -1;
        
    }

    public void noObjectFound(String accessionCode) {
        // TODO Auto-generated method stub
        
    }
    
    public void addPDBSequenceListener(SequenceListener li){
        pdbSequenceListener.add(li);
    }
    
    private void triggerSelectedSeqPos(int position){
        Iterator iter = pdbSequenceListener.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectedSeqPosition(position);
            
        }
        
    }
    
  
    
    
}
