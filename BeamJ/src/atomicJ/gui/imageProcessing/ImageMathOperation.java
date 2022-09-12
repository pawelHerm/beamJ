package atomicJ.gui.imageProcessing;

public enum ImageMathOperation
{
    ADD("Add", "+"), SUBTRACT("Subtract", "-"), AVERAGE("Average", "average"), MULTIPLY("Multiply", "*"), DIVIDE("Divide", "/");

    private final String name;
    private final String symbol;

    ImageMathOperation(String name, String symbol)
    {
        this.name = name;
        this.symbol = symbol;
    }

    public String getSymbol()
    {
        return symbol;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
