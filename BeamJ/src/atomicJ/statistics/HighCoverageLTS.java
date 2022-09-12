
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


public class HighCoverageLTS implements HighBreakdownEstimator
{
    private final LTS finalLTS;
    private final double robustMedian;

    public static HighCoverageLTS findFit(double[][] data, int deg, boolean constant, double k, int nstarts)
    {
        int n = data.length;
        int p = deg + MathUtilities.boole(constant);	
        double minimalCoverage = MathUtilities.minimalCoverage(n, p);		

        return findFit(data, deg, constant, k, minimalCoverage, nstarts);
    }

    public static HighCoverageLTS findFit(double[] data, int deg, boolean constant, double k, int nstarts)
    {
        int n = data.length;
        int p = deg + MathUtilities.boole(constant);    
        double minimalCoverage = MathUtilities.minimalCoverage(n, p);       

        return findFit(data, deg, constant, k, minimalCoverage, nstarts);
    }

    public static HighCoverageLTS findFit(double[][] data, int deg, boolean constant, double k, double minimalCoverage, int nstarts)
    {
        LinearRegressionEsimator firstReg = LTS.findFit(data, deg, constant, minimalCoverage, nstarts);
        ResidualVector residuals = firstReg.getResiduals();

        double[] residualsArray = residuals.getResiduals();
        int n = residualsArray.length;
        int p = deg + MathUtilities.boole(constant);
        int c = (int)Math.min(minimalCoverage*n, n);	

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

        LTS finalLTS = LTS.findFit(data, deg, constant, h2, nstarts);
        double median = absResiduals[medianIndex];

        return new HighCoverageLTS(finalLTS, median);
    }


    public static HighCoverageLTS findFit(double[] data, int deg, boolean constant, double k, double minimalCoverage, int nstarts)
    {
        LinearRegressionEsimator firstReg = LTS1DFactory.findFit(data, deg, constant, minimalCoverage, nstarts);
        ResidualVector residuals = firstReg.getResiduals();

        double[] residualsArray = residuals.getResiduals();
        int n = residualsArray.length;
        int p = deg + MathUtilities.boole(constant);
        int c = (int)Math.min(minimalCoverage*n, n);    

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

        LTS finalLTS = LTS1DFactory.findFit(data, deg, constant, h2, nstarts);
        double median = absResiduals[medianIndex];

        return new HighCoverageLTS(finalLTS, median);
    }


    public static HighCoverageLTS findFit(double[][] data, double[] model, double k, int nstarts)
    {
        int n = data.length;
        int p = model.length;	
        double h1 = MathUtilities.minimalCoverage(n, p);		

        return findFit(data, model, k, h1, nstarts);
    }

    public static HighCoverageLTS findFit(double[][] data, double[] model, double k, double h1, int nstarts)
    {		
        LinearRegressionEsimator firstReg = LTS.findFit(data, model, nstarts);
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

        LTS finalLTS = LTS.findFit(data, model, h2, nstarts);

        double median = medianIndex < n ? absResiduals[medianIndex] : Double.NaN;

        return new HighCoverageLTS(finalLTS, median);
    }

    public static HighCoverageLTS findFit(double[] data, double[] model, double k, double h1, int nstarts)
    {       
        LinearRegressionEsimator firstReg = LTS1DFactory.findFit(data, model, nstarts);
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

        LTS finalLTS = LTS1DFactory.findFit(data, model, h2, nstarts);

        double median = medianIndex < n ? absResiduals[medianIndex] : Double.NaN;

        return new HighCoverageLTS(finalLTS, median);
    }

    public static HighCoverageLTS getHighCoverageLTSforFittedFunction(double[][] data, FittedLinearUnivariateFunction function, double k)
    {     
        int p = function.getParameterCount();
        double h1 = MathUtilities.minimalCoverage(data.length, p);        

        LinearRegressionEsimator firstReg = LTS.getLTSforFittedFunction(data, function, h1);
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

        LTS finalLTS = LTS.getLTSforFittedFunction(data, function, h2);
        double median = absResiduals[medianIndex];

        return new HighCoverageLTS(finalLTS, median);
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
                crit += absResidual*absResidual;
            }
            else
            {
                break;
            }
        }

        return crit;
    }

    private HighCoverageLTS(LTS reg, double median)
    {
        this.finalLTS = reg;
        this.robustMedian = median;
    }

    public double[][] getData()
    {
        return finalLTS.getData();
    }

    @Override
    public FittedLinearUnivariateFunction getBestFit() 
    {
        return finalLTS.getBestFit();
    }

    @Override
    public double getObjectiveFunctionMinimum() 
    {
        return finalLTS.getObjectiveFunctionMinimum();
    }

    @Override
    public ResidualVector getResiduals() 
    {
        return finalLTS.getResiduals();
    }

    public double getRobustMedian()
    {
        return robustMedian;
    }

    @Override
    public double getCoverage() 
    {
        return finalLTS.getCoverage();
    }

    @Override
    public double[][] getCoveredCases() 
    {
        return finalLTS.getCoveredCases();
    }

    @Override
    public double[] getLastCoveredPoint()
    {
        return finalLTS.getLastCoveredPoint();
    }

    public double getLargestClusterOfCoveredCases() 
    {
        return finalLTS.getLargestClusterOfCoveredCases();
    }

}
