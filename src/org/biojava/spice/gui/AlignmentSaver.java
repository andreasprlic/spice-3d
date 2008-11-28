package org.biojava.spice.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.biojava.bio.structure.Structure;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.config.SpiceDefaults;


/** take the currently displayed structure alignment and writes it to the file system as a PDB file.
 * 
 * @author Andreas Prlic
 *
 */
public class AlignmentSaver {
	
	static Logger    logger      = Logger.getLogger(SpiceDefaults.LOGGER);
	
	public AlignmentSaver(SpiceApplication spice){

		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();

			if ( file.exists()){
				int status = JOptionPane.showConfirmDialog(null,
					    "The file " + file.toString() + " already exists. Overwrite it?"
					    );
				if ( status != JOptionPane.YES_OPTION){
					logger.info("saving of file canceled");
					return;
				}
				
				
				
			}
			logger.info("saving alignment to " + file);
			Structure s = spice.getSelectionPanel().getAlignmentChooser().getStructureAlignment().createArtificalStructure();
			try {
			 Writer output = new BufferedWriter(new FileWriter(file));
			    try {
			      //FileWriter always assumes default encoding is OK!
			      output.write( s.toPDB() );
			    }
			    finally {
			      output.close();
			    }
			} catch (Exception e){
				
				logger.warning("could not save alignment. " + e.getMessage());
			}

		}


	}
}
