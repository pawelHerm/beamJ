package chloroplastInterface.optics;

import com.google.common.base.Objects;

import atomicJ.utilities.Validation;
import java.io.Serializable;

public class SimpleFilter implements Filter, Serializable
{
    private static final long serialVersionUID = 1L;

    private final String description;

    public SimpleFilter(String description)
    {
        Validation.requireNonNullParameterName(description, "description");
        this.description = description;
    }

    @Override
    public String getDescription() 
    {
        return description;
    }

    @Override
    public boolean canBeDescribedBy(String descriptionOther) 
    {
        Validation.requireNonNullParameterName(descriptionOther, "descriptionOther");

        boolean canBeDescribed = Objects.equal(this.description.trim(), descriptionOther.trim());
        return canBeDescribed;
    }
}
