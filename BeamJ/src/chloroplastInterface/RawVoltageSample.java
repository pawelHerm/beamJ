package chloroplastInterface;

import atomicJ.data.units.Quantity;
import atomicJ.data.units.SIPrefix;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.Units;

public class RawVoltageSample 
{
    private final static Quantity X_QUANTITY = new UnitQuantity("Time", new SimplePrefixedUnit("s", SIPrefix.m));
    private final static Quantity Y_QUANTITY = new UnitQuantity("Voltage", Units.VOLT_UNIT);

    private final double valueInVolts;
    private final long timeInMilis;

    public RawVoltageSample(double valueInVolts, long timeInMilis)
    {
        this.valueInVolts = valueInVolts;
        this.timeInMilis = timeInMilis;
    }

    public static Quantity getXQuantity()
    {
        return X_QUANTITY;
    }

    public static Quantity getYQuantity()
    {
        return Y_QUANTITY;
    }

    public double getValueInVolts()
    {
        return valueInVolts;
    }

    public long getAbsoluteTimeInMilis()
    {
        return timeInMilis;
    }
}
