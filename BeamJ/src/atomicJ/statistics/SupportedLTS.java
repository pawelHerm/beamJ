
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

import atomicJ.functions.*;
import atomicJ.utilities.*;

import Jama.LUDecomposition;
import Jama.Matrix;

public class SupportedLTS implements LinearRegressionEsimator
{
    private static final double TOLERANCE = 1e-14; 
    private static final double[] LINEAR = new double[] {0,1};
    private static final double[] LINEAR_INTERCEPTLESS = new double[] {1};
    private static final double[] QUADRATIC = new double[] {0,1,2};
    private static final double[] QUADRATIC_INTERCEPTLESS = new double[] {1,2};

    private final double lowestCriterion;
    private final FittedLinearUnivariateFunction bestFit;
    private final ResidualVector residuals;	

    private static int getDefaultNumberOfStarts(double h, int p, double lim)
    {
        int nstarts = (int)Math.ceil(Math.log(lim)/Math.log((1 - Math.pow(h, lim))));
        return nstarts;
    }

    public static SupportedLTS findFit(double[][] data,double[][] support, int deg, int nstarts)
    {
        int n = data.length;	
        double h = MathUtilities.minimalCoverage(n, deg);

        return findFit(data, support, deg, true, h, nstarts);		
    }

    public static SupportedLTS findFit(double[][] data,double[][] support, int deg, boolean constant, int nstarts)
    {
        int n = data.length;
        int p =  deg + MathUtilities.boole(constant); 		
        double h = MathUtilities.minimalCoverage(n, p);

        return findFit(data,support, deg, constant, h, nstarts);
    }

    public static SupportedLTS findFit(double[][] data, double[][] support, int deg, boolean constant, double h, int nstarts)
    {
        int intercept = MathUtilities.boole(constant);
        int p =  deg + intercept; 

        if(deg == 1) {return findLinearFit(data, support, constant, h, nstarts);}
        if(deg == 2) {return findQuadraticFit(data, support, constant, h, nstarts);}
        else {return findPolynomialFit(data, support, p, h, nstarts);}
    }

    public static SupportedLTS findFit(double[][] data, double[][] support, double[] model)
    {
        int n = data.length;
        int p = model.length;
        double h = MathUtilities.minimalCoverage(n, p);
        int nstarts = getDefaultNumberOfStarts(h, p, 0.0001);

        return findFit(data, support, model,h,nstarts);
    }

    public static SupportedLTS findFit(double[][] data, double[][] support, double[] model, int nstarts)
    {
        int n = data.length;
        int p = model.length;
        double h = MathUtilities.minimalCoverage(n, p);

        return findFit(data, support, model,h,nstarts);
    }

    public static SupportedLTS findFit(double[][] data, double[][] support, double[] model, double h, int nstarts)
    {
        if(model.length == 1)
        {
            double exp = model[0];
            if(exp == 2){return findBareQuadraticFit(data, support, h, nstarts);}
            else if(exp == 1.5){return findSesquiFit(data, support, h, nstarts);}
            else if(exp == 1){return findLinearFit(data, support, false, h, nstarts);}
            else {return findPowerFit(data, support, exp, h, nstarts);}
        }
        else if(model.length == 2 && model[1] == 1.5)
        {
            return findSesquiInterceptFit(data, support, h, nstarts);          
        }
        else if(Arrays.equals(model, LINEAR)) {return findLinearFit(data, support, true, h, nstarts);}
        else if(Arrays.equals(model, QUADRATIC)){return findQuadraticFit(data, support, true, h, nstarts);}
        else if(Arrays.equals(model, QUADRATIC_INTERCEPTLESS)) {return findQuadraticFit(data, support, false, h, nstarts);}
        else {return findPowerFunctionsCombinationFit(data, support, model, h, nstarts);}
    }

    private SupportedLTS(FittedLinearUnivariateFunction bestFit, ResidualVector residuals, double lowestCriterion)
    {
        this.bestFit = bestFit;
        this.residuals = residuals;
        this.lowestCriterion = lowestCriterion;
    }	

    private static SupportedLTS findLinearFit(double[][] optional, double[][] support, boolean constant, double h, int nstarts)
    {        
        double[][] allPoints = ArrayUtilities.join(optional, support);	
        int n = optional.length;
        int s = support.length;
        int t = n + s;
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

                double[] p = allPoints[random.nextInt(t)];
                x0 = p[0];y0 = p[1];			
                p = allPoints[random.nextInt(t)];
                x1 = p[0];y1 = p[1];			
                double denomin = (x0 - x1);
                if(Math.abs(denomin)>TOLERANCE)
                {
                    double a = (x0*y1 - x1*y0)/denomin;
                    double b = (-y0 + y1)/denomin;
                    Line exactFit = new Line(a,b);
                    double crit = exactFit.trimmedSquares(optional, support,residualsSqrt, c);

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
                double x0,y0;

                double[] p = allPoints[random.nextInt(t)];
                x0 = p[0];y0 = p[1];						
                if(Math.abs(x0)>TOLERANCE)
                {
                    double a = y0/x0;
                    InterceptlessLine exactFit = new InterceptlessLine(a);
                    double crit = exactFit.trimmedSquares(optional, support, residualsSqrt, c);

                    if(crit<lowestCriterion)
                    {
                        lowestCriterion = crit;
                        bestSlope = a;
                    };
                }

            }
            bestFit = new InterceptlessLine(bestSlope);
        }

        double[] residualValues = new double[t];
        for(int i = 0;i<t;i++)
        {
            residualValues[i] = bestFit.residual(allPoints[i]);
        }
        ResidualVector residuals = new ResidualVector(residualValues);
        SupportedLTS fit = new SupportedLTS(bestFit,residuals,lowestCriterion);
        return fit;
    }

    private static SupportedLTS findBareQuadraticFit(double[][] data, double[][] support, double h, int nstarts)
    {
        double[][] allPoints = ArrayUtilities.join(data, support);	
        int n = data.length;
        int s = support.length;
        int t = n + s;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSqrt = new double[n];
        Random random = new Random();
        double bestA = 0;

        for(int i = 0;i<nstarts;i++)
        {
            double x0,y0;

            double[] p = allPoints[random.nextInt(t)];
            x0 = p[0];
            y0 = p[1];			
            if(Math.abs(x0)>TOLERANCE)
            {
                double a = y0/(x0*x0);
                BareQuadratic exactFit = new BareQuadratic(a);
                double crit = exactFit.trimmedSquares(data,support, residualsSqrt, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestA = a;
                };
            }

        }
        FittedLinearUnivariateFunction bestFit = new BareQuadratic(bestA);

        double[] residualsdata = new double[t];
        for(int i = 0;i<t;i++)
        {
            residualsdata[i] = bestFit.residual(allPoints[i]);
        }
        ResidualVector residuals = new ResidualVector(residualsdata);
        SupportedLTS fit = new SupportedLTS(bestFit,residuals,lowestCriterion);
        return fit;
    }

    private static SupportedLTS findSesquiFit(double[][] data, double[][] support, double h, int nstarts)
    {
        double[][] allPoints = ArrayUtilities.join(data, support);	
        int n = data.length;
        int s = support.length;
        int t = n + s;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSqrt = new double[n];
        Random random = new Random();
        double bestA = 0;

        for(int i = 0;i<nstarts;i++)
        {
            double x0,y0;

            double[] p = allPoints[random.nextInt(t)];

            x0 = p[0];y0 = p[1];			
            if(Math.abs(x0)>TOLERANCE)
            {
                double a = y0/(x0*Math.sqrt(x0));
                Sesquilinear exactFit = new Sesquilinear(a);
                double crit = exactFit.trimmedSquares(data,support, residualsSqrt, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestA = a;
                };
            }

        }
        FittedLinearUnivariateFunction bestFit = new Sesquilinear(bestA);

        double[] residualsdata = new double[t];
        for(int i = 0;i<t;i++)
        {
            residualsdata[i] = bestFit.residual(allPoints[i]);
        }
        ResidualVector residuals = new ResidualVector(residualsdata);
        SupportedLTS fit = new SupportedLTS(bestFit,residuals,lowestCriterion);
        return fit;
    }

    private static SupportedLTS findSesquiInterceptFit(double[][] data, double[][] support, double h, int nstarts)
    {
        double[][] allPoints = ArrayUtilities.join(data, support);  
        int n = data.length;
        int s = support.length;
        int t = n + s;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSqrt = new double[n];
        Random random = new Random();
        double bestA = 0;
        double bestB = 0;

        for(int i = 0;i<nstarts;i++)
        {
            double x0,y0,x1,y1;

            double[] p0 = allPoints[random.nextInt(t)];
            double[] p1 = allPoints[random.nextInt(t)];

            x0 = p0[0];
            y0 = p0[1];
            x1 = p1[0];
            y1 = p1[1]; 

            if(Math.abs(x0)>TOLERANCE)
            {
                double sqrtX0 = Math.sqrt(x0); 
                double sqrtX1 = Math.sqrt(x1);
                double a = (y0 - y1)/(x0*sqrtX0 - x1*sqrtX1);
                double b = y0 - a*x0*sqrtX0;

                SesquilinearIntercept exactFit = new SesquilinearIntercept(a,b);
                double crit = exactFit.trimmedSquares(data,support, residualsSqrt, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestA = a;
                    bestB = b;
                };
            }

        }
        FittedLinearUnivariateFunction bestFit = new SesquilinearIntercept(bestA, bestB);

        double[] residualsdata = new double[t];
        for(int i = 0;i<t;i++)
        {
            residualsdata[i] = bestFit.residual(allPoints[i]);
        }
        ResidualVector residuals = new ResidualVector(residualsdata);
        SupportedLTS fit = new SupportedLTS(bestFit,residuals,lowestCriterion);
        return fit;
    }

    public static SupportedLTS findPowerFit(double[][] data, double[][] support, double exp, double h, int nstarts)
    {        
        double[][] allPoints = ArrayUtilities.join(data, support);	
        int n = data.length;
        int s = support.length;
        int t = n + s;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSqrt = new double[n];
        Random random = new Random();
        double bestA = 0;

        for(int i = 0;i<nstarts;i++)
        {
            double x0,y0;

            double[] p = allPoints[random.nextInt(t)];
            x0 = p[0];y0 = p[1];			
            if(Math.abs(x0)>TOLERANCE)
            {
                double a = y0/Math.pow(x0,exp);
                PowerFunction exactFit = new PowerFunction(a,exp);
                double crit = exactFit.trimmedSquares(data, support, residualsSqrt, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestA = a;
                };
            }

        }
        FittedLinearUnivariateFunction bestFit = new PowerFunction(bestA,exp);

        double[] residualsdata = new double[t];
        for(int i = 0;i<t;i++)
        {
            residualsdata[i] = bestFit.residual(allPoints[i]);
        }
        ResidualVector residuals = new ResidualVector(residualsdata);
        SupportedLTS fit = new SupportedLTS(bestFit,residuals,lowestCriterion);
        return fit;
    }

    public static SupportedLTS findPowerInterceptFit(double[][] data, double[][] support, double exp, double h, int nstarts)
    {
        double[][] allPoints = ArrayUtilities.join(data, support);  
        int n = data.length;
        int s = support.length;
        int t = n + s;
        int c = (int)Math.min(n, h*n);

        double lowestCriterion = Double.POSITIVE_INFINITY;
        double[] residualsSqrt = new double[n];
        Random random = new Random();
        double bestA = 0;
        double bestB = 0;

        for(int i = 0;i<nstarts;i++)
        {
            double x0,y0,x1,y1;

            double[] p0 = allPoints[random.nextInt(t)];
            double[] p1 = allPoints[random.nextInt(t)];

            x0 = p0[0];
            y0 = p0[1];
            x1 = p1[0];
            y1 = p1[1];

            if(Math.abs(x0)>TOLERANCE)
            {
                double x0Exp = Math.pow(x0, exp);
                double a = (y0 - y1)/(x0Exp - Math.pow(x1, exp));
                double b = y0 - a*x0Exp;

                PowerInterceptFunction exactFit = new PowerInterceptFunction(a,exp, b);
                double crit = exactFit.trimmedSquares(data, support, residualsSqrt, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestA = a;
                    bestB = b;
                };
            }

        }
        FittedLinearUnivariateFunction bestFit = new PowerInterceptFunction(bestA,exp, bestB);

        double[] residualsdata = new double[t];
        for(int i = 0;i<t;i++)
        {
            residualsdata[i] = bestFit.residual(allPoints[i]);
        }
        ResidualVector residuals = new ResidualVector(residualsdata);
        SupportedLTS fit = new SupportedLTS(bestFit,residuals,lowestCriterion);
        return fit;
    }


    //Constructs an SupportedLTS object, which represent quadratic fit, i.e. quadratic function with intercept is fitted
    private static SupportedLTS findQuadraticFit(double[][] data, double[][] support, boolean constant, double h, int nstarts)
    {	
        double[][] allPoints = ArrayUtilities.join(data, support);	
        int n = data.length;
        int s = support.length;
        int t = n + s;
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
                    int k = random.nextInt(t);
                    elementalSet[j] = allPoints[k];
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
                if(!decomp.isNonsingular()){continue;}
                Matrix coeffMatrix = decomp.solve(new Matrix(ordinates,3));
                coeff = coeffMatrix.getRowPackedCopy();

                Quadratic exactFit = new Quadratic(coeff[0],coeff[1],coeff[2]);
                double crit = exactFit.trimmedSquares(data, support, residualsSqrt, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestFitCoeff = coeff;
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

                double[] p = allPoints[random.nextInt(t)];
                x0 = p[0];y0 = p[1];			
                p = allPoints[random.nextInt(t)];
                x1 = p[0];y1 = p[1];			
                double denomin = (x0*(x0 - x1)*x1);
                if(Math.abs(denomin)>TOLERANCE)
                {
                    double a = (-x1*x1*y0 + x0*x0*y1)/denomin;
                    double b = ((x1*y0 - x0*y1)/denomin);
                    InterceptlessQuadratic exactFit = new InterceptlessQuadratic(a,b);
                    double crit = exactFit.trimmedSquares(data, support, residualsSqrt, c);

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

        double[] residualsdata = new double[t];
        for(int i = 0;i<t;i++)
        {
            residualsdata[i] = bestFit.residual(allPoints[i]);
        }
        ResidualVector residuals = new ResidualVector(residualsdata);
        SupportedLTS fit = new SupportedLTS(bestFit,residuals,lowestCriterion);
        return fit;
    }


    private static SupportedLTS findPolynomialFit(double[][] data, double[][] support, int p, double h, int nstarts)
    {	
        double[][] allPoints = ArrayUtilities.join(data, support);	
        int n = data.length;
        int s = support.length;
        int t = n + s;
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
                int k = random.nextInt(t);
                elementalSet[j] = allPoints[k];
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

                double crit =  Polynomial.trimmedSquares(data, coeff, p-1,residualsSqrt, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestFitCoeff = coeff;
                }
            }

        }

        FittedLinearUnivariateFunction bestFit = new Polynomial(bestFitCoeff);;

        double[] residualsdata = new double[t];
        for(int i = 0;i<t;i++)
        {
            residualsdata[i] = bestFit.residual(allPoints[i]);
        }
        ResidualVector residuals = new ResidualVector(residualsdata);
        SupportedLTS fit = new SupportedLTS(bestFit,residuals,lowestCriterion);
        return fit;
    }

    private static SupportedLTS findPowerFunctionsCombinationFit(double[][] data, double[][] support, double[] model, double h, int nstarts)
    {
        double[][] allPoints = ArrayUtilities.join(data, support);	
        int n = data.length;
        double[] residualsSqrt = new double[n];

        int s = support.length;
        int t = n + s;
        int c = (int)Math.min(n, h*n);
        int p = model.length;

        double lowestCriterion = Double.POSITIVE_INFINITY;
        PowerFunctionCombination bestFit = null;
        double[][] elementalSet = new double[p][2];

        double[][] powersMatrix = new double[p][p];
        double[] ordinates = new double[p];

        Random random = new Random();

        for(int i = 0;i<nstarts;i++)
        {
            for(int j = 0;j<p;j++)
            {
                int k = random.nextInt(t);
                elementalSet[j] = allPoints[k];
            }


            double[] coeff;

            for(int j = 0;j<p;j++)
            {
                double[] point = elementalSet[j];
                double x = point[0];
                double y = point[1];

                ordinates[j] = y;

                for(int k = 0;k<p;k++)
                {
                    double e = model[k];
                    powersMatrix[j][k] = Math.pow(x, e);
                }
            }
            Matrix matrixTest = new Matrix(powersMatrix, p, p);
            LUDecomposition decomp = new LUDecomposition(matrixTest);

            if(decomp.isNonsingular())
            {
                Matrix coeffMatrix = decomp.solve(new Matrix(ordinates,p));
                coeff = coeffMatrix.getRowPackedCopy();
                PowerFunctionCombination elementalFit = new PowerFunctionCombination(model, coeff);;

                double crit = elementalFit.trimmedSquares(data, support, residualsSqrt, c);

                if(crit<lowestCriterion)
                {
                    lowestCriterion = crit;
                    bestFit = elementalFit;
                }
            }		
        }	
        double[] residualsData = new double[t];
        for(int i = 0;i<t;i++)
        {
            residualsData[i] = bestFit.residual(allPoints[i]);
        }
        ResidualVector residuals = new ResidualVector(residualsData);
        SupportedLTS fit = new SupportedLTS(bestFit,residuals,lowestCriterion);
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

}