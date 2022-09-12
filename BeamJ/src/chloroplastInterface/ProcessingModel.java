
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

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;

import atomicJ.analysis.*;
import atomicJ.curveProcessing.SpanType;
import atomicJ.data.Channel1DCollection;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.ResourceSelectionModel;
import atomicJ.gui.curveProcessing.Dataset1DCroppingModel;
import atomicJ.gui.curveProcessing.CroppingReceiver;
import atomicJ.gui.curveProcessing.CurveVisualizationHandle;
import atomicJ.gui.curveProcessing.NumericalResultsHandler;
import atomicJ.gui.curveProcessing.PreprocessCurvesHandle;
import atomicJ.gui.curveProcessing.SmootherType;
import atomicJ.statistics.LocalRegressionWeightFunction;
import atomicJ.statistics.SpanGeometry;

public class ProcessingModel extends AbstractModel implements CroppingReceiver, ResourceSelectionModel<SimplePhotometricSource>, PropertyChangeListener
{
    public static final String CURRENT_BATCH_NUMBER = "CurrentBatchNumber";

    private final List<ProcessingBatchModel> batchModels = new ArrayList<>();
    private final PreprocessCurvesHandle<SimplePhotometricSource> preprocessHandle;

    private final ResultDestinationPhotometric resultDestination;

    private ProcessingBatchModel currentBatch;

    private CurveVisualizationHandle<VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric>> curveVisualizationHandle;
    private NumericalResultsHandler<ProcessedPackPhotometric> resultHandle;

    private int index;

    public ProcessingModel(ResultDestinationPhotometric parent, PreprocessCurvesHandle<SimplePhotometricSource> preprocessingDestination)
    {
        this(parent, preprocessingDestination, new ArrayList<>(), Integer.toString(parent.getResultBatchesCoordinator().getPublishedBatchCount()),parent.getResultBatchesCoordinator().getPublishedBatchCount());
    }

    public ProcessingModel(ResultDestinationPhotometric parent, PreprocessCurvesHandle<SimplePhotometricSource> preprocessingDestination, List<SimplePhotometricSource> sources, int index)
    {
        this(parent, preprocessingDestination, sources, Integer.toString(index), index);
    }

    public ProcessingModel(ResultDestinationPhotometric parent, PreprocessCurvesHandle<SimplePhotometricSource> preprocessingDestination, List<SimplePhotometricSource> sources, String name, int number)
    {
        this.resultDestination = parent;
        this.preprocessHandle = preprocessingDestination;
        this.currentBatch = new ProcessingBatchModel(parent, sources, name, number);	
        this.currentBatch.addPropertyChangeListener(this);

        this.curveVisualizationHandle = resultDestination.getDefaultCurveVisualizationHandle();
        this.resultHandle = resultDestination.getDefaultNumericalResultsHandler();

        batchModels.add(currentBatch);
    }

    public ProcessingModel(ResultDestinationPhotometric parent, PreprocessCurvesHandle<SimplePhotometricSource> preprocessingDestination, List<ProcessingBatchModel> batches)
    {
        this.resultDestination = parent;
        this.preprocessHandle = preprocessingDestination;
        this.currentBatch = batches.get(index);	
        this.currentBatch.addPropertyChangeListener(this);

        this.curveVisualizationHandle = resultDestination.getDefaultCurveVisualizationHandle();
        this.resultHandle = resultDestination.getDefaultNumericalResultsHandler();

        batchModels.addAll(batches);
    }


    public CurveVisualizationHandle<VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric>> getCurveVisualizationHandle()
    {
        return curveVisualizationHandle;
    }

    public void setCurveVisualizationHandle(CurveVisualizationHandle<VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric>> curveVisualizationHandle)
    {
        this.curveVisualizationHandle = curveVisualizationHandle;
    }

    public NumericalResultsHandler<ProcessedPackPhotometric> getNumericalResultsHandle()
    {
        return resultHandle;
    }

    public void setNumericalResultsHandle(NumericalResultsHandler<ProcessedPackPhotometric> resultHandleNew)
    {
        this.resultHandle = resultHandleNew;
    }

    public ResultDestinationPhotometric getResultDestination()
    {
        return resultDestination;
    }

    public Window getPublicationSite()
    {
        return resultDestination.getPublicationSite();
    }

    @Override
    public boolean isRestricted()
    {
        return false;
    }

    public void nextBatch()
    {
        int newIndex = index + 1;	
        if(newIndex == batchModels.size())
        {
            addNewBatch();
        }
        setCurrentBatch(newIndex);
    }

    public void previousBatch()
    {
        int newIndex = index - 1;	
        setCurrentBatch(newIndex);
    }

    private void setCurrentBatch(int newIndex)
    {
        int size = batchModels.size();
        boolean withinRange = (newIndex>=0)&&(newIndex<size);

        if(!withinRange)
        {
            return;
        }

        if(this.index != newIndex)
        {
            this.index = newIndex;
            ProcessingBatchModel newModel = batchModels.get(newIndex);
            setCurrentBatch(newModel);            
        }	
    }

    private void setCurrentBatch(ProcessingBatchModel newModel)
    {
        ProcessingBatchModel oldModel = currentBatch;

        this.currentBatch = newModel;
        this.currentBatch.addPropertyChangeListener(this);

        if(oldModel != null)
        {
            oldModel.removePropertyChangeListener(this);               
            firePropertyChange(ProcessingModel.CURRENT_BATCH_NUMBER, oldModel.getBatchNumber(), newModel.getBatchNumber());
        }        
    }

    public void addNewBatch()
    {
        int batchNumber = batchModels.size() + resultDestination.getResultBatchesCoordinator().getPublishedBatchCount();
        ProcessingBatchModel newBatch = new ProcessingBatchModel(resultDestination,Integer.toString(batchNumber),batchNumber);
        batchModels.add(newBatch);
    }

    public void addNewBatch(List<SimplePhotometricSource> sources)
    {
        int batchNumber = batchModels.size() + resultDestination.getResultBatchesCoordinator().getPublishedBatchCount();
        ProcessingBatchModel newBatch = new ProcessingBatchModel(resultDestination, sources, Integer.toString(batchNumber),batchNumber);
        batchModels.add(newBatch);
    }

    public boolean isCurrentBatchLast()
    {
        boolean isLast = (batchModels.size() - 1 == index);
        return isLast;
    }

    @Override
    public boolean areSourcesSelected()
    {
        return currentBatch.isNonEmpty();
    }

    public boolean areSettingsSpecified()
    {
        return currentBatch.areSettingSpecified();
    }

    public boolean areBasicSettingsSpecified()
    {
        return currentBatch.areBasicSettingsSpecified();
    }

    public boolean canProcessingOfCurrentBatchBeFinished()
    {
        boolean nonEmpty = currentBatch.isNonEmpty();
        boolean inputProvided = currentBatch.isNecessaryInputProvided();

        boolean canBeFinished = inputProvided && nonEmpty;
        return canBeFinished;
    }

    public boolean canProcessingBeFinished()
    {
        boolean allInputProvided = true;
        boolean isAtLeastOneBatchNonEmpty = false;
        for(ProcessingBatchModel batch: batchModels)
        {
            boolean nonEmpty = batch.isNonEmpty();
            isAtLeastOneBatchNonEmpty = isAtLeastOneBatchNonEmpty||nonEmpty;
            boolean inputProvided = batch.isNecessaryInputProvided();
            allInputProvided = allInputProvided&&inputProvided;
        }
        boolean canBeFinished = allInputProvided && isAtLeastOneBatchNonEmpty;
        return canBeFinished;
    }

    public int getCurrentBatchNumber()
    {
        return currentBatch.getBatchNumber();
    }

    @Override
    public String getIdentifier()
    {
        return Integer.toString(currentBatch.getBatchNumber());
    }

    public int getBatchNumber()
    {
        return batchModels.size();
    }

    public String getCurrentBatchName()
    {
        return currentBatch.getBatchName();
    }

    public void setBatchName(String batchNameNew)
    {
        currentBatch.setBatchName(batchNameNew);
    }

    public boolean isCalibrationInputEnabled()
    {
        return currentBatch.isCalibrationInputEnabled();
    }

    public boolean isCalibrationUseReadInEnabled()
    {
        return currentBatch.isCalibrationUseReadInEnabled();
    }

    public boolean isUseReadInCalibration()
    {
        return currentBatch.getUseReadInCalibration();
    }

    public void setUseReadInCalibration(boolean useReadInCalibrationNew)
    {
        currentBatch.setUseReadInCalibration(useReadInCalibrationNew);      
    }


    public double getCalibrationSlope()
    {
        return currentBatch.getCalibrationSlope();
    }

    public void setCalibrationSlope(double calibrationSlopeNew)
    {
        currentBatch.setCalibrationSlope(calibrationSlopeNew);
    }

    public double getCalibrationOffset()
    {
        return currentBatch.getCalibrationOffset();
    }

    public void setCalibrationOffset(double calibrationOffsetNew)
    {
        currentBatch.setCalibrationOffset(calibrationOffsetNew);
    }

    @Override
    public void setSources(List<SimplePhotometricSource> newSources)
    {
        currentBatch.setSources(newSources);
    }

    @Override
    public boolean isSourceFilteringPossible()
    {
        return false;
    }

    @Override
    public void addSources(List<SimplePhotometricSource> sourcesToAdd)
    {
        currentBatch.addSources(sourcesToAdd);
    }

    @Override
    public void removeSources(List<SimplePhotometricSource> sourcesToRemove)
    {
        currentBatch.removeSources(sourcesToRemove);
    }

    @Override
    public List<SimplePhotometricSource> getSources()
    {
        return currentBatch.getSources();
    }

    public File getCommonSourceDirectory()
    {
        return currentBatch.getCommonSourceDirectory();
    }

    @Override
    public void setLeftCropping(double leftTrimmingNew)
    {
        currentBatch.setLeftCropping(leftTrimmingNew);
    }

    public double getLeftTrimming()
    {
        return currentBatch.getLeftCropping();
    }

    @Override
    public void setRightCropping(double rightTrimmingNew)
    {
        currentBatch.setRightCropping(rightTrimmingNew);
    }

    public double getRightTrimming()
    {
        return currentBatch.getRightCropping();
    }

    @Override
    public void setLowerCropping(double lowerTrimmingNew)
    {
        currentBatch.setLowerCropping(lowerTrimmingNew);
    }

    public double getLowerTrimming()
    {
        return currentBatch.getLowerCropping();
    }

    @Override
    public void setUpperCropping(double upperTrimmingNew)
    {
        currentBatch.setUpperCropping(upperTrimmingNew);
    }

    public double getUpperTrimming()
    {
        return currentBatch.getUpperCropping();
    }

    public void setDomainCropped(boolean domainTrimmedNew)
    {
        currentBatch.setDomainCropped(domainTrimmedNew);
    }

    public boolean isDomainCropped()
    {
        return currentBatch.isDomainCropped();
    }

    public void setRangeCropped(boolean rangeTrimmedNew)
    {		
        currentBatch.setRangeCropped(rangeTrimmedNew);
    }

    public boolean isRangeTrimmed()
    {
        return currentBatch.isRangeTrimmed();
    }

    public void setSmootherType(SmootherType smootherNameNew)
    {
        currentBatch.setSmootherType(smootherNameNew);
    }

    public SmootherType getSmootherType()
    {
        return currentBatch.getSmootherType();
    }

    public void setDataSmoothed(boolean dataSmoothedNew)
    {		
        currentBatch.setDataSmoothed(dataSmoothedNew);
    }

    public boolean areDataSmoothed()
    {
        return currentBatch.areDataSmoothed();
    }

    public void setLoessSpan(double loessSpanNew)
    {
        currentBatch.setLoessSpan(loessSpanNew);
    }

    public double getLoessSpan()
    {
        return currentBatch.getLoessSpan();
    }

    public void setLoessIterations(Number loessIterationsNew)
    {
        currentBatch.setLoessIterations(loessIterationsNew);
    }

    public Number getLoessIterations()
    {
        return currentBatch.getLoessIterations();
    }

    public void setSavitzkyDegree(Number savitzkyDegreeNew)
    {
        currentBatch.setSavitzkyDegree(savitzkyDegreeNew);
    }

    public Number getSavitzkyDegree()
    {
        return currentBatch.getSavitzkyDegree();
    }

    public void setSavitzkySpan(Double savitzkySpanNew)
    {
        currentBatch.setSavitzkySpan(savitzkySpanNew);
    }

    public double getSavitzkySpan()
    {
        return currentBatch.getSavitzkySpan();
    }

    public int getDerivativePolynomialDegree()
    {
        return currentBatch.getDerivativePolynomialDegree();
    }

    public void setDerivativePolynomialDegree(int derivativePolynomialDegreeNew)
    {
        currentBatch.setDerivativePolynomialDegree(derivativePolynomialDegreeNew);
    }

    public double getDerivativeSpan()
    {
        return currentBatch.getDerivativeSpan();
    }

    public void setDerivativeSpan(double derivativeSpanNew)
    {
        currentBatch.setDerivativeSpan(derivativeSpanNew);
    }

    public SpanType getDerivativeSpanType()
    {
        return currentBatch.getDerivativeSpanType();
    }

    public void setDerivativeSpanType(SpanType spanTypeNew)
    {
        currentBatch.setDerivativeSpanType(spanTypeNew);       
    }

    public SpanGeometry getDerivativeSpanGeometry()
    {
        return currentBatch.getDerivativeSpanGeometry();
    }

    public void setDerivativeSpanGeometry(SpanGeometry spanGeometryNew)
    {
        currentBatch.setDerivativeSpanGeometry(spanGeometryNew);
    }

    public LocalRegressionWeightFunction getDerivativeWeightFunction()
    {
        return currentBatch.getDerivativeWeightFunction();
    }

    public void setDerivativeWeightFunction(LocalRegressionWeightFunction derivativeWeightFunctionNew)
    {
        currentBatch.setDerivativeWeightFunction(derivativeWeightFunctionNew);
    }

    public int getDerivativeRobustnessIterationsCount()
    {
        return currentBatch.getDerivativeRobustnessIterationsCount();
    }

    public void setDerivativeRobustnessIterationsCount(int derivativeRobustnessIterationsCountNew)
    {
        currentBatch.setDerivativeRobustnessIterationsCount(derivativeRobustnessIterationsCountNew);
    }  

    public boolean isPlotRecordedCurve()
    {
        return currentBatch.isPlotRecordedCurve();
    }

    public void setPlotRecordedCurve(boolean plotCurveNew)
    {
        currentBatch.setPlotRecordedCurve(plotCurveNew);
    }


    public boolean isPlotDerivativeCurve()
    {
        return currentBatch.isPlotDerivativeCurve();
    }

    public void setPlotDerivativeCurve(boolean plotDerivativeCurveNew)
    {
        currentBatch.setPlotDerivativeCurve(plotDerivativeCurveNew);
    }


    public boolean isSelectionOfCroppingOnCurvePossible()
    {
        return currentBatch.isSelectionOfCroppingOnCurvePossible();
    }

    public Dataset1DCroppingModel<Channel1DCollection> getCroppingModel()
    {
        return currentBatch.getCroppingModel();
    }

    public int getSignalIndex()
    {
        return currentBatch.getSignalIndex();
    }
    
    public void setSignalIndex(int signalIndexNew)
    {
        this.currentBatch.setSignalIndex(signalIndexNew);
    }
    
    public int getMaximalSignalIndex()
    {
        return currentBatch.getMaximalSignalIndex();
    }
    
    @Override
    public void showPreview()
    {
        List<SimplePhotometricSource> sources = currentBatch.getSources();
        showPreview(sources);
    }

    @Override
    public void showPreview(List<SimplePhotometricSource> sources)
    {
        showPreprocessing(sources);
    }

    public void showPreprocessing(List<SimplePhotometricSource> sources)
    {
        preprocessHandle.preprocess(sources);
    }

    @Override
    public void cancel()
    {
        resultDestination.endProcessing();
    }

    public void processCurvesConcurrently()
    {         
        Processor<ProcessablePackPhotometric, ProcessingResultPhotometric> processor = StandardProcessor.getInstance();
        List<ProcessablePackPhotometric> allPacks = getAllProcessablePacks();
        
        if(!allPacks.isEmpty())
        {
            ProcessingResultsHandler<ProcessingResultPhotometric> processingHandle =  new ProcessingHandlerPhotometric(resultDestination, curveVisualizationHandle, resultHandle);
            ConcurrentProcessingTask<ProcessablePackPhotometric, ProcessingResultPhotometric> task = new ConcurrentProcessingTask<>(allPacks, processor, processingHandle);
            task.execute();
        }
        else
        {
            resultDestination.withdrawPublication();
            resultDestination.endProcessing();
        }
    }

    public int processCurves()
    {        
        Processor<ProcessablePackPhotometric, ProcessingResultPhotometric> processor = StandardProcessor.getInstance();
        List<ProcessablePackPhotometric> allPacks = getAllProcessablePacks();

        int failures = 0;

        if(!allPacks.isEmpty())
        {
            ProcessingResultsHandler<ProcessingResultPhotometric> processingHandle = new ProcessingHandlerPhotometric(resultDestination, curveVisualizationHandle, resultHandle);
            List<ProcessingResultPhotometric> allProcessingResult = new ArrayList<>();

            for(ProcessablePackPhotometric pack : allPacks)
            {
                try
                {
                    ProcessingResultPhotometric result = processor.process(pack);                  
                    allProcessingResult.add(result);                                                 
                }           

                catch(Exception e)
                {
                    e.printStackTrace();
                    failures++;
                }
            }

            processingHandle.acceptAndSegregateResults(allProcessingResult);
            processingHandle.reactToFailures(failures);
            processingHandle.sendResultsToDestination();
            processingHandle.endProcessing();          
        }
        else
        {
            resultDestination.withdrawPublication();
            resultDestination.endProcessing();
        }

        return failures;
    }

    public List<ProcessablePackPhotometric> getAllProcessablePacks()
    {
        List<ProcessablePackPhotometric> allPacks = new ArrayList<>();

        for(ProcessingBatchModel model: batchModels)
        {                 
            if(model.isNonEmpty())
            {               
                List<ProcessablePackPhotometric> packs = model.buildProcessingBatch();

                allPacks.addAll(packs);
            }
        }

        return allPacks;
    }

    public Properties getProperties()
    {
        return currentBatch.getProperties();
    }

    public void loadProperties(Properties properties)
    {
        currentBatch.loadProperties(properties);
    }

    @Override
    public Window getParent() 
    {
        return getResultDestination().getPublicationSite();
    }

    @Override
    public String getTaskName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTaskDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isFirst() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isLast() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void back() {
        // TODO Auto-generated method stub

    }

    @Override
    public void next() {
        // TODO Auto-generated method stub

    }

    @Override
    public void skip() {
        // TODO Auto-generated method stub

    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isBackEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNextEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSkipEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isFinishEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNecessaryInputProvided() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        firePropertyChange(evt);
    }
}
