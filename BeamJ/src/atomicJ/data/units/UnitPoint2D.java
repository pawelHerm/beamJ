package atomicJ.data.units;

import org.jfree.util.ObjectUtilities;

public class UnitPoint2D 
{
    private final UnitExpression x;
    private final UnitExpression y;

    public UnitPoint2D(UnitExpression x, UnitExpression y)
    {
        this.x = x;
        this.y = y;
    }

    public UnitExpression getX()
    {
        return x;
    }

    public UnitExpression getY()
    {
        return y;
    }

    @Override
    public int hashCode()
    {
        int hashCode = 17;

        hashCode = 31*hashCode + x.hashCode();
        hashCode = 31*hashCode + y.hashCode();

        return hashCode;
    }

    @Override
    public boolean equals(Object other)
    {
        if(!(other instanceof UnitPoint2D))
        {
            return false;
        }

        UnitPoint2D that = (UnitPoint2D)other;

        boolean equal = ObjectUtilities.equal(this.x, that.x) && ObjectUtilities.equal(this.y, that.y);

        return equal;
    }
}
