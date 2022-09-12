
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

package chloroplastInterface;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.filechooser.FileFilter;

import atomicJ.data.ChannelFilter;
import atomicJ.data.PermissiveChannelFilter;
import atomicJ.readers.SourceReader;
import atomicJ.readers.SourceReaderFactory;
import atomicJ.readers.SourceReadingModel;

public class PhotometricCurveReadingModel implements SourceReadingModel<SimplePhotometricSource>
{
    private ChannelFilter dataFilter = PermissiveChannelFilter.getInstance();

    private final Map<FileFilter, SourceReaderFactory<? extends SourceReader<SimplePhotometricSource>>> readerFilterMap = new LinkedHashMap<>();
    private final Map<String, FileFilter> filterNameMap = new LinkedHashMap<>();

    private final FileFilter defaultFilter;

    private PhotometricCurveReadingModel()
    {		
        CLMFileReaderFactory general = CLMFileReaderFactory.getInstance();

        defaultFilter = general.getFileFilter();
        readerFilterMap.put(defaultFilter, general);  

        for(FileFilter filter: readerFilterMap.keySet())
        {
            filterNameMap.put(filter.getDescription(), filter);
        }
    }

    public static PhotometricCurveReadingModel getInstance()
    {
        return new PhotometricCurveReadingModel();
    }

    @Override
    public List<FileFilter> getExtensionFilters() 
    {
        return new ArrayList<>(filterNameMap.values());
    }

    @Override
    public void setDataFilter(ChannelFilter filter)
    {
        this.dataFilter = filter;
    }

    @Override
    public ChannelFilter getDataFilter()
    {
        return this.dataFilter;
    }

    @Override
    public SourceReader<SimplePhotometricSource> getSourceReader(FileFilter filter) 
    {
        SourceReaderFactory<? extends SourceReader<SimplePhotometricSource>> factory = readerFilterMap.get(filter);

        SourceReader<SimplePhotometricSource> reader = (factory != null) ? factory.getReader(): null;
        return reader;
    }

    @Override
    public FileFilter getExtensionFilter(String filterName) 
    {
        FileFilter filter = filterNameMap.get(filterName);
        if(filter == null)
        {
            filter = defaultFilter;
        }
        return filter;
    }
}
