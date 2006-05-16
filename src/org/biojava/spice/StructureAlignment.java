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
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.io.DASStructureClient;
import org.biojava.bio.structure.jama.Matrix;
import org.biojava.dasobert.dasregistry.Das1Source;
import org.biojava.spice.Config.ConfigGui;
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
    
    public StructureAlignment() {
        super();
        
        structureServers = new Das1Source[0];
        structures = new Structure[0];
        selection = new boolean[0];
        loaded = new boolean[0];
        sortedBlocks = new Annotation[0];
        
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
        
        
        
        matrices     = new Matrix[n];
        shiftVectors = new Atom[n];
        structures   = new Structure[n];
        intObjectIds = new String[n];
        selection    = new boolean[n];
        loaded       = new boolean[n];
        
        for (int i=0;i< n;i++){
            
            Annotation a = maxs[i];
            matrices[i] = getMatrix(a);
            
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
    }
    
    public void deselect(int pos){
        selection[pos] = false;
        
    }
    
    public String getRasmolScript(){
        String cmd = "select *; backbone 0.3;";
        
        float stepsize   = 1.0f / (float)intObjectIds.length;
        float saturation = 1.0f;
        float brightness = 1.0f;
        int modelcount = 0;
        
        for ( int p=0;p<selection.length;p++){
            
            if (! selection[p])
                continue;
            
            String intId = intObjectIds[p];
            modelcount ++;
            
            float hue = p * stepsize;
            Color col = Color.getHSBColor(hue,saturation,brightness);
            
            
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
     * @return
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
            return structures[pos];
        
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
        
        return s;
        
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
     * @return
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
