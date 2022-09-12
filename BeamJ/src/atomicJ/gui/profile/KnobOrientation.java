package atomicJ.gui.profile;

public enum KnobOrientation 
{
    UP("Upward") {
        @Override
        public double getRotationAngle(int index) {
            return 0;
        }
    }, DOWN("Downward") {
        @Override
        public double getRotationAngle(int index) {
            return Math.PI;
        }
    }, ALTERNATE("Alternate") 
    {
        @Override
        public double getRotationAngle(int index) 
        {
            double angle = index % 2 == 0 ? 0 : Math.PI;
            return angle;
        }
    };

    private final String name;

    private KnobOrientation(String name)
    {
        this.name = name;
    }

    public abstract double getRotationAngle(int index);

    @Override
    public String toString()
    {
        return name;
    }
}
