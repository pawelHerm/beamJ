package chloroplastInterface;

import java.util.Objects;

public enum StandardTimeUnit
{
    MILISECOND("ms", 1) 
    {
        @Override
        public StandardTimeUnit[] getLargerOrEqualUnits() 
        {
            return new StandardTimeUnit[] {MILISECOND, SECOND, MINUTE, HOUR};
        }
    }, SECOND("s", 1000) {
        @Override
        public StandardTimeUnit[] getLargerOrEqualUnits()
        {
            return new StandardTimeUnit[] {SECOND, MINUTE, HOUR};
        }
    }, MINUTE("min", 60*1000) 
    {
        @Override
        public StandardTimeUnit[] getLargerOrEqualUnits() 
        {
            return new StandardTimeUnit[] {MINUTE, HOUR};
        }
    }, HOUR("h", 3600*1000) {
        @Override
        public StandardTimeUnit[] getLargerOrEqualUnits() 
        {
            return new StandardTimeUnit[] {HOUR};
        }
    };

    private final String unitShort;
    private final double miliseconds;

    StandardTimeUnit(String unitShort, double miliseconds)
    {
        this.unitShort = unitShort;
        this.miliseconds = miliseconds;
    }

    public double getConversionFactorToMilliseconds()
    {
        return miliseconds;
    }

    public double getConversionFactorTo(StandardTimeUnit unitOther)
    {
        double conversionFactor = this.miliseconds/unitOther.miliseconds;
        return conversionFactor;
    }

    @Override
    public String toString()
    {
        return unitShort;
    }       

    public abstract StandardTimeUnit[] getLargerOrEqualUnits();

    public static StandardTimeUnit getDefaultUnit()
    {
        return StandardTimeUnit.MINUTE;
    }

    public static StandardTimeUnit getUnit(String name)
    {
        StandardTimeUnit[] units = StandardTimeUnit.values();

        for(StandardTimeUnit unit : units)
        {
            if(Objects.equals(unit.name(), name))
            {
                return unit;
            }
        }

        throw new IllegalArgumentException("No " + StandardTimeUnit.class.getName() + " known for name " + name);
    }
}