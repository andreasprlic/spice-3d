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
 * Created on Aug 3, 2005
 *
 */
package org.biojava.spice.DAS;


import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes            ;
import java.util.*;

/** a class to parse the XML response of a DAS - stylesheet request.
 * @author Andreas Prlic
 *
 */
public class DAS_Stylesheet_Handler extends DefaultHandler {
    
    List typeGlyphMaps;
    Map  currentType;
    String chars ;
    
    /**
     * 
     */
    public DAS_Stylesheet_Handler() {
        super();
        typeGlyphMaps = new ArrayList();
        currentType = new HashMap();
    }
    
    
    public Map[] getTypeStyles(){
        return (Map[]) typeGlyphMaps.toArray(new Map[typeGlyphMaps.size()]);
    }
    
    public void startElement (String uri, String name, String qName, Attributes atts){
        chars = "";
        if ( qName.equals("TYPE")){
            // this glyph matches to features of type >id<.
            String id = atts.getValue("id");
            currentType = new HashMap(); 
            currentType.put("type",id);
        }
        
        else if ( qName.equals("ARROW")){
            currentType.put("style","arrow");
        } else if ( qName.equals("ANCHORED_ARROW")){
            currentType.put("style","anchored_arrow");
        } else if ( qName.equals("BOX")){
            currentType.put("style","box");
        } else if ( qName.equals("CROSS")){
            currentType.put("style","cross");
        } else if ( qName.equals("EX")){
            currentType.put("style","EX");
        } else if ( qName.equals("HELIX")){
            currentType.put("style","helix");
        } else if ( qName.equals("LINE")){
            currentType.put("style","LINE");
        }  else if ( qName.equals("SPAN")){
            currentType.put("style","span");
        } else if ( qName.equals("TRIANGLE")){
            currentType.put("style","triangle");
        }
        
    }
    
    public void endElement(String uri, String name, String qName) {
        if ( qName.equals("HEIGHT")){
            currentType.put("height",chars);
        } else if ( qName.equals("COLOR")){
            currentType.put("color",chars);
        } else if ( qName.equals("OUTLINECOLOR")){
            currentType.put("outlinecolor",chars);
        } else if ( qName.equals("BACKGROUND")){
            currentType.put("background",chars);
        } else if ( qName.equals("BUMP")){
            if ( chars.equals("no"))
                currentType.put("bump","no");
            else 
                currentType.put("bump","yes");
        } 
        
        else if ( qName.equals("TYPE")){
            typeGlyphMaps.add(currentType);
        }
    }
    
    public void characters (char ch[], int start, int length){
        
     
            for (int i = start; i < start + length; i++) {
               chars += ch[i];
            }
        
        
    }
    
}




