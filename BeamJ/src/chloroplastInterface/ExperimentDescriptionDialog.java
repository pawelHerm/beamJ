
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

package chloroplastInterface;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import atomicJ.gui.EditableComboBox;
import atomicJ.gui.NumericalField;
import atomicJ.gui.SubPanel;
import chloroplastInterface.ExperimentDescriptionModel.PhotometricDescriptionImmutable;


public class ExperimentDescriptionDialog extends JDialog implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    //   private static final double TOLERANCE = 1e-10;

    private final EditableComboBox comboSpeciesName;
    private final EditableComboBox comboLineName;
    private final JCheckBox boxDarkAdpted = new JCheckBox("Dark adapted");
    private final JComboBox<IrradianceUnitType> comboIrradianceUnit = new JComboBox<>(IrradianceUnitType.values());
    private final JFormattedTextField fieldAdditionalComments = new JFormattedTextField(new DefaultFormatter());

    private List<JLabel> phaseLabels = new ArrayList<>();
    private List<JSpinner> intensitySpinners = new ArrayList<>();
    private List<NumericalField> intensityFields = new ArrayList<>();

    private final Action finishAction = new FinishAction();
    private final Action cancelAction = new CancelAction();

    private final JButton buttonFinish = new JButton(finishAction);
    private final JButton buttonCancel = new JButton(cancelAction);

    private final SubPanel mainPanel;

    private final ExperimentDescriptionModel model;
    private PhotometricDescriptionImmutable modelState;

    public ExperimentDescriptionDialog(Component parent, ExperimentDescriptionModel model)
    {
        super(SwingUtilities.getWindowAncestor(parent), "Sample description assistant", ModalityType.APPLICATION_MODAL);

        Image icon = Toolkit.getDefaultToolkit().getImage("Resources/Logo.png");
        setIconImage(icon);

        this.model = model;
        this.model.addPropertyChangeListener(this);

        this.comboSpeciesName = new EditableComboBox(model.getSuggestedPlantNames().toArray(new String[]{}));
        this.comboLineName = new EditableComboBox(model.getSuggestedPlantLineNames().toArray(new String[]{}));

        DefaultFormatter formatterSpeciesName = comboSpeciesName.getEditorFormatter();
        formatterSpeciesName.setOverwriteMode(false);
        formatterSpeciesName.setCommitsOnValidEdit(true);

        DefaultFormatter formatterLineName = comboLineName.getEditorFormatter();
        formatterLineName.setOverwriteMode(false);
        formatterLineName.setCommitsOnValidEdit(true);

        DefaultFormatter formatterAdditionalComments = (DefaultFormatter) fieldAdditionalComments.getFormatter();
        formatterAdditionalComments.setOverwriteMode(false);
        formatterAdditionalComments.setCommitsOnValidEdit(true);

        JPanel panelButtons = buildButtonPanel();	

        this.mainPanel = buildMainPanel();
        this.mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.add(mainPanel, BorderLayout.NORTH);

        JScrollPane scrollFrame = new JScrollPane(outerPanel,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollFrame.setAutoscrolls(true);
        scrollFrame.setPreferredSize(new Dimension(300,500));

        pullModelProperties();

        Container content = getContentPane();
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);

        layout.setAutoCreateContainerGaps(false);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(
                        layout.createParallelGroup()
                        .addComponent(panelButtons)
                        .addComponent(scrollFrame, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        )
                );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(scrollFrame, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panelButtons, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                );

        initItemListeners();
        initFieldListeners();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                ExperimentDescriptionDialog.this.cancel();
            }
        });


        pack();
        setLocationRelativeTo(parent);
    }

    private SubPanel buildMainPanel()
    {        
        SubPanel panel = new SubPanel();

        JLabel labelSpecies = new JLabel("Species");
        JLabel labelLine = new JLabel("Line");
        JLabel labelActinicBeamPhases = new JLabel("Actinic beam phases");
        JLabel labelUnit = new JLabel("Unit");
        JLabel labelComments = new JLabel("Comments");

        panel.addComponent(labelSpecies, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panel.addComponent(comboSpeciesName, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        panel.addComponent(labelLine, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panel.addComponent(comboLineName, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        panel.addComponent(labelComments, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panel.addComponent(fieldAdditionalComments, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        panel.addComponent(boxDarkAdpted, 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);     
        panel.addComponent(labelActinicBeamPhases, 1, 4, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panel.addComponent(labelUnit, 0, 5, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1, new Insets(5, 5, 5, 5));
        panel.addComponent(comboIrradianceUnit, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(5, 5, 5, 5));

        return panel;
    }

    @Override
    public void setVisible(boolean visible)
    {
        this.modelState = this.model.getMemento();
        super.setVisible(visible);
    }

    //has to be called after buildMainPanel() method
    private void pullModelProperties()
    {    
        String speciesName = model.getSpeciesName();
        String lineName = model.getLineName();
        String comments = model.getComments();
        boolean darkAdapted = model.isDarkAdapted();
        IrradianceUnitType unitType = model.getUnitType();

        comboSpeciesName.setSelectedItem(speciesName);
        comboLineName.setSelectedItem(lineName);
        fieldAdditionalComments.setValue(comments);
        boxDarkAdpted.setSelected(darkAdapted);
        comboIrradianceUnit.setSelectedItem(unitType);

        int actinicBeamPhaseCount = model.getActinicBeamPhaseCount();
        addPhaseDependentComponentsAndSetTheirValues(0, actinicBeamPhaseCount);

        boolean finishEnabled = model.isNecessaryInputProvided();
        finishAction.setEnabled(finishEnabled);
    }

    private void initItemListeners()
    {   
        boxDarkAdpted.addItemListener(new ItemListener() 
        {         
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setDarkAdapted(selected);
            }
        });

        comboIrradianceUnit.addItemListener(new ItemListener() 
        {           
            @Override
            public void itemStateChanged(ItemEvent e) {
                IrradianceUnitType unitSelected = comboIrradianceUnit.getItemAt(comboIrradianceUnit.getSelectedIndex());
                model.setUnitType(unitSelected);
            }
        });

        comboSpeciesName.addItemListener(new ItemListener() 
        {            
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                String speciesName = (String)comboSpeciesName.getSelectedItem();

                model.setSpeciesName(speciesName);
                updateSuggestedPlantLines();
            }
        });

        comboLineName.addItemListener(new ItemListener() 
        {            
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                String lineName = (String)comboLineName.getSelectedItem();
                model.setLineName(lineName);
            }
        });
    }

    private void initFieldListeners()
    {
        fieldAdditionalComments.addPropertyChangeListener("value",new PropertyChangeListener() {            
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                String valNew = ((String)evt.getNewValue());
                model.setComments(valNew);;            
            }
        }); 
    }

    private void addPhaseDependentComponentsAndSetTheirValues(int oldComponentCount,int newComponentCount)
    {
        int layoutMaxRowOld = this.mainPanel.getMaxRow();

        for(int i = oldComponentCount; i< newComponentCount;i++)
        {                
            JLabel phaseLabel = new JLabel("Phase " + Integer.toString(i + 1));
            double intensityValue = model.getActinicBeamIrradianceValue(i);

            NumericalField fieldIntensity = new NumericalField("Intensity must be a non-negative number", 0);
            fieldIntensity.setValue(intensityValue);

            JSpinner spinnerIntensity =  new JSpinner(new SpinnerNumberModel(1., 0, Double.MAX_VALUE, 1.));
            spinnerIntensity.setEditor(fieldIntensity);

            phaseLabels.add(phaseLabel);
            intensitySpinners.add(spinnerIntensity);
            intensityFields.add(fieldIntensity);

            mainPanel.addComponent(phaseLabel, 0,layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(2, 0, 5, 5));
            mainPanel.addComponent(spinnerIntensity, 1, layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(2, 5, 5, 5));
        }

        mainPanel.revalidate();
        mainPanel.repaint();

        initListenersForPhaseNumberDependentComponents(oldComponentCount,  newComponentCount);
    }

    private void initListenersForPhaseNumberDependentComponents(int oldComponentCount, int newComponentCount)
    {
        for(int i = oldComponentCount; i < newComponentCount;i++)
        { 
            JSpinner spinnerIntensity = intensitySpinners.get(i);
            NumericalField fieldIntensity = intensityFields.get(i);

            final int phaseIndex = i;

            spinnerIntensity.addChangeListener(new ChangeListener() 
            {
                @Override
                public void stateChanged(ChangeEvent e) 
                {
                    double valNew = ((SpinnerNumberModel)spinnerIntensity.getModel()).getNumber().doubleValue();
                    double valOld = fieldIntensity.getValue().doubleValue();

                    if(Double.compare(valNew, valOld) != 0)
                    {
                        model.setActinicBeamIrradianceValue(valNew, phaseIndex);                
                    }            
                }
            });

            fieldIntensity.addPropertyChangeListener(NumericalField.VALUE_EDITED,new PropertyChangeListener() 
            {            
                @Override
                public void propertyChange(PropertyChangeEvent evt) 
                {
                    double valNew = ((Number)evt.getNewValue()).doubleValue();

                    model.setActinicBeamIrradianceValue(valNew, phaseIndex);            
                }
            }); 
        }

    }

    private void removePhaseDependentComponents(int oldComponentCount,int newComponentCount)
    {
        for(int i = newComponentCount; i < oldComponentCount;i++)
        {
            JLabel labelPhase = phaseLabels.get(i);
            JSpinner spinnerIntensity = intensitySpinners.get(i);

            mainPanel.remove(labelPhase);
            mainPanel.remove(spinnerIntensity);     
        }

        this.phaseLabels = phaseLabels.subList(0, newComponentCount);
        this.intensitySpinners = intensitySpinners.subList(0, newComponentCount);
        this.intensityFields = intensityFields.subList(0, newComponentCount);
    }

    private void updateSuggestedPlantLines()
    {
        String currentPlantLine = (String) comboLineName.getSelectedItem();
        List<String> suggestedPlantLines = model.getSuggestedPlantLineNames();
        String[] allPlantLines;

        if(suggestedPlantLines.contains(currentPlantLine))
        {
            allPlantLines = suggestedPlantLines.toArray(new String[] {});
        }
        else
        {
            allPlantLines = new String[suggestedPlantLines.size() + 1];
            allPlantLines[0] = currentPlantLine;
            for(int i = 0; i<suggestedPlantLines.size();i++)
            {
                allPlantLines[i + 1] = suggestedPlantLines.get(i);
            }
        }

        DefaultComboBoxModel<String> comboLinesModel = new DefaultComboBoxModel<String> (allPlantLines);
        comboLineName.setModel(comboLinesModel);
        comboLineName.setSelectedItem(currentPlantLine);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if(ExperimentDescriptionModel.SPECIES_NAME.equals(property))
        {            
            String valNew = (String)evt.getNewValue();
            String valOld = (String) comboSpeciesName.getSelectedItem();


            if(!Objects.equals(valOld, valNew))
            {
                comboSpeciesName.setSelectedItem(valNew);
            }
        }
        else if(ExperimentDescriptionModel.LINE_NAME.equals(property))
        {
            String valNew = (String)evt.getNewValue();
            String valOld = (String)comboLineName.getSelectedItem();

            if(!Objects.equals(valOld, valNew))
            {
                comboLineName.setSelectedItem(valNew);
            }
        }  
        else if(ExperimentDescriptionModel.COMMENTS.equals(property))
        {
            String valNew = (String)evt.getNewValue();
            String valOld = (String)fieldAdditionalComments.getValue();

            if(!Objects.equals(valOld, valNew))
            {
                fieldAdditionalComments.setValue(valNew);;
            }
        }  
        else if(ExperimentDescriptionModel.DARK_ADAPTED.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = boxDarkAdpted.isSelected();

            if(valOld != valNew)
            {
                boxDarkAdpted.setSelected(valNew);
            }
        }
        else if(ExperimentDescriptionModel.UNIT_TYPE.equals(property))
        {
            IrradianceUnitType unitOld = comboIrradianceUnit.getItemAt(comboIrradianceUnit.getSelectedIndex());
            IrradianceUnitType unitNew = (IrradianceUnitType)evt.getNewValue();
            if(!Objects.equals(unitOld,unitNew))
            {
                comboIrradianceUnit.setSelectedItem(unitNew);
            }
        }
        else if(ExperimentDescriptionModel.ACTINIC_BEAM_IRRADIANCE.equals(property))
        {
            IndexedPropertyChangeEvent indexedEvent = (IndexedPropertyChangeEvent)evt;

            int index = indexedEvent.getIndex();

            NumericalField field = intensityFields.get(index);
            double valOld = field.getValue().doubleValue();
            double valNew = ((Number)indexedEvent.getNewValue()).doubleValue();

            if(Double.compare(valOld, valNew) != 0)
            {
                field.setValue(valNew);
            }
            
            JSpinner spinner = intensitySpinners.get(index);
            double valSpinnerOld = ((Number)spinner.getValue()).doubleValue();
            if(Double.compare(valSpinnerOld, valNew) != 0)
            {
                spinner.setValue(valNew);
            }
        }
        else if(ExperimentDescriptionModel.ACTINIC_BEAM_PHASE_COUNT.equals(property))
        {
            int valNew = ((Number)evt.getNewValue()).intValue();
            int valOld = intensityFields.size();

            if(valNew > valOld)
            {
                addPhaseDependentComponentsAndSetTheirValues(valOld, valNew);
            }
            else if(valNew < valOld)
            {
                removePhaseDependentComponents(valOld, valNew);
            }
        }
        else if(ExperimentDescriptionModel.NECESSARY_INPUT_PROVIDED.equals(property))
        {
            boolean finishEnabled = (boolean) evt.getNewValue();
            finishAction.setEnabled(finishEnabled);
        }
    }

    private JPanel buildButtonPanel()
    {
        JPanel panelButtons = new JPanel();	

        GroupLayout layout = new GroupLayout(panelButtons);
        panelButtons.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonFinish)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonFinish)
                .addComponent(buttonCancel));

        layout.linkSize(buttonFinish,buttonCancel);

        return panelButtons;
    }

    public void finish()
    {
        setVisible(false);
    }

    public void cancel()
    {
        model.setStateFromMemento(modelState);
        setVisible(false);
    }

    private class FinishAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FinishAction()
        {			
            putValue(NAME, "Finish");
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            finish();
        }
    }

    private class CancelAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CancelAction()
        {			
            putValue(NAME, "Cancel");
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            cancel();
        }
    }
}
