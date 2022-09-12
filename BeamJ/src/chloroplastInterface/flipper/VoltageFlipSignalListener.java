package chloroplastInterface.flipper;

import chloroplastInterface.SignalReceiverController;
import chloroplastInterface.StandardTimeUnit;

public interface VoltageFlipSignalListener
{
    public void sendVoltageSignalAfterFlipChanged(FlipperPosition flipperPosition, boolean sendVoltageSignalAfterFlipOld, boolean sendVoltageSignalAfterFlipNew);
    public void voltageLagTimeValueChanged(FlipperPosition flipperPosition, double flipVoltageLagTimeValueOld, double flipVoltageLagTimeValueNew);
    public void voltageLagTimeUnitChanged(FlipperPosition flipperPosition, StandardTimeUnit flipVoltageLagTimeUnitOld, StandardTimeUnit flipVoltageLagTimeUnitNew);
    public void voltageValueChanged(FlipperPosition flipperPosition, double flipVoltageValueOld, double flipVoltageValueNew);       
    public void voltageSignalDurationValueChanged(FlipperPosition flipperPosition, double voltageSignalDurationValueOld, double voltageSignalDurationValueNew);       
    public void voltageSignalDurationTimeUnitChanged(FlipperPosition flipperPosition, StandardTimeUnit voltageSignalDurationTimeUnitOld, StandardTimeUnit voltageSignalDurationTimeUnitNew);

    public void signalReceiverControllerChanged(SignalReceiverController signalReceiverControllerOld, SignalReceiverController signalReceiverControllerNew);
    public void functioningSignalReceiverControllerRemoved(SignalReceiverController controller);
    public void functioningSignalReceiverControllerAdded(SignalReceiverController controllerNew);
    public void availabilityOfFunctioningSignalReceiverControllersChange(boolean functionalSignalReceiverControllerAvailableOld, boolean functionalSignalReceiverControllerAvailableNew);
}