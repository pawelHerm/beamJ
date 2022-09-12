package atomicJ.gui;

import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.urls.XYURLGenerator;

public abstract class AbstractXYRendererDataImmutable extends AbstractRendererDataImmutable implements AbstractXYRendererData
{    
    private XYItemLabelGenerator baseItemLabelGenerator;
    private XYToolTipGenerator baseToolTipGenerator;
    private XYURLGenerator urlGenerator;

    private final XYSeriesLabelGenerator legendItemLabelGenerator;
    private XYSeriesLabelGenerator legendItemToolTipGenerator;
    private XYSeriesLabelGenerator legendItemURLGenerator;

    public AbstractXYRendererDataImmutable()
    {
        this.legendItemLabelGenerator = AbstractXYRendererData.DEFAULT_LEGEND_ITEM_LABEL_GENERATOR;
    }

    public AbstractXYRendererDataImmutable(AbstractXYRendererData data)
    {
        super(data);       
        this.legendItemLabelGenerator = data.getLegendItemLabelGenerator();
    }

    @Override
    public XYItemLabelGenerator getBaseItemLabelGenerator() {
        return this.baseItemLabelGenerator;
    }

    @Override
    public XYToolTipGenerator getBaseToolTipGenerator() {
        return this.baseToolTipGenerator;
    }

    @Override
    public XYURLGenerator getURLGenerator() {
        return this.urlGenerator;
    }

    @Override
    public XYSeriesLabelGenerator getLegendItemLabelGenerator() {
        return this.legendItemLabelGenerator;
    }

    @Override
    public XYSeriesLabelGenerator getLegendItemToolTipGenerator() {
        return this.legendItemToolTipGenerator;
    }

    @Override
    public XYSeriesLabelGenerator getLegendItemURLGenerator() {
        return this.legendItemURLGenerator;
    }
}
