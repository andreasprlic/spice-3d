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
 * Created on Jan 25, 2006
 *
 */
package org.biojava.spice.gui;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.biojava.spice.SpiceApplication;

public class MenuCreator {

    public MenuCreator() {
        super();

    }
    
    public static JMenu createAlignmentMenu(SpiceMenuListener ml){
        JMenu amenu = new JMenu("Alignment");
        JMenuItem region = new JMenuItem("Toggle full structure");
        region.addActionListener(ml);
        region.setMnemonic(KeyEvent.VK_T);
        amenu.add(region);
        return amenu;
    }

    public static ImageIcon createImageIcon(String name){
        return SpiceApplication.createImageIcon(name);
    }
    
    public static JMenu createHelpMenu(SpiceMenuListener ml){
        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);
        help.getAccessibleContext().setAccessibleDescription("get help");
      
        
        ImageIcon helpIcon = createImageIcon("help.png");
        
        JMenuItem aboutspice;
        if ( helpIcon == null )
            aboutspice = new JMenuItem("About SPICE");
        else
            aboutspice = new JMenuItem("About SPICE",helpIcon);
        aboutspice.addActionListener  ( ml );
        aboutspice.setMnemonic(KeyEvent.VK_A);
        help.add(aboutspice);
        
        JMenuItem techinfo;
        if ( helpIcon == null )
            techinfo = new JMenuItem("Tech Info");
        else
            techinfo = new JMenuItem("Tech Info", helpIcon);
        techinfo.addActionListener(ml);
        techinfo.setMnemonic(KeyEvent.VK_T);
        help.add(techinfo);
        
        
        JMenuItem spicemanual;
        ImageIcon manualIcon =  createImageIcon("toggle_log.png");
        if ( manualIcon == null)
            spicemanual = new JMenuItem("Manual");
        else
            spicemanual = new JMenuItem("Manual",manualIcon);
        spicemanual.addActionListener(ml);
        spicemanual.setMnemonic(KeyEvent.VK_M);
        help.add(spicemanual);
       
        
        return help;   
    }
    
    public static JMenu createFileMenu(SpiceMenuListener ml){
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        file.getAccessibleContext().setAccessibleDescription("the file menu");
        
        
        JMenuItem newWindow;
        ImageIcon nwIcon = createImageIcon("window_new.png");
        if ( nwIcon == null)
            newWindow = new JMenuItem("New Window");
        else 
            newWindow = new JMenuItem("New Window", nwIcon);
        
        newWindow.setMnemonic(KeyEvent.VK_N);
        
        
        JMenuItem newTab;
        ImageIcon tabIcon = createImageIcon("view-right.png");
        if ( tabIcon == null)
            newTab = new JMenuItem("New Tab");
        else
            newTab = new JMenuItem("New Tab", tabIcon);
        
        JMenuItem openpdb;
        ImageIcon openIcon = createImageIcon("network.png");
        if ( openIcon == null)
            openpdb = new JMenuItem("Open");
        else
            openpdb = new JMenuItem("Open", openIcon);
        
        openpdb.setMnemonic(KeyEvent.VK_O);
        
        JMenuItem save ;
        ImageIcon saveIcon = createImageIcon("3floppy_unmount.png");
        if ( saveIcon == null)
            save = new JMenuItem("Save");
        else
            save = new JMenuItem("Save",saveIcon);
        save.setMnemonic(KeyEvent.VK_S);
        
        JMenuItem revert;
        ImageIcon revertIcon = createImageIcon("revert.png");
        if (revertIcon == null)
            revert = new JMenuItem("Load");
        else
            revert = new JMenuItem("Load",revertIcon);
        revert.setMnemonic(KeyEvent.VK_L);
        
        ImageIcon exitIcon = createImageIcon("exit.png");
        JMenuItem exit;
        if ( exitIcon != null)
            exit    = new JMenuItem("Exit",exitIcon);
        else
            exit    = new JMenuItem("Exit");
        exit.setMnemonic(KeyEvent.VK_X);
        
        ImageIcon propIcon = createImageIcon("configure.png");
        JMenuItem props ;
        if ( propIcon != null )
            props   = new JMenuItem("Properties",propIcon);
        else
            props   = new JMenuItem("Properties");
        props.setMnemonic(KeyEvent.VK_P);
      
        newWindow.addActionListener(ml);
        newTab.addActionListener(ml);
        openpdb.addActionListener( ml );
        save.addActionListener   ( ml );
        revert.addActionListener ( ml );
        exit.addActionListener   ( ml );
        props.addActionListener  ( ml );

        file.add(newWindow);
     
        file.add(newTab);
        
        file.addSeparator();
        
        file.add( openpdb );
        file.add( save    );
        file.add( revert  );
        
        file.addSeparator();
        
        file.add( props   );
        
        file.addSeparator();
        
        file.add( exit    );

        return file;
    }
    
    /** create a JMenu to be used for interacting with the structure Panel.
     * requires an ActionListener to be called when one of the Menus is being used. */
    public static JMenu createDisplayMenu(ActionListener ml){
        JMenu display = new JMenu("Display");
        display.setMnemonic(KeyEvent.VK_D);
        display.getAccessibleContext().setAccessibleDescription("change display");
        
        ImageIcon resetIcon = SpiceApplication.createImageIcon("reload.png");
        JMenuItem reset;
        if ( resetIcon == null)
            reset   = new JMenuItem("Reset");
        else
            reset   = new JMenuItem("Reset",resetIcon);
        reset.setMnemonic(KeyEvent.VK_R);
        
        JMenu select = new JMenu("Select");
        select.setMnemonic(KeyEvent.VK_S);
        
        JMenuItem selall = new JMenuItem("Select - All");
        JMenuItem selnon = new JMenuItem("Select - None");
        JMenuItem selami = new JMenuItem("Select - Amino");
        JMenuItem selnuc = new JMenuItem("Select - Nucleic");
        JMenuItem selhet = new JMenuItem("Select - Hetero");
        JMenuItem selh20 = new JMenuItem("Select - Water");
        JMenuItem selhyd = new JMenuItem("Select - Hydrogen");
        JMenuItem selcar = new JMenuItem("Select - Carbon");
        JMenuItem selnit = new JMenuItem("Select - Nitrogen");
        JMenuItem seloxy = new JMenuItem("Select - Oxygen");
        JMenuItem selpho = new JMenuItem("Select - Phosphorus");
        JMenuItem selsul = new JMenuItem("Select - Sulphur");
        
        selall.addActionListener(ml);
        selnon.addActionListener(ml);
        selami.addActionListener(ml);
        selnuc.addActionListener(ml);
        selhet.addActionListener(ml);
        selh20.addActionListener(ml);
        selhyd.addActionListener(ml);
        selcar.addActionListener(ml);
        selnit.addActionListener(ml);
        seloxy.addActionListener(ml);
        selpho.addActionListener(ml);
        selsul.addActionListener(ml);
        
        select.add(selall);
        select.add(selnon);
        select.addSeparator();
        
        select.add(selami);
        select.add(selnuc);
        select.add(selhet);
        select.add(selh20);
        select.addSeparator();
        select.add(selhyd);
        select.add(selcar);
        select.add(selnit);
        select.add(seloxy);
        select.add(selpho);
        select.add(selsul);
        
        /*
         * ImageIcon lockIcon = createImageIcon("lock.png");
         
         if (lockIcon != null)
         lock = new JMenuItem("Lock Selection",lockIcon);
         else
         lock = new JMenuItem("Lock Selection");
         
         ImageIcon unlockIcon = createImageIcon("decrypted.png");
         if ( unlockIcon == null)
         unlock = new JMenuItem("Unlock Selection");
         else
         unlock = new JMenuItem("Unlock Selection", unlockIcon);
         
         lockMenu = unlock;
         lockMenu.setMnemonic(KeyEvent.VK_U);
         lockMenu.setEnabled(selectionLocked);
         */
        JMenuItem backbone   = new JMenuItem("Backbone");
        JMenuItem wireframe  = new JMenuItem("Wireframe");
        JMenuItem cartoon    = new JMenuItem("Cartoon");
        JMenuItem ballnstick = new JMenuItem("Ball and Stick");
        JMenuItem spacefill  = new JMenuItem("Spacefill");
        
        
        JMenu colorsub  = new JMenu("Color");
        colorsub.setMnemonic(KeyEvent.VK_C);
        
        JMenuItem colorchain = new JMenuItem("Color - chain");
        JMenuItem colorrain  = new JMenuItem("Color - rainbow");
        JMenuItem colorsec   = new JMenuItem("Color - secondary");
        JMenuItem colorcpk   = new JMenuItem("Color - cpk");
        JMenuItem colorred   = new JMenuItem("Color - red");
        JMenuItem colorblue  = new JMenuItem("Color - blue");
        JMenuItem colorgreen = new JMenuItem("Color - green");
        JMenuItem coloryellow = new JMenuItem("Color - yellow");
        
        reset.addActionListener     ( ml );
        colorrain.addActionListener(  ml );
        //lockMenu.addActionListener    ( ml );
        backbone.addActionListener  ( ml );
        wireframe.addActionListener ( ml ); 
        cartoon.addActionListener   ( ml );
        ballnstick.addActionListener( ml );
        spacefill.addActionListener ( ml );     
        colorchain.addActionListener( ml );
        colorsec.addActionListener  ( ml );
        colorcpk.addActionListener  ( ml );
        colorred.addActionListener (ml);
        colorblue.addActionListener(ml);
        colorgreen.addActionListener(ml);
        coloryellow.addActionListener(ml);
        
        
        display.add( reset   );
        //display.add( lockMenu  );
        display.addSeparator();
        display.add(select);
        display.addSeparator();
        
        display.add( backbone   );
        display.add( wireframe  );
        display.add( cartoon    );
        display.add( ballnstick );
        display.add( spacefill  );
        display.addSeparator();
        
        display.add(colorsub);
        
        colorsub.add(colorchain);
        colorsub.add(colorrain);
        colorsub.add(colorsec)   ;
        colorsub.add(colorcpk)  ;
        colorsub.add(colorred);
        colorsub.add(colorblue);
        colorsub.add(colorgreen);
        colorsub.add(coloryellow);
        
        // add the Jmol menu
        // TODO: add a menu for Jmol
        //JMenu jmolM = new JMenu("Jmol");
        //display.add(jmolM);
        //jmolM.add(JmolPopup.)
        
        return display;
    }
    
    
    
}
