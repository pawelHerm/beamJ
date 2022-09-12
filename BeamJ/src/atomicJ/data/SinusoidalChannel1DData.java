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

import java.util.Arrays;

import org.jfree.data.Range;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.units.Quantity;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.OrderedIntegerPair;
import atomicJ.utilities.Validation;


public class SinusoidalChannel1DData implements Channel1DData
{
    private static final double TOLERANCE = 1e-12;

    private final double[] data;
    private final double amplitude;
    private final double angleFactor;
    private final double phaseShift;
    private final int initIndex;
    private final double xShift;
    private final double effectiveXShift;
    private final SortedArrayOrder xOrder;
    private final Quantity xQuantity;
    private final Quantity yQuantity;

    //z = amplitude*(Math.cos((i + initIndex)*angleFactor) + 1) = A(Cos(w*i + phi)  +  1), where w = angleFactor, t = i, phi = angleFactor*initIndex
    //we can also express it in terms of sine function z = amplitude(Sin((i + initIndex)*angleFactor - PI/2.) + 1)

    //z = -amplitude * Math.sin(phase) + amplitude = amplitude(1 - Sin(phase)) = A(1 - Sin(i*angleFactor - PI/2))
    //phase = 2*Math.PI*index/numFcPoints + phaseShift phaseShift = Math.PI/2.0 - 2*Math.PI * (0.5*numFcPoints)/numFcPoints
    //phaseShift = -PI/2.
    //z = A(1 - Sin(i*angleFactor - PI/2)) = A(Sin(i*angleFactor + PI/2) + 1)

    public SinusoidalChannel1DData(double[] ys, double amplitude, double angleFactor, int initIndex, double phaseShift, Quantity xQuantity, Quantity yQuantity)
    {
        this(ys, amplitude, angleFactor, initIndex, phaseShift, 0, xQuantity, yQuantity);        
    }

    public SinusoidalChannel1DData(double[] ys, double amplitude, double angleFactor, int initIndex, double phaseShift, double xShift,Quantity xQuantity, Quantity yQuantity)
    {
        this.data = ys;
        this.amplitude = amplitude;
        this.angleFactor = angleFactor;
        this.phaseShift = phaseShift;
        this.initIndex = initIndex;
        this.xShift = xShift;
        this.effectiveXShift = xShift + amplitude;
        this.xOrder = calculateXOrder();
        this.xQuantity = xQuantity;
        this.yQuantity = yQuantity;
    }

    public SinusoidalChannel1DData(SinusoidalChannel1DData that)
    {
        this.data = Arrays.copyOf(that.data, that.data.length);
        this.amplitude = that.amplitude;
        this.angleFactor = that.angleFactor;
        this.phaseShift = that.phaseShift;
        this.initIndex = that.initIndex;
        this.xShift = that.xShift;
        this.effectiveXShift = that.effectiveXShift;
        this.xOrder = that.xOrder;
        this.xQuantity = that.xQuantity;
        this.yQuantity = that.yQuantity;
    }

    private SortedArrayOrder calculateXOrder()
    {
        SortedArrayOrder order = null;
        int n = data.length;

        double initAngle = angleFactor*(initIndex) + phaseShift;
        double endAngle = angleFactor*(data.length + initIndex) + phaseShift;

        //we check whether sine function is monotonic between initAngle and endAngle

        if(n > 1 &&Math.abs(endAngle - initAngle) <= Math.PI + TOLERANCE)
        {
            //if the data were dense as the subset of real numbers, then the monotonicity would require that Math.cos(initAngle)*Math.cos(endAngle) >= 0
            //in practice Math.cos(initAngle)*Math.cos(endAngle) >= -TOLERANCE
            //however, as the data are sampled at finite intervals, its is possible that they are monotonic even if it is not the case that Math.cos(initAngle)*Math.cos(endAngle) >= - TOLERANCE

            double x0 = getX(0);
            double x1 = getX(1);
            double xnMinus1 = getX(n - 1);
            double xnMinus2 = getX(n - 2);

            if((x0 <= x1) && (xnMinus2 <= xnMinus1))
            {
                order = SortedArrayOrder.ASCENDING;
            }
            else if((x0 >= x1) && (xnMinus2 >= xnMinus1))
            {
                order = SortedArrayOrder.DESCENDING;
            }           
        }

        return order;
    }

    public double getAmplitude()
    {
        return amplitude;
    }

    public double getAngleFactor()
    {
        return angleFactor;
    }

    public double getPhaseShift()
    {
        return phaseShift;
    }

    public int getInitIndex()
    {
        return initIndex;
    }

    public double getXShift()
    {
        return xShift;
    }

    @Override
    public double getXMinimum()
    {    
        if(xOrder != null)
        {
            double initAngle = angleFactor*(initIndex) + phaseShift;
            double endAngle = angleFactor*(data.length - 1 + initIndex) + phaseShift;

            return Math.min(effectiveXShift - amplitude*Math.sin(initAngle), effectiveXShift - amplitude*Math.sin(endAngle));
        }

        return ArrayUtilities.getMinimum(getXCoordinates());
    }

    @Override
    public double getXMaximum()
    {
        if(xOrder != null)
        {
            double initAngle = angleFactor*(initIndex) + phaseShift;
            double endAngle = angleFactor*(data.length - 1 + initIndex) + phaseShift;

            return Math.max(effectiveXShift - amplitude*Math.sin(initAngle), effectiveXShift - amplitude*Math.sin(endAngle));
        }

        return ArrayUtilities.getMaximum(getXCoordinates());
    }

    @Override
    public Range getXRange()
    {
        Range xRange = new Range(getXMinimum(), getXMaximum());
        return xRange;
    }

    @Override
    public double getYMinimum()
    {
        return ArrayUtilities.getMinimum(data);
    }

    @Override
    public double getYMinimum(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, data.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, data.length, "to");

        return ArrayUtilities.getMinimum(data, from, to);
    }

    @Override
    public int getYMinimumIndex()
    {
        return ArrayUtilities.getMinimumIndex(data);
    }

    @Override
    public int getYMinimumIndex(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, data.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, data.length, "to");

        return ArrayUtilities.getMinimumIndex(data, from, to);
    }

    @Override
    public double getYMaximum()
    {
        return ArrayUtilities.getMaximum(data);
    }

    @Override
    public double getYMaximum(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, data.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, data.length, "to");

        return ArrayUtilities.getMaximum(data, from, to);
    }

    @Override
    public int getYMaximumIndex()
    {
        return ArrayUtilities.getMaximumIndex(data);
    }

    @Override
    public int getYMaximumIndex(int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, data.length, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, data.length, "to");

        return ArrayUtilities.getMaximumIndex(data, from, to);
    }

    @Override
    public OrderedIntegerPair getIndicesOfYExtrema()
    {
        return ArrayUtilities.getIndicesOfExtrema(data);
    }

    @Override
    public OrderedIntegerPair getIndicesOfYExtrema(int from, int to)
    {
        return ArrayUtilities.getIndicesOfExtrema(data, from, to);
    }

    @Override
    public Range getYRange()
    {
        return ArrayUtilities.getRange(data);
    }

    @Override
    public Range getYRange(Range xRange)
    {
        IndexRange indexRange = getIndexRangeBoundedBy(xRange.getLowerBound(), xRange.getUpperBound());

        Range yRange = indexRange.isWellFormed(data.length) ? ArrayUtilities.getRange(data, indexRange.getMinIndex(), indexRange.getMaxIndex()) : ArrayUtilities.getBoundedYRange(getPoints(), xRange);

        return yRange;
    }

    @Override
    public int getIndexWithinDataBoundsOfItemWithXClosestTo(double x)
    {
        double iMinPositive = calculateRealIndex(x);

        int index = (int) Math.min(data.length - 1, Math.max(0, Math.rint(iMinPositive)));

        return index;
    }

    //we ensure that for r = calculateRealIndex(double x) it is always the case that  0 <= r <= data.length - 1
    private double calculateRealIndex(double x)
    {
        double factor1 = (effectiveXShift - x)/amplitude;

        if(factor1 > 1 || factor1 < -1)
        {            
            double index = SortedArrayOrder.ASCENDING.equals(xOrder) && x >= getXMaximum() || SortedArrayOrder.DESCENDING.equals(xOrder) && x <= getXMinimum() ? data.length - 1 : 0;          
            return index;
        }

        //even if abs(factor) < 1, there still may be no such r, that i(r) == x i 0 <= r <= data.length - 1
        //in such a case we must return either 0 or data.length - 1
        double xMinimum = getXMinimum();
        double xMaximum = getXMaximum();

        if(x <= xMinimum + TOLERANCE || x>= xMaximum - TOLERANCE)
        {
            double index = SortedArrayOrder.ASCENDING.equals(xOrder) && x >= xMaximum || SortedArrayOrder.DESCENDING.equals(xOrder) && x <= xMinimum ? data.length - 1 : 0;          
            return index;
        }

        double arcSinFactor = Math.asin(factor1);

        double basicSolutionA = (-angleFactor*initIndex - phaseShift + arcSinFactor)/angleFactor;
        double basicSolutionB = (-angleFactor*initIndex - phaseShift + Math.PI - arcSinFactor)/angleFactor;

        double constantFactor = 2*Math.PI/angleFactor;

        double iMinPositiveA = basicSolutionA - constantFactor*Math.floor(basicSolutionA /constantFactor);
        double iMinPositiveB = basicSolutionB - constantFactor*Math.floor(basicSolutionB /constantFactor);

        double iMinPositive = Math.min(iMinPositiveA, iMinPositiveB);

        return iMinPositive;

    }

    @Override
    public int getIndexOfGreatestXSmallerOrEqualTo(double upperBound)
    {
        int n = data.length;

        if(xOrder == null)
        {
            return -1;
        }

        if(upperBound == Double.POSITIVE_INFINITY)
        {
            return SortedArrayOrder.ASCENDING.equals(xOrder) ? n - 1 : 0;
        }

        double iMinPositive = calculateRealIndex(upperBound);
        //        double derivative = -amplitude*angleFactor*Math.cos(angleFactor*(iMinPositive + initIndex) + phaseShift);

        int index = SortedArrayOrder.ASCENDING.equals(xOrder) ? Math.min(n - 1, (int)Math.floor(iMinPositive)) : Math.max(0, (int)Math.ceil(iMinPositive));

        return index;
    }

    @Override
    public int getIndexOfSmallestXGreaterOrEqualTo(double lowerBound)
    {
        int n = data.length;

        if(xOrder == null)
        {
            return -1;
        }

        if(lowerBound == Double.NEGATIVE_INFINITY)
        {
            return SortedArrayOrder.ASCENDING.equals(xOrder) ? 0 : n - 1;
        }

        double iMinPositive = calculateRealIndex(lowerBound);           

        int index = SortedArrayOrder.ASCENDING.equals(xOrder) ? Math.max(0, (int)Math.ceil(iMinPositive)) : Math.min(n - 1, (int)Math.floor(iMinPositive));

        return index;
    }

    @Override
    public IndexRange getIndexRangeBoundedBy(double lowerBound, double upperBound)
    {
        int lowerBoundIndex = getIndexOfSmallestXGreaterOrEqualTo(lowerBound);          
        int upperBoundIndex = getIndexOfGreatestXSmallerOrEqualTo(upperBound);

        SortedArrayOrder order = getXOrder();
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

        return ArrayUtilities.countXValuesWithinRange(getPoints(), lowerBound, upperBound);
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
    public SinusoidalChannel1DData getCopy()
    {
        return new SinusoidalChannel1DData(this);
    }

    @Override
    public SinusoidalChannel1DData getCopy(double scale)
    {
        return getCopy(scale, this.yQuantity);
    }

    @Override
    public SinusoidalChannel1DData getCopy(double scale, Quantity yQuantityNew)
    {
        int n = data.length;
        double[] pointsCopy = new double[n];

        for(int i = 0; i<n;i++)
        {
            pointsCopy[i] = scale*data[i];
        }

        SinusoidalChannel1DData copy = new SinusoidalChannel1DData(pointsCopy, amplitude, angleFactor, initIndex, phaseShift, xQuantity, yQuantityNew);

        return copy;
    }

    public double[] getData()
    {
        return data;
    }

    @Override
    public double[][] getPoints() 
    {        
        int n = data.length;
        return getPoints(0, n);
    }

    public double[][] getPoints(int from, int to) 
    {        
        int n = data.length;

        Validation.requireValueEqualToOrBetweenBounds(from, 0, n, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, n, "to");

        double[][] point = new double[to - from][];

        for(int i = from; i < to; i++)
        {
            point[i] = new double[] {effectiveXShift - amplitude*Math.sin(angleFactor*(i + initIndex) + phaseShift), data[i]};
        }
        return point;
    }

    @Override
    public double[][] getPointsCopy()
    {
        return getPoints();
    }

    //from inclusive, to exclusive
    @Override
    public double[][] getPointsCopy(int from, int to) 
    {
        if(from < 0 || from >= data.length)
        {
            throw new IllegalArgumentException("Index 'from' " + from + " is outside channel index range");
        }

        if(to < 0 || to > data.length)
        {
            throw new IllegalArgumentException("Index 'to' " + to + " is outside channel index range");
        }

        int n = to - from;

        double[][] points = new double[n][];

        for(int i = from; i<to; i++)
        {
            points[i - from] = new double[] {effectiveXShift - amplitude*Math.sin(angleFactor*(i + initIndex) + phaseShift), data[i]};
        }
        return points;
    }

    @Override
    public double[][] getPointsCopy(double conversionFactor) 
    {
        int n = data.length;

        double[][] point = new double[n][];

        for(int i = 0; i<n; i++)
        {
            point[i] = new double[] {effectiveXShift - amplitude*Math.sin(angleFactor*(i + initIndex) + phaseShift), conversionFactor*data[i]};
        }
        return point;
    }

    //effectiveXShift + hybridAmplitude*Math.sin(angleFactor*(i + initIndex) + Math.PI/2.)
    //HybriD = amplitude = -hybridAmplitude
    //phaseShift = -Math.PI/2

    @Override
    public boolean isEmpty()
    {
        return (data.length == 0);
    }

    @Override
    public int getItemCount()
    {
        return data.length;
    }

    @Override
    public double getX(int item)
    {
        return (effectiveXShift - amplitude*Math.sin(angleFactor*(item + initIndex) + phaseShift));
    }

    @Override
    public SortedArrayOrder getXOrder()
    {
        return xOrder;
    }

    @Override
    public double getY(int item)
    {
        return data[item];
    }

    @Override
    public double[] getPoint(int item)
    {
        return new double[] {(effectiveXShift - amplitude*Math.sin(angleFactor*(item + initIndex) + phaseShift)), data[item]};
    } 

    public Channel1DData sortX(SortedArrayOrder order)
    {
        SortedArrayOrder currentOrder = getXOrder();

        if(order.equals(currentOrder))
        {
            return this;
        }

        else if(currentOrder != null)
        {
            double initAngle = angleFactor*(initIndex) + phaseShift;

            double phaseShiftNew = (Math.PI - initAngle) + phaseShift;

            int n = data.length;
            double[] dataReversed = new double[n];

            for(int i = 0; i<n;i++)
            {
                dataReversed[n - 1 - i] = data[i];
            }

            return new SinusoidalChannel1DData(dataReversed, this.amplitude, this.angleFactor,
                    this.initIndex, phaseShiftNew, this.xShift, xQuantity, yQuantity);
        }
        return new FlexibleChannel1DData(order.sortX(getPointsCopy()), this.xQuantity, this.yQuantity, order);
    }

    @Override
    public double[] getXCoordinates() 
    {
        int n = data.length;
        return getXCoordinates(0, n);
    }

    @Override
    public double[] getXCoordinates(int from, int to) 
    {
        int n = data.length;

        Validation.requireValueEqualToOrBetweenBounds(from, 0, n, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, n, "to");

        double[] xs = new double[to - from];

        for(int i = from; i < to; i++)
        {
            xs[i] = effectiveXShift - amplitude*Math.sin(angleFactor*(i + initIndex) + phaseShift);
        }
        return xs;
    }


    @Override
    public double[] getYCoordinates()
    {
        int n = data.length;
        return getYCoordinates(0, n);
    }

    @Override
    public double[] getYCoordinates(int from, int to)
    {
        int n = data.length;

        Validation.requireValueEqualToOrBetweenBounds(from, 0, n, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, n, "to");

        double[] ys = new double[to - from];

        for(int i = from; i < to; i++)
        {
            ys[i] = data[i];
        }
        return ys;
    }

    @Override
    public double[][] getXYView()
    {
        int n = data.length;
        return getXYView(0, n);
    }

    @Override
    public double[][] getXYView(int from, int to)
    {
        int n = data.length;

        Validation.requireValueEqualToOrBetweenBounds(from, 0, n, "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, n, "to");

        double[] xs = new double[to - from];
        double[] ys = new double[to - from];

        for(int i = from; i < to; i++)
        {
            xs[i] = effectiveXShift - amplitude*Math.sin(angleFactor*(i + initIndex) + phaseShift);
            ys[i] = data[i];
        }
        return new double[][] {xs, ys};
    }
}

