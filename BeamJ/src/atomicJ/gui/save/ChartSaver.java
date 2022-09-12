
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


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;

public abstract class ChartSaver 
{
    public abstract void saveChart(JFreeChart chart, File path, ChartRenderingInfo info) throws IOException;

    public void saveChart(JFreeChart chart, String name, File dir, boolean ext, ChartRenderingInfo info) throws IOException 
    {
        File path = getOutputFile(name, dir, ext);
        saveChart(chart, path, info);
    }


    public abstract void writeChartToStream(JFreeChart chart, OutputStream out) throws IOException;

    public abstract String getExtension();

    protected File getOutputFile(String name, File dir, boolean ext)
    {
        String child = ext ? name + getExtension() : name;			
        return new File(dir, child);
    }
}
