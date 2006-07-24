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
 * Created on Oct 31, 2005
 *
 */
package org.biojava.spice.manypanel.eventmodel;

/** some things that can happen with DAS sources
 * 
 * @author Andreas Prlic
 *
 */
public interface DasSourceListener {

    /** a new DAS source to be added
     * 
     * @param ds an event
     */
    public void newDasSource(DasSourceEvent ds);
    
    /** remove a DAS source
     * 
     * @param ds an event
     */
    public void removeDasSource(DasSourceEvent ds);
    
    /** a DAS source has been selected (in the GUI)
     * 
     * @param ds
     */
    public void selectedDasSource(DasSourceEvent ds);
    
    /** the features of this das source are being loaded
     * 
     * @param ds
     */
    public void loadingStarted(DasSourceEvent ds);
          
    
    /** the loading of the features of this das source has finished
     * 
     * @param ds
     */
    public void loadingFinished(DasSourceEvent ds);
    
    
       
    /** a DAS source has been enabled 
     * 
     * @param ds
     */
    public void enableDasSource(DasSourceEvent ds);
    
    /** a DAS source has been disabled
     * 
     * @param ds
     */
    public void disableDasSource(DasSourceEvent ds);
}
