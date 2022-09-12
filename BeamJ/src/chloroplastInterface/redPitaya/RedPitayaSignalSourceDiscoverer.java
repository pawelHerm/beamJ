package chloroplastInterface.redPitaya;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jmdns.ServiceInfo;

import chloroplastInterface.redPitaya.SSHServiceDiscovery.SSHServiceResolutionListener;

public class RedPitayaSignalSourceDiscoverer 
{ 
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    private static RedPitayaSignalSourceDiscoverer INSTANCE; 

    private final Map<String, RedPitayaLockInDevice> redPitayaDevices = new LinkedHashMap<>();
    private final Set<RedPitayaDeviceListener> listeners = new LinkedHashSet<>();

    private final SSHServiceDiscovery sshServiceDiscovery = new SSHServiceDiscovery();

    private RedPitayaSignalSourceDiscoverer()
    {
        sshServiceDiscovery.addSSHServiceListener(new SSHServiceResolutionListener() {

            @Override
            public void SSHServiceResolved(ServiceInfo sshServiceInfo) 
            {                                                        
                RedPitayaCommunicationChannel communicationChannel = new RedPitayaCommunicationChannel(USERNAME, PASSWORD, sshServiceInfo.getHostAddress(),sshServiceInfo.getPort());

                boolean workingRedPitayaLIAFound = false;
                try
                {
                    workingRedPitayaLIAFound = RedPitayaLockInDevice.checkIfLockInApplicationInstalled(communicationChannel);               
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                if(workingRedPitayaLIAFound)
                {
                    String serviceName = sshServiceInfo.getQualifiedName();
                    RedPitayaLockInDevice controller = new RedPitayaLockInDevice(communicationChannel);
                    redPitayaDevices.put(serviceName, controller);
                    fireRedPitayaDeviceFound(controller);
                }
                else
                {
                    communicationChannel.close();
                }
            }

            @Override
            public void SSHServiceRemoved(ServiceInfo sshServiceInfo)
            {
                String serviceName = sshServiceInfo.getQualifiedName();
                if(redPitayaDevices.containsKey(serviceName))
                {
                    fireRedPitayaDeviceRemoved(redPitayaDevices.get(serviceName));
                }
            }
        });

        try {
            sshServiceDiscovery.startSSHServiceDiscovery();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean isWorking()
    {
        return sshServiceDiscovery.isWorking();
    }

    public void stop()
    {
        System.err.println("STOPS DISCOVERY");

        sshServiceDiscovery.stop();
    }

    public static RedPitayaSignalSourceDiscoverer getInstance()
    {
        if(INSTANCE == null)
        {
            INSTANCE = new RedPitayaSignalSourceDiscoverer();
        }

        return INSTANCE;
    }

    public void addRedPitayaDeviceListener(RedPitayaDeviceListener listener)
    {
        listeners.add(listener);
    }

    public void removeRedPitayaDeviceListener(RedPitayaDeviceListener listener)
    {
        listeners.remove(listener);
    }

    private void fireRedPitayaDeviceFound(RedPitayaLockInDevice controller)
    {
        for(RedPitayaDeviceListener listener: listeners)
        {
            listener.redPitayaLockInFound(controller);
        }
    }

    private void fireRedPitayaDeviceRemoved(RedPitayaLockInDevice controller)
    {
        for(RedPitayaDeviceListener listener: listeners)
        {
            listener.redPitayaLockInRemoved(controller);
        }
    }

    //https://github.com/posicks/mdnsjava
    public List<RedPitayaSignalSourceController> refreshDeviceListAndReturnControllers()
    {  
        ServiceInfo[] services = sshServiceDiscovery.getSSHServices();
        for(ServiceInfo sshServiceInfo : services)
        {
            String serviceName = sshServiceInfo.getQualifiedName();
            if(!redPitayaDevices.containsKey(serviceName))
            {
                RedPitayaCommunicationChannel communicationChannel = new RedPitayaCommunicationChannel(USERNAME, PASSWORD, sshServiceInfo.getHostAddress(),sshServiceInfo.getPort());
                RedPitayaLockInDevice controller = new RedPitayaLockInDevice(communicationChannel);
                redPitayaDevices.put(serviceName, controller);
            }
        }

        List<RedPitayaSignalSourceController> controllers = new ArrayList<>();

        for(RedPitayaLockInDevice dev : redPitayaDevices.values())
        {
            controllers.add(new RedPitayaSignalSourceController(dev));
        }

        return controllers;
    }
}
