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

package atomicJ.data;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.jfree.data.Range;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.units.Quantity;
import atomicJ.functions.MonotonicityType;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.OrderedIntegerPair;
import atomicJ.utilities.Validation;

public class FunctionChannel1DData implements Channel1DData
{
    private final UnivariateFunction f;
    private final MonotonicityType monotonicityType;
    private final Grid1D grid;
    private final Quantity yQuantity;

    public FunctionChannel1DData(UnivariateFunction f, MonotonicityType monotonicityType, Grid1D grid, Quantity yQuantity)
    {
        this.f = f;
        this.monotonicityType = monotonicityType;
        this.grid = grid;
        this.yQuantity = yQuantity;
    }

    public FunctionChannel1DData(FunctionChannel1DData that)
    {
        this.f = that.f;
        this.monotonicityType = that.monotonicityType;
        this.grid = that.grid;
        this.yQuantity = that.yQuantity;
    }

    @Override
    public Quantity getXQuantity()
    {
        return grid.getQuantity();
    }

    @Override
    public Quantity getYQuantity()
    {
        return yQuantity;
    }

    @Override
    public FunctionChannel1DData getCopy()
    {
        return new FunctionChannel1DData(this);
    }

    @Override
    public Channel1DData getCopy(double scale)
    {
        return getCopy(scale, this.yQuantity);
    }

    @Override
    public Channel1DData getCopy(double scale, Quantity yQuantityNew)
    {       
        Channel1DData copy = new GridChannel1DData(getDataCopy(scale), grid, yQuantityNew);

        return copy;
    }

    public Grid1D getGrid()
    {
        return grid;
    }

    public double[] getData() 
    {
        return getData(0, grid.getItemCount());
    }

    //from inclusive, to exclusive
    public double[] getData(int from, int to) 
    {
        double origin = grid.getOrigin();
        double increment = grid.getIncrement();

        double[] data = new double[to - from];

        for(int i = from; i < to; i++)
        {
            data[i] = f.value(origin + i*increment);
        }

        return data;
    }

    public double[] getDataCopy()
    {
        return getData();
    }

    public double[] getDataCopy(int from, int to)
    {
        return getData(from, to);
    }

    public double[] getDataCopy(double s) 
    {
        int columnCount = grid.getIndexCount();
        double origin = grid.getOrigin();
        double increment = grid.getIncrement();

        double[] data = new double[columnCount];

        for(int i = 0; i < columnCount; i++)
        {
            data[i] = s*f.value(origin + i*increment);
        }

        return data;
    }

    @Override
    public double[][] getPoints() 
    {
        int columnCount = grid.getIndexCount();
        double origin = grid.getOrigin();
        double increment = grid.getIncrement();

        double[][] points = new double[columnCount][];

        for(int i = 0; i < columnCount; i++)
        {
            double x = origin + i*increment;
            points[i] = new double[] {x, f.value(x)};
        }

        return points;
    }

    @Override
    public double[][] getPointsCopy(int from, int to) 
    {
        int itemCount = grid.getItemCount();
        if(from < 0 || from >= itemCount)
        {
            throw new IllegalArgumentException("Index 'from' " + from + " is outside channel index range");
        }

        if(to < 0 || to > itemCount)
        {
            throw new IllegalArgumentException("Index 'to' " + to + " is outside channel index range");
        }

        int n = to - from;
        double origin = grid.getOrigin();
        double increment = grid.getIncrement();

        double[][] points = new double[n][];

        for(int i = from; i < to; i++)
        {
            double x = origin + i*increment;
            points[i - from] = new double[] {x, f.value(x)};
        }

        return points;
    }

    @Override
    public double[][] getPointsCopy()
    {
        return getPoints();
    }

    @Override
    public double[][] getPointsCopy(double conversionFactor) 
    {
        int columnCount = grid.getIndexCount();
        double origin = grid.getOrigin();
        double increment = grid.getIncrement();

        double[][] points = new double[columnCount][];

        for(int i = 0; i < columnCount; i++)
        {
            double x = origin + i*increment;
            points[i] = new double[] {x, conversionFactor*f.value(x)};
        }

        return points;
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
        SortedArrayOrder order = getXOrder();

        int columnCount = grid.getIndexCount();
        double origin = grid.getOrigin();
        double increment = grid.getIncrement();

        if(SortedArrayOrder.ASCENDING.equals(order))
        {
            if(upperBound == Double.POSITIVE_INFINITY)
            {
                return columnCount - 1;
            }
            return Math.min(columnCount - 1, Math.max(-1, (int)Math.floor((upperBound - origin)/increment)));
        }
        else if(SortedArrayOrder.DESCENDING.equals(order))
        {
            if(upperBound == Double.POSITIVE_INFINITY)
            {
                return 0;
            }
            return Math.min(columnCount, Math.max(0, (int)Math.ceil((upperBound - origin)/increment)));
        }
        //cannot happen
        return - 1;
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
        SortedArrayOrder order = getXOrder();

        int columnCount = grid.getIndexCount();
        double origin = grid.getOrigin();
        double increment = grid.getIncrement();

        if(SortedArrayOrder.ASCENDING.equals(order))
        {
            if(lowerBound == Double.NEGATIVE_INFINITY)
            {
                return 0;
            }

            return Math.min(columnCount, Math.max(0, (int)Math.ceil((lowerBound - origin)/increment)));
        }
        else if(SortedArrayOrder.DESCENDING.equals(order))
        {
            if(lowerBound == Double.NEGATIVE_INFINITY)
            {
                return columnCount - 1;
            }
            return Math.min(columnCount - 1, Math.max(-1, (int)Math.floor((lowerBound - origin)/increment)));
        }
        //cannot happen
        return -1;
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

    @Override
    public int getIndexCountBoundedBy(double lowerBound, double upperBound)
    {
        SortedArrayOrder order = getXOrder();

        if(SortedArrayOrder.ASCENDING.equals(order) || SortedArrayOrder.DESCENDING.equals(order))
        {
            IndexRange range = getIndexRangeBoundedBy(lowerBound, upperBound);
            return range.getLengthIncludingEdges();
        }

        return ArrayUtilities.countXValuesWithinRange(getPoints(), lowerBound, upperBound);
    }
    @Override
    public boolean isEmpty()
    {
        return grid.isEmpty();
    }

    @Override
    public int getItemCount()
    {
        return grid.getItemCount();
    }

    @Override
    public double getX(int item)
    {
        return grid.getArgumentVal(item);
    }

    @Override
    public double getY(int item)
    {
        return f.value(grid.getArgumentVal(item));
    }

    @Override
    public double getXMinimum()
    {
        return grid.getMinimum();
    }

    @Override
    public double getXMaximum()
    {
        return grid.getMaximum();
    }

    @Override
    public Range getXRange()
    {
        return grid.getXRange();
    }

    @Override
    public double getYMinimum()
    {
        return getYMinimum(0, grid.getIndexCount());
    }

    //from inclusive, to exclusive
    @Override
    public double getYMinimum(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, grid.getIndexCount(), "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, grid.getIndexCount(), "to");

        if(monotonicityType.isMonotonic())
        {
            double yA = f.value(grid.getArgumentVal(from));
            double yB = f.value(grid.getArgumentVal(to - 1));

            return Math.min(yA, yB);
        }
        return ArrayUtilities.getMinimum(getData(from, to));
    }

    //from inclusive, to exclusive
    @Override
    public int getYMinimumIndex()
    {
        return getYMinimumIndex(0, grid.getIndexCount());
    }

    //from inclusive, to exclusive
    @Override
    public int getYMinimumIndex(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, grid.getIndexCount(), "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, grid.getIndexCount(), "to");

        if(monotonicityType.isMonotonic())
        {
            double yA = f.value(grid.getArgumentVal(from));
            double yB = f.value(grid.getArgumentVal(to - 1));

            int minIndex = yA < yB ? from : to - 1;
            return minIndex;
        }
        return ArrayUtilities.getMinimumIndex(getData(from, to));
    }

    @Override
    public double getYMaximum()
    {
        return getYMaximum(0, grid.getIndexCount());
    }

    //from inclusive, to exclusive
    @Override
    public double getYMaximum(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, grid.getIndexCount(), "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, grid.getIndexCount(), "to");

        if(monotonicityType.isMonotonic())
        {
            double yA = f.value(grid.getArgumentVal(from));
            double yB = f.value(grid.getArgumentVal(to - 1));

            return Math.min(yA, yB);
        }
        return ArrayUtilities.getMaximum(getData(from, to));
    }

    @Override
    public int getYMaximumIndex()
    {
        return getYMaximumIndex(0, grid.getIndexCount());
    }

    //from inclusive, to exclusive
    @Override
    public int getYMaximumIndex(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, grid.getIndexCount(), "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, grid.getIndexCount(), "to");

        if(monotonicityType.isMonotonic())
        {
            double yA = f.value(grid.getArgumentVal(from));
            double yB = f.value(grid.getArgumentVal(to - 1));

            int indexOfMaximum = yA > yB ? from : to - 1;
            return indexOfMaximum;
        }
        return ArrayUtilities.getMaximumIndex(getData(from, to));
    }

    @Override
    public OrderedIntegerPair getIndicesOfYExtrema()
    {
        return getIndicesOfYExtrema(0, getItemCount());
    }

    //from inclusive, to exclusive
    @Override
    public OrderedIntegerPair getIndicesOfYExtrema(int from, int to)
    {
        if(monotonicityType.isMonotonic())
        {
            double y0 = f.value(grid.getArgumentVal(from));
            double yn = f.value(grid.getArgumentVal(to - 1));

            int minIndex = y0 <= yn ? 0 : from;
            int maxIndex = y0 >= yn ? 0 : to - 1;

            return new OrderedIntegerPair(minIndex, maxIndex);
        }
        return ArrayUtilities.getIndicesOfExtrema(getData(from, to), 0, getItemCount());
    }

    @Override
    public Range getYRange()
    {
        if(monotonicityType.isMonotonic())
        {            
            double y0 = f.value(grid.getArgumentVal(0));
            double yn = f.value(grid.getArgumentVal(grid.getItemCount() - 1));

            double lowerBound = Math.min(y0, yn);
            double upperBound = Math.max(y0, yn);

            return new Range(lowerBound, upperBound);
        }

        return ArrayUtilities.getBoundedRange(getData());
    }

    @Override
    public Range getYRange(Range xRange)
    {
        IndexRange indexRange = getIndexRangeBoundedBy(xRange.getLowerBound(), xRange.getUpperBound());

        int minIndex = indexRange.getMinIndex();
        int maxIndex = indexRange.getMaxIndex();

        if(monotonicityType.isMonotonic())
        {            
            double y0 = f.value(grid.getArgumentVal(minIndex));
            double yn = f.value(grid.getArgumentVal(maxIndex));

            double lowerBound = Math.min(y0, yn);
            double upperBound = Math.max(y0, yn);

            return new Range(lowerBound, upperBound);
        }

        return ArrayUtilities.getBoundedRange(getData(), minIndex, maxIndex);
    }

    @Override
    public double[] getPoint(int item)
    {
        return new double[] {grid.getArgumentVal(item), f.value(grid.getArgumentVal(item))};
    } 

    @Override
    public int getIndexWithinDataBoundsOfItemWithXClosestTo(double x)
    {
        return grid.getIndex(x);
    }

    @Override
    public SortedArrayOrder getXOrder()
    {
        return grid.getOrder();
    }

    @Override
    public double[] getXCoordinates() 
    {
        return grid.getNodes();
    }

    @Override
    public double[] getXCoordinates(int from, int to) 
    {
        int itemCount = grid.getItemCount();

        Validation.requireValueEqualToOrBetweenBounds(from, 0, itemCount, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, itemCount, "to");

        return grid.getNodes(from, to);
    }

    @Override
    public double[] getYCoordinates()
    {
        return getDataCopy();
    }

    @Override
    public double[] getYCoordinates(int from, int to)
    {
        int n = grid.getItemCount();

        Validation.requireValueEqualToOrBetweenBounds(from, 0, n, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, n, "to");

        return getData(from, to);
    }

    @Override
    public double[][] getXYView()
    {
        int n = grid.getItemCount();
        return getXYView(0, n);
    }

    @Override
    public double[][] getXYView(int from, int to)
    {
        int n = grid.getItemCount();

        Validation.requireValueEqualToOrBetweenBounds(from, 0, n, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, n, "to");

        double[] xs = new double[to - from];
        double[] ys = new double[to - from];

        double increment = grid.getIncrement();
        double origin = grid.getOrigin();

        for(int i = from; i<to; i++)
        {
            double x = origin + i*increment;
            xs[i] = x;
            ys[i] = f.value(x);
        }

        return new double[][] {xs, ys};
    }
}

