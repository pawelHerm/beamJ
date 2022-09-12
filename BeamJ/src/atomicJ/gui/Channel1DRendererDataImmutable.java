package atomicJ.gui;

public class Channel1DRendererDataImmutable extends XYLineAndShapeRendererDataImmutable implements Channel1DRendererData
{
    private final int markerIndex;
    private final float markerSize;

    public Channel1DRendererDataImmutable(Channel1DRendererData data)
    {
        super(data);

        this.markerIndex = data.getBaseMarkerIndex();      
        this.markerSize = data.getBaseMarkerSize();
    }

    @Override
    public Channel1DRendererDataMutable getMutableCopy()
    {
        Channel1DRendererDataMutable mutableCopy = new Channel1DRendererDataMutable(this);
        return mutableCopy;
    }

    @Override
    public Channel1DRendererDataMutable getMutableVersion()
    {
        Channel1DRendererDataMutable mutableCopy = new Channel1DRendererDataMutable(this);
        return mutableCopy;
    }

    @Override
    public Channel1DRendererDataImmutable getImmutableVersion()
    {
        return this;
    }

    @Override
    public int getBaseMarkerIndex() 
    {
        return this.markerIndex;
    }

    @Override
    public float getBaseMarkerSize() 
    {
        return this.markerSize;
    }

}
