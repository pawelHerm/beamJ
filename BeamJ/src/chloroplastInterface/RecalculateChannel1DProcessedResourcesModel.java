package chloroplastInterface;

import atomicJ.gui.ResourceBasedModel;
import atomicJ.gui.ResourceGroupModel;
import atomicJ.gui.ResourceGroupSelectionModel;
import atomicJ.resources.Channel1DProcessedResource;


public class RecalculateChannel1DProcessedResourcesModel<R extends Channel1DProcessedResource<?>> extends ResourceBasedModel<R>
{
    public static final String DELETE_OLD_CURVE_CHARTS = "DeleteOldCurveCharts";
    public static final String DELETE_OLD_NUMERICAL_RESULTS = "DeleteOldNumericalResults";

    private boolean deleteOldCurveCharts = true;
    private boolean deleteOldNumericalResults = true;

    public RecalculateChannel1DProcessedResourcesModel(ResourceGroupModel<R> dataModel, ResourceGroupSelectionModel<R> selectionModel) {
        super(dataModel, selectionModel);
    }

    public boolean isDeleteOldCurveCharts()
    {
        return deleteOldCurveCharts;
    }

    public void setDeleteOldCurveCharts(boolean deleteOldCurveChartsNew)
    {
        boolean deleteOldCurveChartsOld = this.deleteOldCurveCharts;
        this.deleteOldCurveCharts = deleteOldCurveChartsNew;

        firePropertyChange(DELETE_OLD_CURVE_CHARTS, deleteOldCurveChartsOld, deleteOldCurveChartsNew);
    }

    public boolean isDeleteOldNumericalResults()
    {
        return deleteOldNumericalResults;
    }

    public void setDeleteOldNumericalResults(boolean deleteOldNumericalResultsNew)
    {
        boolean deleteOldNumericalResultsOld = this.deleteOldNumericalResults;
        this.deleteOldNumericalResults = deleteOldNumericalResultsNew;

        firePropertyChange(DELETE_OLD_NUMERICAL_RESULTS, deleteOldNumericalResultsOld, deleteOldNumericalResultsNew);
    }

    @Override
    public void reset() {

    }
}
