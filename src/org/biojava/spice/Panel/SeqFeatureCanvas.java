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
import java.awt.*;


import java.util.ArrayList ;
import java.util.HashMap ;

import java.io.* ;
import java.util.Iterator ;

//import ToolTip.* ;

/**
 * requires an arraylist of featrures
 */
public class SeqFeatureCanvas extends Canvas
{

    public static final int    DEFAULT_X_START  = 60  ;
    public static final int    DEFAULT_X_RIGHT_BORDER = 20 ;
    public static final int    DEFAULT_Y_START  = 16 ;
    public static final int    DEFAULT_Y_STEP   = 10 ;
    public static final int    DEFAULT_Y_HEIGHT = 4 ;

    // the master application
    SPICEFrame spice ;

    Image imbuf;
    Dimension imbufDim;
    Color[] entColors ;
	
    Color seqColorOld ;
    Color strucColorOld ;
	
    int current_chainnumber;
	
    //int seqPosOld, strucPosOld ;
	
    float scale ;
	
    ArrayList features ;
    Chain chain ;
	
	
    //mouse events

    // highliting
    int seqOldPos ;
	

    /**
     * 
     */
    public SeqFeatureCanvas(SPICEFrame spicefr ) {
	super();


	// TODO Auto-generated constructor stub
	imbuf=null; 
	imbufDim = new Dimension(-1, -1);
	spice = spicefr;
	features = new ArrayList();
	chain = null ;
		
	entColors = new Color [7];
	entColors[0] = Color.blue;
	entColors[1] = Color.pink;
	entColors[2] = Color.green;
	entColors[3] = Color.magenta;
	entColors[4] = Color.orange;
	entColors[5] = Color.pink;
	entColors[6] = Color.cyan;
		
				
	current_chainnumber = -1 ;
	seqOldPos = -1 ;

    }


    public void setFeatures( ArrayList feats) {
	//System.out.println("DasCanv setFeatures");
	features = feats ;
	this.paint(this.getGraphics());

    }
    
    public void setChain(Chain c,int chainnumber) {
	chain = c;
	current_chainnumber = chainnumber ;
    }
    

    /** select a single segment */
    private void selectSegment (HashMap segment) {
	System.out.println("select Segment");

	// clicked on a segment!
	Color col = (Color) segment.get("color");
	//seqColorOld = col ;
	//spice.setOldColor(col) ;
	
	String fstart  = (String)segment.get("START") ;
	String fend    = (String)segment.get("END") ;
	int start = Integer.parseInt(fstart)-1 ;
	int end   = Integer.parseInt(fend)-1 ;
	
	String type = (String) segment.get("TYPE") ;
	if ( type.equals("DISULFID")){
	    System.out.println("selectSegment DISULFID " + (start+1) + " " + (end+1));
	    
	    //String cmd = spice.getSelectStr(current_chainnumber,start+1);
	    //cmd += spice.getSelectStr(current_chainnumber,end+1);
	    String pdb1 = spice.getSelectStrSingle(current_chainnumber,start+1);
	    String pdb2 = spice.getSelectStrSingle(current_chainnumber,end+1);
	    String cmd = "select "+pdb1 +", " + pdb2 + "; spacefill on; cpk on;" ;

	    spice.executeCmd(cmd);
	} else {
	    spice.select(current_chainnumber,start+1,end+1);
    	    
	}
    }

    /** highlite a single segment */
    private void highliteSegment (HashMap segment) {
	//System.out.println("highlite Segment");

	// clicked on a segment!
	String col = (String) segment.get("colorTxt");
	//seqColorOld = col ;
	//spice.setOldColor(col) ;
	
	String fstart  = (String)segment.get("START") ;
	String fend    = (String)segment.get("END") ;
	//System.out.println("fstart:"+fstart+" fend:"+fend);
	int start = Integer.parseInt(fstart) ;
	int end   = Integer.parseInt(fend) ;
	start = start -1 ;
	end   = end   -1 ;
	String type = (String) segment.get("TYPE") ;
	System.out.println(start+" " + end+" "+type);
	System.out.println(segment);
	if ( type.equals("DISULFID")){
	    //spice.highlite(current_chainnumber,start+1,start+1,"cpk");
	    //spice.highlite(current_chainnumber,end+1  ,end+1  ,"cpk"); 
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
    private void highliteFeature(int featurenr){
	Feature feature = (Feature) features.get(featurenr) ;
		
	ArrayList segments = feature.getSegments() ;
	String cmd = "" ;
	for ( int i =0; i< segments.size() ; i++ ) {
	    HashMap segment = (HashMap) segments.get(i);
	    //highliteSegment(segment);
	    String fstart  = (String)segment.get("START") ;
	    String fend    = (String)segment.get("END") ;
	    int start = Integer.parseInt(fstart) -1 ;
	    int end   = Integer.parseInt(fend)-1 ;
	    cmd += spice.getSelectStr(current_chainnumber,start,end);
	    String col = (String) segment.get("colorTxt");
	    cmd += "color "+ col +";";
	    //if ( start == end ) {
	    //cmd += " spacefill on;";
	    //}
	}
	System.out.println("cmd: "+cmd);
	spice.executeCmd(cmd);
    }


    /* draw a line at current seqence position
     * only if chain_number is currently being displayed
     */
    public void highlite(int chain_number, int seqpos){
				
	Dimension dstruc=this.getSize();
		
	Graphics gstruc=imbuf.getGraphics();
		
	if (seqOldPos != -1) {
	    // clean old highlited region
	    gstruc.setColor(this.getBackground());
	    gstruc.fillRect(0 , 0, dstruc.width, dstruc.height);
	}
		
				
	if (chain_number == current_chainnumber) {
	    //int seqpos =  java.lang.Math.round(x/scale) ;
	    gstruc.setColor(Color.darkGray);
	    int seqx = java.lang.Math.round(seqpos*scale)+DEFAULT_X_START ;
	    int aminosize = java.lang.Math.round(1*scale) ;
	    int tmpfill ;
	    if (aminosize <1) tmpfill = 1;
	    else tmpfill = aminosize ;
				  
	    gstruc.fillRect(seqx , 0, tmpfill, dstruc.height);
	    this.repaint();
	    seqOldPos = seqpos ;
	}
    }
	
    public void paint(Graphics g){

	if ( chain == null   ) return ;
	//System.out.println("PAINTINGDAS!!!") ;
		
	//System.out.println("DasCAnv - paint");
	Dimension dstruc=this.getSize();

	if(!imbufDim.equals(dstruc)) spice.scale();
		
	if(imbuf == null || !imbufDim.equals(dstruc)) {
	    imbuf=this.createImage(dstruc.width, dstruc.height);
	    imbufDim = dstruc;
	}
		
	Graphics gstruc=imbuf.getGraphics();
	
	Color bg=this.getBackground();
		
	float chainlength =  chain.getLength() ;
		
	// scale should span the whole length ...
	scale = (dstruc.width ) / (DEFAULT_X_START + chainlength + DEFAULT_X_RIGHT_BORDER  ) ; 
	//System.out.println("scale:" +scale+ " width: "+dstruc.width + " chainlength: "+chainlength );

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
	//System.out.println(seqx);

	gstruc.fillRect(0+DEFAULT_X_START, 10, seqx, 6);

	int y = DEFAULT_Y_START ;
	// draw features
		
	//System.out.println("number features: "+features.size());

	int aminosize = Math.round(1 * scale) ;

	boolean secstruc = false ;
	for (int i = 0 ; i< features.size();i++) {
	    //System.out.println(i%entColors.length);
	    //gstruc.setColor(entColors[i%entColors.length]);

	    y = y + DEFAULT_Y_STEP ;
	    Feature feature = (Feature) features.get(i);
	    	    
	    //String type = (String)feature.get("TYPE") ;
	    //System.out.println("type "+type);
			
	    //if ( type.equals("reference")){
	    ///	continue ;
	    //}
			
	    //String featid = (String) feature.get("id");
	    //System.out.println(featid);
	    ArrayList segments = feature.getSegments() ;
	    
	    // draw text
	    HashMap seg0 = (HashMap) segments.get(0) ;
	    Color col = (Color) seg0.get("color");	
   	    gstruc.setColor(col);

	    gstruc.drawString(feature.getName(), 1,y+DEFAULT_Y_HEIGHT);
	    
	    for (int s=0; s<segments.size();s++){
		HashMap segment=(HashMap) segments.get(s);
		String fstart = (String)segment.get("START") ;
		String fend   = (String)segment.get("END") ;
		int start     = Integer.parseInt(fstart) -1 ;
		int end       = Integer.parseInt(fend)   -1 ;

		col = (Color) segment.get("color");
		gstruc.setColor(col);

		int xstart =  java.lang.Math.round(start * scale) + DEFAULT_X_START;
		int width   = java.lang.Math.round(end * scale) - xstart +  DEFAULT_X_START+aminosize ;

		int height = DEFAULT_Y_HEIGHT ;
		//System.out.println(feature+ " " + end +" " + width);
		//System.out.println("color"+entColors[i%entColors.length]);
		//System.out.println("new feature  ("+i+"): x1:"+ xstart+" y1:"+y+" width:"+width+" height:"+height);
		String type = (String) segment.get("TYPE") ;
		if ( ! type.equals("DISULFID")){
		    gstruc.fillRect(xstart,y,width,height);
		} else {
		    gstruc.fillRect(xstart,y,aminosize,height);
		    gstruc.fillRect(xstart,y+(height/2),width,1);
		    gstruc.fillRect(xstart+width-aminosize,y,aminosize,height);
		}
	    }
	}
	

	g.drawImage(imbuf, 0, 0, this);
	//this.repaint();
	
    }
	
    /* check if the mouse is over a feature and if it is 
     * return the feature number 
     * @author andreas
     *
     * To change the template for this generated type comment go to
     * Window - Preferences - Java - Code Generation - Code and Comments
     */
    private int get_featurenr(int mouseY){
		
	float top = mouseY - DEFAULT_Y_START +1 ;
	// java.lang.Math.round((y-DEFAULT_Y_START)/ (DEFAULT_Y_STEP + DEFAULT_Y_HEIGHT-1));
	float interval  = DEFAULT_Y_STEP  ;
		
	int featurenr = java.lang.Math.round(top/interval) -1 ;
	//System.out.println("top "+top+" interval "+ interval + " top/interval =" + (top/interval) );	
	if ( featurenr >= features.size()){
	    // can happen at bottom part
	    // simply skip it ...
	    return -1;
	}
			
	return featurenr ;
			
    }


    /** check if mouse is over a segment and if it is, return the segment */
    private HashMap getSegmentUnder(int featurenr, int seqpos){
	if ( ( featurenr < 0) || ( featurenr >= features.size() ) ){
	    return null ;
	}
	Feature feature = (Feature) features.get(featurenr) ;
	
	
	ArrayList segments = feature.getSegments() ;
	for ( int i =0; i< segments.size() ; i++ ) {
	    
	    HashMap segment = (HashMap) segments.get(i);
	    
	    String fstart = (String)segment.get("START") ;
	    String fend   = (String)segment.get("END") ;
	    
	    int start = Integer.parseInt(fstart) -1 ;
	    int end   = Integer.parseInt(fend)-1 ;
	    
	    if ( (start <= seqpos) && (end >= seqpos) ) {
		return segment ;
	    }
	}
	return null ;
    }

    /** create a tooltip string */
    private String getToolString(int seqpos, HashMap segment){
	// current position is seqpos
	String toolstr = spice.getToolString(current_chainnumber,seqpos);
	
	String method  = (String)segment.get("METHOD") ;
	String note    = (String)segment.get("NOTE") ;
	String fstart  = (String)segment.get("START") ;
	String fend    = (String)segment.get("END") ;
	int start = Integer.parseInt(fstart) -1 ;
	int end   = Integer.parseInt(fend)-1 ;
	
	 toolstr += " " + method + " start " + start + " end " + end + " Note:" + note;
	//System.out.println(toolstr);
		
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

	
    public boolean mouseMove(Event e, int x, int y)
    {	
	int seqpos =  java.lang.Math.round((x-DEFAULT_X_START)/scale) ;
		
	int featurenr = get_featurenr(y); 

	if ( featurenr < 0 ) return true;
	if ( seqpos    < 0 ) return true; 

	//String drstr = "x "+ seqpos + " y " + featurenr ;

	//g.drawString(drstr,5,13);

	spice.showSeqPos(current_chainnumber,seqpos);
	//spice.showStatus(drstr);

	
	// and the feature display
	
	HashMap segment = getSegmentUnder(featurenr,seqpos);
	if ( segment != null) {
	    String toolstr = getToolString(seqpos,segment);
	    //gfeat.drawString(toolstr,5,13);
	    spice.showStatus(toolstr) ;
	}

	spice.select(current_chainnumber,seqpos);
	
	return true ;
    }
	
    
    public boolean mouseUp(Event e, int x, int y)
    {

	
	
	int seqpos =  java.lang.Math.round((x-DEFAULT_X_START)/scale)  ;
		
	int featurenr = get_featurenr(y) ;
	System.out.println("CLICK! "+seqpos + " " +featurenr+ " " + chain.getLength());
	if ( featurenr < 0 ) return true;
	if ( seqpos    < 0 ) {
	    // check if the name was clicked
	    if (nameClicked(x)){
		// highlight all features in the line
		highliteFeature(featurenr);
		return true ;
	    }
	} 

	
	if ( seqpos    > chain.getLength()) return true; 

	String drstr = "x "+ seqpos + " y " + featurenr ;

	spice.showStatus(drstr);

	
	HashMap segment = getSegmentUnder(featurenr,seqpos);
	//System.out.println(segment);
	if (segment != null ) {
	
	    highliteSegment(segment);
       
	    
	    String toolstr = getToolString(seqpos,segment);
	    spice.showStatus(drstr + " " + toolstr) ;

	   	
	} else {
	    spice.highlite(current_chainnumber,seqpos);
	}
	//this.repaint();
	    //Color col = entColors[featurenr%entColors.length] ;
	
	//System.out.println("clicked outa space");
	return true ;
    }	
}


