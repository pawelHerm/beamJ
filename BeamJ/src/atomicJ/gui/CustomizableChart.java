package atomicJ.gui;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.Pannable;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;

public abstract class CustomizableChart extends JFreeChart implements CustomChartMouseListener
{
    private static final long serialVersionUID = 1L;

    private Map<String, String> automaticTitles;

    public CustomizableChart(Plot plot)
    {
        super(plot);
    }

    public CustomizableChart(String title, Plot plot)
    {
        super(title, plot);
    }

    public CustomizableChart(String title, Font titleFont, Plot plot,
            boolean createLegend)
    {
        super(title, titleFont, plot, createLegend);
    }

    public void notifyAboutToolChange()
    {
        fireChartChanged();
    }

    public void setAutomaticTitles(Map<String, String> defaultChartStyles)
    {
        this.automaticTitles = defaultChartStyles;
    }

    public Map<String, String> getAutomaticTitles()
    {               
        if(automaticTitles != null)
        {
            return automaticTitles;
        }

        return new LinkedHashMap<>();
    }

    public String getDataTooltipText(Point2D java2DPoint, ChartRenderingInfo info)
    {
        String tooltipText = null;

        ChartEntity entity = getChartEntity(java2DPoint, info);
        if (entity != null) 
        {                    
            tooltipText = entity.getToolTipText();
        }

        return tooltipText;
    }


    public ChartEntity getChartEntity(Point2D java2DPoint, ChartRenderingInfo info)
    {
        ChartEntity entity = null;
        EntityCollection entities = info.getEntityCollection();

        if (entities != null) 
        {
            entity = entities.getEntity(java2DPoint.getX(), java2DPoint.getY());            
        }

        return entity;
    }


    public abstract boolean isDomainZoomable();
    public abstract boolean isRangeZoomable();
    public abstract boolean isDomainPannable();
    public abstract boolean isRangePannable();
    public boolean canPossiblyBePannable()
    {
        Plot plot = getCustomizablePlot();
        boolean possiblyPannable = (plot instanceof Pannable);
        return possiblyPannable;
    }

    public abstract MouseInputMode getMode();
    public abstract void setMode(MouseInputMode mode);

    public abstract ChartSupervisor getChartSupervisor();
    public abstract void setChartSupervisor(ChartSupervisor chartSupervisor);  

    public abstract void updateFixedDataAreaSize(Graphics2D g2, Rectangle2D chartArea);
    public abstract Point2D getDataPoint(Point2D java2DPoint, PlotRenderingInfo info);

    public abstract RoamingTextTitle buildNewTitle(String text);

    public abstract void setRightPadding(double paddingRight);
    public abstract void setLeftPadding(double paddingLeft);
    public abstract void setBottomPadding(double paddingBottom);
    public abstract void setTopPadding(double paddingTop);

    public abstract double getTopPadding();
    public abstract double getLeftPadding();
    public abstract double getRightPadding();
    public abstract double getBottomPadding();

    public abstract Preferences getPreferences();
    public abstract Preferences getPlotSpecificPreferences();

    public abstract Plot getCustomizablePlot();

}
