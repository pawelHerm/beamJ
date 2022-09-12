package atomicJ.analysis;

import java.awt.geom.Point2D;

import org.apache.commons.math3.analysis.UnivariateFunction;

public interface PrecontactInteractionsModel 
{
    public double getPrecontactObjectiveFunctionMinimum(double[][] forceSeparationData, Point2D recordingPoint, RegressionStrategy regressionStrategy);
    public UnivariateFunction getPrecontactFit(double[][] forceSeparationData, Point2D recordingPoint, RegressionStrategy regressionStrategy);
}
