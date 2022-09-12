package chloroplastInterface;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface Visa extends StdCallLibrary
{
    //ViStatus _VI_FUNC  viOpenDefaultRM (ViPSession vi);

    public int viOpenDefaultRM(IntByReference vi);
}
