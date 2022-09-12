package atomicJ.gui;


import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.resources.Channel1DProcessedResource;
import chloroplastInterface.RecalculateChannel1DProcessedResourcesModel;

public class Channel1DRecalculationDialogModel extends ModifiableResourceDialogModel<Channel1D,Channel1DData,String, Channel1DProcessedResource<?>>
{
    public RecalculateChannel1DProcessedResourcesModel<?> getRecalculationModel()
    {
        return new RecalculateChannel1DProcessedResourcesModel<>(getDataModel(), getSelectionModel());
    }
}
