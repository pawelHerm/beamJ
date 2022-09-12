package atomicJ.data.units;

import java.text.NumberFormat;
import java.util.Locale;

import org.jfree.util.ObjectUtilities;

import atomicJ.utilities.MathUtilities;



public class UnitExpression 
{
    private static final double TOLERANCE = 1e-20;

    private final double value;
    private final PrefixedUnit unit;

    public UnitExpression(double value, PrefixedUnit unit)
    {
        this.value = value;
        this.unit = unit;
    }

    public UnitExpression(UnitExpression that)
    {
        this.value = that.value;
        this.unit = that.unit;
    }

    public static UnitExpression parse(String stringRepressentation)
    {       
        return parse(stringRepressentation, "\\s+");
    }

    public static UnitExpression parse(String stringRepressentation, String delimiter)
    {        
        String[] words = stringRepressentation.trim().split(delimiter);

        double value = Double.parseDouble(words[0].trim());
        String unitString = words.length > 1 ? words[1].trim() : "";

        PrefixedUnit unit = UnitUtilities.getSIUnit(unitString);

        UnitExpression unitExpression = new UnitExpression(value, unit);

        return unitExpression;
    }

    public double getValue()
    {
        return value;
    }

    public PrefixedUnit getUnit()
    {
        return unit;
    }

    public UnitExpression multiply(double valueOther)
    {
        double valueNew = valueOther*this.value;
        return new UnitExpression(valueNew, this.unit);
    }

    public UnitExpression derive(PrefixedUnit unitNew)
    {
        double conversionFactor = this.unit.getConversionFactorTo(unitNew);     
        double valueNew = conversionFactor*this.value;

        return new UnitExpression(valueNew, unitNew);
    }

    public UnitExpression deriveSimpleForm()
    {
        PrefixedUnit prefixNew = this.unit.getPreferredCompatibleUnit(this.value);
        return derive(prefixNew);
    }

    public UnitExpression multiply(UnitExpression other)
    {
        double valueNew = this.value * other.value;
        PrefixedUnit unitNew = this.unit.multiply(other.unit).simplify();

        return new UnitExpression(valueNew, unitNew);
    }

    public UnitExpression multiply(PrefixedUnit other)
    {
        double valueNew = this.value ;
        PrefixedUnit unitNew = this.unit.multiply(other).simplify();

        return new UnitExpression(valueNew, unitNew);
    }

    public UnitExpression divide(UnitExpression other)
    {
        double valueNew = this.value / other.value;
        PrefixedUnit unitNew = this.unit.divide(other.unit).simplify();

        return new UnitExpression(valueNew, unitNew);
    }

    public UnitExpression add(UnitExpression other)
    {
        if(!this.unit.isCompatible(other.unit))
        {
            throw new IllegalArgumentException("Units are not compatible");
        }

        UnitExpression otherDerived = other.derive(this.unit);
        double valueNew = this.value + otherDerived.value;

        return new UnitExpression(valueNew, this.unit);
    }

    public UnitExpression subtract(UnitExpression other)
    {
        if(!this.unit.isCompatible(other.unit))
        {
            throw new IllegalArgumentException("Units are not compatible");
        }

        UnitExpression otherDerived = other.derive(this.unit);
        double valueNew = this.value - otherDerived.value;

        return new UnitExpression(valueNew, this.unit);
    }

    @Override
    public String toString()
    {
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        format.setMinimumFractionDigits(5);

        String name = format.format(value) + " " + this.unit.getFullName();

        return name;
    }

    public String toString(NumberFormat format)
    {
        String name = format.format(value) + " " + this.unit.getFullName();

        return name;
    }

    public boolean isEqualUpToPrefixes(UnitExpression other)
    {
        if(other == null)
        {
            return false;
        }

        if(!this.unit.isCompatible(other.unit))
        {
            return false;
        }

        double xFactor = other.unit.getConversionFactorTo(this.unit);

        boolean equal = MathUtilities.equalWithinTolerance(this.value, xFactor*other.value, TOLERANCE);
        return equal;
    }

    public static boolean equalUpToPrefices(UnitExpression expr1, UnitExpression expr2)
    {
        if(expr1 == null && expr2 == null)
        {
            return true;
        }

        return (expr1 != null && expr1.isEqualUpToPrefixes(expr2));
    }

    @Override
    public int hashCode()
    {
        int hashCode = 17;

        long valueBits = Double.doubleToLongBits(value);
        hashCode += 31*hashCode + (int) (valueBits ^ (valueBits >>> 32));
        hashCode += 31*hashCode + this.unit.hashCode();

        return hashCode;
    }

    @Override
    public boolean equals(Object other)
    {
        if(!(other instanceof UnitExpression))
        {
            return false;
        }

        UnitExpression that = (UnitExpression)other;

        //we use Double.compare instead of simple == because Double.compare treats two NaNs as equal, and this is the behavior we need
        boolean equal = (Double.compare(this.value, that.value) == 0) && ObjectUtilities.equal(this.unit, that.unit);

        return equal;
    }
}
