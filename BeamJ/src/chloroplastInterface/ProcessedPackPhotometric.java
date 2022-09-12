
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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;

import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.analysis.VisualizablePack;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.Validation;
import chloroplastInterface.NumericalProcessingResults.CalibratedSignalCurveMeasurement;
import chloroplastInterface.NumericalProcessingResults.SignalPhaseAnalysisResults;

public class ProcessedPackPhotometric implements Processed1DPack<ProcessedPackPhotometric, SimplePhotometricSource>
{	
    private final SimplePhotometricSource source;
    private final NumericalProcessingResults results;
    private final ProcessingSettings processingSettings;
    private IdentityTag batchId;

    public ProcessedPackPhotometric(SimplePhotometricSource source, NumericalProcessingResults results, ProcessingSettings settings, IdentityTag batchIdTag)
    {
        this.source = source;
        this.results = results;
        this.processingSettings = settings;
        this.batchId = batchIdTag;
    }

    public ProcessedPackPhotometric(ProcessedPackPhotometric that)
    {
        this.source = that.source.copy();
        this.results = that.results;
        this.processingSettings = that.processingSettings;
        this.batchId = null;
    }

    public ProcessedPackPhotometric copy()
    {
        return new ProcessedPackPhotometric(this);
    }

    public NumericalProcessingResults getResults()
    {
        return results;
    }

    public ProcessingSettings getProcessingSettings()
    {
        return processingSettings;
    }

    @Override
    public SimplePhotometricSource getSource()
    {
        return source;
    }

    @Override
    public void setBatchIdTag(IdentityTag batchId)
    {
        this.batchId = batchId;
    }

    @Override
    public IdentityTag getBatchIdTag()
    {
        return batchId;
    }

    @Override
    public String toString()
    {
        String result = source.getLongName();
        return result;
    }

    public ProcessingBatchMementoPhotometric getProcessingMemento()
    {
        return source.getProcessingMemento();
    }

    public VisualizablePack<SimplePhotometricSource, ProcessedResourcePhotometric> visualize(VisualizationSettingsPhotometric visSettings)
    {
        return StandardProcessor.visualize(this, visSettings);
    }

    @Override
    public List<? extends ProcessedPackFunction<ProcessedPackPhotometric>> getSpecialFunctions()
    {
        List<ProcessedPackFunction<ProcessedPackPhotometric>> specialFunctions = new ArrayList<>();
        List<SignalPhaseAnalysisResults> phaseResultsAll = results.getPhaseResults();

        for(SignalPhaseAnalysisResults phRes : phaseResultsAll)
        {
            int phaseIndex = phRes.getActinicBeamPhaseIndex();
            int amplitudeCount = phRes.getAmplitudeExtremaCount();
            int rateCount = phRes.getRateExtremaCount();

            for(int i = 0; i < amplitudeCount; i++)
            {
                specialFunctions.add(new ProcessedPackAmplitudeFunction(phaseIndex, i));
            }
            
            for(int i = 0; i < rateCount; i++)
            {
                specialFunctions.add(new ProcessedPackRateFunction(phaseIndex, i));
            }
        }

        return specialFunctions;
    }

    public static class ProcessedPackAmplitudeFunction implements ProcessedPackFunction<ProcessedPackPhotometric>
    {            
        private final int actinicBeamPhaseIndex;
        private final int amplitudeIndex;

        private final Quantity evaluatedQuantity;

        public ProcessedPackAmplitudeFunction(int actinicBeamPhaseIndex, int amplitudeIndex)
        {
            this.actinicBeamPhaseIndex = Validation.requireNonNegativeParameterName(actinicBeamPhaseIndex, "actinicBeamPhaseIndex");
            this.amplitudeIndex = Validation.requireNonNegativeParameterName(amplitudeIndex, "amplitudeIndex");

            //we add 1 to the phase and amplitude indices, because we show in GUI the indices that start from one
            //while actinicBeamPhaseIndex and amplitudeIndex are zero-based
            String quantityName = "Amplitude " + Integer.toString(amplitudeIndex + 1) + " for phase " + Integer.toString(actinicBeamPhaseIndex + 1);
            this.evaluatedQuantity = new UnitQuantity(quantityName, new SimplePrefixedUnit("%"));
        }

        @Override
        public double evaluate(ProcessedPackPhotometric pack) 
        {
            NumericalProcessingResults results = pack.getResults();
            SignalPhaseAnalysisResults phaseResults = results.getPhaseResults(actinicBeamPhaseIndex);
            
            if(amplitudeIndex < phaseResults.getAmplitudeExtremaCount())
            {
                CalibratedSignalCurveMeasurement amplitudeMeasurement = phaseResults.getAmplitudeExtremum(amplitudeIndex);

                return amplitudeMeasurement.getMeasuredValue();
            }

            return Double.NaN;
        }

        @Override
        public Quantity getEvaluatedQuantity()
        {
            return evaluatedQuantity;
        } 

        @Override
        public int hashCode()
        {
            int result = Integer.hashCode(this.actinicBeamPhaseIndex);
            result = 31*result + Integer.hashCode(this.amplitudeIndex);
            result = 31*result + Objects.hashCode(this.evaluatedQuantity);

            return result;
        }

        @Override
        public boolean equals(Object o)
        {
            if(o instanceof ProcessedPackAmplitudeFunction)
            {
                ProcessedPackAmplitudeFunction that = (ProcessedPackAmplitudeFunction)o;
                if(this.actinicBeamPhaseIndex != that.actinicBeamPhaseIndex)
                {
                    return false;
                }
                if(this.amplitudeIndex != that.amplitudeIndex)
                {
                    return false;
                }

                if(!Objects.equal(this.evaluatedQuantity, that.evaluatedQuantity))
                {
                    return false;
                }

                return true;
            }

            return false;
        }
    }
    
    public static class ProcessedPackRateFunction implements ProcessedPackFunction<ProcessedPackPhotometric>
    {            
        private final int actinicBeamPhaseIndex;
        private final int rateIndex;

        private final Quantity evaluatedQuantity;

        public ProcessedPackRateFunction(int actinicBeamPhaseIndex, int rateIndex)
        {
            this.actinicBeamPhaseIndex = Validation.requireNonNegativeParameterName(actinicBeamPhaseIndex, "actinicBeamPhaseIndex");
            this.rateIndex = Validation.requireNonNegativeParameterName(rateIndex, "rateIndex");

            //we add 1 to the phase and rate indices, because we show in GUI the indices that start from one
            //while actinicBeamPhaseIndex and rateIndex are zero-based
            String quantityName = "Rate " + Integer.toString(rateIndex + 1) + " for phase: " + Integer.toString(actinicBeamPhaseIndex + 1);
            this.evaluatedQuantity = new UnitQuantity(quantityName, new SimplePrefixedUnit("%"));
        }

        @Override
        public double evaluate(ProcessedPackPhotometric pack) 
        {
            NumericalProcessingResults results = pack.getResults();
            SignalPhaseAnalysisResults phaseResults = results.getPhaseResults(actinicBeamPhaseIndex);
            
            if(rateIndex < phaseResults.getRateExtremaCount())
            {
                CalibratedSignalCurveMeasurement amplitudeMeasurement = phaseResults.getRateExtremum(rateIndex);

                return amplitudeMeasurement.getMeasuredValue();
            }

            return Double.NaN;
        }

        @Override
        public Quantity getEvaluatedQuantity()
        {
            return evaluatedQuantity;
        } 

        @Override
        public int hashCode()
        {
            int result = Integer.hashCode(this.actinicBeamPhaseIndex);
            result = 31*result + Integer.hashCode(this.rateIndex);
            result = 31*result + Objects.hashCode(this.evaluatedQuantity);

            return result;
        }

        @Override
        public boolean equals(Object o)
        {
            if(o instanceof ProcessedPackAmplitudeFunction)
            {
                ProcessedPackAmplitudeFunction that = (ProcessedPackAmplitudeFunction)o;
                if(this.actinicBeamPhaseIndex != that.actinicBeamPhaseIndex)
                {
                    return false;
                }
                if(this.rateIndex != that.amplitudeIndex)
                {
                    return false;
                }

                if(!Objects.equal(this.evaluatedQuantity, that.evaluatedQuantity))
                {
                    return false;
                }

                return true;
            }

            return false;
        }
    }
}
