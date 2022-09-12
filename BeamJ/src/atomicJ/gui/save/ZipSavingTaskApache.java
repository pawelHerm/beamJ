
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Paweł Hermanowicz
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

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;


import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import atomicJ.analysis.*;

public class ZipSavingTaskApache extends MonitoredSwingWorker<Void, Void> 
{
    private final List<StreamSavable> savables;

    private final File path;
    private final Component parent;

    private int savedChartCount;

    private Exception exception;

    private ZipSavingTaskApache(Component parent, List<StreamSavable> savables, File path)
    {
        super(parent, "Saving to archive in progress", "Saved", savables.size());
        this.savables = savables;
        this.path = path;
        this.parent = parent;
    }

    public static ZipSavingTaskApache getSavingTask(List<StreamSavable> savables, File path, Component parent)
    {
        if(path.isDirectory())
        {
            throw new IllegalArgumentException("The 'path' cannot be a directory");
        }

        ZipSavingTaskApache task = new ZipSavingTaskApache(parent, savables, path);
        return task;
    }

    @Override
    public Void doInBackground() 
    {                
        int n = savables.size();

        try(ZipArchiveOutputStream zos = new ZipArchiveOutputStream(path);)
        {		
            for(int i = 0;i<n;i++)
            {
                StreamSavable savable = savables.get(i);

                if(!isCancelled())
                {
                    String name = savable.getName();
                    ZipArchiveEntry entry = new ZipArchiveEntry(name);
                    zos.putArchiveEntry(entry);

                    savable.save(zos);
                    zos.closeArchiveEntry();

                    this.savedChartCount = i + 1;

                    setStep(i + 1);
                }
                else
                {
                    break;
                }
            }
        } 
        catch (IOException e) 
        {
            exception = e;
        } 

        return null;
    }

    @Override
    protected void done()
    {
        super.done();

        if(isCancelled())
        {
            JOptionPane.showMessageDialog(parent, "Saving terminated. Saved " + 
                    savedChartCount + " charts", "AtomicJ", JOptionPane.INFORMATION_MESSAGE);
        }	
        else if(exception != null)
        {
            closeProgressMonitor();
            exception.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error occured during saving files in the archive\n" 
                    + exception.getMessage() + "\n Saving terminated. Saved " + savedChartCount + " charts",
                    "AtomicJ", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void cancelAllTasks() 
    {
        cancel(false);
    }
}
