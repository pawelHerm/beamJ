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
import atomicJ.statistics.HighCoverageLTA;
import atomicJ.statistics.HighCoverageLTS;
import atomicJ.statistics.L1Regression;
import atomicJ.statistics.L2Regression;
import atomicJ.statistics.LTA;
import atomicJ.statistics.LTS;
import atomicJ.statistics.LinearRegressionEsimator;
import atomicJ.utilities.ArrayUtilities;


public enum BasicRegressionStrategy implements RegressionStrategy
{
    ROBUST_LTS("Robust (LTS)")
    {
        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int degree, boolean constant)
        {
            return LTS.findFit(data, degree, constant, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int degree, boolean constant)
        {
            return LTS.findFit(data, degree, constant, 200).getObjectiveFunctionMinimum();
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double[] model)
        {
            return LTS.findFit(data, model, 200);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int exponent)
        {
            return LTS.findFit(data, new double[] {exponent}, 200);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double exponent)
        {
            return LTS.findFit(data, new double[] {exponent}, 200);
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[][] data)
        {
            return LTS.findFit(data, new double[] {1}, 200).getObjectiveFunctionMinimum();
        }

        @Override
        public double[] getLastCoveredPoint(double[][] data, UnivariateFunction f) 
        {
            return TransitionPointEstimator.getTransitionPoint(f, data, 2.5);
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, ParametrizedUnivariateFunction function) 
        {
            return LTS.getObjectiveFunctionValue(data, function);
        }
    },

    ROBUST_HLTS("Robust (HLTS)")
    {
        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int degree, boolean constant)
        {
            return HighCoverageLTS.findFit(data, degree, constant, 2, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int degree, boolean constant)
        {
            return HighCoverageLTS.findFit(data, degree, constant, 2, 200).getObjectiveFunctionMinimum();
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double[] model)
        {
            return HighCoverageLTS.findFit(data, model, 2, 200);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int exponent)
        {
            return HighCoverageLTS.findFit(data, new double[] {exponent}, 2, 200);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double exponent)
        {
            return HighCoverageLTS.findFit(data, new double[] {exponent}, 2, 200);
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[][] data)
        {
            return HighCoverageLTS.findFit(data, new double[] {1}, 2, 200).getObjectiveFunctionMinimum();
        }

        @Override
        public double[] getLastCoveredPoint(double[][] data, UnivariateFunction f) 
        {
            return TransitionPointEstimator.getTransitionPoint(f, data, 2.5);

        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, ParametrizedUnivariateFunction function) 
        {
            return HighCoverageLTS.getObjectiveFunctionValue(data, function, 2);
        }
    },

    ROBUST_LTA("Robust (LTA)")
    {
        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int degree, boolean constant)
        {
            return LTA.findFit(ArrayUtilities.deepCopy(data), degree, constant, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int degree, boolean constant)
        {
            return LTA.findFit(ArrayUtilities.deepCopy(data), degree, constant, 200).getObjectiveFunctionMinimum();
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double[] model)
        {
            return LTA.findFit(ArrayUtilities.deepCopy(data), model, 200);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int exponent)
        {
            return LTA.findFit(ArrayUtilities.deepCopy(data), new double[] {exponent}, 200);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double exponent)
        {
            return LTA.findFit(ArrayUtilities.deepCopy(data), new double[] {exponent}, 200);
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[][] data)
        {
            return LTA.findFit(ArrayUtilities.deepCopy(data), new double[] {1}, 200).getObjectiveFunctionMinimum();
        }

        @Override
        public double[] getLastCoveredPoint(double[][] data, UnivariateFunction f) 
        {
            return TransitionPointEstimator.getTransitionPoint(f, data, 2.5);
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, ParametrizedUnivariateFunction function) 
        {
            return LTA.getObjectiveFunctionValue(data, function);
        }
    },

    ROBUST_HLTA("Robust (HLTA)")
    {
        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int degree, boolean constant)
        {
            return HighCoverageLTA.findFit(ArrayUtilities.deepCopy(data), degree, constant, 2, 200);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int degree, boolean constant)
        {
            return HighCoverageLTA.findFit(ArrayUtilities.deepCopy(data), degree, constant, 2, 200).getObjectiveFunctionMinimum();
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double[] model)
        {
            return HighCoverageLTA.findFit(ArrayUtilities.deepCopy(data), model, 2, 200);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int exponent)
        {
            return HighCoverageLTA.findFit(ArrayUtilities.deepCopy(data), new double[] {exponent}, 2, 200);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double exponent)
        {
            return HighCoverageLTA.findFit(ArrayUtilities.deepCopy(data), new double[] {exponent}, 2, 200);
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[][] data)
        {
            return HighCoverageLTA.findFit(ArrayUtilities.deepCopy(data), new double[] {1}, 2, 200).getObjectiveFunctionMinimum();
        }

        @Override
        public double[] getLastCoveredPoint(double[][] data, UnivariateFunction f) 
        {
            return TransitionPointEstimator.getTransitionPoint(f, data, 2.5);
        }

        @Override
        public double getObjectiveFunctionValue(double[][] data, ParametrizedUnivariateFunction function) 
        {
            return HighCoverageLTA.getObjectiveFunctionValue(data, function, 2);
        }
    },

    CLASSICAL_L2("Classical (L2)")
    {	
        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int degree, boolean constant)
        {
            return L2Regression.findFit(data, degree, constant);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int degree, boolean constant)
        {
            return L2Regression.findObjectiveFunctionMinimum(data, degree, constant);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double[] model)
        {
            return L2Regression.findFit(data, model);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int exponent)
        {
            return L2Regression.findFit(data, exponent);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double exponent)
        {
            return L2Regression.findFit(data, exponent);
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[][] data)
        {
            return L2Regression.findObjectiveFunctionMinimumForLinearFitThroughOrigin(data);
        }

        @Override
        public double[] getLastCoveredPoint(double[][] data, UnivariateFunction f) 
        {			
            return data[data.length - 1];
        }	

        @Override
        public double getObjectiveFunctionValue(double[][] data, ParametrizedUnivariateFunction function) 
        {
            return L2Regression.getObjectiveFunctionValue(data, function);
        }
    },

    CLASSICAL_L1("Classical (L1)")
    {	
        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int degree, boolean constant)
        {
            return L1Regression.findFit(data, degree, constant);
        }

        @Override
        public double getObjectiveFunctionMinimum(double[][] data, int degree, boolean constant)
        {
            return L1Regression.findFit(data, degree, constant).getObjectiveFunctionMinimum();
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double[] model)
        {
            return L1Regression.findFit(data, model);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, int exponent)
        {
            return L1Regression.findFit(data, exponent);
        }

        @Override
        public LinearRegressionEsimator performRegression(double[][] data, double exponent)
        {
            return L1Regression.findFit(data, new double[] {exponent});
        }

        @Override
        public double getObjectiveFunctionMinimumForThroughOriginLinearRegression(double[][] data)
        {
            return L1Regression.findFit(data, new double[] {1}).getObjectiveFunctionMinimum();
        }

        @Override
        public double[] getLastCoveredPoint(double[][] data, UnivariateFunction f) 
        {			
            return data[data.length - 1];
        }	

        @Override
        public double getObjectiveFunctionValue(double[][] data, ParametrizedUnivariateFunction function) 
        {
            return L1Regression.getObjectiveFunctionValue(data, function);
        }
    };

    private final String name;

    BasicRegressionStrategy(String name)
    {
        this.name = name;
    }	

    public static BasicRegressionStrategy getValue(String identifier)
    {
        return getValue(identifier, null);
    }

    public static BasicRegressionStrategy getValue(String identifier, BasicRegressionStrategy fallBackValue)
    {
        BasicRegressionStrategy strategy = fallBackValue;

        if(identifier != null)
        {
            for(BasicRegressionStrategy str : BasicRegressionStrategy.values())
            {
                String estIdentifier =  str.getIdentifier();
                if(estIdentifier.equals(identifier))
                {
                    strategy = str;
                    break;
                }
            }
        }

        return strategy;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public String getIdentifier()
    {
        return name();
    }
}
