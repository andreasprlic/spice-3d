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
 * Created on Feb 6, 2005
 *
 */
package org.biojava.spice.GUI;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import org.biojava.spice.Panel.JmolCommander;
import org.biojava.spice.Panel.StructurePanelListener;

/** A class that listens to various events on the 
 * StructureCommand line ( where it is possible to 
 * enter RASMOL like commands) and takes care of 
 * <li> clearing the selection upon startup</li>
 * <li> tracking the history of entered commands</li>
 * <li> sending the command to the structure panel, once 
 * Enter is being pressed.</li>
 * 
 * @author Andreas Prlic
 *
 */

public class StructureCommandListener 
extends KeyAdapter
implements ActionListener,
MouseListener

{
    JTextField textfield;
    JmolCommander structurePanelListener;
    
    List history;
    int historyPosition;
    
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    
    public StructureCommandListener (JmolCommander spl, JTextField textfield_) {
        super();
        structurePanelListener = spl;
        textfield = textfield_ ;
        history = new ArrayList();
        historyPosition = -2; // -2 = history = empty;
    }
    public void actionPerformed(ActionEvent event) {
        /*
        if ( spice.isLoading() ) {
            logger.finest("loading data, please be patient");
            return ;
        }
        */
        String cmd = textfield.getText();
        structurePanelListener.executeCmd(cmd);
        textfield.setText("");
        
        // now comes history part:
        
        // no need for history:
        if ( cmd.equals("")) return;
        
        // check last command in history
        // if equivalent, don't add,
        // otherwise add               
        if (history.size()>0){
            String txt=(String)history.get(history.size()-1);
            if (! txt.equals(cmd)) {
                history.add(cmd);  
            }
        } else {             
            // the first time always add
            history.add(cmd);
        }
        historyPosition=history.size();
        
        
    }
    
    public void  mouseClicked(MouseEvent e){
        String cmd = textfield.getText();
        if ( cmd.equals("enter RASMOL like command...")){
            textfield.setText("");
        }
    };
    
    
    public void  mouseExited(MouseEvent e){};
    public void  mouseReleased(MouseEvent e){};
    public void  mousePressed(MouseEvent e){};
    
    public void  mouseEntered(MouseEvent e){};
    
    /** takes care of the cursur up/down keys. 
     * triggers copying of stored commands into the current textfield */
    
    public void keyReleased(KeyEvent e){
        
        int code = e.getKeyCode();
        //String s = e.getKeyText(code);
        //System.out.println(s);
        if (( code == KeyEvent.VK_UP ) || 
                ( code == KeyEvent.VK_KP_UP)) {
            // go one back in history;
            if ( historyPosition > 0){
                historyPosition= historyPosition-1;              
            } 
        } else if (( code == KeyEvent.VK_DOWN ) || 
                ( code == KeyEvent.VK_KP_DOWN)) {            
            if ( historyPosition < (history.size()-1) ){
                historyPosition++;                
            } else {
                // clear command if at beginning of history
                textfield.setText("");
                historyPosition=history.size();
                return;
            }
        } else if ( code == KeyEvent.VK_PAGE_UP) {
            if ( historyPosition > 0) {
                historyPosition = 0;
            }
        } else if ( code == KeyEvent.VK_PAGE_DOWN) {
            if ( historyPosition >= 0) {
                historyPosition = history.size()-1;
            }
        } else {
            // some other key has been pressed, do nothing
            return;
        }
        
        if ( historyPosition >= 0) {
            String txt = (String)history.get(historyPosition);
            textfield.setText(txt);
        }
        
        
    }
}
