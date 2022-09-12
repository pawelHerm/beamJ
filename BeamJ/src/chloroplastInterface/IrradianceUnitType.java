package chloroplastInterface;

import java.util.Arrays;

import atomicJ.data.units.GeneralPrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.SIPrefix;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.StandardUnitType;

public enum IrradianceUnitType
{
    WATS_PER_SQUARE_METER(GeneralPrefixedUnit.getInstance(Arrays.<SimplePrefixedUnit>asList(new SimplePrefixedUnit(StandardUnitType.WATT.getSymbol()),
            new SimplePrefixedUnit(StandardUnitType.METER.getSymbol(),SIPrefix.Empty,-2)),false)), 
    MICROMOLES_PER_SQUARE_METER_PER_SECOND(GeneralPrefixedUnit.getInstance(Arrays.<SimplePrefixedUnit>asList(new SimplePrefixedUnit(StandardUnitType.MOLE.getSymbol(),SIPrefix.u),new SimplePrefixedUnit(StandardUnitType.METER.getSymbol(),SIPrefix.Empty,-2),
            new SimplePrefixedUnit(StandardUnitType.SECOND.getSymbol(),SIPrefix.Empty,-1)),false));

    private PrefixedUnit unit;

    IrradianceUnitType(PrefixedUnit unit)
    {
        this.unit = unit;
    }

    public PrefixedUnit getUnit()
    {
        return unit;
    }

    public static IrradianceUnitType getValue(String name, IrradianceUnitType fallBackValue)
    {
        IrradianceUnitType type = fallBackValue;

        if(name != null)
        {
            for(IrradianceUnitType t : IrradianceUnitType.values())
            {
                String currentName =  t.name();
                if(currentName.equals(name))
                {
                    type = t;
                    break;
                }
            }
        }

        return type;
    }

    @Override
    public String toString()
    {
        return unit.getFullName();
    }
}