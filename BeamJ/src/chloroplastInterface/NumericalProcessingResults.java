
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
import java.util.Collections;
import java.util.List;

import atomicJ.utilities.Validation;

public final class NumericalProcessingResults 
{
    private final List<SignalPhaseAnalysisResults> phaseResults;

    public NumericalProcessingResults(List<SignalPhaseAnalysisResults> phaseResults)
    {
        this.phaseResults = Collections.unmodifiableList(new ArrayList<>(phaseResults));
    }

    public List<SignalPhaseAnalysisResults> getPhaseResults()
    {
        return phaseResults;
    }

    public SignalPhaseAnalysisResults getPhaseResults(int phaseIndex)
    {
        for(SignalPhaseAnalysisResults phRes: phaseResults)
        {
            if(phRes.phaseIndex == phaseIndex)
            {
                return phRes;
            }
        }
        return SignalPhaseAnalysisResults.getNullInstance(phaseIndex);
    }

    public static class SignalPhaseAnalysisResults
    {
        private final int phaseIndex;

        private final List<CalibratedSignalCurveMeasurement> amplitudeExtrema;
        private final List<CalibratedSignalCurveMeasurement> rateExtrema;

        private SignalPhaseAnalysisResults(int phaseIndex)
        {
            this.phaseIndex = Validation.requireNonNegative(phaseIndex, "phaseIndex should be a non-negative integer");;
            this.amplitudeExtrema = Collections.emptyList();
            this.rateExtrema = Collections.emptyList();
        }

        public SignalPhaseAnalysisResults(int phaseIndex, CalibratedSignalCurveMeasurement amplitudeExtremum, CalibratedSignalCurveMeasurement rateExtremum)
        {
            this.phaseIndex = Validation.requireNonNegative(phaseIndex, "phaseIndex should be a non-negative integer");
            this.amplitudeExtrema = Collections.singletonList(amplitudeExtremum);
            this.rateExtrema = Collections.singletonList(rateExtremum);
        }

        public SignalPhaseAnalysisResults(int phaseIndex, List<CalibratedSignalCurveMeasurement> amplitudeExtrema, List<CalibratedSignalCurveMeasurement> rateExtrema)
        {
            this.phaseIndex = Validation.requireNonNegative(phaseIndex, "phaseIndex should be a non-negative integer");
            this.amplitudeExtrema = Collections.unmodifiableList(new ArrayList<>(amplitudeExtrema));
            this.rateExtrema = Collections.unmodifiableList(new ArrayList<>(rateExtrema));
        }

        public static SignalPhaseAnalysisResults getNullInstance(int phaseIndex)
        {
            return new SignalPhaseAnalysisResults(phaseIndex);
        }

        public int getActinicBeamPhaseIndex()
        {
            return phaseIndex;
        }

        public int getAmplitudeExtremaCount()
        {
            return amplitudeExtrema.size();
        }

        public List<CalibratedSignalCurveMeasurement> getAmplitudeExtrema()
        {
            return amplitudeExtrema;
        }

        public CalibratedSignalCurveMeasurement getAmplitudeExtremum(int extremumIndex)
        {
            return amplitudeExtrema.get(extremumIndex);
        }

        public List<CalibratedSignalCurveMeasurement> getAvoidanceAmplitudeExtrema()
        {
            List<CalibratedSignalCurveMeasurement> positiveExtrema = new ArrayList<>();

            for(CalibratedSignalCurveMeasurement extremum : this.amplitudeExtrema)
            {
                if(extremum.getMeasuredValue() > 0)
                {
                    positiveExtrema.add(extremum);
                }
            }

            return positiveExtrema;
        }

        public List<CalibratedSignalCurveMeasurement> getAccumulationAmplitudeExtrema()
        {
            List<CalibratedSignalCurveMeasurement> negativeExtrema = new ArrayList<>();

            for(CalibratedSignalCurveMeasurement extremum : this.amplitudeExtrema)
            {
                if(extremum.getMeasuredValue() < 0)
                {
                    negativeExtrema.add(extremum);
                }
            }

            return negativeExtrema;
        }

        public int getRateExtremaCount()
        {
            return rateExtrema.size();
        }
        
        public List<CalibratedSignalCurveMeasurement> getRateExtrema()
        {
            return rateExtrema;
        }       

        public CalibratedSignalCurveMeasurement getRateExtremum(int extremumIndex)
        {
            return rateExtrema.get(extremumIndex);
        }


        public List<CalibratedSignalCurveMeasurement> getAvoidanceRateExtrema()
        {
            List<CalibratedSignalCurveMeasurement> positiveExtrema = new ArrayList<>();

            for(CalibratedSignalCurveMeasurement extremum : this.rateExtrema)
            {
                if(extremum.getMeasuredValue() > 0)
                {
                    positiveExtrema.add(extremum);
                }
            }

            return positiveExtrema;
        }

        public List<CalibratedSignalCurveMeasurement> getAccumulationRateExtrema()
        {
            List<CalibratedSignalCurveMeasurement> negativeExtrema = new ArrayList<>();

            for(CalibratedSignalCurveMeasurement extremum : this.rateExtrema)
            {
                if(extremum.getMeasuredValue() < 0)
                {
                    negativeExtrema.add(extremum);
                }
            }

            return negativeExtrema;
        }
    }

    public static class CalibratedSignalCurveMeasurement
    {
        private final int actinicBeamPhaseIndex;

        private final double measuredValue;

        private final double transmittanceValueInPercents;      
        private final double timeFromPhaseOnset;
        private final double timeFromExperimentOnset;

        public CalibratedSignalCurveMeasurement(int actinicBeamPhaseIndex, double measuredValue, double transmittanceValueInPercents, double timeFromPhaseOnsetInSeconds, double timeFromExperimentOnset)
        {
            this.actinicBeamPhaseIndex = actinicBeamPhaseIndex;

            this.measuredValue = measuredValue;
            this.transmittanceValueInPercents = transmittanceValueInPercents;

            this.timeFromPhaseOnset = timeFromPhaseOnsetInSeconds;
            this.timeFromExperimentOnset = timeFromExperimentOnset;
        }

        public int getActinicBeamPhaseIndex()
        {
            return actinicBeamPhaseIndex;
        }

        public double getMeasuredValue()
        {
            return measuredValue;
        }

        public double getTransmittanceValueInPercents()
        {
            return transmittanceValueInPercents;
        }

        public double getTimeFromPhaseOnsetInSeconds()
        {
            return timeFromPhaseOnset;
        }

        public double getTimeFromExperimentOnsetInSeconds()
        {
            return timeFromExperimentOnset;
        }
    }
}
