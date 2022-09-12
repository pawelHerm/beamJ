
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

import java.util.List;

import atomicJ.data.Channel1D;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.sources.Channel1DSource;
import chloroplastInterface.ExperimentDescriptionModel.PhotometricDescriptionImmutable;

public interface SimplePhotometricSource extends Channel1DSource<Channel1D>
{
    @Override
    public SimplePhotometricSource copy();

    public LightSignalType getSignalType(int signalIndex);
    public boolean isCalibrated(int signalIndex);
    public boolean isCalibrationKnown(int signalIndex);
    public CalibrationSettingsImmutable getCalibrationSettings(int signalIndex);
    public SignalSamplingSettingsImmutable getSignalSettings(int signalIndex);

    public int getRecordedSignalCount();

    public List<ActinicPhaseSettingsImmutable> getActinicBeamPhaseSettings();
    public MeasuringBeamSettingsImmutable getMeasuringBeamSettings();
    public double getTotalDurationOfActinicPhasesInSeconds();

    public PrefixedUnit getSingleDataUnit();
    public Channel1D getRecordedChannel(int singleIndex);

    public PhotometricDescriptionImmutable getDescription();

    public ProcessingBatchMementoPhotometric getProcessingMemento();
    public void setProcessingMemento(ProcessingBatchMementoPhotometric memento);
}
