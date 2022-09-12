
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import atomicJ.analysis.VisualizablePack;
import atomicJ.data.Channel1D;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.curveProcessing.CurveVisualizationHandle;
import atomicJ.gui.curveProcessing.CurveVisualizationReplotHandle;
import atomicJ.gui.curveProcessing.NumericalResultsHandler;
import atomicJ.gui.curveProcessing.NumericalResultsSourceReplaceHandle;
import atomicJ.resources.Channel1DProcessedResource;
import atomicJ.utilities.MultiMap;


public class ProcessedResourcePhotometric extends Channel1DProcessedResource<SimplePhotometricSource>
{
    private final ProcessedPackPhotometric processedPack;

    public ProcessedResourcePhotometric(ProcessedPackPhotometric processedPack, MultiMap<String, Channel1D> channelsTypeMap)
    {
        super(processedPack.getSource(), channelsTypeMap);
        this.processedPack = processedPack;
    }

    public ProcessedResourcePhotometric(ProcessedPackPhotometric processedPack, Map<String, Channel1D> channelsTypeMap)
    {
        super(processedPack.getSource(), channelsTypeMap);
        this.processedPack = processedPack;
    }

    public ProcessedResourcePhotometric(ProcessedResourcePhotometric that)
    {
        super(that);
        this.processedPack = that.processedPack.copy();
    }


    public static List<String> getDefaultTypes()
    {
        return Arrays.asList(RecordingModel.TRANSMITTANCE_CHANNEL_KEY, RecordingModel.TRANSMITTANCE_DERIVATIVE_CHANNEL_KEY);
    }


    private VisualizationSettingsPhotometric getVisualizationSettings()
    {
        boolean plotRecordedCurve = getIdentifiersForAllTypes().contains(RecordingModel.TRANSMITTANCE_CHANNEL_KEY);
        boolean plotDerivativeCurve = getIdentifiersForAllTypes().contains(RecordingModel.TRANSMITTANCE_DERIVATIVE_CHANNEL_KEY);

        return new VisualizationSettingsPhotometric(plotRecordedCurve, plotDerivativeCurve);
    }

    @Override
    public ProcessedResourcePhotometric copy()
    {
        ProcessedResourcePhotometric copied = new ProcessedResourcePhotometric(this);
        return copied;
    }

    public ProcessedPackPhotometric getProcessedPack()
    {
        return processedPack;
    }

    @Override
    public SimplePhotometricSource getSource()
    {
        return processedPack.getSource();
    }

    private void revertModifications()
    {
        SimplePhotometricSource source = processedPack.getSource(); 

        Map<SimplePhotometricSource, SimplePhotometricSource> sourcesNewVsOld = StandardPhotometricSource.copySources(Collections.singletonList(source));

        ProcessingBatchMementoPhotometric memento = source.getProcessingMemento();
        ProcessingBatchModel model = new ProcessingBatchModel(memento, getVisualizationSettings(), new ArrayList<>(sourcesNewVsOld.keySet()));

        recalculate(model, sourcesNewVsOld);
    }

    private int recalculate(ProcessingBatchModel model, Map<SimplePhotometricSource, SimplePhotometricSource> sourcesNewVsOld)
    {
        MainFrame parent = AtomicJ.currentFrame;

        NumericalResultsHandler<ProcessedPackPhotometric> resultsHandle = new NumericalResultsSourceReplaceHandle<>(parent.getResultDialog(), new ArrayList<>(sourcesNewVsOld.values()));
        CurveVisualizationHandle<VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric>> curveVisualizationHandle = new CurveVisualizationReplotHandle<>(parent.getGraphicalResultsDialog(), sourcesNewVsOld);

        ProcessingModel processingModel = new ProcessingModel(parent, parent.getDefaultPreprocessCurvesHandle(), Collections.singletonList(model));
        // new ProcessingModel(parent, parent.getImagePreviewDestination(), parent.getDefaultPreprocessCurvesHandle(), Collections.singletonList(model), true);
        processingModel.setCurveVisualizationHandle(curveVisualizationHandle);
        processingModel.setNumericalResultsHandle(resultsHandle);

        int failuresCount = processingModel.processCurves();  

        return failuresCount;
    }
}
