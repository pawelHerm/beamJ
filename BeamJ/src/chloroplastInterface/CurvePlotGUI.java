package chloroplastInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jfree.ui.Layer;

import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DCollection;
import atomicJ.gui.Channel1DChart;
import atomicJ.gui.CustomizableValueMarker;
import atomicJ.gui.CustomizableXYPlot;
import atomicJ.gui.ExtensionFileChooser;
import atomicJ.gui.Channel1DPlot;
import atomicJ.gui.MultipleXYChartPanel;
import atomicJ.gui.PreferredMarkerStyle;
import atomicJ.gui.SubPanel;

public class CurvePlotGUI
{
    private static final Preferences CURRENT_PHOTOMETRIC_CURVE_PREF = Preferences.userNodeForPackage(RecordingSettingsGUI.class).node(RecordingSettingsGUI.class.getName());

    private final NumberFormat defaultNumberFormat = NumberFormat.getInstance(Locale.US);

    private final JPanel plotPanel;
    private final JPanel currentTransmittanceAndTimePanel;
    private final SubPanel panelFileAndDescription;

    private List<JLabel> signalNameLabels = new ArrayList<>();
    private List<JLabel> currentSignalValueLabels = new ArrayList<>();

    private final SubPanel signalValuesPanel = new SubPanel(); 

    private final JLabel labelRecordingDuration = new JLabel();
    private final JTextField fieldDestination = new JTextField();
    private final JTextArea areaDescription = new JTextArea();
    private final ExtensionFileChooser fileChooser = new ExtensionFileChooser(CURRENT_PHOTOMETRIC_CURVE_PREF, true);
    private final ExperimentDescriptionDialog descriptionDialog;

    private final Action browseForOutputFileAction = new BrowseAction();

    private Channel1DChart<CustomizableXYPlot> chart;
    private final List<CustomizableValueMarker> phaseMarkers = new ArrayList<>(); //this class has to keep its own list of markers, because if an i-th marker is first removed from plot and than added, its style should not change
    private final RecordingModel model;

    public CurvePlotGUI(RecordingModel model)
    {
        this.model = model;

        this.defaultNumberFormat.setMinimumFractionDigits(2);
        this.defaultNumberFormat.setMaximumFractionDigits(2);

        this.panelFileAndDescription = buildFileAndDescriptionPanel();
        this.plotPanel = buildPlotPanel();
        this.currentTransmittanceAndTimePanel = buildCurrentSignalValueAndTimePanel();
        addSignalCountDependentComponentsToSignalValuesPanel(0, model.getSignalSourcesCount());

        this.descriptionDialog = new ExperimentDescriptionDialog(plotPanel, model.getExperimentDescriptionModel());
        fileChooser.setApproveButtonMnemonic('S');  
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);//order of calls of setDialogType and setApprove button text is important
        fileChooser.setApproveButtonText("Select");

        initComponentEnabledState();
        buildAndAddDataReceiverToModel();
    }

    private void initComponentEnabledState()
    {
        this.browseForOutputFileAction.setEnabled(model.isOutputFileSelectionEnabled());
    }

    private JPanel buildPlotPanel()
    {
        MultipleXYChartPanel<Channel1DChart<CustomizableXYPlot>> plotPanel = new MultipleXYChartPanel<>();

        Channel1DCollection initialDataset = model.getRecordedChannelCollection();

        Channel1DPlot plot = PlotFactoryPhotometric.getInstance().getPlot(initialDataset);
             
        plot.getDomainAxis().setRange(0, model.getTotalDurationOfActinicPhasesInSeconds());

        this.chart = new Channel1DChart<>(plot, PhotometricSourceVisualization.TRANSMITTANCE_CURVE_PLOT);

        plotPanel.addChart(chart);
        plotPanel.setSelectedChart(0);      
        plotPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));
     
        this.phaseMarkers.addAll(PhotometricSourceVisualization.createMarkers(this.model.getRecordingPhases(), CURRENT_PHOTOMETRIC_CURVE_PREF));
       
        for(CustomizableValueMarker marker : phaseMarkers)
        {
            plot.addDomainMarker(marker, Layer.FOREGROUND);
        }

        
        return plotPanel;
    }

    private SubPanel buildFileAndDescriptionPanel() 
    {
        SubPanel panelFile = new SubPanel();
        SubPanel destPanel = new SubPanel();

        JLabel labelDestinationDirectory = new JLabel("File");
        JLabel labelDescription = new JLabel("Description");

        JButton buttonBrowse = new JButton(browseForOutputFileAction);
        JButton buttonEdit = new JButton(new EditDescriptionAction());

        File outputFile = model.getOutputFile();
        String filePath = (outputFile != null) ?  outputFile.getPath() : "";
        fieldDestination.setBorder(BorderFactory.createEmptyBorder());
        fieldDestination.setText(filePath);
        fieldDestination.setEnabled(false);

        String experimentDescription = model.getExperimentDescription();
        areaDescription.setText(experimentDescription);
        areaDescription.setBackground(null);
        areaDescription.setEnabled(false);

        SaveFormatType<PhotometricResource> currentSaveFormat = model.getDataSaveFormatType();
        fileChooser.setEnforcedExtension(currentSaveFormat.getFileNameExtensionFilter());

        destPanel.addComponent(labelDestinationDirectory, 0, 0, 1, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(4, 3, 6, 3));
        destPanel.addComponent(fieldDestination, 1, 0, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1,new Insets(5, 3, 5, 3));
        destPanel.addComponent(buttonBrowse, 2, 0, 1, 1,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 1,new Insets(5, 3, 5, 3));

        destPanel.addComponent(labelDescription, 0, 1, 1, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(4, 3, 6, 3));
        destPanel.addComponent(areaDescription, 1, 1, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1,new Insets(5, 3, 5, 3));
        destPanel.addComponent(buttonEdit, 2, 1, 1, 1,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 1,new Insets(5, 3, 5, 3));

        destPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        panelFile.addComponent(destPanel, 0, 0, 1, 1,GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);

        return panelFile;
    }

    private SubPanel buildCurrentSignalValueAndTimePanel() 
    {
        SubPanel panelCurrentSignalValueAndTime = new SubPanel();

        signalValuesPanel.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1,new Insets(5, 3, 5, 3));
        signalValuesPanel.addComponent(new JLabel("Recording time"), 3, 0, 1, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(5, 3, 5, 3));
        signalValuesPanel.addComponent(labelRecordingDuration, 4, 0, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 1,new Insets(5, 3, 5, 3));

        signalValuesPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        panelCurrentSignalValueAndTime.addComponent(signalValuesPanel, 0, 0, 1, 1,GridBagConstraints.NORTH, GridBagConstraints.NONE, 1, 1);

        return panelCurrentSignalValueAndTime;
    }

    //from inclusive, to exclusive
    private void addSignalCountDependentComponentsToSignalValuesPanel(int from, int to)
    {
        int layoutMaxRowOld = signalValuesPanel.getMaxRow();

        for(int signalIndex = from; signalIndex < to; signalIndex++)
        {
            LightSignalType signalType = model.getSignalType(signalIndex);
            JLabel signalNameLabel = new JLabel("Current " + signalType.getPhysicalPropertyName().toLowerCase()+" (%)");
            signalNameLabels.add(signalNameLabel);

            JLabel currentSignalValueLabel = new JLabel();
            currentSignalValueLabels.add(currentSignalValueLabel);

            signalValuesPanel.addComponent(signalNameLabel, 0, layoutMaxRowOld + signalIndex, 1, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(4, 3, 6, 3));
            signalValuesPanel.addComponent(currentSignalValueLabel, 1, layoutMaxRowOld + signalIndex, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 1,new Insets(5, 3, 5, 3));
        }

        signalValuesPanel.revalidate();
    }

    private void removeSignalCountDependentComponentsToSignalValuesPanel(int oldComponentCount, int newComponentCount)
    {
        if(newComponentCount >= oldComponentCount)
        {
            return;
        }

        for(int i = newComponentCount; i < oldComponentCount;i++)
        {
            JLabel signalNameLabel = signalNameLabels.get(i);
            JLabel currentSignalValueLabel = currentSignalValueLabels.get(i);

            signalValuesPanel.remove(signalNameLabel);
            signalValuesPanel.remove(currentSignalValueLabel);
        }

        this.signalNameLabels = signalNameLabels.subList(0, newComponentCount);
        this.currentSignalValueLabels = currentSignalValueLabels.subList(0, newComponentCount);

        signalValuesPanel.revalidate();
        signalValuesPanel.repaint();
    }

    public JPanel getPlotAndDescriptionPanel()
    {        
        JPanel plotTransmittanceAndFilePanel = new JPanel(new BorderLayout());
        JPanel plotAndTransmittancePanel = new JPanel(new BorderLayout());

        plotAndTransmittancePanel.add(plotPanel, BorderLayout.CENTER);
        plotAndTransmittancePanel.add(currentTransmittanceAndTimePanel, BorderLayout.SOUTH);        

        plotTransmittanceAndFilePanel.add(plotAndTransmittancePanel, BorderLayout.CENTER);
        plotTransmittanceAndFilePanel.add(panelFileAndDescription, BorderLayout.SOUTH);

        return plotTransmittanceAndFilePanel;
    }

    private void browse()
    {
        int op = fileChooser.showDialog(plotPanel,null); //if this is not null, then the JFileChooser code will call setDialogType(CUSTOM_DIALOG)

        if (op == JFileChooser.APPROVE_OPTION) 
        {
            File fileNew = fileChooser.getSelectedFile();
            model.setOutputFile(fileNew);
        }
    }

    private void editDescription()
    {
        descriptionDialog.setVisible(true);
    }

    private class EditDescriptionAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public EditDescriptionAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_E);
            putValue(NAME, "Edit");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            editDescription();
        }
    }

    private class BrowseAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public BrowseAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(NAME, "Browse");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            browse();
        }
    }

    private void buildAndAddDataReceiverToModel()
    {
        this.model.addSampleReceiver(new RecordedDataReceiver() 
        {                        
            @Override
            public void recordedChannelChanged(String channelKey) 
            {
                chart.notifyOfDataChange(channelKey);
            }

            @Override
            public void recordedChannelAdded(Channel1D channel) 
            {                               
                chart.addOrReplaceData(channel);
                chart.setRangeOfRangeAxisForDatasetIfPossible(channel.getIdentifier(), 0, 100);
            }

            @Override
            public void recordedChannelRemoved(String channelKeyOld) 
            {
                chart.removeLayer(channelKeyOld, true);
            }     

            @Override
            public void signalSampleReceived(int signalIndex, double signalValueCalibrated)
            {
                if(signalIndex >= currentSignalValueLabels.size())
                {
                    return;
                }

                JLabel currentSignalValueLabel = currentSignalValueLabels.get(signalIndex);
                currentSignalValueLabel.setText(defaultNumberFormat.format(signalValueCalibrated));
            }

            @Override
            public void recordingDurationSecondsChanged(double durationInSecondsOld, double durationInSecondsNew) 
            {
                if(Double.compare(durationInSecondsOld, durationInSecondsNew) != 0)
                {
                    chart.getCustomizablePlot().getDomainAxis().setRange(0, durationInSecondsNew);
                }
            }

            @Override
            public void numberOfActinicPhasesChanged(int oldNumber, int newNumber) 
            {
                CustomizableXYPlot plot = chart.getCustomizablePlot();

                if(newNumber > oldNumber)
                {
                    //those markers are already contained in the list phaseMarkers of this class
                    for(int i = oldNumber; i<Math.min(phaseMarkers.size(), newNumber);i++)
                    {
                        double value = model.getTotalDurationOfActinicPhasesBeforeIthPhaseInSeconds(i) + model.getDurationOfPhaseInSeconds(i);

                        CustomizableValueMarker marker = phaseMarkers.get(i);
                        marker.setValue(value);

                        plot.addDomainMarker(marker, Layer.FOREGROUND);                     
                    }

                    //those markers are newly created
                    for(int i = phaseMarkers.size(); i<newNumber;i++)
                    {
                        double value = model.getTotalDurationOfActinicPhasesBeforeIthPhaseInSeconds(i) + model.getDurationOfPhaseInSeconds(i);

                        String key = "Marker " + Integer.valueOf(i + 1);
                        PreferredMarkerStyle markerStyle = PreferredMarkerStyle.getInstance(CURRENT_PHOTOMETRIC_CURVE_PREF.node(key), Color.BLUE, PreferredMarkerStyle.SOLID_STROKE, 1.f);
                        CustomizableValueMarker marker = new CustomizableValueMarker(key, value, markerStyle, key);
                        phaseMarkers.add(marker);
                        plot.addDomainMarker(marker, Layer.FOREGROUND);                     
                    }
                }
                else
                {
                    for(int i = Math.min(oldNumber, phaseMarkers.size()) - 1; i >= Math.max(newNumber, 0);i--)
                    { 
                        plot.removeDomainMarker(phaseMarkers.get(i), Layer.FOREGROUND);
                    }

                }
            }

            @Override
            public void phaseDurationChanged(int phaseIndex, double durationOld, double durationNew) 
            {
                for(int i = phaseIndex; i < model.getActinicBeamPhaseCount();i++)
                {
                    double valueNew = model.getTotalDurationOfActinicPhasesBeforeIthPhaseInSeconds(i) + model.getDurationOfPhaseInSeconds(i);
                    phaseMarkers.get(i).setValue(valueNew);//a reference to the same object is held by the plot instance
                }
            }

            @Override
            public void phaseUnitDurationChanged(int phaseIndex, StandardTimeUnit durationUnitOld, StandardTimeUnit durationUnitNew) 
            {
                for(int i = phaseIndex; i < model.getActinicBeamPhaseCount();i++)
                {
                    double valueNew = model.getTotalDurationOfActinicPhasesBeforeIthPhaseInSeconds(i) + model.getDurationOfPhaseInSeconds(i);
                    phaseMarkers.get(i).setValue(valueNew);//a reference to the same object is held by the plot instance
                }                
            }

            @Override
            public void sampleOutputFileChanged(File outputFileOld, File outputFileNew) 
            {
                String path = outputFileNew.getPath();
                fieldDestination.setText(path);                
            }

            @Override
            public void sampleDataFormatTypeChanged(SaveFormatType<PhotometricResource> formatTypeOld, SaveFormatType<PhotometricResource> formatTypeNew) 
            {
                fileChooser.setEnforcedExtension(formatTypeNew.getFileNameExtensionFilter());               
            }

            @Override
            public void experimentDescriptionChanged(String descriptionOld, String descriptionNew) 
            {
                areaDescription.setText(descriptionNew);
            }

            @Override
            public void outputFileSelectionEnabledChange(boolean enabledNew) 
            {
                browseForOutputFileAction.setEnabled(enabledNew);
            }

            @Override
            public void elapsedRecordingTime(long elapsedTimeInMiliseconds) 
            {
                Duration duration = Duration.ofMillis(elapsedTimeInMiliseconds);
                String newDurationText = Long.toString(duration.toHours()) + " h : " + Long.toString(duration.toMinutes()%60) + " min : " + Long.toString(duration.getSeconds()%60) +" s";

                if(!Objects.equals(labelRecordingDuration.getText(), newDurationText))
                {
                    labelRecordingDuration.setText(newDurationText);
                }
            }

            @Override
            public void signalSourcesCountChanged(int signalSourcesCountOld, int signalSourcesCountNew)
            {

                if(signalSourcesCountNew > signalSourcesCountOld)
                {
                    addSignalCountDependentComponentsToSignalValuesPanel(signalSourcesCountOld, signalSourcesCountNew);                   
                }
                else if(signalSourcesCountNew < signalSourcesCountOld)
                {
                    removeSignalCountDependentComponentsToSignalValuesPanel(signalSourcesCountOld, signalSourcesCountNew);
                }                
            }

            @Override
            public void signalTypeChanged(int signalIndex, LightSignalType signalTypeOld, LightSignalType signalTypeNew)
            {
                if(signalIndex >= currentSignalValueLabels.size())
                {
                    return;
                }

                String labelTextNew = "Current " + signalTypeNew.getPhysicalPropertyName().toLowerCase()+" (%)";
                JLabel signalNameLabel = signalNameLabels.get(signalIndex);
                signalNameLabel.setText(labelTextNew);

                JLabel currentSignalValueLabel = currentSignalValueLabels.get(signalIndex);
                currentSignalValueLabel.setText("");//if the signal property changes, we have to clear the field with its previously recorded value

                signalValuesPanel.revalidate();
                signalValuesPanel.repaint();
            }     
        });
    }
}