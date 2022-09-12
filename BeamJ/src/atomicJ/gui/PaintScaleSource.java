
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

package atomicJ.gui;

import java.awt.geom.Point2D;

public interface PaintScaleSource extends GradientPaintReceiver
{
    public GradientPaintScale getPaintScale();
    public boolean hasDepthAxis();
    public CustomizableNumberAxis getDepthAxis();
    public ColorGradientLegend getColorGradientLegend();
    public void setColorGradientLegend(ColorGradientLegend ps);
    public void requestPaintScaleChange(Point2D java2DPoint, double percent);
}
