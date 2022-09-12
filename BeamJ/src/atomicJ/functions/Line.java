
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

public final class Line implements FittedLinearUnivariateFunction 
{
    private final double a;
    private final double b;

    public Line(double a, double b)
    {
        this.a = a;
        this.b = b;
    }

    @Override
    public Line multiply(double s)
    {
        return new Line(s*a, s*b);
    }

    @Override
    public FittedLinearUnivariateFunction getDerivative(int deg)
    {
        if(deg < 0)
        {
            throw new IllegalArgumentException("Negative derivative " + deg);
        }

        if(deg == 0)
        {
            return this;
        }

        FittedLinearUnivariateFunction function = deg > 1 ? new Constant(0) : new Constant(b);
        return function;
    }

    public Line getHScaled(double h)
    {
        return new Line(this.a/h, this.b);
    }

    public Line getHScaled(double h, double origin)
    {
        double aNew = this.a/h;
        double bNew = this.b - aNew*origin;

        return new Line(aNew, bNew);
    }

    @Override
    public double value(double x)
    {
        double y = a + b*x;					
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

    @Override
    public double getCoefficient(Number n)
    {
        int i = n.intValue();
        if(i == 0)return a;
        else if(i == 1) return b;
        else return 0;
    }

    private void computeSqrt(double[][] points, double[] residualsSqrt)
    {
        int n = points.length - 1;
        for(int i = n; i>=0; i--)
        {
            double[] p = points[i];

            double r = residual(p);
            residualsSqrt[i] = r*r;
        }
    }

    private void computeSqrt(double[] yValues, double[] residualsSqrt)
    {
        int n = yValues.length - 1;
        for(int i = n; i>=0; i--)
        {
            double r = residual(i, yValues[i]);
            residualsSqrt[i] = r*r;
        }
    }


    public double trimmedSquares(double[][] points,double[] residualsSqrt, int c)
    {
        computeSqrt(points,residualsSqrt);
        Selector.sortSmallest(residualsSqrt, c);
        //        FloydRivest.select(residualsSqrt, c);
        double crit = total(residualsSqrt, c);

        return crit;
    }

    public double trimmedSquares(double[] yValues,double[] residualsSqrt, int c)
    {
        computeSqrt(yValues,residualsSqrt);
        Selector.sortSmallest(residualsSqrt, c);
        double crit = total(residualsSqrt, c);

        return crit;
    }

    public double trimmedSquares(double[][] points, double[][] support, double[] residualsSqrt,int c)
    {        
        computeSqrt(points,residualsSqrt);
        Selector.sortSmallest(residualsSqrt, c);
        double crit = total(residualsSqrt, c);

        int s = support.length;
        for(int i = 0;i<s;i++)
        {
            double[] p = support[i];
            double r = residual(p);
            crit += r*r;
        }
        return crit;
    }	

    public double trimmedWSquares(double[][] points, double[][] support, double[] residualsSqrt,int c)
    {
        computeSqrt(points,residualsSqrt);
        Selector.sortSmallest(residualsSqrt, c);
        double crit = total(residualsSqrt, c);

        int remnants = points.length - c;
        double windsorized = residualsSqrt[c - 1];
        crit = crit + remnants*windsorized;

        int s = support.length;
        for(int i = 0;i<s;i++)
        {
            double[] p = support[i];
            double r = residual(p);
            crit += r*r;
        }
        return crit;
    }   

    @Override
    public int getParameterCount()
    {
        return 2;
    }

    @Override
    public double[] getParameters() 
    {
        return new double[] {a, b};
    }   

    private static double total(double[] array, int c)
    {
        double total = 0;
        for(int i = 0;i<c;i++)
        {
            total += array[i];
        }
        return total;
    }

}

