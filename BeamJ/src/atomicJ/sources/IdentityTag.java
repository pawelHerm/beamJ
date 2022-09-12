package atomicJ.sources;

public class IdentityTag
{
    private final Object key;
    private final String label;

    public IdentityTag(Object key)
    {
        this.key = key;
        this.label = key.toString();
    }

    public IdentityTag(Object key, String label)
    {
        this.key = key;
        this.label = label;
    }

    public Object getKey()
    {
        return key;
    }

    public String getLabel()
    {
        return label;
    }

    @Override
    public int hashCode()
    {
        return key.hashCode(); 
    }

    @Override
    public boolean equals(Object that)
    {
        if(that instanceof IdentityTag)
        {
            return this.key.equals(((IdentityTag)that).key);
        }

        return false;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
