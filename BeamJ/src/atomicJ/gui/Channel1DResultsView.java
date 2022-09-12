
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

package atomicJ.gui;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.*;

import atomicJ.analysis.*;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.Translate1DTransformation;
import atomicJ.curveProcessing.UndoableCurveCommand;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.ChannelGroupTag;
import atomicJ.data.Datasets;
import atomicJ.data.SampleCollection;
import atomicJ.data.VerticalModificationConstraint1D;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.SpectroscopyResultPanel.SpectroscopyPanelFactory;
import atomicJ.gui.curveProcessingActions.Convolve1DAction;
import atomicJ.gui.curveProcessingActions.Crop1DAction;
import atomicJ.gui.curveProcessingActions.GaussianFilter1DAction;
import atomicJ.gui.curveProcessingActions.LocalRegression1DAction;
import atomicJ.gui.curveProcessingActions.Median1DFilterAction;
import atomicJ.gui.curveProcessingActions.MedianWeightedFilter1DAction;
import atomicJ.gui.curveProcessingActions.SavitzkyGolayFilter1DAction;
import atomicJ.gui.curveProcessingActions.Translate1DAction;
import atomicJ.gui.generalProcessing.ConcurrentTransformationTask;
import atomicJ.gui.generalProcessing.UndoableBasicCommand;
import atomicJ.gui.imageProcessing.UnitManager;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.measurements.DistanceMeasurementStyle;
import atomicJ.gui.measurements.DistanceSimpleMeasurementEditor;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.resources.Channel1DProcessedResource;
import atomicJ.resources.Channel1DResource;
import atomicJ.resources.ChannelResource;
import atomicJ.resources.Resource;
import atomicJ.resources.ResourceView;
import atomicJ.sources.Channel1DSource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.MetaMap;
import chloroplastInterface.Channel1DResultsDialogModel;

public class Channel1DResultsView<R extends Channel1DProcessedResource<S>, S extends Channel1DSource<? extends Channel1D>> extends ResourceXYPresentationView<R, ChannelChart<?>, Channel1DResultPanel<Channel1DGraphsSupervisor>>
implements ResourceReceiver<R>, Channel1DGraphsSupervisor, ResourceView<R, Channel1D, String>
{
    private static final long serialVersionUID = 1L;

    private static final Preferences PREF = Preferences.userNodeForPackage(Channel1DResultsView.class).node("GraphicalResultsDialog");

    private final Action recalculateAction = new RecalculateAction();
    private final Action overlayAction = new OverlayAction();

    private final Action correctContactPointAction = new CorrectContactPointAction();
    private final Action correctTransitionIndentationAction = new CorrectTransitionIndentationAction();
    private final Action correctAdhesionAction = new CorrectAdhesionAction();
    private final Action addAdhesionMeasurementAction = new AddAdhesionMeasurementAction();

    //copied

    private final Action cropAction = new Crop1DAction<>(this);
    private final Action translateAction = new Translate1DAction<>(this);

    private final Action medianFilterAction = new Median1DFilterAction<>(this);
    private final Action medianWeightedFilterAction = new MedianWeightedFilter1DAction<>(this);
    private final Action gaussianFilterAction = new GaussianFilter1DAction<>(this);
    private final Action savitzkyGolayFilterAction = new SavitzkyGolayFilter1DAction<>(this);
    private final Action localRegressionAction = new LocalRegression1DAction<>(this);
    private final Action convolveAction = new Convolve1DAction<>(this);
    private final Action undoAction = new UndoAction();
    private final Action undoAllAction = new UndoAllAction();
    private final Action redoAction = new RedoAction();
    private final Action redoAllAction = new RedoAllAction();

    ///

    private final JMenuItem recalculateItem = new JMenuItem(recalculateAction);
    private final JMenuItem overlayItem = new JMenuItem(overlayAction);


    private final JCheckBoxMenuItem correctContactPointItem = new JCheckBoxMenuItem(correctContactPointAction);
    private final JCheckBoxMenuItem correctTransitionIndentationItem = new JCheckBoxMenuItem(correctTransitionIndentationAction);
    private final JCheckBoxMenuItem correctAdhesionMeasurementItem = new JCheckBoxMenuItem(correctAdhesionAction);

    //copied
    private final JMenuItem cropItem = new JMenuItem(cropAction);
    private final JMenuItem translateItem = new JMenuItem(translateAction);

    private final JMenuItem medianFilterItem = new JMenuItem(medianFilterAction);
    private final JMenuItem medianWeightedFilterItem = new JMenuItem(medianWeightedFilterAction);
    private final JMenuItem gaussianFilterItem = new JMenuItem(gaussianFilterAction);
    private final JMenuItem savitzkyGolayFilterItem = new JMenuItem(savitzkyGolayFilterAction);
    private final JMenuItem localRegressionItem = new JMenuItem(localRegressionAction);
    private final JMenuItem convolveItem = new JMenuItem(convolveAction); 

    private final JMenuItem undoItem = new JMenuItem(undoAction);
    private final JMenuItem undoAllItem = new JMenuItem(undoAllAction);
    private final JMenuItem redoItem = new JMenuItem(redoAction);
    private final JMenuItem redoAllItem = new JMenuItem(redoAllAction);

    private final ResultDestinationBasic<S,?> destination;	
    private final ResourceSelectionWizard resourceSelectionWizard;

    private final FlexibleNumericalTableDialog distanceMeasurementsDialog = new FlexibleNumericalTableDialog(this, new StandardNumericalTable(new DistanceGeometryTableModel(), true, true), "Graphs distance measurements");
    private final DistanceSimpleMeasurementEditor measurementEditor = new DistanceSimpleMeasurementEditor(this);


    public Channel1DResultsView(ResultDestinationBasic<S,?> destination, List<String> initialTypes, AbstractChartPanelFactory<Channel1DResultPanel<Channel1DGraphsSupervisor>> panelFactory)
    {
        super(destination.getPublicationSite(), panelFactory, "Graphical results", PREF, ModalityType.MODELESS, new Channel1DResultsDialogModel<R>());

        this.resourceSelectionWizard = new ResourceSelectionWizard(this, this);
        this.destination = destination;

        for(String type: initialTypes)
        {
            addChartPanelIfAbsent(type);
        }

        buildMenuBar();

        Container content = getContentPane();
        content.setLayout(new BorderLayout());

        JPanel panelResources = getPanelResources();

        JTabbedPane graphicsPane = getTabbedPane();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelResources, graphicsPane);
        splitPane.setOneTouchExpandable(true);

        content.add(splitPane, BorderLayout.CENTER);
        content.add(getMultipleResourcesPanelButtons(),BorderLayout.SOUTH);		

        JToolBar toolBar = buildToolBar();
        add(toolBar, BorderLayout.EAST);

        initInputAndActionMaps();

        initSelectionListener();
        createAndRegisterPopupMenu();
        controlForResourceEmptinessPrivate(isEmpty());	
        checkIfActionsShouldBeEnabled();
    }

    private void initInputAndActionMaps()
    {
        InputMap inputMap = getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW );                

        inputMap.put((KeyStroke) undoAction.getValue(Action.ACCELERATOR_KEY), undoAction.getValue(Action.NAME));  
        inputMap.put((KeyStroke) undoAllAction.getValue(Action.ACCELERATOR_KEY), undoAllAction.getValue(Action.NAME));      
        inputMap.put((KeyStroke) redoAction.getValue(Action.ACCELERATOR_KEY), redoAction.getValue(Action.NAME));      
        inputMap.put((KeyStroke) redoAllAction.getValue(Action.ACCELERATOR_KEY), redoAllAction.getValue(Action.NAME));      

        ActionMap actionMap = getRootPane().getActionMap();

        actionMap.put(undoAction.getValue(Action.NAME), undoAction); 
        actionMap.put(undoAllAction.getValue(Action.NAME), undoAllAction); 
        actionMap.put(redoAction.getValue(Action.NAME), redoAction);   
        actionMap.put(redoAllAction.getValue(Action.NAME), redoAllAction);    
    }

    private void buildMenuBar()
    {
        JMenu chartMenu = getChartMenu();
        JMenuItem rawDataItem = new JMenuItem(getRawDataAction());
        chartMenu.add(rawDataItem);
        chartMenu.add(overlayItem);

        JMenu modifyMenu = new JMenu("Data");
        modifyMenu.setMnemonic(KeyEvent.VK_D);

        modifyMenu.add(undoItem);
        modifyMenu.add(redoItem);
        modifyMenu.addSeparator();

        modifyMenu.add(undoAllItem);
        modifyMenu.add(redoAllItem);
        modifyMenu.addSeparator();

        modifyMenu.add(cropItem);
        modifyMenu.add(translateItem);
        modifyMenu.addSeparator();

        modifyMenu.add(medianFilterItem);
        modifyMenu.add(medianWeightedFilterItem);
        modifyMenu.add(gaussianFilterItem);
        modifyMenu.add(savitzkyGolayFilterItem);
        modifyMenu.add(localRegressionItem);
        modifyMenu.addSeparator();
        modifyMenu.add(convolveItem);

        JMenu curveMenu = new JMenu("Curves");        
        curveMenu.add(recalculateItem);
        curveMenu.add(correctContactPointItem);
        curveMenu.add(correctTransitionIndentationItem);
        curveMenu.add(correctAdhesionMeasurementItem);

        JMenuBar menuBar = getMenuBar();
        menuBar.add(modifyMenu);
        menuBar.add(curveMenu);
    }

    private JToolBar buildToolBar()
    {
        JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
        toolBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(10, 3, 10, 3)));
        toolBar.setFloatable(false);
        toolBar.setLayout(new VerticalWrapLayout(VerticalFlowLayout.TOP, 0,0));

        JButton buttonRecalculate = new JButton(recalculateAction);
        buttonRecalculate.setHideActionText(true);
        buttonRecalculate.setMargin(new Insets(0, 0, 0, 0));
        buttonRecalculate.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonRecalculate.getMaximumSize().getHeight()));
        buttonRecalculate.setHorizontalAlignment(SwingConstants.LEFT);


        JToggleButton buttonMeasureLine = new JToggleButton(getMeasureAction());
        buttonMeasureLine.setHideActionText(true);
        buttonMeasureLine.setMargin(new Insets(0, 0, 0, 0));
        buttonMeasureLine.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonMeasureLine.getMaximumSize().getHeight()));
        buttonMeasureLine.setHorizontalAlignment(SwingConstants.LEFT);  

        JToggleButton buttonCrop = new JToggleButton(cropAction);
        buttonCrop.setHideActionText(true);
        buttonCrop.setMargin(new Insets(0, 0, 0, 0));
        buttonCrop.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonMeasureLine.getMaximumSize().getHeight()));
        buttonCrop.setHorizontalAlignment(SwingConstants.LEFT);  

        JButton buttonGaussianSmoothing = new JButton(gaussianFilterAction);
        buttonGaussianSmoothing.setHideActionText(true);
        buttonGaussianSmoothing.setMargin(new Insets(0, 0, 0, 0));
        buttonGaussianSmoothing.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonMeasureLine.getMaximumSize().getHeight()));
        buttonGaussianSmoothing.setHorizontalAlignment(SwingConstants.LEFT);  


        JButton buttonMedianSmoothing = new JButton(medianFilterAction);
        buttonMedianSmoothing.setHideActionText(true);
        buttonMedianSmoothing.setMargin(new Insets(0, 0, 0, 0));
        buttonMedianSmoothing.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonMeasureLine.getMaximumSize().getHeight()));
        buttonMedianSmoothing.setHorizontalAlignment(SwingConstants.LEFT);  

        JButton buttonConvolve = new JButton(convolveAction);
        buttonConvolve.setHideActionText(true);
        buttonConvolve.setMargin(new Insets(0, 0, 0, 0));
        buttonConvolve.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) buttonMeasureLine.getMaximumSize().getHeight()));
        buttonConvolve.setHorizontalAlignment(SwingConstants.LEFT);  


        toolBar.add(buttonRecalculate);
        toolBar.add(buttonMeasureLine);
        toolBar.add(buttonCrop);
        toolBar.add(buttonGaussianSmoothing);
        toolBar.add(buttonMedianSmoothing);
        toolBar.add(buttonConvolve);

        toolBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(0, 3, 0, 3)));
        toolBar.setMargin(new Insets(0,0,0,0));

        return toolBar;
    }

    private void initSelectionListener()
    {
    }

    public void selectResourceContainingChannelsFrom(S source)
    {
        R resource = getResourceContainingChannelsFrom(source);

        if(resource != null)
        {
            selectResource(resource);
        }
    }

    @Override
    public void handleNewChartPanel(Channel1DResultPanel<Channel1DGraphsSupervisor> panel) 
    {
        super.handleNewChartPanel(panel);

        panel.setGraphSupervisor(this);
    }

    @Override
    public void controlForResourceEmptiness(boolean empty) 
    {
        super.controlForResourceEmptiness(empty);
        controlForResourceEmptinessPrivate(empty);
    }

    // we need private 'copy' of the controlForResourceEmptiness, because it is
    // called in the constructor
    // we must copy private copy, so that it is not overridden

    private void controlForResourceEmptinessPrivate(boolean empty)
    {
        checkIfActionsShouldBeEnabled();
    }

    @Override
    public void controlForSelectedChartEmptiness(boolean empty) 
    {
        super.controlForSelectedChartEmptiness(empty);

        checkIfActionsShouldBeEnabled();
    }

    public void overlay()
    {       
        R selectedResource = getSelectedResource();
        List<String> currentTypes = getNonemptyTypes(selectedResource);

        Map<Resource, List<String>> resources = getResourceTypeMap(currentTypes);
        resources.remove(getSelectedResource());

        resourceSelectionWizard.showDialog(resources);
    }

    public void overlay(R resourceThis, R resourceThat, Set<String> newTypes)
    {
        for(String type : newTypes)
        {
            ChannelChart<?> chartThis = getChart(resourceThis, type);

            if(chartThis == null)
            {
                break;
            }

            CustomizableXYPlot plotThis = chartThis.getCustomizablePlot();

            ChannelChart<?> chartThat = getChart(resourceThat, type);
            CustomizableXYPlot plotThat = chartThat.getCustomizablePlot();

            String resourceNewName  = resourceThat.getShortName();

            Map<String, Channel1D> channelsFromNew = resourceThat.getChannels(type);

            for(Entry<String, Channel1D> entry : channelsFromNew.entrySet())
            {
                Channel1D channel = entry.getValue();

                String idOld = entry.getKey();
                String idNew = idOld.toString() + " (" + resourceNewName + ")";

                String nameNew = channel.getName() + " (" + resourceNewName + ")";

                Channel1D channelCopy = channel.getCopy(idNew, nameNew);

                resourceThis.registerChannel(type, null, channelCopy);

                int layerIndex = plotThat.getLayerIndex(idOld);                
                ChannelRenderer renderer;
                try {
                    renderer = (ChannelRenderer) (layerIndex > -1 ? plotThat.getRenderer(layerIndex).clone() : RendererFactory.getChannel1DRenderer(channelCopy));
                } catch (CloneNotSupportedException e) 
                {
                    e.printStackTrace();
                    renderer = RendererFactory.getChannel1DRenderer(channelCopy);
                }

                renderer.setName(nameNew);

                plotThis.addOrReplaceLayer(idNew, new Channel1DDataset(channelCopy, channelCopy.getName()), renderer);
            }
        }
    }

    @Override
    protected void showAllRawResourceData()
    {	
        List<SampleCollection> rawData = new ArrayList<>();

        List<R> selectedResources = getAllSelectedResources();
        for(R resource : selectedResources)
        {
            List<SampleCollection> collections = resource.getSampleCollectionsRawData();
            rawData.addAll(collections);
        }

        publishRawData(rawData);
    }

    @Override
    public void showRawResourceData()
    {
        R selectedResource = getSelectedResource();

        if(selectedResource != null)
        {
            List<SampleCollection> collections = selectedResource.getSampleCollectionsRawData();
            publishRawData(collections);
        }       
    }

    @Override
    public void drawingChartsFinished()
    {
        destination.showFigures(true);
    }


    @Override
    protected void close()
    {
        destination.showFigures(false);
    }

    public S getSelectedSource()
    {
        R resource = getSelectedResource();
        S source = (resource != null) ? resource.getSource() : null;
        return source;
    }

    public List<S> getAllSelectedSources()
    {
        return Channel1DResource.getSources(getAllSelectedResources());
    }

    @Override
    public void jumpToResults()
    {
        try
        {
            destination.showResults(getSelectedSource());
        }
        catch(UserCommunicableException e)
        {
            JOptionPane.showMessageDialog(Channel1DResultsView.this, e.getMessage(), "", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void jumpToAllResults()
    {
        try
        {
            destination.showResults(getAllSelectedSources());
        }
        catch(UserCommunicableException e)
        {
            JOptionPane.showMessageDialog(Channel1DResultsView.this, e.getMessage(), "", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    private void createAndRegisterPopupMenu() 
    {	        
        JPopupMenu popup = getResourceListPopupMenu();

        JMenuItem itemJumpToResults = new JMenuItem(new JumpToResultsAction());
        popup.insert(itemJumpToResults, 0);

        JMenuItem itemRecalculate = new JMenuItem(recalculateAction);
        popup.insert(itemRecalculate, 1);
    }

    /// ITEM MOVEMENTS /////


    @Override
    public boolean isValidValue(Channel1D channel, int itemIndex, double[] newValue)
    {        
        R resource = getSelectedResource();
        return resource.isValidPosition(getSelectedType(), channel.getIdentifier(), itemIndex, new Point2D.Double(newValue[0], newValue[1]));       
    }

    @Override
    public Point2D correctPosition(Channel1D channel, int itemIndex, Point2D dataPoint)
    {
        R resource = getSelectedResource();       
        return resource.correctItemPosition(getSelectedType(), channel.getIdentifier(), itemIndex, dataPoint);
    }

    public void itemAdded(Object channelIdentifier, double[] itemNew)
    {
        R resource = getSelectedResource();       
        resource.itemAdded(getSelectedType(), channelIdentifier, itemNew);
    }

    @Override
    public void channelAdded(Channel1D channel)
    {
        R resource = getSelectedResource();     

        resource.registerChannel(getSelectedType(), resource.getSource(), channel);
    }

    @Override
    public void channelRemoved(Channel1D channel)
    {
        R resource = getSelectedResource();     
        resource.removeChannel(getSelectedType(), resource.getSource(), channel);
    }

    ///MEASUREMENTS

    @Override
    public void showDistanceMeasurements() 
    {
        distanceMeasurementsDialog.setVisible(true);
    }

    @Override
    protected void showMeasurementEditor()
    {
        measurementEditor.setVisible(true);
    }

    @Override
    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {
        R selectedResource = getSelectedResource();

        if(selectedResource != null)
        {
            String selectedType = getSelectedType();
            selectedResource.addOrReplaceDistanceMeasurement(selectedType, measurement);

            boolean measurementsAvailable = getMode().isMeasurement();
            updateMeasurementsAvailability(measurementsAvailable);

            MultipleXYChartPanel<ChannelChart<?>> selectedPanel = getSelectedPanel();
            selectedPanel.addOrReplaceDistanceMeasurement(measurement);        

            DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsDialog.getTable().getModel();
            model.addOrUpdateDistance(measurement.getKey(), measurement.getDistanceShapeFactors());
        }
    }

    @Override
    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement) 
    {                
        R selectedResource = getSelectedResource();

        if(selectedResource != null)
        {
            String selectedType = getSelectedType();
            selectedResource.addOrReplaceDistanceMeasurement(selectedType, measurement);

            int measurementsCount = selectedResource.getMeasurementCount(selectedType);
            boolean measurementsAvailable = getMode().isMeasurement() && measurementsCount > 0;
            updateMeasurementsAvailability(measurementsAvailable);

            MultipleXYChartPanel<ChannelChart<?>> selectedPanel = getSelectedPanel();
            selectedPanel.removeDistanceMeasurement(measurement);

            DistanceGeometryTableModel model = (DistanceGeometryTableModel) distanceMeasurementsDialog.getTable().getModel();
            model.removeDistance(measurement.getKey(), measurement.getDistanceShapeFactors());
        }   
    }

    @Override
    public void handleChangeOfSelectedType(String typeOld, String typeNew) 
    {
        super.handleChangeOfSelectedType(typeOld, typeNew);

        //CHANGES THE DISTANCEMEASUREMENTS WHOSE GEOMETRY IS DISPLAYED IN 'distanceMeasurementsModel'

        R resource = getSelectedResource();

        if(!Objects.equals(typeOld, typeNew) && resource != null)
        {
            File defaultOutputFile = resource.getDefaultOutputLocation();

            Map<Object, DistanceMeasurementDrawable> distanceMeasurements = resource.getDistanceMeasurements(typeNew);
            boolean measurementsAvailable = !distanceMeasurements.isEmpty() && getMode().isMeasurement();
            updateMeasurementsAvailability(measurementsAvailable);

            Map<Object, DistanceShapeFactors> measurementShapeFactors = new LinkedHashMap<>();
            for(Entry<Object, DistanceMeasurementDrawable> entry :  distanceMeasurements.entrySet())
            {
                Object key = entry.getKey();
                DistanceShapeFactors line = entry.getValue().getDistanceShapeFactors();
                measurementShapeFactors.put(key, line);
            }

            PrefixedUnit dataUnitX = SimplePrefixedUnit.getNullInstance();
            PrefixedUnit dataUnitY = SimplePrefixedUnit.getNullInstance();
            PrefixedUnit displayedUnitX = SimplePrefixedUnit.getNullInstance();
            PrefixedUnit displayedUnitY = SimplePrefixedUnit.getNullInstance();

            ChannelChart<?> newChart = getSelectedChart();

            if(newChart != null)
            {
                dataUnitX = newChart.getDomainDataUnit();
                dataUnitY = newChart.getRangeDataUnit();
                displayedUnitX = newChart.getDomainDisplayedUnit();
                displayedUnitY = newChart.getRangeDisplayedUnit();

                DistanceMeasurementStyle measurementStyle = newChart.getDistanceMeasurementStyle();
                measurementEditor.setModel(measurementStyle);
            }

            DistanceGeometryTableModel distanceMeasurementsModel = new DistanceGeometryTableModel(defaultOutputFile, dataUnitX, dataUnitY, displayedUnitX, displayedUnitY);
            distanceMeasurementsModel.addDistances(measurementShapeFactors);

            MinimalNumericalTable dstanceMeasurementsTable = distanceMeasurementsDialog.getTable();
            dstanceMeasurementsTable.setModel(distanceMeasurementsModel); 
        }

        checkIfActionsShouldBeEnabled();
        checkIfUndoRedoEnabled();
        checkIfUndoRedoAllEnabled();
    }

    @Override
    public void handleChangeOfSelectedResource(R resourceOld, R resourceNew) 
    {
        //CHANGES THE DISTANCEMEASUREMENTS WHOSE GEOMETRY IS DISPLAYED IN 'distanceMeasurementsModel'

        if(resourceNew != null)
        {
            String selectedType = getSelectedType();
            File defaultOutputFile = resourceNew.getDefaultOutputLocation();

            Map<Object, DistanceMeasurementDrawable> distanceMeasurements = resourceNew.getDistanceMeasurements(selectedType);
            boolean measurementsAvailable = distanceMeasurements.size()>0 && getMode().equals(MouseInputModeStandard.DISTANCE_MEASUREMENT_LINE);
            updateMeasurementsAvailability(measurementsAvailable);

            Map<Object, DistanceShapeFactors> measurementShapeFactors = new LinkedHashMap<>();
            for(Entry<Object, DistanceMeasurementDrawable> entry :  distanceMeasurements.entrySet())
            {
                Object key = entry.getKey();
                DistanceShapeFactors line = entry.getValue().getDistanceShapeFactors();
                measurementShapeFactors.put(key, line);
            }

            PrefixedUnit unitX = SimplePrefixedUnit.getNullInstance();
            PrefixedUnit unitY = SimplePrefixedUnit.getNullInstance();

            ChannelChart<?> newChart = getSelectedChart();

            if(newChart != null)
            {
                unitX = newChart.getDomainDataUnit();
                unitY = newChart.getRangeDataUnit();

                DistanceMeasurementStyle measurementStyle = newChart.getDistanceMeasurementStyle();
                measurementEditor.setModel(measurementStyle);
            }

            DistanceGeometryTableModel distanceMeasurementsModel = new DistanceGeometryTableModel(defaultOutputFile, unitX, unitY);
            distanceMeasurementsModel.addDistances(measurementShapeFactors);

            MinimalNumericalTable dstanceMeasurementsTable = distanceMeasurementsDialog.getTable();
            dstanceMeasurementsTable.setModel(distanceMeasurementsModel);

        }

        checkIfActionsShouldBeEnabled();
        checkIfUndoRedoEnabled();
        checkIfUndoRedoAllEnabled();
    }

    @Override
    public void setResource(R r, Set<String> types) 
    {
        overlay(getSelectedResource(), r, types);
    }

    private void checkIfActionsShouldBeEnabled()
    {

        ChannelChart<?> curveChart = getSelectedChart("RecordedCurve");
        boolean recordedCurveActionsEnabled = (curveChart != null);


        correctContactPointAction.setEnabled(recordedCurveActionsEnabled);
        correctAdhesionAction.setEnabled(recordedCurveActionsEnabled);
        addAdhesionMeasurementAction.setEnabled(recordedCurveActionsEnabled);

        ChannelChart<?> selectedChart = getSelectedChart(); 
        boolean chartSelected = (selectedChart != null);

        enableStandByActions(chartSelected);

        boolean overlayEnabled = chartSelected && getResourceCount() > 1;

        overlayAction.setEnabled(overlayEnabled);

    }

    //standBy actions are the ones that can always be fired, provided that there is a selected chart 
    private void enableStandByActions(boolean enabled) 
    {
        recalculateAction.setEnabled(enabled);
        correctTransitionIndentationAction.setEnabled(enabled);

        cropAction.setEnabled(enabled);
        translateAction.setEnabled(enabled);

        medianFilterAction.setEnabled(enabled);
        medianWeightedFilterAction.setEnabled(enabled);
        gaussianFilterAction.setEnabled(enabled);
        savitzkyGolayFilterAction.setEnabled(enabled);
        localRegressionAction.setEnabled(enabled);
        convolveAction.setEnabled(enabled);
    }

    @Override
    public void setConsistentWithMode(MouseInputMode modeOld, MouseInputMode modeNew)
    {
        super.setConsistentWithMode(modeOld, modeNew);

        boolean isCorrectContact = modeNew.isMoveDataItems(Datasets.CONTACT_POINT);
        boolean isCorrectTransitionIndentation = modeNew.isMoveDataItems(Datasets.MODEL_TRANSITION_POINT)|| modeNew.isMoveDataItems(Datasets.MODEL_TRANSITION_POINT_FORCE_CURVE) || modeNew.isMoveDataItems(Datasets.MODEL_TRANSITION_POINT_INDENTATION_CURVE) || modeNew.isMoveDataItems(Datasets.MODEL_TRANSITION_POINT_POINTWISE_MODULUS);
        boolean isCorrectAdhesion = modeNew.isMoveDataItems(Datasets.ADHESION_FORCE);
        boolean isAddAdhesion = modeNew.isDrawDataset(Datasets.ADHESION_FORCE);

        correctContactPointAction.putValue(Action.SELECTED_KEY, isCorrectContact);
        correctTransitionIndentationAction.putValue(Action.SELECTED_KEY, isCorrectTransitionIndentation);
        correctAdhesionAction.putValue(Action.SELECTED_KEY, isCorrectAdhesion);
        addAdhesionMeasurementAction.putValue(Action.SELECTED_KEY, isAddAdhesion);
    }

    @Override
    public void handleChangeOfData(Map<String, Channel1D> channelsChanged, String type, R resource)
    {                        
        ChannelChart<?> chart = getChart(resource, type);

        for (Object key : channelsChanged.keySet()) 
        {
            chart.notifyOfDataChange(key);
        }

        DataChangeEvent<String> event = new DataChangeEvent<>(this, channelsChanged.keySet());
        fireDataChangeEvent(event); 
    }

    private class JumpToResultsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public JumpToResultsAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_J);
            putValue(NAME,"Jump to results");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {   
            jumpToAllResults();
        };
    }


    private class OverlayAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public OverlayAction() 
        {           
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Overlay");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            overlay();
        }
    }


    private class RecalculateAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public RecalculateAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/RecalculateCurves.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
            putValue(SHORT_DESCRIPTION, "Recalculate");
            putValue(NAME, "Recalculate");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            recalculateFullDialog();
        }
    }

    private void recalculateFullDialog()
    {
    }


    private class CorrectContactPointAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public CorrectContactPointAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/CorrectContactPoint.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(SHORT_DESCRIPTION, "Correct contact point");
            putValue(NAME, "Correct contact point");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ? new DataModificationMouseInputMode(Datasets.CONTACT_POINT, Collections.singleton(DataModificationType.POINT_MOVEABLE)) : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }

    private class CorrectTransitionIndentationAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public CorrectTransitionIndentationAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/CorrectTransitionPoint.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(SHORT_DESCRIPTION, "Correct transition point");
            putValue(NAME, "Correct transition point");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ? new DataModificationMouseInputMode(Arrays.asList(Datasets.MODEL_TRANSITION_POINT_FORCE_CURVE,Datasets.MODEL_TRANSITION_POINT_INDENTATION_CURVE,Datasets.MODEL_TRANSITION_POINT_POINTWISE_MODULUS), Collections.singleton(DataModificationType.POINT_MOVEABLE)) : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }

    private class CorrectAdhesionAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public CorrectAdhesionAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/CorrectAdhesion.png"));
            putValue(LARGE_ICON_KEY, icon);
            putValue(SHORT_DESCRIPTION, "Correct adhesion");
            putValue(NAME, "Correct adhesion");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ?  new DataModificationMouseInputMode(Datasets.ADHESION_FORCE, Collections.unmodifiableSet(EnumSet.of(DataModificationType.POINT_MOVEABLE, DataModificationType.WHOLE_DATASET_MOVEABLE))) : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }

    private class AddAdhesionMeasurementAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public AddAdhesionMeasurementAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/AddAdhesion.png"));
            putValue(LARGE_ICON_KEY, icon);
            putValue(SHORT_DESCRIPTION, "Add adhesion measurement");
            putValue(NAME, "Add adhesion");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ? new DataDrawingMouseInputMode(Datasets.ADHESION_FORCE, new VerticalModificationConstraint1D(), 2) : MouseInputModeStandard.NORMAL;

            setMode(mode);
        }
    }

    //copied
    @Override
    public List<? extends R> getAdditionalResources() 
    {
        return Collections.emptyList();
    }

    //copied
    @Override
    public Channel1DResultsDialogModel<R> getResourceModel()
    {
        return (Channel1DResultsDialogModel<R>)super.getResourceModel();
    }

    //copied
    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers()
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers();
    }

    //copied
    @Override
    public Set<String> getAllResourcesChannelIdentifiers()
    {
        return getResourceModel().getAllResourcesChannelIdentifiers();
    }

    //copied
    @Override
    public Set<String> getAllResourcesChannelIdentifiers(ChannelFilter2<Channel1D> filter)
    {
        return getResourceModel().getAllResourcesChannelIdentifiers(filter);
    }

    //copied
    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(String type)
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers(type);
    }

    //copied
    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(ChannelFilter2<Channel1D> filter)
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers(filter);
    }

    //copied

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(String type, ChannelFilter2<Channel1D> filter)
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers(type, filter);
    }

    //copied
    @Override
    public Set<String> getAllResourcesChannelIdentifiers(String type)
    {
        return getResourceModel().getAllResourcesChannelIdentifiers(type);
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(String type, ChannelFilter2<Channel1D> filter)
    {
        return getResourceModel().getAllResourcesChannelIdentifiers(type, filter);
    }

    @Override
    public void addResourceSelectionListener(SelectionListener<? super R> listener)
    {
        getResourceModel().addSelectionListener(listener);
    }

    @Override
    public void removeResourceSelectionListener(SelectionListener<? super R> listener)
    {
        getResourceModel().removeSelectionListener(listener);
    }

    @Override
    public void addResourceDataListener(ResourceGroupListener<? super R> listener)
    {
        getResourceModel().addDataModelListener(listener);
    }

    @Override
    public void removeResourceDataListener(ResourceGroupListener<? super R> listener)
    {
        getResourceModel().removeDataModelListener(listener);
    }

    @Override
    public void addResourceTypeListener(ResourceTypeListener listener)
    {
        getResourceModel().addResourceTypeListener(listener);
    }

    @Override
    public void removeResourceTypeListener(ResourceTypeListener listener)
    {
        getResourceModel().removeResourceTypeListener(listener);
    }

    //copied
    @Override
    public PrefixedUnit getDataUnit() 
    {
        R resource = getSelectedResource();
        String type = getSelectedType();
        return (resource != null) ? resource.getSingleDataUnit(type) : null;    
    }

    //copied
    @Override
    public PrefixedUnit getDisplayedUnit() 
    {
        ChannelChart<?> chart = getSelectedChart();
        PrefixedUnit axisUnit = (chart != null) ? chart.getRangeDisplayedUnit() : null;

        return axisUnit;
    }

    //copied
    protected PrefixedUnit getXAxisDisplayedUnit()
    {
        ChannelChart<?> chart = getSelectedChart();
        PrefixedUnit axisUnit = (chart != null) ? chart.getDomainDisplayedUnit() : null;

        return axisUnit;
    }

    protected PrefixedUnit getXAxisDataUnit()
    {
        ChannelChart<?> chart = getSelectedChart();
        PrefixedUnit axisUnit = (chart != null) ? chart.getDomainDataUnit() : null;

        return axisUnit;
    }

    //copied
    @Override
    public UnitManager getUnitManager()
    {
        return new UnitManager(getDataUnit(), getDisplayedUnit());
    }

    //copied
    @Override
    public List<PrefixedUnit> getDomainDisplayedUnits() 
    {
        List<PrefixedUnit> domainUnitManagers = new ArrayList<>();

        domainUnitManagers.add(getXAxisDisplayedUnit());

        return domainUnitManagers;
    }

    @Override
    public List<PrefixedUnit> getDomainDataUnits() 
    {
        List<PrefixedUnit> domainUnitManagers = new ArrayList<>();

        domainUnitManagers.add(getXAxisDataUnit());

        return domainUnitManagers;
    }

    //copied
    @Override
    public ROI getROIUnion() 
    {
        return getResourceModel().getROIUnion();
    }

    //copied
    @Override
    public Map<Object, ROI> getDrawableROIs() 
    {
        return Collections.emptyMap();
    }
    //copied}

    @Override
    public Map<Object, ROI> getAllROIs() 
    {
        return Collections.emptyMap();
    }

    //copied
    @Override
    public void pushCommand(R resource, String type, UndoableCommand command)
    {
        resource.pushCommand(type, command);
        checkIfUndoRedoEnabled();               
    }

    @Override
    public void pushCommands(MetaMap<R, String, UndoableCommand> commands)
    {
        getResourceModel().pushCommands(commands);
        checkIfUndoRedoEnabled();        
    }

    @Override
    public Window getAssociatedWindow()
    {
        return this;
    }

    @Override
    public void notifyToolsOfMouseClicked(CustomChartMouseEvent evt) {        
    }

    @Override
    public void notifyToolsOfMouseDragged(CustomChartMouseEvent evt)
    {
    }

    @Override
    public void notifyToolsOfMouseMoved(CustomChartMouseEvent evt)
    {
    }

    @Override
    public void notifyToolsOfMousePressed(CustomChartMouseEvent evt)
    {
    }

    @Override
    public void notifyToolsOfMouseReleased(CustomChartMouseEvent evt)
    {
    }

    @Override
    public void useMouseInteractiveTool(MouseInteractiveTool tool) {        
    }

    @Override
    public void stopUsingMouseInteractiveTool(MouseInteractiveTool tool) {        
    }

    @Override
    public MouseInteractiveTool getCurrentlyUsedInteractiveTool()
    {
        return null;
    }

    @Override
    public boolean isChartElementCaughtByTool()
    {
        return false;
    }

    @Override
    public boolean isComplexElementUnderConstructionByTool()
    {
        return false;
    }   

    @Override
    public boolean isRightClickReservedByTool(Rectangle2D dataArea, Point2D dataPoint)
    {
        return false;
    }

    @Override
    public void refreshUndoRedoOperations()
    {
        checkIfUndoRedoEnabled();
    }

    private void checkIfUndoRedoEnabled()
    {
        Channel1DResultsDialogModel<R> model = getResourceModel();

        boolean redoEnabled = model.canRedoBeEnabled();
        boolean undoEnabled = model.canUndoBeEnabled();

        undoAction.setEnabled(undoEnabled);
        redoAction.setEnabled(redoEnabled);

        checkIfUndoRedoAllEnabled();
    } 

    private void checkIfUndoRedoAllEnabled()
    {
        Channel1DResultsDialogModel<R> model = getResourceModel();

        boolean redoAllEnabled = model.canRedoAllBeEnabled();
        boolean undoAllEnabled = model.canUndoAllBeEnabled();

        undoAllAction.setEnabled(undoAllEnabled);
        redoAllAction.setEnabled(redoAllEnabled);
    }

    private class UndoAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public UndoAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Undo");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            ChannelResource<Channel1D, Channel1DData, String> resource = getSelectedResource();
            if(resource != null)
            {
                resource.undo(getSelectedType());
                checkIfUndoRedoEnabled();
            }            
        }
    }

    private class UndoAllAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public UndoAllAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
            putValue(NAME, "Undo all");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {           
            getResourceModel().undoAll(Channel1DResultsView.this);
        }     
    }

    private class RedoAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public RedoAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Redo");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {            
            ChannelResource<Channel1D, Channel1DData, String> resource = getSelectedResource();
            if(resource != null)
            {
                resource.redo(getSelectedType());
                checkIfUndoRedoEnabled();
            }        
        }
    }

    private class RedoAllAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public RedoAllAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
            putValue(NAME, "Redo all");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {            
            getResourceModel().redoAll(Channel1DResultsView.this);
        }
    }
}
