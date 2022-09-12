
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

import atomicJ.statistics.FittedLinearUnivariateFunction;
import atomicJ.utilities.Selector;

public final class Constant implements FittedLinearUnivariateFunction 
{
    private final double a;

    public Constant(double a)
    {
        this.a = a;
    }

    @Override
    public Constant multiply(double s)
    {
        return new Constant(a*s);
    }

    @Override
    public Constant getDerivative(int n)
    {
        if(n < 0)
        {
            throw new IllegalArgumentException("Negative derivative " + n);
        }

        Constant der = (n == 0) ? this : new Constant(0);
        return der;
    }

    public Constant getHScaled(double h)
    {
        return new Constant(this.a);
    }

    @Override
    public double value(double x)
    {
        return a;
    }

    @Override
    public double residual(double[] p)
    {		
        double y = p[1];
        double r = y - a;
        return r;
    }

    @Override
    public double residual(double x, double y)
    {       
        double r = y - a;
        return r;
    }

    @Override
    public double getCoefficient(Number n)
    {
        int i = n.intValue();
        if(i == 0)return a;
        else return 0;
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

    public double trimmedSquares(double[][] points,double[] residualsSqrt,int c)
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

    public double trimmedSquares(double[] yValues,double[] residualsSqrt,int c)
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

    public double trimmedSquares(double[][] points, double[][] support, double[] residualsSqrt,int c)
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

    public double trimmedWSquares(double[][] points, double[][] support, double[] residualsSqrt,int c)
    {
        computeSqrt(points,residualsSqrt);
        Selector.sortSmallest(residualsSqrt, c);
        double crit = 0;
        for(int i = 0;i<c;i++)
        {
            crit = crit + residualsSqrt[i];
        }


        int remnants = points.length - c;
        double windsorized = residualsSqrt[c - 1];
        crit = crit + remnants*windsorized;


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
    public int getParameterCount()
    {
        return 1;
    }

    @Override
    public double[] getParameters() 
    {
        return new double[] {a};
    }   
}

