package atomicJ.gui;

import org.jfree.data.xy.XYDataset;

public class Layer
{
    private final Object key;

    private final ChannelRenderer renderer;
    private final XYDataset dataset;

    public Layer(Object key, XYDataset dataset, ChannelRenderer renderer)
    {
        this.key = key;
        this.renderer = renderer;
        this.dataset = dataset;
    }

    public Object getKey()
    {
        return key;
    }

    public ChannelRenderer getRenderer()
    {
        return renderer;
    }

    public XYDataset getDataset()
    {
        return dataset;
    }
}
