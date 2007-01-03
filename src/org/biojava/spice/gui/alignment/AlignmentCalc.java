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
 * Created on Jul 16, 2006
 *
 */
package org.biojava.spice.gui.alignment;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.align.StructurePairAligner;
import org.biojava.bio.structure.align.pairwise.AlternativeAlignment;
import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.das.StructureThread;
import org.biojava.dasobert.eventmodel.StructureEvent;
import org.biojava.dasobert.eventmodel.StructureListener;
import org.biojava.spice.manypanel.eventmodel.StructureAlignmentListener;

/** a class that obtains two structures via DAS and aligns them
 *  This is done in a separate thread.
 *  It is possible to register Event listeners to get notification of when the download has finished.
 *  
 * @author Andreas Prlic
 * @since 4:08:14 PM
 * @version %I% %G%
 */
public class AlignmentCalc implements Runnable {
    
    
    private static String baseName = "alignment";
    private ResourceBundle resource;
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    
    boolean interrupted = false;
    
    
    String pdb1;
    String pdb2;
    String chain1;
    String chain2;
    SpiceDasSource[] servers;
    
    
    myStructureListener myStructureListener1;
    myStructureListener myStructureListener2;
    
    Structure structure1;
    Structure structure2;
    
    AlignmentGui parent;
    List structureAlignmentListeners;
    
    /** requests an alignment of pdb1 vs pdb 2.
     * Chain 1 and chain2 are optional.
     * If they are empty strings, they are ignored
     * @param parent the alignment gui frame that interacts with this class
     * @param pdb1
     * @param chain1
     * @param pdb2
     * @param chain2
     * @param servers array of structure servers
     */
    public AlignmentCalc(AlignmentGui parent, 
            String pdb1, 
            String chain1 , 
            String pdb2, 
            String chain2, 
            SpiceDasSource[] servers) {
        super();
        
        this.parent= parent;
        this.pdb1 = pdb1;
        this.pdb2 = pdb2;
        this.chain1 = chain1;
        this.chain2 = chain2;
        this.servers = servers;
        
        myStructureListener1 = new myStructureListener(this,1);
        myStructureListener2 = new myStructureListener(this,2);
        structureAlignmentListeners = new ArrayList();
        resource = ResourceBundle.getBundle(baseName);
    }
    
    /** launch the calculation
     * 
     */
    public void run() {
        logger.info("starting calculation with" + servers.length + " servers");
        if ( servers.length == 0) {
            cleanup();
            return;
        }
        StructureThread sthread1 = new StructureThread(pdb1,servers);
        sthread1.addStructureListener(myStructureListener1);
        
        boolean samePdb = false;
        
        if ( pdb1.equalsIgnoreCase(pdb2)) {
            sthread1.addStructureListener(myStructureListener2);
            samePdb = true;
        }
        if ( interrupted) {
            cleanup();
            return;
        }
        sthread1.start();
        
        if (! samePdb) {
            StructureThread sthread2 = new StructureThread(pdb2,servers);
            sthread2.addStructureListener(myStructureListener2);
            if ( interrupted) {
                cleanup();
                return;
            }
            sthread2.start();
        }
        
    }
    
    public synchronized void setStructure1(Structure s1) {
        if ( s1 == null) {
            
            String warn =  resource.getString("alignment.structurenotfound.msg");
            
            Object[] arg = {pdb1};
            String msg =  MessageFormat.format(warn,arg);
            logger.warning(msg);
            interrupt();
        }
        if ( interrupted) {
            cleanup();
            return;
        }
        structure1 = s1;
        testStructureOk();
    }
    
    public synchronized  void setStructure2(Structure s2) {
        if ( s2 == null) {
            
            String warn =  resource.getString("alignment.structurenotfound.msg");
            
            Object[] arg = {pdb2};
            String msg =  MessageFormat.format(warn,arg);
            logger.warning(msg);
            
            interrupt();
        }
        
        if ( interrupted) {
            cleanup();
            return;
        }
        structure2 = s2;
        testStructureOk();
    }
    
    
    private void testStructureOk(){
        
        if (( structure1 == null) || ( structure2 == null )){
            // not ready yet
            return;
        }
        
        // both structure have been downloaded, now calculate the alignment ...
        
        
        Structure tmp1 = new StructureImpl();
        Structure tmp2 = new StructureImpl();
        
        if (( chain1 != null) && (chain1.length()>0)){
            try {
                Chain c1 = structure1.findChain(chain1);
                tmp1.setPDBCode(structure1.getPDBCode());
                tmp1.addChain(c1);
            } catch (StructureException e){
                
                String warn =  resource.getString("alignment.chainnotfound.msg");
                
                Object[] arg = {chain1,pdb1};
                String msg =  MessageFormat.format(warn,arg);
                logger.warning(msg);
                tmp1 = structure1;
            } 
        } else {
            tmp1 = structure1;
        }
        
        
        if (( chain2 != null) && (chain2.length()>0)){
            try {
                Chain c2 = structure2.findChain(chain2);
                tmp2.setPDBCode(structure2.getPDBCode());
                tmp2.addChain(c2);
            } catch (StructureException e){
                String warn =  resource.getString("alignment.chainnotfound.msg");
                
                Object[] arg = {chain2,pdb2};
                String msg =  MessageFormat.format(warn,arg);
                logger.warning(msg);
                tmp2 = structure2;
            }
            
        } else {
            tmp2 = structure2;
        }
        
        if ( interrupted) {
            cleanup();
            return;
        }
        logger.info("got structures, now doing alignment");
        StructurePairAligner aligner = new StructurePairAligner();
        try {
            aligner.align(tmp1,tmp2);
        } catch (StructureException e){
            logger.warning(e.getMessage());
        
        }
        
        if ( interrupted) {
            cleanup();
            return;
        }
        

        
        AlternativeAlignment[] aligs = aligner.getAlignments();
        
        showAlignment(aligs);

        logger.info("done!");
        
        parent.notifyCalcFinished();
        
    }
    
    public void addStructureAlignmentListener(StructureAlignmentListener li) {
        structureAlignmentListeners.add(li);
    }
    
    public void clearListeners(){
        structureAlignmentListeners.clear();
    }
    
    
    private void showAlignment(AlternativeAlignment[] aligs) {
        AlternativeAlignmentFrame frame = new AlternativeAlignmentFrame(structure1, structure2);
        frame.setAlternativeAlignments(aligs);
        frame.pack();
        frame.show();
        Iterator iter = structureAlignmentListeners.iterator();
        while (iter.hasNext()){
            StructureAlignmentListener li = (StructureAlignmentListener)iter.next();
            frame.addStructureAlignmentListener(li);
        }
        
    }
    
    /** stops what is currently happening and does not continue
     * 
     *
     */
    public void interrupt() {
        interrupted = true;
    }
    
    public void cleanup() {
        
        parent.notifyCalcFinished();

        parent=null;
        // cleanup...
        servers    = null;
        structure1 = null;
        structure2 = null;
        clearListeners();
    }
    
}


class myStructureListener implements StructureListener {
    
    AlignmentCalc parent;
    int position ;
    public myStructureListener(AlignmentCalc parent, int position){
        this.parent = parent;
        this.position = position;
    }
    
    public void newStructure(StructureEvent event) {
        // TODO Auto-generated method stub
        if ( position == 1)
            parent.setStructure1(event.getStructure());
        else
            parent.setStructure2(event.getStructure());
        
    }
    
    public void selectedChain(StructureEvent event) {
        // TODO Auto-generated method stub
        
    }
    
    public void newObjectRequested(String accessionCode) {
        // TODO Auto-generated method stub
        
    }
    
    public void noObjectFound(String accessionCode) {
        // TODO Auto-generated method stub
        if ( position == 1)
            parent.setStructure1(null);
        else
            parent.setStructure2(null);
        
    }
    
    
}

