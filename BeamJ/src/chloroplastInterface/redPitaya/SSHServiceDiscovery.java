package chloroplastInterface.redPitaya;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import atomicJ.utilities.Validation;

public class SSHServiceDiscovery 
{   
    //RedPitaya services can be found here https://github.com/RedPitaya/RedPitaya/tree/master/OS/debian/overlay/etc/avahi/services
    //Since the OS version 0.96, RedPitaya uses Avahi to broadcast its SSH service
    private static final String SSH_SERVICE_TYPE = "_ssh._tcp.local.";
    private final List<SSHServiceResolutionListener> listeners = new ArrayList<>();
    private JmDNS jmdns;
    private boolean works;

    public void addSSHServiceListener(SSHServiceResolutionListener listener)
    {
        Validation.requireNonNullParameterName(listener, "listener");

        if(!listeners.contains(listener))
        {
            listeners.add(listener);
        }
    }

    public void removeSSHServiceListener(SSHServiceResolutionListener listener)
    {
        Validation.requireNonNullParameterName(listener, "listener");

        listeners.remove(listener);
    }

    private void fireSSHServiceResolved(ServiceInfo sshServiceInfo)
    {
        for(SSHServiceResolutionListener listener : listeners)
        {
            listener.SSHServiceResolved(sshServiceInfo);
        }
    }

    private void fireSSHServiceRemoved(ServiceInfo sshServiceInfo)
    {
        for(SSHServiceResolutionListener listener : listeners)
        {
            listener.SSHServiceRemoved(sshServiceInfo);
        }
    }


    private class SampleListener implements ServiceListener 
    {
        @Override
        public void serviceAdded(ServiceEvent event) 
        {
        }

        @Override
        public void serviceRemoved(ServiceEvent event) 
        {
            ServiceInfo info = event.getInfo();           
            fireSSHServiceRemoved(info);
        }

        @Override
        public void serviceResolved(ServiceEvent event)
        {                  
            ServiceInfo info = event.getInfo();           
            fireSSHServiceResolved(info);           
        }
    }

    public boolean isWorking()
    {
        return works;
    }

    public void stop()
    {
        if(this.jmdns != null)
        {
            try {
                this.jmdns.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        this.works = false;
    }

    public void startSSHServiceDiscovery() throws InterruptedException 
    {
        initJmDNSAndAddListener();
        jmdns.addServiceListener(SSH_SERVICE_TYPE, new SampleListener());
    }

    private void initJmDNSAndAddListener()
    {
        try {
            this.jmdns = JmDNS.create(InetAddress.getLocalHost());            
            this.works = true;
        } catch (UnknownHostException e) 
        {
            System.out.println(e.getMessage());

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public ServiceInfo[] getSSHServices()
    {
        if(!works)
        {
            initJmDNSAndAddListener();
        }

        ServiceInfo[] infos = this.jmdns.list(SSH_SERVICE_TYPE);
        return infos;
    }

    public static interface SSHServiceResolutionListener
    {
        public void SSHServiceResolved(ServiceInfo sshServiceInfo);
        public void SSHServiceRemoved(ServiceInfo sshServiceInfo);
    }
}
