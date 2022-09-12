
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

public class ActinicPhaseScheduleSwingWorker extends CustomSwingWorker<Void,Void>
{
    public static final String PHASE = "phase";

    private PhaseStamp phase;
    private final List<ActinicPhase> recordingPhasesToExecute;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final List<ActinicPhaseRealTiming> phaseRealTimings = new ArrayList<>();
    private final int initialPhaseIndex;
    private final RecordingModel model;

    public ActinicPhaseScheduleSwingWorker(RecordingModel model, int initalPhaseIndex, boolean isScheduleContinuedAfterStop, List<? extends ActinicPhase> recordingPhasesToExecute, int repeatCount)
    {	
        this.model = model;
        this.phase = model.getActinicBeamPhase();
        this.initialPhaseIndex = initalPhaseIndex;
        this.recordingPhasesToExecute = Collections.unmodifiableList(recordingPhasesToExecute);
        addPropertyChangeListener(PHASE, new PropertyChangeListener() 
        {
            @Override
            public  void propertyChange(PropertyChangeEvent evt) 
            {                
                PhaseStamp phase = (PhaseStamp)evt.getNewValue();
                model.setActinicBeamPhase(phase, isScheduleContinuedAfterStop);
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
            ActinicPhase currentPhase = recordingPhasesToExecute.get(i);
            long durationInMiliseconds = Math.round(currentPhase.getDurationInMiliseconds());
            double intensity = currentPhase.getBeamIntensityInPercent();

            PhaseTask task = new PhaseTask(intensity, durationInMiliseconds, initialPhaseIndex + i, initialPhaseIndex + i, latch);
            scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);

            delay += durationInMiliseconds;
        }

        PhaseTask finalLightSwitchOffTask = new PhaseTask(0, 0, recordingPhasesToExecute.size(), -1, latch);
        scheduler.schedule(finalLightSwitchOffTask, delay, TimeUnit.MILLISECONDS);

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

        setActinicLightIntensity(0);

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

        if(isCancelled())
        {          
            model.runFinishedAfterCancelling(Collections.unmodifiableList(phaseRealTimings));
        }
        else
        {
            //for the purpose of checking whether error was thrown
            //https://stackoverflow.com/questions/32445902/the-proper-way-to-handle-exceptions-thrown-by-the-swingworker-doinbackground
            try {
                get();
                model.runFinishedSuccessfully();
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
                model.runFinishedWithException();
            }
        }        
    }

    private void setActinicLightIntensity(double lightIntensityInPercent)
    {
        model.sendActinicLightIntensityToController(lightIntensityInPercent);
    }

    private class PhaseTask implements Callable<Void>
    { 
        private final double intensityInPercent;
        private final long intendedDurationInMiliseconds;
        private final int phaseIndex;
        private final int currentPhaseAfterExecution;

        private final CountDownLatch latch;

        public PhaseTask(double intensityInPercent, long durationInMiliseconds, int phaseIndex, int currentPhaseAfterExecution,CountDownLatch latch)
        {
            this.intensityInPercent = intensityInPercent;
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

            setActinicLightIntensity(intensityInPercent);

            long phaseBeginingTime = System.currentTimeMillis();
            ActinicPhaseRealTiming phaseTiming = new ActinicPhaseRealTiming(phaseIndex, intensityInPercent, intendedDurationInMiliseconds, phaseBeginingTime);
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

    public static class ActinicPhaseRealTiming
    {
        private final int phaseIndex;
        private final double lightIntensityInPercent;
        private final long intendedDurationInMiliseconds;
        private final long beginningTime;
        private long endTime = -1;

        public ActinicPhaseRealTiming(int phaseIndex, double lightIntensityInPercent, long intendedDurationInMiliseconds, long timeOfBeginning)
        {
            this.phaseIndex = phaseIndex;
            this.lightIntensityInPercent = lightIntensityInPercent;
            this.intendedDurationInMiliseconds = intendedDurationInMiliseconds;
            this.beginningTime = timeOfBeginning;
        }

        public int getPhaseIndex()
        {
            return phaseIndex;
        }

        public double getLightIntensityInPercent()
        {
            return lightIntensityInPercent;
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

        public ActinicPhaseRemainder getMismatchPhase()
        {
            if(endTime == -1)
            {
                return null;
            }

            long mismatchNonNegative = Math.max(intendedDurationInMiliseconds - (endTime - beginningTime), 0);
            ActinicPhaseRemainder mismatchPhase = new ActinicPhaseRemainder(mismatchNonNegative, StandardTimeUnit.MILISECOND,lightIntensityInPercent,intendedDurationInMiliseconds);

            return mismatchPhase;
        }

        //this method should be used if the ActinicBeam
        public ActinicPhaseRemainder getMismatchPhase(double newPhaseDurationInMiliseconds, long furtherIncreaseInPhaseLength)
        {
            if(endTime == -1)
            {
                return null;
            }

            long mismatchNonNegative = Math.max(intendedDurationInMiliseconds - (endTime - beginningTime) + furtherIncreaseInPhaseLength, 0);
            ActinicPhaseRemainder mismatchPhase = new ActinicPhaseRemainder(mismatchNonNegative, StandardTimeUnit.MILISECOND, lightIntensityInPercent, newPhaseDurationInMiliseconds);

            return mismatchPhase;
        }
    }
}
