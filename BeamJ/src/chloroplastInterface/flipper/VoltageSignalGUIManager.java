package chloroplastInterface.flipper;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.NumericalField;
import atomicJ.gui.SpinnerDoubleModel;
import atomicJ.gui.SubPanel;
import chloroplastInterface.SignalReceiverController;
import chloroplastInterface.StandardTimeUnit;

public class VoltageSignalGUIManager
{
    private final static String POSITION_WITH_DISPLAYED_VOLTAGE_SETTINGS = "PositionWithDisplayedVoltageSettigs";
    private final static Preferences PREF = Preferences.userNodeForPackage(VoltageSignalGUIManager.class).node(VoltageSignalGUIManager.class.getName());

    private final static String NO_SIGNAL_RECEIVER_CONTROLLER_DETECTED = "NoSignalReceiverControllerDetected";
    private final static String SIGNAL_RECEIVER_CONTROLLER_DETECTED = "SignalReceiverControllerDetected";

    private final JComboBox<SignalReceiverController> comboReceiverController;
    private final CardLayout signalReceiverControllerLayout = new CardLayout();
    private final JPanel signalReceiverControllerPanel = new JPanel(signalReceiverControllerLayout);

    private final Map<FlipperPosition, JCheckBox> sendSignalBoxes = new LinkedHashMap<>();

    private final Map<FlipperPosition, JSpinner> signalVoltageSpinners = new LinkedHashMap<>();
    private final Map<FlipperPosition, NumericalField> signalVoltageFields = new LinkedHashMap<>();

    private final Map<FlipperPosition, JSpinner> signalDurationValueSpinners = new LinkedHashMap<>();
    private final Map<FlipperPosition, NumericalField> signalDurationValueFields = new LinkedHashMap<>();

    private final Map<FlipperPosition, JComboBox<StandardTimeUnit>> signalDurationUnitCombos = new LinkedHashMap<>();

    private final Map<FlipperPosition, JSpinner> lagValueSpinners = new LinkedHashMap<>();
    private final Map<FlipperPosition, NumericalField> lagValueFields = new LinkedHashMap<>();
    private final Map<FlipperPosition, JComboBox<StandardTimeUnit>> lagUnitCombos = new LinkedHashMap<>();

    private final JComboBox<FlipperPosition> displayedVoltageSettingsCombo;
    private final SubPanel panelSignals = new SubPanel();

    private final CardLayout sendSignalBoxLayout = new CardLayout();
    private final CardLayout spinnerVoltageValueLayout = new CardLayout();
    private final CardLayout spinnerSignalDurationValueLayout = new CardLayout();
    private final CardLayout comboSignalDurationUnitLayout = new CardLayout();
    private final CardLayout spinnerLagValueLayout = new CardLayout();
    private final CardLayout comboLagUnitLayout = new CardLayout();

    private final JPanel sendSignalBoxPanel = new JPanel(sendSignalBoxLayout);
    private final JPanel spinnerVoltageValuePanel = new JPanel(spinnerVoltageValueLayout);
    private final JPanel spinnerSignalDurationValuePanel = new JPanel(spinnerSignalDurationValueLayout);
    private final JPanel comboSignalDurationUnitPanel = new JPanel(comboSignalDurationUnitLayout);
    private final JPanel spinnerLagValuePanel = new JPanel(spinnerLagValueLayout);
    private final JPanel comboLagUnitPanel = new JPanel(comboLagUnitLayout);

    private final FlipperModel model;

    public VoltageSignalGUIManager(FlipperModel model)
    {
        this.model = model;
        this.displayedVoltageSettingsCombo = new JComboBox<>(model.getFlipperPositionsThatCanBeAssociatedWithVoltageSignals().toArray(new FlipperPosition[] {}));

        List<SignalReceiverController> availableReceiverSources = model.getAvailableSignalReceiverControllers();
        this.comboReceiverController = new JComboBox<>(new DefaultComboBoxModel<>(availableReceiverSources.toArray(new SignalReceiverController[] {})));
        this.comboReceiverController.setSelectedItem(this.model.getSelectedSignalReceiverController());

        initializeSignalReceiverControllerComponent();
        String signalReceiverComponentCard = availableReceiverSources.isEmpty() ? NO_SIGNAL_RECEIVER_CONTROLLER_DETECTED : SIGNAL_RECEIVER_CONTROLLER_DETECTED;
        signalReceiverControllerLayout.show(signalReceiverControllerPanel, signalReceiverComponentCard);

        initializeModelListener();
        initializeComponents();
        initItemListeners();
        initChangeListeners();
    }

    private void initializeSignalReceiverControllerComponent()
    {
        signalReceiverControllerPanel.add(NO_SIGNAL_RECEIVER_CONTROLLER_DETECTED, new JLabel("Undetected!"));
        signalReceiverControllerPanel.add(SIGNAL_RECEIVER_CONTROLLER_DETECTED, comboReceiverController);
    }

    private void initializeModelListener()
    {
        this.model.addVoltageSignalListener(new VoltageFlipSignalListener()
        {
            @Override
            public void sendVoltageSignalAfterFlipChanged(FlipperPosition flipperPosition, boolean sendVoltageSignalAfterFlipOld,
                    boolean sendVoltageSignalAfterFlipNew)
            {
                sendSignalBoxes.get(flipperPosition).setSelected(sendVoltageSignalAfterFlipNew);
            }

            @Override
            public void voltageLagTimeValueChanged(FlipperPosition flipperPosition,
                    double flipVoltageLagTimeValueOld,
                    double flipVoltageLagTimeValueNew) 
            {
                lagValueFields.get(flipperPosition).setValue(flipVoltageLagTimeValueNew);
                lagValueSpinners.get(flipperPosition).setValue(flipVoltageLagTimeValueNew);
            }

            @Override
            public void voltageLagTimeUnitChanged(
                    FlipperPosition flipperPosition,
                    StandardTimeUnit flipVoltageLagTimeUnitOld, StandardTimeUnit flipVoltageLagTimeUnitNew) 
            {
                lagUnitCombos.get(flipperPosition).setSelectedItem(flipVoltageLagTimeUnitNew);
            }

            @Override
            public void voltageValueChanged(FlipperPosition flipperPosition,
                    double flipVoltageValueOld, double flipVoltageValueNew) 
            {
                signalVoltageFields.get(flipperPosition).setValue(flipVoltageValueNew);
                signalVoltageSpinners.get(flipperPosition).setValue(flipVoltageValueNew);                    
            }

            @Override
            public void voltageSignalDurationValueChanged(FlipperPosition flipperPosition,
                    double voltageSignalDurationValueOld, double voltageSignalDurationValueNew)
            {
                signalDurationValueFields.get(flipperPosition).setValue(voltageSignalDurationValueNew);
                signalDurationValueSpinners.get(flipperPosition).setValue(voltageSignalDurationValueNew);
            }

            @Override
            public void voltageSignalDurationTimeUnitChanged(FlipperPosition flipperPosition,
                    StandardTimeUnit voltageSignalDurationTimeUnitOld, StandardTimeUnit voltageSignalDurationTimeUnitNew) 
            {
                signalDurationUnitCombos.get(flipperPosition).setPrototypeDisplayValue(voltageSignalDurationTimeUnitOld);
            }

            @Override
            public void signalReceiverControllerChanged(SignalReceiverController signalReceiverControllerOld, SignalReceiverController signalReceiverControllerNew) 
            {
                comboReceiverController.setSelectedItem(signalReceiverControllerNew);
            }

            @Override
            public void availabilityOfFunctioningSignalReceiverControllersChange(boolean functionalSignalReceiverControllerAvailableOld,
                    boolean functioningSignalReceiverControllerAvailableNew) 
            {
                String signalResourceComponentCard = functioningSignalReceiverControllerAvailableNew ? SIGNAL_RECEIVER_CONTROLLER_DETECTED : NO_SIGNAL_RECEIVER_CONTROLLER_DETECTED;               
                signalReceiverControllerLayout.show(signalReceiverControllerPanel, signalResourceComponentCard);
            }

            @Override
            public void functioningSignalReceiverControllerRemoved(SignalReceiverController controller) 
            {
                comboReceiverController.removeItem(controller);
            }

            @Override
            public void functioningSignalReceiverControllerAdded(SignalReceiverController controllerNew)
            {
                comboReceiverController.addItem(controllerNew);                    
            }
        });
    }

    //we use multiple CardLayouts instead of one, which is ugly, but turned out to be necessary to keep all components well
    //aligned in a single panel with a GridBagLayout
    private void initializeComponents()
    {                       
        List<FlipperPosition> flipperPositions = this.model.getFlipperPositionsThatCanBeAssociatedWithVoltageSignals();

        if(flipperPositions.isEmpty()){
            return;
        }           

        int firstRow = 0;

        FlipperPosition displayedPositions = FlipperPosition.valueOf(PREF.get(POSITION_WITH_DISPLAYED_VOLTAGE_SETTINGS, flipperPositions.get(0).name()));
        displayedVoltageSettingsCombo.setSelectedItem(displayedPositions);

        JLabel labelSignalGenerator = new JLabel("Signal generator");
        panelSignals.addComponent(labelSignalGenerator, 0, firstRow, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panelSignals.addComponent(signalReceiverControllerPanel, 1, firstRow, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.1, 1);

        JLabel labelSettingsFor = new JLabel("Settings for");
        panelSignals.addComponent(labelSettingsFor, 0, firstRow + 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panelSignals.addComponent(displayedVoltageSettingsCombo, 1, firstRow + 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.1, 1);

        JLabel labelVoltage = new JLabel("Voltage");
        JLabel labelVolt = new JLabel("V");
        JLabel labelDuration = new JLabel("Signal duration");
        JLabel labelLag = new JLabel("Lag");

        panelSignals.addComponent(sendSignalBoxPanel, 1, firstRow + 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);

        panelSignals.addComponent(labelVoltage, 0, firstRow + 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panelSignals.addComponent(spinnerVoltageValuePanel, 1, firstRow + 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelSignals.addComponent(labelVolt, 2, firstRow + 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        panelSignals.addComponent(labelDuration, 0, firstRow + 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panelSignals.addComponent(spinnerSignalDurationValuePanel, 1, firstRow + 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelSignals.addComponent(comboSignalDurationUnitPanel, 2, firstRow + 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        panelSignals.addComponent(labelLag, 0, firstRow + 5, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panelSignals.addComponent(spinnerLagValuePanel, 1, firstRow + 5, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelSignals.addComponent(comboLagUnitPanel, 2, firstRow + 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        for(FlipperPosition position : flipperPositions)
        {
            boolean sendSignal = model.isSendVoltageSignalAfterFlip(position);

            JCheckBox sendSignalBox = new JCheckBox("Generate signal");
            sendSignalBox.setSelected(sendSignal);
            sendSignalBoxes.put(position, sendSignalBox);             
            sendSignalBoxPanel.add(sendSignalBox, position.name());

            double minPossibleVoltage = model.getMinPossibleVoltageValue(position);
            double maxPossibleVoltage = model.getMaxPossibleVoltageValue(position);
            double currentVoltage = model.getVoltageValue(position);

            JSpinner spinnerVoltageValue = new JSpinner(new SpinnerDoubleModel(currentVoltage, minPossibleVoltage, maxPossibleVoltage, 1.));
            NumericalField fieldVoltageValue = new NumericalField("Voltage value must be a between " + Double.toString(minPossibleVoltage)+ " and " + Double.toString(maxPossibleVoltage), minPossibleVoltage, maxPossibleVoltage);
            fieldVoltageValue.setValue(currentVoltage);            
            spinnerVoltageValue.setEditor(fieldVoltageValue);              
            spinnerVoltageValuePanel.add(spinnerVoltageValue, position.name());

            signalVoltageSpinners.put(position, spinnerVoltageValue);
            signalVoltageFields.put(position, fieldVoltageValue);

            double signalDurationValue = model.getVoltageSignalDurationValue(position);

            JSpinner spinnerSignalDurationValue = new JSpinner(new SpinnerDoubleModel(signalDurationValue, 0, 100000, 1.));
            NumericalField fieldSignalDurationValue = new NumericalField("Time interval must be a non-negative number", 0);
            fieldSignalDurationValue.setValue(signalDurationValue);
            spinnerSignalDurationValue.setEditor(fieldSignalDurationValue);

            spinnerSignalDurationValuePanel.add(spinnerSignalDurationValue, position.name());

            signalDurationValueSpinners.put(position, spinnerSignalDurationValue);   
            signalDurationValueFields.put(position, fieldSignalDurationValue);

            StandardTimeUnit signalDurationUnit = model.getVoltageSignalDurationUnit(position);               
            JComboBox<StandardTimeUnit> comboSignalDurationUnit = new JComboBox<>(new StandardTimeUnit[] {StandardTimeUnit.SECOND,StandardTimeUnit.MINUTE,StandardTimeUnit.HOUR});
            comboSignalDurationUnit.setSelectedItem(signalDurationUnit);
            signalDurationUnitCombos.put(position, comboSignalDurationUnit);               
            comboSignalDurationUnitPanel.add(comboSignalDurationUnit, position.name());

            double lagValue = model.getVoltageSignalLagValue(position);

            JSpinner spinnerLagValue = new JSpinner(new SpinnerDoubleModel(lagValue, 0, 100000, 1.));
            NumericalField fieldLagValue = new NumericalField("Time interval must be a non-negative number", 0);
            fieldLagValue.setValue(lagValue);
            spinnerLagValue.setEditor(fieldLagValue);

            spinnerLagValuePanel.add(spinnerLagValue, position.name());

            lagValueSpinners.put(position, spinnerLagValue);
            lagValueFields.put(position, fieldLagValue);

            StandardTimeUnit lagUnit = model.getVoltageSignalLagUnit(position);              
            JComboBox<StandardTimeUnit> comboLagUnit = new JComboBox<>(new StandardTimeUnit[] {StandardTimeUnit.SECOND,StandardTimeUnit.MINUTE,StandardTimeUnit.HOUR});
            comboLagUnit.setSelectedItem(lagUnit);
            lagUnitCombos.put(position, comboLagUnit);

            comboLagUnitPanel.add(comboLagUnit, position.name());
        }   
    }

    public JPanel getSettingsPanel()
    {
        return panelSignals;
    }

    private void initItemListeners()
    {   
        List<FlipperPosition> flipperPositions = this.model.getFlipperPositionsThatCanBeAssociatedWithVoltageSignals();

        for(FlipperPosition position: flipperPositions)
        {
            sendSignalBoxes.get(position).addItemListener(new ItemListener() 
            {         
                @Override
                public void itemStateChanged(ItemEvent evt) 
                {                
                    boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                    model.setSendVoltageSignalAfterFlip(position, selected);;               
                }
            });

            JComboBox<StandardTimeUnit> signalDurationUnitCombo = signalDurationUnitCombos.get(position);
            signalDurationUnitCombo.addItemListener(new ItemListener() 
            {           
                @Override
                public void itemStateChanged(ItemEvent e) {
                    StandardTimeUnit signalDurationlUnit = signalDurationUnitCombo.getItemAt(signalDurationUnitCombo.getSelectedIndex());
                    model.setVoltageSignalDurationUnit(position, signalDurationlUnit);
                }
            });

            JComboBox<StandardTimeUnit> lagUnitCombo = lagUnitCombos.get(position);
            lagUnitCombo.addItemListener(new ItemListener()
            {                    
                @Override
                public void itemStateChanged(ItemEvent e) {
                    StandardTimeUnit lagUnit = lagUnitCombo.getItemAt(lagUnitCombo.getSelectedIndex());
                    model.setVoltageSignalLagUnit(position, lagUnit);
                }
            });        
        }

        displayedVoltageSettingsCombo.addItemListener(new ItemListener()
        {                  
            @Override
            public void itemStateChanged(ItemEvent e) 
            {                     
                FlipperPosition currentPosition = displayedVoltageSettingsCombo.getItemAt(displayedVoltageSettingsCombo.getSelectedIndex());

                sendSignalBoxLayout.show(sendSignalBoxPanel, currentPosition.name());
                spinnerVoltageValueLayout.show(spinnerVoltageValuePanel, currentPosition.name());
                spinnerSignalDurationValueLayout.show(spinnerSignalDurationValuePanel, currentPosition.name());
                comboSignalDurationUnitLayout.show(comboSignalDurationUnitPanel, currentPosition.name());
                spinnerLagValueLayout.show(spinnerLagValuePanel, currentPosition.name());
                comboLagUnitLayout.show(comboLagUnitPanel, currentPosition.name());

                PREF.put(POSITION_WITH_DISPLAYED_VOLTAGE_SETTINGS, currentPosition.name());                     
                flushPreferences();
            }
        });

        comboReceiverController.addItemListener(new ItemListener() 
        {               
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                SignalReceiverController signalController = comboReceiverController.getItemAt(comboReceiverController.getSelectedIndex());                    
                model.selectSignalReceiverController(signalController);
            }
        });
    }

    private void flushPreferences()
    {
        try {
            PREF.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    private void initChangeListeners()
    {
        List<FlipperPosition> flipperPositions = this.model.getFlipperPositionsThatCanBeAssociatedWithVoltageSignals();

        for(FlipperPosition position: flipperPositions)
        {
            JSpinner signalVoltageSpinner = signalVoltageSpinners.get(position);
            signalVoltageSpinner.addChangeListener(new ChangeListener()
            {                   
                @Override
                public void stateChanged(ChangeEvent e) 
                {
                    double valNew = ((SpinnerDoubleModel)signalVoltageSpinner.getModel()).getDoubleValue();
                    model.setVoltageSignalValue(position, valNew);
                }
            });

            JSpinner signalDurationValueSpinner = signalDurationValueSpinners.get(position);
            signalDurationValueSpinner.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) 
                {
                    double valNew = ((SpinnerDoubleModel)signalDurationValueSpinner.getModel()).getDoubleValue();
                    model.setVoltageSignalDurationValue(position, valNew);
                }
            });

            JSpinner lagValueSpinner = lagValueSpinners.get(position);
            lagValueSpinner.addChangeListener(new ChangeListener() 
            {
                @Override
                public void stateChanged(ChangeEvent e)
                {
                    double valNew = ((SpinnerDoubleModel)lagValueSpinner.getModel()).getDoubleValue();
                    model.setVoltageSignalLagValue(position, valNew);
                }
            });

            NumericalField signalVoltageField = signalVoltageFields.get(position);
            signalVoltageField.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener()
            {            
                @Override
                public void propertyChange(PropertyChangeEvent evt) 
                {
                    double valNew = ((Number)evt.getNewValue()).doubleValue();
                    model.setVoltageSignalValue(position, valNew);
                }
            }); 

            NumericalField signalDurationValueField = signalDurationValueFields.get(position);
            signalDurationValueField.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener() 
            {                   
                @Override
                public void propertyChange(PropertyChangeEvent evt) 
                {
                    double valNew = ((Number)evt.getNewValue()).doubleValue();
                    model.setVoltageSignalDurationValue(position, valNew);
                }
            });

            NumericalField lagValueField = lagValueFields.get(position);
            lagValueField.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener() 
            {                   
                @Override
                public void propertyChange(PropertyChangeEvent evt) 
                {
                    double valNew = ((Number)evt.getNewValue()).doubleValue();
                    model.setVoltageSignalLagValue(position, valNew);
                }
            });
        }
    }
}