
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

package atomicJ.data;

import java.util.ArrayList;
import java.util.List;

public class PointwiseModulusCurve implements Data1D
{	
    private static final String PLOT_IDENTIFIER = "PointwiseModulusPlot";

    private final Channel1D pointwiseModulus;	
    private final Channel1D transitionIndentationPoint;
    private final Channel1D fit;

    public PointwiseModulusCurve(Channel1D pointwiseModulus, Channel1D transitionIndentationPoint)
    {
        this(pointwiseModulus, transitionIndentationPoint, null);
    }

    public PointwiseModulusCurve(Channel1D pointwiseModulus, Channel1D transitionIndentationPoint, Channel1D fit)
    {        
        this.pointwiseModulus = pointwiseModulus;
        this.transitionIndentationPoint = transitionIndentationPoint;
        this.fit = fit;
    }

    @Override
    public List<Channel1D> getChannels()
    {
        List<Channel1D> curves = new ArrayList<>();
        curves.add(pointwiseModulus);

        if(fit != null)
        {
            curves.add(fit);
        }
        curves.add(transitionIndentationPoint);

        return curves;
    }

    @Override
    public String getIdentifier() 
    {
        return Datasets.POINTWISE_MODULUS_COMPOSITE;
    }

    @Override
    public String getName() 
    {
        return Datasets.POINTWISE_MODULUS;
    }

    public String getPlotIdentfier() 
    {
        return PLOT_IDENTIFIER;
    }
}
