package chloroplastInterface;


import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.gui.ModifiableResourceDialogModel;
import atomicJ.resources.Channel1DProcessedResource;

public class Channel1DResultsDialogModel<R extends Channel1DProcessedResource<?>> extends ModifiableResourceDialogModel<Channel1D,Channel1DData,String, R>
{
    public RecalculateChannel1DProcessedResourcesModel<R> getRecalculationModel()
    {
        return new RecalculateChannel1DProcessedResourcesModel<R>(getDataModel(), getSelectionModel());
    }
}
