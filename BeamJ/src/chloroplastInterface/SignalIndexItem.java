package chloroplastInterface;

public class SignalIndexItem
{
    //zero - based index
    private final int signalIndex;

    public SignalIndexItem(int signalIndex)
    {
        this.signalIndex = signalIndex;
    }

    static SignalIndexItem[] buildSignalIndexArray(int signalCount)
    {
        SignalIndexItem[] signalIndices = new SignalIndexItem[signalCount];
        for(int i = 0; i<signalCount;i++)
        {
            signalIndices[i] = new SignalIndexItem(i);
        }

        return signalIndices;
    }


    public int getZeroBasedSignalIndex()
    {
        return signalIndex;
    }

    @Override
    public String toString()
    {
        String oneBasedIndexString= Integer.toString(signalIndex + 1);

        return oneBasedIndexString;
    }

    @Override
    public int hashCode()
    {
        int hashCode = Integer.hashCode(this.signalIndex);
        return hashCode;
    }

    @Override
    public boolean equals(Object other)
    {
        if(other instanceof SignalIndexItem)
        {
            SignalIndexItem that = (SignalIndexItem)other;
            if(this.signalIndex == that.signalIndex)
            {
                return true;
            }
        }
        return false;
    }
}