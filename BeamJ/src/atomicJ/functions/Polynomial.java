
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

import java.util.Arrays;

import atomicJ.statistics.FittedLinearUnivariateFunction;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.Selector;



public final class Polynomial implements FittedLinearUnivariateFunction
{
    private final double[] coeff;
    private final int deg;

    public Polynomial(double[] coeff)
    {
        this.coeff = coeff;		
        this.deg = coeff.length - 1;
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

        if(n > this.deg)
        {
            return new Constant(0);
        }

        int p = coeff.length;

        int pNew = p - n;

        double[] coeffNew = new double[pNew];

        for(int i = n; i < p; i++)
        {
            coeffNew[i - n] = coeff[i]*MathUtilities.fallingFactorial(i, n);
        }

        return new Polynomial(coeffNew);
    }

    public Polynomial getHScaled(double h)
    {
        int n = coeff.length;
        double[] coeffNew = new double[n];

        for(int i = 0; i<n; i++)
        {
            coeffNew[i] = coeff[i]/MathUtilities.intPow(h, i);
        }

        return new Polynomial(coeffNew);
    }

    @Override
    public Polynomial multiply(double s)
    {       
        return new Polynomial(MathUtilities.multiply(coeff, s));
    }

    public int degree()
    {
        return deg;
    }

    public double getCoefficient(int k)
    {
        double c = k > deg ? 0 : coeff[k];       
        return c;
    }

    public double[] getCoefficients()
    {
        return Arrays.copyOf(coeff, deg + 1);
    }

    @Override
    public double getCoefficient(Number n)
    {
        int c = n.intValue();
        if(c<0||c>deg)
        {
            return 0;
        }
        else
        {
            return coeff[c];
        }
    }

    /**Uses Horner's method to compute the value of a polynomial function for a given argument*/
    @Override
    public double value(double x)
    {
        double y = 0;
        for(int i = deg;i>=0;i--)
        {
            y = x*y + coeff[i];
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


    /**Multiplies the polynomial by a scalar*/
    public Polynomial times(double c)
    {
        int k = this.deg;
        double[] coefResult = new double[k + 1];
        for(int i = 0;i<=k;i++)
        {
            coefResult[i] = c*coeff[i];
        }
        Polynomial result = new Polynomial(coefResult);
        return result;
    }

    public Polynomial[] divideWithRemainder(Polynomial p2)
    {
        int n = this.deg;
        int m = p2.degree();
        int k = n - m;

        double[] coefDivider = p2.getCoefficients();
        double[] coefQuotient = new double[k + 1];
        double[] coefRemainder = this.getCoefficients();

        for(int i = k;i>=0;i--)
        {
            double q = coefRemainder[i + n]/coefDivider[n];
            coefQuotient[i] = q;
            for(int j = n;j>=0;j--)
            {
                coefRemainder[i+j] = coefRemainder[i+j] - q*coefDivider[j];
            }
        }
        Polynomial quotient = new Polynomial(coefQuotient);
        Polynomial remiander = new Polynomial(coefRemainder);
        Polynomial[] result = {quotient, remiander};
        return result;
    }

    public Polynomial times(Polynomial p2)
    {
        int n = this.deg;
        int m = p2.degree();
        int k = n*m;

        double[] coef1 = this.coeff;
        double[] coef2 = p2.getCoefficients();
        double[] coefResult = new double[k + 1];

        for(int i = 0;i<=n;i++)
        {
            for(int j = 0;j<=m;j++)
            {
                coefResult[i + j] = coefResult[i + j] + coef1[i]*coef2[j];
            }
        }
        Polynomial result = new Polynomial(coefResult);
        return result;		
    }


    public Polynomial plus(Polynomial p2)
    {
        int m = this.deg;
        int n = p2.degree();
        int k = Math.max(m,n);

        double[] coef1 = Arrays.copyOf(coeff, k);
        double[] coef2 = Arrays.copyOf(p2.getCoefficients(),k);
        double[] coefResult = new double[k + 1];

        for(int i = 0;i<=k;i++)
        {
            coefResult[i] = coef1[i] + coef2[i];
        }
        Polynomial result = new Polynomial(coefResult);
        return result;
    }

    public Polynomial minus(Polynomial p2)
    {
        Polynomial result = plus(p2.times(-1));
        return result;
    }

    public static double value(double x, double[] coeff, int deg)
    {
        double y = 0;
        for(int i = deg;i>=0;i--)
        {
            y = x*y + coeff[i];
        }
        return y;
    }

    public static double residual(double[] p, double[] coeff, int deg)
    {	
        double x = p[0];
        double y = p[1];
        double r = y - value(x, coeff, deg);
        return r;
    }

    public static double residual(double x, double y, double[] coeff, int deg)
    {   
        double r = y - value(x, coeff, deg);
        return r;
    }

    public static double trimmedSquares(double[][] points,double[] coeff,int deg, double[] residualsSqrt,int c)
    {
        int n = points.length - 1;
        for(int i = n;i>=0;i--)
        {
            double[] p = points[i];

            double r = residual(p, coeff, deg);
            residualsSqrt[i] = r*r;
        }
        Selector.sortSmallest(residualsSqrt,c);
        double crit = 0;
        for(int i = 0;i<c;i++)
        {
            crit = crit + residualsSqrt[i];
        }
        return crit;
    }

    public static double trimmedSquares(double[] yValues, double[] coeff,int deg, double[] residualsSqrt,int c)
    {
        int n = yValues.length - 1;
        for(int i = n;i>=0;i--)
        {
            double r = residual(i, yValues[i], coeff, deg);
            residualsSqrt[i] = r*r;
        }

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
        int n = points.length - 1;
        for(int i = n;i>=0;i--)
        {
            double[] p = points[i];

            double r = residual(p, coeff, deg);
            residualsSqrt[i] = r*r;
        }
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
        int n = points.length - 1;
        for(int i = n;i>=0;i--)
        {
            double[] p = points[i];

            double r = residual(p, coeff, deg);
            residualsSqrt[i] = r*r;
        }
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
        return coeff.length;
    }


    @Override
    public double[] getParameters() 
    {
        return Arrays.copyOf(coeff, coeff.length);
    } 

    public static double trimmedWSquares(double[][] points, double[] coeff,
            int deg, double[] residualsSqrt, int c) {
        int n = points.length - 1;
        for(int i = n;i>=0;i--)
        {
            double[] p = points[i];

            double r = residual(p, coeff, deg);
            residualsSqrt[i] = r*r;
        }
        Selector.sortSmallest(residualsSqrt,c);
        double crit = 0;
        for(int i = 0;i<c;i++)
        {
            crit = crit + residualsSqrt[i];
        }
        int remnants = points.length - c;
        double windsorized = residualsSqrt[c - 1];
        crit = crit + remnants*windsorized;



        return crit;       
    }

    public boolean isZero()
    {
        for(double c : coeff)
        {
            if(c != 0)
            {
                return false;
            }
        }

        return true;
    }
}
