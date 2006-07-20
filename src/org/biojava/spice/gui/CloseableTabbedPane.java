
/*
 *  Java Napster version x.yz (for current version number as well as for
 *  additional information see version.txt)
 *
 *  Previous versions of this program were written by Florian Student
 *  and Michael Ransburg available at www.weblicity.de/jnapster and
 *  http://www.tux.org/~daneel/content/projects/10.shtml respectively.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * modified version with some extensions from Andreas Prlic
 */


package org.biojava.spice.gui;


import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;

import org.biojava.spice.SpiceApplication;


public class CloseableTabbedPane extends JTabbedPane {
    
    //--- Data field(s) ---
    
    static final long serialVersionUID = 1023093290823478l;
    
    protected ImageIcon closingIcon;
    List tabListeners;
    //--- Constructor(s) ---
    
    public CloseableTabbedPane(ImageIcon closingIcon) 
    {
        this.closingIcon = closingIcon;
        tabListeners = new ArrayList();
        
        addMouseListener(new ClosingListener());
    }
    
    public CloseableTabbedPane() 
    {
        this(SpiceApplication.createImageIcon("editdelete.gif"));
    }
    
    //--- Method(s) ---
    
    public void addTab(String title, Component component, boolean closeable) 
    {
        if (closeable) {
            super.addTab(title, new ClosingIcon(closingIcon), component);
        }
        else {
            super.addTab(title, component);
        }
        setSelectedComponent(component);
    }
    
    public void addTab(String title, Component component) 
    {
        addTab(title, component, true);
    }
    
    
    public void addTabListener(TabListener listener){
        tabListeners.add(listener);
    }
    
    /*public void removeTabAt(int i){
        
        System.out.println("remove tab at " + i);
        Component c = getComponentAt(i);
        super.remove(c);
        triggerTabClosed(c);
        
      
        
    }*/
    
    public TabListener[] getTabListener(){
        return (TabListener[])tabListeners.toArray(new TabListener[tabListeners.size()]);
    }
    
    
    /*
    private void triggerTabClosed(Component c){
        System.out.println("closeabletab trigger close");
        Iterator iter = tabListeners.iterator();
        while (iter.hasNext()){
            TabListener li = (TabListener)iter.next();
            TabEvent event = new TabEvent();
            event.setTabNumber(-1);
            event.setComponent(c);
            li.tabClosed(event);
        }
    }
    */
    
    //--- Inner Class(es) ---
    
    protected class ClosingListener extends MouseAdapter
    {
        private void triggerTabSelected(int tabPosition, Component c){
            Iterator iter = tabListeners.iterator();
            while (iter.hasNext()){
                TabListener li = (TabListener)iter.next();
                TabEvent event = new TabEvent();
                event.setTabNumber(tabPosition);
                event.setComponent(c);
                li.tabSelected(event);
            }
        }
        
        private void triggerTabClosing(int tabPosition, Component c){
            Iterator iter = tabListeners.iterator();
            while (iter.hasNext()){
                TabListener li = (TabListener)iter.next();
                TabEvent event = new TabEvent();
                event.setTabNumber(tabPosition);
                event.setComponent(c);
                li.tabClosing(event);
            }
        }
        
        
        
        
        public void mouseReleased(MouseEvent e)
        {
            int i = getSelectedIndex();
            
            // nothing selected
            if (i == -1)
                return;
            
            triggerTabSelected(i,getComponentAt(i));
            
            
            ClosingIcon icon = (ClosingIcon)getIconAt(i);
            
            // close tab, if icon was clicked
            if (icon != null && icon.contains(e.getX(), e.getY())) {
                
                Component comp = getComponentAt(i);
                triggerTabClosing( i, comp);
                
                // TODO: improve this hack:
                // the actual removing is done by listener! ...
                //removeTabAt(i);
               
                
            }
        }
        
    }
    
    
    
    /**
     * the idea for this class stems from limewire's CancelSearchIconProxy
     * class, thanks for going open source guys.
     */
    protected class ClosingIcon implements Icon
    {
        
        //--- Data field(s) ---
        
        private Icon icon;    
        private int x = 0;
        private int y = 0;
        private int height = 10;
        private int width = 10;
        
        //--- Constructor(s) ---
        
        public ClosingIcon(ImageIcon icon)
        {
            this.icon = icon;
            
            if (icon != null) {
                height = icon.getIconHeight();
                width = icon.getIconWidth();
            }
        }
        
        //--- Method(s) ---
        
        /**
         *
         */
        public int getIconHeight()
        {
            return height;
        }
        
        /**
         * 
         */
        public int getIconWidth()
        {
            return width;
        }
        
        /**
         * Overwrites paintIcon to get hold of the coordinates of the icon,
         * this is a rather rude approach just to find out if the closingIcon
         * was pressed.
         */
        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            this.x = x;
            this.y = y;
            
            if (icon != null) {
                icon.paintIcon(c, g, x, y + 1);
            }
            else {
                //Debug.log("CloseableTabbedPane: no icon");
                g.drawRect(x, y + 1, width, height);
            }
        }    
        
        /** Verifies if x and y are within the icon's borders.
         * 
         * @param xEvent
         * @param yEvent
         * @return boolean if within the borders
         */
        public boolean contains(int xEvent, int yEvent)
        {
            if (!(xEvent >= x) || !(xEvent <= x + width)) {
                return false;
            }
            if (!(yEvent >= y) || !(yEvent <= y + height)) {
                return false;
            }
            
            return true;
        }
    }
    
}

