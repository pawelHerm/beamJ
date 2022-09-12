package atomicJ.gui.imageProcessing;

public enum ImageLineOrientation 
{
    HORIZONTAL("Horizontal"), VERTICAL("Vertical");

    private final String name;

    private ImageLineOrientation(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
