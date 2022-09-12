package chloroplastInterface;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.filechooser.FileFilter;

import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.save.SavingException;
import atomicJ.utilities.SerializationUtilities;
import atomicJ.utilities.Validation;
import chloroplastInterface.optics.Filter;
import chloroplastInterface.optics.NullFilter;
import chloroplastInterface.optics.SliderMountedFilter;

public class ActinicBeamManualCalibrationModel 
{
    private final static String ACTINIC_BEAM_CALIBRATION = "ActinicBeamCalibration";
    private final static String ABSOLUTE_LIGHT_INTENSITY_UNIT = "AbsoluteLightIntensityUnit";

    private final static Preferences PREF = Preferences.userNodeForPackage(ActinicBeamManualCalibrationModel.class).node(ActinicBeamManualCalibrationModel.class.getName());

    private List<ActinicBeamCalibrationPoint> actinicBeamCalibrationPoints;
    private final List<ActinicBeamCalibrationModelListener> modelListeners = new ArrayList<>();

    private IrradianceUnitType absoluteLightIntensityUnit;

    private final SliderMountedFilter initFilter = new SliderMountedFilter(0, NullFilter.getInstance());
    private final double initIntensityInPercents = 50;
    private final double initIntensityInAbsoluteUnits = 120;

    private boolean readActinicBeamCalibrationPointsFromFileEnabled;

    private boolean savingToFileEnabled;

    public ActinicBeamManualCalibrationModel()
    {    
        this.actinicBeamCalibrationPoints = (List<ActinicBeamCalibrationPoint>)SerializationUtilities.getSerializableObject(PREF, ACTINIC_BEAM_CALIBRATION, new ArrayList<>(Arrays.asList(new ActinicBeamCalibrationPoint(initIntensityInPercents, initIntensityInAbsoluteUnits, initFilter))));
        this.absoluteLightIntensityUnit = IrradianceUnitType.getValue(PREF.get(ABSOLUTE_LIGHT_INTENSITY_UNIT, IrradianceUnitType.MICROMOLES_PER_SQUARE_METER_PER_SECOND.name()), IrradianceUnitType.MICROMOLES_PER_SQUARE_METER_PER_SECOND);
        updateSaveToFileEnabled();
    }

    public void close()
    {
        saveSettingsToPreferences();
    }

    private void saveSettingsToPreferences()
    {
        PREF.put(ABSOLUTE_LIGHT_INTENSITY_UNIT, absoluteLightIntensityUnit.name());
        try {
            SerializationUtilities.putSerializableObject(PREF, ACTINIC_BEAM_CALIBRATION, this.actinicBeamCalibrationPoints);
        } catch (ClassNotFoundException | IOException
                | BackingStoreException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        flushPreferences(PREF);
    }

    public double getLightIntensityInAbsoluteUnits(int calibrationPointIndex)
    {      
        Validation.requireValueEqualToOrBetweenBounds(calibrationPointIndex, 0, actinicBeamCalibrationPoints.size() - 1, "calibrationPointIndex");

        double lightIntensity = actinicBeamCalibrationPoints.get(calibrationPointIndex).getLightIntensityInAbsoluteUnits();

        return lightIntensity;
    }

    public void setLightIntensityInAbsoluteUnits(int calibrationPointIndex, double lightIntensityNew)
    {
        Validation.requireValueEqualToOrBetweenBounds(calibrationPointIndex, 0, actinicBeamCalibrationPoints.size() - 1, "calibrationPointIndex");
        Validation.requireValueGreaterOrEqualToParameterName(lightIntensityNew, 0, "lightIntensityNew");

        ActinicBeamCalibrationPoint calibrationPoint = actinicBeamCalibrationPoints.get(calibrationPointIndex);
        double lightIntensityOld = calibrationPoint.getLightIntensityInAbsoluteUnits();

        if(Double.compare(lightIntensityOld, lightIntensityNew) != 0)
        {
            calibrationPoint.setLightIntensityInAbsoluteUnits(lightIntensityNew);
            notifyAboutLightIntensityInAbsoluteUnitsChanged(calibrationPointIndex, lightIntensityOld, lightIntensityNew);
            updateSaveToFileEnabled();
        }
    }

    public IrradianceUnitType getAbsoluteLightIntensityUnit()
    {
        return absoluteLightIntensityUnit;
    }

    public void setAbsoluteLightIntensityUnit(IrradianceUnitType absoluteLightIntensityUnitNew)
    {
        Validation.requireNonNullParameterName(absoluteLightIntensityUnitNew, "absoluteLightIntensityUnitNew");

        IrradianceUnitType absoluteLightIntensityUnitOld = this.absoluteLightIntensityUnit;
        this.absoluteLightIntensityUnit = absoluteLightIntensityUnitNew;

        notifyAboutAbsoluteLightIntensityUnitChanged(absoluteLightIntensityUnitOld, absoluteLightIntensityUnitNew);
    }

    public SliderMountedFilter getActinicBeamFilter(int calibrationPointIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(calibrationPointIndex, 0, actinicBeamCalibrationPoints.size() - 1, "calibrationPointIndex");

        SliderMountedFilter sliderFilter = actinicBeamCalibrationPoints.get(calibrationPointIndex).getFilter();       
        return sliderFilter;
    }

    public void setActinicBeamFilter(int calibrationPointIndex, SliderMountedFilter filterNew)
    {
        Validation.requireValueEqualToOrBetweenBounds(calibrationPointIndex, 0, actinicBeamCalibrationPoints.size() - 1, "calibrationPointIndex");
        Validation.requireNonNullParameterName(filterNew, "filterNew");

        ActinicBeamCalibrationPoint calibrationPoint = actinicBeamCalibrationPoints.get(calibrationPointIndex);
        SliderMountedFilter filterOld = calibrationPoint.getFilter();

        if(!Objects.equals(filterOld, filterNew))
        {
            calibrationPoint.setFilter(filterNew);
            notifyAboutFilterChanged(calibrationPointIndex, filterOld, filterNew);
        }
    }

    public List<SliderMountedFilter> getAvailableActinicBeamSliderMountedFilters()
    {
        List<SliderMountedFilter> filters = Collections.unmodifiableList(ChloroplastJ.CURRENT_FRAME.getOpticsConfiguration().getAvailableActinicBeamSliderMountedFilters());

        return filters;
    }

    public double getLightIntensityInPercent(int calibrationPointIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(calibrationPointIndex, 0, actinicBeamCalibrationPoints.size() - 1, "calibrationPointIndex");

        double calibrationPointIntensityInPercent = actinicBeamCalibrationPoints.get(calibrationPointIndex).getLightIntensityInPercent();

        return calibrationPointIntensityInPercent;
    }

    public void setLightIntensityInPercent(int calibrationPointIndex, double lightIntensityInPercentNew)
    {
        Validation.requireValueEqualToOrBetweenBounds(calibrationPointIndex, 0, actinicBeamCalibrationPoints.size() - 1, "calibrationPointIndex");
        Validation.requireValueEqualToOrBetweenBounds(lightIntensityInPercentNew, 0., 100., "lightIntensityInPercentNew");

        ActinicBeamCalibrationPoint calibrationPoint = actinicBeamCalibrationPoints.get(calibrationPointIndex);
        double lightIntensityInPercentOld = calibrationPoint.getLightIntensityInPercent();

        if(Double.compare(lightIntensityInPercentOld, lightIntensityInPercentNew) != 0)
        {
            calibrationPoint.setLightIntensityInPercent(lightIntensityInPercentNew);
            notifyAboutActinicLightIntensityInPercentChanged(calibrationPointIndex, lightIntensityInPercentOld, lightIntensityInPercentNew);
            updateSaveToFileEnabled();
        }
    }

    public void readInActinicCalibrationPointsFromFile(File f) throws UserCommunicableException
    {               
        ActinicBeamCalibrationImmutable actnicBeamCalibration = ABCFileReader.getInstance().readActinicBeamSettings(f);

        List<ActinicCalibrationPointImmutable> actnicBeamCalibrationPoints = actnicBeamCalibration.getActinicBeamCalibrationPoints();
        int calibrationPointCountNew = actnicBeamCalibrationPoints.size();

        setCalibrationPointCount(calibrationPointCountNew);

        for(int i = 0; i<calibrationPointCountNew;i++)
        {
            ActinicCalibrationPointImmutable iThCalibrationPointSettings = actnicBeamCalibrationPoints.get(i);
            setLightIntensityInAbsoluteUnits(i, iThCalibrationPointSettings.getLightIntensityInAbsoluteUnits());
            setActinicBeamFilter(i, iThCalibrationPointSettings.getFilter());
            setLightIntensityInPercent(i, iThCalibrationPointSettings.getLightIntensityInPercent());
        }
    }

    public List<FileFilter> getFileFiltersForReadingActinicBeamCalibrationPoints()
    {
        return Collections.singletonList(CLMFileReaderFactory.getInstance().getFileFilter());
    }

    public int getCalibrationPointCount()
    {
        return actinicBeamCalibrationPoints.size();
    }

    public void setCalibrationPointCount(int calibrationPointCountNew)
    {        
        Validation.requireValueGreaterOrEqualToParameterName(calibrationPointCountNew, 0, "calibrationPointCountNew");

        int calibrationPointCountOld = actinicBeamCalibrationPoints.size();

        if(calibrationPointCountNew == calibrationPointCountOld)
        {
            return;
        }

        if(calibrationPointCountNew < calibrationPointCountOld)
        {
            this.actinicBeamCalibrationPoints = new ArrayList<>(actinicBeamCalibrationPoints.subList(0, calibrationPointCountNew));//we need to make sure it is arrray list for serialization purposes, as the returned sublist is not serializable
        }
        else if(calibrationPointCountNew > calibrationPointCountOld)
        {
            double intensityInPercent = calibrationPointCountOld > 0 ? actinicBeamCalibrationPoints.get(calibrationPointCountOld - 1).getLightIntensityInPercent(): initIntensityInPercents;
            double intensityInAbsoluteUnits = calibrationPointCountOld > 0 ? actinicBeamCalibrationPoints.get(calibrationPointCountOld - 1).getLightIntensityInAbsoluteUnits() : initIntensityInAbsoluteUnits;
            SliderMountedFilter filter = calibrationPointCountOld > 0 ? actinicBeamCalibrationPoints.get(calibrationPointCountOld - 1).getFilter() : initFilter;

            for(int i = 0; i<(calibrationPointCountNew - calibrationPointCountOld); i++)
            {
                ActinicBeamCalibrationPoint calibrationPointNew = new ActinicBeamCalibrationPoint(intensityInPercent, intensityInAbsoluteUnits, filter);
                this.actinicBeamCalibrationPoints.add(calibrationPointNew);
            }

            updateSaveToFileEnabled();
        }

        notifyAboutNumberOfCalibrationPointsChanged(calibrationPointCountOld, calibrationPointCountNew);
    }

    public List<ActinicCalibrationPointImmutable> getCalibrationPoints()
    {
        List<ActinicCalibrationPointImmutable> immutableCalibrationPoints = new ArrayList<>();

        for(ActinicBeamCalibrationPoint calibrationPoint : actinicBeamCalibrationPoints)
        {
            immutableCalibrationPoints.add(calibrationPoint.getImmutableCopy());
        }

        return immutableCalibrationPoints;
    }


    public boolean isReadActinicBeamCalibrationPointsFromFileEnabled()
    {
        return readActinicBeamCalibrationPointsFromFileEnabled;
    }

    public void addListener(ActinicBeamCalibrationModelListener listener)
    {
        modelListeners.add(listener);
    }

    public void removeListener(ActinicBeamCalibrationModelListener listener)
    {
        modelListeners.remove(listener);
    }

    private void notifyAboutNumberOfCalibrationPointsChanged(int oldNumber, int newNumber)
    {
        if(oldNumber != newNumber)
        {
            for(ActinicBeamCalibrationModelListener receiver: modelListeners)
            {
                receiver.numberOfCalibrationPointsChanged(oldNumber, newNumber);
            }
        }
    }

    private void notifyAboutFilterChanged(int calibrationPointIndex, SliderMountedFilter filterOld, SliderMountedFilter filterNew)
    {
        if(!Objects.equals(filterOld, filterNew))
        {
            for(ActinicBeamCalibrationModelListener listener: modelListeners)
            {
                listener.filterChanged(calibrationPointIndex, filterOld, filterNew);
            }
        }
    }

    private void notifyAboutActinicLightIntensityInPercentChanged(int calibrationPointIndex, double intensityInPercentOld,double intensityInPercentNew)
    {
        //we use Double.compare as it behaves predictably for NaNs
        if(Double.compare(intensityInPercentOld, intensityInPercentNew) != 0)
        {
            for(ActinicBeamCalibrationModelListener listener: modelListeners)
            {
                listener.actinicLightIntensityInPercentChanged(calibrationPointIndex, intensityInPercentOld, intensityInPercentNew);
            }
        }
    }

    private void notifyAboutLightIntensityInAbsoluteUnitsChanged(int calibrationPointIndex, double lightIntensityOld, double lightIntensityNew)
    {
        for(ActinicBeamCalibrationModelListener listener : modelListeners)
        {
            listener.lightIntensityInAbsoluteUnitsChanged(calibrationPointIndex, lightIntensityOld, lightIntensityNew);
        }
    }

    private void notifyAboutAbsoluteLightIntensityUnitChanged(IrradianceUnitType absoluteLightIntensityUnitOld, IrradianceUnitType absoluteLightIntensityUnitNew)
    {
        for(ActinicBeamCalibrationModelListener listener : modelListeners)
        {
            listener.absoluteLightIntensityUnitChanged(absoluteLightIntensityUnitOld, absoluteLightIntensityUnitNew);
        }
    }

    public void saveToFile(File outputFile) throws UserCommunicableException
    {
        if(!savingToFileEnabled)
        {
            throw new IllegalStateException("Saving of actinic beam calibration to file is not possible");
        }

        try {
            ABCSaver.getInstance().save(getImmutableCopy(), outputFile);
        } catch (SavingException e) {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured during parsing the settings", e);
        }
    }

    public boolean isSaveToFileEnabled()
    {
        return savingToFileEnabled;
    }

    public void setSaveToFileEnabled(boolean savingToFileEnabledNew)
    {
        if(this.savingToFileEnabled != savingToFileEnabledNew)
        {
            boolean savingToFileEnabledOld = this.savingToFileEnabled;
            this.savingToFileEnabled = savingToFileEnabledNew;

            notifyAboutSaveToFileEnabledChanged(savingToFileEnabledOld, savingToFileEnabledNew);
        }
    }

    private void updateSaveToFileEnabled()
    {
        boolean necessaryDataKnown = this.absoluteLightIntensityUnit != null;
        if(necessaryDataKnown)
        {
            for(ActinicBeamCalibrationPoint calibrationPoint : actinicBeamCalibrationPoints)
            {
                necessaryDataKnown = necessaryDataKnown && calibrationPoint.isWellSpecified();
                if(!necessaryDataKnown)
                {
                    break;
                }
            }
        }

        setSaveToFileEnabled(necessaryDataKnown);
    }

    public void notifyAboutSaveToFileEnabledChanged(boolean savingToFileEnabledOld, boolean savingToFileEnabledNew)
    {
        for(ActinicBeamCalibrationModelListener listener : modelListeners)
        {
            listener.saveToFileEnabledChanged(savingToFileEnabledOld, savingToFileEnabledNew);
        }
    }

    private void flushPreferences(Preferences pref)
    {
        try {
            PREF.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public ActinicBeamCalibrationImmutable getImmutableCopy()
    {
        ActinicBeamCalibrationImmutable immutableCopy = new ActinicBeamCalibrationImmutable(getCalibrationPoints(), absoluteLightIntensityUnit);
        return immutableCopy;
    }

    public static class ActinicBeamCalibrationImmutable
    {
        private final List<ActinicCalibrationPointImmutable> actinicBeamCalibrationPoints;
        private final IrradianceUnitType absoluteLightIntensityUnit;

        public ActinicBeamCalibrationImmutable(List<ActinicCalibrationPointImmutable> actinicBeamCalibrationPoints, IrradianceUnitType absoluteLightIntensityUnit)
        {
            this.actinicBeamCalibrationPoints = new ArrayList<>(actinicBeamCalibrationPoints);
            this.absoluteLightIntensityUnit = absoluteLightIntensityUnit;
        }

        public IrradianceUnitType getAbsoluteLightIntensityUnit()
        {
            return absoluteLightIntensityUnit;
        }

        public List<ActinicCalibrationPointImmutable> getActinicBeamCalibrationPoints()
        {
            return Collections.unmodifiableList(actinicBeamCalibrationPoints);
        }

        public double convertExactlyIntensityInPercentsToAbsoluteValue(double lightIntensityInPercents, Filter filter)
        {
            if(lightIntensityInPercents == 0)
            {
                return 0;
            }

            double intensityAbsolute = Double.NaN;

            for(ActinicCalibrationPointImmutable calibrationPoints : actinicBeamCalibrationPoints)
            {
                if(calibrationPoints.matches(lightIntensityInPercents, filter))
                {
                    intensityAbsolute = calibrationPoints.getLightIntensityInAbsoluteUnits();
                    break;
                }
            }

            return intensityAbsolute;
        }
    }

    public static class ActinicBeamCalibrationPoint implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private SliderMountedFilter filter;
        private double intensityInAbsoluteUnits;
        private double intensityInPercent;

        public ActinicBeamCalibrationPoint(double intensityInPercent, double intensityInAbsoluteUnits, SliderMountedFilter filter)
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

        public void setLightIntensityInAbsoluteUnits(double intensityInAbsoluteUnits)
        {
            this.intensityInAbsoluteUnits = intensityInAbsoluteUnits;
        }

        public double getLightIntensityInPercent()
        {
            return intensityInPercent;
        }

        public void setLightIntensityInPercent(double intensityInPercent)
        {
            this.intensityInPercent = intensityInPercent;
        }   

        public SliderMountedFilter getFilter()
        {
            return filter;
        }

        public void setFilter(SliderMountedFilter filter)
        {
            this.filter = filter;
        }

        public boolean isWellSpecified()
        {
            boolean wellSpecified = !Double.isNaN(intensityInAbsoluteUnits) && !Double.isNaN(intensityInPercent);
            return wellSpecified;
        }

        public ActinicCalibrationPointImmutable getImmutableCopy()
        {
            ActinicCalibrationPointImmutable immutableCopy = new ActinicCalibrationPointImmutable(intensityInPercent, intensityInAbsoluteUnits, filter);
            return immutableCopy;
        }
    }
}
