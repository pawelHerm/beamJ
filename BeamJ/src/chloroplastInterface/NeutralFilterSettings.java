package chloroplastInterface;

import java.io.Serializable;

import atomicJ.utilities.Validation;
import chloroplastInterface.optics.NeutralDensityFilter;

public class NeutralFilterSettings implements Serializable
{
    private static final long serialVersionUID = 1L;

    private double transmittance;

    public NeutralFilterSettings(double transmittance)
    {
        Validation.requireValueEqualToOrBetweenBounds(transmittance, 0, 100, "transmittance");
        this.transmittance = transmittance;
    }

    public double getTransmittanceInPercents()
    {
        return transmittance;
    }

    public void setTransmittanceInPercents(double transmittanceNew)
    {
        Validation.requireValueEqualToOrBetweenBounds(transmittanceNew, 0, 100, "transmittanceNew");

        this.transmittance = transmittanceNew;
    }

    public double getOpticalDensity()
    {
        double opticalDensity = Double.isNaN(transmittance) ? Double.NaN : -Math.log10(transmittance*0.01);
        return opticalDensity;
    }

    public boolean isWellSpecified()
    {
        boolean wellSpecified = Double.isNaN(this.transmittance);
        return wellSpecified;
    }

    public NeutralDensityFilter buildImmutableFilter()
    {
        NeutralDensityFilter immutableCopy = new NeutralDensityFilter(this.transmittance);
        return immutableCopy;
    }
}
