package atomicJ.algorithms;

/******************************************************************************
 *  Compilation:  javac GrahamaScan.java
 *  Execution:    java GrahamScan < input.txt
 *  Dependencies: Point2D.java
 * 
 *  Create points from standard input and compute the convex hull using
 *  Graham scan algorithm.
 *
 *  May be floating-point issues if x- and y-coordinates are not integers.
 *
 ******************************************************************************/

import java.util.Arrays;
import java.util.Stack;


/**
 *  The <tt>GrahamScan</tt> data type provides methods for computing the 
 *  convex hull of a set of <em>N</em> points in the plane.
 *  <p>
 *  The implementation uses the Graham-Scan convex hull algorithm.
 *  It runs in O(<em>N</em> log <em>N</em>) time in the worst case
 *  and uses O(<em>N</em>) extra memory.
 *  <p>
 *  For additional documentation, see <a href="http://algs4.cs.princeton.edu/99scientific">Section 9.9</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */

/******************************************************************************
 *  Copyright 2002-2015, Robert Sedgewick and Kevin Wayne.
 *
 *  This file is part of algs4.jar, which accompanies the textbook
 *
 *      Algorithms, 4th edition by Robert Sedgewick and Kevin Wayne,
 *      Addison-Wesley Professional, 2011, ISBN 0-321-57351-X.
 *      http://algs4.cs.princeton.edu
 *
 *
 *  algs4.jar is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  algs4.jar is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with algs4.jar.  If not, see http://www.gnu.org/licenses.
 ******************************************************************************/
public class GrahamScan {
    private final Stack<Point2D> hull = new Stack<Point2D>();

    /**
     * Computes the convex hull of the specified array of points.
     *
     * @param  pts the array of points
     * @throws NullPointerException if <tt>points</tt> is <tt>null</tt> or if any
     *         entry in <tt>points[]</tt> is <tt>null</tt>
     */
    public GrahamScan(Point2D[] pts) {

        // defensive copy
        int N = pts.length;
        Point2D[] points = new Point2D[N];
        for (int i = 0; i < N; i++)
            points[i] = pts[i];

        // preprocess so that points[0] has lowest y-coordinate; break ties by x-coordinate
        // points[0] is an extreme point of the convex hull
        // (alternatively, could do easily in linear time)
        Arrays.sort(points);

        // sort by polar angle with respect to base point points[0],
        // breaking ties by distance to points[0]
        Arrays.sort(points, 1, N, points[0].polarOrder());

        hull.push(points[0]);       // p[0] is first extreme point

        // find index k1 of first point not equal to points[0]
        int k1;
        for (k1 = 1; k1 < N; k1++)
            if (!points[0].equals(points[k1])) break;
        if (k1 == N) return;        // all points equal

        // find index k2 of first point not collinear with points[0] and points[k1]
        int k2;
        for (k2 = k1 + 1; k2 < N; k2++)
            if (Point2D.ccw(points[0], points[k1], points[k2]) != 0) break;
        hull.push(points[k2-1]);    // points[k2-1] is second extreme point

        // Graham scan; note that points[N-1] is extreme point different from points[0]
        for (int i = k2; i < N; i++) {
            Point2D top = hull.pop();
            while (Point2D.ccw(hull.peek(), top, points[i]) <= 0) {
                top = hull.pop();
            }
            hull.push(top);
            hull.push(points[i]);
        }
    }

    /**
     * Returns the extreme points on the convex hull in counterclockwise order.
     *
     * @return the extreme points on the convex hull in counterclockwise order
     */
    public double[][] hull()
    {
        int n = hull.size();
        double[][] hullPoints = new double[n][];
        for (int i = 0; i<n; i++) 
        {
            Point2D p = hull.get(i);
            hullPoints[i] = new double[] {p.x(), p.y()};
        }

        return hullPoints;
    }
}