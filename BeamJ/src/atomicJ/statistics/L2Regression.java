
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

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;

import atomicJ.functions.IntegerPowerFunction;
import atomicJ.functions.Line;
import atomicJ.functions.Polynomial;
import atomicJ.functions.PowerFunction;
import atomicJ.functions.PowerFunctionCombination;
import atomicJ.functions.Quadratic;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.RegressionUtilities;

import Jama.Matrix;
import Jama.QRDecomposition;


public class L2Regression implements LinearRegressionEsimator
{
    private final double lowestCriterion;
    private final double[][] data;
    private final ResidualVector residuals;
    private final FittedLinearUnivariateFunction bestFit;

    public static L2Regression findFit(double[][] data, int deg, boolean constant)
    {                
        int[] model = RegressionUtilities.getModelInt(deg, constant);   

        int n = data.length;
        int p = model.length;

        RegressionModel regModel = RegressionModel.getRegressionModel(data, model);
        double[][] design = regModel.getDesign(); 
        double[] obs = regModel.getObservations();

        double[][] designTransposed = MathUtilities.transpose(design, n, p);
        QRDecompositionCustomized decomposition = new QRDecompositionCustomized(MathUtilities.multipyByTranspose(designTransposed, p, n), p, p);
        double[] parametersMatrix = decomposition.solveWithColumn(MathUtilities.multiply3(designTransposed,p,n, obs));
        double[] predictedResponses = MathUtilities.multiply3(design, n, p, parametersMatrix);

        double[] coefficents = constant ? parametersMatrix : ArrayUtilities.padLeft(parametersMatrix, 0, 1);
        FittedLinearUnivariateFunction bestFit = new Polynomial(coefficents);

        //we do not need response matrix any more, so we call subtract() to perform in place operation
        ResidualVector residuals = new ResidualVector(MathUtilities.subtract(obs, predictedResponses));

        return new L2Regression(data, bestFit, residuals);
    };

    public static double findObjectiveFunctionMinimum(double[][] data, int deg, boolean constant)
    {            
        if(deg == 1 && !constant)
        {
            return findObjectiveFunctionMinimumForLinearFitThroughOrigin(data);
        }

        int n = data.length;
        int intercept = MathUtilities.boole(constant);
        int p =  deg + intercept; //Number of parameters

        double[] design = new double[n*p]; 
        double[] obs = new double[n];

        if(deg == 2 && !constant)
        {
            for(int i = 0;i<n;i++)
            {
                double[] pt = data[i];
                double x = pt[0];
                design[2*i] = x;
                design[2*i + 1] = x*x;
                obs[i] = pt[1];
            }
        }
        else if (deg == 3 && !constant)
        {
            for(int i = 0;i<n;i++)
            {
                double[] pt = data[i];
                double x = pt[0];
                int index = 3*i;
                double xSq = x*x;
                design[index] = x;
                design[index + 1] = xSq;
                design[index + 2] = xSq*x;
                obs[i] = pt[1];
            }
        }
        else
        {
            int[] model = RegressionUtilities.getModelInt(deg, constant);   

            for(int i = 0;i<n;i++)
            {
                double[] pt = data[i];
                double x = pt[0];
                int j = i*p;
                for(int exponent: model)
                {
                    design[j++] = MathUtilities.intPow(x,exponent);
                }
                obs[i] = pt[1];
            }
        }
        // D^T*D
        QRDecompositionConcatenatedArray decomposition = new QRDecompositionConcatenatedArray(MathUtilities.multipyTransposeOfConcatenatedMatrixCByCSymmetryUsedConcatenated(design, n, p), p, p);
        double[] parametersArray = decomposition.solveWithColumn(MathUtilities.multiplyTransposeOfConcatenatedMatrixAAndVector(design,n,p, obs));
        double[] predictedResponses = MathUtilities.multiplyConcatenatedMatrixAndVector(design, n, p, parametersArray);

        double ss = 0;

        for(int i = 0; i<n;i++)
        {
            double dx = obs[i] - predictedResponses[i];
            ss += dx*dx;
        }

        return ss;
    };


    //when we use linear or quadratic functions instead of polynomials, the performance improvement
    //is modest, although measurable (5 % for line fit correction of images)
    private static FittedLinearUnivariateFunction getPolynomialFunction(double[] parameters, int deg, boolean constant)
    {
        double[] coefficents;
        if(constant)
        {
            coefficents = parameters;

            if(deg == 1)
            {
                return new Line(coefficents[0], coefficents[1]);
            }
            if(deg == 2)
            {
                return new Quadratic(coefficents[0], coefficents[1], coefficents[2]); 
            }
        }
        else
        {
            coefficents = ArrayUtilities.padLeft(parameters, 0, 1);
        }

        return new Polynomial(coefficents);
    }

    public static UnivariateFunction[] performRegressionsOnEquispacedLines(double[][] dataSets, int columnCount, int deg) 
    {
        int n = dataSets.length;

        UnivariateFunction[] functions = new UnivariateFunction[n];

        double[][] coeffs = SavitzkyGolay.getCoefficients(0, columnCount - 1, deg);

        int p = deg + 1;

        for(int i = 0; i<n;i++)
        {
            double[] parameters = new double[p];

            double[] data = dataSets[i];

            for(int j = 0; j<p; j++)
            {
                double par = 0;
                double[] coeffsForDeg = coeffs[j];


                for(int k = 0; k<columnCount; k++)
                {
                    par += data[k]*coeffsForDeg[k];
                }

                parameters[j] = par;
            }

            functions[i] = getPolynomialFunction(parameters, deg, true);
        }

        return functions;
    }

    public static L2Regression findFitNative(double[][] data,int deg, boolean constant)
    {
        return findFitNative(data, RegressionUtilities.getModel(deg, constant));     
    };

    public static L2Regression findFit(double[][] data, double[] model)
    {        
        int n = data.length;
        int p =  model.length;

        RegressionModel regModel = RegressionModel.getRegressionModel(data, model);
        double[][] design = regModel.getDesign(); 
        double[] obs = regModel.getObservations();

        double[][] designTransposed = MathUtilities.transpose(design, n, p);
        QRDecompositionCustomized decomposition = new QRDecompositionCustomized(MathUtilities.multipyByTranspose(designTransposed, p, n), p, p);
        double[] parameters = decomposition.solveWithColumn(MathUtilities.multiply3(designTransposed,p,n, obs));
        double[] predictedResponses = MathUtilities.multiply3(design, n, p, parameters);

        FittedLinearUnivariateFunction bestFit = new PowerFunctionCombination(model, parameters);	

        //we do not need response matrix any more, so we call subtract() to perform in place operation
        ResidualVector residuals = new ResidualVector(MathUtilities.subtract(obs, predictedResponses));

        return new L2Regression(data, bestFit, residuals);
    };

    public static L2Regression findFit(double[][] data, int exponent)
    {
        int n = data.length;

        double[] designTransposed = new double[n]; 
        double[] obs = new double[n];
        double designTxDesign = 0;
        double designTxObs = 0;
        for(int i = 0;i<n;i++)
        {
            double[] pt = data[i];
            double ob = pt[1];
            obs[i] = ob;

            double xexp = MathUtilities.intPow(pt[0],exponent);
            designTransposed[i] = xexp;

            designTxDesign += xexp*xexp;
            designTxObs += xexp*ob;
        }

        double parameter = designTxObs/designTxDesign;
        double[] residuals = new double[n];
        for(int i = 0; i<n;i++)
        {
            residuals[i] = obs[i] - parameter*designTransposed[i];
        }

        FittedLinearUnivariateFunction bestFit = new IntegerPowerFunction(parameter, exponent);  
        ResidualVector residualVector = new ResidualVector(residuals);

        return new L2Regression(data, bestFit, residualVector);
    };



    public static L2Regression findFit(double[][] data, double exponent)
    {
        int n = data.length;

        double[] designTransposed = new double[n]; 
        double[] obs = new double[n];
        double designTxDesign = 0;
        double designTxObs = 0;
        for(int i = 0;i<n;i++)
        {
            double[] pt = data[i];
            double ob = pt[1];
            obs[i] = ob;

            double xexp = Math.pow(pt[0],exponent);
            designTransposed[i] = xexp;

            designTxDesign += xexp*xexp;
            designTxObs += xexp*ob;
        }

        double parameter = designTxObs/designTxDesign;
        double[] residuals = new double[n];
        for(int i = 0; i<n;i++)
        {
            residuals[i] = obs[i] - parameter*designTransposed[i];
        }

        FittedLinearUnivariateFunction bestFit = new PowerFunction(parameter, exponent);  
        ResidualVector residualVector = new ResidualVector(residuals);

        return new L2Regression(data, bestFit, residualVector);
    };

    public static double findObjectiveFunctionMinimumForLinearFitThroughOrigin(double[][] data)
    {
        int n = data.length;

        double designTxDesign = 0;
        double designTxObs = 0;
        for(int i = 0;i<n;i++)
        {
            double[] pt = data[i];
            double ob = pt[1];
            double x = pt[0];            
            designTxDesign += x*x;
            designTxObs += x*ob;
        }

        double parameter = designTxObs/designTxDesign;

        double ss = 0;
        for(int i = 0; i<n;i++)
        {
            double[] p = data[i];
            double dx = p[1] - parameter*p[0];
            ss += dx*dx;
        }

        return ss;
    };

    public static FittedLinearUnivariateFunction findFitedFunction(double[] data,int deg, boolean constant)
    {
        int[] model = RegressionUtilities.getModelInt(deg, constant);   

        int n = data.length;
        int p =  model.length;

        RegressionModel regModel = RegressionModel.getRegressionModel(data, model);
        double[][] design = regModel.getDesign(); 
        double[] obs = regModel.getObservations();


        double[][] designTransposed = MathUtilities.transpose(design, n, p);
        QRDecompositionCustomized decomposition = new QRDecompositionCustomized(MathUtilities.multipyByTranspose(designTransposed, p, n), p, p);
        double[] parametersMatrix = decomposition.solveWithColumn(MathUtilities.multiply3(designTransposed,p,n, obs));

        FittedLinearUnivariateFunction bestFit = getPolynomialFunction(parametersMatrix, deg, constant);  

        return bestFit;
    };

    public static FittedLinearUnivariateFunction findFitedFunction(double[][] data,int deg, boolean constant)
    {
        int[] model = RegressionUtilities.getModelInt(deg, constant);   

        int n = data.length;
        int p =  model.length;

        RegressionModel regModel = RegressionModel.getRegressionModel(data, model);
        double[][] design = regModel.getDesign(); 
        double[] obs = regModel.getObservations();

        double[][] designTransposed = MathUtilities.transpose(design, n, p);
        QRDecompositionCustomized decomposition = new QRDecompositionCustomized(MathUtilities.multipyByTranspose(designTransposed, p, n), p, p);
        double[] parameters = decomposition.solveWithColumn(MathUtilities.multiply3(designTransposed,p,n, obs));

        FittedLinearUnivariateFunction bestFit = getPolynomialFunction(parameters, deg, constant);    

        return bestFit;
    };

    public static UnivariateFunction findFitedFunction(double[] data, double[] model)
    {
        int n = data.length;
        int p =  model.length;

        RegressionModel regModel = RegressionModel.getRegressionModel(data, model);
        double[][] design = regModel.getDesign(); 
        double[] obs = regModel.getObservations();

        double[][] designTransposed = MathUtilities.transpose(design, n, p);
        QRDecompositionCustomized decomposition = new QRDecompositionCustomized(MathUtilities.multipyByTranspose(designTransposed, p, n), p, p);
        double[] parameters = decomposition.solveWithColumn(MathUtilities.multiply3(designTransposed,p,n, obs));

        FittedLinearUnivariateFunction bestFit = new PowerFunctionCombination(model, parameters);   

        return bestFit;
    };


    public static UnivariateFunction findFitedFunction(double[][] data, double[] weights, double[] model)
    {
        int n = data.length;
        int p =  model.length;

        RegressionModel regModel = RegressionModel.getRegressionModel(data, weights, model);

        double[][] weightedDesign = regModel.getWeightedDesign();
        double[] wobs = regModel.getWeightedObservations();

        Matrix weightedDesignMatrix = new Matrix(weightedDesign, n, p);
        Matrix weightedDesignTransposed = weightedDesignMatrix.transpose();

        QRDecomposition decomposition = new QRDecomposition(MathUtilities.multipyByTranspose(weightedDesignTransposed));
        Matrix parametersMatrix = decomposition.solve(MathUtilities.multiply(weightedDesignTransposed, wobs));

        FittedLinearUnivariateFunction bestFit = new PowerFunctionCombination(model, parametersMatrix.getRowPackedCopy());   

        return bestFit;
    };

    public static FittedLinearUnivariateFunction findFitedFunction(double[][] data, int from, int to, double[] weights, int degree)
    {        
        int n = to - from;
        int p = degree + 1;

        RegressionModel regModel = RegressionModel.getWeightedDesignAndWeightedObservations(data, from, to, weights, degree);

        double[][] weightedDesign = regModel.getWeightedDesign();
        double[] wobs = regModel.getWeightedObservations();

        return getFittedFunction(weightedDesign, wobs, degree, n, p);
    };

    public static FittedLinearUnivariateFunction findFitedFunction(double[] data, int from, int to, double[] weights, int degree)
    {
        int n = to - from;
        int p =  degree + 1;

        RegressionModel regModel = RegressionModel.getWeightedDesignAndWeightedObservations(data, from, to, weights, degree);

        double[][] weightedDesign = regModel.getWeightedDesign();
        double[] wobs = regModel.getWeightedObservations();

        return getFittedFunction(weightedDesign, wobs, degree, n, p);
    };

    private static FittedLinearUnivariateFunction getFittedFunction(double[][] weightedDesign, double[] wobs, int degree, int n, int p)
    {                    
        double[][] weightedDesignTransposed = MathUtilities.transpose(weightedDesign, n, p);
        QRDecompositionCustomized decomposition = new QRDecompositionCustomized(MathUtilities.multipyByTranspose(weightedDesignTransposed, p, n), p, p);
        double[] parameters = decomposition.solveWithColumn(MathUtilities.multiply3(weightedDesignTransposed,p,n, wobs));

        FittedLinearUnivariateFunction bestFit = getPolynomialFunction(parameters, degree, true);    

        return bestFit;
    }

    public static L2Regression findFitNative(double[][] data, double[] model)
    {
        RegressionModel regModel = RegressionModel.getRegressionModel(data, model);
        double[][] design = regModel.getDesign(); 
        double[] obs = regModel.getObservations();

        DoubleMatrix designMatrix = new DoubleMatrix(design);
        DoubleMatrix responseMatrix = new DoubleMatrix(obs);

        DoubleMatrix parametersMatrix = Solve.solveLeastSquares(designMatrix, responseMatrix);
        DoubleMatrix predictedResponses = designMatrix.mmul(parametersMatrix);
        FittedLinearUnivariateFunction bestFit = new PowerFunctionCombination(model,parametersMatrix.toArray());   
        ResidualVector residuals = new ResidualVector(responseMatrix.sub(predictedResponses).toArray());

        return new L2Regression(data, bestFit, residuals);

    };

    public static L2Regression findFitNative(double[][] data, double[] wieghts, double[] model)
    {      
        RegressionModel regModel = RegressionModel.getRegressionModel(data, wieghts, model);

        double[][] design = regModel.getDesign(); 
        double[][] weightedDesign = regModel.getWeightedDesign();
        double[] obs = regModel.getObservations();
        double[] wobs = regModel.getWeightedObservations();

        DoubleMatrix designMatrix = new DoubleMatrix(design);
        DoubleMatrix weightedDesignMatrix = new DoubleMatrix(weightedDesign);

        DoubleMatrix responseMatrix = new DoubleMatrix(obs);
        DoubleMatrix weightedResponseMatrix = new DoubleMatrix(wobs);

        DoubleMatrix parametersMatrix = Solve.solveLeastSquares(weightedDesignMatrix, weightedResponseMatrix);
        DoubleMatrix predictedResponses = designMatrix.mmul(parametersMatrix);

        FittedLinearUnivariateFunction bestFit = new PowerFunctionCombination(model,parametersMatrix.toArray());   
        ResidualVector residuals = new ResidualVector(responseMatrix.sub(predictedResponses).toArray());

        return new L2Regression(data, bestFit, residuals);
    };

    public static double[] findParameters(double[][] design, double[] obs)
    {
        int n = obs.length;
        int p =  design[0].length;

        double[][] designTransposed = MathUtilities.transpose(design, n, p);
        QRDecompositionCustomized decomposition = new QRDecompositionCustomized(MathUtilities.multipyByTranspose(designTransposed, p, n), p, p);
        double[] parameters = decomposition.solveWithColumn(MathUtilities.multiply3(designTransposed,p,n, obs));

        return parameters;   
    };

    public static L2Regression getL2RegressionForFittedFunction(double[][] data, FittedLinearUnivariateFunction function)
    {      
        int n = data.length;

        double[] residualsData = new double[n];
        for(int i = 0;i<n;i++)
        {
            residualsData[i] = function.residual(data[i]);
        }

        ResidualVector residuals = new ResidualVector(residualsData);

        L2Regression l2Regression = new L2Regression(data, function, residuals);

        return l2Regression;
    }

    public static double getObjectiveFunctionValue(double[][] data, UnivariateFunction f)
    {
        int n = data.length;

        double objective = 0;

        for(int i = 0;i<n;i++)
        {
            double[] p = data[i];
            double x = p[0];
            double y = p[1];

            double r = f.value(x) - y;
            objective += r*r;
        }

        return objective;
    }

    private L2Regression(double[][] data, FittedLinearUnivariateFunction bestFit, ResidualVector residuals)
    {
        this.data = data;
        this.bestFit = bestFit;	
        this.residuals = residuals;
        this.lowestCriterion = residuals.getSquaresSum();
    }

    @Override
    public FittedLinearUnivariateFunction getBestFit()
    {
        return bestFit;
    }

    @Override
    public ResidualVector getResiduals()
    {
        return residuals;
    }

    @Override
    public double getObjectiveFunctionMinimum()
    {
        return lowestCriterion;
    }

    public double[][] getData()
    {
        return data;
    }
}
