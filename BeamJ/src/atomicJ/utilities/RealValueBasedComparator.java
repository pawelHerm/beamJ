
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Pawe� Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.utilities;

import java.util.Comparator;

public class RealValueBasedComparator<K> implements Comparator<K> 
{
    private final RealValuedFunction<K> funct;

    public RealValueBasedComparator(RealValuedFunction<K> funct)
    {
        this.funct = funct;
    }

    @Override
    public int compare(K arg0, K arg1) 
    {
        double v0 = funct.value(arg0);
        double v1 = funct.value(arg1);
        if(v0 > v1){return 1;}
        else if(v0<v1){return -1;}		
        else {return 0;}
    }
}
