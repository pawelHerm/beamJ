package atomicJ.gui;


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


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import javax.swing.*;

import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.MultipleXYChartPanel.MultipleChartPanelFactory;
import atomicJ.gui.curveProcessingActions.Convolve1DAction;
import atomicJ.gui.curveProcessingActions.Crop1DAction;
import atomicJ.gui.curveProcessingActions.GaussianFilter1DAction;
import atomicJ.gui.curveProcessingActions.Gridding1DAction;
import atomicJ.gui.curveProcessingActions.LocalRegression1DAction;
import atomicJ.gui.curveProcessingActions.Median1DFilterAction;
import atomicJ.gui.curveProcessingActions.MedianWeightedFilter1DAction;
import atomicJ.gui.curveProcessingActions.SavitzkyGolayFilter1DAction;
import atomicJ.gui.curveProcessingActions.Translate1DAction;
import atomicJ.gui.imageProcessing.UnitManager;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.measurements.DistanceMeasurementStyle;
import atomicJ.gui.measurements.DistanceSimpleMeasurementEditor;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.resources.Channel1DResource;
import atomicJ.resources.ResourceView;
import atomicJ.utilities.MetaMap;


public abstract class Channel1DView<E extends Channel1DResource<?>> extends ResourceXYPresentationView<E, ChannelChart<?>, MultipleXYChartPanel<ChannelChart<?>>>
implements ResourceView<E, Channel1D, String>
{
    private static final long serialVersionUID = 1L;    
    private final static Preferences PREF =  Preferences.userNodeForPackage(Channel1DView.class).node("PreviewDialog");

    private final Action cropAction = new Crop1DAction<>(this);
    private final Action translateAction = new Translate1DAction<>(this);
    private final Action griddingAction = new Gridding1DAction<>(this);

    private final Action medianFilterAction = new Median1DFilterAction<>(this);
    private final Action medianWeightedFilterAction = new MedianWeightedFilter1DAction<>(this);
    private final Action gaussianFilterAction = new GaussianFilter1DAction<>(this);
    private final Action savitzkyGolayFilterAction = new SavitzkyGolayFilter1DAction<>(this);
    private final Action localRegressionAction = new LocalRegression1DAction<>(this);
    private final Action convolveAction = new Convolve1DAction<>(this);

    private final Action processAction = new ProcessAction();
    private final Action processSelectedAction = new ProcessSelectedAction();
    private final Action openAction = new OpenAction();

    private final Action undoAction = new UndoAction();
    private final Action undoAllAction = new UndoAllAction();
    private final Action redoAction = new RedoAction();
    private final Action redoAllAction = new RedoAllAction();

    private final JMenuItem cropItem = new JMenuItem(cropAction);
    private final JMenuItem translateItem = new JMenuItem(translateAction);
    private final JMenuItem griddingItem = new JMenuItem(griddingAction);

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

    private final FlexibleNumericalTableDialog distanceMeasurementsDialog = new FlexibleNumericalTableDialog(this, new StandardNumericalTable(new DistanceGeometryTableModel(), true, true), "Curves distance measurements");
    private final DistanceSimpleMeasurementEditor measurementEditor = new DistanceSimpleMeasurementEditor(this);

    public Channel1DView(final Window parent, ModifiableResourceDialogModel<Channel1D,Channel1DData,String, E> dialogModel)
    {
        super(parent, MultipleChartPanelFactory.getInstance(), "Force curves", PREF, ModalityType.MODELESS, dialogModel);  

        JMenu chartMenu = getChartMenu();
        JMenuItem rawDataItem = new JMenuItem(getRawDataAction());
        chartMenu.add(rawDataItem);

        Container content = getContentPane();
        content.setLayout(new BorderLayout());  

        JPanel panelResources = getPanelResources();
        JTabbedPane panelCharts = getTabbedPane();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,panelResources,panelCharts);
        splitPane.setOneTouchExpandable(true);

        content.add(splitPane, BorderLayout.CENTER);
        content.add(getMultipleResourcesPanelButtons(),BorderLayout.SOUTH);

        controlForResourceEmptiness(isEmpty());

        buildMenuBar();

        initInputAndActionMaps();
        createAndRegisterPopupMenu(); 
    }

    @Override
    public ModifiableResourceDialogModel<Channel1D,Channel1DData,String, E> getResourceModel()
    {
        return (ModifiableResourceDialogModel<Channel1D,Channel1DData,String, E>)super.getResourceModel();
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers()
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers();
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(ChannelFilter2<Channel1D> filter)
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers(filter);
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers()
    {
        return getResourceModel().getAllResourcesChannelIdentifiers();
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(ChannelFilter2<Channel1D> filter)
    {
        return getResourceModel().getAllResourcesChannelIdentifiers(filter);
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(String type)
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers(type);
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(String type)
    {
        return getResourceModel().getAllResourcesChannelIdentifiers(type);
    }

    @Override
    public Set<String> getSelectedResourcesChannelIdentifiers(String type, ChannelFilter2<Channel1D> filter)
    {
        return getResourceModel().getSelectedResourcesChannelIdentifiers(type, filter);
    }

    @Override
    public Set<String> getAllResourcesChannelIdentifiers(String type, ChannelFilter2<Channel1D> filter)
    {
        return getResourceModel().getAllResourcesChannelIdentifiers(type, filter);
    }

    @Override
    public void addResourceSelectionListener(SelectionListener<? super E> listener)
    {
        getResourceModel().addSelectionListener(listener);
    }

    @Override
    public void removeResourceSelectionListener(SelectionListener<? super E> listener)
    {
        getResourceModel().removeSelectionListener(listener);
    }

    @Override
    public void addResourceDataListener(ResourceGroupListener<? super E> listener)
    {
        getResourceModel().addDataModelListener(listener);
    }

    @Override
    public void removeResourceDataListener(ResourceGroupListener<? super E> listener)
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

    private void initInputAndActionMaps()
    {
        InputMap inputMap = getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW );                
        inputMap.put((KeyStroke) openAction.getValue(Action.ACCELERATOR_KEY), openAction.getValue(Action.NAME));      

        inputMap.put((KeyStroke) undoAction.getValue(Action.ACCELERATOR_KEY), undoAction.getValue(Action.NAME));  
        inputMap.put((KeyStroke) undoAllAction.getValue(Action.ACCELERATOR_KEY), undoAllAction.getValue(Action.NAME));      
        inputMap.put((KeyStroke) redoAction.getValue(Action.ACCELERATOR_KEY), redoAction.getValue(Action.NAME));      
        inputMap.put((KeyStroke) redoAllAction.getValue(Action.ACCELERATOR_KEY), redoAllAction.getValue(Action.NAME));      

        ActionMap actionMap = getRootPane().getActionMap();
        actionMap.put(openAction.getValue(Action.NAME), openAction); 

        actionMap.put(undoAction.getValue(Action.NAME), undoAction); 
        actionMap.put(undoAllAction.getValue(Action.NAME), undoAllAction); 
        actionMap.put(redoAction.getValue(Action.NAME), redoAction);   
        actionMap.put(redoAllAction.getValue(Action.NAME), redoAllAction);    
    }

    private void buildMenuBar()
    {
        JMenuBar menuBar = getMenuBar();

        JMenu fileMenu = getFileMenu();

        fileMenu.insert(new JMenuItem(openAction), 0);
        fileMenu.insert(new JMenuItem(processAction), 1);

        JMenu modifyMenu = new JMenu("Preprocess");

        modifyMenu.add(undoItem);
        modifyMenu.add(redoItem);
        modifyMenu.addSeparator();

        modifyMenu.add(undoAllItem);
        modifyMenu.add(redoAllItem);
        modifyMenu.addSeparator();

        modifyMenu.add(cropItem);
        modifyMenu.add(translateItem);
        modifyMenu.add(griddingItem);
        modifyMenu.addSeparator();

        modifyMenu.add(medianFilterItem);
        modifyMenu.add(medianWeightedFilterItem);
        modifyMenu.add(gaussianFilterItem);
        modifyMenu.add(savitzkyGolayFilterItem);
        modifyMenu.add(localRegressionItem);
        modifyMenu.addSeparator();

        modifyMenu.add(convolveItem);

        menuBar.add(modifyMenu);
    }

    private void createAndRegisterPopupMenu() 
    {           
        JPopupMenu popup = getResourceListPopupMenu();

        JMenuItem item = new JMenuItem(processSelectedAction);
        popup.insert(item, 0);
    }

    // we need private 'copy' of the controlForResourceEmptiness, because it is
    // called in the constructor
    // we must use private copy, so that it is not overriden
    @Override
    public void controlForResourceEmptiness(boolean empty) {
        super.controlForResourceEmptiness(empty);
        controlForResourceEmptinessPrivate(empty);
    }

    private void controlForResourceEmptinessPrivate(boolean empty) {
        boolean enabled = !empty;
        enableActionsSpectroscopyDialog(enabled);
    }

    private void enableActionsSpectroscopyDialog(boolean enabled)
    {
        processAction.setEnabled(enabled);

        cropAction.setEnabled(enabled);
        translateAction.setEnabled(enabled);
        convolveAction.setEnabled(enabled);
        griddingAction.setEnabled(enabled);
        medianFilterAction.setEnabled(enabled);
        medianWeightedFilterAction.setEnabled(enabled);
        gaussianFilterAction.setEnabled(enabled);
        savitzkyGolayFilterAction.setEnabled(enabled);
        localRegressionAction.setEnabled(enabled);

        checkIfUndoRedoEnabled();
    }

    protected abstract void startPreview();

    @Override
    protected void close()
    {
        setVisible(false);
    }

    protected abstract void startProcessingAllSources();
    protected abstract void startProcessingSelectedSources();

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
        E selectedResource = getSelectedResource();

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
        E selectedResource = getSelectedResource();

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
    public void handleChangeOfSelectedResource(E resourceOld, E resourceNew) 
    {
        //CHANGES THE DISTANCEMEASUREMENTS WHOSE GEOMETRY IS DISPLAYED IN 'distanceMeasurementsModel'

        if(resourceNew != null)
        {
            String selectedType = getSelectedType();

            File defaultOutputFile = resourceNew.getDefaultOutputLocation();

            Map<Object, DistanceMeasurementDrawable> distanceMeasurements = resourceNew.getDistanceMeasurements(selectedType);
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
                CustomizableXYPlot plot = newChart.getCustomizablePlot();
                dataUnitX = plot.getDomainDataUnit();
                dataUnitY = plot.getRangeDataUnit();
                displayedUnitX = plot.getDomainDisplayedUnit();
                displayedUnitY = plot.getRangeDisplayedUnit();

                DistanceMeasurementStyle measurementStyle = newChart.getDistanceMeasurementStyle();

                measurementEditor.setModel(measurementStyle);
            }

            DistanceGeometryTableModel distanceMeasurementsModel = new DistanceGeometryTableModel(defaultOutputFile, dataUnitX, dataUnitY, displayedUnitX, displayedUnitY);
            distanceMeasurementsModel.addDistances(measurementShapeFactors);

            MinimalNumericalTable dstanceMeasurementsTable = distanceMeasurementsDialog.getTable();
            dstanceMeasurementsTable.setModel(distanceMeasurementsModel);      
        }

        checkIfUndoRedoEnabled();
    }

    @Override
    public PrefixedUnit getDataUnit() 
    {
        E resource = getSelectedResource();
        String type = getSelectedType();
        return resource.getSingleDataUnit(type);    
    }

    @Override
    public PrefixedUnit getDisplayedUnit()
    {
        PrefixedUnit axisUnit = null;
        ChannelChart<?> chart = getSelectedChart();

        if(chart != null)
        {
            axisUnit = chart.getRangeDisplayedUnit();
        }

        return axisUnit;
    }

    private PrefixedUnit getXAxisDisplayedUnit()
    {
        PrefixedUnit axisUnit = null;
        ChannelChart<?> chart = getSelectedChart();

        if(chart != null)
        {
            axisUnit = chart.getDomainDisplayedUnit();
        }

        return axisUnit;
    }

    private PrefixedUnit getXAxisDataUnit()
    {
        PrefixedUnit axisUnit = null;
        ChannelChart<?> chart = getSelectedChart();

        if(chart != null)
        {
            axisUnit = chart.getDomainDataUnit();
        }

        return axisUnit;
    }


    @Override
    public UnitManager getUnitManager() 
    {
        return new UnitManager(getDataUnit(), getDisplayedUnit());
    }

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

    @Override
    public ROI getROIUnion() 
    {
        return getResourceModel().getROIUnion();
    }

    @Override
    public Map<Object, ROI> getDrawableROIs() 
    {
        return Collections.emptyMap();
    }

    @Override
    public Map<Object, ROI> getAllROIs()
    {
        return Collections.emptyMap();
    }

    @Override
    public List<? extends E> getAdditionalResources() 
    {
        return Collections.emptyList();
    }

    @Override
    public void handleChangeOfData(Map<String, Channel1D> channelsChanged, String type, E resource) 
    {
        ChannelChart<?> chart = getChart(resource, type);

        for (Object key : channelsChanged.keySet()) 
        {
            chart.notifyOfDataChange(key);
        }

        DataChangeEvent<String> event = new DataChangeEvent<>(this, channelsChanged.keySet());
        fireDataChangeEvent(event);        
    }

    @Override
    public void pushCommand(E resource, String type,
            UndoableCommand command) 
    {
        resource.pushCommand(type, command);
        checkIfUndoRedoEnabled();        
    }

    @Override
    public void pushCommands(MetaMap<E, String, UndoableCommand> commands)
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
    public void notifyToolsOfMouseClicked(CustomChartMouseEvent evt) 
    {        
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
        ModifiableResourceDialogModel<Channel1D,Channel1DData,String, E> model = getResourceModel();

        boolean redoEnabled = model.canRedoBeEnabled();
        boolean undoEnabled = model.canUndoBeEnabled();

        undoAction.setEnabled(undoEnabled);
        redoAction.setEnabled(redoEnabled);

        checkIfUndoRedoAllEnabled();
    } 

    private void checkIfUndoRedoAllEnabled()
    {
        ModifiableResourceDialogModel<Channel1D,Channel1DData,String, E> model = getResourceModel();

        boolean redoAllEnabled = model.canRedoAllBeEnabled();
        boolean undoAllEnabled = model.canUndoAllBeEnabled();

        undoAllAction.setEnabled(undoAllEnabled);
        redoAllAction.setEnabled(redoAllEnabled);
    }

    private class ProcessAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ProcessAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));

            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(NAME,"Process");
            putValue(SHORT_DESCRIPTION, "Process force curves");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            startProcessingAllSources();
        }
    }

    private class ProcessSelectedAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ProcessSelectedAction()
        {
            putValue(NAME,"Process");
            putValue(SHORT_DESCRIPTION, "Process force curves");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            startProcessingSelectedSources();
        }
    }

    private class OpenAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public OpenAction() 
        {
            putValue(NAME, "Open");

            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {            
            startPreview();
        }
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
            E resource = getSelectedResource();
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
            getResourceModel().undoAll(Channel1DView.this);
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
            E resource = getSelectedResource();
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
            getResourceModel().redoAll(Channel1DView.this);
        }
    }
}
