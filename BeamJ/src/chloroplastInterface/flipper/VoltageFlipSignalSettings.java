package chloroplastInterface.flipper;

import java.io.Serializable;
import java.util.Objects;

import chloroplastInterface.StandardTimeUnit;

public class VoltageFlipSignalSettings implements FlipAssociatedSignalSettings, Serializable
{
    private static final long serialVersionUID = 1L;

    private boolean sendSignalAfterFlip;

    private double flipLagTimeValue = 0;
    private StandardTimeUnit flipVoltageLagTimeUnit = StandardTimeUnit.SECOND;
    private double flipVoltageValue = 0;

    private double voltageSignalDurationValue = 0;
    private StandardTimeUnit voltageSignalDurationTimeUnit = StandardTimeUnit.SECOND;

    public VoltageFlipSignalSettings.FlipVoltageSignalImmutable getImmutable()
    {
        VoltageFlipSignalSettings.FlipVoltageSignalImmutable copy = new FlipVoltageSignalImmutable(this);
        return copy;
    }

    @Override
    public boolean isSendSignalAfterFlip()
    {
        return sendSignalAfterFlip;
    }

    public void setSendSignalAfterFlip(boolean sendVoltageSignalAfterFlipNew)
    {
        this.sendSignalAfterFlip = sendVoltageSignalAfterFlipNew;
    }

    public double getLagValue()
    {
        return flipLagTimeValue;
    }

    public void setLagValue(double flipLagTimeValueNew)
    {
        this.flipLagTimeValue = flipLagTimeValueNew;
    }

    public StandardTimeUnit getLagTimeUnit()
    {
        return flipVoltageLagTimeUnit;
    }

    public void setLagUnit(StandardTimeUnit flipVoltageLagTimeUnitNew)
    {
        this.flipVoltageLagTimeUnit = flipVoltageLagTimeUnitNew;
    }

    @Override
    public double getLagTimeInMilliseconds()
    {
        double lagInMiliseconds = flipLagTimeValue*this.flipVoltageLagTimeUnit.getConversionFactorToMilliseconds();
        return lagInMiliseconds;
    }

    public double getSignalVoltageValue()
    {
        return flipVoltageValue;
    }

    public void setSignalVoltageValue(double flipVoltageValueNew)
    {
        this.flipVoltageValue = flipVoltageValueNew;
    }

    public double getVoltageSignalDurationValue()
    {
        return voltageSignalDurationValue;
    }

    public void setVoltageSignalDurationValue(double voltageSignalDurationValueNew)
    {
        this.voltageSignalDurationValue = voltageSignalDurationValueNew;
    }

    public StandardTimeUnit getSignalDurationUnit()
    {
        return voltageSignalDurationTimeUnit;
    }

    public void setSignalDurationUnit(StandardTimeUnit voltageSignalDurationTimeUnitNew)
    {
        this.voltageSignalDurationTimeUnit = voltageSignalDurationTimeUnitNew;
    }

    @Override
    public double getSignalDurationInMilliseconds()
    {
        double signalDurationInMiliseconds = voltageSignalDurationValue*this.voltageSignalDurationTimeUnit.getConversionFactorToMilliseconds();
        return signalDurationInMiliseconds;
    }

    public static final class FlipVoltageSignalImmutable
    {
        private final boolean sendVoltageSignalAfterFlip;

        private final double flipVoltageLagTimeValue;
        private final StandardTimeUnit flipVoltageLagTimeUnit;
        private final double flipVoltageValue;

        private final double voltageSignalDurationValue;
        private final StandardTimeUnit voltageSignalDurationTimeUnit;

        public FlipVoltageSignalImmutable(VoltageFlipSignalSettings originalSignal)
        {
            this.sendVoltageSignalAfterFlip = originalSignal.sendSignalAfterFlip;
            this.flipVoltageLagTimeValue = originalSignal.flipLagTimeValue;
            this.flipVoltageLagTimeUnit = originalSignal.flipVoltageLagTimeUnit;
            this.flipVoltageValue = originalSignal.flipVoltageValue;

            this.voltageSignalDurationValue = originalSignal.voltageSignalDurationValue;
            this.voltageSignalDurationTimeUnit = originalSignal.voltageSignalDurationTimeUnit;
        }

        public boolean isSendVoltageSignalAfterFlip()
        {
            return sendVoltageSignalAfterFlip;
        }

        public double getFlipVoltageLagValue()
        {
            return flipVoltageLagTimeValue;
        }

        public StandardTimeUnit getFlipVoltageLagTimeUnit()
        {
            return flipVoltageLagTimeUnit;
        }

        public double isFlipVoltageValue()
        {
            return flipVoltageValue;
        }

        public double getVoltageSignalDurationValue()
        {
            return voltageSignalDurationValue;
        }

        public StandardTimeUnit getVoltageSignalDurationTimeUnit()
        {
            return voltageSignalDurationTimeUnit;
        }

        @Override
        public boolean equals(Object other)
        {
            if(!(other instanceof VoltageFlipSignalSettings.FlipVoltageSignalImmutable))
            {
                return false;
            }

            VoltageFlipSignalSettings.FlipVoltageSignalImmutable that = (VoltageFlipSignalSettings.FlipVoltageSignalImmutable)other;

            if(this.sendVoltageSignalAfterFlip != that.sendVoltageSignalAfterFlip)
            {
                return false;
            }

            if(Double.compare(this.flipVoltageLagTimeValue, that.flipVoltageLagTimeValue) != 0)
            {
                return false;
            }

            if(!Objects.equals(this.flipVoltageLagTimeUnit, that.flipVoltageLagTimeUnit))
            {
                return false;
            }

            if(Double.compare(this.flipVoltageValue, that.flipVoltageValue) != 0)
            {
                return false;
            }

            if(Double.compare(this.voltageSignalDurationValue, that.voltageSignalDurationValue) != 0)
            {
                return false;
            }

            if(!Objects.equals(this.voltageSignalDurationTimeUnit, that.voltageSignalDurationTimeUnit))
            {
                return false;
            }          

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = Boolean.hashCode(sendVoltageSignalAfterFlip);

            result = 31*result + Double.hashCode(flipVoltageLagTimeValue);
            result = 31*result + Objects.hashCode(flipVoltageLagTimeUnit);
            result = 31*result + Double.hashCode(flipVoltageValue);
            result = 31*result + Double.hashCode(voltageSignalDurationValue);
            result = 31*result + Objects.hashCode(voltageSignalDurationTimeUnit);

            return result;
        }
    }
}