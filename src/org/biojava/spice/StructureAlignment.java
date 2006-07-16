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
package org.biojava.spice;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.biojava.spice.gui.SpiceMenuListener;
import org.biojava.spice.manypanel.renderer.ScalePanel;

public class StructureAlignment {
    
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    
    Alignment alignment;
    
    Structure[] structures;
    Matrix[] matrices;
    Atom[] shiftVectors;
    String[] intObjectIds;
    boolean[] selection;
    boolean[] loaded ;
    Das1Source[] structureServers;
    JFrame progressFrame;
    Annotation[] sortedBlocks;
    int nrSelected;
    
    public StructureAlignment() {
        super();
        
        structureServers = new Das1Source[0];
        structures = new Structure[0];
        selection = new boolean[0];
        loaded = new boolean[0];
        sortedBlocks = new Annotation[0];
        nrSelected = 0;
    }
    
    public void setStructureServers(Das1Source[] servers){
        structureServers = servers;
    }
    
    public Alignment getAlignment() {
        return alignment;
    }
    
    public void setAlignment(Alignment alignment) throws StructureException {
        this.alignment = alignment;
        
        // convert the alignment object into the internal matrices, vectors
        Annotation[] maxs = alignment.getMatrices();
        
        Annotation[] objects = alignment.getObjects();
        Annotation[] vectors = alignment.getVectors();
        Annotation[] blockx  = alignment.getBlocks();
        //Annotation[] blocks = new Annotation[blockx.length];
        sortedBlocks = sortBlocks(blockx);
        
        int n = objects.length;
        
        if ( maxs.length != n) {
            throw new StructureException("number of rotation matrices ("+maxs.length+
                    ") does not match number of objects ("+n+") !");
            
        }
        
        if ( vectors.length != n){
            throw new StructureException("number of shift vectors ("+vectors.length+
                    ") does not match number of objects ("+n+") !");
        }
        
        
        // remove objects that have a Matrix object that contains all zeros!
        Matrix zero =  Matrix.identity(3,3);
        for (int i=0;i<3;i++ ){
            zero.set(i,i,0.0);
        }
        //zero.print(3,3);
        List objectNew   = new ArrayList();
        List matricesNew = new ArrayList();
        List vectorsNew  = new ArrayList();
      
        
        for (int i = 0 ; i< n ; i++){
            Annotation a = maxs[i];
            Matrix m = getMatrix(a);
           // System.out.println(objects[i]+ ":");
            //m.print(3,3);
            boolean zeroValues = true;
            for ( int x=0;x<3;x++){
                for ( int y=0;y<3;y++){
                    double val = m.get(x,y);
                    if ( val != 0.0){
                        zeroValues = false;
                        x=3;
                        y=3;
                    }
                }
            }
            if ( zeroValues == true){
                // something went wrong during creation of rotmat
                // ignore the whole object!
               // System.out.println("matrix is zero:"  + objects[i]);
                m.print(3,3);
                continue;
            }
            objectNew.add(objects[i]);
            matricesNew.add(m);
            vectorsNew.add(vectors[i]);
       
        }
        // copy the data back ...
        objects      = (Annotation[]) objectNew.toArray(   new Annotation[objectNew.size()]);
        matrices     = (Matrix[])     matricesNew.toArray( new Matrix[matricesNew.size()]);
        vectors      = (Annotation[]) vectorsNew.toArray(  new Annotation[vectorsNew.size()]);
      
        n = objects.length;
        nrSelected = 0;
        //matrices     = new Matrix[n];
        shiftVectors = new Atom[n];
        structures   = new Structure[n];
        intObjectIds = new String[n];
        selection    = new boolean[n];
        loaded       = new boolean[n];
        
        for (int i=0;i< n;i++){
            
        
            
            Annotation v = vectors[i];            
            Atom vec = (Atom) v.getProperty("vector");
            shiftVectors[i] = vec; 
            
            structures[i] = null;
            
            intObjectIds[i] = (String)objects[i].getProperty("intObjectId");
            
            selection[i] = false;
            loaded[i]    = false;
        }        
    }
    
    public String[] getIds(){
        return intObjectIds;
    }
    
    public void select(int pos){
        selection[pos] = true;
        nrSelected++;
    }
    
    public void deselect(int pos){
        selection[pos] = false;
        nrSelected--;
        
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
    
    public String getRasmolScript(){
        String cmd = "select *; backbone 0.3;";
        
        
        int modelcount = 0;
        
        for ( int p=0;p<selection.length;p++){
            
            if (! selection[p])
                continue;
            
            String intId = intObjectIds[p];
            modelcount ++;
            
            
            Color col =  getColor(p);
            
            Color chaincol = new Color(col.getRed()/2,col.getGreen()/2,col.getBlue()/2);
            
            cmd += "select */"+modelcount+"; ";
            cmd += " color [" +chaincol.getRed()+","+chaincol.getGreen() +","+chaincol.getBlue() +"];";
            
            for (int b=0;b<sortedBlocks.length;b++){
                Annotation block = sortedBlocks[b];
                
                
                List segments = (List)block.getProperty("segments");
                Iterator siter = segments.iterator();
                while (siter.hasNext()){
                    Annotation seg = (Annotation)siter.next();
                    String ii =  (String)seg.getProperty("intObjectId");
                    
                    if (! ii.equals(intId))
                        continue;
                    
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
        }
        cmd += " model 0;";
        System.out.println(cmd);
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
        Structure newStruc = new StructureImpl();
        
        newStruc.setNmr(true);
        boolean first = true;
        int n = intObjectIds.length;
        for (int i=0;i<n;i++){
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
        float stepsize   = 0;
        //if ( nrSelected > 0 )
        //    stepsize = 1.0f / (float)nrSelected;
        if ( structures.length > 0 )
            stepsize = 1.0f / (float)structures.length * 0.7f;
        float saturation = 1.0f;
        float brightness = 1.0f;
        
        float hue = position * stepsize ;
        Color col = Color.getHSBColor(hue,saturation,brightness);
        return col;
    }
    
    
    private void showProgressFrame(String pdbCode){
        progressFrame = new JFrame();
        progressFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        /*progressFrame.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent evt) {
         Frame frame = (Frame) evt.getSource();
         frame.setVisible(false);
         frame.dispose();
         }
         });
         */
        
        ImageIcon icon = SpiceApplication.createImageIcon("spice16x16.gif");
        if (icon != null) {
            progressFrame.setIconImage(icon.getImage());
        }
        JFrame.setDefaultLookAndFeelDecorated(false);
        //progressFrame.setUndecorated(true);
        
        JPanel panel = new JPanel();
        panel.setBackground(ScalePanel.BACKGROUND_COLOR);
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
        
    }
    private void disposeProgressBar(){
        progressFrame.setVisible(false);
        progressFrame.dispose();
    }
    
    
    public Structure getStructure(int pos) throws StructureException{
        if ( loaded[pos])
            return returnStructureOrRange(pos,structures[pos]);
        
        String pdbCode = intObjectIds[pos].substring(0,4);
        // show busy frame...
        showProgressFrame(pdbCode);
        
        Structure s = null;
        // not loaded, yet. do a structure request, and rotate, shft structure.
        for (int i=0;i< structureServers.length;i++){
            Das1Source ds = structureServers[i];
            String dasstructurecommand = ds.getUrl() + "structure?model=1&query=";
            DASStructureClient dasc= new DASStructureClient(dasstructurecommand);
            
            try {
                s = dasc.getStructureById(pdbCode);
            } catch (IOException e){
                continue;
            }
        }
        disposeProgressBar();
        
        if ( s == null)
            throw new StructureException("Could not load structure at position " + pos);
        
        
        
        
        // rotate, shift structure...
        Matrix m = matrices[pos];
        Atom vector = shiftVectors[pos];
        
        Calc.rotate(s,m);
        Calc.shift(s,vector);
        structures[pos] = s;
        loaded[pos] = true;
        
        
        
        
        return returnStructureOrRange(pos,s);
        
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
                
            }
        }
        return ret;
    }
    
    private Structure getStructureRanges(int pos , Structure s ) throws StructureException{
        
        Structure newStruc = new StructureImpl();
        
        // check if the alignment has an object detail "region" for this
        // if yes = restrict the used structure...
        Annotation[] objects = alignment.getObjects();
        Annotation object = objects[pos];
        if ( object.containsProperty("details")){
            List details = (List) object.getProperty("details");
            
            newStruc.setPDBCode(s.getPDBCode());
            newStruc.setHeader(s.getHeader());
            
            for ( int det = 0 ; det< details.size();det++) {
                Annotation detanno = (Annotation) details.get(det);
                String property = (String)detanno.getProperty("property");
                if ( property.equals("region")){
                    String detail = (String) detanno.getProperty("detail");
                    
                    // split up the structure and add the region to the new structure...
                    int cpos = detail.indexOf(":");
                    String chainId = " ";
                    
                    if ( cpos > 0) {
                        chainId = detail.substring(0,cpos);
                        detail  = detail.substring(cpos+1,detail.length());
                    } else {
                        detail = detail.substring(1,detail.length());
                    }
                    
                    System.out.println(detail + " " + cpos + " " + chainId);
                    
                    String[] spl = detail.split("-");
                    
                    if ( spl.length != 2)
                        continue;
                    String start = spl[0];
                    String end   = spl[1];
                    System.out.println("start " + start + " end " + end);
                  
                    Chain c = s.getChainByPDB(chainId);
                    
                    Chain nc = new ChainImpl ();
                    nc.setName(chainId);
                    boolean knownChain = false;
                    try {
                        nc = newStruc.findChain(chainId);
                        knownChain = true;
                        
                    } catch (Exception e){}
                    
                    
                    List groups = c.getGroups();
                    Iterator iter = groups.iterator();
                    boolean known =false;
                    while (iter.hasNext()){
                        Group g = (Group) iter.next();
                        if (g.getPDBCode().equals(start)){
                            known = true;
                        }
                        
                        Group n = (Group) g.clone();
                        // todo: check multi domain chains -
                        // are regions on the same chain deleted by this?
                        if (! known)
                            n.clearAtoms();
                        
                        if (g.getPDBCode().equals(end)){
                            known = false;
                        }
                        nc.addGroup(n);
                        
                    }
                    if ( ! knownChain)
                        newStruc.addChain(nc);
                    
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
