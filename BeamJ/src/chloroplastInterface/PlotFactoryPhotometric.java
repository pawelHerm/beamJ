package chloroplastInterface;

import java.util.prefs.Preferences;

import org.jfree.chart.axis.ValueAxis;

import atomicJ.data.Channel1DCollection;
import atomicJ.data.Data1D;
import atomicJ.gui.Channel1DPlot;
import atomicJ.gui.Channel1DPlot.Channel1DPlotFactory;
import atomicJ.gui.PreferredScaleBarStyle;

public class PlotFactoryPhotometric implements Channel1DPlotFactory
{
    static final Preferences PHOTOMETRIC_CURVE_PREF = Preferences.userNodeForPackage(PlotFactoryPhotometric.class).node(PlotFactoryPhotometric.class.getName());

    private static final PreferredScaleBarStyle PREFERRED_DOMAIN_SCALE_BAR_STYLE = new PreferredScaleBarStyle(PHOTOMETRIC_CURVE_PREF.node("DomainScaleBar"));
    private static final PreferredScaleBarStyle PREFERRED_RANGE_SCALE_BAR_STYLE = new PreferredScaleBarStyle(PHOTOMETRIC_CURVE_PREF.node("RangeScaleBar"));

    private static final PlotFactoryPhotometric INSTANCE = new PlotFactoryPhotometric();

    private PlotFactoryPhotometric(){};

    public static PlotFactoryPhotometric getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Channel1DPlot getPlot(Data1D dataset) 
    {
        Channel1DPlot plot = new Channel1DPlot(dataset, PHOTOMETRIC_CURVE_PREF, PREFERRED_DOMAIN_SCALE_BAR_STYLE, PREFERRED_RANGE_SCALE_BAR_STYLE);
       
        int rangeAxisCount = plot.getRangeAxisCount();
        for(int i = 0; i<rangeAxisCount;i++)
        {
            ValueAxis axis = plot.getRangeAxis(i);
            if(axis != null)
            {
                axis.setRange(0,100);
            }
        }

        return plot;
    }
}
