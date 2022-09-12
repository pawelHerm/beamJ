package chloroplastInterface.flipper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FlipAssociatedSignalSimpleManager implements FlipAssociatedSignalManager
{
    private final Map<FlipperPosition, FlipAssociatedSignalAcceptor> signalAcceptors;

    private static FlipAssociatedSignalSimpleManager NULL_INSTANCE = new FlipAssociatedSignalSimpleManager(new HashMap<>());

    public FlipAssociatedSignalSimpleManager(Map<FlipperPosition, FlipAssociatedSignalAcceptor> signalAcceptors)
    {
        this.signalAcceptors = new LinkedHashMap<>(signalAcceptors);
    }

    public static FlipAssociatedSignalSimpleManager getNullInstance()
    {
        return NULL_INSTANCE;
    }

    @Override
    public boolean isSendSignalAfterFlip(FlipperPosition position)
    {
        boolean sendSignal = false;
        if(signalAcceptors.containsKey(position))
        {
            sendSignal = signalAcceptors.get(position).isSendSignalAfterFlip();
        }

        return sendSignal;
    }

    @Override
    public double getLagTimeInMilliseconds(FlipperPosition position) 
    {
        double lagTimeInMilliseconds = 0;
        if(signalAcceptors.containsKey(position))
        {
            lagTimeInMilliseconds = signalAcceptors.get(position).getLagTimeInMilliseconds();
        }

        return lagTimeInMilliseconds;
    }

    @Override
    public void triggerSignal(FlipperPosition currentPosition)
    {           
        if(signalAcceptors.containsKey(currentPosition))
        {
            signalAcceptors.get(currentPosition).triggerSignal();
        }
    }

    @Override
    public void endSignal(FlipperPosition currentPosition)
    {           
        if(signalAcceptors.containsKey(currentPosition))
        {
            signalAcceptors.get(currentPosition).endSignal();
        }
    }

    @Override
    public double getSignalDurationInMilliseconds(FlipperPosition currentPosition)
    {
        double signalDuration = 0;
        if(signalAcceptors.containsKey(currentPosition))
        {
            signalDuration = signalAcceptors.get(currentPosition).getSignalDurationInMilliseconds();
        }

        return signalDuration;
    }
}