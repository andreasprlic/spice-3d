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
package org.biojava.spice;

import javax.swing.JTextPane              ;
import java.awt.Dimension                 ;
import java.awt.event.MouseListener       ;
import java.awt.event.MouseMotionListener ;
import java.awt.event.MouseEvent          ;
import javax.swing.text.*                 ;
import java.awt.Color                     ;
import org.biojava.bio.structure.Chain    ;
import java.awt.Graphics                  ;
import javax.swing.text.Document          ;
import javax.swing.text.Element           ;
import java.awt.Point                     ;  
import java.awt.event.KeyEvent            ;
import java.awt.event.KeyListener         ;
import java.awt.event.KeyAdapter          ;
import java.util.logging.*                ;
import java.util.List                     ;
import java.util.regex.*                  ;

import org.biojava.bio.structure.*        ;


/** a JTexPane - SeqPanel object that displays the amino acid sequence
 * of a protein.
 * @author Andreas Prlic
 */
public class SeqTextPane
    extends JTextPane
       
    implements SeqPanel, MouseListener, MouseMotionListener
{

    SPICEFrame spice ;
    Chain chain ;
    int current_chainnumber ;
    public SeqTextPane (SPICEFrame spicef) {
	super();
	spice = spicef;
	chain = null ;
	int current_chainnumber = -1;

	//this.setBackground(Color.black);
	
	// add font styles to mark sequence position
	//StyledDocument doc = this.getStyledDocument();
	Style style = this.addStyle("red",null);
	StyleConstants.setBackground(style,Color.red);
	StyleConstants.setForeground(style,Color.black);
	//StyleConstants.setBold(style,true);
	
	Style bstyle = this.addStyle("black",null);
	StyleConstants.setForeground(bstyle,Color.black);
	StyleConstants.setBackground(bstyle,Color.white);
	//StyleConstants.setBold(bstyle,false);

	this.setEditable(false);
	this.addKeyListener(new ISearchListener(spice,this));
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

    public void mouseDragged(MouseEvent e) {
	System.out.println("dragging mouse "+e);
    }	

    public void mouseMoved(MouseEvent e)
    {	


	//int dot = e.getDot();
	int x = e.getX();
	int y = e.getY();
	Point p = new Point(x,y);

	int seqpos = viewToModel(p);

	//caretPosLabel.setText( (line+1) + ":" + (col+1) );
	//System.out.println("mouseMoved "+ pos);

	//, int x, int y
	//
	//
	//int seqpos = getSeqpos(x,y);

	//System.out.println("SeqTextPane mouseMoved " + x + " " + y + " " + seqpos);
	if ( seqpos < this.getText().length()) {
	    spice.showSeqPos(current_chainnumber,seqpos);
	    spice.select(current_chainnumber,seqpos);
	}
    }

    public void mouseClicked(MouseEvent e)
    {
	//System.out.println("CLICK");
	int x = e.getX();
	int y = e.getY();
	Point p = new Point(x,y);

	int seqpos = viewToModel(p);
	spice.select(current_chainnumber,seqpos);
	String pdb1 = spice.getSelectStrSingle(current_chainnumber,seqpos);
	if ( ! pdb1.equals("")) {
	    String cmd = "select "+pdb1 +"; spacefill on; colour cpk;" ;
	    spice.executeCmd(cmd);
	}

    }

    public void mouseEntered(MouseEvent e)  {}
    public void mouseExited(MouseEvent e)   {}
    public void mousePressed(MouseEvent e)  {}
    public void mouseReleased(MouseEvent e) {}


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
	StyledDocument doc = this.getStyledDocument();
	doc.setCharacterAttributes(start,(end-start +1), this.getStyle("red"),true);
    
    }

    /** highighting of single residue */    
    public void highlite( int seqpos) {
	//System.out.println("SeqTExtPane highlite " + seqpos);
	select(seqpos);
	StyledDocument doc = this.getStyledDocument();
	doc.setCharacterAttributes(seqpos,1, this.getStyle("red"),true);
    }

    /** select range of residues */
    public void select(int start, int end){
	//System.out.println("SeqTExtPane select " + start + " "  + end);
	if ( chain == null ) { return ;}
	StyledDocument doc = this.getStyledDocument();
	doc.setCharacterAttributes(0,chain.getLength(), this.getStyle("black"),true);
	doc.setCharacterAttributes(start,(end-start +1), this.getStyle("red"),true);

    }
    
    /** select single residue */
    public void select( int seqpos) {
	//System.out.println("SeqTExtPane select " + seqpos);
	if ( chain == null ) { return ;}
	StyledDocument doc = this.getStyledDocument();
	doc.setCharacterAttributes(0,chain.getLength(), this.getStyle("black"),true);
	doc.setCharacterAttributes(seqpos,1, this.getStyle("red"),true);
	

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
    public ISearchListener (SPICEFrame spiceparent, SeqTextPane textpane) {
	spice      = spiceparent ;
	parent     = textpane ;
	startpos   = 0 ;
	lasthit    = 0 ;
	searchtext = "" ;

	validText = Pattern.compile("[a-zA-Z]");
	
    }
    
    

    public void keyReleased(KeyEvent e){
	int code = e.getKeyCode();

     	String s = e.getKeyText(code);
	logger.finest("SeqPanel pressed " + s);

	Matcher m = validText.matcher(s);

	if ( code == KeyEvent.VK_BACK_SPACE) {
	    // if keyPressed = backspace, remove last character
	    if (searchtext.length() > 0) {
		searchtext = searchtext.substring(0,searchtext.length()-1);
		logger.finest("new searchtext " + searchtext);
	    }
	} 
	else if ( code == KeyEvent.VK_ENTER) {
	    // if keyPressed = Enter, search from startpos +1
	    startpos = lasthit + searchtext.length()-1;
	    logger.finest("starting search from here " + searchtext);
	}
	else if ( code == KeyEvent.VK_HOME) {
	    // if keyPressed = Home , move to first character.
	    logger.finest("going home " );
	    searchtext = "" ;
	    startpos = 0 ;
	    lasthit  = 0 ;
	} 
	else if ( m.matches() ) {
	    // this text can be added to searchtext varaiable
	    searchtext += (s);
	    logger.finest("new searchtext " + searchtext);
	}
	
	
	
	// display popup with current searchtext.
	// TODO ...


	int start = getStartPos();
	if ( start != -1 ) {
	    lasthit = start ;
	    // highlite text
	    int chainnr = spice.getCurrentChain();
	    spice.highlite(chainnr,start,start+searchtext.length()-1);
	} else {
	    logger.finest("no substring " + searchtext + " found");
	    startpos = 0 ;
	    lasthit  = 0 ;
	}

	
    }
    
    private int getStartPos(){
	String sequence = parent.getText();
	return sequence.indexOf(searchtext,startpos);
    }
    
}

