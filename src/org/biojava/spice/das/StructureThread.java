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
 * Created on Nov 7, 2005
 *
 */
package org.biojava.spice.das;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.DASStructureClient;
import org.biojava.bio.structure.io.PDBFileReader;
import org.biojava.spice.manypanel.eventmodel.StructureEvent;
import org.biojava.spice.manypanel.eventmodel.StructureListener;


/** a thread that gets the protein structure from a das server
 * 
 * @author Andreas Prlic
 *
 */
public class StructureThread
extends Thread{
    
    
    SpiceDasSource[] dasSources;
    String accessionCode;
    static Logger logger = Logger.getLogger("org.biojava.spice");
    List structureListeners;
     
    
    public StructureThread(String accessionCode, SpiceDasSource[] dss) {
        super();
        dasSources = dss;
        this.accessionCode = accessionCode;
        structureListeners = new ArrayList();
    }
    
    public void addStructureListener(StructureListener li){
        structureListeners.add(li);
    }
    
    public void triggerNewStructure(StructureEvent event){
        
            Iterator iter = structureListeners.iterator();
            while (iter.hasNext()){
               //StructureListener li = (StructureListener) iter.next();
               //li.newStructure(event);
                StructureListener li = (StructureListener) iter.next();
                li.newStructure(event);
            }
    }
    
    public void run() {
        Structure structure = null ;
        for (int i=0 ; i < dasSources.length; i++){
            SpiceDasSource ds = dasSources[i];
            
            String url = ds.getUrl();
            logger.finest(url);
            
            if ( url.substring(0,7).equals("file://") ) {
                // load local PDB file
                String dir  = url.substring(7);
                structure = getLocalPDB(dir,accessionCode);
            } else {
                char lastChar = url.charAt(url.length()-1);      
                if ( ! (lastChar == '/') ) 
                    url +="/" ;
                
                String dasstructurecommand = url + "structure?model=1&query=";
                
                
                DASStructureClient dasc= new DASStructureClient(dasstructurecommand);
                logger.info("requesting structure from "+dasstructurecommand  +accessionCode);     
                try {
                    structure = dasc.getStructureById(accessionCode);                  
                }
                catch (Exception e) {
                    logger.log(Level.WARNING,"could not retreive structure from "+dasstructurecommand ,e);
                    
                }
            }
        }
        if ( structure != null ){
            StructureEvent event = new StructureEvent(structure);
            triggerNewStructure(event);
        }
        //notifyAll();
        
    }
    private Structure getLocalPDB(String dir,String pdbcode) {
        PDBFileReader parser = new PDBFileReader() ;
        //TODO make extensions configurable
        String[] extensions = {".ent",".pdb"};
        for (int i =0; i< extensions.length ; i++){
            String s = extensions[i];
            parser.addExtension(s);
        }
        parser.setPath(dir+ java.io.File.separator);
        Structure struc = null ;
        try {
            struc = parser.getStructureById(pdbcode);
        }  catch ( IOException e) {
            logger.log(Level.INFO,"local structure "+pdbcode+" not found, trying somewhere else");
            //e.printStackTrace();
            return null ;
        }
        return struc ;
    }
}