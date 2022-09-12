package atomicJ.gui.curveProcessing;

import java.util.List;

import atomicJ.analysis.Visualizable;

public interface CurveVisualizationHandle <E extends Visualizable>
{
    public void handlePublicationRequest(List<E> visualizablePacks);  
}
