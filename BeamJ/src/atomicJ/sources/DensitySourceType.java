package atomicJ.sources;

public enum DensitySourceType {

    ALL("All"), MAP("Map"), IMAGE("Image");

    private final String name;

    DensitySourceType(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
