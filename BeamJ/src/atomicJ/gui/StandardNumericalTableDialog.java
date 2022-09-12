
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

package atomicJ.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import javax.swing.*;

public class StandardNumericalTableDialog extends JDialog
{
    private static final long serialVersionUID = 1L;

    private final FormatAction formatAction = new FormatAction();
    private final SaveAction saveAction = new SaveAction();
    private final PrintAction printAction = new PrintAction();
    private final TextFileChooser fileChooser = new TextFileChooser();
    private final NumericalFormatDialog customizeDialog;

    private final MinimalNumericalTable table;

    private final JScrollPane scrollPane;
    private final JMenu menuCustomize;

    public StandardNumericalTableDialog(Window parent, ModalityType modalityType, MinimalNumericalTable table, String title)
    {
        super(parent, title, modalityType);
        setLayout(new BorderLayout(1,5));

        this.table = table;
        this.customizeDialog = new NumericalFormatDialog(this, table, "Customize number format");

        JMenuBar menuBar = new JMenuBar();


        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic(KeyEvent.VK_F);

        JMenuItem itemSave = new JMenuItem(saveAction);
        JMenuItem itemPrint = new JMenuItem(printAction);
        JMenuItem itemClose = new JMenuItem(new CloseAction());

        menuFile.add(itemSave);
        menuFile.add(itemPrint);
        menuFile.addSeparator();
        menuFile.add(itemClose);

        menuCustomize = new JMenu("Customize");
        menuCustomize.setMnemonic(KeyEvent.VK_U);
        JMenuItem itemCustomize = new JMenuItem(formatAction);

        menuCustomize.add(itemCustomize);

        menuBar.add(menuFile);
        menuBar.add(menuCustomize);	

        menuBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredSoftBevelBorder(), BorderFactory.createEmptyBorder(3,3,3,3)));


        scrollPane = new JScrollPane(table); 
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        scrollPane.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),scrollPane.getBorder()));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(menuBar, BorderLayout.NORTH);
        add(mainPanel,BorderLayout.CENTER);

        JPanel buttonGroupResults = new JPanel(new GridLayout(1, 0, 5, 5));
        JPanel buttonGroupResultsContainer = new JPanel();

        JButton buttonClose = new JButton(new CloseAction());
        JButton buttonShowAll = new JButton(saveAction);
        JButton buttonPrint = new JButton(printAction);

        buttonGroupResults.add(buttonShowAll);
        buttonGroupResults.add(buttonPrint);
        buttonGroupResults.add(buttonClose);
        buttonGroupResultsContainer.add(buttonGroupResults);
        buttonGroupResultsContainer.setBorder(BorderFactory.createRaisedBevelBorder());
        add(buttonGroupResultsContainer,BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    public MinimalNumericalTable getTable()
    {
        return table;
    }

    protected JScrollPane getScrollPane()
    {
        return scrollPane;
    }

    protected void addMenuItem(JMenuItem menuItem)
    {
        menuCustomize.add(menuItem);
    }

    protected void setHorizontalScrollBarPolicy(int policy)
    {
        scrollPane.setHorizontalScrollBarPolicy(policy);
    }

    protected void setVerticalScrollBarPolicy(int policy)
    {
        scrollPane.setVerticalScrollBarPolicy(policy);
    }

    public void saveTable()
    {
        if(!table.isEmpty())
        {
            File path = table.getDefaultOutputDirectory();

            JFileChooser chooser = getFileChooser();           
            chooser.setCurrentDirectory(path);
            int op = chooser.showSaveDialog(getParent());

            if(op != JFileChooser.APPROVE_OPTION)
            {
                return;
            }

            try 
            {                
                NumericalTableExporter exporter = new NumericalTableExporter(); 
                File selectedFile = getFileChooser().getSelectedFile();
                String selectedExt = fileChooser.getSelectedExtension();

                if(TextFileChooser.TSV_EXTENSION.equals(selectedExt))
                {                   
                    exporter.exportTableAsTSV(table, selectedFile, table.getDecimalFormat());
                }
                else
                {                  
                    exporter.exportTableAsCSV(table, selectedFile, table.getDecimalFormat());
                }
                table.setSaved(true);
            } 
            catch (IOException ex) 
            {
                JOptionPane.showMessageDialog(StandardNumericalTableDialog.this, "Error encountered while saving", "", JOptionPane.ERROR_MESSAGE);			
            }

        }		
    }


    public JFileChooser getFileChooser() {
        return fileChooser;
    }


    private class SaveAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SaveAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME, "Save");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {			
            saveTable();
        }
    }

    private class PrintAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public PrintAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(NAME, "Print");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            try 
            {
                table.print();
            } 
            catch (PrinterException pe) 
            {
                JOptionPane.showMessageDialog(StandardNumericalTableDialog.this, pe.getMessage(), "", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class CloseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;
        public CloseAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME, "Close");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            setVisible(false);
        }
    }

    private class FormatAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FormatAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
            putValue(NAME,"Format data");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            customizeDialog.setVisible(true);
        }
    }
}
