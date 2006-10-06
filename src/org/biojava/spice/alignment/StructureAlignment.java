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
 * Created on May 15, 2006
 *
 */
package org.biojava.spice.alignment;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.biojava.bio.Annotation;
import org.biojava.bio.program.das.dasalignment.Alignment;
import org.biojava.bio.program.das.dasalignment.DASException;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.io.DASStructureClient;
import org.biojava.bio.structure.jama.Matrix;
import org.biojava.dasobert.dasregistry.Das1Source;
import org.biojava.dasobert.dasregistry.DasCoordinateSystem;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.gui.SpiceMenuListener;
import org.biojava.spice.manypanel.renderer.SequenceScalePanel;

public class StructureAlignment {
    
    public static Logger logger =  Logger.getLogger("org.biojava.spice");    
    
    Alignment alignment;
    
    Structure[] structures;
    Matrix[] matrices;
    Atom[] shiftVectors;
    String[] intObjectIds;
    String[] accessionCodes;
    
    boolean[] selection;
    boolean[] loaded ;
    
    Das1Source[] structureServers;
    JFrame progressFrame;
    
    Annotation[] sortedBlocks;
    
    int nrSelected;
    
    String[] rasmolScripts;

    String filterBy;
    
    JProgressBar progressBar;
    boolean waitingForStructure;
    
    Structure loadedStructure;
    
    static Matrix zero;
    
    static {
        // remove objects that have a Matrix object that contains all zeros!
        zero =  Matrix.identity(3,3);
        for (int i=0;i<3;i++ ){
            zero.set(i,i,0.0);
        }
    }
    
    DasCoordinateSystem coordSys;
    
    List selectionOrder;
    
    public StructureAlignment(DasCoordinateSystem coordSys) {
        super();
        
        this.coordSys = coordSys;
        
        structureServers = new Das1Source[0];
        structures = new Structure[0];
        selection = new boolean[0];
        loaded = new boolean[0];
        sortedBlocks = new Annotation[0];
        nrSelected = 0;
        waitingForStructure = false;
        loadedStructure = null;
        selectionOrder = new ArrayList();
    }
    
    public DasCoordinateSystem getCoordinateSystem(){
        return coordSys;
    }
    
    public void setStructureServers(Das1Source[] servers){
        structureServers = servers;
    }
    
    public Das1Source[] getStructureServers(){
        return structureServers;
    }
    
    public Alignment getAlignment() {
        return alignment;
    }
    
    public String getFilterBy() {
        return filterBy;
    }

    public void setFilterBy(String filterBy) {
        this.filterBy = filterBy;
    }

    public int getNrStructures(){
        if ( structures == null)
            return 0;
        return structures.length;
    }
    
    public boolean[] getLoaded() {
        return loaded;
    }
    
    public void setLoaded(boolean[] loaded) {
        this.loaded = loaded;
    }
    
    public Matrix[] getMatrices() {
        return matrices;
    }
    
    public void setMatrices(Matrix[] matrices) {
        this.matrices = matrices;
    }
    
    public Atom[] getShiftVectors() {
        return shiftVectors;
    }
    
    public void setShiftVectors(Atom[] shiftVectors) {
        this.shiftVectors = shiftVectors;
    }
    
    public Structure[] getStructures() {
        return structures;
    }
    
    public void setStructures(Structure[] structures) {
        this.structures = structures;
    }
    
    
    public String[] getRasmolScripts() {
        return rasmolScripts;
    }
    
    public void setRasmolScripts(String[] rasmolScripts) {
        this.rasmolScripts = rasmolScripts;
    }
    
    public String[] getIntObjectIds() {
        return intObjectIds;
    }
    
    public void setIntObjectIds(String[] intObjectIds) {
        this.intObjectIds = intObjectIds;
    }    
    
    public String[] getAccessionCodes() {
		return accessionCodes;
	}

	public void setAccessionCodes(String[] accessionCodes) {
		this.accessionCodes = accessionCodes;
	}

	public boolean isZeroMatrix(Matrix m){
       
        for ( int x=0;x<3;x++){
            for ( int y=0;y<3;y++){
                double val = m.get(x,y);
                if ( val != 0.0){
                   return false;
                }
            }
        }
        return true;
    }
    
    private Alignment newAlignmentFromCheckedData(Annotation[] objects,
            Annotation[] matrices, 
            Annotation[] vectors,
            Annotation[] blockx) 
    throws DASException {
        Alignment ali = new Alignment();
        
        for (int i=0 ; i < objects.length;i++) {
            ali.addObject(objects[i]);
        }
        for (int i=0 ; i < matrices.length;i++) {
            ali.addMatrix(matrices[i]);
        }
        for (int i=0 ; i < vectors.length;i++) {
            ali.addVector(vectors[i]);
        }
        for (int i=0 ; i < blockx.length;i++) {
            ali.addBlock(blockx[i]);
            
        }
        
        return ali;
    }
    
    public void setAlignment(Alignment alignment) throws StructureException {
       
        
        // convert the alignment object into the internal matrices, vectors
        Annotation[] maxs = alignment.getMatrices();
        
        Annotation[] objects = alignment.getObjects();
        System.out.println(objects[0]);
        Annotation[] vectors = alignment.getVectors();
       
        
        int n = objects.length;
        
        if ( maxs.length != n) {
            throw new StructureException("number of rotation matrices ("+maxs.length+
                    ") does not match number of objects ("+n+") !");
            
        }
        
        if ( vectors.length != n){
            throw new StructureException("number of shift vectors ("+vectors.length+
                    ") does not match number of objects ("+n+") !");
        }
        
        
       
        //zero.print(3,3);
        List objectNew   = new ArrayList();
        List matricesNew = new ArrayList();
        List vectorsNew  = new ArrayList();
        List annoMax     = new ArrayList();
        
        for (int i = 0 ; i< n ; i++){
            Annotation a = maxs[i];
            Matrix m = getMatrix(a);
            if ( isZeroMatrix(m)) {
                
                // something went wrong during creation of rotmat
                // ignore the whole object!
                //System.out.println("matrix is zero:"  + objects[i]);
                //m.print(3,3);
                continue;
            }
            
            //System.out.println("adding " + objects[i]);            
            objectNew.add(objects[i]);
            matricesNew.add(m);
            vectorsNew.add(vectors[i]);
            annoMax.add(a);
            
        }
        // copy the data back ...
        objects      = (Annotation[]) objectNew.toArray(   new Annotation[objectNew.size()]);
        matrices     = (Matrix[])     matricesNew.toArray( new Matrix[matricesNew.size()]);
        vectors      = (Annotation[]) vectorsNew.toArray(  new Annotation[vectorsNew.size()]);
       
        Annotation[] blockx  = alignment.getBlocks();
        Annotation[] allMaxes =(Annotation[]) annoMax.toArray(new Annotation[annoMax.size()]); 
        try {
            this.alignment = newAlignmentFromCheckedData(objects, allMaxes, vectors,blockx);
        } catch (DASException e) {
            e.printStackTrace();
          
        }
        
        sortedBlocks = sortBlocks(blockx);
        
        /*System.out.println(n);
        System.out.println(objects.length);
        System.out.println(matrices.length);
        System.out.println(vectors.length);
        */
        
        n = objects.length;
        nrSelected = 0;
        //matrices     = new Matrix[n];
        shiftVectors   = new Atom[n];
        structures     = new Structure[n];
        intObjectIds   = new String[n];
        selection      = new boolean[n];
        loaded         = new boolean[n];
        accessionCodes = new String[n];
        
        for (int i=0;i< n;i++){
            
            Annotation v = vectors[i];            
            Atom vec = (Atom) v.getProperty("vector");
            
            shiftVectors[i]   = vec;             
            structures[i]     = null;            
            intObjectIds[i]   = (String)objects[i].getProperty("intObjectId");
            accessionCodes[i] = (String)objects[i].getProperty("dbAccessionId");
            selection[i]      = false;
            loaded[i]         = false;
        } 
        
       
        
    }
    
    public String[] getIds(){
        return intObjectIds;
    }
    
    
    
    public boolean[] getSelection() {
        return selection;
    }
    
    public void setSelection(boolean[] selection) {
        this.selection = selection;
    }
    
    public void select(int pos){
        selection[pos] = true;
        nrSelected++;
        
        if ( ! selectionOrder.contains(new Integer(pos)))
            selectionOrder.add(new Integer(pos));
    }
    
    public void deselect(int pos){
        selection[pos] = false;
        nrSelected--;
        selectionOrder.remove(new Integer(pos));
        
    }
    
    
    
    /** get the position of the first selected structure
     * or -1 if none selected.
     * 
     * @return first selected structure position
     */
    public int getFirstSelectedPos(){
        for (int i =0 ; i< selection.length;i++){
            if (selection[i])
                return i;
        }
        return -1;
    }
    
    
    /** get the position of the first selected structure
     * or -1 if none selected.
     * 
     * @return first selected structure position
     */
    public int getLastSelectedPos(){
        for (int i =selection.length -1 ; i>=0 ;i--){
            if (selection[i])
                return i;
        }
        return -1;
    }
    
    
    
    public String getRasmolScript(){
    
        return getRasmolScript(getLastSelectedPos()); 
            
        
    }
    
    
    public String getRasmolScript(int firstSelectedPos){
        
        String cmd = "select *; backbone 0.3;";
        

        if ( firstSelectedPos < 0)
            return cmd;
        
        
        // set for the firstSelectedPos ...
        Color col =  getColor(firstSelectedPos);
        Color chaincol = new Color(col.getRed()/2,col.getGreen()/2,col.getBlue()/2);
        int modelcount = 1;
        cmd += "select */"+modelcount+"; ";
        cmd += " color [" +chaincol.getRed()+","+chaincol.getGreen() +","+chaincol.getBlue() +"];";
              
        if ( (sortedBlocks != null)&&(sortedBlocks.length > 0)) {
            cmd += getRasmolFromSortedBlock(firstSelectedPos, modelcount);
        } else {
            cmd += getRasmolScriptFromPrepared(firstSelectedPos);
        }    
       
        
        for ( int p=0;p<selection.length;p++){
            
            if (! selection[p])
                continue;            
            
            if ( p == firstSelectedPos )
                continue;
            
            modelcount ++;
            
            col =  getColor(p);
            chaincol = new Color(col.getRed()/2,col.getGreen()/2,col.getBlue()/2);
            
            cmd += "select */"+modelcount+"; ";
            cmd += " color [" +chaincol.getRed()+","+chaincol.getGreen() +","+chaincol.getBlue() +"];";
            
            if ( (sortedBlocks != null)&&(sortedBlocks.length > 0)) {
                cmd += getRasmolFromSortedBlock(p, modelcount);
            } else {
                cmd += getRasmolScriptFromPrepared(p);
            }                        
        }
        cmd += " model 0;";
        
        System.out.println(cmd);
        
        return cmd;
    }
    
    private String getRasmolScriptFromPrepared(int p){
    	if ( rasmolScripts == null)
    		return "";
    	else 
    		return rasmolScripts[p];
    }
    
    private String getRasmolFromSortedBlock(int p, int modelcount){
        
        String cmd = "";
        String intId = intObjectIds[p];
        //logger.info("get rasmol sc ript from sorted blocks for " + intId );
        Color col =  getColor(p);
        
        
        for (int b=0;b<sortedBlocks.length;b++){
            Annotation block = sortedBlocks[b];
            
            
            List segments = (List)block.getProperty("segments");
            Iterator siter = segments.iterator();
            while (siter.hasNext()){
                Annotation seg = (Annotation)siter.next();
                String ii =  (String)seg.getProperty("intObjectId");
                
                if (! ii.equals(intId))
                    continue;
                //System.out.println("StructureAlignment getRasmolFromSortedBlock segment: " + seg);
                
                String start = (String) seg.getProperty("start");
                String end   = (String) seg.getProperty("end");
                
                //String chain ="";
                int indx1 = start.indexOf(":");
                String chainId = " ";
                if ( indx1 >-1)  {
                    chainId = start.substring(indx1+1,indx1+2);
                    start = start.substring(0,indx1);
                    
                }
                int indx2 = end.indexOf(":");
                if ( indx2>-1)
                    end = end.substring(0,indx2);
                
                if ( ! (chainId.equals(" ")))
                    cmd += " select "+start+"-"+end +":"+chainId+"/"+modelcount+";";
                else
                    cmd += " select "+start+"-"+end +"/"+modelcount+";";
                cmd += " color [" +col.getRed()+","+col.getGreen() +","+col.getBlue() +"];";
                cmd += " backbone 0.6;";                            
                break;
            }        
            
        }
        return cmd;
    }
    
    /** create a single structure, that joins all the actively selected chains together
     * for proper display in the 3D panel
     * 
     * currently: each structure is a new model. Select in jmol with /modelNr
     * 
     * @return Structure - an NMR structure containing all the aligned ones as models
     */
    public Structure createArtificalStructure(){
        
        int firstOne = getLastSelectedPos() ;        
         
        return createArtificalStructure(firstOne);
        
    }
    
    
    /** create a single structure, that joins all the actively selected chains together
     * for proper display in the 3D panel
     * 
     * currently: each structure is a new model. Select in jmol with /modelNr
     * 
     * @param firstPosition the position of the structure to use as model 1 
     * @return Structure - an NMR structure containing all the aligned ones as models
     */
    public Structure createArtificalStructure(int firstPosition){
        
        
        //logger.info("firstPosition " + firstPosition);
        Structure newStruc = new StructureImpl();
        newStruc.setNmr(true);
                
        
        if ( firstPosition == -1 )
            return newStruc;
        
        boolean first = true;
        
        if ( selection[firstPosition]){
            try {
                Structure s = getStructure(firstPosition);
                newStruc.setPDBCode(s.getPDBCode());
                
                List chains = s.getChains(0);
                newStruc.addModel(chains);
                first = false;
            } catch (StructureException e){
                e.printStackTrace();
                logger.warning(e.getMessage());
            }
        }
        
        
      
        int n = intObjectIds.length;
        for (int i=0;i<n;i++){
            if ( i == firstPosition )
                continue;
            
            if ( selection[i]){
                try {
                    Structure s = getStructure(i);
                    if ( first){
                        newStruc.setPDBCode(s.getPDBCode());
                        first = false;
                    }
                    List chains = s.getChains(0);
                    newStruc.addModel(chains);
                } catch (Exception e){
                    logger.warning(e.getMessage());
                }
                
            }
            
        }
        return newStruc;
    }
    
    /** re turns the color for a particular PDB file
     * 
     * @param position
     * @return Color
     */
    public Color getColor(int position){
    	
    	if ( position == 0 ) {
    		if ( coordSys.toString().equals("CASP,Protein Structure")){
    			return Color.white;
    		}
    	}
    	
         float stepsize   = 0;
         
         // do a circle of 6 colors...
         
         int NR_COLS = 6;
         stepsize = 1.0f / (float)NR_COLS ;
         float saturation = 1.0f;
         float brightness = 1.0f;
        
        Integer selectionPosition = new Integer(position);

        if (!  selectionOrder.contains(selectionPosition)) {
            int pos = ( position % NR_COLS );
            
            //System.out.println("color: position " + position + " nrSel " + nrSelected + " " + pos );
            
            float hue = ( pos * stepsize );
            Color col = Color.getHSBColor(hue,saturation,brightness);
            return col;
            
        } else {           
            
            int selectPos = selectionOrder.indexOf(selectionPosition);
            int pos = ( selectPos % NR_COLS);
            float hue = ( pos * stepsize );
            Color col = Color.getHSBColor(hue,saturation,brightness);
            return col;
            
        }
    }
    
    
    /** convert the internal objecet ids to accession codes
     * 
     * @param pos
     * @return the accession code for object nr X.
     */
    private String getAccesionCodeForObject(int pos){
    	
    	//TODO:  move these rules to a config file!
    	
    	//TODO: the SISYPHUS should acutally use the dbAccesionId codes as foreign IDS
    	//TODO:  update the SISYPHUS das server
    	
    	String pdbCode = "";
    	
    	if ((coordSys != null) &&( coordSys.toString().equals("CASP,Protein Structure")))
    		pdbCode = accessionCodes[pos];
    	
    	else {
    		pdbCode = intObjectIds[pos].substring(0,4);
    	}
    	
    	return pdbCode;

    }
   
    
    
    public Structure getStructure(int pos) throws StructureException{
        
    	if ( loaded[pos])
            return returnStructureOrRange(pos,structures[pos]);
        
    	
    	String pdbCode = getAccesionCodeForObject(pos);
    	
        
        // show busy frame...
        ProgressThreadDrawer drawer = new ProgressThreadDrawer(pdbCode);
        
        //javax.swing.SwingUtilities.invokeLater(drawer);
        //drawer.start();
        drawer.showProgressFrame();
        
        
        Structure s = null;
        
        /*StructureThread sthread = new StructureThread(pdbCode,structureServers);
        sthread.addStructureListener(new MyStructureListener(this));
        waitingForStructure = true;
        sthread.start();
       
        while ( waitingForStructure){
            try {
                System.out.println("waitingForStructure " + waitingForStructure);
                wait(100);             
            } catch (InterruptedException ex){
                waitingForStructure = false;
            }
        }*/
        
        
        
        
        // not loaded, yet. do a structure request, and rotate, shft structure.
        for (int i=0;i< structureServers.length;i++){
            Das1Source ds = structureServers[i];
            String dasstructurecommand = ds.getUrl() + "structure?model=1&query=";
            DASStructureClient dasc= new DASStructureClient(dasstructurecommand);
            
            try {
                s = dasc.getStructureById(pdbCode);
                if ( s != null) {
                	if ( s.size() > 0)
                		break;
                }
            } catch (IOException e){
                continue;
            }
        }
        
        drawer.terminate();
        
        if ( s == null)
            throw new StructureException("Could not load structure at position " + pos + " " + pdbCode);
        
        
        
        
        // rotate, shift structure...
        Matrix m = matrices[pos];
        Atom vector = shiftVectors[pos];
        
        //System.out.println("applying rotation matrix");
       // m.print(3,3);
        
        Calc.rotate(s,m);
        Calc.shift(s,vector);
        structures[pos] = s;
        loaded[pos] = true;
        
        
        Structure tmp = returnStructureOrRange(pos,s); 
        if ( tmp == null)
            throw new StructureException("could not getStructureOrRange");
        return tmp;
        
    }
    
    private Structure returnStructureOrRange(int pos, Structure s){
      
        String property = SpiceMenuListener.structureDisplayProperty;
        
        String val = System.getProperty(property);
        //System.out.println("in struc alig:" + val);
        Structure ret = s;
        if ( ( val == null) || (val == "show region")) { 
            try {
                Structure newStruc  = getStructureRanges(pos, s);
                if (( newStruc != null) && ( newStruc.size() > 0)) {
                    ret = newStruc;
                }
            } catch (StructureException e){
                e.printStackTrace();
                
            }
        }
        return ret;
    }
    
    /** return the structure object restricted to the ranges given in the ALignment
     * if no ranges give, returns null
     * @param pos
     * @param s
     * @return nu; if no ranges given, otherwise restricts the structure to the ranges.
     * @throws StructureException
     */
    private Structure getStructureRanges(int pos , Structure s ) throws StructureException{
        
        if (alignment == null)
            return null;
        logger.fine("get structure range for " + s.getPDBCode());
        
        // check if the alignment has an object detail "region" for this
        // if yes = restrict the used structure...
        Annotation[] objects = alignment.getObjects();
        Annotation object = objects[pos];
        //System.out.println(object);
        Map protectionMap = new HashMap();
        
        if ( object.containsProperty("details")){

            List details = (List) object.getProperty("details");
              
            for ( int det = 0 ; det< details.size();det++) {
                Annotation detanno = (Annotation) details.get(det);
                String property = (String)detanno.getProperty("property");
                if (! property.equals("region"))
                    continue;
                
                
                String detail = (String) detanno.getProperty("detail");
                //System.out.println("got detail " + detail);
              
                
                // split up the structure and add the region to the new structure...
                int cpos = detail.indexOf(":");
                String chainId = " ";
                
                if ( cpos > 0) {
                    chainId = detail.substring(0,cpos);
                    detail  = detail.substring(cpos+1,detail.length());
                } else {
                    detail = detail.substring(1,detail.length());
                }
                
                //System.out.println(detail + " " + cpos + " " + chainId);
                
                String[] spl = detail.split("-");
                
                if ( spl.length != 2)
                    continue;
                String start = spl[0];
                String end   = spl[1];
                //System.out.println("start " + start + " end " + end);
                
                
                Object prot = protectionMap.get(chainId);

                List protectedResidues;
                if ( prot == null)
                    protectedResidues = new ArrayList();
                else {
                    protectedResidues = (List) prot;
                }
                
                Chain c = s.getChainByPDB(chainId);
                
                //TODO: Q: do we need to do  all groups or only the aminos??
                List groups = c.getGroups();
                
                Iterator iter = groups.iterator();
                boolean known =false;
                
                while (iter.hasNext()){
                    Group g = (Group) iter.next();
                    if (g.getPDBCode().equals(start)){
                        known = true;
                    }
                    
                    Group n = (Group) g.clone();
                    
                    String code = n.getPDBCode();
                    
                    if ( known) 
                        protectedResidues.add(code);
                    
                    
                    if (g.getPDBCode().equals(end)){
                        known = false;
                    }
                    
                }
                protectionMap.put(chainId,protectedResidues);
                
                /*Group[] groups = c.getGroupsByPDB(start,end);
                 
                 Chain nc = new ChainImpl ();
                 nc.setName(chainId);
                 boolean knownChain = false;
                 try {
                 nc = newStruc.findChain(chainId);
                 knownChain = true;
                 
                 } catch (Exception e){}
                 
                 for (int g=0;g<groups.length;g++){
                 Group gr = groups[g];
                 nc.addGroup(gr);
                 
                 
                 }
                 */
            }             
        }
        
        // o.k. here we got all residues in the protectedMap 
        // now we build up the new structure ...
        
        Structure newStruc = new StructureImpl();
        newStruc.setPDBCode(s.getPDBCode());
        newStruc.setHeader(s.getHeader());
        
        Set keys = protectionMap.keySet();
        
        Iterator iter = keys.iterator();
        while (iter.hasNext()){
            String chainId =(String) iter.next();
            List residues = (List)protectionMap.get(chainId);
            
            Chain origChain = s.findChain(chainId);
            Chain newChain  = new ChainImpl();
            newChain.setName(chainId);
            
            //TODO: Q: do we need to do  all groups or only the aminos??
            List origGroups = origChain.getGroups();
            Iterator giter = origGroups.iterator();
            
            while (giter.hasNext()){
              Group orig = (Group) giter.next();
              Group n = (Group) orig.clone();
              if ( ! residues.contains(orig.getPDBCode())){
                  // this group has been
                  n.clearAtoms();

              }
              newChain.addGroup(n);              
            }
            newStruc.addChain(newChain);                        
        }
        
        if (newStruc.size() > 0){
            return newStruc;
        } else
            return null;
        
    }
    
    private Annotation[] sortBlocks(Annotation[] blockx){
        // sort blocks
        Annotation[] blocks = new Annotation[blockx.length];
        
        for (int i = 0; i < blockx.length; i++) {
            boolean found = false;
            for (int bb = 0 ; bb <blockx.length;bb++){
                Annotation blo = blockx[bb];
                String bo = (String) blo.getProperty("blockOrder");
                int boi = Integer.parseInt(bo);
                if (boi == i+1) {
                    blocks[i] = blo;
                    found = true;
                }
            }
            if ( ! found){
                System.out.println("did not find blockOrder "+i);
            }
        }
        return blocks;
    }
    
    public boolean isLoaded(int pos){
        return loaded[pos];
    }
    
    public void setLoaded(int pos, boolean flag){
        loaded[pos] = flag;
    }
    
    
    public boolean isSelected(int pos){
        return selection[pos];
    }
    
    public void setSelected(int pos, boolean flag){
        selection[pos] = flag;
    }

    /** convert the Matrix annotation to a Matrix
     * 
     * @param anno
     * @return rotation Matrix 
     */
    private Matrix getMatrix(Annotation anno){
        Matrix max = new Matrix(3,3);
        
        for(int x=1;x<4;x++){
            for(int y=1;y<4;y++){
                String m = "mat"+x+y;
                String val = (String)anno.getProperty(m);
                double d = Double.parseDouble(val);
                max.set(x-1,y-1,d);
            }
        }
        
        return max;
    }    
}



class ProgressThreadDrawer extends Thread {

    JProgressBar progress;
    static int interval = 100;
    JFrame frame;
    String pdbCode;
    boolean terminated ;
    
    public ProgressThreadDrawer(String pdb) {
        frame = new JFrame("loading structure ...");
        pdbCode = pdb;
        
        terminated = false;
        
        frame.addWindowListener(new WindowListener(){
            public void windowClosing(WindowEvent arg0) {
                terminate();                
            }
            public void windowOpened(WindowEvent arg0) {
             }

            public void windowClosed(WindowEvent arg0) {
             }

            public void windowIconified(WindowEvent arg0) {
             }

            public void windowDeiconified(WindowEvent arg0) {
             }

            public void windowActivated(WindowEvent arg0) {
             }

            public void windowDeactivated(WindowEvent arg0) {
                
            }
        });
    }
    
    
    public void terminate(){
        terminated = true;
        if (progress != null)
            progress.setIndeterminate(false);
        if ( frame != null) {
            frame.setVisible(false);
            frame.dispose();
        }
    }
    
    public void showProgressFrame(){
        
       JFrame progressFrame = frame;
        
        ImageIcon icon = SpiceApplication.createImageIcon("spice16x16.gif");
        if (icon != null) {
            progressFrame.setIconImage(icon.getImage());
        }
                
        JFrame.setDefaultLookAndFeelDecorated(false);
   
        
        JPanel panel = new JPanel();
        panel.setBackground(SequenceScalePanel.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        Box vbox = Box.createVerticalBox();
        JLabel txt = new JLabel("loading structure " + pdbCode); 
        vbox.add(txt);
        
        JProgressBar progressBar = new JProgressBar(0,100);
        progressBar.setStringPainted(true); //get space for the string
        progressBar.setString("");          //but don't paint it
        progressBar.setIndeterminate(true);
        progressBar.setValue(0);
        progressBar.setMaximumSize(new Dimension(400,20));
        progressBar.setBorder(BorderFactory.createEmptyBorder());
                
        progressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        
        //progressBar.setMaximum(100);
        //progressBar.setValue(50);
        
        vbox.add(progressBar);
        
        //JLabel server = new JLabel("contacting "+REGISTRY, JLabel.RIGHT);
        //logger.info("contacting DAS registry at "+REGISTRY);
        //vbox.add(server);
        panel.add(vbox);
        progressFrame.getContentPane().add(panel);
        progressFrame.pack();
        
        // get resolution of screen
        Dimension dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        // Determine the new location of the window
        int w = progressFrame.getSize().width;
        int h = progressFrame.getSize().height;
        int x = (dim.width-w)/2;
        int y = (dim.height-h)/2;
        
        // Move the window
        progressFrame.setLocation(x, y);
        progressFrame.repaint();
        
        progressFrame.setVisible(true);        
       
        progress =progressBar;
    }
    
    
    public void run() {
        //System.out.println("drawer started");
        showProgressFrame();
        
        boolean finished = false;
        while ( ! finished) {
            try {
                //System.out.println("repainting frame");
                progress.repaint();
                frame.repaint();
               
                if ( !terminated){
                    finished =false;
                    break;
                }
                
                sleep(interval);
            } catch (InterruptedException e){
            }
            progress.repaint();
        }
        progress = null;    
        terminate();
    }
    
}


