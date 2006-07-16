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
 * Created on Aug 1, 2005
 *
 */
package org.biojava.spice.gui.msdkeyword;

import java.util.*;

/** thanks to Adel Golovin for providing most of this code.
 * A class that contains the data returned by
 * e.g. http://www.ebi.ac.uk/msd-srv/msdsite/entryQueryXML?act=getall&searchOptions=%26keyword=histone";
 * 
 * @author Andreas Prlic, Adel Golovin
 *
 */
public class Deposition {
    protected String id;
    protected String accessionCode;
    protected Date   lastModified;
    protected float  resolution;
    protected float  rfactor;
    protected String classification;
    protected String expData;
    protected String heteroList;
    protected String title;
    
    public Deposition() {}
    public Deposition(String id,  
		      String accessionCode, 
		      float resolution, 
		      float rfactor, 
		      Date lastModified, 
		      String classification,
		      String title,
		      String expData,
		      String heteroList) {
	this.id = id; 
	this.accessionCode = accessionCode;
	this.resolution = resolution;
	this.rfactor = rfactor;
	this.lastModified = lastModified;
	this.classification = classification;
	this.expData = expData;
	this.heteroList = heteroList;
	this.title = title;
    }
    
    public String toString() {
        String txt = accessionCode + " " + expData + " " + resolution + " " + title + " " +classification;
        return txt;
    }
    public void setId(String id) { this.id = id; }
    public String getId() { return id; }
    
    public void setAccessionCode(String ac){ accessionCode = ac;}
    public String getAccessionCode() { return accessionCode; }
    public void setLastModified(Date d){lastModified = d;}
    public Date   getLastModified() { return lastModified; }
    public void setResolution( float resolution){this.resolution = resolution;}
    public float  getResolution() { return resolution; }
    public void setRfactor(float rfactor){this.rfactor = rfactor;}
    public float  getRfactor() { return rfactor; }
    public void setClassification(String classi){classification = classi;}
    public String getClassification() { return classification; }
    public void setExpData(String expdat) { expData = expdat;}
    public String getExpData() { return expData; }
    public void setTitle(String title){ this.title=title;}
    public String getTitle(){ return title;}
    public void setHeteroList(String hetlist){heteroList = hetlist;}
    public String getHeteroList() { return heteroList; }
};
