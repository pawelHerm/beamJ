
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

package atomicJ.statistics;

import atomicJ.utilities.Selector;

public class ResidualVector 
{
    private final double[] residuals;

    public ResidualVector(double[] residuals)
    {
        this.residuals = residuals;
    }

    public double[] getResiduals()
    {
        return residuals;
    }

    public double getSquaresSum()
    {
        double norm = 0;
        double n = residuals.length;
        for (int i = 0; i < n; i++) 
        {
            double x = residuals[i];
            norm = norm + Math.pow(x,2);
        }
        return norm;
    }

    public double getAbsoluteVulesSum()
    {
        double norm = 0;
        int n = residuals.length;
        for (int i = 0; i < n; i++) 
        {
            double x = residuals[i];
            norm = norm + Math.pow(x,2);
        }
        return norm;
    }  
    public double getInfNorm()
    {
        double norm = 0;
        double n = residuals.length;
        for (int i = 0; i < n; i++) 
        {
            double x = Math.abs(residuals[i]);
            norm = Math.max(norm,x);
        }
        return norm;
    }


    public double getAbsoluteRankStatistic(int k)
    {
        int n = residuals.length;

        double[] absResiduals = new double[n];
        for(int i = 0;i<n;i++)
        {
            absResiduals[i] = Math.abs(residuals[i]);
        }
        Selector.sortSmallest(absResiduals, k);
        return absResiduals[k];
    }
}
