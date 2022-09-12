package chloroplastInterface;

import atomicJ.utilities.StringUtilities;

public interface PhotometricDescription
{
    static final String SPECIES_DESCRIPTION = "Species: ";
    static final String LINE_DESCRIPTION = "Line: ";
    static final String DARK_ADAPTED_DESCRIPTION = "Dark adapted: ";
    static final String ACTINIC_BEAM_PHASES = "Actinic beam phases irradiance ";

    public String getSpeciesName();
    public String getLineName();
    public boolean isDarkAdapted();
    public IrradianceUnitType getUnitType();
    public String getComments();

    public int getActinicBeamPhaseCount();
    public double getActinicBeamIrradianceValue(int phaseIndex);

    public default String getDescription()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append(SPECIES_DESCRIPTION).append(getSpeciesName()).append(' ').
        append(LINE_DESCRIPTION).append(getLineName()).append(' ').append(DARK_ADAPTED_DESCRIPTION).append(isDarkAdapted()).append('\n');

        String comments = getComments();
        if(!StringUtilities.isNullOrEmpty(comments))
        {
            buffer.append(comments).append('\n');
        }

        buffer.append(ACTINIC_BEAM_PHASES).append(getUnitType().getUnit()).append(":");

        int actinicBeamCount = getActinicBeamPhaseCount();
        for(int i = 0; i<actinicBeamCount; i++)
        {
            Double intensityValue = getActinicBeamIrradianceValue(i);
            String intensity = intensityValue.isNaN() ? "" : Double.toString(intensityValue);
            buffer.append(intensity).append('\t');
        }

        return buffer.toString();
    }
}