package chloroplastInterface;

import java.util.Objects;

import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.Validation;
import chloroplastInterface.optics.Filter;
import chloroplastInterface.optics.SliderMountedFilter;

public class ActinicCalibrationPointImmutable
{
    private static final double TOLERANCE = 1e-10;

    private final SliderMountedFilter filter;
    private final double intensityInAbsoluteUnits;
    private final double intensityInPercent;

    public ActinicCalibrationPointImmutable(double intensityInPercent, double intensityInAbsoluteUnits, SliderMountedFilter filter)
    {
        Validation.requireValueEqualToOrBetweenBounds(intensityInPercent, 0, 100, "intensityInPercent");
        Validation.requireNonNegativeParameterName(intensityInAbsoluteUnits, "intensityInAbsoluteUnits");
        Validation.requireNonNullParameterName(filter, "filter");

        this.intensityInPercent = intensityInPercent;
        this.intensityInAbsoluteUnits = intensityInAbsoluteUnits;
        this.filter = filter;
    }

    public double getLightIntensityInAbsoluteUnits()
    {
        return intensityInAbsoluteUnits;
    }

    public double getLightIntensityInPercent()
    {
        return intensityInPercent;
    }

    public SliderMountedFilter getFilter()
    {
        return filter;
    }

    public boolean matches(double intensityInPercentOther, Filter filterOther)
    {        
        boolean intensityEqual = MathUtilities.equalWithinTolerance(this.intensityInPercent, intensityInPercentOther, TOLERANCE);
        boolean filterEqual = Objects.equals(this.filter, filterOther);
        boolean matches = intensityEqual&&filterEqual;
        return matches;
    }

    public boolean isWellSpecified()
    {
        boolean wellSpecified = !Double.isNaN(intensityInAbsoluteUnits) && !Double.isNaN(intensityInPercent);
        return wellSpecified;
    }
}