/*
 *                    BioJava development code
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
 * Created on 20.03.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.spice ;

import org.biojava.bio.structure.* ;
import org.biojava.bio.program.das.dasalignment.* ;
import java.util.List ;
import java.io.IOException ;
import org.biojava.bio.Annotation;

/** Loads a structure alignment in an independent Thread.  once loading
 * is finished sets the new structure object in the master SPICEFrame
 * @author Andreas Prlic
 */
public class LoadStructureAlignmentThread 
    extends Thread {
    SPICEFrame spiceframe ;

    String pdb1 ;
    String pdb2 ;

    boolean finished ;
   
    public LoadStructureAlignmentThread(SPICEFrame master,
					String pdb1_, 
					String pdb2_) {
	spiceframe = master ;
	pdb1 = pdb1_ ;
	pdb2 = pdb2_ ;
    }

    public void run () {
	loadCompound() ;
    }
    
    public synchronized void loadCompound() {
	
	try {

	    // just start two LoadStructureThreads
	    LoadStructureThread thr1 = new LoadStructureThread(spiceframe,pdb1);
	    LoadStructureThread thr2 = new LoadStructureThread(spiceframe,pdb2);
	    thr1.start();
	    thr2.start();

	    // load prosup alignment
	    String prosupserver = "http://127.0.0.1:8080/dazzle/prosupalig/" ;
	    String cmd = "alignment?query="+pdb1+"&subject="+pdb2;
	    
		
	    DASAlignmentCall alicall = new DASAlignmentCall(prosupserver+cmd);
	    Alignment[] alignments =  alicall.getAlignments();
	    //System.out.println(ali[0]);

	    boolean done = false ;
	    while ( ! done ) {
		try {
		    wait(30);		   
		} catch ( InterruptedException e) {		
		    done = true ;
		}
		if ( thr1.isDone() && thr2.isDone() ) {
		    done = true ;
		    Structure struc1 = thr1.getStructure();		    
		    Structure struc2 = thr2.getStructure();
		    // join them
		    //Structure  structure = struc1 ;
		    //List chains1 = struc1.getChains(0);
		    //List chains2 = struc2.getChains(0);
		    StructureImpl strucnew = new StructureImpl();


		    // shift and rotate  structure2
		    Alignment ali = alignments[0] ;
		    Annotation[] vectors = ali.getVectors();
		    
		    for ( int i =0 ; i<vectors.length; i++){
			Annotation vector = vectors[i];
			String intObjectId = (String) vector.getProperty("intObjectId");
			Atom avect = (Atom)vector.getProperty("vector");
		    
			// resolve intObjectId to structure...
			int iId = Integer.parseInt(intObjectId);
			
			if ( iId == 1 ) {
			    Calc.shift(struc1,avect);
			} 
			else if ( iId == 2 ){
			    
			    Calc.shift(struc2,avect);
			    Annotation[] matrices = ali.getMatrices();
			    Annotation max = matrices[0];
			    double[][] matrix = new double[3][3];
			    for ( int x=1; x<=3; x++){
				for ( int y=1; y<=3; y++){
				    String mat = "mat"+x+y;
				    String vals = (String)max.getProperty(mat) ;
				    double val = Double.parseDouble(vals);
				    matrix[x-1][y-1]= val ;
				    System.out.println(mat + " " + val);
				}
			    }
			    System.out.println("shifting struc2" + vector);
			    Calc.rotate(struc2,matrix);
			}
			
		    }
		    

		    // get the correct chain ..
		    Chain chain1 = null ;
		    Chain chain2 = null ;
		    try {
			chain1 = getChainFrom(struc1,pdb1);
			chain2 = getChainFrom(struc2,pdb2);
		    } catch ( IOException e){
			//could not find the correct chain!
			// AARGH!
			e.printStackTrace();
			//spiceframe.showStatus("a problem occured");
			    
		    } 
		   
		    // rename the chains!
		    chain1.setName("A");
		    chain2.setName("B");
		    strucnew.addChain(chain1);
		    strucnew.addChain(chain2);
		    strucnew.setName("alignment");
		    strucnew.setPDBCode("alig");
		    strucnew.setNmr(false);
		    
		    //System.out.println(pdb1 +" chain:");
		    //System.out.println(chain1);
		    //System.out.println(pdb2 + " chain:");
		    //System.out.println(chain2);


		    String scmd =" select all; cpk off ; wireframe off ; backbone on ; select *A; color red; select*B; color blue; ";

		    // go through the blocks of the alignment and color segments:
		    Annotation[] blocks = ali.getBlocks();
		    for ( int b = 0; b< blocks.length; b++){
			Annotation block = blocks[b] ;
			List segments = (List) block.getProperty("segments");
			for (int s = 0; s<segments.size(); s++){
			    Annotation segment = (Annotation)segments.get(s);
			    String intObjectId = (String) segment.getProperty("intObjectId");
			    String start = (String) segment.getProperty("start");
			    String end = (String) segment.getProperty("end");

			    String currentchain = "b";
			    if ( intObjectId.equals("1") ) {
				currentchain = "a" ;
			    }
			    //scmd += "select *"+currentchain+"; 
			    //System.out.println("select "+start+"-"+end+"and **"+currentchain+"; backbone 150;");
			    scmd += " select "+start+"-"+end+"and **"+currentchain+"; backbone 150;" ;
			    
			}
			
		    }

		    System.out.println(scmd);
		    spiceframe.setStructure(strucnew);
		    //spiceframe.executeCmd(strucnew);
		    //TODO : imlement how this command can be sent to Jmol ...
		    //System.out.println(strucnew);
		    //spiceframe.showStatus("structure alignment loaded!!!");

		    // and create the alignment command:

		    //spiceframe.executeCmd(scmd);
		}	
	    } 
	    
	    notifyAll();
	    }
catch (Exception e){ 
	    // at some point raise some IO exception, which should be defined by the Inferface
	    e.printStackTrace();
			
	}

    }
    
    private Chain getChainFrom(Structure struc, String pdbcode)
	throws IOException
    {
	List chains = struc.getChains(0);
	
	// get chainName from PDB code 
	String[] spl   = pdbcode.split("\\.");
	//String prot    = "";
	String chainName = "" ;
	if (spl.length<2) {
	    //prot = pdbcode    ;
	    chainName = " " ;
	} else {
	    //prot      = spl[0]    ;
	    chainName = spl[1] ;
	}

	for ( int i=0; i<chains.size();i++) {
	    Chain c = (Chain)chains.get(i);
	    if ( c.getName().equals(chainName)){
		return c;
	    }
	}
	    throw new IOException("not chain with name "+chainName+" found!");
    }
    
}
