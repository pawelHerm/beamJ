package chloroplastInterface;

import java.util.Objects;

public enum ScaleType
{
    LINEAR("Linear"), LOGARITHMIC("Logarithmic");

    private final String description;

    ScaleType(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }     

    public static ScaleType getStepType(String name)
    {
        ScaleType[] types = ScaleType.values();

        for(ScaleType type : types)
        {
            if(Objects.equals(type.name(), name))
            {
                return type;
            }
        }

        throw new IllegalArgumentException("No " + ScaleType.class.getName() + " known for name " + name);
    }
}