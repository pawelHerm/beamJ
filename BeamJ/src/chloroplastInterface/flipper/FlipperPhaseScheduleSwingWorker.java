
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import chloroplastInterface.CustomSwingWorker;
import chloroplastInterface.PhaseStamp;
import chloroplastInterface.StandardTimeUnit;

public class FlipperPhaseScheduleSwingWorker extends CustomSwingWorker<Void,Void>
{
    public static final String PHASE = "phase";

    private PhaseStamp phase;
    private final List<FlipperPhase> recordingPhasesToExecute;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final List<FlipperPhaseRealTiming> phaseRealTimings = new ArrayList<>();
    private final int initialPhaseIndex;
    private final FlipperModel model;

    public FlipperPhaseScheduleSwingWorker(FlipperModel model, int initalPhaseIndex, boolean isScheduleContinuedAfterStop, List<? extends FlipperPhase> recordingPhasesToExecute, int repeatCount)
    {	
        this.model = model;
        this.phase = model.getFlipperPhase();
        this.initialPhaseIndex = initalPhaseIndex;
        this.recordingPhasesToExecute = Collections.unmodifiableList(recordingPhasesToExecute);
        addPropertyChangeListener(PHASE, new PropertyChangeListener() 
        {
            @Override
            public  void propertyChange(PropertyChangeEvent evt) 
            {                
                PhaseStamp phase = (PhaseStamp)evt.getNewValue();
                model.setFlipperPhase(phase, isScheduleContinuedAfterStop);
            }
        });
    }

    @Override
    public Void doInBackground() throws InterruptedException 
    {            
        CountDownLatch latch = new CountDownLatch(recordingPhasesToExecute.size() + 1);

        long delay = 0;

        for(int i = 0; i < recordingPhasesToExecute.size(); i++ ) 
        {
            FlipperPhase currentPhase = recordingPhasesToExecute.get(i);
            long durationInMiliseconds = Math.round(currentPhase.getDurationInMiliseconds());
            FlipperPosition intensity = currentPhase.getFlipperPosition();

            PhaseTask task = new PhaseTask(intensity, durationInMiliseconds, initialPhaseIndex + i, initialPhaseIndex + i, latch);
            scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);

            delay += durationInMiliseconds;
        }

        latch.await();

        return null;        
    }   

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        PropertyChangeSupport support = getPropertyChangeSupport();
        support.addPropertyChangeListener(propertyName, listener);
    }

    protected void setPhase(PhaseStamp finishedPhase)
    {
        PhaseStamp phaseOld = this.phase;
        this.phase = finishedPhase;

        firePropertyChange(PHASE, phaseOld, finishedPhase);
    }

    public void terminateAllTasks()
    {
        scheduler.shutdownNow();

        long terminationTime = System.currentTimeMillis();

        if(!phaseRealTimings.isEmpty())
        {
            phaseRealTimings.get(phaseRealTimings.size() - 1).setEndTime(terminationTime);
        }

        cancel(false);
    }

    public boolean isActive()
    {
        boolean active = !isDone() && !isCancelled();
        return active;
    }

    @Override
    protected void done()
    {
        scheduler.shutdown();

        if(!isCancelled())
        {
            //for the purpose of checking whether error was thrown
            //https://stackoverflow.com/questions/32445902/the-proper-way-to-handle-exceptions-thrown-by-the-swingworker-doinbackground
            try {
                get();
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }        
    }

    private void setFlipperPosition(FlipperPosition flipperPosition)
    {
        model.selectFlipperPosition(flipperPosition);;
    }

    private class PhaseTask implements Callable<Void>
    { 
        private final FlipperPosition flipperPosition;
        private final long intendedDurationInMiliseconds;
        private final int phaseIndex;
        private final int currentPhaseAfterExecution;

        private final CountDownLatch latch;

        public PhaseTask(FlipperPosition flipperPosition, long durationInMiliseconds, int phaseIndex, int currentPhaseAfterExecution, CountDownLatch latch)
        {
            this.flipperPosition = flipperPosition;
            this.intendedDurationInMiliseconds = durationInMiliseconds;
            this.phaseIndex = phaseIndex;
            this.currentPhaseAfterExecution = currentPhaseAfterExecution;
            this.latch = latch;
        }

        @Override
        public Void call() throws InterruptedException
        {           
            Thread currentThread = Thread.currentThread();

            if(currentThread.isInterrupted())
            {
                throw new InterruptedException();
            }

            setFlipperPosition(flipperPosition);

            long phaseBeginingTime = System.currentTimeMillis();
            FlipperPhaseRealTiming phaseTiming = new FlipperPhaseRealTiming(phaseIndex, flipperPosition, intendedDurationInMiliseconds, phaseBeginingTime);
            phaseRealTimings.add(phaseTiming);

            if(phaseIndex > 0)
            {
                phaseRealTimings.get(phaseIndex - 1).setEndTime(phaseBeginingTime);
            }

            setPhase(new PhaseStamp(phaseIndex, currentPhaseAfterExecution, phaseBeginingTime));

            latch.countDown();

            return null;
        }
    }

    public static class FlipperPhaseRealTiming
    {
        private final int phaseIndex;
        private final FlipperPosition flipperPosition;
        private final long intendedDurationInMiliseconds;
        private final long beginningTime;
        private long endTime = -1;

        public FlipperPhaseRealTiming(int phaseIndex, FlipperPosition flipperPosition, long intendedDurationInMiliseconds, long timeOfBeginning)
        {
            this.phaseIndex = phaseIndex;
            this.flipperPosition = flipperPosition;
            this.intendedDurationInMiliseconds = intendedDurationInMiliseconds;
            this.beginningTime = timeOfBeginning;
        }

        public int getPhaseIndex()
        {
            return phaseIndex;
        }

        public FlipperPosition getFlipperPosition()
        {
            return flipperPosition;
        }

        public void setEndTime(long endTime)
        {
            this.endTime = endTime;
        }

        public long getIntendedDurationInMiliseconds()
        {
            return intendedDurationInMiliseconds;
        }

        public long getRealDurationInMiliseconds()
        {
            if(endTime == -1)
            {
                return -1;
            }

            long realDuration = endTime - beginningTime;

            return realDuration;
        }

        public long getDurationMismatchInMiliseconds()
        {
            if(endTime == -1)
            {
                return -1;
            }

            long mismatch = (endTime - beginningTime) - intendedDurationInMiliseconds;

            return mismatch;
        }

        public FlipperPhaseRemainder getMismatchPhase()
        {
            if(endTime == -1)
            {
                return null;
            }

            long mismatchNonNegative = Math.max(intendedDurationInMiliseconds - (endTime - beginningTime), 0);
            FlipperPhaseRemainder mismatchPhase = new FlipperPhaseRemainder(flipperPosition, mismatchNonNegative, StandardTimeUnit.MILISECOND,intendedDurationInMiliseconds);

            return mismatchPhase;
        }

        //this method should be used if the ActinicBeam
        public FlipperPhaseRemainder getMismatchPhase(double newPhaseDurationInMiliseconds, long furtherIncreaseInPhaseLength)
        {
            if(endTime == -1)
            {
                return null;
            }

            long mismatchNonNegative = Math.max(intendedDurationInMiliseconds - (endTime - beginningTime) + furtherIncreaseInPhaseLength, 0);
            FlipperPhaseRemainder mismatchPhase = new FlipperPhaseRemainder(flipperPosition, mismatchNonNegative, StandardTimeUnit.MILISECOND, newPhaseDurationInMiliseconds);

            return mismatchPhase;
        }
    }
}
