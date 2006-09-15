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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.spice.config.RegistryConfiguration;
import org.biojava.spice.config.SpiceDefaults;
import org.biojava.spice.manypanel.eventmodel.StructureAlignmentListener;


public class AlignmentGui extends JFrame{

    private final static long serialVersionUID =0l;
    private static String baseName = "alignment";
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    
    ResourceBundle resource;
    
    JTextField f11;
    JTextField f12;
    JTextField f21;
    JTextField f22;
    JButton abortB;
    
    
    JPanel cont ;
    RegistryConfiguration config;
    Thread thread;
    
    
    AlignmentCalc alicalc;
    
    JProgressBar progress;
    List structureAlignmentListeners;
    
    public AlignmentGui(RegistryConfiguration config) {
        super();
        this.config = config;
        
        thread = null;
        
        resource = ResourceBundle.getBundle(baseName);
        String wid = resource.getString("alignment.frame.width");
        String heig = resource.getString("alignment.frame.height");
        
        int w = Integer.parseInt(wid);
        int h = Integer.parseInt(heig);
        
        this.setSize(w,h);
        Dimension prefSize = new Dimension(w,h);
        
        this.setTitle(resource.getString("alignment.frame.title"));
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                JFrame frame = (JFrame) evt.getSource();
                frame.setVisible(false);
                frame.dispose();
            }
        });
        
        
        cont = new JPanel();
        cont.setSize(prefSize);
        cont.setPreferredSize(prefSize);
        
        this.getContentPane().add(cont);
        
        
        cont.setLayout(new GridLayout(5,2));
        
//      row 0 the labels
        JLabel l01 = new JLabel(resource.getString("alignment.mol1.desc"));
        JLabel l02 = new JLabel(resource.getString("alignment.mol2.desc"));
        
        cont.add(l01);
        cont.add(l02);
        
        
        // row 1 the pdb codes
        Box hBox11 = Box.createHorizontalBox();
        
        JLabel l11 = new JLabel(resource.getString("alignment.pdb1.desc"));
        String fieldsize = resource.getString("alignment.pdb.fieldsize");
        int pdbfSize = Integer.parseInt(fieldsize);
        
        f11 = new JTextField(pdbfSize);
        
        f11.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
      
        hBox11.add(l11);
        hBox11.add(Box.createVerticalGlue());
        hBox11.add(f11, BorderLayout.CENTER);
        hBox11.add(Box.createVerticalGlue());
        
        cont.add(hBox11);
        
        JLabel l12 = new JLabel(resource.getString("alignment.pdb2.desc"));
        f12 = new JTextField(pdbfSize);
        f12.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        
        Box hBox12 = Box.createHorizontalBox();
        hBox12.add(l12);
        hBox12.add(Box.createGlue());
        hBox12.add(f12, BorderLayout.CENTER);
        hBox12.add(Box.createGlue());
        
        cont.add(hBox12);
        
        
        // row 2 the (optional) chainIds
        Box hBox21 = Box.createHorizontalBox();
        JLabel l21 = new JLabel(resource.getString("alignment.chain1.desc"));
        f21 = new JTextField(1);
        f21.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        hBox21.add(l21);
        hBox21.add(Box.createGlue());
        hBox21.add(f21, BorderLayout.CENTER);
        hBox21.add(Box.createGlue());
        
        cont.add(hBox21);
        
       
        Box hBox22 = Box.createHorizontalBox();
        JLabel l22 = new JLabel(resource.getString("alignment.chain2.desc"));
        f22 = new JTextField(1);
        f22.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        hBox22.add(l22);
        hBox22.add(Box.createGlue());
        hBox22.add(f22);
        hBox22.add(Box.createGlue());
        
        cont.add(hBox22);
        
        
        //  row 3  the status
        Action action3 = new AbstractAction(resource.getString("alignment.abort.txt")) {
            public static final long serialVersionUID = 0l;
            // This method is called when the button is pressed
            public void actionPerformed(ActionEvent evt) {
                // Perform action...
                abortCalc();
            }
        };
        abortB = new JButton(action3);
        
        abortB.setEnabled(false);
        
        Box hBox41 = Box.createHorizontalBox();
        hBox41.add(Box.createGlue());
        hBox41.add(abortB);
        hBox41.add(Box.createGlue());
        
        cont.add(hBox41);
        
        
        
       Box hBox42 = Box.createHorizontalBox();
        progress =new JProgressBar();
        
        hBox42.add(Box.createGlue());
        hBox42.add(progress);
        hBox42.add(Box.createGlue());
        cont.add(hBox42);
        progress.setIndeterminate(false);
        
        
        // row 4 the submit buttons
        
       
        
        
        Action action2 = new AbstractAction(resource.getString("alignment.close.txt")) {
            public static final long serialVersionUID = 0l;
            // This method is called when the button is pressed
            public void actionPerformed(ActionEvent evt) {
                // Perform action...
                abortCalc();
                dispose();
            }
        };
        JButton closeB = new JButton(action2);
        
        Box hBox31 = Box.createHorizontalBox();     
        hBox31.add(Box.createGlue());
        hBox31.add(closeB);
        hBox31.add(Box.createGlue());
        
        cont.add(hBox31);
        
        
        Action action1 = new AbstractAction(resource.getString("alignment.submit.txt")) {
            public static final long serialVersionUID = 0l;
            // This method is called when the button is pressed
            public void actionPerformed(ActionEvent evt) {
                // Perform action...
                //System.out.println("calc structure alignment");
                calcAlignment();
                
            }
        };
        JButton submitB = new JButton(action1);
        
        Box hBox32 = Box.createHorizontalBox();
        hBox32.add(Box.createGlue());
        hBox32.add(submitB);
        hBox32.add(Box.createGlue());
        cont.add(hBox32);
        
        
      
        
        structureAlignmentListeners = new ArrayList();
    }
    
    public void addStructureAlignmentListener(StructureAlignmentListener li) {
        structureAlignmentListeners.add(li);
    }
    
    public void clearListeners(){
        structureAlignmentListeners.clear();
    }
    
    public void cleanUp() {
        structureAlignmentListeners.clear();
        if ( alicalc != null) {
            alicalc.cleanup();
        }
    }
    private  void calcAlignment(){
        
        if ( this.thread != null ){
            // already running
            logger.info("alread running a calculation. please wait");
            return;
        }
        
        
        String pdb1 = f11.getText();
        String pdb2 = f12.getText();
        
        if ( pdb1.length() < 4) {
            f11.setText("!!!");
            if ( pdb2.length() < 4) 
                f12.setText("!!!");
            
            return;
        }
        if ( pdb2.length() < 4) {
            f12.setText("!!!");                
            return;
        }
        
        String chain1 = f21.getText();
        String chain2 = f22.getText();
        
        
        List servs =  config.getServers("structure",SpiceDefaults.PDBCOORDSYS);
        
        SpiceDasSource[] structureServers = (SpiceDasSource[] ) servs.toArray(new SpiceDasSource[servs.size()]);
        
        
        
        alicalc = new AlignmentCalc(this,pdb1,chain1, pdb2, chain2, structureServers);
        
        Iterator iter = structureAlignmentListeners.iterator();
        while (iter.hasNext() ){
            StructureAlignmentListener li = (StructureAlignmentListener) iter.next();
            alicalc.addStructureAlignmentListener(li);
        }
        
        
        thread = new Thread(alicalc);
        thread.start();
        abortB.setEnabled(true);
        progress.setIndeterminate(true);
        ProgressThreadDrawer drawer = new ProgressThreadDrawer(progress);
        drawer.start();
        
    }
    
    public void notifyCalcFinished(){
        abortB.setEnabled(false);
        thread = null;
        progress.setIndeterminate(false);
        this.repaint();
    }
    
    private void abortCalc(){
        if ( alicalc != null )
            alicalc.interrupt();
        
    }

    
    
}

class ProgressThreadDrawer extends Thread {

    JProgressBar progress;
    static int interval = 100;
    
    public ProgressThreadDrawer(JProgressBar progress) {
        this.progress = progress;
    }
    
    
    public void run() {
        boolean finished = false;
        while ( ! finished) {
            try {
                progress.repaint();
                if ( ! progress.isIndeterminate() ){
                    finished =false;
                    break;
                }
                
                sleep(interval);
            } catch (InterruptedException e){
            }
            progress.repaint();
        }
        progress = null;       
    }
    
}
