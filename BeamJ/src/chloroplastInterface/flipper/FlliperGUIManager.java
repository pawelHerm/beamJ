
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Pawe³ Hermanowicz
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

package chloroplastInterface.flipper;

import java.util.*;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import atomicJ.gui.NumericalField;
import atomicJ.gui.SpinnerDoubleModel;
import atomicJ.gui.SubPanel;
import chloroplastInterface.StandardTimeUnit;

public class FlliperGUIManager implements PropertyChangeListener
{
    private final JComboBox<FlipperPosition> comboCurrentPosition = new JComboBox<>(FlipperPosition.getKnownPositions());
    private final JCheckBox boxFlipDuringMeasurement = new JCheckBox("Flip during measurement");
    private final JComboBox<StandardTimeUnit> comboIntervalUnit = new JComboBox<>(new StandardTimeUnit[] {StandardTimeUnit.SECOND,StandardTimeUnit.MINUTE,StandardTimeUnit.HOUR});
    private final JFormattedTextField fieldSerialNumber = new JFormattedTextField(new DefaultFormatter());

    private final JSpinner spinnerIntervalValue = new JSpinner(new SpinnerDoubleModel(1., 0, 100000, 1.));
    private final NumericalField fieldIntervalValue = new NumericalField("Time interval must be a non-negative number", 0);
    private final JSpinner spinnerTransitTime = new JSpinner();

    private final JPanel mainPanel;

    private final VoltageSignalGUIManager voltageSignalGUI;

    private final FlipperModel model;

    private FlliperGUIManager(FlipperModel model)
    {
        this.model = model;
        this.model.addPropertyChangeListener(this);

        spinnerIntervalValue.setEditor(fieldIntervalValue);

        this.voltageSignalGUI = new VoltageSignalGUIManager(model);
        this.mainPanel = buildMainPanel();
        this.mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.add(mainPanel, BorderLayout.NORTH);

        pullModelProperties();
        initChangeListeners();
        initItemListeners();
        initFieldListeners();
    }

    public static JPanel getGUI(FlipperModel model)
    {
        FlliperGUIManager guiManager = new FlliperGUIManager(model);
        return guiManager.mainPanel;
    }

    private JPanel buildMainPanel()
    {        
        SubPanel panelFlipper = new SubPanel();

        JLabel labelSerialNumber = new JLabel("Serial no S/N");
        JLabel labelCurrentPosition = new JLabel("Current position");
        JLabel labelFlipInterval = new JLabel("Flip interval");
        JLabel labelTransitTime = new JLabel("Transit time");
        JLabel labelTransitTimeUnit = new JLabel("ms");

        panelFlipper.addComponent(labelSerialNumber, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panelFlipper.addComponent(fieldSerialNumber, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        panelFlipper.addComponent(labelCurrentPosition, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panelFlipper.addComponent(comboCurrentPosition, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        panelFlipper.addComponent(labelTransitTime, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panelFlipper.addComponent(spinnerTransitTime, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        panelFlipper.addComponent(labelTransitTimeUnit, 2, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);

        panelFlipper.addComponent(boxFlipDuringMeasurement, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);     
        panelFlipper.addComponent(labelFlipInterval, 0, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panelFlipper.addComponent(spinnerIntervalValue, 1, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(5, 5, 5, 5));
        panelFlipper.addComponent(comboIntervalUnit, 2, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(5, 5, 5, 5));

        //JPanel outerPanel = new JPanel(new BorderLayout());

        SubPanel outerPanel = new SubPanel();

        outerPanel.addComponent(panelFlipper, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);

        // outerPanel.add(panelFlipper, BorderLayout.WEST);        

        outerPanel.addComponent(Box.createVerticalStrut(10), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);
        outerPanel.addComponent(voltageSignalGUI.getSettingsPanel(), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);

        //outerPanel.add(panelVoltageSignals, BorderLayout.SOUTH);        

        return outerPanel;
    }

    //has to be called after buildMainPanel() method
    private void pullModelProperties()
    {    
        String serialNumber = model.getSerialNumber();
        double intervalValue = model.getFlipIntervalValue();
        boolean flipDuringMeasurement = model.isStartFlippingWhenNewMeasurementBegins();

        int transitTime = model.getTransitTimeInMiliseconds();
        int shortestTransitTime = model.getShortestTransitTimeInMiliseconds();
        int longestTransitTime = model.getLongestTransitTimeInMiliseconds();

        StandardTimeUnit intervalUnit = model.getFlipIntervalUnit();
        FlipperPosition currentPosition = model.getFlipperPosition();

        fieldSerialNumber.setValue(serialNumber);
        comboCurrentPosition.setSelectedItem(currentPosition);
        boxFlipDuringMeasurement.setSelected(flipDuringMeasurement);

        fieldIntervalValue.setValue(intervalValue);
        spinnerIntervalValue.setValue(intervalValue);

        comboIntervalUnit.setSelectedItem(intervalUnit);


        SpinnerNumberModel transitTimeModel = new SpinnerNumberModel(transitTime, shortestTransitTime, longestTransitTime, 1);
        spinnerTransitTime.setModel(transitTimeModel);
    }

    private void initChangeListeners()
    {
        spinnerIntervalValue.addChangeListener(new ChangeListener() 
        {
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                double valNew = ((SpinnerDoubleModel)spinnerIntervalValue.getModel()).getDoubleValue();
                double valOld = fieldIntervalValue.getValue().doubleValue();

                if(Double.compare(valOld, valNew) != 0)
                {
                    model.setFlipIntervalValue(valNew);                
                }            
            }
        });

        fieldIntervalValue.addPropertyChangeListener(NumericalField.VALUE_EDITED,new PropertyChangeListener() {            
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                double valNew = ((Number)evt.getNewValue()).doubleValue();

                model.setFlipIntervalValue(valNew);            
            }
        }); 

        spinnerTransitTime.addChangeListener(new ChangeListener() 
        {         
            @Override
            public void stateChanged(ChangeEvent e)
            {
                int valNew = ((SpinnerNumberModel)spinnerTransitTime.getModel()).getNumber().intValue();
                model.selectTransitTimeInMiliseconds(valNew);
            }
        });
    }

    private void initItemListeners()
    {   
        boxFlipDuringMeasurement.addItemListener(new ItemListener() 
        {         
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {                
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setStartFlippingWhenNewMeasurementBegins(selected);               
            }
        });

        comboIntervalUnit.addItemListener(new ItemListener() 
        {           
            @Override
            public void itemStateChanged(ItemEvent e) {
                StandardTimeUnit intervalUnit = comboIntervalUnit.getItemAt(comboIntervalUnit.getSelectedIndex());
                model.setFlipIntervalUnit(intervalUnit);;
            }
        });

        comboCurrentPosition.addItemListener(new ItemListener() 
        {            
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                FlipperPosition currentPosition = comboCurrentPosition.getItemAt(comboCurrentPosition.getSelectedIndex());

                model.selectFlipperPosition(currentPosition);
            }
        });
    }

    private void initFieldListeners()
    {
        fieldSerialNumber.addPropertyChangeListener("value",new PropertyChangeListener() {            
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                String valNew = ((String)evt.getNewValue());
                model.setSerialNumber(valNew);            
            }
        }); 
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if(FlipperModel.CURRENT_POSITION.equals(property))
        {            
            FlipperPosition valNew = (FlipperPosition)evt.getNewValue();
            FlipperPosition valOld = comboCurrentPosition.getItemAt(comboCurrentPosition.getSelectedIndex());

            if(!Objects.equals(valOld, valNew))
            {
                comboCurrentPosition.setSelectedItem(valNew);
            }
        }
        else if(FlipperModel.SERIAL_NUMBER.equals(property))
        {
            String valNew = (String)evt.getNewValue();
            String valOld = (String)fieldSerialNumber.getValue();

            if(!Objects.equals(valOld, valNew))
            {
                fieldSerialNumber.setValue(valNew);;
            }
        }  
        else if(FlipperModel.START_FLIPPING_WHEN_NEW_MEASUREMENT_BEGINS.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = boxFlipDuringMeasurement.isSelected();

            if(valOld != valNew)
            {
                boxFlipDuringMeasurement.setSelected(valNew);
            }
        }
        else if(FlipperModel.FLIP_INTERVAL_UNIT.equals(property))
        {
            StandardTimeUnit unitOld = comboIntervalUnit.getItemAt(comboIntervalUnit.getSelectedIndex());
            StandardTimeUnit unitNew = (StandardTimeUnit)evt.getNewValue();
            if(!Objects.equals(unitOld,unitNew))
            {
                comboIntervalUnit.setSelectedItem(unitNew);
            }
        }
        else if(FlipperModel.FLIP_INTERVAL_VALUE.equals(property))
        {
            double valOld = fieldIntervalValue.getValue().doubleValue();
            double valNew = ((Number)evt.getNewValue()).doubleValue();

            if(Double.compare(valOld, valNew) != 0)
            {
                spinnerIntervalValue.setValue(valNew);
                fieldIntervalValue.setValue(valNew);
            }
        }
        else if(FlipperModel.NECESSARY_INPUT_PROVIDED.equals(property))
        {
        }
    }
}
