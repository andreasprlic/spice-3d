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
 * Created on Nov 29, 2005
 *
 */
package org.biojava.spice.manypanel.eventmodel;

import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.spice.Feature.Feature;
import org.biojava.spice.Feature.Segment;

public class SpiceFeatureEvent {

    Feature feature;
    Segment segment;
    SpiceDasSource source;
    
    public SpiceFeatureEvent(SpiceDasSource source,Feature feature) {
        super();
        this.feature = feature;
        this.segment = null;
        this.source = source;
    }
    
    public SpiceFeatureEvent(SpiceDasSource source,Feature feature, Segment s){
        this(source,feature);
        segment = s;
    }
    
    public Feature getFeature(){
        return feature;
    }
    
    public void setFeature(Feature feat){
        feature = feat;
        
    }
    
    public Segment getSegment(){
        return segment;
    }
    
    public void setSegment(Segment s){
        segment = s;
    }
    
    public void setDasSource(SpiceDasSource source){
        this.source = source;
    }

    public SpiceDasSource getDasSource(){
        return source;
    }
    
}
