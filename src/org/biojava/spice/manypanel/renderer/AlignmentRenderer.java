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
package org.biojava.spice.manypanel.renderer;


import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.biojava.bio.structure.Chain;
import org.biojava.spice.manypanel.eventmodel.ScaleEvent;
import org.biojava.spice.manypanel.eventmodel.ScaleListener;
import org.biojava.spice.manypanel.eventmodel.SequenceEvent;
import org.biojava.spice.manypanel.eventmodel.SequenceListener;

public class AlignmentRenderer 
extends JPanel {
    
    static final long serialVersionUID = 28371037192730472l;
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    
    MyScaleListener scale1Listener;
    MyScaleListener scale2Listener;
    JLayeredPane layeredPane;
    AlignmentPanel alignmentPanel;
    
    AlignmentCursorPanel cursorPanel;
    JScrollPane scrollPane;
    SequenceListener sequenceListener1;
    SequenceListener sequenceListener2;
    
    AdjustmentListener adjust1;
    AdjustmentListener adjust2;
    

    
    public AlignmentRenderer(){
        
        this.setOpaque(true);
        setDoubleBuffered(true);
        
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder());
        
        this.setBackground(Color.blue);
        
        layeredPane = new JLayeredPane();
        layeredPane.setBorder(BorderFactory.createEmptyBorder());
        layeredPane.setDoubleBuffered(true);
        layeredPane.setOpaque(true);
        layeredPane.setBackground(Color.yellow);
        
        scrollPane = new JScrollPane(layeredPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(true);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        
        scale1Listener = new MyScaleListener(1,this);
        scale2Listener = new MyScaleListener(2,this);
        
        alignmentPanel = new AlignmentPanel();
        
        cursorPanel = new AlignmentCursorPanel();
        //cursorPanel1.setOpaque(true);
        
        //cursorPanel2.setOpaque(true);
        layeredPane.add(alignmentPanel,new Integer(0));
        layeredPane.add(cursorPanel, new Integer(1));
        
        
        //layeredPane.moveToBack(alignmentPanel);
        this.add(scrollPane);
        
        sequenceListener1 = new MySequenceListener(1,this);
        sequenceListener2 = new MySequenceListener(2,this);
        
        adjust1 = new MyAdjustmentListener(1,this);
        adjust2 = new MyAdjustmentListener(2,this);
        
        updatePanelPositions();
        
        this.setMinimumSize(new Dimension(Short.MAX_VALUE,30));
        this.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        this.setSize(new Dimension(Short.MAX_VALUE,30));
    }
    
    
    public void clearAlignment(){
        alignmentPanel.clearAlignment();
    }
    
    public SequenceListener getSequenceListener1(){
        return sequenceListener1;
    }
    
    public SequenceListener getSequenceListener2(){
        return sequenceListener2;
    }
    
    public AlignmentCursorPanel getCursorPanel(){
        return cursorPanel;
        
    }
    
    public AdjustmentListener getAdjust1(){
        return adjust1;
    }
    
    public AdjustmentListener getAdjust2(){
        return adjust2;
    }
    
    public void updatePanelPositions(){
        
        cursorPanel.setBounds(0,0,this.getWidth(),30);
        cursorPanel.setLocation(0,0);
      
        alignmentPanel.setBounds(0,0,this.getWidth(),30);
        alignmentPanel.setLocation(0,0);
       
        this.repaint();
    }
    
    public ScaleListener getSeq1ScaleListener(){
        return scale1Listener;
    }
    public ScaleListener getSeq2ScaleListener(){
        return scale2Listener;
    }
    public void setSequence1(Chain c){
        alignmentPanel.setSequence1(c);
        
        cursorPanel.setChain1(c);
        updatePanelPositions();
        this.repaint();
        
    }
    
    public void setSequence2(Chain c){
        alignmentPanel.setSequence2(c);
        cursorPanel.setChain2(c);
        updatePanelPositions();
        this.repaint();
        
    }
    
    public void setAlignmentMap1(Map one){
        alignmentPanel.setAlignmentMap1(one);
        this.repaint();
    }
    public void setAlignmentMap2(Map m){
        alignmentPanel.setAlignmentMap2(m);
    this.repaint();
    }
    
    
    
    public void setScale1(float scale){
        //logger.info("scale1 " + scale);
        alignmentPanel.setScale1(scale);
        cursorPanel.setScale1(scale);
        updatePanelPositions();
        this.repaint();
        
    }
    public void setScale2(float scale){
        //logger.info("scale2 "+ scale);
        alignmentPanel.setScale2(scale);
        cursorPanel.setScale2(scale);
        updatePanelPositions();
        this.repaint();
        
    }
    
    public void setScrolled1(Adjustable source, int value){
        //System.out.println("current scroll value " + value + " " + source.getVisibleAmount() + " " + source.getMaximum()+ " " +
             //   source.getMinimum());
        //System.out.println(source);
        alignmentPanel.setScrolled1(value);
        cursorPanel.setScrolled1(value);
        this.repaint();
        
    }
    
    public void setScrolled2(Adjustable source,int value){
        //System.out.println("current scroll value " + value + " " + source.getVisibleAmount() + " " + source.getMaximum()+ " " +
             //   source.getMinimum());
        //System.out.println(source);
        alignmentPanel.setScrolled2(value);
        cursorPanel.setScrolled2(value);
        this.repaint();
    }
    
}


class MySequenceListener implements SequenceListener {
    
    int pos;
    AlignmentRenderer parent;
    public MySequenceListener(int position, AlignmentRenderer parent) {
        this.pos = position;
        this.parent = parent;
    }
    
    
    private AlignmentCursorPanel getCursorPanel(){
        
        return parent.getCursorPanel();
        
        
    }
    public void clearSelection() {
        AlignmentCursorPanel c = getCursorPanel();
        c.clearSelection();
        c.repaint();
        
    }
    
    public void newSequence(SequenceEvent e) {
        AlignmentCursorPanel c = getCursorPanel();
        if ( pos == 1)
            c.newSequence1(e);
        else
            c.newSequence2(e);
        
        c.repaint();
    }
    
    public void selectedSeqPosition(int position) {
        AlignmentCursorPanel c = getCursorPanel();
        if ( pos == 1)
            c.selectedSeqPosition1(position);
        else
            c.selectedSeqPosition2(position);
        c.repaint();
    }
    
    public void selectedSeqRange(int start, int end) {
        AlignmentCursorPanel c = getCursorPanel();
        if (pos == 1)
            c.selectedSeqRange1(start,end);
        else
            c.selectedSeqRange2(start,end);
        c.repaint();
        
    }
    
    public void selectionLocked(boolean flag) {
        AlignmentCursorPanel c = getCursorPanel();
        c.selectionLocked(flag);
        c.repaint();
    }
    
    public void newObjectRequested(String accessionCode) {
        AlignmentCursorPanel c = getCursorPanel();
        if ( pos == 1)
            c.newObjectRequested1(accessionCode);
        else
            c.newObjectRequested2(accessionCode);
        c.repaint();
        
    }
    
    public void noObjectFound(String accessionCode) {
        AlignmentCursorPanel c = getCursorPanel();
        if (pos == 1)
            c.noObjectFound1(accessionCode);
        else
            c.noObjectFound2(accessionCode);
        c.repaint();
        
    }
    
}


class MyScaleListener implements ScaleListener {
    AlignmentRenderer parent;
    int position;
    public MyScaleListener(int position, AlignmentRenderer parent){
        this.parent = parent;
        this.position=position;
    }
    public void scaleChanged(ScaleEvent event) {
        if ( position == 1){
            parent.setScale1(event.getScale());
        }
        else 
            parent.setScale2(event.getScale());
        
    }
    
    
    
}



class MyAdjustmentListener implements AdjustmentListener {
   
    int pos;
    AlignmentRenderer parent;
    
    public MyAdjustmentListener(int position, AlignmentRenderer parent){
       this.parent = parent;
       pos = position;
    }
   
    
    
    // This method is called whenever the value of a scrollbar is changed,
    // either by the user or programmatically.
    public void adjustmentValueChanged(AdjustmentEvent evt) {
        Adjustable source = evt.getAdjustable();

        // getValueIsAdjusting() returns true if the user is currently
        // dragging the scrollbar's knob and has not picked a final value
        if (evt.getValueIsAdjusting()) {
            // The user is dragging the knob
            return;
        }

        // Determine which scrollbar fired the event
        int orient = source.getOrientation();
        if (orient == Adjustable.HORIZONTAL) {
            // Event from horizontal scrollbar
        } else {
            // Event from vertical scrollbar
        }

        // Determine the type of event
        int type = evt.getAdjustmentType();
        switch (type) {
          case AdjustmentEvent.UNIT_INCREMENT:
              // Scrollbar was increased by one unit
              break;
          case AdjustmentEvent.UNIT_DECREMENT:
              // Scrollbar was decreased by one unit
              break;
          case AdjustmentEvent.BLOCK_INCREMENT:
              // Scrollbar was increased by one block
              break;
          case AdjustmentEvent.BLOCK_DECREMENT:
              // Scrollbar was decreased by one block
              break;
          case AdjustmentEvent.TRACK:
              // The knob on the scrollbar was dragged
              break;
        }

        // Get current value
        int value = evt.getValue();
       
        triggerScrolled(source,value);
    }
    
    private void triggerScrolled(Adjustable source,int value){
        if ( pos == 1)
            parent.setScrolled1(source,value);
        else
            parent.setScrolled2(source,value);
    }
}
