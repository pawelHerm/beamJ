
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

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

public class SignalSamplingSwingWorker extends SwingWorker<Void, RawVoltageSample>
{
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final long samplingPeriodInMiliseconds;
    private final SignalSource signalSource;
    private final SignalSourceModel model;

    public SignalSamplingSwingWorker(SignalSourceModel model, SignalSource signalSource, long samplingPeriodInMiliseconds)
    {	
        this.model = model;
        this.signalSource = signalSource;
        this.samplingPeriodInMiliseconds = samplingPeriodInMiliseconds;
    }

    public SignalSource getSignalSourceForSharing()
    {
        return new SignalSourceSharingWrapper(signalSource);
    }

    public double getSamplingPeriodInMiliseconds()
    {
        return samplingPeriodInMiliseconds;
    }

    @Override
    public Void doInBackground() throws InterruptedException 
    {                
        long delay = 0;
        SignalSamplingTask task = new SignalSamplingTask(this.signalSource);
        scheduler.scheduleAtFixedRate(task, delay, samplingPeriodInMiliseconds, TimeUnit.MILLISECONDS);

        return null;        
    }   

    @Override
    public void process(List<RawVoltageSample> receivedSignalSamples)
    {
        if(!isCancelled())
        {
            this.model.signalSamplesReceived(receivedSignalSamples);
        }
    }

    public void terminateAllTasks()
    {
        cancel(false);
        scheduler.shutdownNow();              
        signalSource.finishReading();
    }

    @Override
    protected void done()
    {
    }

    private class SignalSamplingTask implements Runnable
    { 
        private final SignalSource signalSource;

        public SignalSamplingTask(SignalSource signalSource)
        {
            this.signalSource = signalSource;
            this.signalSource.initializeIfNecessary();
        }

        @Override
        public void run() 
        {             
            Thread currentThread = Thread.currentThread();

            if(currentThread.isInterrupted())
            {
                return;
            }

            RawVoltageSample transmittance = this.signalSource.getSample();     

            publish(transmittance);                 
        }
    }
} 
