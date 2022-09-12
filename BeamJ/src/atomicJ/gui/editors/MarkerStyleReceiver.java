
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

package atomicJ.gui.editors;

import java.awt.Paint;
import java.awt.Stroke;

public interface MarkerStyleReceiver
{
    public int getMarkerIndex();
    public void setMarkerIndex(int i);

    public float getMarkerSize();
    public void setMarkerSize(float size);

    public Paint getMarkerFillPaint();

    public boolean getDrawMarkerOutline();
    public Stroke getMarkerOutlineStroke();
    public Paint getMarkerOutlinePaint();
}
