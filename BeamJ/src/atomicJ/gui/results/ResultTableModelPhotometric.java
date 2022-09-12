
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Paweł Hermanowicz
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

package atomicJ.gui.results;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.units.StandardUnitSource;
import atomicJ.sources.IdentityTag;
import atomicJ.statistics.DescriptiveStatistics;
import chloroplastInterface.LightSignalType;
import chloroplastInterface.ProcessedPackPhotometric;
import chloroplastInterface.ProcessingSettings;
import chloroplastInterface.SimplePhotometricSource;


public class ResultTableModelPhotometric extends ResultTableModel<SimplePhotometricSource, ProcessedPackPhotometric>
{
    private static final long serialVersionUID = 1L;

    private static final int SPECIAL_COLUMN_INSERTION_INDEX = 1;
    private static final int STANDARD_COLUMN_COUNT = 4;

    private final List<IdentityTag> columnIds = new ArrayList<>(Arrays.asList(
            new IdentityTag("Source name"),   
            new IdentityTag("Signal index"),  
            new IdentityTag("Signal type"),          
            new IdentityTag("Batch")));

    private final List<PrefixedUnit> dataUnits = new ArrayList<>(Arrays.<PrefixedUnit>asList(
            null, null,null,
            null));

    private final StandardUnitSource unitSource;


    public ResultTableModelPhotometric()
    {
        super(STANDARD_COLUMN_COUNT, SPECIAL_COLUMN_INSERTION_INDEX);
        this.unitSource = buildUnitSource(columnIds, dataUnits);

    }

    public ResultTableModelPhotometric(ResultDataModel<SimplePhotometricSource,ProcessedPackPhotometric> dataModel)
    {	
        super(dataModel, STANDARD_COLUMN_COUNT, SPECIAL_COLUMN_INSERTION_INDEX);
        this.unitSource = buildUnitSource(columnIds, dataUnits);
    }

    @Override
    public StandardUnitSource getUnitSource()
    {
        return unitSource;
    }

    @Override
    protected void addUnitSourceGroup(IdentityTag group, PrefixedUnit selectedUnit, List<PrefixedUnit> units)
    {
        unitSource.addUnitGroup(group, selectedUnit, units);
    }

    @Override
    public List<IdentityTag> getColumnShortNames()
    {
        return new ArrayList<>(columnIds);
    }

    @Override
    public int getModelIndex(IdentityTag tag)
    {
        return columnIds.indexOf(tag);
    }

    @Override
    public int getColumnIndex(IdentityTag columnId)
    {      
        int index = columnIds.indexOf(columnId);
        return index;
    }

    @Override
    public PrefixedUnit getDataUnit(IdentityTag columnId)
    {
        int index = columnIds.indexOf(columnId);
        return dataUnits.get(index);
    }

    @Override
    public List<PrefixedUnit> getDataUnits()
    {
        return new ArrayList<>(dataUnits);
    }

    @Override
    public List<PrefixedUnit> getDisplayedUnits()
    {
        List<PrefixedUnit> units = new ArrayList<>(Collections.<PrefixedUnit>nCopies(columnIds.size(), null));

        for(Entry<IdentityTag, PrefixedUnit> entry : unitSource.getSelectedUnits().entrySet())
        {
            int index = columnIds.indexOf(entry.getKey());
            units.set(index, entry.getValue());
        }

        return units;
    }

    @Override
    public String getColumnName(int index)
    {
        IdentityTag id = columnIds.get(index);
        PrefixedUnit unit = unitSource.getSelectedUnit(id);
        String name = (unit != null) ? id.getLabel() + " (" + unit.getFullName() + ")" : id.getLabel();
        return name;
    }

    @Override
    protected void updateDefaultPrefices()
    {  
        for(int i = 0; i<columnIds.size(); i++)
        {
            PrefixedUnit dataUnit = dataUnits.get(i);
            if(dataUnit != null)
            {
                unitSource.setDefaultUnit(columnIds.get(i), getDefultUnit(i));
            }
        }
    }

    @Override
    protected PrefixedUnit getDefultUnit(int columnIndex)
    {
        int rowCount = getRowCount();
        double[] values = new double[rowCount];

        for(int i = 0; i<rowCount; i++)
        {
            Object value = getValueAt(i, columnIndex);
            if(value instanceof Number)
            {
                values[i] = Math.abs(((Number)value).doubleValue());   
            }
        }

        double median = DescriptiveStatistics.median(values);

        return getDataUnit(columnIndex).getPreferredCompatibleUnit(median);
    }

    @Override
    protected PrefixedUnit getDataUnit(int columnIndex)
    {
        return dataUnits.get(columnIndex);
    }

    @Override
    protected void registerAddedColumnIdAndDataUnit(IdentityTag columnId,PrefixedUnit dataUnit)
    {
        columnIds.add(columnId);
        dataUnits.add(dataUnit);
    }

    @Override
    protected void readInBasicColumns(Object[] row, ProcessedPackPhotometric pack, String batchName) 
    {
        ProcessingSettings settings = pack.getProcessingSettings();
        SimplePhotometricSource source = pack.getSource();

        int signalIndex = settings.getSignalIndex();

        LightSignalType signalType = source.getSignalType(signalIndex);

        row[0] = pack;       
        //we add 1 to the signal index, because we want to show the index in the one-based system, not the zer-based system
        row[1] = signalIndex + 1;
        row[2] = signalType.getPhysicalPropertyName();
        row[3] = batchName;
    }
}