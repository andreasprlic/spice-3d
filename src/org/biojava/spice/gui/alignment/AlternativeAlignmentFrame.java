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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.AtomImpl;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.align.pairwise.AlternativeAlignment;
import org.biojava.bio.structure.jama.Matrix;
import org.biojava.spice.StructureAlignment;
import org.biojava.spice.manypanel.eventmodel.StructureAlignmentListener;

/** a frame showing the alternative alignments, which are the result of a structure superimposition
 * 
 * @author Andreas Prlic
 * @since 8:04:29 PM
 * @version %I% %G%
 */
public class AlternativeAlignmentFrame 
extends JFrame{

    private static final long serialVersionUID=0l;
    private static String baseName = "alignment";
    private ResourceBundle resource;
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    AlternativeAlignment[] aligs;
    JPanel panel;
    
    Structure structure1;
    Structure structure2;
    List alignmentListeners;
    
    public AlternativeAlignmentFrame(Structure s1, Structure s2) {
        super();
        panel = new JPanel();
        this.getContentPane().add(panel);
        resource = ResourceBundle.getBundle(baseName);
    
        structure1 = s1;
        structure2 = s2;
        String pdb1= s1.getPDBCode();
        String pdb2 = s2.getPDBCode();
        
        String t = resource.getString("alignment.resultframe.title");
        Object[] args = {pdb1,pdb2};
        
        String title =  MessageFormat.format(t,args);
        this.setTitle(title);
        alignmentListeners = new ArrayList();
    }
    
    public StructureAlignmentListener[] getStructureAlignmentListeners() {
        return (StructureAlignmentListener[]) alignmentListeners.toArray(new StructureAlignmentListener[alignmentListeners.size()]);
    }


    public void addStructureAlignmentListener(StructureAlignmentListener alignmentListener) {
        alignmentListeners.add( alignmentListener);
    }
    
    
    public void clearListeners(){
        alignmentListeners.clear();        
    }
    
    public void cleanUp(){
        clearListeners();
    }
    
    public void setAlternativeAlignments(AlternativeAlignment[] aligs) {
        this.aligs = aligs;
        panel.removeAll();
        
        Box vBox = Box.createVerticalBox();
        panel.add(vBox);
        for ( int i=0;i< aligs.length;i++){
            Box hBox = Box.createHorizontalBox();
            vBox.add(hBox);
            AlternativeAlignment alig = aligs[i];
            String txt =alig.getEqr() + " " + alig.getScore() + 
            " " + alig.getRms() + " " + alig.getEqr();
            JLabel lab = new JLabel(txt);
            hBox.add(lab);
            
            String t = resource.getString("alignment.resultframe.show");
            
            Action action1 = new MyButtonAction(t,this,i);
            JButton but = new JButton(action1);
            hBox.add(but);
        }
        
    }
    
    protected void showAlternative(int position){
        if ( position > aligs.length){
            return;
        }
        AlternativeAlignment alig = aligs[position];
        logger.info("display alternative alignment " + (position +1));
        
        // create the structure alignment object and tell the listeners ...
        
        StructureAlignment salig = new StructureAlignment();
       
        salig.setLoaded(new boolean[] {true,true});
        
        Matrix m1 = Matrix.identity(3,3);
        Matrix m2 = alig.getRotationMatrix();
        
        salig.setMatrices(new Matrix[]{m1,m2});
        
        String pdb1 = structure1.getPDBCode();
        String pdb2 = structure2.getPDBCode();
        salig.setIntObjectIds(new String[]{pdb1,pdb2});
        
        salig.setSelection(new boolean[] {true,true});
        
        Atom shift1 = new AtomImpl();
        shift1.setCoords(new double[]{0,0,1});
        Atom shift2 = alig.getShift();
        salig.setShiftVectors(new Atom[] {shift1,shift2});
        
        Structure s3 = (Structure)structure2.clone();
       
        Calc.rotate(s3,m2);
        Calc.shift(s3,shift2);
        
        salig.setStructures(new Structure[] {structure1, s3});
        
        String[] scripts = createRasmolScripts(alig,salig);
        salig.setRasmolScripts(scripts);
        
        triggerNewAlignment(salig);
    }
    
    private String[] createRasmolScripts(AlternativeAlignment alig,StructureAlignment salig){
        String[] scripts = new String[2];
        
        Color col1 = salig.getColor(0);
        Color col2 = salig.getColor(1);
        
        Color chaincol1 = new Color(col1.getRed()/2,col1.getGreen()/2,col1.getBlue()/2);
        Color chaincol2 = new Color(col2.getRed()/2,col2.getGreen()/2,col2.getBlue()/2);
        
        String cmd1 = "";
        String cmd2 = "";
        
        cmd1 += "select */"+1+"; ";
        cmd1 += " color [" +chaincol1.getRed()+","+chaincol1.getGreen() +","+chaincol1.getBlue() +"];";
        
        cmd2 += "select */"+2+"; ";
        cmd2 += " color [" +chaincol2.getRed()+","+chaincol2.getGreen() +","+chaincol2.getBlue() +"];";
        
        cmd1 += "select ";
        cmd2 += "select ";
        
        String[] pdb1s = alig.getPDBresnum1();
        String[] pdb2s = alig.getPDBresnum2();
        
        
        for ( int i =0 ; i< pdb1s.length;i++){
            
            String p1 = pdb1s[i];
            String p2 = pdb2s[i];
            
            cmd1 += p1 +"/1";
            cmd2 += p2 +"/2";
            
            if ( i <= pdb1s.length -2){
                cmd1 += ",";
                cmd2 += ",";
            }
        }
        
        cmd1 += "; color [" +col1.getRed()+","+col1.getGreen() +","+col1.getBlue() +"];";
        cmd1 += " backbone 0.6;";   
        
        cmd2 += "; color [" +col2.getRed()+","+col2.getGreen() +","+col2.getBlue() +"];";
        cmd2 += " backbone 0.6;";   
                
        //System.out.println(cmd1);
        scripts[0] = cmd1;
        scripts[1] = cmd2;

        return scripts;
    }
   
    
    private void triggerNewAlignment(StructureAlignment alig){
        Iterator iter = alignmentListeners.iterator();
        while (iter.hasNext()){
            StructureAlignmentListener li = (StructureAlignmentListener)iter.next();
            li.setStructureAlignment(alig);
        }
    }

}

class MyButtonAction extends AbstractAction {
    
    private static final long serialVersionUID =0l;
    AlternativeAlignmentFrame parent;
    int pos;
    public MyButtonAction(String text, AlternativeAlignmentFrame parent, int position){
        super(text);
        this.parent = parent;
        this.pos = position;
    }
    
    public void actionPerformed(ActionEvent evt) {
        // Perform action...
        parent.showAlternative(pos);
    }
}
