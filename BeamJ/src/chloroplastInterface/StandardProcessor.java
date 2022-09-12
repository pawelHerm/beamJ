
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Paweł Hermanowicz
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

import atomicJ.analysis.Processor;
import atomicJ.analysis.SortedArrayOrder;
import atomicJ.analysis.VisualizablePack;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.SortX1DTransformation;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.Channel1DStandard;
import atomicJ.data.IndexRange;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.OrderedIntegerPair;
import chloroplastInterface.NumericalProcessingResults.CalibratedSignalCurveMeasurement;
import chloroplastInterface.NumericalProcessingResults.SignalPhaseAnalysisResults;

public final class StandardProcessor implements Processor<ProcessablePackPhotometric, ProcessingResultPhotometric>
{
    private static final StandardProcessor INSTANCE = new StandardProcessor();

    private static final boolean ALWAYS_CALCULATE_TWO_EXTREMA = false;

    private StandardProcessor(){}

    public static StandardProcessor getInstance()
    {
        return INSTANCE;
    }

    @Override
    public ProcessingResultPhotometric process(ProcessablePackPhotometric processable)
    {        
        Channel1DData trimmedSortedAndSmoothedSignal = getTrimmedAndSmoothedSignalChannelData(processable.getProcessingSettings(), processable.getSourceToProcess());
        Channel1DData firstDerivativeOfSignal = getTrimmedSignalDerivativeChannelData(processable.getProcessingSettings(), processable.getSourceToProcess());

        SimplePhotometricSource sourceToProcess = processable.getSourceToProcess();
        List<ActinicPhaseSettingsImmutable> actinicPhases = sourceToProcess.getActinicBeamPhaseSettings();
        ActinicPhaseSettingsImmutable darkPhase = actinicPhases.get(0);
        double darkPhaseLengthInSeconds = darkPhase.getDuration(StandardTimeUnit.SECOND);

        int darkTransmittanceReferenceIndex = trimmedSortedAndSmoothedSignal.getIndexOfGreatestXSmallerOrEqualTo(darkPhaseLengthInSeconds);
        double darkTransmittanceLevel = trimmedSortedAndSmoothedSignal.getY(darkTransmittanceReferenceIndex);

        List<SignalPhaseAnalysisResults> analysisResults = new ArrayList<>();

        double currentPhaseOnsetTimeInSeconds = darkPhaseLengthInSeconds;
        for(int i = 1; i < actinicPhases.size(); i++)
        {
            ActinicPhaseSettingsImmutable currentPhase = actinicPhases.get(i);
            double currentPhaseDurationInSeconds = currentPhase.getDuration(StandardTimeUnit.SECOND);
            double currentPhaseEndInSeconds = currentPhaseOnsetTimeInSeconds + currentPhaseDurationInSeconds;

            IndexRange currentPhaseIndexRange = trimmedSortedAndSmoothedSignal.getIndexRangeBoundedBy(currentPhaseOnsetTimeInSeconds, currentPhaseEndInSeconds);
            IndexRange currentPhaseDerivativeChannelIndexRange = firstDerivativeOfSignal.getIndexRangeBoundedBy(currentPhaseOnsetTimeInSeconds, currentPhaseEndInSeconds);

            OrderedIntegerPair indicesOfExtrema = trimmedSortedAndSmoothedSignal.getIndicesOfYExtrema(currentPhaseIndexRange.getMinIndex(), currentPhaseIndexRange.getMaxIndex() + 1);

            int indexOfMinimum = indicesOfExtrema.getFirst();
            int indexOfMaximum = indicesOfExtrema.getSecond();

            int indexOfFirstExtremum = Math.min(indexOfMinimum, indexOfMaximum);

            double amplitudeMinimum = trimmedSortedAndSmoothedSignal.getY(indexOfMinimum) - darkTransmittanceLevel;
            double amplitudeMaximum = trimmedSortedAndSmoothedSignal.getY(indexOfMaximum) - darkTransmittanceLevel;
            double amplitudeFirstExtremum = trimmedSortedAndSmoothedSignal.getY(indexOfFirstExtremum) - darkTransmittanceLevel;

            double greaterAmplitudeInTermsOfModulus = Math.max(Math.abs(amplitudeMinimum), Math.abs(amplitudeMaximum));
            
            int indexOfGreaterAmplitudeInTermsOfModulus = Math.abs(amplitudeMinimum) > Math.abs(amplitudeMaximum) ? indexOfMinimum: indexOfMaximum;
            int indexOfFirstPronouncedExtremum = amplitudeFirstExtremum > 0.1*greaterAmplitudeInTermsOfModulus ? indexOfFirstExtremum : indexOfGreaterAmplitudeInTermsOfModulus;

            double[] yCoordinatesAfterFirstPronouncedExtremum = 
                    trimmedSortedAndSmoothedSignal.getYCoordinates(indexOfFirstPronouncedExtremum, currentPhaseIndexRange.getMaxIndex());

            int indexOfSecondAmplitudePoint = indexOfFirstPronouncedExtremum + 
                    ArrayUtilities.getIndexOfValueMostDistantFrom(yCoordinatesAfterFirstPronouncedExtremum, trimmedSortedAndSmoothedSignal.getY(indexOfFirstPronouncedExtremum));

            List<CalibratedSignalCurveMeasurement> amplitudeMeasurements = new ArrayList<>();
            List<CalibratedSignalCurveMeasurement> rateMeasurements = new ArrayList<>();

            CalibratedSignalCurveMeasurement firstAmplitudeMeasurement = buildAmplitudeMeasurement(trimmedSortedAndSmoothedSignal, i, currentPhaseOnsetTimeInSeconds, indexOfFirstPronouncedExtremum, darkTransmittanceLevel);
            amplitudeMeasurements.add(firstAmplitudeMeasurement);

            int firstRateExtremumIndex = firstAmplitudeMeasurement.getMeasuredValue() > 0 ? firstDerivativeOfSignal.getYMaximumIndex(currentPhaseDerivativeChannelIndexRange.getMinIndex(),firstDerivativeOfSignal.getIndexOfGreatestXSmallerOrEqualTo(trimmedSortedAndSmoothedSignal.getX(indexOfFirstPronouncedExtremum))): firstDerivativeOfSignal.getYMinimumIndex(currentPhaseDerivativeChannelIndexRange.getMinIndex(),firstDerivativeOfSignal.getIndexOfGreatestXSmallerOrEqualTo(trimmedSortedAndSmoothedSignal.getX(indexOfFirstPronouncedExtremum)));

            CalibratedSignalCurveMeasurement firstRateMeasurement = buildRateMeasurement(trimmedSortedAndSmoothedSignal, firstDerivativeOfSignal, i, currentPhaseOnsetTimeInSeconds, firstRateExtremumIndex);
            rateMeasurements.add(firstRateMeasurement);

            double secondExtremumWithRespectToFirst = Math.abs(trimmedSortedAndSmoothedSignal.getY(indexOfSecondAmplitudePoint) - trimmedSortedAndSmoothedSignal.getY(indexOfFirstPronouncedExtremum));
            boolean addSecondExtremum = ALWAYS_CALCULATE_TWO_EXTREMA || secondExtremumWithRespectToFirst >= 0.05*greaterAmplitudeInTermsOfModulus;

            if(addSecondExtremum)
            {
                CalibratedSignalCurveMeasurement secondAmplitudeMeasurement = buildAmplitudeMeasurement(trimmedSortedAndSmoothedSignal, i, currentPhaseOnsetTimeInSeconds, indexOfSecondAmplitudePoint, darkTransmittanceLevel);
                amplitudeMeasurements.add(secondAmplitudeMeasurement);


                int secondRateExtremumIndex = secondAmplitudeMeasurement.getMeasuredValue() > 0 ?
                        firstDerivativeOfSignal.getYMaximumIndex(firstDerivativeOfSignal.getIndexOfGreatestXSmallerOrEqualTo(trimmedSortedAndSmoothedSignal.getX(indexOfFirstPronouncedExtremum)), currentPhaseDerivativeChannelIndexRange.getMaxIndex())
                        : firstDerivativeOfSignal.getYMinimumIndex(firstDerivativeOfSignal.getIndexOfGreatestXSmallerOrEqualTo(trimmedSortedAndSmoothedSignal.getX(indexOfFirstPronouncedExtremum)), currentPhaseDerivativeChannelIndexRange.getMaxIndex());          


                        CalibratedSignalCurveMeasurement secondRateMeasurement = buildRateMeasurement(trimmedSortedAndSmoothedSignal, firstDerivativeOfSignal, i, currentPhaseOnsetTimeInSeconds, secondRateExtremumIndex);
                        rateMeasurements.add(secondRateMeasurement);
            }
            
            SignalPhaseAnalysisResults phaseAnalysisResults = new SignalPhaseAnalysisResults(i, amplitudeMeasurements, rateMeasurements);
            analysisResults.add(phaseAnalysisResults);

            currentPhaseOnsetTimeInSeconds = currentPhaseEndInSeconds;
        }

        NumericalProcessingResults numericalResults = new NumericalProcessingResults(analysisResults);
        ProcessedPackPhotometric processedPack = new ProcessedPackPhotometric(sourceToProcess, numericalResults, processable.getProcessingSettings(), processable.getBatchIdentityTag());

        VisualizationSettingsPhotometric visSettings = processable.getVisualizationSettings();
        VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric> visualizablePack = visualize(processedPack, trimmedSortedAndSmoothedSignal, firstDerivativeOfSignal, visSettings);

        ProcessingResultPhotometric results = new ProcessingResultPhotometric(processedPack, visualizablePack);

        return results;
    }  

    private CalibratedSignalCurveMeasurement buildAmplitudeMeasurement(Channel1DData signalChannel, int phaseIndex, double currentPhaseOnsetTimeInSeconds, int indexOfExtremum, double darkTransmittance)
    {    
        double transmittance = signalChannel.getY(indexOfExtremum);
        double amplitude = transmittance - darkTransmittance;
        double measurementTimeInSeconds = signalChannel.getX(indexOfExtremum);
        double measurementTimeFromPhaseOnsetInSeconds = measurementTimeInSeconds - currentPhaseOnsetTimeInSeconds;
        
        CalibratedSignalCurveMeasurement amplitudeMeasurement = 
                new CalibratedSignalCurveMeasurement(phaseIndex, amplitude, transmittance, measurementTimeFromPhaseOnsetInSeconds, measurementTimeInSeconds);

        return amplitudeMeasurement;
    }

    private CalibratedSignalCurveMeasurement buildRateMeasurement(Channel1DData signalChannel, Channel1DData rateChannel, int phaseIndex, double currentPhaseOnsetTimeInSeconds, int indexOfExtremum)
    {    
        double rate = rateChannel.getY(indexOfExtremum);
        double measurementTimeInSeconds = rateChannel.getX(indexOfExtremum);
        double measurementTimeFromPhaseOnsetInSeconds = measurementTimeInSeconds - currentPhaseOnsetTimeInSeconds;

        double transmittance = signalChannel.getY(signalChannel.getIndexWithinDataBoundsOfItemWithXClosestTo(measurementTimeInSeconds));

        CalibratedSignalCurveMeasurement amplitudeMeasurement = 
                new CalibratedSignalCurveMeasurement(phaseIndex, rate, transmittance, measurementTimeFromPhaseOnsetInSeconds, measurementTimeInSeconds);

        return amplitudeMeasurement;
    }


    public static Channel1DData getTrimmedAndSmoothedSignalChannelData(ProcessingSettings processSettings, SimplePhotometricSource sourceToProcess)
    {
        int signalIndex = processSettings.getSignalIndex();
        Channel1D signalChannel = sourceToProcess.getRecordedChannel(signalIndex);
        Channel1DDataTransformation trimmer = processSettings.getTrimmer();

        Channel1DData trimmedSignal = trimmer.transform(signalChannel.getChannelData());

        Channel1DDataTransformation ascendingSorter = new SortX1DTransformation(SortedArrayOrder.ASCENDING);
        Channel1DData trimmedAndSortedSignal = ascendingSorter.transform(trimmedSignal);                

        Channel1DDataTransformation smoother = processSettings.getSmoother();

        Channel1DData trimmedSortedAndSmoothedSignal = smoother.transform(trimmedAndSortedSignal);

        return trimmedSortedAndSmoothedSignal;
    }

    public static Channel1DData getTrimmedSignalDerivativeChannelData(ProcessingSettings processSettings, SimplePhotometricSource sourceToProcess)
    {
        int signalIndex = processSettings.getSignalIndex();

        Channel1D transmittanceChannel = sourceToProcess.getRecordedChannel(signalIndex);
        Channel1DDataTransformation trimmer = processSettings.getTrimmer();

        Channel1DData trimmedTransmittance = trimmer.transform(transmittanceChannel.getChannelData());

        Channel1DDataTransformation derivativeTransformation = processSettings.getDerivativeTransformation();

        Channel1DData firstDerivativeChannel = derivativeTransformation.transform(trimmedTransmittance);

        return firstDerivativeChannel;
    }

    public static VisualizablePack<SimplePhotometricSource, ProcessedResourcePhotometric> visualize(ProcessedPackPhotometric processedPack, Channel1DData signalChannelTrimmedSmoothedData, Channel1DData signalDerivativeData, VisualizationSettingsPhotometric visSettings)
    {                
        VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric> visualizable = null;

        if(visSettings.areResultsToBeVisualized())
        {
            Channel1D signalChannelTrimmedSmoothed = visSettings.isPlotRecordedCurve() ? new Channel1DStandard(signalChannelTrimmedSmoothedData, RecordingModel.TRANSMITTANCE_CHANNEL_KEY) : null;
            Channel1D signalDerivative = visSettings.isPlotDerivativeCurve() ? new Channel1DStandard(signalDerivativeData, RecordingModel.TRANSMITTANCE_DERIVATIVE_CHANNEL_KEY) : null ;

            visualizable = new VisualizablePhotometricPack(processedPack, signalChannelTrimmedSmoothed, signalDerivative, visSettings);
        }    

        return visualizable;
    }

    public static VisualizablePack<SimplePhotometricSource, ProcessedResourcePhotometric> visualize(ProcessedPackPhotometric processedPack, VisualizationSettingsPhotometric visSettings)
    {           
        Channel1DData trimmedSortedAndSmoothedSignalData = getTrimmedAndSmoothedSignalChannelData(processedPack.getProcessingSettings(), processedPack.getSource());
        Channel1DData firstDerivativeOfSignalData = getTrimmedSignalDerivativeChannelData(processedPack.getProcessingSettings(), processedPack.getSource());

        VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric> visualizable = null;

        if(visSettings.areResultsToBeVisualized())
        {
            Channel1D signalChannelTrimmedSmoothed = visSettings.isPlotRecordedCurve() ? new Channel1DStandard(trimmedSortedAndSmoothedSignalData, RecordingModel.TRANSMITTANCE_CHANNEL_KEY) : null;
            Channel1D signalDerivative = visSettings.isPlotDerivativeCurve() ? new Channel1DStandard(firstDerivativeOfSignalData, RecordingModel.TRANSMITTANCE_DERIVATIVE_CHANNEL_KEY) : null ;

            visualizable = new VisualizablePhotometricPack(processedPack, signalChannelTrimmedSmoothed, signalDerivative, visSettings);
        }    

        return visualizable;
    }
}
