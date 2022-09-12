package chloroplastInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import atomicJ.analysis.VisualizablePack;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.ResourceGroupModel;
import atomicJ.gui.ResourceGroupSelectionModel;
import atomicJ.gui.curveProcessing.CurveVisualizationHandle;
import atomicJ.gui.curveProcessing.CurveVisualizationReplaceHandle;
import atomicJ.gui.curveProcessing.NumericalResultsHandler;
import atomicJ.gui.curveProcessing.NumericalResultsSourceReplaceHandle;
import atomicJ.resources.Channel1DResource;


public class RecalculatePhotometricProcessedResourcesModel extends RecalculateChannel1DProcessedResourcesModel<ProcessedResourcePhotometric>
{

    public RecalculatePhotometricProcessedResourcesModel(ResourceGroupModel<ProcessedResourcePhotometric> dataModel, ResourceGroupSelectionModel<ProcessedResourcePhotometric> selectionModel) {
        super(dataModel, selectionModel);
    }

    @Override
    public void apply()
    {
        super.apply();
        recalculatePacks(AtomicJ.currentFrame, AtomicJ.currentFrame, Channel1DResource.getSources(getData()));
    }

    private void recalculatePacks(ProcessingOrigin<SimplePhotometricSource,ProcessedPackPhotometric> processingOrigin,ResultDestinationPhotometric destination, List<SimplePhotometricSource> sources)
    {
        List<ProcessingBatchModel> batchModels = new ArrayList<>();

        Map<SimplePhotometricSource, SimplePhotometricSource> sourcesNewVsOld = StandardPhotometricSource.copySources(sources);

        int batchNumber = destination.getResultBatchesCoordinator().getPublishedBatchCount();
        String name = Integer.toString(batchNumber);

        ProcessingBatchModel model = new ProcessingBatchModel(destination, new ArrayList<>(sourcesNewVsOld.keySet()), name, batchNumber);
        batchModels.add(model);

        NumericalResultsHandler<ProcessedPackPhotometric> resultsHandle = isDeleteOldNumericalResults() ? new NumericalResultsSourceReplaceHandle<>(destination.getResultDialog(), sources) : destination.getDefaultNumericalResultsHandler();
        CurveVisualizationHandle<VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric>> curveVisualizationHandle = isDeleteOldCurveCharts() ? new CurveVisualizationReplaceHandle<>(destination.getGraphicalResultsDialog(), sourcesNewVsOld) : destination.getDefaultCurveVisualizationHandle();

        processingOrigin.startProcessing(batchModels, curveVisualizationHandle, resultsHandle);        
    }

    @Override
    public void reset() {
    }
}
