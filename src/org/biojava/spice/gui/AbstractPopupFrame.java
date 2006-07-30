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
 * Created on Jul 28, 2006
 *
 */
package org.biojava.spice.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.biojava.spice.SpiceApplication;


/** a class that provides a "floating" frame that follows the position of the cursor
 * the "content" of the panel is provided by the content Component that can be set from 
 * the outside
 * 
 * @author Andreas Prlic
 * @since 11:23:08 AM
 * @version %I% %G%
 */
public abstract class AbstractPopupFrame 
implements MouseListener, MouseMotionListener{
    
    
    boolean frameshown ;    
    JFrame floatingFrame;
    MyHideTimer hideTimer;
    static final ImageIcon delTabIcon = SpiceApplication.createImageIcon("editdelete.png");
    
    public AbstractPopupFrame() {
        super();
        
        frameshown = false;
        
    }
    
   
    public void repaint() {
        if ( frameshown ) {
            Container c = getContent();
            floatingFrame.setContentPane(createContentPane(c));
            //System.out.println(c);
            floatingFrame.pack();
            floatingFrame.repaint();
            
            
        }
    }
 
    
    /** this class needs to be provided by implementing sub-classes. it provides the 
     * content that should be displayed in the frame
     * 
     * @return a Component
     */
     
    public abstract Container getContent() ;
    
    
    protected JFrame createFrame(){
        JFrame frame = new JFrame();
        JFrame.setDefaultLookAndFeelDecorated(false);
        frame.setUndecorated(true);
        frame.pack();
        return frame;
    }
    
    private Container createContentPane(Container content){
        Border border = BorderFactory.createLineBorder(Color.black);
        
        JPanel panel = new JPanel();
        panel.setBorder(border);
        
        Box vBox = Box.createVerticalBox();
        
        
        Box hBox = Box.createHorizontalBox();
        hBox.add(Box.createGlue());
        JLabel button = new JLabel("",delTabIcon,JLabel.RIGHT);
        hBox.add(button);
       
        vBox.add(hBox);
        vBox.add(content);
        
        panel.add(vBox);
        
           return panel;
    }
    
    protected void displayFrame() {
        //System.out.println("displayFrame");
        if ( frameshown ) {
            if ( hideTimer != null) {
                hideTimer.resetTimer();
            }
            //floatingFrame.repaint();
            repaint();
            return;
        }
        
        if ( floatingFrame == null)
            floatingFrame = createFrame();
        
        
        Container content = getContent();
        
        if ( content != null) {            
            floatingFrame.setContentPane(createContentPane(content));
            floatingFrame.pack();
        }
        
        floatingFrame.setVisible(true);
        
        frameshown = true;
        
    }
    
    /** set a flag that this frame will be disposed in X seconds, unless updateFramePosition is called again
     * 
     *
     */
    
    public synchronized void markForHide() {
       // System.out.println("markForHide");
        if ( hideTimer == null) {
            hideTimer = new MyHideTimer(this);
        } 
    }
    
    /** usually the frame is not dispsosed, but just set to invisible!
     * 
     *
     */
    protected synchronized void disposeFrame(){
        if ( ! frameshown ){
            return;
        }
      //  System.out.println("disposing floating frame");
        floatingFrame.setVisible(false);
        floatingFrame.dispose();
        
        frameshown = false;
        hideTimer = null;
    }
    
    protected synchronized void hideFrame(){
       // System.out.println("hideFrame");
        if (floatingFrame != null) {
            floatingFrame.setVisible(false);
        }
        frameshown = false;
    }
    
    
    private void updateFramePosition(MouseEvent e){
        //System.out.println("updateFramePosition");
        if ( ! frameshown){
           // System.out.println("frame not shown ...");
            return;
        }
        
        
        int x = e.getX();
        int y = e.getY();
        
        // get parent components locations
        Component compo = e.getComponent();
        Point screenTopLeft = compo.getLocationOnScreen();
        
        int cx = screenTopLeft.x;
        int cy = screenTopLeft.y;
        
        //int compo_h = compo.getHeight();
        
        //Dimension d = floatingFrame.getSize();
        //int dx = d.width;
        
        //int posx = cx + x  - ( dx/2)    ;
        //int posy = cy + y + compo_h + 5 ;
        
        int posx = cx + x   + 20; // draw a bit right of cursor
        int posy = cy + y + 20; // draw a bit below the cursor
        
        floatingFrame.setLocation(posx,posy);
        floatingFrame.requestFocus();
        floatingFrame.toFront();
        
    }
    
    
    public void mouseClicked(MouseEvent arg0) {}
    
    public void mousePressed(MouseEvent arg0) {  }
    
    public void mouseReleased(MouseEvent arg0) { }
    
    /** when the mouse is dragged the frame stays at the same location and is not disposed
     * 
     */
    public void mouseDragged(MouseEvent arg0) {
        if ( hideTimer != null) {
            hideTimer.interrupt();
            hideTimer = null;
        }
        if (frameshown) {
            floatingFrame.requestFocus();
            floatingFrame.toFront();
        }
    }
    
    public void mouseEntered(MouseEvent e) {
        //displayFrame();
        updateFramePosition(e);
        //floatingFrame.setVisible(true);
        
    }
    
    public void mouseExited(MouseEvent arg0) {
        //disposeFrame();
        markForHide();
        
    }
    
    
    public void mouseMoved(MouseEvent e) {
        //System.out.println("floating frame mouse moved");
        if ( frameshown) {
            updateFramePosition(e);
        } else {
            //displayFrame();
            //updateFramePosition(e);
            //floatingFrame.setVisible(true);
        }
        
    }
    
}

class MyHideTimer  implements ActionListener{

    int countdown ;
    AbstractPopupFrame hideMe ;
    boolean interrupted ;
    
    public static final int INITAL_COUNTDOWN = 1000;
    Timer timer;
    
    public MyHideTimer(AbstractPopupFrame disposeMe){
        timer = new Timer(INITAL_COUNTDOWN,this);
        
       // System.out.println("new hide timer");
        this.hideMe = disposeMe;
        interrupted = false;
    }
    
   //public Timer getTimer(){
   //    return timer;
   //}
    
    
    public synchronized void resetTimer() {
       // System.out.println("reset timer");
        countdown = INITAL_COUNTDOWN;
        timer.restart();
    }
    
    public synchronized void interrupt(){
        //System.out.println("interrupt");
        interrupted = true;
        timer.stop();
    }

    public void actionPerformed(ActionEvent arg0) {

        if ( ! interrupted) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    hideMe.hideFrame();
                    timer.stop();
                }
            });
            
        }
        
    }
}
    
    

