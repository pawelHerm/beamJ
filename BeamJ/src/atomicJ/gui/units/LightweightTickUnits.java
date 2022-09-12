
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

package atomicJ.gui.units;


import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.TickUnitSource;

import atomicJ.gui.NumberFormatReceiver;
import atomicJ.gui.PrefixedTickUnit;
import atomicJ.gui.PremutipliedSexagesimalTickUnit;
import atomicJ.utilities.MathUtilities;


public class LightweightTickUnits implements TickUnitSource, NumberFormatReceiver, Cloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    private static final double[] DECIMAL_TICK_UNIT_SIZES = new double[42];
    private static final double[] SEXAGESIMAL_TICK_UNIT_SIZES = new double[] {30, 60, 300, 600, 1800, 3600, 18000, 36000, 180000};//when units are hours, minutes, or degrees of angle
    private static final double TOLERANCE = 10e-12;

    static
    {
        int index = 0;
        for(int maxDigit = -10; maxDigit <= 10; maxDigit++)
        {	
            double power= MathUtilities.intPow(10, maxDigit);
            DECIMAL_TICK_UNIT_SIZES[index++] = power;
            DECIMAL_TICK_UNIT_SIZES[index++] = 5*power;
        }
    }

    private boolean showTrailingZeroes;
    private char groupingSeparator;			
    private char decimalSeparator;			
    private boolean groupingUsed;
    private double conversionFactor = 1;

    public void setConversionFactor(double conversionFactorNew)
    {        
        this.conversionFactor = conversionFactorNew;
    }

    private boolean isConversionFactorDecimal()
    {
        double exponent = Math.log10(conversionFactor);
        boolean isDecimal = Math.abs(exponent - Math.round(exponent)) < TOLERANCE;

        return isDecimal;
    }

    public NumberTickUnit getTickUnitForSize(double size)
    {   
        if(isConversionFactorDecimal())
        {
            return getDecimalTickUnitForSize(size);
        }

        return getSexagesimalTickUnitForSize(size);
    }

    private NumberTickUnit getDecimalTickUnitForSize(double size)
    {
        NumberFormat format = buildNumberFormat(size);
        PrefixedTickUnit tickUnit = new PrefixedTickUnit(size, format, 5, conversionFactor);
        return tickUnit;
    }

    private NumberTickUnit getSexagesimalTickUnitForSize(double size)
    {           
        NumberFormat integerPartFormat = buildIntegerPartFormat(size);
        NumberFormat fractionalFormat = buildFractionalPartFormat(size);

        double smallerUnitsPerMainUnit = 60;
        PremutipliedSexagesimalTickUnit tickUnit = new PremutipliedSexagesimalTickUnit(size, integerPartFormat,fractionalFormat, 5, conversionFactor,smallerUnitsPerMainUnit);
        return tickUnit;
    }

    private NumberFormat buildNumberFormat(double size)
    {
        int maxDigit = MathUtilities.getFractionCount(conversionFactor*size);
        int minDigits = showTrailingZeroes ? maxDigit : 0;
        DecimalFormat format = new DecimalFormat();

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

        symbols.setDecimalSeparator(decimalSeparator);
        symbols.setGroupingSeparator(groupingSeparator);
        format.setGroupingUsed(groupingUsed);
        format.setMaximumFractionDigits(maxDigit);
        format.setMinimumFractionDigits(minDigits);

        format.setDecimalFormatSymbols(symbols);

        return format;
    }

    private NumberFormat buildFractionalPartFormat(double size)
    {
        DecimalFormat format = new DecimalFormat();

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

        symbols.setDecimalSeparator(decimalSeparator);
        symbols.setGroupingSeparator(groupingSeparator);
        format.setGroupingUsed(groupingUsed);
        format.setMinimumFractionDigits(0);
        format.setMinimumIntegerDigits(2);

        format.setDecimalFormatSymbols(symbols);

        return format;
    }

    private NumberFormat buildIntegerPartFormat(double size)
    {
        DecimalFormat format = new DecimalFormat();

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

        symbols.setGroupingSeparator(groupingSeparator);
        format.setGroupingUsed(groupingUsed);
        format.setMinimumFractionDigits(0);

        format.setDecimalFormatSymbols(symbols);

        return format;
    }

    @Override
    public NumberTickUnit getLargerTickUnit(TickUnit unit) 
    {
        boolean decimal = isConversionFactorDecimal();

        double[] tickUnitSizes =  decimal ? DECIMAL_TICK_UNIT_SIZES : SEXAGESIMAL_TICK_UNIT_SIZES;

        int index = Arrays.binarySearch(tickUnitSizes, unit.getSize());
        int indexLarger = index >= 0 ? index + 1: -index;

        double sizeLarger = tickUnitSizes[Math.min(indexLarger, tickUnitSizes.length - 1)];

        NumberTickUnit tickUnit = decimal ? getDecimalTickUnitForSize(sizeLarger) : getSexagesimalTickUnitForSize(sizeLarger);
        return tickUnit;
    }

    @Override
    public NumberTickUnit getCeilingTickUnit(TickUnit unit) 
    {
        return getCeilingTickUnit(unit.getSize());
    }

    @Override
    public NumberTickUnit getCeilingTickUnit(double size) 
    {
        boolean decimal = isConversionFactorDecimal();

        double[] tickUnitSizes = decimal ? DECIMAL_TICK_UNIT_SIZES : SEXAGESIMAL_TICK_UNIT_SIZES;

        int index = Arrays.binarySearch(tickUnitSizes, size);
        int indexCeiling = index < 0 ? -(index + 1): index;

        double sizeCeiling = tickUnitSizes[Math.min(indexCeiling, tickUnitSizes.length - 1)];

        NumberTickUnit tickUnit = decimal ?  getDecimalTickUnitForSize(sizeCeiling) : getSexagesimalTickUnitForSize(sizeCeiling);
        return tickUnit;
    }

    @Override
    public boolean isTickLabelTrailingZeroes()
    {
        return showTrailingZeroes;
    }

    @Override
    public void setTickLabelShowTrailingZeroes(boolean trailingZeroes)
    {
        this.showTrailingZeroes = trailingZeroes;
    }

    @Override
    public boolean isTickLabelGroupingUsed()
    {
        return groupingUsed;
    }

    @Override
    public void setTickLabelGroupingUsed(boolean used)
    {
        this.groupingUsed = used;
    }

    @Override
    public char getTickLabelGroupingSeparator()
    {
        return groupingSeparator;
    }

    @Override
    public void setTickLabelGroupingSeparator(char separator)
    {
        this.groupingSeparator = separator;
    }

    @Override
    public char getTickLabelDecimalSeparator()
    {
        return decimalSeparator;
    }

    @Override
    public void setTickLabelDecimalSeparator(char separator)
    {
        this.decimalSeparator = separator;

    }

    @Override
    public Object clone() throws CloneNotSupportedException 
    {
        LightweightTickUnits clone = (LightweightTickUnits) super.clone();
        return clone;
    }

    @Override
    public int hashCode()
    {
        int result = Boolean.hashCode(showTrailingZeroes);
        result = 31*result + Boolean.hashCode(groupingUsed);
        result = 31*result + Character.hashCode(decimalSeparator);
        result = 31*result + Character.hashCode(groupingSeparator);
        result = 31*result + Double.hashCode(conversionFactor);

        return result;
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LightweightTickUnits)) {
            return false;
        }
        LightweightTickUnits that = (LightweightTickUnits) obj;

        boolean equal = true;
        equal = equal && (this.showTrailingZeroes == that.showTrailingZeroes);
        equal = equal && (this.groupingUsed == that.groupingUsed);
        equal = equal && (this.decimalSeparator == that.decimalSeparator);
        equal = equal && (this.groupingSeparator == that.groupingSeparator);
        equal = equal && (this.conversionFactor == that.conversionFactor);

        return equal;
    }
}

