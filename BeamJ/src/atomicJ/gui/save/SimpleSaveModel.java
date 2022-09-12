
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

package atomicJ.gui.save;

import static atomicJ.gui.save.SaveModelProperties.*;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;


import org.jfree.chart.JFreeChart;
import org.jfree.util.ObjectUtilities;

import atomicJ.gui.AbstractModel;
import atomicJ.gui.ExtensionFileChooser;
import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.IOUtilities;

public class SimpleSaveModel extends AbstractModel implements PropertyChangeListener
{
    private File filePath;

    private ChartSaveFormatType currentFormatType;

    private final ChartSaveFormatType[] formatTypes;
    private boolean formatParametersSpecified;
    private boolean inputSpecified;

    //ZIPPABLE

    private final FileNameExtensionFilter zipFilter = new FileNameExtensionFilter("ZIP archive", "zip");
    private boolean saveInArchiveEnabled;
    private boolean saveInArchive;

    private FileNameExtensionFilter currentFileFilter;

    public SimpleSaveModel()
    {
        this(new ChartSaveFormatType[] 
                {new EPSFormatType(), new PSFormatType(), new PDFFormatType(), 
                        new SVGFormatType(), new TIFFFormatType(), new EMFFormatType(),
                        new JPEGFormatType(), new JPEG2000FormatType(), new PNGFormatType(),
                        new GIFFormatType(), new PPMFormatType(),new BMPFormatType(),
                        new CSVFormatType(), new TSVFormatType()});
    }

    public SimpleSaveModel(ChartSaveFormatType[] formatTypes)
    {
        this.formatTypes = formatTypes;
        initDefaults();
        checkIfSaveInArchiveEnabled();
        checkIfNecessaryInputProvided();

    }

    private void initDefaults()
    {
        this.filePath = null;
        this.currentFormatType = formatTypes[0];
        this.formatParametersSpecified = currentFormatType.isNecessaryIputProvided();
        currentFormatType.addPropertyChangeListener(this);

        updateFileFilter();
    }

    public boolean isNecessaryInputProvided()
    {
        return inputSpecified;
    }

    public ChartSaveFormatType[] getFormatTypes()
    {
        return formatTypes;
    }

    public File getFilePath()
    {
        return filePath;
    }

    public void setFilePath(File filePathNew)
    {
        if(filePathNew != null && filePathNew.isDirectory())
        {
            throw new IllegalArgumentException("File object passed as the 'file' argument should not be a directory");
        }

        File filePathOld = this.filePath;
        this.filePath = filePathNew;

        firePropertyChange(FILE_PATH, filePathOld, filePathNew);

        checkIfNecessaryInputProvided();
    }

    public ChartSaveFormatType getSaveFormat()
    {
        return currentFormatType;
    }


    private ChartSaveFormatType getFormatType(String description)
    {
        ChartSaveFormatType formatType = null;

        if(description == null)
        {
            return formatType;
        }

        for(ChartSaveFormatType type : formatTypes)
        {
            String typeDescription = type.getDescription();

            if(typeDescription.equals(description))
            {
                formatType = type;
                break;
            }
        }

        return formatType;
    }

    public void setSaveFormat(String descriptionSaveFormatNew)
    {
        ChartSaveFormatType saveFormatNew = getFormatType(descriptionSaveFormatNew);
        if(saveFormatNew != null)
        {
            setSaveFormat(saveFormatNew);
        }
    }

    public void setSaveFormat(ChartSaveFormatType saveFormatNew)
    {
        if(saveFormatNew == null)
        {
            throw new IllegalArgumentException("Null 'saveFormatNew' parameter");
        }

        ChartSaveFormatType saveFormatOld = currentFormatType;
        this.currentFormatType = saveFormatNew;

        saveFormatOld.removePropertyChangeListener(this);
        saveFormatNew.addPropertyChangeListener(this);

        firePropertyChange(SAVE_FORMAT, saveFormatOld, saveFormatNew);

        checkIfSaveInArchiveEnabled();
        checkPathExtensionCorrectness();
        checkIfNecessaryInputProvided();
        updateFileFilter();
    }

    private void checkPathExtensionCorrectness()
    {
        String ext = saveInArchive ?  "zip" : currentFormatType.getExtension();
        if(filePath != null)
        {
            String[] expectedExtensions =  saveInArchive ? new String[] {"zip"} : currentFormatType.getFileNameExtensionFilter().getExtensions();
            File correctExtFile = ExtensionFileChooser.ensureCorrectExtension(filePath, expectedExtensions, ext);
            setFilePath(correctExtFile);
        }
    }

    private void checkIfNecessaryInputProvided()
    {
        boolean inputSpecifiedNew = checkIfFormatParametersProvided() && checkIfFilePathProvded();		
        boolean inputSpecifiedOld = inputSpecified;
        this.inputSpecified = inputSpecifiedNew;

        firePropertyChange(INPUT_PROVIDED, inputSpecifiedOld, inputSpecifiedNew);
    }

    private boolean checkIfFilePathProvded()
    {
        boolean filePathProvided = filePath != null;

        return filePathProvided;
    }


    private boolean checkIfFormatParametersProvided()
    {
        boolean formatParametersSpecifiedNew = currentFormatType.isNecessaryIputProvided();		
        this.formatParametersSpecified = formatParametersSpecifiedNew;
        return formatParametersSpecified;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String poperty = evt.getPropertyName();
        if(FORMAT_PARAMETERS_PROVIDED.equals(poperty))
        {
            checkIfNecessaryInputProvided();
        }

        firePropertyChange(evt);
    }

    public boolean isSavePathFree()
    {
        boolean free = filePath == null || !filePath.exists();

        return free;
    }

    public void save(JFreeChart chart)  throws UserCommunicableException
    {
        ChartSaver saver = currentFormatType.getChartSaver();
        try 
        {
            boolean inArchive = this.saveInArchive && saver instanceof ZippableFrameFormatSaver;
            if(inArchive)
            {
                String entryName = IOUtilities.getBareName(filePath) + 
                        "." + currentFormatType.getExtension();

                ((ZippableFrameFormatSaver)saver).saveAsZip(chart, filePath, entryName, null);
            }
            else
            {
                saver.saveChart(chart, filePath, null);
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured during saving the file");
        }
    }

    public boolean isSaveInArchiveEnabled()
    {
        return saveInArchiveEnabled;
    }

    private void checkIfSaveInArchiveEnabled()
    {
        boolean saveInArchiveEnabledOld = this.saveInArchiveEnabled;

        ChartSaver saver = currentFormatType.getChartSaver();

        boolean saveInArchiveEnabledNew = 
                (saver instanceof ZippableFrameFormatSaver)
                && ((ZippableFrameFormatSaver)saver).isZippable();

        this.saveInArchiveEnabled = saveInArchiveEnabledNew;

        firePropertyChange(SAVE_IN_ARCHIVE_ENABLED, saveInArchiveEnabledOld, saveInArchiveEnabledNew);

        if(!this.saveInArchiveEnabled && saveInArchive)
        {
            setSaveInArchive(false);
        }
    }

    public boolean isSaveInArchive()
    {
        return saveInArchive;
    }

    public void setSaveInArchive(boolean saveInArchiveNew)
    {
        boolean saveInArchiveOld = saveInArchive;
        this.saveInArchive = saveInArchiveNew;

        firePropertyChange(SAVE_IN_ARCHIVE, saveInArchiveOld, saveInArchive);

        checkPathExtensionCorrectness();
        updateFileFilter();
    }

    public FileNameExtensionFilter getFileFilter()
    {
        return currentFileFilter;
    }

    private void updateFileFilter()
    {
        FileNameExtensionFilter fileFilterNew = saveInArchive ? zipFilter : currentFormatType.getFileNameExtensionFilter();

        if(!ObjectUtilities.equal(fileFilterNew, this.currentFileFilter))
        {
            FileNameExtensionFilter fileFilterOld = this.currentFileFilter;
            this.currentFileFilter = fileFilterNew;

            firePropertyChange(FILE_FILTER, fileFilterOld, fileFilterNew);
        }
    }
}
