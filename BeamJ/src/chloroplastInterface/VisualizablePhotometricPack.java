
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package chloroplastInterface;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import atomicJ.analysis.VisualizablePack;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DCollection;
import atomicJ.data.Channel1DData;
import atomicJ.gui.Channel1DChart;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.Channel1DPlot;
import atomicJ.utilities.MultiMap;

public class VisualizablePhotometricPack implements VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric>
{   
    private final ProcessedPackPhotometric pack;

    private final Channel1D transmittance;
    private final Channel1D transmittanceDerivative;

    private final VisualizationSettingsPhotometric visSettings;

    public VisualizablePhotometricPack(ProcessedPackPhotometric pack, Channel1D transmittance, Channel1D transmittanceDerivative, VisualizationSettingsPhotometric visSettings)
    {
        this.pack = pack;
        this.transmittance = transmittance;
        this.transmittanceDerivative = transmittanceDerivative;
        this.visSettings = visSettings;
    }

    @Override
    public SimplePhotometricSource getSource()
    {
        return pack.getSource();
    }

    public ProcessedPackPhotometric getProcessedPack()
    {
        return pack;
    }

    public Channel1DData getTransmittanceData()
    {
        return transmittance.getChannelData();
    }

    public Channel1DData getTransmittanceDerivativeData()
    {
        return transmittanceDerivative.getChannelData();
    }   

    public VisualizationSettingsPhotometric getVisualizationSettings()
    {
        return visSettings;
    }

    public Channel1DChart<?> getTransmittanceCurveChart()
    {
        Channel1DChart<?> chart = null;
        if(visSettings.isPlotRecordedCurve())
        {
            Channel1DCollection transmittanceChannelCollection = 
                    new Channel1DCollection(Collections.singletonMap(transmittance.getIdentifier(), transmittance), 
                            RecordingModel.TRANSMITTANCE_CHANNEL_KEY, RecordingModel.TRANSMITTANCE_CHANNEL_KEY);
            Channel1DPlot plot = PlotFactoryPhotometric.getInstance().getPlot(transmittanceChannelCollection);
            chart = new Channel1DChart<>(plot, PhotometricSourceVisualization.TRANSMITTANCE_CURVE_PLOT);
            chart.setAutomaticTitles(pack.getSource().getAutomaticChartTitles());
        }

        return chart;                                               
    }

    public Channel1DChart<?> getTransmittanceDerivativeChart()
    {
        Channel1DChart<?> chart = null;
        if(visSettings.isPlotDerivativeCurve())
        {
            Channel1DCollection transmittanceDerivativeChannelCollection = 
                    new Channel1DCollection(Collections.singletonMap(transmittanceDerivative.getIdentifier(), transmittanceDerivative), 
                            RecordingModel.TRANSMITTANCE_DERIVATIVE_CHANNEL_KEY, RecordingModel.TRANSMITTANCE_DERIVATIVE_CHANNEL_KEY);

            Channel1DPlot plot = PlotFactoryPhotometric.getInstance().getPlot(transmittanceDerivativeChannelCollection);
            chart = new Channel1DChart<>(plot, PhotometricSourceVisualization.TRANSMITTANCE_CURVE_PLOT);
            chart.setAutomaticTitles(pack.getSource().getAutomaticChartTitles());
        }

        return chart;       
    }

    @Override
    public Map<String, Collection<? extends Channel1D>> getChannels()
    {
        Map<String, Collection<? extends Channel1D>> charts = new LinkedHashMap<>();

        charts.put(RecordingModel.TRANSMITTANCE_CHANNEL_KEY, Collections.singletonList(transmittance));  
        charts.put(RecordingModel.TRANSMITTANCE_DERIVATIVE_CHANNEL_KEY, Collections.singletonList(transmittanceDerivative));

        return charts;
    }

    @Override
    public Map<String, ChannelChart<?>> visualize()
    {
        Map<String, ChannelChart<?>> charts = new LinkedHashMap<>();

        ChannelChart<?> transmittanceCurveChart = getTransmittanceCurveChart();  
        ChannelChart<?> derivativeCurveChart = getTransmittanceDerivativeChart();

        charts.put(RecordingModel.TRANSMITTANCE_CHANNEL_KEY, transmittanceCurveChart);  
        charts.put(RecordingModel.TRANSMITTANCE_DERIVATIVE_CHANNEL_KEY, derivativeCurveChart);

        return charts;
    }

    @Override
    public ProcessedResourcePhotometric buildChannelResource() 
    {
        return new ProcessedResourcePhotometric(pack, new MultiMap<>(getChannels()));
    }
}
