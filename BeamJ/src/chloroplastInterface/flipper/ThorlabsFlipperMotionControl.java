package chloroplastInterface.flipper;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

public interface ThorlabsFlipperMotionControl extends StdCallLibrary
{    
    ThorlabsFlipperMotionControl INSTANCE = (ThorlabsFlipperMotionControl) Native.loadLibrary("Thorlabs.MotionControl.FilterFlipper", ThorlabsFlipperMotionControl.class);

    /*
     * short __cdecl TLI_GetDeviceListSize();
     */
    public int TLI_GetDeviceListSize();

    /*
     * FILTERFLIPPERDLL_API short __cdecl FF_Open(char const * serialNo);
     */
    public int FF_Open(String serialNo);

    /*
     * bool __cdecl FF_StartPolling(char const * serialNo, int milliseconds);
     */
    public boolean FF_StartPolling(String serialNo, int milliseconds);

    /*
     * FILTERFLIPPERDLL_API void __cdecl FF_StopPolling(char const * serialNo);
     */  
    public void FF_StopPolling(String serialNo);

    /*
     * FILTERFLIPPERDLL_API void __cdecl FF_Close(char const * serialNo);
     */
    public int FF_Close(String serialNo);

    /*
     * FILTERFLIPPERDLL_API short __cdecl TLI_BuildDeviceList(void);
     */
    public int TLI_BuildDeviceList();

    /*
     * FILTERFLIPPERDLL_API bool __cdecl FF_CheckConnection(char const * serialNo);
     */
    public boolean FF_CheckConnection(String serialNo);

    /*
     * FILTERFLIPPERDLL_API short __cdecl FF_MoveToPosition(char const * serialNo, FF_Positions position);
     */
    public int FF_MoveToPosition(String serialNo, int position);


    /*
     * FILTERFLIPPERDLL_API short __cdecl FF_RequestStatus(char const * serialNo);
     */

    public int FF_RequestStatus(String serialNo);

    /*
     * FILTERFLIPPERDLL_API FF_Positions __cdecl FF_GetPosition(char const * serialNo);
     */

    public int FF_GetPosition(String serialNo);

    /*
     * FILTERFLIPPERDLL_API unsigned int __cdecl FF_GetTransitTime(const char * serialNo);
     */
    public int FF_GetTransitTime(String serialNo);

    /*
     * short __cdecl FF_SetTransitTime(const char * serialNo, unsigned int transitTime)
     */  
    public int FF_SetTransitTime(String serialNo, int transitTime);
}
