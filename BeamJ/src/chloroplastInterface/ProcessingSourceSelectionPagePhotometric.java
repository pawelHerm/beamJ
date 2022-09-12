
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Pawe³ Hermanowicz
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

package chloroplastInterface;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import atomicJ.gui.AbstractMultipleSourceSelectionPage;
import atomicJ.gui.WizardPage;
import atomicJ.gui.curveProcessing.ProcessingBatchModelInterface;


public class ProcessingSourceSelectionPagePhotometric extends AbstractMultipleSourceSelectionPage<SimplePhotometricSource> implements PropertyChangeListener, WizardPage
{
    private static final long serialVersionUID = 1L;
    private static final String TASK_NAME = "Curve selection";
    private static final String TASK_DESCRIPTION = "Select files containing curves to process";

    public ProcessingSourceSelectionPagePhotometric(ProcessingModel model)
    {
        super(model, PhotometricCurveReadingModel.getInstance(), "Batch no ", true, "Preprocess");
    }

    @Override
    public String getTaskName() 
    {
        return TASK_NAME;
    }

    @Override
    public String getTaskDescription() 
    {
        return TASK_DESCRIPTION;
    }

    @Override
    public String getIdentifier()
    {
        return ProcessingBatchModelInterface.SOURCES;
    }

    @Override
    public boolean isLast() 
    {
        return false;
    }

    @Override
    public boolean isFirst()
    {
        return true;
    }

    @Override
    protected void handleUnreadImages(List<File> unreadImages) 
    {
        int unreadImagesCount = unreadImages.size();

        if(unreadImagesCount>0)
        {
            String ending = (unreadImagesCount == 1) ? "": "s";
            JOptionPane.showMessageDialog(this, unreadImagesCount + " file" + ending + " contained image data instead of curves", "AtomicJ", JOptionPane.ERROR_MESSAGE);
        }		
    }
}
