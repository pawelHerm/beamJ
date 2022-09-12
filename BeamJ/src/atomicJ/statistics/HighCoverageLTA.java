
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

import java.util.Arrays;

import atomicJ.functions.ParametrizedUnivariateFunction;
import atomicJ.utilities.MathUtilities;


public class HighCoverageLTA implements HighBreakdownEstimator
{
    private final LTA finalLTA;
    private final double robustMedian;

    public static HighCoverageLTA findFit(double[][] data, int deg, boolean constant,double k, int nstarts)
    {
        int n = data.length;
        int p = deg + MathUtilities.boole(constant);	
        double h1 = MathUtilities.minimalCoverage(n, p);		

        return findFit(data, deg, constant, k, h1, nstarts);
    }

    public static HighCoverageLTA findFit(double[][] data, int deg, boolean constant,double k, double h1, int nstarts)
    {
        LinearRegressionEsimator firstReg = LTA.findFit(data, deg, constant, h1, nstarts);
        ResidualVector residuals = firstReg.getResiduals();

        double[] residualsArray = residuals.getResiduals();
        int n = residualsArray.length;
        int p = deg + MathUtilities.boole(constant);
        int c = (int)Math.min(h1*n, n);	

        double[] absResiduals = new double[n];
        for(int i = 0;i<n;i++)
        {
            absResiduals[i] = Math.abs(residualsArray[i]);
        }
        Arrays.sort(absResiduals);
        double limit = k*absResiduals[c - 1];
        double count = 0;
        for(int i = 0;i<n;i++)
        {
            if(absResiduals[i]<=limit)
            {
                count++;
            }
            else{break;}
        }

        double h2 = count/n;
        int medianIndex = MathUtilities.robustMedianIndex(n, p);

        LTA finalLTA = LTA.findFit(data, deg, constant, h2, nstarts);
        double median = absResiduals[medianIndex];

        return new HighCoverageLTA(finalLTA, median);
    }

    public static HighCoverageLTA findFit(double[][] data, double[] model, double k, int nstarts)
    {
        int n = data.length;
        int p = model.length;	
        double h1 = MathUtilities.minimalCoverage(n, p);		

        return findFit(data, model, k, h1, nstarts);
    }

    public static HighCoverageLTA findFit(double[][] data, double[] model, double k, double h1, int nstarts)
    {		
        LinearRegressionEsimator firstReg = LTA.findFit(data, model, nstarts);
        ResidualVector residuals = firstReg.getResiduals();

        double[] residualsArray = residuals.getResiduals();
        int n = residualsArray.length;
        int p = model.length;
        int c = (int)Math.min(h1*n, n);	

        double[] absResiduals = new double[n];
        for(int i = 0;i<n;i++)
        {
            absResiduals[i] = Math.abs(residualsArray[i]);
        }
        Arrays.sort(absResiduals);
        double limit = k*absResiduals[c - 1];
        double count = 0;
        for(int i = 0;i<n;i++)
        {
            if(absResiduals[i]<=limit)
            {
                count++;
            }
            else
            {
                break;
            }
        }

        double h2 = count/n;
        int medianIndex = MathUtilities.robustMedianIndex(n, p);

        LTA finalLTA = LTA.findFit(data, model, h2, nstarts);
        double median = absResiduals[medianIndex];

        return new HighCoverageLTA(finalLTA, median);
    }

    public static HighCoverageLTA getHighCoverageLTAforFittedFunction(double[][] data, FittedLinearUnivariateFunction function, double k)
    {     
        int p = function.getParameterCount();

        double h1 = MathUtilities.minimalCoverage(data.length, p);        

        LinearRegressionEsimator firstReg = LTA.getLTAforFittedFunction(data, function, h1);
        ResidualVector residuals = firstReg.getResiduals();

        double[] residualsArray = residuals.getResiduals();
        int n = residualsArray.length;
        int c = (int)Math.min(h1*n, n); 

        double[] absResiduals = new double[n];
        for(int i = 0;i<n;i++)
        {
            absResiduals[i] = Math.abs(residualsArray[i]);
        }
        Arrays.sort(absResiduals);
        double limit = k*absResiduals[c - 1];
        double count = 0;
        for(int i = 0;i<n;i++)
        {
            if(absResiduals[i]<=limit)
            {
                count++;
            }
            else
            {
                break;
            }
        }

        double h2 = count/n;
        int medianIndex = MathUtilities.robustMedianIndex(n, p);

        LTA finalLTA = LTA.getLTAforFittedFunction(data, function, h2);
        double median = absResiduals[medianIndex];

        return new HighCoverageLTA(finalLTA, median);
    }

    public static double getObjectiveFunctionValue(double[][] data, ParametrizedUnivariateFunction function, double k)
    {     
        int n = data.length;

        if(n == 0)
        {
            return 0;
        }

        int p = function.getParameterCount();
        double h1 = MathUtilities.minimalCoverage(n, p);        
        int c = (int)Math.min(h1*n, n); 

        double[] absResiduals = new double[n];        

        for(int i = 0;i<n;i++)
        {
            double[] pt = data[i]; 
            double x = pt[0];
            double y = pt[1];
            double r = Math.abs(y - function.value(x));
            absResiduals[i] = r;
        }

        Arrays.sort(absResiduals);

        double limit = k*absResiduals[c - 1];
        double crit = 0;

        for(int i = 0;i<n;i++)
        {
            double absResidual = absResiduals[i];
            if(absResidual<=limit)
            {
                crit += absResidual;
            }
            else
            {
                break;
            }
        }

        return crit;
    }

    private HighCoverageLTA(LTA reg, double median)
    {
        this.finalLTA = reg;
        this.robustMedian = median;
    }

    @Override
    public FittedLinearUnivariateFunction getBestFit() 
    {
        return finalLTA.getBestFit();
    }

    @Override
    public double getObjectiveFunctionMinimum() 
    {
        return finalLTA.getObjectiveFunctionMinimum();
    }

    @Override
    public ResidualVector getResiduals() 
    {
        return finalLTA.getResiduals();
    }

    public double getRobustMedian()
    {
        return robustMedian;
    }

    @Override
    public double getCoverage() 
    {
        return finalLTA.getCoverage();
    }

    @Override
    public double[][] getCoveredCases() 
    {
        return finalLTA.getCoveredCases();
    }

    @Override
    public double[] getLastCoveredPoint()
    {
        return finalLTA.getLastCoveredPoint();
    }
}
