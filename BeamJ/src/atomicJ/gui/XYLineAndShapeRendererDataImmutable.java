package atomicJ.gui;

public class XYLineAndShapeRendererDataImmutable extends AbstractXYRendererDataImmutable implements XYLineAndShapeRendererData
{
    private final boolean baseLinesVisible;
    private final boolean baseShapesVisible;
    private final boolean baseShapesFilled;
    private final boolean drawOutlines;
    private final boolean useFillPaint;
    private final boolean useOutlinePaint;
    private final boolean drawSeriesLineAsPath;

    public XYLineAndShapeRendererDataImmutable()
    {
        this.baseLinesVisible = true;
        this.baseShapesVisible = true;
        this.useFillPaint = false;     // use item paint for fills by default
        this.baseShapesFilled = true;
        this.drawOutlines = true;
        this.useOutlinePaint = false;  // use item paint for outlines by default, not outline paint
        this.drawSeriesLineAsPath = false;
    }

    public XYLineAndShapeRendererDataImmutable(XYLineAndShapeRendererData data)
    {
        super(data);

        this.baseLinesVisible = data.getBaseLinesVisible();
        this.baseShapesVisible = data.getBaseShapesVisible();
        this.useFillPaint = data.getUseFillPaint();   
        this.baseShapesFilled = data.getBaseShapesFilled();
        this.drawOutlines = data.getDrawOutlines();
        this.useOutlinePaint = data.getUseOutlinePaint();  
        this.drawSeriesLineAsPath = data.getDrawSeriesLineAsPath();
    }

    @Override
    public XYLineAndShapeRendererDataMutable getMutableCopy()
    {
        XYLineAndShapeRendererDataMutable mutableCopy = new XYLineAndShapeRendererDataMutable(this);
        return mutableCopy;
    }

    @Override
    public XYLineAndShapeRendererDataMutable getMutableVersion()
    {
        XYLineAndShapeRendererDataMutable mutableCopy = new XYLineAndShapeRendererDataMutable(this);
        return mutableCopy;
    }


    @Override
    public XYLineAndShapeRendererDataImmutable getImmutableVersion()
    {
        return this;
    }

    @Override
    public boolean getDrawSeriesLineAsPath()
    {
        return this.drawSeriesLineAsPath;
    }

    @Override
    public boolean getBaseLinesVisible()
    {
        return this.baseLinesVisible;
    }

    @Override
    public boolean getBaseShapesVisible()
    {
        return this.baseShapesVisible;
    }

    @Override
    public boolean getBaseShapesFilled()
    {
        return this.baseShapesFilled;
    }

    @Override
    public boolean getDrawOutlines()
    {
        return this.drawOutlines;
    }

    @Override
    public boolean getUseFillPaint()
    {
        return this.useFillPaint;
    }

    @Override
    public boolean getUseOutlinePaint()
    {
        return this.useOutlinePaint;
    }
}
