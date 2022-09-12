package atomicJ.analysis;

import java.util.Arrays;
import java.util.List;

import atomicJ.utilities.AbscissaComparator;
import atomicJ.utilities.ReverseAbscissaComparator;

public enum SortedArrayOrder
{
    ASCENDING 
    {
        //in place
        @Override
        public double[][] sortXIfNecessary(double[][] points)
        {
            int n = points.length;

            if(n<2)
            {
                return points;
            }

            boolean xAscending = SortedArrayOrder.ASCENDING.equals(SortedArrayOrder.getInitialXOrder(points)) && SortedArrayOrder.ASCENDING.equals(SortedArrayOrder.getOverallXOrder(points));

            //if it is not ascending, we sort
            if(!xAscending)
            {
                Arrays.sort(points, new AbscissaComparator());
            }

            return points;
        }

        @Override
        public double[][] sortX(double[][] points)
        {
            Arrays.sort(points, new AbscissaComparator());

            return points;
        }
    }, 

    DESCENDING 
    {
        @Override
        public double[][] sortXIfNecessary(double[][] points) 
        {
            int n = points.length;

            if(n < 2)
            {
                return points;
            }

            boolean xDescending = SortedArrayOrder.DESCENDING.equals(SortedArrayOrder.getInitialXOrder(points)) && SortedArrayOrder.DESCENDING.equals(SortedArrayOrder.getOverallXOrder(points));

            //if it is not descending, we sort
            if(!xDescending)
            {
                Arrays.sort(points, new ReverseAbscissaComparator());
            }

            return points;
        }

        @Override
        public double[][] sortX(double[][] points) 
        {
            Arrays.sort(points, new ReverseAbscissaComparator());
            return points;
        }
    };

    public abstract double[][] sortX(double[][] points);
    public abstract double[][] sortXIfNecessary(double[][] points);

    public static SortedArrayOrder getInitialXOrder(double[][] sortedPoints)
    {
        boolean xDescending = isInitiallyXDescending(sortedPoints);
        SortedArrayOrder order = xDescending ? DESCENDING : ASCENDING;

        return order;
    }

    public static SortedArrayOrder getInitialXOrder(List<double[]> sortedPoints)
    {
        boolean xDescending = isInitiallyXDescending(sortedPoints);
        SortedArrayOrder order = xDescending ? DESCENDING : ASCENDING;

        return order;
    }

    public static SortedArrayOrder getOverallXOrder(double[][] sortedPoints)
    {
        boolean xDescending = isXOverallDescending(sortedPoints);
        SortedArrayOrder order = xDescending ? DESCENDING : ASCENDING;

        return order;
    }

    public static SortedArrayOrder getOverallXOrder(List<double[]> sortedPoints)
    {
        boolean xDescending = isXOverallDescending(sortedPoints);
        SortedArrayOrder order = xDescending ? DESCENDING : ASCENDING;

        return order;
    }

    private static boolean isXOverallDescending(double[][] points)
    {
        int n = points.length;

        if(n < 2)
        {
            return true;
        }

        boolean descending = points[0][0] > points[n - 1][0];

        return descending;
    }

    private static boolean isXOverallDescending(List<double[]> points)
    {
        int n = points.size();

        if(n < 2)
        {
            return true;
        }

        boolean descending = points.get(0)[0] > points.get(n - 1)[0];

        return descending;
    }

    private static boolean isInitiallyXDescending(double[][] points)
    {
        int n = points.length;

        boolean descending = false;
        double previous = points[0][0];

        for(int i = 0; i<n; i++)
        {
            double current = points[i][0];
            if(current != previous)
            {
                descending = previous > current;
                return descending;
            }
            else
            {
                previous = current;
            }
        }

        return descending;
    }

    private static boolean isInitiallyXDescending(List<double[]> points)
    {
        int n = points.size();

        boolean descending = false;
        double previous = points.get(0)[0];

        for(int i = 0; i<n; i++)
        {
            double current = points.get(i)[0];
            if(current != previous)
            {
                descending = previous > current;
                return descending;
            }
            else
            {
                previous = current;
            }
        }

        return descending;
    }

    public static int getIndexOfFirstAscendingX(double[][] points)
    {
        int n = points.length;

        double previous = points[0][0];

        for(int i = 0; i<n; i++)
        {
            double current = points[i][0];
            if(current <= previous)
            {
                previous = current;
            }
            else 
            {
                return i;
            }
        }

        return n;
    }

    public static int getIndexOfFirstAscendingXResistantToIsolatedPoints(double[][] points)
    {
        int n = points.length;

        double previous = points[0][0];

        for(int i = 0; i<n - 1; i++)
        {
            double current = points[i][0];
            if(current <= previous || current >= points[i+1][0]) //to make it resistant to isolated points
            {
                previous = current;
            }
            else 
            {
                return i;
            }
        }

        return n;
    }

    public static int getIndexOfFirstDescendingX(double[][] points)
    {
        int n = points.length;

        double previous = points[0][0];

        for(int i = 0; i<n; i++)
        {
            double current = points[i][0];
            if(current >= previous)
            {
                previous = current;
            }
            else 
            {
                return i;
            }
        }

        return n;
    }

    public static int getIndexOfFirstDescendingXResistantToIsolatedPoints(double[][] points)
    {
        int n = points.length;

        double previous = points[0][0];

        for(int i = 0; i<n - 1; i++)
        {
            double current = points[i][0];
            if(current >= previous || current <= points[i+1][0])
            {
                previous = current;
            }
            else 
            {
                return i;
            }
        }

        return n;
    }
}