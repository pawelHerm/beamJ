
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Pawe� Hermanowicz
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


import org.apache.commons.math.distribution.*;

import java.io.Serializable;

import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;

public class LaplaceDistribution extends AbstractContinuousDistribution implements Serializable {

    public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1e-9;

    private static final long serialVersionUID = 1L;

    private double location = 0;

    private double scale = 1;

    private final double solverAbsoluteAccuracy;

    public LaplaceDistribution(double location, double scale){
        this(location, scale, DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }


    public LaplaceDistribution(double location, double scale, double inverseCumAccuracy) {
        super();
        setLocationParameterInternal(location);
        setScaleParameterInternal(scale);
        solverAbsoluteAccuracy = inverseCumAccuracy;
    }

    public LaplaceDistribution(){
        this(0.0, 1.0);
    }

    public double getLocationParameter() {
        return location;
    }

    private void setLocationParameterInternal(double location) {
        this.location = location;
    }

    public double getScaleParameter() {
        return scale;
    }

    private void setScaleParameterInternal(double scale) {
        if (scale <= 0.0) 
        {
            throw MathRuntimeException.createIllegalArgumentException(
                    "standard deviation must be positive ({0})",
                    scale);
        }
        this.scale = scale;
    }

    @Override
    public double density(double x) {
        double x0 = Math.abs(x -location);
        double density =  Math.exp(-x0 /scale) / (2*scale);
        return density;
    }


    @Override
    public double cumulativeProbability(double x) throws MathException 
    {
        double cdf = (x<location) ?  0.5*Math.exp((x - location)/scale) : (1 -  0.5*Math.exp((location - x)/scale));
        return cdf;
    }


    @Override
    protected double getSolverAbsoluteAccuracy() 
    {
        return solverAbsoluteAccuracy;
    }

    /**
     * For this distribution, X, this method returns the critical point x, such
     * that P(X < x) = <code>p.
     * <p>
     * Returns <code>Double.NEGATIVE_INFINITY for p=0 and
     * <code>Double.POSITIVE_INFINITY for p=1.


     *
     * @param p the desired probability
     * @return x, such that P(X < x) = <code>p
     * @throws MathException if the inverse cumulative probability can not be
     *         computed due to convergence or other numerical errors.
     * @throws IllegalArgumentException if <code>p is not a valid
     *         probability.
     */
    @Override
    public double inverseCumulativeProbability(final double p)
            throws MathException {
        if (p == 0) 
        {
            return Double.NEGATIVE_INFINITY;
        }
        if (p == 1) 
        {
            return Double.POSITIVE_INFINITY;
        }
        return super.inverseCumulativeProbability(p);
    }

    /**
     * Access the domain value lower bound, based on <code>p, used to
     * bracket a CDF root.  This method is used by
     * {@link #inverseCumulativeProbability(double)} to find critical values.
     *
     * @param p the desired probability for the critical value
     * @return domain value lower bound, i.e.
     *         P(X < <i>lower bound) < p
     */
    @Override
    protected double getDomainLowerBound(double p) 
    {
        double ret;

        if (p < .5) 
        {
            ret = 0;
        } 
        else 
        {
            ret = Math.exp(location);
        }

        return ret;
    }

    /**
     * Access the domain value upper bound, based on <code>p, used to
     * bracket a CDF root.  This method is used by
     * {@link #inverseCumulativeProbability(double)} to find critical values.
     *
     * @param p the desired probability for the critical value
     * @return domain value upper bound, i.e.
     *         P(X < <i>upper bound) > p
     */
    @Override
    protected double getDomainUpperBound(double p) 
    {
        double ret;

        if (p < .5) 
        {
            ret = Math.exp(location);
        } 
        else 
        {
            ret = Double.MAX_VALUE;
        }

        return ret;
    }

    /**
     * Access the initial domain value, based on <code>p, used to
     * bracket a CDF root.  This method is used by
     * {@link #inverseCumulativeProbability(double)} to find critical values.
     *
     * @param p the desired probability for the critical value
     * @return initial domain value
     */
    @Override
    protected double getInitialDomain(double p) {
        double ret;

        if (p < .5) 
        {
            ret = Math.exp(location - scale);
        } else if (p > .5) 
        {
            ret = Math.exp(location + scale);
        } else 
        {
            ret = Math.exp(location);
        }

        return ret;
    }
}
