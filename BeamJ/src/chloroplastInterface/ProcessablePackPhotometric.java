
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

import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.Validation;

public final class ProcessablePackPhotometric 
{
    private final SimplePhotometricSource sourceToProcess;
    private final ProcessingSettings processSettings;
    private final VisualizationSettingsPhotometric visualizationSettings;

    private final IdentityTag batch;

    public ProcessablePackPhotometric(SimplePhotometricSource sourcesToProcess, ProcessingSettings settings, VisualizationSettingsPhotometric visSettings, IdentityTag batch)
    {
        this.sourceToProcess = sourcesToProcess;
        this.processSettings = settings;
        this.visualizationSettings = visSettings;
        this.batch = batch;
    }

    public Channel1DData getSignalChannelData(int signalIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, sourceToProcess.getRecordedSignalCount() - 1, "signalIndex");

        Channel1D signalChannel = sourceToProcess.getRecordedChannel(signalIndex);
        Channel1DData signalChannelData = signalChannel.getChannelData();

        return signalChannelData;
    }

    public SimplePhotometricSource getSourceToProcess()
    {
        return sourceToProcess;
    }

    public ProcessingSettings getProcessingSettings()
    {
        return processSettings;
    }

    public VisualizationSettingsPhotometric getVisualizationSettings()
    {
        return visualizationSettings;
    }

    public IdentityTag getBatchIdentityTag()
    {
        return batch;
    }
}
