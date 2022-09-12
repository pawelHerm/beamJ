package atomicJ.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;

public class ZeroFunction implements UnivariateFunction
{
    private static final ZeroFunction instance = new ZeroFunction();

    private ZeroFunction()
    {}

    public static ZeroFunction getInstance()
    {
        return instance;
    }

    @Override
    public double value(double x) {
        return 0;
    }

}
