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
 * Created on Dec 6, 2005
 *
 */
package org.biojava.spice.manypanel.renderer;



//import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.biojava.spice.JNLPProxy;

public class StatusPanel extends JPanel {
    public static final long serialVersionUID= 309781239871208973l;
    
    JTextField dbName;
    JTextField accessionCode;
    JTextField description;
    JProgressBar progressBar;
    ArrowPanel arrowPanel;
    
    public static String PDBLINK     = "http://www.rcsb.org/pdb/cgi/explore.cgi?pdbId=";
    public static String UNIPROTLINK = "http://www.ebi.uniprot.org/uniprot-srv/uniProtView.do?proteinAc=" ;
    
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    static Color BG_COLOR = Color.WHITE;
    
   
    
    public StatusPanel() {
        super();

        this.setBackground(BG_COLOR);
        //this.setBorder(BorderFactory.createEmptyBorder());
        Box hBox =  Box.createHorizontalBox();
        hBox.setOpaque(true);
        hBox.setBackground(BG_COLOR);

        dbName = new JTextField("");
        dbName.setEditable(false);
        dbName.setBorder(BorderFactory.createEmptyBorder());
        dbName.setMaximumSize(new Dimension(120,20));
        dbName.setPreferredSize(new Dimension(120,20));
        dbName.setBackground(BG_COLOR);
        hBox.add(dbName);
        
        arrowPanel = new ArrowPanel();
        hBox.add(arrowPanel);
        arrowPanel.setBackground(BG_COLOR);
        arrowPanel.setMaximumSize(new Dimension (120,20));
        arrowPanel.setPreferredSize(new Dimension(120,20));
        accessionCode = new JTextField("    ");
        accessionCode.setEditable(false);
        
        accessionCode.setBorder(BorderFactory.createEmptyBorder());
        accessionCode.setMaximumSize(new Dimension(120,20));
        accessionCode.setPreferredSize(new Dimension(120,20));
        accessionCode.setBackground(BG_COLOR);
       
        
        MouseListener mousiPdb = new PanelMouseListener(this,PDBLINK);
        // mouse listener         
        accessionCode.addMouseListener(mousiPdb);
        
        hBox.add(accessionCode);
        //hBox.add(pdbCode,BorderLayout.WEST);

        // pdb description
         
        description = new JTextField("");
        description.setBorder(BorderFactory.createEmptyBorder());
        description.setEditable(false);
        description.setMaximumSize(new Dimension(150,20));
        description.setBackground(BG_COLOR);
        hBox.add(description);   
        
        //TODO: enable this again
        /*
        DescMouseListener descMouseListener = new DescMouseListener();
        
          //pdbdescMouseListener.setPDBHeader(new HashMap());
       
        pdbdescMouseListener.setHeader(pdbheader);
        pdbDescription.addMouseListener(pdbdescMouseListener);
        pdbDescription.addMouseMotionListener(pdbdescMouseListener);
        
        hBox.add(pdbDescription);
        */
        hBox.add(Box.createHorizontalGlue());
        
        
        progressBar = new JProgressBar(0,60);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setString(""); 
        progressBar.setMaximumSize(new Dimension(10,20));
        progressBar.setIndeterminate(false);
        progressBar.setBorder(BorderFactory.createEmptyBorder());
        hBox.add(progressBar);
        progressBar.setVisible(false);
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        this.add(hBox);
        
       
    }
    
    public ArrowPanel getArrowPanel(){
        return arrowPanel;
    }
    
  
    
    
    
    public void setAccessionCode(String ac){
        accessionCode.setText(ac);
        accessionCode.repaint();
        
    }
    
    public void setDescription(String d){
        description.setText(d);
        description.repaint();
     
    }
    
    public void setName(String n){
        dbName.setText(n);
        
       
    }
    
    public void setLoading(boolean flag){
        progressBar.setIndeterminate(flag);
	if ( flag )
	    progressBar.setVisible(true);
	else 
	    progressBar.setVisible(false);
        progressBar.repaint();
    }
    

}


class PanelMouseListener
implements MouseListener
{
    
    StatusPanel parent;
    String caller;
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    PanelMouseListener(StatusPanel parent_,String caller_){
        
        parent=parent_;
        caller=caller_;
        
    }
    
    public void mouseClicked(MouseEvent e){
        JTextField source = (JTextField)  e.getSource();
        
        try {
            URL url = new URL(caller+source.getText());
            showDocument(url);
        } catch ( Exception ex){
            
        }
        
        
    }
    public void mouseExited(MouseEvent e){
        // remove tooltip
        JTextField source = (JTextField)e.getSource();
        source.setToolTipText(null);
    }
    public void mouseEntered(MouseEvent e){
        // display tooltip
        JTextField source = (JTextField)e.getSource();
        source.setToolTipText("click to open in browser");
        
    }
    public void mouseReleased(MouseEvent e){}
    public void mousePressed(MouseEvent e){
        
        
     
    }
    public boolean showDocument(URL url) 
    {
        if ( url != null ){
            boolean success = JNLPProxy.showDocument(url); 
            if ( ! success)
                logger.info("could not open URL "+url+" in browser. check your config or browser version.");
        return success;
        
        }
        else
            return false;
    }
    
    public boolean showDocument(String urlstring){
        try{
            URL url = new URL(urlstring);
            
            return showDocument(url);
        } catch (MalformedURLException e){
            logger.warning("malformed URL "+urlstring);
            return false;
        }
    }

    
    
}



/** a class responsible of creating afloating frame
 *  if the mouse is moved over the description of the PDB file */
class DescMouseListener implements MouseListener, MouseMotionListener {
    Map pdbHeader ;
    boolean frameshown ;
    
    JFrame floatingFrame;
    public DescMouseListener(){
        super();
        pdbHeader = new HashMap();
        frameshown = false;
    }
    
    private void displayFrame() {
        if ( frameshown ) {
            return;
        }
        
        floatingFrame = new JFrame();
        JFrame.setDefaultLookAndFeelDecorated(false);
        floatingFrame.setUndecorated(true);
        updateFrameContent(pdbHeader);       
        
                
        frameshown = true;
        
    }
    
    private void disposeFrame(){
        if ( ! frameshown ){
            return;
        }
        
        floatingFrame.setVisible(false);
        floatingFrame.dispose();
        
        frameshown = false;
    }
    
    private void updateFrameContent(Map h){
        
        if ( h == null )h = new HashMap();
        
        String t = "<html><body><table>";
        Set s = h.keySet();
        Iterator iter = s.iterator();
        while (iter.hasNext()){
            String key = (String) iter.next();
            String value = (String)h.get(key);
            t+="<tr><td>"+key+"</td><td>"+value+"</td></tr>";
        }
        t+="</table></body></html>";
        
        JEditorPane txt = new JEditorPane("text/html",t);
        txt.setEditable(false);
        
        floatingFrame.getContentPane().add(txt);
        floatingFrame.pack();
    }
    
    private void updateFramePosition(MouseEvent e){
        if ( ! frameshown){
            return;
        }
       int x = e.getX();
       int y = e.getY();
       // get parent components locations
       Component compo = e.getComponent();
       Point screenTopLeft = compo.getLocationOnScreen();
       int cx = screenTopLeft.x;
       int cy = screenTopLeft.y;
       
       int compo_h = compo.getHeight();
       //floatingFrame.setLocationRelativeTo(compo);
        
        // update the position of the frame, according to the mouse position
       //System.out.println((cx-x-5)+" " + (cy+ y+5)+" x:" + x + " y:" + y +
         //      " cx:" + cx + " cy:" + cy + " c_h:"+ compo_h  );
        
        Dimension d = floatingFrame.getSize();
        int dx = d.width;
        //int dy = d.height;
        
        int posx = cx + x  - ( dx/2)    ;
        int posy = cy + y + compo_h + 5 ;
        
        floatingFrame.setLocation(posx,posy);
    }
    
    public void setPDBHeader(Map h ){
        pdbHeader = h;
        if ( frameshown){
            updateFrameContent(h);
        }
    }
    
//  for mousemotion:
    public void mouseDragged(MouseEvent e){
        
    }
    
    public void mouseMoved(MouseEvent e){
        
        if ( frameshown) {
            updateFramePosition(e);
        } else {
            displayFrame();
            updateFramePosition(e);
            floatingFrame.setVisible(true);
        }
        
    }
    
    // for mouselistener
    
    
    public void mouseEntered(MouseEvent e){
        displayFrame();
        updateFramePosition(e);
        floatingFrame.setVisible(true);
        //System.out.println("mouse entered");
    }
    
    public void mousePressed(MouseEvent e){
        
    }
    public void mouseClicked(MouseEvent e){
        
    }
    
    public void mouseExited(MouseEvent e){
        disposeFrame();
        //System.out.println("mouse exited");
    }
    public void mouseReleased(MouseEvent e){
        
    }
    
  
    
}
