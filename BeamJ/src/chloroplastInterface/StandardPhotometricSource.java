package chloroplastInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import atomicJ.data.Channel1D;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardSampleCollection;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.Quantity;
import atomicJ.sources.AbstractChannelSource;
import atomicJ.utilities.Validation;
import chloroplastInterface.ExperimentDescriptionModel.PhotometricDescriptionImmutable;

public class StandardPhotometricSource extends AbstractChannelSource<Channel1D> implements SimplePhotometricSource
{
    private final List<Channel1D> recordedSignalChannels;

    private final List<ActinicPhaseSettingsImmutable> actinicBeamPhases;
    private final MeasuringBeamSettingsImmutable measuringBeamSettings;
    private final List<SignalSettingsImmutable> signalSettings;
    private final PhotometricDescriptionImmutable description;

    private ProcessingBatchMementoPhotometric memento;

    public StandardPhotometricSource(File defaultOutputLocation, String shortName, String longName, 
            Collection<? extends Channel1D> recordedSignals, List<ActinicPhaseSettingsImmutable> actinicBeamPhases, MeasuringBeamSettingsImmutable measuringBeamSettings, 
            List<SignalSettingsImmutable> signalSettings, PhotometricDescriptionImmutable descriptionModel)
    {
        super(defaultOutputLocation, shortName, longName);

        this.recordedSignalChannels = new ArrayList<>(recordedSignals);
        this.actinicBeamPhases = actinicBeamPhases;
        this.measuringBeamSettings = measuringBeamSettings;
        this.signalSettings = new ArrayList<>(signalSettings);
        this.description = descriptionModel;
    }

    public StandardPhotometricSource(StandardPhotometricSource that)
    {
        super(that);

        this.recordedSignalChannels = new ArrayList<>();
        for(Channel1D channel : that.recordedSignalChannels)
        {
            this.recordedSignalChannels.add(channel.getCopy());
        }

        this.signalSettings = new ArrayList<>(that.signalSettings);
        this.actinicBeamPhases = new ArrayList<>(that.actinicBeamPhases);
        this.measuringBeamSettings = that.measuringBeamSettings;
        this.description = that.description;
    }

    @Override
    public int getRecordedSignalCount()
    {
        int signalCount = signalSettings.size();

        return signalCount;
    }

    @Override
    public List<ActinicPhaseSettingsImmutable> getActinicBeamPhaseSettings()
    {
        return Collections.unmodifiableList(this.actinicBeamPhases);
    }

    @Override
    public double getTotalDurationOfActinicPhasesInSeconds()
    {
        double duration = calculateTotalDurationOfActinicPhasesInMiliseconds()/1000.;
        return duration;
    }

    private double calculateTotalDurationOfActinicPhasesInMiliseconds()
    {
        double duration = 0;

        for(ActinicPhaseSettingsImmutable phase : actinicBeamPhases)
        {
            duration = duration + phase.getDurationInMiliseconds();
        }

        return duration;
    }

    @Override
    public MeasuringBeamSettingsImmutable getMeasuringBeamSettings()
    {
        return measuringBeamSettings;
    }

    @Override
    public SignalSamplingSettingsImmutable getSignalSettings(int signalIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSettings.size() - 1, "signalIndex");
        SignalSamplingSettingsImmutable signalSamplingSettings = signalSettings.get(signalIndex).getSamplingSettings();

        return signalSamplingSettings;
    }

    @Override
    public List<SampleCollection> getSampleCollections() 
    { 
        List<SampleCollection> collections = new ArrayList<>();

        for(Channel1D channel : recordedSignalChannels)
        {
            SampleCollection collection = new StandardSampleCollection(channel.getSamples(), getShortName(), getShortName(), getDefaultOutputLocation());
            collections.add(collection);
        }

        return collections;
    }
    
    @Override
    public LightSignalType getSignalType(int signalIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSettings.size() - 1, "signalIndex");

        LightSignalType signalType = signalSettings.get(signalIndex).getSignalType();
        return signalType;
    }

    @Override
    public boolean isCalibrated(int signalIndex) 
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSettings.size() - 1, "signalIndex");

        Channel1D channel = recordedSignalChannels.get(signalIndex);
        boolean calbrated = !channel.getYQuantity().hasDimension();
        return calbrated;
    }

    @Override
    public boolean isCalibrationKnown(int signalIndex) 
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSettings.size() - 1, "signalIndex");
        CalibrationSettingsImmutable calibrationSettings = signalSettings.get(signalIndex).getCalibrationSettings();

        boolean calibrationKnown = calibrationSettings.isCalibrationKnown();
        return calibrationKnown;
    }

    @Override
    public CalibrationSettingsImmutable getCalibrationSettings(int signalIndex) 
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSettings.size() - 1, "signalIndex");
        CalibrationSettingsImmutable calibrationSettings = signalSettings.get(signalIndex).getCalibrationSettings();

        return calibrationSettings;
    }

    @Override
    public SimplePhotometricSource copy() 
    {
        return new StandardPhotometricSource(this);
    }

    @Override
    public List<String> getIdentifiers() 
    {
        List<String> identifiers = new ArrayList<>();
        for(Channel1D channel : recordedSignalChannels)
        {
            identifiers.add(channel.getIdentifier());
        }
        return identifiers;
    }

    @Override
    public List<Channel1D> getChannels() 
    {
        List<Channel1D> channels = new ArrayList<>(recordedSignalChannels);
        return channels;
    }

    @Override
    public List<Channel1D> getChannels(Collection<String> identifiers) 
    {
        Validation.requireNonNullParameterName(identifiers, "identifiers");

        List<Channel1D> selectedChannels = new ArrayList<>();
        for(Channel1D channel : recordedSignalChannels)
        {
            String id = channel.getIdentifier();
            if(identifiers.contains(id))
            {
                selectedChannels.add(channel);
            }
        }

        return selectedChannels;
    }

    @Override
    public PrefixedUnit getSingleDataUnit()
    {
        PrefixedUnit singleUnit = null;

        Iterator<Channel1D> it = recordedSignalChannels.iterator();
        if(it.hasNext())
        {
            singleUnit = it.next().getYQuantity().getUnit();

            while(it.hasNext())
            {
                if(!Objects.equals(singleUnit, it.next().getYQuantity().getUnit()))
                {
                    singleUnit = null;
                    break;
                }
            }

        }

        return singleUnit;
    }

    @Override
    public Channel1D getRecordedChannel(int signalIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, recordedSignalChannels.size() - 1, "signalIndex");
        return recordedSignalChannels.get(signalIndex);
    }
 
    @Override
    public PhotometricDescriptionImmutable getDescription()
    {
        return description;
    }

    @Override
    public ProcessingBatchMementoPhotometric getProcessingMemento()
    {
        return memento;
    }

    @Override
    public void setProcessingMemento(ProcessingBatchMementoPhotometric memento)
    {
        this.memento = memento;
    }

    //copies are keys, originals are values
    public static Map<SimplePhotometricSource, SimplePhotometricSource> copySources(List<SimplePhotometricSource> sourcesOld)
    {
        Map<SimplePhotometricSource, SimplePhotometricSource> copies = new LinkedHashMap<>();

        for(SimplePhotometricSource source: sourcesOld)
        {
            SimplePhotometricSource sourceCopy = source.copy();
            copies.put(sourceCopy, source);

        }

        return copies;
    }
}
