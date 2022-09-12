package chloroplastInterface;

import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_X;
import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_Y;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.ExtensionFileChooser;
import atomicJ.gui.SubPanel;
import atomicJ.gui.UserCommunicableException;
import chloroplastInterface.optics.SliderMountedFilter;

public class ActinicBeamAutomaticCalibrationGUI
{
    private static final Preferences PREF = Preferences.userNodeForPackage(ActinicBeamAutomaticCalibrationGUI.class).node(ActinicBeamAutomaticCalibrationGUI.class.getName());

    private final static int DEFAULT_MINIMAL_PHASE_COUNT = 1;
    private final static int DEFAULT_MAXIMAL_PHASE_COUNT = 10000;

    private final JComboBox<IrradianceUnitType> comboIrradianceUnitType = new JComboBox<>(IrradianceUnitType.values());
    private final JCheckBox promptForFilterChangeBox =  new JCheckBox();
    private final JSpinner voltageToIntensityConversionFactorSpinner = new JSpinner(new SpinnerNumberModel(1., 0., 10000., 0.1));

    private final JSpinner spinnerPhaseCount = new JSpinner(new SpinnerNumberModel(DEFAULT_MINIMAL_PHASE_COUNT, DEFAULT_MINIMAL_PHASE_COUNT, DEFAULT_MAXIMAL_PHASE_COUNT, 1));

    private List<JLabel> phaseLabels = new ArrayList<>();

    private List<JSpinner> minimumIntensityInPercentsSpinners = new ArrayList<>();
    private List<JSpinner> maximumIntensityInPercentsSpinners = new ArrayList<>();

    private List<JComboBox<SliderMountedFilter>> filterComboBoxes = new ArrayList<>();

    private List<JSpinner> stepCountSpinners = new ArrayList<>();
    private List<JComboBox<ScaleType>> stepScaleTypeCombos = new ArrayList<>();

    private List<JSpinner> stepRecordingTimeSpinners = new ArrayList<>();
    private List<JComboBox<StandardTimeUnit>> stepRecordingTimeUnitCombos = new ArrayList<>();
    private List<JSpinner> stepPauseTimeSpinners = new ArrayList<>();
    private List<JComboBox<StandardTimeUnit>> stepPauseTimeUnitCombos = new ArrayList<>();

    private List<JLabel> filterLabels = new ArrayList<>();
    private List<JLabel> minimumLabels = new ArrayList<>();
    private List<JLabel> maximumLabels = new ArrayList<>();
    private List<JLabel> stepCountLabels = new ArrayList<>();
    private List<JLabel> scaleTypeLabels = new ArrayList<>();
    private List<JLabel> integrationTimeLabels = new ArrayList<>();
    private List<JLabel> pauseLabels = new ArrayList<>();

    private final SubPanel actinicBeamCalibrationPointsPanel = new SubPanel();

    private final ExtensionFileChooser fileChooser = new ExtensionFileChooser(PREF, "Actinic beam calibration", "abc",true);

    private final Action saveToFileAction = new SaveToFileAction();
    private final Action readFromFileAction = new ReadFromFileAction();
    private final Action closeAction = new CloseAction();

    private final ActinicBeamAutomaticCalibrationModel model;

    private final JDialog dialog;

    public ActinicBeamAutomaticCalibrationGUI(Window parent, ActinicBeamAutomaticCalibrationModel model)
    {
        this.model = model;
        this.dialog = new JDialog(parent, "Actinic beam automatic calibration", ModalityType.APPLICATION_MODAL);

        this.spinnerPhaseCount.setValue(model.getActinicBeamPhaseCount());    
        this.comboIrradianceUnitType.setSelectedItem(this.model.getAbsoluteLightIntensityUnitType());

        this.voltageToIntensityConversionFactorSpinner.setValue(this.model.getIntensityUnitsPerVolt());
        this.promptForFilterChangeBox.setSelected(this.model.isPromptForFilterChange());      

        buildActinicBeamSettingsPanel();

        initComponentEnabledState();

        initModelListener();

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.add(actinicBeamCalibrationPointsPanel, BorderLayout.NORTH);

        JScrollPane scrollFrame = new JScrollPane(innerPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollFrame.setAutoscrolls(true);
        scrollFrame.setPreferredSize(new Dimension(550,400));     

        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.add(scrollFrame, BorderLayout.NORTH);
        outerPanel.add(buildButtonPanel(),BorderLayout.SOUTH);

        Container content = dialog.getContentPane();
        content.add(outerPanel,BorderLayout.CENTER);

        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                closeSafely();
                model.close();
            }
        });

        int locationX = Math.max(0, PREF.getInt(WINDOW_LOCATION_X, -1));
        int locationY = Math.max(0,PREF.getInt(WINDOW_LOCATION_Y, -1));

        if(locationX < 1 || locationY < 1)
        {
            dialog.setLocationRelativeTo(parent);
        }
        else
        {
            dialog.setLocation(locationX,locationY); 
        }
        dialog.pack();
    }


    private void closeSafely()
    {
        PREF.putInt(WINDOW_LOCATION_X, (int) Math.max(0,dialog.getLocation().getX()));          
        PREF.putInt(WINDOW_LOCATION_Y, (int)  Math.max(0,dialog.getLocation().getY())); 
    }

    public void setDialogVisible(boolean visible)
    {
        dialog.revalidate();
        dialog.setVisible(visible);
    }

    private void initComponentEnabledState()
    {
        saveToFileAction.setEnabled(model.isSaveToFileEnabled());     
    } 

    private void buildActinicBeamSettingsPanel()
    {         
        actinicBeamCalibrationPointsPanel.addComponent(new JLabel("Voltage to intensity multiplier"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 5, 5));           
        actinicBeamCalibrationPointsPanel.addComponent(voltageToIntensityConversionFactorSpinner, 1, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 5));
        actinicBeamCalibrationPointsPanel.addComponent(new JLabel("Intensity unit"), 3, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1,new Insets(0, 5, 5, 5));
        actinicBeamCalibrationPointsPanel.addComponent(comboIrradianceUnitType, 4, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 5, 5));           

        actinicBeamCalibrationPointsPanel.addComponent(new JLabel("Phase count"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1,new Insets(0, 5, 5, 0));
        actinicBeamCalibrationPointsPanel.addComponent(spinnerPhaseCount, 1, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 5));           
        actinicBeamCalibrationPointsPanel.addComponent(new JLabel("Voltage source"), 3, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1,new Insets(0, 5, 5, 0));

        actinicBeamCalibrationPointsPanel.addComponent(new JLabel("Prompt for next filter"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 15, 5));           
        actinicBeamCalibrationPointsPanel.addComponent(promptForFilterChangeBox, 1, 2, 3, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 15, 5));           


        initListenersForPermanentComponents();

        int phaseCount = model.getActinicBeamPhaseCount();
        addPhaseDependentComponents(0, phaseCount);

        actinicBeamCalibrationPointsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
    }

    private void addPhaseDependentComponents(int oldCalibrationPointCount, int newCalibrationPointCount)
    {
        int layoutMaxRowOld = actinicBeamCalibrationPointsPanel.getMaxRow();
        int noOfRowsPerPhase = 5;

        for(int i = oldCalibrationPointCount; i< newCalibrationPointCount;i++)
        {       
            SliderMountedFilter filter = model.getActinicBeamFilter(i);

            double minimumIntensityInPercents = model.getMinimumIntensityInPercent(i);
            double maximumIntensityInPercents = model.getMaximumIntensityInPercent(i);

            int stepCount = model.getStepCount(i);
            ScaleType stepScaleType = model.getStepScaleType(i);

            double stepRecordingTime = model.getStepRecordingTime(i);
            double stepPauseTime = model.getPauseBetweenSteps(i);

            StandardTimeUnit stepRecordingTimeUnit = model.getStepRecordingTimeUnit(i);
            StandardTimeUnit stepPauseTimeUnit = model.getPauseBetweenStepsUnit(i);

            JComboBox<SliderMountedFilter> comboFilter = new JComboBox<>(model.getAvailableActinicBeamSliderMountedFilters().toArray(new SliderMountedFilter[] {}));
            comboFilter.setSelectedItem(filter);

            JSpinner spinnerMinimumIntensityInPercent = new JSpinner(new SpinnerNumberModel(minimumIntensityInPercents, 0, maximumIntensityInPercents, 1.0));
            JSpinner spinnerMaximumIntensityInPercents = new JSpinner(new SpinnerNumberModel(maximumIntensityInPercents, minimumIntensityInPercents, 100., 1.0));

            JSpinner spinnerStepCount = new JSpinner(new SpinnerNumberModel(stepCount, 1, 10000, 1));
            JComboBox<ScaleType> comboScaleType = new JComboBox<>(ScaleType.values());
            comboScaleType.setSelectedItem(stepScaleType);

            JSpinner spinnerStepRecordingTime = new JSpinner(new SpinnerNumberModel(stepRecordingTime, Double.MIN_VALUE, 100000., 1.0));
            JSpinner spinnerStepPauseTime = new JSpinner(new SpinnerNumberModel(stepPauseTime, 0., 100000., 1.0));

            JComboBox<StandardTimeUnit> comboStepRecordingTimeUnit = new JComboBox<>(StandardTimeUnit.values());
            comboStepRecordingTimeUnit.setSelectedItem(stepRecordingTimeUnit);
            JComboBox<StandardTimeUnit> comboStepPauseTimeUnit = new JComboBox<>(StandardTimeUnit.values());
            comboStepPauseTimeUnit.setSelectedItem(stepPauseTimeUnit);


            filterComboBoxes.add(comboFilter);

            minimumIntensityInPercentsSpinners.add(spinnerMinimumIntensityInPercent);
            maximumIntensityInPercentsSpinners.add(spinnerMaximumIntensityInPercents);

            stepCountSpinners.add(spinnerStepCount);
            stepScaleTypeCombos.add(comboScaleType);

            stepRecordingTimeSpinners.add(spinnerStepRecordingTime);
            stepPauseTimeSpinners.add(spinnerStepPauseTime);

            stepRecordingTimeUnitCombos.add(comboStepRecordingTimeUnit);
            stepPauseTimeUnitCombos.add(comboStepPauseTimeUnit);

            JLabel phaseLabel = new JLabel("Phase " + Integer.toString(i + 1));
            JLabel filterLabel = new JLabel("Filter");
            JLabel minimumLabel = new JLabel("Minimum (%)");
            JLabel maximumLabel = new JLabel("Maximum (%)");
            JLabel stepCountLabel = new JLabel("Step count");
            JLabel scaleTypeLabel = new JLabel("Scale type");
            JLabel integrationTimeLabel = new JLabel("Integration");
            JLabel pauseLabel = new JLabel("Pause");

            phaseLabels.add(phaseLabel);
            filterLabels.add(filterLabel);
            minimumLabels.add(minimumLabel);
            maximumLabels.add(maximumLabel);
            stepCountLabels.add(stepCountLabel);
            scaleTypeLabels.add(scaleTypeLabel);
            integrationTimeLabels.add(integrationTimeLabel);
            pauseLabels.add(pauseLabel);

            int noOfPhaseDependentRows = (i - oldCalibrationPointCount)*noOfRowsPerPhase;

            actinicBeamCalibrationPointsPanel.addComponent(phaseLabel, 1, layoutMaxRowOld + 1 + noOfPhaseDependentRows, 3, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 5, 5));

            actinicBeamCalibrationPointsPanel.addComponent(filterLabel, 0, layoutMaxRowOld + 2 + noOfPhaseDependentRows, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(0, 0, 5, 5));
            actinicBeamCalibrationPointsPanel.addComponent(comboFilter, 1, layoutMaxRowOld + 2 + noOfPhaseDependentRows, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 5));

            actinicBeamCalibrationPointsPanel.addComponent(minimumLabel, 0, layoutMaxRowOld + 3 + noOfPhaseDependentRows, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(0, 0, 5, 5));
            actinicBeamCalibrationPointsPanel.addComponent(spinnerMinimumIntensityInPercent, 1, layoutMaxRowOld + 3 + noOfPhaseDependentRows, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 5));
            actinicBeamCalibrationPointsPanel.addComponent(maximumLabel, 3, layoutMaxRowOld + 3 + noOfPhaseDependentRows, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 5, 5));
            actinicBeamCalibrationPointsPanel.addComponent(spinnerMaximumIntensityInPercents, 4, layoutMaxRowOld + 3 + noOfPhaseDependentRows, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 0));           

            actinicBeamCalibrationPointsPanel.addComponent(stepCountLabel, 0, layoutMaxRowOld + 4 + noOfPhaseDependentRows, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(0, 0, 5, 5));
            actinicBeamCalibrationPointsPanel.addComponent(spinnerStepCount, 1, layoutMaxRowOld + 4 + noOfPhaseDependentRows, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 5));
            actinicBeamCalibrationPointsPanel.addComponent(scaleTypeLabel, 3, layoutMaxRowOld + 4 + noOfPhaseDependentRows, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 5, 5));
            actinicBeamCalibrationPointsPanel.addComponent(comboScaleType, 4, layoutMaxRowOld + 4 + noOfPhaseDependentRows, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 0));           

            actinicBeamCalibrationPointsPanel.addComponent(integrationTimeLabel, 0, layoutMaxRowOld + 5 + noOfPhaseDependentRows, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(0, 0, 5, 5));
            actinicBeamCalibrationPointsPanel.addComponent(spinnerStepRecordingTime, 1, layoutMaxRowOld + 5 + noOfPhaseDependentRows, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 5));
            actinicBeamCalibrationPointsPanel.addComponent(comboStepRecordingTimeUnit, 2, layoutMaxRowOld + 5 + noOfPhaseDependentRows, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 5));
            actinicBeamCalibrationPointsPanel.addComponent(pauseLabel, 3, layoutMaxRowOld + 5 + noOfPhaseDependentRows, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 5, 5));
            actinicBeamCalibrationPointsPanel.addComponent(spinnerStepPauseTime, 4, layoutMaxRowOld + 5 + noOfPhaseDependentRows, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 0));           
            actinicBeamCalibrationPointsPanel.addComponent(comboStepPauseTimeUnit, 5, layoutMaxRowOld + 5 + noOfPhaseDependentRows, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 0));           
        }

        initListenersForPhaseNumberDependentComponents(oldCalibrationPointCount,  newCalibrationPointCount);

        actinicBeamCalibrationPointsPanel.revalidate();
        actinicBeamCalibrationPointsPanel.repaint();
        dialog.pack();
    }

    private void removePhaseDependentComponents(int oldComponentCount,int newComponentCount)
    {
        for(int i = newComponentCount; i < oldComponentCount;i++)
        {
            JComboBox<SliderMountedFilter> comboBoxFilter = filterComboBoxes.get(i);

            JSpinner spinnerMaximumIntensityInPercents = maximumIntensityInPercentsSpinners.get(i);
            JSpinner spinnerMinimumIntensityInPercent = minimumIntensityInPercentsSpinners.get(i);

            JSpinner spinnerStepCount = stepCountSpinners.get(i);
            JComboBox<ScaleType> comboScaleType = stepScaleTypeCombos.get(i);           

            JSpinner spinnerStepRecordingTime = stepRecordingTimeSpinners.get(i);
            JSpinner spinnerStepPauseTime = stepPauseTimeSpinners.get(i);

            JComboBox<StandardTimeUnit> comboStepPauseTimeUnit = stepPauseTimeUnitCombos.get(i);
            JComboBox<StandardTimeUnit> comboStepRecordingTimeUnit = stepRecordingTimeUnitCombos.get(i);

            JLabel labelPhase = phaseLabels.get(i);
            JLabel filterLabel = filterLabels.get(i);
            JLabel minimumLabel = minimumLabels.get(i);
            JLabel maximumLabel = maximumLabels.get(i);
            JLabel stepCountLabel = stepCountLabels.get(i);
            JLabel scaleTypeLabel = scaleTypeLabels.get(i);
            JLabel integrationTimeLabel = integrationTimeLabels.get(i);
            JLabel pauseLabel = pauseLabels.get(i);

            actinicBeamCalibrationPointsPanel.remove(labelPhase);

            actinicBeamCalibrationPointsPanel.remove(filterLabel);
            actinicBeamCalibrationPointsPanel.remove(comboBoxFilter);

            actinicBeamCalibrationPointsPanel.remove(spinnerMaximumIntensityInPercents);
            actinicBeamCalibrationPointsPanel.remove(spinnerMinimumIntensityInPercent);              

            actinicBeamCalibrationPointsPanel.remove(spinnerStepCount);
            actinicBeamCalibrationPointsPanel.remove(comboScaleType);

            actinicBeamCalibrationPointsPanel.remove(spinnerStepRecordingTime);
            actinicBeamCalibrationPointsPanel.remove(spinnerStepPauseTime);

            actinicBeamCalibrationPointsPanel.remove(comboStepPauseTimeUnit);
            actinicBeamCalibrationPointsPanel.remove(comboStepRecordingTimeUnit);

            actinicBeamCalibrationPointsPanel.remove(minimumLabel);
            actinicBeamCalibrationPointsPanel.remove(maximumLabel);
            actinicBeamCalibrationPointsPanel.remove(stepCountLabel);
            actinicBeamCalibrationPointsPanel.remove(scaleTypeLabel);
            actinicBeamCalibrationPointsPanel.remove(integrationTimeLabel);
            actinicBeamCalibrationPointsPanel.remove(pauseLabel);
        }

        this.phaseLabels = phaseLabels.subList(0, newComponentCount);
        this.filterLabels = filterLabels.subList(0, newComponentCount);
        this.filterComboBoxes = filterComboBoxes.subList(0, newComponentCount);

        this.maximumIntensityInPercentsSpinners = maximumIntensityInPercentsSpinners.subList(0, newComponentCount);
        this.minimumIntensityInPercentsSpinners = minimumIntensityInPercentsSpinners.subList(0, newComponentCount);

        this.stepCountSpinners = stepCountSpinners.subList(0, newComponentCount);
        this.stepScaleTypeCombos = stepScaleTypeCombos.subList(0, newComponentCount);

        this.stepRecordingTimeSpinners = stepRecordingTimeSpinners.subList(0, newComponentCount);
        this.stepPauseTimeSpinners = stepPauseTimeSpinners.subList(0, newComponentCount);

        this.stepPauseTimeUnitCombos = stepPauseTimeUnitCombos.subList(0, newComponentCount);
        this.stepRecordingTimeUnitCombos = stepRecordingTimeUnitCombos.subList(0, newComponentCount);

        this.minimumLabels = minimumLabels.subList(0, newComponentCount);
        this.maximumLabels = maximumLabels.subList(0, newComponentCount);
        this.stepCountLabels = stepCountLabels.subList(0, newComponentCount);
        this.scaleTypeLabels = scaleTypeLabels.subList(0, newComponentCount);
        this.integrationTimeLabels = integrationTimeLabels.subList(0, newComponentCount);
        this.pauseLabels = pauseLabels.subList(0, newComponentCount);

        //https://docs.oracle.com/javase/tutorial/uiswing/components/jcomponent.html#custompaintingapi
        actinicBeamCalibrationPointsPanel.revalidate();
        actinicBeamCalibrationPointsPanel.repaint();
    }

    //from - inclusive, to - exclusive
    private void initListenersForPhaseNumberDependentComponents(int from, int to)
    {               
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

        for(int i = from; i < to; i++)
        {
            JSpinner minimumIntensitySpinner = minimumIntensityInPercentsSpinners.get(i);
            final int phaseIndex = i;

            minimumIntensitySpinner.addChangeListener(new ChangeListener() 
            {          
                @Override
                public void stateChanged(ChangeEvent evt)
                {
                    double phaseIntensityNew = ((SpinnerNumberModel)minimumIntensitySpinner.getModel()).getNumber().doubleValue();
                    model.setMinimumIntensityInPercent(phaseIndex, phaseIntensityNew);
                }
            });
        }

        for(int i = from; i< to; i++)
        {
            JSpinner maximumIntensitySpinner = maximumIntensityInPercentsSpinners.get(i);
            final int phaseIndex = i;

            maximumIntensitySpinner.addChangeListener(new ChangeListener() 
            {          
                @Override
                public void stateChanged(ChangeEvent evt)
                {
                    double phasIntensityNew = ((SpinnerNumberModel)maximumIntensitySpinner.getModel()).getNumber().doubleValue();
                    model.setMaximumIntensityInPercent(phaseIndex, phasIntensityNew);                       
                }
            });
        }

        for(int i = from; i< to; i++)
        {
            JSpinner stepCountSpinner = stepCountSpinners.get(i);
            final int phaseIndex = i;

            stepCountSpinner.addChangeListener(new ChangeListener() 
            {          
                @Override
                public void stateChanged(ChangeEvent evt)
                {
                    int stepCountNew = ((SpinnerNumberModel)stepCountSpinner.getModel()).getNumber().intValue();
                    model.setStepCount(phaseIndex, stepCountNew);                       
                }
            });
        }



        for(int i = from; i < to; i++)
        {
            JComboBox<ScaleType> comboBox = stepScaleTypeCombos.get(i);
            final int phaseIndex = i;

            comboBox.addItemListener(new ItemListener() 
            {                    
                @Override
                public void itemStateChanged(ItemEvent e) {
                    ScaleType scaleTypeNew = comboBox.getItemAt(comboBox.getSelectedIndex());
                    model.setStepScaleType(phaseIndex, scaleTypeNew);
                }
            });
        }


        for(int i = from; i< to; i++)
        {
            JSpinner pauseTimeSpinner = stepPauseTimeSpinners.get(i);
            final int phaseIndex = i;

            pauseTimeSpinner.addChangeListener(new ChangeListener() 
            {          
                @Override
                public void stateChanged(ChangeEvent evt)
                {
                    double pauseTimeNew = ((SpinnerNumberModel)pauseTimeSpinner.getModel()).getNumber().doubleValue();
                    model.setPauseBetweenSteps(phaseIndex, pauseTimeNew);                       
                }
            });
        }

        for(int i = from; i < to; i++)
        {
            JComboBox<StandardTimeUnit> comboBox = stepPauseTimeUnitCombos.get(i);
            final int phaseIndex = i;

            comboBox.addItemListener(new ItemListener() 
            {                    
                @Override
                public void itemStateChanged(ItemEvent e) {
                    StandardTimeUnit pauseUnitNew = comboBox.getItemAt(comboBox.getSelectedIndex());
                    model.setPauseBetweenStepsUnit(phaseIndex, pauseUnitNew);
                }
            });
        }


        for(int i = from; i< to; i++)
        {
            JSpinner recodingTimeSpinner = stepRecordingTimeSpinners.get(i);
            final int phaseIndex = i;

            recodingTimeSpinner.addChangeListener(new ChangeListener() 
            {          
                @Override
                public void stateChanged(ChangeEvent evt)
                {
                    double recordingTimeNew = ((SpinnerNumberModel)recodingTimeSpinner.getModel()).getNumber().doubleValue();
                    model.setStepRecordingTime(phaseIndex, recordingTimeNew);                       
                }
            });
        }

        for(int i = from; i < to; i++)
        {
            JComboBox<StandardTimeUnit> comboBox = stepRecordingTimeUnitCombos.get(i);
            final int phaseIndex = i;

            comboBox.addItemListener(new ItemListener() 
            {                    
                @Override
                public void itemStateChanged(ItemEvent e) {
                    StandardTimeUnit recordingTimeUnitNew = comboBox.getItemAt(comboBox.getSelectedIndex());
                    model.setStepRecordingTimeUnit(phaseIndex, recordingTimeUnitNew);
                }
            });
        }
    }

    private void initListenersForPermanentComponents()
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

        voltageToIntensityConversionFactorSpinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) 
            {
                double conversionFactorNew = ((SpinnerNumberModel)voltageToIntensityConversionFactorSpinner.getModel()).getNumber().doubleValue();
                model.setIntensityUnitsPerVolt(conversionFactorNew);
            }
        });

        comboIrradianceUnitType.addItemListener(new ItemListener() {            
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                IrradianceUnitType irradianceUnitTypeNew = comboIrradianceUnitType.getItemAt(comboIrradianceUnitType.getSelectedIndex());
                model.setAbsoluteLightIntensityUnitType(irradianceUnitTypeNew);
            }
        });

        promptForFilterChangeBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setPromptForFilterChange(selected);              
            }
        });
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        JButton buttonSaveToFile = new JButton(saveToFileAction);
        JButton buttonReadFromFile = new JButton(readFromFileAction);
        JButton buttonClose = new JButton(closeAction);

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonSaveToFile).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,10,20)
                .addComponent(buttonReadFromFile).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,10,20)
                .addComponent(buttonClose));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonSaveToFile)
                .addComponent(buttonReadFromFile)
                .addComponent(buttonClose));

        layout.linkSize(buttonSaveToFile, buttonSaveToFile);

        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
    }

    private class SaveToFileAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SaveToFileAction()
        {           
            putValue(NAME, "Save");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            int op = fileChooser.showSaveDialog(actinicBeamCalibrationPointsPanel); 

            if (op == JFileChooser.APPROVE_OPTION) 
            {
                File fileNew = fileChooser.getSelectedFile();

                try {
                    model.saveToFile(fileNew);
                } catch (UserCommunicableException e) 
                {
                    JOptionPane.showMessageDialog(actinicBeamCalibrationPointsPanel, "An error occured during reading actinic beam calibration", "", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }
    }


    private class ReadFromFileAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ReadFromFileAction()
        {           
            putValue(NAME, "Read from file");
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            int op = fileChooser.showOpenDialog(actinicBeamCalibrationPointsPanel); 

            if (op == JFileChooser.APPROVE_OPTION) 
            {
                File fileNew = fileChooser.getSelectedFile();

                //                try {
                //                    model.readInActinicCalibrationPhasesFromFile(fileNew);
                //                } catch (UserCommunicableException e) 
                //                {
                //                    JOptionPane.showMessageDialog(actinicBeamPhasesPanel, "An error occured during reading actinic beam calibration", "", JOptionPane.ERROR_MESSAGE);
                //                    e.printStackTrace();
                //                }
            }
        }
    }

    private class CloseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CloseAction()
        {           
            putValue(NAME, "Close");
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            model.close();
            dialog.setVisible(false);
        }
    }

    private void initModelListener()
    {
        model.addListener(new ActinicBeamAutomaticCalibrationModelListener()
        {               


            @Override
            public void intensityPerVoltChanged(double intensityPerVoltOld, double intensityPerVoltNew) 
            {
                double spinnerOldValue = ((SpinnerNumberModel)voltageToIntensityConversionFactorSpinner.getModel()).getNumber().doubleValue();
                if(Double.compare(spinnerOldValue, intensityPerVoltNew) != 0)
                {
                    voltageToIntensityConversionFactorSpinner.setValue(intensityPerVoltNew);
                }                  
            }

            @Override
            public void absoluteLightIntensityUnitChanged(IrradianceUnitType absoluteLightIntensityUnitOld, IrradianceUnitType absoluteLightIntensityUnitNew)
            {
                if(!Objects.equals(absoluteLightIntensityUnitNew, comboIrradianceUnitType.getSelectedItem()))
                {
                    comboIrradianceUnitType.setSelectedItem(absoluteLightIntensityUnitNew);
                } 
            }

            @Override
            public void numberOfPhasesChanged(int oldNumber, int newNumber) 
            {
                if(oldNumber != newNumber)
                {
                    spinnerPhaseCount.setValue(newNumber);     

                    if(newNumber > oldNumber)
                    {
                        addPhaseDependentComponents(oldNumber, newNumber);                            
                    }
                    else
                    {
                        removePhaseDependentComponents(oldNumber, newNumber);
                    }

                    actinicBeamCalibrationPointsPanel.revalidate();
                    actinicBeamCalibrationPointsPanel.repaint();
                }
            }

            @Override
            public void saveToFileEnabledChanged(boolean enabledOld, boolean enabledNew) 
            {
                if(enabledOld != enabledNew)
                {
                    saveToFileAction.setEnabled(enabledNew);
                }
            }

            @Override
            public void filterPromptingChanged(boolean promptForFilterChangeOld, boolean promptForFilterChangeNew)
            {
                boolean selectedOld = promptForFilterChangeBox.isSelected();
                if(selectedOld != promptForFilterChangeNew)
                {
                    promptForFilterChangeBox.setSelected(promptForFilterChangeNew);
                }
            }

            @Override
            public void filterChanged(int phaseIndex, SliderMountedFilter durationUnitOld, SliderMountedFilter filterNew) 
            {
                JComboBox<SliderMountedFilter> combo = filterComboBoxes.get(phaseIndex);
                if(!Objects.equals(filterNew, combo.getSelectedItem()))
                {
                    combo.setSelectedItem(filterNew);
                }
            }

            @Override
            public void minimumActinicIntensityInPercentChanged(int phaseIndex, double minimumIntensityInPercentOld,
                    double minimumIntensityInPercentNew)
            {
                JSpinner spinner = minimumIntensityInPercentsSpinners.get(phaseIndex);
                double spinnerOldValue = ((SpinnerNumberModel)spinner.getModel()).getNumber().doubleValue();
                if(Double.compare(spinnerOldValue, minimumIntensityInPercentNew) != 0)
                {
                    spinner.setValue(minimumIntensityInPercentNew);
                }
            }

            @Override
            public void maximumActinicIntensityInPercentChanged(int phaseIndex, double maximumIntensityInPercentOld, double maximumIntensityInPercentNew) 
            {
                JSpinner spinner = maximumIntensityInPercentsSpinners.get(phaseIndex);
                double spinnerOldValue = ((SpinnerNumberModel)spinner.getModel()).getNumber().doubleValue();
                if(Double.compare(spinnerOldValue, maximumIntensityInPercentNew) != 0)
                {
                    spinner.setValue(maximumIntensityInPercentNew);
                }
            }

            @Override
            public void stepRecordingTimeChanged(int phaseIndex, double stepRecordingTimeOld, double stepRecordingTimeNew) 
            {
                JSpinner spinner = stepRecordingTimeSpinners.get(phaseIndex);
                double spinnerOldValue = ((SpinnerNumberModel)spinner.getModel()).getNumber().doubleValue();
                if(Double.compare(spinnerOldValue, stepRecordingTimeNew) != 0)
                {
                    spinner.setValue(stepRecordingTimeNew);
                }
            }

            @Override
            public void unitOfStepRecordingTimeChanged(int phaseIndex, StandardTimeUnit stepRecordingTimeUnitOld, StandardTimeUnit stepRecordingTimeUnitNew) 
            {
                JComboBox<StandardTimeUnit> combo = stepRecordingTimeUnitCombos.get(phaseIndex);
                if(!Objects.equals(stepRecordingTimeUnitNew, combo.getSelectedItem()))
                {
                    combo.setSelectedItem(stepRecordingTimeUnitNew);
                }                  
            }

            @Override
            public void pauseBetweenStepsChanged(int phaseIndex, double pauseBetweenStepsOld, double pauseBetweenStepsNew)
            {
                JSpinner spinner = stepPauseTimeSpinners.get(phaseIndex);
                double spinnerOldValue = ((SpinnerNumberModel)spinner.getModel()).getNumber().doubleValue();
                if(Double.compare(spinnerOldValue, pauseBetweenStepsNew) != 0)
                {
                    spinner.setValue(pauseBetweenStepsNew);
                }                
            }

            @Override
            public void unitOfPauseBetweenStepsChanged(int phaseIndex, StandardTimeUnit pauseBetweenStepsUnitOld, StandardTimeUnit pauseBetweenStepsUnitNew) 
            {
                JComboBox<StandardTimeUnit> combo = stepPauseTimeUnitCombos.get(phaseIndex);
                if(!Objects.equals(pauseBetweenStepsUnitNew, combo.getSelectedItem()))
                {
                    combo.setSelectedItem(pauseBetweenStepsUnitNew);
                }                   
            }

            @Override
            public void scaleTypeOfStepsChanged(int phaseIndex, ScaleType stepScaleTypeOld, ScaleType stepScaleTypeNew) 
            {
                JComboBox<ScaleType> combo = stepScaleTypeCombos.get(phaseIndex);
                if(!Objects.equals(stepScaleTypeNew, combo.getSelectedItem()))
                {
                    combo.setSelectedItem(stepScaleTypeNew);
                }                
            }

            @Override
            public void stepCountChanged(int phaseIndex, int stepCountOld, int stepCountNew) 
            {
                JSpinner spinner = stepPauseTimeSpinners.get(phaseIndex);
                int spinnerOldValue = ((SpinnerNumberModel)spinner.getModel()).getNumber().intValue();
                if(spinnerOldValue != stepCountNew)
                {
                    spinner.setValue(stepCountNew);
                }                     
            }
        });
    }
}