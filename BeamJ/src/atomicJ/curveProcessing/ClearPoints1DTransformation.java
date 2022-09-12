package atomicJ.curveProcessing;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Point1DData;

public class ClearPoints1DTransformation implements Channel1DDataTransformation
{   
    private final SortedArrayOrder newPointsOrder;

    public ClearPoints1DTransformation( SortedArrayOrder orderOfNewChannel)
    {
        this.newPointsOrder = orderOfNewChannel;
    }

    @Override
    public Channel1DData transform(Channel1DData channel) 
    {  
        return transformAnyChannel(channel);
    }

    //the order of the result channel is the same as the order of the input
    private Channel1DData transformAnyChannel(Channel1DData channel)
    {
        FlexibleChannel1DData channelModified = new FlexibleChannel1DData(new double[][] {}, channel.getXQuantity(), channel.getYQuantity(), newPointsOrder);
        return channelModified;
    }

    @Override
    public Point1DData transformPointChannel(Point1DData channel)
    {
        return channel;
    }
}
