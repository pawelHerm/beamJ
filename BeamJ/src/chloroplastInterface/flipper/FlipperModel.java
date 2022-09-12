package chloroplastInterface.flipper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import atomicJ.gui.AbstractModel;
import atomicJ.utilities.SerializationUtilities;
import atomicJ.utilities.Validation;
import chloroplastInterface.DummySignalReceiverController;
import chloroplastInterface.NIDevice;
import chloroplastInterface.NIDeviceSignalReceiverController;
import chloroplastInterface.PhaseRealTiming;
import chloroplastInterface.PhaseStamp;
import chloroplastInterface.RecordingModel;
import chloroplastInterface.SignalReceiver;
import chloroplastInterface.SignalReceiverController;
import chloroplastInterface.StandardTimeUnit;
import chloroplastInterface.TestingSignalReceiverController;

public class FlipperModel extends AbstractModel
{
    private final static Preferences PREF = Preferences.userNodeForPackage(FlipperModel.class).node(FlipperModel.class.getName());

    private final static String TRANSMITTANCE_RECEIVER_CONTROLLER_ID = "TransmittanceReceiverControllerID";

    public final static String SERIAL_NUMBER = "SerialNumber";
    public final static String TRANSIT_TIME = "TransitTime";
    public final static String FLIP_INTERVAL_VALUE = "FlipIntervalValue";
    public final static String FLIP_INTERVAL_UNIT = "FlipIntervalUnit";
    public final static String CURRENT_POSITION = "CurrentPosition";
    public final static String START_FLIPPING_WHEN_NEW_MEASUREMENT_BEGINS = "StartFlippingWhenNewMeasurementBegins";

    public final static String VOLTAGE_SIGNALS = "VoltageSignals";


    public final static String NECESSARY_INPUT_PROVIDED = "NecessaryInputProvided";

    private final double initFlipIntervalValue= 1;
    private final StandardTimeUnit initFlipIntervalUnit = StandardTimeUnit.MINUTE;

    private String serialNumber;
    private FlipperPosition currentPosition;
    private double flipIntervalValue;
    private StandardTimeUnit flipIntervalUnit;
    private boolean startFlippingWhenNewMeasurementBegins;
    private int transitTimeInMiliseconds;

    private Map<FlipperPosition,VoltageFlipSignalSettings> flipVoltageSignals = new HashMap<>();
    private final Set<VoltageFlipSignalListener> voltageSignalListeners = new LinkedHashSet<>();
    private FlipAssociatedSignalSimpleManager associatedSignalManager = FlipAssociatedSignalSimpleManager.getNullInstance();

    private final static double MIN_POSSIBLE_VOLTAGE_SIGNAL_VALUE = -10.0;
    private final static double MAX_POSSIBLE_VOLTAGE_SIGNAL_VALUE = 10.0;

    private final boolean initSendVoltageSignalAfterFlip = true;
    private final double initFlipVoltageLagTimeValue = 1;
    private final StandardTimeUnit initFlipVoltageLagTimeUnit = StandardTimeUnit.SECOND;
    private final double initFlipVoltageValue = 5;

    private final double initVoltageSignalDurationValue = 1;
    private final StandardTimeUnit initVoltageSignalDurationTimeUnit =  StandardTimeUnit.SECOND;

    private SignalReceiverController signalReceiverController = RecordingModel.TEST ? TestingSignalReceiverController.getInstance() : DummySignalReceiverController.getInstance();
    private final Map<String, SignalReceiverController> availableFunctionalSignalReceiverControllers = new LinkedHashMap<>();

    private boolean necessaryInputProvided;

    private PhaseStamp flipperPhase = PhaseStamp.IDLE_INSTANCE;
    private FlipperPhaseRemainder phaseRemainderToResumeAfterStop = null;
    private long latestRecordingOnsetAbsoluteTime = -1;

    private Flipper currentFlipper;
    private FlipperFixedRateFlippingSwingWorker lastExecutedFlipperWorker;

    public FlipperModel()
    {
        initialize();
    }

    private void initialize()
    {       
        this.serialNumber = PREF.get(SERIAL_NUMBER, "");
        this.currentFlipper = ThorlabsFlipperFactory.isCompleteValidThorlabsFlipperSerialNumberAtLeastWhenTrimmed(serialNumber) ? ThorlabsFlipperFactory.getInstance().getFlipperIfPossible(serialNumber):DummyFlipper.getInstance();

        this.flipIntervalValue = PREF.getDouble(FLIP_INTERVAL_VALUE, this.initFlipIntervalValue);
        this.flipIntervalUnit = StandardTimeUnit.getUnit(PREF.get(FLIP_INTERVAL_UNIT, this.initFlipIntervalUnit.name()));
        this.startFlippingWhenNewMeasurementBegins = PREF.getBoolean(START_FLIPPING_WHEN_NEW_MEASUREMENT_BEGINS, true);

        this.flipVoltageSignals = (Map<FlipperPosition,VoltageFlipSignalSettings>)SerializationUtilities.getSerializableObject(PREF, VOLTAGE_SIGNALS, buildDefaultVoltageSignals(this.flipIntervalValue, this.flipIntervalUnit));

        if(this.currentFlipper.isActive())
        {          
            Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {

                @Override
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() 
                        {
                            ensureConsistencyOfSettingsWithActiveFlipper();
                        }
                    });
                }
            }, 2, TimeUnit.SECONDS);

        }

        //this code has to be executed even if the current flipper is active
        //this is so because ensureConsistencyOfSettingsWithActiveFlipper() will be executed at some point in the future
        //and we have to make sure that in the meantime the FlipperModel's fields are within permitted bounds
        int preferredTransitTimeInMiliseconds = PREF.getInt(TRANSIT_TIME, ThorlabsFlipper.DEFAULT_TRANSIT_TIME_IN_MILISECONDS);
        int validPreferredTransitTimeInMiliseconds = ThorlabsFlipper.ensureThatTransitTimeInMilisecondsIsValid(preferredTransitTimeInMiliseconds);
        this.currentFlipper.setTransitTimeInMiliseconds(validPreferredTransitTimeInMiliseconds);

        //we query the flipper just in case setting the transit time failed
        this.transitTimeInMiliseconds = this.currentFlipper.getTransitTimeInMiliseconds();

        this.currentPosition = FlipperPosition.getThorlabsFlipperPosition(PREF.getInt(CURRENT_POSITION, FlipperPosition.FIRST.getCode()));      


        if(signalReceiverController.isFunctional())
        {
            this.availableFunctionalSignalReceiverControllers.put(signalReceiverController.getUniqueDescription(), signalReceiverController);
        }

        List<NIDeviceSignalReceiverController> niDevices = NIDeviceSignalReceiverController.buildFactoriesWhenPossible(NIDevice.getAvailableNIDevices());
        for(NIDeviceSignalReceiverController niDevice : niDevices)
        {
            this.availableFunctionalSignalReceiverControllers.put(niDevice.toString(), niDevice);
        }

        String selectedTransmittanceSourceControllerKey = PREF.get(TRANSMITTANCE_RECEIVER_CONTROLLER_ID, "");

        this.signalReceiverController = !availableFunctionalSignalReceiverControllers.isEmpty() ? (availableFunctionalSignalReceiverControllers.containsKey(selectedTransmittanceSourceControllerKey) ? availableFunctionalSignalReceiverControllers.get(selectedTransmittanceSourceControllerKey) :  availableFunctionalSignalReceiverControllers.values().iterator().next()): this.signalReceiverController;  
        refreshVoltageSignalManager();
    }

    private void registerNewFunctionalTransmittanceSourceController(SignalReceiverController controller)
    {
        boolean workingSignalReceiverControllerAvilableOld = isFunctioningSignalReceiverControllerAvailable();

        availableFunctionalSignalReceiverControllers.put(controller.getUniqueDescription(), controller);
        notifyAboutNewFunctioningReceiverControllerAdded(controller);

        if(signalReceiverController.shouldBeReplacedWhenOtherControllerFound())
        {
            setSignalReceiverController(controller);
        }      

        boolean functioningSignalReceiverControllerAvilableNew = isFunctioningSignalReceiverControllerAvailable();
        notifyAboutChangeInAvailabilityOfFunctioningSignalReceiverControllers(workingSignalReceiverControllerAvilableOld, functioningSignalReceiverControllerAvilableNew);
    }

    private void removeFunctionalTransmittanceSourceController(SignalReceiverController controller)
    {
        boolean functionalTransmittanceSourceControllerAvailableOld = isFunctioningSignalReceiverControllerAvailable();

        availableFunctionalSignalReceiverControllers.remove(controller.getUniqueDescription());
        notifyAboutFunctioningReceiverControllerRemoved(controller);

        if(Objects.equals(this.signalReceiverController, controller))
        {
            SignalReceiverController transmittanceSourceControllerNew = availableFunctionalSignalReceiverControllers.isEmpty() ? (RecordingModel.TEST ? TestingSignalReceiverController.getInstance() : DummySignalReceiverController.getInstance()) : availableFunctionalSignalReceiverControllers.values().iterator().next();
            setSignalReceiverController(transmittanceSourceControllerNew);
        }      

        boolean functioningSignalReceiverControllerAvilableNew = isFunctioningSignalReceiverControllerAvailable();
        notifyAboutChangeInAvailabilityOfFunctioningSignalReceiverControllers(functionalTransmittanceSourceControllerAvailableOld, functioningSignalReceiverControllerAvilableNew);
    }

    public boolean isFunctioningSignalReceiverControllerAvailable()
    {
        boolean workingAvailableTransmittanceReceiverAvailable = !availableFunctionalSignalReceiverControllers.isEmpty();
        return workingAvailableTransmittanceReceiverAvailable;
    }

    private void notifyAboutNewFunctioningReceiverControllerAdded(SignalReceiverController controllerNew)
    {
        for(VoltageFlipSignalListener listener : voltageSignalListeners)
        {
            listener.functioningSignalReceiverControllerAdded(controllerNew);
        }
    }

    private void notifyAboutFunctioningReceiverControllerRemoved(SignalReceiverController controller)
    {
        for(VoltageFlipSignalListener listener : voltageSignalListeners)
        {
            listener.functioningSignalReceiverControllerRemoved(controller);
        }
    }

    private void notifyAboutChangeInAvailabilityOfFunctioningSignalReceiverControllers(boolean workingSignalReceiverControllerAvailableOld, boolean workingSignalReceiverControllerAvailableNew)
    {
        for(VoltageFlipSignalListener listener: voltageSignalListeners)
        {
            listener.availabilityOfFunctioningSignalReceiverControllersChange(workingSignalReceiverControllerAvailableOld, workingSignalReceiverControllerAvailableNew);
        }
    }

    public void selectSignalReceiverController(SignalReceiverController signalReceiverControllerNew)
    {     
        Validation.requireNonNullParameterName(signalReceiverControllerNew, "signalReceiverControllerNew");

        if(!this.availableFunctionalSignalReceiverControllers.containsValue(signalReceiverControllerNew))
        {
            throw new IllegalArgumentException(signalReceiverControllerNew.toString() + " is not available");
        }

        setSignalReceiverController(signalReceiverControllerNew);
    }

    private void setSignalReceiverController(SignalReceiverController signalReceiverControllerNew)
    {
        if(!Objects.equals(this.signalReceiverController, signalReceiverControllerNew))
        {
            SignalReceiverController signalReceiverControllerOld = this.signalReceiverController;
            this.signalReceiverController = signalReceiverControllerNew;

            PREF.put(TRANSMITTANCE_RECEIVER_CONTROLLER_ID, this.signalReceiverController.getUniqueDescription());       
            flushPreferences();

            notifyAboutSignalReceiverControllerChange(signalReceiverControllerOld, signalReceiverControllerNew);

            if(isConnectionWithSignalReceiverEstablished())
            {
                //buildAndExecuteTransmittanceSamplingWorker();
            }
            else
            {
                //terminateTransmittanceSamplingWorker();
            }

            refreshVoltageSignalManager();
        }
    }

    private boolean isConnectionWithSignalReceiverEstablished()
    {
        boolean connectionEstablished = this.signalReceiverController.isFunctional();
        return connectionEstablished;
    }

    private void notifyAboutSignalReceiverControllerChange(SignalReceiverController signalReceiverControllerOld, SignalReceiverController signalReceiverControllerNew)
    {
        for(VoltageFlipSignalListener listener : voltageSignalListeners)
        {
            listener.signalReceiverControllerChanged(signalReceiverControllerOld, signalReceiverControllerNew);
        }
    }

    public SignalReceiverController getSelectedSignalReceiverController()
    {
        return signalReceiverController;
    }

    public List<SignalReceiverController> getAvailableSignalReceiverControllers()
    {
        return new ArrayList<>(availableFunctionalSignalReceiverControllers.values());
    }

    public void addVoltageSignalListener(VoltageFlipSignalListener listener)
    {
        this.voltageSignalListeners.add(listener);
    }

    public void removeVoltageSignalListener(VoltageFlipSignalListener listener)
    {
        this.voltageSignalListeners.remove(listener);
    }

    public List<FlipperPosition> getFlipperPositionsThatCanBeAssociatedWithVoltageSignals()
    {
        List<FlipperPosition> positions = new ArrayList<>(flipVoltageSignals.keySet());

        return positions;
    }

    public boolean isSendVoltageSignalAfterFlip(FlipperPosition flipperPosition)
    {
        if(!flipVoltageSignals.containsKey(flipperPosition))
        {
            throw new IllegalArgumentException("No specifications for the " + flipperPosition +" flipper position known");
        }

        VoltageFlipSignalSettings signal = flipVoltageSignals.get(flipperPosition);

        boolean sendSignals = signal.isSendSignalAfterFlip();
        return sendSignals;
    }

    public void setSendVoltageSignalAfterFlip(FlipperPosition flipperPosition, boolean sendVoltageSignalAfterFlipNew)
    {
        Validation.requireNonNullParameterName(flipperPosition, "flipperPosition");
        if(!flipVoltageSignals.containsKey(flipperPosition))
        {
            throw new IllegalArgumentException("No specifications for the " + flipperPosition +" flipper position known");
        }

        VoltageFlipSignalSettings signal = flipVoltageSignals.get(flipperPosition);
        boolean sendVoltageSignalAfterFlipOld = signal.isSendSignalAfterFlip();

        if(sendVoltageSignalAfterFlipOld != sendVoltageSignalAfterFlipNew)
        {
            signal.setSendSignalAfterFlip(sendVoltageSignalAfterFlipNew);
            saveVoltageSettingsToPreferences();
            notifyAboutSendVoltageSignalAfterFlipChanged(flipperPosition, sendVoltageSignalAfterFlipOld, sendVoltageSignalAfterFlipNew);
        }
    }

    private void notifyAboutSendVoltageSignalAfterFlipChanged(FlipperPosition flipperPosition, boolean sendVoltageSignalAfterFlipOld, boolean sendVoltageSignalAfterFlipNew)
    {
        for(VoltageFlipSignalListener listener : voltageSignalListeners)
        {
            listener.sendVoltageSignalAfterFlipChanged(flipperPosition, sendVoltageSignalAfterFlipOld, sendVoltageSignalAfterFlipNew);
        }
    }

    public double getVoltageSignalDurationValue(FlipperPosition flipperPosition)
    {        
        Validation.requireNonNullParameterName(flipperPosition, "flipperPosition");

        if(!flipVoltageSignals.containsKey(flipperPosition))
        {
            throw new IllegalArgumentException("No specifications for the " + flipperPosition +" flipper position known");
        }

        VoltageFlipSignalSettings signal = flipVoltageSignals.get(flipperPosition);

        double duration = signal.getVoltageSignalDurationValue();
        return duration;
    }

    public void setVoltageSignalDurationValue(FlipperPosition flipperPosition, double flipVoltageSignalDurationValueNew)
    {
        Validation.requireNonNullParameterName(flipperPosition, "flipperPosition");
        Validation.requireNonNegativeParameterName(flipVoltageSignalDurationValueNew, "flipVoltageSignalDurationValueNew");

        if(!flipVoltageSignals.containsKey(flipperPosition))
        {
            throw new IllegalArgumentException("No specifications for the " + flipperPosition +" flipper position known");
        }

        VoltageFlipSignalSettings signal = flipVoltageSignals.get(flipperPosition);
        double flipVoltageSignalDurationValueOld = signal.getVoltageSignalDurationValue();

        if(Double.compare(flipVoltageSignalDurationValueOld, flipVoltageSignalDurationValueNew) != 0)
        {
            signal.setVoltageSignalDurationValue(flipVoltageSignalDurationValueNew);
            saveVoltageSettingsToPreferences();
            notifyAboutVoltageSignalDurationValueChanged(flipperPosition, flipVoltageSignalDurationValueOld, flipVoltageSignalDurationValueNew);
        }
    }

    private void notifyAboutVoltageSignalDurationValueChanged(FlipperPosition flipperPosition, double flipVoltageSignalDurationValueOld, double flipVoltageSignalDurationValueNew)
    {
        for(VoltageFlipSignalListener listener : voltageSignalListeners)
        {
            listener.voltageSignalDurationValueChanged(flipperPosition, flipVoltageSignalDurationValueOld, flipVoltageSignalDurationValueNew);
        }
    }

    public StandardTimeUnit getVoltageSignalDurationUnit(FlipperPosition flipperPosition)
    {
        Validation.requireNonNullParameterName(flipperPosition, "flipperPosition");
        if(!flipVoltageSignals.containsKey(flipperPosition))
        {
            throw new IllegalArgumentException("No specifications for the " + flipperPosition +" flipper position known");
        }
        VoltageFlipSignalSettings signal = flipVoltageSignals.get(flipperPosition);
        StandardTimeUnit durationUnit = signal.getSignalDurationUnit();

        return durationUnit;
    }

    public void setVoltageSignalDurationUnit(FlipperPosition flipperPosition, StandardTimeUnit durationUnitNew)
    {
        Validation.requireNonNullParameterName(flipperPosition, "flipperPosition");
        Validation.requireNonNullParameterName(durationUnitNew, "lagTimeUnitNew");

        VoltageFlipSignalSettings signal = flipVoltageSignals.get(flipperPosition);
        StandardTimeUnit durationUnitOld = signal.getLagTimeUnit();

        if(!Objects.equals(durationUnitOld, durationUnitNew))
        {
            signal.setSignalDurationUnit(durationUnitNew);
            saveVoltageSettingsToPreferences();
            notifyAboutVoltageSignalDurationUnitChanged(flipperPosition, durationUnitOld, durationUnitNew);
        }
    }

    private void notifyAboutVoltageSignalDurationUnitChanged(FlipperPosition flipperPosition, StandardTimeUnit durationUnitOld, StandardTimeUnit durationUnitNew)
    {
        for(VoltageFlipSignalListener listener : voltageSignalListeners)
        {
            listener.voltageSignalDurationTimeUnitChanged(flipperPosition, durationUnitOld, durationUnitNew);
        }
    }  


    public double getVoltageSignalLagValue(FlipperPosition flipperPosition)
    {        
        Validation.requireNonNullParameterName(flipperPosition, "flipperPosition");

        if(!flipVoltageSignals.containsKey(flipperPosition))
        {
            throw new IllegalArgumentException("No specifications for the " + flipperPosition +" flipper position known");
        }

        VoltageFlipSignalSettings signal = flipVoltageSignals.get(flipperPosition);

        double duration = signal.getLagValue();
        return duration;
    }

    public void setVoltageSignalLagValue(FlipperPosition flipperPosition, double voltageSignalLagValueNew)
    {
        Validation.requireNonNullParameterName(flipperPosition, "flipperPosition");
        Validation.requireNonNegativeParameterName(voltageSignalLagValueNew, "flipVoltageSignalLagValueNew");

        if(!flipVoltageSignals.containsKey(flipperPosition))
        {
            throw new IllegalArgumentException("No specifications for the " + flipperPosition +" flipper position known");
        }

        VoltageFlipSignalSettings signal = flipVoltageSignals.get(flipperPosition);
        double voltageSignalLagValueOld = signal.getVoltageSignalDurationValue();

        if(Double.compare(voltageSignalLagValueOld, voltageSignalLagValueNew) != 0)
        {
            signal.setLagValue(voltageSignalLagValueNew);
            saveVoltageSettingsToPreferences();
            notifyAboutVoltageSignalLagValueChanged(flipperPosition, voltageSignalLagValueOld, voltageSignalLagValueNew);
        }
    }

    private void notifyAboutVoltageSignalLagValueChanged(FlipperPosition flipperPosition, double voltageSignalLagValueOld, double voltageSignalLagValueNew)
    {
        for(VoltageFlipSignalListener listener : voltageSignalListeners)
        {
            listener.voltageLagTimeValueChanged(flipperPosition, voltageSignalLagValueOld, voltageSignalLagValueNew);
        }
    }  


    public StandardTimeUnit getVoltageSignalLagUnit(FlipperPosition flipperPosition)
    {
        Validation.requireNonNullParameterName(flipperPosition, "flipperPosition");
        if(!flipVoltageSignals.containsKey(flipperPosition))
        {
            throw new IllegalArgumentException("No specifications for the " + flipperPosition +" flipper position known");
        }
        VoltageFlipSignalSettings signal = flipVoltageSignals.get(flipperPosition);
        StandardTimeUnit durationUnit = signal.getLagTimeUnit();

        return durationUnit;
    }

    public void setVoltageSignalLagUnit(FlipperPosition flipperPosition, StandardTimeUnit lagUnitNew)
    {
        Validation.requireNonNullParameterName(flipperPosition, "flipperPosition");
        Validation.requireNonNullParameterName(lagUnitNew, "lagUnitNew");

        VoltageFlipSignalSettings signal = flipVoltageSignals.get(flipperPosition);
        StandardTimeUnit lagTimeOld = signal.getLagTimeUnit();

        if(!Objects.equals(lagTimeOld, lagUnitNew))
        {
            signal.setLagUnit(lagUnitNew);
            saveVoltageSettingsToPreferences();
            notifyAboutVoltageSignalLagUnitChanged(flipperPosition, lagTimeOld, lagUnitNew);
        }
    }

    private void notifyAboutVoltageSignalLagUnitChanged(FlipperPosition flipperPosition, StandardTimeUnit lagUnitOld, StandardTimeUnit lagUnitNew)
    {
        for(VoltageFlipSignalListener listener : voltageSignalListeners)
        {
            listener.voltageLagTimeUnitChanged(flipperPosition, lagUnitOld, lagUnitNew);
        }
    }

    public double getMinPossibleVoltageValue(FlipperPosition flipperPosition)
    {
        return MIN_POSSIBLE_VOLTAGE_SIGNAL_VALUE;
    }

    public double getMaxPossibleVoltageValue(FlipperPosition flipperPosition)
    {
        return MAX_POSSIBLE_VOLTAGE_SIGNAL_VALUE;
    }

    public double getVoltageValue(FlipperPosition flipperPosition)
    {
        Validation.requireNonNullParameterName(flipperPosition, "flipperPosition");
        if(!flipVoltageSignals.containsKey(flipperPosition))
        {
            throw new IllegalArgumentException("No specifications for the " + flipperPosition +" flipper position known");
        }

        VoltageFlipSignalSettings signal = flipVoltageSignals.get(flipperPosition);
        double voltageValue = signal.getSignalVoltageValue();

        return voltageValue;
    }

    public void setVoltageSignalValue(FlipperPosition flipperPosition, double voltageValueNew)
    {
        Validation.requireNonNullParameterName(flipperPosition, "flipperPosition");
        Validation.requireNonNullParameterName(voltageValueNew, "voltageValueNew");

        VoltageFlipSignalSettings signal = flipVoltageSignals.get(flipperPosition);
        double voltageValueOld = signal.getSignalVoltageValue();

        if(Double.compare(voltageValueOld, voltageValueNew) != 0)
        {
            signal.setSignalVoltageValue(voltageValueNew);
            saveVoltageSettingsToPreferences();
            notifyAboutVoltageValueChanged(flipperPosition, voltageValueOld, voltageValueNew);
        }
    }

    private void notifyAboutVoltageValueChanged(FlipperPosition flipperPosition, double voltageValueOld, double voltageValueNew)
    {
        for(VoltageFlipSignalListener listener : voltageSignalListeners)
        {
            listener.voltageValueChanged(flipperPosition, voltageValueOld, voltageValueNew);
        }
    }

    private void saveVoltageSettingsToPreferences()
    {
        try {
            SerializationUtilities.putSerializableObject(PREF, VOLTAGE_SIGNALS, this.flipVoltageSignals);
        } catch (ClassNotFoundException | IOException
                | BackingStoreException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        flushPreferences();
    }

    private Map<FlipperPosition,VoltageFlipSignalSettings> buildDefaultVoltageSignals(double flipIntervalValue, StandardTimeUnit flipIntervalUnit)
    {
        Map<FlipperPosition,VoltageFlipSignalSettings> signals = new HashMap<>();
        signals.put(FlipperPosition.FIRST, new VoltageFlipSignalSettings());
        signals.put(FlipperPosition.SECOND, buildDefaultActiveVoltageSignal(flipIntervalValue, flipIntervalUnit));

        return signals;
    }

    private VoltageFlipSignalSettings buildDefaultActiveVoltageSignal(double flipIntervalValue, StandardTimeUnit flipIntervalUnit)
    {
        double flipVoltageSignalDurationValue = Math.min(initVoltageSignalDurationValue, flipIntervalValue*flipIntervalUnit.getConversionFactorTo(initVoltageSignalDurationTimeUnit));

        double flipIntervalValueInUnitsOfLagTime = flipIntervalValue*flipIntervalUnit.getConversionFactorTo(initFlipVoltageLagTimeUnit);        
        double flipVoltageSignalDurationInUnitsOfLagTime = flipVoltageSignalDurationValue*initVoltageSignalDurationTimeUnit.getConversionFactorTo(initFlipVoltageLagTimeUnit);

        double flipVoltageLagTimeValue = Math.min(flipIntervalValueInUnitsOfLagTime - flipVoltageSignalDurationInUnitsOfLagTime,initFlipVoltageLagTimeValue);

        VoltageFlipSignalSettings defaultSignal = new VoltageFlipSignalSettings();

        defaultSignal.setSendSignalAfterFlip(initSendVoltageSignalAfterFlip);
        defaultSignal.setLagUnit(initFlipVoltageLagTimeUnit);
        defaultSignal.setLagValue(flipVoltageLagTimeValue);
        defaultSignal.setSignalVoltageValue(initFlipVoltageValue);
        defaultSignal.setVoltageSignalDurationValue(flipVoltageSignalDurationValue);
        defaultSignal.setSignalDurationUnit(initVoltageSignalDurationTimeUnit); 

        return defaultSignal;
    }

    private void refreshVoltageSignalManager()
    {
        SignalReceiver signalReceiver = signalReceiverController.getSignalReceiver();
        Map<FlipperPosition, FlipAssociatedSignalAcceptor> signalAcceptors = new LinkedHashMap<>();

        for(Entry<FlipperPosition, VoltageFlipSignalSettings> entry : flipVoltageSignals.entrySet())
        {
            FlipAssociatedVoltageSignalAcceptor signalAcceptor = new FlipAssociatedVoltageSignalAcceptor(signalReceiver, entry.getValue());
            signalAcceptors.put(entry.getKey(), signalAcceptor);
        }

        this.associatedSignalManager = new FlipAssociatedSignalSimpleManager(signalAcceptors);
    }

    public String getSerialNumber()
    {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumberNew)
    {
        Validation.requireNonNullParameterName(serialNumberNew, "serialNumber");

        if(!Objects.equals(this.serialNumber, serialNumberNew))
        {
            String serialNumberOld = this.serialNumber;
            this.serialNumber = serialNumberNew;

            updateWhetherNecessaryInputProvided();

            PREF.put(SERIAL_NUMBER, this.serialNumber);
            flushPreferences();

            firePropertyChange(SERIAL_NUMBER, serialNumberOld, serialNumberNew);

            if(ThorlabsFlipperFactory.isCompleteValidThorlabsFlipperSerialNumberAtLeastWhenTrimmed(serialNumberNew))
            {
                this.currentFlipper = ThorlabsFlipperFactory.getInstance().getFlipperIfPossible(serialNumberNew);
                selectFlipperPosition(this.currentFlipper.getPosition());
            }
            else
            {
                this.currentFlipper = DummyFlipper.getInstance();
            }

            //we do not change the position or transit time of the hardware, to avoid behaviour that is surprising to the 
            ensureConsistencyOfSettingsWithActiveFlipper();
        }
    }

    private void ensureConsistencyOfSettingsWithActiveFlipper()
    {
        FlipperPosition positionOfHardware = this.currentFlipper.getPosition();
        if(positionOfHardware.isKnown())
        {
            setFlipperPosition(positionOfHardware);
        }
        int transitTimeInMiliseconds = this.currentFlipper.getTransitTimeInMiliseconds();
        setTransitTimeInMiliseconds(transitTimeInMiliseconds);
    }

    public boolean isStartFlippingWhenNewMeasurementBegins()
    {
        return startFlippingWhenNewMeasurementBegins;
    }

    public void setStartFlippingWhenNewMeasurementBegins(boolean startFlippingWhenNewMeasurementBeginsNew)
    {
        if(this.startFlippingWhenNewMeasurementBegins != startFlippingWhenNewMeasurementBeginsNew)
        {            
            boolean startTogglingWhenNewMeasurementBeginsOld = this.startFlippingWhenNewMeasurementBegins;
            this.startFlippingWhenNewMeasurementBegins = startFlippingWhenNewMeasurementBeginsNew;

            PREF.putBoolean(START_FLIPPING_WHEN_NEW_MEASUREMENT_BEGINS, this.startFlippingWhenNewMeasurementBegins);
            flushPreferences();

            firePropertyChange(START_FLIPPING_WHEN_NEW_MEASUREMENT_BEGINS, startTogglingWhenNewMeasurementBeginsOld, startFlippingWhenNewMeasurementBeginsNew);
        }
    }

    public double getFlipIntervalValue()
    {
        return flipIntervalValue;
    }

    public void setFlipIntervalValue(double flipIntervalValueNew)
    {
        Validation.requireNotNaNParameterName(flipIntervalValueNew, "flipIntervalValueNew");
        Validation.requireNonNegativeParameterName(flipIntervalValueNew, "flipIntervalValueNew");

        if(Double.compare(this.flipIntervalValue, flipIntervalValueNew) != 0)
        {
            double flipIntervalValueOld = this.flipIntervalValue;
            this.flipIntervalValue = flipIntervalValueNew;

            PREF.putDouble(FLIP_INTERVAL_VALUE, flipIntervalValueNew);
            flushPreferences();

            firePropertyChange(FLIP_INTERVAL_VALUE, flipIntervalValueOld, flipIntervalValueNew);
        }
    }

    public StandardTimeUnit getFlipIntervalUnit()
    {
        return this.flipIntervalUnit;
    }

    public void setFlipIntervalUnit(StandardTimeUnit flipIntervalUnitNew)
    {
        Validation.requireNonNullParameterName(flipIntervalUnitNew, "flipIntervalUnitNew");

        if(!Objects.equals(this.flipIntervalUnit, flipIntervalUnitNew))
        {
            StandardTimeUnit flipIntervalUnitOld = this.flipIntervalUnit;
            this.flipIntervalUnit = flipIntervalUnitNew;

            PREF.put(FLIP_INTERVAL_UNIT, flipIntervalUnitNew.name());
            flushPreferences();

            firePropertyChange(FLIP_INTERVAL_UNIT, flipIntervalUnitOld, flipIntervalUnitNew);
        }
    }

    public double getFlipperIntervalValueInMiliseconds()
    {
        double unitToMilisConversionFactor = this.flipIntervalUnit.getConversionFactorToMilliseconds();
        double intervalInMiliseconds = this.flipIntervalValue*unitToMilisConversionFactor;

        return intervalInMiliseconds;
    }

    public FlipperPosition getFlipperPosition()
    {
        return this.currentPosition;
    }

    public void selectFlipperPosition(FlipperPosition positionNew)
    {                
        if(!Objects.equals(this.currentPosition, positionNew))
        {
            setFlipperPosition(positionNew);           
            this.currentFlipper.moveToPositionIfPossible(this.currentPosition);
        }
    }

    private void setFlipperPosition(FlipperPosition positionNew)
    {                
        if(!Objects.equals(this.currentPosition, positionNew))
        {
            FlipperPosition positionOld = this.currentPosition;
            this.currentPosition = positionNew;

            PREF.putInt(CURRENT_POSITION, positionNew.getCode());       
            flushPreferences();

            firePropertyChange(CURRENT_POSITION, positionOld, positionNew);

            updateWhetherNecessaryInputProvided();            
        }
    }

    public void flipIfPossible()
    {        
        if(currentFlipper.isActive())
        {
            boolean flippedSucessfully = currentFlipper.flipIfPossible();
            //we do not query position, as the device returns error code, probably because queried too fast
            if(flippedSucessfully)
            {
                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

                FlipperPosition newPosition = this.currentPosition.getNextPosition();

                Runnable taskTriggerSignal = new Runnable()
                {                  
                    @Override
                    public void run() 
                    {
                        associatedSignalManager.triggerSignal(newPosition);
                    }
                }; 


                Runnable taskEndSignal = new Runnable()
                {                  
                    @Override
                    public void run() 
                    {
                        associatedSignalManager.endSignal(newPosition);
                    }
                }; 

                long lagTimeInMillis = Math.round(associatedSignalManager.getLagTimeInMilliseconds(newPosition));

                System.err.println("lag time in millis " + lagTimeInMillis);
                executor.schedule(taskTriggerSignal, lagTimeInMillis, TimeUnit.MILLISECONDS);

                long signalDurationInMillis = Math.round(associatedSignalManager.getSignalDurationInMilliseconds(newPosition));

                System.out.println("signalDurationInMillis "+signalDurationInMillis);
                executor.schedule(taskEndSignal, lagTimeInMillis + signalDurationInMillis, TimeUnit.MILLISECONDS);

                executor.shutdown();

                setFlipperPosition(newPosition);  
            }
        }   
    }

    public int getTransitTimeInMiliseconds()
    {
        return transitTimeInMiliseconds;
    }

    public void selectTransitTimeInMiliseconds(int transitTimeNew)
    {
        Validation.requireValueEqualToOrBetweenBounds(transitTimeNew, ThorlabsFlipper.SHORTEST_TRANSIT_TIME_IN_MILISECONDS, ThorlabsFlipper.LONGEST_TRANSIT_TIME_IN_MILISECONDS, "transitTimeNew");
        if(Double.compare(this.transitTimeInMiliseconds, transitTimeNew) != 0)
        {
            setTransitTimeInMiliseconds(transitTimeNew);
            this.currentFlipper.setTransitTimeInMiliseconds(transitTimeNew);
        }
    }

    private void setTransitTimeInMiliseconds(int transitTimeNew)
    {
        Validation.requireValueEqualToOrBetweenBounds(transitTimeNew, ThorlabsFlipper.SHORTEST_TRANSIT_TIME_IN_MILISECONDS, ThorlabsFlipper.LONGEST_TRANSIT_TIME_IN_MILISECONDS, "transitTimeNew");
        if(Double.compare(this.transitTimeInMiliseconds, transitTimeNew) != 0)
        {
            int transitTimeOld = this.transitTimeInMiliseconds;
            this.transitTimeInMiliseconds = transitTimeNew;

            PREF.putInt(TRANSIT_TIME, transitTimeNew);       
            flushPreferences();

            firePropertyChange(TRANSIT_TIME, transitTimeOld, transitTimeNew);            
        }
    }

    public int getShortestTransitTimeInMiliseconds()
    {
        return ThorlabsFlipper.SHORTEST_TRANSIT_TIME_IN_MILISECONDS;
    }

    public int getLongestTransitTimeInMiliseconds()
    {
        return ThorlabsFlipper.LONGEST_TRANSIT_TIME_IN_MILISECONDS;
    }

    public boolean isNecessaryInputProvided()
    {
        return necessaryInputProvided;
    }

    protected void updateWhetherNecessaryInputProvided()
    {
        boolean necessaryInputProvidedNew = checkIfNecessaryInputProvided();
        if(this.necessaryInputProvided != necessaryInputProvidedNew)
        {
            boolean necessaryInputProvidedOld = this.necessaryInputProvided;
            this.necessaryInputProvided = necessaryInputProvidedNew;

            firePropertyChange(NECESSARY_INPUT_PROVIDED, necessaryInputProvidedOld, necessaryInputProvidedNew);
        }
    }

    protected boolean checkIfNecessaryInputProvided()
    {
        boolean necessaryInputProvided = true;
        necessaryInputProvided = necessaryInputProvided && Objects.nonNull(serialNumber);

        return necessaryInputProvided;
    }

    public void notifyAboutStartOfFlippingAtIntervals()
    {}

    public void notifyAboutEndOfFlippingAtIntervals()
    {}

    public void notifyAboutRunFinishedWithException()
    {
        terminateFlipperWorker();
    }

    public void notifyAboutRunFinishedSuccessfully()
    {
        terminateFlipperWorker();
    }

    public void notifyAboutRunStarted()
    {        
        if(this.startFlippingWhenNewMeasurementBegins)
        {        
            buildAndExecuteFlipperWorkerIfPossible();
        }
    }

    public void notifyAboutRunCanceled()
    {       
        //we should not use  !this.lastExecutedFlipperWorker.isDone() in check, as the flipper worker is done quickly, only scheduler goes on
        terminateFlipperWorker();
    }

    public void notifyAboutRunStopped()
    {        
        terminateFlipperWorker();

        if(this.lastExecutedFlipperWorker != null)
        {
            List<PhaseRealTiming> phasesRealTimings = this.lastExecutedFlipperWorker.getPhaseRealTimings();
            if(!phasesRealTimings.isEmpty())
            {
                PhaseRealTiming lastExecutedPhaseTimings = phasesRealTimings.get(phasesRealTimings.size() - 1);               
                this.phaseRemainderToResumeAfterStop = new FlipperPhaseRemainder(this.currentPosition, lastExecutedPhaseTimings.getMismatchPhase());                
            }
            else 
            {
                this.phaseRemainderToResumeAfterStop = null;
            }   
        }
        else
        {
            this.phaseRemainderToResumeAfterStop = null;
        }
    }

    public void notifyAboutRunResumed()
    {
        if(this.startFlippingWhenNewMeasurementBegins)
        {
            buildAndExecuteActinicBeamPhaseScheduleWorkerInResponseToRunResume();
        }
    }

    private void buildAndExecuteFlipperWorkerIfPossible()
    {        
        this.lastExecutedFlipperWorker = new FlipperFixedRateFlippingSwingWorker(this, Math.round(getFlipperIntervalValueInMiliseconds()));
        this.lastExecutedFlipperWorker.execute();
    }

    private void buildAndExecuteActinicBeamPhaseScheduleWorkerInResponseToRunResume()
    {
        long initialDelay = phaseRemainderToResumeAfterStop !=null ? Math.round(phaseRemainderToResumeAfterStop.getDurationInMiliseconds()) : Math.round(getFlipperIntervalValueInMiliseconds());
        this.lastExecutedFlipperWorker = new FlipperFixedRateFlippingSwingWorker(this, initialDelay);         

        lastExecutedFlipperWorker.execute();
    }

    private void terminateFlipperWorker()
    {
        if(lastExecutedFlipperWorker != null)
        {
            lastExecutedFlipperWorker.terminateAllTasks();
        }
    }

    public PhaseStamp getFlipperPhase()
    {
        return flipperPhase;
    }

    protected void setFlipperPhase(PhaseStamp phaseNew, boolean isScheduleContinuedAfterStop)
    {
        if(!Objects.equals(this.flipperPhase, phaseNew))
        {   
            if(phaseNew.isFirstPhaseOfRecording() && !isScheduleContinuedAfterStop)
            {
                this.latestRecordingOnsetAbsoluteTime =  phaseNew.getOnsetAbsoluteTimeInMiliseconds();
            }

            this.flipperPhase = phaseNew;
        }
    }

    private void flushPreferences()
    {
        try {
            PREF.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }


}
