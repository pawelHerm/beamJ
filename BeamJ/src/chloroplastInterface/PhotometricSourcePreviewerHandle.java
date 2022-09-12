package chloroplastInterface;

import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import atomicJ.analysis.PreviewDestination;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.ConcurrentPreviewTask.SourcePreviewerHandle;
import atomicJ.gui.ConcurrentPreviewTask.SourcePreviewerTask;

public class PhotometricSourcePreviewerHandle implements SourcePreviewerHandle<SimplePhotometricSource>
{
    private final PreviewDestination<PhotometricResource, ChannelChart<?>> destinationSpectroscopy;

    private final List<SpectroscopyPreviewerTask> createdTasks = new ArrayList<>();

    public PhotometricSourcePreviewerHandle(PreviewDestination<PhotometricResource, ChannelChart<?>> destinationSpectroscopy)
    {
        this.destinationSpectroscopy = destinationSpectroscopy;
    }

    @Override
    public SourcePreviewerTask<SimplePhotometricSource> createAndRegisterANewTask() 
    {
        SpectroscopyPreviewerTask task = new SpectroscopyPreviewerTask();
        createdTasks.add(task);
        return task;
    }

    @Override
    public Window getAssociatedComponent()
    {
        return this.destinationSpectroscopy.getPublicationSite();
    }

    @Override
    public void sendPreviewedDataToDestination() 
    {
        Map<PhotometricResource, Map<String,ChannelChart<?>>> curveChartMap = new LinkedHashMap<>();

        for(SpectroscopyPreviewerTask subtask: createdTasks)
        {
            curveChartMap.putAll(subtask.getConstructedCurveCharts());
        }

        this.destinationSpectroscopy.publishPreview(curveChartMap);
    }

    @Override
    public void reactToCancellation()
    {
        JOptionPane.showMessageDialog(getAssociatedComponent(), "Previewing terminated", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);            
    }

    @Override
    public void reactToFailures(int failureCount) 
    {
        if(failureCount > 0)
        {
            JOptionPane.showMessageDialog(getAssociatedComponent(), "Errors occured during rendering of " + failureCount + " charts", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }         

    @Override
    public void requestPreviewEndAndPrepareForNewExecution()
    {
        this.destinationSpectroscopy.requestPreviewEnd();
        this.createdTasks.clear();
    }

    @Override
    public void showMessage(String message) 
    {
        JOptionPane.showMessageDialog(getAssociatedComponent(), message, "", JOptionPane.WARNING_MESSAGE);
    } 

    private static class SpectroscopyPreviewerTask implements SourcePreviewerTask<SimplePhotometricSource>
    {
        private final Map<PhotometricResource, Map<String,ChannelChart<?>>> constructedCurveCharts = new LinkedHashMap<>();

        public Map<PhotometricResource, Map<String, ChannelChart<?>>> getConstructedCurveCharts()
        {
            return constructedCurveCharts;
        }

        @Override
        public void preview(SimplePhotometricSource source) 
        {
            Map<String,ChannelChart<?>> charts = PhotometricSourceVisualization.getCharts(source);
            constructedCurveCharts.put(new PhotometricResource(source), charts);
        }
    }
}