package atomicJ.data;

import org.jfree.data.Range;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.units.Quantity;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.OrderedIntegerPair;
import atomicJ.utilities.Validation;

public class FlexibleChannel1DData implements Channel1DData
{
    private final double[][] points;
    private final SortedArrayOrder xOrder;
    private final Quantity xQuantity;
    private final Quantity yQuantity;

    public FlexibleChannel1DData(double[][] points, Quantity xQuantity, Quantity yQuantity, SortedArrayOrder order)
    {
        this.points = points;
        this.xOrder = order;
        this.xQuantity = xQuantity;
        this.yQuantity = yQuantity;       
    }

    public FlexibleChannel1DData(FlexibleChannel1DData that)
    {
        this.points = ArrayUtilities.deepCopy(that.points);
        this.xOrder = that.xOrder;
        this.xQuantity = that.xQuantity;
        this.yQuantity = that.yQuantity;
    }

    public static FlexibleChannel1DData getInstance(double[] xValues, double[] yValues, Quantity xQuantity, Quantity yQuantity, SortedArrayOrder desiredOrder)
    {
        if(xValues.length != yValues.length)
        {
            throw new IllegalArgumentException("The lists xValues and yValues should be of equal length");
        }

        int n = xValues.length;

        double[][] points = new double[n][];
        for(int i = 0; i<n;i++)
        {
            points[i] = new double[] {xValues[i], yValues[i]};
        }

        points = desiredOrder.sortX(points);

        return new FlexibleChannel1DData(points, xQuantity, yQuantity, desiredOrder);
    }

    public static FlexibleChannel1DData getEmptyInstance(Quantity xQuantity, Quantity yQuantity)
    {
        return new FlexibleChannel1DData(new double[][] {}, xQuantity, yQuantity, null);
    }


    public static FlexibleChannel1DData getEmptyInstance(Channel1DData channel)
    {
        return new FlexibleChannel1DData(new double[][] {}, channel.getXQuantity(), channel.getYQuantity(), null);
    }

    @Override
    public Quantity getXQuantity()
    {
        return xQuantity;
    }

    @Override
    public Quantity getYQuantity()
    {
        return yQuantity;
    }

    @Override
    public Channel1DData getCopy()
    {
        return new FlexibleChannel1DData(this);
    }

    @Override
    public Channel1DData getCopy(double scale)
    {
        return getCopy(scale, this.yQuantity);
    }

    @Override
    public Channel1DData getCopy(double scale, Quantity yQuantityNew)
    {
        int n = points.length;
        double[][] pointsCopy = new double[n][];

        for(int i = 0; i<n;i++)
        {
            double[] p = points[i];
            pointsCopy[i] = new double[] {p[0], scale*p[1]};
        }

        Channel1DData copy = new FlexibleChannel1DData(pointsCopy, this.xQuantity, yQuantityNew, this.xOrder);

        return copy;
    }

    @Override
    public int getIndexWithinDataBoundsOfItemWithXClosestTo(double x)
    {
        if(xOrder == null)
        {
            return ArrayUtilities.getIndexOfPointWithXCoordinateClosestTo(points, x);
        }

        int n = points.length;

        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {
            int greaterOrEqualXIndex = Math.min(n - 1, ArrayUtilities.binarySearchAscendingX(points, 0, points.length, x));
            int closestXIndex = greaterOrEqualXIndex - 1 >= 0 && Math.abs(points[greaterOrEqualXIndex][0] - x) < Math.abs(points[greaterOrEqualXIndex - 1][0] - x) ? greaterOrEqualXIndex: greaterOrEqualXIndex - 1;

            return closestXIndex;
        }
        else if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            int smallerOrEqualXIndex = Math.min(n - 1, ArrayUtilities.binarySearchDescendingX(points, 0, points.length, x));
            int closestXIndex = smallerOrEqualXIndex - 1 >= 0 && Math.abs(points[smallerOrEqualXIndex][0] - x) < Math.abs(points[smallerOrEqualXIndex - 1][0] - x) ? smallerOrEqualXIndex: smallerOrEqualXIndex - 1;

            return closestXIndex;
        }
        else throw new IllegalArgumentException("Unkniown SortedArrayOrder " + xOrder);
    }

    @Override
    public double[][] getPoints() 
    {
        return points;
    }

    @Override
    public double[][] getPointsCopy()
    {
        return ArrayUtilities.deepCopy(points);
    }

    @Override
    public double[][] getPointsCopy(int from, int to)
    {
        return ArrayUtilities.deepCopyOfRange(points, from, to);
    }

    @Override
    public double[][] getPointsCopy(double conversionFactor) 
    {
        int n = points.length;
        double[][] copy = new double[n][];
        for(int i = 0;i<n;i++)
        {
            double[] pt = points[i];
            copy[i] = new double[] {pt[0], conversionFactor*pt[1]};
        }

        return copy;
    }

    @Override
    public boolean isEmpty()
    {
        return points.length == 0;
    }

    @Override
    public int getItemCount()
    {
        return points.length;
    }

    @Override
    public double getX(int item)
    {
        return points[item][0];
    }

    public double getXMaximumIndex()
    {
        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {
            return points.length - 1;
        }
        if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            return 0;
        }

        return ArrayUtilities.getMaximumXIndex(points);
    }

    public double getXMinimumIndex()
    {
        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {
            return 0;
        }
        if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            return points.length - 1;
        }

        return ArrayUtilities.getMinimumXIndex(points);
    }

    @Override
    public double getXMinimum()
    {        
        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {
            return points[0][0];
        }
        if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            return points[points.length - 1][0];
        }
        return ArrayUtilities.getMinimumX(points);
    }

    @Override
    public double getXMaximum()
    {
        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {
            return points[points.length - 1][0];
        }
        if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            return points[0][0];
        }
        return ArrayUtilities.getMaximumX(points);
    }

    @Override
    public Range getXRange()
    {
        if(points.length == 0)
        {
            return null;
        }
        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {                  
            Range range = new Range(points[0][0], points[points.length - 1][0]);
            if(!Double.isNaN(range.getLength()))
            {
                return range;
            }
        }
        if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            Range range = new Range(points[points.length - 1][0], points[0][0]);
            if(!Double.isNaN(range.getLength()))
            {
                return range;
            }
        }
        return ArrayUtilities.getBoundedXRange(points);
    }

    @Override
    public double getYMinimum()
    {
        return ArrayUtilities.getMinimumY(points);
    }

    @Override
    public double getYMinimum(int from, int to)
    {       
        Validation.requireValueEqualToOrBetweenBounds(from, 0, points.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, points.length, "to");

        return ArrayUtilities.getMinimumY(points, from, to);
    }

    @Override
    public int getYMinimumIndex()
    {
        return ArrayUtilities.getMinimumYIndex(points);
    }

    @Override
    public int getYMinimumIndex(int from, int to)
    {       
        Validation.requireValueEqualToOrBetweenBounds(from, 0, points.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, points.length, "to");

        return ArrayUtilities.getMinimumYIndex(points, from, to);
    }

    @Override
    public double getYMaximum()
    {
        return ArrayUtilities.getMaximumY(points);
    }

    @Override
    public double getYMaximum(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, points.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, points.length, "to");

        return ArrayUtilities.getMaximumY(points, from, to);
    }

    @Override
    public int getYMaximumIndex()
    {
        return ArrayUtilities.getMaximumYIndex(points);
    }

    @Override
    public int getYMaximumIndex(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, points.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, points.length, "to");

        return ArrayUtilities.getMaximumYIndex(points, from, to);
    }

    @Override
    public OrderedIntegerPair getIndicesOfYExtrema()
    {
        return ArrayUtilities.getIndicesOfYExtrema(points);
    }

    @Override
    public OrderedIntegerPair getIndicesOfYExtrema(int from, int to)
    {
        return ArrayUtilities.getIndicesOfYExtrema(points, from, to);
    }

    @Override
    public Range getYRange()
    {
        Range range = ArrayUtilities.getBoundedYRange(points);

        return range;
    }

    @Override
    public Range getYRange(Range xRange)
    {
        if(points.length == 0)
        {
            return null;
        }

        if((SortedArrayOrder.ASCENDING.equals(xOrder) || SortedArrayOrder.DESCENDING.equals(xOrder)))
        {
            IndexRange indexRange = getIndexRangeBoundedBy(xRange.getLowerBound(), xRange.getUpperBound());

            return ArrayUtilities.getBoundedYRange(points, indexRange.getMinIndex(), indexRange.getMaxIndex());
        }

        Range range = ArrayUtilities.getBoundedYRange(points, xRange);

        return range;
    }

    /**
     * Returns such an {@code index1} that {@code getX(index1) <=  upperBound} and for any other {@code index2} 
     * that satisfies  {@code getX(index2) <=  upperBound} it holds that {@code getIndex(index2) <= getIndex(index1)}
     * 
     * If no point has an x coordinate smaller or equal to upperBound, then the returned integer depends on the order of x-coordinates
     * of points in the channel: if points are in the ascending or undetermined order in respect to their x-coordinates, then the method returns -1.
     * If the order of x-coordinates is descending, the the method returns {@code getItemCount()}.
     * */
    @Override
    public int getIndexOfGreatestXSmallerOrEqualTo(double upperBound)
    {
        int n = points.length;

        if(n == 0)
        {
            return -1;
        }

        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {
            if(upperBound == Double.POSITIVE_INFINITY)
            {
                return n - 1;
            }

            int indexOfFirstXLargerOrEqualToBound = ArrayUtilities.binarySearchAscendingX(points, 0, n, upperBound); 
            if(indexOfFirstXLargerOrEqualToBound == 0 && points[0][0] > upperBound)
            {
                return -1;
            }

            int index = indexOfFirstXLargerOrEqualToBound < n && points[indexOfFirstXLargerOrEqualToBound][0] == upperBound ? indexOfFirstXLargerOrEqualToBound : indexOfFirstXLargerOrEqualToBound - 1;

            return index;
        }
        else if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            if(upperBound == Double.POSITIVE_INFINITY)
            {
                return 0;
            }

            int index = ArrayUtilities.binarySearchDescendingX(points, 0, n, upperBound);

            return index;
        }            
        else
        {
            return ArrayUtilities.getIndexOfGreatestXSmallerOrEqualTo(points,upperBound);
        }
    }

    /**
     * Returns such an {@code index1} that {@code getX(index1) >=  lowerBound}  and for any other {@code index2} 
     * that satisfies  {@code getX(index2) >=  lowerBound} it holds that {@code getIndex(index2) >= getIndex(index1)}
     * 
     * If no point has an x coordinate greater or equal to lowerBound, then the returned integer depends on the order of x-coordinates
     * of points in the channel: if points are in the ascending order in respect to their x-coordinates, then the method returns 
     * {@code getItemCount()}. If the order of x-coordinates is descending or undetermined, the the method returns -1
     * */
    @Override
    public int getIndexOfSmallestXGreaterOrEqualTo(double lowerBound)
    {
        int n = points.length;

        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {
            if(lowerBound == Double.NEGATIVE_INFINITY)
            {
                return 0;
            }

            return ArrayUtilities.binarySearchAscendingX(points, 0, n, lowerBound);
        }
        else if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            if(lowerBound == Double.NEGATIVE_INFINITY)
            {
                return n - 1;
            }

            int smallerOrEqualXIndex = ArrayUtilities.binarySearchDescendingX(points, 0, n, lowerBound);

            if(smallerOrEqualXIndex == n)
            {
                return n - 1;
            }

            int index = smallerOrEqualXIndex < n && points[smallerOrEqualXIndex][0] == lowerBound ? smallerOrEqualXIndex: smallerOrEqualXIndex - 1;             
            return index;
        }
        else
        {
            return ArrayUtilities.getIndexOfSmallestXGreaterOrEqualTo(points, lowerBound);
        }
    }

    @Override
    public IndexRange getIndexRangeBoundedBy(double lowerBound, double upperBound)
    {
        SortedArrayOrder order = getXOrder();

        int lowerBoundIndex = getIndexOfSmallestXGreaterOrEqualTo(lowerBound);
        int upperBoundIndex = getIndexOfGreatestXSmallerOrEqualTo(upperBound);

        IndexRange range = SortedArrayOrder.ASCENDING.equals(order) ? new IndexRange(lowerBoundIndex, upperBoundIndex) : new IndexRange(upperBoundIndex, lowerBoundIndex);

        return range;
    }

    //also includes points for which x == lowerBound or x == upperBound
    @Override
    public int getIndexCountBoundedBy(double lowerBound, double upperBound)
    {
        SortedArrayOrder order = getXOrder();

        if(SortedArrayOrder.ASCENDING.equals(order) || SortedArrayOrder.DESCENDING.equals(order))
        {
            IndexRange range = getIndexRangeBoundedBy(lowerBound, upperBound);
            return range.getLengthIncludingEdges();
        }

        return ArrayUtilities.countXValuesWithinRange(points, lowerBound, upperBound);
    }

    @Override
    public double getY(int item)
    {
        return points[item][1];
    }

    @Override
    public double[] getPoint(int item)
    {
        double[] p = points[item];
        return new double[] {p[0], p[1]};
    }

    @Override
    public SortedArrayOrder getXOrder()
    {
        return xOrder;
    }

    @Override
    public double[] getXCoordinates() 
    {
        return getXCoordinates(0, points.length);
    }

    @Override
    public double[] getXCoordinates(int from, int to) 
    {
        int n = points.length;

        Validation.requireValueEqualToOrBetweenBounds(from, 0, n, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, n, "to");

        double[] xs = new double[to - from];

        for(int i = from; i<to; i++)
        {
            double[] p = points[i];
            xs[i] = p[0];
        }
        return xs;
    }

    @Override
    public double[] getYCoordinates()
    {
        return getYCoordinates(0, points.length);
    }

    @Override
    public double[] getYCoordinates(int from, int to)
    {
        int n = points.length;

        Validation.requireValueEqualToOrBetweenBounds(from, 0, n, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, n, "to");

        double[] ys = new double[to - from];

        for(int i = from; i < to; i++)
        {
            double[] p = points[i];
            ys[i] = p[1];
        }

        return ys;
    }

    @Override
    public double[][] getXYView()
    {
        int n = points.length;

        double[] xs = new double[n];
        double[] ys = new double[n];

        for(int i = 0; i<n; i++)
        {
            double[] pt = points[i];
            xs[i] = pt[0];
            ys[i] = pt[1];
        }
        return new double[][] {xs, ys};
    }

    @Override
    public double[][] getXYView(int to, int from)
    {
        int n = points.length;

        Validation.requireValueEqualToOrBetweenBounds(from, 0, n, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, n, "to");

        double[] xs = new double[to - from];
        double[] ys = new double[to - from];

        for(int i = from; i < to; i++)
        {
            double[] pt = points[i];
            xs[i] = pt[0];
            ys[i] = pt[1];
        }
        return new double[][] {xs, ys};
    }
}

