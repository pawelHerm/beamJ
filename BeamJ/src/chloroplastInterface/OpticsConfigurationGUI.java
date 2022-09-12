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
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
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
import atomicJ.gui.NumericalField;
import atomicJ.gui.SpinnerDoubleModel;
import atomicJ.gui.SubPanel;
import atomicJ.gui.UserCommunicableException;

public class OpticsConfigurationGUI
{
    private static final Preferences PREF = Preferences.userNodeForPackage(OpticsConfigurationGUI.class).node(OpticsConfigurationGUI.class.getName());

    private final static int DEFAULT_MINIMAL_ACTINIC_BEAM_FILTER_COUNT = 1;
    private final static int DEFAULT_MAXIMAL_ACTINIC_BEAM_FILTER_COUNT = 10000;

    private final JSpinner spinnerActinicBeamFilterCount;

    private List<JLabel> filterPositionLabels = new ArrayList<>();
    private List<JSpinner> transmittanceInPercentsSpinners = new ArrayList<>();
    private List<NumericalField> transmittanceInPercentsFields = new ArrayList<>();

    private List<JLabel> opticalDensityLabels = new ArrayList<>();
    private final NumberFormat opticalDensityFormat = NumberFormat.getInstance(Locale.US);

    private final SubPanel actinicBeamFiltersPanel = new SubPanel();

    private final ExtensionFileChooser fileChooser = new ExtensionFileChooser(PREF, "Actinic beam calibration", "abc", true);

    private final Action saveToFileAction = new SaveToFileAction();
    private final Action readFromFileAction = new ReadFromFileAction();
    private final Action closeAction = new CloseAction();

    private final OpticsConfigurationModel model;

    private final JDialog dialog;

    public OpticsConfigurationGUI(Window parent, OpticsConfigurationModel model)
    {
        this.model = model;
        this.dialog = new JDialog(parent, "Filters", ModalityType.APPLICATION_MODAL);
        this.spinnerActinicBeamFilterCount = new JSpinner(new SpinnerNumberModel(model.getActinicBeamSliderMountedFilterCount(), DEFAULT_MINIMAL_ACTINIC_BEAM_FILTER_COUNT, DEFAULT_MAXIMAL_ACTINIC_BEAM_FILTER_COUNT, 1));

        buildActinicBeamSettingsPanel();

        initComponentEnabledState();

        initModelListener();

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.add(actinicBeamFiltersPanel, BorderLayout.NORTH);

        JScrollPane scrollFrame = new JScrollPane(innerPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollFrame.setAutoscrolls(true);
        scrollFrame.setPreferredSize(new Dimension(500,400));     

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
        actinicBeamFiltersPanel.addComponent(new JLabel("Filter count"), 0, 0, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, 1, 1,new Insets(0, 5, 15, 0));
        actinicBeamFiltersPanel.addComponent(spinnerActinicBeamFilterCount, 1, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 15, 5));           

        actinicBeamFiltersPanel.addComponent(new JLabel("Transmittance (%)"), 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 5, 5));           
        actinicBeamFiltersPanel.addComponent(new JLabel("Optical density"), 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1,new Insets(0, 5, 5, 5));

        initListenersForPermanentComponents();

        int phaseCount = model.getActinicBeamSliderMountedFilterCount();
        addFilterCountDependentComponents(0, phaseCount);

        actinicBeamFiltersPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
    }

    private void addFilterCountDependentComponents(int oldComponentCount, int newComponentCount)
    {
        int layoutMaxRowOld = actinicBeamFiltersPanel.getMaxRow();

        for(int i = oldComponentCount; i< newComponentCount;i++)
        {                            
            double transmittanceInPercents = model.getTransmittanceInPercent(i);
            double opticalDensity = -Math.log10(transmittanceInPercents*0.01);

            JLabel positionLabel = new JLabel(Integer.toString(i + 1));
            JSpinner spinnerTransmittanceInPercent = new JSpinner(new SpinnerDoubleModel(transmittanceInPercents, 0, 100, 1.0));
            NumericalField fieldTransmittanceInPercent = new NumericalField("Transmittance must be a number between 0 and 100", 0., 100.);
            fieldTransmittanceInPercent.setValue(Double.valueOf(transmittanceInPercents));
            spinnerTransmittanceInPercent.setEditor(fieldTransmittanceInPercent);

            String opticalDensityText = !Double.isNaN(opticalDensity) ? opticalDensityFormat.format(opticalDensity) : "";
            JLabel opticalDensityLabel = new JLabel(opticalDensityText);

            filterPositionLabels.add(positionLabel);
            transmittanceInPercentsSpinners.add(spinnerTransmittanceInPercent);
            transmittanceInPercentsFields.add(fieldTransmittanceInPercent);
            opticalDensityLabels.add(opticalDensityLabel);

            actinicBeamFiltersPanel.addComponent(positionLabel, 0,layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(0, 0, 5, 5));
            actinicBeamFiltersPanel.addComponent(spinnerTransmittanceInPercent, 1, layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0, 5, 5, 5));
            actinicBeamFiltersPanel.addComponent(opticalDensityLabel, 2, layoutMaxRowOld + 1 + i, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1, new Insets(0, 5, 5, 5));
        }

        initListenersForPhaseNumberDependentComponents(oldComponentCount,  newComponentCount);

        actinicBeamFiltersPanel.revalidate();
        actinicBeamFiltersPanel.repaint();
        dialog.pack();
    }

    private void removeFilterCountDependentComponents(int oldComponentCount,int newComponentCount)
    {
        for(int i = newComponentCount; i < oldComponentCount;i++)
        {
            JLabel labelPhase = filterPositionLabels.get(i);
            JSpinner spinnerTransmittanceInPercent = transmittanceInPercentsSpinners.get(i);
            JLabel opticalDensityLabel = opticalDensityLabels.get(i);

            actinicBeamFiltersPanel.remove(labelPhase);
            actinicBeamFiltersPanel.remove(spinnerTransmittanceInPercent);
            actinicBeamFiltersPanel.remove(opticalDensityLabel);
        }

        this.filterPositionLabels = filterPositionLabels.subList(0, newComponentCount);
        this.transmittanceInPercentsSpinners = transmittanceInPercentsSpinners.subList(0, newComponentCount);
        this.transmittanceInPercentsFields = transmittanceInPercentsFields.subList(0, newComponentCount);
        this.opticalDensityLabels = opticalDensityLabels.subList(0, newComponentCount);

        //https://docs.oracle.com/javase/tutorial/uiswing/components/jcomponent.html#custompaintingapi
        actinicBeamFiltersPanel.revalidate();
        actinicBeamFiltersPanel.repaint();
    }

    //from - inclusive, to - exclusive
    private void initListenersForPhaseNumberDependentComponents(int from, int to)
    {       
        for(int i = from; i < to; i++)
        {
            JSpinner transmittanceInPercentsSpinner = transmittanceInPercentsSpinners.get(i);
            final int phaseIndex = i;

            transmittanceInPercentsSpinner.addChangeListener(new ChangeListener() 
            {          
                @Override
                public void stateChanged(ChangeEvent evt)
                {
                    double transmittanceNew = ((SpinnerDoubleModel)transmittanceInPercentsSpinner.getModel()).getDoubleValue();

                    model.setTransmittanceInPercent(phaseIndex, transmittanceNew);
                }
            });



            NumericalField transmittanceInPercentsField = transmittanceInPercentsFields.get(i);
            transmittanceInPercentsField.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener()
            {            
                @Override
                public void propertyChange(PropertyChangeEvent evt) 
                {
                    double valNew = ((Number)evt.getNewValue()).doubleValue();
                    model.setTransmittanceInPercent(phaseIndex, valNew);
                }
            }); 
        }
    }

    private void initListenersForPermanentComponents()
    {
        spinnerActinicBeamFilterCount.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int filterCountNew = ((SpinnerNumberModel)spinnerActinicBeamFilterCount.getModel()).getNumber().intValue();
                model.setActinicBeamSliderMountedFilterCount(filterCountNew);
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
            int op = fileChooser.showSaveDialog(actinicBeamFiltersPanel); 

            if (op == JFileChooser.APPROVE_OPTION) 
            {
                File fileNew = fileChooser.getSelectedFile();

                try {
                    model.saveToFile(fileNew);
                } catch (UserCommunicableException e) 
                {
                    JOptionPane.showMessageDialog(actinicBeamFiltersPanel, "An error occured during reading actinic beam calibration", "", JOptionPane.ERROR_MESSAGE);
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
            int op = fileChooser.showOpenDialog(actinicBeamFiltersPanel); 

            if (op == JFileChooser.APPROVE_OPTION) 
            {
                File fileNew = fileChooser.getSelectedFile();

                try {
                    model.readInActinicCalibrationPhasesFromFile(fileNew);
                } catch (UserCommunicableException e) 
                {
                    JOptionPane.showMessageDialog(actinicBeamFiltersPanel, "An error occured during reading actinic beam calibration", "", JOptionPane.ERROR_MESSAGE);
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
        model.addListener(new OpticsConfigurationListener()
        {            
            @Override
            public void actinicBeamFilterTransmittanceInPercentChanged(int filterIndex, double transmittanceInPercentOld, double transmittanceInPercentNew) 
            {
                JSpinner spinner = transmittanceInPercentsSpinners.get(filterIndex);
                double spinnerOldValue = ((SpinnerDoubleModel)spinner.getModel()).getDoubleValue();


                if(Double.compare(spinnerOldValue, transmittanceInPercentNew) != 0)
                {
                    spinner.setValue(transmittanceInPercentNew);
                    transmittanceInPercentsFields.get(filterIndex).setValue(transmittanceInPercentNew);
                }

                NumericalField transmittanceInPercentsField = transmittanceInPercentsFields.get(filterIndex);
                double fieldOldValue = transmittanceInPercentsField.getValue().doubleValue();

                if(Double.compare(fieldOldValue, transmittanceInPercentNew) != 0)
                {
                    transmittanceInPercentsField.setValue(Double.valueOf(transmittanceInPercentNew));
                }

                double opticalDensity = -Math.log10(transmittanceInPercentNew*0.01);
                JLabel opticalDensityLabel = opticalDensityLabels.get(filterIndex);
                String opticalDensityText = !Double.isNaN(opticalDensity) ? opticalDensityFormat.format(opticalDensity) : "";
                opticalDensityLabel.setText(opticalDensityText);
            }

            @Override
            public void saveToFileEnabledChanged(boolean enabledOld, boolean enabledNew) 
            {

            }

            @Override
            public void numberOfActinicBeamFiltersChanged(int oldNumber, int newNumber) 
            {
                if(oldNumber != newNumber)
                {
                    spinnerActinicBeamFilterCount.setValue(newNumber);     

                    if(newNumber > oldNumber)
                    {
                        addFilterCountDependentComponents(oldNumber, newNumber);                            
                    }
                    else
                    {
                        removeFilterCountDependentComponents(oldNumber, newNumber);
                    }

                    actinicBeamFiltersPanel.revalidate();
                    actinicBeamFiltersPanel.repaint();
                }
            }
        });;
    }
}