package chloroplastInterface;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.ExtensionFileChooser;
import atomicJ.gui.JSpinnerNumeric;
import atomicJ.gui.SpinnerDoubleModel;
import atomicJ.gui.SubPanel;
import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.LinkUtilities;
import chloroplastInterface.CalibrationSwingWorker.CalibrationPhase;
import chloroplastInterface.MainFrame.LinkMouseListener;
import chloroplastInterface.flipper.FlliperGUIManager;
import chloroplastInterface.optics.SliderMountedFilter;

public class RecordingSettingsGUI
{
    private static final Preferences CURRENT_PHOTOMETRIC_CURVE_PREF = Preferences.userNodeForPackage(RecordingSettingsGUI.class).node(RecordingSettingsGUI.class.getName());
    private static final Preferences CURRENT_ACTINIC_BEAM_CALIBRATION_CURVE_PREF = Preferences.userNodeForPackage(RecordingSettingsGUI.class).node("ActinicCalibration");

    private final static String NO_SIGNAL_CONTROLLER_DETECTED = "NoTransmittanceSourceDetected";
    private final static String SIGNAL_CONTROLLER_DETECTED = "TransmittanceSourceDetected";

    private final static String NO_MEASURING_BEAM_CONTROLLER_DETECTED = "NoMeasuringBeamControllerDetected";
    private final static String MEASURING_BEAM_CONTROLLER_DETECTED = "MeasuringBeamControllerDetected";

    private final static double PHASE_DURATION_DEFAULT_MINIMUM = 0;
    private final static double PHASE_DURATION_DEFAULT_MAXIMUM = 100000;

    private final static String PROGRESS_NOT_DISPLAYED_CARD = "ProgressNotDisplayedCard";
    private final static String PROGRESS_BAR_CARD = "ProgressBarCard";

    private static final String TEXT_CITATION = "<html>When JPhotometer contributes to a published work, please cite <br> " +
            "<u>available here</u>). Thank you!</html>";

    private final JLabel labelActinicBeamController = new JLabel();

    private final SubPanel measuringBeamPanel = new SubPanel();
    private final JComboBox<String> comboMeasuringBeamController;
    private final JPanel measuringBeamControllerInnerPanel;
    private final CardLayout measuringBeamControllerLayout = new CardLayout();

    private final JCheckBox boxKeepMeasuringBeamOnWhenIdle = new JCheckBox("Keep on when idle");

    private final JSpinner spinnerMeasuringBeamFrequency;
    private final JSpinner spinnerMeasuringBeamIntensityInPercents;

    private final JSpinner spinnerSignalSourcesCount;
    private List<JLabel> signalSourceLabels = new ArrayList<>();
    private List<JLabel> samplesPerMinuteLabels = new ArrayList<>();
    private List<JComboBox<SignalSourceController>> signalControllerCombos = new ArrayList<>();
    private List<CardLayout> signalControllerLayouts = new ArrayList<>();
    private List<JPanel> signalControllerPanels = new ArrayList<>();
    private List<JSpinner> signalSamplesPerMinuteSpinners = new ArrayList<>();
    private List<JLabel> signalTypeLabels = new ArrayList<>();
    private List<JComboBox<LightSignalType>> signalTypeCombos = new ArrayList<>();

    private final JComboBox<SignalIndexItem> comboSignalWithDisplayedCalibration;

    private final SimpleDateFormat calibrationDateFormat = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");
    private final SimpleDateFormat phaseEndDateFormat = new SimpleDateFormat("HH:mm:ss");
    private final NumberFormat calibrationValueFormat = NumberFormat.getInstance(Locale.US);
    private final NumberFormat absoluteLightIntensityFormat = NumberFormat.getInstance(Locale.US);

    private final CardLayout calibrationSignalChoiceLayout = new CardLayout();
    private final JPanel calibrationCardsForSignalsPanel = new JPanel(calibrationSignalChoiceLayout);

    private List<JLabel> latestCalibrationTimeLabels = new ArrayList<>();
    private List<JLabel> latestCalibrationSlopeValueLabels = new ArrayList<>();
    private List<JLabel> latestCalibrationOffsetLabels = new ArrayList<>();

    private List<JProgressBar> calibrationProgressBars = new ArrayList<>();//new JProgressBar(0, 100);
    private List<CardLayout> calibrationProgressBarLayouts = new ArrayList<>();
    private List<JPanel> calibrationProgressPanels = new ArrayList<>();
    private List<JLabel> calibrationPhaseLabels = new ArrayList<>();
    private List<JPanel> calibrationPanels = new ArrayList<>();

    private final JSpinner spinnerPhaseCount;

    private List<JLabel> phaseLabels = new ArrayList<>();
    private List<JSpinner> phaseDurationSpinners = new ArrayList<>();
    private List<JSpinner> phaseIntensitySpinners = new ArrayList<>();
    private List<JComboBox<StandardTimeUnit>> timeUnitComboBoxes = new ArrayList<>();
    private List<JComboBox<SliderMountedFilter>> filterComboBoxes = new ArrayList<>();
    private List<JLabel> endTimeLabels = new ArrayList<>();
    private List<JLabel> absoluteIntensityLabels = new ArrayList<>();

    private final JTextField fieldActinicCalibrationFile = new JTextField();

    private final SubPanel actinicBeamPhasesPanel = new SubPanel();

    private final JCheckBox boxOverlayNewPlotOnPrevious = new JCheckBox("Overlay new plot on previous");

    private final ExtensionFileChooser fileChooserOutput = new ExtensionFileChooser(CURRENT_PHOTOMETRIC_CURVE_PREF, true);
    private final ExtensionFileChooser fileChooserActinicCalibration = new ExtensionFileChooser(CURRENT_ACTINIC_BEAM_CALIBRATION_CURVE_PREF, true);

    private final Action readPhasesFromFile = new ReadPhasesFromFileAction();
    private final Action readActinicBeamCalibrationFromFileAction = new ReadActinicBeamCalibrationFromFileAction();

    private final Action runAction = new RunAction();
    private final Action stopAction = new StopAction();
    private final Action resumeAction = new ResumeAction();
    private final Action cancelAction = new CancelAction();
    private List<Action> calibrateActions = new ArrayList<>();

    private final CurvePlotGUI guiPlot;
    private final JPanel mainPanel = new JPanel(); 

    private final RecordingModel model;

    public RecordingSettingsGUI(RecordingModel model)
    {
        this.model = model;

        this.guiPlot = new CurvePlotGUI(model);
        this.calibrationValueFormat.setMaximumFractionDigits(2);
        this.calibrationValueFormat.setMinimumFractionDigits(2);

        this.spinnerSignalSourcesCount = new JSpinner(new SpinnerNumberModel(model.getSignalSourcesCount(), 1, this.model.getMaximalUsedSignalSourcesCount(), 1));
        this.spinnerPhaseCount = new JSpinner(new SpinnerNumberModel(model.getActinicBeamPhaseCount(), 1, this.model.getMaximumAllowedActinicBeamPhaseCount(), 1));

        SpinnerModel spinnerMeasuringBeamFrequencyModel = buildModelForMeasuringBeamFrequencySpinner(model.getPreferredFrequencyIncrement(), model.getPreferredFrequencyDecrement());
        this.spinnerMeasuringBeamFrequency = new JSpinner(spinnerMeasuringBeamFrequencyModel);

        //for unknown to me reasons JSpinner editor's field is not editable when the custom spinnerModel is used
        JComponent spinnerEditor = spinnerMeasuringBeamFrequency.getEditor();
        if(spinnerEditor instanceof DefaultEditor)
        {
            ((DefaultEditor) spinnerEditor).getTextField().setEditable(true);
        }

        this.spinnerMeasuringBeamIntensityInPercents = new JSpinner(new SpinnerDoubleModel(model.getMeasuringLightIntensityInPercent(), 0., 100., 1.)); 

        this.boxKeepMeasuringBeamOnWhenIdle.setSelected(model.isKeepMeasuringBeamOnWhenIdle());

        List<String> availableMeasuringBeamControllers = model.getDescriptionsOfAvailableMeasuringBeamControllers();
        this.comboMeasuringBeamController = new JComboBox<>(new DefaultComboBoxModel<>(availableMeasuringBeamControllers.toArray(new String[] {})));
        this.comboMeasuringBeamController.setSelectedItem(this.model.getMeasuringBeamControllerDescription());

        this.measuringBeamControllerInnerPanel = buildMeasuringBeamControllerPanel();
        String measuringBeamComponentCard = availableMeasuringBeamControllers.isEmpty() ? NO_MEASURING_BEAM_CONTROLLER_DETECTED : MEASURING_BEAM_CONTROLLER_DETECTED;
        this.measuringBeamControllerLayout.show(measuringBeamControllerInnerPanel, measuringBeamComponentCard);


        JComponent actinicBeamSettingsPanel = buildActinicBeamSettingsPanel();
        JPanel measuringBeamSettingsPanel = buildMeasuringBeamSettingsPanel();
        int signalControllerCount = this.model.getSignalSourcesCount();
        addSignalCountDependentSignalPropertiesComponents(0, signalControllerCount);
        addListenersToSignalCountDependentSignalPropertiesComponents(0, signalControllerCount);
        JPanel actuatorsPanel = buildActuatorsPanel();       

        int signalCount = model.getSignalSourcesCount();
        this.comboSignalWithDisplayedCalibration = new JComboBox<>(new DefaultComboBoxModel<>(SignalIndexItem.buildSignalIndexArray(signalCount)));

        JPanel calibrationMainPanel = new JPanel(new BorderLayout());
        SubPanel calibrationDisplayedSignalPanel = new SubPanel();
        calibrationDisplayedSignalPanel.addComponent(new JLabel("Signal to calibrate"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1,new Insets(15, 5, 0, 5));
        calibrationDisplayedSignalPanel.addComponent(comboSignalWithDisplayedCalibration, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(15, 5, 0, 5));           

        calibrationMainPanel.add(calibrationDisplayedSignalPanel, BorderLayout.NORTH);

        JPanel calibrationSignalCardsPanel = buildCalibrationCardsPanel();
        calibrationMainPanel.add(calibrationSignalCardsPanel, BorderLayout.SOUTH);

        JPanel basicSettingsPanel = new JPanel(new BorderLayout());
        basicSettingsPanel.add(actinicBeamSettingsPanel, BorderLayout.NORTH);
        basicSettingsPanel.add(calibrationMainPanel, BorderLayout.SOUTH);

        JPanel plotSettingsPanel = buildPlotSettings();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Basic settings", basicSettingsPanel);
        tabbedPane.addTab("Measuring beam", measuringBeamSettingsPanel);
        tabbedPane.addTab("Actuators", actuatorsPanel);
        tabbedPane.addTab("Plot settings", plotSettingsPanel);

        JPanel buttonPanel = buildButtonPanel();

        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.add(tabbedPane, BorderLayout.NORTH);
        outerPanel.add(buttonPanel,BorderLayout.SOUTH);

        mainPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        mainPanel.setLayout(new BorderLayout());

        JPanel plotPanelOuter = new JPanel(new BorderLayout());
        plotPanelOuter.add(guiPlot.getPlotAndDescriptionPanel(), BorderLayout.CENTER);
        plotPanelOuter.add(buildPanelCitation(), BorderLayout.SOUTH);

        mainPanel.add(plotPanelOuter, BorderLayout.CENTER);
        mainPanel.add(outerPanel, BorderLayout.EAST);

        fileChooserOutput.setApproveButtonMnemonic('S');  
        fileChooserOutput.setDialogType(JFileChooser.OPEN_DIALOG);//order of calls of setDialogType and setApprove button text is important
        fileChooserOutput.setApproveButtonText("Select");
        fileChooserOutput.addChoosableFileFilters(model.getFileFiltersForReadingActinicBeamPhases());

        File actinicCalibrationFile = model.getActinicBeamCalibrationFile();
        String filePath = (actinicCalibrationFile != null) ? actinicCalibrationFile.getPath() : "";

        fieldActinicCalibrationFile.setBorder(BorderFactory.createEmptyBorder());
        fieldActinicCalibrationFile.setText(filePath);
        fieldActinicCalibrationFile.setEnabled(false);

        fileChooserActinicCalibration.addChoosableFileFilters(model.getFileFiltersForReadingActinicBeamCalibrationFile());

        initComponentEnabledState();
        setEnabledFinishedPhaseCountDependentComponents(false, 0, model.getFinishedActinicBeamPhaseCount());
        setEnabledFinishedPhaseCountDependentComponents(true, model.getFinishedActinicBeamPhaseCount(), model.getActinicBeamPhaseCount());

        initModelListener();
    }

    public JPanel getBeamSettingsPanel()
    {
        return mainPanel;
    }

    private void initComponentEnabledState()
    {
        boxKeepMeasuringBeamOnWhenIdle.setEnabled(model.isMeasuringBeamIdleStateModificationEnabled());

        spinnerMeasuringBeamFrequency.setEnabled(model.isMeasuringBeamFrequencyModificationEnabled());
        spinnerMeasuringBeamIntensityInPercents.setEnabled(model.isMeasuringBeamIntensityModificationEnabled());

        readPhasesFromFile.setEnabled(model.isReadActinicBeamPhasesFromFileEnabled());
        runAction.setEnabled(model.isRunEnabled());
        stopAction.setEnabled(model.isStopEnabled());
        resumeAction.setEnabled(model.isResumeEnabled());
        cancelAction.setEnabled(model.isCancelEnabled());
    }

    private void setEnablednessConsistentWithCurrentPhase(int currentPhaseOld, int currentPhaseNew)
    {
        if(currentPhaseOld >= 0)
        {
            phaseLabels.get(currentPhaseOld).setForeground(Color.BLACK);
            JSpinner durationSpinner = phaseDurationSpinners.get(currentPhaseOld);
            double duration = ((Number)durationSpinner.getValue()).doubleValue();

            durationSpinner.setModel(new SpinnerDoubleModel(duration, PHASE_DURATION_DEFAULT_MINIMUM, PHASE_DURATION_DEFAULT_MAXIMUM, 1.0));

            JComboBox<StandardTimeUnit> comboDurationUnit = timeUnitComboBoxes.get(currentPhaseOld);
            StandardTimeUnit currentDurationTimeUnit = (StandardTimeUnit)comboDurationUnit.getSelectedItem();
            comboDurationUnit.setModel(new DefaultComboBoxModel<>(StandardTimeUnit.values()));
            comboDurationUnit.setSelectedItem(currentDurationTimeUnit);
        }

        if(currentPhaseNew >= 0)
        {
            int currentPhaseCount = ((Number)spinnerPhaseCount.getValue()).intValue();
            spinnerPhaseCount.setModel(new SpinnerNumberModel(currentPhaseCount, currentPhaseNew + 1, PHASE_DURATION_DEFAULT_MAXIMUM, 1));

            phaseLabels.get(currentPhaseNew).setForeground(Color.RED);

            //makes it impossible to shorten current phase, it can only be prolonged
            JSpinner currentPhaseDurationSpinner = phaseDurationSpinners.get(currentPhaseNew);
            double currentDuration = ((Number)currentPhaseDurationSpinner.getValue()).doubleValue();
            currentPhaseDurationSpinner.setModel(new SpinnerDoubleModel(currentDuration, currentDuration, PHASE_DURATION_DEFAULT_MAXIMUM, 1.0));

            JComboBox<StandardTimeUnit> comboDurationUnit = timeUnitComboBoxes.get(currentPhaseNew);
            StandardTimeUnit currentDurationTimeUnit = (StandardTimeUnit)comboDurationUnit.getSelectedItem();
            comboDurationUnit.setModel(new DefaultComboBoxModel<>(currentDurationTimeUnit.getLargerOrEqualUnits()));
            comboDurationUnit.setSelectedItem(currentDurationTimeUnit);

            phaseIntensitySpinners.get(currentPhaseNew).setEnabled(false);
            filterComboBoxes.get(currentPhaseNew).setEnabled(false);
        }  
        else
        {
            int currentPhaseCount = ((Number)spinnerPhaseCount.getValue()).intValue();
            spinnerPhaseCount.setModel(new SpinnerNumberModel(currentPhaseCount, 1, model.getMaximumAllowedActinicBeamPhaseCount(), 1));       
        }
    }

    //from - inclusive, to - exclusive
    private void setEnabledFinishedPhaseCountDependentComponents(boolean enabled, int from, int to)
    {
        for(int i = from; i<to; i++)
        {
            phaseLabels.get(i).setEnabled(enabled);
            phaseDurationSpinners.get(i).setEnabled(enabled);
            phaseIntensitySpinners.get(i).setEnabled(enabled);
            timeUnitComboBoxes.get(i).setEnabled(enabled);
            filterComboBoxes.get(i).setEnabled(enabled);
        }
    }

    private JComponent buildActinicBeamSettingsPanel()
    {         
        String actinicBeamPortDescription = model.getActinicBeamControllerDescription();            
        labelActinicBeamController.setText(actinicBeamPortDescription);

        JLabel absoluteIntensityLabel = new JLabel("<html><div style='text-align: center;'>Intensity<br/>(abs)</div></html>");

        actinicBeamPhasesPanel.addComponent(new JLabel("Beam controller"), 0, 0, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, 1, 1,new Insets(0, 5, 15, 0));
        actinicBeamPhasesPanel.addComponent(labelActinicBeamController, 1, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 15, 5));           

        actinicBeamPhasesPanel.addComponent(new JLabel("Phase count"), 0, 1, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, 1, 1);
        actinicBeamPhasesPanel.addComponent(spinnerPhaseCount, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 15, 5));           
        actinicBeamPhasesPanel.addComponent(new JLabel("Intensity (%)"), 3, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE, 1, 1);
        actinicBeamPhasesPanel.addComponent(new JLabel("Filter"), 4, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE, 1, 1);
        actinicBeamPhasesPanel.addComponent(new JLabel("Phase end"), 5, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE, 1, 1);
        actinicBeamPhasesPanel.addComponent(absoluteIntensityLabel, 6, 0, 1, 2, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, 1, 1);

        initListenersForActinicBeamPanelPermanentComponents();

        int phaseCount = model.getActinicBeamPhaseCount();
        addActinicBeamPhaseCountDependentComponents(0, phaseCount);

        actinicBeamPhasesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.add(actinicBeamPhasesPanel, BorderLayout.NORTH);
        outerPanel.add(buildReadPhasesFromFilePanel(), BorderLayout.SOUTH);

        JScrollPane scrollFrame = new JScrollPane(outerPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollFrame.setAutoscrolls(true);
        scrollFrame.setPreferredSize(new Dimension(555,440));      

        return scrollFrame;
    }

    private void addActinicBeamPhaseCountDependentComponents(int oldComponentCount, int newComponentCount)
    {
        int layoutMaxRowOld = actinicBeamPhasesPanel.getMaxRow();

        for(int i = oldComponentCount; i < newComponentCount;i++)
        {                
            double duration = model.getPhaseDuration(i);
            StandardTimeUnit timeUnit = model.getPhaseDurationUnit(i);
            double lightIntensity = model.getActinicBeamIntensityInPercent(i);
            SliderMountedFilter filter = model.getActinicBeamFilter(i);

            JLabel phaseLabel = new JLabel("Phase " + Integer.toString(i + 1));
            JSpinnerNumeric spinnerDuration = new JSpinnerNumeric(new SpinnerDoubleModel("Duration should be a non-negative number",duration, PHASE_DURATION_DEFAULT_MINIMUM, PHASE_DURATION_DEFAULT_MAXIMUM, 1.0));
            JComboBox<StandardTimeUnit> comboBoxTimeUnit = new JComboBox<>(StandardTimeUnit.values());
            comboBoxTimeUnit.setSelectedItem(timeUnit);

            JComboBox<SliderMountedFilter> comboBoxFilter = new JComboBox<>(this.model.getActinicBeamSliderMountedFilters().toArray(new SliderMountedFilter[] {}));
            comboBoxFilter.setSelectedItem(filter);

            JSpinnerNumeric spinnerIntensityInPercent = new JSpinnerNumeric(new SpinnerDoubleModel("Intensity should be a number between 0 and 100", lightIntensity, 0, 100, 1.0));

            Date endTime = model.getExpectedPhaseEndTime(i);
            String endTimeFormatted = (endTime == null) ? "": phaseEndDateFormat.format(endTime);
            JLabel endTimeLabel = new JLabel(endTimeFormatted,SwingConstants.CENTER);

            double absoluteIntensity = model.getAbsoluteIntensityOfActinicLight(i);
            String absoluteIntensityDescription = !Double.isNaN(absoluteIntensity) ? absoluteLightIntensityFormat.format(absoluteIntensity) :"";
            JLabel absoluteIntensityLabel = new JLabel(absoluteIntensityDescription, SwingConstants.CENTER);

            phaseLabels.add(phaseLabel);
            phaseDurationSpinners.add(spinnerDuration);
            phaseIntensitySpinners.add(spinnerIntensityInPercent);
            timeUnitComboBoxes.add(comboBoxTimeUnit);
            filterComboBoxes.add(comboBoxFilter);
            endTimeLabels.add(endTimeLabel);
            absoluteIntensityLabels.add(absoluteIntensityLabel);

            actinicBeamPhasesPanel.addComponent(phaseLabel, 0,layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(0, 0, 5, 5));
            actinicBeamPhasesPanel.addComponent(spinnerDuration, 1, layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 5));
            actinicBeamPhasesPanel.addComponent(comboBoxTimeUnit, 2, layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 0, 5, 5));

            actinicBeamPhasesPanel.addComponent(spinnerIntensityInPercent, 3,layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 5));      
            actinicBeamPhasesPanel.addComponent(comboBoxFilter, 4,layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 5, 5));      

            actinicBeamPhasesPanel.addComponent(endTimeLabel, 5, layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 5));
            actinicBeamPhasesPanel.addComponent(absoluteIntensityLabel, 6, layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 5));
        }

        initListenersForActinicBeamPhaseCountDependentComponents(oldComponentCount,  newComponentCount);

        actinicBeamPhasesPanel.revalidate();
        actinicBeamPhasesPanel.repaint();
    }

    private void removeActinicBeamPhaseCountDependentComponents(int oldComponentCount, int newComponentCount)
    {
        if(newComponentCount >= oldComponentCount)
        {
            return;
        }

        for(int i = newComponentCount; i < oldComponentCount;i++)
        {
            JLabel labelPhase = phaseLabels.get(i);
            JSpinner spinnerDuration = phaseDurationSpinners.get(i);
            JSpinner spinnerIntensityInPercent = phaseIntensitySpinners.get(i);
            JComboBox<StandardTimeUnit> comboBoxTimeUnit = timeUnitComboBoxes.get(i);
            JComboBox<SliderMountedFilter> comboBoxFilter = filterComboBoxes.get(i);
            JLabel endTimeLabel = endTimeLabels.get(i);
            JLabel absoluteIntensityLabel = absoluteIntensityLabels.get(i);

            actinicBeamPhasesPanel.remove(labelPhase);
            actinicBeamPhasesPanel.remove(spinnerDuration);
            actinicBeamPhasesPanel.remove(comboBoxTimeUnit);
            actinicBeamPhasesPanel.remove(comboBoxFilter);
            actinicBeamPhasesPanel.remove(spinnerIntensityInPercent);            
            actinicBeamPhasesPanel.remove(endTimeLabel);
            actinicBeamPhasesPanel.remove(absoluteIntensityLabel);
        }

        this.phaseLabels = phaseLabels.subList(0, newComponentCount);
        this.phaseDurationSpinners = phaseDurationSpinners.subList(0, newComponentCount);
        this.phaseIntensitySpinners = phaseIntensitySpinners.subList(0, newComponentCount);
        this.timeUnitComboBoxes = timeUnitComboBoxes.subList(0, newComponentCount);
        this.filterComboBoxes = filterComboBoxes.subList(0, newComponentCount);
        this.endTimeLabels = endTimeLabels.subList(0, newComponentCount);
        this.absoluteIntensityLabels = absoluteIntensityLabels.subList(0, newComponentCount);

        actinicBeamPhasesPanel.revalidate();
        actinicBeamPhasesPanel.repaint();
    }

    //from - inclusive, to - exclusive
    private void initListenersForActinicBeamPhaseCountDependentComponents(int from, int to)
    {
        for(int i = from; i< to; i++)
        {
            JSpinner phaseDurationSpinner = phaseDurationSpinners.get(i);
            final int phaseIndex = i;

            phaseDurationSpinner.addChangeListener(new ChangeListener() 
            {          
                @Override
                public void stateChanged(ChangeEvent evt)
                {
                    double phaseDurationNew = ((Number)phaseDurationSpinner.getValue()).doubleValue();
                    model.setPhaseDuration(phaseIndex, phaseDurationNew);                       
                }
            });
        }

        for(int i = from; i < to; i++)
        {
            JSpinner phaseIntensitySpinner = phaseIntensitySpinners.get(i);
            final int phaseIndex = i;

            phaseIntensitySpinner.addChangeListener(new ChangeListener() 
            {          
                @Override
                public void stateChanged(ChangeEvent evt)
                {
                    double phaseIntensityNew = ((Number)phaseIntensitySpinner.getValue()).doubleValue();
                    model.setActinicBeamIntensityInPercent(phaseIndex, phaseIntensityNew);
                }
            });
        }

        for(int i = from; i < to; i++)
        {
            JComboBox<StandardTimeUnit> comboBox = timeUnitComboBoxes.get(i);
            final int phaseIndex = i;

            comboBox.addItemListener(new ItemListener() 
            {                    
                @Override
                public void itemStateChanged(ItemEvent e) {
                    StandardTimeUnit unitNew = comboBox.getItemAt(comboBox.getSelectedIndex());
                    model.setPhaseDurationUnit(phaseIndex, unitNew);
                }
            });
        }

        for(int i = from; i < to; i++)
        {
            JComboBox<SliderMountedFilter> comboBox = filterComboBoxes.get(i);
            final int phaseIndex = i;

            comboBox.addItemListener(new ItemListener() 
            {                    
                @Override
                public void itemStateChanged(ItemEvent e) {
                    SliderMountedFilter filterNew = comboBox.getItemAt(comboBox.getSelectedIndex());
                    model.setActinicBeamFilter(phaseIndex, filterNew);
                }
            });
        }
    }

    private void initListenersForActinicBeamPanelPermanentComponents()
    {
        spinnerPhaseCount.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int phaseCountNew = ((SpinnerNumberModel)spinnerPhaseCount.getModel()).getNumber().intValue();
                model.setPhaseCount(phaseCountNew);
            }
        });

        spinnerSignalSourcesCount.addChangeListener(new ChangeListener() 
        {            
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                int signalCountNew = ((SpinnerNumberModel)spinnerSignalSourcesCount.getModel()).getNumber().intValue();
                model.setSignalSourcesCount(signalCountNew);
            }
        });
    }

    private JPanel buildMeasuringBeamSettingsPanel()
    {           
        measuringBeamPanel.addComponent(new JLabel("Beam Controller"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1,new Insets(0, 5, 15, 0));
        measuringBeamPanel.addComponent(measuringBeamControllerInnerPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 15, 5));           

        measuringBeamPanel.addComponent(boxKeepMeasuringBeamOnWhenIdle, 1, 1, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 10, 5));

        measuringBeamPanel.addComponent(new JLabel("Frequency (Hz)"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 15, 5));
        measuringBeamPanel.addComponent(spinnerMeasuringBeamFrequency, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 15, 5));           

        measuringBeamPanel.addComponent(new JLabel("Intensity (%)"), 2, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 15, 5));
        measuringBeamPanel.addComponent(spinnerMeasuringBeamIntensityInPercents, 3, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(5, 0, 15, 0));           

        measuringBeamPanel.addComponent(new JLabel("Number of signals"), 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1,new Insets(5, 5, 15, 5));
        measuringBeamPanel.addComponent(spinnerSignalSourcesCount, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1,new Insets(5, 5, 15, 5));

        initListenersForMeasuringBeamComponents();

        JPanel outerPanel = new JPanel(new BorderLayout());

        outerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        outerPanel.add(measuringBeamPanel, BorderLayout.NORTH);

        JPanel outermostPanel = new JPanel(new BorderLayout());
        outermostPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        outermostPanel.add(outerPanel, BorderLayout.WEST);

        return outermostPanel;
    }

    private JPanel buildMeasuringBeamControllerPanel()
    {
        JPanel panel = new JPanel(measuringBeamControllerLayout);
        panel.add(new JLabel("Undetected!"), NO_MEASURING_BEAM_CONTROLLER_DETECTED);
        panel.add(comboMeasuringBeamController, MEASURING_BEAM_CONTROLLER_DETECTED);

        return panel;
    }

    private SpinnerModel buildModelForMeasuringBeamFrequencySpinner(double preferredIncrement, double preferredDecrement)
    {
        SpinnerModel frequencySpinnerModel = new SpinnerNumberModelWithTwoIncrements(model.getMeasuringBeamFrequencyInHertz(), 0., model.getMaximalAllowedMeasuringBeamFrequencyInHertz(), preferredIncrement, preferredDecrement);
        return frequencySpinnerModel;
    }

    private void initListenersForMeasuringBeamComponents()
    {
        spinnerMeasuringBeamFrequency.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                double frequencyNew = ((Number)spinnerMeasuringBeamFrequency.getValue()).doubleValue();
                model.setDesiredMeasuringLightFrequencyInHertz(frequencyNew);;
            }
        });

        spinnerMeasuringBeamIntensityInPercents.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                double intensityNew = ((Number)spinnerMeasuringBeamIntensityInPercents.getValue()).doubleValue();
                model.setMeasuringBeamIntensityInPercent(intensityNew);;
            }
        });

        boxKeepMeasuringBeamOnWhenIdle.addItemListener(new ItemListener() 
        {                
            @Override
            public void itemStateChanged(ItemEvent evt)
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setKeepMeasuringBeamOnWhenIdle(selected);
            }
        });

        comboMeasuringBeamController.addItemListener(new ItemListener() 
        {           
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                String beamControllerId = (String)comboMeasuringBeamController.getSelectedItem();
                model.selectMeasuringBeamController(beamControllerId);
            }
        });
    }

    private JPanel buildPlotSettings()
    {
        SubPanel ploSettingsPanel = new SubPanel();
        ploSettingsPanel.addComponent(boxOverlayNewPlotOnPrevious, 1, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 15, 5));           

        JPanel outerPanel = new JPanel(new BorderLayout());

        outerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        outerPanel.add(ploSettingsPanel, BorderLayout.NORTH);

        initListenersForPlotSettingsComponents();

        return outerPanel;
    }

    private void initListenersForPlotSettingsComponents()
    {
        boxOverlayNewPlotOnPrevious.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setOverlayPlotOnPrevious(selected);              
            }
        });
    }

    private JPanel buildActuatorsPanel()
    {
        JPanel actuatorsPanel = FlliperGUIManager.getGUI(model.getFlipperModel());

        JPanel outerPanel = new JPanel(new BorderLayout());

        outerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        outerPanel.add(actuatorsPanel, BorderLayout.NORTH);

        return outerPanel;     
    }

    private JPanel buildCalibrationCardsPanel()
    {
        calibrationCardsForSignalsPanel.setLayout(calibrationSignalChoiceLayout);

        int signalCount = model.getSignalSourcesCount();
        addSignalCountDependentComponentsToCalibrationPanel(0, signalCount);

        initListenersForCalibrationPermanentComponents();

    
        return calibrationCardsForSignalsPanel;
    }

    private void initListenersForCalibrationPermanentComponents()
    {       
        comboSignalWithDisplayedCalibration.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                SignalIndexItem displayedSignalIndexNew = comboSignalWithDisplayedCalibration.getItemAt(comboSignalWithDisplayedCalibration.getSelectedIndex());
                displayCalibrationCardsForSignal(displayedSignalIndexNew.getZeroBasedSignalIndex());
            }
        });
    }

    private void displayCalibrationCardsForSignal(int signalIndex)
    {
        calibrationSignalChoiceLayout.show(calibrationCardsForSignalsPanel, Integer.toString(signalIndex));
    }

    //from - inclusive, to - exclusive
    private void addSignalCountDependentComponentsToCalibrationPanel(int from, int to)
    {
        for(int signalIndex = from; signalIndex < to; signalIndex++)
        {                                  
            Date latestCalibrationDateNew = model.getLatestCalibrationDate(signalIndex);
            String dateString = latestCalibrationDateNew != null ? calibrationDateFormat.format(latestCalibrationDateNew) : "Unknown";
            JLabel labelLatestCalibrationTime = new JLabel();
            labelLatestCalibrationTime.setText(dateString);      
            latestCalibrationTimeLabels.add(labelLatestCalibrationTime);

            double calibrationSlopeInPercentsPerVolt = model.getCalibrationSlopeInPercentsPerVolt(signalIndex);
            String slopeString = !Double.isNaN(calibrationSlopeInPercentsPerVolt) ? calibrationValueFormat.format(calibrationSlopeInPercentsPerVolt): "Unknown";
            JLabel labelLatestCalibrationSlopeValue = new JLabel();
            labelLatestCalibrationSlopeValue.setText(slopeString);
            latestCalibrationSlopeValueLabels.add(labelLatestCalibrationSlopeValue);

            double calibrationOffsetInVolts = model.getCalibrationOffsetInVolts(signalIndex);
            String offsetString = !Double.isNaN(calibrationOffsetInVolts) ? calibrationValueFormat.format(calibrationOffsetInVolts): "Unknown";
            JLabel labelLatestCalibrationOffset = new JLabel();
            labelLatestCalibrationOffset.setText(offsetString);
            latestCalibrationOffsetLabels.add(labelLatestCalibrationOffset);

            JPanel calibrationProgressPanel = new JPanel();
            calibrationProgressPanels.add(calibrationProgressPanel);

            CardLayout calibrationProgressBarLayout = new CardLayout();
            calibrationProgressBarLayouts.add(calibrationProgressBarLayout);

            JProgressBar calibrationProgressBar = new JProgressBar(0, 100);
            calibrationProgressBars.add(calibrationProgressBar);

            JLabel calibrationPhaseLabel = new JLabel();
            calibrationPhaseLabels.add(calibrationPhaseLabel);

            calibrationProgressPanel.setLayout(calibrationProgressBarLayout);
            calibrationProgressPanel.add(calibrationProgressBar, PROGRESS_BAR_CARD);
            calibrationProgressPanel.add(new JPanel(), PROGRESS_NOT_DISPLAYED_CARD);

            calibrationProgressBarLayout.show(calibrationProgressPanel, PROGRESS_NOT_DISPLAYED_CARD);

            Action calibrateAction = new CalibrateAction(signalIndex);
            calibrateAction.setEnabled(model.isCalibrateEnabled(signalIndex)); 

            this.calibrateActions.add(calibrateAction);

            JButton buttonCalibrate = new JButton(calibrateAction);

            SubPanel calibrationPanel = new SubPanel();
            this.calibrationPanels.add(calibrationPanel);

            calibrationPanel.addComponent(new JLabel("Latest calibration"), 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1,new Insets(30, 5, 10, 0));
            calibrationPanel.addComponent(labelLatestCalibrationTime, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1,new Insets(30, 0, 10, 0));
            calibrationPanel.addComponent(calibrationPhaseLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(30, 0, 10, 0));           

            calibrationPanel.addComponent(new JLabel("Slope (%/V)"), 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);
            calibrationPanel.addComponent(labelLatestCalibrationSlopeValue, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);           
            calibrationPanel.addComponent(calibrationProgressPanel, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);           

            calibrationPanel.addComponent(new JLabel("Offset (V)"), 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);
            calibrationPanel.addComponent(labelLatestCalibrationOffset, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);           
            calibrationPanel.addComponent(buttonCalibrate, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);           

            calibrationCardsForSignalsPanel.add(calibrationPanel, Integer.toString(signalIndex));
        }
      
        calibrationCardsForSignalsPanel.revalidate();
        calibrationCardsForSignalsPanel.repaint();
    }

    private void removeSignalCountDependentComponentsToCalibrationPanel(int oldComponentCount, int newComponentCount)
    {
        if(newComponentCount >= oldComponentCount)
        {
            return;
        }

        for(int signalIndex = newComponentCount; signalIndex< oldComponentCount; signalIndex++)
        {                                 
            JPanel calibrationPanel = calibrationPanels.get(signalIndex);
            calibrationCardsForSignalsPanel.remove(calibrationPanel);
        }

        this.latestCalibrationTimeLabels =  latestCalibrationTimeLabels.subList(0, newComponentCount);
        this.latestCalibrationSlopeValueLabels = latestCalibrationSlopeValueLabels.subList(0, newComponentCount);
        this.latestCalibrationOffsetLabels = latestCalibrationOffsetLabels.subList(0, newComponentCount);
        this.calibrationProgressPanels = calibrationProgressPanels.subList(0, newComponentCount);
        this.calibrationProgressBarLayouts = calibrationProgressBarLayouts.subList(0, newComponentCount);
        this.calibrationProgressBars = calibrationProgressBars.subList(0, newComponentCount);
        this.calibrationPhaseLabels = calibrationPhaseLabels.subList(0, newComponentCount);
        this.calibrateActions = calibrateActions.subList(0, newComponentCount);
        this.calibrationPanels = calibrationPanels.subList(0, newComponentCount);

        calibrationCardsForSignalsPanel.revalidate();
        calibrationCardsForSignalsPanel.repaint();
    }

    //from inclusive, to exclusive
    private void addSignalCountDependentSignalPropertiesComponents(int from, int to)
    {
        List<SignalSourceController> availableSignalSources = model.getAvailableSignalSourceControllers();
        String signalComponentCard = availableSignalSources.isEmpty() ? NO_SIGNAL_CONTROLLER_DETECTED : SIGNAL_CONTROLLER_DETECTED;
        
        int layoutMaxRowOld = measuringBeamPanel.getMaxRow();

        for(int signalIndex = from; signalIndex < to; signalIndex++)
        {
            JLabel labelSignalSource = new JLabel("Signal source");
            signalSourceLabels.add(labelSignalSource);

            JComboBox<SignalSourceController> comboSignalController = new JComboBox<>(new DefaultComboBoxModel<>(availableSignalSources.toArray(new SignalSourceController[] {})));
            comboSignalController.setSelectedItem(this.model.getSelectedSignalSourceController(signalIndex));
            signalControllerCombos.add(comboSignalController);           
            comboSignalController.setEnabled(model.isSignalSourceSelectionEnabled());

            CardLayout signalControllerLayout = new CardLayout();
            signalControllerLayouts.add(signalControllerLayout);

            JPanel panelSignalController = new JPanel(signalControllerLayout);

            panelSignalController.add(new JLabel("Undetected!"), NO_SIGNAL_CONTROLLER_DETECTED);
            panelSignalController.add(comboSignalController, SIGNAL_CONTROLLER_DETECTED);
            signalControllerLayout.show(panelSignalController, signalComponentCard);

            signalControllerPanels.add(panelSignalController);

            JLabel samplesPerMinuteLabel = new JLabel("Samples per minute");
            samplesPerMinuteLabels.add(samplesPerMinuteLabel);

            double samplesPerMinute = model.getSignalSamplesPerMinute(signalIndex);
            double minSamplesPerMinute = model.getMinimalSignalSamplesPerMinute(signalIndex);
            double maxSamplesPerMinute =  model.getMaximalAllowedMeasuringBeamFrequencyInHertz();
            String illegalValueWarning = "No of samples per minute should be a number between " + Double.toString(minSamplesPerMinute) + " and " +Double.toString(maxSamplesPerMinute);
            JSpinnerNumeric spinnerSignalSamplesPerMinute = new JSpinnerNumeric(new SpinnerDoubleModel(illegalValueWarning,samplesPerMinute, minSamplesPerMinute, maxSamplesPerMinute, 1.)); 
            signalSamplesPerMinuteSpinners.add(spinnerSignalSamplesPerMinute);      

            JLabel signalTypeLabel = new JLabel("Signal type");
            signalTypeLabels.add(signalTypeLabel);

            LightSignalType signalType = model.getSignalType(signalIndex);
            JComboBox<LightSignalType> signalTypeCombo = new JComboBox<>(new DefaultComboBoxModel<>(LightSignalType.values()));
            signalTypeCombo.setSelectedItem(signalType);
            signalTypeCombos.add(signalTypeCombo);

            int currentPanelRowIndex = layoutMaxRowOld + (signalIndex - from + 1);
            measuringBeamPanel.addComponent(labelSignalSource, 0, currentPanelRowIndex, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1,new Insets(5, 5, 0, 5));
            measuringBeamPanel.addComponent(panelSignalController, 1, currentPanelRowIndex, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1,new Insets(5, 5, 0, 5));
            measuringBeamPanel.addComponent(samplesPerMinuteLabel, 2, currentPanelRowIndex, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1,new Insets(5, 5, 0, 5));
            measuringBeamPanel.addComponent(spinnerSignalSamplesPerMinute, 3, currentPanelRowIndex, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1,new Insets(5, 0, 0, 0));
            measuringBeamPanel.addComponent(signalTypeLabel, 4, currentPanelRowIndex, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1,new Insets(5, 10, 0, 0));
            measuringBeamPanel.addComponent(signalTypeCombo, 5, currentPanelRowIndex, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1,new Insets(5, 5, 0, 0));

        }

        measuringBeamPanel.revalidate();
        measuringBeamPanel.repaint();

    }

    private void addListenersToSignalCountDependentSignalPropertiesComponents(int from, int to)
    {
        for(int signalIndex = from; signalIndex < to; signalIndex++)
        {
            final int signalIndexFinal = signalIndex;
            JComboBox<SignalSourceController> comboSignalController = signalControllerCombos.get(signalIndex);
            comboSignalController.addItemListener(new ItemListener() 
            {           
                @Override
                public void itemStateChanged(ItemEvent e) 
                {
                    SignalSourceController sourceController = comboSignalController.getItemAt(comboSignalController.getSelectedIndex());
                    model.selectTransmittanceSourceController(signalIndexFinal, sourceController);
                }
            });

            JComboBox<LightSignalType> comboSignalType = signalTypeCombos.get(signalIndexFinal);
            comboSignalType.addItemListener(new ItemListener() 
            {               
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    LightSignalType signalType = comboSignalType.getItemAt(comboSignalType.getSelectedIndex());
                    model.setSignalType(signalIndexFinal, signalType);
                }
            });

            JSpinner spinnerSignalSamplesPerMinute = signalSamplesPerMinuteSpinners.get(signalIndex);
            spinnerSignalSamplesPerMinute.addChangeListener(new ChangeListener() 
            {          
                @Override
                public void stateChanged(ChangeEvent evt)
                {
                    double transmittanceSamplesPerMinuteNew = ((Number)spinnerSignalSamplesPerMinute.getValue()).doubleValue();
                    model.selectSignalSamplesPerMinute(signalIndexFinal, transmittanceSamplesPerMinuteNew);
                }
            });
        }
    }

    private void removeSignalCountDependentSignalPropertiesComponents(int oldComponentCount, int newComponentCount)
    {
        if(newComponentCount >= oldComponentCount)
        {
            return;
        }

        for(int signalIndex = newComponentCount; signalIndex < oldComponentCount; signalIndex++)
        {                        
            JLabel signalSourceLabel = signalSourceLabels.get(signalIndex);
            measuringBeamPanel.remove(signalSourceLabel);

            JPanel panelSignalController = signalControllerPanels.get(signalIndex);
            measuringBeamPanel.remove(panelSignalController);

            JLabel samplesPerMinuteLabel = samplesPerMinuteLabels.get(signalIndex);
            measuringBeamPanel.remove(samplesPerMinuteLabel);

            JSpinner spinnerSignalSamplesPerMinute = signalSamplesPerMinuteSpinners.get(signalIndex); 
            measuringBeamPanel.remove(spinnerSignalSamplesPerMinute);

            JLabel signalTypeLabel = signalTypeLabels.get(signalIndex);
            measuringBeamPanel.remove(signalTypeLabel);

            JComboBox<LightSignalType> signalTypeCombo = signalTypeCombos.get(signalIndex);
            measuringBeamPanel.remove(signalTypeCombo);
        }

        this.signalSourceLabels = signalSourceLabels.subList(0, newComponentCount);
        this.signalControllerCombos = signalControllerCombos.subList(0, newComponentCount);
        this.signalControllerLayouts = signalControllerLayouts.subList(0, newComponentCount);
        this.signalControllerPanels = signalControllerPanels.subList(0, newComponentCount);
        this.samplesPerMinuteLabels = samplesPerMinuteLabels.subList(0, newComponentCount);
        this.signalSamplesPerMinuteSpinners = signalSamplesPerMinuteSpinners.subList(0, newComponentCount);
        this.signalTypeLabels = signalTypeLabels.subList(0, newComponentCount);
        this.signalTypeCombos = signalTypeCombos.subList(0, newComponentCount);

        measuringBeamPanel.revalidate();
        measuringBeamPanel.repaint();
    }

    private void rebuildGUIAfterChangeOfSignalCount(int oldComponentCount, int newComponentCount)
    {
        if(newComponentCount > oldComponentCount)
        {
            addSignalCountDependentComponentsToCalibrationPanel(oldComponentCount, newComponentCount);
            addSignalCountDependentSignalPropertiesComponents(oldComponentCount, newComponentCount);
            addListenersToSignalCountDependentSignalPropertiesComponents(oldComponentCount, newComponentCount);
        }
        else if(newComponentCount < oldComponentCount)
        {
            removeSignalCountDependentComponentsToCalibrationPanel(oldComponentCount, newComponentCount);
            removeSignalCountDependentSignalPropertiesComponents(oldComponentCount, newComponentCount);
        }       
    }

    private JPanel buildReadPhasesFromFilePanel()
    {
        JPanel buttonPanel = new JPanel();
        JButton buttonRead = new JButton(readPhasesFromFile);
        JButton buttonActinicCalibration = new JButton(readActinicBeamCalibrationFromFileAction);

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(buttonRead).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup().addComponent(fieldActinicCalibrationFile).addComponent(buttonActinicCalibration)));

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(fieldActinicCalibrationFile).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup().addComponent(buttonActinicCalibration).addComponent(buttonRead)));

        layout.linkSize(buttonRead, buttonActinicCalibration);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return buttonPanel;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        JButton buttonRun = new JButton(runAction);
        JButton buttonStop = new JButton(stopAction);
        JButton buttonResume = new JButton(resumeAction);
        JButton buttonCancel = new JButton(cancelAction);

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonRun).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,10,20)
                .addComponent(buttonStop).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonResume).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 10,50)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonRun)
                .addComponent(buttonStop)
                .addComponent(buttonResume)
                .addComponent(buttonCancel));

        layout.linkSize(buttonRun, buttonRun);

        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
    }

    private JPanel buildPanelCitation()
    {
        JPanel panelCitation = new JPanel(new BorderLayout());

        JLabel labelCitation = new JLabel();

        labelCitation.setOpaque(true);
        labelCitation.setText(TEXT_CITATION);
        labelCitation.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));

        if (LinkUtilities.isBrowsingSupported()) {
            LinkUtilities.makeLinkable(labelCitation, new LinkMouseListener());
        }
        panelCitation.add(labelCitation, BorderLayout.WEST);

        return panelCitation;
    }

    private void initModelListener()
    {
        model.addPhotometricRecordingModelListener(new RecordingModelListener()
        {               
            @Override
            public void phaseUnitDurationChanged(int phaseIndex, StandardTimeUnit durationUnitOld, StandardTimeUnit durationUnitNew) 
            {
                JComboBox<StandardTimeUnit> combo = timeUnitComboBoxes.get(phaseIndex);
                StandardTimeUnit comboOldValue = combo.getItemAt(combo.getSelectedIndex());
                if(!Objects.equals(durationUnitNew, comboOldValue))
                {
                    combo.setSelectedItem(durationUnitNew);
                }
            }        

            @Override
            public void actinicBeamCalibrationFileChanged(File calibrationFileOld, File calibrationFileNew)
            {    
                String calibrationFilePathNew = (calibrationFileNew != null) ? calibrationFileNew.getAbsolutePath() : "";
                fieldActinicCalibrationFile.setText(calibrationFilePathNew);
            }

            @Override
            public void phaseDurationChanged(int phaseIndex, double durationOld, double durationNew) 
            {
                JSpinner spinner = phaseDurationSpinners.get(phaseIndex);
                double spinnerOldValue = ((Number)spinner.getValue()).doubleValue();
                if(Double.compare(spinnerOldValue, durationNew) != 0)
                {
                    spinner.setValue(durationNew);
                }
            }

            @Override
            public void numberOfActinicPhasesChanged(int oldNumber, int newNumber) 
            {
                if(oldNumber != newNumber)
                {
                    spinnerPhaseCount.setValue(newNumber);     

                    if(newNumber > oldNumber)
                    {
                        addActinicBeamPhaseCountDependentComponents(oldNumber, newNumber);                            
                    }
                    else
                    {
                        removeActinicBeamPhaseCountDependentComponents(oldNumber, newNumber);
                    }

                    mainPanel.revalidate();
                    mainPanel.repaint();
                }
            }

            @Override
            public void phaseEndTimeChanged(int phaseIndex, Date newDate)
            {
                Date endTime = model.getExpectedPhaseEndTime(phaseIndex);
                String endTimeFormatted = (endTime == null) ? "": phaseEndDateFormat.format(endTime);

                JLabel endLabel = endTimeLabels.get(phaseIndex);
                if(!Objects.equals(endLabel.getText(), endTimeFormatted))
                {
                    endLabel.setText(endTimeFormatted);
                }
            }

            @Override
            public void actinicLightIntensityInPercentChanged(int phaseIndex, double intensityInPercentOld, double intensityInPercentNew) 
            {
                JSpinner spinner = phaseIntensitySpinners.get(phaseIndex);
                double spinnerOldValue = ((Number)spinner.getValue()).doubleValue();
                if(Double.compare(spinnerOldValue, intensityInPercentNew) != 0)
                {
                    spinner.setValue(intensityInPercentNew);
                }
            }

            @Override
            public void actinicBeamFilterChanged(int phaseIndex, SliderMountedFilter filterOld, SliderMountedFilter filterNew) 
            {
                JComboBox<SliderMountedFilter> combo = filterComboBoxes.get(phaseIndex);
                SliderMountedFilter comboOldValue = combo.getItemAt(combo.getSelectedIndex());
                if(!Objects.equals(filterNew, comboOldValue))
                {
                    combo.setSelectedItem(filterNew);
                }
            }

            @Override
            public void runEnabledChange(boolean enabledNew) 
            {
                runAction.setEnabled(enabledNew);
            }

            @Override
            public void stopEnabledChange(boolean enabledNew) 
            {
                stopAction.setEnabled(enabledNew);
            }

            @Override
            public void resumeEnabledChange(boolean enabledNew) 
            {
                resumeAction.setEnabled(enabledNew);
            }

            @Override
            public void cancelEnabledChange(boolean enabledNew) 
            {
                cancelAction.setEnabled(enabledNew);
            }

            @Override
            public void calibrationEnabledChange(int signalIndex, boolean enabledNew) 
            {
                Action calibrateAction = calibrateActions.get(signalIndex);
                calibrateAction.setEnabled(enabledNew);
            }

            @Override
            public void actinicBeamPhaseChange(long recordingStartAbsoluteTimeInMiliseconds, PhaseStamp phaseOld, PhaseStamp phaseNew) 
            {                   
                int finishedPhaseCountNew = phaseNew.getFinishedPhaseCount();

                setEnabledFinishedPhaseCountDependentComponents(false, 0, finishedPhaseCountNew);
                setEnabledFinishedPhaseCountDependentComponents(true, finishedPhaseCountNew, model.getActinicBeamPhaseCount()); 

                int currentPhaseOld = phaseOld.getCurrentPhaseIndex();
                int currentPhaseNew = phaseNew.getCurrentPhaseIndex();

                setEnablednessConsistentWithCurrentPhase(currentPhaseOld, currentPhaseNew);
            }

            @Override
            public void supportOfSoftwareControlOfMeasuringBeamIntensityChanged(boolean supportedNew)
            {
                spinnerMeasuringBeamIntensityInPercents.setEnabled(supportedNew);
            }

            @Override
            public void measuringBeamIntensityInPercentChanged(double intensityInPercentOld, double intensityInPercentNew) 
            {
                double spinnerOldValue = ((Number)spinnerMeasuringBeamIntensityInPercents.getValue()).doubleValue();
                if(Double.compare(spinnerOldValue, intensityInPercentNew) != 0)
                {
                    spinnerMeasuringBeamIntensityInPercents.setValue(intensityInPercentNew);
                }                       
            }

            @Override
            public void updateMeasuringBeamFrequencyInHertz(double measuringBeamFrequencyInHertzOld, double measuringBeamFrequencyInHertzNew) 
            {
                double spinnerOldValue = ((Number)spinnerMeasuringBeamFrequency.getValue()).doubleValue();

                if(Double.compare(spinnerOldValue, measuringBeamFrequencyInHertzNew) != 0)
                {
                    spinnerMeasuringBeamFrequency.setValue(measuringBeamFrequencyInHertzNew);
                }                    
            }

            @Override
            public void statusOfConnectionWithActinicBeamControllerChanged(boolean connectedOld, boolean connectedNew) 
            {
                String actinicBeamPortDescription = model.getActinicBeamControllerDescription();            
                labelActinicBeamController.setText(actinicBeamPortDescription);                    
            }

            @Override
            public void statusOfConnectionWithMeasuringBeamControllerChanged(boolean connectedOld, boolean connectedNew) 
            {

            }

            @Override
            public void setKeepMeasuringBeamOnWhenIdleChanged(boolean keepMeasuringBeamOnWhenIdleOld, boolean keepMeasuringBeamOnWhenIdleNew) 
            {
                boolean currentValue = boxKeepMeasuringBeamOnWhenIdle.isSelected();
                if(currentValue != keepMeasuringBeamOnWhenIdleNew)
                {
                    boxKeepMeasuringBeamOnWhenIdle.setSelected(keepMeasuringBeamOnWhenIdleNew);
                }
            }

            @Override
            public void measuringBeamFrequencyModificationEnabledChange(boolean enabled) 
            {
                spinnerMeasuringBeamFrequency.setEnabled(enabled);
            }

            @Override
            public void measuringBeamIntensityModificationEnabledChange(boolean enabled)
            {
                spinnerMeasuringBeamIntensityInPercents.setEnabled(enabled);                    
            }

            @Override
            public void measuringBeamIdleStateBehaviourModificationEnabledChange(boolean enabled) 
            {
                boxKeepMeasuringBeamOnWhenIdle.setEnabled(enabled);                    
            }

            @Override
            public void latestCalibrationDateChanged(int signalIndex, Date latestCalibrationDateOld, Date latestCalibrationDateNew) 
            {
                String dateString = latestCalibrationDateNew != null ? calibrationDateFormat.format(latestCalibrationDateNew) : "Unknown";
                JLabel labelLatestCalibrationTime = latestCalibrationTimeLabels.get(signalIndex);
                labelLatestCalibrationTime.setText(dateString);
            }

            @Override
            public void calibrationOffsetInVoltsChanged(int signalIndex, double calibrationOffsetInVoltsOld, double calibrationOffsetInVoltsNew) 
            {
                String offsetString = !Double.isNaN(calibrationOffsetInVoltsNew) ? calibrationValueFormat.format(calibrationOffsetInVoltsNew): "Unknown";
                JLabel labelLatestCalibrationOffset = latestCalibrationOffsetLabels.get(signalIndex);
                labelLatestCalibrationOffset.setText(offsetString);
            }

            @Override
            public void calibrationSlopeInPercentsPerVoltChanged(int signalIndex, double calibrationSlopeInPercentsPerVoltOld, double calibrationSlopeInPercentsPerVoltNew) 
            {
                String slopeString = !Double.isNaN(calibrationSlopeInPercentsPerVoltNew) ? calibrationValueFormat.format(calibrationSlopeInPercentsPerVoltNew): "Unknown";
                JLabel labelLatestCalibrationSlopeValue = latestCalibrationSlopeValueLabels.get(signalIndex);
                labelLatestCalibrationSlopeValue.setText(slopeString);
            }

            @Override
            public void calibrationPhaseChanged(int signalIndex, CalibrationPhase calibrationPhaseOld, CalibrationPhase calibrationPhaseNew) 
            {
                JLabel calibrationPhaseLabel = calibrationPhaseLabels.get(signalIndex);
                calibrationPhaseLabel.setText(calibrationPhaseNew.getGUIAnnouncement());
                String progressLayoutCardToShow = calibrationPhaseNew.isProgressBarRequired() ? PROGRESS_BAR_CARD : PROGRESS_NOT_DISPLAYED_CARD;
                JPanel calibrationProgressPanel = calibrationProgressPanels.get(signalIndex);
                CardLayout calibrationProgressBarLayout = calibrationProgressBarLayouts.get(signalIndex);
                calibrationProgressBarLayout.show(calibrationProgressPanel, progressLayoutCardToShow);
            }

            @Override
            public void signalSourceControllerChanged(int signalIndex, SignalSourceController transmittanceSourceControllerOld, SignalSourceController transmittanceSourceControllerNew) 
            {
                JComboBox<SignalSourceController> comboSignalController = signalControllerCombos.get(signalIndex);
                SignalSourceController currentSetting = comboSignalController.getItemAt(comboSignalController.getSelectedIndex());
                if(!Objects.equals(currentSetting, transmittanceSourceControllerNew))
                {
                    comboSignalController.setSelectedItem(transmittanceSourceControllerNew);
                }
            }

            @Override
            public void signalSamplesPerMinuteChanged(int signalIndex, double transmittanceSamplesPerMinuteOld, double transmittanceSamplesPerMinuteNew) 
            {
                JSpinner spinnerSignalSamplesPerMinute = signalSamplesPerMinuteSpinners.get(signalIndex);
                double spinnerOldValue = ((Number)spinnerSignalSamplesPerMinute.getValue()).doubleValue();
                if(Double.compare(spinnerOldValue, transmittanceSamplesPerMinuteNew) != 0)
                {
                    spinnerSignalSamplesPerMinute.setValue(transmittanceSamplesPerMinuteNew);
                }
            }

            @Override
            public void signalSourceSelectionEnabledChange(boolean enabledNew) 
            {
                for(JComboBox<SignalSourceController> comboSignalController : signalControllerCombos)
                {
                    comboSignalController.setEnabled(enabledNew);
                }
            }

            @Override
            public void maximalSignalSamplesPerMinuteChanged(int signalIndex, double maximalTransmittanceSamplesPerMinuteForSelectedSourceOld, double maximalTransmittanceSamplesPerMinuteForSelectedSourceNew) 
            {
                JSpinner spinnerSignalSamplesPerMinute = signalSamplesPerMinuteSpinners.get(signalIndex);
                double samplesPerMinute = model.getSignalSamplesPerMinute(signalIndex);
                double minSamplesPerMinute = model.getMinimalSignalSamplesPerMinute(signalIndex);
                double maxSamplesPerMinute =  model.getMaximalAllowedMeasuringBeamFrequencyInHertz();
                String illegalValueWarning = "No of samples per minute should be a number between " + Double.toString(minSamplesPerMinute) + " and " +Double.toString(maxSamplesPerMinute);

                spinnerSignalSamplesPerMinute.setModel(new SpinnerDoubleModel(illegalValueWarning,samplesPerMinute, model.getMinimalSignalSamplesPerMinute(signalIndex), maximalTransmittanceSamplesPerMinuteForSelectedSourceNew, 1.0));
            }

            @Override
            public void overlayPlotOnPreviousChanged(boolean overlayPlotOnPreviousOld, boolean overlayPlotOnPreviousNew) 
            {
                boolean previousValue = boxOverlayNewPlotOnPrevious.isSelected();
                if(previousValue != overlayPlotOnPreviousNew)
                {
                    boxOverlayNewPlotOnPrevious.setSelected(overlayPlotOnPreviousNew);
                }
            }

            @Override
            public void readActinicBeamPhasesFromFileEnabled(boolean enabledNew) 
            {
                readPhasesFromFile.setEnabled(enabledNew);
            }

            @Override
            public void absoluteLightIntensityChanged(int phaseIndex, double absoluteIntensityNew) 
            {
                String absoluteIntensityDescription = !Double.isNaN(absoluteIntensityNew) ? absoluteLightIntensityFormat.format(absoluteIntensityNew) :"";
                absoluteIntensityLabels.get(phaseIndex).setText(absoluteIntensityDescription);
            }

            @Override
            public void maximalMeasuringBeamFrequencyInHertzChanged(double maxFrequencyInHertzOld, double maxFrequencyInHertzNew) 
            {

            }

            @Override
            public void measuringBeamControllerChanged(String descriptionOld, String descriptionNew)
            {
                String currentComboSettings = (String) comboMeasuringBeamController.getSelectedItem();
                if(!Objects.equals(currentComboSettings, descriptionNew))
                {
                    comboMeasuringBeamController.setSelectedItem(descriptionNew);
                }
            }

            @Override
            public void availabilityOfFunctionalMeasuringBeamControllersChange(boolean functionalMeasuringBeamControllerAvilableOld, boolean functionalMeasuringBeamControllerAvilableNew) 
            {    
                String measuringBeamCard = functionalMeasuringBeamControllerAvilableNew ? MEASURING_BEAM_CONTROLLER_DETECTED : NO_MEASURING_BEAM_CONTROLLER_DETECTED;               
                measuringBeamControllerLayout.show(measuringBeamControllerInnerPanel, measuringBeamCard);
            }

            @Override
            public void functionalMeasuringBeamControllerAdded(String newMeasuringBeamDescription)
            {
                comboMeasuringBeamController.addItem(newMeasuringBeamDescription);
            }

            @Override
            public void functionalMeasuringBeamControllerRemoved(String measuringBeamControllerDescription) 
            {
                comboMeasuringBeamController.removeItem(measuringBeamControllerDescription);                
            }

            @Override
            public void availabilityOfFunctionalSignalSourceControllersChange(boolean functionalSignalSourceControllerAvailableOld, boolean functionalSignalSourceControllerAvailableNew) 
            {
                int panelSignalControllerCount = signalControllerPanels.size();
                for(int i = 0; i < panelSignalControllerCount; panelSignalControllerCount++)
                {
                    CardLayout signalControllerLayout = signalControllerLayouts.get(i);
                    JPanel panelSignalController = signalControllerPanels.get(i);
                    String transmittanceComponentCard = functionalSignalSourceControllerAvailableNew ? SIGNAL_CONTROLLER_DETECTED : NO_SIGNAL_CONTROLLER_DETECTED;               
                    signalControllerLayout.show(panelSignalController, transmittanceComponentCard);
                }
            }

            @Override
            public void functionalSignalControllerAdded(SignalSourceController controllerNew)
            {
                for(JComboBox<SignalSourceController> comboSignalController: signalControllerCombos)
                {
                    comboSignalController.addItem(controllerNew);
                }
            }

            @Override
            public void functionalSignalControllerRemoved(SignalSourceController controller) 
            {
                for(JComboBox<SignalSourceController> comboSignalController: signalControllerCombos)
                {
                    comboSignalController.removeItem(controller);   
                }
            }

            @Override
            public void preferredFrequencyIncrementAndDecrementChanged(double preferredIncrementOld, double preferredIncrementNew, double preferredDecrementOld, double preferredDecrementNew)
            {                                        
                SpinnerModel frequencySpinnerModel = buildModelForMeasuringBeamFrequencySpinner(preferredIncrementNew, preferredDecrementNew);

                spinnerMeasuringBeamFrequency.setModel(frequencySpinnerModel);

                JComponent spinnerEditor = spinnerMeasuringBeamFrequency.getEditor();
                if(spinnerEditor instanceof DefaultEditor)
                {
                    ((DefaultEditor) spinnerEditor).getTextField().setEditable(true);
                }
            }

            @Override
            public void progressInPercentOfCurrentCalibrationPhaseChanged(int signalIndex, int currentProgressInPercent)
            {
                JProgressBar calibrationProgressBar = calibrationProgressBars.get(signalIndex);
                calibrationProgressBar.setValue(currentProgressInPercent);
            }

            @Override
            public void availableActinicBeamFiltersChanged(int oldCount, int newCount) 
            {
                for(int i = 0; i < filterComboBoxes.size(); i++)
                {
                    JComboBox<SliderMountedFilter> comboBox = filterComboBoxes.get(i);
                    int currentlySelectedIndex = comboBox.getSelectedIndex();

                    ComboBoxModel<SliderMountedFilter> comboModelNew = new DefaultComboBoxModel<>(model.getActinicBeamSliderMountedFilters().toArray(new SliderMountedFilter[]{}));
                    int indexToSelect = Math.min(currentlySelectedIndex, newCount -1);

                    comboBox.setModel(comboModelNew);
                    comboBox.setSelectedIndex(indexToSelect);
                }           
            }

            @Override
            public void propertiesOfActinicBeamFilterChanged(SliderMountedFilter filterNew)
            {
                int filterPositionIndex = filterNew.getPositionIndex();

                for(int i = 0; i < filterComboBoxes.size(); i++)
                {
                    JComboBox<SliderMountedFilter> comboBox = filterComboBoxes.get(i);
                    int currentlySelectedIndex = comboBox.getSelectedIndex();

                    for(int j = 0; j<comboBox.getItemCount();j++)
                    {
                        SliderMountedFilter jThItem = comboBox.getItemAt(j);
                        if(filterPositionIndex == jThItem.getPositionIndex())
                        {
                            comboBox.removeItemAt(j);
                            comboBox.insertItemAt(filterNew, j);
                            comboBox.setSelectedIndex(currentlySelectedIndex);
                        }
                    }
                }
            }

            @Override
            public void signalSourcesCountChanged(int signalSourcesCountOld, int signalSourcesCountNew) 
            {
                if(signalSourcesCountOld != signalSourcesCountNew)
                {
                    spinnerSignalSourcesCount.setValue(signalSourcesCountNew);
                    rebuildGUIAfterChangeOfSignalCount(signalSourcesCountOld, signalSourcesCountNew);

                    int previousDisplayedCalibrationPanelIndex = (comboSignalWithDisplayedCalibration.getItemAt(comboSignalWithDisplayedCalibration.getSelectedIndex())).getZeroBasedSignalIndex();
                    int currentDisplayedCalibrationPanelIndex = Math.min(previousDisplayedCalibrationPanelIndex, signalSourcesCountNew - 1);
                    comboSignalWithDisplayedCalibration.setModel(new DefaultComboBoxModel<>(SignalIndexItem.buildSignalIndexArray(signalSourcesCountNew)));
                    comboSignalWithDisplayedCalibration.setSelectedItem(Integer.valueOf(currentDisplayedCalibrationPanelIndex));
                   
                    if(currentDisplayedCalibrationPanelIndex != previousDisplayedCalibrationPanelIndex)
                    {
                        displayCalibrationCardsForSignal(currentDisplayedCalibrationPanelIndex);
                    }
                }
            }

            @Override
            public void signalTypeChanged(int signalIndex, LightSignalType signalTypeOld, LightSignalType signalTypeNew) 
            {
                JComboBox<LightSignalType> signalTypeCombo = signalTypeCombos.get(signalIndex);
                LightSignalType currentlySelected = signalTypeCombo.getItemAt(signalTypeCombo.getSelectedIndex());
                if(!Objects.equals(currentlySelected, signalTypeNew))
                {
                    signalTypeCombo.setSelectedItem(signalTypeNew);
                }
            }
        });
    }

    private class ReadPhasesFromFileAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ReadPhasesFromFileAction()
        {           
            putValue(NAME, "From file");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            int op = fileChooserOutput.showDialog(actinicBeamPhasesPanel,null); //if this is not null, then the JFileChooser code will call setDialogType(CUSTOM_DIALOG)

            if (op == JFileChooser.APPROVE_OPTION) 
            {
                File fileNew = fileChooserOutput.getSelectedFile();

                try {
                    model.readInActinicPhaseSettingsFromFile(fileNew);
                } catch (UserCommunicableException e) 
                {
                    JOptionPane.showMessageDialog(mainPanel, "An error occured during reading actnic beam phases", "", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }
    }

    private class ReadActinicBeamCalibrationFromFileAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ReadActinicBeamCalibrationFromFileAction()
        {           
            putValue(NAME, "Actinic calibration");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            int op = fileChooserActinicCalibration.showDialog(actinicBeamPhasesPanel,null); //if this is not null, then the JFileChooser code will call setDialogType(CUSTOM_DIALOG)

            if (op == JFileChooser.APPROVE_OPTION) 
            {
                File fileNew = fileChooserActinicCalibration.getSelectedFile();

                try {
                    model.setActinicBeamCalibrationFile(fileNew);
                } catch (UserCommunicableException e) 
                {
                    JOptionPane.showMessageDialog(mainPanel, "An error occured during reading actnic beam calibration", "", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }
    }

    private class RunAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public RunAction()
        {           
            putValue(NAME, "Run");
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            model.run();
        }
    }


    private class StopAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public StopAction()
        {           
            putValue(NAME, "Stop");
            putValue(MNEMONIC_KEY, KeyEvent.VK_T);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            model.stop();
        }
    }

    private class ResumeAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ResumeAction()
        {           
            putValue(NAME, "Resume");
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            model.resume();
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
            model.cancel();
        }
    }

    private class CalibrateAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        private final int signalIndex;

        public CalibrateAction(int signalIndex)
        {           
            this.signalIndex = signalIndex;
            putValue(NAME, "Calibrate");
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            model.calibrate(signalIndex);
        }
    }
}