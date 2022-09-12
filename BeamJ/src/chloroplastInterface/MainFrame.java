
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

package chloroplastInterface;

import static atomicJ.gui.PreferenceKeys.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.*;

import atomicJ.analysis.*;
import atomicJ.data.SampleCollection;
import atomicJ.gui.AboutDialog;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.Channel1DGraphsSupervisor;
import atomicJ.gui.Channel1DResultPanel;
import atomicJ.gui.Channel1DResultsView;
import atomicJ.gui.ConcurrentPreviewTask;
import atomicJ.gui.ConcurrentPreviewTask.SourcePreviewerHandle;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.FileOpeningWizard;
import atomicJ.gui.GeneralPreferencesDialog;
import atomicJ.gui.OpeningModelStandard;
import atomicJ.gui.ResourcePresentationView;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.curveProcessing.CurveVisualizationHandle;
import atomicJ.gui.curveProcessing.NumericalResultsHandler;
import atomicJ.gui.curveProcessing.PreprocessCurvesHandle;
import atomicJ.gui.curveProcessing.CurveVisualizationStandardHandle;
import atomicJ.gui.curveProcessing.NumericalResultsStandardHandle;
import atomicJ.gui.histogram.TransformableHistogramDialog;
import atomicJ.gui.results.ResultDataModel;
import atomicJ.gui.results.ResultView;
import atomicJ.gui.results.ResultTable;
import atomicJ.gui.results.ResultTableModelPhotometric;
import atomicJ.gui.statistics.StatisticsTable;
import atomicJ.utilities.LinkRunner;


public class MainFrame extends JFrame implements ResultDestinationPhotometric, ProcessingOrigin<SimplePhotometricSource,ProcessedPackPhotometric>
{
    private static final long serialVersionUID = 1L;
    private static final String PAPER_LINK = "http://dx.doi.org/10.1063/1.4881683";

    private final static Preferences PREF = Preferences.userNodeForPackage(MainFrame.class).node("MainFrame");

    private static final boolean DESKTOP_AVAILABLE = Desktop.isDesktopSupported()&&(!GraphicsEnvironment.isHeadless());

    private Desktop desktop;
    private boolean mailSupported;
    private boolean openSupported;

    private final Action processAction = new ProcessAction();
    private final Action openAction = new OpenAction();
    private final Action calibrateActinicBeamManuallyAction = new CalibrateActinicBeamManuallyAction();
    private final Action calibrateActinicBeamAutomaticallyAction = new CalibrateActinicBeamAutomaticallyAction();
    private final Action filterSetupAction = new FilterSetupAction();

    private final Action curvesAction = new CurvesAction(); 
    private final Action graphsAction = new GraphicalResultsAction(); 
    private final Action resultsAction = new NumericalResultsAction();
    private final Action statisticsAction = new StatisticsAction();
    private final Action showResultHistogramsAction = new ResultHistogramsAction();
    private final Action parallelComputationPreferencesAction = new ParallelComputationPreferencesAction();

    private final Action aboutAction = new AboutAction();
    private final Action manualAction = new ManualAction();
    private final Action questionAction = new QuestionAction();
    private final Action exitAction = new ExitAction();

    private final AboutDialog infoDialog = new AboutDialog(MainFrame.this);
    private final ResultView<SimplePhotometricSource, ProcessedPackPhotometric> resultsDialog = new ResultView<>(new ResultTable<>(new ResultTableModelPhotometric(new ResultDataModel<>(Collections.emptyList())), this), this);
    private final Channel1DResultsView<ProcessedResourcePhotometric,SimplePhotometricSource> graphsDialog = new Channel1DResultsView<>(this, ProcessedResourcePhotometric.getDefaultTypes(), new Channel1DResultPanel.Channel1DPanelFactory<Channel1DGraphsSupervisor>());
    private final TransformableHistogramDialog resultsHistogramDialog = new TransformableHistogramDialog(this, "Result histograms");
    private final CurvesView previewDialog = new CurvesView(this);
    private final CurvesView preprocessDialog = new CurvesView(this);

    private final JCheckBoxMenuItem itemCharts = new JCheckBoxMenuItem(graphsAction);
    private final JCheckBoxMenuItem itemCalculations = new JCheckBoxMenuItem(resultsAction);
    private final JCheckBoxMenuItem itemPreview = new JCheckBoxMenuItem(curvesAction);
    private final JMenuItem itemParallelComputationPreferences = new JMenuItem(parallelComputationPreferencesAction);

    private final PreviewDestination<PhotometricResource, ChannelChart<?>> photometricPreviewDestination = new PhotometricPreviewDestination();
    private final FileOpeningWizard<SimplePhotometricSource> openingWizard = new FileOpeningWizard<>(new OpeningModelStandard<>(new PhotometricSourcePreviewerHandle(photometricPreviewDestination)), PhotometricCurveReadingModel.getInstance());

    private ProcessingWizardPhotometric processingWizard;

    private final GeneralPreferencesDialog parallelismPeferencesDialog = new GeneralPreferencesDialog(this, "Parallel computation preferences");
    private final ResultBatchesCoordinator resultBatchesCoordinator = new ResultBatchesCoordinator();

    private final OpticsConfigurationModel opticsConfigurationModel = new OpticsConfigurationModel();
    private final OpticsConfigurationGUI opticsConfigurationGUI = new OpticsConfigurationGUI(this, opticsConfigurationModel);

    private final RecordingModel photometricModel = new RecordingModel(opticsConfigurationModel);
    private final ActinicBeamManualCalibrationModel actinicBeamManualCalibrationModel = new ActinicBeamManualCalibrationModel();
    private final ActinicBeamManualCalibrationGUI actinicBeamManualCalibrationGUI = new ActinicBeamManualCalibrationGUI(this,actinicBeamManualCalibrationModel);

    private final ActinicBeamAutomaticCalibrationModel actinicBeamAutomaticCalibrationModel = new ActinicBeamAutomaticCalibrationModel(opticsConfigurationModel);
    private final ActinicBeamAutomaticCalibrationGUI actinicBeamAutomaticCalibrationGUI = new ActinicBeamAutomaticCalibrationGUI(this, actinicBeamAutomaticCalibrationModel);


    public MainFrame()
    {		
        Image icon = Toolkit.getDefaultToolkit().getImage("Resources/Logo.png");
        setIconImage(icon);

        initDesktop();

        curvesAction.setEnabled(false);
        graphsAction.setEnabled(false);
        resultsAction.setEnabled(false);
        statisticsAction.setEnabled(false);
        showResultHistogramsAction.setEnabled(false);

        manualAction.setEnabled(openSupported);
        questionAction.setEnabled(mailSupported);

        initPropertyChangeListeners();

        JMenuBar menuBar = buildMenuBar();
        setJMenuBar(menuBar);

        JToolBar toolBar = buildToolBar();
        add(toolBar, BorderLayout.PAGE_START);

        JPanel southOuterPanel = new JPanel(new BorderLayout());
        southOuterPanel.setBorder(BorderFactory.createEmptyBorder(0, 7, 7, 7));
        southOuterPanel.add(new RecordingSettingsGUI(this.photometricModel).getBeamSettingsPanel(), BorderLayout.CENTER);
        add(southOuterPanel, BorderLayout.CENTER);

        setTitle("JPhotometer");

        initComponentListeners();

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                closeSafely();
            }
        });

        this.preprocessDialog.addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                MainFrame.this.preprocessDialog.clear();
            }
        });

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);	


        int width = PREF.getInt(WINDOW_WIDTH, -1);
        int height = PREF.getInt(WINDOW_HEIGHT, -1);

        if(width <= 0 || height <= 0)
        {            
            setExtendedState(getExtendedState()|Frame.MAXIMIZED_BOTH );
        }
        else 
        {
            setSize(width, height);

            int locationX = Math.max(0, PREF.getInt(WINDOW_LOCATION_X, 0));
            int locationY = Math.max(0,PREF.getInt(WINDOW_LOCATION_Y, 0));

            setLocation(locationX,locationY);
        }		
    }	

    private JMenuBar buildMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createLoweredBevelBorder());
        JMenu menuFile = new JMenu("File");
        JMenu menuControls = new JMenu("Controls");
        JMenu menuHelp = new JMenu("Help");

        JMenuItem itemStartPreview = new JMenuItem(openAction);
        JMenuItem itemProcess = new JMenuItem(processAction);
        JMenuItem itemCalibrateActinicBeamManually = new JMenuItem(calibrateActinicBeamManuallyAction);
        JMenuItem itemCalibrateActinicBeamAutomatically = new JMenuItem(calibrateActinicBeamAutomaticallyAction);
        JMenuItem itemFilters = new JMenuItem(filterSetupAction);

        JMenuItem itemExit = new JMenuItem(exitAction);
        JMenuItem itemAbout = new JMenuItem(aboutAction);
        JMenuItem itemManual = new JMenuItem(manualAction);
        JMenuItem itemQuestion = new JMenuItem(questionAction);

        menuFile.add(itemStartPreview);
        menuFile.add(itemProcess);
        menuFile.add(itemCalibrateActinicBeamManually);
        menuFile.add(itemCalibrateActinicBeamAutomatically);

        menuFile.add(new JSeparator());
        menuFile.add(itemFilters);

        menuFile.add(new JSeparator());
        menuFile.add(itemExit);
        menuFile.setMnemonic(KeyEvent.VK_F);        

        menuControls.add(itemPreview);
        menuControls.add(itemCharts);
        menuControls.add(itemCalculations);

        menuControls.addSeparator();
        menuControls.add(itemParallelComputationPreferences);
        JMenu menuFileFormatPreferences = new JMenu("Format preferences");

        menuControls.add(menuFileFormatPreferences);

        menuControls.setMnemonic(KeyEvent.VK_C);


        menuHelp.add(itemAbout);    
        menuHelp.add(itemManual);
        menuHelp.add(itemQuestion);

        menuHelp.setMnemonic(KeyEvent.VK_H);

        menuBar.add(menuFile);
        menuBar.add(menuControls);
        menuBar.add(menuHelp);

        return menuBar;
    }

    private JToolBar buildToolBar()
    {
        JToolBar toolBar = new JToolBar(SwingConstants.HORIZONTAL);

        JButton buttonProcess = new JButton(processAction);
        JButton buttonStartPreview = new JButton(openAction);
        JButton buttonShowPreview = new JButton(curvesAction);
        JButton buttonCharts = new JButton(graphsAction);
        JButton buttonResults = new JButton(resultsAction);
        JButton buttonAbout = new JButton(aboutAction);

        buttonProcess.setHideActionText(true);
        buttonStartPreview.setHideActionText(true);
        buttonShowPreview.setHideActionText(true);
        buttonCharts.setHideActionText(true);
        buttonResults.setHideActionText(true);
        buttonAbout.setHideActionText(true);

        toolBar.add(buttonProcess);
        toolBar.add(buttonStartPreview);
        toolBar.add(buttonShowPreview);
        toolBar.add(buttonCharts);
        toolBar.add(buttonResults);
        toolBar.add(buttonAbout);

        for(Component c: toolBar.getComponents())
        {
            c.setMaximumSize(new Dimension((int) c.getMaximumSize().getWidth(),Integer.MAX_VALUE));
        }

        return toolBar;
    }


    @Override
    public Frame getPublicationSite()
    {
        return this;
    }

    @Override
    public void startPreview()
    {
        openingWizard.setVisible(true);
    }

    @Override
    public void startPreview(List<SimplePhotometricSource> sources)
    {
        ConcurrentPreviewTask<SimplePhotometricSource> task = new ConcurrentPreviewTask<>(sources, new PhotometricSourcePreviewerHandle(photometricPreviewDestination));			
        task.execute();
    }

    @Override
    public void withdrawPublication()
    {
        JOptionPane.showMessageDialog(this, "Computation terminated", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public ResultBatchesCoordinator getResultBatchesCoordinator()
    {
        return resultBatchesCoordinator;
    }

    @Override
    public void showFigures(boolean b)
    {
        graphsDialog.setVisible(b);
    }

    @Override
    public void showCalculations(boolean b)
    {
        resultsDialog.setVisible(b);
    }

    @Override
    public void showCurves(boolean b)
    {
        previewDialog.setVisible(b);
    }

    @Override
    public void showCalculationsHistograms(boolean b)
    {
        resultsHistogramDialog.setVisible(b);
    }

    public void showPreferences()
    {
        parallelismPeferencesDialog.showDialog();
    }

    public TransformableHistogramDialog getCalculationHistogramDialog()
    {
        return resultsHistogramDialog;
    }

    @Override
    public Channel1DResultsView<ProcessedResourcePhotometric,SimplePhotometricSource> getGraphicalResultsDialog()
    {
        return graphsDialog;
    }

    @Override
    public ResultView<SimplePhotometricSource, ProcessedPackPhotometric> getResultDialog()
    {
        return resultsDialog;
    }

    @Override
    public void startProcessing()
    {
        startProcessing(new ArrayList<SimplePhotometricSource>(), resultBatchesCoordinator.getPublishedBatchCount());	
    }

    @Override
    public void startProcessing(int initialIndex)
    {
        startProcessing(new ArrayList<SimplePhotometricSource>(), initialIndex);
    }

    @Override
    public void startProcessing(List<SimplePhotometricSource> sources, int initialIndex)
    {
        ProcessingModel model = new ProcessingModel(this, getDefaultPreprocessCurvesHandle(), sources, initialIndex);

        processAction.setEnabled(false);

        if(processingWizard == null)
        {
            processingWizard = new ProcessingWizardPhotometric(model);
        }
        else
        {
            processingWizard.setProcessingModel(model);
        }		
        processingWizard.setVisible(true);
    }

    @Override
    public void startProcessing(List<ProcessingBatchModel> batches)
    {
        ProcessingModel model = new ProcessingModel(this,  getDefaultPreprocessCurvesHandle(), batches);

        processAction.setEnabled(false);
        if(processingWizard == null)
        {
            processingWizard = new ProcessingWizardPhotometric(model);
        }
        else
        {
            processingWizard.setProcessingModel(model);
        }	
        processingWizard.setVisible(true);
    }

    @Override
    public void startProcessing(List<ProcessingBatchModel> batches, CurveVisualizationHandle<VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric>> curveVisualizationHandle, NumericalResultsHandler<ProcessedPackPhotometric> numericalResultsHandle)
    {      
        ProcessingModel model = new ProcessingModel(this, getDefaultPreprocessCurvesHandle(), batches);
        model.setCurveVisualizationHandle(curveVisualizationHandle);
        model.setNumericalResultsHandle(numericalResultsHandle);

        processAction.setEnabled(false);
        if(processingWizard == null)
        {
            processingWizard = new ProcessingWizardPhotometric(model);
        }
        else
        {
            processingWizard.setProcessingModel(model);
        }   
        processingWizard.setVisible(true);
    }

    @Override
    public void endProcessing()
    {	
        //we set a new processing model to free memory,as the old ProcessingModel retains much memory, actually all the read in curves and the results of 
        //their processing
        processingWizard.setProcessingModel(new ProcessingModel(this, getDefaultPreprocessCurvesHandle()));
        processAction.setEnabled(true);
    }

    private void initDesktop()
    {
        if(DESKTOP_AVAILABLE)
        {
            desktop = Desktop.getDesktop();
            mailSupported = desktop.isSupported(Desktop.Action.MAIL);
            openSupported = desktop.isSupported(Desktop.Action.OPEN);
        }
    }

    private void closeSafely()
    {
        this.photometricModel.close();

        ResultTable<?, ProcessedPackPhotometric, ?> table = resultsDialog.getResultTable();

        if(!table.isEmpty() && !table.areChangesSaved())
        {
            final JOptionPane pane = new JOptionPane("Some results have not been saved. Do you want to save them now?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);

            JDialog dialog = pane.createDialog(this, "AtomicJ");
            dialog.addWindowListener(new WindowAdapter() 
            {           
                @Override
                public void windowClosing(WindowEvent evt) 
                {
                    pane.setValue(JOptionPane.CANCEL_OPTION);
                }
            });
            dialog.setContentPane(pane);
            dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            dialog.pack();

            dialog.setVisible(true);
            int result = ((Number) pane.getValue()).intValue();
            switch(result)
            {
            case JOptionPane.YES_OPTION: resultsDialog.saveResultTable(); break;
            case JOptionPane.NO_OPTION: break;
            case JOptionPane.CANCEL_OPTION: return;
            }
        }

        PREF.putInt(WINDOW_LOCATION_X, (int) Math.max(0,MainFrame.this.getLocation().getX()));			
        PREF.putInt(WINDOW_LOCATION_Y, (int)  Math.max(0,MainFrame.this.getLocation().getY()));	
        PREF.putInt(WINDOW_WIDTH, Math.max(10, MainFrame.this.getWidth()));
        PREF.putInt(WINDOW_HEIGHT, Math.max(10,MainFrame.this.getHeight()));

        dispose();
        System.exit(0);
    }

    private void initPropertyChangeListeners()
    {
        resultsDialog.addPropertyChangeListener(ResultView.RESULTS_EMPTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                boolean enabled = !(boolean)evt.getNewValue();
                resultsAction.setEnabled(enabled);
                statisticsAction.setEnabled(enabled);                   
            }
        });

        resultsDialog.addPropertyChangeListener(ResultView.RESULT_STATISTICS_DISPLAYED, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                boolean displayed = (boolean)evt.getNewValue();
                statisticsAction.putValue(Action.SELECTED_KEY, displayed);
            }
        });

        resultsHistogramDialog.addPropertyChangeListener(ResourcePresentationView.DIALOG_EMPTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                boolean enabled = !(boolean)evt.getNewValue();
                showResultHistogramsAction.setEnabled(enabled);
                resultsDialog.setHistogramsAvailable(enabled);                                   
            }
        });

        graphsDialog.addPropertyChangeListener(ResourcePresentationView.DIALOG_EMPTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                boolean enabled = !(boolean)evt.getNewValue();
                graphsAction.setEnabled(enabled);
            }
        });

        previewDialog.addPropertyChangeListener(ResourcePresentationView.DIALOG_EMPTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                boolean enabled = !(boolean)evt.getNewValue();
                curvesAction.setEnabled(enabled);
            }
        });
    }

    private void initComponentListeners()
    {
        previewDialog.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent evt)
            {
                curvesAction.putValue(Action.SELECTED_KEY, false);
            }

            @Override
            public void componentShown(ComponentEvent evt)
            {
                curvesAction.putValue(Action.SELECTED_KEY, true);
            }
        });

        graphsDialog.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent evt)
            {
                graphsAction.putValue(Action.SELECTED_KEY, false);
            }

            @Override
            public void componentShown(ComponentEvent evt)
            {
                graphsAction.putValue(Action.SELECTED_KEY, true);
            }
        });

        resultsDialog.addComponentListener(new ComponentAdapter()
        {

            @Override
            public void componentHidden(ComponentEvent evt)
            {
                resultsAction.putValue(Action.SELECTED_KEY, false);
            }

            @Override
            public void componentShown(ComponentEvent evt)
            {
                resultsAction.putValue(Action.SELECTED_KEY, true);
            }
        });

        resultsHistogramDialog.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent evt)
            {
                showResultHistogramsAction.putValue(Action.SELECTED_KEY, false);
            }

            @Override
            public void componentShown(ComponentEvent evt)
            {
                showResultHistogramsAction.putValue(Action.SELECTED_KEY, true);
            }
        });
    }

    public OpticsConfiguration getOpticsConfiguration()
    {
        return opticsConfigurationModel.getOpticsConfiguration();
    }

    private class PhotometricPreviewDestination implements PreviewDestination <PhotometricResource, ChannelChart<?>>
    {
        @Override
        public Window getPublicationSite() 
        {
            return MainFrame.this;
        }

        @Override
        public void publishPreview(Map<PhotometricResource, Map<String, ChannelChart<?>>> charts) 
        {
            if(!charts.isEmpty())
            {
                int previousCount = previewDialog.getResourceCount();
                previewDialog.addResources(charts);
                previewDialog.selectResource(previousCount);

                itemPreview.setSelected(true);
                previewDialog.setVisible(true);
            }
        }

        @Override
        public void requestPreviewEnd() 
        {
            openingWizard.endPreview();                        
        }        
    }

    //    private class SpectroscopyDataPreviewDestination implements PreviewDestination <SpectroscopyBasicResource, ChannelChart<?>>
    //    {
    //        @Override
    //        public Window getPublicationSite() 
    //        {
    //            return MainFrame.this;
    //        }
    //
    //        @Override
    //        public void publishPreview(Map<SpectroscopyBasicResource, Map<String, ChannelChart<?>>> charts) 
    //        {
    //            if(!charts.isEmpty())
    //            {
    //                int previousCount = previewDialog.getResourceCount();
    //                previewDialog.addResources(charts);
    //                previewDialog.selectResource(previousCount);
    //
    //                itemPreview.setSelected(true);
    //                previewDialog.setVisible(true);
    //            }
    //        }
    //
    //        @Override
    //        public void requestPreviewEnd() 
    //        {
    //            openingWizard.endPreview();                        
    //        }        
    //    }

    //    private class ImagePreviewDestination implements PreviewDestination <Channel2DResource, Channel2DChart<?>>
    //    {
    //        @Override
    //        public Window getPublicationSite() 
    //        {
    //            return MainFrame.this;
    //        }
    //
    //        @Override
    //        public void publishPreview(Map<Channel2DResource, Map<String, Channel2DChart<?>>> charts) 
    //        {
    //            if(!charts.isEmpty())
    //            {
    //                imageDialog.addCharts(charts, true);            
    //                itemImages.setSelected(true);
    //                imageDialog.setVisible(true);
    //            }       
    //        }
    //
    //        @Override
    //        public void requestPreviewEnd() 
    //        {
    //            openingWizard.endPreview();            
    //        }     
    //    }


    private class StatisticsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public StatisticsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/Sigma.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(DISPLAYED_MNEMONIC_INDEX_KEY, 7);
            putValue(NAME,"Result statistics");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            resultsDialog.showStatistics(true);
            putValue(SELECTED_KEY, resultsDialog.isStatisticsDisplayed());
        }
    }

    private class ProcessAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ProcessAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/cogWheel.png"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(NAME,"Process");
            putValue(SHORT_DESCRIPTION,"Process force curves and maps");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            startProcessing();
        }
    }

    private class CalibrateActinicBeamManuallyAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CalibrateActinicBeamManuallyAction()
        {
            putValue(NAME,"Actinic manual calibration");
            putValue(SHORT_DESCRIPTION,"Calibrate actinic beam manually");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            actinicBeamManualCalibrationGUI.setDialogVisible(true);
        }
    }

    private class CalibrateActinicBeamAutomaticallyAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CalibrateActinicBeamAutomaticallyAction()
        {
            putValue(NAME, "Actinic automatic calibration");
            putValue(SHORT_DESCRIPTION, "Calibrate actinic beam automatically");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            actinicBeamAutomaticCalibrationGUI.setDialogVisible(true);
        }
    }

    private class FilterSetupAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FilterSetupAction()
        {
            putValue(NAME, "Installed filters");
            putValue(SHORT_DESCRIPTION, "Filters");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            opticsConfigurationGUI.setDialogVisible(true);
        }
    }

    private class OpenAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public OpenAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/preview.png"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(NAME,"Open");
            putValue(SHORT_DESCRIPTION,"Open force curves and images");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            startPreview();
        }
    }


    private class CurvesAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CurvesAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/showPreview.png"));


            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_V);
            putValue(NAME,"Force curves");
            putValue(SHORT_DESCRIPTION,"Previewed force curves");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showCurves(true);
            putValue(SELECTED_KEY, previewDialog.isVisible());
        }
    }

    private class GraphicalResultsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public GraphicalResultsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/graph.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_G);
            putValue(NAME,"Graphical results");
            putValue(SHORT_DESCRIPTION,"Graphical results");

        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showFigures(true);
            putValue(SELECTED_KEY, graphsDialog.isVisible());
        }
    }

    private class NumericalResultsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public NumericalResultsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/results.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_N);
            putValue(NAME,"Numerical results");
            putValue(SHORT_DESCRIPTION,"Show calculated results");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showCalculations(true);
            putValue(SELECTED_KEY, resultsDialog.isVisible());
        }
    }


    private class ResultHistogramsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ResultHistogramsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/Histogram.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_H);
            putValue(NAME,"Result histograms");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showCalculationsHistograms(true);
            putValue(SELECTED_KEY, resultsHistogramDialog.isVisible());
        }
    }


    private class ParallelComputationPreferencesAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ParallelComputationPreferencesAction()
        {    
            putValue(NAME,"Parallel computation");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            showPreferences();
        }
    }

    private class AboutAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public AboutAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/about.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_A);
            putValue(NAME,"About");
            putValue(SHORT_DESCRIPTION,"About AtomicJ");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            infoDialog.setVisible(true);
        }
    }

    private class ManualAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ManualAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(NAME,"Manual");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(openSupported)
            {
                try {

                    File manualFile = new File(AtomicJ.MANUAL_FILE_NAME);
                    if (manualFile.exists()) 
                    {
                        desktop.open(manualFile);
                    } 
                    else 
                    {
                        JOptionPane.showMessageDialog(MainFrame.this, 
                                "The manual file was not found", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
                    }             
                } 
                catch (IOException ex) 
                {
                    JOptionPane.showMessageDialog(MainFrame.this, 
                            "The manual PDF file could not be opened.", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);                 
                }
            }
        }
    }

    private class QuestionAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public QuestionAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_Q);
            putValue(NAME,"Ask a question");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(mailSupported)
            {
                URI mailURI;
                try 
                {
                    mailURI = new URI("mailto", AtomicJ.CONTACT_MAIL + "?subject=AtomicJ question", null);
                    desktop.mail(mailURI);
                }
                catch(IOException e)
                {
                    JOptionPane.showMessageDialog(MainFrame.this, "Error occured during launching the default mail client", "AtomicJ", JOptionPane.ERROR_MESSAGE);

                }
                catch (URISyntaxException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ExitAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ExitAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_E);
            putValue(NAME,"Exit");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            closeSafely();
        }
    }


    @Override
    public boolean containsFiguresForSource(SimplePhotometricSource source)
    {
        return graphsDialog.containsChannelsFromSource(source);
    }

    @Override
    public void showFigures(SimplePhotometricSource source) throws UserCommunicableException 
    {
        graphsDialog.selectResourceContainingChannelsFrom(source);
        showFigures(true);           
    }

    @Override
    public void showFigures(ProcessedPackPhotometric pack) throws UserCommunicableException 
    {
        if(graphsDialog.containsChannelsFromSource(pack.getSource()))
        {
            graphsDialog.selectResourceContainingChannelsFrom(pack.getSource());
        }
        else
        {
            VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric> visualizedPack = pack.visualize(new VisualizationSettingsPhotometric());
            List<VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric>> visualizablePacks = Collections.singletonList(visualizedPack);
            getDefaultCurveVisualizationHandle().handlePublicationRequest(visualizablePacks);
        }
        showFigures(true);      
    }

    @Override
    public void showResults(SimplePhotometricSource source) throws UserCommunicableException 
    {
        resultsDialog.selectSources(Collections.singletonList(source));
        showCalculations(true);
    }

    @Override
    public void showResults(List<SimplePhotometricSource> sources) throws UserCommunicableException 
    {
        resultsDialog.selectSources(sources);
        showCalculations(true);
    }


    public static class LinkMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            JLabel l = (JLabel) evt.getSource();
            try {
                URI uri = new java.net.URI(PAPER_LINK);
                (new LinkRunner(uri)).execute();
            } catch (URISyntaxException use) {
                throw new AssertionError(use + ": " + l.getText()); //NOI18N
            }
        }
    }

    @Override
    public CurveVisualizationHandle<VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric>> getDefaultCurveVisualizationHandle() {
        return new CurveVisualizationStandardHandle<>(graphsDialog);
    }

    @Override
    public NumericalResultsHandler<ProcessedPackPhotometric> getDefaultNumericalResultsHandler()
    {
        return new NumericalResultsStandardHandle<>(resultsDialog);
    }

    public PreprocessCurvesHandle<SimplePhotometricSource> getDefaultPreprocessCurvesHandle()
    {
        return new PreprocessPhotometricCurvesHandle();
    }

    private class PreprocessPhotometricCurvesHandle implements PreprocessCurvesHandle<SimplePhotometricSource>
    {
        @Override
        public void preprocess(List<SimplePhotometricSource> sources)
        {
            SourcePreviewerHandle<SimplePhotometricSource> previewer = new PhotometricSourcePreviewerHandle(photometricPreviewDestination);
            if(!sources.isEmpty())
            {               
                ConcurrentPreviewTask<SimplePhotometricSource> task = new ConcurrentPreviewTask<>(sources, previewer);          
                task.execute();
            }       
            else
            {
                previewer.showMessage("No file to preprocess");
            }            
        }       
    }


    @Override
    public void requestAdditionOfCalculationHistograms(
            SampleCollection samples) {
        // TODO Auto-generated method stub

    }



    @Override
    public void showTemporaryCalculationStatisticsDialog(
            Map<String, StatisticsTable> tables, String title) {
        // TODO Auto-generated method stub

    }



    @Override
    public void showRecalculationDialog(
            ResultDataModel<SimplePhotometricSource, ProcessedPackPhotometric> dataModel) {
        // TODO Auto-generated method stub

    }
}