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
    JFrame floatingFrame; 
    MyTimer hideTimer;
    static final ImageIcon delTabIcon = SpiceApplication.createImageIcon("editdelete.png");
       
    Point oldPoint;
    
    public static final int DIST = 20; // the distance of the frame to the mouse cursor
    
    public AbstractPopupFrame() {
        super();
        
        floatingFrame = createFrame();
        
        frameshown = false;
        
        oldPoint = new Point(0,0);
        
    }
    
    /** return a flag if the frame is currently shown
     * 
     * @return flag is currently being shown
     */
    public boolean isShown(){
        return frameshown;
    }
    
    protected JFrame createFrame(){
        JFrame frame = new JFrame();
        JFrame.setDefaultLookAndFeelDecorated(false);
        frame.setUndecorated(true);        
        frame.pack();
        
        MyFrameMouseListener frameMouse = new MyFrameMouseListener(frame, this);
        frame.addMouseMotionListener(frameMouse);
        
        
        return frame;
    }
    
    protected void dispatchFrame() {
        floatingFrame = createFrame();
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
        //System.out.println("create ContentPane");
        Border border = BorderFactory.createLineBorder(Color.DARK_GRAY);
        
        JPanel panel = new JPanel();
        panel.setBackground(Color.white);
        panel.setMaximumSize(new Dimension(100,100));
        
        panel.setBorder(border);
        
        Box vBox = Box.createVerticalBox();
        
        
        Box hBox = Box.createHorizontalBox();
       
        JLabel button;
        if ( delTabIcon != null)
            button = new JLabel("",delTabIcon,JLabel.LEFT);
        else 
            button = new JLabel("X");
        
       
        button.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent arg0) {}

            public void mousePressed(MouseEvent arg0) { }

            public void mouseReleased(MouseEvent arg0) {
                disposeFrame();
            }

            public void mouseEntered(MouseEvent arg0) {   }

            public void mouseExited(MouseEvent arg0) {  }
            
        });
        
        button.addMouseListener(new MyFrameCloseListener(floatingFrame));
        
        button.setToolTipText("click here to close frame");
        hBox.add(button);
        hBox.add(Box.createGlue());
        
        vBox.add(hBox);
        
        if ( content != null)
            vBox.add(content);
        
        panel.add(vBox);
        
        
        
        
        return panel;
    }
    
    public void resetTimer() {
        //System.out.println("displayFrame");
        if ( frameshown ) {
            if ( hideTimer != null) {
                hideTimer.resetTimer();
            }
            
            repaint();
        }
        
        if ( hideTimer == null) {           
            hideTimer = new MyTimer(this, MyTimer.SHOW);
        }
        
        
    }
    
    /** set a flag that this frame will be disposed in X seconds, unless updateFramePosition is called again
     * 
     *
     */
    
    public synchronized void markForHide() {
        //System.out.println("markForHide");
        if ( frameshown) {
            if ( hideTimer == null) {
                hideTimer = new MyTimer(this, MyTimer.HIDE);
            } 
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
        hideTimer = null;
    }
    
    public synchronized void showFrame(){
        //ystem.out.println("showFrame");
        
        floatingFrame.setLocation(oldPoint);
        
        Container content = getContent();
        
        if ( content != null) {            
            floatingFrame.setContentPane(createContentPane(content));
            floatingFrame.pack();
            //System.out.println("got content " + content);
        }
        
        javax.swing.SwingUtilities.invokeLater(new MyLocation(oldPoint,floatingFrame));
        
        floatingFrame.setVisible(true);
        
        if ( hideTimer != null) {
            hideTimer.interrupt();
            hideTimer = null;
        }
        
        frameshown = true;
        
        repaint();
    }
    
    
    private Point getFramePoint(MouseEvent e){
        int x = e.getX();
        int y = e.getY();
        
        // get parent components locations
        Component compo = e.getComponent();
        Point screenTopLeft = compo.getLocationOnScreen();
        
        int cx = screenTopLeft.x;
        int cy = screenTopLeft.y;
        
        int posx = cx + x + DIST; // draw a bit right of cursor
        int posy = cy + y + DIST; // draw a bit below the cursor
        
        Point p = new Point(posx, posy);
        return p;
    }
    
    
    public void updateFramePosition(MouseEvent e){
        //System.out.println("updateFramePosition");
       
        Point p = getFramePoint(e);
        if (oldPoint.equals(p))
            return;
        
        int x = e.getX();
        int y = e.getY();
        int posx = p.x;
        int posy = p.y;
        Component compo = e.getComponent();
        Point screenTopLeft = compo.getLocationOnScreen();
        int cx = screenTopLeft.x;
        int cy = screenTopLeft.y;
        
        // height = y!
        // widht = x ...
        
        Container content = getContent();
        
        int compo_h = 0 ;
        int compo_w = 0 ;
        
        if (content != null ) {
            compo_h = content.getHeight();
            compo_w = content.getWidth();
        }
        
        
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        int screen_w = (int) screenDim.getWidth();
        int screen_h = (int) screenDim.getHeight();
        
        //System.out.println("before x " + x + " y " + y + " posx " + posx + " posy " + posy + " sw " +
        //        screenDim.getWidth() + " sh " + screenDim.getHeight() + 
        //        " compo_H " + compo_h + " compo_w " + compo_w );
        
        if ( (posy + compo_h) > screen_h) {
            //System.out.println("chainging y");
            posy = cy + y - compo_h - DIST - DIST;
        }
        
        if ( (posx + compo_w) > screen_w) {
            //System.out.println("chainging x");
            posx = cx + x - compo_w - DIST - DIST;
        }
        
        //System.out.println("after x " + x + " y " + y + " posx " + posx + " posy " + posy + " sw " +
        //        screenDim.getWidth() + " sh " + screenDim.getHeight() +
        //        " compo_H " + compo_h + " compo_w " + compo_w );
        
        
        p = new Point(posx,posy);
        
        oldPoint = p;
        
    
        
       
    }
    
    public void interruptTimer(){
        if ( hideTimer != null) {
            hideTimer.interrupt();
            hideTimer = null;
        }
    }
    
    public void mouseClicked(MouseEvent arg0) {}
    
    public void mousePressed(MouseEvent arg0) {  
        interruptTimer();
        
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
        oldPoint = getFramePoint(e);
        
        if ( frameshown) {
            updateFramePosition(e);
        }        
    }
    
}

class MyFrameCloseListener 
implements MouseListener {
    JFrame parent;
    public MyFrameCloseListener (JFrame parent) {
        this.parent = parent;
    }
    
    public void mouseClicked(MouseEvent arg0) {}

    public void mousePressed(MouseEvent arg0) { }

    public void mouseReleased(MouseEvent arg0) {
        parent.dispose();
        
    }

    public void mouseEntered(MouseEvent arg0) { }

    public void mouseExited(MouseEvent arg0) {  }
    
}



class MyFrameMouseListener
implements MouseMotionListener {

    Point prevMouseOnScreen;
    JFrame parent;
    AbstractPopupFrame manager;
    public MyFrameMouseListener(JFrame frame, AbstractPopupFrame manager) {
        prevMouseOnScreen = null;
        parent =frame;
        this.manager = manager;
    }
    
    public void mouseDragged(MouseEvent e) {

        manager.dispatchFrame();
        
        if ( prevMouseOnScreen == null)  {
            prevMouseOnScreen = getMouseOnScreen(e);
            return;
        }
        
           
        
        Component compo = e.getComponent();
        Point screenTopLeft = compo.getLocationOnScreen();
       
        Point mouseOnScreen = getMouseOnScreen(e);
        
        int diffX = prevMouseOnScreen.x - mouseOnScreen.x;
        int diffY = prevMouseOnScreen.y - mouseOnScreen.y;
        
        prevMouseOnScreen = getMouseOnScreen(e);
        
        Point newPosition = new Point(screenTopLeft.x - diffX, screenTopLeft.y - diffY);     
        MyLocation loc = new MyLocation(newPosition,parent);
        javax.swing.SwingUtilities.invokeLater(loc);
        
       
        
    }
    
    private Point getMouseOnScreen(MouseEvent e){
        Point thisMouse = e.getPoint();
        
        
//      get parent components locations
        Component compo = e.getComponent();
        Point screenTopLeft = compo.getLocationOnScreen();
                       
        
        Point mouseOnScreen = (Point) screenTopLeft.clone();
        mouseOnScreen.translate(thisMouse.x,thisMouse.y);
        return mouseOnScreen;
        
    }

    public void mouseMoved(MouseEvent e) {
        prevMouseOnScreen = getMouseOnScreen(e);        
    }
    
}


class MyLocation implements Runnable {
    Point p;
    JFrame c;
    
    public MyLocation(Point p, JFrame c) {
        this.p = p;
        this.c = c;
    }
    public void run() {
        c.setLocation(p);
        //c.requestFocus();
        c.toFront();

    }
}

/** a small class that takes care of timing the disappearing of popup windows
 * 
 * @author Andreas Prlic
 * @since 10:48:33 AM
 * @version %I% %G%
 */
class MyTimer  implements ActionListener{
    
    int countdown ;
    AbstractPopupFrame hideMe ;
    boolean interrupted ;
    
    public static final int SHOW_COUNTDOWN = 1;
    public static final int HIDE_COUNTDOWN = 1;
    
    public static final int SHOW = 1;
    public static final int HIDE = 2;
    
    Timer timer;
    
    int action ;
    
    
    public MyTimer(AbstractPopupFrame disposeMe, int ACTIONTYPE){              
        
        if ( ACTIONTYPE == SHOW)
            timer = new Timer(SHOW_COUNTDOWN,this);
        else
            timer = new Timer(HIDE_COUNTDOWN,this);
                  
        this.hideMe = disposeMe;
        interrupted = false;
        action = HIDE;
        if ( ACTIONTYPE != HIDE )
            action = SHOW;
        
        timer.start();
    }        
    
    public synchronized void resetTimer() {
      
        timer.restart();
    }
    
    public synchronized void interrupt(){
    
        interrupted = true;
        timer.stop();
    }
    
    /** this method is called by the timer
     * 
     */
    public void actionPerformed(ActionEvent arg0) {
     
        if ( ! interrupted) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    
                    if ( action == HIDE) {
                        hideMe.hideFrame();
                        timer.stop();
                        
                    } else {                       
                        hideMe.showFrame();
                        timer.stop();
                        action = HIDE;
                    }
                }
            });
            
        }
        
    }
}



