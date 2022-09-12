package atomicJ.curveProcessing;

import atomicJ.data.Channel1D;
import atomicJ.resources.Channel1DResource;
import atomicJ.resources.ResourceView;

public class GaussianFilter1DModel<R extends Channel1DResource> extends GaussianBasedFilter1DModel<R>
{
    public GaussianFilter1DModel(ResourceView<R, Channel1D, String> manager)
    {
        super(manager);
    }

    @Override
    protected Channel1DDataInROITransformation buildTransformation()
    {
        Kernel1D kernel = new KernelSampledGaussian1D(getSigmaX());
        Channel1DDataInROITransformation transformation = new Kernel1DConvolution(kernel);
        return transformation;
    }
}
