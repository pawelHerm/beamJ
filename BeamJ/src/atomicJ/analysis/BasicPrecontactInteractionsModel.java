package atomicJ.analysis;

import java.awt.geom.Point2D;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.statistics.LinearRegressionEsimator;
import atomicJ.utilities.MathUtilities;


public class BasicPrecontactInteractionsModel implements PrecontactInteractionsModel
{
    private final int p;
    private final int deg;
    private final boolean constant;

    public BasicPrecontactInteractionsModel(int degree, boolean constant)
    {
        this.deg = degree;
        this.constant = constant;
        this.p = deg + MathUtilities.boole(constant);
    }

    @Override
    public double getPrecontactObjectiveFunctionMinimum(double[][] forceSeparationData, Point2D recordingPoint, RegressionStrategy regressionStrategy) 
    {
        int n = forceSeparationData.length;

        if(n <= p)
        {
            return 0;
        }

        return regressionStrategy.getObjectiveFunctionMinimum(forceSeparationData, deg, constant);
    }

    @Override
    public UnivariateFunction getPrecontactFit(double[][] forceSeparationData, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        LinearRegressionEsimator reg = regressionStrategy.performRegression(forceSeparationData, deg, constant);
        return reg.getBestFit();
    }
}
