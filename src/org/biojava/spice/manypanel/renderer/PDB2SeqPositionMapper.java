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
 * Created on Aug 22, 2006
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.util.Iterator;
import java.util.List;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.spice.feature.Feature;
import org.biojava.spice.feature.FeatureImpl;
import org.biojava.spice.feature.Segment;

// from abstractChainrenderer ...

public class PDB2SeqPositionMapper {

    
    
    public static Feature mapPDBFeature2Seq(Feature f, Chain sequence){
        Feature newF = new FeatureImpl();
        newF.setMethod(f.getMethod());
        newF.setName(f.getName());
        newF.setNote(f.getNote());
        newF.setScore(f.getScore());
        newF.setSource(f.getSource());
        newF.setType(f.getType());
        
        newF.setLink(f.getLink());
        List segments = f.getSegments();

        Iterator iter = segments.iterator();
        while (iter.hasNext()){
            Segment s = (Segment)iter.next();
            Segment newS =(Segment) s.clone();
            // and now re-label the positions!
            int startOld = s.getStart();
            int seqPos = getGroupPosByPDBPos(startOld+"",sequence);
            
            // if seqPos = -1 this means it could not be mapped
            // ignore segments that can not be mapped 
            if ( seqPos < 0 )
                continue;
            
            newS.setStart(seqPos);
            int endOld = s.getEnd();
            int seqePos = getGroupPosByPDBPos(endOld+"",sequence);
            if ( seqePos < 0)
                continue;
            newS.setEnd(seqePos);
            newF.addSegment(newS);
        }
        return newF;
        
        
    }
    
    /* returns the seqeunce position of this PDB residue number
     * returns -1 if could not be mapped
     *  
     */
    
    private static int getGroupPosByPDBPos(String pdbPos, Chain c){

    
        List groups = c.getGroups();
        
        // now iterate over all groups in this chain.
        // in order to find the amino acid that has this pdbRenum.               
        
        Iterator giter = groups.iterator();
        int i = 0;
        while (giter.hasNext()){
            i++;
        
            Group g = (Group) giter.next();
            String rnum = g.getPDBCode();
            if ( rnum.equals(pdbPos)) {
                //System.out.println(i + " = " + rnum);
                return i;
            }
        }    
        //logger.warning("could not map pdb pos " + pdbPos + " to sequence!");
        return -1;
    }

}
