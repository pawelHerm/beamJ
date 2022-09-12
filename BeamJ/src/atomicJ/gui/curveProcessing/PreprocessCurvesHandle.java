package atomicJ.gui.curveProcessing;

import java.util.List;

import atomicJ.sources.Channel1DSource;

public interface PreprocessCurvesHandle<E extends Channel1DSource<?>>
{
    public void preprocess(List<E> sources);
}
