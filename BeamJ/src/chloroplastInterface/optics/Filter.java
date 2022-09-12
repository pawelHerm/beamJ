package chloroplastInterface.optics;

public interface Filter 
{
    public String getDescription();
    public boolean canBeDescribedBy(String description);
}
