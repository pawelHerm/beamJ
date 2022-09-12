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

public class ActinicBeamManualCalibrationGUI
{
    private static final Preferences PREF = Preferences.userNodeForPackage(ActinicBeamManualCalibrationGUI.class).node(ActinicBeamManualCalibrationGUI.class.getName());

    private final static int DEFAULT_MINIMAL_CALIBRATION_POINT_COUNT = 1;
    private final static int DEFAULT_MAXIMAL_CALIBRATION_POINT_COUNT = 10000;

    private final JComboBox<IrradianceUnitType> comboIrradianceUnitType = new JComboBox<>(IrradianceUnitType.values());

    private final JSpinner spinnerPhaseCount;

    private List<JLabel> calibrationPointLabels = new ArrayList<>();
    private List<JSpinner> intensityInPercentsSpinners = new ArrayList<>();
    private List<JSpinner> intensityInAbsoluteUnitSpinners = new ArrayList<>();
    private List<JComboBox<SliderMountedFilter>> filterComboBoxes = new ArrayList<>();

    private final SubPanel actinicBeamCalibrationPointsPanel = new SubPanel();

    private final ExtensionFileChooser fileChooser = new ExtensionFileChooser(PREF, "Actinic beam calibration", "abc", true);

    private final Action saveToFileAction = new SaveToFileAction();
    private final Action readFromFileAction = new ReadFromFileAction();
    private final Action closeAction = new CloseAction();

    private final ActinicBeamManualCalibrationModel model;

    private final JDialog dialog;

    public ActinicBeamManualCalibrationGUI(Window parent, ActinicBeamManualCalibrationModel model)
    {
        this.model = model;
        this.dialog = new JDialog(parent, "Actinic beam calibration assistant", ModalityType.APPLICATION_MODAL);
        this.spinnerPhaseCount = new JSpinner(new SpinnerNumberModel(model.getCalibrationPointCount(), DEFAULT_MINIMAL_CALIBRATION_POINT_COUNT, DEFAULT_MAXIMAL_CALIBRATION_POINT_COUNT, 1));
        this.comboIrradianceUnitType.setSelectedItem(this.model.getAbsoluteLightIntensityUnit());

        buildActinicBeamSettingsPanel();

        initComponentEnabledState();

        initModelListener();

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.add(actinicBeamCalibrationPointsPanel, BorderLayout.NORTH);

        JScrollPane scrollFrame = new JScrollPane(innerPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollFrame.setAutoscrolls(true);
        scrollFrame.setPreferredSize(new Dimension(500,400));     

        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.add(scrollFrame, BorderLayout.NORTH);
        outerPanel.add(buildButtonPanel(), BorderLayout.SOUTH);

        Container content = dialog.getContentPane();
        content.add(outerPanel, BorderLayout.CENTER);

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
        actinicBeamCalibrationPointsPanel.addComponent(new JLabel("Phase count"), 0, 0, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, 1, 1,new Insets(0, 5, 15, 0));
        actinicBeamCalibrationPointsPanel.addComponent(spinnerPhaseCount, 1, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 15, 5));           

        actinicBeamCalibrationPointsPanel.addComponent(new JLabel("Intensity unit"), 2, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE, 1, 1,new Insets(0, 5, 15, 0));
        actinicBeamCalibrationPointsPanel.addComponent(comboIrradianceUnitType, 3, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 15, 5));           

        actinicBeamCalibrationPointsPanel.addComponent(new JLabel("Intensity (%)"), 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 5, 5));           
        actinicBeamCalibrationPointsPanel.addComponent(new JLabel("Filter position"), 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1,new Insets(0, 5, 5, 5));
        actinicBeamCalibrationPointsPanel.addComponent(new JLabel("Absolute intensity"), 3, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1,new Insets(0, 5, 5, 5));

        initListenersForPermanentComponents();

        int phaseCount = model.getCalibrationPointCount();
        addPhaseDependentComponents(0, phaseCount);

        actinicBeamCalibrationPointsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
    }

    private void addPhaseDependentComponents(int oldComponentCount,int newComponentCount)
    {
        int layoutMaxRowOld = actinicBeamCalibrationPointsPanel.getMaxRow();

        for(int i = oldComponentCount; i< newComponentCount;i++)
        {                            
            double lightIntensityInAbsoluteUnits = model.getLightIntensityInAbsoluteUnits(i);
            double lightIntensityInPercents = model.getLightIntensityInPercent(i);
            SliderMountedFilter filter = model.getActinicBeamFilter(i);

            JLabel phaseLabel = new JLabel("Phase " + Integer.toString(i + 1));
            JSpinner spinnerIntensityInPercent = new JSpinner(new SpinnerNumberModel(lightIntensityInPercents, 0, 100, 1.0));
            JSpinner spinnerLightIntensityInAbsoluteUnits = new JSpinner(new SpinnerNumberModel(lightIntensityInAbsoluteUnits, 0, 100000, 1.0));
            JComboBox<SliderMountedFilter> comboBoxTimeUnit = new JComboBox<>();
            comboBoxTimeUnit.setSelectedItem(filter);

            calibrationPointLabels.add(phaseLabel);
            intensityInPercentsSpinners.add(spinnerIntensityInPercent);
            filterComboBoxes.add(comboBoxTimeUnit);
            intensityInAbsoluteUnitSpinners.add(spinnerLightIntensityInAbsoluteUnits);

            actinicBeamCalibrationPointsPanel.addComponent(phaseLabel, 0,layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(0, 0, 5, 5));
            actinicBeamCalibrationPointsPanel.addComponent(spinnerIntensityInPercent, 1, layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 5));
            actinicBeamCalibrationPointsPanel.addComponent(comboBoxTimeUnit, 2, layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 5, 5));
            actinicBeamCalibrationPointsPanel.addComponent(spinnerLightIntensityInAbsoluteUnits, 3, layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 0));           
        }

        initListenersForPhaseNumberDependentComponents(oldComponentCount,  newComponentCount);

        actinicBeamCalibrationPointsPanel.revalidate();
        actinicBeamCalibrationPointsPanel.repaint();
        dialog.pack();
    }

    private void removePhaseDependentComponents(int oldComponentCount,int newComponentCount)
    {
        for(int i = newComponentCount; i < oldComponentCount;i++)
        {
            JLabel labelPhase = calibrationPointLabels.get(i);
            JSpinner spinnerIntensityInAbsoluteUnit = intensityInAbsoluteUnitSpinners.get(i);
            JSpinner spinnerIntensityInPercent = intensityInPercentsSpinners.get(i);
            JComboBox<SliderMountedFilter> comboBoxFilter = filterComboBoxes.get(i);

            actinicBeamCalibrationPointsPanel.remove(labelPhase);
            actinicBeamCalibrationPointsPanel.remove(spinnerIntensityInAbsoluteUnit);
            actinicBeamCalibrationPointsPanel.remove(comboBoxFilter);
            actinicBeamCalibrationPointsPanel.remove(spinnerIntensityInPercent);              
        }

        this.calibrationPointLabels = calibrationPointLabels.subList(0, newComponentCount);
        this.intensityInAbsoluteUnitSpinners = intensityInAbsoluteUnitSpinners.subList(0, newComponentCount);
        this.intensityInPercentsSpinners = intensityInPercentsSpinners.subList(0, newComponentCount);
        this.filterComboBoxes = filterComboBoxes.subList(0, newComponentCount);

        //https://docs.oracle.com/javase/tutorial/uiswing/components/jcomponent.html#custompaintingapi
        actinicBeamCalibrationPointsPanel.revalidate();
        actinicBeamCalibrationPointsPanel.repaint();
    }

    //from - inclusive, to - exclusive
    private void initListenersForPhaseNumberDependentComponents(int from, int to)
    {
        for(int i = from; i< to; i++)
        {
            JSpinner phaseIntensityAbsoluteSpinner = intensityInAbsoluteUnitSpinners.get(i);
            final int phaseIndex = i;

            phaseIntensityAbsoluteSpinner.addChangeListener(new ChangeListener() 
            {          
                @Override
                public void stateChanged(ChangeEvent evt)
                {
                    double phasIntensityNew = ((SpinnerNumberModel)phaseIntensityAbsoluteSpinner.getModel()).getNumber().doubleValue();
                    model.setLightIntensityInAbsoluteUnits(phaseIndex, phasIntensityNew);                       
                }
            });
        }

        for(int i = from; i < to; i++)
        {
            JSpinner phaseIntensityInPercentsSpinner = intensityInPercentsSpinners.get(i);
            final int phaseIndex = i;

            phaseIntensityInPercentsSpinner.addChangeListener(new ChangeListener() 
            {          
                @Override
                public void stateChanged(ChangeEvent evt)
                {
                    double phaseIntensityNew = ((SpinnerNumberModel)phaseIntensityInPercentsSpinner.getModel()).getNumber().doubleValue();
                    model.setLightIntensityInPercent(phaseIndex, phaseIntensityNew);
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

    private void initListenersForPermanentComponents()
    {
        spinnerPhaseCount.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int phaseCountNew = ((SpinnerNumberModel)spinnerPhaseCount.getModel()).getNumber().intValue();
                model.setCalibrationPointCount(phaseCountNew);
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

                try {
                    model.readInActinicCalibrationPointsFromFile(fileNew);
                } catch (UserCommunicableException e) 
                {
                    JOptionPane.showMessageDialog(actinicBeamCalibrationPointsPanel, "An error occured during reading actinic beam calibration", "", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
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
        model.addListener(new ActinicBeamCalibrationModelListener()
        {               
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
            public void absoluteLightIntensityUnitChanged(IrradianceUnitType absoluteLightIntensityUnitOld, IrradianceUnitType absoluteLightIntensityUnitNew)
            {
                if(!Objects.equals(absoluteLightIntensityUnitNew, comboIrradianceUnitType.getSelectedItem()))
                {
                    comboIrradianceUnitType.setSelectedItem(absoluteLightIntensityUnitNew);
                } 
            }

            @Override
            public void numberOfCalibrationPointsChanged(int oldNumber, int newNumber) 
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
            public void actinicLightIntensityInPercentChanged(int phaseIndex,
                    double intensityInPercentOld, double intensityInPercentNew) 
            {
                JSpinner spinner = intensityInPercentsSpinners.get(phaseIndex);
                double spinnerOldValue = ((SpinnerNumberModel)spinner.getModel()).getNumber().doubleValue();
                if(Double.compare(spinnerOldValue, intensityInPercentNew) != 0)
                {
                    spinner.setValue(intensityInPercentNew);
                }
            }

            @Override
            public void lightIntensityInAbsoluteUnitsChanged(int phaseIndex,
                    double intensityOld, double intensityNew) 
            {
                JSpinner spinner = intensityInAbsoluteUnitSpinners.get(phaseIndex);
                double spinnerOldValue = ((SpinnerNumberModel)spinner.getModel()).getNumber().doubleValue();
                if(Double.compare(spinnerOldValue, intensityNew) != 0)
                {
                    spinner.setValue(intensityNew);
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
        });
    }
}