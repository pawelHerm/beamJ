package chloroplastInterface.optics;

import java.io.Serializable;

import atomicJ.utilities.RomanNumeralConverter;
import atomicJ.utilities.Validation;

public class SliderMountedFilter implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final int positionIndex;
    private final Filter filter;

    public SliderMountedFilter(int positionIndex, Filter filter)
    {
        Validation.requireValueGreaterOrEqualToParameterName(positionIndex, 0, "positionIndex");
        this.positionIndex = positionIndex;
        this.filter = filter;
    }

    public int getPositionIndex()
    {
        return positionIndex;
    }

    public Filter getFilter()
    {
        return filter;
    }

    public boolean canFilterBeDescribedBy(String description)
    {
        return this.filter.canBeDescribedBy(description);
    }

    public String getDescription()
    {
        return buildDescription();
    }

    private String buildDescription()
    {
        String positionRoman = RomanNumeralConverter.convertToRoman(positionIndex + 1);
        String description = positionRoman + " (" + this.filter.getDescription() + ")";
        return description;
    }

    @Override
    public String toString()
    {
        return buildDescription();
    }
}