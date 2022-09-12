package chloroplastInterface;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortPacketListener;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.ClearPoints1DTransformation;
import atomicJ.curveProcessing.MultiplePointsAddition1DTransformation;
import atomicJ.data.Channel1DCollection;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.geometricSets.ClosedInterval;
import atomicJ.geometricSets.RealSet;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.save.SavingException;
import atomicJ.utilities.SerializationUtilities;
import atomicJ.utilities.Validation;
import chloroplastInterface.ActinicBeamManualCalibrationModel.ActinicBeamCalibrationImmutable;
import chloroplastInterface.CalibrationSwingWorker.CalibrationPhase;
import chloroplastInterface.ActinicPhaseScheduleSwingWorker.ActinicPhaseRealTiming;
import chloroplastInterface.redPitaya.RedPitayaDeviceListener;
import chloroplastInterface.redPitaya.RedPitayaLockInDevice;
import chloroplastInterface.redPitaya.RedPitayaMeasuringBeamController;
import chloroplastInterface.redPitaya.RedPitayaSignalSourceController;
import chloroplastInterface.redPitaya.RedPitayaSignalSourceDiscoverer;
import chloroplastInterface.ExperimentDescriptionModel.PhotometricDescriptionImmutable;
import chloroplastInterface.SignalSourceModel.SignalModelListener;
import chloroplastInterface.flipper.FlipperModel;
import chloroplastInterface.optics.NullFilter;
import chloroplastInterface.optics.SliderMountedFilter;

public class RecordingModel 
{
    public static final boolean TEST = false;

    private static final boolean DEFAULT_KEEP_MEASURING_BEAM_ON_WHEN_IDLE = true;
    private static final int DEFAULT_MEASURING_BEAM_INTENSITY_IN_PERCENT = 13;
    private static final int DEFAULT_MEASURING_BEAM_FREQUENCY = 1012;

    static final short MAX_VOLTAGE_12_BITS = 4095;
    static final short MAX_FREQUENCY_15_BITS = 32767;

    private static final int MAX_PHASE_COUNT = 1000;

    private static final int MAXIMAL_USED_SIGNAL_SOURCES_COUNT = 100;

    private static final String ACTINIC_BEAM_CALIBRATION_FILE = "ActinicBeamCalibrationFile";

    private final static String KEEP_MEASURING_BEAM_ON_WHEN_IDLE = "KeepMeasuringBeamOnWhenIdle";
    private final static String MEASURING_BEAM_FREQUENCY = "MeasuringBeamFrequency";
    private final static String MEASURING_BEAM_INTENSITY_IN_PERCENT = "MeasuringBeamIntensityInPercent";
    private final static String MEASURING_BEAM_CONTROLLER_ID = "MeasuringBeamControllerID";


    private final static String SIGNAL_SOURCES_COUNT = "SignalSourcesCount";

    private final static String ACTINIC_BEAM_PHASES = "ActinicBeamPhases";

    private final static String OVERLAY_NEW_CHART_ON_PREVIOUS = "OverlayNewChartOnPrevious";

    public static final String PHOTOMETRIC_RECORDING_KEY = "PhotometricRecording";
    public static final String TRANSMITTANCE_CHANNEL_KEY = "Transmittance";
    public static final String TRANSMITTANCE_DERIVATIVE_CHANNEL_KEY = "TransmittanceDerivative";

    private final static Preferences PREF = Preferences.userNodeForPackage(RecordingModel.class).node(RecordingModel.class.getName());

    private List<ActinicPhaseSettings> actinicBeamPhases;
    private File actinicBeamCalibrationFile;
    private ActinicBeamCalibrationImmutable actinicBeamCalibration;
    private final List<RecordingModelListener> modelListeners = new ArrayList<>();
    private final List<RecordedDataReceiver> sampleReceivers = new ArrayList<>();

    private final double initDuration = 5;
    private final StandardTimeUnit initTimeUnit = StandardTimeUnit.MINUTE;
    private final double initIntensity = 50;
    private final int initActinicBeamSliderPositionIndex = 0;

    private PhaseStamp actinicBeamPhase = PhaseStamp.IDLE_INSTANCE;
    private ActinicPhaseRemainder phaseRemainderToResumeAfterStop = null;
    private long latestRecordingOnsetAbsoluteTime = -1;

    private RecordingStatus recordingStatus = RecordingStatus.IDLE;

    private double measuringBeamIntensityInPercent = PREF.getDouble(MEASURING_BEAM_INTENSITY_IN_PERCENT, DEFAULT_MEASURING_BEAM_INTENSITY_IN_PERCENT);
    private double measuringBeamFrequencyInHertz;
    private boolean keepMeasuringBeamOnWhenIdle = PREF.getBoolean(KEEP_MEASURING_BEAM_ON_WHEN_IDLE, DEFAULT_KEEP_MEASURING_BEAM_ON_WHEN_IDLE);

    private double maximalMeasuringBeamModulationFrequency;

    private double preferredMeasuringFrequencyIncrement;
    private double preferredMeasuringFrequencyDecrement;

    private final Map<String, MeasuringBeamController> availableFunctionalMeasuringBeamControllers = new LinkedHashMap<>();    
    private MeasuringBeamController measuringBeamController = TEST ? TestingMeasuringBeamController.getInstance() : DummyMeasuringBeamController.getInstance();
    private final RedPitayaSignalSourceDiscoverer pitayaDiscoverer = RedPitayaSignalSourceDiscoverer.getInstance();

    private ActinicBeamController actinicBeamController = TEST ? TestingActinicBeamController.getInstance() : DummyActinicBeamController.getInstance();

    private File outputFile;
    private SaveFormatType<PhotometricResource> formatType = CLMDataSaveFormatType.getInstance();
    private final ExperimentDescriptionModel experimentDescriptionModel;
    private final FlipperModel flipperModel = new FlipperModel();
    private final Channel1DCollection recordedChannelCollection = new Channel1DCollection(PHOTOMETRIC_RECORDING_KEY);

    private List<String> channelIdentifiers = new ArrayList<>();

    private final Map<String, SignalSourceController> availableFunctionalSignalSourceControllers = new LinkedHashMap<>();

    private final Map<SerialPortVisitor, Boolean> portsVisited = SerialPortVisitor.getMapForRecordingVisits();

    private boolean overlayPlotOnPrevious = PREF.getBoolean(OVERLAY_NEW_CHART_ON_PREVIOUS, false);

    private boolean measuringBeamParametersEnabled;
    private boolean measuringBeamIdleStateModificationEnabled;
    private boolean signalSourceSelectionEnabled;

    private boolean readActinicBeamPhasesFromFileEnabled;

    private boolean runEnabled;
    private boolean stopEnabled;
    private boolean resumeEnabled;
    private boolean cancelEnabled;
    private boolean outputFileSelectionEnabled;

    private int signalSourcesCount;
    private final List<SignalSourceModel> signalSourceModels = new ArrayList<>();//we will keep all signal source models, even if the user reduces their number, so that settings are stored in case the user changes his mind and increases the number of signal sources later

    private final OpticsConfigurationModel opticsModel;

    private ActinicPhaseScheduleSwingWorker lastExecutedActinicBeamPhaseScheduleWorker;

    public RecordingModel(OpticsConfigurationModel opticsModel)
    {    
        this.opticsModel = opticsModel;
        initListenerForOpticsConfiguration();

        SliderMountedFilter defaultSliderFilter = this.opticsModel.getActinicBeamSliderMountedFilterCount() > 0 ? this.opticsModel.getActinicBeamSliderFilter(this.initActinicBeamSliderPositionIndex) : new SliderMountedFilter(this.initActinicBeamSliderPositionIndex, NullFilter.getInstance());
        this.actinicBeamPhases = (List<ActinicPhaseSettings>)SerializationUtilities.getSerializableObject(PREF, ACTINIC_BEAM_PHASES, new ArrayList<>(Arrays.asList(new ActinicPhaseSettings(initDuration, initTimeUnit, initIntensity, defaultSliderFilter))));
        this.experimentDescriptionModel = new ExperimentDescriptionModel(actinicBeamPhases.size());
        initExperimentDescriptionListener();

        if(TEST)
        {
            SignalSourceController testSignalSourceController = TestingSignalSourceController.getInstance();
            this.availableFunctionalSignalSourceControllers.put(testSignalSourceController.getUniqueDescription(), testSignalSourceController);
        }

        List<NIDeviceSignalSourceController> niDevices = NIDeviceSignalSourceController.buildFactoriesWhenPossible(NIDevice.getAvailableNIDevices());
        for(NIDeviceSignalSourceController niDevice : niDevices)
        {
            this.availableFunctionalSignalSourceControllers.put(niDevice.toString(), niDevice);
        }

        List<RedPitayaSignalSourceController> redPitayaControllers = pitayaDiscoverer.refreshDeviceListAndReturnControllers();

        for(RedPitayaSignalSourceController rpController : redPitayaControllers)
        {
            this.availableFunctionalSignalSourceControllers.put(rpController.toString(), rpController);
        }

        this.signalSourcesCount = PREF.getInt(SIGNAL_SOURCES_COUNT, 1);        
        addSignalSourceModels(0, signalSourcesCount);

        if(measuringBeamController.isFunctional())
        {
            this.availableFunctionalMeasuringBeamControllers.put(measuringBeamController.getUniqueDescription(), measuringBeamController);
        }

        for(RedPitayaSignalSourceController rpController : redPitayaControllers)
        {
            MeasuringBeamController mbController = new RedPitayaMeasuringBeamController(rpController.getDevice());
            this.availableFunctionalMeasuringBeamControllers.put(mbController.getUniqueDescription(), mbController);
        }

        String selectedMeasuringBeamControllerKey = PREF.get(MEASURING_BEAM_CONTROLLER_ID, "");
        this.measuringBeamController = !availableFunctionalMeasuringBeamControllers.isEmpty() ? (availableFunctionalMeasuringBeamControllers.containsKey(selectedMeasuringBeamControllerKey) ? availableFunctionalMeasuringBeamControllers.get(selectedMeasuringBeamControllerKey) :  availableFunctionalMeasuringBeamControllers.values().iterator().next()): this.measuringBeamController;

        //code relating to supported frequencies should be executed after measuringBeamController and signalSourceController are created 
        double maxMeasuringBeamModulationFrequency = this.measuringBeamController.getMaximalSupportedFrequencyInHertz();
        for(SignalSourceModel  signalSourceModel : signalSourceModels)
        {
            maxMeasuringBeamModulationFrequency = Math.min(maxMeasuringBeamModulationFrequency, signalSourceModel.getMaximalSupportedFrequencyInHertz());
        }
        this.maximalMeasuringBeamModulationFrequency = maxMeasuringBeamModulationFrequency;

        boolean discreteFrequencyControllerFound = this.measuringBeamController.isSetOfSupportedFrequenciesDiscrete();
        for(SignalSourceModel  signalSourceModel : signalSourceModels)
        {
            discreteFrequencyControllerFound = discreteFrequencyControllerFound || signalSourceModel.isSetOfSupportedFrequenciesDiscrete();
        }

        //these lines must be called after measuringBeamController and signalSourceController are set
        double desiredMeasuringBeamFrequencyInHertzNew = PREF.getDouble(MEASURING_BEAM_FREQUENCY, DEFAULT_MEASURING_BEAM_FREQUENCY);
        this.measuringBeamFrequencyInHertz = findClosestSupportedBeamFrequency(desiredMeasuringBeamFrequencyInHertzNew);

        sendMeasuringBeamFrequencyToControllers(this.measuringBeamFrequencyInHertz);
        double measuringBeamIntensity = this.keepMeasuringBeamOnWhenIdle ? measuringBeamIntensityInPercent: 0;
        attemptToSendMeasuringBeamIntensityToController(measuringBeamIntensity);

        this.preferredMeasuringFrequencyIncrement = getPreferredFrequencyDecrement(measuringBeamFrequencyInHertz);
        this.preferredMeasuringFrequencyDecrement = getPreferredFrequencyDecrement(measuringBeamFrequencyInHertz);

        initializeSerialPorts();
        setEnablednessConsistentWithModelState();

        buildAndExecuteSignalSamplingWorkers();

        String actinicBeamCalibrationFilePreviouslUsedPath = PREF.get(ACTINIC_BEAM_CALIBRATION_FILE,"");
        if(!actinicBeamCalibrationFilePreviouslUsedPath.isEmpty())
        {
            File actinicBeamCalibrationFilePreviouslUsed = new File(actinicBeamCalibrationFilePreviouslUsedPath);
            if(actinicBeamCalibrationFilePreviouslUsed.exists())
            {
                try {
                    this.actinicBeamCalibrationFile = actinicBeamCalibrationFilePreviouslUsed;
                    this.actinicBeamCalibration = ABCFileReader.getInstance().readActinicBeamSettings(actinicBeamCalibrationFilePreviouslUsed);

                    updateAbsoluteActinicLightIntensities();
                    updateAbsoluteActinicLightIntensityUnit(this.actinicBeamCalibration.getAbsoluteLightIntensityUnit());
                } catch (UserCommunicableException e) {
                    e.printStackTrace();
                }
            }
        }

        //we will start listening only after the constructor code is finished, to avoid surpsises
        this.pitayaDiscoverer.addRedPitayaDeviceListener(new RedPitayaLockInDeviceListener());
    }

    public long getLatestRecordingOnsetAbsoluteTime()
    {
        return latestRecordingOnsetAbsoluteTime;
    }

    public int getSignalSourcesCount()
    {
        return signalSourcesCount;
    }

    public void setSignalSourcesCount(int signalSourcesCountNew)
    {
        Validation.requireValueGreaterOrEqualToParameterName(signalSourcesCountNew, 1, "signalSourcesCountNew");

        if(this.signalSourcesCount != signalSourcesCountNew)
        {
            int signalSourcesCountOld = this.signalSourcesCount;
            this.signalSourcesCount = signalSourcesCountNew;

            if(signalSourcesCountNew > signalSourcesCountOld)
            {
                addSignalSourceModels(signalSourcesCountOld, signalSourcesCountNew);
            }
            else
            {
                removeSignalSourceModels(signalSourcesCountOld, signalSourcesCountNew);
            }

            modelListeners.forEach(listener ->listener.signalSourcesCountChanged(signalSourcesCountOld, signalSourcesCountNew));
            sampleReceivers.forEach(sampleReceiver -> sampleReceiver.signalSourcesCountChanged(signalSourcesCountOld, signalSourcesCountNew));
            
            PREF.putInt(SIGNAL_SOURCES_COUNT, this.signalSourcesCount);       
            flushPreferences();
        }
    }

    private void addSignalSourceModels(int signalSourcesCountOld, int signalSourcesCountNew)
    {
        for(int i = signalSourcesCountOld; i < signalSourcesCountNew; i++)
        {
            SignalSourceModel signalModel = new SignalSourceModel(i, this, availableFunctionalSignalSourceControllers, TEST);
            signalModel.addListener(new StandardSignalModelListener(i));
            signalSourceModels.add(signalModel);

            String channelKey = signalModel.getRecordedChannelKey();
            channelIdentifiers.add(channelKey);

            Channel1DData signalChannelData = new FlexibleChannel1DData(new double[][] {}, signalModel.getXQuantity(), signalModel.getYQuantity(), SortedArrayOrder.ASCENDING);
            recordedChannelCollection.addChannel(signalChannelData, channelKey);

            sampleReceivers.forEach(receiver -> receiver.recordedChannelAdded(recordedChannelCollection.getChannel(channelKey)));
        }
    }

    private void removeSignalSourceModels(int signalSourcesCountOld, int signalSourcesCountNew)
    {
        for(int i = signalSourcesCountOld - 1; i >= signalSourcesCountNew; i--)
        {
            SignalSourceModel signalModel = this.signalSourceModels.get(i);
            signalModel.terminate();
            this.signalSourceModels.remove(i);

            String channelKey = signalModel.getRecordedChannelKey();
            recordedChannelCollection.removeChannel(channelKey);

            sampleReceivers.forEach(receiver -> receiver.recordedChannelRemoved(channelKey));
        }

        this.channelIdentifiers = channelIdentifiers.subList(0, signalSourcesCountNew);

    }

    public int getMaximalUsedSignalSourcesCount()
    {
        return MAXIMAL_USED_SIGNAL_SOURCES_COUNT;
    }

    private void initListenerForOpticsConfiguration()
    {
        this.opticsModel.addListener(new OpticsConfigurationListener()
        {
            @Override
            public void actinicBeamFilterTransmittanceInPercentChanged(int filterPositionIndex, double transmittanceInPercentOld,double transmittanceInPercentNew) 
            {
                modelListeners.forEach(listener -> listener.propertiesOfActinicBeamFilterChanged(opticsModel.getActinicBeamSliderFilter(filterPositionIndex)));
            }

            @Override
            public void saveToFileEnabledChanged(boolean enabledOld, boolean enabledNew)
            {                
            }

            @Override
            public void numberOfActinicBeamFiltersChanged(int oldNumber, int newNumber) 
            {             
                List<SliderMountedFilter> currentlyAvailableFiltrers = opticsModel.getAvailableActinicBeamSliderMountedFilters();
                modelListeners.forEach(listener -> listener.availableActinicBeamFiltersChanged(oldNumber, newNumber));

                for(int i = 0; i < actinicBeamPhases.size(); i++)
                {
                    ActinicPhaseSettings phaseSettings = actinicBeamPhases.get(i);
                    SliderMountedFilter selectedFilter = phaseSettings.getSliderFilter();
                    int selectedFilterPosition = selectedFilter.getPositionIndex();

                    if(newNumber == 0)
                    {

                    }
                    else if(selectedFilterPosition >= newNumber)
                    {
                        setActinicBeamFilter(i, currentlyAvailableFiltrers.get(newNumber - 1));
                    }
                }
            }
        });
    }

    public Channel1DCollection getRecordedChannelCollection()
    {
        return recordedChannelCollection;
    }

    private void initExperimentDescriptionListener()
    {
        this.experimentDescriptionModel.addPropertyChangeListener(ExperimentDescriptionModel.DESCRIPTION_TEXT, new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {                
                sampleReceivers.forEach(receiver -> receiver.experimentDescriptionChanged((String)evt.getOldValue(), (String)evt.getNewValue()));
            }
        });
    }

    public ExperimentDescriptionModel getExperimentDescriptionModel()
    {
        return experimentDescriptionModel;
    }

    public FlipperModel getFlipperModel()
    {
        return flipperModel;
    }

    public String getExperimentDescription()
    {
        return experimentDescriptionModel.getDescription();
    }

    private boolean isConnectionWithActinicBeamControllerEstablished()
    {
        boolean connectionsEstablished = this.actinicBeamController.isOpen();        
        return connectionsEstablished;
    }

    private boolean isConnectionWithMeasuringBeamControllerEstablished()
    {
        boolean connectionsEstablished = this.measuringBeamController.isFunctional();        
        return connectionsEstablished;
    }

    protected void registerActinicBeamPort(SerialPort actinicLightPort)
    {     
        if(!actinicBeamController.shouldBeReplacedWhenOtherControllerFound())
        {
            return;
        }

        boolean connectedOld = this.actinicBeamController.isOpen();
        this.actinicBeamController = new ActinicBeamControllerArduinoSerialPort(actinicLightPort);
        boolean connectedNew = new ActinicBeamControllerArduinoSerialPort(actinicLightPort).isOpen();

        communicateSettingsWithNewlySetActinicBeamController();
        setEnablednessConsistentWithModelState();

        modelListeners.forEach(listener -> listener.statusOfConnectionWithActinicBeamControllerChanged(connectedOld, connectedNew));
    }

    private void communicateSettingsWithNewlySetActinicBeamController()
    {
        sendActinicLightIntensityToController(0);
    }

    protected void registerMeasurementBeamPort(SerialPort measurementLightPort)
    {               
        MeasuringBeamController measuringBeamControllerNew = new MeasuringBeamControllerArduino(measurementLightPort);
        registerNewFunctionalMeasuringBeamController(measuringBeamControllerNew);
    }

    private void registerNewFunctionalMeasuringBeamController(MeasuringBeamController controller)
    {
        boolean workingMeasuringBeamControllerAvilableOld = isFunctionalMeasuringBeamControllerAvailable();

        availableFunctionalMeasuringBeamControllers.put(controller.getUniqueDescription(), controller);
        modelListeners.forEach(listener -> listener.functionalMeasuringBeamControllerAdded(controller.getUniqueDescription()));

        if(measuringBeamController.shouldBeReplacedWhenOtherControllerFound())
        {
            setMeasuringBeamController(controller);
        }      

        boolean workingMeasuringBeamControllerAvilableNew = isFunctionalMeasuringBeamControllerAvailable();
        modelListeners.forEach(listener -> listener.availabilityOfFunctionalMeasuringBeamControllersChange(workingMeasuringBeamControllerAvilableOld, workingMeasuringBeamControllerAvilableNew));
    }

    private void removeFunctionalMeasuringBeamController(MeasuringBeamController controller)
    {
        boolean workingMeasuringBeamControllerAvilableOld = isFunctionalMeasuringBeamControllerAvailable();

        availableFunctionalMeasuringBeamControllers.remove(controller.getUniqueDescription());
        modelListeners.forEach(listener -> listener.functionalMeasuringBeamControllerRemoved(controller.getUniqueDescription()));

        if(Objects.equals(this.measuringBeamController,controller))
        {
            MeasuringBeamController measuringBeamControllerNew = availableFunctionalMeasuringBeamControllers.isEmpty() ? (TEST ? TestingMeasuringBeamController.getInstance() : DummyMeasuringBeamController.getInstance()) : availableFunctionalMeasuringBeamControllers.values().iterator().next();
            setMeasuringBeamController(measuringBeamControllerNew);
        }      

        boolean workingMeasuringBeamControllerAvilableNew = isFunctionalMeasuringBeamControllerAvailable();
        modelListeners.forEach(listener -> listener.availabilityOfFunctionalMeasuringBeamControllersChange(workingMeasuringBeamControllerAvilableOld, workingMeasuringBeamControllerAvilableNew));
    }

    private void registerNewFunctionalSignalSourceController(SignalSourceController controller)
    {
        boolean functionalSignalSourceControllerAvailableOld = isFunctionalSignalSourceControllerAvailable();

        availableFunctionalSignalSourceControllers.put(controller.getUniqueDescription(), controller);
        modelListeners.forEach(listener -> listener.functionalSignalControllerAdded(controller));

        for(SignalSourceModel signalModel : signalSourceModels)
        {
            boolean offerAccepted = signalModel.considerOfferOfSignalSourceController(controller);
            if(offerAccepted)
            {
                break;
            }
        }

        boolean functionalSignalSourceControllerAvilableNew = isFunctionalSignalSourceControllerAvailable();
        modelListeners.forEach(listener -> listener.availabilityOfFunctionalSignalSourceControllersChange(functionalSignalSourceControllerAvailableOld, functionalSignalSourceControllerAvilableNew));
    }

    private void removeFunctionalSignalSourceController(SignalSourceController controller)
    {
        boolean functionalSignalSourceControllerAvailableOld = isFunctionalSignalSourceControllerAvailable();

        availableFunctionalSignalSourceControllers.remove(controller.getUniqueDescription());
        modelListeners.forEach(listener ->listener.functionalSignalControllerRemoved(controller));

        for(SignalSourceModel signalModel : signalSourceModels)
        {
            signalModel.replaceSignalControllerIfUsed(controller, Collections.unmodifiableMap(availableFunctionalSignalSourceControllers));
        }  

        boolean functionalSignalSourceControllerAvilableNew = isFunctionalSignalSourceControllerAvailable();
        modelListeners.forEach(listener -> listener.availabilityOfFunctionalSignalSourceControllersChange(functionalSignalSourceControllerAvailableOld, functionalSignalSourceControllerAvilableNew));
    }

    public boolean isFunctionalSignalSourceControllerAvailable()
    {
        boolean functionalSignalSourceAvailable = !availableFunctionalSignalSourceControllers.isEmpty();
        return functionalSignalSourceAvailable;
    }

    public boolean isFunctionalMeasuringBeamControllerAvailable()
    {
        boolean workingMeasuringBeamControllerAvailable = !availableFunctionalMeasuringBeamControllers.isEmpty();
        return workingMeasuringBeamControllerAvailable;
    }

    public void selectMeasuringBeamController(String beamControllerId)
    {
        Validation.requireNonNullParameterName(beamControllerId, "beamControllerId");

        if(!availableFunctionalMeasuringBeamControllers.containsKey(beamControllerId))
        {
            throw new IllegalArgumentException("Unknown beam controller " + beamControllerId);
        }

        MeasuringBeamController controller = availableFunctionalMeasuringBeamControllers.get(beamControllerId);
        setMeasuringBeamController(controller);
    }

    private void setMeasuringBeamController(MeasuringBeamController measuringBeamControllerNew)
    {
        boolean connectedOld = this.measuringBeamController.isFunctional();
        boolean connectedNew = measuringBeamControllerNew.isFunctional();

        boolean softwareSupportOfBeamIntensityOld = measuringBeamParametersEnabled && this.measuringBeamController.isSoftwareControlOfMeasuringBeamIntensitySupported();
        boolean softwareSupportOfBeamIntensityNew = measuringBeamParametersEnabled && measuringBeamControllerNew.isSoftwareControlOfMeasuringBeamIntensitySupported();

        String descriptionOld = this.measuringBeamController.getUniqueDescription();
        String descirptionNew = measuringBeamControllerNew.getUniqueDescription();

        double preferredIncrementOld = this.preferredMeasuringFrequencyIncrement;
        double preferredDecrementOld = this.preferredMeasuringFrequencyDecrement;

        this.measuringBeamController = measuringBeamControllerNew;

        PREF.put(MEASURING_BEAM_CONTROLLER_ID, this.measuringBeamController.getUniqueDescription());       
        flushPreferences();

        setEnablednessConsistentWithModelState();

        double measuringBeamIntensity = this.keepMeasuringBeamOnWhenIdle ? measuringBeamIntensityInPercent: 0;
        attemptToSendMeasuringBeamIntensityToController(measuringBeamIntensity);

        double newFrequencyInHertz = findClosestSupportedBeamFrequency(this.measuringBeamFrequencyInHertz);
        this.preferredMeasuringFrequencyIncrement = getPreferredFrequencyDecrement(newFrequencyInHertz);
        this.preferredMeasuringFrequencyDecrement = getPreferredFrequencyDecrement(newFrequencyInHertz);

        if(Double.compare(this.preferredMeasuringFrequencyIncrement, preferredIncrementOld) != 0 || Double.compare(this.preferredMeasuringFrequencyDecrement, preferredDecrementOld) != 0)
        {
            modelListeners.forEach(listener -> listener.preferredFrequencyIncrementAndDecrementChanged(preferredIncrementOld, this.preferredMeasuringFrequencyIncrement, preferredDecrementOld, this.preferredMeasuringFrequencyDecrement));
        }

        setMeasuringBeamFrequencyInHertz(newFrequencyInHertz);

        if(softwareSupportOfBeamIntensityOld != softwareSupportOfBeamIntensityNew)
        {
            modelListeners.forEach(listener -> listener.supportOfSoftwareControlOfMeasuringBeamIntensityChanged(softwareSupportOfBeamIntensityNew));
        }

        updateMaximalAllowedMeasuringBeamFrequencyInHertz();

        modelListeners.forEach(listener -> listener.measuringBeamControllerChanged(descriptionOld, descirptionNew));
        modelListeners.forEach(listener -> listener.statusOfConnectionWithMeasuringBeamControllerChanged(connectedOld, connectedNew));
    }   

    public List<String> getDescriptionsOfAvailableMeasuringBeamControllers()
    {
        return new ArrayList<>(availableFunctionalMeasuringBeamControllers.keySet());
    }

    public String getActinicBeamControllerDescription()
    {
        String description = this.actinicBeamController.getBeamControllerDescription();
        return description;
    }

    public String getMeasuringBeamControllerDescription()
    {
        String description = this.measuringBeamController.getUniqueDescription();
        return description;
    }

    public void close()
    {
        //to make sure that the DAQ settings are 0 when we start the setup next time
        actinicBeamController.sendActinicLightIntensity(0);

        closeAllSerialPorts();
        pitayaDiscoverer.stop();
        saveSettingsToPreferences();
    }

    private void saveSettingsToPreferences()
    {
        PREF.putBoolean(KEEP_MEASURING_BEAM_ON_WHEN_IDLE, this.keepMeasuringBeamOnWhenIdle);
        PREF.putDouble(MEASURING_BEAM_FREQUENCY, this.measuringBeamFrequencyInHertz);
        PREF.putDouble(MEASURING_BEAM_INTENSITY_IN_PERCENT, this.measuringBeamIntensityInPercent); 
        PREF.putBoolean(OVERLAY_NEW_CHART_ON_PREVIOUS, this.overlayPlotOnPrevious);
        PREF.put(MEASURING_BEAM_CONTROLLER_ID, this.measuringBeamController.getUniqueDescription());

        for(SignalSourceModel signalSourceModel : signalSourceModels)
        {
            signalSourceModel.saveSettingsToPreferences();
        }

        if(this.actinicBeamCalibrationFile != null)
        {
            PREF.put(ACTINIC_BEAM_CALIBRATION_FILE, this.actinicBeamCalibrationFile.getAbsolutePath());
        }

        try {
            SerializationUtilities.putSerializableObject(PREF, ACTINIC_BEAM_PHASES, this.actinicBeamPhases);
        } catch (ClassNotFoundException | IOException
                | BackingStoreException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        flushPreferences();
    }

    private void flushPreferences()
    {
        try {
            PREF.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    protected void sendActinicLightIntensityToController(double lightIntensityInPercent)
    {
        actinicBeamController.sendActinicLightIntensity(lightIntensityInPercent);
    }

    protected void attemptToSendMeasuringBeamIntensityToController(double lightIntensityInPercent)
    {
        if(measuringBeamController.isSoftwareControlOfMeasuringBeamIntensitySupported())
        {
            measuringBeamController.sendMeasuringLightIntensity(lightIntensityInPercent);
        }
    }

    protected void sendMeasuringBeamFrequencyToControllers(double lightFrequencyInHertz)
    {
        measuringBeamController.sendMeasuringBeamFrequency(lightFrequencyInHertz);
    }

    protected void ensureThatMeasuringBeamIsPreparedForCalibration()
    {
        if(keepMeasuringBeamOnWhenIdle)
        {
            attemptToSendMeasuringBeamIntensityToController(measuringBeamIntensityInPercent);
            sendMeasuringBeamFrequencyToControllers(measuringBeamFrequencyInHertz);   
        }
    }

    public double getPhaseDuration(int phaseIndex)
    {        
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");

        double phaseDuration = phaseIndex < actinicBeamPhases.size() ? actinicBeamPhases.get(phaseIndex).getDuration() : 0;

        return phaseDuration;
    }

    public void setPhaseDuration(int phaseIndex, double phaseDurationNew)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, actinicBeamPhases.size(), "phaseIndex");                
        Validation.requireNonNegativeParameterName(phaseDurationNew, "phaseDurationNew");

        ActinicPhaseSettings phase = actinicBeamPhases.get(phaseIndex);
        double phaseDurationOld = phase.getDuration();

        if(Double.compare(phaseDurationOld, phaseDurationNew) != 0)
        {
            double durationInSecondsOld = calculateTotalDurationOfActinicPhasesInMiliseconds()/1000.;   

            phase.setDuration(phaseDurationNew);
            double durationInSecondsNew = calculateTotalDurationOfActinicPhasesInMiliseconds()/1000.;
            sampleReceivers.forEach(receiver ->receiver.recordingDurationSecondsChanged(durationInSecondsOld, durationInSecondsNew));

            modelListeners.forEach(listener ->listener.phaseDurationChanged(phaseIndex, phaseDurationOld, phaseDurationNew));
            sampleReceivers.forEach(receiver -> receiver.phaseDurationChanged(phaseIndex, phaseDurationOld, phaseDurationNew));
            respondToInteractiveActinicBeamSettingsModification();
        }
    }

    public StandardTimeUnit getPhaseDurationUnit(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");

        StandardTimeUnit phaseDurationUnit = phaseIndex < actinicBeamPhases.size() ? actinicBeamPhases.get(phaseIndex).getDurationUnit() : StandardTimeUnit.getDefaultUnit();       
        return phaseDurationUnit;
    }

    public void setPhaseDurationUnit(int phaseIndex, StandardTimeUnit phaseDurationUnitNew)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, actinicBeamPhases.size(), "phaseIndex");                
        Validation.requireNonNullParameterName(phaseDurationUnitNew, "phaseDurationUnitNew");

        ActinicPhaseSettings phase = actinicBeamPhases.get(phaseIndex);
        StandardTimeUnit phaseDurationUnitOld = phase.getDurationUnit();

        if(!Objects.equals(phaseDurationUnitOld, phaseDurationUnitNew))
        {
            double durationInSecondsOld = calculateTotalDurationOfActinicPhasesInMiliseconds()/1000.;   

            phase.setDurationUnit(phaseDurationUnitNew);
            double durationInSecondsNew = calculateTotalDurationOfActinicPhasesInMiliseconds()/1000.;
            sampleReceivers.forEach(receiver -> receiver.recordingDurationSecondsChanged(durationInSecondsOld, durationInSecondsNew));
            modelListeners.forEach(listener -> listener.phaseUnitDurationChanged(phaseIndex, phaseDurationUnitOld, phaseDurationUnitNew));
            sampleReceivers.forEach(receiver -> receiver.phaseUnitDurationChanged(phaseIndex, phaseDurationUnitOld, phaseDurationUnitNew));
            respondToInteractiveActinicBeamSettingsModification();
        }
    }

    private double calculateTotalDurationOfActinicPhasesInMiliseconds()
    {
        double duration = 0;

        for(ActinicPhaseSettings phase : actinicBeamPhases)
        {
            duration = duration + phase.getDurationInMiliseconds();
        }

        return duration;
    }

    //phaseIndex starts with 0
    private double calculateTotalDurationOfActinicPhasesBeforeIthPhaseInMiliseconds(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, actinicBeamPhases.size(), "phaseIndex");                

        double duration = 0;

        for(int i = 0; i < phaseIndex; i++)
        {
            duration += actinicBeamPhases.get(i).getDurationInMiliseconds();
        }

        return duration;
    }

    public double getDurationOfPhaseInSeconds(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, actinicBeamPhases.size(), "phaseIndex");                

        return actinicBeamPhases.get(phaseIndex).getDurationInMiliseconds()/1000.;
    }

    public double getTotalDurationOfActinicPhasesInSeconds()
    {
        double duration = calculateTotalDurationOfActinicPhasesInMiliseconds()/1000.;
        return duration;
    }

    //phaseIndex starts with 0
    public double getTotalDurationOfActinicPhasesBeforeIthPhaseInSeconds(int phaseIndex)
    {
        double duration = calculateTotalDurationOfActinicPhasesBeforeIthPhaseInMiliseconds(phaseIndex)/1000.;
        return duration;
    }

    public Date getExpectedPhaseEndTime(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");

        boolean canDateBeCalculated = latestRecordingOnsetAbsoluteTime > -1 && (lastExecutedActinicBeamPhaseScheduleWorker != null) && !lastExecutedActinicBeamPhaseScheduleWorker.isCancelled();
        Date phaseOnsetTime = phaseIndex < actinicBeamPhases.size() && canDateBeCalculated ? new Date((long) (latestRecordingOnsetAbsoluteTime + calculateTotalDurationOfActinicPhasesBeforeIthPhaseInMiliseconds(phaseIndex) + actinicBeamPhases.get(phaseIndex).getDurationInMiliseconds())) : null;

        return phaseOnsetTime;
    }

    public double getActinicBeamIntensityInPercent(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, actinicBeamPhases.size(), "phaseIndex");                

        double phaseIntensityInPercent = actinicBeamPhases.get(phaseIndex).getBeamIntensityInPercent();

        return phaseIntensityInPercent;
    }

    public void setActinicBeamIntensityInPercent(int phaseIndex, double beamIntensityInPercentNew)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireNonNegativeParameterName(beamIntensityInPercentNew, "beamIntensityInPercentNew");
        Validation.requireValueSmallerOrEqualToParameterName(beamIntensityInPercentNew, 100, "beamIntensityInPercentNew");

        ActinicPhaseSettings phase = actinicBeamPhases.get(phaseIndex);
        double beamIntensityInPercentOld = phase.getBeamIntensityInPercent();

        if(Double.compare(beamIntensityInPercentOld, beamIntensityInPercentNew) != 0)
        {            
            phase.setBeamIntensityInPercent(beamIntensityInPercentNew);
            modelListeners.forEach(listener -> listener.actinicLightIntensityInPercentChanged(phaseIndex, beamIntensityInPercentOld, beamIntensityInPercentNew));

            updateAbsoluteActinicBeamIntensities(phaseIndex);
            respondToInteractiveActinicBeamSettingsModification();
        }
    }

    public SliderMountedFilter getActinicBeamFilter(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, actinicBeamPhases.size(), "phaseIndex");                

        SliderMountedFilter filter = actinicBeamPhases.get(phaseIndex).getSliderFilter();       
        return filter;
    }

    private boolean isNonZeroActinicBeamIntensityScheduled()
    {
        boolean nonZeroActinicBeamIntensityScheduled = false;

        for(ActinicPhaseSettings actinicBeamPhase : actinicBeamPhases)
        {
            double intensityInPercent = actinicBeamPhase.getBeamIntensityInPercent();
            if(intensityInPercent > 0)
            {
                nonZeroActinicBeamIntensityScheduled = true;
                break;
            }
        }

        return nonZeroActinicBeamIntensityScheduled;
    }

    private SliderMountedFilter getFirstFilterForNonZeroIntensityPhase()
    {
        SliderMountedFilter filter = null;

        for(ActinicPhaseSettings actinicBeamPhase : actinicBeamPhases)
        {
            double intensityInPercent = actinicBeamPhase.getBeamIntensityInPercent();
            if(intensityInPercent > 0)
            {
                filter = actinicBeamPhase.getSliderFilter();
                break;
            }
        }

        return filter;
    }

    public void setActinicBeamFilter(int phaseIndex, SliderMountedFilter filtertNew)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, actinicBeamPhases.size(), "phaseIndex");                
        Validation.requireNonNullParameterName(filtertNew, "filtertNew");

        ActinicPhaseSettings phase = actinicBeamPhases.get(phaseIndex);
        SliderMountedFilter filterOld = phase.getSliderFilter();

        if(!Objects.equals(filterOld, filtertNew))
        {
            phase.setFilter(filtertNew);
            modelListeners.forEach(listener ->listener.actinicBeamFilterChanged(phaseIndex, filterOld, filtertNew));

            respondToInteractiveActinicBeamSettingsModification();
            updateAbsoluteActinicBeamIntensities(phaseIndex);
        }
    }

    public void readInActinicPhaseSettingsFromFile(File f) throws UserCommunicableException
    {               
        List<ActinicPhaseSettingsImmutable> phaseSettings = CLMFileReader.getInstance().readActinicBeamSettings(f);

        int phaseCountNew = phaseSettings.size();

        setPhaseCount(phaseCountNew);

        for(int i = 0; i<phaseCountNew;i++)
        {
            ActinicPhaseSettingsImmutable iThPhaseSettings = phaseSettings.get(i);
            setPhaseDuration(i, iThPhaseSettings.getDuration());
            setPhaseDurationUnit(i, iThPhaseSettings.getDurationUnit());
            setActinicBeamIntensityInPercent(i, iThPhaseSettings.getBeamIntensityInPercent());
            setActinicBeamFilter(i, iThPhaseSettings.getFilter());
        }
    }

    public List<FileFilter> getFileFiltersForReadingActinicBeamPhases()
    {
        return Collections.singletonList(CLMFileReaderFactory.getInstance().getFileFilter());
    }

    public List<FileFilter> getFileFiltersForReadingActinicBeamCalibrationFile()
    {
        return Collections.singletonList(ABCFileReader.getInstance().getFileFilter());
    }

    public File getActinicBeamCalibrationFile()
    {
        return actinicBeamCalibrationFile;
    }

    public void setActinicBeamCalibrationFile(File actinicBeamCalibrationFileNew) throws UserCommunicableException
    {
        if(!Objects.equals(actinicBeamCalibrationFile, actinicBeamCalibrationFileNew))
        {
            if(actinicBeamCalibrationFileNew != null && actinicBeamCalibrationFileNew.exists())
            {
                File actinicBeamCalibrationFileOld = this.actinicBeamCalibrationFile;
                this.actinicBeamCalibrationFile = actinicBeamCalibrationFileNew;

                this.actinicBeamCalibration = ABCFileReader.getInstance().readActinicBeamSettings(actinicBeamCalibrationFileNew);

                modelListeners.forEach(listener ->listener.actinicBeamCalibrationFileChanged(actinicBeamCalibrationFileOld, actinicBeamCalibrationFileNew));

                updateAbsoluteActinicLightIntensities();

                IrradianceUnitType unitType = (actinicBeamCalibration != null) ? actinicBeamCalibration.getAbsoluteLightIntensityUnit() :null;
                updateAbsoluteActinicLightIntensityUnit(unitType);
            }
        }
    }

    private void updateAbsoluteActinicLightIntensityUnit(IrradianceUnitType unitType)
    {
        Validation.requireNonNullParameterName(unitType, "unitType");

        experimentDescriptionModel.setUnitType(unitType);
    }

    private void updateAbsoluteActinicLightIntensities()
    {
        for(int i = 0; i<actinicBeamPhases.size();i++)
        {
            updateAbsoluteActinicBeamIntensities(i);
        }
    }

    private void updateAbsoluteActinicBeamIntensities(int phaseIndex)
    {
        ActinicPhaseSettings phase = actinicBeamPhases.get(phaseIndex);
        double absoluteIntensityNew = (this.actinicBeamCalibration != null) ? this.actinicBeamCalibration.convertExactlyIntensityInPercentsToAbsoluteValue(phase.getBeamIntensityInPercent(), phase.getSliderFilter().getFilter()):Double.NaN;
        modelListeners.forEach(listener -> listener.absoluteLightIntensityChanged(phaseIndex, absoluteIntensityNew));

        if(!Double.isNaN(absoluteIntensityNew))
        {
            experimentDescriptionModel.setActinicBeamIrradianceValue(absoluteIntensityNew, phaseIndex);
        }
    }

    public double getAbsoluteIntensityOfActinicLight(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");

        ActinicPhaseSettings phase = actinicBeamPhases.get(phaseIndex);
        double intensity = (this.actinicBeamCalibration != null) ? this.actinicBeamCalibration.convertExactlyIntensityInPercentsToAbsoluteValue(phase.getBeamIntensityInPercent(), phase.getSliderFilter().getFilter()) : Double.NaN;

        return intensity;
    }

    public double getMeasuringLightIntensityInPercent()
    {
        return measuringBeamIntensityInPercent;
    }

    public void setMeasuringBeamIntensityInPercent(double measuringBeamIntensityInPercentNew)
    {
        Validation.requireNonNegativeParameterName(measuringBeamIntensityInPercentNew, "measuringBeamIntensityInPercentNew");
        Validation.requireValueSmallerOrEqualToParameterName(measuringBeamIntensityInPercentNew, 100, "measuringBeamIntensityInPercentNew");

        double measuringBeamIntensityInPercentOld = this.measuringBeamIntensityInPercent;

        if(Double.compare(measuringBeamIntensityInPercentOld, measuringBeamIntensityInPercentNew) != 0)
        {
            this.measuringBeamIntensityInPercent = measuringBeamIntensityInPercentNew;
            if(this.keepMeasuringBeamOnWhenIdle && RecordingStatus.IDLE.equals(this.recordingStatus))
            {
                attemptToSendMeasuringBeamIntensityToController(measuringBeamIntensityInPercentNew);
            }
            modelListeners.forEach(listener ->listener.measuringBeamIntensityInPercentChanged(measuringBeamIntensityInPercentOld, measuringBeamIntensityInPercentNew));
        }
    }

    public boolean isSoftwareControlOfMeasuringLightIntensitySupported()
    {
        boolean supported = this.measuringBeamController.isSoftwareControlOfMeasuringBeamIntensitySupported();
        return supported;
    }

    public boolean isMeasuringBeamIntensityModificationEnabled()
    {
        boolean enabled = measuringBeamParametersEnabled && this.measuringBeamController.isSoftwareControlOfMeasuringBeamIntensitySupported();
        return enabled;
    }

    public double getMeasuringBeamFrequencyInHertz()
    {
        return measuringBeamFrequencyInHertz;
    }

    public void setDesiredMeasuringLightFrequencyInHertz(double desiredMeasuringBeamFrequencyInHertz)
    {
        Validation.requireNonNegativeParameterName(desiredMeasuringBeamFrequencyInHertz, "desiredMeasuringLightFrequencyInHertzNew");

        double preferredIncrementOld = getPreferredFrequencyDecrement(this.measuringBeamFrequencyInHertz);
        double preferredDecrementOld = getPreferredFrequencyDecrement(this.measuringBeamFrequencyInHertz);

        double measuringBeamFrequencyInHertzNew = findClosestSupportedBeamFrequency(desiredMeasuringBeamFrequencyInHertz);
        setMeasuringBeamFrequencyInHertz(measuringBeamFrequencyInHertzNew);

        //we cannot move this code to setMeasuringBeamFrequencyInHertz, as setMeasuringBeamFrequencyInHertz is called by controller setting methods, which
        //needs to notifyAboutChangeInPreferredFrequencyIncrementAndDecrement even if there is no change in measuringBeamFrequency
        double newFrequencyInHertz = findClosestSupportedBeamFrequency(measuringBeamFrequencyInHertzNew);
        double preferredIncrementNew = getPreferredFrequencyDecrement(newFrequencyInHertz);
        double preferredDecrementNew = getPreferredFrequencyDecrement(newFrequencyInHertz);

        if(Double.compare(preferredIncrementNew, preferredIncrementOld) != 0 || Double.compare(preferredDecrementNew, preferredDecrementOld) != 0)
        {
            modelListeners.forEach(listener -> listener.preferredFrequencyIncrementAndDecrementChanged(preferredIncrementOld, preferredIncrementNew, preferredDecrementOld, preferredDecrementNew));
        }
    }

    private void setMeasuringBeamFrequencyInHertz(double measuringBeamFrequencyInHertzNew)
    {                
        double measuringLightFrequencyInHertzOld = this.measuringBeamFrequencyInHertz;

        if(Double.compare(measuringLightFrequencyInHertzOld, measuringBeamFrequencyInHertzNew) != 0)
        {
            this.measuringBeamFrequencyInHertz = measuringBeamFrequencyInHertzNew;
            if(!RecordingStatus.IDLE.equals(this.recordingStatus) || (this.keepMeasuringBeamOnWhenIdle && RecordingStatus.IDLE.equals(this.recordingStatus)))
            {
                sendMeasuringBeamFrequencyToControllers(this.measuringBeamFrequencyInHertz);
            }
        }
        //we want to notify registered listeners about the frequency even if it had not changed
        //in case one of them called setDesiredMeasuringLightFrequencyInHertz but the change was not possible
        modelListeners.forEach(listener -> listener.updateMeasuringBeamFrequencyInHertz(measuringLightFrequencyInHertzOld, this.measuringBeamFrequencyInHertz));
    }

    private double findClosestSupportedBeamFrequency(double desiredMeasuringBeamFrequencyInHertz)
    {
        RealSet supportedFrequencies = getSetOfFrequenciesSupportedByAllFrequencyDependentControllers();
        if(supportedFrequencies.isEmpty())
        {
            throw new IllegalStateException("No frequency satisfies the requirments of the signal or measuring beam controllers currently in use;");
        }

        double closestSupportedFrequency = supportedFrequencies.getClosestContainedDoubleValue(desiredMeasuringBeamFrequencyInHertz);

        return closestSupportedFrequency;
    }


    public double getMaximalAllowedMeasuringBeamFrequencyInHertz()
    {
        double maxFrequency = maximalMeasuringBeamModulationFrequency;
        return maxFrequency;
    }

    private void updateMaximalAllowedMeasuringBeamFrequencyInHertz()
    {
        double maxFrequencyNew = this.measuringBeamController.getMaximalSupportedFrequencyInHertz();

        for(SignalSourceModel model : this.signalSourceModels)
        {
            maxFrequencyNew = Math.min(maxFrequencyNew, model.getMaximalSupportedFrequencyInHertz());
        }

        final double maxFrequencyFinal= maxFrequencyNew;
        if(this.maximalMeasuringBeamModulationFrequency != maxFrequencyNew)
        {
            double maxFrequencyOld = this.maximalMeasuringBeamModulationFrequency;
            this.maximalMeasuringBeamModulationFrequency = maxFrequencyOld;

            modelListeners.forEach(listener -> listener.maximalMeasuringBeamFrequencyInHertzChanged(maxFrequencyOld, maxFrequencyFinal));
        }
    }

    public double getPreferredFrequencyIncrement()
    {
        return getPreferredFrequencyIncrement(this.measuringBeamFrequencyInHertz);
    }

    private double getPreferredFrequencyIncrement(double currentFrequency)
    {        
        RealSet setOfFrequenciesSupportedByAllControllers = getSetOfFrequenciesSupportedByAllFrequencyDependentControllers();
        RealSet setOfSupportedFrequenciesEqualOrGreaterThanCurrent = setOfFrequenciesSupportedByAllControllers.intersect(new ClosedInterval(currentFrequency, Double.MAX_VALUE));

        FrequencyDependentController controller = getControllerToFavourWhenQuerringAboutPreferredFrequencyChanges();

        double incrementPreferredByMeasuringBeamController = controller.getPreferredFrequencyIncrement(currentFrequency);      
        double frequencyAfterIncrementation = setOfSupportedFrequenciesEqualOrGreaterThanCurrent.getClosestContainedDoubleValue(currentFrequency + incrementPreferredByMeasuringBeamController);

        double acceptableFrequencyIncrement = frequencyAfterIncrementation - currentFrequency;

        return acceptableFrequencyIncrement;
    }

    public double getPreferredFrequencyDecrement()
    {
        return getPreferredFrequencyDecrement(this.measuringBeamFrequencyInHertz);
    }

    private double getPreferredFrequencyDecrement(double currentFrequency)
    {        
        RealSet setOfFrequenciesSupportedByAllControllers = getSetOfFrequenciesSupportedByAllFrequencyDependentControllers();
        RealSet setOfSupportedFrequenciesEqualOrSmallerThanCurrent = setOfFrequenciesSupportedByAllControllers.intersect(new ClosedInterval(-Double.MAX_VALUE, currentFrequency));

        FrequencyDependentController controller = getControllerToFavourWhenQuerringAboutPreferredFrequencyChanges();

        double decrementPreferredByMeasuringBeamController = controller.getPreferredFrequencyDecrement(currentFrequency);      
        double frequencyAfterDecrementation = setOfSupportedFrequenciesEqualOrSmallerThanCurrent.getClosestContainedDoubleValue(currentFrequency - decrementPreferredByMeasuringBeamController);


        double acceptableFrequencyDecrement = currentFrequency - frequencyAfterDecrementation;

        return acceptableFrequencyDecrement;
    }

    private FrequencyDependentController getControllerToFavourWhenQuerringAboutPreferredFrequencyChanges()
    {
        if(measuringBeamController.isSetOfSupportedFrequenciesDiscrete())
        {
            return measuringBeamController;
        }

        FrequencyDependentController controller = measuringBeamController;

        for(SignalSourceModel model : signalSourceModels)
        {
            if(model.isSetOfSupportedFrequenciesDiscrete())
            {
                controller = model.getSelectedSignalSourceController();
                break;
            }
        }

        return controller;
    }

    private RealSet getSetOfFrequenciesSupportedByAllFrequencyDependentControllers()
    {
        RealSet supportedFrequencies = this.measuringBeamController.getSupportedFrequencies();

        for(SignalSourceModel signalModel : signalSourceModels)
        {
            supportedFrequencies = supportedFrequencies.intersect(signalModel.getSupportedFrequencies());
        }

        return supportedFrequencies;
    }

    public boolean isKeepMeasuringBeamOnWhenIdle()
    {
        return keepMeasuringBeamOnWhenIdle;
    }

    public void setKeepMeasuringBeamOnWhenIdle(boolean keepMeasuringBeamOnWhenIdleNew)
    {
        boolean keepMeasuringBeamOnWhenIdleOld = this.keepMeasuringBeamOnWhenIdle;

        if(keepMeasuringBeamOnWhenIdleOld != keepMeasuringBeamOnWhenIdleNew)
        {
            this.keepMeasuringBeamOnWhenIdle = keepMeasuringBeamOnWhenIdleNew;
            if(RecordingStatus.IDLE.equals(this.recordingStatus))
            {
                double intensity = keepMeasuringBeamOnWhenIdleNew ? measuringBeamIntensityInPercent: 0.;
                attemptToSendMeasuringBeamIntensityToController(intensity);
            }

            modelListeners.forEach(listener -> listener.setKeepMeasuringBeamOnWhenIdleChanged(keepMeasuringBeamOnWhenIdleOld, keepMeasuringBeamOnWhenIdleNew));
        }
    }

    public int getMaximumAllowedActinicBeamPhaseCount()
    {
        return MAX_PHASE_COUNT;
    }

    public int getActinicBeamPhaseCount()
    {
        return actinicBeamPhases.size();
    }

    public void setPhaseCount(int phaseCountNew)
    {        
        Validation.requireNonNegativeParameterName(phaseCountNew, "phaseCountNew");
        Validation.requireValueSmallerOrEqualToParameterName(phaseCountNew, MAX_PHASE_COUNT, "phaseCountNew");                

        int phaseCountOld = actinicBeamPhases.size();

        if(phaseCountNew == phaseCountOld)
        {
            return;
        }

        double durationInSecondsOld = calculateTotalDurationOfActinicPhasesInMiliseconds()/1000.;   

        if(phaseCountNew < phaseCountOld)
        {
            this.actinicBeamPhases = new ArrayList<>(actinicBeamPhases.subList(0, phaseCountNew));//we need to make sure it is array list for serialization purposes, as the returned sublist is not serializable
        }
        else if(phaseCountNew > phaseCountOld)
        {
            double duration = phaseCountOld > 0 ? actinicBeamPhases.get(phaseCountOld - 1).getDuration() : initDuration;
            StandardTimeUnit durationTimeUnit = phaseCountOld > 0 ? actinicBeamPhases.get(phaseCountOld - 1).getDurationUnit() : initTimeUnit;
            double intensity = phaseCountOld > 0 ? actinicBeamPhases.get(phaseCountOld - 1).getBeamIntensityInPercent(): initIntensity;
            SliderMountedFilter filter = phaseCountOld > 0 ? actinicBeamPhases.get(phaseCountOld - 1).getSliderFilter(): new SliderMountedFilter(initActinicBeamSliderPositionIndex, NullFilter.getInstance()) ;

            for(int i = 0; i<(phaseCountNew - phaseCountOld); i++)
            {
                ActinicPhaseSettings recordingPhaseNew = new ActinicPhaseSettings(duration, durationTimeUnit, intensity, filter);
                this.actinicBeamPhases.add(recordingPhaseNew);
            }
        }

        double durationInSecondsNew = calculateTotalDurationOfActinicPhasesInMiliseconds()/1000.;   
        sampleReceivers.forEach(receiver ->receiver.recordingDurationSecondsChanged(durationInSecondsOld, durationInSecondsNew));

        modelListeners.forEach(listener -> listener.numberOfActinicPhasesChanged(phaseCountOld, phaseCountNew));
        sampleReceivers.forEach(receiver -> receiver.numberOfActinicPhasesChanged(phaseCountOld, phaseCountNew));

        this.experimentDescriptionModel.setActinicBeamPhaseCount(phaseCountNew);

        respondToInteractiveActinicBeamSettingsModification();
    }

    public int getFinishedActinicBeamPhaseCount()
    {
        return actinicBeamPhase.getFinishedPhaseCount();
    }

    public List<ActinicPhaseSettingsImmutable> getRecordingPhases()
    {
        List<ActinicPhaseSettingsImmutable> immutablePhases = new ArrayList<>();

        for(ActinicPhaseSettings phase : actinicBeamPhases)
        {
            immutablePhases.add(phase.getImmutableCopy());
        }

        return immutablePhases;
    }

    public PhaseStamp getActinicBeamPhase()
    {
        return actinicBeamPhase;
    }

    protected void setActinicBeamPhase(PhaseStamp phaseNew, boolean isScheduleContinuedAfterStop)
    {
        if(phaseNew.getFinishedPhaseCount() > actinicBeamPhases.size())
        {
            throw new IllegalArgumentException("The argument finishedPhaseCountNew cannot be greater than number of phases");
        }

        if(!Objects.equals(this.actinicBeamPhase, phaseNew))
        {   
            if(phaseNew.isFirstPhaseOfRecording() && !isScheduleContinuedAfterStop)
            {
                setLatestRecordingOnsetAbsoluteTime(phaseNew.getOnsetAbsoluteTimeInMiliseconds());

                //the status is not yet "Running" when the "Run"button is clicked, as the SwingWorker may take time to execute
                setRecordingStatus(RecordingStatus.RUNNING);

                notifyAboutChangeOfExpectedPhaseEnd();
            }

            PhaseStamp phaseOld = this.actinicBeamPhase;
            this.actinicBeamPhase = phaseNew;

            modelListeners.forEach(listener -> listener.actinicBeamPhaseChange(this.latestRecordingOnsetAbsoluteTime, phaseOld, phaseNew));
        }
    }

    private void setLatestRecordingOnsetAbsoluteTime(long latestRecordingOnsetAbsoluteTimNew)
    {
        this.latestRecordingOnsetAbsoluteTime = latestRecordingOnsetAbsoluteTimNew;

        for(SignalSourceModel signalModel : signalSourceModels)
        {
            signalModel.setlatestRecordingOnsetAbsoluteTime(latestRecordingOnsetAbsoluteTimNew);
        }
    }

    public boolean isReadActinicBeamPhasesFromFileEnabled()
    {
        return readActinicBeamPhasesFromFileEnabled;
    }

    private void setReadActinicBeamPhasesFromFileEnabled(boolean enabledNew)
    {
        if(this.readActinicBeamPhasesFromFileEnabled != enabledNew)
        {
            this.readActinicBeamPhasesFromFileEnabled = enabledNew;
            for(RecordingModelListener listener: modelListeners)
            {
                listener.readActinicBeamPhasesFromFileEnabled(enabledNew);
            }
        }
    }

    //should be called by the SwingWorker done
    protected void runFinishedAfterCancelling(List<ActinicPhaseRealTiming> phasesRealTimings)
    {
        if(RecordingStatus.CANCELLING_IN_PROGRESS.equals(recordingStatus))
        {
            setActinicBeamPhase(PhaseStamp.IDLE_INSTANCE, false);
            setRecordingStatus(RecordingStatus.IDLE);
        }
        else if (RecordingStatus.STOPPED.equals(recordingStatus))
        {                        
            if(!phasesRealTimings.isEmpty())
            {
                ActinicPhaseRealTiming lastExecutedPhaseTimings = phasesRealTimings.get(phasesRealTimings.size() - 1);
                this.phaseRemainderToResumeAfterStop = lastExecutedPhaseTimings.getMismatchPhase();                
            }
            else 
            {
                this.phaseRemainderToResumeAfterStop = null;
            }        
        }
        else if (RecordingStatus.ACTINIC_BEAM_SETTINGS_UNDER_MODIFICATION_WHEN_STOPPED.equals(recordingStatus))
        {                        
            handleSchedulingTerminationAfterSettingsWereModified(phasesRealTimings);
            setRecordingStatus(RecordingStatus.STOPPED);
        }
        else if (RecordingStatus.ACTINIC_BEAM_SETTINGS_UNDER_MODIFICATION_WHEN_RUNNING.equals(recordingStatus))
        {                
            handleSchedulingTerminationAfterSettingsWereModified(phasesRealTimings);          
            buildAndExecuteActinicBeamPhaseScheduleWorkerAfterSettingsUpdateDuringRecording();            
            setRecordingStatus(RecordingStatus.RUNNING);
        }
    }

    private void handleSchedulingTerminationAfterSettingsWereModified(List<ActinicPhaseRealTiming> phasesRealTimings)
    {
        if(!phasesRealTimings.isEmpty())
        {
            ActinicPhaseRealTiming lastExecutedPhaseTimings = phasesRealTimings.get(phasesRealTimings.size() - 1);
            int lastExecutedPhaseIndex = lastExecutedPhaseTimings.getPhaseIndex();
            ActinicPhase terminatedPhase = actinicBeamPhases.get(lastExecutedPhaseIndex);

            /*phaseRemainderToResumeAfterStop is the terminated phase if the spinners for increasing duration where clicked multiple times quickly*/
            long furtherIncreaseInPhaseLength = (long) (this.phaseRemainderToResumeAfterStop != null ? terminatedPhase.getDurationInMiliseconds() - this.phaseRemainderToResumeAfterStop.getOriginalPhaseDurationInMiliseconds(): terminatedPhase.getDurationInMiliseconds()- lastExecutedPhaseTimings.getIntendedDurationInMiliseconds());
            this.phaseRemainderToResumeAfterStop = lastExecutedPhaseTimings.getMismatchPhase(terminatedPhase.getDurationInMiliseconds(), furtherIncreaseInPhaseLength);                
        }
        else 
        {
            this.phaseRemainderToResumeAfterStop = null;
        }     

        notifyAboutChangeOfExpectedPhaseEnd();
    }

    protected void runFinishedWithException()
    {
        setRecordingStatus(RecordingStatus.IDLE);
        terminateActinicBeamPhaseScheduleWorker();

        this.flipperModel.notifyAboutRunFinishedWithException();
    }

    protected void runFinishedSuccessfully()
    {
        this.phaseRemainderToResumeAfterStop = null;

        if(!this.keepMeasuringBeamOnWhenIdle)
        {
            attemptToSendMeasuringBeamIntensityToController(0.);
        }
        this.flipperModel.notifyAboutRunFinishedSuccessfully();

        setRecordingStatus(RecordingStatus.IDLE);
        setActinicBeamPhase(PhaseStamp.IDLE_INSTANCE, false);

        finishFileSaving();
    }

    public RecordingStatus getRecordingStatus()
    {
        return recordingStatus;
    }

    private void setRecordingStatus(RecordingStatus recordingStatusNew)
    {
        this.recordingStatus = recordingStatusNew;    

        if(RecordingStatus.IDLE.equals(recordingStatusNew))
        {
            double intensity = this.keepMeasuringBeamOnWhenIdle ? measuringBeamIntensityInPercent: 0.;
            attemptToSendMeasuringBeamIntensityToController(intensity);
        }
        setEnablednessConsistentWithModelState();
    }

    private void setEnablednessConsistentWithModelState()
    {
        boolean connectionWithActinicBeamControllerEstablished = isConnectionWithActinicBeamControllerEstablished();
        boolean connectionWithMeasuringBeamControllerEstablished = isConnectionWithMeasuringBeamControllerEstablished();
        boolean connectionWithAllSignalSourcesEstablished = isConnectionWithAllSignalSourceEstablished();
        boolean calibrationSpecified = isCalibrationWellSpecifiedForAllSources();
        boolean outputFileSpecified = (this.outputFile != null);

        setRunEnabled(calibrationSpecified && outputFileSpecified && this.recordingStatus.isRunEnabled(connectionWithActinicBeamControllerEstablished, connectionWithMeasuringBeamControllerEstablished,connectionWithAllSignalSourcesEstablished));
        setStopEnabled(this.recordingStatus.isStopEnabled(connectionWithActinicBeamControllerEstablished, connectionWithMeasuringBeamControllerEstablished,connectionWithAllSignalSourcesEstablished));
        setResumeEnabled(calibrationSpecified && outputFileSpecified && this.recordingStatus.isResumeEnabled(connectionWithActinicBeamControllerEstablished, connectionWithMeasuringBeamControllerEstablished,connectionWithAllSignalSourcesEstablished));
        setCancelEnabled(this.recordingStatus.isCancelEnabled(connectionWithActinicBeamControllerEstablished, connectionWithMeasuringBeamControllerEstablished,connectionWithAllSignalSourcesEstablished));


        for(int signalIndex = 0; signalIndex < signalSourceModels.size();signalIndex++)
        {
            SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);
            boolean connectionWithIthSignalSourceEstablished = signalSourceModel.isConnectionWithSignalSourceEstablished();
            setCalibrateEnabled(signalIndex, this.recordingStatus.isCalibrateEnabled(connectionWithActinicBeamControllerEstablished, connectionWithMeasuringBeamControllerEstablished,connectionWithIthSignalSourceEstablished));  
        }

        setMeasuringBeamParametersModificationEnabled(this.recordingStatus.isMeasuringBeamParameterModificationsEnabled(connectionWithActinicBeamControllerEstablished, connectionWithMeasuringBeamControllerEstablished,connectionWithAllSignalSourcesEstablished));
        setMeasuringBeamIdleStateModificationEnabled(this.recordingStatus.isMeasuringBeamIdleStateBehaviourModificationsEnabled(connectionWithActinicBeamControllerEstablished, connectionWithMeasuringBeamControllerEstablished, connectionWithAllSignalSourcesEstablished));
        setSignaleSourceSelectionEnabled(this.recordingStatus.isSignalSourceSelectionEnabled(connectionWithActinicBeamControllerEstablished, connectionWithMeasuringBeamControllerEstablished, connectionWithAllSignalSourcesEstablished));
        setOutputFileSelectionEnabled(this.recordingStatus.isOutputFileSelectionEnabled());
        setReadActinicBeamPhasesFromFileEnabled(this.recordingStatus.iReadActinicBeamPhasesFromFileEnabled());
    }

    public void run()
    {
        if(runEnabled)
        {
            if(this.outputFile.exists())
            {
                int result = JOptionPane.showConfirmDialog(AtomicJ.currentFrame,"The file " + outputFile.getName() + " exists, overwrite?","ChloroplastJ",JOptionPane.YES_NO_CANCEL_OPTION);
                switch(result)
                {
                case JOptionPane.YES_OPTION: break;
                case JOptionPane.NO_OPTION: return;
                case JOptionPane.CANCEL_OPTION: return;
                }
            }

            boolean nonZeroIntensityScheduled = isNonZeroActinicBeamIntensityScheduled();
            if(nonZeroIntensityScheduled)
            {
                int result = JOptionPane.showConfirmDialog(AtomicJ.currentFrame,"Check whether the filter " + getFirstFilterForNonZeroIntensityPhase() + " is selected","ChloroplastJ",JOptionPane.OK_CANCEL_OPTION);
                switch(result)
                {
                case JOptionPane.YES_OPTION: break;
                case JOptionPane.CANCEL_OPTION:  return;
                }
            }
            else
            {
                int result = JOptionPane.showConfirmDialog(AtomicJ.currentFrame,"All scheduled actinic beam phases are of zero intensity. Do you want to proceed?","ChloroplastJ",JOptionPane.OK_CANCEL_OPTION);
                switch(result)
                {
                case JOptionPane.YES_OPTION: break;
                case JOptionPane.CANCEL_OPTION:  return;
                }
            }



            //the status is not yet "Running" when the "Run" button is clicked, as the SwingWorker may take time to execute
            //we set it to Running only when the SwingWorkers starts the first phase, when it calls the setActinicBeamPhase() method

            attemptToSendMeasuringBeamIntensityToController(measuringBeamIntensityInPercent);
            sendMeasuringBeamFrequencyToControllers(measuringBeamFrequencyInHertz);

            prepareChannelDataCollectionForNewRecording();

            buildAndExecuteSignalSamplingWorkers();
            buildAndExecuteActinicBeamPhaseScheduleWorker();

            this.flipperModel.notifyAboutRunStarted();
        }
    }

    private void buildAndExecuteActinicBeamPhaseScheduleWorker()
    {
        this.lastExecutedActinicBeamPhaseScheduleWorker = new ActinicPhaseScheduleSwingWorker(this, 0, false, Collections.unmodifiableList(actinicBeamPhases),1);
        lastExecutedActinicBeamPhaseScheduleWorker.execute();
    }

    private void terminateActinicBeamPhaseScheduleWorker()
    {
        if(lastExecutedActinicBeamPhaseScheduleWorker != null)
        {
            lastExecutedActinicBeamPhaseScheduleWorker.terminateAllTasks();
        }

        notifyAboutChangeOfExpectedPhaseEnd();
    }



    private void prepareChannelDataCollectionForNewRecording()
    {
        if(overlayPlotOnPrevious)
        {

        }
        else
        {
            clearChannelsAndNotifyListeners();
        }
    }

    private void buildAndExecuteSignalSamplingWorkers()
    {
        for(SignalSourceModel signalModel : signalSourceModels)
        {
            signalModel.tryToBuildAndExecuteSignalSamplingWorker();
        }
    }

    public boolean isRunEnabled()
    {
        return runEnabled;
    }

    private void setRunEnabled(boolean enabledNew)
    {
        if(this.runEnabled != enabledNew)
        {
            this.runEnabled = enabledNew;
            for(RecordingModelListener listener: modelListeners)
            {
                listener.runEnabledChange(enabledNew);
            }
        }
    }

    public void stop()
    {
        if(stopEnabled)
        {
            setRecordingStatus(RecordingStatus.STOPPED);
            terminateActinicBeamPhaseScheduleWorker();
            this.flipperModel.notifyAboutRunStopped();
        }
    }

    public boolean isStopEnabled()
    {
        return stopEnabled;
    }

    private void setStopEnabled(boolean enabledNew)
    {
        this.stopEnabled = enabledNew;
        for(RecordingModelListener listener: modelListeners)
        {
            listener.stopEnabledChange(enabledNew);
        }
    }

    public void resume()
    {
        if(resumeEnabled)
        {
            resumeCodeCall();
        }
    }

    private void resumeCodeCall()
    {
        buildAndExecuteSignalSamplingWorkers();
        buildAndExecuteActinicBeamPhaseScheduleWorkerAfterSettingsUpdateDuringRecording();

        this.flipperModel.notifyAboutRunResumed();

        setRecordingStatus(RecordingStatus.RUNNING);
    }

    private void buildAndExecuteActinicBeamPhaseScheduleWorkerAfterSettingsUpdateDuringRecording()
    {
        int indexOfPhaseToResume = this.actinicBeamPhase.getCurrentPhaseIndex();

        if(phaseRemainderToResumeAfterStop == null || phaseRemainderToResumeAfterStop.isInstantenous())
        {
            this.lastExecutedActinicBeamPhaseScheduleWorker = new ActinicPhaseScheduleSwingWorker(this, indexOfPhaseToResume, true, 
                    Collections.unmodifiableList(actinicBeamPhases.subList(indexOfPhaseToResume, getActinicBeamPhaseCount())),1);         
        }
        else
        {
            //it is necessary to copy the list (even if it is sublisted) using the constructor new ArrayList, otherwise the original list will be modified
            List<ActinicPhase> phasesToResume = new ArrayList<>(actinicBeamPhases.subList(indexOfPhaseToResume + 1, getActinicBeamPhaseCount()));
            phasesToResume.add(0, phaseRemainderToResumeAfterStop);

            this.lastExecutedActinicBeamPhaseScheduleWorker = new ActinicPhaseScheduleSwingWorker(this, indexOfPhaseToResume, true, phasesToResume, 1);         
        }            

        lastExecutedActinicBeamPhaseScheduleWorker.execute();
        notifyAboutChangeOfExpectedPhaseEnd();
    }

    public boolean isResumeEnabled()
    {
        return resumeEnabled;
    }

    private void setResumeEnabled(boolean enabledNew)
    {
        this.resumeEnabled = enabledNew;
        for(RecordingModelListener listener: modelListeners)
        {
            listener.resumeEnabledChange(enabledNew);
        }
    }

    public void cancel()
    {
        if(cancelEnabled)
        {
            if(this.lastExecutedActinicBeamPhaseScheduleWorker != null && !this.lastExecutedActinicBeamPhaseScheduleWorker.isDone())
            {
                setRecordingStatus(RecordingStatus.CANCELLING_IN_PROGRESS);         
                terminateActinicBeamPhaseScheduleWorker();
            }
            else
            {
                setRecordingStatus(RecordingStatus.IDLE); 
                setActinicBeamPhase(PhaseStamp.IDLE_INSTANCE, false);
            }

            for(int signalIndex = 0; signalIndex < signalSourceModels.size();signalIndex++)
            {
                signalSourceModels.get(signalIndex).terminateCalibrationWorker();
            }           

            this.flipperModel.notifyAboutRunCanceled();
        }
    }

    public boolean isCancelEnabled()
    {
        return cancelEnabled;
    }

    private void setCancelEnabled(boolean enabledNew)
    {
        this.cancelEnabled = enabledNew;
        for(RecordingModelListener listener: modelListeners)
        {
            listener.cancelEnabledChange(enabledNew);
        }
    }

    private void signalSamplesReceived(int signalIndex, List<CalibratedSignalSample> signalSamples)
    {     
        if(signalSamples.isEmpty())
        {
            return;
        }

        int n = signalSamples.size();        

        if(RecordingStatus.RUNNING.equals(recordingStatus) && latestRecordingOnsetAbsoluteTime > 0)
        { 
            long timeElapsedSinceRecordingOnset = signalSamples.get(n - 1).getAbsoluteTimeInMillis() - latestRecordingOnsetAbsoluteTime;
            sampleReceivers.forEach(receiver -> receiver.elapsedRecordingTime(timeElapsedSinceRecordingOnset));

            updateSignalChannelAndNotifyListeners(signalIndex, Collections.unmodifiableList(signalSamples));          
        }

        CalibratedSignalSample calibratedSample = signalSamples.get(n - 1);

        double lastSignalValue = calibratedSample.getSignalValueInPercents();
        sampleReceivers.forEach(receiver -> receiver.signalSampleReceived(signalIndex, lastSignalValue));
    }

    public boolean isCalibrateEnabled(int signalIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSourceModels.size() - 1, "signalIndex");
        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);

        return signalSourceModel.isCalibrateEnabled();
    }

    private void setCalibrateEnabled(int signalIndex, boolean enabledNew)
    {
        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);
        signalSourceModel.setCalibrateEnabled(enabledNew);       
    }

    public void calibrate(int signalIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSourceModels.size() - 1, "signalIndex");

        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);
        
        if(signalSourceModel.isCalibrateEnabled())
        {
            RecordingStatus statusOld = this.recordingStatus;
            signalSourceModel.calibrate(statusOld);

            //setRecordingStatus(RecordingStatus.UNDER_CALIBRATION) must be execute after signalSourceModel.calibrate(statusOld)
            //because it sets calibrateEnabled property of the SignalSourceModel to false
            setRecordingStatus(RecordingStatus.UNDER_CALIBRATION);
        }
    } 

    public Date getLatestCalibrationDate(int signalIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSourceModels.size() - 1, "signalIndex");
        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);

        return signalSourceModel.getLatestCalibrationDate();
    }

    public double getCalibrationOffsetInVolts(int signalIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSourceModels.size() - 1, "signalIndex");
        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);

        return signalSourceModel.getCalibrationOffsetInVolts();
    }

    protected void setCalibrationOffsetInVolts(int signalIndex, double calibrationOffsetInVoltsNew)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSourceModels.size() - 1, "signalIndex");
        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);

        signalSourceModel.setCalibrationOffsetInVolts(calibrationOffsetInVoltsNew);
    }

    public double getCalibrationSlopeInPercentsPerVolt(int signalIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSourceModels.size() - 1, "signalIndex");
        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);

        return signalSourceModel.getCalibrationSlopeInPercentsPerVolt();
    }

    protected void setCalibrationSlopeInPercentsPerVolt(int signalIndex, double calibrationSlopeInPercentsPerVoltNew)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSourceModels.size() - 1, "signalIndex");

        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);
        signalSourceModel.setCalibrationSlopeInPercentsPerVolt(calibrationSlopeInPercentsPerVoltNew);
    }

    public boolean isCalibrationWellSpecified(int signalIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSourceModels.size() - 1, "signalIndex");
        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);

        return signalSourceModel.isCalibrationWellSpecified();
    }

    public boolean isCalibrationWellSpecifiedForAllSources()
    {
        boolean wellSpecified = true;

        for(SignalSourceModel signalSourceModel : signalSourceModels)
        {
            wellSpecified = wellSpecified && signalSourceModel.isCalibrationWellSpecified();
            if(!wellSpecified)
            {
                break;
            }
        }

        return wellSpecified;
    }

    public boolean isConnectionWithAllSignalSourceEstablished()
    {
        boolean connectionEstablished = true;

        for(SignalSourceModel signalSourceModel : signalSourceModels)
        {
            connectionEstablished = connectionEstablished && signalSourceModel.isConnectionWithSignalSourceEstablished();
            if(!connectionEstablished)
            {
                break;
            }
        }

        return connectionEstablished;
    }


    public boolean isMeasuringBeamFrequencyModificationEnabled()
    {
        return measuringBeamParametersEnabled;
    }

    private void setMeasuringBeamParametersModificationEnabled(boolean measuringBeamParametersEnabledNew)
    {
        this.measuringBeamParametersEnabled = measuringBeamParametersEnabledNew;
        modelListeners.forEach(listener -> listener.measuringBeamFrequencyModificationEnabledChange(this.measuringBeamParametersEnabled));
        modelListeners.forEach(listener -> listener.supportOfSoftwareControlOfMeasuringBeamIntensityChanged(this.measuringBeamParametersEnabled && this.measuringBeamController.isSoftwareControlOfMeasuringBeamIntensitySupported()));
    }

    public boolean isMeasuringBeamIdleStateModificationEnabled()
    {
        return measuringBeamIdleStateModificationEnabled;
    }

    private void setMeasuringBeamIdleStateModificationEnabled(boolean measuringBeamIdelStateModificationEnabledNew)
    {
        this.measuringBeamIdleStateModificationEnabled = measuringBeamIdelStateModificationEnabledNew;
        modelListeners.forEach(listener -> listener.measuringBeamIdleStateBehaviourModificationEnabledChange(this.measuringBeamIdleStateModificationEnabled));
    }

    public boolean isSignalSourceSelectionEnabled()
    {
        return signalSourceSelectionEnabled;
    }

    private void setSignaleSourceSelectionEnabled(boolean signalSourceSelectionEnabledNew)
    {
        this.signalSourceSelectionEnabled = signalSourceSelectionEnabledNew;
        modelListeners.forEach(listener -> listener.signalSourceSelectionEnabledChange(this.signalSourceSelectionEnabled));
    }

    private void respondToInteractiveActinicBeamSettingsModification()
    {
        if(RecordingStatus.RUNNING.equals(this.recordingStatus))
        {
            setRecordingStatus(RecordingStatus.ACTINIC_BEAM_SETTINGS_UNDER_MODIFICATION_WHEN_RUNNING);   
            terminateActinicBeamPhaseScheduleWorker();
        }
        else if(RecordingStatus.STOPPED.equals(this.recordingStatus))
        {
            setRecordingStatus(RecordingStatus.ACTINIC_BEAM_SETTINGS_UNDER_MODIFICATION_WHEN_STOPPED);         
            terminateActinicBeamPhaseScheduleWorker();
        }
    }

    public File getOutputFile()
    {
        return outputFile;
    }

    public void setOutputFile(File outputFileNew)
    {
        if(!Objects.equals(outputFile, outputFileNew))
        {
            File outputFileOld = this.outputFile;
            this.outputFile = outputFileNew;

            sampleReceivers.forEach(receiver -> receiver.sampleOutputFileChanged(outputFileOld, outputFileNew));
            setEnablednessConsistentWithModelState();
        }
    }

    public boolean isOutputFileSelectionEnabled()
    {
        return outputFileSelectionEnabled;
    }

    private void setOutputFileSelectionEnabled(boolean outputFileSelectionEnabledNew)
    {
        if(this.outputFileSelectionEnabled != outputFileSelectionEnabledNew)
        {
            this.outputFileSelectionEnabled = outputFileSelectionEnabledNew;
            for(RecordedDataReceiver listener : sampleReceivers)
            {
                listener.outputFileSelectionEnabledChange(this.outputFileSelectionEnabled);
            }
        }               
    }

    public SaveFormatType<PhotometricResource> getDataSaveFormatType()
    {
        return formatType;
    }

    public void setDataSaveFormatType(SaveFormatType<PhotometricResource> formatTypeNew)
    {
        if(!Objects.equals(formatType, formatTypeNew))
        {
            SaveFormatType<PhotometricResource> formatTypeOld = this.formatType;
            this.formatType = formatTypeNew;

            sampleReceivers.forEach(receiver ->receiver.sampleDataFormatTypeChanged(formatTypeOld, formatTypeNew));
        }
    }

    public boolean isOverlayPlotOnPrevious()
    {
        return overlayPlotOnPrevious;
    }

    public void setOverlayPlotOnPrevious(boolean overlayPlotOnPreviousNew)
    {
        if(this.overlayPlotOnPrevious != overlayPlotOnPreviousNew)
        {
            boolean overlayPlotOnPreviousOld = this.overlayPlotOnPrevious;
            this.overlayPlotOnPrevious = overlayPlotOnPreviousNew;

            PREF.putBoolean(OVERLAY_NEW_CHART_ON_PREVIOUS, this.overlayPlotOnPrevious);
            flushPreferences();

            modelListeners.forEach(listener ->listener.overlayPlotOnPreviousChanged(overlayPlotOnPreviousOld, overlayPlotOnPreviousNew));
        }
    }

    public void addPhotometricRecordingModelListener(RecordingModelListener listener)
    {
        modelListeners.add(listener);
    }

    public void removePhotometricRecordingModelListener(RecordingModelListener listener)
    {
        modelListeners.remove(listener);
    }

    public void addSampleReceiver(RecordedDataReceiver listener)
    {
        sampleReceivers.add(listener);
    }

    public void removeSampleReceiver(RecordedDataReceiver listener)
    {
        sampleReceivers.remove(listener);
    }

    public void selectTransmittanceSourceController(int signalIndex, SignalSourceController transmittanceSourceControllerNew)
    {     
        Validation.requireNonNullParameterName(transmittanceSourceControllerNew, "transmittanceSourceControllerNew");

        if(!this.availableFunctionalSignalSourceControllers.containsValue(transmittanceSourceControllerNew))
        {
            throw new IllegalArgumentException(transmittanceSourceControllerNew.toString() + " is not available");
        }

        setSignalSourceController(signalIndex,transmittanceSourceControllerNew);
    }

    private void setSignalSourceController(int signalIndex, SignalSourceController signalSourceControllerNew)
    {
        SignalSourceModel signalModel = signalSourceModels.get(signalIndex);
        signalModel.selectSignaleSourceController(signalSourceControllerNew);
    }

    private void respondToSignalSourceControllerChange(int signalIndex, SignalSourceController signalSourceControllerOld, SignalSourceController signalSourceControllerNew)
    {
        double newFrequencyInHertz = findClosestSupportedBeamFrequency(this.measuringBeamFrequencyInHertz);
        setMeasuringBeamFrequencyInHertz(newFrequencyInHertz);

        updateMaximalAllowedMeasuringBeamFrequencyInHertz();

        double preferredIncrementOld = this.preferredMeasuringFrequencyIncrement;
        double preferredDecrementOld = this.preferredMeasuringFrequencyDecrement;

        this.preferredMeasuringFrequencyIncrement = getPreferredFrequencyDecrement(measuringBeamFrequencyInHertz);
        this.preferredMeasuringFrequencyDecrement = getPreferredFrequencyDecrement(measuringBeamFrequencyInHertz);

        if(Double.compare(this.preferredMeasuringFrequencyIncrement, preferredIncrementOld) != 0 || Double.compare(this.preferredMeasuringFrequencyDecrement, preferredDecrementOld) != 0)
        {
            modelListeners.forEach(listener -> listener.preferredFrequencyIncrementAndDecrementChanged(preferredIncrementOld, this.preferredMeasuringFrequencyIncrement, preferredDecrementOld, this.preferredMeasuringFrequencyDecrement));
        }

        modelListeners.forEach(listener -> listener.signalSourceControllerChanged(signalIndex, signalSourceControllerOld, signalSourceControllerNew));
    }

    public SignalSourceController getSelectedSignalSourceController(int signalIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSourceModels.size() - 1, "signalIndex");
        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);

        return signalSourceModel.getSelectedSignalSourceController();
    }

    public List<SignalSourceController> getAvailableSignalSourceControllers()
    {
        return new ArrayList<>(availableFunctionalSignalSourceControllers.values());
    }

    public double getMinimalSignalSamplesPerMinute(int signalIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSourceModels.size() - 1, "signalIndex");
        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);

        return signalSourceModel.getMinimalSignalSamplesPerMinute();
    }

    public double getMaximalSignalSamplesPerMinute(int signalIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSourceModels.size() - 1, "signalIndex");
        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);

        return signalSourceModel.getMaximalSignalSamplesPerMinute();
    }

    public double getSignalSamplesPerMinute(int signalIndex)
    {    
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSourceModels.size() - 1, "signalIndex");
        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);

        return signalSourceModel.getSignalSamplesPerMinute();
    }

    public void selectSignalSamplesPerMinute(int signalIndex, double signalSamplesPerMinuteNew)
    {        
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSourceModels.size() - 1, "signalIndex");
        setSignalSamplesPerMinute(signalIndex, signalSamplesPerMinuteNew);
    }

    private void setSignalSamplesPerMinute(int signalIndex, double signalSamplesPerMinuteNew)
    {
        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);
        signalSourceModel.selectSignalSamplesPerMinute(signalSamplesPerMinuteNew);
    }

    public LightSignalType getSignalType(int signalIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSourceModels.size() - 1, "signalIndex");
        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);

        return signalSourceModel.getLightSignalType();
    }

    public void setSignalType(int signalIndex, LightSignalType signalTypeNew)
    {
        Validation.requireNonNullParameterName(signalTypeNew, "signalTypeNew");
        Validation.requireValueEqualToOrBetweenBounds(signalIndex, 0, signalSourceModels.size() - 1, "signalIndex");

        SignalSourceModel signalSourceModel = signalSourceModels.get(signalIndex);
        signalSourceModel.setLightSignalType(signalTypeNew);
    }

    private void updateSignalChannelAndNotifyListeners(int signalIndex, List<CalibratedSignalSample> samples)
    {
        if(samples.isEmpty())
        {
            return;
        }

        int n = samples.size();
        double[][] newPoints = new double[n][];
        for(int i = 0; i<n;i++)
        {
            CalibratedSignalSample sample = samples.get(i);
            newPoints[i] = new double[] {sample.getTimeInSecondsSinceExperimentOnset(), sample.getSignalValueInPercents()};
        }

        double lastSignalValue = samples.get(n - 1).getSignalValueInPercents();

        String channelIdentifier = channelIdentifiers.get(signalIndex);

        Channel1DDataTransformation tr = new MultiplePointsAddition1DTransformation(newPoints, SortedArrayOrder.ASCENDING);
        recordedChannelCollection.transform(channelIdentifier, tr);    

        for(RecordedDataReceiver receiver : sampleReceivers)
        {
            receiver.recordedChannelChanged(channelIdentifier);
            receiver.signalSampleReceived(signalIndex, lastSignalValue);
        }
    }

    private void clearChannelsAndNotifyListeners()
    {
        Channel1DDataTransformation tr = new ClearPoints1DTransformation(SortedArrayOrder.ASCENDING);
        recordedChannelCollection.transform(tr);    

        for(RecordedDataReceiver receiver : sampleReceivers)
        {
            for(String channelIdentifier : channelIdentifiers)
            {
                receiver.recordedChannelChanged(channelIdentifier);
            }
        }
    }

    private void notifyAboutChangeOfExpectedPhaseEnd()
    {
        for(int i = 0; i < actinicBeamPhases.size();i++)
        {
            Date endTime = getExpectedPhaseEndTime(i);
            for(RecordingModelListener receiver : modelListeners)
            {
                receiver.phaseEndTimeChanged(i, endTime);
            }           
        }
    }

    private MeasuringBeamSettingsImmutable buildMeasuringBeamSettings()
    {
        MeasuringBeamSettingsImmutable measuringBeamSettings = new MeasuringBeamSettingsImmutable(measuringBeamFrequencyInHertz, measuringBeamIntensityInPercent, keepMeasuringBeamOnWhenIdle);
        return measuringBeamSettings;
    }

    private List<ActinicPhaseSettingsImmutable> buildImmutableActinicPhaseSettings()
    {
        List<ActinicPhaseSettingsImmutable> settings = new ArrayList<>();
        for(ActinicPhaseSettings phaseSettings : actinicBeamPhases)
        {
            settings.add(phaseSettings.getImmutableCopy());
        }

        return Collections.unmodifiableList(settings);
    }

    private StandardPhotometricSource buildSource()
    {      
        List<SignalSettingsImmutable> allSignalSettings = new ArrayList<>();

        for(SignalSourceModel signalModel : signalSourceModels)
        {
            SignalSettingsImmutable signalSettings = signalModel.getSignalSettings();
            allSignalSettings.add(signalSettings);
        }

        MeasuringBeamSettingsImmutable measuringBeamSettings = buildMeasuringBeamSettings();
        List<ActinicPhaseSettingsImmutable> phaseSettingsImmutable = buildImmutableActinicPhaseSettings();
        PhotometricDescriptionImmutable description = experimentDescriptionModel.getMemento();
        String recordingName = "ActiveCurve";
        StandardPhotometricSource source = new StandardPhotometricSource(outputFile, recordingName, recordingName, recordedChannelCollection.getChannels(), phaseSettingsImmutable, measuringBeamSettings, allSignalSettings, description);

        return source;
    }

    private PhotometricResource buildDataResource()
    {
        StandardPhotometricSource source = buildSource();
        PhotometricResource dataResource = new PhotometricResource(source);

        return dataResource;
    }

    private void finishFileSaving()
    {
        PhotometricResource dataResource = buildDataResource();
        Saver<PhotometricResource> saver = formatType.getSaver();
        try {
            saver.save(dataResource, outputFile);
        } catch (SavingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private class SerialPortPingResponseListener implements SerialPortPacketListener 
    {
        @Override
        public void serialEvent(SerialPortEvent evt) 
        {            
            byte[] received = evt.getReceivedData();
            SerialPort port = evt.getSerialPort();
            boolean accepted  = false;
            for(SerialPortVisitor portVisitor : SerialPortVisitor.values())
            {
                if(portVisitor.acceptsAsMagicBytes(received))
                {
                    portVisitor.registerThePortWithModel(received, port, RecordingModel.this);
                    RecordingModel.this.portsVisited.put(portVisitor, Boolean.TRUE);
                    accepted = true;
                    break;
                }               
            }  

            if(!accepted && port.isOpen())
            {
                port.closePort();
            }
            closeUnnecessaryPorts();
        }

        @Override
        public int getListeningEvents() 
        {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public int getPacketSize() 
        {
            return 4;
        }
    }

    private void initializeSerialPorts()
    {
        SerialPort[] serialPorts = SerialPort.getCommPorts();

        for(SerialPort port: serialPorts)
        {
            port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
            port.setBaudRate(38400); 

            port.addDataListener(new SerialPortPingResponseListener());
            port.openPort();

            port.writeBytes(SerialPortVisitor.PING_BYTES, SerialPortVisitor.PING_BYTES.length);
        } 
    }

    private void closeUnnecessaryPorts()
    {
        boolean allVisitorsContacted = true;
        for(SerialPortVisitor visit: SerialPortVisitor.values())
        {
            Boolean visited = portsVisited.get(visit);
            if(!Objects.equals(Boolean.TRUE, visited))
            {
                allVisitorsContacted = false;
                break;
            }
        }

        if(allVisitorsContacted)
        {
            SerialPort[] serialPorts = SerialPort.getCommPorts();

            for(SerialPort port : serialPorts)
            {                
                port.closePort();
            }
        }       
    }


    private void closeAllSerialPorts()
    {
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        for(SerialPort port : serialPorts)
        {
            if(port.isOpen())
            {
                port.closePort();
            }
        }
    }

    private class RedPitayaLockInDeviceListener implements RedPitayaDeviceListener
    {
        @Override
        public void redPitayaLockInFound(RedPitayaLockInDevice device) 
        {            
            MeasuringBeamController mbController = new RedPitayaMeasuringBeamController(device);
            SignalSourceController transmittanceSourceController = new RedPitayaSignalSourceController(device);

            registerNewFunctionalSignalSourceController(transmittanceSourceController);
            registerNewFunctionalMeasuringBeamController(mbController);
        }

        @Override
        public void redPitayaLockInRemoved(RedPitayaLockInDevice device) 
        { 
            String deviceId = device.getIdentifier();
            for(SignalSourceController controller : availableFunctionalSignalSourceControllers.values())
            {
                if(controller.requiresDevice(deviceId))
                {
                    removeFunctionalSignalSourceController(controller);
                }
            }

            for(MeasuringBeamController controller : availableFunctionalMeasuringBeamControllers.values())
            {
                if(controller.requiresDevice(deviceId))
                {
                    removeFunctionalMeasuringBeamController(controller);
                }
            }

        }
    }


    //OPTICS CONFIGURATION

    public List<SliderMountedFilter> getActinicBeamSliderMountedFilters()
    {
        return opticsModel.getAvailableActinicBeamSliderMountedFilters();
    }

    private class StandardSignalModelListener implements SignalModelListener
    {
        private final int signalIndex;

        public StandardSignalModelListener(int signalIndex)
        {
            this.signalIndex = signalIndex;
        }

        @Override
        public void calibrationInitialized()
        {
            setRecordingStatus(RecordingStatus.UNDER_CALIBRATION);
        }

        @Override
        public void calibrationDoneAfterCancellation(RecordingStatus previousRecordingStatus)
        {
            setRecordingStatus(previousRecordingStatus);
        }

        @Override
        public void calibrationDoneWithException(RecordingStatus previousRecordingStatus)
        {
            setRecordingStatus(previousRecordingStatus);
        }

        @Override
        public void calibrationFinishedSuccessfully(RecordingStatus previousRecordingStatus) 
        {      
            setRecordingStatus(previousRecordingStatus);
        }

        @Override
        public void latestCalibrationDateChanged(Date latestCalibrationDateOld, Date latestCalibrationDateNew) 
        {
            modelListeners.forEach(listener -> listener.latestCalibrationDateChanged(signalIndex, latestCalibrationDateOld, latestCalibrationDateNew));
        }

        @Override
        public void calibrationOffsetInVoltsChanged(double calibrationOffsetInVoltsOld, double calibrationOffsetInVoltsNew)
        {
            modelListeners.forEach(listener ->listener.calibrationOffsetInVoltsChanged(signalIndex, calibrationOffsetInVoltsOld, calibrationOffsetInVoltsNew));
        }

        @Override
        public void calibrationSlopeInPercentsPerVoltChanged(double calibrationSlopeInPercentsPerVoltOld, double calibrationSlopeInPercentsPerVoltNew) 
        {
            modelListeners.forEach(listener ->listener.calibrationSlopeInPercentsPerVoltChanged(signalIndex, calibrationSlopeInPercentsPerVoltOld, calibrationSlopeInPercentsPerVoltNew));
        }

        @Override
        public void calibrationPhaseChanged(CalibrationPhase calibrationPhaseOld, CalibrationPhase calibrationPhaseNew) 
        {
            modelListeners.forEach(listener ->listener.calibrationPhaseChanged(signalIndex, calibrationPhaseOld, calibrationPhaseNew));  
        }

        @Override
        public void progressInPercentOfCurrentCalibrationPhaseChanged(int currentProgressInPercent) 
        {
            modelListeners.forEach(listener -> listener.progressInPercentOfCurrentCalibrationPhaseChanged(signalIndex, currentProgressInPercent));
        }

        @Override
        public void signalSourceControllerChanged(SignalSourceController signalSourceControllerOld, SignalSourceController signalSourceControllerNew) 
        {
            respondToSignalSourceControllerChange(signalIndex, signalSourceControllerOld, signalSourceControllerNew);
        }

        @Override
        public void signalSamplesPerMinuteChanged(double signalSamplesPerMinuteOld, double signalPerMinuteNew)
        {
            modelListeners.forEach(listener -> listener.signalSamplesPerMinuteChanged(signalIndex, signalSamplesPerMinuteOld, signalPerMinuteNew));
        }

        @Override
        public void maximalSignalSamplesPerMinuteChanged(double maximalSignalSamplesPerMinuteForSelectedSourceOld, double maximalSignalSamplesPerMinuteForSelectedSourceNew) 
        {
            modelListeners.forEach(listener -> listener.maximalSignalSamplesPerMinuteChanged(signalIndex, maximalSignalSamplesPerMinuteForSelectedSourceOld, maximalSignalSamplesPerMinuteForSelectedSourceNew));
        }

        @Override
        public void calibrationEnabledChange(boolean enabledNew)
        {
            for(RecordingModelListener listener: modelListeners)
            {
                listener.calibrationEnabledChange(signalIndex, enabledNew);
            }
        }

        @Override
        public void signalSamplesReceived(List<CalibratedSignalSample> signaleSamples) 
        {
            RecordingModel.this.signalSamplesReceived(signalIndex, signaleSamples);
        }

        @Override
        public void signalTypeChanged(LightSignalType signalTypeOld,LightSignalType signalTypeNew) 
        {
            for(RecordingModelListener listener : modelListeners)
            {
                listener.signalTypeChanged(signalIndex, signalTypeOld, signalTypeNew);
            }

            SignalSourceModel signalModel = signalSourceModels.get(signalIndex);
            String channelKeyNew = signalModel.getRecordedChannelKey();

            String channelKeyOld = channelIdentifiers.get(signalIndex);
            channelIdentifiers.set(signalIndex, channelKeyNew);

            recordedChannelCollection.removeChannel(channelKeyOld);

            Channel1DData signalChannelData = new FlexibleChannel1DData(new double[][] {}, signalModel.getXQuantity(), signalModel.getYQuantity(), SortedArrayOrder.ASCENDING);
            recordedChannelCollection.addChannel(signalChannelData, channelKeyNew);

            for(RecordedDataReceiver receiver : sampleReceivers)
            {
                receiver.signalTypeChanged(signalIndex, signalTypeOld, signalTypeNew);
                receiver.recordedChannelAdded(recordedChannelCollection.getChannel(channelKeyNew));
                receiver.recordedChannelRemoved(channelKeyOld);
            }            
        }       
    }
}
