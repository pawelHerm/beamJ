package chloroplastInterface;

import static atomicJ.data.Datasets.TIME;

import com.google.common.base.Objects;

import atomicJ.data.units.DimensionlessQuantity;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.Second;
import atomicJ.data.units.UnitQuantity;

public enum LightSignalType 
{
    REFLECTANCE("Reflectance",new UnitQuantity(TIME, Second.getInstance()), new DimensionlessQuantity("Reflectance (%)")), 
    TRANSMITTANCE("Transmittance", new UnitQuantity(TIME, Second.getInstance()), new DimensionlessQuantity("Transmittance (%)"));

    private final String property;
    private final Quantity xQuantity;
    private final Quantity yQuantity;

    LightSignalType(String name, Quantity xQuantity, Quantity yQuantity)
    {
        this.property = name;
        this.xQuantity = xQuantity;
        this.yQuantity = yQuantity;
    }

    public String getIdentifierForChannel(int signalIndex)
    {
        String identifier = this.property + " " + Integer.toString(signalIndex);
        
        return identifier;
    }
    
    public static boolean isTypeKnown(String name)
    {
        for(LightSignalType type : values())
        {
            if(Objects.equal(type.name(), name))
            {
                return true;
            }
        }

        return false;
    }

    public static LightSignalType valueOf(String name, LightSignalType defaultSignalType)
    {
        for(LightSignalType type : values())
        {
            if(Objects.equal(type.name(), name))
            {
                return type;
            }
        }

        return defaultSignalType;
    }

    public Quantity getXQuantity()
    {
        return xQuantity;
    }

    public Quantity getYQuantity()
    {
        return yQuantity;
    }

    public String getPhysicalPropertyName()
    {
        return property;
    }

    @Override
    public String toString()
    {     
        return property;
    }
}
