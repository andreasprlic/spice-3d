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
 * Created on Jan 19, 2007
 *
 */
package org.biojava.spice.jmol;

import java.util.Hashtable;
import java.util.Properties;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.AtomImpl;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.HetatomImpl;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.StructureTools;
import org.biojava.spice.config.SpiceDefaults;
import org.jmol.api.JmolAdapter;

public class SpiceJmolAdapter extends JmolAdapter{
    

    public static java.util.logging.Logger    logger      = java.util.logging.Logger.getLogger(SpiceDefaults.LOGGER);
    
    Structure structure;
    
    
    public SpiceJmolAdapter() {
        super("SpiceJmolAdapter");
      
        structure = new StructureImpl();
     
    }

    public Structure getStructure(){
        return structure;
    }
    
    public void setStructure(Structure structure){
        if ( structure == null)
            structure = new StructureImpl();
        
        this.structure = structure;
    }
    
    public JmolAdapter.AtomIterator getAtomIterator(Object clientFile) {
        
        if ( ! (clientFile instanceof Structure)) {
            logger.info("did not get a structure object ...");
            logger.info(clientFile.getClass()+"");
            return new ProjectedAtomIterator(new StructureImpl());
        }
        
        return new ProjectedAtomIterator(structure);
    }

    public int getEstimatedAtomCount(Object clientFile) {
        
        // get number atoms ...
        
        return StructureTools.getNrAtoms(structure);
                
    }
    
    

    /** return the number of models (each model is an AtomSet) **/
    public int getAtomSetCount(Object clientFile) {
        //logger.info("get atomsetcount " + structure.nrModels());
        return structure.nrModels();   
    }
    
    
    public String getAtomSetCollectionName(Object clientFile) {
        //logger.info("getAtomSetCollectionName");
        return null;
      }
      
      public Properties getAtomSetCollectionProperties(Object clientFile) {
          //logger.info("getAtomSetCollectionProperties");
        return null;
      }

      public Hashtable getAtomSetCollectionAuxiliaryInfo(Object clientFile) {
        Hashtable tab = new Hashtable();
        tab.put("isPDB", new Boolean(true));
        return tab;
        
      }
      
      public boolean coordinatesAreFractional(Object clientFile) {
          return false;
      }

      public JmolAdapter.StructureIterator
          getStructureIterator(Object clientFile) {
         // logger.info("getStructureIterator");
          return null;
      }
      
      
      public int getAtomSetNumber(Object clientFile, int atomSetIndex) {        
              return atomSetIndex + 1;
      }

      public String getAtomSetName(Object clientFile, int atomSetIndex) {
         // logger.info("!!! getAtomSetName " + atomSetIndex);
            return null;
      }
      
      public Properties getAtomSetProperties(Object clientFile, int atomSetIndex) {
         // logger.info("getAtomSetProperties " + atomSetIndex);
          return null;
      }
      
      public Hashtable getAtomSetAuxiliaryInfo(Object clientFile, int atomSetIndex) {
          //logger.info("getAtomSetAuxiliaryInfo " + atomSetIndex);
        return null;
      }
    


    //[PRO]13.CA #199 34.518997 0.168 11.227

    /* **************************************************************
     * the frame iterators
     * **************************************************************/
    class ProjectedAtomIterator extends JmolAdapter.AtomIterator {
        Structure structure;
        Atom atom;
        org.biojava.bio.structure.AtomIterator iter;
        int pos;
        
        ProjectedAtomIterator(Structure structure) {
          //  logger.info("new ProjectedAtomIterator");
            iter  = new org.biojava.bio.structure.AtomIterator(structure);
            pos = 0;
            atom = new AtomImpl();
            this.structure = structure;
           
        }
        
       
        
        public String getElementSymbol() {
            //if ( atom.getPDBserial() < 200)
            //    logger.info(atom.getParent().getPDBCode() + " " + atom.getParent().getPDBName() + " " + atom.getPDBserial()+ atom.getFullName() +  " " + JmolUtils.deduceElementSymbol(atom.getFullName()));
            return JmolUtils.deduceElementSymbol(atom.getFullName());
            //return "Xx";
            //return super.getElementSymbol();
        }


        public boolean hasNext() {
            if ( iter.hasNext()){
                atom = (Atom)iter.next();
                pos++;
                return true;
            } else {
                return false;
            }            
        }
        
        public Object getUniqueID() {
       
            return new Integer(pos);
        }
        
        public int getAtomSerial() { 
            //return pos;
            return atom.getPDBserial(); 
        }
        
        public float getX() {
           
            return (float)atom.getX();
        }
        public float getY() {
           
            return (float)atom.getY();
        }
        public float getZ() {
            
            return (float)atom.getZ();
        }


        public String getAtomName() {
            //System.out.println(atom.getName());
            //return atom.getFullName();
            return atom.getName();
        }

        public String getGroup3(){
            
            Group parent = atom.getParent();
            if (parent != null){
                
                return parent.getPDBName();
            }
            return null;
        }
        
        public int getSequenceNumber() {
            Group g = atom.getParent();
            if ( g != null ){
                
                    return Integer.parseInt(g.getPDBCode());                
            }
            
            return -1;
        }

        public char getChainID() {
           
            Chain c = iter.getCurrentChain();

            if ( c != null) {

                String name = c.getName();          
                if ( (name != null) && ( name.length() > 0)){
                                        return name.charAt(0);
                }
            }

            return ' ';
            
        }
                        
        public int getAtomSetIndex() {
            int idx =  iter.getCurrentModel() ;
            //if ( structure.nrModels() > 1) 
            //    idx++;
                
            
            //System.out.println(idx + " " + atom.getParent().getPDBCode() + " " + atom);
            
            return idx;
        }



       



        public char getAlternateLocationID() {
            if ( atom.getAltLoc().charValue() != ' ') {
                logger.info("alternate location at " + atom);
                return atom.getAltLoc().charValue();
            }
            else
                return (char)0;
        }


        public char getInsertionCode() {
           //TODO check support for insertion codes ...
            Group g = atom.getParent();
            if ( g != null){
                String pdbresnum = g.getPDBCode();
                try {
                    Integer.parseInt(pdbresnum);
                } catch (NumberFormatException ex){
                    
                    // TODO: check this ...
                    String insertionCode = pdbresnum.substring(pdbresnum.length()-2,pdbresnum.length());
                    return insertionCode.charAt(0);
                }
            }
            return (char)0;
        }


        public boolean getIsHetero() {
            Group parent = atom.getParent();
            if ( parent != null) {
                if (parent.getType().equals(HetatomImpl.type)){
                    //System.out.println(atom + " is hetero");
                    return true;
                }
            }
                
            return false;
        }


        public int getOccupancy() {
                        
            // trying to match what Jmol does in PdbReader
            return (int)atom.getOccupancy()*100;
        }


        public float getBfactor() {
            return (float)atom.getTempFactor();
        }

        
        
        
    }



    
    
}

