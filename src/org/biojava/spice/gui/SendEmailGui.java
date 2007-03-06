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
 * Created on Feb 13, 2007
 *
 */
package org.biojava.spice.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.biojava.spice.ResourceManager;
import org.biojava.spice.config.SpiceDefaults;
import org.biojava.spice.utils.SendEmail;

public class SendEmailGui {

    String[] recipients;
    String subject;
    String message;
    
    JTextField fromField;
    
    public SendEmailGui(){

    }
    
    
    

    public String getFrom() {
        return fromField.getText();
    }







    public String getMessage() {
        return message;
    }



    public String[] getRecipients() {
        return recipients;
    }



    public String getSubject() {
        return subject;
    }


    /** create a simple user interface to post emails
     * 
     * @param recipients
     * @param subject
     * @param message
     * @param from     
     */
    public void postMailFromGui(String recipients[ ], 
            String subject, 
            String message , 
            String from
            ){

        this.recipients = recipients;
        this.subject = subject;
        
        this.message =  message;
       
        
        JFrame frame = new JFrame("send email");

        final JDialog dialog = new JDialog (frame, "File Bug Report", false);
        Dimension d = new Dimension(500,500);
        dialog.setSize(d);
        
        dialog.getContentPane().setLayout (new BorderLayout());
        fromField = new JTextField(ResourceManager.getString("org.biojava.spice.BugReportEmail"));

        { // NORTH
            final JPanel panel = new JPanel();
            
            panel.setLayout (new BoxLayout (panel, BoxLayout.Y_AXIS));

            JLabel label = new JLabel (
            "Can I submit this bug report to the authors?");
            label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            panel.add (label);
            label = new JLabel (
            "");
            label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            panel.add (label);

            final JPanel fromPanel = new JPanel();
            fromPanel.setLayout (new BorderLayout());
            fromPanel.add (new JLabel ("From: "), BorderLayout.WEST);
            fromPanel.add (fromField, BorderLayout.CENTER);
            fromPanel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            panel.add (fromPanel);
            dialog.getContentPane().add (panel, BorderLayout.NORTH);
        }

        { // CENTER
            JEditorPane bugArea = new JEditorPane("text/html", message);
            bugArea.setSize(d);
            bugArea.setPreferredSize(d);
            //bugArea.setText (message);
            bugArea.setEditable (false);
            bugArea.setAlignmentX (JTextArea.CENTER_ALIGNMENT);
            dialog.getContentPane().add (new JScrollPane (bugArea),
                    BorderLayout.CENTER);
        }

        { // SOUTH
            final JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout (new BoxLayout (buttonPanel, BoxLayout.X_AXIS));
            buttonPanel.add (Box.createHorizontalGlue());
            JButton yes = new JButton ("Yes");
            JButton no = new JButton ("No");
            yes.addActionListener (new ActionListener(){
                public void actionPerformed (ActionEvent evt)
                {
                    dialog.dispose();

                }});
            yes.addActionListener(new MyActionListener(this));
            
            no.addActionListener (new ActionListener ()
            {
                public void actionPerformed (ActionEvent evt)
                {
                    dialog.dispose();
                }
            });
            buttonPanel.add (yes);
            buttonPanel.add (no);
            dialog.getContentPane().add (buttonPanel, BorderLayout.SOUTH);
        }

        dialog.pack();
        dialog.setVisible (true);
    }
}


class MyActionListener
implements ActionListener {

    SendEmailGui parent;

    Logger logger = Logger.getLogger(SpiceDefaults.LOGGER);
    
    public MyActionListener(SendEmailGui parent){
        this.parent = parent;
    }


    public void actionPerformed (ActionEvent evt)
    {
       
        String subject      = parent.getSubject();
        String from         = parent.getFrom();
        String message      = parent.getMessage();

        System.out.println("do send email action");
        SendEmail sender = new SendEmail();
        try {
            sender.postMail(subject,message,from);
        } catch (IOException ex){
            logger.warning(ex.getMessage());
            
        }
    }
}

