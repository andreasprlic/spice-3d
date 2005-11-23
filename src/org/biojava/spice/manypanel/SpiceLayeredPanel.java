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
 * Created on Oct 28, 2005
 *
 */
package org.biojava.spice.manypanel;

import javax.swing.*;


public class SpiceLayeredPanel 
{
    
    JLayeredPane layeredPane;
    /*private String[] layerStrings = { "Yellow (0)", "Magenta (1)",
            "Cyan (2)",   "Red (3)",
            "Green (4)",  "Blue (5)" };
    private Color[] layerColors = { Color.yellow, Color.magenta,
            Color.cyan,   Color.red,
            Color.green,  Color.blue };
    */
    
    //CursorPanel cursorPanel ;
    
    public SpiceLayeredPanel() {
        super();
        /*
        layeredPane = new JLayeredPane();
        layeredPane.addMouseMotionListener(this);
        
        layeredPane.setPreferredSize(new Dimension(layerStrings.length*140, 310));
        layeredPane.setBorder(BorderFactory.createTitledBorder(
        "Move the Mouse"));
        
        
//      Add several labels to the layered pane.
        //layeredPane.setLayout(new GridLayout(2,3));
        for (int i = 0; i < layerStrings.length; i++) {
            JLabel label = createColoredLabel(layerStrings[i],
                    layerColors[i]);
            label.setBounds((i*140-140),0,i*140,140);
            layeredPane.add(label, new Integer(i));
        }
        
        cursorPanel = new CursorPanel();
        layeredPane.add(cursorPanel, new Integer(layerStrings.length));
        layeredPane.moveToFront(cursorPanel);
        cursorPanel.setLocation(0,0);
        cursorPanel.setOpaque(false);
        cursorPanel.setBounds(0,0,layerStrings.length*140,310);
        //add(Box.createRigidArea(new Dimension(0, 10)));
        //add(createControlPanel());
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(layeredPane);
        */
    }
    
    /*public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        cursorPanel.setSelectionStart(x);
        cursorPanel.setSelectionEnd(2);
        cursorPanel.repaint();
    }
    */
    //public void mouseDragged(MouseEvent e) {} //do nothing
    
    //Create and set up a colored label.
   /* private JLabel createColoredLabel(String text,
            Color color) {
        JLabel label = new JLabel(text);
        label.setVerticalAlignment(JLabel.TOP);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setOpaque(true);
        label.setBackground(color);
        label.setForeground(Color.black);
        label.setBorder(BorderFactory.createLineBorder(Color.black));
        label.setPreferredSize(new Dimension(140, 140));
        return label;
    }
    */
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        /*JFrame.setDefaultLookAndFeelDecorated(true);
        
        //Create and set up the window.
        JFrame frame = new JFrame("spiceLayeredPanel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Create and set up the content pane.
        JComponent newContentPane = new SpiceLayeredPanel();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
        */
        
        JFrame spiceFrame = new JFrame("SPICE - devel");
        spiceFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        BrowserPane browserPane = new BrowserPane();
        
        browserPane.triggerLoadStructure("1boi");
        //browserPane.triggerLoadUniProt("P50225");
        //browserPane.triggerLoadENSP("ENSP00000346625");
        //browserPane.setPreferredSize(new Dimension(1000, 1000));
        //browserPane.setOpaque(true); // contentPanes must be opaque
        spiceFrame.setContentPane(browserPane); 
        spiceFrame.pack();
        spiceFrame.setVisible(true);
        
        
        
    }
    
    
    
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
    
    
}
