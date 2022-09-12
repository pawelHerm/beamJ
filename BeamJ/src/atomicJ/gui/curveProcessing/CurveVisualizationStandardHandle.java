package atomicJ.gui.curveProcessing;

import java.util.LinkedHashMap;
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

public class CurveVisualizationStandardHandle <S extends Channel1DSource<?>,E extends Channel1DResource<S>> implements CurveVisualizationHandle<VisualizablePack<S,E>> 
{
    private final ResourceXYPresentationView<E, ChannelChart<?>, ?> graphsDialog;

    public CurveVisualizationStandardHandle(ResourceXYPresentationView<E, ChannelChart<?>, ?> graphsDialog)
    {
        this.graphsDialog = graphsDialog;
    }

    @Override
    public void handlePublicationRequest(List<VisualizablePack<S,E>> visualizablePacks)
    {
        if(!visualizablePacks.isEmpty())
        {
            final ConcurrentCurveVisualizationTask<VisualizablePack<S,E>> task = new ConcurrentCurveVisualizationTask<>(visualizablePacks, graphsDialog, new StandardChartVisualizationHandle());
            task.execute();
        }
    }

    private class StandardChartVisualizationHandle implements ChartDrawingHandle<VisualizablePack<S,E>>
    {
        @Override
        public void sendChartsToDestination(Map<VisualizablePack<S,E>, Map<String, ChannelChart<?>>> charts)
        {
            Map<E, Map<String, ChannelChart<?>>> resourceChartMap = new LinkedHashMap<>();

            for(Entry<VisualizablePack<S,E>, Map<String, ChannelChart<?>>> entry : charts.entrySet())
            {
                VisualizablePack<S,E> pack = entry.getKey();
                resourceChartMap.put(pack.buildChannelResource(), entry.getValue());
            }

            int previousCount = graphsDialog.getResourceCount();    

            graphsDialog.addResources(resourceChartMap);
            graphsDialog.selectResource(previousCount);
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
