package atomicJ.gui.curveProcessing;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import atomicJ.analysis.VisualizablePack;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.ResourceXYPresentationView;
import atomicJ.resources.Channel1DResource;
import atomicJ.sources.Channel1DSource;

public class CurveVisualizationReplaceHandle <S extends Channel1DSource<?>,E extends Channel1DResource<S>> implements CurveVisualizationHandle<VisualizablePack<S,E>>
{
    private final ResourceXYPresentationView<E, ChannelChart<?>, ?> graphsDialog;
    private final Map<S, S> mapNewVsOldSource;

    public CurveVisualizationReplaceHandle(ResourceXYPresentationView<E, ChannelChart<?>, ?> graphsDialog, Map<S, S> mapNewVsOldSource)
    {
        this.graphsDialog = graphsDialog;
        this.mapNewVsOldSource = new LinkedHashMap<>(mapNewVsOldSource);
    }

    @Override
    public void handlePublicationRequest(List<VisualizablePack<S,E>> visualizablePacks)
    {       
        if(!visualizablePacks.isEmpty())
        {
            ConcurrentCurveVisualizationTask<VisualizablePack<S,E>> task = new ConcurrentCurveVisualizationTask<>(visualizablePacks, graphsDialog, new ReplaceChartVisualizationHandle());
            task.execute();
        }
        //even if we do not want to show newly plotted charts, we still have to delete old ones
        else
        {
            graphsDialog.removeResources(new LinkedHashSet<>(mapNewVsOldSource.values()));
        }
    }

    private class ReplaceChartVisualizationHandle implements ChartDrawingHandle<VisualizablePack<S,E>>
    { 
        @Override
        public void sendChartsToDestination(Map<VisualizablePack<S,E>, Map<String, ChannelChart<?>>> charts)
        {
            Map<E, Map<String, ChannelChart<?>>> resourceChartMap = new LinkedHashMap<>();
            Map<E, E> resourcesNewVsOld = new LinkedHashMap<>();

            for(Entry<VisualizablePack<S,E>, Map<String, ChannelChart<?>>> entry : charts.entrySet())
            {
                VisualizablePack<S,E> pack = entry.getKey();
                E resource = pack.buildChannelResource();
                resourcesNewVsOld.put(resource, graphsDialog.getResourceContainingChannelsFrom(mapNewVsOldSource.get(pack.getSource())));
                resourceChartMap.put(resource, entry.getValue());
            }

            graphsDialog.addOrReplaceResources(resourceChartMap, resourcesNewVsOld);

            int[] publishedChartIndices = graphsDialog.getIndicesOfPresentResources(charts.keySet());

            if(publishedChartIndices.length > 0)
            {
                graphsDialog.selectResource(publishedChartIndices[0]);
            }
        }

        @Override
        public void handleFinishDrawingRequest()
        {
            graphsDialog.drawingChartsFinished();    
        }

        @Override
        public void reactToFailures(int failureCount)
        {
            if(failureCount > 0)
            {
                JOptionPane.showMessageDialog(graphsDialog, "Errors occured during rendering of " + failureCount + " charts", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
            }           
        }   

        @Override
        public void reactToCancellation()
        {
            JOptionPane.showMessageDialog(graphsDialog, "Rendering terminated", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }
}