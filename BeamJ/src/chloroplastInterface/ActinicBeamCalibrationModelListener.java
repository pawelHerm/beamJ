package chloroplastInterface;

import chloroplastInterface.optics.SliderMountedFilter;

public interface ActinicBeamCalibrationModelListener
{
    public void numberOfCalibrationPointsChanged(int oldNumber, int newNumber);
    public void actinicLightIntensityInPercentChanged(int calibrationPointIndex, double intensityInPercentOld,double intensityInPercentNew);
    public void lightIntensityInAbsoluteUnitsChanged(int calibrationPointIndex, double lightIntensityOld, double lightIntensityNew);
    public void filterChanged(int calibrationPointIndex, SliderMountedFilter filterOld, SliderMountedFilter filterNew);
    public void absoluteLightIntensityUnitChanged(IrradianceUnitType absoluteLightIntensityUnitOld, IrradianceUnitType absoluteLightIntensityUnitNew);
    public void saveToFileEnabledChanged(boolean enabledOld, boolean enabledNew);
}