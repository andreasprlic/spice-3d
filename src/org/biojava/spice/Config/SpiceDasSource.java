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
 * Created on 17.10.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.spice ;

import org.biojava.services.das.registry.*;

public class SpiceDasSource
    extends DasSource 

{

    boolean status ;

    public SpiceDasSource() {
	super();
	status = true ;
    }

    public void setStatus(boolean flag) {
	status = flag ;
    }

    public boolean getStatus() {
	return status ;
    }

    
    
    /** convert DasSource to SpiceDasSource */
    public void fromDasSource(DasSource ds) {
	this.setUrl(ds.getUrl());
	this.setAdminemail(ds.getAdminemail());
	this.setDescription(ds.getDescription());
	this.setCoordinateSystem(ds.getCoordinateSystem());
	this.setCapabilities(ds.getCapabilities());
	this.setRegisterDate(ds.getRegisterDate());
	this.setLeaseDate(ds.getLeaseDate());
    }


}
