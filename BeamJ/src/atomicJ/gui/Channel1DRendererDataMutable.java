package atomicJ.gui;

public class Channel1DRendererDataMutable extends XYLineAndShapeRendererDataMutable implements Channel1DRendererData
{
    private int markerIndex;
    private float markerSize;

    public Channel1DRendererDataMutable() 
    {}

    public Channel1DRendererDataMutable(Channel1DRendererData data)
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
        return this;
    }

    @Override
    public Channel1DRendererDataImmutable getImmutableVersion()
    {
        Channel1DRendererDataImmutable immutableCopy = new Channel1DRendererDataImmutable(this);
        return immutableCopy;
    }

    @Override
    public int getBaseMarkerIndex() 
    {
        return this.markerIndex;
    }

    public void setBaseMarkerIndex(int i)
    {
        this.markerIndex = i;
        setBaseShape(ShapeSupplier.createShape(markerIndex,markerSize));
    }   

    @Override
    public float getBaseMarkerSize() 
    {
        return this.markerSize;
    }

    public void setBaseMarkerSize(float size)
    {
        this.markerSize = size;
        setBaseShape(ShapeSupplier.createShape(markerIndex, markerSize));
    }
}
