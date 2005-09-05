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
 * Created on Aug 23, 2005
 *
 */
package org.biojava.spice.server;

import org.biojava.spice.*;

/**
 * @author Andreas Prlic
 *
 */
public class SpiceProtocol {
    // the response
    public final static String SPICE_OK = "SPICE: OK";
    public final static String SPICE_WHAT = "SPICE: WHAT?";
    public final static String SPICE_ERROR = "SPICE: ERROR";
    public final static String NO_RUNNING_SPICE = "NO SPICE!";
    /**
     * 
     */
    public SpiceProtocol() {
        super();
       
    }
    
    
    /** The input should look like:
     * 
     * SPICE: load Type Accessioncode
     * 
     * where Type is the type of accession code ( 'UniProt' or 'PDB' )
     * Accessioncode is the accession code (e.g. '5pti' or 'P50225' ) 
     * 
     * 
     * the response can have three types of responses:
     *  SPICE_OK ... successfully sent to SPICE
     *  SPICE_WHAT ... I do not understand what you say (you do not stick to this protocol)
     *  SPICE_ERROR .. something went wrong 
     *  NO_RUNNING_SPICE ... there is no spice running 
     * 
     * @param str
     * @return
     */
    public String processInput(String str,SPICEFrame spice){
        
        try {
            String start = str.substring(0,11);
            System.out.println(start);
            if ( str.length() > 50){
                return SPICE_WHAT;
            }
            if (start.equals("SPICE: load")){
                // seems to be a SPICE protocol message...
                String[] split = str.split(" ");
                if ( split.length != 4) {
                    //logger.info("message does not have right length...") ;
                    return SPICE_WHAT;
                }
                String type = split[2];
                String accessionCode = split[3];
                System.out.println("trying to display " +type+ " " + accessionCode);
                
                if (  (type.equals("PDB")    ) || 
                      (type.equals("UniProt")) ) {
                    spice.load(type,accessionCode);
                    if ( spice instanceof SpiceApplication ){
                        SpiceApplication parent = (SpiceApplication) spice;
                        parent.setVisible(true);
                        parent.show();
                        parent.toFront();
                        parent.requestFocus();
                        parent.setState(java.awt.Frame.NORMAL);
                        
                    }
                    return SPICE_OK;
                }
                else {
                    // what kind of type is this ???
                    return SPICE_WHAT;
                }
                
            } else {
                return SPICE_WHAT;
            }
                
        } catch (Exception e) {
            e.printStackTrace();
            return SPICE_ERROR;
        }
        
     //   return SPICE_ERROR;
    }

}
