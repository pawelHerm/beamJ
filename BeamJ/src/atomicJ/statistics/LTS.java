
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

import org.jfree.data.Range;

import atomicJ.functions.*;
import atomicJ.utilities.*;

import Jama.LUDecomposition;
import Jama.Matrix;

public class LTS implements HighBreakdownEstimator
{
    private static final double TOLERANCE = 1e-14; 
    private static final double[] LINEAR = new double[] {0,1};
    private static final double[] LINEAR_INTERCEPTLESS = new double[] {1};
    private static final double[] QUADRATIC = new double[] {0,1,2};
    private static final double[] QUADRATIC_INTERCEPTLESS = new double[] {1,2};

    private final int coveredCount;
    private final double lowestCriterion;
    private final double[][] data;
    private final FittedLinearUnivariateFunction bestFit;

    public static LTS findFit(double[][] data, int deg, int nstarts)
    {
        int n = data.length;    
        double h = MathUtilities.minimalCoverage(n, deg);

        return findFit(data, deg, true, h, nstarts);        
    }

    public static LTS findFit(double[][] data, int deg, boolean constant, int nstarts)
    {
        int n = data.length;
        int p =  deg + MathUtilities.boole(constant);       
        double h = MathUtilities.minimalCoverage(n, p);

        return findFit(data, deg, constant, h, nstarts);
    }

    public static LTS findFit(double[][] data, int deg, boolean constant, double h, int nstarts)
    {
        int intercept = MathUtilities.boole(constant);
        int p =  deg + intercept; 


        if(deg == 1) {return findLinearFit(data, constant, h, nstarts);}
        if(deg == 2) {return findQuadraticFit(data, constant, h, nstarts);}
        if(deg == 0) {return findConstantFit(data, h, nstarts);}
        else {return findPolynomialFit(data, p, h, nstarts);}
    }

    public static LTS findFit(double[][] data, double[] model, int nstarts)
    {
        int n = data.length;
        int p = model.length;
        double h = MathUtilities.minimalCoverage(n, p);

        return findFit(data,model,h,nstarts);
    }

    public static LTS findFit(double[][] data, double[] model, double h, int nstarts)
    {
        if(model.length == 1)
        {
            double exp = model[0];
            if(exp == 2){return findBareQuadraticFit(data, h, nstarts);}
            else if(exp == 1.5){return findSesquiFit(data, h, nstarts);}
            else {return findPowerFit(data, exp, h, nstarts);}
        }
        else if(Arrays.equals(model, LINEAR)) {return findLinearFit(data, true, h, nstarts);}
        else if(Arrays.equals(model, LINEAR_INTERCEPTLESS)) {return findLinearFit(data, false, h, nstarts);}
        else if(Arrays.equals(model, QUADRATIC)){return findQuadraticFit(data, true, h, nstarts);}
        else if(Arrays.equals(model, QUADRATIC_INTERCEPTLESS)) {return findQuadraticFit(data, false, h, nstarts);}
        else {return findPowerFunctionsCombinationFit(data, model,h, nstarts);}
    }

    LTS(double[][] data, FittedLinearUnivariateFunction bestFit, double lowestCriterion, int c)
    {
        this.data = data;
        this.bestFit = bestFit;
        this.lowestCriterion = lowestCriterion;
        this.coveredCount = c;
    }

    private static LTS findLinearFit(double[][] data, boolean constant, double h, int nstarts)
    {
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSqrt = new double[n];
        FittedLinearUnivariateFunction bestFit = null;
        Random random = new Random();
        double bestIntercept = 0;
        double bestSlope = 0;

        if(constant)
        {
            for(int i = 0;i<nstarts;i++)
            {
                double x0,x1,y0,y1;

                double[] p = data[random.nextInt(n)];
                x0 = p[0];y0 = p[1];            
                p = data[random.nextInt(n)];
                x1 = p[0];y1 = p[1];            
                double denomin = (x0 - x1);
                if(Math.abs(denomin)>TOLERANCE)
                {
                    double a = (x0*y1 - x1*y0)/denomin;
                    double b = (y0 - y1)/denomin;
                    Line exactFit = new Line(a,b);
                    double crit = exactFit.trimmedSquares(data,residualsSqrt, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                        bestIntercept = a;
                        bestSlope = b;
                    };
                }       
            }
            bestFit = new Line(bestIntercept,bestSlope);
        }
        else
        {
            for(int i = 0;i<nstarts;i++)
            {
                double[] p = data[random.nextInt(n)];

                double x0 = p[0];
                double y0 = p[1]; 

                if(Math.abs(x0)>TOLERANCE)
                {
                    double a = y0/x0;
                    InterceptlessLine exactFit = new InterceptlessLine(a);
                    double crit = exactFit.trimmedSquares(data,residualsSqrt, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                        bestSlope = a;
                    };
                }

            }
            bestFit = new InterceptlessLine(bestSlope);
        }
        LTS fit = new LTS(data, bestFit, lowestCriterion,c);
        return fit;
    }

    public static LTS findConstantFit(double[][] data, double h, int nstarts)
    {
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSqrt = new double[n];
        Random random = new Random();
        double bestA = 0;

        for(int i = 0;i<nstarts;i++)
        {
            double x0,y0;

            double[] p = data[random.nextInt(n)];
            x0 = p[0];y0 = p[1];            
            if(Math.abs(x0)>TOLERANCE)
            {
                double a = y0/(x0*x0);
                Constant exactFit = new Constant(a);
                double crit = exactFit.trimmedSquares(data,residualsSqrt, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestA = a;
                };
            }

        }
        FittedLinearUnivariateFunction bestFit = new Constant(bestA);
        LTS fit = new LTS(data, bestFit, lowestCriterion, c);
        return fit;
    }

    private static LTS findBareQuadraticFit(double[][] data, double h, int nstarts)
    {
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSqrt = new double[n];
        Random random = new Random();
        double bestA = 0;

        for(int i = 0;i<nstarts;i++)
        {
            double x0,y0;

            double[] p = data[random.nextInt(n)];
            x0 = p[0];y0 = p[1];            
            if(Math.abs(x0)>TOLERANCE)
            {
                double a = y0/(x0*x0);
                BareQuadratic exactFit = new BareQuadratic(a);
                double crit = exactFit.trimmedSquares(data,residualsSqrt, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestA = a;
                };
            }

        }
        FittedLinearUnivariateFunction bestFit = new BareQuadratic(bestA);
        LTS fit = new LTS(data, bestFit, lowestCriterion, c);
        return fit;
    }

    private static LTS findSesquiFit(double[][] data, double h, int nstarts)
    {
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSqrt = new double[n];
        Random random = new Random();
        double bestA = 0;

        for(int i = 0;i<nstarts;i++)
        {
            double x0,y0;

            double[] p = data[random.nextInt(n)];
            x0 = p[0];y0 = p[1];            
            if(Math.abs(x0) > TOLERANCE)
            {
                double a = y0/(x0*Math.sqrt(x0));
                Sesquilinear exactFit = new Sesquilinear(a);
                double crit = exactFit.trimmedSquares(data,residualsSqrt, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestA = a;
                };
            }

        }
        FittedLinearUnivariateFunction bestFit = new Sesquilinear(bestA);
        LTS fit = new LTS(data, bestFit, lowestCriterion, c);
        return fit;
    }

    private static LTS findPowerFit(double[][] data, double exp, double h, int nstarts)
    {
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSqrt = new double[n];
        Random random = new Random();
        double bestA = 0;

        for(int i = 0;i<nstarts;i++)
        {
            double x0,y0;

            double[] p = data[random.nextInt(n)];
            x0 = p[0];y0 = p[1];            
            if(Math.abs(x0)>TOLERANCE)
            {
                double a = y0/Math.pow(x0,exp);
                PowerFunction exactFit = new PowerFunction(a,exp);
                double crit = exactFit.trimmedSquares(data,residualsSqrt, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestA = a;
                };
            }

        }
        FittedLinearUnivariateFunction bestFit = new PowerFunction(bestA,exp);
        LTS fit = new LTS(data, bestFit, lowestCriterion, c);
        return fit;
    }

    //Constructs an LTS object, which represent quadratic fit, i.e. quadratic function with an intercept is fitted
    private static LTS findQuadraticFit(double[][] data, boolean constant, double h, int nstarts)
    {   
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSqrt = new double[n];
        FittedLinearUnivariateFunction bestFit = null;
        Random random = new Random();

        if(constant)
        {
            double[][] vandermondData = new double[3][3];
            double[] ordinates = new double[3];
            double[] bestFitCoeff = null;
            double[][] elementalSet = new double[3][2]; 

            for(int i = 0;i<nstarts;i++)
            {
                for(int j = 0;j<3;j++)
                {
                    int k = random.nextInt(n);
                    elementalSet[j] = data[k];
                }

                double[] coeff;

                for(int j = 0;j<3;j++)
                {
                    double[] point = elementalSet[j];
                    double x = point[0];
                    double y = point[1];

                    double[] row = vandermondData[j];
                    row[0] = 1;
                    row[1] = x;
                    row[2] = x*x;

                    ordinates[j] = y;
                }

                Matrix vandermondMatrix = new Matrix(vandermondData, 3, 3);

                LUDecomposition decomp = new LUDecomposition(vandermondMatrix);
                if(decomp.isNonsingular())
                {
                    Matrix coeffMatrix = decomp.solve(new Matrix(ordinates,3));
                    coeff = coeffMatrix.getRowPackedCopy();

                    Quadratic exactFit = new Quadratic(coeff[0],coeff[1],coeff[2]);
                    double crit = exactFit.trimmedSquares(data, residualsSqrt, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                        bestFitCoeff = coeff;
                    }
                }

            }
            bestFit = new Polynomial(bestFitCoeff);
        }
        else
        {
            double bestA = 0;
            double bestB = 0;

            for(int i = 0;i<nstarts;i++)
            {
                double x0,x1,y0,y1;

                double[] p = data[random.nextInt(n)];
                x0 = p[0];y0 = p[1];            
                p = data[random.nextInt(n)];
                x1 = p[0];y1 = p[1];            
                double denomin = (x0*(x0 - x1)*x1);
                if(Math.abs(denomin)>TOLERANCE)
                {
                    double a = (-x1*x1*y0 + x0*x0*y1)/denomin;
                    double b = ((x1*y0 - x0*y1)/denomin);
                    InterceptlessQuadratic exactFit = new InterceptlessQuadratic(a,b);
                    double crit = exactFit.trimmedSquares(data,residualsSqrt, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                        bestA = a;
                        bestB = b;
                    };
                }

            }
            bestFit = new Polynomial(new double[] {0, bestA,bestB});
        }
        LTS fit = new LTS(data, bestFit, lowestCriterion, c);
        return fit;
    }

    private static LTS findPolynomialFit(double[][] data,int p, double h, int nstarts)
    {   
        int n = data.length;
        int c = (int)Math.min(n, h*n);

        Random random = new Random();

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] ordinates = new double[p];
        double[] bestFitCoeff = null;
        double[] residualsSqrt = new double[n];
        double[][] elementalSet = new double[p][2];
        double[][] vandermondData = new double[p][p];


        for(int i = 0;i<nstarts;i++)
        {
            for(int j = 0;j<p;j++)
            {
                int k = random.nextInt(n);
                elementalSet[j] = data[k];
            }

            double[] coeff;

            for(int j = 0;j<p;j++)
            {
                double[] point = elementalSet[j];
                double x = point[0];
                double y = point[1];

                ordinates[j] = y;

                double[] row = vandermondData[j];
                row[0] = 1;
                row[1] = x;
                for(int k = 2;k<p;k++)
                {
                    row[k] = x*row[k - 1];
                }
            }
            Matrix vandermondMatrix = new Matrix(vandermondData, p, p);
            LUDecomposition decomp = new LUDecomposition(vandermondMatrix);
            if(decomp.isNonsingular())
            {
                Matrix coeffMatrix = decomp.solve(new Matrix(ordinates,p));
                coeff = coeffMatrix.getRowPackedCopy();

                double crit =  Polynomial.trimmedSquares(data,coeff, p-1,residualsSqrt, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestFitCoeff = coeff;
                }
            }

        }
        FittedLinearUnivariateFunction bestFit = new Polynomial(bestFitCoeff);;

        LTS fit = new LTS(data, bestFit, lowestCriterion, c);
        return fit;
    }

    private static LTS findPowerFunctionsCombinationFit(double[][] data, double[] model, double h, int nstarts)
    {

        int n = data.length;
        int p = model.length;
        int coverage = (int)Math.min(n, h*n);

        List<double[]> points = Arrays.asList(data);        

        double lowestCriterion = Double.POSITIVE_INFINITY;
        PowerFunctionCombination bestFit = null;
        Random random = new Random();

        for(int i = 0;i<nstarts;i++)
        {

            for(int j = 0;j<p;j++)
            {
                int k = random.nextInt(n);
                Collections.swap(points, j, k);
            }

            List<double[]> elementalSet = points.subList(0, p);         

            double[][] matrixData1 = new double[p][p];
            double[] matrixData2 = new double[p];
            double[] coeff;

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
            Matrix matrixTest = new Matrix(matrixData1, p, p);
            LUDecomposition decomp = new LUDecomposition(matrixTest);
            if(decomp.isNonsingular())
            {
                Matrix coeffMatrix = decomp.solve(new Matrix(matrixData2,p));
                coeff = coeffMatrix.getRowPackedCopy();
                PowerFunctionCombination elementalFit = new PowerFunctionCombination(model, coeff);;
                Collections.sort(points,new ResidualsComparator(elementalFit));
                double crit = 0;
                for(int j = 0;j<coverage;j++)
                {
                    double r = elementalFit.residual(points.get(j));
                    double r2 = r*r;
                    crit = crit + r2;
                }
                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestFit = elementalFit;
                }
            }       
        }   
        LTS fit = new LTS(data, bestFit, lowestCriterion, coverage);
        return fit; 
    }


    public static LTS getLTSforFittedFunction(double[][] data, FittedLinearUnivariateFunction function)
    {
        int n = data.length;
        int p = function.getParameterCount();
        double h = MathUtilities.minimalCoverage(n, p);

        return getLTSforFittedFunction(data, function, h);

    }

    public static LTS getLTSforFittedFunction(double[][] data, FittedLinearUnivariateFunction function, double h)
    {
        double[][] dataCopy = Arrays.copyOf(data, data.length);        

        int n = data.length;
        int coverage = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;

        SelectorArray.sortSmallest(dataCopy, coverage - 1, new ResidualsComparator(function));

        double crit = 0;

        for(int j = 0;j<coverage;j++)
        {
            double r = function.residual(dataCopy[j]);
            double r2 = r*r;
            crit = crit + r2;
        }

        LTS fit = new LTS(dataCopy, function, lowestCriterion, coverage);

        return fit; 
    }

    public static double getObjectiveFunctionValue(double[][] data, ParametrizedUnivariateFunction function)
    {
        int n = data.length;
        int p = function.getParameterCount();
        double h = MathUtilities.minimalCoverage(n, p);

        return getObjectiveFunctionValue(data, function, h);

    }

    public static double getObjectiveFunctionValue(double[][] data, ParametrizedUnivariateFunction function, double h)
    {
        int n = data.length;
        int coverage = (int)Math.min(n, h*n);

        double[] squaredResiduals = new double[n];        

        for(int i = 0;i<n;i++)
        {
            double[] p = data[i]; 
            double x = p[0];
            double y = p[1];
            double r = y - function.value(x);
            squaredResiduals[i] = r*r;
        }

        Selector.sortSmallest(squaredResiduals, coverage - 1);

        double crit = 0;

        for(int j = 0;j<coverage;j++)
        {
            crit = crit + squaredResiduals[j];
        }

        return crit; 
    }

    public double[][] getData()
    {
        return data;
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
        int n = data.length;
        double[] residuals = new double[n];
        for(int i = 0;i<n;i++)
        {
            residuals[i] = bestFit.residual(data[i]);
        }
        return new ResidualVector(residuals);
    }

    @Override
    public double getCoverage()
    {
        double n = data.length;
        double h = coveredCount/n;
        return h;
    }

    public double getLargestClusterOfCoveredCases()
    {
        double[][] coveredCases = getCoveredCases();

        Arrays.sort(coveredCases, new AbscissaComparator());

        int n = coveredCases.length;

        Range xRange = ArrayUtilities.getXRange(data);

        double eps = 15*xRange.getLength()/data.length;

        double largestClusterLength = 0;

        double currentClusterLeftX = coveredCases[0][0];

        double previousX = coveredCases[0][0];

        for(int i = 1; i<n;i++)
        {
            double x = coveredCases[i][0];

            double dx = x - previousX;

            if(dx <= eps)
            {
                double currentClusterLength = x - currentClusterLeftX;
                if(currentClusterLength > largestClusterLength)
                {
                    largestClusterLength = currentClusterLength;
                }
            }
            else
            {
                currentClusterLeftX = x;
            }

            previousX = x;   
        }

        return currentClusterLeftX;
    }


    @Override
    public double[][] getCoveredCases()
    {
        double[][] dataCopy = Arrays.copyOf(data, data.length);

        SelectorArray.sortSmallest(dataCopy, coveredCount - 1, new ResidualsComparator(bestFit));
        double[][] covered = Arrays.copyOf(dataCopy, coveredCount);

        return covered;
    }

    @Override
    public double[] getLastCoveredPoint()
    {
        double[][] coveredCases = getCoveredCases();
        int index = ArrayUtilities.getMaximumXIndex(coveredCases);

        return coveredCases[index];
    }
}