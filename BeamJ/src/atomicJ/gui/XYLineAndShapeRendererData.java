package atomicJ.gui;

import atomicJ.gui.AbstractXYRendererData;

public interface XYLineAndShapeRendererData extends AbstractXYRendererData
{   
    public boolean getDrawSeriesLineAsPath();
    public boolean getBaseLinesVisible();
    public boolean getBaseShapesVisible();
    public boolean getBaseShapesFilled();
    public boolean getDrawOutlines();
    public boolean getUseFillPaint();
    public boolean getUseOutlinePaint();
}
