
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

import java.util.*;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.functions.ParametrizedUnivariateFunction;
import atomicJ.functions.PowerFunctionCombination;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.ResidualsComparator;




import Jama.LUDecomposition;
import Jama.Matrix;

public class LTA implements HighBreakdownEstimator
{
    private final double lowestCriterion;
    private final int coveredCount;
    private final double[][] data;
    private final FittedLinearUnivariateFunction bestFit;
    private final ResidualVector residuals;	

    //findFit() methods change the order of data, so if this an issue, copy them before passing  to LTA

    public static LTA findFit(double[][] data, int deg, boolean constant, int nstarts)
    {
        int n = data.length;
        int p =  deg + MathUtilities.boole(constant); 		
        double h = MathUtilities.minimalCoverage(n, p);

        return findFit(data, deg, constant, h, nstarts);
    }

    public static LTA findFit(double[][] data, int deg, boolean constant, double h, int nstarts)
    {
        int intercept = MathUtilities.boole(constant);
        int p =  deg + intercept;

        double[] model = new double[p];

        for(int j = 0;j<p;)
        {
            model[j++] = j - intercept;
        }
        return findFit(data, model,h, nstarts);
    }

    public static LTA findFit(double[][] data, double[] model, int nstarts)
    {
        int n = data.length;
        int p = model.length;
        double h = MathUtilities.minimalCoverage(n, p);

        return findFit(data, model,h, nstarts);
    }

    public static LTA findFit(double[][] data, double[] model, double h, int nstarts)
    {
        int n = data.length;
        int p = model.length;
        int c = (int)Math.min(n, h*n);

        List<double[]> points = Arrays.asList(data);		

        double lowestCriterion = Double.POSITIVE_INFINITY;
        PowerFunctionCombination bestFit = null;

        for(int i = 0;i<nstarts;i++)
        {
            Collections.shuffle(points);

            List<double[]> elementalSet = points.subList(0, p);			
            PowerFunctionCombination elementalFit = getElementalFit(elementalSet, model);
            if(elementalFit == null){continue;}
            Collections.sort(points, new ResidualsComparator(elementalFit));
            double crit = 0;
            for(int j = 0;j<c;j++)
            {
                double[] point = points.get(j);
                double r = Math.abs(elementalFit.residual(point));
                crit = crit + r;
            }

            if(crit<lowestCriterion)
            {
                lowestCriterion = crit;
                bestFit = elementalFit;
            }
        }	
        LTA fit = new LTA(data, bestFit, lowestCriterion, c);

        return fit;
    }

    public static LTA getLTAforFittedFunction(double[][] data, FittedLinearUnivariateFunction function)
    {
        int n = data.length;
        int p = function.getParameterCount();
        double h = MathUtilities.minimalCoverage(n, p);

        return getLTAforFittedFunction(data, function, h);      
    }

    public static LTA getLTAforFittedFunction(double[][] data, FittedLinearUnivariateFunction function, double h)
    {
        int n = data.length;
        int coverage = (int)Math.min(n, h*n);

        double[][] dataCopy = Arrays.copyOf(data, data.length);        

        double lowestCriterion = Double.POSITIVE_INFINITY;
        Arrays.sort(dataCopy, new ResidualsComparator(function));

        double crit = 0;

        for(int j = 0;j<coverage;j++)
        {
            double r = Math.abs(function.residual(dataCopy[j]));
            crit = crit + r;
        }

        LTA fit = new LTA(data, function, lowestCriterion, coverage);
        return fit; 
    }

    public static double getObjectiveFunctionValue(double[][] data, ParametrizedUnivariateFunction function)
    {
        int n = data.length;
        int p = function.getParameterCount();
        double h = MathUtilities.minimalCoverage(n, p);

        return getObjectiveFunctionValue(data, function, h);      
    }

    public static double getObjectiveFunctionValue(double[][] data, UnivariateFunction function, double h)
    {
        int n = data.length;
        int coverage = (int)Math.min(n, h*n);

        double[] absoluteResiduals = new double[n];        

        for(int i = 0;i<n;i++)
        {
            double[] p = data[i]; 
            double x = p[0];
            double y = p[1];
            double r = Math.abs(y - function.value(x));
            absoluteResiduals[i] = r;
        }

        Arrays.sort(absoluteResiduals);

        double crit = 0;

        for(int j = 0;j<coverage;j++)
        {
            crit = crit + absoluteResiduals[j];
        }

        return crit; 
    }

    private LTA(double[][] data, FittedLinearUnivariateFunction bestFit, double lowestCriterion, int coveredCount)
    {
        int n = data.length;

        this.data = data;
        this.bestFit = bestFit;
        this.lowestCriterion = lowestCriterion;
        this.coveredCount = coveredCount;

        double[] residualsdata = new double[n];
        for(int i = 0;i<n;i++)
        {
            residualsdata[i] = bestFit.residual(data[i]);
        }

        this.residuals = new ResidualVector(residualsdata);
    }

    private static PowerFunctionCombination getElementalFit(List<double[]> elementalSet, double[] model)
    {
        int p = model.length;

        double[][] matrixData1 = new double[p][p];
        double[] matrixData2 = new double[p];

        for(int j = 0;j<p;j++)
        {
            double[] point = elementalSet.get(j);
            double x = point[0];
            double y = point[1];

            matrixData2[j] = y;

            for(int k = 0;k<p;k++)
            {
                double e = model[k];
                matrixData1[j][k] = Math.pow(x, e);
            }
        }

        Matrix matrixTest = new Matrix(matrixData1);
        LUDecomposition decomp = new LUDecomposition(matrixTest);
        if(!decomp.isNonsingular()){return null;}
        Matrix coeffMatrix = decomp.solve(new Matrix(matrixData2,p));
        double[] coeff = coeffMatrix.getRowPackedCopy();
        PowerFunctionCombination fit = new PowerFunctionCombination(model, coeff);

        return fit;
    }

    @Override
    public FittedLinearUnivariateFunction getBestFit()
    {
        return bestFit;
    }


    @Override
    public double getObjectiveFunctionMinimum() 
    {
        return lowestCriterion;
    }

    @Override
    public ResidualVector getResiduals() 
    {
        return residuals;
    }

    @Override
    public double getCoverage()
    {
        double n = data.length;
        double h = coveredCount/n;
        return h;
    }

    @Override
    public double[][] getCoveredCases()
    {
        int n = data.length;
        double[][] covered = new double[coveredCount][];
        double[][] dataCopy = Arrays.copyOf(data, n);
        Arrays.sort(dataCopy,new ResidualsComparator(bestFit));
        for(int i = 0;i<coveredCount;i++)
        {
            covered[i] = dataCopy[i];
        }
        return covered;
    }

    @Override
    public double[] getLastCoveredPoint()
    {
        double[][] coveredCases =getCoveredCases();
        int index = ArrayUtilities.getMaximumXIndex(coveredCases);

        return coveredCases[index];
    }
}