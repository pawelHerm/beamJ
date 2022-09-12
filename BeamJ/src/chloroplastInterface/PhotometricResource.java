package chloroplastInterface;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.Channel1DDataInROITransformation;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.SampleCollection;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIComposite;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.gui.undo.UndoManager;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.resources.AbstractResource;
import atomicJ.resources.Channel1DResource;
import atomicJ.resources.DataModelResource;
import atomicJ.utilities.MetaMap;
import atomicJ.utilities.MultiMap;

public class PhotometricResource extends AbstractResource implements DataModelResource, Channel1DResource<SimplePhotometricSource>
{
    public static final String RECORDED_CURVE = "Recorded curve";

    private final MetaMap<String, Object, DistanceMeasurementDrawable> distanceMeasurements = new MetaMap<>();
    private final UndoManager undoManager = new UndoManager(2);
    private final SimplePhotometricSource source;

    public PhotometricResource(SimplePhotometricSource source)
    {
        super(source.getDefaultOutputLocation(), source.getShortName(), source.getLongName());

        this.source = source;
    }

    public PhotometricResource(PhotometricResource that) 
    {
        super(that);        
        this.source = that.source.copy();
    }

    @Override
    public SimplePhotometricSource getSource()
    {
        return source;
    }

    @Override
    public List<SampleCollection> getSampleCollections() 
    {
        return source.getSampleCollections();
    }

    @Override
    public List<String> getAllTypes() 
    {
        return Collections.singletonList(RECORDED_CURVE);
    }

    @Override
    public Set<String> getIdentifiers(String type) 
    {
        Set<String> identifiers = RECORDED_CURVE.equals(type) ? new LinkedHashSet<>(source.getIdentifiers()) : Collections.emptySet();
        return identifiers;
    }

    @Override
    public Set<String> getIdentifiers(String type, ChannelFilter2<Channel1D> filter) 
    {
        if(!RECORDED_CURVE.equals(type))
        {
            return Collections.emptySet();
        }


        List<? extends Channel1D> allChannels = source.getChannels();
        Set<String> filteredIdentifiers = new LinkedHashSet<>();

        for(Channel1D channel : allChannels)
        {
            if(filter.accepts(channel))
            {
                filteredIdentifiers.add(channel.getIdentifier());
            }
        }

        return filteredIdentifiers;
    }

    @Override
    public Set<String> getIdentifiersForAllTypes() 
    {
        return new LinkedHashSet<>(source.getIdentifiers());
    }

    @Override
    public Set<String> getIdentifiersForAllTypes(ChannelFilter2<Channel1D> filter) 
    {
        List<? extends Channel1D> allChannels = source.getChannels();
        Set<String> filteredIdentifiers = new LinkedHashSet<>();

        for(Channel1D channel : allChannels)
        {
            if(filter.accepts(channel))
            {
                filteredIdentifiers.add(channel.getIdentifier());
            }
        }

        return filteredIdentifiers;
    }

    //returns a map with values being those channels whose identifiers are in the set 'identifiers'. The keys are the corresponding identifiers
    //A multimap is returned, because this TypeModelManager may contain two different sources, with channels of the same identifier
    @Override
    public MultiMap<String, Channel1D> getChannelsForIdentifiers(Set<String> identifiers) 
    {
        MultiMap<String, Channel1D> channelsForIdentifiers = new MultiMap<>();
        List<? extends Channel1D> channels = source.getChannels(identifiers);

        for(Channel1D channel : channels)
        {
            channelsForIdentifiers.put(channel.getIdentifier(), channel);
        }

        return channelsForIdentifiers;
    }

    @Override
    public Map<String, Channel1DData> getChannelData(String type) 
    {
        Map<String, Channel1D> channels = getChannels(type);
        Map<String, Channel1DData> dataMap = new LinkedHashMap<>();

        for (Entry<String, Channel1D> entry : channels.entrySet()) 
        {
            String key = entry.getKey();

            Channel1D channel = entry.getValue();
            Channel1DData basicData = channel.getChannelData();
            dataMap.put(key, basicData);
        }

        return dataMap;
    }

    @Override
    public Map<String, Channel1D> setChannelData(String type, Map<String, Channel1DData> dataMap) 
    {
        Map<String, Channel1D> changedChannels = getChannels(type);

        for (Entry<String, Channel1D> entry : changedChannels.entrySet()) 
        {
            String key = entry.getKey();
            Channel1D channel = entry.getValue();
            changedChannels.put(key, channel);

            Channel1DData basicData = dataMap.get(key);
            if(basicData != null)
            {
                channel.setChannelData(basicData);           
            }
        }

        return changedChannels;
    }

    @Override
    public void setUndoSizeLimit(int sizeLimit)
    {
        undoManager.setSizeLimit(sizeLimit);
    }

    @Override
    public void pushCommand(String type, UndoableCommand command)
    {
        undoManager.push(type, command);
    }

    @Override
    public boolean canBeRedone(String type)
    {
        return undoManager.canBeRedone(type);
    }

    @Override
    public void redo(String type)
    {
        undoManager.redo(type);
    }

    @Override
    public boolean canBeUndone(String type)
    {
        return undoManager.canBeUndone(type);
    }

    @Override
    public void undo(String type)
    {
        undoManager.undo(type);
    }

    @Override
    public CommandIdentifier getCommandToRedoCompundIdentifier(String type)
    {
        return undoManager.getCommandToRedoCompundIdentifier(type);
    }

    @Override
    public CommandIdentifier getCommandToUndoCompundIdentifier(String type) 
    {
        return undoManager.getCommandToUndoCompundIdentifier(type);
    }

    @Override
    public ROI getROIUnion() 
    {
        return new ROIComposite("All");
    }

    @Override
    public PrefixedUnit getSingleDataUnit(String type)
    {
        return source.getSingleDataUnit();
    }

    @Override
    public Map<String, Channel1D> getChannels(String type)
    {
        Map<String, Channel1D> map = new LinkedHashMap<>();
        List<? extends Channel1D> channels = source.getChannels();

        for(Channel1D channel : channels)
        {
            map.put(channel.getIdentifier(), channel);
        }

        return map;
    }

    @Override
    public Map<String, Channel1D> transform(String type, Channel1DDataTransformation tr) 
    {
        Map<String, Channel1D> channelsForType = getChannels(type);

        for(Entry<String, Channel1D> entry : channelsForType.entrySet())
        {
            Channel1D channel = entry.getValue();
            channel.transform(tr);
        }        

        return channelsForType;
    }

    @Override
    public Map<String, Channel1D> transform(String type, Set<String> identifiers, Channel1DDataTransformation tr) 
    {
        Map<String, Channel1D> channelsForType = getChannels(type);
        Map<String, Channel1D> changedChannels = new LinkedHashMap<>();

        for(Entry<String, Channel1D> entry : channelsForType.entrySet())
        {
            Channel1D channel = entry.getValue();
            if(identifiers.contains(channel.getIdentifier()))
            {
                channel.transform(tr);
                changedChannels.put(entry.getKey(), channel);
            }
        }        

        return changedChannels;
    }

    @Override
    public Map<String, Channel1D> transform(String type, Set<String> identifiers, Channel1DDataInROITransformation tr, ROI roi, ROIRelativePosition position) 
    {
        Map<String, Channel1D> channelsForType = getChannels(type);
        Map<String, Channel1D> changedChannels = new LinkedHashMap<>();

        for(Entry<String, Channel1D> entry : channelsForType.entrySet())
        {
            Channel1D channel = entry.getValue();

            if(identifiers.contains(channel.getIdentifier()))
            {
                channel.transform(tr, roi, position);
                changedChannels.put(entry.getKey(), channel);
            }
        }

        return changedChannels;
    }

    //MEASUREMENTS

    @Override
    public int getMeasurementCount(String type)
    {
        int count = distanceMeasurements.size(type);
        return count;     
    }

    @Override
    public Map<Object, DistanceMeasurementDrawable> getDistanceMeasurements(String type)
    {
        Map<Object, DistanceMeasurementDrawable> measurtementsForType = distanceMeasurements.get(type);         
        return new LinkedHashMap<>(measurtementsForType);
    }

    @Override
    public void addOrReplaceDistanceMeasurement(String type, DistanceMeasurementDrawable measurement)
    {
        distanceMeasurements.put(type, measurement.getKey(), measurement);    
    }

    @Override
    public void removeDistanceMeasurement(String type, DistanceMeasurementDrawable measurement)
    {
        distanceMeasurements.remove(type, measurement.getKey());
    }
}
