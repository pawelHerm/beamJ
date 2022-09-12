package atomicJ.gui;

import java.text.NumberFormat;

import org.jfree.chart.axis.NumberTickUnit;

public class PremutipliedSexagesimalTickUnit extends NumberTickUnit 
{
    private static final long serialVersionUID = 1L;

    private final double premultiplierMainUnit;
    private final double smallerUnitsPerMainUnit;

    private final NumberFormat fractionalPartFormat;

    public PremutipliedSexagesimalTickUnit(double size, NumberFormat integerPartFormat, NumberFormat fractionalPartFormat, int minorTickCount, double premultiplierMainUnit, double smallerUnitsPerMainUnit)
    {
        super(size, integerPartFormat, minorTickCount);

        this.premultiplierMainUnit = premultiplierMainUnit;
        this.smallerUnitsPerMainUnit = smallerUnitsPerMainUnit;

        this.fractionalPartFormat = fractionalPartFormat;
    }

    @Override
    public String valueToString(double value)
    {
        double premultipliedValue = premultiplierMainUnit*value;
        double fractionalPart = premultipliedValue % 1;
        double integralPart = premultipliedValue - fractionalPart;

        String integralPartString = super.valueToString(integralPart);
        String fractionalPartString = fractionalPartFormat.format(fractionalPart*smallerUnitsPerMainUnit);

        StringBuilder builder = new StringBuilder(integralPartString);
        builder.append(":").append(fractionalPartString);

        return builder.toString();
    }
}