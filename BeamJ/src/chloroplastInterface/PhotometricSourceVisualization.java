package chloroplastInterface;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import org.jfree.ui.Layer;

import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DCollection;
import atomicJ.gui.Channel1DChart;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.CustomizableValueMarker;
import atomicJ.gui.CustomizableXYPlot;
import atomicJ.gui.Channel1DPlot;
import atomicJ.gui.PreferredMarkerStyle;

public class PhotometricSourceVisualization 
{
    public static final String TRANSMITTANCE_CURVE_PLOT = "transmittanceCurvePlot";

    private static final String PHOTOMETRIC_CURVE_KEY = "PhotometricCurveKey";
    private static final Preferences PHOTOMETRIC_CURVE_PREF = Preferences.userNodeForPackage(PhotometricSourceVisualization.class).node(PhotometricSourceVisualization.class.getName());

    public static Map<String, ChannelChart<?>> getCharts(SimplePhotometricSource source)
    {     
        List<? extends Channel1D> channels = source.getChannels();
        Map<Object, Channel1D> channelMap = new LinkedHashMap<>();
        for(Channel1D ch: channels)
        {
            channelMap.put(ch.getIdentifier(), ch);
        }

        Channel1DCollection channelDataset = new Channel1DCollection(channelMap, PHOTOMETRIC_CURVE_KEY,PHOTOMETRIC_CURVE_KEY);

        Channel1DPlot plot = PlotFactoryPhotometric.getInstance().getPlot(channelDataset);
        plot.getDomainAxis().setRange(0, source.getTotalDurationOfActinicPhasesInSeconds());

        Channel1DChart<CustomizableXYPlot> chart = new Channel1DChart<>(plot, TRANSMITTANCE_CURVE_PLOT);

        Map<String,ChannelChart<?>> charts = new LinkedHashMap<>();
        charts.put(PhotometricResource.RECORDED_CURVE, chart);

        List<CustomizableValueMarker> phaseMarkers = createMarkers(source.getActinicBeamPhaseSettings(), PHOTOMETRIC_CURVE_PREF);

        for(CustomizableValueMarker marker : phaseMarkers)
        {
            plot.addDomainMarker(marker, Layer.FOREGROUND);
        }

        return charts;
    }

    public static List<CustomizableValueMarker> createMarkers(List<ActinicPhaseSettingsImmutable> initialRecordingPhases, Preferences plotStylePreferences)
    {
        List<CustomizableValueMarker> phaseMarkers = new ArrayList<>();

        double previousPhaseEndInMiliseconds = 0;

        for(int i = 0; i < initialRecordingPhases.size(); i++)
        {
            ActinicPhaseSettingsImmutable settings = initialRecordingPhases.get(i);
            double currentPhaseEndInMiliseconds = previousPhaseEndInMiliseconds + settings.getDurationInMiliseconds();

            String key = "Marker " + Integer.valueOf(i + 1);
            double value = currentPhaseEndInMiliseconds/1000.;

            PreferredMarkerStyle markerStyle = PreferredMarkerStyle.getInstance(plotStylePreferences.node(key), Color.BLUE, PreferredMarkerStyle.SOLID_STROKE, 1.f);
            CustomizableValueMarker marker = new CustomizableValueMarker(key, value, markerStyle, key);
            phaseMarkers.add(marker);

            previousPhaseEndInMiliseconds = currentPhaseEndInMiliseconds;
        }

        return phaseMarkers;
    }
}
