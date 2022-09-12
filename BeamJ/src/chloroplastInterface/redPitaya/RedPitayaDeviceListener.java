package chloroplastInterface.redPitaya;

public interface RedPitayaDeviceListener
{
    public void redPitayaLockInFound(RedPitayaLockInDevice device);
    public void redPitayaLockInRemoved(RedPitayaLockInDevice device);
}