package atomicJ.analysis;

import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.Units;

public enum PhotodiodeSignalType 
{
    VOLTAGE("Voltage", Units.MICRO_METER_PER_VOLT_UNIT), 
    ELECTRIC_CURRENT("Electric current", Units.MICROMETER_PER_NANO_AMPERE_UNIT);

    private final String name;
    private final PrefixedUnit defaultUnit;

    PhotodiodeSignalType(String name, PrefixedUnit defaultUnit)
    {
        this.name = name;
        this.defaultUnit = defaultUnit;
    }

    public PrefixedUnit getDefaultUnit()
    {
        return defaultUnit;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public static PhotodiodeSignalType getSignalType(Quantity yAxisQuantity, PhotodiodeSignalType fallBackSignalType)
    {   
        return getSignalType(yAxisQuantity.getUnit(), fallBackSignalType);
    }

    public static PhotodiodeSignalType getSignalType(PrefixedUnit yUnit, PhotodiodeSignalType fallBackSignalType)
    {        
        for(PhotodiodeSignalType signalType : values())
        {
            if(signalType.defaultUnit.isCompatible(yUnit))
            {
                return signalType;
            }
        }

        return fallBackSignalType;
    }
}
