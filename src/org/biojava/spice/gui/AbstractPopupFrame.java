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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
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
 * the "content" of the panel is provided by the content Container that has to be provided
 * by a class that extends this abstract class
 * 
 * @author Andreas Prlic
 * @since 11:23:08 AM
 * @version %I% %G%
 */
public abstract class AbstractPopupFrame 
implements MouseListener, MouseMotionListener{
    
    
    boolean frameshown ;    
    static JFrame floatingFrame = createFrame();
    MyHideTimer hideTimer;
    static final ImageIcon delTabIcon = SpiceApplication.createImageIcon("editdelete.png");
       
    
    public AbstractPopupFrame() {
        super();
        
        frameshown = false;
        
    }
    
    protected static JFrame createFrame(){
        JFrame frame = new JFrame();
        JFrame.setDefaultLookAndFeelDecorated(false);
        frame.setUndecorated(true);
        frame.pack();
        return frame;
    }
    
    
    public void repaint() {
        if ( frameshown ) {
            
            Container c = getContent();
            
            floatingFrame.setContentPane(createContentPane(c));           
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
    
    
   
    
    private Container createContentPane(Container content){
        Border border = BorderFactory.createLineBorder(Color.DARK_GRAY);
        
        JPanel panel = new JPanel();
        panel.setBackground(Color.white);
        panel.setMaximumSize(new Dimension(100,100));
        
        panel.setBorder(border);
        
        Box vBox = Box.createVerticalBox();
        
        
        Box hBox = Box.createHorizontalBox();
        hBox.add(Box.createGlue());
        JLabel button;
        if ( delTabIcon != null)
            button = new JLabel("",delTabIcon,JLabel.RIGHT);
        else 
            button = new JLabel("X");
        
        button.addMouseListener(new MouseListener(){
            
            public void mouseClicked(MouseEvent arg0) { }
            
            public void mousePressed(MouseEvent arg0) { }
            
            public void mouseReleased(MouseEvent arg0) {
                disposeFrame();
            }
            
            public void mouseEntered(MouseEvent arg0) {   }
            
            public void mouseExited(MouseEvent arg0) { }
            
        });
        button.setToolTipText("click here to close frame");
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
        //int compo_w = compo.getWidth();        
        
        Container content = getContent();
        int compo_h = content.getHeight();
        int compo_w = content.getWidth();
        
        int DIST = 20;
        
        int posx = cx + x + DIST; // draw a bit right of cursor
        int posy = cy + y + DIST; // draw a bit below the cursor
        
        // height = y!
        // widht = x ...
        
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        int screen_w = (int) screenDim.getWidth();
        int screen_h = (int) screenDim.getHeight();
        
        //System.out.println("before x " + x + " y " + y + " posx " + posx + " posy " + posy + " sw " +
        //        screenDim.getWidth() + " sh " + screenDim.getHeight() + 
        //        " compo_H " + compo_h + " compo_w " + compo_w );
        
        if ( (posy + compo_h) > screen_h) {
            System.out.println("chainging y");
            posy = cy + y - compo_h - DIST - DIST;
        }
        
        if ( (posx + compo_w) > screen_w) {
            System.out.println("chainging x");
            posx = cx + x - compo_w - DIST - DIST;
        }
        
        //System.out.println("after x " + x + " y " + y + " posx " + posx + " posy " + posy + " sw " +
        //        screenDim.getWidth() + " sh " + screenDim.getHeight() +
        //        " compo_H " + compo_h + " compo_w " + compo_w );
        
        floatingFrame.setLocation(posx,posy);
        floatingFrame.requestFocus();
        floatingFrame.toFront();
        
    }
    
    
    public void mouseClicked(MouseEvent arg0) {}
    
    public void mousePressed(MouseEvent arg0) {  
        if ( hideTimer != null) {
            hideTimer.interrupt();
            hideTimer = null;
        }
        if (frameshown) {
            floatingFrame.requestFocus();
            floatingFrame.toFront();
        }
    }
    public void mouseReleased(MouseEvent arg0) { }
    
    /** when the mouse is dragged the frame stays at the same location and is not disposed
     * 
     */
    public void mouseDragged(MouseEvent arg0) {
        if ( hideTimer != null) {
            hideTimer.resetTimer();
            hideTimer = null;
        }
        if (frameshown) {
            floatingFrame.requestFocus();
            floatingFrame.toFront();
        }
    }
    
    public void mouseEntered(MouseEvent e) {       
        updateFramePosition(e);  
    }
    
    public void mouseExited(MouseEvent arg0) {     
        markForHide();        
    }
    
    
    public void mouseMoved(MouseEvent e) {
       
        if ( frameshown) {
            updateFramePosition(e);
        }        
    }
    
}

/** a small class that takes care of timing the disappearing of popup windows
 * 
 * @author Andreas Prlic
 * @since 10:48:33 AM
 * @version %I% %G%
 */
class MyHideTimer  implements ActionListener{
    
    int countdown ;
    AbstractPopupFrame hideMe ;
    boolean interrupted ;
    
    public static final int INITAL_COUNTDOWN = 1400;
    Timer timer;
    
    public MyHideTimer(AbstractPopupFrame disposeMe){
        timer = new Timer(INITAL_COUNTDOWN,this);
              
        this.hideMe = disposeMe;
        interrupted = false;
    }        
    
    public synchronized void resetTimer() {
       
        countdown = INITAL_COUNTDOWN;
        timer.restart();
    }
    
    public synchronized void interrupt(){
        
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



