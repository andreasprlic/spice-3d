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

import java.util.logging.Logger;

import org.biojava.bio.structure.AminoAcidImpl;
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
    

    static Logger    logger      = Logger.getLogger(SpiceDefaults.LOGGER);
    
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
        if ( ! (clientFile instanceof Structure))
            return new ProjectedAtomIterator(new StructureImpl());
        
        return new ProjectedAtomIterator(structure);
    }

    public int getEstimatedAtomCount(Object clientFile) {
        
        // get number atoms ...
        
        return StructureTools.getNrAtoms(structure);
        
        
    }
    
    

    /** return the number of models (each model is an AtomSet) **/
    public int getAtomSetCount(Object clientFile) {
        
        
        return StructureTools.getNrAtoms(structure);
    }




    /* **************************************************************
     * the frame iterators
     * **************************************************************/
    class ProjectedAtomIterator extends JmolAdapter.AtomIterator {
        Structure structure;
        Atom atom;
        org.biojava.bio.structure.AtomIterator iter;
        int pos;
        
        ProjectedAtomIterator(Structure structure) {
            logger.info("new ProjectedAtomIterator");
            iter  = new org.biojava.bio.structure.AtomIterator(structure);
            pos = 0;
            atom = new AtomImpl();
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
            return atom.getFullName();
        }

        public String getGroup3(){
            
            Group parent = atom.getParent();
            if (parent != null){
                //System.out.println(parent.getPDBName());
                return parent.getPDBCode();
            }
            return null;
        }

        public char getChainID() {
            Group g = atom.getParent();
            if ( g != null ){
                Chain c = g.getParent();
                if ( c != null) {
                    String name = c.getName();
                    if ( (name != null) && ( name.length() > 0)){
                        return name.charAt(0);
                    }
                }
            }
            return ' ';
            
        }
        
        public char getAlternateLocationID() {
            if ( atom.getAltLoc().charValue() != ' ')
                return atom.getAltLoc().charValue();
            else
                return (char)0;
        }


        public char getInsertionCode() {
           //TODO add support for insertion codes ...
            
            return (char)0;
        }


        public boolean getIsHetero() {
            Group parent = atom.getParent();
            if ( parent != null) {
                if (parent.getType().equals(HetatomImpl.type)){
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

