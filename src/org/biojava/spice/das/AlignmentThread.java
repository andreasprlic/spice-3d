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
 * Created on Nov 20, 2005
 *
 */
package org.biojava.spice.DAS;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.ArrayList;
import org.biojava.bio.program.das.dasalignment.Alignment;
import org.biojava.bio.program.das.dasalignment.DASAlignmentCall;
import org.biojava.spice.Config.SpiceDasSource;
import org.biojava.spice.DAS.AlignmentTools;
import org.biojava.spice.manypanel.eventmodel.*;


/** A thread that gets the alignment from a das server
 * 
 * @author Andreas Prlic
 *
 */
public class AlignmentThread 
extends Thread{
    
    SpiceDasSource[] dasSources;
    String query;
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    List alignmentListeners;
    DASAlignmentCall dasalignmentCall;
    String subjectCoordSys;
    String logname;
    String subject;
    
    public AlignmentThread(String logname,String query, SpiceDasSource[] dss) {
        super();
        dasSources = dss;
        if (dss == null){
            dasSources = new SpiceDasSource[0];
        }
        this.query = query;
        clearAlignmentListeners();
    
        dasalignmentCall= new DASAlignmentCall();
        subjectCoordSys= null;
        subject=null;
        this.logname = logname;
    }
    
    public AlignmentThread(String logname,String query, String subject, SpiceDasSource[] dss) {
        this(logname,query,dss);
    }
    
    public AlignmentThread(String logname,String query, SpiceDasSource[] dss, String subjectCoordSys) {
        this(logname,query,dss);
     
        this.subjectCoordSys = subjectCoordSys;
        
    }
    
    public AlignmentThread(String logname,String query,String subject, SpiceDasSource[] dss, String subjectCoordSys) {
        this(logname,query,dss,subjectCoordSys);
        
        this.subject = subject;
        
        
    }
    
    
    public void clearAlignmentListeners(){
        alignmentListeners = new ArrayList();
    }
    
    public void addAlignmentListener(AlignmentListener ali){
        alignmentListeners.add(ali);
    }
    
    public void run() {
        Alignment[] aligs = getAlignments(query);
        if ( aligs.length == 0)
            return;
        
        Alignment finalAlig = aligs[0];
        
        // take the right alignment
        if ( subject != null){
            for ( int i=0; i< aligs.length;i++ ){
                Alignment a = aligs[i];
                try {
                    AlignmentTools.getObject(subject,a);
                    finalAlig = a;
                    break;
                } catch (NoSuchElementException e){
                    continue;
                }
            }
        }
        
        Iterator iter = alignmentListeners.iterator();
        while (iter.hasNext()){
            AlignmentListener li = (AlignmentListener ) iter.next();
            li.newAlignment (new AlignmentEvent(finalAlig));
        }
        
    }
    
    /** get alignments for a particular uniprot or pdb code */
    public  Alignment[] getAlignments(String code) {
        logger.finest(logname + "searching for alignments of "+code+" ");
        Alignment[] alignments = new Alignment[0] ;
        
        //List aligservers = config.getServers("alignment");
        logger.finest(logname + "found " + dasSources.length + " alignment servers");
        
        String  dasalignmentcommand = null  ;
        
        // loop over all available alignment servers 
        for ( int i =0 ; i < dasSources.length ; i++ ) {
            SpiceDasSource sds= dasSources[i];
            
            //logger.finest(logname + " investigating " + i + " url" + sds.getUrl());
            //System.out.println("investigating" + sds.getUrl());
            // only consider those serving uniprot and PDB alignments
            
            
            
            String url = sds.getUrl() ;
            char lastChar = url.charAt(url.length()-1);      
            if ( ! (lastChar == '/') ) 
                url +="/" ;
            dasalignmentcommand  = url +  "alignment?query="+code ;
            
            if ( subject != null){
                dasalignmentcommand += "&subject="+subject;
            }
            
            if (subjectCoordSys != null ){
                dasalignmentcommand +="&subjectcoordsys="+subjectCoordSys;
            }
            logger.info(logname + " contacing alignment server " + dasalignmentcommand);
            //System.out.println("contacing alignment server " + dasalignmentcommand);
            
            try{
                //alignments = dasc.getAlignments(code);
                alignments= retreiveAlignments(dasalignmentcommand);
                
                logger.finest(logname + " DASAlignmentHandler: got "+ alignments.length +" alignment(s):");
                if ( alignments.length == 0 ) {
                    // check next alignment server ...
                    continue ;
                }
                return alignments ;
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
        
        
        
        logger.log(Level.SEVERE,logname +" no  alignment found!");
        
        
        
        return new Alignment[0] ;
    }
    
    private Alignment[] retreiveAlignments(String url)
    throws IOException
    {
        /* now connect to DAS server */
        
        URL dasUrl = null ;
        try {
            dasUrl = new URL(url);
        } catch (Exception e) {
            throw new IOException("error during creation of URL " + e.getMessage());
        }
        
        InputStream inStream = connectDASServer(dasUrl);
        
        
        Alignment[] ali = null;
        try{
            ali =  dasalignmentCall.parseDASResponse(inStream) ;
        } catch (Exception e) {
            throw new IOException("error during creation of URL " + e.getMessage());
        }
        return ali;
        
    }
    
    /** connect to DAS server and return result as an InputStream.
    *
    */    
   private InputStream connectDASServer(URL url) 
    throws IOException
   {
    InputStream inStream = null ;
                
    //System.out.println("opening connection to "+url);
    HttpURLConnection huc = org.biojava.spice.SpiceApplication.openHttpURLConnection(url);  
     

    //System.out.println("temporarily disabled: accepting gzip encoding ");
    // should make communication much faster!
    huc.setRequestProperty("Accept-Encoding", "gzip");
    
    //System.out.println("response code " +huc.getResponseCode());
    String contentEncoding = huc.getContentEncoding();
    //System.out.println("getting InputStream");
    inStream = huc.getInputStream();
    if (contentEncoding != null) {
        if (contentEncoding.indexOf("gzip") != -1) {
        // we have gzip encoding
        inStream = new GZIPInputStream(inStream);
        //System.out.println("using gzip encoding!");
        }
    }
    //System.out.println("got InputStream from  DAS Alignment server");
    //System.out.println("encoding: " + contentEncoding);

    return inStream;
    
   }
   
    
}
