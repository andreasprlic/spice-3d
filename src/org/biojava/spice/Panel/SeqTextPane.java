/*
 *                    BioJava development code
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
 * Created on 21.03.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.spice.Panel;

import org.biojava.spice.SPICEFrame ;
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.Segment;
import org.biojava.spice.GUI.SelectionLockMenuListener;
import org.biojava.spice.Panel.seqfeat.FeatureEvent;
import org.biojava.spice.Panel.seqfeat.SelectedSeqPositionListener;
import org.biojava.spice.Panel.seqfeat.FeatureViewListener;
import javax.swing.JTextPane;
import javax.swing.JPopupMenu;
import java.awt.event.MouseListener       ;
import java.awt.event.MouseMotionListener ;
import java.awt.event.MouseEvent          ;
import javax.swing.text.*                 ;
import java.awt.Color                     ;
import org.biojava.bio.structure.Chain    ;
import java.awt.Point                     ;  
import java.awt.event.KeyEvent            ;
import java.awt.event.KeyAdapter          ;
import java.util.logging.*                ;
import java.util.List                     ;
import java.util.regex.*                  ;
import java.util.ArrayList;
import java.util.Iterator;
import org.biojava.bio.structure.*        ;


/** a JTexPane - SeqPanel object that displays the amino acid sequence
 * of a protein.
 * @author Andreas Prlic
 */
public class SeqTextPane
extends JTextPane

implements FeatureViewListener, 
SelectedSeqPositionListener,
MouseListener, MouseMotionListener
{
    
    SPICEFrame spice ;
    Chain chain ;
    int current_chainnumber ;
    
    int selectionStart  ;
    ISearchListener isearchListener ;
    JPopupMenu popupMenu;
    boolean dragging ;
    boolean selectionIsLocked;
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    List selectedSeqPositionListeners;
    public SeqTextPane (SPICEFrame spicef) {
        super();
        selectionIsLocked = false;
        spice = spicef;
        chain = null ;
        dragging = false ;
        int current_chainnumber = -1;
        selectionStart = -1 ;
        //this.setBackground(Color.black);
        
        selectedSeqPositionListeners = new ArrayList();
        
        // add font styles to mark sequence position
        //StyledDocument doc = this.getStyledDocument();
        Style style = this.addStyle("red",null);
        StyleConstants.setBackground(style,Color.red);
        StyleConstants.setForeground(style,Color.black);
        //StyleConstants.setBold(style,true);
        
        Style bstyle = this.addStyle("black",null);
        StyleConstants.setForeground(bstyle,Color.black);
        StyleConstants.setBackground(bstyle,Color.white);
        
        Style ystyle = this.addStyle("yellow",null);
        StyleConstants.setBackground(ystyle,Color.yellow);
        StyleConstants.setForeground(ystyle,Color.black);
        
        Style gstyle = this.addStyle("grey",null);
        StyleConstants.setBackground(gstyle,Color.gray );
        StyleConstants.setForeground(gstyle,Color.black);
        
        //StyleConstants.setBold(bstyle,false);
        
        this.setEditable(false);
        isearchListener = new ISearchListener(spice,this);
        this.addKeyListener(isearchListener);
        
        popupMenu = new JPopupMenu();
        
        SelectionLockMenuListener ml = new SelectionLockMenuListener(spice, null);
        
        /*JMenuItem menuItem = new JMenuItem("lock selection");
        menuItem.addActionListener(ml);
        popupMenu.add(menuItem);
        *///menuItem = new JMenuItem("delete");
        //menuItem.addActionListener(ml);
        //tablePopup.add(menuItem);
        
        
        //MouseListener popupListener = new SelectionLockPopupListener(popupMenu,spice);
        //this.addMouseListener(popupListener);
        
    }
    
    public void addSelectedSeqPositionListener(SelectedSeqPositionListener listener){
        selectedSeqPositionListeners.add(listener);
        isearchListener.addSelectedSeqPositionListener(listener);
    }
    
    public void setChain(Chain c,int chainnumber) {
        chain = c;
        current_chainnumber = chainnumber ;
        
        // get sequence
        //Chain c = structure.getChain(0);
        List aminos = c.getGroups("amino");
        StringBuffer sequence = new StringBuffer() ;
        for ( int i=0 ; i< aminos.size(); i++){
            AminoAcid a = (AminoAcid)aminos.get(i);
            sequence.append( a.getAminoType());
        }
        
        String s = sequence.toString();
        this.setText(s);
        
    }
    
    
    public void mouseMoved(MouseEvent e)
    {	
        // do not change selection if  popupMenu is open
        if ( popupMenu.isVisible())
            return;
        
        
        isearchListener.clear();
        
        int seqpos = getSeqPos(e);
        if ( seqpos < 0 ) return ; 
        //System.out.println("SeqTextPane mouseMoved " + x + " " + y + " " + seqpos);
        if ( seqpos > this.getText().length()) return ;
        
        //spice.showSeqPos(current_chainnumber,seqpos);
        if ( dragging) return;
        highlite(seqpos);
        Iterator iter = selectedSeqPositionListeners.iterator();
        while (iter.hasNext()){
            SelectedSeqPositionListener li = (SelectedSeqPositionListener)iter.next();
            li.selectedSeqPosition(seqpos);
        }
    }
    
    
    
    private int getSeqPos(MouseEvent e) {
        
        int x = e.getX();
        int y = e.getY();
        Point p = new Point(x,y);
        
        int seqpos = viewToModel(p);
        return seqpos  ;
    }
    
    public void mouseClicked(MouseEvent e)  { 
    
    }
    public void mouseEntered(MouseEvent e)  {}
    public void mouseExited(MouseEvent e)   {}
    
    public void mousePressed(MouseEvent e)  {
        int b = e.getButton();
        logger.finest("mousePressed " + b);
        if ( b == MouseEvent.BUTTON3) return;
        selectionStart = getSeqPos(e);
        dragging = false;
        setSelectionLocked(false);
        
    }
    public void mouseReleased(MouseEvent e) {
        int b = e.getButton();
        
        //logger.finest("mouseReleased " + b);
        if ( b != MouseEvent.BUTTON1) return;
        selectionStart =  -1 ;
        
        // do not change selection if  popupMenu is open
        if ( popupMenu.isVisible())
            return;
        
        if ( dragging) {
            int selEnd =  getSeqPos(e);
            int start = selectionStart ;
            int end   = selEnd         ;
            
            if ( selEnd < selectionStart ) {
                start = selEnd ;
                end = selectionStart ;
            }
            highlite(start,end);
            Iterator iter = selectedSeqPositionListeners.iterator();
            while (iter.hasNext()){
                SelectedSeqPositionListener li = (SelectedSeqPositionListener)iter.next();
                li.selectedSeqRange(start,end);
            }
            setSelectionLocked(true);
            return ;
        }
        
        // not dragging ... :
        if (  selectionIsLocked)   return;
        
        int seqpos = getSeqPos(e);
        if ( seqpos < 0 ) return ; 
        
        
        //spice.select(current_chainnumber,seqpos);
        /*String pdb1 = spice.getSelectStrSingle(current_chainnumber,seqpos);
        if ( ! pdb1.equals("")) {
            String cmd = "select "+pdb1 +"; spacefill on; colour cpk;" ;
            spice.executeCmd(cmd);
        }*/
        
        Iterator iter = selectedSeqPositionListeners.iterator();
        while (iter.hasNext()){
            SelectedSeqPositionListener li = (SelectedSeqPositionListener)iter.next();
            li.selectedSeqPosition(seqpos);
        }
        
        
    }
    public void mouseDragged(MouseEvent e) {
        //System.out.println("dragging mouse "+e);
        dragging = true ;
        //setSelectionLocked(true);
        if ( selectionStart < 0 )
            return ;
        int b = e.getButton();
        if ( b == MouseEvent.BUTTON3) return;
        // logger.finest("mouseDragged " + b);
        int selEnd =  getSeqPos(e);
        int start = selectionStart ;
        int end   = selEnd         ;
        
        if ( selEnd < selectionStart ) {
            start = selEnd ;
            end = selectionStart ;
        }
        highlite(start,end);
        Iterator iter = selectedSeqPositionListeners.iterator();
        while (iter.hasNext()){
            SelectedSeqPositionListener li = (SelectedSeqPositionListener)iter.next();
            li.selectedSeqRange(start,end);
        }
    }	
    
    
    /*
     private int getSeqpos(int x,int y){
     Dimension d = this.getSize();
     double w = d.width ;
     System.out.println("x y w" + x + " " + y + " " + w);
     System.out.println(x /w);
     System.out.println(w /x);
     return Math.round (x/(long)w);
     }
     
     */
    
    /** highighting of range of residues */
    public void highlite( int start, int end) {
        //System.out.println("SeqTExtPane highlite " + start + " " + end);
        //select(start,end);
        dragging = true;
        setSelectionLocked(true);
        StyledDocument doc = this.getStyledDocument();
        doc.setCharacterAttributes(0,chain.getLength(), this.getStyle("black"),true);
        doc.setCharacterAttributes(start,(end-start +1), this.getStyle("red"),true);
        this.repaint();
    }
    
    /** highighting of single residue */    
    public void highlite( int seqpos) {
        dragging = false ;
        setSelectionLocked(false);
        //System.out.println("SeqTExtPane highlite " + seqpos);
        selectedSeqPosition(seqpos);
        StyledDocument doc = this.getStyledDocument();
        doc.setCharacterAttributes(0,chain.getLength(), this.getStyle("black"),true);
        doc.setCharacterAttributes(seqpos,1, this.getStyle("red"),true);
        this.repaint();
    }
    
    /** select range of residues */
    //public void select(int start, int end){
      // selectedSeqRange(start,end);
    //}
    
    /** select single residue */
    //public void select( int seqpos) {
        //System.out.println("SeqTExtPane select " + seqpos);
       //selectedSeqPosition(seqpos);
    //}
    
    public void selectedSeqRange(int start, int end) {
        //System.out.println("selected " + start + " " + end);
        //System.out.println("SeqTExtPane select " + start + " "  + end);
        dragging = true ;
        //setSelectionLocked(true);
        if ( chain == null ) { return ;}
        StyledDocument doc = this.getStyledDocument();
        doc.setCharacterAttributes(0,chain.getLength(), this.getStyle("black"),true);
        doc.setCharacterAttributes(start,(end-start +1), this.getStyle("red"),true);
        this.repaint();
    }
    
    public void selectedSeqPosition(int seqpos){
       // System.out.println("selected seqpos " + seqpos );
        if ( chain == null ) { return ;}
        dragging = false;
        //setSelectionLocked(false);
        selectionIsLocked = false;
        StyledDocument doc = this.getStyledDocument();
        doc.setCharacterAttributes(0,chain.getLength(), this.getStyle("black"),true);
        doc.setCharacterAttributes(seqpos,1, this.getStyle("red"),true);	
        this.repaint();
    }
    
    /** goes through all SeqPositionSelected listeners and locks/unlocks selection */
    private void setSelectionLocked(boolean flag){
        selectionIsLocked = flag;
        Iterator iter = selectedSeqPositionListeners.iterator();
        while (iter.hasNext()){
            SelectedSeqPositionListener li = (SelectedSeqPositionListener)iter.next();
            li.selectionLocked(flag);
        }
        
    }
    
    public void selectionLocked(boolean flag){
        selectionIsLocked = flag;
    }
    
	public void mouseOverFeature(FeatureEvent e){
	    
	    
	    Feature feat = (Feature) e.getSource();
	    //System.out.println("selected feature " + feat);
	    paintFeature(feat);
	}
	
	public void mouseOverSegment(FeatureEvent e){
	    Segment seg = (Segment)e.getSource();
	    //System.out.println("mouse over segment " + seg);
	    int start = seg.getStart();
	    int end = seg.getEnd();
	    highlite(start,end);
	}
	
	private void paintFeature(Feature feat){
	    StyledDocument doc = this.getStyledDocument();
        doc.setCharacterAttributes(0,chain.getLength(), this.getStyle("black"),true);
        List segments = feat.getSegments();
        Iterator iter = segments.iterator();
        while ( iter.hasNext()){
            Segment s = (Segment) iter.next();
            int start = s.getStart();
            int end = s.getEnd();
            String name = s.getName();
            Style style =  this.getStyle("red");
            if (name.equals("HELIX"))
                    style = this.getStyle("red");
            if (name.equals("STRAND"))
                style = this.getStyle("yellow");
            if (name.equals("COIL"))	
                this.getStyle("grey")	;
            doc.setCharacterAttributes(start,(end-start +1),style,true);
        }
        
        this.repaint();
	    
	}
	public void featureSelected(FeatureEvent e){
	    Feature feat = (Feature) e.getSource();
	    //System.out.println("selected feature " + feat);
	    paintFeature(feat);
	}
	public void segmentSelected(FeatureEvent e){
	    Segment seg = (Segment)e.getSource();
	    //System.out.println("selected segment " + seg);
	    int start = seg.getStart();
	    int end = seg.getEnd();
	    selectedSeqRange(start,end);
	}

    
    
    
}

class ISearchListener
extends KeyAdapter

{
    
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    
    SPICEFrame spice   ;
    SeqTextPane parent ;
    String searchtext  ;
    int startpos       ;
    int lasthit        ;
    Pattern validText  ;
    List selectedSeqPositionListeners;
    
    public ISearchListener (SPICEFrame spiceparent, SeqTextPane textpane) {
        spice      = spiceparent ;
        parent     = textpane ;
        startpos   = 0 ;
        lasthit    = 0 ;
        searchtext = "" ;
        selectedSeqPositionListeners = new ArrayList();
        validText = Pattern.compile("[a-zA-Z]");
        
    }
    public void addSelectedSeqPositionListener(SelectedSeqPositionListener listener){
        selectedSeqPositionListeners.add(listener);
    }
    
    /** clear the current searchtext */
    public void clear(){
        searchtext ="" ;
        
    }
    
    public void keyReleased(KeyEvent e){
        int code = e.getKeyCode();
        
        String s = KeyEvent.getKeyText(code);
        logger.finest("SeqPanel pressed " + s);
        
        Matcher m = validText.matcher(s);
        
        if ( code == KeyEvent.VK_BACK_SPACE) {
            // if keyPressed = backspace, remove last character
            if (searchtext.length() > 0) {
                searchtext = searchtext.substring(0,searchtext.length()-1);
                logger.finest("new searchtext " + searchtext);
                tooltip(searchtext);	
            }
        } 
        else if ( code == KeyEvent.VK_ENTER) {
            // if keyPressed = Enter, search from startpos +1
            startpos = lasthit + searchtext.length() ;
            logger.finest("starting search from here " + searchtext);
            tooltip(searchtext);
            
        }
        else if ( code == KeyEvent.VK_HOME) {
            // if keyPressed = Home , move to first character.
            logger.finest("going home " );
            searchtext = "" ;
            startpos = 0 ;
            lasthit  = 0 ;
            tooltip(null);
            
        } 
        else if ( m.matches() ) {
            // this text can be added to searchtext varaiable
            searchtext += (s);
            logger.finest("new searchtext " + searchtext);
            tooltip(searchtext);
            
        }
        
        
//      TODO 
        // display popup with current searchtext.
        
        
        
        int start = getStartPos();
        if ( start != -1 ) {
            lasthit = start ;
            // highlite text
            //int chainnr = spice.getCurrentChainNumber();
            //spice.highlite(chainnr,start,start+searchtext.length()-1);
            Iterator iter = selectedSeqPositionListeners.iterator();
            while (iter.hasNext()){
                SelectedSeqPositionListener li = (SelectedSeqPositionListener)iter.next();
                li.selectedSeqRange(start,start+searchtext.length()-1);
            }
            
            
        } else {
            logger.finest("no substring " + searchtext + " found");
            startpos = 0 ;
            lasthit  = 0 ;
        }
        
        
    }
    
    private void tooltip(String txt) {
        String help =  "<ENTER> for next occurance, <HOME> to clear" ;
        if (txt == null)  parent.setToolTipText("press key to search for pattern. "+help);
        else 
            parent.setToolTipText("searching " +txt +" " + help);
        
    }
    
    private int getStartPos(){
        String sequence = parent.getText();
        return sequence.indexOf(searchtext,startpos);
    }
    
}

