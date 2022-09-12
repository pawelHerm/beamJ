
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

package atomicJ.gui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import atomicJ.analysis.MonitoredSwingWorker;
import atomicJ.sources.ChannelSource;

public class ConcurrentPreviewTask <E extends ChannelSource> extends MonitoredSwingWorker<Void, Void> 
{
    private static final int maxTaskNumber = Runtime.getRuntime().availableProcessors();

    private final List<? extends E> sources;
    private final SourcePreviewerHandle<E> previewer;

    private final AtomicInteger failures = new AtomicInteger();
    private final int problemSize;
    private ExecutorService executor;

    private final AtomicInteger previewedCount = new AtomicInteger();

    public ConcurrentPreviewTask(List<? extends E> sources, SourcePreviewerHandle<E> previewer)
    {
        super(previewer.getAssociatedComponent(), "Rendering charts in progress", "Rendered", sources.size());
        this.previewer = previewer;
        this.sources = sources;		
        this.problemSize = sources.size();
    }

    @Override
    public Void doInBackground() throws UserCommunicableException
    {	
        int taskNumber = Math.min(Math.max(problemSize/20, 1), maxTaskNumber);
        int basicTaskSize = problemSize/taskNumber;
        int remainingFiles = problemSize%taskNumber;

        executor = Executors.newFixedThreadPool(taskNumber); 

        int currentIndex = 0;

        List<Subtask<E>> tasks = new ArrayList<>();

        for( int i = 0; i <taskNumber; i++ ) 
        {
            int currentTaskSize = basicTaskSize;
            if(i<remainingFiles)
            {
                currentTaskSize++;
            }
            List<E> fileSublist = new ArrayList<>(sources.subList(currentIndex, currentIndex + currentTaskSize));

            Subtask<E> task = new Subtask<>(fileSublist, previewer.createAndRegisterANewTask());
            tasks.add(task);
            currentIndex = currentIndex + currentTaskSize;
        }
        try 
        {
            CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);

            for(Subtask<E> subtask: tasks)
            {
                completionService.submit(subtask);
            }
            for(int i = 0; i<tasks.size(); i++)
            {
                completionService.take().get();
            }
        } 
        catch (InterruptedException | ExecutionException e) 
        {
            e.printStackTrace();
        }	
        finally
        {
            executor.shutdown();
            setStep(previewedCount.intValue());
        }        
        return null;
    }	

    private void incrementProgress()
    {
        setStep(previewedCount.incrementAndGet());	
    }

    public int getFailuresCount()
    {
        return failures.intValue();
    }

    private class Subtask <E extends ChannelSource> implements Callable<Void>
    {
        private final List<? extends E> sources;
        private final SourcePreviewerTask<E> previewerTask;

        public Subtask(List<? extends E> sources, SourcePreviewerTask<E> previewerTask)
        {
            this.sources = sources;
            this.previewerTask = previewerTask;
        }

        @Override
        public Void call() throws InterruptedException
        {             
            Thread currentThread = Thread.currentThread();

            int n = sources.size();

            for(int i = 0; i < n;i++)
            {
                if(currentThread.isInterrupted())
                {
                    throw new InterruptedException();
                }

                E source = sources.get(i);				
                try
                {
                    previewerTask.preview(source);
                }
                catch(OutOfMemoryError e)
                {
                    setRunOutOfMemory();
                    e.printStackTrace();
                    throw e;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    failures.incrementAndGet();
                }

                incrementProgress();
            }	
            return null;
        }
    }

    @Override
    public void done()
    {
        super.done();
        try 
        {
            if(isCancelled())
            {
                previewer.reactToCancellation();
            }
            else
            {
                previewer.reactToFailures(getFailuresCount());
                previewer.sendPreviewedDataToDestination();
            }
        }

        finally
        {
            setProgress(100);
        }
    }

    @Override
    public void cancelAllTasks() 
    {
        if(executor != null)
        {
            executor.shutdownNow();
        }

        cancel(true);
    }

    public static interface SourcePreviewerHandle <E extends ChannelSource>
    {
        public SourcePreviewerTask<E> createAndRegisterANewTask();
        public void sendPreviewedDataToDestination();
        public void reactToCancellation();
        public void reactToFailures(int failureCount);
        public void showMessage(String message);
        public Component getAssociatedComponent();
        public void requestPreviewEndAndPrepareForNewExecution();
    }

    public static interface SourcePreviewerTask <E extends ChannelSource>
    {
        public void preview(E source);
    }


}
