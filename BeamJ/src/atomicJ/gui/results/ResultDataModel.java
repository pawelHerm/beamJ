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

package atomicJ.gui.results;


import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import atomicJ.analysis.Batch;
import atomicJ.analysis.BatchUtilities;
import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.gui.AbstractModel;
import atomicJ.sources.Channel1DSource;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.IOUtilities;
import atomicJ.utilities.Validation;


public class ResultDataModel <S extends Channel1DSource<?>, E extends Processed1DPack<E,S>> extends AbstractModel
{
    public static final String BATCH_COUNT = "Batch count";	
    public static final String PACK_COUNT = "Pack count";	
    public static final String RESULTS_EMPTY = "Results empty";

    private int packCount = 0;
    private int batchCount = 0;

    private final Set<ProcessedPackFunction<? super E>> packFunctions;
    private final Map<IdentityTag, Batch<E>> processedBatches = new LinkedHashMap<>();

    private final List<ResultDataListener<S,E>> dataListeners = new CopyOnWriteArrayList<>();
    private final List<PackFunctionListener<E>> packFunctionListeners = new CopyOnWriteArrayList<>();

    //CAN BE MOVED TO ANOTHER CLASS THAT DECORATES BATCHMANAGER

    private final ResultSelectionModel<E> selectionModel = new ResultSelectionModel<>();


    public ResultDataModel(Collection<? extends ProcessedPackFunction<? super E>> packFunctions)
    {
        this.packFunctions = new LinkedHashSet<>(packFunctions);
    }

    public Set<ProcessedPackFunction<? super E>> getPackFunctions()
    {
        return new LinkedHashSet<>(packFunctions);
    }

    public void addPackFunction(ProcessedPackFunction<? super E> function)
    {
        boolean added = packFunctions.add(function);
        if(added)
        {
            firePackFunctionAdded(function);
        }
    }

    public List<E> getSelectedPacks()
    {
        return selectionModel.getSelectedProcessedPacks();
    }

    public void setSelectedPacks(List<E> packs)
    {
        selectionModel.setSelectedPacks(BatchUtilities.segregateIntoBatches(processedBatches, packs));
    }

    public ResultSelectionModel<E> getResultSelectionModel()
    {
        return selectionModel;
    }


    public List<E> getProcessedPacks()
    {
        List<E> packs = new ArrayList<>();

        for(Batch<E> batch: processedBatches.values())
        {
            packs.addAll(batch.getPacks());
        }

        return packs;
    }

    public List<E> getProcessedPacks(boolean selected)
    {
        if(selected)
        {
            return selectionModel.getSelectedProcessedPacks();
        }
        return getProcessedPacks();
    }

    public List<E> getProcessedPacks(String batchName, boolean selected)
    {
        Batch<E> batch = processedBatches.get(batchName);
        if(batch == null)
        {
            return Collections.emptyList();
        }
        if(selected)
        {
            return selectionModel.getSelectedProcessedPacks(batch);
        }
        return batch.getPacks();
    }

    public List<String> getAvailableBatchIds(boolean selected)
    {
        if(selected)
        {
            return BatchUtilities.getIds(selectionModel.getBatchesWithSelectedPacks());
        }

        return BatchUtilities.getIds(getBatches());
    }

    ///////////////////////////

    public List<Batch<E>> getBatches()
    {
        Collection<Batch<E>> batchCollection = processedBatches.values();
        List<Batch<E>> batches = new ArrayList<>(batchCollection);

        return batches;
    }

    public void addProcessedBatch(Batch<E> batch)
    {	
        addProcessedBatches(Collections.singletonList(batch));
    }

    public void addProcessedBatches(Collection<Batch<E>> results)
    {	
        Validation.requireNonNullParameterName(results, "results");
        
        List<Batch<E>> batchesNew = new ArrayList<>();

        for(Batch<E> batch: results)
        {
            IdentityTag key = batch.getIdentityTag();

            List<E> packs = batch.getPacks();

            if(processedBatches.containsKey(key))
            {                
                Batch<E> existingBatch = processedBatches.get(key);
                existingBatch.addProcessedPacks(packs);
                firePacksAdded(packs, existingBatch);
            }
            else
            {
                processedBatches.put(key, batch);
                batchesNew.add(batch);
            }	
        }

        fireBatchesAdded(batchesNew);

        updatePackCount();
        updateBatchCount();
    }

    public void removeProcessedBatch(Batch<E> batch)
    {	
        Validation.requireNonNullParameterName(batch, "batch");

        removeProcessedBatches(Collections.singletonList(batch));
    }

    public void removeProcessedBatches(List<Batch<E>> batches)
    {	
        Validation.requireNonNullParameterName(batches, "batches");

        List<Batch<E>> removedBatches = new ArrayList<>();

        for(Batch<E> batch: batches)
        {
            String name = batch.getName();

            if(processedBatches.containsKey(name))
            {
                processedBatches.remove(name);		
                removedBatches.add(batch);
            }
        }

        fireBatchesRemoved(removedBatches);

        updatePackCount();
        updateBatchCount();
    }

    public List<E> findProcessedPack(List<S> sources)
    {
        List<E> foundPacks = new ArrayList<>();
        List<E> allPacks = getProcessedPacks();

        for(S source : sources)
        {            
            for(E pack : allPacks)
            {
                if(pack.getSource().equals(source))
                {
                    foundPacks.add(pack);
                    break;
                }
            }
        }

        return foundPacks;
    }

    public void removeProcessedPack(E pack)
    {
        Validation.requireNonNullParameterName(pack, "pack");

        removeProcessedPacks(Collections.singletonList(pack));
    }

    public void removeProcessedPacks(List<E> packs)
    {
        Validation.requireNonNullParameterName(packs, "packs");

        List<Batch<E>> removedBatches = new ArrayList<>();

        for (Iterator<Batch<E>> it = processedBatches.values().iterator(); it.hasNext();)
        {
            Batch<E> batch = it.next();
            List<E> removedPacks = new ArrayList<>();

            for(E pack: packs)
            {                
                boolean isRemoved = batch.removeProcessedPack(pack);
                if(isRemoved)
                {
                    removedPacks.add(pack);
                }	
            }

            firePacksRemoved(removedPacks, batch);

            if(batch.isEmpty())
            {
                it.remove();
                removedBatches.add(batch);
            }          
        }

        fireBatchesRemoved(removedBatches);

        updateBatchCount();
        updatePackCount();

    }

    public void removeProcessedPack(E pack, String name)
    {
        Validation.requireNonNullParameterName(pack, "pack");
        Validation.requireNonNullParameterName(name, "name");
        
        removeProcessedPacks(Collections.singletonList(pack), name);
    }

    public void removeProcessedPacks(List<E> packs, String name)
    {
        Validation.requireNonNullParameterName(packs, "packs");
        Validation.requireNonNullParameterName(name, "name");

        boolean isBatchContained = processedBatches.containsKey(name);

        if(isBatchContained)
        {
            Batch<E> batch = processedBatches.get(name);
            List<E> removedPacks = new ArrayList<>();
            for(E pack: packs)
            {
                boolean isPackRemoved = batch.removeProcessedPack(pack);
                if(isPackRemoved)
                {
                    removedPacks.add(pack);
                }
            }

            firePacksAdded(removedPacks, batch);

            if(batch.isEmpty())
            {
                List<Batch<E>> removedBatches = new ArrayList<>();
                removedBatches.add(batch);
                fireBatchesRemoved(removedBatches);
            }
        }
        updatePackCount();
        updateBatchCount();
    }

    public void clear()
    {
        List<Batch<E>> batches = new ArrayList<>(processedBatches.values());		
        processedBatches.clear();

        fireBatchesCleared(batches);

        updatePackCount();
        updateBatchCount();
    }	


    public boolean isEmpty()
    {
        return processedBatches.isEmpty();
    }

    public int getBatchCount()
    {
        int count = processedBatches.size();
        return count;
    }

    public int getPackCount()
    {
        int count = 0;
        for(Batch<E> batch: processedBatches.values())
        {
            count = count + batch.getPackCount();
        }
        return count;
    }

    public File getDefaultOutputDirectory()
    {
        List<File> outputLocations = new ArrayList<>();
        List<E> packs = getProcessedPacks();

        for(E pack: packs)
        {
            File location = pack.getSource().getDefaultOutputLocation();
            outputLocations.add(location);
        }

        File commonDirectory = IOUtilities.findLastCommonDirectory(outputLocations);
        return commonDirectory;
    }

    private void updateBatchCount()
    {
        int newCount = getBatchCount();
        int oldCount = batchCount;
        this.batchCount = newCount;

        firePropertyChange(BATCH_COUNT, oldCount, newCount);
    }

    private void updatePackCount()
    {
        int newCount = getPackCount();
        int oldCount = packCount;
        this.packCount = newCount;

        firePropertyChange(PACK_COUNT, oldCount, newCount);
        firePropertyChange(RESULTS_EMPTY, oldCount == 0, newCount == 0);
    }

    public double[] getValues(String name, ProcessedPackFunction<E> function)
    {
        Validation.requireNonNullParameterName(name, "name");

        Batch<E> batch = processedBatches.get(name);
        return batch.getValues(function);
    }	

    public double[] getAllValues(ProcessedPackFunction<E> function)
    {
        return Batch.getValues(new ArrayList<>(processedBatches.values()), function);
    }

    public void addResultModelListener(ResultDataListener<S,E> listener) 
    {
        dataListeners.add(listener);
    }

    public void removeResultModelListener(ResultDataListener<S,E> listener) 
    {
        dataListeners.remove(listener);
    }

    protected void fireBatchesAdded(List<Batch<E>> batches) 
    {   
        if(!batches.isEmpty())
        {
            ResultDataEvent<S,E> evt = new ResultDataEvent<>(this, batches, ResultDataEvent.BATCHES_ADDED);

            for(ResultDataListener<S,E> listener: dataListeners)
            {
                listener.batchesAdded(evt);
            }
        }   	
    }

    protected void fireBatchesCleared(List<Batch<E>> batches) 
    {
        ResultDataEvent<S,E> evt = new ResultDataEvent<>(this, batches, ResultDataEvent.BATCHES_CLEARED);

        for(ResultDataListener<S,E> listener: dataListeners)
        {
            listener.batchesCleared(evt);
        }
    }

    protected void fireBatchesRemoved(List<Batch<E>> batches) 
    {
        if(!batches.isEmpty())
        {
            ResultDataEvent<S,E> evt = new ResultDataEvent<>(this, batches, ResultDataEvent.BATCHES_REMOVED);

            for(ResultDataListener<S,E> listener: dataListeners)
            {
                listener.batchesRemoved(evt);
            }
        }
    }

    protected void firePacksAdded(List<E> packs, Batch<E> batch) 
    {
        Validation.requireNonNullParameterName(batch, "batch");
        Validation.requireNonNullParameterName(packs, "packs");

        if(!packs.isEmpty())
        {
            ResultDataEvent<S,E> evt = new ResultDataEvent<>(this, packs, batch, ResultDataEvent.PACKS_ADDED);

            for(ResultDataListener<S,E> listener: dataListeners)
            {
                listener.packsAdded(evt);
            }
        }
    }

    protected void firePacksRemoved(List<E> packs, Batch<E> batch) 
    {
        Validation.requireNonNullParameterName(batch, "batch");
        Validation.requireNonNullParameterName(packs, "packs");
        
        if(!packs.isEmpty())
        {
            ResultDataEvent<S,E> evt = new ResultDataEvent<>(this, packs, batch, ResultDataEvent.PACKS_REMOVED);

            for(ResultDataListener<S,E> listener: dataListeners)
            {
                listener.packsRemoved(evt);
            }
        }
    }

    public void addPackFunctionListener(PackFunctionListener<E> listener)
    {
        packFunctionListeners.add(listener);
    }

    public void removePackFunctionListener(PackFunctionListener<E> listener)
    {
        packFunctionListeners.remove(listener);
    }

    protected void firePackFunctionAdded(ProcessedPackFunction<? super E> function)
    {
        for(PackFunctionListener<E> listener : packFunctionListeners)
        {
            listener.packFunctionAdded(function);
        }
    }
}




