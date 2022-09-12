
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

import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.VisualizablePack;
import atomicJ.gui.curveProcessing.CurveVisualizationHandle;
import atomicJ.gui.curveProcessing.NumericalResultsHandler;
import atomicJ.sources.Channel1DSource;


public interface ProcessingOrigin <S extends Channel1DSource<?>, E extends Processed1DPack<E,S>>
{	
    public void startProcessing();	
    public void startProcessing(int batchIndex);
    public void startProcessing(List<S> sources, int batchIndex);
    public void startProcessing(List<ProcessingBatchModel> batches);
    public void startProcessing(List<ProcessingBatchModel> batches,
            CurveVisualizationHandle<VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric>> curveVisualizationHandle, NumericalResultsHandler<E> numericalResultsHandle);
}
