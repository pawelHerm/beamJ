
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Pawe� Hermanowicz
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

package atomicJ.gui.imageProcessing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import atomicJ.gui.NumericalField;
import atomicJ.gui.SubPanel;
import atomicJ.gui.TextFileChooser;
import atomicJ.gui.generalProcessing.BatchProcessingDialog;



public class MedianWeighted2DDialog extends BatchProcessingDialog<MedianWeighted2DModel, String> implements PropertyChangeListener, KernelChangeListener
{
    private static final long serialVersionUID = 1L;

    private int initRadiusX = 1;
    private int initRadiusY = 1;

    private double[][] initKernelData;

    private final ImportAction importAction = new ImportAction();
    private final ExportAction exportAction = new ExportAction();

    private final JSpinner spinnerRadiusX = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));
    private final JSpinner spinnerRadiusY = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));

    private final InegerKernelPanel panelKernelData = new InegerKernelPanel();

    private final TextFileChooser fileChooser = new TextFileChooser();

    public MedianWeighted2DDialog(Window parent, String title, boolean temporary)
    {
        super(parent, title,temporary);

        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);   	
        add(panelButtons, BorderLayout.SOUTH);   	

        initChangeListener();

        pack();
        setLocationRelativeTo(parent);
    }

    private void initChangeListener()
    {
        spinnerRadiusX.addChangeListener(new ChangeListener() 
        {       
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                int radiusX = ((SpinnerNumberModel)spinnerRadiusX.getModel()).getNumber().intValue();
                getModel().setKernelXRadius(radiusX);
            }
        });
        spinnerRadiusY.addChangeListener(new ChangeListener() 
        {       
            @Override
            public void stateChanged(ChangeEvent evt) 
            {
                int radiusX = ((SpinnerNumberModel)spinnerRadiusY.getModel()).getNumber().intValue();
                getModel().setKernelYRadius(radiusX);                
            }
        });
    }	

    private JPanel buildMainPanel()
    {	
        SubPanel mainPanel = new SubPanel();

        SubPanel panelActiveArea = new SubPanel();
        panelActiveArea.addComponent(new JLabel("Apply to: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelActiveArea.addComponent(buildPanelROIRelative(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, .05, 1);
        panelActiveArea.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        SubPanel panelBatchType = new SubPanel();
        panelBatchType.addComponent(new JLabel("Sources:   "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelBatchType.addComponent(buildPanelBatchType(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelBatchType.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        SubPanel panelIdentifierType = new SubPanel();
        panelIdentifierType.addComponent(new JLabel("Channels: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelIdentifierType.addComponent(getIdentifierSelectionPanel(), 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelIdentifierType.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        SubPanel panelRadii = new SubPanel();        

        panelRadii.addComponent(new JLabel("X radius"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelRadii.addComponent(spinnerRadiusX, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      
        panelRadii.addComponent(new JLabel("Y radius"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelRadii.addComponent(spinnerRadiusY, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);      

        SubPanel panelSettings = new SubPanel();
        panelSettings.addComponent(panelKernelData, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(panelRadii, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        SubPanel panelImportExport = new SubPanel();        

        panelImportExport.addComponent(new JButton(importAction), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        panelImportExport.addComponent(new JButton(exportAction), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);

        mainPanel.addComponent(panelActiveArea, 0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelBatchType, 0, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelIdentifierType, 0, 2, 3, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(panelImportExport, 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(panelSettings, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(buildPanelPreview(), 2, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(Box.createHorizontalGlue(), 3, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    @Override
    protected void setModel(MedianWeighted2DModel modelNew)
    {
        super.setModel(modelNew);  

        modelNew.addKernelChangeListener(this);

        panelKernelData.setModel(modelNew);
    }

    protected void clearOldModel(Convolution2DModel model)
    {
        super.clearOldModel();

        if(model != null)
        {
            model.removeKernelChangeListener(this);
        }
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        MedianWeighted2DModel model = getModel();

        this.initRadiusX = model.getKernelXRadius();
        this.initRadiusY = model.getKernelYRadius();

        this.initKernelData = model.getKernel();

        boolean exportEnabled = model.isExportEnabled();
        exportAction.setEnabled(exportEnabled);
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        MedianWeighted2DModel model = getModel();

        model.setKernelXRadius(initRadiusX);
        model.setKernelYRadius(initRadiusY);
        model.setKernelElements(initKernelData);    
    }

    @Override
    protected void resetEditor()
    {	
        super.resetEditor();

        spinnerRadiusX.setValue(initRadiusX);
        spinnerRadiusY.setValue(initRadiusY);      
        panelKernelData.setKernelElements(initKernelData);

        pack();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(Convolution2DModel.KERNEL_X_RADIUS.equals(property))
        {
            int valueNew = (int)evt.getNewValue();
            int valueOld = ((Number)spinnerRadiusX.getValue()).intValue(); 

            if(valueNew != valueOld)
            {
                spinnerRadiusX.setValue(valueNew);
            }
        }
        else if(Convolution2DModel.KERNEL_Y_RADIUS.equals(property))
        {
            int valueNew = (int)evt.getNewValue();
            int valueOld = ((Number)spinnerRadiusY.getValue()).intValue(); 

            if(valueNew != valueOld)
            {
                spinnerRadiusY.setValue(valueNew);
            }
        }
        else if(Convolution2DModel.EXPORT_ENABLED.equals(property))
        {
            boolean enabledNew = (boolean)evt.getNewValue();
            exportAction.setEnabled(enabledNew);
        }
    }

    @Override
    public void kernelElementValueChanged(KernelElementValueEvent evt) 
    {
        panelKernelData.kernelElementValueChanged(evt);
    }

    @Override
    public void kernelStructureChanged(KernelStructuralEvent evt) 
    {
        panelKernelData.kernelStructureChanged(evt);

        pack();
    }

    private void importKernel()
    {
        int op = fileChooser.showOpenDialog(getParent());

        if(op == JFileChooser.APPROVE_OPTION)
        {   
            try 
            {                
                File selectedFile = fileChooser.getSelectedFile();                

                FileNameExtensionFilter filter = (FileNameExtensionFilter)fileChooser.getFileFilter();

                MedianWeighted2DModel model = getModel();

                model.importKernel(selectedFile, filter.getExtensions());                         
            } 
            catch (IOException ex) 
            {
                JOptionPane.showMessageDialog(this, "Error encountered while saving", "", JOptionPane.ERROR_MESSAGE);
            }
        }   
    }

    private void exportKernel()
    {
        int op = fileChooser.showSaveDialog(getParent());

        if(op == JFileChooser.APPROVE_OPTION)
        {   
            try 
            {                
                File selectedFile = fileChooser.getSelectedFile();                
                MedianWeighted2DModel model = getModel();

                model.exportKernel(selectedFile, fileChooser.getSelectedExtensions());                         
            } 
            catch (IOException ex) 
            {
                JOptionPane.showMessageDialog(this, "Error encountered while saving", "", JOptionPane.ERROR_MESSAGE);
            }
        }   
    }

    private class ImportAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ImportAction()
        {           
            putValue(MNEMONIC_KEY, KeyEvent.VK_I);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Import");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            importKernel();
        }
    }

    private class ExportAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ExportAction()
        {           
            putValue(MNEMONIC_KEY, KeyEvent.VK_E);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Export");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            exportKernel();
        }
    }

    private class InegerKernelPanel extends KernelPanel
    {
        private static final long serialVersionUID = 1L;

        @Override
        protected NumericalField createNumericalField()
        {
            NumericalField field = new NumericalField("Invalid input: only integers allowed", 0, false);
            return field;
        }

    }
}
