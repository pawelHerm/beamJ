package chloroplastInterface.optics;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import atomicJ.utilities.Validation;

public class NeutralDensityFilter implements Filter, Serializable
{
    private static final long serialVersionUID = 1L;
    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance(Locale.US);
    private final double transmittanceInPercents;

    public NeutralDensityFilter(double transmittanceInPercents)
    {
        Validation.requireValueEqualToOrBetweenBounds(transmittanceInPercents, 0, 100, "transmittance");

        this.transmittanceInPercents = transmittanceInPercents;
    }

    public double getNominalTransmittanceInPercents()
    {
        return transmittanceInPercents;
    }       

    public double getOpticalDensity()
    {
        double OD = Double.isNaN(transmittanceInPercents) ? Double.NaN : -Math.log10(transmittanceInPercents*0.01);
        return OD;
    }

    public boolean isWellSpecified()
    {
        boolean wellSpecified = Double.isNaN(this.transmittanceInPercents);
        return wellSpecified;
    }

    @Override
    public String getDescription()
    {
        String description = Double.isNaN(transmittanceInPercents) ? "" : FORMAT.format(transmittanceInPercents) + " %";
        return description;
    }

    @Override
    public boolean canBeDescribedBy(String description) 
    {
        Validation.requireNonNullParameterName(description, "description");

        boolean canBeDescribed = Objects.equals(getDescription(), description.trim());
        return canBeDescribed;
    }
}