
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

package chloroplastInterface.flipper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingWorker;

import chloroplastInterface.PhaseRealTiming;

public class FlipperFixedRateFlippingSwingWorker extends SwingWorker<Void,Void>
{
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final long flippingIntervalInMiliseconds;
    private final long timeBeforeFirstFlipInMiliseconds;
    private final FlipperModel flipperModel; 

    private final List<PhaseRealTiming> phaseRealTimings = new ArrayList<>();

    private final AtomicInteger phaseIndex = new AtomicInteger(0);


    public FlipperFixedRateFlippingSwingWorker(FlipperModel flipperModel, long timeBeforeFirstFlipInMiliseconds)
    {	
        this.flipperModel = flipperModel;
        this.timeBeforeFirstFlipInMiliseconds = timeBeforeFirstFlipInMiliseconds;
        this.flippingIntervalInMiliseconds = Math.round(flipperModel.getFlipperIntervalValueInMiliseconds());        
    }

    public double getFlippingIntervalInMiliseconds()
    {
        return flippingIntervalInMiliseconds;
    }

    public List<PhaseRealTiming> getPhaseRealTimings()
    {
        return Collections.unmodifiableList(phaseRealTimings);
    }

    @Override
    public Void doInBackground() throws InterruptedException 
    {                
        FlipperTask task = new FlipperTask(this.flipperModel);
        scheduler.scheduleAtFixedRate(task, timeBeforeFirstFlipInMiliseconds, flippingIntervalInMiliseconds, TimeUnit.MILLISECONDS);


        //first phase,before any flipping
        int newlyStartedPhaseIndex = phaseIndex.incrementAndGet();
        long phaseBeginingTime = System.currentTimeMillis();
        PhaseRealTiming phaseTiming = new PhaseRealTiming(newlyStartedPhaseIndex, timeBeforeFirstFlipInMiliseconds, phaseBeginingTime);
        phaseRealTimings.add(phaseTiming);

        return null;        
    }   

    @Override
    public void process(List<Void> receivedTransmittanceSamples)
    {
        if(!isCancelled())
        {
        }
    }

    public void terminateAllTasks()
    {
        cancel(false);
        scheduler.shutdownNow();   

        long terminationTime = System.currentTimeMillis();
        if(!phaseRealTimings.isEmpty())
        {
            phaseRealTimings.get(phaseRealTimings.size() - 1).setEndTime(terminationTime);
        }

        flipperModel.notifyAboutEndOfFlippingAtIntervals();
    }

    @Override
    protected void done()
    {
    }

    private class FlipperTask implements Runnable
    { 
        private final FlipperModel flipperModel;

        public FlipperTask(FlipperModel flipperModel)
        {
            this.flipperModel = flipperModel;
        }

        @Override
        public void run() 
        {             
            Thread currentThread = Thread.currentThread();

            if(currentThread.isInterrupted())
            {
                return;
            }

            //starting of a new phase
            int newlyStartedPhaseIndex = phaseIndex.incrementAndGet();
            long phaseBeginingTime = System.currentTimeMillis();
            PhaseRealTiming phaseTiming = new PhaseRealTiming(newlyStartedPhaseIndex, flippingIntervalInMiliseconds, phaseBeginingTime);
            phaseRealTimings.add(phaseTiming);

            if(newlyStartedPhaseIndex > 0)
            {
                phaseRealTimings.get(newlyStartedPhaseIndex - 1).setEndTime(phaseBeginingTime);
            }


            flipperModel.flipIfPossible();
        }
    }
} 
