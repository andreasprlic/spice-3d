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
 * Created on Jun 16, 2005
 *
 */
package org.biojava.spice.Panel;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure ;
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.Segment;
import org.biojava.spice.Panel.seqfeat.FeatureEvent;
import org.biojava.spice.Panel.seqfeat.FeatureViewListener;
import org.biojava.spice.Panel.seqfeat.SelectedSeqPositionListener;
import java.util.Map;
import java.awt.Color;

/**
 * @author Andreas Prlic
 *
 */
public class StructurePanelListener
implements FeatureViewListener,
SelectedSeqPositionListener

{
//1l1y
    static String INIT_SELECT = "select all; cpk off ; wireframe off ; backbone off; "
        +"cartoon on; colour chain;select not protein and not solvent;spacefill 2.0;";
    
    StructurePanel structurePanel ;
    int currentChainNumber ;
    Structure structure;
    static Logger    logger      = Logger.getLogger("org.biojava.spice");
    int oldpos ;
    boolean selectionIsLocked ;
    /**
     * 
     */
    public StructurePanelListener(StructurePanel structurePanel) {
        super();
        this.structurePanel = structurePanel;
        currentChainNumber = 0;
        int oldpos = -1;
        selectionIsLocked =false;
    }
    
    /** display a new PDB structure in Jmol 
    * @param structure a Biojava structure object
    * @param displayScript  a flag to set if the INIT_SELECT script should be executed or not    
    *
    */
   public  void setStructure(Structure structure, boolean displayScript) {
       if ( structure == null ) {
           //executeCmd(EMPTYCMD);
           return;
       }
       this.structure=structure;
       structurePanel.setStructure(structure);
       if ( displayScript)
       executeCmd(INIT_SELECT);
   
      
   }
   
   /** display a new PDB structure in Jmol 
    * @param structure a Biojava structure object    
    *
    */
   public  void setStructure(Structure structure) {
       boolean displayScript = true;
       setStructure(structure, displayScript );
      
   }
   
    public void setCurrentChainNumber(int i){
        currentChainNumber = i;
    }
    

    /** reset the Jmol panel */
    public void resetDisplay(){
        String cmd = INIT_SELECT;
        structurePanel.executeCmd(cmd);
        
    }
    
    /** send a RASMOL like command to Jmol
     * @param command - a String containing a RASMOL like command. e.g. "select protein; cartoon on;"
     */
    public void executeCmd(String command) {
        //System.out.println("StructurePanel: " + command);
        structurePanel.executeCmd(command);
    }
    
    /** get Chain number X from structure 
     * @return a Chain object or null ;
     */
    public Chain getChain(int chainnumber) {
        
      
        
        if ( structure == null ) {
            //logger.log(Level.WARNING,"no structure loaded, yet");
            return null ;
        }
        
        if ( structure.size() < 1 ) {
            logger.log(Level.WARNING,"structure object is empty, please load new structure");
            return null ;
        }
        
        if ( chainnumber > structure.size()) {
            logger.log(Level.WARNING,"requested chain number "+chainnumber+" but structure has size " + structure.size());
            return null ;
        }
        
        Chain c = structure.getChain(chainnumber);
        return c;
    }
    

    /** return the pdbcode + chainid to select a single residue. This
     * can be used to create longer select statements for individual
     * amino acids.
     * seqpos is in UniProt coordinate system.
     *  */
    
    private String getSelectStrSingle(int chain_number, int seqpos) {
        Chain chain = getChain(chain_number);
        if ( chain == null) return "" ;
        
        if ( ! ((seqpos >= 0) && (seqpos < chain.getLength()))) {
            logger.finest("seqpos " + seqpos + "chainlength:" + chain.getLength());
            return "" ;
        }
        
        //SeqFeatureCanvas dascanv = daspanel.getCanv();
        //if ( chain_number == currentChain )
        //  dascanv.highlite(seqpos);
        
        Group g = chain.getGroup(seqpos);
        if (! g.has3D()){
            return "" ;
        }
        
        String pdbcod = g.getPDBCode() ;
        
        if ( hasInsertionCode(pdbcod) ) {
            String inscode = pdbcod.substring(pdbcod.length()-1,pdbcod.length());
            String rawcode = pdbcod.substring(0,pdbcod.length()-1);
            pdbcod = rawcode +"^" + inscode;
        }
        
        
        //String pdbname = g.getPDBName() ;
        String chainid = chain.getName() ;
        //logger.finest("selected "+pdbcod+" " +pdbname);
        String cmd =  pdbcod+chainid ;
        return cmd ;
    }

    private Group getGroupNext(int chain_number,int startpos, String direction) {
        Chain chain = getChain(chain_number) ;
        if ( chain == null) return null ;
        
        while ( (startpos >= 0 ) && (startpos < chain.getLength())){
            Group g = chain.getGroup(startpos);	
            if (g.has3D()){
                return g ;
            }
            if ( direction.equals("incr") ) {
                startpos += 1;
            } else {
                startpos -= 1 ;
            }
        }
        return null ;
    }
    /** return a select command that can be send to executeCmd
     * coordinates are uniprot position
     * */
    private String getSelectStr(int chain_number, int start, int end) {
        Chain chain = getChain(chain_number) ;
        if ( chain == null) return "" ;
        String chainid = chain.getName() ;
        
        Group gs = getGroupNext( chain_number,(start-1),"incr");
        //Group gs = chain.getGroup(start-1);	
        Group ge = getGroupNext( chain_number,(end-1),"decr");
        //= chain.getGroup(end-1);	
        //logger.finest("gs: "+gs+" ge: "+ge);
        if (( gs == null) && (ge == null) ) {
            return "" ;
        }
        
        if (gs == null) {
            return getSelectStr( chain_number, end-1) ;
        }
        
        if (ge == null) {
            return getSelectStr( chain_number, start-1) ;
        }
        
        String startpdb = gs.getPDBCode() ;
        String endpdb = ge.getPDBCode() ;
        String cmd =  "select "+startpdb+"-"+endpdb;
        if ( ! chainid.equals(" ")) 
            cmd += ":" +chainid;
        cmd +=";";
        return cmd ;
    }
    
        

    /** test if pdbserial has an insertion code */
    private boolean hasInsertionCode(String pdbserial) {
        try {
            int pos = Integer.parseInt(pdbserial) ;
        } catch (NumberFormatException e) {
            return true ;
        }
        return false ;
    }
    
    public void selectedSeqRange(int start, int end) {
        //System.out.println("selected " + start + " " + end);
        //highlite(currentChainNumber,start,end,"");
        String cmd = getSelectStr(currentChainNumber,start,end);
        cmd += " set display selected;";
        if ( ! cmd.equals(""))
            executeCmd(cmd);
    }
    
    public void selectionLocked(boolean flag){
        selectionIsLocked = flag;
    }
    
    public void selectedSeqPosition(int seqpos){
        
        //highlite(currentChainNumber,seqpos,"");
        if ( seqpos == oldpos ) return ;
        oldpos = seqpos ; 
        //System.out.println("structurepanel selected seqpos " + seqpos );
        String cmd = getSelectStr(currentChainNumber,seqpos);
        
        if ( ! cmd.equals(""))
            executeCmd(cmd);
        else {
            cmd = "select null ; set display selected" ;
            executeCmd(cmd);	
        }
    }
    public void mouseOverFeature(FeatureEvent e){
        /*
        Feature feat = (Feature) e.getSource();
        //System.out.println("StructurePanel mouse over feature " + feat);
        
        highliteFeature(feat,false);
        */
    }
    
    public void mouseOverSegment(FeatureEvent e){
        /*
        Segment s = (Segment)e.getSource();
        //System.out.println("StructurePanel mouseOverSegment " + s);
        //highliteSegment(seg);
        int start =s.getStart()-1;
        int end = s.getEnd()-1;
        String cmd = "";
        if (  s.getName().equals("DISULFID")) {
           
            cmd += "select "+ getDisulfidSelect(start,end) + " ;";
            
            
        }
        else {
            
            cmd = getSelectStr(currentChainNumber,start,end);
            
        } 
        cmd += " set display selected;  ";
        executeCmd(cmd);
        */
    }
    public void featureSelected(FeatureEvent e){
        
        Feature feat = (Feature) e.getSource();
        //System.out.println("StructurePanel selected feature " + feat);
        Map[] stylesheet = e.getDasSource().get3DStylesheet();
        highliteFeature(feat,stylesheet,true);
    }
    public void segmentSelected(FeatureEvent e){
        Segment seg = (Segment)e.getSource();
        //System.out.println("StructurePanel: selected segment " + seg);
        highliteSegment(seg);
    }
    
    
    /** return a select command that can be send to executeCmd*/
    private String getSelectStr(int chain_number,int seqpos) {
        
        String pdbdat = getSelectStrSingle(chain_number, seqpos);
        
        if (pdbdat.equals("")){
            return "" ;
        }
        
        String cmd = "select " + pdbdat + ";";
        return cmd ;
        
    }
    
    private void highliteFeature(Feature feature, Map[] stylesheet, boolean color ){
        logger.finest("highlite feature " + feature.getName());
        //Feature feature = (Feature) features.get(featurenr) ;
        //logger.finest("highlite feature " + feature);
        
        
        List segments = feature.getSegments() ;
        String cmd = "" ;
  
        Chain chain = getChain(currentChainNumber);
        String chainId = chain.getName();
        String chainselect = ":"+chainId;
        if ( chainId.equals(" ") || chainId.equals("")){
            chainselect = "";
        }
        
        
        //boolean first = true;
        for ( int i =0; i< segments.size() ; i++ ) {
            
            Segment segment = (Segment) segments.get(i);
            //highliteSegment(segment);
            
            int start = segment.getStart()-1;
            int end   = segment.getEnd()-1;
            //logger.finest("highilte feature " +featurenr+" " + start + " " +end );
            
            if ( feature.getType().equals("DISULFID")){
            
                String c = getDisulfidSelect(start,end);
                cmd += "select " + c;
                
            } else {
                
                Group gs = getGroupNext( currentChainNumber,(start),"incr");
                //Group gs = chain.getGroup(start-1);	
                Group ge = getGroupNext( currentChainNumber,(end),"decr");
                //= chain.getGroup(end-1);	
                //logger.finest("gs: "+gs+" ge: "+ge);
                if (( gs == null) || (ge == null) ) {
                    continue;
                }
                
                String startpdb = gs.getPDBCode() ;
                String endpdb = ge.getPDBCode() ;
                
                String c = "" ;

           
                c += startpdb + " - " + endpdb + chainselect ; 
                cmd += "select "+  c;
            } 
            cmd +=";";
            
           
            
            // check for stylesheet, otherwise use default ...
            //feature.getSource();
            
            String type = feature.getType();
            boolean cmdSet = false;
            String[] displayTypes = { "cartoon","wireframe","spacefill","backbone","ribbons"};
            if (( stylesheet != null ) &( stylesheet.length>0)){
                for (int m=0; m< stylesheet.length;m++){
                    Map s = stylesheet[m];
                    //logger.finest(" style:" + s);
                    String styleType = (String) s.get("type");
                    if ( styleType.equals(type)){
                        logger.finest("coloring 3D stylesheet");
                        cmdSet = true;
                        String display = (String)s.get("display");
                        for ( int d = 0 ; d< displayTypes.length; d++){
                            String cType = displayTypes[d];
                            cmd += " " + cType;
                            if ( cType.equals(display)){
                                String width = (String)s.get("width");
                                
                                if ( width != null){
                                     cmd += " "+width+";";
                                } else 
                                    cmd += " on;";
                            } else  {
                                cmd += " off;";
                            }
                        }
                        
                        String colorStyle = (String)s.get("cpkcolor");
                        
                        if ((colorStyle!= null ) &&(  colorStyle.equals("true")))
                            cmd += "colour cpk;";
                        else 
                            if ( color){
                                Color col = (Color)s.get("color");
                                if ( col != null )
                                    cmd += " color [" +col.getRed()+","+col.getGreen() +","+col.getBlue() +"];";
                                else {
                                    col = segment.getColor();
                                    cmd += " color [" +col.getRed()+","+col.getGreen()+","+col.getBlue() +"];";
                                }
                            }
                            
                            
                      
                    }
                }
                
            }
            if ( ! cmdSet ) {
                if ( color){
                    Color col = segment.getColor();
                    cmd += " color [" +col.getRed()+","+col.getGreen()+","+col.getBlue() +"];";
                    //cmd += " color " + segment.getTxtColor() +";";
                }
                
                logger.finest("no 3D stylesheet found");
                if ( ( feature.getType().equals("METAL")) ||
                        ( feature.getType().equals("SITE"))  ||
                        ( feature.getType().equals("ACT_SITE")) 	     
                ){
                    if ( color)
                        cmd += " spacefill on; " ;
                } else if ( feature.getType().equals("MSD_SITE")|| 
                        feature.getType().equals("snp") 
                ) {
                    if ( color)
                        cmd += " wireframe on; " ;
                } else if ( feature.getType().equals("DISULFID")){
                    if ( color)
                        cmd += "colour cpk; spacefill on;";
                }
            }
            
        }
     
        
        // and now select everything ...
        boolean first = true;
        for ( int i =0; i< segments.size() ; i++ ) {
            //
            Segment segment = (Segment) segments.get(i);
            //highliteSegment(segment);
            
            int start = segment.getStart() -1;
            int end   = segment.getEnd() -1;
            
            //logger.finest("highilte feature " +featurenr+" " + start + " " +end );
            
            if ( feature.getType().equals("DISULFID")){
            
                String c = getDisulfidSelect(start,end);
                cmd +="select " + c;

                
            } else {
                
                Group gs = getGroupNext( currentChainNumber,(start),"incr");
                //Group gs = chain.getGroup(start-1);	
                Group ge = getGroupNext( currentChainNumber,(end),"decr");
                //= chain.getGroup(end-1);	
                //logger.finest("gs: "+gs+" ge: "+ge);
                if (( gs == null) || (ge == null) ) {
                    continue;
                }
                
                String startpdb = gs.getPDBCode() ;
                String endpdb = ge.getPDBCode() ;
                
                String c = "" ;
                if ( first ){
                    c += "select ";
                } else{
                    c += ", ";
                }
                first = false;
                c += startpdb + " - " + endpdb +chainselect; 
                cmd +=  c;
            } 
        }
        
        //logger.finest("cmd: "+cmd);
        cmd += "; set display selected;";
        logger.info(cmd);
        executeCmd(cmd);
        
        
    }
    
    public void highlite(int chainNumber, int seqpos, String colour) {
        //logger.finest("highlite " + seqpos);
        
        if ( seqpos     < 0 ) return ;
        if (chainNumber < 0 ) return ;
        //if ( selectionLocked ) return ;
        
        
        String cmd = getSelectStr( chainNumber,  seqpos);
        if ( ! cmd.equals("") ){
            cmd +=  " spacefill on ;" ;
            structurePanel.executeCmd(cmd);
        }
        //structurePanel.forceRepaint();
        
        if ( colour  != "") {
            colour(chainNumber,seqpos,colour) ;
        }
    }
    
    public void colour(int chainNumber, int seqpos, String colour) {
       		
        if ( seqpos    < 0 ) return ;
        if (chainNumber < 0 ) return ;
        String cmd = getSelectStr( chainNumber,  seqpos);
        if (! cmd.equals("")){
            cmd += "colour "+ colour+";";
            structurePanel.executeCmd(cmd);
        }
        //structurePanel.forceRepaint();
        
    }
    
    
    public void colour(int chainNumber, int start, int end, String colour) {
        		
        if ( start    < 0 ) return ;
        if (chainNumber < 0 ) return ;
        
        String cmd = getSelectStr( chainNumber,  start,  end);
        if ( ! cmd.equals("")){
            cmd += "colour "+ colour+"; set display selected;";
            structurePanel.executeCmd(cmd);
        }
        //cmd += "set display selected;";
    }
    
    public void highlite(int chainNumber, int start, int end, String colour){
        //logger.finest("highlite start end" + start + " " + end );
        //if ( first_load)       return ;		
      
        if ( start       < 0 ) return ;
        if ( chainNumber < 0 ) return ;
        //if ( selectionLocked ) return ;
        
        // highlite structure
        String cmd = getSelectStr( chainNumber,  start,  end);
        //cmd +=  " spacefill on; " ;
        if (! cmd.equals("")){
            if ( colour  != "") {
                cmd += "colour " +colour ;
                colour(chainNumber,start,end,colour) ;
            }
        }
        
        structurePanel.executeCmd(cmd);
        //structurePanel.forceRepaint();
        
    }
    
    /** select the two residues of a disulfid bridge
     * 
     * @param c1 position of C in uniprot
     * @param c2 position of C in uniprot
     * @return rasmol - like select command
     */
    private String getDisulfidSelect(int c1,int c2){
        String cmd = "";
        
        // a disulfid bridge...
        String cs1 = getSelectStrSingle(currentChainNumber,c1);
       if (! cs1.equals("")) {
           cmd += cs1 ;
       }
       String cs2 = getSelectStrSingle(currentChainNumber,c2);
       if ( ! cs2.equals("")){
	   if ( ! cs1.equals("") )
	       cmd +=", ";
           cmd += cs2;
           
       }
       //cmd += ";";
       //System.out.println("disulfid command " + cmd);
       return cmd ;
    }
    
    /** highlite a single segment */
    private void highliteSegment (Segment segment) {
        logger.finest("highlite Segment");
        //logger.finest("segment");
        
        // clicked on a segment!
        String col =  segment.getTxtColor();
        //seqColorOld = col ;
        //spice.setOldColor(col) ;
        
        // we assume that DAS features start with 1
        // internal in SPICE they start with 0
        
        int start = segment.getStart() -1 ;
        int end   = segment.getEnd()   -1 ;
        
        String type =  segment.getParent().getType() ;
        //logger.finest(start+" " + end+" "+type);
        //logger.finest(segment);
        
        
        
        
        if ( type.equals("DISULFID")){
            //highlite(currentChainNumber,start);
            //highlite(currentChainNumber,end);
            String cmd = "select " + getDisulfidSelect(start,end) + "; spacefill on; colour cpk;" ;
            executeCmd(cmd);
            
            
        } else if (type.equals("METAL") ){
            highlite(currentChainNumber,start+1,end+1  ,"cpk");
            
        } else if ( type.equals("MSD_SITE") || 
                type.equals("snp")      		  
        ) {
            highlite(currentChainNumber,start+1,end+1  ,"wireframe");	    
        } else if ( (end - start) == 0 ) {
            // feature of size 1
            highlite(currentChainNumber,start+1,start+1  ,"cpk");	     
        } else {
            colour(currentChainNumber,start+1  ,end+1  ,col);	    
        }
        
    }
}
