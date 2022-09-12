
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

package atomicJ.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import atomicJ.statistics.FittedLinearUnivariateFunction;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.Selector;


public final class PowerFunctionCombination implements FittedLinearUnivariateFunction
{
    private Map<Double,Double> coefficients;
    private final double[] exponents;
    private final double[] factors;

    public PowerFunctionCombination(double[] exponents, double[] factors)
    {
        int k = exponents.length;
        int v = factors.length;
        if( k != v){throw new IllegalArgumentException();}

        this.exponents = exponents;
        this.factors = factors;              
    }

    @Override
    public PowerFunctionCombination multiply(double s)
    {
        return new PowerFunctionCombination(this.exponents, MathUtilities.multiply(this.factors, s));
    }

    @Override
    public FittedLinearUnivariateFunction getDerivative(int n)
    {
        if(n < 0)
        {
            throw new IllegalArgumentException("Negative derivative " + n);
        }

        if(n == 0)
        {
            return this;
        }

        int p = this.exponents.length;

        List<Double> expsDer = new ArrayList<>();
        List<Double> factorsDer = new ArrayList<>();

        for(int i = 0; i<p; i++)
        {
            double exp = exponents[i];
            double a = factors[i];

            boolean termDisappears = n > exp && MathUtilities.equalWithinTolerance(exp % 0, 0, 1e-15);
            if(!termDisappears)
            {
                double expDer = exp - n;
                double factorDer = a*MathUtilities.fallingFactorial(exp, n);

                expsDer.add(expDer);
                factorsDer.add(factorDer);
            }          
        }

        if(factorsDer.isEmpty())
        {
            return new Constant(0);
        }

        FittedLinearUnivariateFunction der = new PowerFunctionCombination(ArrayUtilities.getDoubleArray(expsDer), ArrayUtilities.getDoubleArray(factorsDer));
        return der;
    }

    public PowerFunctionCombination getHScaled(double h)
    {
        int n = exponents.length;

        double[] exponentsNew = Arrays.copyOf(exponents, n);
        double[] factorsNew = new double[n];

        for(int i = 0; i < n; i++)
        {
            factorsNew[i] = factors[i]/Math.pow(h, exponents[i]);
        }

        return new PowerFunctionCombination(exponentsNew, factorsNew);
    }

    @Override
    public double value(double x)
    {
        double y = 0;
        double a;
        double exp;
        for(int i = 0; i<exponents.length; i++)
        {
            exp = exponents[i];
            a = factors[i];
            y = y + a*Math.pow(x, exp);
        }
        return y;
    }

    @Override
    public double residual(double[] p)
    {
        double x = p[0];
        double y = p[1];
        double r = y - value(x);
        return r;
    }

    @Override
    public double residual(double x, double y)
    {
        double r = y - value(x);
        return r;
    }

    private void computeSqrt(double[][] points,double[] residualsSqrt)
    {
        int n = points.length - 1;
        for(int i = n;i>=0;i--)
        {
            double[] p = points[i];

            double r = residual(p);
            residualsSqrt[i] = r*r;
        }
    }

    private void computeSqrt(double[] yValues,double[] residualsSqrt)
    {
        int n = yValues.length - 1;
        for(int i = n;i>=0;i--)
        {
            double r = residual(i, yValues[i]);
            residualsSqrt[i] = r*r;
        }
    }

    public double trimmedSquares(double[][] points, double[] residualsSqrt, int c)
    {
        computeSqrt(points,residualsSqrt);
        Selector.sortSmallest(residualsSqrt, c);
        double crit = 0;
        for(int i = 0;i<c;i++)
        {
            crit = crit + residualsSqrt[i];
        }
        return crit;
    }       

    public double trimmedSquares(double[] yValues, double[] residualsSqrt, int c)
    {
        computeSqrt(yValues,residualsSqrt);
        Selector.sortSmallest(residualsSqrt, c);
        double crit = 0;
        for(int i = 0;i<c;i++)
        {
            crit = crit + residualsSqrt[i];
        }
        return crit;
    }       

    public double trimmedSquares(double[][] points, double[][] support, double[] residualsSqrt, int c)
    {
        computeSqrt(points,residualsSqrt);
        Selector.sortSmallest(residualsSqrt, c);
        double crit = 0;
        for(int i = 0;i<c;i++)
        {
            crit = crit + residualsSqrt[i];
        }
        int s = support.length;
        for(int i = 0;i<s;i++)
        {
            double[] p = support[i];
            double r = residual(p);
            crit = crit + r*r;
        }
        return crit;
    } 

    @Override
    public double getCoefficient(Number exp)
    {
        if(coefficients == null)
        {
            int k = exponents.length;

            coefficients = new HashMap<Double,Double>();
            for(int i = 0;i<k;i++)
            {
                Double ex = Double.valueOf(exponents[i]);
                Double coeff = Double.valueOf(factors[i]);
                coefficients.put(ex, coeff);
            }
        }

        Double value = coefficients.get(exp);
        double coeff = value != null ? value : 0;

        return coeff;
    }


    @Override
    public int getParameterCount()
    {
        return 2*exponents.length;
    }

    @Override
    public double[] getParameters()
    {
        return ArrayUtilities.join(factors, exponents);
    }
}