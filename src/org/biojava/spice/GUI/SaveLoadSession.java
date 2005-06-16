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
 * Created on Mar 9, 2005
 *
 */
package org.biojava.spice.GUI;

import org.biojava.spice.*;
import org.biojava.spice.Feature.*;
import org.biojava.spice.Feature.Segment;
import org.biojava.spice.DAS.StructureXMLStAXAdaptor;
import org.biojava.utils.stax.DelegationManager;
import org.biojava.utils.stax.StAXContentHandler;
import org.biojava.utils.stax.StAXContentHandlerBase;
import org.biojava.utils.stax.SAX2StAXAdaptor;
import org.biojava.utils.xml.PrettyXMLWriter;
import org.biojava.utils.xml.XMLWriter;
import org.biojava.bio.structure.io.FileConvert;
import org.biojava.bio.structure.Structure;

import java.awt.Color;


import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import org.xml.sax.*;


/**
 * @author Andreas Prlic
 *
 */
public class SaveLoadSession {

    	SPICEFrame spice;
    	File  oldfile;
    	Logger logger; 
    /**
     * 
     */
    public SaveLoadSession(SPICEFrame parent) {
        super();
        logger = Logger.getLogger("org.biojava.spice");
        spice = parent;
        oldfile = null;
        // TODO Auto-generated constructor stub
    }
    
    /** save current SPICE session */
    public void save() {
        System.out.println("save");
        File f = null;
        if ( oldfile == null ){
            try {	
                f = new File("session.spice");
            } catch ( Exception e){ }
        }
        else 
            f = oldfile ;
        
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(f);
        
        chooser.setDialogTitle("select file to save current session");
        //      In response to a button click:
        int returnVal = chooser.showSaveDialog(null);
        if ( returnVal == JFileChooser.APPROVE_OPTION) {
             File file = chooser.getSelectedFile();
             oldfile = f;
             if ( file.exists() ){
                 //open ConfirmDialog
                 
                 int i =  JOptionPane.showConfirmDialog(null,
                         "A file with name " + file.getAbsolutePath() + " already exists.\n" +
                         " are you sure you want to overwrite it?");
                 if ( i != JOptionPane.OK_OPTION) {
                     logger.info("saving canceled according to user request.");
          
                     return;
                 }
             }
             
             //System.out.println("you choose file " + file);
             //spice.saveSession(file);
             try {
                 toXML(file);
             } catch (Exception e){
                 e.printStackTrace();
                 logger.warning("Could not save file. "+e.getMessage());
             }
             logger.info("saved session to "+f.getAbsolutePath());
        
        }
        
    }
    
    /**  load a previously saved session into SPICE */
    public void load(){
        System.out.println("load");
        File f = null;
        if ( oldfile == null ){
            try {	
                f = new File("session.spice");
            } catch ( Exception e){ }
        }
        else 
            f = oldfile ;
        
        JFileChooser chooser = new JFileChooser();
        //FileFilter filter = new FileFilter();
        //filter.addExtension("spice");
        //filter.setDescription("SPICE session files");
        //chooser.setFileFilter(filter);
        chooser.setSelectedFile(f);
        chooser.setDialogTitle("select file to restore previously saved session from");
                
        //      In response to a button click:
        
        int returnVal = chooser.showOpenDialog(null);
        System.out.println("here");
        if ( returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("pressed o.k.");
            
            File file = chooser.getSelectedFile();
            logger.info("you requested to load file "+ file.getAbsolutePath());
            oldfile = file;
            spice.setLoading(true);
            try  {
                fromXML(file);
            } catch (Exception e){
                e.printStackTrace();
                logger.warning("Could not load file. "+e.getMessage());
                spice.setLoading(false);
                return;
            }
            spice.setLoading(false);
            logger.info("restored session from " + file.getAbsolutePath());
        }
    }
    
    /** parse the serialized session.spice file */
    public void fromXML(File f)
    	throws FileNotFoundException, SAXException, IOException, ParserConfigurationException
    {
        System.out.println("init XML file parse");
        // get stream from file
       
        
        SAXParserFactory spfactory =
		    SAXParserFactory.newInstance();
		
		spfactory.setValidating(false);
		
		SAXParser saxParser = null ;
		
		saxParser = spfactory.newSAXParser();
		
		XMLReader xmlreader = saxParser.getXMLReader();
		MyParser mp = new MyParser(spice);

		SAX2StAXAdaptor cont_handle = new SAX2StAXAdaptor(mp);
		
		xmlreader.setContentHandler(cont_handle);
		xmlreader.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
		InputSource insource = new InputSource() ;
		
		//open the file
		InputStream is = new FileInputStream(f);
		insource.setByteStream(is);	
		
		xmlreader.parse(insource);			
		
    }
    
    
    
    public void toXML(File f) 
    	throws FileNotFoundException, IOException
    	{
        OutputStream os = new FileOutputStream(f); 
        PrintWriter pw = new PrintWriter(os,true);
        XMLWriter  xw = new PrettyXMLWriter( pw);
        xw.printRaw("<?xml version='1.0' standalone='no' ?>");
    	
    		xw.openTag("SpiceSession");
    		
    		xw.openTag("date");
    		Date now = new Date();
    		DateFormat df = new SimpleDateFormat("yyy.MM.dd-HH.mm.ss");
    		xw.print(df.format(now));
    		xw.closeTag("date");
    		
    		xw.openTag( "PDBCode");
    		xw.print(spice.getPDBCode());
    		xw.closeTag("PDBCode");
    		
    		xw.openTag("UniProtCode");
    		xw.print(spice.getUniProtCode());
    		xw.closeTag("UniProtCode");
    		
    		
    		xw.openTag("CurrentChain");
    		xw.print(spice.getCurrentChainNumber()+"");
    		xw.closeTag("CurrentChain");
    		
    		xw.openTag("Features");
    		List features = spice.getFeatures();
    		Iterator iter =features.iterator();
    		while ( iter.hasNext()){
    		    Feature feat = (Feature) iter.next();
    		    feature2XML(feat,xw);
    		}
    		xw.closeTag("Features");
    		
//    		 the structure is serialized using biojava
    		xw.openTag("DASStructure");
    		Structure structure = spice.getStructure();
    		List connections = structure.getConnections();
    		// hm this check should not be necessary!
    		// bug somewhere when retreiving the DAS structure?
    		if ( connections == null )
    		    structure.setConnections(new ArrayList());
    		FileConvert fc = new FileConvert(structure);
    		fc.toDASStructure(xw);
    		xw.closeTag("DASStructure");
    		
    		xw.closeTag("SpiceSession");
    }
    
    public void feature2XML(Feature f, XMLWriter xw) 
    throws IOException {
        // iterate over sub-segments
        
        xw.openTag("feature");
        
        String type = f.getType();
        if ( type == null) type ="";
        
        String source = f.getSource();
        if ( source == null) source ="";
        
        String note = f.getNote();
        if (  note == null) note = "";
        
        String link = f.getLink();
        if ( link == null ) link = "";
        
        String method = f.getMethod();
        if ( method == null) method ="";
        
        String name = f.getName();
        if (name == null) name = "";
        
        String score = f.getScore();
        if (score == null) score = "";
        
        xw.attribute("name",name);
        xw.attribute("type",type);
        xw.attribute("source",source);
        xw.attribute("method", method);
        xw.attribute("note", note);
        xw.attribute("link",link);
        xw.attribute("score",score);
        
        List segments = f.getSegments();
        Iterator iter = segments.iterator();
        xw.openTag("segments");
        while (iter.hasNext()){
            Segment s = (Segment) iter.next();
            xw.openTag("segment");
            xw.attribute("start",""+s.getStart());
            xw.attribute("end", ""+s.getEnd());
            xw.attribute("name",s.getName());
            Color c = s.getColor();
            xw.attribute("colorR",c.getRed()+"");
            xw.attribute("colorG",c.getGreen()+"");
            xw.attribute("colorB",c.getBlue()+"");
            xw.attribute("txtcolor",s.getTxtColor());
            xw.closeTag("segment");
        }
        xw.closeTag("segments");
        xw.closeTag("feature");
    }
    

}

class MyParser
	extends StAXContentHandlerBase{
    
    SPICEFrame spice;
    StructureXMLStAXAdaptor sxs;
    String date;
    String uniProtCode;
    String pdbCode;
    String characterdata;
    int currentChain;
    List features;
    Feature currentFeature;
    Logger logger;
    
    public MyParser(SPICEFrame parent){
        spice = parent;
		sxs = new StructureXMLStAXAdaptor() ;
		characterdata = "";
		//System.out.println("init MyParser");
		features  = new ArrayList();
		currentFeature = new FeatureImpl();
		currentChain=0;
		logger= Logger.getLogger("org.biojava.spice");
		uniProtCode = "";
		pdbCode ="";
    }
    
    public void startTree() throws SAXException {
        //System.out.println("startTree");
    }
    public void endTree()
    	throws SAXException
    {
        // structure needs to be set first, before features,
        // otherwise SPICE does not know where the features belong to...
        
        System.out.println("getting structure");
        Structure s = sxs.getStructure();
        spice.setStructure(s);
        
        // spice.setStructure set loading to false;
        spice.setLoading(true);
        
        // then setting features, because once we select the chain,
        // spice checks if they are already in memory, otherwise it tries 
        // to get them via DAS and we do not want this during session-restore.
        System.out.println("setting features "+ uniProtCode);
        System.out.println(features.size());
        spice.setFeatures(uniProtCode,features);

        	// finally setting the currently active Chain in spice
        // features have to be already  set ( see above)
        System.out.println(currentChain);
        spice.setCurrentChainNumber(currentChain);
        System.out.println(currentChain);
        spice.setCurrentChainNumber(currentChain);
        
        logger.info("restored session from "+date);
       
    }
    public void startElement(String nsURI,
            String localName,
            String qName,
            Attributes attrs,
            DelegationManager dm)
    throws SAXException
    {
        characterdata = "";
        //System.out.println("startElement nsURI: " + nsURI + " localName: "+ localName+" qName "+qName);
        if (qName.equals("DASStructure")){
            //delegate(sxs);
            dm.delegate(sxs);
        } else if (qName.equals("feature")){
            createNewFeature(attrs);
        } else if (qName.equals("segment")){
            addSegment(attrs);
        }else {
           // System.out.println("startElement nsURI: " + nsURI + " localName: "+ localName+" qName "+qName);
        }
    }
    
    
    private void createNewFeature(Attributes attrs){
        currentFeature = new FeatureImpl();
        currentFeature.setName(attrs.getValue("name"));
        currentFeature.setType(attrs.getValue("type"));
        currentFeature.setSource(attrs.getValue("source"));
        currentFeature.setMethod(attrs.getValue("method"));
        currentFeature.setLink(attrs.getValue("link"));
        currentFeature.setNote(attrs.getValue("note"));
        currentFeature.setScore(attrs.getValue("score"));
        
    }
    private void addSegment(Attributes attrs){
        Segment s = new Segment();
        s.setParent(currentFeature);
        s.setStart(Integer.parseInt(attrs.getValue("start")));
        s.setEnd(Integer.parseInt(attrs.getValue("end")));
        s.setName(attrs.getValue("name"));
        s.setTxtColor(attrs.getValue("txtcolor"));
        int r = Integer.parseInt(attrs.getValue("colorR"));
        int g = Integer.parseInt(attrs.getValue("colorG"));
        int b = Integer.parseInt(attrs.getValue("colorB"));
        s.setColor(new Color(r,g,b));
        currentFeature.addSegment(s);
    }
    
    
    public void endElement(String nsURI,
            String localName,
            String qName,
            StAXContentHandler delegate)
    throws SAXException
    {
        //System.out.println("edElement " + nsURI);
        if ( qName.equals("DASStructure")){
            //
            
        } else if ( qName.equals("date")){
            date = characterdata;           
        } else if (qName.equals("UniProtCode")){
            uniProtCode = characterdata;
        } else if (qName.equals("PDBCode")){
            pdbCode = characterdata;
        } else if (qName.equals("CurrentChain")){
            //System.out.println("processing chaindata >" + characterdata+"<");
            currentChain = Integer.parseInt(characterdata);
        } else if (qName.equals("feature")){
            features.add(currentFeature);
        } else if (qName.equals("Features")){
          //
        }
        characterdata = "";
    }
    
    public void characters(char[] ch,
            int start,
            int length)
    throws SAXException
    {
        //System.out.println("characters");
        for (int i = start; i < start + length; i++) {
    		
    				characterdata += ch[i];
    			}
    }
}
