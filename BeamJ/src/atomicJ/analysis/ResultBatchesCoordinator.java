package atomicJ.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import atomicJ.sources.IdentityTag;

public class ResultBatchesCoordinator 
{
    private int currentBatchNumber = 1;
    private final List<IdentityTag> analysedBatches = new ArrayList<>();



    public int getPublishedBatchCount() 
    {
        return currentBatchNumber;
    }

    public void countNewBatches(Collection<IdentityTag> batchIds) 
    {
        for(IdentityTag id: batchIds)
        {
            if(!analysedBatches.contains(id))
            {
                analysedBatches.add(id);
                this.currentBatchNumber++;
            }
        }
    }
}
