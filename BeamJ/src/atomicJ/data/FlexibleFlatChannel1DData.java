package atomicJ.data;

import java.util.Arrays;

import org.jfree.data.Range;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.units.Quantity;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.OrderedIntegerPair;
import atomicJ.utilities.Validation;

public class FlexibleFlatChannel1DData implements Channel1DData
{
    private final double[] xValues;
    private final double[] yValues;
    private final SortedArrayOrder xOrder;
    private final Quantity xQuantity;
    private final Quantity yQuantity;

    public FlexibleFlatChannel1DData(double[] xValues, double[] yValues, Quantity xQuantity, Quantity yQuantity, SortedArrayOrder order)
    {
        if(xValues.length != yValues.length)
        {
            throw new IllegalArgumentException("The lists xValues and yValues should be of equal length");
        }

        this.xValues = xValues;
        this.yValues = yValues;
        this.xOrder = order;
        this.xQuantity = xQuantity;
        this.yQuantity = yQuantity;       
    }

    public FlexibleFlatChannel1DData(FlexibleFlatChannel1DData that)
    {
        this.xValues = Arrays.copyOf(that.xValues,that.xValues.length);
        this.yValues = Arrays.copyOf(that.yValues, that.yValues.length);
        this.xOrder = that.xOrder;
        this.xQuantity = that.xQuantity;
        this.yQuantity = that.yQuantity;
    }

    public static FlexibleFlatChannel1DData getEmptyInstance(Quantity xQuantity, Quantity yQuantity)
    {
        return new FlexibleFlatChannel1DData(new double[] {}, new double[] {}, xQuantity, yQuantity, null);
    }

    public static FlexibleFlatChannel1DData getEmptyInstance(Channel1DData channel)
    {
        return new FlexibleFlatChannel1DData(new double[] {}, new double[] {}, channel.getXQuantity(), channel.getYQuantity(), null);
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
        return new FlexibleFlatChannel1DData(this);
    }

    @Override
    public Channel1DData getCopy(double scale)
    {
        return getCopy(scale, this.yQuantity);
    }

    @Override
    public Channel1DData getCopy(double scale, Quantity yQuantityNew)
    {
        double[] xValuesCopy = Arrays.copyOf(xValues, xValues.length);
        double[] yValuesCopy = MathUtilities.multiply(yValues, scale) ;

        Channel1DData copy = new FlexibleFlatChannel1DData(xValuesCopy, yValuesCopy, this.xQuantity, yQuantityNew, this.xOrder);

        return copy;
    }

    @Override
    public int getIndexWithinDataBoundsOfItemWithXClosestTo(double x)
    {
        if(xOrder == null)
        {
            return ArrayUtilities.getIndexOfValueClosestTo(xValues, x);
        }

        int n = xValues.length;

        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {
            int indexOfGreaterOrEqualX = Math.min(n - 1, ArrayUtilities.binarySearchAscending(xValues, 0, n, x));
            int indexOfClosestX = indexOfGreaterOrEqualX - 1 >= 0 && Math.abs(xValues[indexOfGreaterOrEqualX] - x) < Math.abs(xValues[indexOfGreaterOrEqualX - 1] - x) ? indexOfGreaterOrEqualX: indexOfGreaterOrEqualX - 1;

            return indexOfClosestX;
        }
        else if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            int indexOfSmallerOrEqualX = Math.min(n - 1, ArrayUtilities.binarySearchDescending(xValues, 0, n, x));
            int indexOfClosestX = indexOfSmallerOrEqualX - 1 >= 0 && Math.abs(xValues[indexOfSmallerOrEqualX] - x) < Math.abs(xValues[indexOfSmallerOrEqualX - 1] - x) ? indexOfSmallerOrEqualX: indexOfSmallerOrEqualX - 1;

            return indexOfClosestX;
        }
        else throw new IllegalArgumentException("Unkniown SortedArrayOrder " + xOrder);
    }

    @Override
    public double[][] getPoints() 
    {
        int n = xValues.length;
        double[][] points = new double[n][];

        for(int i = 0; i<n;i++)
        {
            double x = xValues[i];
            double y = yValues[i];
            points[i] = new double[] {x, y};
        }

        return points;
    }

    @Override
    public double[][] getPointsCopy()
    {
        return getPoints();
    }

    @Override
    public double[][] getPointsCopy(int from, int to)
    {
        double[][] points = new double[to - from][];

        for(int i = from; i< to;i++)
        {
            double x = xValues[i];
            double y = yValues[i];
            points[i] = new double[] {x, y};
        }

        return points;
    }

    @Override
    public double[][] getPointsCopy(double conversionFactor) 
    {
        int n = xValues.length;
        double[][] points = new double[n][];

        for(int i = 0; i< n;i++)
        {
            double x = xValues[i];
            double y = yValues[i];
            points[i] = new double[] {x, conversionFactor*y};
        }

        return points;
    }

    @Override
    public boolean isEmpty()
    {
        return xValues.length == 0;
    }

    @Override
    public int getItemCount()
    {
        return xValues.length;
    }

    @Override
    public double getX(int item)
    {
        return xValues[item];
    }

    public double getXMaximumIndex()
    {
        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {
            return xValues.length - 1;
        }
        if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            return 0;
        }

        return ArrayUtilities.getMaximumIndex(xValues);
    }

    public double getXMinimumIndex()
    {
        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {
            return 0;
        }
        if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            return xValues.length - 1;
        }

        return ArrayUtilities.getMinimumIndex(xValues);
    }

    @Override
    public double getXMinimum()
    {        
        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {
            return xValues[0];
        }
        if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            return xValues[xValues.length - 1];
        }
        return ArrayUtilities.getMinimum(xValues);
    }

    @Override
    public double getXMaximum()
    {
        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {
            return xValues[xValues.length - 1];
        }
        if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            return xValues[0];
        }
        return ArrayUtilities.getMaximum(xValues);
    }

    @Override
    public Range getXRange()
    {
        if(xValues.length == 0)
        {
            return null;
        }
        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {                  
            Range range = new Range(xValues[0], xValues[xValues.length - 1]);
            if(!Double.isNaN(range.getLength()))
            {
                return range;
            }
        }
        if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            Range range = new Range(xValues[xValues.length - 1], xValues[0]);
            if(!Double.isNaN(range.getLength()))
            {
                return range;
            }
        }
        return ArrayUtilities.getBoundedRange(xValues);
    }

    @Override
    public double getYMinimum()
    {
        return ArrayUtilities.getMinimum(yValues);
    }

    @Override
    public double getYMinimum(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, yValues.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, yValues.length, "to");

        return ArrayUtilities.getMinimum(yValues, from, to);
    }

    @Override
    public int getYMinimumIndex()
    {
        return ArrayUtilities.getMinimumIndex(yValues);
    }

    @Override
    public int getYMinimumIndex(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, yValues.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, yValues.length, "to");

        return ArrayUtilities.getMinimumIndex(yValues, from, to);
    }

    @Override
    public double getYMaximum()
    {
        return ArrayUtilities.getMaximum(yValues);
    }

    @Override
    public double getYMaximum(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, yValues.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, yValues.length, "to");

        return ArrayUtilities.getMaximum(yValues, from, to);
    }

    @Override
    public int getYMaximumIndex()
    {
        return ArrayUtilities.getMaximumIndex(yValues);
    }

    @Override
    public int getYMaximumIndex(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, yValues.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, yValues.length, "to");

        return ArrayUtilities.getMaximumIndex(yValues, from, to);
    }

    @Override
    public OrderedIntegerPair getIndicesOfYExtrema()
    {
        return ArrayUtilities.getIndicesOfExtrema(yValues);
    }

    @Override
    public OrderedIntegerPair getIndicesOfYExtrema(int from, int to)
    {
        return ArrayUtilities.getIndicesOfExtrema(yValues, from, to);
    }

    @Override
    public Range getYRange()
    {
        Range range = ArrayUtilities.getBoundedRange(yValues);

        return range;
    }

    @Override
    public Range getYRange(Range xRange)
    {
        if(yValues.length == 0)
        {
            return null;
        }

        if((SortedArrayOrder.ASCENDING.equals(xOrder) || SortedArrayOrder.DESCENDING.equals(xOrder)))
        {
            IndexRange indexRange = getIndexRangeBoundedBy(xRange.getLowerBound(), xRange.getUpperBound());

            return ArrayUtilities.getBoundedRange(yValues, indexRange.getMinIndex(), indexRange.getMaxIndex());
        }

        Range range = ArrayUtilities.getBoundedYRange(xValues, yValues, xRange);

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
        int n = xValues.length;

        if(n == 0)
        {
            int returnedIndex = SortedArrayOrder.DESCENDING.equals(xOrder) ? 0 : -1;
            return returnedIndex;
        }

        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {
            if(upperBound == Double.POSITIVE_INFINITY)
            {
                return n - 1;
            }

            int indexOfFirstXLargerOrEqualToBound = ArrayUtilities.binarySearchAscending(xValues, 0, n, upperBound); 
            if(indexOfFirstXLargerOrEqualToBound == 0 && xValues[0] > upperBound)
            {
                return -1;
            }

            int index = indexOfFirstXLargerOrEqualToBound < n && xValues[indexOfFirstXLargerOrEqualToBound] == upperBound ? indexOfFirstXLargerOrEqualToBound : indexOfFirstXLargerOrEqualToBound - 1;

            return index;
        }
        else if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            if(upperBound == Double.POSITIVE_INFINITY)
            {
                return 0;
            }

            int index = ArrayUtilities.binarySearchDescending(xValues, 0, n, upperBound);

            return index;
        }            
        else
        {
            return ArrayUtilities.getIndexOfGreatestValueSmallerOrEqualTo(xValues,upperBound);
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
        int n = xValues.length;

        if(n == 0)
        {
            int returnedIndex = SortedArrayOrder.ASCENDING.equals(xOrder) ? 0 : -1;
            return returnedIndex;
        }

        if(SortedArrayOrder.ASCENDING.equals(xOrder))
        {
            if(lowerBound == Double.NEGATIVE_INFINITY)
            {
                return 0;
            }

            return ArrayUtilities.binarySearchAscending(xValues, 0, n, lowerBound);
        }
        else if(SortedArrayOrder.DESCENDING.equals(xOrder))
        {
            if(lowerBound == Double.NEGATIVE_INFINITY)
            {
                return n - 1;
            }

            int smallerOrEqualXIndex = ArrayUtilities.binarySearchDescending(xValues, 0, n, lowerBound);

            if(smallerOrEqualXIndex == n)
            {
                return n - 1;
            }

            int index = smallerOrEqualXIndex < n && xValues[smallerOrEqualXIndex] == lowerBound ? smallerOrEqualXIndex: smallerOrEqualXIndex - 1;             
            return index;
        }
        else
        {
            return ArrayUtilities.getIndexOfSmallestValueGreaterOrEqualTo(xValues, lowerBound);
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

        return ArrayUtilities.countValuesWithinRange(xValues, lowerBound, upperBound);
    }

    @Override
    public double getY(int item)
    {
        return yValues[item];
    }

    @Override
    public double[] getPoint(int item)
    {
        return new double[] {xValues[item], yValues[item]};
    }

    @Override
    public SortedArrayOrder getXOrder()
    {
        return xOrder;
    }

    @Override
    public double[] getXCoordinates() 
    {
        return Arrays.copyOf(xValues, xValues.length);
    }

    @Override
    public double[] getXCoordinates(int from, int to) 
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, xValues.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, xValues.length, "to");

        return Arrays.copyOfRange(xValues, from, to);
    }

    @Override
    public double[] getYCoordinates()
    {
        return Arrays.copyOf(yValues, yValues.length);
    }

    //from inclusive, to exclusive
    @Override
    public double[] getYCoordinates(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, yValues.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, yValues.length, "to");

        return Arrays.copyOfRange(yValues, from, to);
    }

    @Override
    public double[][] getXYView()
    {
        return getXYView(9, xValues.length);
    }

    @Override
    public double[][] getXYView(int from, int to)
    {
        int n = xValues.length;

        Validation.requireValueEqualToOrBetweenBounds(from, 0, n, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, n, "to");

        double[] xs = new double[to - from];
        double[] ys = new double[to - from];

        for(int i = from; i < to; i++)
        {
            xs[i] = xValues[i];
            ys[i] = yValues[i];
        }

        return new double[][] {xs, ys};
    }
}

