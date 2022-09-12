
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

package atomicJ.gui.save;



import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;


import org.jfree.chart.JFreeChart;

public class PPMFormatSaver extends BasicImageFormatSaver
{
    private static final String EXT = ".ppm";


    public PPMFormatSaver(Rectangle2D chartInitialArea, int width, int height, boolean saveDataArea)
    {
        super(chartInitialArea, width, height, saveDataArea);
    }

    @Override
    public String getExtension() 
    {
        return EXT;
    }

    @Override
    public void writeChartToStream(JFreeChart chart, OutputStream out) throws IOException
    {
        FlexibleImageGraphics2D g2d = new FlexibleImageGraphics2D(out,new Dimension(getWidth(),getHeight()),"ppm");
        g2d.startExport();

        paintOnGraphicsDevice(chart, g2d);
        g2d.endExportWithoutClosingStream();
    }
}
