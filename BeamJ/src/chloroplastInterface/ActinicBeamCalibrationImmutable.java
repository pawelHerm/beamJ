package chloroplastInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import chloroplastInterface.optics.SliderMountedFilter;

public class ActinicBeamCalibrationImmutable
{
    private final List<ActinicCalibrationPointImmutable> actinicBeamCalibrationPoint;
    private final IrradianceUnitType absoluteLightIntensityUnit;

    public ActinicBeamCalibrationImmutable(List<ActinicCalibrationPointImmutable> actinicBeamPhases, IrradianceUnitType absoluteLightIntensityUnit)
    {
        this.actinicBeamCalibrationPoint = new ArrayList<>(actinicBeamPhases);
        this.absoluteLightIntensityUnit = absoluteLightIntensityUnit;
    }

    public IrradianceUnitType getAbsoluteLightIntensityUnit()
    {
        return absoluteLightIntensityUnit;
    }

    public List<ActinicCalibrationPointImmutable> getActinicBeamPhaseCalibrations()
    {
        return Collections.unmodifiableList(actinicBeamCalibrationPoint);
    }

    public double convertExactlyIntensityInPercentsToAbsoluteValue(double lightIntensityInPercents, SliderMountedFilter sliderPosition)
    {
        if(lightIntensityInPercents == 0)
        {
            return 0;
        }

        double intensityAbsolute = Double.NaN;

        for(ActinicCalibrationPointImmutable phase : actinicBeamCalibrationPoint)
        {
            if(phase.matches(lightIntensityInPercents, sliderPosition.getFilter()))
            {
                intensityAbsolute = phase.getLightIntensityInAbsoluteUnits();
                break;
            }
        }

        return intensityAbsolute;
    }
}