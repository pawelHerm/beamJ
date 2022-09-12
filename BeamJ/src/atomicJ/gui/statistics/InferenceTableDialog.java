
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Paweł Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.gui.statistics;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import atomicJ.gui.NumericalArrayExporter;
import atomicJ.gui.StandardNumericalTableDialog;


public class InferenceTableDialog extends StandardNumericalTableDialog
{
    private static final long serialVersionUID = 1L;

    public InferenceTableDialog(Window parent, InferencesTable table)
    {
        super(parent , ModalityType.MODELESS, table, "Test results");					
    }

    @Override
    public void saveTable()
    {
        InferencesTable table = (InferencesTable)getTable();
        if(!getTable().isEmpty())
        {
            File path = table.getDefaultOutputDirectory();

            getFileChooser().setCurrentDirectory(path);
            int op = getFileChooser().showSaveDialog(getParent());

            if(op == JFileChooser.APPROVE_OPTION)
            {	
                try 
                {                
                    NumericalArrayExporter exporter = new NumericalArrayExporter(); 
                    File selectedFile = getFileChooser().getSelectedFile();
                    int n = selectedFile.getName().lastIndexOf(".");

                    FileNameExtensionFilter filter = (FileNameExtensionFilter)getFileChooser().getFileFilter();
                    String selectedExt = filter.getExtensions()[0];

                    if(selectedExt.equals("tsv"))
                    {
                        if(n<0)
                        {
                            selectedFile = new  File(selectedFile.getParent(), selectedFile.getName() + ".tsv");
                        }
                        exporter.exportTableAsTSV(((InferencesTableModel)table.getModel()).getSavableData(), selectedFile, getTable().getDecimalFormat());
                    }
                    else
                    {
                        if(n<0)
                        {
                            selectedFile = new  File(selectedFile.getParent(), selectedFile.getName() + ".csv");
                        }
                        exporter.exportTableAsCSV(((InferencesTableModel)table.getModel()).getSavableData(), selectedFile, table.getDecimalFormat());
                    }
                    table.setSaved(true);
                } 
                catch (IOException ex) 
                {
                    JOptionPane.showMessageDialog(InferenceTableDialog.this, "Error encountered while saving", "", JOptionPane.ERROR_MESSAGE);			
                }
            }
        }		
    }
}
