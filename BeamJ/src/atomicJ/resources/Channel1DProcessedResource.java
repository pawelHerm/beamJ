
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.resources;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Objects;

import atomicJ.curveProcessing.Channel1DDataInROITransformation;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardSampleCollection;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIComposite;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.gui.undo.UndoManager;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.sources.Channel1DSource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.MetaMap;
import atomicJ.utilities.MultiMap;


public abstract class Channel1DProcessedResource <E extends Channel1DSource<?>> implements Channel1DResource<E>
{
    private final File file;
    private final String shortName;
    private final String longName;

    private final UndoManager undoManager = new UndoManager(2);

    private final MetaMap<String, Object, DistanceMeasurementDrawable> distanceMeasurements = new MetaMap<>();
    //meta map would be better here
    private final MultiMap<String, Channel1D> channelsTypeMap = new MultiMap<>();

    public Channel1DProcessedResource(E source, MultiMap<String, Channel1D> channelsTypeMap)
    {
        this.file = source.getCorrespondingFile();
        this.shortName = source.getShortName();
        this.longName = source.getLongName();
        this.channelsTypeMap.putAll(channelsTypeMap);
    }

    public Channel1DProcessedResource(E source, Map<String, Channel1D> channelsTypeMap)
    {
        this.file = source.getCorrespondingFile();
        this.shortName = source.getShortName();
        this.longName = source.getLongName();
        this.channelsTypeMap.put(channelsTypeMap);
    }

    public Channel1DProcessedResource(Channel1DProcessedResource<E> that)
    {
        this.file = that.file;
        this.longName = that.getLongName();
        this.shortName = that.getShortName();
    }

    @Override
    public PrefixedUnit getSingleDataUnit(String type)
    {
        Map<String, Channel1D> channels = getChannels(type);

        if(channels.isEmpty())
        {
            return null;
        }

        PrefixedUnit unit = channels.values().iterator().next().getYQuantity().getUnit();

        for(Channel1D channel : channels.values())
        {
            PrefixedUnit currentUnit = channel.getYQuantity().getUnit();
            if(!Objects.equals(unit, currentUnit))
            {
                return null;
            }
        }

        return unit;
    }

    @Override
    public String getLongName()
    {
        return longName;
    }

    @Override
    public String getShortName() 
    {
        return shortName;
    }

    @Override 
    public String toString()
    {
        return longName;
    }

    @Override
    public File getDefaultOutputLocation()
    {
        return file;
    }

    @Override
    public boolean containsChannelsFromSource(ChannelSource source)
    {
        boolean contains = (source != null) ? getSource().equals(source) : false;
        return contains;
    }

    public abstract Channel1DProcessedResource<E> copy();

    public List<SampleCollection> getSampleCollectionsRawData()
    {
        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

        for(Channel1D channel : channelsTypeMap.allValues())
        {
            samples.putAll(channel.getSamples());
        }

        SampleCollection collection = new StandardSampleCollection(samples, getShortName(), getShortName(), getDefaultOutputLocation());
        collection.setKeysIncluded(true);

        return Collections.singletonList(collection);
    }

    @Override
    public List<SampleCollection> getSampleCollections()
    {        
        return getSource().getSampleCollections();
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

    @Override
    public Map<String, String> getAutomaticChartTitles()
    {
        return getSource().getAutomaticChartTitles();
    }

    @Override
    public List<String> getAllTypes()
    {
        List<String> types = new ArrayList<>(channelsTypeMap.keySet());

        return types;
    }

    @Override
    public Set<String> getIdentifiers(String type)
    {
        Set<String> identifiers = new LinkedHashSet<>();
        List<Channel1D> channels = this.channelsTypeMap.get(type);

        for(Channel1D ch : channels)
        {
            identifiers.add(ch.getIdentifier());
        }

        return identifiers;
    }

    @Override
    public Set<String> getIdentifiers(String type, ChannelFilter2<Channel1D> filter)
    {
        Set<String> identifiers = new LinkedHashSet<>();
        List<Channel1D> channels = this.channelsTypeMap.get(type);

        for(Channel1D ch : channels)
        {
            if(filter.accepts(ch))
            {
                identifiers.add(ch.getIdentifier());
            }
        }

        return identifiers;
    }

    @Override
    public Set<String> getIdentifiersForAllTypes()
    {
        Set<String> identifiers = new LinkedHashSet<>();
        List<Channel1D> channels = this.channelsTypeMap.allValues();

        for(Channel1D ch : channels)
        {
            identifiers.add(ch.getIdentifier());
        }

        return identifiers;
    }

    //returns a map with values being those channels whose identifiers are in the set 'identifiers'. The keys are the corresponding identifiers
    //A multimap is returned, because this TypeModelManager may contain two different sources, with channels of the same identifier
    @Override
    public MultiMap<String, Channel1D> getChannelsForIdentifiers(Set<String> identifiers)
    {
        MultiMap<String, Channel1D> channelsForIdentifiers = new MultiMap<>();
        List<Channel1D> channels = this.channelsTypeMap.allValues();

        for(Channel1D channel : channels)
        {
            String identifier = channel.getIdentifier();
            if(identifiers.contains(identifier))
            {
                channelsForIdentifiers.put(identifier, channel);
            }
        }

        return channelsForIdentifiers;
    }


    @Override
    public Set<String> getIdentifiersForAllTypes(ChannelFilter2<Channel1D> filter)
    {
        List<Channel1D> channels = this.channelsTypeMap.allValues();
        Set<String> filteredIdentifiers = new LinkedHashSet<>();

        for(Channel1D channel : channels)
        {
            if(filter.accepts(channel))
            {
                filteredIdentifiers.add(channel.getIdentifier());
            }
        }

        return filteredIdentifiers;
    }

    @Override
    public Map<String, Channel1D> getChannels(String type)
    {
        Map<String, Channel1D> channelMap = new LinkedHashMap<>();
        List<Channel1D> channels = this.channelsTypeMap.get(type);

        for(Channel1D ch : channels)
        {            
            channelMap.put(ch.getIdentifier(), ch);
        }

        return channelMap;
    }

    public Channel1D getChannel(String type, Object identifier)
    {
        return getChannels(type).get(identifier);
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

    public Point2D correctItemPosition(String type, String channelIdentifier, int itemIndex, Point2D dataPoint)
    {
        Channel1D channel = getChannel(type, channelIdentifier);

        return (channel != null) ? channel.constrain(getChannels(type), itemIndex, dataPoint) : dataPoint;
    }

    public boolean isValidPosition(String type, String channelIdentifier, int itemIndex, Point2D dataPoint)
    {
        Channel1D channel = getChannel(type, channelIdentifier);

        return (channel != null) ? channel.isValidPosition(channel, getChannels(type), itemIndex, dataPoint) : true;
    }

    @Override
    public ROI getROIUnion()
    {
        return new ROIComposite("All");
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

    public void registerChannel(String type, E source, Channel1D channel) 
    {
        this.channelsTypeMap.put(type, channel);
    }

    public void removeChannel(String type, E source, Channel1D channel)
    {
        this.channelsTypeMap.remove(type, channel);
    }

    public void itemAdded(String selectedType, Object channelIdentifier, double[] item) 
    {
    }
}
