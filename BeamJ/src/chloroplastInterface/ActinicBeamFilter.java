package chloroplastInterface;

import java.text.NumberFormat;
import java.util.Locale;

public enum ActinicBeamFilter
{
    FIRST(1,100,"I"), SECOND(2,10,"II"), THIRD(3,1,"III"), FOURTH(4,0.1,"IV");

    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance(Locale.US);

    private final String positionDescription;
    private final double transmittanceInPercents;
    private final int position;

    private ActinicBeamFilter(int position, double transmittance, String positionDescription)
    {
        this.position = position;
        this.transmittanceInPercents = transmittance;
        this.positionDescription = positionDescription;
    }

    public int getPosition()
    {
        return position;
    }

    public String getPositionDescription()  
    {
        return positionDescription;
    }

    public double getTransmittanceInPercents()
    {
        return transmittanceInPercents;
    }

    @Override
    public String toString()
    {
        String desc = positionDescription +" ("+ FORMAT.format(transmittanceInPercents)+"%)";
        return desc;
    }    
}