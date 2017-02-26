package net.sourceforge.fidocadj.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import net.sourceforge.fidocadj.globals.*;
import net.sourceforge.fidocadj.dialogs.mindimdialog.MinimumSizeDialog;


/** The class DialogAttachImage allows to determine which image has to be
    attached and shown as a background (for retracing/vectorization purposes).

    <pre>
    This file is part of FidoCadJ.

    FidoCadJ is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FidoCadJ is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FidoCadJ. If not,
    @see <a href=http://www.gnu.org/licenses/>http://www.gnu.org/licenses/</a>.

    Copyright 2017 by Davide Bucci
    </pre>

    @author Davide Bucci
*/

public class DialogAttachImage extends MinimumSizeDialog
{
    private final JFrame parent;        // Parent window
    private JTextField fileName;        // File name text field
    private JTextField resolution;      // Resolution text field

    private boolean attach;     // Indicates that the attach should be done

    /** Standard constructor.
        @param p the dialog parent
    */
    public DialogAttachImage(JFrame p)
    {
        super(500, 450, p, Globals.messages.getString("Attach_image_t"),
            true);
        parent=p;

        int ygrid=0;
        // Obtain the current content pane and create the grid layout manager
        // which will be used for putting the elements of the interface.
        GridBagLayout bgl=new GridBagLayout();
        GridBagConstraints constraints;
        Container contentPane=getContentPane();
        contentPane.setLayout(bgl);

        JLabel lblfilename=
            new JLabel(Globals.messages.getString("Image_file_attach"));

        constraints = DialogUtil.createConst(0,ygrid,1,1,100,100,
            GridBagConstraints.EAST, GridBagConstraints.NONE,
            new Insets(6,6,6,6));

        contentPane.add(lblfilename, constraints);

        fileName=new JTextField(10);
        fileName.setText("");

        constraints = DialogUtil.createConst(1,ygrid,1,1,100,100,
            GridBagConstraints.EAST, GridBagConstraints.NONE,
            new Insets(6,6,6,6));

        contentPane.add(fileName, constraints);

        JButton browse=new JButton(Globals.messages.getString("Browse"));
        constraints = DialogUtil.createConst(2,ygrid++,1,1,100,100,
            GridBagConstraints.CENTER, GridBagConstraints.NONE,
            new Insets(6,0,12,12));

        contentPane.add(browse, constraints);

        browse.addActionListener(createBrowseActionListener());

        JLabel lblresolution=
            new JLabel(Globals.messages.getString("Image_resolution"));

        constraints = DialogUtil.createConst(0,ygrid,1,1,100,100,
            GridBagConstraints.EAST, GridBagConstraints.NONE,
            new Insets(6,6,6,6));

        contentPane.add(lblresolution, constraints);

        resolution=new JTextField(10);
        resolution.setText("150");

        constraints = DialogUtil.createConst(1,ygrid++,2,1,100,100,
            GridBagConstraints.EAST, GridBagConstraints.BOTH,
            new Insets(6,6,6,6));

        contentPane.add(resolution, constraints);
        // Put the OK and Cancel buttons and make them active.
        JButton ok=new JButton(Globals.messages.getString("Ok_btn"));
        JButton cancel=new JButton(Globals.messages.getString("Cancel_btn"));
        Box b=Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        ok.setPreferredSize(cancel.getPreferredSize());

        if (Globals.okCancelWinOrder) {
            b.add(ok);
            b.add(Box.createHorizontalStrut(12));
            b.add(cancel);
        } else {
            b.add(cancel);
            b.add(Box.createHorizontalStrut(12));
            b.add(ok);
        }
        constraints = DialogUtil.createConst(1,ygrid++,3,1,0,0,
            GridBagConstraints.EAST, GridBagConstraints.NONE,
            new Insets(12,40,0,0));
        contentPane.add(b, constraints);            // Add OK/cancel buttons
        ok.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent evt)
                {
                    attach=true;
                    setVisible(false);
                }
            });
        cancel.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent evt)
                {
                    setVisible(false);
                }
            });
        // Here is an action in which the dialog is closed
        AbstractAction cancelAction = new AbstractAction ()
        {
            public void actionPerformed (ActionEvent e)
            {
                setVisible(false);
            }
        };
        DialogUtil.addCancelEscape (this, cancelAction);
        pack();
        DialogUtil.center(this);
        getRootPane().setDefaultButton(ok);
    }
    /** Create an action listener which handle clicking on the 'browse' button.
        @return the ActionListener
    */
    private ActionListener createBrowseActionListener()
    {
        return new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                // Open the browser in order to let the user select the file
                // name on which export
                if(Globals.useNativeFileDialogs) {
                    // Native file dialog
                    FileDialog fd = new FileDialog(parent,
                        Globals.messages.getString("Select_file_export"),
                        FileDialog.LOAD);
                    String filen;

                    // Set defaults and make visible.
                    fd.setDirectory(new File(fileName.getText()).getPath());
                    fd.setVisible(true);

                    // The user has selected a file.
                    if(fd.getFile() != null) {
                        filen=Globals.createCompleteFileName(
                            fd.getDirectory(),
                            fd.getFile());
                        fileName.setText(filen);
                    }
                } else {
                    // Swing file dialog
                    JFileChooser fc = new JFileChooser(
                        new File(fileName.getText()).getPath());
                    int r = fc.showOpenDialog(null);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        fileName.setText(fc.getSelectedFile().toString());
                    }
                }
            }
        };
    }
    /** Indicates that the image attach should be done: the user selected
        the "ok" button.
        @return a boolean value which indicates if the attach should be done.
    */
    public boolean shouldAttach()
    {
        return attach;
    }

    /** @return a string containing the file name given by the user.
    */
    public String getFileName()
    {
        return fileName.getText();
    }

    /** @return the resolution in dpi of the image to be used.
    */
    public double getResolution()
    {
        return Double.parseDouble(resolution.getText());
    }
}