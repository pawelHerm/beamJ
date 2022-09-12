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

import org.jfree.data.Range;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.units.Quantity;
import atomicJ.utilities.OrderedIntegerPair;

public interface Channel1DData
{
    public Channel1DData getCopy();
    public Channel1DData getCopy(double scale);
    public Channel1DData getCopy(double scale, Quantity yQuantityNew);
    public Quantity getXQuantity();
    public Quantity getYQuantity();
    public double[][] getPoints();
    public double[][] getPointsCopy();
    public double[][] getPointsCopy(double scale);
    //from inclusive, to exclusive
    //it must be the case that from > 0 && to > 0 && from < pointCount && to <= pointCount
    //otherwise IllegalArgumentException is thrown
    public double[][] getPointsCopy(int from, int to);
    public SortedArrayOrder getXOrder();
    public boolean isEmpty();
    public int getItemCount();  
    public double getX(int item);    
    public double getY(int item);
    public double getXMinimum();
    public double getXMaximum();    
    public Range getXRange();    
    public double getYMinimum();   
    public double getYMinimum(int from, int to);
    public int getYMinimumIndex();   
    public int getYMinimumIndex(int from, int to);
    public double getYMaximum();
    public double getYMaximum(int from, int to);
    public int getYMaximumIndex();
    public int getYMaximumIndex(int from, int to);
    public OrderedIntegerPair getIndicesOfYExtrema();
    public OrderedIntegerPair getIndicesOfYExtrema(int from, int to);
    public Range getYRange();
    public Range getYRange(Range xRange);
    public IndexRange getIndexRangeBoundedBy(double lowerBound, double upperBound);

    /**
     * Returns such an {@code index1} that {@code getX(index1) <=  upperBound} and for any other {@code index2} 
     * that satisfies  {@code getX(index2) <=  upperBound} it holds that {@code getIndex(index2) <= getIndex(index1)}
     * 
     * If no point has an x coordinate smaller or equal to upperBound, then the returned integer depends on the order of x-coordinates
     * of points in the channel: if points are in the ascending order in respect to their x-coordinates, then the method returns -1.
     * If the order of x-coordinates is descending or undetermined, the the method returns {@code getItemCount()}.
     * */
    public int getIndexOfGreatestXSmallerOrEqualTo(double upperBound);
    /**
     * Returns such an {@code index1} that {@code getX(index1) >=  lowerBound}  and for any other {@code index2} 
     * that satisfies  {@code getX(index2) >=  lowerBound} it holds that {@code getIndex(index2) >= getIndex(index1)}
     * 
     * If no point has an x coordinate greater or equal to lowerBound, then the returned integer depends on the order of x-coordinates
     * of points in the channel: if points are in the ascending order in respect to their x-coordinates, then the method returns 
     * {@code getItemCount()}. If the order of x-coordinates is descending or undetermined, the the method returns -1
     * */
    public int getIndexOfSmallestXGreaterOrEqualTo(double lowerBound);

    //also includes points for which x == lowerBound or x == upperBound
    public int getIndexCountBoundedBy(double lowerBound, double upperBound);
    public int getIndexWithinDataBoundsOfItemWithXClosestTo(double x);
    public double[] getPoint(int item);
    public double[] getXCoordinates();
    public double[] getXCoordinates(int from, int to);
    public double[] getYCoordinates();
    public double[] getYCoordinates(int from, int to);
    public double[][] getXYView();
    public double[][] getXYView(int from, int to);
}

