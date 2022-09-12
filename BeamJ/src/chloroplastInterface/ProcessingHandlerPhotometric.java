
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

import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

import atomicJ.analysis.Batch;
import atomicJ.analysis.ProcessingResultsHandler;
import atomicJ.analysis.VisualizablePack;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.curveProcessing.CurveVisualizationHandle;
import atomicJ.gui.curveProcessing.NumericalResultsHandler;
import atomicJ.sources.IdentityTag;


public class ProcessingHandlerPhotometric implements ProcessingResultsHandler<ProcessingResultPhotometric>
{   
    private final List<VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric>> allVisualizablePacks = new ArrayList<>();
    private final ResultDestinationPhotometric  destination;

    private final Map<IdentityTag, Batch> processedBatches = new LinkedHashMap<>(); 

    private final CurveVisualizationHandle<VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric>> curveVisualizationHandle;
    private final NumericalResultsHandler resultHandle;

    public ProcessingHandlerPhotometric(ResultDestinationPhotometric  destination, CurveVisualizationHandle<VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric>> curveVisualizationHandle, NumericalResultsHandler resultHandle)
    {
        this.destination = destination; 
        this.curveVisualizationHandle = curveVisualizationHandle;
        this.resultHandle = resultHandle;
    }

    @Override
    public Component getAssociatedComponent()
    {
        return destination.getPublicationSite();
    }

    @Override
    public void acceptAndSegregateResults(List<ProcessingResultPhotometric> results)
    {       
        for(ProcessingResultPhotometric result: results)
        {
            ProcessedPackPhotometric processedPack = result.getProcessedPack();          
            IdentityTag tag = processedPack.getBatchIdTag();

            Batch batch = processedBatches.get(tag);
            if(batch == null)
            {
                batch = new Batch(tag.getLabel(), (int)tag.getKey());
                processedBatches.put(tag, batch);
            }

            batch.addProcessedPack(processedPack);

            VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric> visualized = result.getVisualizablePack();
            if(visualized != null)
            {
                allVisualizablePacks.add(visualized);
            }
        }

    }

    @Override
    public void sendResultsToDestination()
    {        
        destination.getResultBatchesCoordinator().countNewBatches(processedBatches.keySet());

        resultHandle.handlePublicationRequest(processedBatches.values());
        curveVisualizationHandle.handlePublicationRequest(allVisualizablePacks);
    }

    @Override
    public void reactToFailures(int failuresCount)
    {
        if(failuresCount > 0)
        {
            JOptionPane.showMessageDialog(destination.getPublicationSite(), "Errors occured during processing of " + failuresCount + " source files", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
        }  
    }

    @Override
    public void reactToCancellation()
    {
        destination.withdrawPublication();
    }

    @Override
    public void endProcessing()
    {
        destination.endProcessing();
    }
}
