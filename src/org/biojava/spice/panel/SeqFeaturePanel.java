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

import org.biojava.spice.SPICEFrame  	   	;
import org.biojava.spice.GUI.SelectionLockMenuListener;

import org.biojava.spice.Feature.*     	;
import org.biojava.spice.Panel.SeqPanel	;
import org.biojava.bio.structure.Chain 	;
import org.biojava.bio.structure.Group  	;


import java.awt.Color                     ;
import java.awt.Graphics                  ;
//import java.awt.image.BufferedImage       ;
import java.awt.Font                      ;
import java.awt.Dimension                 ;

import java.awt.event.ActionListener;
import java.awt.event.MouseListener       ; 
import java.awt.event.MouseMotionListener ;
import java.awt.event.MouseEvent          ;
import java.awt.Graphics2D                ;
import java.awt.* ;

import java.net.URL;

import java.util.List                     ;
import java.util.ArrayList                ;

import java.util.Date        ;
import java.util.logging.Logger;
//import java.uti.logging.*   ;
import javax.swing.JMenuItem;
import javax.swing.JPanel    ;
import javax.swing.JPopupMenu;

import java.net.MalformedURLException;




//import ToolTip.* ;

/**
 * A class the provides a graphical reqpresentation of the Features of a protein sequences. Requires an arraylist of features.
 @author Andreas Prlic
 */
public class SeqFeaturePanel 
extends JPanel 
implements SeqPanel, MouseListener, MouseMotionListener

{
    private static final long serialVersionUID = 94293124130433611L;
    
    public static final int    DEFAULT_X_START        = 60  ;
    public static final int    DEFAULT_X_RIGHT_BORDER = 20 ;
    public static final int    DEFAULT_Y_START        = 30 ;
    public static final int    DEFAULT_Y_STEP         = 10 ;
    public static final int    DEFAULT_Y_HEIGHT       = 4 ;
    public static final int    DEFAULT_Y_BOTTOM       = 16 ;
    
    // the line where to draw the structure
    public static final int    DEFAULT_STRUCTURE_Y    = 20 ;
    public static final int    TIMEDELAY              = 300 ;
    public static final Color SELECTION_COLOR         = Color.lightGray;
    public static final Color STRUCTURE_COLOR         = Color.red;
    public static final Color STRUCTURE_BACKGROUND_COLOR = new Color(0.5f, 0.1f, 0.5f, 0.5f);
    
    // use this font for the text
    public static final Font plainFont = new Font("SansSerif", Font.PLAIN, 10);
    
    static Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
    static Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    
    JPopupMenu popupMenu ;
    int oldposition;
    int selectStart ;
    int selectEnd      ;
    int mouseDragStart ;
    // the master application
    SPICEFrame spice ;
    
    
    Color seqColorOld ;
    Color strucColorOld ;
    
    Chain chain      ;
    int current_chainnumber;
    
    //int chainlength ;
    //int seqPosOld, strucPosOld ;
    
    float scale ;
    
    List drawLines ;
    ArrayList knownLinks;
    
    //mouse events
    Date lastHighlight ;
    // highliting
    
    boolean dragging;
    
    //Logger logger        ;
    static Logger logger      = Logger.getLogger("org.biojava.spice");
    
    /**
     * 
     */
    public SeqFeaturePanel(SPICEFrame spicefr ) {
        super();
        //int oldposition = -1;
        //logger = Logger.getLogger("org.biojava.spice");
        setDoubleBuffered(true) ;
        
        // TODO REMOVE THIS CLASS - IT IS OBSOLETE
        //Dimension dstruc=this.getSize();
        //imbuf    = this.createImage(dstruc.width, dstruc.height);
        //imbuf = null ;
        
        //imbufDim = dstruc;
        //Graphics gstruc=imbuf.getGraphics();
        //gstruc.drawImage(imbuf, 0, 0, this);
        
        spice = spicefr;
        //features = new ArrayList();
        drawLines = new ArrayList();
        knownLinks = new ArrayList();
        chain = null ;
        
        lastHighlight = new Date();
        
        current_chainnumber = -1 ;
        
        setOpaque(true);
        
        selectStart    = -1 ;
        selectEnd      =  1 ;
        mouseDragStart = -1 ;
        dragging = false;
        
        
        popupMenu = new JPopupMenu();
        
        
        //SelectionLockMenuListener ml = new SelectionLockMenuListener(spice,null);
        
        //JMenuItem menuItem = new JMenuItem("lock selection");
        //menuItem.addActionListener(ml);
        //popupMenu.add(menuItem);
        //menuItem = new JMenuItem("delete");
        //menuItem.addActionListener(ml);
        //tablePopup.add(menuItem);
        
        //MouseListener popupListener = new SelectionLockPopupListener(popupMenu,spice);
        //this.addMouseListener(popupListener);
        
    }
    
    
    public void paintComponent( Graphics g) {
        //logger.entering(this.getClass().getName(),"paintComponent");
        super.paintComponent(g); 	
        
        
        //logger.finest("DasCanv - paintComponent");
        
        if ( chain == null   ) return ;
        
        Dimension dstruc=this.getSize();
        //BufferedImage imbuf = (BufferedImage)
        this.createImage(dstruc.width,dstruc.height);
        
        Graphics2D g2D = (Graphics2D)g ;
        
        // Set current alpha
        Composite oldComposite = g2D.getComposite();
        
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));                 
        g2D.setFont(plainFont);
        
        int chainlength =  chain.getLength() ;
        
        // scale should span the whole length ...
        scale(dstruc);
        
        //logger.finest("scale:" +scale+ " width: "+dstruc.width + " chainlength: "+chainlength );
        
        
        // draw scale
        drawScale(g2D,chainlength);
        
        // draw sequence
        int seqx = drawSequence(g2D, chainlength);
        
        //	 minimum size of one aminoacid
        int aminosize = getAminoSize();
        
        // draw region covered with structure
        drawStructureRegion(g2D,aminosize);
        
        // draw features
        drawFeatures(g2D,aminosize, chainlength);
        
        // highlite selection
        drawSelection(g2D, aminosize, seqx);
        
        // copy new image to visible one
        g2D.setComposite(oldComposite);
        
        //	g.drawImage(imbuf,0,0,this.getBackground(),this);
        //logger.exiting(this.getClass().getName(),"paintComponent");
    }
    
    /** returns the size of one amino acid in the display; in pixel */
    public int getAminoSize(){
        return Math.round(1 * scale) ;
    }
    
    
    public void scale(Dimension dstruc){
        
        int chainlength =chain.getLength() ;
        scale = (dstruc.width ) / (float)(DEFAULT_X_START + chainlength + DEFAULT_X_RIGHT_BORDER ) ;
        
    }
    
    /** draw structrure covered region as feature */
    private void drawStructureRegion(Graphics2D g2D, int aminosize){
        // data is coming from chain;
        
        g2D.drawString("Structure",1,DEFAULT_STRUCTURE_Y+DEFAULT_Y_HEIGHT);
        
        int start = -1;
        int end   = -1;
        
        for ( int i=0 ; i< chain.getLength() ; i++ ) {
            Group g = chain.getGroup(i);
            
            if ( g.size() > 0 ){
                if ( start == -1){
                    start = i;
                }
                end = i;
            } else {
                if ( start > -1) {
                    drawStruc(g2D,start,end,aminosize);
                    start = -1 ;
                }
            }
        }
        // finish
        if ( start > -1) 
            drawStruc(g2D,start,end,aminosize);
        
    }
    private void drawStruc(Graphics2D g2D, int start, int end, int aminosize){
        //System.out.println("Structure " + start + " " + end);
        
        //int y = DEFAULT_STRUCTURE_Y ;
        
        int xstart = java.lang.Math.round(start * scale) + DEFAULT_X_START;
        int endx   = java.lang.Math.round(end * scale)-xstart + DEFAULT_X_START +aminosize;
        //int width  = aminosize ;
        //int height = DEFAULT_Y_HEIGHT ;
        
        // draw the red structure line
        //g2D.setColor(STRUCTURE_COLOR);	
        //g2D.fillRect(xstart,y,endx,height);
        
        
        // highlite the background
        Composite origComposite = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));
        g2D.setColor(STRUCTURE_BACKGROUND_COLOR);
        Dimension dstruc=this.getSize();
        Rectangle strucregion = new Rectangle(xstart , 0, endx, dstruc.height);
        g2D.fill(strucregion);
        g2D.setComposite(origComposite);
    }
    
    /** draw a line representing a sequence
     * returns the scaled length of the sequence */
    public  int drawSequence(Graphics2D g2D, float chainlength){
        g2D.setColor(Color.white);
        int seqx = java.lang.Math.round((chainlength-1) * scale) ;
        g2D.drawString("Sequence",1,10+DEFAULT_Y_HEIGHT);
        g2D.fillRect(0+DEFAULT_X_START, 10, seqx, 6);
        return seqx ;
    }
    
    /** draw the Scale */
    public void drawScale(Graphics2D g2D, int chainlength){
        g2D.setColor(Color.GRAY);
        
        for (int i =0 ; i< chainlength ; i++){
            if ( (i%100) == 0 ) {
                int xpos = Math.round(i*scale)+DEFAULT_X_START ;
                g2D.fillRect(xpos, 0, 1, 8);
            }else if  ( (i%50) == 0 ) {
                int xpos = Math.round(i*scale)+DEFAULT_X_START ;
                g2D.fillRect(xpos, 0, 1, 6);
            } else if  ( (i%10) == 0 ) {
                int xpos = Math.round(i*scale)+DEFAULT_X_START ;
                g2D.fillRect(xpos, 0, 1, 4);
            }
        }
    }
    
    /** draw the selected region */
    public void drawSelection(Graphics2D g2D, int aminosize, int seqx){
        
        
        if ( selectStart > -1 ) {
            Dimension dstruc=this.getSize();
            g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));        
            
            g2D.setColor(SELECTION_COLOR);
            seqx = java.lang.Math.round(selectStart*scale)+DEFAULT_X_START ;
            
            int selectEndX = java.lang.Math.round(selectEnd * scale)-seqx + DEFAULT_X_START +aminosize; 
            if ( selectEndX < aminosize) 
                selectEndX = aminosize ;
            
            if ( selectEndX  < 1 )
                selectEndX = 1 ;
            
            Rectangle selection = new Rectangle(seqx , 0, selectEndX, dstruc.height);
            g2D.fill(selection);
            
        }
    }
    
    
    
    
    
    /** draw the features */
    public void drawFeatures(Graphics g2D, int aminosize, int chainlength){
        int y = DEFAULT_Y_START ;
        drawFeatures(g2D,aminosize,y, chainlength);
    }
    /** draw the features starting at position y
     * returns the y coordinate of the last feature ;
     * */
    public int drawFeatures(Graphics g2D, int aminosize, int y, int chainlength){
        //logger.finest("number features: "+features.size());
        //System.out.println("seqFeatCanvas aminosize "+ aminosize);
        
        //boolean secstruc = false ;
        
        
        for (int i = 0 ; i< drawLines.size();i++) {
            //logger.finest(i%entColors.length);
            //g2D.setColor(entColors[i%entColors.length]);
            
            y = y + DEFAULT_Y_STEP ;
            
            List features = (List) drawLines.get(i) ;
            
            for ( int f =0 ; f< features.size();f++) {
                
                Feature feature = (Feature) features.get(f);
                
                // line separator
                logger.finest("drawing feature "+ feature);
                if ( feature.getMethod().equals("_SPICE_LINESEPARATOR")) {
                    //logger.finest("_SPICE_LINESEPARATOR");
                    String ds = "-- "+feature.getSource()+" --";
                    
                    g2D.setColor(Color.white);
                    g2D.drawString(ds,0,y+DEFAULT_Y_HEIGHT);
                    continue ;
                }
                
                
                List segments = feature.getSegments() ;
                
                // draw text
                //if ( segments.size() < 1) {
                //logger.finest(feature.getMethod());
                //  continue ;
                //}
                Segment seg0 = (Segment) segments.get(0) ;
                Color col =  seg0.getColor();	
                g2D.setColor(col);
                
                g2D.drawString(feature.getName(), 1,y+DEFAULT_Y_HEIGHT);
                
                for (int s=0; s<segments.size();s++){
                    Segment segment=(Segment) segments.get(s);
                    
                    int start     = segment.getStart() -1 ;
                    int end       = segment.getEnd()   -1 ;
                    
                    // hum some people say this if annotation relates to whole seq.
                    if (( start == -1) && ( end == -1 )){
                        //System.out.println(feature);
                        start = 0;
                        end = chainlength - 1;
                        segment.setStart(1);
                        segment.setEnd(chainlength);
                    }
                    
                    col = segment.getColor();
                    g2D.setColor(col);
                    
                    int xstart =  java.lang.Math.round(start * scale) + DEFAULT_X_START;
                    int width   = java.lang.Math.round(  end * scale) - xstart +  DEFAULT_X_START+aminosize ;
                    
                    int height = DEFAULT_Y_HEIGHT ;
                    //logger.finest(feature+ " " + end +" " + width);
                    //logger.finest("color"+entColors[i%entColors.length]);
                    //logger.finest("new feature  ("+i+"): x1:"+ xstart+" y1:"+y+" width:"+width+" height:"+height);
                    String type = feature.getType() ;
                    if (  type.equals("DISULFID")){
                        
                        g2D.fillRect(xstart,y,aminosize,height);
                        g2D.fillRect(xstart,y+(height/2),width,1);
                        g2D.fillRect(xstart+width-aminosize,y,aminosize,height);
                    } else {
                        g2D.fillRect(xstart,y,width,height);
                    }
                }
            }
        }   
        return y ;
    }
    
    
    public void setFeatures( List feats) {
        //logger.entering(this.getClass().getName(), "setFeatures",  new Object[]{" features size: " +feats.size()});
        //logger.finest("DasCanv setFeatures");
        //features = feats ;
        // check if features are overlapping, if yes, add to a new line 
        
        drawLines          = new ArrayList();
        List currentLine   = new ArrayList();
        Feature oldFeature = new FeatureImpl();
        boolean start      = true ;
        String featureSource = "" ;
        clearOldLinkMenus();
        for (int i=0; i< feats.size(); i++) {
            Feature feat = (Feature)feats.get(i);
            //logger.finest(feat.toString());
            // add link to popupmenu
            String link = feat.getLink();
            if ( link != null) {
                registerLinkMenu(link,feat.getMethod() );
            }
            
            
            String ds =feat.getSource();
            // check if new feature source
            if ( ! featureSource.equals(ds) ) {
                //logger.finest("new DAS source " + ds );
                
                if ( ! start) {
                    drawLines.add(currentLine);
                }
                Feature tmpfeat = new FeatureImpl();
                tmpfeat.setSource(ds);
                tmpfeat.setMethod("_SPICE_LINESEPARATOR");		
                currentLine = new ArrayList();
                currentLine.add(tmpfeat);
                drawLines.add(currentLine);
                currentLine = new ArrayList();
                
            }
            featureSource = ds ;
            
            
            // check for type
            //logger.finest(feat.toString());
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
    
    private void registerLinkMenu(String link, String txt){
        
        //  add link to the menu
        //ActionListener ml = new LinkMenuListener(spice,link);
        if ( knownLinks.contains(link))
            return;
        //System.out.println("adding menu:" + link);
        URL u ;
        try {
            u = new URL(link);
        } catch ( MalformedURLException e){
            return;
        }
        ActionListener ml = new SelectionLockMenuListener(spice,u);
        JMenuItem menuItem = new JMenuItem("open in browser "+ txt);
        menuItem.addActionListener(ml);
        popupMenu.add(menuItem);
        knownLinks.add(link);
        
    }
    
    private void clearOldLinkMenus(){
        int nr = popupMenu.getComponentCount();
        for ( int i = nr-1; i >= 0; i--){
            popupMenu.remove(i);
        }
        knownLinks = new ArrayList();
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
    
    
    /** select a single segment 
    private void selectSegment (Segment segment) {
        //logger.finest("select Segment");
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
              
    }
    */
    
    
    /** highlite all segments of a feature */
    
    
    /** select single position
     select a position
     */
    public void select(int seqp){	
        // should be done at spice level
        //if ( spice.isSelectionLocked() ) return;
        highlite(seqp);
        
    }
    
    /** select range */
    public void select(int start, int end) {
        //if ( spice.isSelectionLocked() ) return;
        highlite(start,end);
    }
    
    
    /** same as select here. draw a line at current seqence position
     * only if chain_number is currently being displayed
     */
    public void highlite( int seqpos){
        if  ( chain == null   ) return ;
        //if 	(selectionLocked) return;
        
        if ( seqpos > chain.getLength()) 
            return ;
        selectStart  = seqpos ;
        selectEnd    = 1     ;
        dragging = false;
        //spice.setSelectionLocked(false);
        this.repaint();
        
    }
    
    /** highlite a region */
    public void highlite( int start , int end){
        if  ( chain == null   ) return ;
        
        //if (selectionLocked) return ;
        if ( (start > chain.getLength()) || 
                (end   > chain.getLength())
        ) 	      
            return ;
        
        selectStart = start  ;
        selectEnd   = end    ;
        dragging = true;
        //spice.setSelectionLocked(true);
        this.repaint();
        
    }
    
    
    
    /* check if the mouse is over a feature and if it is 
     * return the feature number 
     * @author andreas
     *
     * To change the template for this generated type comment go to
     * Window - Preferences - Java - Code Generation - Code and Comments
     */
    private int getLineNr(MouseEvent e){
        int mouseY = e.getY();
        
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
    
    /*
    private Feature getFeatureAt(int seqpos, int lineNr){
        //logger.finest("getFeatureAt " + seqpos + " " + lineNr);
        Segment s = getSegmentUnder(seqpos,lineNr);
        if (s == null ) 
            return null;
        
        return s.getParent();
        
    }
    */
    
    /** check if mouse is over a segment and if it is, return the segment */
    private Segment getSegmentUnder( int seqpos,int lineNr){
        //logger.entering(this.getClass().getName(),"getSegmentUnder", new Object[]{new Integer(seqpos),new Integer(lineNr)});
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
                    //logger.exiting(this.getClass().getName(),"getSegmentUnder", new Object[]{segment});
                    return segment ;
                }
            }
        }
        //logger.exiting(this.getClass().getName(),"getSegmentUnder", new Object[]{null});
        return null ;
    }
    
    
    /** create a tooltip string */
    private String getToolString(int seqpos, Segment segment){
        //logger.finest("getToolString");
        // current position is seqpos
        //String toolstr = spice.getToolString(current_chainnumber,seqpos);
        String toolstr = "";
        //logger.finest("getToolString");
        
        Feature parent = segment.getParent() ;
        String method  = parent.getMethod() ;
        String type    = parent.getType() ;
        String note    = parent.getNote() ;
        String score   = parent.getScore() ;
        
        
        int start = segment.getStart();  
        int end   = segment.getEnd(); 
        toolstr +=  " start " + start + " end " + end ;
        
        // DAS features are starting with 1,
        // in SPICE they start with 0
        //start--;
        //end--;
        
        if ( method != null)
            toolstr += " Method: " + method;
        if ( type != null)
            toolstr += " Type: " + type ;
        if ( note != null ) {
            if ( ! note.equals(""))
                toolstr += " Note: " + note;
        }
        if (score != null) {
            if ( ! score.equals("-"))
                toolstr += " Score: " + score;
        }
        //logger.finest(toolstr);
        
        //new ToolTip(toolstr, this);
        //this.repaint();
        
        return toolstr ;
        
    }
    
    /** test if the click was on the name of the feature 
    private boolean nameClicked(MouseEvent e) {
        int x = e.getX();
        if (x <= DEFAULT_X_START) {
            return true ;
        }
        return false ;
    }
    */
    /** get the sequence position of the current mouse event */
    private int getSeqPos(MouseEvent e) {
        
        int x = e.getX();
        //int y = e.getY();
        
        int seqpos =  java.lang.Math.round((x-DEFAULT_X_START)/scale) ;
        
        return seqpos  ;
    }	
    
    public void mouseMoved(MouseEvent e)
    {	
        
        int b = e.getButton();
        logger.finest("mouseMoved button " +b);
        
        // do not change selection if we display the popup window
        if ( popupMenu.isVisible())
            return;
        if (b != 0 )
            if ( b != MouseEvent.BUTTON1 ) 
                return;
            
            
            //, int x, int y
        int seqpos = getSeqPos(e);
        int linenr = getLineNr(e);
        
        if ( seqpos == oldposition)
            return;
        oldposition = seqpos;
        
        //int featurenr = get_featurenr(y); 
        
        if ( linenr < 0 ) return ;
        if ( seqpos < 0 ) return ; 
        
        
        // and the feature display
        
        //Feature feature = getFeatureAt(seqpos,linenr);
        
        Segment segment = getSegmentUnder(seqpos,linenr);
        if ( segment != null) {
            String toolstr = getToolString(seqpos,segment);
            //gfeat.drawString(toolstr,5,13);
            //spice.showStatus(toolstr) ;
            // display ToolTip
            this.setToolTipText(toolstr);
            
            /*
             Feature feat = segment.getParent();
             String link = feat.getLink();
             
             //logger.finest(segment);
              if (link != null ) {
              // check if this feature has a link, if yes,
               // change mouse cursor
                System.out.println("setting cursor to hand cursor");
                addLinkMenu(link);      
                setCursor(handCursor);
                } else {
                System.out.println("setting cursor to default cursor");
                setCursor(defaultCursor);
                removeLinkMenu();
                }
                */
            
        } else {
            this.setToolTipText(null);
            setCursor(defaultCursor);
            //spice.showSeqPos(current_chainnumber,seqpos);
            
        }
        
        //if ( spice.isSelectionLocked()) 
        //    return;
        
        if ( dragging) return;
        //spice.select(current_chainnumber,seqpos);
        return  ;
    }
    
    
    
    
    public void mouseClicked(MouseEvent e)
    {  
        
        //logger.finest("mouseClicked");
        
        //this.setToolTipText(null);
        return  ;
    }	
    
    public void mouseEntered(MouseEvent e)  {}
    public void mouseExited(MouseEvent e)   {}
    
    
    public void mousePressed(MouseEvent e)  {
        int b = e.getButton();
        //logger.finest("mousePressed "+b);
        if ( b == MouseEvent.BUTTON1 )
            mouseDragStart = getSeqPos(e);
        
        dragging = false;
        //spice.setSelectionLocked(false);
        
    }
    
    public void mouseReleased(MouseEvent e) {}
    /*
    public void mouseReleased(MouseEvent e) {
        int b = e.getButton();
        //logger.finest("mouseReleased "+b);       
        if ( b != MouseEvent.BUTTON1) {          
            return;
        }           
        
        if ( popupMenu.isVisible()) return;
        
        int seqpos = getSeqPos(e);
        int lineNr = getLineNr(e);
        //logger.finest("mouseReleased at " + seqpos + " line " + lineNr);
        
        if ( seqpos    > chain.getLength()) return ;
        
        if ( b == MouseEvent.BUTTON1 ) {
            mouseDragStart =  -1 ;
            
            
        }
        if ( dragging ) {
            this.setToolTipText(null);
            return;
        }
        if ( b == MouseEvent.BUTTON1 )
        {
            //System.out.println("checking more");
            if ( lineNr < 0 ) return ;
            if ( seqpos < 0 ) {
                // check if the name was clicked
                if (nameClicked(e)){
                    //System.out.println("nameclicked");
                    // highlight all features in the line
                    //Feature feature = getFeatureAt(seqpos,lineNr);
                    List features = (List) drawLines.get(lineNr);
                    String cmd = "";
                    for ( int i=0; i<features.size(); i++) {
                        Feature feature = (Feature) features.get(i);
                        //cmd += highliteFeature(feature);		    
                    }
                    logger.finest("sending cmd to jmol: " + cmd);
                    //spice.executeCmd(cmd);
                    dragging=true;
                    spice.setSelectionLocked(true);
                    return  ;
                } 
            } else {
                //System.out.print("getSegmentUnder");
                Segment segment = getSegmentUnder(seqpos,lineNr);
                //logger.finest(segment.toString());
                if (segment != null ) {
                    highliteSegment(segment);
                    spice.setSelectionLocked(true);
                    dragging = true;
                    
                    //String drstr = "x "+ seqpos + " y " + lineNr ;
                    String toolstr = getToolString(seqpos,segment);
                    
                    //spice.showStatus(drstr + " " + toolstr) ;
                    this.setToolTipText(toolstr);
                    
                    
                } else {
                    
                    spice.highlite(current_chainnumber,seqpos);
                    this.setToolTipText(null);
                }
            }
        }            
    }
    */
    public void mouseDragged(MouseEvent e) {
        dragging = true;
        
        if ( mouseDragStart < 0 )
            return ;        
        
        //if (spice.isSelectionLocked())
        //  return;
        
        //int b = e.getButton();
        //logger.finest("dragging mouse "+b);
        
        // ARGH my linux java 142_05 does not show Button 1 when being dragged!
        //if ( b != MouseEvent.BUTTON1 ) 
        //  return;
        
        
        // only do with left mouse click
        //int seqpos = getSeqPos(e);
        
        //int selEnd =  seqpos;
        //int start = mouseDragStart ;
        //int end   = selEnd         ;
        //if ( selEnd < mouseDragStart ) {
            //start = selEnd ;
            //end = mouseDragStart ;
        //}
        //spice.highlite(current_chainnumber,start,end);
    }
    
}



