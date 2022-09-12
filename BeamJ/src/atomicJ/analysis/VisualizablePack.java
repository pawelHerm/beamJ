package atomicJ.analysis;


import java.util.Collection;
import java.util.Map;

import atomicJ.data.Channel1D;
import atomicJ.resources.Channel1DResource;
import atomicJ.sources.Channel1DSource;

public interface VisualizablePack <S extends Channel1DSource<?>,R extends Channel1DResource<S>> extends Visualizable
{
    public S getSource();
    public R buildChannelResource();
    public Map<String, Collection<? extends Channel1D>> getChannels();
}
