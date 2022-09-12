package atomicJ.analysis;

import java.util.List;

import atomicJ.sources.Channel1DSource;
import atomicJ.sources.IdentityTag;

public interface Processed1DPack <E extends Processed1DPack<E,S>,S extends Channel1DSource<?>>
{
    public S getSource();
    public void setBatchIdTag(IdentityTag batchId);
    public IdentityTag getBatchIdTag();
    public List<? extends ProcessedPackFunction<? super E>> getSpecialFunctions();
}
