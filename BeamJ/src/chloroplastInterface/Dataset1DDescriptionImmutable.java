package chloroplastInterface;

import com.google.common.base.Objects;

import atomicJ.data.units.Quantity;

public class Dataset1DDescriptionImmutable 
{
    private final String identifier;
    private final int length;
    private final Quantity xQuantity;
    private final Quantity yQuantity;

    Dataset1DDescriptionImmutable(String identifier, int length, Quantity xQuantity, Quantity yQuantity)
    {
        this.identifier = identifier;
        this.length = length;
        this.xQuantity = xQuantity;
        this.yQuantity = yQuantity;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public int getLength()
    {
        return length;
    }

    public Quantity getXQuantity()
    {
        return xQuantity;
    }
    
    public Quantity getYQuantity()
    {
        return yQuantity;
    }
    
    @Override
    public int hashCode()
    {
        int result = Objects.hashCode(this.identifier);
        result = 31*result + Integer.hashCode(this.length);
        result = 31*result + Objects.hashCode(this.xQuantity);
        result = 31*result + Objects.hashCode(this.yQuantity);
        
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Dataset1DDescriptionImmutable)
        {
            Dataset1DDescriptionImmutable that = (Dataset1DDescriptionImmutable)o;
            boolean equal = Objects.equal(this.identifier, that.identifier);
            equal = equal && (Integer.compare(this.length, that.length) == 0);
            equal = equal && Objects.equal(this.xQuantity, that.xQuantity);
            equal = equal && Objects.equal(this.yQuantity, that.yQuantity);

            return equal;
        }

        return false;
    }
}
