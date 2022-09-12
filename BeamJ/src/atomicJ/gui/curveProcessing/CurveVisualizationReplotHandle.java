package atomicJ.gui.curveProcessing;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import atomicJ.analysis.VisualizablePack;
import atomicJ.data.Channel1D;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.ResourceXYPresentationView;
import atomicJ.resources.Channel1DResource;
import atomicJ.sources.Channel1DSource;

public class CurveVisualizationReplotHandle <S extends Channel1DSource<?>,E extends Channel1DResource<S>> implements CurveVisualizationHandle<VisualizablePack<S,E>>
{
    private final ResourceXYPresentationView<E, ChannelChart<?>, ?> graphsDialog;
    private final Map<S, S> mapNewVsOldSource;

    public CurveVisualizationReplotHandle(ResourceXYPresentationView<E, ChannelChart<?>, ?> graphsDialog, Map<S, S> mapNewVsOldSource)
    {
        this.graphsDialog = graphsDialog;
        this.mapNewVsOldSource = new LinkedHashMap<>(mapNewVsOldSource);
    }

    @Override
    public void handlePublicationRequest(List<VisualizablePack<S,E>> visualizablePacks)
    {               
        if(!visualizablePacks.isEmpty())
        {
            Map<E, Map<String, Collection<? extends Channel1D>>> channels = new LinkedHashMap<>();
            Map<E, E> resourcesNewVsOld = new LinkedHashMap<>();

            for(VisualizablePack<S,E> visPack : visualizablePacks)
            {
                E resource = visPack.buildChannelResource();
                resourcesNewVsOld.put(resource, graphsDialog.getResourceContainingChannelsFrom(mapNewVsOldSource.get(visPack.getSource())));

                Map<String, Collection<? extends Channel1D>> charts = visPack.getChannels();
                channels.put(resource, charts);
            }

            graphsDialog.replaceData(channels, resourcesNewVsOld);
        }       
    }
}