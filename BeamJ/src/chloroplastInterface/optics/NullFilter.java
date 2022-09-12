package chloroplastInterface.optics;

import java.io.Serializable;
import java.util.Objects;

public class NullFilter implements Filter, Serializable
{
    private static final long serialVersionUID = 1L;

    private static final String DESCRIPTION = "empty";

    private static final NullFilter INSTANCE = new NullFilter();

    private NullFilter()
    {}

    public static NullFilter getInstance()
    {
        return INSTANCE;
    }

    @Override
    public String getDescription() 
    {
        return DESCRIPTION;
    }

    @Override
    public boolean canBeDescribedBy(String description) 
    {
        boolean canBeDescribed = Objects.equals(DESCRIPTION, description);
        return canBeDescribed;
    }
}
