package atomicJ.gui;

import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.urls.XYURLGenerator;

public abstract class AbstractXYRendererDataMutable extends AbstractRendererDataMutable implements AbstractXYRendererData
{    
    private XYItemLabelGenerator baseItemLabelGenerator;
    private XYToolTipGenerator baseToolTipGenerator;
    private XYURLGenerator urlGenerator;

    private XYSeriesLabelGenerator legendItemLabelGenerator;
    private XYSeriesLabelGenerator legendItemToolTipGenerator;
    private XYSeriesLabelGenerator legendItemURLGenerator;

    public AbstractXYRendererDataMutable()
    {
        this.legendItemLabelGenerator = AbstractXYRendererData.DEFAULT_LEGEND_ITEM_LABEL_GENERATOR;
    }

    public AbstractXYRendererDataMutable(AbstractXYRendererData data)
    {
        super(data);       
        this.legendItemLabelGenerator = data.getLegendItemLabelGenerator();
    }

    @Override
    public XYItemLabelGenerator getBaseItemLabelGenerator() 
    {
        return this.baseItemLabelGenerator;
    }

    public void setBaseItemLabelGenerator(XYItemLabelGenerator generator)
    {
        this.baseItemLabelGenerator = generator;
        notifyRendererOfDataChange();
    }

    @Override
    public XYToolTipGenerator getBaseToolTipGenerator() {
        return this.baseToolTipGenerator;
    }

    public void setBaseToolTipGenerator(XYToolTipGenerator generator) 
    {
        this.baseToolTipGenerator = generator;
        notifyRendererOfDataChange();
    }

    @Override
    public XYURLGenerator getURLGenerator() {
        return this.urlGenerator;
    }

    public void setURLGenerator(XYURLGenerator urlGenerator) {
        this.urlGenerator = urlGenerator;
        notifyRendererOfDataChange();
    }

    @Override
    public XYSeriesLabelGenerator getLegendItemLabelGenerator() {
        return this.legendItemLabelGenerator;
    }

    public void setLegendItemLabelGenerator(XYSeriesLabelGenerator generator) {
        if (generator == null) {
            throw new IllegalArgumentException("Null 'generator' argument.");
        }
        this.legendItemLabelGenerator = generator;
        notifyRendererOfDataChange();
    }

    @Override
    public XYSeriesLabelGenerator getLegendItemToolTipGenerator() {
        return this.legendItemToolTipGenerator;
    }


    public void setLegendItemToolTipGenerator(XYSeriesLabelGenerator generator) {
        this.legendItemToolTipGenerator = generator;
        notifyRendererOfDataChange();
    }


    @Override
    public XYSeriesLabelGenerator getLegendItemURLGenerator() {
        return this.legendItemURLGenerator;
    }    

    public void setLegendItemURLGenerator(XYSeriesLabelGenerator generator) {
        this.legendItemURLGenerator = generator;
        notifyRendererOfDataChange();
    }
}
