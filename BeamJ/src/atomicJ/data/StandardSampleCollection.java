
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Pawe� Hermanowicz
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

package atomicJ.data;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class StandardSampleCollection implements SampleCollection
{
    private final String name;
    private final String shortName;
    private final Object key;

    private final File defaultOutputDir;

    private boolean collectionIncluded = true;

    private final Map<String, QuantitativeSample> samples;


    public StandardSampleCollection(Map<String, QuantitativeSample> samples, String name, String shortName, File defaultOutputDir)
    {
        this(samples, name, name, shortName, defaultOutputDir);
    }

    public StandardSampleCollection(Map<String, QuantitativeSample> samples, Object key, String name, String shortName, File defaultOutputDir)
    {
        this.defaultOutputDir = defaultOutputDir;
        this.samples = samples;
        this.name = name;
        this.shortName = shortName;
        this.key = key;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getShortName()
    {
        return shortName;
    }

    @Override
    public Object getKey()
    {
        return key;
    }

    @Override
    public List<String> getSampleTypes() 
    {
        Set<String> types = samples.keySet();
        return new ArrayList<>(types);
    }

    @Override
    public QuantitativeSample getSample(String type)
    {
        return samples.get(type);
    }

    @Override
    public Map<String, QuantitativeSample> getAllSamples()
    {
        return new LinkedHashMap<>(samples);
    }

    @Override
    public List<String> getIncludedSampleTypes()
    {
        List<String> includedSampleTypes = new ArrayList<>();
        Set<String> allTypes = samples.keySet();

        for(String type: allTypes)
        {
            if(isKeyIncluded(type))
            {
                includedSampleTypes.add(type);
            }
        }
        return includedSampleTypes;
    }

    @Override
    public Map<String, QuantitativeSample> getIncludedSamples() 
    {
        Map<String, QuantitativeSample> models = new LinkedHashMap<>();

        for(Entry<String, QuantitativeSample> entry: samples.entrySet())
        {
            String identifier = entry.getKey();
            QuantitativeSample sample = entry.getValue();
            boolean datasetIncluded = sample.isIncluded();

            if(datasetIncluded)
            {
                models.put(identifier, sample);				
            }
        }

        return models;	
    }

    @Override
    public Map<String, QuantitativeSample> getIncludedSamples(int sizeLimit) 
    {
        Map<String, QuantitativeSample> models = new LinkedHashMap<>();

        for(Entry<String, QuantitativeSample> entry: samples.entrySet())
        {
            String identifier = entry.getKey();

            QuantitativeSample sample = entry.getValue();
            boolean datasetIncluded = sample.isIncluded();

            if(datasetIncluded)
            {
                if(sample.size()>sizeLimit)
                {
                    models.put(identifier, sample);
                }
            }
        }

        return models;
    }

    @Override
    public boolean isKeyIncluded(String sampleType) 
    {
        boolean included = false;

        QuantitativeSample sample = samples.get(sampleType);
        if(sample != null)
        {
            included = sample.isIncluded();
        }

        return included;
    }

    @Override
    public void setKeysIncluded(boolean included) 
    {
        for(Entry<String, QuantitativeSample> entry: samples.entrySet())
        {
            QuantitativeSample sample = entry.getValue();
            if(sample != null)
            {			
                sample.setIncluded(included);
            }	
        }	
    }

    @Override
    public void setKeyIncluded(String sampleType, boolean included) 
    {
        QuantitativeSample sample = samples.get(sampleType);
        if(sample != null)
        {	
            sample.setIncluded(included);
        }	
    }

    @Override
    public boolean isCollectionIncluded()
    {
        return collectionIncluded;
    }

    @Override
    public void setCollectionIncluded(boolean collectionIncludedNew)
    {
        this.collectionIncluded = collectionIncludedNew;
    }

    @Override
    public File getDefaultOutputDirectory() 
    {
        return defaultOutputDir;
    }
}
