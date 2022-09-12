
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


import java.awt.Window;

import atomicJ.analysis.ResultDestinationBasic;
import atomicJ.analysis.VisualizablePack;
import atomicJ.gui.Channel1DResultsView;
import atomicJ.gui.curveProcessing.CurveVisualizationHandle;
import atomicJ.gui.curveProcessing.NumericalResultsHandler;


public interface ResultDestinationPhotometric extends ResultDestinationBasic<SimplePhotometricSource, ProcessedPackPhotometric>
{	
    @Override
    public Window getPublicationSite(); 

    public Channel1DResultsView<ProcessedResourcePhotometric,SimplePhotometricSource> getGraphicalResultsDialog();

    public CurveVisualizationHandle<VisualizablePack<SimplePhotometricSource,ProcessedResourcePhotometric>> getDefaultCurveVisualizationHandle();
    @Override
    public NumericalResultsHandler<ProcessedPackPhotometric> getDefaultNumericalResultsHandler();
}
