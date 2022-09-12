package chloroplastInterface;

public interface Phase
{   
    public double getDuration();
    public StandardTimeUnit getDurationUnit();
    public double getDuration(StandardTimeUnit unit);
    public double getDurationInMiliseconds();
    public boolean isInstantenous();  
}