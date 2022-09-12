
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
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingWorker;

public class CalibrationSwingWorker extends SwingWorker<Void,Void>
{
    private static final String CALIBRATION_PHASE = "calibrationPhase";
    private static final String CURRENT_PHASE_PROGRESS_IN_PERCENT = "currentPhaseProgressInPercents";

    private final RecordingStatus initialRecordingStatus;
    private CalibrationPhase calibrationPhase = CalibrationPhase.INITIALIZATION;

    private Date calibrationDate;
    private double calibrationOffsetInVolts;
    private double calibrationSlopeInPercentsPerVolt;

    private final long delayInMilisecondsAfterMeasuringBeamIsSwitchedOff;
    private final long delayInMilisecondsAfterMeasuringBeamIsSwitchedOn;
    private final SignalSourceModel model;
    private final SignalSource signalSource;

    public CalibrationSwingWorker(SignalSourceModel model, SignalSource signalSource, RecordingStatus initialRecordingStatus, long delayInMilisecondsAfterMeasuringBeamIsSwitchedOff, long delayInMilisecondsAfterMeasuringBeamIsSwitchedOn)
    {	
        this.model = model;
        this.initialRecordingStatus = initialRecordingStatus;
        this.delayInMilisecondsAfterMeasuringBeamIsSwitchedOff = delayInMilisecondsAfterMeasuringBeamIsSwitchedOff;
        this.delayInMilisecondsAfterMeasuringBeamIsSwitchedOn = delayInMilisecondsAfterMeasuringBeamIsSwitchedOn;
        this.signalSource = signalSource;

        addPropertyChangeListener(CALIBRATION_PHASE, new PropertyChangeListener() 
        {
            @Override
            public  void propertyChange(PropertyChangeEvent evt) 
            {                
                CalibrationPhase calibrationPhaseNew = (CalibrationPhase)evt.getNewValue();
                CalibrationPhase calibrationPhaseOld = (CalibrationPhase)evt.getOldValue();
                model.notifyAboutCalibrationPhaseChange(calibrationPhaseOld, calibrationPhaseNew);
            }
        });

        addPropertyChangeListener(CURRENT_PHASE_PROGRESS_IN_PERCENT, new PropertyChangeListener() 
        {          
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                int currentProgressInPercent = ((Number)evt.getNewValue()).intValue();
                model.notifAboutCurrentCalibrationProcessInPercent(currentProgressInPercent);
            }
        });
    }

    @Override
    public Void doInBackground() throws InterruptedException 
    {            
        setCalibrationPhase(CalibrationPhase.RECORDING_DARK_OFFSET);
        this.signalSource.initializeIfNecessary();


        ScheduledExecutorService executorDarkOffsetProgress = Executors.newSingleThreadScheduledExecutor();

        long periodOfDarkOffsetProgressUpdateInMicroseconds = (this.delayInMilisecondsAfterMeasuringBeamIsSwitchedOff*10);      
        AtomicInteger darkOffsetCalibrationProgressInPercents = new AtomicInteger();
        executorDarkOffsetProgress.scheduleAtFixedRate(new Runnable() 
        {           
            @Override
            public void run() 
            {
                if(!isCancelled())
                {
                    int currentProgressInPercents = darkOffsetCalibrationProgressInPercents.incrementAndGet();
                    firePropertyChange(CURRENT_PHASE_PROGRESS_IN_PERCENT, currentProgressInPercents-1, currentProgressInPercents);
                }
            }
        }, periodOfDarkOffsetProgressUpdateInMicroseconds, periodOfDarkOffsetProgressUpdateInMicroseconds, TimeUnit.MICROSECONDS);

        java.awt.Toolkit.getDefaultToolkit().beep();
        Thread.sleep(this.delayInMilisecondsAfterMeasuringBeamIsSwitchedOff);
        executorDarkOffsetProgress.shutdown();

        //  this.model.sendMeasuringLightIntensityThroughSerialPort(0);     
        // Thread.sleep(this.delayInMilisecondsAfterMeasuringBeamIsSwitchedOff);
        this.calibrationOffsetInVolts = this.signalSource.getSample().getValueInVolts();     

        java.awt.Toolkit.getDefaultToolkit().beep();

        setCalibrationPhase(CalibrationPhase.RECORDING_SLOPE);

        this.model.ensureThatMeasuringBeamIsPreparedForCalibration();

        ScheduledExecutorService executorSlopeProgress = Executors.newSingleThreadScheduledExecutor();
        long periodOfSlopeProgressUpdateInMicroseconds = (this.delayInMilisecondsAfterMeasuringBeamIsSwitchedOn*10);
        AtomicInteger slopeCalibrationProgressInPercents = new AtomicInteger();
        executorSlopeProgress.scheduleAtFixedRate(new Runnable() 
        {           
            @Override
            public void run() 
            {
                if(!isCancelled())
                {
                    int currentProgressInPercents = slopeCalibrationProgressInPercents.incrementAndGet();
                    firePropertyChange(CURRENT_PHASE_PROGRESS_IN_PERCENT, currentProgressInPercents-1, currentProgressInPercents);
                }
            }
        }, periodOfSlopeProgressUpdateInMicroseconds, periodOfSlopeProgressUpdateInMicroseconds, TimeUnit.MICROSECONDS);

        Thread.sleep(this.delayInMilisecondsAfterMeasuringBeamIsSwitchedOn);
        executorSlopeProgress.shutdown();

        this.calibrationDate = new Date();
        RawVoltageSample readingWithoutLeaf = this.signalSource.getSample();               
        this.calibrationSlopeInPercentsPerVolt = 100./(readingWithoutLeaf.getValueInVolts() - calibrationOffsetInVolts);

        this.signalSource.finishReading();

        setCalibrationPhase(CalibrationPhase.CALIBRATION_FINISHED);
        java.awt.Toolkit.getDefaultToolkit().beep();

        return null;        
    }   

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        PropertyChangeSupport support = getPropertyChangeSupport();
        support.addPropertyChangeListener(propertyName, listener);
    }

    public CalibrationPhase getCalibrationPhase()
    {
        return this.calibrationPhase;
    }

    private void setCalibrationPhase(CalibrationPhase currentPhase)
    {
        CalibrationPhase oldPhase = this.calibrationPhase;
        if(!Objects.equals(oldPhase, currentPhase))
        {
            this.calibrationPhase = currentPhase;
            firePropertyChange(CALIBRATION_PHASE, oldPhase, currentPhase);
        }
    }

    public void terminateAllTasks()
    {        
        cancel(true);
    }

    @Override
    protected void done()
    {
        if(isCancelled())
        {          
            model.calibrationDoneAfterCancellation(initialRecordingStatus);
            return;
        }

        //for the purpose of checking whether error was thrown
        //https://stackoverflow.com/questions/32445902/the-proper-way-to-handle-exceptions-thrown-by-the-swingworker-doinbackground
        try {
            get();
            model.calibrationFinishedSuccessfully(calibrationDate, calibrationSlopeInPercentsPerVolt, calibrationOffsetInVolts, initialRecordingStatus);
        } catch (ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
            model.calibrationDoneWithException(initialRecordingStatus);
        }      
    }

    public static enum CalibrationPhase
    {
        INITIALIZATION("Initialization","",false),RECORDING_DARK_OFFSET("Recording dark offset", true), 
        RECORDING_SLOPE("Recording slope", true), CALIBRATION_FINISHED("Calibration finished", "", false);

        private final String name;
        private final String guiAnnouncement;
        private final boolean progressBarRequired;

        private CalibrationPhase(String name, boolean progressBarRequired)
        {
            this(name, name, progressBarRequired);
        }

        private CalibrationPhase(String name,String interfaceAnnouncement, boolean progressBarRequired)
        {
            this.name = name;
            this.guiAnnouncement = interfaceAnnouncement;
            this.progressBarRequired = progressBarRequired;
        }

        public boolean isProgressBarRequired()
        {
            return progressBarRequired;
        }

        public String getGUIAnnouncement()
        {
            return guiAnnouncement;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
