
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

package atomicJ.analysis;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.functions.ParametrizedUnivariateFunction;
import atomicJ.statistics.LinearRegressionEsimator;


public interface RegressionStrategy 
{
    public LinearRegressionEsimator performRegression(double[][] data, int degree, boolean constant);
    public double getObjectiveFunctionMinimum(double[][] data, int degree, boolean constant);
    public LinearRegressionEsimator performRegression(double[][] data, double[] model);
    public LinearRegressionEsimator performRegression(double[][] data, double exponent);
    public LinearRegressionEsimator performRegression(double[][] data, int exponent);
    public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[][] data);
    public double getObjectiveFunctionValue(double[][] data, ParametrizedUnivariateFunction function);
    public double[] getLastCoveredPoint(double[][] data, UnivariateFunction f);
}
