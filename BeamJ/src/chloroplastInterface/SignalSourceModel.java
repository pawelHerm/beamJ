package chloroplastInterface;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import atomicJ.data.units.Quantity;
import atomicJ.geometricSets.RealSet;
import atomicJ.utilities.NumberUtilities;
import atomicJ.utilities.Validation;
import chloroplastInterface.CalibrationSwingWorker.CalibrationPhase;

public class SignalSourceModel
{
    private final static Preferences PREF_PARENT = Preferences.userNodeForPackage(SignalSourceModel.class).node(SignalSourceModel.class.getName());

    private final Preferences pref;

    private final static String SIGNAL_SAMPLES_PER_MINUTE = "SignalSamplesPerMinute";
    private final static String SIGNAL_SOURCE_CONTROLLER_ID = "SignalSourceFactoryID";
    private final static String LIGHT_SIGNAL_TYPE = "LightSignalType";

    private static final double MIN_SIGNAL_SAMPLES_PER_MINUTE = 0.01;
    private static final double MAX_SIGNAL_SAMPLES_PER_MINUTE_SUPPORTED_BY_SOFTWARE = 600;

    private static final int DEFAULT_SIGNAL_SAMPLES_PER_MINUTE = 60;

    private final static String LATEST_CALIBRATION_TIME = "LatestCalibrationTime";
    private final static String LATEST_CALIBRATION_SLOPE  = "LatestCalibrationSlope";
    private final static String LATEST_CALIBRATION_OFFSET = "LatestCalibrationOffset";

    private Date latestCalibrationDate;
    private double calibrationOffsetInVolts;
    private double calibrationSlopeInPercentsPerVolt;
    private boolean calibrationWellSpecified;

    private final long delayInMilisecondsAfterMeasuringBeamIsSwitchedOffDuringCalibration = 20000;
    private final long delayInMilisecondsAfterMeasuringBeamIsSwitchedOnDuringCalibration = 50000;

    private boolean calibrateEnabled;

    private SignalSourceController signalSourceController;

    private double signalSamplesPerMinute;
    private double maximalSignalSamplesPerMinuteForSelectedController;

    private final int numberOfSignalSamplesRecordedSimultanouslyToAverage = 10;
    private LightSignalType signalType;

    private SignalSamplingSwingWorker signalSamplingWorker;
    private CalibrationSwingWorker calibrationWorker;

    private long latestRecordingOnsetAbsoluteTime;

    private final List<SignalModelListener> listeners = new ArrayList<>();
    private final RecordingModel recordingModel;

    private final int signalIndex;

    private final boolean testMode;

    public SignalSourceModel(int signalIndex, RecordingModel recordingModel, Map<String, SignalSourceController> availableFunctionalSignalSourceControllers, boolean testMode)
    {
        this.testMode = testMode;
        this.signalIndex = Validation.requireNonNegativeParameterName(signalIndex, "signalIndex");
        this.recordingModel = Validation.requireNonNullParameterName(recordingModel, "recordingModel");
        this.latestRecordingOnsetAbsoluteTime = recordingModel.getLatestRecordingOnsetAbsoluteTime();

        this.pref = PREF_PARENT.node(Integer.toString(signalIndex));

        this.calibrationOffsetInVolts = pref.getDouble(LATEST_CALIBRATION_OFFSET, Double.NaN);
        this.calibrationSlopeInPercentsPerVolt = pref.getDouble(LATEST_CALIBRATION_SLOPE, Double.NaN);

        long latestCalibrationTime = pref.getLong(LATEST_CALIBRATION_TIME, -1);
        this.latestCalibrationDate = latestCalibrationTime >= 0 ? new Date(latestCalibrationTime) : null;
        this.calibrationWellSpecified = checkIfCalibrationWellSpecified();

        SignalSourceController fallBackSignalSourceController = testMode ? TestingSignalSourceController.getInstance() : DummySignalSourceController.getInstance();

        String selectedSignalSourceControllerKey = pref.get(SIGNAL_SOURCE_CONTROLLER_ID, "");
        this.signalSourceController = !availableFunctionalSignalSourceControllers.isEmpty() ? (availableFunctionalSignalSourceControllers.containsKey(selectedSignalSourceControllerKey) ? availableFunctionalSignalSourceControllers.get(selectedSignalSourceControllerKey) :  availableFunctionalSignalSourceControllers.values().iterator().next()): fallBackSignalSourceController;
        
        double signalSamplesPerMinuteStored = pref.getDouble(SIGNAL_SAMPLES_PER_MINUTE, DEFAULT_SIGNAL_SAMPLES_PER_MINUTE);
        this.maximalSignalSamplesPerMinuteForSelectedController = Math.min(60*this.signalSourceController.getMaximalSignalSamplingRateInHertz(), MAX_SIGNAL_SAMPLES_PER_MINUTE_SUPPORTED_BY_SOFTWARE);
        this.signalSamplesPerMinute = Math.min(this.maximalSignalSamplesPerMinuteForSelectedController, signalSamplesPerMinuteStored);

        String signalTypeName = pref.get(LIGHT_SIGNAL_TYPE, LightSignalType.TRANSMITTANCE.name());
        this.signalType = LightSignalType.isTypeKnown(signalTypeName) ? LightSignalType.valueOf(signalTypeName): LightSignalType.TRANSMITTANCE;
    }

    public String getRecordedChannelKey()
    {
        String key = this.signalType.getIdentifierForChannel(this.signalIndex);
        return key;
    }

    public Quantity getXQuantity()
    {
        return this.signalType.getXQuantity();
    }

    public Quantity getYQuantity()
    {
        return this.signalType.getYQuantity();
    }

    void terminate()
    {
        terminateSignalSamplingWorker();
        terminateCalibrationWorker();
        listeners.clear();
    }

    public void setlatestRecordingOnsetAbsoluteTime(long latestRecordingOnsetAbsoluteTimeNew)
    {
        this.latestRecordingOnsetAbsoluteTime = latestRecordingOnsetAbsoluteTimeNew;
    }

    public boolean isSetOfSupportedFrequenciesDiscrete()
    {
        return this.signalSourceController.isSetOfSupportedFrequenciesDiscrete();
    }

    public double getMaximalSupportedFrequencyInHertz()
    {
        return this.signalSourceController.getMaximalSupportedFrequencyInHertz();
    }

    public RealSet getSupportedFrequencies()
    {
        return this.signalSourceController.getSupportedFrequencies();
    }

    public void selectSignaleSourceController(SignalSourceController signalSourceControllerNew)
    {     
        Validation.requireNonNullParameterName(signalSourceControllerNew, "signalSourceControllerNew");

        setSignalSourceController(signalSourceControllerNew);
    }

    private void setSignalSourceController(SignalSourceController signalSourceControllerNew)
    {
        if(!Objects.equals(this.signalSourceController, signalSourceControllerNew))
        {    
            SignalSourceController signalSourceControllerOld = this.signalSourceController;
            this.signalSourceController = signalSourceControllerNew;

            pref.put(SIGNAL_SOURCE_CONTROLLER_ID, this.signalSourceController.getUniqueDescription());       
            flushPreferences();

            listeners.forEach(listener -> listener.signalSourceControllerChanged(signalSourceControllerOld, signalSourceControllerNew));

            double signalSamplingRateNew = findClosestSupportedSignalSamplingRate(signalSamplesPerMinute);
            setSignalSamplesPerMinute(signalSamplingRateNew);

            double maximalSignalSamplesPerMinuteForSourceNew = Math.min(this.signalSourceController.getMaximalSignalSamplingRateInHertz(), MAX_SIGNAL_SAMPLES_PER_MINUTE_SUPPORTED_BY_SOFTWARE);

            setMaximalSignalSamplesPerMinuteForController(maximalSignalSamplesPerMinuteForSourceNew);

            if(isConnectionWithSignalSourceEstablished())
            {
                buildAndExecuteSignalSamplingWorker();
            }
            else
            {
                terminateSignalSamplingWorker();
            }
        }
    }

    public boolean considerOfferOfSignalSourceController(SignalSourceController signalSourceController)
    {
        boolean acceptOffer = this.signalSourceController.shouldBeReplacedWhenOtherControllerFound();
        if(acceptOffer)
        {
            setSignalSourceController(signalSourceController);
        } 

        return acceptOffer;
    }

    public void replaceSignalControllerIfUsed(SignalSourceController controllerToRemove, Map<String, SignalSourceController> availableFunctionalSignalSourceControllers)
    {
        if(Objects.equals(this.signalSourceController, controllerToRemove))
        {
            SignalSourceController signalSourceControllerNew = availableFunctionalSignalSourceControllers.isEmpty() ? (testMode ? TestingSignalSourceController.getInstance() : DummySignalSourceController.getInstance()) : availableFunctionalSignalSourceControllers.values().iterator().next();
            setSignalSourceController(signalSourceControllerNew);
        }   
    }

    public boolean isConnectionWithSignalSourceEstablished()
    {
        boolean connectionEstablished = this.signalSourceController.isFunctional();
        return connectionEstablished;
    }

    private double findClosestSupportedSignalSamplingRate(double desiredTransmittanceSamplingRate)
    {
        double supported = Math.min(this.signalSourceController.getMaximalSignalSamplingRateInHertz(), desiredTransmittanceSamplingRate);
        return supported;
    }

    public double getMinimalSignalSamplesPerMinute()
    {
        return MIN_SIGNAL_SAMPLES_PER_MINUTE;
    }

    public double getMaximalSignalSamplesPerMinute()
    {
        return maximalSignalSamplesPerMinuteForSelectedController;
    }

    public double getSignalSamplesPerMinute()
    {
        return this.signalSamplesPerMinute;
    }

    public void selectSignalSamplesPerMinute(double signalSamplesPerMinuteNew)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalSamplesPerMinuteNew, MIN_SIGNAL_SAMPLES_PER_MINUTE, maximalSignalSamplesPerMinuteForSelectedController, "signalSamplesPerMinuteNew");
        setSignalSamplesPerMinute(signalSamplesPerMinuteNew);
    }

    private void setSignalSamplesPerMinute(double signalSamplesPerMinuteNew)
    {
        if(Double.compare(this.signalSamplesPerMinute, signalSamplesPerMinuteNew) != 0)
        {
            double signaleSamplesPerMinuteOld = this.signalSamplesPerMinute;
            this.signalSamplesPerMinute = signalSamplesPerMinuteNew;

            pref.putDouble(SIGNAL_SAMPLES_PER_MINUTE, this.signalSamplesPerMinute);       
            flushPreferences();

            listeners.forEach(listener -> listener.signalSamplesPerMinuteChanged(signaleSamplesPerMinuteOld, signalSamplesPerMinuteNew));


            buildAndExecuteSignalSamplingWorker();
        }
    }

    private void setMaximalSignalSamplesPerMinuteForController(double maximalSignalSamplesPerMinuteForSelectedControllerNew)
    {
        if(maximalSignalSamplesPerMinuteForSelectedControllerNew > MAX_SIGNAL_SAMPLES_PER_MINUTE_SUPPORTED_BY_SOFTWARE)
        {
            throw new IllegalArgumentException("maximalTransmittanceSamplesPerMinuteForSelectedControllerNew is larger than the maximal number of samples supported by the software ( " + Double.toString(MAX_SIGNAL_SAMPLES_PER_MINUTE_SUPPORTED_BY_SOFTWARE) + ")" );
        }

        if(Double.compare(this.maximalSignalSamplesPerMinuteForSelectedController, maximalSignalSamplesPerMinuteForSelectedControllerNew) != 0)
        {
            double maximalTransmittanceSamplesPerMinuteForSelectedSourceOld = this.maximalSignalSamplesPerMinuteForSelectedController;
            this.maximalSignalSamplesPerMinuteForSelectedController = maximalSignalSamplesPerMinuteForSelectedControllerNew;

            listeners.forEach(listener -> listener.maximalSignalSamplesPerMinuteChanged(maximalTransmittanceSamplesPerMinuteForSelectedSourceOld, maximalSignalSamplesPerMinuteForSelectedControllerNew));
        }
    }

    public LightSignalType getLightSignalType()
    {
        return this.signalType;
    }

    public void setLightSignalType(LightSignalType signalTypeNew)
    {
        Validation.requireNonNullParameterName(signalTypeNew, "signalTypeNew");
        if(!Objects.equals(this.signalType, signalTypeNew))
        {
            LightSignalType signalTypeOld = this.signalType;
            this.signalType = signalTypeNew;

            pref.put(LIGHT_SIGNAL_TYPE, this.signalType.name());
            listeners.forEach(listener -> listener.signalTypeChanged(signalTypeOld, signalTypeNew));
        }
    }

    public SignalSourceController getSelectedSignalSourceController()
    {
        return signalSourceController;
    }

    public void signalSamplesReceived(List<RawVoltageSample> signalSamples)
    {
        if(signalSamples.isEmpty() || !calibrationWellSpecified)
        {
            return;
        }

        List<CalibratedSignalSample> calibratedSamples = new ArrayList<>();
        for(RawVoltageSample rawSample :signalSamples)
        {
            CalibratedSignalSample calibratedSample = CalibratedSignalSample.getCalibrated(rawSample, calibrationSlopeInPercentsPerVolt, calibrationOffsetInVolts, rawSample.getAbsoluteTimeInMilis(), latestRecordingOnsetAbsoluteTime);
            calibratedSamples.add(calibratedSample);
        }

        for(SignalModelListener listener: listeners)
        {
            listener.signalSamplesReceived(calibratedSamples);
        }
    }

    public void terminateSignalSamplingWorker()
    {
        if(this.signalSamplingWorker != null)
        {
            this.signalSamplingWorker.terminateAllTasks();
        }
        this.signalSamplingWorker = null;
    }

    public void tryToBuildAndExecuteSignalSamplingWorker()
    {
        if(isConnectionWithSignalSourceEstablished())
        {
            buildAndExecuteSignalSamplingWorker();
        }
    }

    public void tryToBuildAndExecuteSignalSamplingWorkerIfNotPossibleThenTerminate()
    {
        if(isConnectionWithSignalSourceEstablished())
        {
            buildAndExecuteSignalSamplingWorker();
        }
        else
        {
            terminateSignalSamplingWorker();
        }
    }


    private void buildAndExecuteSignalSamplingWorker()
    {
        if(this.signalSamplingWorker != null)
        {
            this.signalSamplingWorker.terminateAllTasks();
        }

        long samplingPeriodInMiliseconds = Math.round(60*1000./signalSamplesPerMinute);    
        double minExpectedVoltage = calibrationWellSpecified ? Math.min(0, calibrationOffsetInVolts) :0;
        double maxExpectedVoltage = calibrationWellSpecified ? Math.min(1.1*(100./calibrationSlopeInPercentsPerVolt + calibrationOffsetInVolts), 10.0) : 10;

        SignalSource transmittanceSource = this.signalSourceController.getSignalSource(numberOfSignalSamplesRecordedSimultanouslyToAverage, minExpectedVoltage, maxExpectedVoltage, signalSamplesPerMinute/60.);

        this.signalSamplingWorker = new SignalSamplingSwingWorker(this, transmittanceSource, samplingPeriodInMiliseconds);                  
        this.signalSamplingWorker.execute();
    }

    public boolean isCalibrateEnabled()
    {
        return calibrateEnabled;
    }

    public void setCalibrateEnabled(boolean enabledNew)
    {        
        this.calibrateEnabled = enabledNew;
        for(SignalModelListener listener: listeners)
        {
            listener.calibrationEnabledChange(enabledNew);
        }
    }

    public void calibrate(RecordingStatus recordingModelStatusBeforeCalibration)
    {
        if(this.calibrateEnabled)
        {            
            for(SignalModelListener listener: listeners)
            {
                listener.calibrationInitialized();
            }

            SignalSource transmittanceSource =  this.signalSamplingWorker != null ? this.signalSamplingWorker.getSignalSourceForSharing(): this.signalSourceController.getSignalSource(numberOfSignalSamplesRecordedSimultanouslyToAverage, 1);

            this.calibrationWorker = new CalibrationSwingWorker(this, transmittanceSource, recordingModelStatusBeforeCalibration, delayInMilisecondsAfterMeasuringBeamIsSwitchedOffDuringCalibration, delayInMilisecondsAfterMeasuringBeamIsSwitchedOnDuringCalibration);
            calibrationWorker.execute();
        }
    } 

    public void terminateCalibrationWorker()
    {
        if(this.calibrationWorker != null && !this.calibrationWorker.isDone())
        {
            this.calibrationWorker.terminateAllTasks();
        }
    }

    public void ensureThatMeasuringBeamIsPreparedForCalibration()
    {
        this.recordingModel.ensureThatMeasuringBeamIsPreparedForCalibration();
    }

    double getSignalReadingInVolts()
    {
        double callCountPerSecond = 0.2;
        //   TransmittanceSource transmittanceSource = this.selectedTransmittanceSourceFactory.getTransmittanceSource(numberOfTransmittanceSamplesToAverage, transmittanceSamplesPerMinute/60.);

        SignalSource signalSource = signalSourceController.getSignalSource(numberOfSignalSamplesRecordedSimultanouslyToAverage, callCountPerSecond);
        RawVoltageSample transmittance =  signalSource.getSample();  

        return transmittance.getValueInVolts();
    }


    public boolean isCalibrationWellSpecified()
    {
        return calibrationWellSpecified;
    }

    protected void calibrationFinishedSuccessfully(Date calibrationDate, double slope, double offset, RecordingStatus previousRecordingStatus)
    {
        setCalibrationOffsetInVolts(offset);
        setCalibrationSlopeInPercentsPerVolt(slope);
        setLatestCalibrationDate(calibrationDate);
        listeners.forEach(listener -> listener.calibrationFinishedSuccessfully(previousRecordingStatus));
    }

    public Date getLatestCalibrationDate()
    {
        return latestCalibrationDate;
    }

    protected void setLatestCalibrationDate(Date latestCalibrationDateNew)
    {
        Date latestCalibrationDateOld = this.latestCalibrationDate;

        if(!Objects.equals(latestCalibrationDateOld, latestCalibrationDateNew))
        {
            this.latestCalibrationDate = latestCalibrationDateNew;

            long calibrationTime = (this.latestCalibrationDate != null) ? this.latestCalibrationDate.getTime(): -1;

            pref.putLong(LATEST_CALIBRATION_TIME, calibrationTime);       
            flushPreferences();

            listeners.forEach(listener -> listener.latestCalibrationDateChanged(latestCalibrationDateOld, latestCalibrationDateNew));
        }
    }

    public double getCalibrationOffsetInVolts()
    {
        return calibrationOffsetInVolts;
    }

    protected void setCalibrationOffsetInVolts(double calibrationOffsetInVoltsNew)
    {
        double calibrationOffsetInVoltsOld = this.calibrationOffsetInVolts;

        if(Double.compare(calibrationOffsetInVoltsOld, calibrationOffsetInVoltsNew) != 0)
        {
            this.calibrationOffsetInVolts = calibrationOffsetInVoltsNew;

            pref.putDouble(LATEST_CALIBRATION_OFFSET, this.calibrationOffsetInVolts);       
            flushPreferences();

            listeners.forEach(listener -> listener.calibrationOffsetInVoltsChanged(calibrationOffsetInVoltsOld, calibrationOffsetInVoltsNew));
            updateCalibrationWellSpecified();
        }
    }

    public double getCalibrationSlopeInPercentsPerVolt()
    {
        return calibrationSlopeInPercentsPerVolt;
    }

    protected void setCalibrationSlopeInPercentsPerVolt(double calibrationSlopeInPercentsPerVoltNew)
    {
        double calibrationSlopeInPercentsPerVoltOld = this.calibrationSlopeInPercentsPerVolt;

        if(Double.compare(calibrationSlopeInPercentsPerVoltOld, calibrationSlopeInPercentsPerVoltNew) != 0)
        {
            this.calibrationSlopeInPercentsPerVolt = calibrationSlopeInPercentsPerVoltNew;

            pref.putDouble(LATEST_CALIBRATION_SLOPE, this.calibrationSlopeInPercentsPerVolt);       
            flushPreferences();

            listeners.forEach(listener -> listener.calibrationSlopeInPercentsPerVoltChanged(calibrationSlopeInPercentsPerVoltOld, calibrationSlopeInPercentsPerVoltNew));
            updateCalibrationWellSpecified();
        }
    }

    private boolean checkIfCalibrationWellSpecified()
    {
        boolean calibrationWellSpecifiedNew = NumberUtilities.isNumeric(this.calibrationOffsetInVolts) && NumberUtilities.isNumeric(this.calibrationSlopeInPercentsPerVolt);
        return calibrationWellSpecifiedNew;
    }

    public void updateCalibrationWellSpecified()
    {
        boolean calibrationWellSpecifiedNew = checkIfCalibrationWellSpecified();
        boolean calibrationWellSpecifiedOld = this.calibrationWellSpecified;

        if(calibrationWellSpecifiedOld != calibrationWellSpecifiedNew)
        {
            this.calibrationWellSpecified = calibrationWellSpecifiedNew;
        }
    }

    public SignalSettingsImmutable getSignalSettings()
    {
        SignalSamplingSettingsImmutable samplingSettings = new SignalSamplingSettingsImmutable(signalSamplesPerMinute, signalSourceController.getUniqueDescription());
        CalibrationSettingsImmutable calibrationSettings = new CalibrationSettingsImmutable(calibrationSlopeInPercentsPerVolt, calibrationOffsetInVolts);

        SignalSettingsImmutable signalSettings = new SignalSettingsImmutable(samplingSettings, calibrationSettings, signalType);

        return signalSettings;      
    }

    private void flushPreferences()
    {
        try {
            pref.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    protected void saveSettingsToPreferences()
    {
        pref.put(SIGNAL_SOURCE_CONTROLLER_ID, this.signalSourceController.getUniqueDescription());
    }

    public void calibrationDoneAfterCancellation(RecordingStatus previousRecordingStatus)
    {
        listeners.forEach(listener -> listener.calibrationDoneAfterCancellation(previousRecordingStatus));
    }

    public void calibrationDoneWithException(RecordingStatus previousRecordingStatus)
    {
        listeners.forEach(listener -> listener.calibrationDoneWithException(previousRecordingStatus));
    }

    void notifyAboutCalibrationPhaseChange(CalibrationPhase calibrationPhaseOld, CalibrationPhase calibrationPhaseNew)
    {
        listeners.forEach(listener -> listener.calibrationPhaseChanged(calibrationPhaseOld, calibrationPhaseNew));
    }

    void notifAboutCurrentCalibrationProcessInPercent(int currentProgressInPercent) 
    {
        listeners.forEach(listener -> listener.progressInPercentOfCurrentCalibrationPhaseChanged(currentProgressInPercent));
    }

    public void addListener(SignalModelListener listener)
    {
        listeners.add(listener);
    }

    public void removePhotometricRecordingModelListener(SignalModelListener listener)
    {
        listeners.remove(listener);
    }

    public static interface SignalModelListener
    {
        public void signalSamplesReceived(List<CalibratedSignalSample> signaleSamples);
        public void calibrationDoneAfterCancellation(RecordingStatus previousRecordingStatus);
        public void calibrationEnabledChange(boolean enabledNew);
        public void calibrationDoneWithException(RecordingStatus previousRecordingStatus);
        public void calibrationFinishedSuccessfully(RecordingStatus previousRecordingStatus);
        public void latestCalibrationDateChanged(Date latestCalibrationDateOld, Date latestCalibrationDateNew);
        public void calibrationOffsetInVoltsChanged(double calibrationOffsetInVoltsOld, double calibrationOffsetInVoltsNew);
        public void calibrationSlopeInPercentsPerVoltChanged(double calibrationSlopeInPercentsPerVoltOld, double calibrationSlopeInPercentsPerVoltNew);
        public void calibrationPhaseChanged(CalibrationPhase calibrationPhaseOld, CalibrationPhase calibrationPhaseNew);
        public void progressInPercentOfCurrentCalibrationPhaseChanged(int currentProgressInPercent);
        public void signalSourceControllerChanged(SignalSourceController signalSourceControllerOld, SignalSourceController signalSourceControllerNew);
        public void signalSamplesPerMinuteChanged(double signalSamplesPerMinuteOld, double signalPerMinuteNew);
        public void maximalSignalSamplesPerMinuteChanged(double maximalSignalSamplesPerMinuteForSelectedSourceOld, double maximalSignalSamplesPerMinuteForSelectedSourceNew);
        public void signalTypeChanged(LightSignalType signalTypeOld, LightSignalType signalTypeNew);
        void calibrationInitialized();
    }
}
