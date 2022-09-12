package atomicJ.analysis;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.ResidualsComparator;


public class TransitionPointEstimator 
{
    public static double[] getTransitionPoint(UnivariateFunction f, double[][] data, double k)
    {
        int n = data.length;

        double[][] dataCopy = Arrays.copyOf(data, n);

        Comparator<double[]> comparator = new ResidualsComparator(f);
        Arrays.sort(dataCopy, comparator);

        int medianIndex = (int)Math.rint(.5*n);    
        double[] medianResidualPoint = dataCopy[medianIndex];

        double limit = k*(Math.abs(medianResidualPoint[1] - f.value(medianResidualPoint[0])));       

        int index = ArrayUtilities.binarySearchAscendingResidual(dataCopy, medianIndex, n, limit, f);

        return ArrayUtilities.getPointWithMaximumX(Arrays.copyOfRange(dataCopy, 0, index));
    }
}
