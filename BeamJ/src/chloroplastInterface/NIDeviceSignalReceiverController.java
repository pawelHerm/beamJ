package chloroplastInterface;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;

public class NIDeviceSignalReceiverController implements SignalReceiverController 
{
    private final NIDevice niDevice;

    public NIDeviceSignalReceiverController(NIDevice niDevice)
    {
        this.niDevice = niDevice;
    }

    public static List<NIDeviceSignalReceiverController> buildFactoriesWhenPossible(List<NIDevice> devices)
    {
        List<NIDeviceSignalReceiverController> factories = new ArrayList<>();

        for(NIDevice device : devices)
        {
            if(!device.isSimulated() && device.isProperlyConstructed())
            {
                factories.add(new NIDeviceSignalReceiverController(device));
            }
        }

        return factories;
    }

    @Override
    public String toString()
    {
        String name = "NI " + niDevice.toString();
        return name;
    }

    @Override
    public String getUniqueDescription()
    {
        String name = "NI " + niDevice.toString();
        return name;
    }

    @Override
    public SignalReceiver getSignalReceiver() 
    {
        SignalReceiver source = new NIDAQmxSignalReceiver(niDevice);
        return source;
    }

    @Override
    public SignalReceiver getSignalReceiver(double minOutVoltage, double maxOutVoltage)
    {
        SignalReceiver source = new NIDAQmxSignalReceiver(niDevice,/*output channel index*/0,minOutVoltage,maxOutVoltage);
        return source;
    }

    @Override
    public boolean isFunctional() {
        return true;
    }

    public boolean requiresDevice(String deviceIdentifier) 
    {
        boolean required= Objects.equal(niDevice.getIdentifier(), deviceIdentifier);
        return required;
    }

    @Override
    public boolean shouldBeReplacedWhenOtherControllerFound()
    {
        return false;
    }
}
