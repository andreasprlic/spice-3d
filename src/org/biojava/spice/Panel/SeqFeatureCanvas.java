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

import org.biojava.bio.structure.Chain ;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Canvas;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Font ;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.util.List ;
import java.util.ArrayList ;
import java.util.HashMap ;

import java.io.*             ;
import java.util.Iterator    ;
import java.util.Date        ;
import java.util.Calendar    ;
import java.util.logging.*   ;
import javax.swing.JPanel    ;
import javax.swing.JLabel    ;
import javax.swing.ImageIcon ;




//import ToolTip.* ;

/**
 * A class the provides a graphical reqpresentation of the Features of a protein sequences. Requires an arraylist of features.
 @author Andreas Prlic
 */
public class SeqFeatureCanvas 
    extends JPanel 
    implements SeqPanel, MouseListener, MouseMotionListener
				      
{


    public static final int    DEFAULT_X_START  = 60  ;
    public static final int    DEFAULT_X_RIGHT_BORDER = 20 ;
    public static final int    DEFAULT_Y_START  = 16 ;
    public static final int    DEFAULT_Y_STEP   = 10 ;
    public static final int    DEFAULT_Y_HEIGHT = 4 ;
    public static final int    DEFAULT_Y_BOTTOM  = 16 ;
    public static final int    TIMEDELAY        = 0 ;
    
    int selectPosition = -1 ;
    // the master application
    SPICEFrame spice ;

    //Image imbuf;
    //Dimension imbufDim;
    Color[] entColors ;
	
    Color seqColorOld ;
    Color strucColorOld ;
	
    int current_chainnumber;
	
    //int seqPosOld, strucPosOld ;
	
    float scale ;
	
    List drawLines ;
    //ArrayList features ;
    Chain chain ;
	
	
    //mouse events
    Date lastHighlight ;
    // highliting

    Font plainFont ;
    Logger logger        ;
    /**
     * 
     */
    public SeqFeatureCanvas(SPICEFrame spicefr ) {
	super();
	logger = Logger.getLogger("org.biojava.spice");
	setDoubleBuffered(true) ;

	// TODO Auto-generated constructor stub
	Dimension dstruc=this.getSize();
	//imbuf    = this.createImage(dstruc.width, dstruc.height);
	//imbuf = null ;
	
	//imbufDim = dstruc;
	//Graphics gstruc=imbuf.getGraphics();
	//gstruc.drawImage(imbuf, 0, 0, this);

	spice = spicefr;
	//features = new ArrayList();
	drawLines = new ArrayList();
	chain = null ;
		
	entColors = new Color [7];
	entColors[0] = Color.blue;
	entColors[1] = Color.pink;
	entColors[2] = Color.green;
	entColors[3] = Color.magenta;
	entColors[4] = Color.orange;
	entColors[5] = Color.pink;
	entColors[6] = Color.cyan;
	//this.setBackground(Color.black) ;
	//super.setBackground(Color.black) ;

	lastHighlight = new Date();
				
	current_chainnumber = -1 ;

	plainFont = new Font("SansSerif", Font.PLAIN, 10);
	//setOpaque(true);

	//this.paintComponent(this.getGraphics());
	//ImageIcon icon = new ImageIcon(imbuf);
	//setIcon(icon);
	//this.repaint();
	
    }


    public void setFeatures( List feats) {
	//logger.finest("DasCanv setFeatures");
	//features = feats ;
	// check if features are overlapping, if yes, add to a new line 

	drawLines          = new ArrayList();
	List currentLine   = new ArrayList();
	Feature oldFeature = new Feature();
	boolean start      = true ;
	String featureSource = "" ;
	
	for (int i=0; i< feats.size(); i++) {
	    Feature feat = (Feature)feats.get(i);

	    String ds =feat.getSource();
	    // check if new feature source
	    if ( ! featureSource.equals(ds) ) {
		//logger.finest("new DAS source " + ds );
		
		if ( ! start) {
		    drawLines.add(currentLine);
		}
		Feature tmpfeat = new Feature();
		tmpfeat.setSource(ds);
		tmpfeat.setMethod("_SPICE_LINESEPARATOR");		
		currentLine = new ArrayList();
		currentLine.add(tmpfeat);
		drawLines.add(currentLine);
		currentLine = new ArrayList();
		
	    }
	    featureSource = ds ;


	    // check for type
	    //logger.finest(feat);
	    if (oldFeature.getType().equals(feat.getType())){
		// see if they are overlapping
		if ( overlap(oldFeature,feat)) {
		    //logger.finest("OVERLAP found!" + oldFeature+ " "+ feat);
		    drawLines.add(currentLine);
		    currentLine = new ArrayList();
		    currentLine.add(feat);
		    oldFeature = feat ; 
		} else {
		    // not overlapping, they fit into the same line
		    //logger.finest("same line" +  oldFeature+ " "+ feat);
		    currentLine.add(feat);
		    oldFeature = feat ;
		}
	    } else {
		// a new feature type has been found
		// always put into a new line
		if ( ! start ) {
		    drawLines.add(currentLine);
		    currentLine = new ArrayList();
		}
		start = false;
		currentLine.add(feat);
		oldFeature = feat ;
	    }
	}

	drawLines.add(currentLine);

	int height = getImageHeight();
	Dimension dstruc=this.getSize();


	int width  =  dstruc.width ;
	this.setPreferredSize(new Dimension(width,height)) ;
	this.setMaximumSize(new Dimension(width,height));
	
	this.repaint();

    }
    
    /** return height of image. dpends on number of features! */
    
    private int getImageHeight(){
	int h = DEFAULT_Y_START + drawLines.size() * DEFAULT_Y_STEP + DEFAULT_Y_BOTTOM; 
	//logger.finest("setting height " + h);
	return h ;
    }

   
    // an overlap occurs if any of the segments overlap ...

    private boolean overlap (Feature a, Feature b) {
	List segmentsa = a.getSegments() ;
	List segmentsb = b.getSegments();

	for ( int i =0; i< segmentsa.size();i++) {
	    Segment sa = (Segment)segmentsa.get(i) ;
	    int starta = sa.getStart();
	    int enda   = sa.getEnd();
		
	    for (int j = 0; j<segmentsb.size();j++){
		Segment sb = (Segment)segmentsb.get(j) ;
		
		// compare boundaries:
		int startb = sb.getStart();
		int endb   = sb.getEnd();

		// overlap!
		if  (( starta <= endb ) && ( enda >= startb)) 
		    return true ;		
		if  (( startb <= enda ) && ( endb >= starta))
		    return true ;
		
	    }
	}
	return false ;
    }

    public void setChain(Chain c,int chainnumber) {
	chain = c;
	current_chainnumber = chainnumber ;
	//this.paintComponent(this.getGraphics());
	this.repaint();
    }
    

    /** select a single segment */
    private void selectSegment (Segment segment) {
	logger.finest("select Segment");
	/*


	// clicked on a segment!
	Color col =  segment.getColor();
	//seqColorOld = col ;
	//spice.setOldColor(col) ;
	
	int start  = segment.getStart() ;
	int end    = segment.getEnd() ;
	
	String type =  segment.getType() ;
	if ( type.equals("DISULFID")){
	    logger.finest("selectSegment DISULFID " + (start+1) + " " + (end+1));
	    
	    //String cmd = spice.getSelectStr(current_chainnumber,start+1);
	    //cmd += spice.getSelectStr(current_chainnumber,end+1);
	    String pdb1 = spice.getSelectStrSingle(current_chainnumber,start+1);
	    String pdb2 = spice.getSelectStrSingle(current_chainnumber,end+1);
	    String cmd = "select "+pdb1 +", " + pdb2 + "; spacefill on; cpk on;" ;

	    spice.executeCmd(cmd);
	} else {
	    spice.select(current_chainnumber,start+1,end+1);
    	    
	}
	*/
    }

    /** highlite a single segment */
    private void highliteSegment (Segment segment) {
	logger.finest("highlite Segment");
	//logger.finest("segment");
	
	// clicked on a segment!
	String col =  segment.getTxtColor();
	//seqColorOld = col ;
	//spice.setOldColor(col) ;
	

	int start = segment.getStart();
	int end   = segment.getEnd()  ;
	start = start -1 ;
	end   = end   -1 ;
	String type =  segment.getParent().getType() ;
	//logger.finest(start+" " + end+" "+type);
	//logger.finest(segment);
	if ( type.equals("DISULFID")){
	    spice.highlite(current_chainnumber,start);
	    spice.highlite(current_chainnumber,end);

	    String pdb1 = spice.getSelectStrSingle(current_chainnumber,start);
	    String pdb2 = spice.getSelectStrSingle(current_chainnumber,end);
	    String cmd = "select "+pdb1 +", " + pdb2 + "; spacefill on; colour cpk;" ;

	    spice.executeCmd(cmd);


	} else if (type.equals("METAL") ){
	    spice.highlite(current_chainnumber,start+1,end+1  ,"cpk");
	    
	} else if ( (end - start) == 0 ) {
	    // feature of size 1
	    spice.highlite(current_chainnumber,start+1,start+1  ,"cpk");	     
	} else {
	    spice.colour(current_chainnumber,start+1  ,end+1  ,col);	    
	}
	
    }

    /** highlite all segments of a feature */
    private String highliteFeature(Feature feature){
	logger.finest("highlite feature " + feature);
	//Feature feature = (Feature) features.get(featurenr) ;
	//logger.finest("highlite feature " + feature);
	
	List segments = feature.getSegments() ;
	String cmd = "" ;
	
	for ( int i =0; i< segments.size() ; i++ ) {
	    Segment segment = (Segment) segments.get(i);
	    //highliteSegment(segment);

	    int start = segment.getStart();
	    int end   = segment.getEnd();
	    //logger.finest("highilte feature " +featurenr+" " + start + " " +end );
	    
	    if ( feature.getType().equals("DISULFID")){
		cmd += spice.getSelectStr(current_chainnumber,start-1);
		cmd += "colour cpk; spacefill on;";
		cmd += spice.getSelectStr(current_chainnumber,end-1);
		cmd += "colour cpk; spacefill on;";
		
	    } else {
		cmd += spice.getSelectStr(current_chainnumber,start,end);
		String col = segment.getTxtColor();
		cmd += "color "+ col +";";
	    } 
	    //if ( start == end ) {
	    //cmd += " spacefill on;";
	    //}
	}
	if ( ( feature.getType().equals("METAL")) ||
	     ( feature.getType().equals("SITE"))  ||
	     ( feature.getType().equals("ACT_SITE")) 	     
	       ){
	    cmd += " spacefill on; " ;
	}
		    
	//logger.finest("cmd: "+cmd); 
	return cmd ;

	
    }

    
    /** select single position
       select a position
     */
    public void select(int seqpos){	
	highlite(current_chainnumber,seqpos);

    }
    
    /** select range */
    public void select(int start, int end) {
	// don't know what to do ...
	return ;
    }

    
    /** same as select here */
    public void highlite(int seqpos){	
	highlite(current_chainnumber,seqpos);

    }
    
    /* draw a line at current seqence position
     * only if chain_number is currently being displayed
     */
    public void highlite(int chain_number, int seqpos){
	if  ( chain == null   ) return ;
	
	if (chain_number == current_chainnumber) {
	    if ( seqpos > chain.getLength()) 
		return ;
	    selectPosition = seqpos ;
	}
	else 
	    selectPosition = -1 ;
	
	this.repaint();

    }
    


    public void paintComponent( Graphics g) {
	super.paintComponent(g); 	

	//logger.finest("PAINTINGDAS!!!") ;	
	//logger.finest("DasCanv - paintComponent");
	//logger.finest(this.getBackground());


	if ( chain == null   ) return ;

	Dimension dstruc=this.getSize();
	Image imbuf = this.createImage(dstruc.width,dstruc.height);
	
	Graphics gstruc = g ;
	//Graphics gstruc = imbuf.getGraphics();

	gstruc.setFont(plainFont);

	//gstruc.setBackground(this.getBackground());
	//gstruc.setColor(this.getBackground());
	//gstruc.fillRect(0 , 0, dstruc.width, dstruc.height);
	//g.clearRect(0 , 0, dstruc.width, dstruc.height);
	

	float chainlength =  chain.getLength() ;
		
	// scale should span the whole length ...
	scale = (dstruc.width ) / (DEFAULT_X_START + chainlength + DEFAULT_X_RIGHT_BORDER  ) ; 
	//logger.finest("scale:" +scale+ " width: "+dstruc.width + " chainlength: "+chainlength );
	

	// reset image
	//gstruc.setColor(this.getBackground());
	//gstruc.fillRect(0 , 0, dstruc.width, dstruc.height);


	// draw scale
	gstruc.setColor(Color.GRAY);
	//gstruc.fillRect(0, 0, seqx, 1);
		
	for (int i =0 ; i< chainlength ; i++){
	    if ( (i%100) == 0 ) {
		int xpos = Math.round(i*scale)+DEFAULT_X_START ;
		gstruc.fillRect(xpos, 0, 1, 8);
	    }else if  ( (i%50) == 0 ) {
		int xpos = Math.round(i*scale)+DEFAULT_X_START ;
		gstruc.fillRect(xpos, 0, 1, 6);
	    } else if  ( (i%10) == 0 ) {
		int xpos = Math.round(i*scale)+DEFAULT_X_START ;
		gstruc.fillRect(xpos, 0, 1, 4);
	    }
	}
		
				
	// draw sequence
	gstruc.setColor(Color.white);
	int seqx = java.lang.Math.round(chainlength * scale) ;


	gstruc.fillRect(0+DEFAULT_X_START, 10, seqx, 6);

	int y = DEFAULT_Y_START ;
	// draw features
		
	//logger.finest("number features: "+features.size());

	int aminosize = Math.round(1 * scale) ;

	boolean secstruc = false ;


	for (int i = 0 ; i< drawLines.size();i++) {
	    //logger.finest(i%entColors.length);
	    //gstruc.setColor(entColors[i%entColors.length]);

	    y = y + DEFAULT_Y_STEP ;

	    List features = (List) drawLines.get(i) ;

	    for ( int f =0 ; f< features.size();f++) {

		Feature feature = (Feature) features.get(f);
	
		// line separator

		if ( feature.getMethod().equals("_SPICE_LINESEPARATOR")) {
		    //logger.finest("_SPICE_LINESEPARATOR");
		    String ds = feature.getSource();
		    gstruc.setColor(Color.white);
		    gstruc.drawString(ds,DEFAULT_X_START,y+DEFAULT_Y_HEIGHT);
		    continue ;
		}
		
		
		List segments = feature.getSegments() ;
	    
		// draw text
		if ( segments.size() < 1) {
		    //logger.finest(feature.getMethod());
		    continue ;
		}
		Segment seg0 = (Segment) segments.get(0) ;
		Color col =  seg0.getColor();	
		gstruc.setColor(col);
		
		gstruc.drawString(feature.getName(), 1,y+DEFAULT_Y_HEIGHT);
		
		for (int s=0; s<segments.size();s++){
		    Segment segment=(Segment) segments.get(s);
		    
		    int start     = segment.getStart()-1 ;
		    int end       = segment.getEnd()  -1 ;
		    
		    col = segment.getColor();
		    gstruc.setColor(col);
		    
		    int xstart =  java.lang.Math.round(start * scale) + DEFAULT_X_START;
		    int width   = java.lang.Math.round(end * scale) - xstart +  DEFAULT_X_START+aminosize ;
		    
		    int height = DEFAULT_Y_HEIGHT ;
		    //logger.finest(feature+ " " + end +" " + width);
		    //logger.finest("color"+entColors[i%entColors.length]);
		    //logger.finest("new feature  ("+i+"): x1:"+ xstart+" y1:"+y+" width:"+width+" height:"+height);
		    String type = feature.getType() ;
		    if (  type.equals("DISULFID")){
		
			gstruc.fillRect(xstart,y,aminosize,height);
			gstruc.fillRect(xstart,y+(height/2),width,1);
			gstruc.fillRect(xstart+width-aminosize,y,aminosize,height);
		    } else {
			gstruc.fillRect(xstart,y,width,height);
		    }
		}
	    }
	    
	    //int seqpos =  java.lang.Math.round(x/scale) ;
	    if ( selectPosition > -1 ) {
		gstruc.setColor(Color.darkGray);
		seqx = java.lang.Math.round(selectPosition*scale)+DEFAULT_X_START ;
		//aminosize = java.lang.Math.round(1*scale) ;
		int tmpfill ;
		if (aminosize <1) tmpfill = 1;
		else tmpfill = aminosize ;
		//logger.finest("draw rec");
		gstruc.fillRect(seqx , 0, tmpfill, dstruc.height);
	    }

	    //g.drawImage(imbuf,0,0,this.getBackground(),this);
	}
	

	
    }
	
    /* check if the mouse is over a feature and if it is 
     * return the feature number 
     * @author andreas
     *
     * To change the template for this generated type comment go to
     * Window - Preferences - Java - Code Generation - Code and Comments
     */
    private int getLineNr(int mouseY){
		
	float top = mouseY - DEFAULT_Y_START +1 ;
	// java.lang.Math.round((y-DEFAULT_Y_START)/ (DEFAULT_Y_STEP + DEFAULT_Y_HEIGHT-1));
	float interval  = DEFAULT_Y_STEP  ;
		
	int linenr = java.lang.Math.round(top/interval) -1 ;
	//logger.finest("top "+top+" interval "+ interval + " top/interval =" + (top/interval) );	
	if ( linenr >= drawLines.size()){
	    // can happen at bottom part
	    // simply skip it ...
	    return -1;
	}
			
	return linenr ;
			
    }

    private Feature getFeatureAt(int seqpos, int lineNr){
	//logger.finest("getFeatureAt " + seqpos + " " + lineNr);
	Segment s = getSegmentUnder(seqpos,lineNr);
	if (s == null ) 
	    return null;
	
	return s.getParent();
	
    }


    /** check if mouse is over a segment and if it is, return the segment */
    private Segment getSegmentUnder( int seqpos,int lineNr){

	if ( ( lineNr < 0) || ( lineNr >= drawLines.size() ) ){
	    return null ;
	}

	List features = (List) drawLines.get(lineNr);

	for ( int i =0; i<features.size();i++ ){
	    Feature feature = (Feature) features.get(i) ;
	    List segments = feature.getSegments() ;
	    for ( int j =0; j< segments.size() ; j++ ) {
		
		Segment segment = (Segment) segments.get(j);
		int start = segment.getStart() -1 ;
		int end   = segment.getEnd()   -1 ;
		
		if ( (start <= seqpos) && (end >= seqpos) ) {
		    return segment ;
		}
	    }
	}
	return null ;
    }
    

    /** create a tooltip string */
    private String getToolString(int seqpos, Segment segment){
	//logger.finest("getToolString");
	// current position is seqpos
	String toolstr = spice.getToolString(current_chainnumber,seqpos);
	
	//logger.finest("getToolString");
	
	Feature parent = segment.getParent() ;
	String method  = parent.getMethod() ;
	String type    = parent.getType() ;
	String note    = parent.getNote() ;
	
	int start = segment.getStart() -1 ; 
	int end   = segment.getEnd()   -1 ; 
	
	 toolstr += " " + type + " start " + start + " end " + end + " Note:" + note; 

	//logger.finest(toolstr);
		
	//new ToolTip(toolstr, this);
	//this.repaint();
	
	return toolstr ;

    }

    /** test if the click was on the name of the feature */
    private boolean nameClicked(int x) {
	if (x <= DEFAULT_X_START) {
	    return true ;
	}
	return false ;
    }


    public void mouseDragged(MouseEvent e) {
	logger.finest("dragging mouse "+e);
    }	
    

    public void mouseMoved(MouseEvent e)
    {	
	//, int x, int y
	int x = e.getX();
	int y = e.getY();
	int seqpos =  java.lang.Math.round((x-DEFAULT_X_START)/scale) ;
		
	int linenr   = getLineNr(y);
	
       
	//int featurenr = get_featurenr(y); 

	if ( linenr < 0 ) return ;
	if ( seqpos    < 0 ) return ; 

	//String drstr = "x "+ seqpos + " y " + featurenr ;

	//g.drawString(drstr,5,13);
	
	spice.showSeqPos(current_chainnumber,seqpos);
	//spice.showStatus(drstr);
	
	// and the feature display
	
	Feature feature = getFeatureAt(seqpos,linenr);

	Segment segment = getSegmentUnder(seqpos,linenr);
	if ( segment != null) {
	    String toolstr = getToolString(seqpos,segment);
	    //gfeat.drawString(toolstr,5,13);
	    spice.showStatus(toolstr) ;
	    // display ToolTip
	    this.setToolTipText(toolstr);
	} else {
	    this.setToolTipText(null);
	}


	// add a new Thread that highlites the position of the mouse every two seconds.
	
	
	// add a time delay for repainting the bar
	Date currentTime = new Date();
	long timediff = currentTime.getTime() - lastHighlight.getTime() ;
	//logger.finest("timediff:" + timediff);
	if ( timediff  > TIMEDELAY) {
	    //logger.finest("highliting "+current_chainnumber + " " + seqpos);
	    //highlite(current_chainnumber,seqpos);	    
	    spice.select(current_chainnumber,seqpos);
	    lastHighlight = currentTime ;
	}

	
	return  ;
    }
	
    
    public void mouseClicked(MouseEvent e)
    {
	//logger.finest("CLICK");
	int x = e.getX();
	int y = e.getY();

	int seqpos =  java.lang.Math.round((x-DEFAULT_X_START)/scale)  ;		
	int lineNr    = getLineNr(y);
	
	//int featurenr = get_featurenr(y) ;
	//logger.finest("CLICK! "+seqpos + " " +lineNr+ " " + chain.getLength());
	
	if ( lineNr < 0 ) return ;
	if ( seqpos    < 0 ) {
	    // check if the name was clicked
	    if (nameClicked(x)){
		// highlight all features in the line
		//Feature feature = getFeatureAt(seqpos,lineNr);
		List features = (List) drawLines.get(lineNr);
		String cmd = "";
		for ( int i=0; i<features.size(); i++) {
		    Feature feature = (Feature) features.get(i);
		    cmd += highliteFeature(feature);		    
		}
		logger.finest(cmd);
		spice.executeCmd(cmd);
		return  ;
	    }
	} 

	
	if ( seqpos    > chain.getLength()) return ; 

	String drstr = "x "+ seqpos + " y " + lineNr ;

	spice.showStatus(drstr);
	Segment segment = getSegmentUnder(seqpos,lineNr);
	//logger.finest(segment);
	if (segment != null ) {
	
	    highliteSegment(segment);
       
	    
	    String toolstr = getToolString(seqpos,segment);
	    spice.showStatus(drstr + " " + toolstr) ;
	    this.setToolTipText(toolstr);

	   	
	} else {
	    spice.highlite(current_chainnumber,seqpos);
	    this.setToolTipText(null);
	}
	//this.repaint();
	    //Color col = entColors[featurenr%entColors.length] ;
	
	//logger.finest("clicked outa space");
	return  ;
    }	
    
    public void mouseEntered(MouseEvent e)  {}
    public void mouseExited(MouseEvent e)   {}
    public void mousePressed(MouseEvent e)  {}
    public void mouseReleased(MouseEvent e) {}
    
    
}


